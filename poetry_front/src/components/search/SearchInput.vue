<template>
    <div class="search-section">
      <div class="search-container">
        <!-- 主搜索框 -->
        <div class="main-search-box" :class="{ 'focused': isFocused }">
          <div class="search-input-wrapper">
            <span class="search-icon">🔍</span>
            <input
            :value="modelValue"
            @input="$emit('update:modelValue', $event.target.value)"
            @focus="$emit('focus')"
            @blur="$emit('blur')"
            @keydown="handleKeydown"
            class="search-input"
            :placeholder="getPlaceholder()"
            autocomplete="off"
            />
            <div class="search-actions">
              <!-- 模式指示器 -->
              <div class="mode-indicator" :class="currentMode">
                <span class="mode-tag">{{ currentMode === 'ai' ? 'AI' : '精确' }}</span>
              </div>
              <button 
                v-if="modelValue" 
                @click="$emit('clear')"
                class="clear-btn"
              >
                ×
              </button>
            </div>
          </div>
          
          <!-- 搜索建议下拉 -->
          <SearchSuggestions
            v-if="showSuggestions && suggestions.length"
            :suggestions="suggestions"
            :highlighted-index="highlightedIndex"
            @select="$emit('select-suggestion', $event)"
          />
        </div>
  
        <!-- 搜索模式选择 -->
        <div class="search-modes">
          <!-- 传统搜索按钮 -->
          <button 
            @click="switchToTraditionalMode"
            class="search-mode-btn traditional-mode"
            :class="{ 'active': currentMode === 'traditional' }"
          >
            <span class="mode-icon">🔍</span>
            <div class="mode-content">
              <span class="mode-title">传统搜索</span>
              <span class="mode-desc">关键词精确匹配</span>
            </div>
          </button>
          
          <!-- AI智能搜索按钮 -->
          <button 
            @click="switchToAIMode"
            class="search-mode-btn ai-mode"
            :class="{ 'active': currentMode === 'ai' }"
          >
            <span class="mode-icon">🤖</span>
            <div class="mode-content">
              <span class="mode-title">AI智能搜索</span>
              <span class="mode-desc">语义理解，智能推荐</span>
            </div>
          </button>
        </div>
  
        <!-- 快速搜索标签 -->
        <QuickSearchTags @quick-search="handleQuickSearch" />
      </div>
    </div>
  </template>
  
  <script setup>
  import SearchSuggestions from './SearchSuggestions.vue'
  import QuickSearchTags from './QuickSearchTags.vue'
  
  const props = defineProps({
    modelValue: String,
    isFocused: Boolean,
    suggestions: Array,
    showSuggestions: Boolean,
    highlightedIndex: Number,
    currentMode: {
      type: String,
      default: 'traditional',
      validator: value => ['traditional', 'ai'].includes(value)
    }
  })
  
  const emit = defineEmits([
    'update:modelValue',
    'search',
    'mode-change',
    'focus',
    'blur',
    'keydown',
    'clear',
    'quick-search',
    'select-suggestion'
  ])
  
 // 只处理回车，其他键不 emit
const handleKeydown = (event) => {
  if (event.key === 'Enter') {
    event.preventDefault()
    event.stopPropagation()
    emit('search')
  }
}
  
  // 获取动态占位符
  const getPlaceholder = () => {
    if (props.currentMode === 'ai') {
      return '🤖 AI智能搜索：试试「描述月亮的诗」「李白的豪放诗词」「思乡主题」...'
    } else {
      return '🔍 传统搜索：输入关键词进行精确匹配，如「春」「李白」「静夜思」...'
    }
  }
  
  // 🔧 修复：切换到传统搜索模式
  const switchToTraditionalMode = () => {
    console.log('🔄 切换到传统搜索模式')
    emit('mode-change', 'traditional')
  }
  
  // 🔧 修复：切换到AI搜索模式
  const switchToAIMode = () => {
    console.log('🔄 切换到AI搜索模式')
    emit('mode-change', 'ai')
  }
  
  // 🔧 修复：快速搜索处理
  const handleQuickSearch = (tag) => {
    // 根据标签类型智能选择搜索模式
    emit('quick-search', { 
      tag, 
      mode: ['李白', '杜甫', '苏轼'].includes(tag) ? 'traditional' : 'ai'
    })
  }
  </script>
  
  <!-- 样式保持不变 -->
  <style scoped>
  .search-section {
    padding: 2rem 1rem;
    background: white;
    box-shadow: 0 2px 15px rgba(0, 0, 0, 0.05);
  }
  
  .search-container {
    max-width: 900px;
    margin: 0 auto;
  }
  
  .main-search-box {
    position: relative;
    margin-bottom: 2rem;
  }
  
  .search-input-wrapper {
    position: relative;
    display: flex;
    align-items: center;
  }
  
  .search-icon {
    position: absolute;
    left: 1.5rem;
    font-size: 1.4rem;
    color: #667eea;
    z-index: 2;
  }
  
  .search-input {
    width: 100%;
    padding: 1.2rem 1.5rem 1.2rem 4rem;
    border: 3px solid #e1e5e9;
    border-radius: 25px;
    font-size: 1.1rem;
    color: #333;
    background: white;
    outline: none;
    transition: all 0.3s ease;
    padding-right: 8rem;
  }
  
  .search-input:focus {
    border-color: #667eea;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
  }
  
  .search-actions {
    position: absolute;
    right: 1rem;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    z-index: 2;
  }
  
  .mode-indicator {
    padding: 0.3rem 0.8rem;
    border-radius: 12px;
    font-size: 0.75rem;
    font-weight: 600;
    transition: all 0.3s ease;
  }
  
  .mode-indicator.traditional {
    background: linear-gradient(135deg, #e3f2fd 0%, #f0f8ff 100%);
    color: #1976d2;
    border: 1px solid rgba(25, 118, 210, 0.3);
  }
  
  .mode-indicator.ai {
    background: linear-gradient(135deg, #f3e5f5 0%, #faf0fc 100%);
    color: #7b1fa2;
    border: 1px solid rgba(123, 31, 162, 0.3);
  }
  
  .clear-btn {
    background: #f5f5f5;
    border: none;
    border-radius: 50%;
    width: 30px;
    height: 30px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    font-size: 1.2rem;
    color: #666;
    transition: all 0.2s ease;
  }
  
  .clear-btn:hover {
    background: #e0e0e0;
    color: #333;
  }
  
  .search-modes {
    display: flex;
    gap: 1rem;
    margin-bottom: 1.5rem;
    justify-content: center;
  }
  
  .search-mode-btn {
    display: flex;
    align-items: center;
    gap: 1rem;
    padding: 1rem 1.5rem;
    background: white;
    border: 2px solid #e1e5e9;
    border-radius: 15px;
    cursor: pointer;
    transition: all 0.3s ease;
    min-width: 200px;
    text-align: left;
  }
  
  .traditional-mode {
    border-color: #1976d2;
    background: linear-gradient(135deg, #e3f2fd 0%, #f0f8ff 100%);
  }
  
  .traditional-mode.active {
    border-color: #1976d2;
    background: linear-gradient(135deg, #1976d2 0%, #1565c0 100%);
    color: white;
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(25, 118, 210, 0.3);
  }
  
  .ai-mode {
    border-color: #e1e5e9;
    background: white;
  }
  
  .ai-mode.active {
    border-color: #7b1fa2;
    background: linear-gradient(135deg, #7b1fa2 0%, #6a1b9a 100%);
    color: white;
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(123, 31, 162, 0.3);
  }
  
  .search-mode-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  }
  
  .mode-icon {
    font-size: 1.8rem;
    flex-shrink: 0;
  }
  
  .mode-content {
    display: flex;
    flex-direction: column;
  }
  
  .mode-title {
    font-weight: 600;
    font-size: 1rem;
    margin-bottom: 0.2rem;
  }
  
  .mode-desc {
    font-size: 0.8rem;
    opacity: 0.8;
  }
  
  .search-mode-btn.active .mode-desc {
    opacity: 0.9;
  }
  
  @media (max-width: 768px) {
    .search-modes {
      flex-direction: column;
      gap: 0.8rem;
    }
    
    .search-mode-btn {
      min-width: auto;
      padding: 0.8rem 1rem;
    }
    
    .mode-icon {
      font-size: 1.5rem;
    }
  }
  </style>