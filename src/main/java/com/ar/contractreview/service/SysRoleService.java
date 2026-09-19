package com.ar.contractreview.service;

import com.ar.contractreview.security.dto.RoleSaveRequest;
import com.ar.contractreview.entity.SysRole;
import com.ar.contractreview.vo.RoleVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 系统角色表 服务类
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 分页查询角色列表（含用户数统计）
     *
     * @param page 页码，从 1 开始
     * @param size 每页条数
     * @return 角色列表
     */
    List<RoleVO> pageRoles(int page, int size);

    /**
     * 查询角色总数
     *
     * @return 未删除的角色总数
     */
    long countRoles();

    /**
     * 获取角色详情（含权限菜单ID列表）
     *
     * @param id 角色ID
     * @return 角色详情
     */
    RoleVO getRoleDetail(Long id);

    /**
     * 新增角色（角色编码唯一校验 + 菜单权限关联）
     *
     * @param request 新增角色请求
     * @return 新增后的角色（含自增主键）
     */
    SysRole addRole(RoleSaveRequest request);

    /**
     * 更新角色（角色编码唯一校验，传入 menuIds 时全量重设菜单权限）
     *
     * @param id      角色ID
     * @param request 更新角色请求
     * @return 更新后的角色
     */
    SysRole updateRole(Long id, RoleSaveRequest request);

    /**
     * 删除角色（管理员角色不可删除；角色下存在用户时不可删除）
     *
     * @param id 角色ID
     */
    void deleteRole(Long id);

    /**
     * 分配角色菜单权限（全量覆盖）
     *
     * @param id      角色ID
     * @param menuIds 菜单ID列表
     */
    void assignMenus(Long id, List<Long> menuIds);
}
