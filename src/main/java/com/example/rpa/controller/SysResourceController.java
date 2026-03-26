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
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/resource")
public class SysResourceController {

    @Autowired
    private SysResourceService sysResourceService;

    @GetMapping("/query")
    @RequireAdmin("查询资源列表")
    public Result<Page<ResourceListItemVO>> queryResourceList(QueryResourceRequest request) {
        Page<ResourceListItemVO> page = sysResourceService.queryResourceList(request);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    @RequireAdmin("查询资源详情")
    public Result<SysResource> getResourceById(@PathVariable Long id) {
        SysResource resource = sysResourceService.getResourceById(id);
        return Result.success(resource);
    }

    @GetMapping("/parents")
    @RequireAdmin("查询父级资源")
    public Result<List<SysResource>> getParentResources() {
        List<SysResource> parents = sysResourceService.getParentResources();
        return Result.success(parents);
    }

    @PostMapping
    @RequireAdmin("新增资源")
    public Result<Void> addResource(@Valid @RequestBody AddResourceRequest request) {
        sysResourceService.addResource(request);
        return Result.success();
    }

    @PutMapping
    @RequireAdmin("修改资源")
    public Result<Void> updateResource(@Valid @RequestBody UpdateResourceRequest request) {
        sysResourceService.updateResource(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    @RequireAdmin("修改资源")
    public Result<Void> updateResourceById(@PathVariable Long id, @Valid @RequestBody UpdateResourceRequest request) {
        request.setId(id);
        sysResourceService.updateResource(request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @RequireAdmin("删除资源")
    public Result<Void> deleteResource(@PathVariable Long id) {
        sysResourceService.deleteResource(id);
        return Result.success();
    }
}
