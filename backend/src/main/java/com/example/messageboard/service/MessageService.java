package com.example.messageboard.service;

import com.example.messageboard.common.PageResult;
import com.example.messageboard.entity.Message;
import com.example.messageboard.util.JsonFileUtil;
import com.example.messageboard.util.SensitiveWordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 留言服务类
 * 处理留言的业务逻辑
 */
@Service
public class MessageService {

    private final JsonFileUtil jsonFileUtil;
    private final SensitiveWordUtil sensitiveWordUtil;

    @Autowired
    public MessageService(JsonFileUtil jsonFileUtil, SensitiveWordUtil sensitiveWordUtil) {
        this.jsonFileUtil = jsonFileUtil;
        this.sensitiveWordUtil = sensitiveWordUtil;
    }

    /**
     * 添加留言
     */
    public Message addMessage(Message message) {
        List<Message> messages = jsonFileUtil.readArray(Message.class);
        if (messages == null) {
            messages = new ArrayList<>();
        }

        // 生成ID
        Long maxId = messages.stream()
                .mapToLong(m -> m.getId() != null ? m.getId() : 0)
                .max()
                .orElse(0L);

        message.setId(maxId + 1);
        message.setCreateTime(LocalDateTime.now());
        message.setIsDeleted(false);

        // 设置管理员标识
        if (message.getIsAdmin() == null) {
            message.setIsAdmin(false);
        }

        // 敏感词过滤
        String filteredContent = sensitiveWordUtil.filterSensitiveWord(message.getContent());
        message.setContent(filteredContent);

        messages.add(message);
        jsonFileUtil.writeArray(messages);

        return message;
    }

    /**
     * 分页获取留言列表
     */
    public PageResult<Message> getMessageList(Integer page, Integer size, Boolean isAdmin) {
        List<Message> allMessages = jsonFileUtil.readArray(Message.class);
        if (allMessages == null) {
            allMessages = new ArrayList<>();
        }

        // 过滤已删除的留言
        List<Message> filteredMessages = allMessages.stream()
                .filter(m -> !Boolean.TRUE.equals(m.getIsDeleted()))
                .filter(m -> isAdmin == null || isAdmin.equals(m.getIsAdmin()))
                .sorted(Comparator.comparing(Message::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        Long total = (long) filteredMessages.size();

        // 分页计算
        int start = (page - 1) * size;
        int end = Math.min(start + size, filteredMessages.size());

        List<Message> pageMessages = start < filteredMessages.size()
                ? filteredMessages.subList(start, end)
                : new ArrayList<>();

        return new PageResult<>(pageMessages, total, page, size);
    }

    /**
     * 逻辑删除留言
     */
    public boolean deleteMessage(Long id) {
        if (id == null) {
            return false;
        }

        List<Message> messages = jsonFileUtil.readArray(Message.class);
        if (messages == null) {
            return false;
        }

        for (Message message : messages) {
            if (id.equals(message.getId())) {
                message.setIsDeleted(true);
                jsonFileUtil.writeArray(messages);
                return true;
            }
        }
        return false;
    }

    /**
     * 根据ID获取留言
     */
    public Message getMessageById(Long id) {
        if (id == null) {
            return null;
        }

        List<Message> messages = jsonFileUtil.readArray(Message.class);
        if (messages == null) {
            return null;
        }

        return messages.stream()
                .filter(m -> id.equals(m.getId()) && !Boolean.TRUE.equals(m.getIsDeleted()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取所有留言（不分页）
     */
    public List<Message> getAllMessages() {
        List<Message> messages = jsonFileUtil.readArray(Message.class);
        if (messages == null) {
            return new ArrayList<>();
        }

        return messages.stream()
                .filter(m -> !Boolean.TRUE.equals(m.getIsDeleted()))
                .sorted(Comparator.comparing(Message::getCreateTime, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }
}
