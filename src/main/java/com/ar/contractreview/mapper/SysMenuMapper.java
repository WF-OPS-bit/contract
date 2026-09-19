package com.ar.contractreview.mapper;

import com.ar.contractreview.entity.SysMenu;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 系统菜单表 Mapper 接口
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    List<SysMenu> selectByList(QueryWrapper<SysMenu> wrapper);
}
