<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useAuth } from './composables/useAuth'
import { useGameStats } from './composables/useGameStats'
import { useFavorites } from './composables/useFavorites'
import AuthModal from './components/AuthModal.vue'
import PasswordModal from './components/PasswordModal.vue'
import ScrollHint from './components/ScrollHint.vue'

// Store
const userStore = useUserStore()

// Composables
const {
  showAuthModal,
  isLoginMode,
  authLoading,
  authError,
  handleShowAuth,
  handleLogin,
  handleRegister,
  switchAuthMode,
  closeAuthModal,
  logout
} = useAuth(userStore)

const {
  gameStats,
  achievements,
  loadGameStats
} = useGameStats()

const {
  favoritePoems,
  loading: favoritesLoading,
  searchKeyword,
  sortBy,
  filteredFavorites,
  favoriteStats,
  initializeFavorites,
  removeFavorite,
  exportFavorites,
  getPreviewText,
  formatDate
} = useFavorites()

// 响应式数据
const currentScreen = ref(0)
const isScrolling = ref(false)
const showPasswordModal = ref(false)
const showScrollHint = ref(true)
const loading = ref(false)

// 默认头像
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

// 用户统计数据
const userStats = reactive({
  poemsRead: 0,
  daysActive: 0
})

// 计算属性
const isLoggedIn = computed(() => userStore.isAuthenticated)

const screenStyles = computed(() => ({
  userScreen: {
    transform: `translateY(${currentScreen.value * -100}vh)`
  },
  statsScreen: {
    transform: `translateY(${(1 - currentScreen.value) * 100}vh)`
  }
}))

// 方法
const handleWheel = (event) => {
  if (isScrolling.value) return
  
  event.preventDefault()
  const delta = event.deltaY
  
  if (delta > 0 && currentScreen.value === 0) {
    goToScreen(1)
  } else if (delta < 0 && currentScreen.value === 1) {
    goToScreen(0)
  }
}

const handleKeydown = (event) => {
  if (isScrolling.value) return
  
  switch (event.key) {
    case 'ArrowDown':
    case 'PageDown':
      event.preventDefault()
      if (currentScreen.value === 0) goToScreen(1)
      break
    case 'ArrowUp':
    case 'PageUp':
      event.preventDefault()
      if (currentScreen.value === 1) goToScreen(0)
      break
  }
}

const goToScreen = (screenIndex) => {
  if (isScrolling.value || currentScreen.value === screenIndex) return
  
  isScrolling.value = true
  currentScreen.value = screenIndex
  
  setTimeout(() => {
    isScrolling.value = false
  }, 800)
}

const showLogin = () => {
  handleShowAuth('login')
}

const showRegister = () => {
  handleShowAuth('register')
}

const handlePasswordChange = async (passwordData) => {
  loading.value = true
  
  try {
    // 这里应该调用修改密码的API
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    showPasswordModal.value = false
    alert('密码修改成功！')
  } catch (error) {
    console.error('修改密码失败:', error)
    alert('修改密码失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 🔥 优化的移除收藏方法
const handleRemoveFavorite = async (pid) => {
  const poem = favoritePoems.value.find(p => p.pid === pid)
  if (!poem) {
    alert('未找到该收藏')
    return
  }

  if (confirm(`确定要取消收藏《${poem.title}》吗？`)) {
    try {
      const result = await removeFavorite(pid)
      if (result.success) {
        console.log('✅ 取消收藏成功:', result.message)
        // 可以添加成功提示UI
      } else {
        alert(result.message)
      }
    } catch (error) {
      console.error('💥 取消收藏失败:', error)
      alert('取消收藏失败，请稍后重试')
    }
  }
}

// 🔥 优化的导出收藏方法
const handleExportFavorites = async () => {
  try {
    const result = exportFavorites()
    if (!result.success) {
      alert(result.message)
    }
  } catch (error) {
    console.error('💥 导出失败:', error)
    alert('导出失败，请稍后重试')
  }
}

const viewPoem = (poem) => {
  console.log('查看诗词:', poem)
  // 这里可以跳转到诗词详情页面或显示详情弹窗
  // router.push(`/poem/${poem.pid}`)
}

// 🔥 优化的刷新数据方法
const refreshUserData = async () => {
  loading.value = true
  
  try {
    console.log('🔄 开始刷新用户数据...')
    
    // 重新加载各种数据
    await Promise.all([
      loadGameStats(),
      initializeFavorites()
    ])
    
    // 更新用户统计
    userStats.poemsRead = Math.floor(Math.random() * 100) + 20
    userStats.daysActive = Math.floor(Math.random() * 90) + 10
    
    console.log('✅ 数据刷新完成')
    alert('数据已刷新！')
  } catch (error) {
    console.error('💥 刷新数据失败:', error)
    alert('刷新失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 生命周期
onMounted(async () => {
  console.log('🚀 [UserInfo] 组件开始初始化')
  console.log('👤 [UserInfo] 用户登录状态:', userStore.isAuthenticated)
  console.log('🆔 [UserInfo] 用户UID:', userStore.uid)
  
  // 确保用户状态初始化
  if (userStore && typeof userStore.initFromStorage === 'function') {
    userStore.initFromStorage()
  }
  
  // 初始化用户数据
  if (userStore.isAuthenticated) {
    try {
      await Promise.all([
        initializeFavorites(),
        loadGameStats()
      ])
      
      // 模拟用户统计数据
      userStats.poemsRead = Math.floor(Math.random() * 100) + 20
      userStats.daysActive = Math.floor(Math.random() * 90) + 10
      
      console.log('✅ [UserInfo] 已登录用户数据加载完成')
    } catch (error) {
      console.error('💥 [UserInfo] 加载用户数据失败:', error)
    }
  } else {
    // 未登录时也尝试加载本地收藏
    try {
      await initializeFavorites()
      console.log('✅ [UserInfo] 本地收藏数据加载完成')
    } catch (error) {
      console.error('💥 [UserInfo] 加载本地收藏失败:', error)
    }
  }
  
  // 5秒后隐藏滚动提示
  setTimeout(() => {
    showScrollHint.value = false
  }, 5000)
  
  // 让容器能够接收键盘事件
  document.querySelector('.profile-layout')?.focus()
  
  console.log('✅ [UserInfo] 组件初始化完成')
})

onUnmounted(() => {
  // 清理事件监听器
})
</script>

<template>
  <div class="profile-layout" @wheel="handleWheel" @keydown="handleKeydown" tabindex="0">
    <!-- 第一屏：用户资料 -->
    <div class="screen user-screen" :style="screenStyles.userScreen">
      <!-- 顶部标题 -->
      <header class="profile-header">
        <h1>个人中心</h1>
        <p class="subtitle">"诗意人生，书香致远"</p>
      </header>

      <main class="profile-container">
        <!-- 用户信息卡片 -->
        <div class="user-info-card">
          <!-- 未登录状态 -->
          <div v-if="!isLoggedIn" class="login-prompt">
            <div class="login-prompt-icon">👤</div>
            <h2>欢迎来到云舟词渡</h2>
            <p>登录后查看个人资料和收藏</p>
            <div class="login-prompt-buttons">
              <button @click="showLogin" class="auth-prompt-btn login">
                🔑 立即登录
              </button>
              <button @click="showRegister" class="auth-prompt-btn register">
                📝 注册账号
              </button>
            </div>
          </div>

          <!-- 已登录状态 -->
          <div v-else class="user-profile">
            <!-- 用户头像 -->
            <div class="avatar-section">
              <div class="avatar-wrapper">
                <img 
                  :src="userStore.avatar || defaultAvatar" 
                  :alt="userStore.username" 
                  class="user-avatar"
                >
              </div>
              <div class="user-details">
                <h3 class="username">{{ userStore.username }}</h3>
                <p class="user-id">ID: {{ userStore.uid }}</p>
              </div>
            </div>

            <!-- 用户统计 -->
            <div class="user-stats">
              <div class="stat-item">
                <span class="stat-number">{{ userStats.poemsRead }}</span>
                <span class="stat-label">阅读诗词</span>
              </div>
              <div class="stat-item">
                <span class="stat-number">{{ favoriteStats.total }}</span>
                <span class="stat-label">收藏数量</span>
              </div>
              <div class="stat-item">
                <span class="stat-number">{{ userStats.daysActive }}</span>
                <span class="stat-label">活跃天数</span>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="action-buttons">
              <button @click="showPasswordModal = true" class="action-btn secondary">
                🔐 修改密码
              </button>
              <button @click="refreshUserData" class="action-btn secondary" :disabled="loading">
                🔄 刷新数据
              </button>
              <button @click="handleExportFavorites" class="action-btn secondary" :disabled="favoriteStats.total === 0">
                📤 导出收藏
              </button>
              <button @click="logout" class="action-btn logout">
                👋 退出登录
              </button>
            </div>
          </div>
        </div>

        <!-- 收藏区域 -->
        <div class="favorites-section">
          <div class="section-header">
            <h3>我的收藏 
              <span v-if="favoritesLoading" class="loading-indicator">🔄</span>
              <span v-else class="count-badge">({{ favoriteStats.total }})</span>
            </h3>
            <div class="controls">
              <input
                v-model="searchKeyword"
                type="text"
                placeholder="搜索收藏的诗词..."
                class="search-input"
              >
              <select v-model="sortBy" class="sort-select">
                <option value="time">收藏时间</option>
                <option value="poet">诗人</option>
                <option value="title">标题</option>
              </select>
            </div>
          </div>

          <!-- 加载状态 -->
          <div v-if="favoritesLoading" class="loading-state">
            <div class="loading-spinner">🔄</div>
            <p>正在加载收藏列表...</p>
          </div>

          <!-- 空状态 -->
          <div v-else-if="filteredFavorites.length === 0" class="empty-state">
            <div class="empty-icon">📚</div>
            <p>{{ favoritePoems.length === 0 ? '还没有收藏任何诗词' : '没有找到相关诗词' }}</p>
            <p v-if="!isLoggedIn" class="empty-hint">登录后可以同步服务器收藏</p>
          </div>

          <!-- 收藏列表 -->
          <div v-else class="favorites-list">
            <div
              v-for="poem in filteredFavorites"
              :key="poem.pid"
              class="favorite-item"
              @click="viewPoem(poem)"
            >
              <h4 class="poem-title">{{ poem.title || '无题' }}</h4>
              <p class="poem-poet">{{ poem.poet || '佚名' }}</p>
              <p class="poem-preview">{{ getPreviewText(poem.text) }}</p>
              <div class="poem-meta">
                <span class="favorite-time">{{ formatDate(poem.favoriteTime) }}</span>
                <button
                  class="remove-btn"
                  @click.stop="handleRemoveFavorite(poem.pid)"
                  title="取消收藏"
                >
                  ❌
                </button>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>

    <!-- 第二屏：游戏统计 -->
    <div class="screen stats-screen" :style="screenStyles.statsScreen">
      <!-- 游戏统计内容保持不变 -->
      <!-- ... existing game stats content ... -->
    </div>

    <!-- 认证弹窗 -->
    <AuthModal
      v-if="showAuthModal"
      :is-login-mode="isLoginMode"
      :loading="authLoading"
      :error="authError"
      @close="closeAuthModal"
      @login="handleLogin"
      @register="handleRegister"
      @switch-mode="switchAuthMode"
    />

    <!-- 密码修改弹窗 -->
    <PasswordModal
      v-if="showPasswordModal"
      @close="showPasswordModal = false"
      @confirm="handlePasswordChange"
    />

    <!-- 滚动提示 -->
    <ScrollHint
      :current-screen="currentScreen"
      :total-screens="2"
      :show="showScrollHint"
    />
  </div>
</template>

<style scoped>
@import './styles/userinfo.scss';
@import './styles/modals.scss';

/* 新增样式 */
.loading-indicator {
  display: inline-block;
  animation: spin 1s linear infinite;
}

.count-badge {
  color: #8c7853;
  font-weight: 500;
}

.loading-state {
  text-align: center;
  padding: 2rem;
  color: #666;
}

.loading-spinner {
  font-size: 2rem;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

.empty-hint {
  font-size: 0.9rem;
  color: #999;
  margin-top: 0.5rem;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>