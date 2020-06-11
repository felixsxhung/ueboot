/*
* Copyright (c)  2018
* All rights reserved.
* 2018-08-22 19:58:32
*/
package com.ueboot.shiro.controller.resources.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
* 用于前端发起查询请求时，返回查询结果
* Created on 2018-08-22 19:58:32
* @author yangkui
* @since 2.1.0 by ueboot-generator
*/
@Setter
@Getter
@NoArgsConstructor
public class ResourcesResp {
    private Long id;
    private String name;
    private String resourceType;
    private String url;
    private String themeJson;
    private String permission;
    private Long parentId;
    private String parentName;
    private Long rank;
    private Boolean available;
}