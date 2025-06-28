<template>
    <div class="search-results">
      <!-- 结果头部 -->
      <div class="results-header">
        <div class="results-info">
          <h2 class="results-title">
            <span class="search-icon">{{ searchType === 'ai' ? '🤖' : '🔍' }}</span>
            搜索结果
          </h2>
          <div class="results-meta">
            <span class="query-display">"{{ searchQuery }}"</span>
            <span class="results-count">找到 {{ searchResults.length }} 首诗词</span>
            <span class="search-type-badge" :class="searchType">
              {{ searchType === 'ai' ? 'AI智能搜索' : '传统搜索' }}
            </span>
          </div>
        </div>
        
        <div class="results-actions">
          <button @click="$emit('clear-search')" class="clear-search-btn">
            <span class="btn-icon">↻</span>
            重新搜索
          </button>
        </div>
      </div>
  
      <!-- 排序和视图控制 -->
      <div class="results-controls">
        <div class="sort-controls">
          <span class="control-label">排序：</span>
          <select 
            :value="sortBy" 
            @change="$emit('sort-change', $event.target.value)"
            class="sort-select"
          >
            <option value="relevance">相关度</option>
            <option value="title">标题</option>
            <option value="poet">作者</option>
            <option value="dynasty">朝代</option>
          </select>
        </div>
        
        <div class="view-controls">
          <span class="control-label">视图：</span>
          <button 
            @click="$emit('change-view', 'grid')"
            class="view-btn"
            :class="{ 'active': viewMode === 'grid' }"
          >
            ⊞ 网格
          </button>
          <button 
            @click="$emit('change-view', 'list')"
            class="view-btn"
            :class="{ 'active': viewMode === 'list' }"
          >
            ☰ 列表
          </button>
        </div>
      </div>
  
      <!-- 搜索结果列表 -->
      <div v-if="searchResults.length === 0" class="no-results">
        <div class="no-results-icon">😔</div>
        <h3>没有找到相关诗词</h3>
        <p>试试调整搜索关键词，或使用AI智能搜索</p>
        <div class="suggestions">
          <span class="suggestion-label">建议搜索：</span>
          <button 
            v-for="suggestion in searchSuggestions"
            :key="suggestion"
            @click="suggestSearch(suggestion)"
            class="suggestion-btn"
          >
            {{ suggestion }}
          </button>
        </div>
      </div>
  
      <div v-else :class="['results-grid', viewMode]">
        <!-- 🔧 修复：传递收藏相关属性 -->
        <PoemCard
          v-for="poem in sortedResults"
          :key="poem.PID || poem.id"
          :poem="poem"
          :font-size="fontSize"
          :search-query="searchQuery"
          :view-mode="viewMode"
          :is-favorite="isFavorite"
          @view-detail="$emit('view-detail', poem.PID || poem.id)"
          @toggle-favorite="$emit('toggle-favorite', poem.PID || poem.id)"
        />
      </div>
  
      <!-- 加载更多 -->
      <div v-if="searchResults.length > 0" class="load-more-section">
        <button class="load-more-btn" @click="loadMore">
          <span class="btn-icon">↓</span>
          加载更多结果
        </button>
      </div>
    </div>
  </template>
  
  <script setup>
  import { computed } from 'vue'
  import PoemCard from './PoemCard.vue'
  
  // 🔧 修复：添加 isFavorite 属性
  const props = defineProps({
    searchResults: Array,
    searchQuery: String,
    sortBy: String,
    viewMode: String,
    fontSize: Number,
    searchType: String,
    isFavorite: Function
  })
  
  // 🔧 修复：添加 toggle-favorite emit
  defineEmits([
    'sort-change',
    'view-detail',
    'toggle-favorite',
    'clear-search',
    'change-view'
  ])
  
  const searchSuggestions = [
    '李白的诗', '思乡诗词', '春天的诗', '月亮', '爱情诗词'
  ]
  
  const sortedResults = computed(() => {
    const results = [...props.searchResults]
    
    switch (props.sortBy) {
      case 'title':
        return results.sort((a, b) => (a.Title || a.title || '').localeCompare(b.Title || b.title || ''))
      case 'poet':
        return results.sort((a, b) => (a.Poet || a.poet || '').localeCompare(b.Poet || b.poet || ''))
      case 'dynasty':
        return results.sort((a, b) => (a.Dynasty || a.dynasty || '').localeCompare(b.Dynasty || b.dynasty || ''))
      default:
        return results // 保持原始相关度排序
    }
  })
  
  const suggestSearch = (suggestion) => {
    // 触发新的搜索
    console.log('建议搜索:', suggestion)
  }
  
  const loadMore = () => {
    // 加载更多结果
    console.log('加载更多结果')
  }
  </script>
  
  <style scoped>
  .search-results {
    max-width: 1200px;
    margin: 0 auto;
  }
  
  .results-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 2rem;
    padding-bottom: 1rem;
    border-bottom: 2px solid #f0f0f0;
  }
  
  .results-info {
    flex: 1;
  }
  
  .results-title {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: #333;
    font-size: 1.8rem;
    margin: 0 0 0.5rem 0;
    font-weight: 600;
  }
  
  .search-icon {
    font-size: 1.5rem;
  }
  
  .results-meta {
    display: flex;
    align-items: center;
    gap: 1rem;
    flex-wrap: wrap;
  }
  
  .query-display {
    font-weight: 600;
    color: #667eea;
    background: rgba(102, 126, 234, 0.1);
    padding: 0.3rem 0.8rem;
    border-radius: 15px;
    font-size: 0.9rem;
  }
  
  .results-count {
    color: #666;
    font-size: 0.9rem;
  }
  
  .search-type-badge {
    padding: 0.3rem 0.8rem;
    border-radius: 12px;
    font-size: 0.8rem;
    font-weight: 500;
  }
  
  .search-type-badge.traditional {
    background: linear-gradient(135deg, #e3f2fd 0%, #f0f8ff 100%);
    color: #1976d2;
  }
  
  .search-type-badge.ai {
    background: linear-gradient(135deg, #f3e5f5 0%, #faf0fc 100%);
    color: #7b1fa2;
  }
  
  .clear-search-btn {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    background: #f8f9fa;
    border: 1px solid #e1e5e9;
    padding: 0.7rem 1.2rem;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s ease;
    color: #666;
    font-size: 0.9rem;
  }
  
  .clear-search-btn:hover {
    background: #e9ecef;
    color: #333;
  }
  
  .results-controls {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 2rem;
    padding: 1rem;
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  }
  
  .sort-controls, .view-controls {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }
  
  .control-label {
    font-weight: 500;
    color: #555;
    font-size: 0.9rem;
  }
  
  .sort-select {
    padding: 0.5rem 1rem;
    border: 1px solid #ddd;
    border-radius: 6px;
    background: white;
    cursor: pointer;
    font-size: 0.9rem;
  }
  
  .view-btn {
    padding: 0.5rem 1rem;
    border: 1px solid #ddd;
    background: white;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.2s ease;
    font-size: 0.85rem;
  }
  
  .view-btn:hover, .view-btn.active {
    background: #667eea;
    color: white;
    border-color: #667eea;
  }
  
  .no-results {
    text-align: center;
    padding: 4rem 2rem;
    color: #666;
  }
  
  .no-results-icon {
    font-size: 4rem;
    margin-bottom: 1rem;
  }
  
  .no-results h3 {
    margin: 0 0 1rem 0;
    color: #333;
  }
  
  .suggestions {
    margin-top: 2rem;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    flex-wrap: wrap;
  }
  
  .suggestion-label {
    font-weight: 500;
    color: #555;
  }
  
  .suggestion-btn {
    background: #f8f9fa;
    border: 1px solid #e1e5e9;
    padding: 0.5rem 1rem;
    border-radius: 20px;
    cursor: pointer;
    transition: all 0.2s ease;
    font-size: 0.85rem;
  }
  
  .suggestion-btn:hover {
    background: #667eea;
    color: white;
    border-color: #667eea;
  }
  
  .results-grid {
    display: grid;
    gap: 1.5rem;
  }
  
  .results-grid.grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  }
  
  .results-grid.list {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .load-more-section {
    text-align: center;
    margin-top: 3rem;
    padding: 2rem 0;
  }
  
  .load-more-btn {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border: none;
    padding: 1rem 2rem;
    border-radius: 25px;
    cursor: pointer;
    font-size: 1rem;
    font-weight: 500;
    transition: all 0.3s ease;
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
  }
  
  .load-more-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  }
  
  @media (max-width: 768px) {
    .results-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 1rem;
    }
    
    .results-controls {
      flex-direction: column;
      align-items: flex-start;
      gap: 1rem;
    }
    
    .results-grid.grid {
      grid-template-columns: 1fr;
    }
    
    .results-meta {
      flex-direction: column;
      align-items: flex-start;
      gap: 0.5rem;
    }
  }
  </style>