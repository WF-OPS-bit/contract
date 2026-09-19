package com.ar.contractreview.mapper;
import com.ar.contractreview.entity.SysRoleMenu;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
/**
 * <p>
 * 角色菜单关联表 Mapper 接口
 * </p>
 * @author wyh
 * @since 2026-09-03
 */
public interface SysRoleMenuMapper extends BaseMapper<SysRoleMenu> {
    // 根据角色ID查询菜单ID列表
    List<Long> selectMenuIdsByRoleId(@Param("roleId") Long roleId);

    // 批量插入角色-菜单关联
    int batchInsert(@Param("roleId") Long roleId, @Param("menuIds") List<Long> menuIds);

    // 根据角色ID删除关联
    int deleteByRoleId(@Param("roleId") Long roleId);
}
