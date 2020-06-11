/*
* Copyright (c)  2018
* All rights reserved.
* 2018-08-08 11:53:52
*/
package com.ueboot.shiro.repository.organization;

import com.ueboot.shiro.entity.Organization;
import com.ueboot.core.repository.BaseRepository;
import org.springframework.stereotype.Repository;

/**
* 这个类里面使用spring data jpa 方式实现数据库的CRUD
* Created on 2018-08-08 11:53:52
* @author yangkui
* @since 2.1.0 by ueboot-generator
*/
@Repository
public interface OrganizationRepository extends BaseRepository<Organization, Long>,OrganizationBaseRepository {



}
