<template>
    <div class="favorites-panel-overlay" @click="$emit('close')">
      <div class="favorites-panel" @click.stop>
        <div class="panel-header">
          <h3>
            <span class="panel-icon">❤️</span>
            我的收藏
            <span class="count-badge">{{ favorites.length }}</span>
          </h3>
          <button @click="$emit('close')" class="close-btn">×</button>
        </div>
        
        <div class="panel-content">
          <div v-if="favorites.length === 0" class="empty-state">
            <div class="empty-icon">📚</div>
            <p>暂无收藏的诗词</p>
            <p class="empty-hint">在搜索结果中点击❤️收藏诗词</p>
          </div>
          
          <div v-else class="favorites-list">
            <div
              v-for="poem in favorites"
              :key="poem.PID"
              class="favorite-item"
              @click="$emit('view-detail', poem.PID)"
            >
              <div class="poem-info">
                <h4 class="poem-title">{{ poem.title }}</h4>
                <p class="poem-author">{{ poem.poet }}</p>
                <p class="poem-preview">{{ getTextPreview(poem.text) }}</p>
                <div v-if="poem.category" class="poem-category">
                  <span class="category-tag">{{ poem.category }}</span>
                </div>
              </div>
              <button
                @click.stop="$emit('toggle-favorite', poem.PID)"
                class="remove-btn"
                title="取消收藏"
              >
                🗑️
              </button>
            </div>
          </div>
        </div>
        
        <div v-if="favorites.length > 0" class="panel-footer">
          <button @click="$emit('clear-all')" class="clear-all-btn">
            清空全部
          </button>
          <button @click="exportFavorites" class="export-btn">
            导出收藏
          </button>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  const props = defineProps({
    favorites: Array
  })
  
  const emit = defineEmits([
    'close',
    'view-detail',
    'toggle-favorite',
    'clear-all'
  ])
  
  // 获取文本预览
  const getTextPreview = (text) => {
    if (!text) return ''
    
    if (Array.isArray(text)) {
      return text.slice(0, 2).join(' ')
    }
    
    if (text.length > 60) {
      return text.substring(0, 60) + '...'
    }
    
    return text
  }
  
  // 导出收藏
  const exportFavorites = () => {
    if (props.favorites.length === 0) {
      alert('暂无收藏内容')
      return
    }
    
    try {
      const exportData = {
        exportTime: new Date().toLocaleString(),
        totalCount: props.favorites.length,
        poems: props.favorites.map(poem => ({
          title: poem.title,
          poet: poem.poet,
          text: poem.text,
          category: poem.category
        }))
      }
      
      const dataStr = JSON.stringify(exportData, null, 2)
      const dataBlob = new Blob([dataStr], { type: 'application/json;charset=utf-8' })
      const url = URL.createObjectURL(dataBlob)
      const link = document.createElement('a')
      link.href = url
      link.download = `我的诗词收藏_${new Date().toISOString().split('T')[0]}.json`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
      
      console.log('✅ 收藏导出完成')
    } catch (error) {
      console.error('❌ 导出收藏失败:', error)
      alert(`导出失败: ${error.message}`)
    }
  }
  </script>
  
  <style scoped>
  .favorites-panel-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
    backdrop-filter: blur(4px);
  }
  
  .favorites-panel {
    background: white;
    border-radius: 20px;
    width: 90%;
    max-width: 700px;
    max-height: 85vh;
    display: flex;
    flex-direction: column;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
    overflow: hidden;
  }
  
  .panel-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1.5rem 2rem;
    border-bottom: 1px solid #f0f0f0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
  }
  
  .panel-header h3 {
    margin: 0;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 1.3rem;
    font-weight: 600;
  }
  
  .panel-icon {
    font-size: 1.5rem;
  }
  
  .count-badge {
    background: rgba(255, 255, 255, 0.2);
    color: white;
    border-radius: 12px;
    padding: 0.2rem 0.8rem;
    font-size: 0.85rem;
    font-weight: 500;
  }
  
  .close-btn {
    background: rgba(255, 255, 255, 0.1);
    border: none;
    color: white;
    font-size: 1.8rem;
    cursor: pointer;
    padding: 0.5rem;
    border-radius: 50%;
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;
  }
  
  .close-btn:hover {
    background: rgba(255, 255, 255, 0.2);
  }
  
  .panel-content {
    flex: 1;
    overflow-y: auto;
    padding: 0;
  }
  
  .empty-state {
    text-align: center;
    padding: 4rem 2rem;
    color: #666;
  }
  
  .empty-icon {
    font-size: 4rem;
    margin-bottom: 1.5rem;
    opacity: 0.6;
  }
  
  .empty-state p {
    margin: 0.5rem 0;
    font-size: 1.1rem;
  }
  
  .empty-hint {
    color: #888;
    font-size: 0.9rem;
    font-style: italic;
  }
  
  .favorites-list {
    padding: 1rem;
  }
  
  .favorite-item {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    padding: 1.5rem;
    margin-bottom: 1rem;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.3s ease;
    background: #f8f9fa;
    border: 1px solid #e9ecef;
  }
  
  .favorite-item:hover {
    background: #e9ecef;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
  
  .poem-info {
    flex: 1;
    margin-right: 1rem;
  }
  
  .poem-title {
    margin: 0 0 0.5rem 0;
    font-size: 1.2rem;
    color: #333;
    font-weight: 600;
  }
  
  .poem-author {
    margin: 0 0 0.5rem 0;
    color: #667eea;
    font-size: 0.95rem;
    font-weight: 500;
  }
  
  .poem-preview {
    margin: 0 0 0.5rem 0;
    color: #666;
    font-size: 0.9rem;
    line-height: 1.5;
  }
  
  .poem-category {
    margin-top: 0.5rem;
  }
  
  .category-tag {
    display: inline-block;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 0.2rem 0.6rem;
    border-radius: 10px;
    font-size: 0.75rem;
    font-weight: 500;
  }
  
  .remove-btn {
    background: rgba(231, 76, 60, 0.1);
    border: none;
    color: #e74c3c;
    font-size: 1.2rem;
    cursor: pointer;
    padding: 0.5rem;
    border-radius: 50%;
    width: 36px;
    height: 36px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s ease;
    flex-shrink: 0;
  }
  
  .remove-btn:hover {
    background: rgba(231, 76, 60, 0.2);
    transform: scale(1.1);
  }
  
  .panel-footer {
    padding: 1.5rem 2rem;
    border-top: 1px solid #f0f0f0;
    background: #f8f9fa;
    display: flex;
    gap: 1rem;
    justify-content: flex-end;
  }
  
  .clear-all-btn, .export-btn {
    padding: 0.8rem 1.5rem;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    font-size: 0.9rem;
    font-weight: 500;
    transition: all 0.2s ease;
  }
  
  .clear-all-btn {
    background: #e74c3c;
    color: white;
  }
  
  .clear-all-btn:hover {
    background: #c0392b;
  }
  
  .export-btn {
    background: #667eea;
    color: white;
  }
  
  .export-btn:hover {
    background: #5a6fd8;
  }
  
  @media (max-width: 768px) {
    .favorites-panel {
      width: 95%;
      max-height: 90vh;
    }
    
    .panel-header {
      padding: 1rem 1.5rem;
    }
    
    .panel-header h3 {
      font-size: 1.1rem;
    }
    
    .favorite-item {
      padding: 1rem;
      flex-direction: column;
      align-items: stretch;
    }
    
    .poem-info {
      margin-right: 0;
      margin-bottom: 1rem;
    }
    
    .remove-btn {
      align-self: center;
    }
    
    .panel-footer {
      padding: 1rem 1.5rem;
      flex-direction: column;
    }
  }
  </style>