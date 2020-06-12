package com.ueboot.shiro.entity;


import com.ueboot.core.entity.AbstractVersionEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

/**
 * 用户角色
 *
 * @author yangkui
 */
@Setter
@Getter
@NoArgsConstructor
@Entity
@Table(name = "PROPERTY_SYS_ROLE")
public class Role extends AbstractVersionEntity<Long> {
    public static final String TYPE_GENERAL = "general";

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 角色名称 */
    @Column(name = "NAME")
    private String name;

    /** 所属系统（finance 财务，property 物业，emergency 紧急调度） */
    @Column(name = "SYSTEM")
    private String system;

    /** 角色分类（management管理，general普通） */
    @Column(name = "TYPE")
    private String type = TYPE_GENERAL;

    /** 角色描述 */
    @Column(name = "DESCRIPTION")
    private String description;

    /** 可用 */
    @Column(name = "AVAILABLE")
    private Boolean available = Boolean.TRUE;

}
