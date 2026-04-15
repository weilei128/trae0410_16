<template>
  <div class="pagination" v-if="totalPages > 1">
    <button
      class="page-btn"
      :disabled="currentPage <= 1"
      @click="goToPage(currentPage - 1)"
    >
      上一页
    </button>

    <span class="page-info">
      第 {{ currentPage }} / {{ totalPages }} 页
    </span>

    <button
      class="page-btn"
      :disabled="currentPage >= totalPages"
      @click="goToPage(currentPage + 1)"
    >
      下一页
    </button>

    <span class="total-info">
      共 {{ total }} 条
    </span>
  </div>
</template>

<script>
export default {
  name: 'Pagination',
  props: {
    currentPage: {
      type: Number,
      default: 1
    },
    totalPages: {
      type: Number,
      default: 1
    },
    total: {
      type: Number,
      default: 0
    }
  },
  emits: ['page-change'],
  methods: {
    goToPage(page) {
      if (page >= 1 && page <= this.totalPages && page !== this.currentPage) {
        this.$emit('page-change', page)
      }
    }
  }
}
</script>

<style scoped>
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 15px;
  margin-top: 25px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.page-btn {
  padding: 8px 16px;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background: #5568d3;
}

.page-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.page-info {
  font-size: 14px;
  color: #666;
}

.total-info {
  font-size: 13px;
  color: #999;
  margin-left: 10px;
}
</style>
