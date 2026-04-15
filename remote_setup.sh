#!/bin/bash
# 在服务器上执行的完整部署脚本

set -e

APP_NAME="message-board-10013"
BACKEND_PORT=10013
FRONTEND_PORT=10023

echo "========================================"
echo "  在线留言反馈系统 - 服务器部署"
echo "========================================"

# 1. 安装依赖
echo "[1/7] 检查并安装依赖..."
apt-get update -qq

# 安装Java
if ! command -v java &> /dev/null; then
    echo "  安装Java..."
    apt-get install -y -qq openjdk-11-jdk
fi
java -version

# 安装Nginx
if ! command -v nginx &> /dev/null; then
    echo "  安装Nginx..."
    apt-get install -y -qq nginx
fi

# 安装Node.js
if ! command -v npm &> /dev/null; then
    echo "  安装Node.js..."
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash - > /dev/null 2>&1
    apt-get install -y -qq nodejs
fi
node -v && npm -v

# 安装Maven
if ! command -v mvn &> /dev/null; then
    echo "  安装Maven..."
    apt-get install -y -qq maven
fi
mvn -version

echo "  ✓ 依赖安装完成"

# 2. 关闭旧进程
echo "[2/7] 关闭旧进程..."
kill $(lsof -t -i:${BACKEND_PORT}) 2>/dev/null || true
kill $(lsof -t -i:${FRONTEND_PORT}) 2>/dev/null || true
ps aux | grep "message-board" | grep -v grep | awk '{print $2}' | xargs -r kill -9 2>/dev/null || true
echo "  ✓ 旧进程已关闭"

# 3. 创建目录
echo "[3/7] 创建部署目录..."
mkdir -p /opt/${APP_NAME}/{backend,frontend,logs,data}
echo "  ✓ 目录创建完成"

# 4. 复制文件
echo "[4/7] 复制项目文件..."
if [ -d "/tmp/trae0410-kimi" ]; then
    cp -r /tmp/trae0410-kimi/backend /opt/${APP_NAME}/
    cp -r /tmp/trae0410-kimi/frontend /opt/${APP_NAME}/
    echo "  ✓ 文件复制完成"
else
    echo "  ✗ 项目文件不存在，请确保文件已上传到 /tmp/trae0410-kimi"
    exit 1
fi

# 5. 构建后端
echo "[5/7] 构建后端..."
cd /opt/${APP_NAME}/backend
mvn clean package -DskipTests -q
cp target/message-board-1.0.0.jar /opt/${APP_NAME}/backend/
echo "  ✓ 后端构建完成"

# 6. 构建前端
echo "[6/7] 构建前端..."
cd /opt/${APP_NAME}/frontend
npm install -q
npm run build 2>&1 | tail -5
cp -r dist /opt/${APP_NAME}/frontend/
echo "  ✓ 前端构建完成"

# 7. 配置Nginx
echo "[7/7] 配置Nginx..."
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
echo "  ✓ Nginx配置完成"

# 8. 启动后端
echo ""
echo "启动后端服务..."
cd /opt/${APP_NAME}/backend
nohup java -jar message-board-1.0.0.jar \
    --server.port=${BACKEND_PORT} \
    --message.data.path=/opt/${APP_NAME}/data/messages.json \
    > /opt/${APP_NAME}/logs/backend.log 2>&1 &

sleep 5

if lsof -i:${BACKEND_PORT} > /dev/null 2>&1; then
    echo "  ✓ 后端服务启动成功 (端口: ${BACKEND_PORT})"
else
    echo "  ✗ 后端服务启动失败"
    tail -20 /opt/${APP_NAME}/logs/backend.log
    exit 1
fi

# 9. 验证
echo ""
echo "验证部署..."
if curl -s http://localhost:${BACKEND_PORT}/api/messages > /dev/null 2>&1; then
    echo "  ✓ API测试通过"
else
    echo "  ! API测试可能需要等待服务完全启动"
fi

echo ""
echo "========================================"
echo "  部署完成!"
echo "========================================"
echo ""
echo "访问地址:"
echo "  前端: http://$(curl -s ifconfig.me 2>/dev/null || echo '49.235.161.106'):${FRONTEND_PORT}"
echo "  后端API: http://$(curl -s ifconfig.me 2>/dev/null || echo '49.235.161.106'):${BACKEND_PORT}/api"
echo ""
echo "日志: tail -f /opt/${APP_NAME}/logs/backend.log"
echo ""
