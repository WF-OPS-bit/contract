package com.ar.contractreview.security.service.impl;

import com.ar.contractreview.security.bo.SecurityUser;
import com.ar.contractreview.security.mapper.MenuMapper;
import com.ar.contractreview.security.mapper.RoleMapper;
import com.ar.contractreview.mapper.SysUserMapper;
import com.ar.contractreview.entity.SysMenu;
import com.ar.contractreview.entity.SysRole;
import com.ar.contractreview.entity.SysUser;
import com.ar.contractreview.security.service.IUserService;
import com.ar.contractreview.utils.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author xiaobobo
 * @title: UserService
 * @projectName ar_26_springboot-parent
 * @description: 这个是用户的业务逻辑类
 * @date 2026/9/3  9:04
 */
@Service
@Slf4j
public class UserService implements IUserService, UserDetailsService {

    @Autowired
    private SysUserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private MenuMapper menuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //通过用户名查询用户数据
        if(StringUtils.isEmpty(username)){
            throw new UsernameNotFoundException("用户名不存在");
        }
        //执行到这里说明没有问题
        QueryWrapper<SysUser> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("username",username);
        SysUser user = userMapper.selectOne(userQueryWrapper);
        if(null==user){
            log.info("用户名不存在:{}",user.getUsername());
            throw new UsernameNotFoundException("用户名不存在");
        }
        //程序执行到这里 说明查询到数据了 那么接下来就应该通过用户id查询用户具有的权限和角色
        List<SysRole> roles = roleMapper.listRoleByUserId(user.getId());
        List<SysMenu> menus = menuMapper.listMenuByUserId(user.getId());
        log.info("查询到的权限信息是{}",menus);
        log.info("查询到的角色信息是:{}",roles);
        //将上面的角色信息，权限信息 弄成整体的List<String>
        List<String> roleAndPerms=wrapRoleAndPerm(roles,menus);

        SecurityUser securityUser = new SecurityUser();
        securityUser.setUser(user);
        securityUser.setRoleAndPerms(roleAndPerms);
        log.info("封装完成:{}",securityUser);
        return securityUser;
    }

    /**
     * 将角色信息菜单信息弄成List集合
     * @param roles
     * @param menus
     * @return
     */
    private List<String> wrapRoleAndPerm(List<SysRole> roles, List<SysMenu> menus) {
        List<String> roleAndMenuList=new ArrayList<>();
        if(null!=roles){
            for (SysRole r:roles){
                roleAndMenuList.add("ROLE_"+r.getRoleName());
            }
        }
        if(null!=menus){
            for (SysMenu m:menus){
                String permission = m.getPermission();
                if(!StringUtils.isEmpty(permission)){
                    roleAndMenuList.add(permission);
                }
            }
        }
        log.info("权限和角色信息封装成功:{}",roleAndMenuList);
        return roleAndMenuList;
    }

}
