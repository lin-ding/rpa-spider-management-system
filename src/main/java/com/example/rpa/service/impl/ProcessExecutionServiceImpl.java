package com.example.rpa.service.impl;

import com.example.rpa.entity.RpaProcess;
import com.example.rpa.exception.BusinessException;
import com.example.rpa.mapper.RpaProcessMapper;
import com.example.rpa.service.ProcessExecutionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilationFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ProcessExecutionServiceImpl implements ProcessExecutionService {

    private static final String LANGUAGE_GROOVY = "groovy";
    private static final String LANGUAGE_PYTHON = "python";
    private static final String LANGUAGE_JAVA = "java";

    @Autowired
    private RpaProcessMapper rpaProcessMapper;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public Map<String, Object> executeProcess(Long processId) {
        return executeProcess(processId, null);
    }

    @Override
    public Map<String, Object> executeProcess(Long processId, Map<String, Object> extraContext) {
        RpaProcess process = rpaProcessMapper.selectById(processId);
        if (process == null) {
            throw new BusinessException("流程不存在");
        }
        
        Map<String, Object> context = new HashMap<>();
        context.put("processId", processId);
        context.put("processName", process.getProcessName());
        context.put("processCode", process.getProcessCode());
        if (extraContext != null && !extraContext.isEmpty()) {
            context.putAll(extraContext);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("processId", processId);
        result.put("processName", process.getProcessName());
        List<Map<String, Object>> stageResults = new ArrayList<>();
        result.put("stageResults", stageResults);
        
        try {
            if (process.getProcessData() != null && !process.getProcessData().isEmpty()) {
                Map<String, Object> processData = objectMapper.readValue(process.getProcessData(), Map.class);
                List<Map<String, Object>> stages = (List<Map<String, Object>>) processData.get("stages");
                
                if (stages != null && !stages.isEmpty()) {
                    int stageIndex = 0;
                    for (Map<String, Object> stage : stages) {
                        String stageName = (String) stage.get("name");
                        String script = (String) stage.get("script");
                        LocalDateTime stageStartTime = LocalDateTime.now();
                        Map<String, Object> stageLog = new HashMap<>();
                        stageLog.put("stageOrder", stageIndex + 1);
                        stageLog.put("stageName", stageName);
                        stageLog.put("startTime", stageStartTime);
                        
                        log.info("执行阶段 [{}]: {}", stageIndex, stageName);
                        
                        try {
                            if (script != null && !script.trim().isEmpty()) {
                                String scriptLanguage = normalizeLanguage((String) stage.get("scriptLanguage"));
                                Map<String, Object> stageResult = executeStage(scriptLanguage, script, context);
                                context.putAll(stageResult);
                                log.info("阶段 [{}] 执行完成，结果: {}", stageName, stageResult);
                                stageLog.put("status", "success");
                                stageLog.put("stageResult", objectMapper.writeValueAsString(stageResult));
                                stageLog.put("logDetail", "阶段执行成功");
                                stageLog.put("scriptLanguage", scriptLanguage);
                            } else {
                                log.info("阶段 [{}] 无脚本，跳过", stageName);
                                stageLog.put("status", "success");
                                stageLog.put("logDetail", "阶段无脚本，已跳过");
                            }
                        } catch (Exception e) {
                            LocalDateTime stageEndTime = LocalDateTime.now();
                            stageLog.put("status", "failed");
                            stageLog.put("errorMessage", e.getMessage());
                            stageLog.put("logDetail", "阶段执行失败");
                            stageLog.put("endTime", stageEndTime);
                            stageLog.put("durationMs", Duration.between(stageStartTime, stageEndTime).toMillis());
                            stageResults.add(stageLog);
                            throw e;
                        }
                        LocalDateTime stageEndTime = LocalDateTime.now();
                        stageLog.put("endTime", stageEndTime);
                        stageLog.put("durationMs", Duration.between(stageStartTime, stageEndTime).toMillis());
                        stageResults.add(stageLog);
                        stageIndex++;
                    }
                    result.put("success", true);
                    result.put("message", "流程执行成功");
                    result.put("executedStages", stages.size());
                    result.put("context", context);
                } else {
                    result.put("success", false);
                    result.put("message", "流程没有配置阶段");
                }
            } else {
                result.put("success", false);
                result.put("message", "流程没有配置数据");
            }
        } catch (Exception e) {
            log.error("流程执行失败", e);
            result.put("success", false);
            result.put("message", "流程执行失败: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> executeStage(String script, Map<String, Object> context) {
        return executeStage(LANGUAGE_GROOVY, script, context);
    }

    @Override
    public Map<String, Object> executeStage(String scriptLanguage, String script, Map<String, Object> context) {
        String normalizedLanguage = normalizeLanguage(scriptLanguage);
        return switch (normalizedLanguage) {
            case LANGUAGE_PYTHON -> executePythonStage(script, context);
            case LANGUAGE_GROOVY -> executeGroovyStage(script, context);
            default -> throw new BusinessException("暂不支持的脚本语言: " + normalizedLanguage);
        };
    }

    private Map<String, Object> executeGroovyStage(String script, Map<String, Object> context) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            Binding binding = new Binding();
            if (context != null) {
                context.forEach(binding::setVariable);
                binding.setVariable("context", context);
            } else {
                binding.setVariable("context", new HashMap<>());
            }
            
            binding.setVariable("log", log);
            binding.setVariable("result", result);
            
            GroovyShell shell = new GroovyShell(binding);
            Object executionResult = shell.evaluate(script);
            
            if (executionResult != null) {
                if (executionResult instanceof Map) {
                    result.putAll((Map<String, Object>) executionResult);
                } else {
                    result.put("returnValue", executionResult);
                }
            }
            
            result.put("stageSuccess", true);
            
        } catch (Exception e) {
            log.error("脚本执行失败", e);
            result.put("stageSuccess", false);
            result.put("errorMessage", e.getMessage());
            throw new BusinessException("脚本执行失败: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public Map<String, Object> testScript(String script) {
        return testScript(LANGUAGE_GROOVY, script);
    }

    @Override
    public Map<String, Object> testScript(String scriptLanguage, String script) {
        Map<String, Object> result = new HashMap<>();
        String normalizedLanguage = normalizeLanguage(scriptLanguage);
        result.put("scriptLanguage", normalizedLanguage);
        if (!StringUtils.hasText(script)) {
            result.put("success", false);
            result.put("message", "脚本内容不能为空");
            return result;
        }

        try {
            switch (normalizedLanguage) {
                case LANGUAGE_GROOVY -> validateGroovySyntax(script);
                case LANGUAGE_PYTHON -> validatePythonSyntax(script);
                case LANGUAGE_JAVA -> validateJavaSyntax(script);
                default -> throw new BusinessException("暂不支持的脚本语言: " + normalizedLanguage);
            }
            result.put("success", true);
            result.put("message", normalizedLanguage.equals(LANGUAGE_JAVA) ? "Java 语法检查通过，当前正式执行仍建议使用 Groovy 或 Python" : "脚本语法检查通过");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "脚本检查失败: " + e.getMessage());
            result.put("error", e.getClass().getSimpleName());
        }

        return result;
    }

    private String normalizeLanguage(String scriptLanguage) {
        if (!StringUtils.hasText(scriptLanguage)) {
            return LANGUAGE_GROOVY;
        }
        return scriptLanguage.trim().toLowerCase();
    }

    private void validateGroovySyntax(String script) {
        try {
            Binding binding = new Binding();
            binding.setVariable("context", new HashMap<>());
            binding.setVariable("result", new HashMap<>());
            binding.setVariable("log", log);
            new GroovyShell(binding).parse(script);
        } catch (CompilationFailedException e) {
            throw new BusinessException(e.getMessage());
        } catch (Exception e) {
            throw new BusinessException(e.getMessage());
        }
    }

    private void validatePythonSyntax(String script) {
        Path wrapperPath = null;
        try {
            wrapperPath = Files.createTempFile("rpa-python-syntax-", ".py");
            Files.writeString(wrapperPath, buildPythonWrapper(script, true), StandardCharsets.UTF_8);
            Process process = new ProcessBuilder("python", "-m", "py_compile", wrapperPath.toString())
                    .redirectErrorStream(true)
                    .start();
            String output = readStream(process.getInputStream());
            boolean finished = process.waitFor(15, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new BusinessException("Python 语法检查超时");
            }
            if (process.exitValue() != 0) {
                throw new BusinessException(StringUtils.hasText(output) ? output.trim() : "Python 语法检查失败");
            }
        } catch (IOException e) {
            throw new BusinessException("无法执行 Python 语法检查: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("Python 语法检查被中断");
        } finally {
            deleteTempFile(wrapperPath);
            deleteCompiledPythonArtifact(wrapperPath);
        }
    }

    private void validateJavaSyntax(String script) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new BusinessException("当前运行环境不支持 Java 语法检查");
        }

        Path javaFile = null;
        try {
            javaFile = Files.createTempFile("RpaJavaStageCheck", ".java");
            Files.writeString(javaFile, buildJavaWrapper(script), StandardCharsets.UTF_8);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int result = compiler.run(null, outputStream, outputStream, javaFile.toString());
            if (result != 0) {
                throw new BusinessException(outputStream.toString(StandardCharsets.UTF_8).trim());
            }
        } catch (IOException e) {
            throw new BusinessException("无法执行 Java 语法检查: " + e.getMessage());
        } finally {
            deleteTempFile(javaFile);
            if (javaFile != null) {
                Path classFile = javaFile.getParent().resolve("RpaJavaStageCheck.class");
                deleteTempFile(classFile);
            }
        }
    }

    private Map<String, Object> executePythonStage(String script, Map<String, Object> context) {
        Path wrapperPath = null;
        Path contextPath = null;
        try {
            wrapperPath = Files.createTempFile("rpa-python-stage-", ".py");
            contextPath = Files.createTempFile("rpa-python-context-", ".json");
            Files.writeString(wrapperPath, buildPythonWrapper(script, false), StandardCharsets.UTF_8);
            Files.writeString(contextPath, objectMapper.writeValueAsString(context == null ? Map.of() : context), StandardCharsets.UTF_8);

            Process process = new ProcessBuilder("python", wrapperPath.toString(), contextPath.toString())
                    .redirectErrorStream(true)
                    .start();
            String output = readStream(process.getInputStream());
            boolean finished = process.waitFor(60, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new BusinessException("Python 脚本执行超时");
            }
            if (process.exitValue() != 0) {
                throw new BusinessException(StringUtils.hasText(output) ? output.trim() : "Python 脚本执行失败");
            }
            if (!StringUtils.hasText(output)) {
                throw new BusinessException("Python 脚本未返回结果");
            }

            Map<String, Object> result = objectMapper.readValue(output, Map.class);
            result.putIfAbsent("stageSuccess", true);
            return result;
        } catch (IOException e) {
            throw new BusinessException("Python 脚本执行失败: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("Python 脚本执行被中断");
        } finally {
            deleteTempFile(wrapperPath);
            deleteCompiledPythonArtifact(wrapperPath);
            deleteTempFile(contextPath);
        }
    }

    private String buildPythonWrapper(String script, boolean syntaxOnly) {
        StringBuilder builder = new StringBuilder();
        builder.append("import json\n");
        builder.append("import logging\n");
        builder.append("import sys\n");
        builder.append("import traceback\n");
        builder.append("logging.basicConfig(level=logging.INFO)\n");
        builder.append("log = logging.getLogger('rpa-stage')\n");
        builder.append("context = {}\n");
        builder.append("result = {}\n");
        if (!syntaxOnly) {
            builder.append("if len(sys.argv) > 1:\n");
            builder.append("    with open(sys.argv[1], 'r', encoding='utf-8') as f:\n");
            builder.append("        context = json.load(f)\n");
            builder.append("else:\n");
            builder.append("    context = {}\n");
            builder.append("result = {}\n");
            builder.append("try:\n");
            for (String line : script.split("\\R", -1)) {
                builder.append("    ").append(line).append("\n");
            }
            builder.append("    if 'stageSuccess' not in result:\n");
            builder.append("        result['stageSuccess'] = True\n");
            builder.append("    print(json.dumps(result, ensure_ascii=False))\n");
            builder.append("except Exception as exc:\n");
            builder.append("    print(str(exc) or traceback.format_exc(), file=sys.stderr)\n");
            builder.append("    sys.exit(1)\n");
            return builder.toString();
        }
        builder.append("\n");
        builder.append(script).append("\n");
        return builder.toString();
    }

    private String buildJavaWrapper(String script) {
        return """
                import java.util.*;

                public class RpaJavaStageCheck {
                    public static void main(String[] args) {
                        Map<String, Object> context = new HashMap<>();
                        Map<String, Object> result = new HashMap<>();
                """ + script + """
                    }
                }
                """;
    }

    private String readStream(InputStream inputStream) throws IOException {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private void deleteTempFile(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("删除临时文件失败: {}", path, e);
        }
    }

    private void deleteCompiledPythonArtifact(Path wrapperPath) {
        if (wrapperPath == null) {
            return;
        }
        File pycacheDir = wrapperPath.getParent().resolve("__pycache__").toFile();
        if (!pycacheDir.exists() || !pycacheDir.isDirectory()) {
            return;
        }
        File[] compiledFiles = pycacheDir.listFiles((dir, name) -> name.startsWith(wrapperPath.getFileName().toString().replace(".py", "")) && name.endsWith(".pyc"));
        if (compiledFiles == null) {
            return;
        }
        for (File compiledFile : compiledFiles) {
            if (!compiledFile.delete()) {
                log.warn("删除 Python 编译缓存失败: {}", compiledFile.getAbsolutePath());
            }
        }
        File[] remainingFiles = pycacheDir.listFiles();
        if (remainingFiles != null && remainingFiles.length == 0 && !pycacheDir.delete()) {
            log.warn("删除 __pycache__ 目录失败: {}", pycacheDir.getAbsolutePath());
        }
    }
}
