package com.li.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.li.entity.BaseData;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 *
 * @TableName account
 */
@TableName(value ="account")
@Data
@AllArgsConstructor
public class Account implements Serializable, BaseData {
    /**
     *
     */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /**
     *
     */
    private String username;

    /**
     *
     */
    private String email;

    /**
     *
     */
    private String password;

    /**
     *
     */
    private String role;

    /**
     *
     */
    private Date registerTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

}