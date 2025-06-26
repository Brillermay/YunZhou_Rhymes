<template>
  <div class="poetry-search-container">
    <!-- 顶部标题区域 -->
    <header class="search-header">
      <div class="header-content">
        <h1 class="main-title">
          <span class="title-icon">📜</span>
          诗词搜索
        </h1>
        <p class="subtitle">案头轻点觅古篇，屏上翰墨入云烟</p>
      </div>
    </header>

    <!-- 搜索区域 -->
    <div class="search-section">
      <div class="search-container">
        <!-- 主搜索框 -->
        <div class="main-search-box" :class="{ 'focused': isSearchFocused }">
          <div class="search-input-wrapper">
            <span class="search-icon">🔍</span>
            <input
              ref="searchInput"
              v-model="searchQuery"
              @input="handleSearchInput"
              @focus="handleSearchFocus"
              @blur="handleSearchBlur"
              @keydown="handleKeydown"
              class="search-input"
              placeholder="请输入诗词标题、作者、内容或关键词..."
              autocomplete="off"
            />
            <button 
              v-if="searchQuery" 
              @click="clearSearch"
              class="clear-btn"
            >
              ×
            </button>
          </div>
          
          <!-- 搜索建议下拉 -->
          <div 
            v-if="showSuggestions && searchSuggestions.length" 
            class="search-suggestions"
          >
            <div
              v-for="(suggestion, index) in searchSuggestions"
              :key="index"
              @click="selectSuggestion(suggestion)"
              class="suggestion-item"
              :class="{ 'highlighted': highlightedIndex === index }"
            >
              <span class="suggestion-icon">{{ getSuggestionIcon(suggestion.type) }}</span>
              <div class="suggestion-content">
                <div class="suggestion-main">{{ suggestion.text }}</div>
                <div class="suggestion-sub">{{ suggestion.subtitle }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 快速搜索标签 -->
        <div class="quick-search-tags">
          <span class="tags-label">热门搜索：</span>
          <button
            v-for="tag in quickSearchTags"
            :key="tag"
            @click="quickSearch(tag)"
            class="quick-tag"
          >
            {{ tag }}
          </button>
        </div>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <button 
          v-if="searchHistory.length" 
          @click="showHistoryPanel = !showHistoryPanel"
          class="tool-btn"
        >
          <span class="btn-icon">🕒</span>
          搜索历史
        </button>
        <button 
          v-if="favoritePoems.length" 
          @click="showFavoritesPanel = !showFavoritesPanel"
          class="tool-btn"
        >
          <span class="btn-icon">❤️</span>
          我的收藏 ({{ favoritePoems.length }})
        </button>
      </div>
      
      <div class="toolbar-right">
        <div class="font-controls">
          <button @click="adjustFontSize(-2)" class="font-btn">A⁻</button>
          <span class="font-size-display">{{ fontSize }}px</span>
          <button @click="adjustFontSize(2)" class="font-btn">A⁺</button>
        </div>
        
        <div class="view-controls">
          <button 
            @click="viewMode = 'grid'"
            :class="{ 'active': viewMode === 'grid' }"
            class="view-btn"
          >
            ⚏
          </button>
          <button 
            @click="viewMode = 'list'"
            :class="{ 'active': viewMode === 'list' }"
            class="view-btn"
          >
            ☰
          </button>
        </div>
        
        <button 
          v-if="favoritePoems.length" 
          @click="exportFavorites"
          class="tool-btn export-btn"
        >
          <span class="btn-icon">📥</span>
          导出收藏
        </button>
      </div>
    </div>

    <!-- 搜索历史面板 -->
    <div v-if="showHistoryPanel" class="history-panel">
      <div class="panel-header">
        <h3>搜索历史</h3>
        <button @click="clearHistory" class="clear-history-btn">清空</button>
      </div>
      <div class="history-items">
        <button
          v-for="(item, index) in searchHistory"
          :key="index"
          @click="searchFromHistory(item)"
          class="history-item"
        >
          <span class="history-text">{{ item.query }}</span>
          <span class="history-time">{{ formatTime(item.timestamp) }}</span>
        </button>
      </div>
    </div>

    <!-- 收藏面板 -->
    <div v-if="showFavoritesPanel" class="favorites-panel">
      <div class="panel-header">
        <h3>我的收藏</h3>
        <button @click="clearAllFavorites" class="clear-favorites-btn">全部取消</button>
      </div>
      <div class="favorites-grid">
        <div
          v-for="poem in favoritePoems"
          :key="poem.PID"
          @click="goToDetail(poem.PID)"
          class="favorite-card"
        >
          <button 
            @click.stop="toggleFavorite(poem.PID)"
            class="remove-favorite-btn"
          >
            ×
          </button>
          <h4>{{ poem.title }}</h4>
          <p class="author">{{ poem.poet }}</p>
          <p class="preview">{{ getPreview(poem.text) }}</p>
        </div>
      </div>
    </div>

    <!-- 主内容区域 -->
    <main class="main-content">
      <!-- 欢迎页面 -->
      <div v-if="!hasSearched && !loading" class="welcome-section">
        <div class="welcome-header">
          <h2>🌸 每日精选 🌸</h2>
          <p>发现古代诗词的韵律之美</p>
        </div>
        
        <div class="featured-categories">
          <button
            v-for="category in categories"
            :key="category.name"
            @click="searchByCategory(category.name)"
            class="category-card"
          >
            <span class="category-icon">{{ category.icon }}</span>
            <span class="category-name">{{ category.name }}</span>
            <span class="category-count">{{ category.count }}首</span>
          </button>
        </div>

        <div :class="['poems-display', viewMode]">
          <div
            v-for="poem in featuredPoems"
            :key="poem.PID"
            @click="goToDetail(poem.PID)"
            class="poem-card"
          >
            <button 
              @click.stop="toggleFavorite(poem.PID)"
              class="favorite-btn"
              :class="{ 'favorited': isFavorite(poem.PID) }"
            >
              {{ isFavorite(poem.PID) ? '❤️' : '🤍' }}
            </button>
            
            <div class="poem-header">
              <h3 class="poem-title">{{ poem.title }}</h3>
              <div class="poem-meta">
                <span class="poet">{{ poem.poet }}</span>
                <span class="separator">•</span>
                <span class="category">{{ poem.category }}</span>
              </div>
            </div>
            
            <div class="poem-content" :style="{ fontSize: fontSize + 'px' }">
              {{ formatPoemText(poem.text) }}
            </div>
            
            <div v-if="poem.appreciation" class="poem-analysis">
              <span class="analysis-icon">💡</span>
              {{ poem.appreciation }}
            </div>
          </div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-section">
        <div class="loading-spinner"></div>
        <p>正在搜索诗词...</p>
      </div>

      <!-- 搜索结果 -->
      <div v-if="hasSearched && !loading" class="results-section">
        <!-- 结果统计 -->
        <div class="results-header">
          <div class="results-info">
            <span v-if="searchResults.length">
              找到 <strong>{{ searchResults.length }}</strong> 首相关诗词
            </span>
            <span v-else>
              未找到相关诗词，请尝试其他关键词
            </span>
          </div>
          
          <div class="sort-controls" v-if="searchResults.length">
            <select v-model="sortBy" @change="sortResults" class="sort-select">
              <option value="relevance">按相关度</option>
              <option value="title">按标题</option>
              <option value="poet">按作者</option>
              <option value="category">按类别</option>
            </select>
          </div>
        </div>

        <!-- 搜索结果列表 -->
        <div v-if="searchResults.length" :class="['poems-display', viewMode]">
          <div
            v-for="poem in sortedResults"
            :key="poem.PID"
            @click="goToDetail(poem.PID)"
            class="poem-card search-result"
          >
            <button 
              @click.stop="toggleFavorite(poem.PID)"
              class="favorite-btn"
              :class="{ 'favorited': isFavorite(poem.PID) }"
            >
              {{ isFavorite(poem.PID) ? '❤️' : '🤍' }}
            </button>
            
            <div class="poem-header">
              <h3 class="poem-title" v-html="highlightText(poem.title, searchQuery)"></h3>
              <div class="poem-meta">
                <span class="poet" v-html="highlightText(poem.poet, searchQuery)"></span>
                <span class="separator">•</span>
                <span class="category">{{ poem.category }}</span>
              </div>
            </div>
            
            <div class="poem-content" :style="{ fontSize: fontSize + 'px' }">
              <div v-html="highlightText(formatPoemText(poem.text), searchQuery)"></div>
            </div>
            
            <div v-if="poem.background" class="poem-analysis">
              <span class="analysis-icon">📝</span>
              {{ poem.background }}
            </div>
          </div>
        </div>

        <!-- 无结果提示 -->
        <div v-else class="no-results">
          <div class="no-results-icon">🤔</div>
          <h3>未找到相关诗词</h3>
          <p>尝试使用不同的关键词，或者浏览我们的精选内容</p>
          <button @click="clearSearch" class="try-again-btn">重新搜索</button>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'

// 路由
const router = useRouter()

// 响应式数据
const searchQuery = ref('')
const searchResults = ref([])
const featuredPoems = ref([])
const hasSearched = ref(false)
const loading = ref(false)
const isSearchFocused = ref(false)

// 搜索建议相关
const showSuggestions = ref(false)
const searchSuggestions = ref([])
const highlightedIndex = ref(-1)

// 界面状态
const fontSize = ref(18)
const viewMode = ref('grid') // 'grid' 或 'list'
const sortBy = ref('relevance')
const showHistoryPanel = ref(false)
const showFavoritesPanel = ref(false)

// 数据存储
const favoriteIds = ref(JSON.parse(localStorage.getItem('poetryFavorites') || '[]'))
const searchHistory = ref(JSON.parse(localStorage.getItem('poetrySearchHistory') || '[]'))

// 配置
const API_BASE_URL = 'http://localhost:8081/poem'

// 快速搜索标签
const quickSearchTags = ['李白', '杜甫', '苏轼', '唐诗', '宋词', '元曲', '春天', '秋天', '月亮', '思乡']

// 分类数据
const categories = ref([
  { name: '唐诗', icon: '🏛️', count: 2000 },
  { name: '宋词', icon: '🎭', count: 1500 },
  { name: '元曲', icon: '🎪', count: 800 },
  { name: '古诗', icon: '📜', count: 3000 },
  { name: '现代诗', icon: '✨', count: 500 },
  { name: '民歌', icon: '🎵', count: 300 }
])

// 计算属性
const favoritePoems = computed(() => {
  return [...searchResults.value, ...featuredPoems.value]
    .filter(poem => favoriteIds.value.includes(poem.PID))
})

const sortedResults = computed(() => {
  const results = [...searchResults.value]
  
  switch (sortBy.value) {
    case 'title':
      return results.sort((a, b) => a.title.localeCompare(b.title))
    case 'poet':
      return results.sort((a, b) => a.poet.localeCompare(b.poet))
    case 'category':
      return results.sort((a, b) => a.category.localeCompare(b.category))
    default:
      return results // 按相关度（默认API返回顺序）
  }
})

// 方法
const handleSearchInput = async () => {
  if (searchQuery.value.trim()) {
    await generateSuggestions()
    showSuggestions.value = true
  } else {
    showSuggestions.value = false
    searchSuggestions.value = []
  }
}

const handleSearchFocus = () => {
  isSearchFocused.value = true
  if (searchQuery.value.trim()) {
    showSuggestions.value = true
  }
}

const handleSearchBlur = () => {
  isSearchFocused.value = false
  // 延迟隐藏建议，允许点击建议项
  setTimeout(() => {
    showSuggestions.value = false
  }, 200)
}

const handleKeydown = (event) => {
  if (!showSuggestions.value) {
    if (event.key === 'Enter') {
      performSearch()
    }
    return
  }

  switch (event.key) {
    case 'ArrowDown':
      event.preventDefault()
      highlightedIndex.value = Math.min(
        highlightedIndex.value + 1,
        searchSuggestions.value.length - 1
      )
      break
    case 'ArrowUp':
      event.preventDefault()
      highlightedIndex.value = Math.max(highlightedIndex.value - 1, -1)
      break
    case 'Enter':
      event.preventDefault()
      if (highlightedIndex.value >= 0) {
        selectSuggestion(searchSuggestions.value[highlightedIndex.value])
      } else {
        performSearch()
      }
      break
    case 'Escape':
      showSuggestions.value = false
      highlightedIndex.value = -1
      break
  }
}

const generateSuggestions = async () => {
  const query = searchQuery.value.trim()
  if (!query) return

  // 模拟搜索建议生成（实际项目中应该调用API）
  const suggestions = []
  
  // 添加历史搜索建议
  const historyMatches = searchHistory.value
    .filter(item => item.query.includes(query))
    .slice(0, 3)
    .map(item => ({
      type: 'history',
      text: item.query,
      subtitle: '搜索历史'
    }))
  
  suggestions.push(...historyMatches)
  
  // 添加作者建议
  const poets = ['李白', '杜甫', '苏轼', '李清照', '辛弃疾', '王维', '白居易']
  const poetMatches = poets
    .filter(poet => poet.includes(query))
    .map(poet => ({
      type: 'poet',
      text: poet,
      subtitle: '诗人'
    }))
  
  suggestions.push(...poetMatches)
  
  // 添加分类建议
  const categoryMatches = categories.value
    .filter(cat => cat.name.includes(query))
    .map(cat => ({
      type: 'category',
      text: cat.name,
      subtitle: `${cat.count}首诗词`
    }))
  
  suggestions.push(...categoryMatches)
  
  searchSuggestions.value = suggestions.slice(0, 8)
  highlightedIndex.value = -1
}

const selectSuggestion = (suggestion) => {
  searchQuery.value = suggestion.text
  showSuggestions.value = false
  performSearch()
}

const getSuggestionIcon = (type) => {
  const icons = {
    history: '🕒',
    poet: '👤',
    category: '📚',
    keyword: '🔍'
  }
  return icons[type] || '🔍'
}

const clearSearch = () => {
  searchQuery.value = ''
  searchResults.value = []
  hasSearched.value = false
  showSuggestions.value = false
}

const quickSearch = (tag) => {
  searchQuery.value = tag
  performSearch()
}

const performSearch = async () => {
  const query = searchQuery.value.trim()
  if (!query) return

  loading.value = true
  hasSearched.value = true
  showSuggestions.value = false

  try {
    const response = await fetch(`${API_BASE_URL}/keyword/${encodeURIComponent(query)}`)
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const data = await response.json()
    searchResults.value = Array.isArray(data) ? data : []
    
    // 保存搜索历史
    saveSearchHistory(query)
    
  } catch (error) {
    console.error('搜索失败:', error)
    searchResults.value = []
    // 可以显示错误提示
  } finally {
    loading.value = false
  }
}

const searchFromHistory = (historyItem) => {
  searchQuery.value = historyItem.query
  showHistoryPanel.value = false
  performSearch()
}

const searchByCategory = (category) => {
  searchQuery.value = category
  performSearch()
}

const saveSearchHistory = (query) => {
  const newItem = {
    query,
    timestamp: Date.now()
  }
  
  // 移除重复项
  const filtered = searchHistory.value.filter(item => item.query !== query)
  
  // 添加到开头，保持最多10条
  searchHistory.value = [newItem, ...filtered].slice(0, 10)
  
  localStorage.setItem('poetrySearchHistory', JSON.stringify(searchHistory.value))
}

const clearHistory = () => {
  if (confirm('确定要清空搜索历史吗？')) {
    searchHistory.value = []
    localStorage.setItem('poetrySearchHistory', '[]')
    showHistoryPanel.value = false
  }
}

const adjustFontSize = (delta) => {
  fontSize.value = Math.max(12, Math.min(28, fontSize.value + delta))
  localStorage.setItem('poetryFontSize', fontSize.value.toString())
}

const toggleFavorite = (poemId) => {
  const index = favoriteIds.value.indexOf(poemId)
  if (index > -1) {
    favoriteIds.value.splice(index, 1)
  } else {
    favoriteIds.value.push(poemId)
  }
  localStorage.setItem('poetryFavorites', JSON.stringify(favoriteIds.value))
}

const isFavorite = (poemId) => {
  return favoriteIds.value.includes(poemId)
}

const clearAllFavorites = () => {
  if (confirm('确定要取消所有收藏吗？')) {
    favoriteIds.value = []
    localStorage.setItem('poetryFavorites', '[]')
    showFavoritesPanel.value = false
  }
}

const exportFavorites = () => {
  const favPoems = favoritePoems.value
  const content = favPoems.map(poem => 
    `## ${poem.title} — ${poem.poet}\n\n${formatPoemText(poem.text)}\n`
  ).join('\n---\n\n')
  
  const blob = new Blob([content], { type: 'text/markdown' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `我的诗词收藏_${new Date().toLocaleDateString()}.md`
  a.click()
  URL.revokeObjectURL(url)
}

const sortResults = () => {
  // sortedResults 计算属性会自动更新
}

const goToDetail = (poemId) => {
  // 跳转到诗词详情页
  console.log('查看诗词详情:', poemId)
  // router.push(`/poem/${poemId}`)
}

const formatPoemText = (text) => {
  if (!text) return ''
  return text.replace(/[。！？；]/g, '$&\n').trim()
}

const getPreview = (text) => {
  if (!text) return ''
  return text.length > 30 ? text.substring(0, 30) + '...' : text
}

const highlightText = (text, keyword) => {
  if (!keyword || !text) return text
  const regex = new RegExp(`(${keyword})`, 'gi')
  return text.replace(regex, '<mark class="highlight">$1</mark>')
}

const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  const now = new Date()
  const diff = now - date
  
  if (diff < 3600000) { // 1小时内
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) { // 1天内
    return `${Math.floor(diff / 3600000)}小时前`
  } else {
    return date.toLocaleDateString()
  }
}

const loadFeaturedPoems = async () => {
  try {
    // 加载精选诗词
    const response = await fetch(`${API_BASE_URL}/1`)
    if (response.ok) {
      const poem = await response.json()
      featuredPoems.value = [poem]
    }
  } catch (error) {
    console.error('加载精选诗词失败:', error)
    featuredPoems.value = []
  }
}

// 生命周期
onMounted(() => {
  loadFeaturedPoems()
  
  // 恢复字体大小设置
  const savedFontSize = localStorage.getItem('poetryFontSize')
  if (savedFontSize) {
    fontSize.value = parseInt(savedFontSize)
  }
})
</script>

<style scoped>
/* 基本布局 */
.poetry-search-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5efe6 0%, #faf8f3 100%);
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 头部区域 */
.search-header {
  background: linear-gradient(135deg, #8c7853 0%, #6e5773 100%);
  color: white;
  padding: 2rem 1rem;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.header-content {
  max-width: 800px;
  margin: 0 auto;
}

.main-title {
  margin: 0;
  font-size: 2.5rem;
  font-weight: 300;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.title-icon {
  font-size: 2rem;
}

.subtitle {
  margin: 1rem 0 0;
  font-size: 1rem;
  opacity: 0.9;
  font-style: italic;
}

/* 搜索区域 */
.search-section {
  padding: 2rem 1rem;
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
}

.search-container {
  max-width: 800px;
  margin: 0 auto;
}

.main-search-box {
  position: relative;
  margin-bottom: 1.5rem;
}

.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 1.2rem;
  font-size: 1.3rem;
  color: #8c7853;
  z-index: 2;
}

.search-input {
  width: 100%;
  padding: 1rem 1rem 1rem 3rem;
  font-size: 1.1rem;
  border: 2px solid #e1d8c9;
  border-radius: 50px;
  background: #fafaf8;
  outline: none;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.main-search-box.focused .search-input {
  border-color: #8c7853;
  box-shadow: 0 4px 20px rgba(140, 120, 83, 0.15);
  background: white;
}

.clear-btn {
  position: absolute;
  right: 1rem;
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #999;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 50%;
  transition: all 0.2s;
}

.clear-btn:hover {
  background: #f0f0f0;
  color: #666;
}

/* 搜索建议 */
.search-suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #e1d8c9;
  border-top: none;
  border-radius: 0 0 20px 20px;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  z-index: 1000;
  max-height: 400px;
  overflow-y: auto;
}

.suggestion-item {
  display: flex;
  align-items: center;
  padding: 0.8rem 1.2rem;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #f5f5f5;
}

.suggestion-item:hover,
.suggestion-item.highlighted {
  background: #f8f4ed;
}

.suggestion-icon {
  margin-right: 0.8rem;
  font-size: 1.1rem;
}

.suggestion-content {
  flex: 1;
}

.suggestion-main {
  font-weight: 500;
  color: #333;
}

.suggestion-sub {
  font-size: 0.85rem;
  color: #666;
  margin-top: 0.2rem;
}

/* 快速搜索标签 */
.quick-search-tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.tags-label {
  color: #666;
  font-size: 0.9rem;
  margin-right: 0.5rem;
}

.quick-tag {
  padding: 0.4rem 0.8rem;
  background: #f8f4ed;
  color: #8c7853;
  border: 1px solid #e1d8c9;
  border-radius: 20px;
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.2s;
}

.quick-tag:hover {
  background: #8c7853;
  color: white;
  transform: translateY(-1px);
}

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background: white;
  border-top: 1px solid #f0f0f0;
  flex-wrap: wrap;
  gap: 1rem;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.tool-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.6rem 1rem;
  background: #f8f4ed;
  color: #8c7853;
  border: 1px solid #e1d8c9;
  border-radius: 20px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s;
}

.tool-btn:hover {
  background: #8c7853;
  color: white;
  transform: translateY(-1px);
}

.export-btn {
  background: linear-gradient(135deg, #8c7853, #6e5773);
  color: white;
  border: none;
}

.font-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: #f8f4ed;
  padding: 0.3rem;
  border-radius: 20px;
  border: 1px solid #e1d8c9;
}

.font-btn {
  padding: 0.4rem 0.6rem;
  background: none;
  border: none;
  border-radius: 15px;
  cursor: pointer;
  color: #8c7853;
  transition: background 0.2s;
}

.font-btn:hover {
  background: #e1d8c9;
}

.font-size-display {
  font-size: 0.8rem;
  color: #666;
  padding: 0 0.3rem;
}

.view-controls {
  display: flex;
  background: #f8f4ed;
  border-radius: 20px;
  border: 1px solid #e1d8c9;
  overflow: hidden;
}

.view-btn {
  padding: 0.6rem;
  background: none;
  border: none;
  cursor: pointer;
  color: #8c7853;
  transition: all 0.2s;
}

.view-btn.active,
.view-btn:hover {
  background: #8c7853;
  color: white;
}

/* 面板样式 */
.history-panel,
.favorites-panel {
  background: white;
  margin: 0 2rem;
  border-radius: 15px;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  margin-bottom: 1rem;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  border-bottom: 1px solid #f0f0f0;
}

.panel-header h3 {
  margin: 0;
  color: #8c7853;
}

.clear-history-btn,
.clear-favorites-btn {
  padding: 0.4rem 0.8rem;
  background: #ff6b6b;
  color: white;
  border: none;
  border-radius: 15px;
  cursor: pointer;
  font-size: 0.8rem;
  transition: all 0.2s;
}

.clear-history-btn:hover,
.clear-favorites-btn:hover {
  background: #ff5252;
  transform: translateY(-1px);
}

.history-items {
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.8rem 1rem;
  background: #f8f4ed;
  border: none;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  text-align: left;
}

.history-item:hover {
  background: #e1d8c9;
  transform: translateX(5px);
}

.history-text {
  color: #333;
  font-weight: 500;
}

.history-time {
  color: #666;
  font-size: 0.8rem;
}

.favorites-grid {
  padding: 1rem;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 1rem;
}

.favorite-card {
  position: relative;
  background: #f8f4ed;
  padding: 1rem;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
}

.favorite-card:hover {
  background: #e1d8c9;
  transform: translateY(-2px);
}

.remove-favorite-btn {
  position: absolute;
  top: 0.5rem;
  right: 0.5rem;
  background: #ff6b6b;
  color: white;
  border: none;
  border-radius: 50%;
  width: 24px;
  height: 24px;
  cursor: pointer;
  font-size: 0.8rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.favorite-card h4 {
  margin: 0 0 0.5rem 0;
  color: #8c7853;
  font-size: 1rem;
}

.favorite-card .author {
  color: #666;
  font-size: 0.85rem;
  margin: 0 0 0.5rem 0;
}

.favorite-card .preview {
  color: #555;
  font-size: 0.8rem;
  line-height: 1.4;
  margin: 0;
}

/* 主内容区 */
.main-content {
  flex: 1;
  padding: 2rem;
}

/* 欢迎区域 */
.welcome-section {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-header {
  text-align: center;
  margin-bottom: 2rem;
}

.welcome-header h2 {
  color: #8c7853;
  font-size: 2rem;
  margin: 0 0 0.5rem 0;
  font-weight: 300;
}

.welcome-header p {
  color: #666;
  font-size: 1.1rem;
  margin: 0;
}

.featured-categories {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
  margin-bottom: 3rem;
}

.category-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 1.5rem 1rem;
  background: white;
  border: 2px solid #f0f0f0;
  border-radius: 15px;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
}

.category-card:hover {
  border-color: #8c7853;
  transform: translateY(-5px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.category-icon {
  font-size: 2rem;
  margin-bottom: 0.8rem;
}

.category-name {
  font-weight: 500;
  color: #333;
  margin-bottom: 0.3rem;
}

.category-count {
  font-size: 0.8rem;
  color: #666;
}

/* 诗词展示 */
.poems-display {
  display: grid;
  gap: 1.5rem;
}

.poems-display.grid {
  grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
}

.poems-display.list {
  grid-template-columns: 1fr;
}

.poem-card {
  position: relative;
  background: white;
  border-radius: 20px;
  padding: 1.5rem;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.poem-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  border-color: #8c7853;
}

.poem-card.search-result {
  border-left: 4px solid #8c7853;
}

.favorite-btn {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: rgba(255, 255, 255, 0.9);
  border: none;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  cursor: pointer;
  font-size: 1.2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  backdrop-filter: blur(10px);
}

.favorite-btn:hover {
  background: white;
  transform: scale(1.1);
}

.favorite-btn.favorited {
  background: #ffebee;
}

.poem-header {
  margin-bottom: 1rem;
}

.poem-title {
  color: #8c7853;
  font-size: 1.3rem;
  margin: 0 0 0.5rem 0;
  font-weight: 500;
}

.poem-meta {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #666;
  font-size: 0.9rem;
}

.poet {
  font-weight: 500;
}

.separator {
  opacity: 0.5;
}

.category {
  background: #f8f4ed;
  padding: 0.2rem 0.6rem;
  border-radius: 12px;
  font-size: 0.8rem;
  color: #8c7853;
}

.poem-content {
  color: #444;
  line-height: 1.8;
  white-space: pre-line;
  margin-bottom: 1rem;
  font-family: 'KaiTi', 'STKaiti', serif;
}

.poem-analysis {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
  background: #f8f4ed;
  padding: 0.8rem;
  border-radius: 12px;
  font-size: 0.85rem;
  color: #5a4634;
  line-height: 1.5;
}

.analysis-icon {
  margin-top: 0.1rem;
}

/* 加载状态 */
.loading-section {
  text-align: center;
  padding: 3rem;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f3f3f3;
  border-top: 3px solid #8c7853;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

/* 搜索结果 */
.results-section {
  max-width: 1200px;
  margin: 0 auto;
}

.results-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  padding: 1rem 0;
  border-bottom: 2px solid #f0f0f0;
}

.results-info {
  color: #666;
  font-size: 1rem;
}

.results-info strong {
  color: #8c7853;
  font-weight: 600;
}

.sort-controls {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.sort-select {
  padding: 0.5rem 1rem;
  border: 1px solid #e1d8c9;
  border-radius: 20px;
  background: white;
  color: #8c7853;
  cursor: pointer;
  outline: none;
}

/* 无结果页 */
.no-results {
  text-align: center;
  padding: 3rem;
}

.no-results-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.no-results h3 {
  color: #8c7853;
  margin-bottom: 1rem;
}

.no-results p {
  color: #666;
  margin-bottom: 2rem;
  font-size: 1.1rem;
}

.try-again-btn {
  padding: 0.8rem 2rem;
  background: linear-gradient(135deg, #8c7853, #6e5773);
  color: white;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  font-size: 1rem;
  transition: all 0.2s;
}

.try-again-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

/* 高亮样式 */
:deep(.highlight) {
  background: #fff3cd;
  color: #856404;
  padding: 0.1rem 0.2rem;
  border-radius: 3px;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-title {
    font-size: 2rem;
  }
  
  .search-section {
    padding: 1rem;
  }
  
  .toolbar {
    padding: 1rem;
    flex-direction: column;
    align-items: stretch;
  }
  
  .toolbar-left,
  .toolbar-right {
    justify-content: center;
  }
  
  .main-content {
    padding: 1rem;
  }
  
  .featured-categories {
    grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  }
  
  .poems-display.grid {
    grid-template-columns: 1fr;
  }
  
  .results-header {
    flex-direction: column;
    align-items: stretch;
    gap: 1rem;
  }
  
  .history-panel,
  .favorites-panel {
    margin: 0 1rem;
  }
}

@media (max-width: 480px) {
  .search-input {
    font-size: 1rem;
    padding: 0.8rem 0.8rem 0.8rem 2.5rem;
  }
  
  .quick-search-tags {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .poem-card {
    padding: 1rem;
  }
  
  .favorite-btn {
    width: 35px;
    height: 35px;
    font-size: 1rem;
  }
}
</style>