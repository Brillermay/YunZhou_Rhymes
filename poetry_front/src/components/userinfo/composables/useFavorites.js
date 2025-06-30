import { ref, computed, watch } from 'vue'
import { useUserStore } from '@/stores/user'

export function useFavorites() {
  const favoritePoems = ref([])
  const loading = ref(false)
  const searchKeyword = ref('')
  const sortBy = ref('time')
  const userStore = useUserStore()

  // 🔧 修正API基础URL - 改为8081端口
  const API_BASE = 'http://localhost:8081'
  const STORAGE_KEY = 'poetrySearchFavorites' // 统一存储键名

  // 🔧 检查用户是否完全登录
  const isUserFullyAuthenticated = () => {
    const isAuth = userStore?.isAuthenticated
    const hasUid = userStore?.uid != null && userStore?.uid !== undefined
    
    console.log('🔍 [UserInfo] 检查用户完整登录状态:')
    console.log('  - isAuthenticated:', isAuth)
    console.log('  - uid:', userStore?.uid)
    console.log('  - 完整登录:', isAuth && hasUid)
    
    return isAuth && hasUid
  }

  // 过滤和排序后的收藏列表
  const filteredFavorites = computed(() => {
    let filtered = [...favoritePoems.value]
    
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase()
      filtered = filtered.filter(poem => 
        poem.title?.toLowerCase().includes(keyword) ||
        poem.poet?.toLowerCase().includes(keyword) ||
        poem.text?.toLowerCase().includes(keyword) ||
        poem.category?.toLowerCase().includes(keyword)
      )
    }
    
    filtered.sort((a, b) => {
      switch (sortBy.value) {
        case 'time':
          return new Date(b.favoriteTime) - new Date(a.favoriteTime)
        case 'poet':
          return (a.poet || '').localeCompare(b.poet || '', 'zh-CN')
        case 'title':
          return (a.title || '').localeCompare(b.title || '', 'zh-CN')
        default:
          return 0
      }
    })
    
    return filtered
  })

  // 收藏统计
  const favoriteStats = computed(() => {
    const total = favoritePoems.value.length
    const poets = new Set(favoritePoems.value.map(p => p.poet).filter(Boolean))
    const categories = new Set(favoritePoems.value.map(p => p.category).filter(Boolean))
    
    return {
      total,
      poets: poets.size,
      categories: categories.size,
      avgLength: total > 0 ? Math.round(favoritePoems.value.reduce((sum, p) => sum + (p.text?.length || 0), 0) / total) : 0
    }
  })

  // 🔥 从服务器加载收藏列表
  const loadFavoritesFromServer = async () => {
    if (!isUserFullyAuthenticated()) {
      console.log('⚠️ [UserInfo] 用户未完全登录，加载本地收藏')
      loadFavoritesFromLocalStorage()
      return
    }

    loading.value = true
    console.log('📚 [UserInfo] 从服务器加载收藏列表, UID:', userStore.uid)
    
    try {
      const url = `${API_BASE}/star/list/${userStore.uid}`
      console.log('🔗 [UserInfo] 请求URL:', url)
      
      const response = await fetch(url, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json'
        }
      })

      console.log('📡 [UserInfo] 服务器响应状态:', response.status)

      if (response.ok) {
        let poems = []
        try {
          poems = await response.json()
          console.log('✅ [UserInfo] 收藏列表加载成功:', poems.length, '首')
        } catch (jsonError) {
          const responseText = await response.text()
          console.log('📄 [UserInfo] 服务器返回原始内容:', responseText)
          
          if (responseText.trim() === '' || responseText.trim() === '[]') {
            poems = []
            console.log('✅ [UserInfo] 服务器返回空收藏列表')
          } else {
            throw new Error('服务器返回的不是有效的JSON格式')
          }
        }
        
        // 转换数据格式，添加收藏时间
        favoritePoems.value = poems.map(poem => ({
          pid: poem.pid,
          title: poem.title || '无题',
          poet: poem.poet || '佚名',
          category: poem.category || '古诗',
          text: poem.text || '',
          favoriteTime: poem.favoriteTime || new Date().toISOString()
        }))
        
        // 同步到本地存储
        saveFavoritesToStorage()
      } else {
        throw new Error(`服务器错误: ${response.status}`)
      }
    } catch (error) {
      console.error('💥 [UserInfo] 加载收藏列表失败:', error)
      // fallback到本地存储
      loadFavoritesFromLocalStorage()
    } finally {
      loading.value = false
    }
  }

  // 🔥 添加收藏到服务器
  const addFavoriteToServer = async (poem) => {
    if (!isUserFullyAuthenticated()) {
      console.log('⚠️ [UserInfo] 用户未登录，无法添加收藏')
      return { success: false, message: '请先登录' }
    }

    console.log('⭐ [UserInfo] 添加收藏到服务器:', poem.title, 'UID:', userStore.uid, 'PID:', poem.pid)
    
    try {
      const response = await fetch(`${API_BASE}/star/add`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
          uid: userStore.uid.toString(),
          pid: poem.pid.toString()
        })
      })

      const result = await response.text()
      console.log('[UserInfo] 服务器响应:', result)

      if (response.ok && result.includes('成功')) {
        // 添加到本地列表
        const favoriteItem = {
          pid: poem.pid,
          title: poem.title || '无题',
          poet: poem.poet || '佚名',
          category: poem.category || '古诗',
          text: poem.text || '',
          favoriteTime: new Date().toISOString()
        }
        
        // 检查是否已存在
        if (!favoritePoems.value.some(p => p.pid === poem.pid)) {
          favoritePoems.value.unshift(favoriteItem)
          console.log('✅ [UserInfo] 收藏添加成功，当前收藏数:', favoritePoems.value.length)
        }
        
        return { success: true, message: '收藏成功' }
      } else {
        throw new Error(result || '收藏失败')
      }
    } catch (error) {
      console.error('💥 [UserInfo] 添加收藏失败:', error)
      return { success: false, message: error.message || '收藏失败，请稍后重试' }
    }
  }

  // 🔥 从服务器移除收藏
  const removeFavoriteFromServer = async (pid) => {
    if (!isUserFullyAuthenticated()) {
      console.log('⚠️ [UserInfo] 用户未登录，无法移除收藏')
      return { success: false, message: '请先登录' }
    }

    console.log('❌ [UserInfo] 从服务器移除收藏:', pid, 'UID:', userStore.uid)
    
    try {
      const response = await fetch(`${API_BASE}/star/remove`, {
        method: 'DELETE',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
          uid: userStore.uid.toString(),
          pid: pid.toString()
        })
      })

      const result = await response.text()
      console.log('[UserInfo] 服务器响应:', result)

      if (response.ok && result.includes('成功')) {
        // 从本地列表移除
        const index = favoritePoems.value.findIndex(p => p.pid === pid)
        if (index > -1) {
          const removed = favoritePoems.value.splice(index, 1)[0]
          console.log('✅ [UserInfo] 收藏移除成功:', removed.title)
        }
        
        return { success: true, message: '已取消收藏' }
      } else {
        throw new Error(result || '取消收藏失败')
      }
    } catch (error) {
      console.error('💥 [UserInfo] 移除收藏失败:', error)
      return { success: false, message: error.message || '取消收藏失败，请稍后重试' }
    }
  }

  // 本地存储操作（作为备份）
  const loadFavoritesFromLocalStorage = () => {
    console.log('📚 [UserInfo] 从本地存储加载收藏列表')
    
    try {
      const saved = localStorage.getItem(STORAGE_KEY)
      if (saved) {
        const parsed = JSON.parse(saved)
        favoritePoems.value = parsed.map(poem => ({
          ...poem,
          favoriteTime: poem.favoriteTime || new Date().toISOString()
        }))
        console.log('✅ [UserInfo] 本地收藏列表加载成功:', favoritePoems.value.length, '首')
      } else {
        console.log('📝 [UserInfo] 本地存储中没有收藏数据')
        favoritePoems.value = []
      }
    } catch (error) {
      console.error('💥 [UserInfo] 加载本地收藏列表失败:', error)
      favoritePoems.value = []
    }
  }

  const saveFavoritesToStorage = () => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(favoritePoems.value))
      console.log('💾 [UserInfo] 收藏列表已保存到本地存储')
    } catch (error) {
      console.error('💥 [UserInfo] 保存收藏列表失败:', error)
    }
  }

  // 🔥 统一的添加收藏方法
  const addFavorite = async (poem) => {
    console.log('⭐ [UserInfo] 添加收藏:', poem.title)
    
    // 检查是否已收藏
    if (isFavorited(poem.pid)) {
      console.log('⚠️ [UserInfo] 诗词已在收藏列表中')
      return { success: false, message: '该诗词已在收藏列表中' }
    }
    
    if (isUserFullyAuthenticated()) {
      // 用户已登录，使用服务器API
      return await addFavoriteToServer(poem)
    } else {
      // 用户未登录，使用本地存储
      const favoriteItem = {
        pid: poem.pid,
        title: poem.title || '无题',
        poet: poem.poet || '佚名',
        category: poem.category || '古诗',
        text: poem.text || '',
        favoriteTime: new Date().toISOString()
      }
      
      favoritePoems.value.unshift(favoriteItem)
      console.log('✅ [UserInfo] 本地收藏添加成功，当前收藏数:', favoritePoems.value.length)
      
      return { success: true, message: '收藏成功（本地存储）' }
    }
  }

  // 🔥 统一的移除收藏方法
  const removeFavorite = async (pid) => {
    console.log('❌ [UserInfo] 移除收藏:', pid)
    
    if (isUserFullyAuthenticated()) {
      // 用户已登录，使用服务器API
      return await removeFavoriteFromServer(pid)
    } else {
      // 用户未登录，使用本地存储
      const index = favoritePoems.value.findIndex(p => p.pid === pid)
      if (index > -1) {
        const removed = favoritePoems.value.splice(index, 1)[0]
        console.log('✅ [UserInfo] 本地收藏移除成功:', removed.title)
        return { success: true, message: '已取消收藏（本地存储）' }
      } else {
        console.log('⚠️ [UserInfo] 未找到要移除的收藏')
        return { success: false, message: '未找到该收藏' }
      }
    }
  }

  // 检查是否已收藏
  const isFavorited = (pid) => {
    return favoritePoems.value.some(p => p.pid === pid)
  }

  // 切换收藏状态
  const toggleFavorite = async (poem) => {
    if (isFavorited(poem.pid)) {
      return await removeFavorite(poem.pid)
    } else {
      return await addFavorite(poem)
    }
  }

  // 🔥 统一的加载收藏方法
  const initializeFavorites = async () => {
    console.log('🔄 [UserInfo] 初始化收藏列表')
    console.log('👤 [UserInfo] 用户登录状态:', userStore?.isAuthenticated)
    console.log('🆔 [UserInfo] 用户UID:', userStore?.uid)
    
    if (isUserFullyAuthenticated()) {
      // 用户已登录，从服务器加载
      await loadFavoritesFromServer()
    } else {
      // 用户未登录，从本地存储加载
      loadFavoritesFromLocalStorage()
    }
  }

  // 批量操作
  const clearAllFavorites = () => {
    return new Promise((resolve) => {
      if (confirm(`确定要清空所有收藏吗？这将删除 ${favoritePoems.value.length} 首诗词的收藏记录。`)) {
        favoritePoems.value = []
        saveFavoritesToStorage()
        console.log('🗑️ [UserInfo] 所有收藏已清空')
        resolve({ success: true, message: '收藏已清空' })
      } else {
        resolve({ success: false, message: '取消操作' })
      }
    })
  }

  // 导出收藏列表
  const exportFavorites = () => {
    try {
      if (favoritePoems.value.length === 0) {
        alert('没有收藏的诗词可以导出')
        return { success: false, message: '没有收藏的诗词' }
      }

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
      
      console.log('📤 [UserInfo] 收藏列表导出成功')
      alert(`成功导出 ${favoritePoems.value.length} 首诗词的收藏`)
      return { success: true, message: '导出成功' }
    } catch (error) {
      console.error('💥 [UserInfo] 导出收藏列表失败:', error)
      alert('导出失败，请稍后重试')
      return { success: false, message: '导出失败' }
    }
  }

  // 导入收藏列表
  const importFavorites = (file) => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader()
      
      reader.onload = (e) => {
        try {
          const importedData = JSON.parse(e.target.result)
          if (Array.isArray(importedData)) {
            const existingPids = new Set(favoritePoems.value.map(p => p.pid))
            const newFavorites = importedData.filter(p => !existingPids.has(p.pid))
            
            favoritePoems.value.push(...newFavorites)
            saveFavoritesToStorage()
            console.log('📥 [UserInfo] 收藏列表导入成功，新增:', newFavorites.length, '首')
            resolve({ success: true, message: `导入成功，新增 ${newFavorites.length} 首诗词`, count: newFavorites.length })
          } else {
            throw new Error('文件格式不正确')
          }
        } catch (error) {
          console.error('💥 [UserInfo] 解析导入文件失败:', error)
          reject({ success: false, message: '文件格式错误' })
        }
      }
      
      reader.onerror = () => {
        reject({ success: false, message: '文件读取失败' })
      }
      
      reader.readAsText(file)
    })
  }

  // 获取诗词预览文本
  const getPreviewText = (text) => {
    if (!text) return '暂无内容'
    return text.length > 50 ? text.substring(0, 50) + '...' : text
  }

  // 格式化日期
  const formatDate = (dateString) => {
    if (!dateString) return ''
    const date = new Date(dateString)
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    })
  }

  // 监听用户登录状态变化
  watch(() => userStore.isAuthenticated, async (newValue, oldValue) => {
    console.log('👤 [UserInfo] 用户登录状态变化:', oldValue, '->', newValue)
    
    if (newValue && !oldValue) {
      // 用户刚登录，从服务器加载收藏
      console.log('👤 [UserInfo] 用户登录，重新加载收藏列表')
      setTimeout(async () => {
        if (isUserFullyAuthenticated()) {
          await loadFavoritesFromServer()
        }
      }, 500)
    } else if (!newValue && oldValue) {
      // 用户退出登录，切换到本地存储
      console.log('👤 [UserInfo] 用户退出，切换到本地收藏')
      loadFavoritesFromLocalStorage()
    }
  })

  // 监听用户UID变化
  watch(() => userStore?.uid, async (newUid, oldUid) => {
    console.log('🆔 [UserInfo] 用户UID变化:', oldUid, '->', newUid)
    
    if (newUid && newUid !== oldUid && userStore?.isAuthenticated) {
      console.log('🆔 [UserInfo] UID更新完成，重新加载服务器收藏')
      await loadFavoritesFromServer()
    }
  }, { immediate: false })

  // 监听收藏列表变化，自动保存到本地存储
  watch(favoritePoems, () => {
    saveFavoritesToStorage()
  }, { deep: true })

  return {
    // 状态
    favoritePoems,
    loading,
    searchKeyword,
    sortBy,
    
    // 计算属性
    filteredFavorites,
    favoriteStats,
    
    // 方法
    addFavorite,
    removeFavorite,
    isFavorited,
    toggleFavorite,
    clearAllFavorites,
    exportFavorites,
    importFavorites,
    initializeFavorites,
    getPreviewText,
    formatDate,
    isUserFullyAuthenticated
  }
}