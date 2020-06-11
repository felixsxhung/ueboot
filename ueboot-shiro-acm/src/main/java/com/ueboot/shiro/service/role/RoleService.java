/*
 * Copyright (c)  2018
 * All rights reserved.
 * 2018-08-21 09:40:34
 */
package com.ueboot.shiro.service.role;

import com.ueboot.shiro.entity.Role;
import com.ueboot.core.service.BaseService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Created on 2018-08-21 09:40:34
 *
 * @author yangkui
 * @since 2.1.0 by ueboot-generator
 */
public interface RoleService extends BaseService<Role> {

    Role findByName(String name);

    void deleteRole(Long[] roleIds);


    Page<Role> findByName(Pageable pageable, String name, String system);

    /***
     * 统计是否有
     * @param id
     * @return
     */
    Long statisticUserByRoleId(Long id);

    List<Role> findBySystem(String optUserName);
}
