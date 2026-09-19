package com.ar.contractreview.controller;

import com.ar.contractreview.entity.SysUser;
import com.ar.contractreview.result.R;
import com.ar.contractreview.service.SysUserService;
import com.ar.contractreview.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;



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

        return R.ok()
                .data("list",pageResult.getRecords())
                .data("total",pageResult.getTotal());
    }

    @GetMapping("/info")
    public R info(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (String) authentication.getPrincipal();
        SysUser user = userService.getOne(
                new QueryWrapper<SysUser>().eq("username", username)
        );
        return R.ok().data("data", user);
    }

    @PostMapping
    public R add(@RequestBody SysUser user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setStatus((byte)1);
        userService.save(user);
        return R.ok().data("data",user);
    }

    @PutMapping("/{id}")
    public R update(@PathVariable("id") Long id,@RequestBody SysUser user){
        user.setId(id);
        user.setPassword(null);
        userService.updateById(user);
        return R.ok().data("data",user);
    }

    @DeleteMapping("/{id}")
    public R delete(@PathVariable("id") Long id){
        userService.removeById(id);
        return R.ok().data("data",true);
    }

    @PutMapping("/{id}/resetPassword")
    public R resetPassword(@PathVariable("id") Long id,@RequestBody SysUser user){
        SysUser update = new SysUser();
        update.setId(id);
        update.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.updateById(update);
        return R.ok().data("data",true);
    }
}
