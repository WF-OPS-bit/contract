package com.ar.contractreview.service;

import com.ar.contractreview.entity.SysRoleMenu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 角色菜单关联表 服务类
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
public interface SysRoleMenuService extends IService<SysRoleMenu> {

    /**
     * 查询角色的菜单ID列表
     *
     * @param roleId 角色ID
     * @return 菜单ID列表
     */
    List<Long> selectMenuIdsByRoleId(Long roleId);

    /**
     * 批量插入角色-菜单关联
     *
     * @param roleId  角色ID
     * @param menuIds 菜单ID列表
     */
    void batchInsert(Long roleId, List<Long> menuIds);

    /**
     * 删除角色的全部菜单关联
     *
     * @param roleId 角色ID
     */
    void deleteByRoleId(Long roleId);
}
