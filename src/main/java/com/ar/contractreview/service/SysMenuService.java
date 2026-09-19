package com.ar.contractreview.service;

import com.ar.contractreview.entity.SysMenu;
import com.ar.contractreview.vo.SysMenuTreeVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 系统菜单表 服务类
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
public interface SysMenuService extends IService<SysMenu> {
    /**
     * 获取菜单列表
     * @return
     */
    List<SysMenu> listMenus();
    /**
     * 获取菜单树
     */
    List<SysMenuTreeVO> treeMenus();

    /**
     * 新增菜单
     * @param menu
     * @return
     */
    SysMenu addMenu(SysMenu menu);

    /**
     * 获取菜单详情
     * @param id
     * @return
     */
    SysMenu getMenuById(Long id);

    /**
     * 4.3 根据角色ID获取菜单列表
     * @param roleId 角色ID
     * @return 菜单列表
     */
    List<SysMenu> listByRole(Long roleId);

    /**
     * 4.6 更新菜单
     * @param menu 菜单信息
     * @return 更新后的菜单
     */
    SysMenu updateMenu(SysMenu menu);

    /**
     * 4.7 删除菜单
     * @param id 菜单ID
     * @return 是否成功
     */
    boolean deleteMenu(Long id);
}