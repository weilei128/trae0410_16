package com.example.messageboard.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 留言实体类
 */
@Data
public class Message {

    /**
     * 留言ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 留言内容
     */
    private String content;

    /**
     * 邮箱（可选）
     */
    private String email;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 是否为管理员留言
     */
    private Boolean isAdmin;

    /**
     * 是否已删除（逻辑删除）
     */
    private Boolean isDeleted;

    public Message() {
        this.createTime = LocalDateTime.now();
        this.isAdmin = false;
        this.isDeleted = false;
    }
}
