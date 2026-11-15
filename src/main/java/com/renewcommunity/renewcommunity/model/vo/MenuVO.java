package com.renewcommunity.renewcommunity.model.vo;

import com.renewcommunity.renewcommunity.entity.SysMenu;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单树节点。
 */
@Data
public class MenuVO {

    private Long id;

    private Long parentId;

    private String name;

    private String type;

    private String path;

    private String component;

    private String permission;

    private String icon;

    private Integer sort;

    private Integer status;

    private List<MenuVO> children = new ArrayList<>();

    public MenuVO(SysMenu menu) {
        this.id = menu.getId();
        this.parentId = menu.getParentId();
        this.name = menu.getName();
        this.type = menu.getType();
        this.path = menu.getPath();
        this.component = menu.getComponent();
        this.permission = menu.getPermission();
        this.icon = menu.getIcon();
        this.sort = menu.getSort();
        this.status = menu.getStatus();
    }
}
