package com.example.messageboard.controller;

import com.example.messageboard.common.PageResult;
import com.example.messageboard.common.Result;
import com.example.messageboard.entity.Message;
import com.example.messageboard.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    
    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);
    
    @Autowired
    private MessageService messageService;
    
    @PostMapping
    public Result<Message> addMessage(@RequestBody Message message) {
        try {
            if (message.getContent() == null || message.getContent().trim().isEmpty()) {
                return Result.error(400, "留言内容不能为空");
            }
            if (message.getUsername() == null || message.getUsername().trim().isEmpty()) {
                return Result.error(400, "用户名不能为空");
            }
            
            message.setUsername(message.getUsername().trim());
            message.setContent(message.getContent().trim());
            if (message.getEmail() != null) {
                message.setEmail(message.getEmail().trim());
            }
            
            Message savedMessage = messageService.addMessage(message);
            logger.info("Message added successfully: id={}, username={}", savedMessage.getId(), savedMessage.getUsername());
            return Result.success(savedMessage);
        } catch (Exception e) {
            logger.error("Failed to add message", e);
            return Result.error("提交留言失败: " + e.getMessage());
        }
    }
    
    @GetMapping
    public Result<PageResult<Message>> getMessageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Boolean isAdmin) {
        try {
            if (page < 1) page = 1;
            if (size < 1) size = 10;
            if (size > 100) size = 100;
            
            PageResult<Message> result = messageService.getMessageList(page, size, isAdmin);
            return Result.success(result);
        } catch (Exception e) {
            logger.error("Failed to get message list", e);
            return Result.error("获取留言列表失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/all")
    public Result<List<Message>> getAllMessages() {
        try {
            List<Message> messages = messageService.getAllMessages();
            return Result.success(messages);
        } catch (Exception e) {
            logger.error("Failed to get all messages", e);
            return Result.error("获取留言列表失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/{id}")
    public Result<Message> getMessageById(@PathVariable Long id) {
        try {
            Message message = messageService.getMessageById(id);
            if (message == null) {
                return Result.error(404, "留言不存在");
            }
            return Result.success(message);
        } catch (Exception e) {
            logger.error("Failed to get message by id: {}", id, e);
            return Result.error("获取留言失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteMessage(@PathVariable Long id) {
        try {
            boolean success = messageService.deleteMessage(id);
            if (success) {
                logger.info("Message deleted successfully: id={}", id);
                return Result.success();
            } else {
                return Result.error(404, "留言不存在");
            }
        } catch (Exception e) {
            logger.error("Failed to delete message: {}", id, e);
            return Result.error("删除留言失败: " + e.getMessage());
        }
    }
}
