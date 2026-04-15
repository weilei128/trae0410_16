<template>
  <div class="message-list">
    <h2>留言列表 <span v-if="total > 0" class="total-count">(共 {{ total }} 条)</span></h2>
    
    <div v-if="loading" class="loading">
      加载中...
    </div>
    
    <div v-else-if="messages.length === 0" class="empty">
      暂无留言
    </div>
    
    <div v-else>
      <div class="messages">
        <div 
          v-for="message in messages" 
          :key="message.id" 
          class="message-card"
          :class="{ 'admin-message': message.isAdmin }"
        >
          <div class="message-header">
            <div class="user-info">
              <span class="username">{{ message.username }}</span>
              <span v-if="message.isAdmin" class="admin-badge">管理员</span>
            </div>
            <span class="time">{{ formatTime(message.createTime) }}</span>
          </div>
          
          <div class="message-content">
            {{ message.content }}
          </div>
          
          <div v-if="message.email" class="message-email">
            邮箱: {{ message.email }}
          </div>
          
          <div v-if="isAdmin" class="message-actions">
            <button class="delete-btn" @click="$emit('delete', message.id)">
              删除
            </button>
          </div>
        </div>
      </div>
      
      <div v-if="totalPages > 1" class="pagination">
        <button 
          class="page-btn" 
          :disabled="currentPage === 1"
          @click="$emit('page-change', 1)"
        >
          首页
        </button>
        <button 
          class="page-btn" 
          :disabled="currentPage === 1"
          @click="$emit('page-change', currentPage - 1)"
        >
          上一页
        </button>
        <span class="page-info">
          第 {{ currentPage }} / {{ totalPages }} 页
        </span>
        <button 
          class="page-btn" 
          :disabled="currentPage === totalPages"
          @click="$emit('page-change', currentPage + 1)"
        >
          下一页
        </button>
        <button 
          class="page-btn" 
          :disabled="currentPage === totalPages"
          @click="$emit('page-change', totalPages)"
        >
          末页
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MessageList',
  props: {
    messages: {
      type: Array,
      default: () => []
    },
    loading: {
      type: Boolean,
      default: false
    },
    isAdmin: {
      type: Boolean,
      default: false
    },
    total: {
      type: Number,
      default: 0
    },
    currentPage: {
      type: Number,
      default: 1
    },
    pageSize: {
      type: Number,
      default: 10
    }
  },
  computed: {
    totalPages() {
      return Math.ceil(this.total / this.pageSize) || 1
    }
  },
  methods: {
    formatTime(time) {
      if (!time) return ''
      let date
      if (typeof time === 'string') {
        date = new Date(time)
      } else if (Array.isArray(time) && time.length >= 6) {
        date = new Date(
          time[0], 
          time[1] - 1, 
          time[2], 
          time[3] || 0, 
          time[4] || 0, 
          time[5] || 0
        )
      } else {
        date = new Date(time)
      }
      if (isNaN(date.getTime())) {
        return ''
      }
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    }
  }
}
</script>

<style scoped>
.message-list {
  background: white;
  border-radius: 12px;
  padding: 25px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.message-list h2 {
  color: #333;
  margin-bottom: 20px;
  font-size: 1.5rem;
}

.total-count {
  font-size: 0.9rem;
  color: #888;
  font-weight: normal;
}

.loading,
.empty {
  text-align: center;
  padding: 40px;
  color: #999;
  font-size: 16px;
}

.messages {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.message-card {
  background: #f8f9fa;
  border-radius: 10px;
  padding: 18px;
  border-left: 4px solid #667eea;
  transition: transform 0.2s, box-shadow 0.2s;
}

.message-card:hover {
  transform: translateX(5px);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.message-card.admin-message {
  border-left-color: #ff6b6b;
  background: #fff5f5;
}

.message-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.username {
  font-weight: 600;
  color: #333;
  font-size: 15px;
}

.admin-badge {
  background: #ff6b6b;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.time {
  color: #888;
  font-size: 13px;
}

.message-content {
  color: #444;
  line-height: 1.6;
  word-break: break-word;
}

.message-email {
  margin-top: 10px;
  font-size: 13px;
  color: #666;
}

.message-actions {
  margin-top: 12px;
  text-align: right;
}

.delete-btn {
  background: #ff4757;
  color: white;
  border: none;
  padding: 6px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.2s;
}

.delete-btn:hover {
  background: #ff3344;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.page-btn {
  background: #667eea;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background 0.2s;
}

.page-btn:hover:not(:disabled) {
  background: #5a6fd6;
}

.page-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.page-info {
  color: #666;
  font-size: 14px;
  padding: 0 10px;
}
</style>
