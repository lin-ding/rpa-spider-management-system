package com.example.rpa.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.dto.AddResourceRequest;
import com.example.rpa.dto.QueryResourceRequest;
import com.example.rpa.dto.UpdateResourceRequest;
import com.example.rpa.entity.SysResource;
import com.example.rpa.vo.ResourceListItemVO;

import java.util.List;

public interface SysResourceService {

    Page<ResourceListItemVO> queryResourceList(QueryResourceRequest request);

    SysResource getResourceById(Long id);

    void addResource(AddResourceRequest request);

    void updateResource(UpdateResourceRequest request);

    void deleteResource(Long id);

    List<SysResource> getParentResources();
}
