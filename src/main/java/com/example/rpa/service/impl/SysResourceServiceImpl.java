package com.example.rpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rpa.dto.AddResourceRequest;
import com.example.rpa.dto.QueryResourceRequest;
import com.example.rpa.dto.UpdateResourceRequest;
import com.example.rpa.entity.SysResource;
import com.example.rpa.exception.BusinessException;
import com.example.rpa.mapper.SysResourceMapper;
import com.example.rpa.mapper.SysRoleResourceMapper;
import com.example.rpa.service.SysResourceService;
import com.example.rpa.vo.ResourceListItemVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class SysResourceServiceImpl implements SysResourceService {

    @Autowired
    private SysResourceMapper sysResourceMapper;

    @Autowired
    private SysRoleResourceMapper sysRoleResourceMapper;

    @Override
    public Page<ResourceListItemVO> queryResourceList(QueryResourceRequest request) {
        Page<SysResource> page = new Page<>(request.getCurrent(), request.getSize());
        
        LambdaQueryWrapper<SysResource> wrapper = new LambdaQueryWrapper<>();
        
        if (request.getResourceName() != null && !request.getResourceName().isEmpty()) {
            wrapper.like(SysResource::getResourceName, request.getResourceName());
        }
        if (request.getResourceCode() != null && !request.getResourceCode().isEmpty()) {
            wrapper.like(SysResource::getResourceCode, request.getResourceCode());
        }
        if (request.getStatus() != null) {
            wrapper.eq(SysResource::getStatus, request.getStatus());
        }
        
        wrapper.orderByAsc(SysResource::getSort);
        
        Page<SysResource> resourcePage = sysResourceMapper.selectPage(page, wrapper);
        
        Page<ResourceListItemVO> resultPage = new Page<>(resourcePage.getCurrent(), resourcePage.getSize());
        resultPage.setTotal(resourcePage.getTotal());
        
        List<ResourceListItemVO> voList = new ArrayList<>();
        for (SysResource resource : resourcePage.getRecords()) {
            ResourceListItemVO vo = new ResourceListItemVO();
            vo.setId(resource.getId());
            vo.setParentId(resource.getParentId());
            vo.setResourceCode(resource.getResourceCode());
            vo.setResourceName(resource.getResourceName());
            vo.setResourceType(resource.getResourceType());
            vo.setUrl(resource.getUrl());
            vo.setIcon(resource.getIcon());
            vo.setSort(resource.getSort());
            vo.setStatus(resource.getStatus());
            vo.setCreateTime(resource.getCreateTime());
            
            if (resource.getParentId() != null) {
                SysResource parent = sysResourceMapper.selectById(resource.getParentId());
                if (parent != null) {
                    vo.setParentResourceName(parent.getResourceName());
                }
            }
            
            voList.add(vo);
        }
        resultPage.setRecords(voList);
        
        return resultPage;
    }

    @Override
    public SysResource getResourceById(Long id) {
        SysResource resource = sysResourceMapper.selectById(id);
        if (resource == null) {
            throw new BusinessException("资源不存在");
        }
        return resource;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addResource(AddResourceRequest request) {
        validateResourceRequest(request.getParentId(), request.getResourceType(), request.getStatus(), null);

        LambdaQueryWrapper<SysResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysResource::getResourceCode, request.getResourceCode());
        Long count = sysResourceMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("资源编码已存在");
        }
        
        SysResource resource = new SysResource();
        resource.setParentId(request.getParentId());
        resource.setResourceCode(request.getResourceCode());
        resource.setResourceName(request.getResourceName());
        resource.setResourceType(request.getResourceType());
        resource.setUrl(request.getUrl());
        resource.setIcon(request.getIcon());
        resource.setSort(request.getSort() != null ? request.getSort() : 0);
        resource.setStatus(request.getStatus());
        resource.setCreateTime(LocalDateTime.now());
        resource.setUpdateTime(LocalDateTime.now());
        resource.setDeleted(0);
        
        sysResourceMapper.insert(resource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateResource(UpdateResourceRequest request) {
        SysResource existResource = getResourceById(request.getId());
        validateResourceRequest(request.getParentId(), request.getResourceType(), request.getStatus(), request.getId());
        
        if (!existResource.getResourceCode().equals(request.getResourceCode())) {
            LambdaQueryWrapper<SysResource> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysResource::getResourceCode, request.getResourceCode());
            wrapper.ne(SysResource::getId, request.getId());
            Long count = sysResourceMapper.selectCount(wrapper);
            if (count > 0) {
                throw new BusinessException("资源编码已存在");
            }
        }
        
        existResource.setParentId(request.getParentId());
        existResource.setResourceCode(request.getResourceCode());
        existResource.setResourceName(request.getResourceName());
        existResource.setResourceType(request.getResourceType());
        existResource.setUrl(request.getUrl());
        existResource.setIcon(request.getIcon());
        existResource.setSort(request.getSort());
        existResource.setStatus(request.getStatus());
        existResource.setUpdateTime(LocalDateTime.now());
        
        sysResourceMapper.updateById(existResource);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteResource(Long id) {
        getResourceById(id);
        
        LambdaQueryWrapper<SysResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysResource::getParentId, id);
        Long childCount = sysResourceMapper.selectCount(wrapper);
        if (childCount > 0) {
            throw new BusinessException("该资源下存在子资源，无法删除");
        }
        
        sysRoleResourceMapper.deleteByResourceId(id);
        sysResourceMapper.physicalDeleteById(id);
    }

    @Override
    public List<SysResource> getParentResources() {
        return sysResourceMapper.selectParentResources();
    }

    private void validateResourceRequest(Long parentId, String resourceType, Integer status, Long currentId) {
        if (resourceType == null || !Set.of("menu", "button", "api").contains(resourceType)) {
            throw new BusinessException("资源类型不合法");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("资源状态不合法");
        }

        if (parentId == null) {
            return;
        }
        if (currentId != null && parentId.equals(currentId)) {
            throw new BusinessException("父级资源不能选择自身");
        }

        SysResource parent = sysResourceMapper.selectById(parentId);
        if (parent == null) {
            throw new BusinessException("父级资源不存在");
        }
        if (!"menu".equals(parent.getResourceType())) {
            throw new BusinessException("父级资源必须是菜单类型");
        }

        if (currentId != null && hasCircularParent(parentId, currentId)) {
            throw new BusinessException("父级资源不能选择当前资源的子资源");
        }
    }

    private boolean hasCircularParent(Long parentId, Long currentId) {
        Long cursor = parentId;
        while (cursor != null) {
            if (cursor.equals(currentId)) {
                return true;
            }
            SysResource parent = sysResourceMapper.selectById(cursor);
            if (parent == null) {
                return false;
            }
            cursor = parent.getParentId();
        }
        return false;
    }
}
