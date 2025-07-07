<template>
  <div class="profile-layout" @wheel="handleWheel" @keydown="handleKeydown" tabindex="0">
    <!-- 第一屏：用户资料 -->
    <div class="screen user-screen" :style="screenStyles.userScreen">
      <!-- 顶部标题 -->

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
              <div class="avatar-wrapper" @click="showProfileModal = true" title="点击编辑个人资料">
                <img 
                  :src="userStore.userInfo?.avatar || defaultAvatar" 
                  :alt="userStore.userInfo?.username || '用户头像'" 
                  class="user-avatar"
                  @error="handleAvatarError"
                >
                <div class="avatar-edit-overlay">
                  <span>✏️</span>
                </div>
              </div>
              <div class="user-details">
                <h3 class="username">{{ userStore.userInfo?.username || userStore.username }}</h3>
                <p class="user-id">ID: {{ userStore.userInfo?.uid || userStore.uid }}</p>
                <p class="join-date">加入时间: {{ formatDate(userStore.userInfo?.createTime || userStore.createTime) }}</p>
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
              <div class="stat-item">
                <span class="stat-number">{{ favoriteStats.poets }}</span>
                <span class="stat-label">收藏诗人</span>
              </div>
            </div>

            <!-- 操作按钮 -->
            <div class="action-buttons">
              <button @click="showPasswordModal = true" class="action-btn secondary">
                🔐 修改密码
              </button>
              <button @click="showProfileModal = true" class="action-btn secondary">
                ✏️ 编辑资料
              </button>
              <button @click="refreshUserData" class="action-btn secondary" :disabled="loading">
                {{ loading ? '🔄 刷新中...' : '🔄 刷新数据' }}
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

          <!-- 收藏统计概览 -->
          <div v-if="favoriteStats.total > 0" class="favorites-overview">
            <div class="overview-item">
              <span class="overview-label">收藏诗人</span>
              <span class="overview-value">{{ favoriteStats.poets }} 位</span>
            </div>

            <div class="overview-item">
              <span class="overview-label">平均长度</span>
              <span class="overview-value">{{ favoriteStats.avgLength }} 字</span>
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
            <p v-else-if="searchKeyword" class="empty-hint">
              尝试使用其他关键词搜索，或者 
              <button @click="searchKeyword = ''" class="link-btn">清空搜索条件</button>
            </p>
          </div>

          <!-- 收藏列表 -->
          <div v-else class="favorites-list">
            <div
              v-for="poem in filteredFavorites"
              :key="poem.pid"
              class="favorite-item"
              @click="viewPoem(poem)"
            >
              <div class="poem-header">
                <h4 class="poem-title">{{ poem.title || '无题' }}</h4>
                <button
                  class="remove-btn"
                  @click.stop="handleRemoveFavorite(poem.pid)"
                  title="取消收藏"
                >
                  ❌
                </button>
              </div>
              <div class="poem-info">
                <p class="poem-poet">{{ poem.poet || '佚名' }} 
                  <span v-if="poem.dynasty" class="poem-dynasty">· {{ poem.dynasty }}</span>
                </p>
                <span v-if="poem.category" class="poem-category">{{ poem.category }}</span>
              </div>
              <p class="poem-preview">{{ getPreviewText(poem.text) }}</p>
              <div class="poem-meta">
                <span class="favorite-time">收藏于 {{ formatDate(poem.favoriteTime) }}</span>
                <span class="poem-source">{{ poem.source === 'server' ? '☁️ 云端' : '📱 本地' }}</span>
              </div>
            </div>
          </div>
        </div>
      </main>
    </div>

    <!-- 第二屏：游戏统计 -->
    <div class="screen stats-screen" :style="screenStyles.statsScreen">
 、

      <main class="stats-container">
        
  

        <!-- 成就系统 -->
        <div class="achievements-section">
          <div class="achievements-header">
            <h3>🏆 成就系统</h3>
            <div class="achievement-controls">+
              <div class="achievement-progress">
                <span>{{ unlockedAchievements.length }}/{{ achievements.length }} 已解锁</span>
                <div class="achievement-progress-bar">
                  <div 
                    class="achievement-progress-fill" 
                    :style="{ width: `${(unlockedAchievements.length / achievements.length) * 100}%` }"
                  ></div>
                </div>
              </div>
            </div>
          </div>

          <div class="achievements-grid">
            <div
              v-for="achievement in achievements"
              :key="achievement.id"
              class="achievement-item"
              :class="{ 
                unlocked: achievement.unlocked,
                'nearly-unlocked': !achievement.unlocked && achievement.progress / achievement.target > 0.8
              }"
            >
              <div class="achievement-icon">{{ achievement.icon }}</div>
              <div class="achievement-info">
                <h4 class="achievement-name">{{ achievement.name }}</h4>
                <p class="achievement-description">{{ achievement.description }}</p>
                
                <!-- 未解锁成就的进度 -->
                <div v-if="!achievement.unlocked && achievement.target" class="achievement-progress-section">
                  <div class="progress-bar small">
                    <div 
                      class="progress-fill" 
                      :style="{ width: `${Math.min((achievement.progress / achievement.target) * 100, 100)}%` }"
                    ></div>
                  </div>
                  <span class="progress-text">{{ achievement.progress }}/{{ achievement.target }}</span>
                </div>
                
                <!-- 已解锁成就的时间 -->
                <div v-if="achievement.unlocked" class="achievement-unlocked">
                  <span class="unlock-badge">✅ 已解锁</span>
                  <span class="unlock-date">{{ formatDate(achievement.unlockedAt) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>


      </main>
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

    <!-- 个人资料编辑弹窗 -->
    <ProfileModal
      v-if="showProfileModal"
      :user-info="userStore.userInfo"
      @close="showProfileModal = false"
      @confirm="handleProfileUpdate"
    />

    <!-- 滚动提示 -->
    <ScrollHint
      :current-screen="currentScreen"
      :total-screens="2"
      :show="showScrollHint"
    />

    <!-- 诗词详情弹窗 -->
    <div v-if="selectedPoem" class="poem-modal-overlay" @click="closeModal">
      <div class="poem-modal" @click.stop>
        <div class="poem-modal-header">
          <h3>{{ selectedPoem.title || '无题' }}</h3>
          <button @click="closeModal" class="close-btn">✕</button>
        </div>
        <div class="poem-modal-content">
          <div class="poem-meta-info">
            <p><strong>作者：</strong>{{ selectedPoem.poet || '佚名' }}</p>
            <p v-if="selectedPoem.dynasty"><strong>朝代：</strong>{{ selectedPoem.dynasty }}</p>
            <p v-if="selectedPoem.category"><strong>分类：</strong>{{ selectedPoem.category }}</p>
          </div>
          <div class="poem-text">
            {{ selectedPoem.text }}
          </div>
          <div class="poem-actions">
            <button @click="handleRemoveFavorite(selectedPoem.pid)" class="action-btn danger">
              取消收藏
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { useUserStore } from '@/stores/user'
import { useAuth } from './composables/useAuth'
import { useGameStats } from './composables/useGameStats'
import { useFavorites } from './composables/useFavorites'
import AuthModal from './components/AuthModal.vue'
import PasswordModal from './components/PasswordModal.vue'
import ProfileModal from './components/ProfileModal.vue'
import ScrollHint from './components/ScrollHint.vue'
import API_BASE_URL from '@/config/api'
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
  loadGameStats,
  loading: statsLoading
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
const showProfileModal = ref(false)
const showScrollHint = ref(true)
const loading = ref(false)
const selectedPoem = ref(null)

// 默认头像
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

// 用户统计数据
const userStats = reactive({
  poemsRead: 0,
  daysActive: 0
})

// 计算属性
const isLoggedIn = computed(() => userStore.isAuthenticated)

const unlockedAchievements = computed(() => 
  achievements.value.filter(a => a.unlocked)
)

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
  
  // 🔧 新增：检测是否在需要内部滚动的容器内
  const scrollableContainers = [
    '.favorites-list',        // 收藏列表
    '.modal-overlay',         // 弹窗遮罩
    '.poem-modal-overlay',    // 诗词详情弹窗
    '.poem-modal-content',    // 诗词详情内容
    '.achievements-grid',     // 成就网格
    '.profile-modal-content', // 个人资料弹窗
    '.auth-modal-content'     // 认证弹窗
  ]
  
  // 检测事件是否来自这些容器
  const isInScrollableContainer = scrollableContainers.some(selector => 
    event.target.closest(selector)
  )
  
  // 如果在滚动容器内，不执行翻页
  if (isInScrollableContainer) return
  
  // 原有的翻页逻辑保持不变
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
    case 'Escape':
      if (selectedPoem.value) closeModal()
      break
  }
}

const goToScreen = (screenIndex) => {
  if (isScrolling.value || currentScreen.value === screenIndex) return
  
  isScrolling.value = true
  currentScreen.value = screenIndex
  
  // 当切换到游戏统计页面时，自动刷新游戏数据
  if (screenIndex === 1) {
    autoRefreshGameStats()
  }
  
  setTimeout(() => {
    isScrolling.value = false
  }, 800)
}

// 自动刷新游戏数据
const autoRefreshGameStats = async () => {
  try {
    console.log('🔄 自动刷新游戏数据...')
    await loadGameStats()
    console.log('✅ 游戏数据刷新完成')
  } catch (error) {
    console.error('💥 自动刷新游戏数据失败:', error)
    // 这里不显示错误弹窗，因为是自动刷新，静默失败
  }
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
    // TODO: 调用修改密码的API
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

// 修改第542行左右的 handleProfileUpdate 方法

const handleProfileUpdate = async (profileData) => {
  loading.value = true
  
  try {
    console.log('🔄 开始更新个人资料...', profileData)
    
    // 如果头像有变化，先更新头像
    if (profileData.avatar && profileData.avatar !== userStore.userInfo?.avatar) {
      console.log('🔄 更新头像...')
      const avatarResponse = await fetch(`${API_BASE_URL}/user/updateAvatar`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          uid: profileData.uid,
          avatar: profileData.avatar
        })
      })
      
      const avatarResult = await avatarResponse.json()
      if (!avatarResult.success) {
        throw new Error(avatarResult.message || '头像更新失败')
      }
      
      console.log('✅ 头像更新成功')
    }
    
    // 获取更新后的用户完整信息
    const profileResponse = await fetch(`${API_BASE_URL}/user/profile/${profileData.uid}`)
    const profileResult = await profileResponse.json()
    
    if (profileResult.success) {
      // 🔧 修复：使用 store 的 updateUserInfo 方法
      userStore.updateUserInfo({
        nickname: profileResult.nickname,
        email: profileResult.email,
        avatar: profileResult.avatar,
        createTime: profileResult.createTime,
        isAdmin: profileResult.isAdmin
      })
      
      console.log('✅ 个人资料更新成功')
      alert('个人资料更新成功！')
      showProfileModal.value = false
    } else {
      throw new Error(profileResult.message || '获取用户信息失败')
    }
    
  } catch (error) {
    console.error('💥 个人资料更新失败:', error)
    alert('个人资料更新失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const handleAvatarError = (event) => {
  event.target.src = defaultAvatar
}

const handleRemoveFavorite = async (pid) => {
  const poem = favoritePoems.value.find(p => p.pid == pid)
  if (!poem) {
    alert('未找到该收藏')
    return
  }

  if (confirm(`确定要取消收藏《${poem.title}》吗？`)) {
    try {
      const result = await removeFavorite(pid)
      if (result.success) {
        console.log('✅ 取消收藏成功:', result.message)
        if (selectedPoem.value && selectedPoem.value.pid == pid) {
          closeModal()
        }
      } else {
        alert(result.message)
      }
    } catch (error) {
      console.error('💥 取消收藏失败:', error)
      alert('取消收藏失败，请稍后重试')
    }
  }
}

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
  selectedPoem.value = poem
}

const closeModal = () => {
  selectedPoem.value = null
}

// 🔧 新增：刷新成就方法
const refreshAchievements = async () => {
  if (!userStore.isAuthenticated || !userStore.uid) {
    alert('请先登录再刷新成就')
    return
  }
  
  try {
    console.log('🔄 手动刷新成就数据...')
    await loadGameStats()
    console.log('✅ 成就数据刷新完成')
  } catch (error) {
    console.error('💥 刷新成就失败:', error)
    alert('刷新成就失败，请稍后重试')
  }
}

// 🔧 修改原有的 refreshUserData 方法，确保也刷新成就
const refreshUserData = async () => {
  loading.value = true
  
  try {
    console.log('🔄 开始刷新用户数据...')
    
    await Promise.all([
      loadGameStats(),  // 这里会自动获取真实成就数据
      initializeFavorites()
    ])
    
    // 更新用户统计
    userStats.poemsRead = Math.floor(Math.random() * 100) + 20
    userStats.daysActive = Math.floor(Math.random() * 90) + 10
    
    console.log('✅ 数据刷新完成')
  } catch (error) {
    console.error('💥 刷新数据失败:', error)
    alert('刷新失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

const formatGameTime = (seconds) => {
  if (!seconds) return '0分钟'
  
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  
  if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  } else {
    return `${minutes}分钟`
  }
}

// 监听登录状态变化，自动刷新数据
watch(isLoggedIn, (newVal) => {
  if (newVal) {
    // 用户登录后自动刷新所有数据
    console.log('🔄 用户登录，自动刷新所有数据...')
    refreshUserData()
  }
}, { immediate: false })

// 生命周期
onMounted(async () => {
  console.log('🚀 [UserInfo] 组件开始初始化')
  
  // 确保用户状态初始化
  if (userStore && typeof userStore.initFromStorage === 'function') {
    userStore.initFromStorage()
  }
  
  // 初始化数据 - 自动刷新游戏数据
  try {
    await Promise.all([
      initializeFavorites(),
      loadGameStats() // 组件挂载时自动加载游戏数据
    ])
    
    // 模拟用户统计数据
    userStats.poemsRead = Math.floor(Math.random() * 100) + 20
    userStats.daysActive = Math.floor(Math.random() * 90) + 10
    
    console.log('✅ [UserInfo] 数据加载完成')
  } catch (error) {
    console.error('💥 [UserInfo] 数据加载失败:', error)
  }
  
  // 5秒后隐藏滚动提示
  setTimeout(() => {
    showScrollHint.value = false
  }, 5000)
  
  // 让容器能够接收键盘事件
  document.querySelector('.profile-layout')?.focus()
})

onUnmounted(() => {
  // 清理
})
</script>

<style scoped>
@import './styles/userinfo.scss';

/* 覆盖和新增样式 */
.profile-layout {
  height: 100vh;
  overflow: hidden;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
  position: relative;
  outline: none;
}

.screen {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100vh;
  transition: transform 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  overflow-y: auto;
  padding: 1rem 2rem;
  box-sizing: border-box;
}

.user-screen,.stats-screen {
  background: rgba(245, 239, 230, 0.98);
}




.profile-container, .stats-container {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  gap: 1rem;
}

.profile-container {
  grid-template-columns: 1fr 2fr;
}

.stats-container {
  grid-template-columns: 1fr;
}

/* 用户信息卡片 */
.user-info-card {
  background: rgba(255,255,255,0.95);
  border-radius: 20px;
  padding: 2rem;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
  backdrop-filter: blur(10px);
}

.login-prompt {
  text-align: center;
  padding: 2rem;
}

.login-prompt-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.login-prompt h2 {
  color: #333;
  margin-bottom: 0.5rem;
}

.login-prompt p {
  color: #666;
  margin-bottom: 2rem;
}

.login-prompt-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.auth-prompt-btn {
  padding: 0.8rem 1.5rem;
  border: none;
  border-radius: 10px;
  font-size: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
}

.auth-prompt-btn.login {
  background: #4f46e5;
  color: white;
}

.auth-prompt-btn.register {
  background: #10b981;
  color: white;
}

.auth-prompt-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.2);
}

/* 用户资料 */
.avatar-section {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
  transition: all 0.3s ease;
}

.avatar-wrapper:hover {
  transform: scale(1.05);
}

.avatar-wrapper:hover .avatar-edit-overlay {
  opacity: 1;
}

.avatar-edit-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 50%;
  color: white;
  font-size: 1.5rem;
}

.user-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.username {
  font-size: 1.5rem;
  color: #333;
  margin: 0 0 0.25rem 0;
}

.user-id, .join-date {
  color: #666;
  margin: 0.25rem 0;
  font-size: 0.9rem;
}

.user-stats {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
  margin-bottom: 2rem;
}

.stat-item {
  text-align: center;
  padding: 1rem;
  background: #f8fafc;
  border-radius: 10px;
}

.stat-number {
  display: block;
  font-size: 1.8rem;
  font-weight: bold;
  color: #4f46e5;
}

.stat-label {
  font-size: 0.9rem;
  color: #666;
}

.action-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0.5rem;
}

.action-btn {
  padding: 0.7rem;
  border: none;
  border-radius: 8px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
}

.action-btn.secondary {
  background: #f1f5f9;
  color: #475569;
}

.action-btn.logout {
  background: #ef4444;
  color: white;
  grid-column: span 2;
}

.action-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}

.action-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 收藏区域 */
.favorites-section {
  background: rgba(255,255,255,0.95);
  border-radius: 20px;
  padding: 2rem;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
  backdrop-filter: blur(10px);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.section-header h3 {
  color: #333;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.loading-indicator {
  animation: spin 1s linear infinite;
}

.count-badge {
  color: #8c7853;
  font-weight: 500;
}

.controls {
  display: flex;
  gap: 0.5rem;
}

.search-input, .sort-select {
  padding: 0.6rem 1rem;
  border: 2px solid rgba(140, 120, 83, 0.3);
  border-radius: 8px;
  font-size: 0.9rem;
  background: rgba(255, 255, 255, 0.9);
  transition: all 0.3s ease;
  color: #333;
}

.search-input:focus, .sort-select:focus {
  outline: none;
  border-color: #8c7853;
  background: white;
  box-shadow: 0 0 0 3px rgba(140, 120, 83, 0.1);
}

.search-input::placeholder {
  color: rgba(140, 120, 83, 0.6);
}

.search-input {
  width: 200px;
}

.sort-select {
  cursor: pointer;
}

.favorites-overview {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding: 1rem;
  background: #f8fafc;
  border-radius: 10px;
}

.overview-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
}

.overview-label {
  font-size: 0.8rem;
  color: #666;
}

.overview-value {
  font-weight: bold;
  color: #333;
}

.loading-state, .empty-state {
  text-align: center;
  padding: 3rem 2rem;
  color: #666;
}

.loading-spinner, .empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
}

.loading-spinner {
  animation: spin 1s linear infinite;
}

.empty-hint {
  font-size: 0.9rem;
  color: #999;
  margin-top: 0.5rem;
}

.link-btn {
  background: none;
  border: none;
  color: #4f46e5;
  cursor: pointer;
  text-decoration: underline;
}

.favorites-list {
  display: grid;
  gap: 1rem;
  max-height: 400px;
  overflow-y: auto;
}

.favorite-item {
  padding: 1.5rem;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
}

.favorite-item:hover {
  border-color: #4f46e5;
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.1);
  transform: translateY(-2px);
}

.poem-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.5rem;
}

.poem-title {
  color: #333;
  margin: 0;
  font-size: 1.1rem;
}

.remove-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.25rem;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.remove-btn:hover {
  background: rgba(239, 68, 68, 0.1);
}

.poem-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}

.poem-poet {
  color: #666;
  margin: 0;
  font-size: 0.9rem;
}

.poem-dynasty {
  color: #999;
}

.poem-category {
  background: #e0e7ff;
  color: #4338ca;
  padding: 0.2rem 0.5rem;
  border-radius: 4px;
  font-size: 0.75rem;
}

.poem-preview {
  color: #555;
  margin: 0 0 1rem 0;
  line-height: 1.5;
  font-size: 0.95rem;
}

.poem-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.8rem;
  color: #999;
}

.poem-source {
  font-weight: 500;
}




.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  background: #f8fafc;
  border-radius: 8px;
}

.detail-label {
  color: #666;
  font-size: 0.9rem;
}

.detail-value {
  font-weight: bold;
  color: #333;
}

.knowledge-stats {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.knowledge-item {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.knowledge-label {
  min-width: 80px;
  color: #666;
  font-size: 0.9rem;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
}

.progress-bar.small {
  height: 6px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #4f46e5, #06b6d4);
  transition: width 0.3s ease;
}

.knowledge-percent {
  min-width: 40px;
  text-align: right;
  font-weight: bold;
  color: #333;
  font-size: 0.9rem;
}

.achievements-section {
  background: rgba(255,255,255,0.95);
  border-radius: 15px;
  padding: 1.5rem;
  box-shadow: 0 8px 25px rgba(0,0,0,0.1);
  backdrop-filter: blur(10px);
  margin-bottom: 2rem;
}

.achievements-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.achievements-header h3 {
  color: #333;
  margin: 0;
  font-size: 1.3rem;
}

.achievement-progress {
  display: flex;
  align-items: center;
  gap: 1rem;
  color: #666;
}

.achievement-progress-bar {
  width: 150px;
  height: 8px;
  background: #e5e7eb;
  border-radius: 4px;
  overflow: hidden;
}

.achievement-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #f59e0b, #10b981);
  transition: width 0.3s ease;
}

.achievements-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

.achievement-item {
  display: flex;
  gap: 1rem;
  padding: 1.5rem;
  border: 2px solid #e5e7eb;
  border-radius: 12px;
  transition: all 0.3s ease;
  background: white;
}

.achievement-item.unlocked {
  border-color: #10b981;
  background: #f0fdf4;
}

.achievement-item.nearly-unlocked {
  border-color: #f59e0b;
  background: #fffbeb;
}

.achievement-icon {
  font-size: 2rem;
  align-self: flex-start;
}

.achievement-info {
  flex: 1;
}

.achievement-name {
  color: #333;
  margin: 0 0 0.5rem 0;
  font-size: 1rem;
}

.achievement-description {
  color: #666;
  margin: 0 0 1rem 0;
  font-size: 0.9rem;
  line-height: 1.4;
}

.achievement-progress-section {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.progress-text {
  font-size: 0.8rem;
  color: #666;
  white-space: nowrap;
}

.achievement-unlocked {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.unlock-badge {
  color: #10b981;
  font-weight: bold;
  font-size: 0.8rem;
}

.unlock-date {
  color: #999;
  font-size: 0.8rem;
}

.stats-actions {
  text-align: center;
}


/* 诗词详情弹窗 */
.poem-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0,0,0,0.7);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.poem-modal {
  background: white;
  border-radius: 15px;
  width: 90%;
  max-width: 600px;
  max-height: 80vh;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0,0,0,0.3);
}

.poem-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  background: #f8fafc;
  border-bottom: 1px solid #e5e7eb;
}

.poem-modal-header h3 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #666;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background-color 0.2s;
}

.close-btn:hover {
  background: #e5e7eb;
}

.poem-modal-content {
  padding: 2rem;
  overflow-y: auto;
  max-height: 60vh;
}

.poem-meta-info {
  margin-bottom: 1.5rem;
}

.poem-meta-info p {
  margin: 0.5rem 0;
  color: #666;
}

.poem-text {
  background: #f8fafc;
  padding: 1.5rem;
  border-radius: 8px;
  line-height: 1.8;
  color: #333;
  font-size: 1.1rem;
  white-space: pre-line;
  margin-bottom: 1.5rem;
}

.poem-actions {
  text-align: center;
}

.action-btn.danger {
  background: #ef4444;
  color: white;
  padding: 0.8rem 1.5rem;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn.danger:hover {
  background: #dc2626;
  transform: translateY(-1px);
}

.achievement-controls {
  display: flex;
  align-items: center;
  gap: 1rem;
}


.achievements-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  gap: 1rem;
}

/* 动画 */
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-container {
    grid-template-columns: 1fr;
  }
  
  .user-stats {
    grid-template-columns: repeat(2, 1fr);
  }
  
  

  .detail-grid {
    grid-template-columns: 1fr;
  }
  
  .achievements-grid {
    grid-template-columns: 1fr;
  }
  
  .screen {
    padding: 1rem;
  }
  
  .profile-header h1, .stats-header h1 {
    font-size: 2rem;
  }
}
</style>