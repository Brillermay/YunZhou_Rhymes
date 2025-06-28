import { ref, watch } from 'vue'

export function useSearchSuggestions(searchQuery, searchHistory, currentSearchType, performTraditionalSearch, performAISearch) {
  const searchSuggestions = ref([])
  const showSuggestions = ref(false)
  const highlightedIndex = ref(-1)
  const isSearchFocused = ref(false)

  const generateSuggestions = async (query) => {
    if (!query.trim()) {
      searchSuggestions.value = []
      return
    }

    const suggestions = []
    
    // 添加历史搜索建议
    const historyMatches = searchHistory.value
      .filter(item => item.query.includes(query))
      .slice(0, 3)
      .map(item => ({
        type: 'history',
        text: item.query,
        subtitle: '搜索历史',
        icon: '🕒'
      }))
    
    suggestions.push(...historyMatches)
    
    // 添加作者建议
    const poets = ['李白', '杜甫', '苏轼', '白居易', '王维', '李商隐', '杜牧', '王昌龄']
    const poetMatches = poets
      .filter(poet => poet.includes(query))
      .slice(0, 2)
      .map(poet => ({
        type: 'poet',
        text: poet,
        subtitle: '诗人',
        icon: '👤'
      }))
    
    suggestions.push(...poetMatches)
    
    // 添加朝代建议
    const dynasties = ['唐代', '宋代', '明代', '清代', '汉代', '魏晋']
    const dynastyMatches = dynasties
      .filter(dynasty => dynasty.includes(query))
      .slice(0, 2)
      .map(dynasty => ({
        type: 'dynasty',
        text: dynasty,
        subtitle: '朝代',
        icon: '🏛️'
      }))
    
    suggestions.push(...dynastyMatches)
    
    searchSuggestions.value = suggestions.slice(0, 8)
    highlightedIndex.value = -1
  }

  const handleSearchInput = async () => {
    await generateSuggestions(searchQuery.value)
    showSuggestions.value = searchSuggestions.value.length > 0
  }

  const handleSearchFocus = () => {
    isSearchFocused.value = true
    if (searchQuery.value.trim()) {
      showSuggestions.value = true
    }
  }

  const handleSearchBlur = () => {
    isSearchFocused.value = false
    setTimeout(() => {
      showSuggestions.value = false
    }, 200)
  }

  // 🔧 修复：只处理搜索建议相关的键盘事件
  const handleKeydown = (event) => {
    // 只在 showSuggestions.value 为 true 时处理方向键和 ESC
    if (!showSuggestions.value) return
  
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
        }
        // 没有选中建议项时，不处理回车，让 SearchInput 处理
        break
      case 'Escape':
        event.preventDefault()
        showSuggestions.value = false
        highlightedIndex.value = -1
        break
    }
  }
  
  const selectSuggestion = (suggestion) => {
    console.log('📝 选择建议:', suggestion)
    searchQuery.value = suggestion.text
    showSuggestions.value = false
    
    // 🔧 修复：建议选择逻辑
    if (suggestion.type === 'history') {
      // 历史搜索保持当前模式
      console.log('📚 历史搜索，当前模式:', currentSearchType.value)
      if (currentSearchType.value === 'ai') {
        performAISearch()
      } else {
        performTraditionalSearch()
      }
    } else {
      // 其他建议根据类型智能选择
      if (suggestion.type === 'poet' || suggestion.type === 'dynasty') {
        console.log('👤 作者/朝代搜索，使用传统搜索')
        performTraditionalSearch()
      } else {
        console.log('🤖 其他搜索，使用AI搜索')
        performAISearch()
      }
    }
  }

  return {
    searchSuggestions,
    showSuggestions,
    highlightedIndex,
    isSearchFocused,
    handleSearchInput,
    handleSearchFocus,
    handleSearchBlur,
    handleKeydown,
    selectSuggestion
  }
}