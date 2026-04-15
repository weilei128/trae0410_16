# 在线留言反馈系统 - 部署指南

## 服务器信息
- IP: 49.235.161.106
- SSH端口: 22
- 认证方式: SSH密钥
- 后端端口: 10013
- 前端端口: 10023

## 部署步骤

### 1. 上传文件到服务器

在本地PowerShell中执行：

```powershell
# 设置变量
$SERVER_IP = "49.235.161.106"
$SSH_KEY = "~/.ssh/your_key.pem"  # 替换为您的SSH密钥路径

# 上传后端jar包
scp -i $SSH_KEY -P 22 backend/target/message-board-1.0.0.jar root@${SERVER_IP}:/tmp/

# 上传前端源码（在服务器上构建）
scp -i $SSH_KEY -P 22 -r frontend root@${SERVER_IP}:/tmp/

# 上传部署脚本
scp -i $SSH_KEY -P 22 deploy_remote.sh root@${SERVER_IP}:/tmp/
```

### 2. 连接服务器并执行部署

```powershell
ssh -i $SSH_KEY -p 22 root@${SERVER_IP}
```

在服务器上执行：

```bash
chmod +x /tmp/deploy_remote.sh
/tmp/deploy_remote.sh
```

### 3. 验证部署

部署完成后，访问以下地址：
- 前端: http://49.235.161.106:10023
- 后端API: http://49.235.161.106:10013/api/messages

### 4. 常用命令

```bash
# 查看后端日志
tail -f /opt/message-board-10013/logs/backend.log

# 停止后端服务
kill $(lsof -t -i:10013)

# 重启后端服务
cd /opt/message-board-10013/backend
nohup java -jar message-board-1.0.0.jar \
    --server.port=10013 \
    --message.data.path=/opt/message-board-10013/data/messages.json \
    > /opt/message-board-10013/logs/backend.log 2>&1 &

# 重启Nginx
systemctl restart nginx

# 查看端口占用
lsof -i:10013
lsof -i:10023
```

## 项目结构

```
/opt/message-board-10013/
├── backend/
│   └── message-board-1.0.0.jar    # 后端jar包
├── frontend/
│   └── dist/                       # 前端构建产物
├── logs/
│   └── backend.log                 # 后端日志
└── data/
    └── messages.json               # 数据文件
```

## 功能特性

1. **留言功能**
   - 发表留言（用户名、邮箱、内容）
   - 敏感词过滤（脏话、暴力、广告等）
   - 时间倒序排列

2. **分页功能**
   - 支持分页显示
   - 每页10条记录
   - 显示总页数和总条数

3. **管理员功能**
   - 管理员视图切换
   - 删除留言（逻辑删除）
   - 管理员标识显示

4. **数据存储**
   - JSON文件存储
   - 无需数据库
   - 数据持久化

## API接口

- `POST /api/messages` - 添加留言
- `GET /api/messages` - 分页获取留言列表
- `GET /api/messages/all` - 获取所有留言
- `GET /api/messages/{id}` - 根据ID获取留言
- `DELETE /api/messages/{id}` - 删除留言

## 修复的Bug

1. 跨域问题 - 统一CorsFilter配置
2. 留言内容丢失 - 完善空值检查
3. 敏感词过滤失效 - 修复过滤逻辑
4. 列表加载异常 - 添加空值处理和异常处理

## 代码优化

1. 抽取通用工具类（JsonFileUtil、SensitiveWordUtil）
2. 统一请求响应格式（Result、PageResult）
3. 添加全局异常处理器
4. 规范代码风格和注释
