/*
* Copyright (c)  2018
* All rights reserved.
* 2018-08-08 14:05:30
*/
package com.ueboot.shiro.controller.permission.vo;


import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.*;

import javax.validation.constraints.NotNull;

/**
 * 用于前端发起对象保存和更新请求时，接收请求参数
 * Created on 2018-08-08 14:05:30
 * @author yangkui
 * @since 2.1.0 by ueboot-generator
 */
@Setter
@Getter
@NoArgsConstructor
public class PermissionReq {
    private Long id;

    @NotNull
    private Long roleId;

    private Long[] resourceIds = new Long[]{};
}