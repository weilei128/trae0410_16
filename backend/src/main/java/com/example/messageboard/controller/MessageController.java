package com.example.messageboard.controller;

import com.example.messageboard.common.PageResult;
import com.example.messageboard.common.Result;
import com.example.messageboard.entity.Message;
import com.example.messageboard.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 留言控制器
 * 处理留言相关的HTTP请求
 */
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * 添加留言
     */
    @PostMapping
    public Result<Message> addMessage(@RequestBody Message message) {
        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            return Result.error(400, "留言内容不能为空");
        }
        if (message.getUsername() == null || message.getUsername().trim().isEmpty()) {
            return Result.error(400, "用户名不能为空");
        }

        Message savedMessage = messageService.addMessage(message);
        return Result.success(savedMessage);
    }

    /**
     * 分页获取留言列表
     */
    @GetMapping
    public Result<PageResult<Message>> getMessageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Boolean isAdmin) {
        PageResult<Message> result = messageService.getMessageList(page, size, isAdmin);
        return Result.success(result);
    }

    /**
     * 获取所有留言（不分页）
     */
    @GetMapping("/all")
    public Result<List<Message>> getAllMessages() {
        List<Message> messages = messageService.getAllMessages();
        return Result.success(messages);
    }

    /**
     * 根据ID获取留言
     */
    @GetMapping("/{id}")
    public Result<Message> getMessageById(@PathVariable Long id) {
        Message message = messageService.getMessageById(id);
        if (message == null) {
            return Result.error(404, "留言不存在");
        }
        return Result.success(message);
    }

    /**
     * 删除留言（逻辑删除）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteMessage(@PathVariable Long id) {
        boolean success = messageService.deleteMessage(id);
        if (success) {
            return Result.success();
        } else {
            return Result.error(404, "留言不存在");
        }
    }
}
