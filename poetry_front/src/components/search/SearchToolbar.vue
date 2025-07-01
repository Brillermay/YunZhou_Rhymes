<template>
  <div class="search-toolbar">
    <div class="toolbar-left">
      <!-- 搜索历史 -->
      <button 
        class="toolbar-btn"
        @click="$emit('show-history')"
        title="搜索历史"
      >
        📚 历史 ({{ searchHistory.length }})
      </button>

      <!-- 收藏按钮 -->
      <button 
        class="toolbar-btn"
        @click="handleFavoritesClick"
        :title="isAuthenticated ? '我的收藏' : '请先登录'"
        :class="{ 'disabled': !isAuthenticated }"
      >
        ⭐ 收藏 ({{ favoritePoems.length }})
      </button>

      <!-- 用户状态指示 -->
      <div class="user-status">
        <span v-if="isAuthenticated" class="user-info">
          👤 {{ username }}
        </span>
        <span v-else class="login-hint" @click="$emit('show-login')">
          🔒 未登录
        </span>
      </div>
    </div>

    <div class="toolbar-right">
      <!-- 字体大小 -->
      <div class="font-controls">
        <button 
          class="toolbar-btn"
          @click="$emit('adjust-font', -2)"
          title="减小字体"
        >
          A-
        </button>
        <span class="font-size">{{ fontSize }}px</span>
        <button 
          class="toolbar-btn"
          @click="$emit('adjust-font', 2)"
          title="增大字体"
        >
          A+
        </button>
      </div>

      <!-- 视图模式 -->
      <div class="view-controls">
        <button 
          class="toolbar-btn"
          :class="{ active: viewMode === 'grid' }"
          @click="$emit('change-view', 'grid')"
          title="网格视图"
        >
          ⚏
        </button>
        <button 
          class="toolbar-btn"
          :class="{ active: viewMode === 'list' }"
          @click="$emit('change-view', 'list')"
          title="列表视图"
        >
          ☰
        </button>
      </div>

      <!-- 导出按钮 -->
      <button 
        class="toolbar-btn"
        @click="$emit('export-favorites')"
        :disabled="favoritePoems.length === 0"
        title="导出收藏"
      >
        📤 导出
      </button>
    </div>
  </div>
</template>

<script setup>
// 定义 props
const props = defineProps({
  searchHistory: {
    type: Array,
    default: () => []
  },
  favoritePoems: {
    type: Array,
    default: () => []
  },
  fontSize: {
    type: Number,
    default: 18
  },
  viewMode: {
    type: String,
    default: 'grid'
  },
  isAuthenticated: {
    type: Boolean,
    default: false
  },
  username: {
    type: String,
    default: ''
  }
})

// 定义事件
const emit = defineEmits([
  'show-history',
  'show-favorites', 
  'adjust-font',
  'change-view',
  'export-favorites',
  'show-login'
])

// 处理收藏按钮点击
const handleFavoritesClick = () => {
  if (props.isAuthenticated) {
    emit('show-favorites')
  } else {
    emit('show-login')
  }
}
</script>

<style scoped>
.search-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(140, 120, 83, 0.1);
  gap: 1rem;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.toolbar-btn {
  padding: 0.5rem 1rem;
  border: 1px solid rgba(140, 120, 83, 0.3);
  border-radius: 8px;
  background: rgba(245, 239, 230, 0.8);
  color: #8c7853;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.toolbar-btn:hover:not(.disabled) {
  background: #8c7853;
  color: white;
  transform: translateY(-1px);
}

.toolbar-btn.active {
  background: #8c7853;
  color: white;
}

.toolbar-btn.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.toolbar-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.user-status {
  display: flex;
  align-items: center;
  font-size: 0.9rem;
}

.user-info {
  color: #8c7853;
  font-weight: 500;
}

.login-hint {
  color: #999;
  cursor: pointer;
  transition: color 0.3s ease;
}

.login-hint:hover {
  color: #8c7853;
  text-decoration: underline;
}

.font-controls,
.view-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.font-size {
  font-size: 0.8rem;
  color: #666;
  min-width: 40px;
  text-align: center;
}

@media (max-width: 768px) {
  .search-toolbar {
    flex-direction: column;
    gap: 1rem;
    padding: 1rem;
  }
  
  .toolbar-left,
  .toolbar-right {
    width: 100%;
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .toolbar-btn {
    font-size: 0.8rem;
    padding: 0.4rem 0.8rem;
  }
}
</style>