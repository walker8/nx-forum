package com.leyuz.uc.auth.permission;

import com.leyuz.common.exception.ValidationException;
import com.leyuz.uc.auth.permission.dataobject.PermissionStatusV;
import com.leyuz.uc.auth.permission.dataobject.PermissionTypeV;
import com.leyuz.uc.auth.permission.dto.PermissionCreateCmd;
import com.leyuz.uc.auth.permission.dto.PermissionDTO;
import com.leyuz.uc.auth.permission.dto.PermissionUpdateCmd;
import com.leyuz.uc.auth.permission.gateway.PermissionGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionApplication {
    private final PermissionGateway permissionGateway;

    public PermissionDTO getPermission(Long permId) {
        return permissionGateway.getById(permId)
                .map(this::convertToDTO)
                .orElseThrow(() -> new ValidationException("权限不存在"));
    }

    public List<PermissionDTO> listPermissions() {
        // 获取所有权限并转换为DTO
        List<PermissionDTO> allPermissions = permissionGateway.list().stream()
                .map(this::convertToDTO)
                .toList();

        // 按父节点分组
        Map<Long, List<PermissionDTO>> parentChildrenMap = allPermissions.stream()
                .collect(Collectors.groupingBy(perm -> perm.getParentId() != null ? perm.getParentId() : 0L));

        // 构建树形结构，从根节点(parentId = null或0)开始
        List<PermissionDTO> rootPermissions = parentChildrenMap.getOrDefault(0L, new ArrayList<>());
        rootPermissions.forEach(root -> buildPermissionTree(root, parentChildrenMap));

        // 对根节点进行排序
        rootPermissions.sort(Comparator.comparing(PermissionDTO::getPermOrder));

        return rootPermissions;
    }

    /**
     * 递归构建权限树
     */
    private void buildPermissionTree(PermissionDTO parent, Map<Long, List<PermissionDTO>> parentChildrenMap) {
        // 获取当前节点的子节点
        List<PermissionDTO> children = parentChildrenMap.getOrDefault(parent.getPermId(), new ArrayList<>());

        // 对子节点进行排序
        children.sort(Comparator.comparing(PermissionDTO::getPermOrder));

        // 设置子节点
        parent.setChildren(children);

        // 递归处理每个子节点
        children.forEach(child -> buildPermissionTree(child, parentChildrenMap));
    }

    public List<PermissionDTO> listPermissionsByRoleKey(String roleKey) {
        return permissionGateway.listByRoleKey(roleKey).stream()
                .map(this::convertToDTO)
                .toList();
    }

    @Transactional
    public boolean createPermission(PermissionCreateCmd cmd) {
        // 创建权限实体
        PermissionE permission = PermissionE.builder()
                .permName(cmd.getPermName())
                .perms(cmd.getPerms())
                .remark(cmd.getRemark())
                .parentId(cmd.getParentId())
                .permType(PermissionTypeV.of(cmd.getPermType()))
                .permOrder(cmd.getPermOrder())
                .build();

        // 领域校验
        permission.validate();

        // 业务规则校验
        if (permissionGateway.existsByPerms(cmd.getPerms())) {
            throw new ValidationException("权限标识已存在");
        }

        return permissionGateway.save(permission);
    }

    @Transactional
    public boolean updatePermission(PermissionUpdateCmd cmd) {
        PermissionE oldPermission = permissionGateway.getById(cmd.getPermId())
                .orElseThrow(() -> new ValidationException("权限不存在"));

        // 如果修改了权限标识，需要校验
        if (!oldPermission.getPerms().equals(cmd.getPerms())
                && permissionGateway.existsByPerms(cmd.getPerms())) {
            throw new ValidationException("权限标识已存在");
        }

        PermissionE newPermission = PermissionE.builder()
                .permId(cmd.getPermId())
                .parentId(cmd.getParentId())
                .permName(cmd.getPermName())
                .permStatus(PermissionStatusV.of(cmd.getPermStatus()))
                .perms(cmd.getPerms())
                .remark(cmd.getRemark())
                .permType(PermissionTypeV.of(cmd.getPermType()))
                .permOrder(cmd.getPermOrder())
                .build();

        return permissionGateway.update(newPermission);
    }

    @Transactional
    public boolean deletePermission(Long permId) {
        // 检查是否存在子权限
        List<PermissionE> children = permissionGateway.listByParentId(permId);
        if (!children.isEmpty()) {
            throw new ValidationException("存在子权限，无法删除");
        }

        return permissionGateway.removeById(permId);
    }

    @Transactional
    public boolean enablePermission(Long permId) {
        return permissionGateway.getById(permId)
                .map(permission -> permissionGateway.update(permission.enable()))
                .orElseThrow(() -> new ValidationException("权限不存在"));
    }

    @Transactional
    public boolean disablePermission(Long permId) {
        return permissionGateway.getById(permId)
                .map(permission -> permissionGateway.update(permission.disable()))
                .orElseThrow(() -> new ValidationException("权限不存在"));
    }

    private PermissionDTO convertToDTO(PermissionE permission) {
        if (permission == null) {
            return null;
        }
        PermissionDTO dto = new PermissionDTO();
        dto.setPermId(permission.getPermId());
        dto.setPermName(permission.getPermName());
        dto.setPerms(permission.getPerms());
        dto.setRemark(permission.getRemark());
        dto.setParentId(permission.getParentId());
        dto.setPermStatus(permission.getPermStatus().getCode());
        dto.setPermType(permission.getPermType().getCode());
        dto.setPermOrder(permission.getPermOrder());
        return dto;
    }

    public List<String> listPermsByRoleKey(String roleKey) {
        return permissionGateway.listPermsByRoleKey(roleKey);
    }

    /**
     * 获取所有权限标识
     *
     * @return
     */
    public List<String> listPerms() {
        return permissionGateway.listAllPerms();
    }

    public Map<String, String> listAllPermsMap() {
        return permissionGateway.listAllPermsMap();
    }
}