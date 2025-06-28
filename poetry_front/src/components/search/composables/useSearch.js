import { ref } from 'vue'

const API_BASE_URL = 'http://localhost:8081'

export function useSearch() {
  const searchQuery = ref('')
  const searchResults = ref([])
  const hasSearched = ref(false)
  const loading = ref(false)
  const currentSearchType = ref('traditional') // 默认传统搜索

  // 🔧 修复：传统搜索API调用
  const performTraditionalSearchAPI = async (query) => {
    try {
      const response = await fetch(`${API_BASE_URL}/poem/keyword/${encodeURIComponent(query)}`)
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      const data = await response.json()
      
      // 处理不同的响应格式
      if (Array.isArray(data)) {
        return data
      } else if (data.data && Array.isArray(data.data)) {
        return data.data
      } else if (data.success && data.data) {
        return Array.isArray(data.data) ? data.data : [data.data]
      }
      
      return []
    } catch (error) {
      console.error('传统搜索API调用失败:', error)
      throw error
    }
  }

  // 🔧 修复：AI搜索API调用
  const performAISearchAPI = async (query) => {
    try {
      const response = await fetch(`${API_BASE_URL}/ai/easy/ai-search/${encodeURIComponent(query)}?maxResults=20`)
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      const data = await response.json()
      
      if (data.success && data.data) {
        return Array.isArray(data.data) ? data.data : [data.data]
      }
      
      return []
    } catch (error) {
      console.error('AI搜索API调用失败:', error)
      throw error
    }
  }

  // 🔧 统一搜索执行方法
  const performSearch = async (searchType = null) => {
    const query = searchQuery.value.trim()
    if (!query) return
  
    // 只在明确切换模式时才覆盖 currentSearchType
    if (searchType && searchType !== currentSearchType.value) {
      currentSearchType.value = searchType
      localStorage.setItem('preferredSearchMode', searchType)
    }
  
    loading.value = true
    hasSearched.value = true
  
    try {
      let results = []
      if (currentSearchType.value === 'ai') {
        results = await performAISearchAPI(query)
      } else {
        results = await performTraditionalSearchAPI(query)
      }
      searchResults.value = Array.isArray(results) ? results : []
    } catch (error) {
      searchResults.value = []
      alert(`${currentSearchType.value === 'ai' ? 'AI' : '传统'}搜索失败: ${error.message}`)
    } finally {
      loading.value = false
    }
  }

  // 🔧 新增：独立的传统搜索方法
  const performTraditionalSearch = async () => {
    await performSearch('traditional')
  }

  // 🔧 新增：独立的AI搜索方法
  const performAISearch = async () => {
    await performSearch('ai')
  }

  // 🔧 新增：模式切换方法
  const switchSearchMode = (mode) => {
    currentSearchType.value = mode
    console.log(`🔄 搜索模式切换为: ${mode === 'ai' ? 'AI智能搜索' : '传统搜索'}`)
    
    // 保存用户偏好
    localStorage.setItem('preferredSearchMode', mode)
  }

  // 🔧 新增：快速搜索方法
  const quickSearch = (tag, mode = null) => {
    searchQuery.value = tag
    const searchMode = mode || currentSearchType.value
    performSearch(searchMode)
  }

  const clearSearch = () => {
    searchQuery.value = ''
    searchResults.value = []
    hasSearched.value = false
    // 保持搜索模式不变
  }

  const searchByPoet = (poet) => {
    searchQuery.value = poet + '的诗词'
    performSearch('ai') // 按作者搜索默认使用AI
  }

  const searchByTheme = (theme) => {
    searchQuery.value = theme
    performSearch('ai') // 按主题搜索默认使用AI
  }

  // 🔧 新增：初始化方法
  const initializeSearch = () => {
    // 恢复搜索模式偏好
    const savedMode = localStorage.getItem('preferredSearchMode')
    if (savedMode && ['traditional', 'ai'].includes(savedMode)) {
      currentSearchType.value = savedMode
    }
  }

  return {
    searchQuery,
    searchResults,
    hasSearched,
    loading,
    currentSearchType,
    performSearch,
    performTraditionalSearch, // 🔧 确保导出
    performAISearch,         // 🔧 确保导出
    switchSearchMode,        // 🔧 新增导出
    quickSearch,
    clearSearch,
    searchByPoet,
    searchByTheme,
    initializeSearch         // 🔧 新增导出
  }
}