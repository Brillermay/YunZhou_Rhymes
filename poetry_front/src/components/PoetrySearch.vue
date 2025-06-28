<template>
  <div class="poetry-search-container">
    <!-- 顶部标题区域 -->
    <SearchHeader />

    <!-- 搜索区域 -->
    <SearchInput
      v-model="searchQuery"
      :is-focused="isSearchFocused"
      :suggestions="searchSuggestions"
      :show-suggestions="showSuggestions"
      :highlighted-index="highlightedIndex"
      :current-mode="currentSearchType"
      @mode-change="switchSearchMode"
      @search="handleUnifiedSearch"
      @focus="handleSearchFocus"
      @blur="handleSearchBlur"
      @input="handleSearchInput"
      @clear="clearSearch"
      @quick-search="handleQuickSearch"
      @select-suggestion="selectSuggestion"
    />

    <!-- 工具栏 -->
    <SearchToolbar
      :search-history="searchHistory"
      :favorite-poems="favoritePoems"
      :font-size="fontSize"
      :view-mode="viewMode"
      @show-history="showHistoryPanel = true"
      @show-favorites="showFavoritesPanel = true"
      @adjust-font="adjustFontSize"
      @change-view="viewMode = $event"
      @export-favorites="exportFavorites"
    />

    <!-- 侧边面板 -->
    <SearchPanels
      :show-history="showHistoryPanel"
      :show-favorites="showFavoritesPanel"
      :search-history="searchHistory"
      :favorite-poems="favoritePoems"
      @close-history="showHistoryPanel = false"
      @close-favorites="showFavoritesPanel = false"
      @search-from-history="handleHistorySearch"
      @clear-history="clearHistory"
      @view-detail="goToDetail"
      @toggle-favorite="toggleFavorite"
      @clear-all-favorites="clearAllFavorites"
    />

    <!-- 主内容区域 -->
    <main class="main-content">
      <!-- 欢迎页面 -->
      <WelcomeContent
        v-if="!hasSearched && !loading"
        :font-size="fontSize"
        :view-mode="viewMode"
        @search-by-poet="searchByPoet"
        @search-by-theme="searchByTheme"
        @view-detail="goToDetail"
        @toggle-favorite="toggleFavorite"
        :is-favorite="isFavorite"
      />

      <!-- 加载状态 -->
      <LoadingSpinner v-if="loading" />

      <!-- 搜索结果 -->
      <SearchResults
        v-if="hasSearched && !loading"
        :search-results="searchResults"
        :search-query="searchQuery"
        :sort-by="sortBy"
        :view-mode="viewMode"
        :font-size="fontSize"
        :search-type="currentSearchType"
        :is-favorite="isFavorite"
        @sort-change="sortBy = $event"
        @view-detail="goToDetail"
        @toggle-favorite="toggleFavorite"
        @clear-search="clearSearch"
        @change-view="viewMode = $event"
      />
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

// 导入子组件
import SearchHeader from './search/SearchHeader.vue'
import SearchInput from './search/SearchInput.vue'
import SearchToolbar from './search/SearchToolbar.vue'
import SearchPanels from './search/SearchPanels.vue'
import WelcomeContent from './search/WelcomeContent.vue'
import LoadingSpinner from './search/LoadingSpinner.vue'
import SearchResults from './search/SearchResults.vue'

// 导入组合式函数
import { useSearch } from './search/composables/useSearch'
import { useFavorites } from './search/composables/useFavorites'
import { useSearchHistory } from './search/composables/useSearchHistory'
import { useSearchSuggestions } from './search/composables/useSearchSuggestions'

const router = useRouter()

// 使用组合式函数
const {
  searchQuery,
  searchResults,
  hasSearched,
  loading,
  currentSearchType,
  performSearch,
  performTraditionalSearch,
  performAISearch,
  switchSearchMode,
  quickSearch,
  clearSearch,
  searchByPoet,
  searchByTheme,
  initializeSearch
} = useSearch()

// 🔧 修复：正确解构 useFavorites
const {
  favoriteIds,
  favoritePoems,
  loadFavorites,
  toggleFavorite,
  isFavorite,
  clearAllFavorites,
  exportFavorites
} = useFavorites()

const {
  searchHistory,
  saveSearchHistory,
  searchFromHistory,
  clearHistory
} = useSearchHistory()

// 传递正确的方法给建议组合式函数
const {
  searchSuggestions,
  showSuggestions,
  highlightedIndex,
  isSearchFocused,
  handleSearchInput,
  handleSearchFocus,
  handleSearchBlur,
  handleKeydown,
  selectSuggestion
} = useSearchSuggestions(
  searchQuery, 
  searchHistory, 
  currentSearchType,
  performTraditionalSearch, 
  performAISearch
)

// 本地状态
const fontSize = ref(18)
const viewMode = ref('grid')
const sortBy = ref('relevance')
const showHistoryPanel = ref(false)
const showFavoritesPanel = ref(false)

// 🔧 修复：统一搜索处理 - 根据当前模式执行搜索
const handleUnifiedSearch = () => {
  if (searchQuery.value.trim()) {
    saveSearchHistory(searchQuery.value.trim())
    performSearch() // 不传参数，使用 currentSearchType
  }
}

// 🔧 修复：快速搜索处理
const handleQuickSearch = ({ tag, mode }) => {
  console.log('⚡ 快速搜索:', tag, '模式:', mode)
  searchQuery.value = tag
  saveSearchHistory(tag)
  if (mode) {
    switchSearchMode(mode) // 先切换模式
  }
  
  // 延迟执行搜索，确保模式切换完成
  setTimeout(() => {
    if (currentSearchType.value === 'ai') {
      performAISearch()
    } else {
      performTraditionalSearch()
    }
  }, 10)
}

// 🔧 修复：历史搜索处理
const handleHistorySearch = (query) => {
  console.log('📚 历史搜索:', query)
  searchQuery.value = query
  
  // 根据当前模式搜索
  if (currentSearchType.value === 'ai') {
    performAISearch()
  } else {
    performTraditionalSearch()
  }
}

// 其他方法
const adjustFontSize = (delta) => {
  fontSize.value = Math.max(12, Math.min(28, fontSize.value + delta))
  localStorage.setItem('poetryFontSize', fontSize.value.toString())
}

const goToDetail = (poemId) => {
  console.log('查看诗词详情:', poemId)
  // router.push(`/poem/${poemId}`)
}

// 生命周期
onMounted(() => {
  // 恢复设置
  const savedFontSize = localStorage.getItem('poetryFontSize')
  if (savedFontSize) {
    fontSize.value = parseInt(savedFontSize)
  }
  
  // 初始化搜索设置
  initializeSearch()
  // 🔧 修复：页面加载时获取收藏
  loadFavorites()
  console.log('🚀 PoetrySearch 组件初始化完成，当前搜索模式:', currentSearchType.value)
})
</script>

<style scoped>
.poetry-search-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5efe6 0%, #faf8f3 100%);
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

.main-content {
  flex: 1;
  padding: 2rem;
}

@media (max-width: 768px) {
  .main-content {
    padding: 1rem;
  }
}
</style>