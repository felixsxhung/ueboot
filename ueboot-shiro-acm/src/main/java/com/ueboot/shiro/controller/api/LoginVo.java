package com.ueboot.shiro.controller.api;

import cn.hutool.crypto.SecureUtil;
import com.ueboot.core.annotation.NotLog;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@Data
@NoArgsConstructor
@NotLog
public class LoginVo {
    static final String ENCRYPT_KEY = "yBnulH9ODtonS5lj";

    private String username;

    private String password;

    private String captcha;

    public String pwd() {
        return getString(password);
    }

    @NotNull
    public static String getString(String password) {
        return SecureUtil.aes(ENCRYPT_KEY.getBytes()).decryptStr(password);
    }
}
