import { ref, computed, watch, onMounted } from 'vue'

export function useFavorites(userStore) {
  const favoriteIds = ref(new Set())
  const favoritePoems = ref([])
  const loading = ref(false)

  // 🔧 修正API基础URL - 改为8081端口
  const API_BASE = 'http://localhost:8081'
  const STORAGE_KEY = 'poetrySearchFavorites'

  // 🔧 检查用户是否完全登录（包括uid）
  const isUserFullyAuthenticated = () => {
    const isAuth = userStore?.isAuthenticated
    const hasUid = userStore?.uid != null && userStore?.uid !== undefined
    
    console.log('🔍 检查用户完整登录状态:')
    console.log('  - isAuthenticated:', isAuth)
    console.log('  - uid:', userStore?.uid)
    console.log('  - 完整登录:', isAuth && hasUid)
    
    return isAuth && hasUid
  }

  // 🔥 从服务器加载收藏列表 - 修复响应处理
  const loadFavoritesFromServer = async () => {
    if (!isUserFullyAuthenticated()) {
      console.log('⚠️ 用户未完全登录，无法从服务器加载收藏')
      loadFavoritesFromStorage()
      return []
    }

    loading.value = true
    console.log('📚 从服务器加载收藏列表, UID:', userStore.uid)
    
    try {
      const url = `${API_BASE}/star/list/${userStore.uid}`
      console.log('🔗 请求URL:', url)
      
      const response = await fetch(url, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        }
      })

      console.log('📡 服务器响应状态:', response.status)
      console.log('📡 服务器响应类型:', response.headers.get('content-type'))

      if (!response.ok) {
        throw new Error(`HTTP错误: ${response.status} ${response.statusText}`)
      }

      // 🔧 简化内容类型检查 - 只要响应成功就尝试解析JSON
      let poems = []
      try {
        poems = await response.json()
        console.log('✅ 服务器收藏列表加载成功:', poems.length, '首')
      } catch (jsonError) {
        // 如果JSON解析失败，检查是否是空数组或其他格式
        const responseText = await response.text()
        console.log('📄 服务器返回原始内容:', responseText)
        
        if (responseText.trim() === '' || responseText.trim() === '[]') {
          poems = []
          console.log('✅ 服务器返回空收藏列表')
        } else {
          throw new Error('服务器返回的不是有效的JSON格式')
        }
      }
      
      // 更新本地状态
      favoritePoems.value = poems
      favoriteIds.value = new Set(poems.map(p => p.pid))
      
      // 同步到本地存储
      saveFavoritesToStorage(poems)
      return poems
    } catch (error) {
      console.error('💥 从服务器加载收藏失败:', error)
      console.log('🔄 回退到本地存储模式')
      
      // fallback到本地存储
      loadFavoritesFromStorage()
      return []
    } finally {
      loading.value = false
    }
  }

  // 🔥 添加收藏到服务器
  const addFavoriteToServer = async (poem) => {
    if (!isUserFullyAuthenticated()) {
      console.log('⚠️ 用户未完全登录，无法添加收藏')
      return { success: false, message: '用户登录信息不完整，请重新登录' }
    }

    console.log('⭐ 添加收藏到服务器:', poem.title, 'UID:', userStore.uid, 'PID:', poem.pid)
    
    try {
      // 确保 uid 和 pid 都存在且有效
      if (!poem.pid) {
        throw new Error('诗词ID无效')
      }

      const response = await fetch(`${API_BASE}/star/add`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
          uid: String(userStore.uid),
          pid: String(poem.pid)
        })
      })

      console.log('📡 添加收藏响应状态:', response.status)
      const result = await response.text()
      console.log('📡 添加收藏服务器响应:', result)

      if (response.ok && result.includes('成功')) {
        // 更新本地状态
        favoriteIds.value.add(poem.pid)
        favoritePoems.value.unshift(poem)
        
        // 同步到本地存储
        saveFavoritesToStorage(favoritePoems.value)
        
        return { success: true, message: '收藏成功' }
      } else {
        throw new Error(result || '收藏失败')
      }
    } catch (error) {
      console.error('💥 添加收藏失败:', error)
      return { success: false, message: error.message || '收藏失败，请稍后重试' }
    }
  }

  // 🔥 从服务器移除收藏
  const removeFavoriteFromServer = async (pid) => {
    if (!isUserFullyAuthenticated()) {
      console.log('⚠️ 用户未完全登录，无法移除收藏')
      return { success: false, message: '用户登录信息不完整，请重新登录' }
    }

    console.log('❌ 从服务器移除收藏:', pid, 'UID:', userStore.uid)
    
    try {
      if (!pid) {
        throw new Error('诗词ID无效')
      }

      const response = await fetch(`${API_BASE}/star/remove`, {
        method: 'DELETE',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
          uid: String(userStore.uid),
          pid: String(pid)
        })
      })

      console.log('📡 移除收藏响应状态:', response.status)
      const result = await response.text()
      console.log('📡 移除收藏服务器响应:', result)

      if (response.ok && result.includes('成功')) {
        // 更新本地状态
        favoriteIds.value.delete(pid)
        favoritePoems.value = favoritePoems.value.filter(p => p.pid !== pid)
        
        // 同步到本地存储
        saveFavoritesToStorage(favoritePoems.value)
        
        return { success: true, message: '已取消收藏' }
      } else {
        throw new Error(result || '取消收藏失败')
      }
    } catch (error) {
      console.error('💥 移除收藏失败:', error)
      return { success: false, message: error.message || '取消收藏失败，请稍后重试' }
    }
  }

  // 本地存储操作
  const loadFavoritesFromStorage = () => {
    console.log('📱 从本地存储加载收藏')
    try {
      const saved = localStorage.getItem(STORAGE_KEY)
      if (saved) {
        const poems = JSON.parse(saved)
        favoritePoems.value = poems
        favoriteIds.value = new Set(poems.map(p => p.pid))
        console.log('✅ 本地收藏加载成功:', poems.length, '首')
        return poems
      } else {
        favoritePoems.value = []
        favoriteIds.value = new Set()
        console.log('📱 本地存储中没有收藏数据')
        return []
      }
    } catch (error) {
      console.error('💥 加载本地收藏失败:', error)
      favoritePoems.value = []
      favoriteIds.value = new Set()
      return []
    }
  }

  const saveFavoritesToStorage = (poems) => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(poems))
      console.log('💾 收藏已保存到本地存储')
    } catch (error) {
      console.error('💥 保存收藏到本地失败:', error)
    }
  }

  // 🔥 统一的加载收藏方法
  const loadFavorites = async () => {
    console.log('🔄 开始加载收藏，用户登录状态:', userStore?.isAuthenticated)
    console.log('🔄 用户UID:', userStore?.uid)
    
    if (isUserFullyAuthenticated()) {
      // 用户已完全登录，从服务器加载
      await loadFavoritesFromServer()
    } else {
      // 用户未登录或登录不完整，从本地存储加载
      loadFavoritesFromStorage()
    }
  }

  // 🔥 统一的切换收藏方法
  const toggleFavorite = async (poem) => {
    console.log('🎯 切换收藏 - 开始检查状态')
    console.log('  - 诗词:', poem.title, 'PID:', poem.pid)
    console.log('  - 用户登录状态:', userStore?.isAuthenticated)
    console.log('  - 用户UID:', userStore?.uid)
    
    if (!isUserFullyAuthenticated()) {
      console.log('⚠️ 用户登录信息不完整，无法操作收藏')
      return { success: false, message: '请重新登录后再试' }
    }

    // 检查诗词数据有效性
    if (!poem || !poem.pid) {
      console.log('⚠️ 诗词数据无效')
      return { success: false, message: '诗词数据无效' }
    }

    const isCurrentlyFavorited = favoriteIds.value.has(poem.pid)
    console.log('🔄 切换收藏状态:', poem.title, '当前状态:', isCurrentlyFavorited ? '已收藏' : '未收藏')

    if (isCurrentlyFavorited) {
      return await removeFavoriteFromServer(poem.pid)
    } else {
      return await addFavoriteToServer(poem)
    }
  }

  // 检查是否已收藏
  const isFavorite = (pid) => {
    return favoriteIds.value.has(pid)
  }

  // 清空所有收藏
  const clearAllFavorites = () => {
    if (confirm('确定要清空所有收藏吗？')) {
      favoriteIds.value.clear()
      favoritePoems.value = []
      saveFavoritesToStorage([])
      console.log('🗑️ 已清空所有收藏')
    }
  }

  // 导出收藏
  const exportFavorites = () => {
    try {
      const dataStr = JSON.stringify(favoritePoems.value, null, 2)
      const dataBlob = new Blob([dataStr], { type: 'application/json' })
      const url = URL.createObjectURL(dataBlob)
      
      const link = document.createElement('a')
      link.href = url
      link.download = `poetry-favorites-${new Date().toISOString().split('T')[0]}.json`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
      
      console.log('📤 收藏列表导出成功')
    } catch (error) {
      console.error('💥 导出收藏失败:', error)
    }
  }

  // 监听用户登录状态变化
  watch(() => userStore?.isAuthenticated, async (newValue, oldValue) => {
    console.log('👤 用户登录状态变化:', oldValue, '->', newValue)
    console.log('👤 当前用户UID:', userStore?.uid)
    
    if (newValue && !oldValue) {
      // 用户刚登录
      setTimeout(async () => {
        if (isUserFullyAuthenticated()) {
          console.log('👤 用户登录完成，从服务器加载收藏')
          await loadFavoritesFromServer()
        }
      }, 500)
    } else if (!newValue && oldValue) {
      // 用户退出登录
      console.log('👤 用户退出，切换到本地收藏')
      loadFavoritesFromStorage()
    }
  }, { immediate: false })

  // 监听用户UID变化
  watch(() => userStore?.uid, async (newUid, oldUid) => {
    console.log('🆔 用户UID变化:', oldUid, '->', newUid)
    
    if (newUid && newUid !== oldUid && userStore?.isAuthenticated) {
      console.log('🆔 UID更新完成，重新加载服务器收藏')
      await loadFavoritesFromServer()
    }
  }, { immediate: false })

  return {
    favoriteIds,
    favoritePoems,
    loading,
    loadFavorites,
    toggleFavorite,
    isFavorite,
    clearAllFavorites,
    exportFavorites,
    isUserFullyAuthenticated
  }
}