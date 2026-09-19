package com.ar.contractreview.service.impl;

import com.ar.contractreview.entity.SysMenu;
import com.ar.contractreview.mapper.SysMenuMapper;
import com.ar.contractreview.service.SysMenuService;
import com.ar.contractreview.vo.SysMenuTreeVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 系统菜单表 服务实现类
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Autowired
    private SysMenuMapper sysMenuMapper;

    @Override
    public List<SysMenu> listMenus() {
        LambdaQueryWrapper<SysMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysMenu::getSortOrder);

        return sysMenuMapper.selectList(wrapper);
    }

    @Override
    public List<SysMenuTreeVO> treeMenus() {
        //查询所有菜单
        List<SysMenu> allMenus = listMenus();
        //转成VO（带children）
        List<SysMenuTreeVO> allVOS = allMenus.stream().map(menu -> {
            SysMenuTreeVO vo = new SysMenuTreeVO();
            BeanUtils.copyProperties(menu,vo);
            vo.setChildren(new ArrayList<>());
            return vo;
        }).collect(Collectors.toList());
        //按parentID分组
        Map<Long,List<SysMenuTreeVO>> childrenMap = allVOS.stream().collect(Collectors.groupingBy(SysMenuTreeVO::getParentID));
        //组装子节点（把每个菜单的children塞进去）
        for (SysMenuTreeVO vo:allVOS){
            List<SysMenuTreeVO> children = childrenMap.get(vo.getId());
            if (null != children){
                vo.setChildren(children);
            }

        }
        //返回顶级菜单
        return childrenMap.getOrDefault(0L,new ArrayList<>());
    }


    @Override
    public SysMenu addMenu(SysMenu menu) {
        //设置默认值
        if (null == menu.getParentId()){
            menu.setParentId(0L);
        }
        if (null == menu.getSortOrder()){
            menu.setSortOrder(1);
        }
        if (null==menu.getStatus()){
            menu.getStatus((byte)1);
        }
        menu.setCreatedTime(LocalDateTime.now());
        return menu;
    }

    @Override
    public SysMenu getMenuById(Long id) {
        return sysMenuMapper.selectById(id);
    }
}
