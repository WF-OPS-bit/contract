package com.ar.contractreview.controller;

import com.ar.contractreview.entity.SysMenu;
import com.ar.contractreview.result.R;
import com.ar.contractreview.result.ResponseCode;
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
    private SysMenuService sysMenuService;

    /**
     * 4.1 获取菜单列表
     * @return
     */
    @GetMapping("/list")
    public R list(){
        List<SysMenu> menus = sysMenuService.listMenus();
        return R.ok().data(menus);
    }

    /**
     * 4.2 获取菜单树
     * @return
     */
    @GetMapping("/tree")
    public R tree(){
        List<SysMenuTreeVO> tree = sysMenuService.treeMenus();
        return R.ok().data(tree);
    }

    /**
     * 4.3 获取角色菜单
     * @param roleId 角色ID
     * @return
     */
    @GetMapping("/byRole")
    public R byRole(@RequestParam(value = "roleId") Long roleId){
        List<SysMenu> menus = sysMenuService.listByRole(roleId);
        return R.ok().data(menus);
    }

    /**
     * 4.5 新增菜单
     * @param menu
     * @return
     */
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
     * 4.4 获取菜单详情
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R detail(@PathVariable("id") Long id){
        SysMenu menu = sysMenuService.getMenuById(id);
        if (null==menu){
            return R.fail(ResponseCode.NO_MENU_EXCEPTION);
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

    /**
     * 4.6 更新菜单
     * @param id 菜单ID
     * @param menu 菜单信息
     * @return
     */
    @PutMapping("/{id}")
    public R update(@PathVariable("id") Long id, @RequestBody SysMenu menu){
        SysMenu exist = sysMenuService.getMenuById(id);
        if (null == exist){
            return R.fail(ResponseCode.NO_MENU_EXCEPTION);
        }
        menu.setId(id);
        SysMenu updated = sysMenuService.updateMenu(menu);
        Map<String,Object> data = new HashMap<>();
        data.put("id",updated.getId());
        data.put("menuName",updated.getMenuName());
        return R.ok().data(data);
    }

    /**
     * 4.7 删除菜单
     * @param id 菜单ID
     * @return
     */
    @DeleteMapping("/{id}")
    public R delete(@PathVariable("id") Long id){
        SysMenu exist = sysMenuService.getMenuById(id);
        if (null == exist){
            return R.fail(ResponseCode.NO_MENU_EXCEPTION);
        }
        boolean removed = sysMenuService.deleteMenu(id);
        if (!removed){
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "菜单不存在或已删除");
        }
        return R.ok().data(true);
    }

}