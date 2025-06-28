<template>
    <div class="panel-overlay" @click="$emit('close')">
      <div class="history-panel" @click.stop>
        <div class="panel-header">
          <h3 class="panel-title">
            <span class="title-icon">🕒</span>
            搜索历史
          </h3>
          <button @click="$emit('close')" class="close-btn">×</button>
        </div>
        
        <div class="panel-content">
          <div v-if="history.length === 0" class="empty-state">
            <div class="empty-icon">📝</div>
            <h4>暂无搜索历史</h4>
            <p>开始搜索诗词，历史记录会出现在这里</p>
          </div>
          
          <div v-else class="history-list">
            <div class="list-header">
              <span class="history-count">共 {{ history.length }} 条记录</span>
              <button @click="$emit('clear')" class="clear-all-btn">
                <span class="btn-icon">🗑️</span>
                清空全部
              </button>
            </div>
            
            <div class="history-items">
              <div
                v-for="(item, index) in history"
                :key="index"
                @click="searchFromHistory(item)"
                class="history-item"
              >
                <div class="item-content">
                  <div class="search-query">{{ item.query }}</div>
                  <div class="search-time">{{ formatTime(item.timestamp) }}</div>
                </div>
                <div class="item-actions">
                  <button @click.stop="searchFromHistory(item)" class="action-btn search-btn">
                    <span class="action-icon">🔍</span>
                  </button>
                  <button @click.stop="removeHistoryItem(index)" class="action-btn remove-btn">
                    <span class="action-icon">×</span>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  const props = defineProps({
    history: Array
  })
  
  defineEmits(['close', 'search', 'clear'])
  
  const formatTime = (timestamp) => {
    const date = new Date(timestamp)
    const now = new Date()
    const diffMs = now - date
    const diffMins = Math.floor(diffMs / 60000)
    const diffHours = Math.floor(diffMs / 3600000)
    const diffDays = Math.floor(diffMs / 86400000)
    
    if (diffMins < 1) return '刚刚'
    if (diffMins < 60) return `${diffMins}分钟前`
    if (diffHours < 24) return `${diffHours}小时前`
    if (diffDays < 7) return `${diffDays}天前`
    
    return date.toLocaleDateString()
  }
  
  const searchFromHistory = (item) => {
    $emit('search', item.query)
    $emit('close')
  }
  
  const removeHistoryItem = (index) => {
    // 这里需要从父组件实现删除单个历史记录的功能
    console.log('删除历史记录:', index)
  }
  </script>
  
  <style scoped>
  .panel-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
    backdrop-filter: blur(5px);
  }
  
  .history-panel {
    background: white;
    border-radius: 20px;
    width: 90%;
    max-width: 500px;
    max-height: 80vh;
    overflow: hidden;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    animation: panel-appear 0.3s ease-out;
  }
  
  @keyframes panel-appear {
    from {
      opacity: 0;
      transform: scale(0.9) translateY(-20px);
    }
    to {
      opacity: 1;
      transform: scale(1) translateY(0);
    }
  }
  
  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 1.5rem 2rem;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
  }
  
  .panel-title {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    margin: 0;
    font-size: 1.3rem;
    font-weight: 500;
  }
  
  .title-icon {
    font-size: 1.5rem;
  }
  
  .close-btn {
    background: rgba(255, 255, 255, 0.2);
    border: none;
    color: white;
    width: 36px;
    height: 36px;
    border-radius: 50%;
    cursor: pointer;
    font-size: 1.5rem;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;
  }
  
  .close-btn:hover {
    background: rgba(255, 255, 255, 0.3);
    transform: scale(1.1);
  }
  
  .panel-content {
    padding: 1.5rem 2rem 2rem;
    max-height: 60vh;
    overflow-y: auto;
  }
  
  .empty-state {
    text-align: center;
    padding: 3rem 1rem;
    color: #666;
  }
  
  .empty-icon {
    font-size: 4rem;
    margin-bottom: 1rem;
    opacity: 0.5;
  }
  
  .empty-state h4 {
    margin: 0 0 0.5rem 0;
    color: #333;
  }
  
  .empty-state p {
    margin: 0;
    font-size: 0.9rem;
  }
  
  .list-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 1rem;
    padding-bottom: 0.5rem;
    border-bottom: 1px solid #f0f0f0;
  }
  
  .history-count {
    font-size: 0.9rem;
    color: #666;
  }
  
  .clear-all-btn {
    display: flex;
    align-items: center;
    gap: 0.3rem;
    padding: 0.4rem 0.8rem;
    background: #f8f9fa;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    font-size: 0.8rem;
    color: #dc3545;
    transition: all 0.2s;
  }
  
  .clear-all-btn:hover {
    background: #dc3545;
    color: white;
  }
  
  .history-items {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .history-item {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 1rem;
    background: #f8f9fa;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.2s;
    border: 2px solid transparent;
  }
  
  .history-item:hover {
    background: #667eea;
    color: white;
    transform: translateX(5px);
  }
  
  .item-content {
    flex: 1;
  }
  
  .search-query {
    font-weight: 500;
    margin-bottom: 0.2rem;
  }
  
  .search-time {
    font-size: 0.8rem;
    opacity: 0.7;
  }
  
  .item-actions {
    display: flex;
    gap: 0.3rem;
    opacity: 0;
    transition: opacity 0.2s;
  }
  
  .history-item:hover .item-actions {
    opacity: 1;
  }
  
  .action-btn {
    background: rgba(255, 255, 255, 0.2);
    border: none;
    color: currentColor;
    width: 30px;
    height: 30px;
    border-radius: 50%;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;
  }
  
  .action-btn:hover {
    background: rgba(255, 255, 255, 0.3);
    transform: scale(1.1);
  }
  
  .action-icon {
    font-size: 0.9rem;
  }
  
  @media (max-width: 768px) {
    .history-panel {
      width: 95%;
      max-height: 90vh;
    }
    
    .panel-header {
      padding: 1rem 1.5rem;
    }
    
    .panel-content {
      padding: 1rem 1.5rem 1.5rem;
    }
    
    .history-item {
      padding: 0.8rem;
    }
    
    .item-actions {
      opacity: 1; /* 在移动端始终显示操作按钮 */
    }
  }
  </style>