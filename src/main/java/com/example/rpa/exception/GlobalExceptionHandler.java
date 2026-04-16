package com.example.rpa.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Map<String, Object> handleBusinessException(BusinessException e) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", e.getCode());
        result.put("message", e.getMessage());
        result.put("success", false);
        return result;
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public Map<String, Object> handleValidationException(org.springframework.web.bind.MethodArgumentNotValidException e) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 400);
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");
        result.put("message", message);
        result.put("success", false);
        return result;
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public Map<String, Object> handleNotFoundException(NoHandlerFoundException e) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 404);
        result.put("message", "请求地址不存在");
        result.put("success", false);
        return result;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, Object> handleNoResourceFoundException(NoResourceFoundException e) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 404);
        result.put("message", "请求地址不存在: " + e.getResourcePath());
        result.put("success", false);
        return result;
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Map<String, Object> handleSecurityException(SecurityException e) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 403);
        result.put("message", e.getMessage() != null ? e.getMessage() : "权限不足");
        result.put("success", false);
        return result;
    }

    @ExceptionHandler(Exception.class)
    public Map<String, Object> handleException(Exception e) {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 500);
        result.put("message", e.getMessage() != null ? e.getMessage() : "系统内部错误");
        result.put("success", false);
        return result;
    }
}
