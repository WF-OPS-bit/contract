package com.ar.contractreview.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SysMenuTreeVO {

    private Long id;
    private Long parentId;
    private String menuName;
    private String menuType;
    private String path;
    private String icon;
    private List<SysMenuTreeVO> children;

    public Long getParentID() {
        return parentId;
    }
}

