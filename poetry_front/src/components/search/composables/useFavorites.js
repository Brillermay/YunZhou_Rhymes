import { ref } from 'vue'

const API_BASE_URL = 'http://localhost:8081'

export function useFavorites() {
  const favoriteIds = ref([])
  const favoritePoems = ref([])

  // 假设用户ID为1，实际应该从登录状态获取
  const uid = 1 // 或者从 localStorage.getItem('uid') 获取

  // 🔧 新增：加载用户收藏
  const loadFavorites = async () => {
    try {
      console.log('🔄 正在加载收藏列表...')
      const response = await fetch(`${API_BASE_URL}/star/list/${uid}`)
      
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      
      const data = await response.json()
      favoritePoems.value = Array.isArray(data) ? data : []
      favoriteIds.value = favoritePoems.value.map(poem => poem.PID)
      console.log('✅ 收藏加载完成:', favoritePoems.value.length, '首')
    } catch (error) {
      console.error('❌ 加载收藏失败:', error)
      favoritePoems.value = []
      favoriteIds.value = []
    }
  }

  // 🔧 新增：收藏/取消收藏
  const toggleFavorite = async (poemId) => {
    if (!poemId) {
      console.warn('⚠️ 诗词ID为空，无法收藏')
      return
    }

    try {
      if (favoriteIds.value.includes(poemId)) {
        // 取消收藏
        console.log('📤 取消收藏诗词:', poemId)
        const response = await fetch(`${API_BASE_URL}/star/remove?uid=${uid}&pid=${poemId}`, {
          method: 'DELETE'
        })
        
        if (response.ok) {
          favoriteIds.value = favoriteIds.value.filter(id => id !== poemId)
          favoritePoems.value = favoritePoems.value.filter(poem => poem.PID !== poemId)
          console.log('✅ 取消收藏成功:', poemId)
        } else {
          throw new Error('取消收藏失败')
        }
      } else {
        // 添加收藏
        console.log('📥 收藏诗词:', poemId)
        const response = await fetch(`${API_BASE_URL}/star/add?uid=${uid}&pid=${poemId}`, {
          method: 'POST'
        })
        
        if (response.ok) {
          // 重新加载收藏列表获取完整信息
          await loadFavorites()
          console.log('✅ 收藏成功:', poemId)
        } else {
          throw new Error('收藏失败')
        }
      }
    } catch (error) {
      console.error('❌ 收藏操作失败:', error)
      alert(`收藏操作失败: ${error.message}`)
    }
  }

  // 🔧 新增：判断是否已收藏
  const isFavorite = (poemId) => {
    return favoriteIds.value.includes(poemId)
  }

  // 🔧 新增：清空所有收藏
  const clearAllFavorites = async () => {
    if (favoritePoems.value.length === 0) {
      alert('暂无收藏内容')
      return
    }

    if (confirm('确定要清空所有收藏吗？此操作不可恢复。')) {
      try {
        console.log('🗑️ 正在清空所有收藏...')
        const deletePromises = favoriteIds.value.map(poemId => 
          fetch(`${API_BASE_URL}/star/remove?uid=${uid}&pid=${poemId}`, {
            method: 'DELETE'
          })
        )
        
        await Promise.all(deletePromises)
        favoriteIds.value = []
        favoritePoems.value = []
        console.log('✅ 清空收藏完成')
        alert('已清空所有收藏')
      } catch (error) {
        console.error('❌ 清空收藏失败:', error)
        alert(`清空收藏失败: ${error.message}`)
      }
    }
  }

  // 🔧 新增：导出收藏
  const exportFavorites = () => {
    if (favoritePoems.value.length === 0) {
      alert('暂无收藏内容')
      return
    }
    
    try {
      const exportData = {
        exportTime: new Date().toLocaleString(),
        totalCount: favoritePoems.value.length,
        poems: favoritePoems.value.map(poem => ({
          title: poem.title,
          poet: poem.poet,
          text: poem.text,
          category: poem.category
        }))
      }
      
      const dataStr = JSON.stringify(exportData, null, 2)
      const dataBlob = new Blob([dataStr], { type: 'application/json;charset=utf-8' })
      const url = URL.createObjectURL(dataBlob)
      const link = document.createElement('a')
      link.href = url
      link.download = `我的诗词收藏_${new Date().toISOString().split('T')[0]}.json`
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      URL.revokeObjectURL(url)
      
      console.log('✅ 收藏导出完成')
    } catch (error) {
      console.error('❌ 导出收藏失败:', error)
      alert(`导出失败: ${error.message}`)
    }
  }

  return {
    favoriteIds,
    favoritePoems,
    loadFavorites,
    toggleFavorite,
    isFavorite,
    clearAllFavorites,
    exportFavorites
  }
}