<template>
  <div class="search-toolbar">
    <div class="toolbar-container">
      <!-- 左侧功能区 -->
      <div class="toolbar-left">
        <button 
          @click="$emit('show-history')"
          class="toolbar-btn history-btn"
          :class="{ 'has-items': searchHistory.length > 0 }"
        >
          <span class="btn-icon">🕒</span>
          <span class="btn-text">搜索历史</span>
          <span v-if="searchHistory.length > 0" class="item-count">{{ searchHistory.length }}</span>
        </button>
        
        <button 
          @click="$emit('show-favorites')"
          class="toolbar-btn favorites-btn"
          :class="{ 'has-items': favoritePoems.length > 0 }"
        >
          <span class="btn-icon">❤️</span>
          <span class="btn-text">我的收藏</span>
          <span v-if="favoritePoems.length > 0" class="item-count">{{ favoritePoems.length }}</span>
        </button>
        
        <button 
          @click="$emit('export-favorites')"
          class="toolbar-btn export-btn"
          :disabled="favoritePoems.length === 0"
        >
          <span class="btn-icon">📤</span>
          <span class="btn-text">导出收藏</span>
        </button>
      </div>

      <!-- 中间状态区 -->
      <div class="toolbar-center">
        <div class="status-info">
          <span class="status-icon">🤖</span>
          <span class="status-text">AI智能搜索已就绪</span>
          <div class="status-indicator"></div>
        </div>
      </div>

      <!-- 右侧设置区 -->
      <div class="toolbar-right">
        <!-- 字体大小调节 -->
        <div class="font-controls">
          <span class="control-label">字体</span>
          <button 
            @click="$emit('adjust-font', -2)"
            class="font-btn decrease"
            :disabled="fontSize <= 12"
          >
            A-
          </button>
          <span class="font-size-display">{{ fontSize }}px</span>
          <button 
            @click="$emit('adjust-font', 2)"
            class="font-btn increase"
            :disabled="fontSize >= 28"
          >
            A+
          </button>
        </div>

        <!-- 视图模式切换 -->
        <div class="view-controls">
          <span class="control-label">视图</span>
          <button 
            @click="$emit('change-view', 'grid')"
            class="view-btn"
            :class="{ 'active': viewMode === 'grid' }"
          >
            <span class="view-icon">⊞</span>
            网格
          </button>
          <button 
            @click="$emit('change-view', 'list')"
            class="view-btn"
            :class="{ 'active': viewMode === 'list' }"
          >
            <span class="view-icon">☰</span>
            列表
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  searchHistory: Array,
  favoritePoems: Array,
  fontSize: Number,
  viewMode: String
})

defineEmits([
  'show-history',
  'show-favorites',
  'adjust-font',
  'change-view',
  'export-favorites'
])
</script>

<style scoped>
.search-toolbar {
  background: white;
  border-bottom: 1px solid #e1e5e9;
  padding: 1rem 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.toolbar-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 2rem;
}

.toolbar-left {
  display: flex;
  gap: 1rem;
}

.toolbar-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.7rem 1rem;
  background: #f8f9fa;
  border: 2px solid transparent;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
  position: relative;
}

.toolbar-btn:hover {
  background: #667eea;
  color: white;
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

.toolbar-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.toolbar-btn.has-items {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-icon {
  font-size: 1.1rem;
}

.btn-text {
  font-weight: 500;
}

.item-count {
  background: rgba(255, 255, 255, 0.3);
  padding: 0.2rem 0.5rem;
  border-radius: 10px;
  font-size: 0.75rem;
  font-weight: 600;
  min-width: 20px;
  text-align: center;
}

.toolbar-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.status-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: linear-gradient(135deg, #e8f5e8 0%, #f0f8f0 100%);
  border-radius: 20px;
  color: #2e7d32;
  font-size: 0.85rem;
  position: relative;
}

.status-icon {
  font-size: 1rem;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.status-indicator {
  width: 8px;
  height: 8px;
  background: #4caf50;
  border-radius: 50%;
  animation: blink 1.5s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}

.toolbar-right {
  display: flex;
  gap: 2rem;
  align-items: center;
}

.font-controls,
.view-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.control-label {
  font-size: 0.85rem;
  color: #666;
  font-weight: 500;
}

.font-btn {
  background: #f0f0f0;
  border: none;
  padding: 0.4rem 0.8rem;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.85rem;
  font-weight: 600;
}

.font-btn:hover:not(:disabled) {
  background: #8c7853;
  color: white;
}

.font-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.font-size-display {
  font-size: 0.8rem;
  color: #666;
  font-weight: 500;
  min-width: 35px;
  text-align: center;
}

.view-btn {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.5rem 0.8rem;
  background: #f0f0f0;
  border: 2px solid transparent;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 0.8rem;
}

.view-btn:hover {
  background: #e0e0e0;
}

.view-btn.active {
  background: #667eea;
  color: white;
  border-color: #5a6fd8;
}

.view-icon {
  font-size: 0.9rem;
}

@media (max-width: 768px) {
  .search-toolbar {
    padding: 0.8rem 1rem;
  }
  
  .toolbar-container {
    flex-direction: column;
    gap: 1rem;
  }
  
  .toolbar-left {
    order: 2;
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .toolbar-center {
    order: 1;
  }
  
  .toolbar-right {
    order: 3;
    gap: 1rem;
  }
  
  .font-controls,
  .view-controls {
    gap: 0.3rem;
  }
  
  .btn-text {
    display: none;
  }
}
</style>