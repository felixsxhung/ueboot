/*
 * Copyright (c)  2018
 * All rights reserved.
 * 2018-08-21 09:39:51
 */
package com.ueboot.shiro.repository.userrole.impl;

import com.ueboot.core.jpa.repository.DefaultJpaRepository;
import com.ueboot.core.jpa.repository.query.StringQuery;
import com.ueboot.shiro.entity.Role;
import com.ueboot.shiro.entity.StatisticInfo;
import com.ueboot.shiro.entity.UserRole;
import com.ueboot.shiro.repository.userrole.UserRoleBaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author yangkui
 * @author zivhung update
 */
@Slf4j
@Repository
public class UserRoleRepositoryImpl extends DefaultJpaRepository<UserRole, Long> implements UserRoleBaseRepository {

    /***
     *统计数据
     */
    @Override
    public Long statisticUserSumByRoleId(Long roleId) {
        StringQuery query = StringQuery.newQuery().query("select count(ur.id) as num from UserRole ur  where  ur.role.id=:roleId").param("roleId", roleId).predicate(true).build();
        List<StatisticInfo> list = this.find(query, StatisticInfo.class);
        if (list == null || list.isEmpty()) {
            return 0L;
        }
        return list.get(0).getNum();
    }
}
