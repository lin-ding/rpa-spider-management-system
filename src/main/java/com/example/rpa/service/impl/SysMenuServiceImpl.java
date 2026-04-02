package com.example.rpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.rpa.entity.SysMenu;
import com.example.rpa.exception.BusinessException;
import com.example.rpa.mapper.SysMenuMapper;
import com.example.rpa.service.SysMenuService;
import com.example.rpa.vo.MenuTreeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单服务实现类
 */
@Service
@RequiredArgsConstructor
public class SysMenuServiceImpl implements SysMenuService {

    private final SysMenuMapper sysMenuMapper;

    @Override
    public List<MenuTreeVO> getMenuTree() {
        List<SysMenu> allMenus = sysMenuMapper.selectList(new LambdaQueryWrapper<>());
        
        List<MenuTreeVO> menuTreeVOS = allMenus.stream()
                .map(this::convertToMenuTreeVO)
                .sorted(Comparator.comparing(MenuTreeVO::getSortOrder))
                .collect(Collectors.toList());
        
        return buildTree(menuTreeVOS, 0L);
    }

    @Override
    public SysMenu getMenuById(Long id) {
        SysMenu menu = sysMenuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException("菜单不存在");
        }
        return menu;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMenu(SysMenu menu) {
        if (!checkMenuNameUnique(menu)) {
            throw new BusinessException("菜单名称已存在");
        }
        
        menu.setCreateTime(LocalDateTime.now());
        menu.setUpdateTime(LocalDateTime.now());
        menu.setDeleted(0);
        
        sysMenuMapper.insert(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(SysMenu menu) {
        SysMenu existMenu = getMenuById(menu.getId());
        
        if (!existMenu.getMenuName().equals(menu.getMenuName()) && !checkMenuNameUnique(menu)) {
            throw new BusinessException("菜单名称已存在");
        }
        
        menu.setUpdateTime(LocalDateTime.now());
        sysMenuMapper.updateById(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long id) {
        getMenuById(id);
        sysMenuMapper.deleteById(id);
    }

    @Override
    public boolean checkMenuNameUnique(SysMenu menu) {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysMenu::getMenuName, menu.getMenuName());
        if (menu.getId() != null) {
            wrapper.ne(SysMenu::getId, menu.getId());
        }
        Long count = sysMenuMapper.selectCount(wrapper);
        return count == 0;
    }

    private MenuTreeVO convertToMenuTreeVO(SysMenu menu) {
        MenuTreeVO vo = new MenuTreeVO();
        vo.setId(menu.getId());
        vo.setParentId(menu.getParentId());
        vo.setLabel(menu.getMenuName());
        vo.setPath(menu.getPath());
        vo.setComponent(menu.getComponent());
        vo.setPermission(menu.getPermission());
        vo.setIcon(menu.getIcon());
        vo.setType(menu.getMenuType());
        vo.setSortOrder(menu.getSortOrder());
        vo.setChildren(new ArrayList<>());
        return vo;
    }

    private List<MenuTreeVO> buildTree(List<MenuTreeVO> nodes, Long parentId) {
        return nodes.stream()
                .filter(node -> node.getParentId().equals(parentId))
                .peek(node -> node.setChildren(buildTree(nodes, node.getId())))
                .collect(Collectors.toList());
    }
}
