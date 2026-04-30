package com.example.rpa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.rpa.dto.LoginRequest;
import com.example.rpa.entity.SysResource;
import com.example.rpa.entity.SysUser;
import com.example.rpa.entity.SysRole;
import com.example.rpa.exception.BusinessException;
import com.example.rpa.mapper.SysResourceMapper;
import com.example.rpa.mapper.SysUserMapper;
import com.example.rpa.mapper.SysRoleMapper;
import com.example.rpa.service.AuthService;
import com.example.rpa.util.JwtUtil;
import com.example.rpa.vo.LoginResponse;
import com.example.rpa.vo.RoleInfoVO;
import com.example.rpa.vo.UserInfoVO;
import com.example.rpa.vo.MenuTreeVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {
    private static final long AVATAR_MAX_SIZE = 2 * 1024 * 1024;
    private static final String AVATAR_URL_PREFIX = "/api/uploads/avatars/";
    private static final Set<String> ALLOWED_AVATAR_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private SysRoleMapper sysRoleMapper;
    @Autowired
    private SysResourceMapper sysResourceMapper;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.upload.avatar-dir:uploads/avatars}")
    private String avatarUploadDir;

    /**
     * 用户登录 - 包含用户名校验、密码比对、状态检查、JWT生成
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = sysUserMapper.selectByUsername(request.getUsername());
        
        if (user == null) {
            SysUser deletedUser = sysUserMapper.selectByUsernameIncludeDeleted(request.getUsername());
            if (deletedUser != null && deletedUser.getDeleted() == 1) {
                throw new BusinessException("账号不存在");
            }
            throw new BusinessException("用户名或密码错误");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        if (user.getStatus() != 1) {
            throw new BusinessException("用户已被禁用，请联系管理员");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        List<SysRole> userRoles = sysRoleMapper.selectRolesByUserId(user.getId());
        List<String> roleCodes = userRoles.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());
        List<String> permissions = sysResourceMapper.selectResourceCodesByUserId(user.getId());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .userId(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .avatar(user.getAvatar())
                .roles(roleCodes)
                .permissions(permissions)
                .build();
    }


    @Override
    public UserInfoVO getCurrentUserInfo(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        ensureActiveUser(user);
        
        // 构建用户信息VO
        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(user.getPhone());
        userInfo.setAvatar(user.getAvatar());
        
        List<SysRole> userRoles = sysRoleMapper.selectRolesByUserId(userId);
        List<RoleInfoVO> roles = userRoles.stream()
                .map(this::toRoleInfoVO)
                .collect(Collectors.toList());
        userInfo.setRoles(roles);
        userInfo.setPermissions(sysResourceMapper.selectResourceCodesByUserId(userId));
        
        return userInfo;
    }

    @Override
    public List<MenuTreeVO> getMenuTree(Long userId) {
        List<SysRole> roles = sysRoleMapper.selectRolesByUserId(userId);
        boolean superAdmin = roles.stream()
                .anyMatch(role -> "super_admin".equals(role.getRoleCode()));

        LambdaQueryWrapper<SysResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysResource::getResourceType, "menu")
                .eq(SysResource::getStatus, 1)
                .orderByAsc(SysResource::getSort);

        if (!superAdmin) {
            List<String> permissions = sysResourceMapper.selectResourceCodesByUserId(userId);
            if (permissions.isEmpty()) {
                return new ArrayList<>();
            }
            wrapper.in(SysResource::getResourceCode, permissions);
        }

        List<SysResource> resources = sysResourceMapper.selectList(wrapper);
        List<MenuTreeVO> nodes = resources.stream()
                .map(this::toMenuTreeVO)
                .sorted(Comparator.comparing(MenuTreeVO::getSortOrder, Comparator.nullsLast(Integer::compareTo)))
                .collect(Collectors.toList());
        return buildMenuTree(nodes, null);
    }

    @Override
    public boolean validateToken(String token) {
        if (!jwtUtil.validateToken(token)) {
            return false;
        }
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            ensureActiveUser(sysUserMapper.selectById(userId));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void logout(String token) {
        // 使Token失效，加入黑名单
        jwtUtil.invalidateToken(token);
    }

    @Override
    public void updateUserInfo(Long userId, String realName, String email, String phone) {
        SysUser user = sysUserMapper.selectById(userId);
        ensureActiveUser(user);
        
        user.setRealName(realName);
        user.setEmail(email);
        user.setPhone(phone);
        
        sysUserMapper.updateById(user);
    }

    @Override
    public String updateAvatar(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的头像文件");
        }
        if (file.getSize() > AVATAR_MAX_SIZE) {
            throw new BusinessException("头像文件不能超过 2MB");
        }

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !ALLOWED_AVATAR_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException("头像仅支持 JPG、PNG、GIF、WEBP 格式");
        }

        SysUser user = sysUserMapper.selectById(userId);
        ensureActiveUser(user);

        Path uploadPath = Paths.get(avatarUploadDir).toAbsolutePath().normalize();
        String extension = resolveAvatarExtension(file.getOriginalFilename(), contentType);
        String fileName = userId + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "") + extension;
        Path targetPath = uploadPath.resolve(fileName).normalize();
        if (!targetPath.startsWith(uploadPath)) {
            throw new BusinessException("头像文件名不合法");
        }

        try {
            Files.createDirectories(uploadPath);
            file.transferTo(targetPath);

            String oldAvatar = user.getAvatar();
            String avatarUrl = AVATAR_URL_PREFIX + fileName;
            user.setAvatar(avatarUrl);
            user.setUpdateTime(LocalDateTime.now());
            sysUserMapper.updateById(user);
            deleteOldLocalAvatar(oldAvatar, uploadPath, fileName);
            return avatarUrl;
        } catch (IOException e) {
            throw new BusinessException("头像上传失败，请稍后重试");
        }
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = sysUserMapper.selectById(userId);
        ensureActiveUser(user);
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserMapper.updateById(user);
    }

    private String resolveAvatarExtension(String originalFilename, String contentType) {
        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (StringUtils.hasText(extension)) {
            String normalized = extension.toLowerCase();
            if (Set.of("jpg", "jpeg", "png", "gif", "webp").contains(normalized)) {
                return "." + normalized;
            }
        }

        return switch (contentType.toLowerCase()) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            default -> throw new BusinessException("头像文件格式不合法");
        };
    }

    private void deleteOldLocalAvatar(String oldAvatar, Path uploadPath, String newFileName) {
        if (!StringUtils.hasText(oldAvatar) || !oldAvatar.startsWith(AVATAR_URL_PREFIX)) {
            return;
        }

        String oldFileName = oldAvatar.substring(AVATAR_URL_PREFIX.length());
        if (!StringUtils.hasText(oldFileName) || oldFileName.equals(newFileName)) {
            return;
        }

        Path oldPath = uploadPath.resolve(oldFileName).normalize();
        if (!oldPath.startsWith(uploadPath)) {
            return;
        }

        try {
            Files.deleteIfExists(oldPath);
        } catch (IOException ignored) {
            // 旧头像清理失败不影响新头像使用。
        }
    }

    private RoleInfoVO toRoleInfoVO(SysRole role) {
        RoleInfoVO vo = new RoleInfoVO();
        vo.setId(role.getId());
        vo.setRoleName(role.getRoleName());
        vo.setRoleCode(role.getRoleCode());
        vo.setDescription(role.getDescription());
        return vo;
    }

    private MenuTreeVO toMenuTreeVO(SysResource resource) {
        MenuTreeVO vo = new MenuTreeVO();
        vo.setId(resource.getId());
        vo.setParentId(resource.getParentId());
        vo.setLabel(resource.getResourceName());
        vo.setPath(resource.getUrl());
        vo.setPermission(resource.getResourceCode());
        vo.setIcon(resource.getIcon());
        vo.setType(resource.getParentId() == null ? 1 : 2);
        vo.setSortOrder(resource.getSort());
        vo.setChildren(new ArrayList<>());
        return vo;
    }

    private List<MenuTreeVO> buildMenuTree(List<MenuTreeVO> nodes, Long parentId) {
        return nodes.stream()
                .filter(node -> parentId == null ? node.getParentId() == null : parentId.equals(node.getParentId()))
                .peek(node -> node.setChildren(buildMenuTree(nodes, node.getId())))
                .collect(Collectors.toList());
    }

    private void ensureActiveUser(SysUser user) {
        if (user == null) {
            throw new BusinessException(401, "用户不存在或已删除");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BusinessException(403, "用户已被禁用，请联系管理员");
        }
    }
}
