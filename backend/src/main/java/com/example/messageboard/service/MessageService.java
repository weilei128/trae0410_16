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

@Service
public class MessageService {
    
    @Autowired
    private JsonFileUtil jsonFileUtil;
    
    @Autowired
    private SensitiveWordUtil sensitiveWordUtil;
    
    public Message addMessage(Message message) {
        List<Message> messages = jsonFileUtil.readArray(Message.class);
        if (messages == null) {
            messages = new ArrayList<>();
        }
        
        Long maxId = messages.stream()
                .filter(m -> m.getId() != null)
                .mapToLong(Message::getId)
                .max()
                .orElse(0L);
        
        message.setId(maxId + 1);
        message.setCreateTime(LocalDateTime.now());
        message.setIsDeleted(false);
        
        if (message.getIsAdmin() == null) {
            message.setIsAdmin(false);
        }
        
        String originalContent = message.getContent();
        if (originalContent != null && !originalContent.trim().isEmpty()) {
            String filteredContent = sensitiveWordUtil.filterSensitiveWord(originalContent);
            message.setContent(filteredContent);
        }
        
        messages.add(message);
        jsonFileUtil.writeArray(messages);
        
        return message;
    }
    
    public PageResult<Message> getMessageList(Integer page, Integer size, Boolean isAdmin) {
        List<Message> allMessages = jsonFileUtil.readArray(Message.class);
        if (allMessages == null) {
            allMessages = new ArrayList<>();
        }
        
        List<Message> filteredMessages = allMessages.stream()
                .filter(m -> m != null && !Boolean.TRUE.equals(m.getIsDeleted()))
                .filter(m -> isAdmin == null || isAdmin.equals(m.getIsAdmin()))
                .sorted((m1, m2) -> {
                    if (m1.getCreateTime() == null && m2.getCreateTime() == null) return 0;
                    if (m1.getCreateTime() == null) return 1;
                    if (m2.getCreateTime() == null) return -1;
                    return m2.getCreateTime().compareTo(m1.getCreateTime());
                })
                .collect(Collectors.toList());
        
        Long total = (long) filteredMessages.size();
        
        int start = (page - 1) * size;
        int end = Math.min(start + size, filteredMessages.size());
        
        List<Message> pageMessages = start < filteredMessages.size() 
                ? filteredMessages.subList(start, end) 
                : new ArrayList<>();
        
        return new PageResult<>(pageMessages, total, page, size);
    }
    
    public boolean deleteMessage(Long id) {
        List<Message> messages = jsonFileUtil.readArray(Message.class);
        if (messages == null) {
            return false;
        }
        
        for (Message message : messages) {
            if (message != null && id.equals(message.getId())) {
                message.setIsDeleted(true);
                jsonFileUtil.writeArray(messages);
                return true;
            }
        }
        return false;
    }
    
    public Message getMessageById(Long id) {
        List<Message> messages = jsonFileUtil.readArray(Message.class);
        if (messages == null) {
            return null;
        }
        return messages.stream()
                .filter(m -> m != null && id.equals(m.getId()) && !Boolean.TRUE.equals(m.getIsDeleted()))
                .findFirst()
                .orElse(null);
    }
    
    public List<Message> getAllMessages() {
        List<Message> messages = jsonFileUtil.readArray(Message.class);
        if (messages == null) {
            return new ArrayList<>();
        }
        return messages.stream()
                .filter(m -> m != null && !Boolean.TRUE.equals(m.getIsDeleted()))
                .sorted((m1, m2) -> {
                    if (m1.getCreateTime() == null && m2.getCreateTime() == null) return 0;
                    if (m1.getCreateTime() == null) return 1;
                    if (m2.getCreateTime() == null) return -1;
                    return m2.getCreateTime().compareTo(m1.getCreateTime());
                })
                .collect(Collectors.toList());
    }
}
