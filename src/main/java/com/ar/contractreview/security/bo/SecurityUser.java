package com.ar.contractreview.security.bo;

import com.ar.contractreview.entity.SysUser;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author xiaobobo
 * @title: SecurityUser
 * @projectName ar_26_springboot-parent
 * @description: 这个对象就是Security中定义的User对象
 * @date 2026/9/2  9:58
 */
@Data
public class SecurityUser implements UserDetails {

    private SysUser user;

    //角色名字和权限名字的集合
    //备注：权限直接写名字  角色的话 ： ROLE_角色名字
    //这里存储的是从数据库查询出来的信息
    private List<String> roleAndPerms;

    //权限信息的集合
    // Security在进行权限校验的时候要调用的方法
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //这里还不知道写啥
        //就是把上面从数据库中查询出来的信息 封装成下面的对象 返回就可以了
        List<GrantedAuthority> lists=new ArrayList<>();
        if (null!=roleAndPerms){
            for (int i = 0; i <roleAndPerms.size() ; i++) {
                GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(roleAndPerms.get(i));
                lists.add(grantedAuthority);
            }
        }
        return lists;
    }

    //密码
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    //用户名
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    //是否没有过期
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    //是否没有被锁定
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    //密码是否没有过期
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    //是否是使能状态
    @Override
    public boolean isEnabled() {
        return true;
    }

}
