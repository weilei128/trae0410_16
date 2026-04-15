#!/bin/bash

# 在线留言反馈系统 - 服务器端部署脚本
# 后端端口: 10013
# 前端端口: 10023

set -e

BACKEND_PORT=10013
FRONTEND_PORT=10023
APP_NAME="message-board-10013"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  在线留言反馈系统 - 服务器部署${NC}"
echo -e "${GREEN}========================================${NC}"

# 1. 安装依赖
echo -e "${YELLOW}[1/8] 检查并安装依赖...${NC}"

# 安装Java
if ! command -v java &> /dev/null; then
    echo "  安装Java..."
    apt-get update
    apt-get install -y openjdk-11-jdk
fi

# 安装Nginx
if ! command -v nginx &> /dev/null; then
    echo "  安装Nginx..."
    apt-get install -y nginx
fi

# 安装Node.js和npm
if ! command -v npm &> /dev/null; then
    echo "  安装Node.js..."
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash -
    apt-get install -y nodejs
fi

echo -e "${GREEN}  ✓ 依赖检查完成${NC}"

# 2. 关闭占用端口的进程
echo -e "${YELLOW}[2/8] 关闭占用端口的旧进程...${NC}"

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
ps aux | grep "message-board" | grep -v grep | awk '{print $2}' | xargs -r kill -9 2>/dev/null || true

echo -e "${GREEN}  ✓ 旧进程已关闭${NC}"

# 3. 创建部署目录
echo -e "${YELLOW}[3/8] 创建部署目录...${NC}"
mkdir -p /opt/${APP_NAME}/{backend,frontend,logs,data}
echo -e "${GREEN}  ✓ 目录创建完成${NC}"

# 4. 复制后端文件
echo -e "${YELLOW}[4/8] 部署后端...${NC}"
if [ -f "/tmp/message-board-1.0.0.jar" ]; then
    cp /tmp/message-board-1.0.0.jar /opt/${APP_NAME}/backend/
    echo -e "${GREEN}  ✓ 后端文件已复制${NC}"
else
    echo -e "${RED}  ✗ 后端jar文件不存在${NC}"
    exit 1
fi

# 5. 构建并部署前端
echo -e "${YELLOW}[5/8] 构建并部署前端...${NC}"
if [ -d "/tmp/frontend" ]; then
    cd /tmp/frontend
    
    # 修改API配置为生产环境
    sed -i "s|target: 'http://localhost:10013'|target: 'http://localhost:${BACKEND_PORT}'|g" vite.config.js
    
    # 安装依赖并构建
    npm install
    npm run build
    
    # 复制构建产物
    rm -rf /opt/${APP_NAME}/frontend/dist
    cp -r dist /opt/${APP_NAME}/frontend/
    echo -e "${GREEN}  ✓ 前端构建并部署完成${NC}"
else
    echo -e "${RED}  ✗ 前端源码目录不存在${NC}"
    exit 1
fi

# 6. 配置Nginx
echo -e "${YELLOW}[6/8] 配置Nginx...${NC}"
cat > /etc/nginx/conf.d/message-board-10023.conf << EOF
server {
    listen ${FRONTEND_PORT};
    server_name localhost;

    root /opt/${APP_NAME}/frontend/dist;
    index index.html;

    location / {
        try_files \$uri \$uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:${BACKEND_PORT};
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }

    location ~* \\.(js|css|png|jpg|jpeg|gif|ico|svg)\$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
EOF

nginx -t && systemctl reload nginx
echo -e "${GREEN}  ✓ Nginx配置完成${NC}"

# 7. 启动后端服务
echo -e "${YELLOW}[7/8] 启动后端服务...${NC}"
cd /opt/${APP_NAME}/backend

# 启动后端
nohup java -jar message-board-1.0.0.jar \
    --server.port=${BACKEND_PORT} \
    --message.data.path=/opt/${APP_NAME}/data/messages.json \
    > /opt/${APP_NAME}/logs/backend.log 2>&1 &

sleep 5

# 检查服务是否启动
if lsof -i:${BACKEND_PORT} > /dev/null 2>&1; then
    echo -e "${GREEN}  ✓ 后端服务启动成功 (端口: ${BACKEND_PORT})${NC}"
else
    echo -e "${RED}  ✗ 后端服务启动失败${NC}"
    echo "  查看日志: tail -20 /opt/${APP_NAME}/logs/backend.log"
    exit 1
fi

# 8. 验证部署
echo -e "${YELLOW}[8/8] 验证部署...${NC}"
sleep 2

# 测试后端API
if curl -s http://localhost:${BACKEND_PORT}/api/messages > /dev/null 2>&1; then
    echo -e "${GREEN}  ✓ API测试通过${NC}"
else
    echo -e "${YELLOW}  ! API测试可能需要等待服务完全启动${NC}"
fi

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}  部署完成!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "访问地址:"
echo -e "  前端: http://$(curl -s ifconfig.me):${FRONTEND_PORT}"
echo -e "  后端API: http://$(curl -s ifconfig.me):${BACKEND_PORT}/api"
echo ""
echo -e "日志文件:"
echo -e "  后端: /opt/${APP_NAME}/logs/backend.log"
echo -e "  数据: /opt/${APP_NAME}/data/messages.json"
echo ""
echo -e "常用命令:"
echo -e "  查看日志: tail -f /opt/${APP_NAME}/logs/backend.log"
echo -e "  停止后端: kill \$(lsof -t -i:${BACKEND_PORT})"
echo -e "  重启Nginx: systemctl restart nginx"
echo ""
