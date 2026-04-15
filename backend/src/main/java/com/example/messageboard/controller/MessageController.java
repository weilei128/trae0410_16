package com.example.messageboard.controller;

import com.example.messageboard.common.PageResult;
import com.example.messageboard.common.Result;
import com.example.messageboard.entity.Message;
import com.example.messageboard.service.MessageService;
import com.example.messageboard.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    
    @Autowired
    private MessageService messageService;
    
    @PostMapping
    public Result<Message> addMessage(@RequestBody Message message) {
        if (!ValidationUtil.isValidUsername(message.getUsername())) {
            return Result.error(400, "用户名为1-50个字符");
        }
        if (!ValidationUtil.isValidContent(message.getContent())) {
            return Result.error(400, "留言内容为1-2000个字符");
        }
        if (!ValidationUtil.isEmail(message.getEmail())) {
            return Result.error(400, "邮箱格式不正确");
        }
        
        Message savedMessage = messageService.addMessage(message);
        return Result.success(savedMessage);
    }
    
    @GetMapping
    public Result<PageResult<Message>> getMessageList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Boolean isAdmin) {
        try {
            PageResult<Message> result = messageService.getMessageList(page, size, isAdmin);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取留言列表失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/all")
    public Result<List<Message>> getAllMessages(@RequestParam(required = false) Boolean isAdmin) {
        try {
            List<Message> messages = messageService.getAllMessages(isAdmin);
            return Result.success(messages);
        } catch (Exception e) {
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
            return Result.error("获取留言失败: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteMessage(@PathVariable Long id) {
        try {
            boolean success = messageService.deleteMessage(id);
            if (success) {
                return Result.success();
            } else {
                return Result.error(404, "留言不存在");
            }
        } catch (Exception e) {
            return Result.error("删除留言失败: " + e.getMessage());
        }
    }
}
