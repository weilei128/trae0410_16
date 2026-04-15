package com.example.messageboard.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class JsonFileUtil {
    
    @Value("${message.data.path}")
    private String dataPath;
    
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    
    private static final String EMPTY_ARRAY = "[]";
    
    @PostConstruct
    public void init() {
        try {
            Path path = Paths.get(dataPath);
            Path parentDir = path.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            if (!Files.exists(path)) {
                Files.createFile(path);
                writeFile(EMPTY_ARRAY);
            } else {
                String content = readFile();
                if (content == null || content.trim().isEmpty()) {
                    writeFile(EMPTY_ARRAY);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize data file: " + e.getMessage(), e);
        }
    }
    
    public <T> List<T> readArray(Class<T> clazz) {
        lock.readLock().lock();
        try {
            String content = readFile();
            if (content == null || content.trim().isEmpty()) {
                return new ArrayList<>();
            }
            content = content.trim();
            if (!content.startsWith("[")) {
                return new ArrayList<>();
            }
            JSONArray array = JSON.parseArray(content);
            if (array == null || array.isEmpty()) {
                return new ArrayList<>();
            }
            List<T> list = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                try {
                    T obj = array.getObject(i, clazz);
                    if (obj != null) {
                        list.add(obj);
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing object at index " + i + ": " + e.getMessage());
                }
            }
            return list;
        } catch (Exception e) {
            System.err.println("Error reading array: " + e.getMessage());
            return new ArrayList<>();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    public <T> void writeArray(List<T> list) {
        lock.writeLock().lock();
        try {
            if (list == null) {
                list = new ArrayList<>();
            }
            String json = JSON.toJSONString(list, true);
            writeFile(json);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    private String readFile() {
        try {
            Path path = Paths.get(dataPath);
            if (!Files.exists(path)) {
                return EMPTY_ARRAY;
            }
            byte[] bytes = Files.readAllBytes(path);
            String content = new String(bytes, StandardCharsets.UTF_8);
            return content.trim().isEmpty() ? EMPTY_ARRAY : content;
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return EMPTY_ARRAY;
        }
    }
    
    private void writeFile(String content) {
        try {
            Path path = Paths.get(dataPath);
            Path parentDir = path.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            try (OutputStreamWriter writer = new OutputStreamWriter(
                    new FileOutputStream(dataPath), StandardCharsets.UTF_8)) {
                writer.write(content);
                writer.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file: " + e.getMessage(), e);
        }
    }
    
    public String getDataPath() {
        return dataPath;
    }
}
