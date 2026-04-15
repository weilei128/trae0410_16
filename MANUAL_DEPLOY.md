# 手动部署指南

## 环境准备

由于本地环境限制（无Node.js、无SSH密钥），请按以下步骤手动部署到服务器。

## 方案一：在服务器上直接构建部署（推荐）

### 1. 连接服务器

```bash
ssh -p 22 root@49.235.161.106
```

### 2. 安装依赖

```bash
# 更新包列表
apt-get update

# 安装Java
apt-get install -y openjdk-11-jdk

# 安装Nginx
apt-get install -y nginx

# 安装Node.js
curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
apt-get install -y nodejs

# 安装Maven
apt-get install -y maven

# 安装Git（用于拉取代码）
apt-get install -y git
```

### 3. 拉取/上传代码

方式A - 从Git仓库拉取：
```bash
cd /opt
git clone <your-repo-url> message-board-10013
cd message-board-10013
```

方式B - 上传本地代码：
```bash
# 在本地执行
scp -P 22 -r backend frontend root@49.235.161.106:/opt/message-board-10013/
```

### 4. 构建后端

```bash
cd /opt/message-board-10013/backend
mvn clean package -DskipTests
```

### 5. 构建前端

```bash
cd /opt/message-board-10013/frontend
npm install
npm run build
```

### 6. 配置目录

```bash
mkdir -p /opt/message-board-10013/{backend,frontend,logs,data}

# 复制后端jar
cp /opt/message-board-10013/backend/target/message-board-1.0.0.jar /opt/message-board-10013/backend/

# 复制前端构建产物
cp -r /opt/message-board-10013/frontend/dist /opt/message-board-10013/frontend/
```

### 7. 配置Nginx

创建配置文件：
```bash
cat > /etc/nginx/conf.d/message-board-10023.conf << 'EOF'
server {
    listen 10023;
    server_name localhost;

    root /opt/message-board-10013/frontend/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:10013;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
EOF

# 测试配置并重启
nginx -t
systemctl reload nginx
```

### 8. 关闭旧进程

```bash
# 关闭占用10013端口的进程
kill $(lsof -t -i:10013) 2>/dev/null || true

# 关闭占用10023端口的进程
kill $(lsof -t -i:10023) 2>/dev/null || true

# 关闭已有的Java进程
ps aux | grep "message-board" | grep -v grep | awk '{print $2}' | xargs -r kill -9 2>/dev/null || true
```

### 9. 启动后端服务

```bash
cd /opt/message-board-10013/backend

nohup java -jar message-board-1.0.0.jar \
    --server.port=10013 \
    --message.data.path=/opt/message-board-10013/data/messages.json \
    > /opt/message-board-10013/logs/backend.log 2>&1 &

# 等待启动
sleep 5

# 验证
lsof -i:10013
```

### 10. 验证部署

```bash
# 测试API
curl http://localhost:10013/api/messages

# 查看日志
tail -20 /opt/message-board-10013/logs/backend.log
```

访问：http://49.235.161.106:10023

## 方案二：使用Docker部署

### 1. 安装Docker

```bash
apt-get update
apt-get install -y docker.io docker-compose
```

### 2. 创建Dockerfile

```dockerfile
# Dockerfile
FROM openjdk:11-jdk-slim

WORKDIR /app

COPY backend/target/message-board-1.0.0.jar app.jar

EXPOSE 10013

ENTRYPOINT ["java", "-jar", "app.jar", "--server.port=10013", "--message.data.path=/app/data/messages.json"]
```

### 3. 构建并运行

```bash
docker build -t message-board:10013 .

docker run -d \
  --name message-board-10013 \
  -p 10013:10013 \
  -v /opt/message-board-10013/data:/app/data \
  -v /opt/message-board-10013/logs:/app/logs \
  message-board:10013
```

## 常用管理命令

```bash
# 查看后端日志
tail -f /opt/message-board-10013/logs/backend.log

# 停止后端
kill $(lsof -t -i:10013)

# 重启后端
cd /opt/message-board-10013/backend && nohup java -jar message-board-1.0.0.jar --server.port=10013 --message.data.path=/opt/message-board-10013/data/messages.json > /opt/message-board-10013/logs/backend.log 2>&1 &

# 重启Nginx
systemctl restart nginx

# 查看端口占用
lsof -i:10013
lsof -i:10023

# 查看进程
ps aux | grep java

# 查看数据文件
cat /opt/message-board-10013/data/messages.json
```

## 测试脚本

```bash
# 添加测试留言
curl -X POST http://localhost:10013/api/messages \
  -H "Content-Type: application/json" \
  -d '{"username":"测试用户","content":"这是一条测试留言","email":"test@example.com"}'

# 获取留言列表
curl http://localhost:10013/api/messages

# 获取分页列表
curl "http://localhost:10013/api/messages?page=1&size=5"

# 删除留言（将1替换为实际ID）
curl -X DELETE http://localhost:10013/api/messages/1
```

## 故障排查

### 1. 端口被占用
```bash
# 查找占用端口的进程
lsof -i:10013

# 强制结束
kill -9 <PID>
```

### 2. 后端启动失败
```bash
# 查看详细日志
cat /opt/message-board-10013/logs/backend.log

# 检查Java版本
java -version

# 检查jar文件
ls -la /opt/message-board-10013/backend/
```

### 3. 前端无法访问
```bash
# 检查Nginx配置
nginx -t

# 检查Nginx状态
systemctl status nginx

# 检查前端文件
ls -la /opt/message-board-10013/frontend/dist/
```

### 4. 跨域问题
确保Nginx配置中正确设置了proxy_pass和headers。

## 安全建议

1. 修改默认SSH端口
2. 配置防火墙规则
3. 使用HTTPS
4. 定期备份数据文件
5. 设置日志轮转
