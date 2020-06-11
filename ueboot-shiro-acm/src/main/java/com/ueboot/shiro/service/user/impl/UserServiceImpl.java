/*
 * Copyright (c)  2018
 * All rights reserved.
 * 2018-08-14 10:47:55
 */
package com.ueboot.shiro.service.user.impl;

import com.ueboot.core.repository.BaseRepository;
import com.ueboot.core.service.impl.BaseServiceImpl;
import com.ueboot.shiro.entity.User;
import com.ueboot.shiro.entity.UserRole;
import com.ueboot.shiro.repository.user.UserRepository;
import com.ueboot.shiro.repository.userrole.UserRoleRepository;
import com.ueboot.shiro.service.user.UserService;
import com.ueboot.shiro.shiro.ShiroEventListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created on 2018-08-14 10:47:55
 *
 * @author yangkui
 * @since 2.1.0 by ueboot-generator
 */
@Slf4j
@Service
@ConditionalOnMissingBean(name = "userService")
public class UserServiceImpl extends BaseServiceImpl<User> implements UserService {
    @Resource
    private UserRepository userRepository;

    @Resource
    private UserRoleRepository userRoleRepository;

    // shiro 权限日志
    @Resource
    private ShiroEventListener shiroEventListener;

    @Override
    protected BaseRepository getBaseRepository() {
        return userRepository;
    }

    /**
     * 根据用户名和密码查找用户
     *
     * @param userName 用户名
     * @return 用户，不存在则返回空对象
     */
    @Override
    public User findByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }


    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30, propagation = Propagation.REQUIRED)
    public void lockByUserName(String userName) {
        User user = this.userRepository.findByUserName(userName);
        if (user == null) {
            return;
        }
        user.setLocked(Boolean.TRUE);
        this.userRepository.save(user);
    }

    /**
     * 根据ID查找用户
     *
     * @param id 主键ID
     * @return 用户对象
     */
    @Override
    public User findById(Long id) {
        return userRepository.getOne(id);
    }

    @Override
    public void deleteById(Long[] id) {
        for (Long i : id) {
            //删除用户所属角色
            List<UserRole> userRoleList = this.userRoleRepository.findByUserId(i);
            if (!userRoleList.isEmpty()) {
                this.userRoleRepository.deleteAll(userRoleList);
            }
        }
        this.delete(id);

        // 删除用户日志记录
        String optUserName = (String) SecurityUtils.getSubject().getPrincipal();
        this.shiroEventListener.deleteUserEvent(optUserName, id);

    }
}
