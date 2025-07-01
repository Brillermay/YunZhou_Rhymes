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
      :is-authenticated="userStore.isAuthenticated"
      :username="userStore.user?.username"
      @show-history="showHistoryPanel = true"
      @show-favorites="showFavoritesPanel = true"
      @adjust-font="adjustFontSize"
      @change-view="viewMode = $event"
      @export-favorites="exportFavorites"
      @show-login="handleShowLogin"
    />

    <!-- 侧边面板 -->
    <SearchPanels
      :show-history="showHistoryPanel"
      :show-favorites="showFavoritesPanel"
      :search-history="searchHistory"
      :favorite-poems="favoritePoems"
      :is-authenticated="userStore.isAuthenticated"
      @close-history="showHistoryPanel = false"
      @close-favorites="showFavoritesPanel = false"
      @search-from-history="handleHistorySearch"
      @clear-history="clearHistory"
      @view-detail="goToDetail"
      @toggle-favorite="handleToggleFavorite"
      @clear-all-favorites="clearAllFavorites"
      @show-login="handleShowLogin"
    />

    <!-- 主内容区域 -->
    <main class="main-content">
      <!-- 欢迎页面 -->
      <WelcomeContent
        v-if="!hasSearched && !loading"
        :font-size="fontSize"
        :view-mode="viewMode"
        :is-authenticated="userStore.isAuthenticated"
        @search-by-poet="searchByPoet"
        @search-by-theme="searchByTheme"
        @view-detail="goToDetail"
        @toggle-favorite="handleToggleFavorite"
        @show-login="handleShowLogin"
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
        :is-authenticated="userStore.isAuthenticated"
        :is-favorite="isFavorite"
        @sort-change="sortBy = $event"
        @view-detail="goToDetail"
        @toggle-favorite="handleToggleFavorite"
        @clear-search="clearSearch"
        @change-view="viewMode = $event"
        @show-login="handleShowLogin"
      />
    </main>

    <!-- 登录提示弹窗 -->
    <div v-if="showLoginPrompt" class="login-prompt-overlay" @click="closeLoginPrompt">
      <div class="login-prompt-modal" @click.stop>
        <div class="prompt-header">
          <h3>🔒 需要登录</h3>
          <button class="close-btn" @click="closeLoginPrompt">×</button>
        </div>
        <div class="prompt-body">
          <p>请先登录以使用收藏功能</p>
          <div class="prompt-actions">
            <button class="btn-login" @click="navigateToLogin">
              立即登录
            </button>
            <button class="btn-cancel" @click="closeLoginPrompt">
              稍后再说
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user' // 导入用户状态管理

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
const userStore = useUserStore() // 使用用户状态管理

// 🔍 添加调试用的计算属性
const debugUserInfo = computed(() => ({
  'userStore对象': userStore,
  'isLoggedIn (state)': userStore.isLoggedIn,
  'isAuthenticated (getter)': userStore.isAuthenticated,
  'uid': userStore.uid,
  'username': userStore.username,
  'userInfo': userStore.userInfo,
  'displayName': userStore.displayName
}))

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

// 传递用户状态给收藏功能
const {
  favoriteIds,
  favoritePoems,
  loadFavorites,
  toggleFavorite,
  isFavorite,
  clearAllFavorites,
  exportFavorites
} = useFavorites(userStore) // 传递 userStore

const {
  searchHistory,
  saveSearchHistory,
  searchFromHistory,
  clearHistory
} = useSearchHistory()

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
const showLoginPrompt = ref(false) // 登录提示弹窗状态

// 统一搜索处理
const handleUnifiedSearch = () => {
  if (searchQuery.value.trim()) {
    saveSearchHistory(searchQuery.value.trim())
    performSearch()
  }
}

// 快速搜索处理
const handleQuickSearch = ({ tag, mode }) => {
  console.log('⚡ 快速搜索:', tag, '模式:', mode)
  searchQuery.value = tag
  saveSearchHistory(tag)
  if (mode) {
    switchSearchMode(mode)
  }
  
  setTimeout(() => {
    if (currentSearchType.value === 'ai') {
      performAISearch()
    } else {
      performTraditionalSearch()
    }
  }, 10)
}

// 历史搜索处理
const handleHistorySearch = (query) => {
  console.log('📚 历史搜索:', query)
  searchQuery.value = query
  
  if (currentSearchType.value === 'ai') {
    performAISearch()
  } else {
    performTraditionalSearch()
  }
}

// 🔥 修正的处理收藏操作
const handleToggleFavorite = async (poemData) => {
  console.log('🔥 收藏操作开始')
  console.log('📊 原始传入参数:', poemData)
  console.log('📊 参数类型:', typeof poemData)
  
  // 🔧 处理不同类型的输入参数
  let poem = null
  
  if (typeof poemData === 'number') {
    // 🔧 修复：使用正确的字段查找诗词
    console.log('🔄 传入的是诗词ID，需要查找完整诗词信息')
    
    // 从搜索结果中查找对应的诗词 - 修复字段匹配
    poem = searchResults.value?.find(p => 
      (p.PID && p.PID === poemData) || 
      (p.pid && p.pid === poemData) || 
      (p.id && p.id === poemData)
    )
    
    console.log('🔍 搜索结果数量:', searchResults.value?.length)
    console.log('🔍 搜索结果示例:', searchResults.value?.[0])
    console.log('🔍 查找目标ID:', poemData)
    
    if (!poem) {
      console.error('❌ 无法找到对应的诗词信息')
      console.log('📋 可用的诗词IDs:', searchResults.value?.map(p => ({ 
        PID: p.PID, 
        pid: p.pid, 
        id: p.id, 
        title: p.title || p.Title 
      })))
      alert('无法获取诗词信息，请重试')
      return
    }
    
    console.log('✅ 找到诗词:', poem.title || poem.Title)
  } else if (typeof poemData === 'object' && poemData !== null) {
    // 如果传入的是对象
    poem = poemData
  } else {
    console.error('❌ 无效的诗词参数:', poemData)
    alert('诗词数据格式错误')
    return
  }
  
  console.log('🔍 处理后的诗词对象:', poem)
  console.log('📊 用户状态详情:', debugUserInfo.value)
  
  // 检查用户登录状态
  const isAuth = userStore.isAuthenticated
  const uid = userStore.uid
  
  console.log('🔍 收藏操作检查:')
  console.log('  - 用户认证状态:', isAuth)
  console.log('  - 用户UID:', uid)
  console.log('  - 诗词PID:', poem?.PID || poem?.pid || poem?.id)
  console.log('  - 诗词标题:', poem?.title || poem?.Title)
  
  if (!isAuth) {
    console.log('⚠️ 用户未登录，显示登录提示')
    showLoginPrompt.value = true
    return
  }
  
  if (!uid) {
    console.log('⚠️ 用户UID缺失，显示登录提示')
    showLoginPrompt.value = true
    return
  }
  
  // 🔧 修复：标准化诗词数据格式，确保字段统一
  const normalizedPoem = {
    pid: poem.PID || poem.pid || poem.id,
    PID: poem.PID || poem.pid || poem.id,
    title: poem.title || poem.Title,
    poet: poem.poet || poem.Poet,
    text: poem.text || poem.Text,
    dynasty: poem.dynasty || poem.Dynasty,
    category: poem.category || poem.Category
  }
  
  if (!normalizedPoem.pid) {
    console.error('❌ 诗词数据无效 - 缺少ID')
    alert('诗词数据错误，无法收藏')
    return
  }
  
  console.log('✅ 验证通过，执行收藏操作')
  console.log('📋 标准化后的诗词数据:', normalizedPoem)
  
  try {
    const result = await toggleFavorite(normalizedPoem)
    console.log('📊 收藏操作结果:', result)
    
    if (result.success) {
      console.log('✅ 收藏操作成功:', result.message)
      // 显示成功提示
    } else {
      console.error('❌ 收藏操作失败:', result.message)
      alert(`操作失败: ${result.message}`)
    }
  } catch (error) {
    console.error('💥 收藏操作异常:', error)
    alert('收藏操作失败，请检查网络连接后重试')
  }
}

// 🔥 显示登录提示
const handleShowLogin = () => {
  showLoginPrompt.value = true
}

// 🔥 关闭登录提示
const closeLoginPrompt = () => {
  showLoginPrompt.value = false
}

// 🔥 跳转到登录页面
const navigateToLogin = () => {
  showLoginPrompt.value = false
  // 这里可以根据您的路由配置跳转到登录页面
  router.push('/user') // 或者您的用户页面路径
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

// 在 script setup 中修改生命周期部分
onMounted(async () => {
  console.log('🚀 PoetrySearch 组件开始初始化')
  
  // 恢复设置
  const savedFontSize = localStorage.getItem('poetryFontSize')
  if (savedFontSize) {
    fontSize.value = parseInt(savedFontSize)
  }
  
  // 确保用户状态初始化
  if (userStore && typeof userStore.initFromStorage === 'function') {
    userStore.initFromStorage()
  }
  
  // 初始化搜索设置
  initializeSearch()
  
  // 延迟加载收藏，确保用户状态完全初始化
  setTimeout(async () => {
    console.log('⏰ 延迟加载收藏')
    console.log('📊 当前用户状态:', debugUserInfo.value)
    
    try {
      await loadFavorites()
      console.log('✅ 收藏加载完成')
    } catch (error) {
      console.error('💥 收藏加载失败:', error)
    }
  }, 300)
  
  console.log('✅ PoetrySearch 组件初始化完成')
  console.log('👤 用户登录状态:', userStore.isAuthenticated)
  console.log('🆔 用户UID:', userStore.uid)
  console.log('🔍 当前搜索模式:', currentSearchType.value)
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

/* 登录提示弹窗样式 */
.login-prompt-overlay {
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

.login-prompt-modal {
  background: white;
  border-radius: 16px;
  padding: 0;
  max-width: 400px;
  width: 90%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  animation: modalAppear 0.3s ease-out;
}

.prompt-header {
  background: linear-gradient(135deg, #8c7853, #6e5773);
  color: white;
  padding: 1.5rem;
  border-radius: 16px 16px 0 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.prompt-header h3 {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 500;
}

.close-btn {
  background: none;
  border: none;
  color: white;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: background 0.3s ease;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.prompt-body {
  padding: 2rem;
  text-align: center;
}

.prompt-body p {
  margin: 0 0 2rem 0;
  color: #666;
  font-size: 1rem;
  line-height: 1.5;
}

.prompt-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.btn-login,
.btn-cancel {
  padding: 0.8rem 2rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: 500;
  transition: all 0.3s ease;
  min-width: 100px;
}

.btn-login {
  background: linear-gradient(135deg, #8c7853, #6e5773);
  color: white;
}

.btn-login:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(140, 120, 83, 0.4);
}

.btn-cancel {
  background: #f5f5f5;
  color: #666;
  border: 1px solid #ddd;
}

.btn-cancel:hover {
  background: #e8e8e8;
}

@keyframes modalAppear {
  from {
    opacity: 0;
    transform: translateY(-20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@media (max-width: 768px) {
  .main-content {
    padding: 1rem;
  }
  
  .login-prompt-modal {
    width: 95%;
  }
  
  .prompt-actions {
    flex-direction: column;
  }
  
  .btn-login,
  .btn-cancel {
    width: 100%;
  }
}
</style>