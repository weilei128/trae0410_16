#!/bin/bash

# 在线留言反馈系统部署脚本
# 后端端口: 10013
# 前端端口: 10023

set -e

SERVER_IP="49.235.161.106"
BACKEND_PORT=10013
FRONTEND_PORT=10023
APP_NAME="message-board-10013"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  在线留言反馈系统部署脚本${NC}"
echo -e "${GREEN}========================================${NC}"

# 1. 关闭占用端口的进程
echo -e "${YELLOW}[1/6] 关闭占用端口的旧进程...${NC}"
# 关闭后端端口
BACKEND_PID=$(lsof -t -i:${BACKEND_PORT} 2>/dev/null || true)
if [ -n "$BACKEND_PID" ]; then
    echo "  关闭后端进程 PID: $BACKEND_PID"
    kill -9 $BACKEND_PID 2>/dev/null || true
fi

# 关闭前端端口
FRONTEND_PID=$(lsof -t -i:${FRONTEND_PORT} 2>/dev/null || true)
if [ -n "$FRONTEND_PID" ]; then
    echo "  关闭前端进程 PID: $FRONTEND_PID"
    kill -9 $FRONTEND_PID 2>/dev/null || true
fi

# 关闭已有的Java进程
JAVA_PIDS=$(ps aux | grep "message-board-10013" | grep -v grep | awk '{print $2}' || true)
if [ -n "$JAVA_PIDS" ]; then
    echo "  关闭Java进程: $JAVA_PIDS"
    kill -9 $JAVA_PIDS 2>/dev/null || true
fi

echo -e "${GREEN}  ✓ 旧进程已关闭${NC}"

# 2. 创建部署目录
echo -e "${YELLOW}[2/6] 创建部署目录...${NC}"
mkdir -p /opt/${APP_NAME}/backend
mkdir -p /opt/${APP_NAME}/frontend
mkdir -p /opt/${APP_NAME}/logs
mkdir -p /opt/${APP_NAME}/data
echo -e "${GREEN}  ✓ 目录创建完成${NC}"

# 3. 部署后端
echo -e "${YELLOW}[3/6] 部署后端服务...${NC}"
# 备份旧数据
if [ -f /opt/${APP_NAME}/data/messages.json ]; then
    cp /opt/${APP_NAME}/data/messages.json /opt/${APP_NAME}/data/messages.json.bak
fi

echo -e "${GREEN}  ✓ 后端部署准备完成${NC}"

# 4. 部署前端
echo -e "${YELLOW}[4/6] 部署前端服务...${NC}"
echo -e "${GREEN}  ✓ 前端部署准备完成${NC}"

# 5. 配置Nginx
echo -e "${YELLOW}[5/6] 配置Nginx...${NC}"
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

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
EOF

nginx -t && systemctl reload nginx
echo -e "${GREEN}  ✓ Nginx配置完成${NC}"

# 6. 启动后端服务
echo -e "${YELLOW}[6/6] 启动后端服务...${NC}"
cd /opt/${APP_NAME}/backend

# 启动后端
nohup java -jar message-board-1.0.0.jar \
    --server.port=${BACKEND_PORT} \
    --message.data.path=/opt/${APP_NAME}/data/messages.json \
    > /opt/${APP_NAME}/logs/backend.log 2>&1 &

sleep 3

# 检查服务是否启动
if lsof -i:${BACKEND_PORT} > /dev/null 2>&1; then
    echo -e "${GREEN}  ✓ 后端服务启动成功 (端口: ${BACKEND_PORT})${NC}"
else
    echo -e "${RED}  ✗ 后端服务启动失败${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  部署完成!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "访问地址:"
echo -e "  前端: http://${SERVER_IP}:${FRONTEND_PORT}"
echo -e "  后端API: http://${SERVER_IP}:${BACKEND_PORT}/api"
echo ""
echo -e "日志文件:"
echo -e "  后端: /opt/${APP_NAME}/logs/backend.log"
echo -e "  数据: /opt/${APP_NAME}/data/messages.json"
echo ""
echo -e "常用命令:"
echo -e "  查看日志: tail -f /opt/${APP_NAME}/logs/backend.log"
echo -e "  停止服务: kill \$(lsof -t -i:${BACKEND_PORT})"
echo ""
