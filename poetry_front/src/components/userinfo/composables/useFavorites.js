import { ref, computed, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import API_BASE_URL from '@/config/api'

export function useFavorites() {
  // ===== 状态管理 =====
  const favoritePoems = ref([])
  const loading = ref(false)
  const searchKeyword = ref('')
  const sortBy = ref('time')
  const userStore = useUserStore()

  // ===== 常量配置 =====
  const STORAGE_KEY = 'user_favorites' // 统一存储键名

  // ===== 工具函数 =====
  
  /**
   * 检查用户是否完全登录（包括UID）
   */
  const isUserFullyAuthenticated = () => {
    const isAuth = userStore?.isAuthenticated
    const hasUid = userStore?.uid != null && userStore?.uid !== undefined
    
    console.log('🔍 [UserInfo] 检查用户完整登录状态:')
    console.log('  - isAuthenticated:', isAuth)
    console.log('  - uid:', userStore?.uid)
    console.log('  - 完整登录:', isAuth && hasUid)
    
    return isAuth && hasUid
  }

  /**
   * 标准化诗词数据格式
   */
  const normalizePoem = (poem) => {
    if (!poem) return null
    
    return {
      pid: poem.PID || poem.pid || poem.id,
      PID: poem.PID || poem.pid || poem.id, // 保持兼容性
      title: poem.title || poem.Title || '无题',
      poet: poem.poet || poem.Poet || poem.author || '佚名',
      text: poem.text || poem.Text || poem.content || '',
      dynasty: poem.dynasty || poem.Dynasty || '',
      category: poem.category || poem.Category || '古诗',
      favoriteTime: poem.favoriteTime || poem.createTime || new Date().toISOString(),
      source: poem.source || 'unknown'
    }
  }

  // ===== 计算属性 =====
  
  /**
   * 过滤和排序后的收藏列表
   */
  const filteredFavorites = computed(() => {
    let filtered = [...favoritePoems.value]
    
    // 搜索过滤
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase()
      filtered = filtered.filter(poem => 
        poem.title?.toLowerCase().includes(keyword) ||
        poem.poet?.toLowerCase().includes(keyword) ||
        poem.text?.toLowerCase().includes(keyword) ||
        poem.category?.toLowerCase().includes(keyword) ||
        poem.dynasty?.toLowerCase().includes(keyword)
      )
    }
    
    // 排序
    filtered.sort((a, b) => {
      switch (sortBy.value) {
        case 'time':
          return new Date(b.favoriteTime) - new Date(a.favoriteTime)
        case 'poet':
          return (a.poet || '').localeCompare(b.poet || '', 'zh-CN')
        case 'title':
          return (a.title || '').localeCompare(b.title || '', 'zh-CN')
        case 'dynasty':
          return (a.dynasty || '').localeCompare(b.dynasty || '', 'zh-CN')
        default:
          return 0
      }
    })
    
    return filtered
  })

  /**
   * 收藏统计
   */
  const favoriteStats = computed(() => {
    const total = favoritePoems.value.length
    const poets = new Set(favoritePoems.value.map(p => p.poet).filter(Boolean))
    const dynasties = new Set(favoritePoems.value.map(p => p.dynasty).filter(Boolean))
    const categories = new Set(favoritePoems.value.map(p => p.category).filter(Boolean))
    
    return {
      total,
      poets: poets.size,
      dynasties: dynasties.size,
      categories: categories.size,
      avgLength: total > 0 ? Math.round(favoritePoems.value.reduce((sum, p) => sum + (p.text?.length || 0), 0) / total) : 0
    }
  })

  // ===== 本地存储操作 =====
  
  /**
   * 从本地存储加载收藏
   */
  const loadFavoritesFromLocal = () => {
    try {
      const localFavorites = localStorage.getItem(STORAGE_KEY)
      if (!localFavorites) {
        console.log('📦 [UserInfo] 本地无收藏数据')
        return []
      }

      const parsed = JSON.parse(localFavorites)
      console.log(`📦 [UserInfo] 从本地加载了 ${parsed.length} 个收藏`)
      
      return parsed.map(item => normalizePoem({
        ...item,
        source: 'local'
      })).filter(Boolean)
    } catch (error) {
      console.error('💥 [UserInfo] 从本地加载收藏失败:', error)
      localStorage.removeItem(STORAGE_KEY) // 清除损坏的数据
      return []
    }
  }

  /**
   * 保存收藏到本地存储
   */
  const saveFavoritesToLocal = (favorites = favoritePoems.value) => {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(favorites))
      console.log(`💾 [UserInfo] 已保存 ${favorites.length} 个收藏到本地`)
    } catch (error) {
      console.error('💥 [UserInfo] 保存收藏到本地失败:', error)
    }
  }

  // ===== 服务器操作 =====
  
  /**
   * 从服务器加载收藏列表
   */
  const loadFavoritesFromServer = async () => {
    if (!isUserFullyAuthenticated()) {
      console.log('⚠️ [UserInfo] 用户未完全登录，无法从服务器加载收藏')
      return []
    }

    try {
      console.log('🔄 [UserInfo] 开始从服务器加载收藏列表...')
      console.log('👤 [UserInfo] 当前用户 UID:', userStore.uid)

      const response = await fetch(`${API_BASE_URL}/star/list/${userStore.uid}`, {
        method: 'GET',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        }
      })

      console.log('📡 [UserInfo] 服务器响应状态:', response.status)

      if (!response.ok) {
        throw new Error(`HTTP错误: ${response.status} ${response.statusText}`)
      }

      // 🔧 修复：简化响应处理，只读取一次
      let responseData
      const contentType = response.headers.get('content-type')
      
      if (contentType && contentType.includes('application/json')) {
        try {
          responseData = await response.json()
        } catch (jsonError) {
          console.warn('⚠️ [UserInfo] JSON 解析失败，尝试读取为文本:', jsonError)
          // 如果JSON解析失败，不要再次读取response，直接返回空数组
          responseData = []
        }
      } else {
        try {
          const textData = await response.text()
          if (textData.trim() === '' || textData.trim() === '[]') {
            responseData = []
          } else {
            try {
              responseData = JSON.parse(textData)
            } catch (parseError) {
              console.warn('⚠️ [UserInfo] 文本解析为JSON失败:', parseError)
              responseData = []
            }
          }
        } catch (textError) {
          console.error('💥 [UserInfo] 读取响应文本失败:', textError)
          responseData = []
        }
      }

      console.log('📋 [UserInfo] 服务器返回数据:', responseData)

      // 处理不同格式的响应
      let favorites = []
      
      if (Array.isArray(responseData)) {
        favorites = responseData
      } else if (typeof responseData === 'string') {
        if (responseData.includes('用户不存在') || responseData.includes('没有收藏')) {
          console.log('ℹ️ [UserInfo] 用户暂无收藏')
          return []
        }
        favorites = []
      } else if (responseData && typeof responseData === 'object') {
        if (responseData.success && Array.isArray(responseData.data)) {
          favorites = responseData.data
        } else if (Array.isArray(responseData.favorites)) {
          favorites = responseData.favorites
        } else {
          console.warn('⚠️ [UserInfo] 未知的响应格式:', responseData)
          favorites = []
        }
      }

      console.log(`✅ [UserInfo] 从服务器加载了 ${favorites.length} 个收藏`)
      
      // 标准化数据格式
      return favorites.map(item => normalizePoem({
        ...item,
        source: 'server'
      })).filter(Boolean)

    } catch (error) {
      console.error('💥 [UserInfo] 从服务器加载收藏失败:', error)
      
      if (error.message.includes('Failed to fetch')) {
        throw new Error('网络连接失败，请检查网络连接')
      } else {
        throw error
      }
    }
  }

  /**
   * 添加收藏到服务器
   */
  const addFavoriteToServer = async (poem) => {
    if (!isUserFullyAuthenticated()) {
      console.log('⚠️ [UserInfo] 用户未完全登录，无法添加收藏')
      return { success: false, message: '请先登录' }
    }

    const normalizedPoem = normalizePoem(poem)
    if (!normalizedPoem || !normalizedPoem.pid) {
      return { success: false, message: '诗词数据无效' }
    }

    console.log('⭐ [UserInfo] 添加收藏到服务器:', normalizedPoem.title, 'UID:', userStore.uid, 'PID:', normalizedPoem.pid)
    
    try {
      const response = await fetch(`${API_BASE_URL}/star/add`, {
        method: 'POST',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
          uid: String(userStore.uid),
          pid: String(normalizedPoem.pid)
        })
      })

      const result = await response.text()
      console.log('📡 [UserInfo] 添加收藏服务器响应:', result)

      if (response.ok && result.includes('成功')) {
        // 添加到本地列表（如果不存在）
        if (!favoritePoems.value.some(p => p.pid == normalizedPoem.pid)) {
          const favoriteItem = {
            ...normalizedPoem,
            favoriteTime: new Date().toISOString(),
            source: 'server'
          }
          favoritePoems.value.unshift(favoriteItem)
          saveFavoritesToLocal()
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

  /**
   * 从服务器移除收藏
   */
  const removeFavoriteFromServer = async (pid) => {
    if (!isUserFullyAuthenticated()) {
      console.log('⚠️ [UserInfo] 用户未完全登录，无法移除收藏')
      return { success: false, message: '请先登录' }
    }

    try {
      console.log('🗑️ [UserInfo] 从服务器移除收藏:', pid)
      
      const response = await fetch(`${API_BASE_URL}/star/remove?uid=${userStore.uid}&pid=${pid}`, {
        method: 'DELETE',
        credentials: 'include',
        headers: {
          'Content-Type': 'application/json',
        }
      })

      const result = await response.text()
      console.log('📡 [UserInfo] 移除收藏服务器响应:', result)

      if (response.ok && (result.includes('成功') || result.includes('删除'))) {
        console.log('✅ [UserInfo] 服务器移除收藏成功')
        return { success: true, message: '服务器移除成功' }
      } else {
        console.warn(`⚠️ [UserInfo] 服务器移除收藏失败: ${result}`)
        return { success: false, message: `服务器移除失败: ${result}` }
      }
    } catch (error) {
      console.error('💥 [UserInfo] 服务器移除收藏失败:', error)
      return { success: false, message: `服务器移除失败: ${error.message}` }
    }
  }

  // ===== 统一操作接口 =====
  
  /**
   * 添加收藏（统一接口）
   */
  const addFavorite = async (poem) => {
    const normalizedPoem = normalizePoem(poem)
    if (!normalizedPoem || !normalizedPoem.pid) {
      return { success: false, message: '诗词数据无效' }
    }

    console.log('⭐ [UserInfo] 添加收藏:', normalizedPoem.title)
    
    // 检查是否已收藏
    if (isFavorited(normalizedPoem.pid)) {
      console.log('⚠️ [UserInfo] 诗词已在收藏列表中')
      return { success: false, message: '该诗词已在收藏列表中' }
    }
    
    if (isUserFullyAuthenticated()) {
      // 用户已登录，使用服务器API
      return await addFavoriteToServer(normalizedPoem)
    } else {
      // 用户未登录，使用本地存储
      const favoriteItem = {
        ...normalizedPoem,
        favoriteTime: new Date().toISOString(),
        source: 'local'
      }
      
      favoritePoems.value.unshift(favoriteItem)
      saveFavoritesToLocal()
      console.log('✅ [UserInfo] 本地收藏添加成功，当前收藏数:', favoritePoems.value.length)
      
      return { success: true, message: '收藏成功（本地存储）' }
    }
  }

  /**
   * 移除收藏（统一接口）
   */
  const removeFavorite = async (pid) => {
    console.log('🗑️ [UserInfo] 移除收藏:', pid)
    
    const poem = favoritePoems.value.find(p => p.pid == pid) // 使用宽松比较
    if (!poem) {
      return { success: false, message: '未找到该收藏' }
    }

    if (isUserFullyAuthenticated()) {
      // 用户已登录，先尝试从服务器移除
      const serverResult = await removeFavoriteFromServer(pid)
      
      // 无论服务器操作是否成功，都从本地移除（保证UI一致性）
      favoritePoems.value = favoritePoems.value.filter(p => p.pid != pid)
      saveFavoritesToLocal()
      
      console.log(`✅ [UserInfo] 移除收藏成功: ${poem.title}`)
      return { success: true, message: `已取消收藏《${poem.title}》` }
    } else {
      // 用户未登录，直接从本地移除
      favoritePoems.value = favoritePoems.value.filter(p => p.pid != pid)
      saveFavoritesToLocal()
      
      console.log(`✅ [UserInfo] 本地收藏移除成功: ${poem.title}`)
      return { success: true, message: `已取消收藏《${poem.title}》（本地存储）` }
    }
  }

  /**
   * 切换收藏状态
   */
  const toggleFavorite = async (poem) => {
    const normalizedPoem = normalizePoem(poem)
    if (!normalizedPoem || !normalizedPoem.pid) {
      return { success: false, message: '诗词数据无效' }
    }

    if (isFavorited(normalizedPoem.pid)) {
      return await removeFavorite(normalizedPoem.pid)
    } else {
      return await addFavorite(normalizedPoem)
    }
  }

  /**
   * 检查是否已收藏
   */
  const isFavorited = (pid) => {
    return favoritePoems.value.some(p => p.pid == pid) // 使用宽松比较
  }

  /**
   * 初始化收藏列表
   */
  const initializeFavorites = async () => {
    if (loading.value) {
      console.log('⏳ [UserInfo] 收藏列表正在加载中，跳过重复初始化')
      return
    }

    loading.value = true
    
    try {
      console.log('🚀 [UserInfo] 开始初始化收藏列表...')
      
      let serverFavorites = []
      let localFavorites = []

      // 加载本地收藏
      localFavorites = loadFavoritesFromLocal()

      // 如果用户已登录，尝试加载服务器收藏
      if (isUserFullyAuthenticated()) {
        try {
          serverFavorites = await loadFavoritesFromServer()
        } catch (error) {
          console.error('💥 [UserInfo] 加载服务器收藏失败:', error)
          // 继续使用本地收藏
        }
      }

      // 合并收藏（服务器优先，本地补充）
      const pidSet = new Set()
      const mergedFavorites = []

      // 先添加服务器收藏
      serverFavorites.forEach(item => {
        if (item && item.pid && !pidSet.has(item.pid)) {
          pidSet.add(item.pid)
          mergedFavorites.push(item)
        }
      })

      // 再添加本地独有的收藏
      localFavorites.forEach(item => {
        if (item && item.pid && !pidSet.has(item.pid)) {
          pidSet.add(item.pid)
          mergedFavorites.push({
            ...item,
            source: 'local'
          })
        }
      })

      favoritePoems.value = mergedFavorites
      
      // 更新本地存储（包含服务器数据）
      saveFavoritesToLocal(mergedFavorites)

      console.log(`✅ [UserInfo] 收藏初始化完成：服务器 ${serverFavorites.length} 个，本地 ${localFavorites.length} 个，合并后 ${mergedFavorites.length} 个`)

    } catch (error) {
      console.error('💥 [UserInfo] 初始化收藏列表失败:', error)
      
      // 即使出错也尝试加载本地数据
      try {
        favoritePoems.value = loadFavoritesFromLocal()
      } catch (localError) {
        console.error('💥 [UserInfo] 本地备用加载也失败:', localError)
        favoritePoems.value = []
      }
      
      // 抛出错误给调用者
      throw error
    } finally {
      loading.value = false
    }
  }

  // ===== 辅助功能 =====
  
  /**
   * 清空所有收藏
   */
  const clearAllFavorites = () => {
    return new Promise((resolve) => {
      if (confirm(`确定要清空所有收藏吗？这将删除 ${favoritePoems.value.length} 首诗词的收藏记录。`)) {
        favoritePoems.value = []
        saveFavoritesToLocal([])
        console.log('🗑️ [UserInfo] 所有收藏已清空')
        resolve({ success: true, message: '收藏已清空' })
      } else {
        resolve({ success: false, message: '取消操作' })
      }
    })
  }

  /**
   * 导出收藏列表
   */
  const exportFavorites = () => {
    try {
      if (favoritePoems.value.length === 0) {
        return { success: false, message: '没有收藏的诗词' }
      }

      const exportData = {
        exportTime: new Date().toISOString(),
        user: userStore.username || '未知用户',
        total: favoritePoems.value.length,
        favorites: favoritePoems.value
      }

      const dataStr = JSON.stringify(exportData, null, 2)
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
      return { success: true, message: '导出成功' }
    } catch (error) {
      console.error('💥 [UserInfo] 导出收藏列表失败:', error)
      return { success: false, message: '导出失败：' + error.message }
    }
  }

  /**
   * 导入收藏列表
   */
  const importFavorites = (file) => {
    return new Promise((resolve, reject) => {
      const reader = new FileReader()
      
      reader.onload = (e) => {
        try {
          const importedData = JSON.parse(e.target.result)
          let favorites = []
          
          if (Array.isArray(importedData)) {
            favorites = importedData
          } else if (importedData.favorites && Array.isArray(importedData.favorites)) {
            favorites = importedData.favorites
          } else {
            throw new Error('文件格式不正确')
          }
          
          const existingPids = new Set(favoritePoems.value.map(p => p.pid))
          const newFavorites = favorites
            .map(p => normalizePoem(p))
            .filter(p => p && p.pid && !existingPids.has(p.pid))
          
          favoritePoems.value.push(...newFavorites)
          saveFavoritesToLocal()
          
          console.log('📥 [UserInfo] 收藏列表导入成功，新增:', newFavorites.length, '首')
          resolve({ 
            success: true, 
            message: `导入成功，新增 ${newFavorites.length} 首诗词`, 
            count: newFavorites.length 
          })
        } catch (error) {
          console.error('💥 [UserInfo] 解析导入文件失败:', error)
          reject({ success: false, message: '文件格式错误：' + error.message })
        }
      }
      
      reader.onerror = () => {
        reject({ success: false, message: '文件读取失败' })
      }
      
      reader.readAsText(file)
    })
  }

  /**
   * 获取诗词预览文本
   */
  const getPreviewText = (text) => {
    if (!text) return '暂无内容'
    return text.length > 50 ? text.substring(0, 50) + '...' : text
  }

  /**
   * 格式化日期
   */
  const formatDate = (dateString) => {
    if (!dateString) return '未知时间'
    try {
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      })
    } catch {
      return '时间格式错误'
    }
  }

  // ===== 监听器 =====
  
  // 监听用户登录状态变化
  watch(() => userStore?.isAuthenticated, async (newValue, oldValue) => {
    console.log('👤 [UserInfo] 用户登录状态变化:', oldValue, '->', newValue)
    
    if (newValue && !oldValue) {
      // 用户刚登录，从服务器加载收藏
      console.log('👤 [UserInfo] 用户登录，重新加载收藏列表')
      setTimeout(async () => {
        if (isUserFullyAuthenticated()) {
          try {
            const serverFavorites = await loadFavoritesFromServer()
            if (serverFavorites && serverFavorites.length >= 0) {
              favoritePoems.value = serverFavorites
              saveFavoritesToLocal(serverFavorites)
            }
          } catch (error) {
            console.error('💥 [UserInfo] 登录后加载收藏失败:', error)
          }
        }
      }, 500)
    } else if (!newValue && oldValue) {
      // 用户退出登录，切换到本地存储
      console.log('👤 [UserInfo] 用户退出，切换到本地收藏')
      try {
        favoritePoems.value = loadFavoritesFromLocal()
      } catch (error) {
        console.error('💥 [UserInfo] 切换到本地收藏失败:', error)
        favoritePoems.value = []
      }
    }
  }, { immediate: false })

  // 监听用户UID变化
  watch(() => userStore?.uid, async (newUid, oldUid) => {
    console.log('🆔 [UserInfo] 用户UID变化:', oldUid, '->', newUid)
    
    if (newUid && newUid !== oldUid && userStore?.isAuthenticated) {
      console.log('🆔 [UserInfo] UID更新完成，重新加载服务器收藏')
      try {
        const serverFavorites = await loadFavoritesFromServer()
        if (serverFavorites && serverFavorites.length >= 0) {
          favoritePoems.value = serverFavorites
          saveFavoritesToLocal(serverFavorites)
        }
      } catch (error) {
        console.error('💥 [UserInfo] UID变化后加载收藏失败:', error)
      }
    }
  }, { immediate: false })

  // 监听收藏列表变化，自动保存到本地存储
  watch(favoritePoems, () => {
    saveFavoritesToLocal()
  }, { deep: true })

  // ===== 返回接口 =====
  return {
    // 状态
    favoritePoems,
    loading,
    searchKeyword,
    sortBy,
    
    // 计算属性
    filteredFavorites,
    favoriteStats,
    
    // 主要方法
    addFavorite,
    removeFavorite,
    toggleFavorite,
    isFavorited,
    initializeFavorites,
    
    // 辅助方法
    clearAllFavorites,
    exportFavorites,
    importFavorites,
    getPreviewText,
    formatDate,
    isUserFullyAuthenticated,
    normalizePoem
  }
}