package com.ar.contractreview.security.mapper;

import com.ar.contractreview.entity.SysMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author xiaobobo
 * @title: MenuMapper
 * @projectName ar_26_springboot-parent
 * @description: TODO
 * @date 2026/9/3  9:52
 */
@Mapper
@Repository
public interface MenuMapper extends BaseMapper<SysMenu> {


    /**
     * 通过用户id查询数据
     * @param userId
     * @return
     */
    List<SysMenu> listMenuByUserId(Long userId);


}
