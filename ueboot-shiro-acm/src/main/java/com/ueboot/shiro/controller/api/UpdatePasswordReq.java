package com.ueboot.shiro.controller.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.ueboot.shiro.controller.api.LoginVo.getString;

/**
 * @author yangkui
 */
@Getter
@Setter
@NoArgsConstructor
class UpdatePasswordReq {
    /**
     * 原来的密码
     */
    private String oldPassword;
    /**
     * 新密码
     */
    private String newPassword;


    public String oldPwd() {
        return getString(oldPassword);
    }


    public String newPwd() {
        return getString(newPassword);
    }

}
