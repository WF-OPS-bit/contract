package com.ar.contractreview.controller;

import com.ar.contractreview.entity.SysRole;
import com.ar.contractreview.entity.SysUser;
import com.ar.contractreview.result.R;
import com.ar.contractreview.result.ResponseCode;
import com.ar.contractreview.security.service.IJwtService;
import com.ar.contractreview.service.SysRoleService;
import com.ar.contractreview.service.SysUserService;
import com.ar.contractreview.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;



/**
 * <p>
 * 系统用户表 前端控制器
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@RestController
@RequestMapping("/user")
public class SysUserController {

    @Autowired
    SysUserService userService;

    @Autowired
    SysRoleService roleService;

    @Autowired
    IJwtService jwtService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @GetMapping("/list")
    public R list(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "roleId", required = false) Long roleId,
            @RequestParam(value = "department", required = false) String department
    ){
        QueryWrapper<SysUser> wrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(username)){
            wrapper.like("username",username);
        }
        if (!StringUtils.isEmpty(name)){
            wrapper.like("name",name);
        }
        if (roleId != null) {
            wrapper.eq("role_id", roleId);
        }
        if (!StringUtils.isEmpty(department)) {
            wrapper.like("department", department);
        }

        IPage<SysUser> pageResult = userService.page(new Page<>(page,size),wrapper);

        // 用户列表同样不能把密码哈希发出去
        pageResult.getRecords().forEach(u -> u.setPassword(null));

        return R.ok()
                .data("list",pageResult.getRecords())
                .data("total",pageResult.getTotal());
    }

    /**
     * 2.3 获取当前用户信息
     * <p>
     * GET /user/info —— MainLayout.vue 刷新页面时调用，用于恢复登录状态。
     * </p>
     * <p>
     * 修复了两个问题：
     * 1. 原来返回 {@code R.ok().data("data", user)}，响应体里多套了一层 data，
     *    前端拿到的对象没有 roleId，导致菜单加载失败；
     * 2. 原来直接把 SysUser 实体返回，把 BCrypt 密码哈希也发给了前端 —— 严重的信息泄露。
     *    现在按《接口文档》2.3 只输出必要字段。
     * </p>
     */
    @GetMapping("/info")
    public R info(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication == null ? null : String.valueOf(authentication.getPrincipal());
        if (StringUtils.isEmpty(username)) {
            return R.fail(ResponseCode.USER_INVALIDATE_EXCEPTION);
        }
        SysUser user = userService.getOne(new QueryWrapper<SysUser>().eq("username", username));
        if (user == null) {
            return R.fail(ResponseCode.DATA_NOT_EXIST.getCode(), "用户不存在");
        }

        // 角色编码（ADMIN / LEGAL_MANAGER ...）：sys_user 只存 role_id，要去 sys_role 取
        String roleCode = "";
        if (user.getRoleId() != null) {
            SysRole role = roleService.getById(user.getRoleId());
            if (role != null) {
                roleCode = role.getRoleCode();
            }
        }

        return R.ok()
                // 把请求头里的 token 原样回给前端，便于刷新页面后直接恢复登录态
                .data("token", jwtService.getToken(request))
                .data("id", user.getId())
                .data("username", user.getUsername())
                .data("name", user.getName())
                .data("role", roleCode)
                .data("roleName", user.getRoleName())
                .data("roleId", user.getRoleId())
                .data("department", user.getDepartment());
    }

    /**
     * 2.5 新增用户
     * <p>按《接口文档》2.5 返回 {id, username, name, roleId, roleName, department}，
     * 不再回传密码哈希，也不再多套一层 data。</p>
     */
    @PostMapping
    public R add(@RequestBody SysUser user){
        if (StringUtils.isEmpty(user.getUsername()) || StringUtils.isEmpty(user.getPassword())) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "用户名、密码为必填项");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus((byte)1);
        fillRoleName(user);
        userService.save(user);
        return R.ok().data("id", user.getId())
                .data("username", user.getUsername())
                .data("name", user.getName())
                .data("roleId", user.getRoleId())
                .data("roleName", user.getRoleName())
                .data("department", user.getDepartment());
    }

    /**
     * 2.6 更新用户
     * <p>密码不允许通过该接口修改（有专门的重置/改密接口），这里显式置空避免被覆盖。</p>
     */
    @PutMapping("/{id}")
    public R update(@PathVariable("id") Long id,@RequestBody SysUser user){
        user.setId(id);
        user.setPassword(null);
        if (user.getRoleId() != null) {
            fillRoleName(user);
        }
        userService.updateById(user);
        return R.ok().data("id", id)
                .data("name", user.getName())
                .data("roleId", user.getRoleId())
                .data("roleName", user.getRoleName())
                .data("department", user.getDepartment());
    }

    /**
     * 2.7 删除用户 —— 按文档返回 data: true
     */
    @DeleteMapping("/{id}")
    public R delete(@PathVariable("id") Long id){
        userService.removeById(id);
        return R.ok().data(true);
    }

    /**
     * 2.8 重置密码（管理员操作，不需要旧密码）—— 按文档返回 data: true
     */
    @PutMapping("/{id}/resetPassword")
    public R resetPassword(@PathVariable("id") Long id,@RequestBody SysUser user){
        if (user == null || StringUtils.isEmpty(user.getPassword())) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "新密码不能为空");
        }
        SysUser update = new SysUser();
        update.setId(id);
        update.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.updateById(update);
        return R.ok().data(true);
    }

    /**
     * 修改当前登录用户自己的密码。
     * <p>
     * 与 2.8 的区别：这个是「本人改密」，必须校验旧密码；2.8 是管理员重置，不校验。
     * 顶部头像下拉菜单里的「修改密码」走的就是这个接口。
     * </p>
     */
    @PutMapping("/password")
    public R changePassword(@RequestBody Map<String, String> body){
        if (body == null) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "参数不能为空");
        }
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (StringUtils.isEmpty(oldPassword) || StringUtils.isEmpty(newPassword)) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "原密码和新密码都不能为空");
        }
        if (newPassword.length() < 6) {
            return R.fail(ResponseCode.PARAMETER_EXCEPTION.getCode(), "新密码长度不能少于 6 位");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication == null ? null : String.valueOf(authentication.getPrincipal());
        SysUser user = userService.getOne(new QueryWrapper<SysUser>().eq("username", username));
        if (user == null) {
            return R.fail(ResponseCode.DATA_NOT_EXIST.getCode(), "用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return R.fail(ResponseCode.PASSWORD_EXCEPTION.getCode(), "原密码不正确");
        }

        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setPassword(passwordEncoder.encode(newPassword));
        userService.updateById(update);
        return R.ok().data(true);
    }

    /**
     * 根据 roleId 补齐 roleName，避免依赖前端传值（前端只选角色ID）。
     */
    private void fillRoleName(SysUser user) {
        if (user.getRoleId() == null) return;
        SysRole role = roleService.getById(user.getRoleId());
        if (role != null) {
            user.setRoleName(role.getRoleName());
        }
    }
}
