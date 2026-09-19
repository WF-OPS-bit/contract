package com.ar.contractreview.controller;

import com.ar.contractreview.security.dto.RoleMenuRequest;
import com.ar.contractreview.security.dto.RoleSaveRequest;
import com.ar.contractreview.entity.SysRole;
import com.ar.contractreview.exception.BusinessException;
import com.ar.contractreview.result.R;
import com.ar.contractreview.result.ResponseCode;
import com.ar.contractreview.service.SysRoleService;
import com.ar.contractreview.vo.RoleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 系统角色表 前端控制器
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@RestController
@RequestMapping("/role")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    /**
     * 3.1 获取角色列表（分页，含用户数统计）
     * GET /role/list
     */
    @GetMapping("/list")
    public R list(@RequestParam(value = "page", defaultValue = "1") Integer page,
                  @RequestParam(value = "size", defaultValue = "10") Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
        if (size > 100) {
            size = 100;
        }
        List<RoleVO> list = sysRoleService.pageRoles(page, size);
        long total = sysRoleService.countRoles();
        return R.ok()
                .data("list", list)
                .data("total", total)
                .data("page", page)
                .data("size", size);
    }

    /**
     * 3.2 获取角色详情（含权限菜单ID列表）
     * GET /role/{id}
     */
    @GetMapping("/{id}")
    public R detail(@PathVariable Long id) {
        RoleVO vo = sysRoleService.getRoleDetail(id);
        return R.ok()
                .data("id", vo.getId())
                .data("roleName", vo.getRoleName())
                .data("roleCode", vo.getRoleCode())
                .data("description", vo.getDescription())
                .data("status", vo.getStatus())
                .data("menuIds", vo.getMenuIds());
    }

    /**
     * 3.3 新增角色
     * POST /role
     */
    @PostMapping
    public R add(@RequestBody RoleSaveRequest request) {
        SysRole role = sysRoleService.addRole(request);
        return R.ok()
                .data("id", role.getId())
                .data("roleName", role.getRoleName())
                .data("roleCode", role.getRoleCode());
    }

    /**
     * 3.4 更新角色
     * PUT /role/{id}
     */
    @PutMapping("/{id}")
    public R update(@PathVariable Long id, @RequestBody RoleSaveRequest request) {
        SysRole role = sysRoleService.updateRole(id, request);
        return R.ok()
                .data("id", role.getId())
                .data("roleName", role.getRoleName())
                .data("roleCode", role.getRoleCode());
    }

    /**
     * 3.5 删除角色（管理员角色不可删除；角色下存在用户时不可删除）
     * DELETE /role/{id}
     */
    @DeleteMapping("/{id}")
    public R delete(@PathVariable Long id) {
        sysRoleService.deleteRole(id);
        return R.ok().data("result", true);
    }

    /**
     * 3.6 分配角色菜单权限（全量覆盖）
     * POST /role/{id}/menus
     */
    @PostMapping("/{id}/menus")
    public R assignMenus(@PathVariable Long id, @RequestBody RoleMenuRequest request) {
        if (request == null || request.getMenuIds() == null) {
            throw new BusinessException(ResponseCode.PARAMETER_EXCEPTION.getCode(), "菜单ID列表不能为空");
        }
        sysRoleService.assignMenus(id, request.getMenuIds());
        return R.ok().data("result", true);
    }
}
