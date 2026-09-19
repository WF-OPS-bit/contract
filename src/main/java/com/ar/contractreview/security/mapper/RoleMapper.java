package com.ar.contractreview.security.mapper;

import com.ar.contractreview.entity.SysRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author xiaobobo
 * @title: RoleMapper
 * @projectName ar_26_springboot-parent
 * @description: TODO
 * @date 2026/9/3  9:36
 */
@Mapper
@Repository
public interface RoleMapper extends BaseMapper<SysRole> {

    /**
     * 通过用户id查询角色
     * @param userId
     * @return
     */
    List<SysRole> listRoleByUserId(Long userId);

}
