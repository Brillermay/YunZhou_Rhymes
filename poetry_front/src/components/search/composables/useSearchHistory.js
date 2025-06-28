import { ref } from 'vue'

export function useSearchHistory() {
  const searchHistory = ref(JSON.parse(localStorage.getItem('poetrySearchHistory') || '[]'))

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

  const searchFromHistory = (historyItem) => {
    return historyItem.query
  }

  const clearHistory = () => {
    if (confirm('确定要清空搜索历史吗？')) {
      searchHistory.value = []
      localStorage.setItem('poetrySearchHistory', '[]')
    }
  }

  return {
    searchHistory,
    saveSearchHistory,
    searchFromHistory,
    clearHistory
  }
}