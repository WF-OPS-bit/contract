package com.ar.contractreview.service.impl;

import com.ar.contractreview.entity.SysRoleMenu;
import com.ar.contractreview.mapper.SysRoleMenuMapper;
import com.ar.contractreview.service.SysRoleMenuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 角色菜单关联表 服务实现类
 * </p>
 *
 * @author wyh
 * @since 2026-09-03
 */
@Service
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> implements SysRoleMenuService {

    @Autowired
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Override
    public List<Long> selectMenuIdsByRoleId(Long roleId) {
        return sysRoleMenuMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    public void batchInsert(Long roleId, List<Long> menuIds) {
        sysRoleMenuMapper.batchInsert(roleId, menuIds);
    }

    @Override
    public void deleteByRoleId(Long roleId) {
        sysRoleMenuMapper.deleteByRoleId(roleId);
    }
}
