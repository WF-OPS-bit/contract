package com.ar.contractreview.service.impl;

import com.ar.contractreview.security.dto.RoleSaveRequest;
import com.ar.contractreview.entity.SysRole;
import com.ar.contractreview.entity.SysUser;
import com.ar.contractreview.exception.BusinessException;
import com.ar.contractreview.mapper.SysRoleMapper;
import com.ar.contractreview.mapper.SysUserMapper;
import com.ar.contractreview.result.ResponseCode;
import com.ar.contractreview.security.bo.SecurityUser;
import com.ar.contractreview.service.SysRoleMenuService;
import com.ar.contractreview.service.SysRoleService;
import com.ar.contractreview.vo.RoleVO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 系统角色表 服务实现类
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    /**
     * 系统管理员角色编码（不可删除）
     */
    private static final String ADMIN_ROLE_CODE = "ADMIN";

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Override
    public List<RoleVO> pageRoles(int page, int size) {
        int offset = (page - 1) * size;
        return sysRoleMapper.selectRolePage(offset, size);
    }

    @Override
    public long countRoles() {
        Long count = sysRoleMapper.countRolePage();
        return count == null ? 0L : count;
    }

    @Override
    public RoleVO getRoleDetail(Long id) {
        SysRole role = requireRole(id);
        RoleVO vo = new RoleVO();
        vo.setId(role.getId());
        vo.setRoleName(role.getRoleName());
        vo.setRoleCode(role.getRoleCode());
        vo.setDescription(role.getDescription());
        vo.setStatus(role.getStatus());
        List<Long> menuIds = sysRoleMenuService.selectMenuIdsByRoleId(id);
        vo.setMenuIds(menuIds == null ? new ArrayList<>() : menuIds);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysRole addRole(RoleSaveRequest request) {
        validateSaveRequest(request);
        // 角色编码唯一性校验
        checkRoleCodeUnique(request.getRoleCode().trim().toUpperCase(), null);

        String operator = currentUserName();
        LocalDateTime now = LocalDateTime.now();
        SysRole role = new SysRole();
        role.setRoleName(request.getRoleName().trim());
        role.setRoleCode(request.getRoleCode().trim().toUpperCase());
        role.setDescription(request.getDescription());
        role.setStatus((byte) 1);
        role.setCreatedBy(operator);
        role.setCreatedTime(now);
        role.setUpdatedBy(operator);
        role.setUpdatedTime(now);
        role.setDeleted((byte) 0);
        int rows = sysRoleMapper.insertRole(role);
        if (rows != 1) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "新增角色失败");
        }

        // 菜单权限关联（menuIds 为空时不建立关联）
        saveRoleMenus(role.getId(), request.getMenuIds());
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysRole updateRole(Long id, RoleSaveRequest request) {
        SysRole role = requireRole(id);
        validateSaveRequest(request);
        // 角色编码唯一性校验（排除自身）
        checkRoleCodeUnique(request.getRoleCode().trim().toUpperCase(), id);

        role.setRoleName(request.getRoleName().trim());
        role.setRoleCode(request.getRoleCode().trim().toUpperCase());
        role.setDescription(request.getDescription());
        role.setUpdatedBy(currentUserName());
        role.setUpdatedTime(LocalDateTime.now());
        int rows = sysRoleMapper.updateRole(role);
        if (rows != 1) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "更新角色失败");
        }

        // 请求体中显式传入 menuIds 时，全量重设菜单权限
        if (request.getMenuIds() != null) {
            saveRoleMenus(id, request.getMenuIds());
        }
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        SysRole role = requireRole(id);
        // 管理员角色不可删除
        if (ADMIN_ROLE_CODE.equals(role.getRoleCode())) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "系统管理员角色不可删除");
        }
        // 角色下存在用户时不可删除
        Long userCount = sysUserMapper.selectCount(
                Wrappers.<SysUser>lambdaQuery().eq(SysUser::getRoleId, id)
        );
        if (userCount != null && userCount > 0) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "该角色下存在用户，无法删除");
        }
        // 逻辑删除角色 + 清理菜单关联
        int rows = sysRoleMapper.deleteRole(id, currentUserName());
        if (rows != 1) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "删除角色失败");
        }
        sysRoleMenuService.deleteByRoleId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long id, List<Long> menuIds) {
        requireRole(id);
        saveRoleMenus(id, menuIds);
    }

    /**
     * 校验新增/更新角色的公共参数
     */
    private void validateSaveRequest(RoleSaveRequest request) {
        if (request == null) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "请求参数不能为空");
        }
        if (request.getRoleName() == null || request.getRoleName().trim().isEmpty()) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "角色名称不能为空");
        }
        if (request.getRoleCode() == null || request.getRoleCode().trim().isEmpty()) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "角色编码不能为空");
        }
    }

    /**
     * 角色编码唯一性校验
     *
     * @param roleCode 角色编码（已转为大写）
     * @param excludeId 需要排除的角色ID（更新时传，新增传 null）
     */
    private void checkRoleCodeUnique(String roleCode, Long excludeId) {
        SysRole exist = sysRoleMapper.selectByRoleCode(roleCode);
        // equals(null) 本身返回 false，无需对 excludeId 做前置 null 判断
        if (exist != null && !exist.getId().equals(excludeId)) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "角色编码已存在：" + roleCode);
        }
    }

    /**
     * 查询角色，不存在则抛出业务异常
     */
    private SysRole requireRole(Long id) {
        if (id == null) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "角色ID不能为空");
        }
        SysRole role = sysRoleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "角色不存在");
        }
        return role;
    }

    /**
     * 全量保存角色菜单关联：先删除旧关联，再批量插入新关联
     */
    private void saveRoleMenus(Long roleId, List<Long> menuIds) {
        sysRoleMenuService.deleteByRoleId(roleId);
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        // 去空 + 去重
        List<Long> distinctMenuIds = menuIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (!distinctMenuIds.isEmpty()) {
            sysRoleMenuService.batchInsert(roleId, distinctMenuIds);
        }
    }

    /**
     * 获取当前登录用户名，未登录（内部调用等场景）时返回 system
     */
    private String currentUserName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SecurityUser securityUser) {
            return securityUser.getUsername();
        }
        return "system";
    }
}
