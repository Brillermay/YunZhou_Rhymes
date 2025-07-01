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
                  @error="handleAvatarError"
                >
              </div>
              <div class="user-details">
                <h3 class="username">{{ userStore.username }}</h3>
                <p class="user-id">ID: {{ userStore.uid }}</p>
                <p class="join-date">加入时间: {{ formatDate(userStore.createTime) }}</p>
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
                <option value="dynasty">朝代</option>
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
              <span class="overview-label">涉及朝代</span>
              <span class="overview-value">{{ favoriteStats.dynasties }} 个</span>
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
      <header class="stats-header">
        <h1>游戏统计</h1>
        <p class="subtitle">"在诗词中游戏，在游戏中学习"</p>
      </header>

      <main class="stats-container">
        <!-- 总览统计 -->
        <div class="stats-overview">
          <div class="stat-card primary">
            <div class="stat-icon">🎮</div>
            <div class="stat-info">
              <span class="stat-number">{{ gameStats.totalGames }}</span>
              <span class="stat-label">总游戏次数</span>
            </div>
          </div>
          
          <div class="stat-card success">
            <div class="stat-icon">🏆</div>
            <div class="stat-info">
              <span class="stat-number">{{ gameStats.winRate }}%</span>
              <span class="stat-label">胜率</span>
            </div>
          </div>
          
          <div class="stat-card warning">
            <div class="stat-icon">⭐</div>
            <div class="stat-info">
              <span class="stat-number">{{ gameStats.highestScore }}</span>
              <span class="stat-label">最高分</span>
            </div>
          </div>
          
          <div class="stat-card info">
            <div class="stat-icon">🏅</div>
            <div class="stat-info">
              <span class="stat-number">{{ unlockedAchievements.length }}/{{ achievements.length }}</span>
              <span class="stat-label">已解锁成就</span>
            </div>
          </div>
        </div>

        <!-- 游戏详细统计 -->
        <div class="game-details">
          <div class="detail-section">
            <h3>📈 游戏表现</h3>
            <div class="detail-grid">
              <div class="detail-item">
                <span class="detail-label">连胜记录</span>
                <span class="detail-value">{{ gameStats.bestStreak || 0 }} 局</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">平均得分</span>
                <span class="detail-value">{{ gameStats.averageScore || 0 }} 分</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">游戏时长</span>
                <span class="detail-value">{{ formatGameTime(gameStats.totalTime) }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">最快答题</span>
                <span class="detail-value">{{ gameStats.fastestAnswer || 0 }} 秒</span>
              </div>
            </div>
          </div>

          <div class="detail-section">
            <h3>🎯 知识掌握</h3>
            <div class="knowledge-stats">
              <div class="knowledge-item">
                <span class="knowledge-label">诗词识别</span>
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: `${gameStats.poemRecognition || 0}%` }"></div>
                </div>
                <span class="knowledge-percent">{{ gameStats.poemRecognition || 0 }}%</span>
              </div>
              <div class="knowledge-item">
                <span class="knowledge-label">作者匹配</span>
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: `${gameStats.authorMatching || 0}%` }"></div>
                </div>
                <span class="knowledge-percent">{{ gameStats.authorMatching || 0 }}%</span>
              </div>
              <div class="knowledge-item">
                <span class="knowledge-label">朝代判断</span>
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: `${gameStats.dynastyKnowledge || 0}%` }"></div>
                </div>
                <span class="knowledge-percent">{{ gameStats.dynastyKnowledge || 0 }}%</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 成就系统 -->
        <div class="achievements-section">
          <div class="achievements-header">
            <h3>🏆 成就系统</h3>
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

        <!-- 刷新按钮 -->
        <div class="stats-actions">
          <button @click="refreshGameStats" class="refresh-btn" :disabled="statsLoading">
            {{ statsLoading ? '🔄 刷新中...' : '🔄 刷新游戏数据' }}
          </button>
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

const refreshUserData = async () => {
  loading.value = true
  
  try {
    console.log('🔄 开始刷新用户数据...')
    
    await Promise.all([
      loadGameStats(),
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

const refreshGameStats = async () => {
  try {
    await loadGameStats()
    console.log('✅ 游戏数据刷新完成')
  } catch (error) {
    console.error('💥 刷新游戏数据失败:', error)
    alert('刷新游戏数据失败，请稍后重试')
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

// 生命周期
onMounted(async () => {
  console.log('🚀 [UserInfo] 组件开始初始化')
  
  // 确保用户状态初始化
  if (userStore && typeof userStore.initFromStorage === 'function') {
    userStore.initFromStorage()
  }
  
  // 初始化数据
  try {
    await Promise.all([
      initializeFavorites(),
      loadGameStats()
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
  padding: 2rem;
  box-sizing: border-box;
}

.user-screen {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stats-screen {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.profile-header, .stats-header {
  text-align: center;
  margin-bottom: 2rem;
  color: white;
}

.profile-header h1, .stats-header h1 {
  font-size: 3rem;
  margin: 0;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
}

.subtitle {
  font-size: 1.2rem;
  opacity: 0.9;
  margin: 0.5rem 0 0 0;
}

.profile-container, .stats-container {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  gap: 2rem;
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
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
  border: 4px solid #e5e7eb;
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
  padding: 0.5rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
}

.search-input {
  width: 200px;
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

/* 游戏统计样式 */
.stats-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: rgba(255,255,255,0.95);
  border-radius: 15px;
  padding: 2rem;
  display: flex;
  align-items: center;
  gap: 1.5rem;
  box-shadow: 0 8px 25px rgba(0,0,0,0.1);
  backdrop-filter: blur(10px);
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-card.primary { border-left: 5px solid #4f46e5; }
.stat-card.success { border-left: 5px solid #10b981; }
.stat-card.warning { border-left: 5px solid #f59e0b; }
.stat-card.info { border-left: 5px solid #06b6d4; }

.stat-icon {
  font-size: 2.5rem;
}

.stat-info {
  display: flex;
  flex-direction: column;
}

.stat-card .stat-number {
  font-size: 2rem;
  font-weight: bold;
  color: #333;
  margin: 0;
}

.stat-card .stat-label {
  color: #666;
  font-size: 1rem;
}

.game-details {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 2rem;
  margin-bottom: 2rem;
}

.detail-section {
  background: rgba(255,255,255,0.95);
  border-radius: 15px;
  padding: 2rem;
  box-shadow: 0 8px 25px rgba(0,0,0,0.1);
  backdrop-filter: blur(10px);
}

.detail-section h3 {
  color: #333;
  margin: 0 0 1.5rem 0;
  font-size: 1.3rem;
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
  padding: 2rem;
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

.refresh-btn {
  padding: 1rem 2rem;
  background: rgba(255,255,255,0.9);
  border: 2px solid #4f46e5;
  border-radius: 10px;
  color: #4f46e5;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.refresh-btn:hover:not(:disabled) {
  background: #4f46e5;
  color: white;
  transform: translateY(-2px);
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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
  
  .stats-overview {
    grid-template-columns: 1fr;
  }
  
  .game-details {
    grid-template-columns: 1fr;
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