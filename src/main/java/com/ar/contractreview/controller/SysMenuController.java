package com.ar.contractreview.controller;

import com.ar.contractreview.entity.SysMenu;
import com.ar.contractreview.result.R;
import com.ar.contractreview.service.SysMenuService;
import com.ar.contractreview.vo.SysMenuTreeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 系统菜单表 前端控制器
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@RestController
@RequestMapping("/menu")
public class SysMenuController {
    @Autowired
    public SysMenuService sysMenuService;

    /**
     * 获取菜单列表
     * @return
     */
    @GetMapping("/list")
    public R list(){
        List<SysMenu> menus = sysMenuService.listMenus();
        Map<String,Object> data = new HashMap<>();
        data.put("list",menus);
        return R.ok().data(data);
    }

    /**
     * 获取菜单树
     * @return
     */
    @GetMapping("/tree")
    public R tree(){
        List<SysMenu> tree = sysMenuService.listMenus();
        Map<String,Object> data = new HashMap<>();
        data.put("tree",tree);
        return R.ok().data(data);
    }
    @PostMapping
    public R add(@RequestBody SysMenu menu){
        SysMenu saved = sysMenuService.addMenu(menu);
        Map<String,Object> data = new HashMap<>();
        data.put("id",saved.getId());
        data.put("parentId",saved.getParentId());
        data.put("menuName",saved.getMenuName());
        return R.ok().data(data);
    }

    /**
     * 获取菜单详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R detail(@PathVariable(value = "id") Long id){
        SysMenu menu = sysMenuService.getMenuById(id);
        if (null==menu){
            return R.fail(404,"菜单不存在");
        }
        Map<String,Object> data = new HashMap<>();
        data.put("id",menu.getId());
        data.put("parentId",menu.getParentId());
        data.put("menuName",menu.getMenuName());
        data.put("menuType",menu.getMenuType());
        data.put("path",menu.getPath());
        data.put("component",menu.getComponent());
        data.put("icon",menu.getIcon());
        data.put("permission",menu.getPermission());
        data.put("sortOrder",menu.getSortOrder());
        data.put("status",menu.getStatus());
        return R.ok().data(data);
    }


}
