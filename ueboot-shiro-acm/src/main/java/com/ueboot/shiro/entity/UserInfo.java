package com.ueboot.shiro.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
public class UserInfo {

    private String userName;

    private Date expiredDate;
}
