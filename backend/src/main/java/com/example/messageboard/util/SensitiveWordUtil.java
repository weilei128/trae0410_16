package com.example.messageboard.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Component
public class SensitiveWordUtil {
    
    @Value("${message.sensitive.words:}")
    private String sensitiveWordsConfig;
    
    private List<String> sensitiveWords = new ArrayList<>();
    private static final String REPLACE_CHAR = "*";
    
    @PostConstruct
    public void init() {
        if (sensitiveWordsConfig != null && !sensitiveWordsConfig.trim().isEmpty()) {
            String[] words = sensitiveWordsConfig.split(",");
            for (String word : words) {
                String trimmed = word.trim();
                if (!trimmed.isEmpty()) {
                    sensitiveWords.add(trimmed);
                }
            }
        }
    }
    
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }
        String lowerText = text.toLowerCase();
        for (String word : sensitiveWords) {
            if (lowerText.contains(word.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
    
    private String repeatChar(char c, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
    
    public String filterSensitiveWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        if (sensitiveWords.isEmpty()) {
            return text;
        }
        
        String result = text;
        for (String word : sensitiveWords) {
            if (word.isEmpty()) continue;
            String patternStr = "(?i)" + Pattern.quote(word);
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(result);
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String replacement = repeatChar(REPLACE_CHAR.charAt(0), matcher.group().length());
                matcher.appendReplacement(sb, replacement);
            }
            matcher.appendTail(sb);
            result = sb.toString();
        }
        return result;
    }
    
    public List<String> getSensitiveWords() {
        return new ArrayList<>(sensitiveWords);
    }
}
