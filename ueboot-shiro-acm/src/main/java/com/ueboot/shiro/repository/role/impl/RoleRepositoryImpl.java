/*
 * Copyright (c)  2018
 * All rights reserved.
 * 2018-08-21 09:40:34
 */
package com.ueboot.shiro.repository.role.impl;

import com.ueboot.core.jpa.repository.query.StringQuery;
import com.ueboot.shiro.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import com.ueboot.core.jpa.repository.DefaultJpaRepository;
import com.ueboot.shiro.repository.role.RoleBaseRepository;
import lombok.extern.slf4j.Slf4j;

import static com.ueboot.shiro.entity.Role.TYPE_GENERAL;

@Slf4j
@Repository
public class RoleRepositoryImpl extends DefaultJpaRepository<Role, Long> implements RoleBaseRepository {


    @Override
    public Page<Role> findByNameLike(Pageable pageable, String name, String system) {
        StringQuery query = StringQuery.newQuery()

                .query(" FROM " + Role.class.getName() + " WHERE id > :id ")
                .param("id", -1L)

                .predicate(!"system".equals(system))
                .query(" AND system = :system")
                .param("system", system)

                .predicate(!"system".equals(system))
                .query(" AND type = :type")
                .param("type", TYPE_GENERAL)

                .predicateHasText(name)
                .query(" AND r.name like :name ")
                .likeParam("name", name)

                .predicate(true)
                .build();

        return find(query, pageable);
    }
}
