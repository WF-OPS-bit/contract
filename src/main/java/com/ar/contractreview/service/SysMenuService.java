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
}
