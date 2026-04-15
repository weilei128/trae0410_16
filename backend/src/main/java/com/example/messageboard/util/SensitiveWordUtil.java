package com.example.messageboard.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 敏感词过滤工具类
 * 提供敏感词检测和过滤功能
 */
@Component
public class SensitiveWordUtil {

    @Value("${message.sensitive.words:}")
    private String sensitiveWordsConfig;

    private Set<String> sensitiveWords = new HashSet<>();
    private static final String REPLACE_CHAR = "*";

    /**
     * 初始化敏感词库
     */
    @PostConstruct
    public void init() {
        sensitiveWords = new HashSet<>();
        if (sensitiveWordsConfig != null && !sensitiveWordsConfig.trim().isEmpty()) {
            String[] words = sensitiveWordsConfig.split(",");
            for (String word : words) {
                String trimmed = word.trim().toLowerCase();
                if (!trimmed.isEmpty()) {
                    sensitiveWords.add(trimmed);
                }
            }
        }
    }

    /**
     * 检测文本是否包含敏感词
     */
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.trim().isEmpty() || sensitiveWords.isEmpty()) {
            return false;
        }
        String lowerText = text.toLowerCase();
        for (String word : sensitiveWords) {
            if (lowerText.contains(word)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 过滤敏感词，用*替换
     */
    public String filterSensitiveWord(String text) {
        if (text == null || text.trim().isEmpty() || sensitiveWords.isEmpty()) {
            return text;
        }

        String result = text;
        for (String word : sensitiveWords) {
            if (word.isEmpty()) continue;

            String lowerWord = word.toLowerCase();
            String lowerResult = result.toLowerCase();
            int index = lowerResult.indexOf(lowerWord);

            while (index != -1) {
                String replacement = REPLACE_CHAR.repeat(word.length());
                result = result.substring(0, index) + replacement + result.substring(index + word.length());
                lowerResult = result.toLowerCase();
                index = lowerResult.indexOf(lowerWord, index + replacement.length());
            }
        }
        return result;
    }

    /**
     * 获取敏感词列表
     */
    public Set<String> getSensitiveWords() {
        return Collections.unmodifiableSet(new HashSet<>(sensitiveWords));
    }
}
