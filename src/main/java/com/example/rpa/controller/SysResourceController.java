package com.example.rpa.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.annotation.RequireAdmin;
import com.example.rpa.dto.AddResourceRequest;
import com.example.rpa.dto.QueryResourceRequest;
import com.example.rpa.dto.UpdateResourceRequest;
import com.example.rpa.entity.SysResource;
import com.example.rpa.common.Result;
import com.example.rpa.service.SysResourceService;
import com.example.rpa.vo.ResourceListItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/resource")
@Tag(name = "资源管理", description = "提供资源分页查询、详情、父级资源查询和资源增删改接口")
public class SysResourceController {

    @Autowired
    private SysResourceService sysResourceService;

    @GetMapping("/query")
    @RequireAdmin("查询资源列表")
    @Operation(summary = "分页查询资源列表", description = "按条件分页查询资源数据，并返回资源列表项分页结果")
    public Result<Page<ResourceListItemVO>> queryResourceList(QueryResourceRequest request) {
        Page<ResourceListItemVO> page = sysResourceService.queryResourceList(request);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @RequireAdmin("查询资源详情")
    @Operation(summary = "查询资源详情", description = "根据资源主键 ID 查询资源详细信息")
    public Result<SysResource> getResourceById(@Parameter(description = "资源主键 ID", required = true)
                                               @PathVariable Long id) {
        SysResource resource = sysResourceService.getResourceById(id);
        return Result.success(resource);
    }

    @GetMapping("/parents")
    @RequireAdmin("查询父级资源")
    @Operation(summary = "查询父级资源列表", description = "查询可作为父级节点的资源列表，用于新增或编辑资源时选择上级资源")
    public Result<List<SysResource>> getParentResources() {
        List<SysResource> parents = sysResourceService.getParentResources();
        return Result.success(parents);
    }

    @PostMapping
    @RequireAdmin("新增资源")
    @Operation(summary = "新增资源", description = "新增菜单、按钮或接口资源信息")
    public Result<Void> addResource(@Valid @RequestBody AddResourceRequest request) {
        sysResourceService.addResource(request);
        return Result.success();
    }

    @PutMapping
    @RequireAdmin("修改资源")
    @Operation(summary = "修改资源", description = "根据请求体中的资源 ID 修改资源信息")
    public Result<Void> updateResource(@Valid @RequestBody UpdateResourceRequest request) {
        sysResourceService.updateResource(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    @RequireAdmin("修改资源")
    @Operation(summary = "按路径修改资源", description = "根据路径中的资源 ID 修改资源信息，适用于 REST 风格更新接口")
    public Result<Void> updateResourceById(@Parameter(description = "资源主键 ID", required = true)
                                           @PathVariable Long id,
                                           @Valid @RequestBody UpdateResourceRequest request) {
        request.setId(id);
        sysResourceService.updateResource(request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireAdmin("删除资源")
    @Operation(summary = "删除资源", description = "根据资源主键 ID 删除资源信息")
    public Result<Void> deleteResource(@Parameter(description = "资源主键 ID", required = true)
                                       @PathVariable Long id) {
        sysResourceService.deleteResource(id);
        return Result.success();
    }
}
