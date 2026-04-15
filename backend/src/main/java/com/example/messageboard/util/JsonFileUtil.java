package com.example.messageboard.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * JSON文件工具类
 * 提供基于JSON文件的持久化存储功能
 */
@Component
public class JsonFileUtil {

    @Value("${message.data.path}")
    private String dataPath;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * 初始化数据文件
     */
    @PostConstruct
    public void init() {
        try {
            Path path = Paths.get(dataPath);
            if (!Files.exists(path)) {
                Path parent = path.getParent();
                if (parent != null && !Files.exists(parent)) {
                    Files.createDirectories(parent);
                }
                Files.createFile(path);
                writeArray(new ArrayList<>());
            }
        } catch (IOException e) {
            throw new RuntimeException("初始化数据文件失败: " + e.getMessage(), e);
        }
    }

    /**
     * 读取JSON数组并转换为对象列表
     */
    public <T> List<T> readArray(Class<T> clazz) {
        lock.readLock().lock();
        try {
            String content = readFile();
            if (content == null || content.trim().isEmpty()) {
                return new ArrayList<>();
            }
            JSONArray array = JSON.parseArray(content);
            List<T> list = new ArrayList<>();
            if (array != null) {
                for (int i = 0; i < array.size(); i++) {
                    list.add(array.getObject(i, clazz));
                }
            }
            return list;
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * 将对象列表写入JSON文件
     */
    public <T> void writeArray(List<T> list) {
        lock.writeLock().lock();
        try {
            String json = JSON.toJSONString(list, true);
            writeFile(json);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * 读取文件内容
     */
    private String readFile() {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(dataPath));
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 写入文件内容
     */
    private void writeFile(String content) {
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(dataPath), StandardCharsets.UTF_8)) {
            writer.write(content);
        } catch (IOException e) {
            throw new RuntimeException("写入文件失败: " + e.getMessage(), e);
        }
    }
}
