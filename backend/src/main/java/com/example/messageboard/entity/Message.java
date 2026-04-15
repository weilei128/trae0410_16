package com.example.messageboard.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class Message {
    private Long id;
    private String username;
    private String content;
    private String email;
    
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    private Boolean isAdmin;
    private Boolean isDeleted;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public Message() {
        this.createTime = LocalDateTime.now();
        this.isAdmin = false;
        this.isDeleted = false;
    }
    
    public String getFormattedCreateTime() {
        if (createTime == null) {
            return "";
        }
        return createTime.format(FORMATTER);
    }
}
