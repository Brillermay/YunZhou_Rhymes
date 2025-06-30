<template>
    <div class="profile-layout" @wheel="handleWheel">
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
            <div v-else class="avatar-section">
              <!-- 用户头像 -->
              <div class="avatar-wrapper">
                <img
                    :src="userInfo.avatar || '/default-avatar.png'"
                    :alt="userInfo.username"
                    class="user-avatar"
                    @error="handleAvatarError"
                >
                <div class="avatar-border"></div>
              </div>

              <!-- 用户基本信息 -->
              <div class="user-details">
                <h2 class="username">{{ userInfo.username }}</h2>
                <p class="user-id">ID: {{ userInfo.uid }}</p>
                <div class="user-stats">
                  <div class="stat-item">
                    <span class="stat-number">{{ userStats.poemsRead }}</span>
                    <span class="stat-label">已读诗词</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-number">{{ userStats.favoriteCount }}</span>
                    <span class="stat-label">收藏诗词</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-number">{{ userStats.daysActive }}</span>
                    <span class="stat-label">活跃天数</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 操作按钮区域 -->
            <div v-if="isLoggedIn" class="action-buttons">
              <button class="action-btn primary" @click="showPasswordModal = true">
                🔒 修改密码
              </button>
              <button class="action-btn secondary" @click="editProfile">
                ✏️ 编辑资料
              </button>
              <button class="action-btn normal" @click="refreshData">
                🔄 刷新数据
              </button>
              <button class="action-btn logout" @click="logout">
                🚪 退出登录
              </button>
            </div>
          </div>
  
          <!-- 收藏诗词列表 -->
          <div class="favorites-section">
            <div class="section-header">
              <h3>我的收藏</h3>
              <div class="section-controls">
                <input 
                  v-model="searchKeyword" 
                  placeholder="搜索收藏的诗词..."
                  class="search-input"
                >
                <select v-model="sortBy" class="sort-select">
                  <option value="time">按时间排序</option>
                  <option value="poet">按诗人排序</option>
                  <option value="title">按标题排序</option>
                </select>
              </div>
            </div>
  
            <!-- 收藏列表 -->
            <div class="favorites-list" v-if="filteredFavorites.length > 0">
              <div 
                v-for="(poem, index) in filteredFavorites" 
                :key="poem.pid || index"
                class="favorite-item"
                @click="viewPoem(poem)"
              >
                <div class="poem-info">
                  <h4 class="poem-title">{{ poem.title || '未知标题' }}</h4>
                  <p class="poem-author">{{ poem.poet || '佚名' }} · {{ poem.category || '古诗' }}</p>
                  <p class="poem-preview">{{ getPreviewText(poem.text) }}</p>
                </div>
                <div class="favorite-actions">
                  <span class="favorite-time">{{ formatDate(poem.favoriteTime) }}</span>
                  <button 
                    @click.stop="removeFavorite(poem.pid)"
                    class="remove-btn"
                    title="取消收藏"
                  >
                    ❌
                  </button>
                </div>
              </div>
            </div>
  
            <!-- 空状态 -->
            <div v-else class="empty-state">
              <div class="empty-icon">📚</div>
              <p>还没有收藏任何诗词</p>
              <p>去发现一些美好的诗句吧！</p>
              <button @click="goToRecommend" class="goto-recommend-btn">
                去推荐页面
              </button>
            </div>
          </div>
        </main>
  
        <!-- 滚动提示 -->
        <div class="scroll-hint" v-show="currentScreen === 0">
          <div class="scroll-arrow">⬇</div>
          <p>向下滚动查看游戏统计</p>
        </div>
      </div>
  
      <!-- 第二屏：游戏统计 -->
      <div class="screen game-screen" :style="screenStyles.gameScreen">
  
        <main class="game-container">
          <!-- 游戏总览卡片 -->
          <div class="game-overview-card">
            <h3>游戏总览</h3>
            <div class="game-stats-grid">
              <div class="game-stat-item">
                <div class="stat-icon">🎮</div>
                <div class="stat-info">
                  <span class="stat-number">{{ gameStats.totalGames }}</span>
                  <span class="stat-label">总游戏次数</span>
                </div>
              </div>
              <div class="game-stat-item">
                <div class="stat-icon">🏆</div>
                <div class="stat-info">
                  <span class="stat-number">{{ gameStats.wins }}</span>
                  <span class="stat-label">胜利次数</span>
                </div>
              </div>
              <div class="game-stat-item">
                <div class="stat-icon">📈</div>
                <div class="stat-info">
                  <span class="stat-number">{{ gameStats.winRate }}%</span>
                  <span class="stat-label">胜率</span>
                </div>
              </div>
              <div class="game-stat-item">
                <div class="stat-icon">⚡</div>
                <div class="stat-info">
                  <span class="stat-number">{{ gameStats.highestScore }}</span>
                  <span class="stat-label">最高分</span>
                </div>
              </div>
            </div>
          </div>
  
          <!-- 各游戏详细统计 -->
          <div class="game-details-grid">
            <!-- 诗词测验统计 -->
            <div class="game-detail-card">
              <div class="card-header">
                <h4>📝 诗词测验</h4>
              </div>
              <div class="card-content">
                <div class="detail-row">
                  <span>参与次数：</span>
                  <span class="highlight">{{ gameDetails.quiz.attempts }}</span>
                </div>
                <div class="detail-row">
                  <span>正确率：</span>
                  <span class="highlight">{{ gameDetails.quiz.accuracy }}%</span>
                </div>
                <div class="detail-row">
                  <span>最佳成绩：</span>
                  <span class="highlight">{{ gameDetails.quiz.bestScore }}分</span>
                </div>
                <div class="detail-row">
                  <span>平均用时：</span>
                  <span class="highlight">{{ gameDetails.quiz.avgTime }}秒</span>
                </div>
              </div>
            </div>
  
            <!-- 飞花令统计 -->
            <div class="game-detail-card">
              <div class="card-header">
                <h4>🌸 飞花令</h4>
              </div>
              <div class="card-content">
                <div class="detail-row">
                  <span>对战次数：</span>
                  <span class="highlight">{{ gameDetails.feihua.battles }}</span>
                </div>
                <div class="detail-row">
                  <span>胜利场次：</span>
                  <span class="highlight">{{ gameDetails.feihua.wins }}</span>
                </div>
                <div class="detail-row">
                  <span>最长连胜：</span>
                  <span class="highlight">{{ gameDetails.feihua.maxStreak }}场</span>
                </div>
                <div class="detail-row">
                  <span>擅长主题：</span>
                  <span class="highlight">{{ gameDetails.feihua.bestTheme }}</span>
                </div>
              </div>
            </div>
  
            <!-- 诗词游戏统计 -->
            <div class="game-detail-card">
              <div class="card-header">
                <h4>🎯 诗词游戏</h4>
              </div>
              <div class="card-content">
                <div class="detail-row">
                  <span>游戏次数：</span>
                  <span class="highlight">{{ gameDetails.game.plays }}</span>
                </div>
                <div class="detail-row">
                  <span>通关次数：</span>
                  <span class="highlight">{{ gameDetails.game.completed }}</span>
                </div>
                <div class="detail-row">
                  <span>最高等级：</span>
                  <span class="highlight">{{ gameDetails.game.maxLevel }}级</span>
                </div>
                <div class="detail-row">
                  <span>收集诗词：</span>
                  <span class="highlight">{{ gameDetails.game.collected }}首</span>
                </div>
              </div>
            </div>
          </div>
  
          <!-- 成就系统 -->
          <div class="achievements-section">
            <h3>🏅 我的成就</h3>
            <div class="achievements-grid">
              <div 
                v-for="achievement in achievements" 
                :key="achievement.id"
                class="achievement-item"
                :class="{ 'unlocked': achievement.unlocked }"
              >
                <div class="achievement-icon">{{ achievement.icon }}</div>
                <div class="achievement-info">
                  <h5>{{ achievement.name }}</h5>
                  <p>{{ achievement.description }}</p>
                  <div class="achievement-progress" v-if="!achievement.unlocked">
                    <div class="progress-bar">
                      <div class="progress-fill" :style="{ width: achievement.progress + '%' }"></div>
                    </div>
                    <span class="progress-text">{{ achievement.progress }}%</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </main>
      </div>
  
      <!-- 页面指示器 -->
      <div class="screen-indicators">
        <div 
          class="indicator" 
          :class="{ 'active': currentScreen === 0 }"
          @click="goToScreen(0)"
        ></div>
        <div 
          class="indicator" 
          :class="{ 'active': currentScreen === 1 }"
          @click="goToScreen(1)"
        ></div>
      </div>
  
      <!-- 修改密码模态框 -->
      <div v-if="showPasswordModal" class="modal-overlay" @click="closePasswordModal">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <h3>修改密码</h3>
            <button @click="closePasswordModal" class="close-btn">×</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>当前密码</label>
              <input 
                v-model="passwordForm.current" 
                type="password" 
                placeholder="请输入当前密码"
                class="form-input"
              >
            </div>
            <div class="form-group">
              <label>新密码</label>
              <input 
                v-model="passwordForm.new" 
                type="password" 
                placeholder="请输入新密码"
                class="form-input"
              >
            </div>
            <div class="form-group">
              <label>确认新密码</label>
              <input 
                v-model="passwordForm.confirm" 
                type="password" 
                placeholder="请再次输入新密码"
                class="form-input"
              >
            </div>
          </div>
          <div class="modal-footer">
            <button @click="closePasswordModal" class="btn-cancel">取消</button>
            <button @click="changePassword" class="btn-confirm">确认修改</button>
          </div>
        </div>
      </div>

      <!-- 登录注册弹窗 -->
      <div v-if="showAuthModal" class="modal-overlay" @click="closeAuthModal">
        <div class="auth-modal-content" @click.stop>
          <!-- 弹窗头部 -->
          <div class="auth-modal-header">
            <h3>{{ isLoginMode ? '登录' : '注册' }}</h3>
            <button @click="closeAuthModal" class="close-btn">×</button>
          </div>

          <!-- 弹窗内容 -->
          <div class="auth-modal-body">
            <form @submit.prevent="handleAuth">
              <div class="form-group">
                <label>用户名</label>
                <input
                    v-model="authForm.username"
                    type="text"
                    placeholder="请输入用户名"
                    class="form-input"
                    required
                >
              </div>

              <div class="form-group">
                <label>密码</label>
                <input
                    v-model="authForm.password"
                    type="password"
                    placeholder="请输入密码"
                    class="form-input"
                    required
                >
              </div>

              <div v-if="!isLoginMode" class="form-group">
                <label>确认密码</label>
                <input
                    v-model="authForm.confirmPassword"
                    type="password"
                    placeholder="请再次输入密码"
                    class="form-input"
                    required
                >
              </div>

              <button
                  type="submit"
                  :disabled="authLoading"
                  class="auth-submit-btn"
              >
                {{ authLoading ? '处理中...' : (isLoginMode ? '登录' : '注册') }}
              </button>
            </form>

            <!-- 切换登录/注册 -->
            <div class="auth-switch">
        <span v-if="isLoginMode">
          还没有账号？
          <button @click="switchToRegister" class="switch-btn">立即注册</button>
        </span>
              <span v-else>
          已有账号？
          <button @click="switchToLogin" class="switch-btn">立即登录</button>
        </span>
            </div>

            <!-- 错误提示 -->
            <div v-if="authError" class="auth-error">
              {{ authError }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, reactive, computed, onMounted, onUnmounted } from 'vue';
  import { useRouter } from 'vue-router';
  import { useUserStore } from '@/stores/user';

  const router = useRouter();

  // 🔧 新增：使用 Pinia Store
  const userStore = useUserStore();

  // 🔧 修改：使用 Store 的状态而不是本地状态
  const isLoggedIn = computed(() => userStore.isAuthenticated);
  const showAuthModal = ref(false);
  const isLoginMode = ref(true);
  const authLoading = ref(false);
  const authError = ref('');

  const authForm = reactive({
    username: '',
    password: '',
    confirmPassword: ''
  });

  // API基础URL
  const API_BASE_URL = 'http://localhost:8081';

  // 响应式数据
  const currentScreen = ref(0);
  const isScrolling = ref(false);
  
  // 🔧 修改：用户信息从 Store 获取
  const userInfo = computed(() => ({
    uid: userStore.uid,
    username: userStore.username,
    nickname: userStore.nickname || userStore.username,
    avatar: '', // 暂时为空，后续可添加
    email: userStore.email || '',
    joinDate: ''
  }));
  
  const userStats = reactive({
    poemsRead: 0,
    favoriteCount: 0,
    daysActive: 0
  });
  
  // 游戏统计数据
  const gameStats = reactive({
    totalGames: 0,
    wins: 0,
    winRate: 0,
    highestScore: 0
  });
  
  const gameDetails = reactive({
    quiz: {
      attempts: 0,
      accuracy: 0,
      bestScore: 0,
      avgTime: 0
    },
    feihua: {
      battles: 0,
      wins: 0,
      maxStreak: 0,
      bestTheme: ''
    },
    game: {
      plays: 0,
      completed: 0,
      maxLevel: 0,
      collected: 0
    }
  });
  
  const achievements = ref([
    {
      id: 1,
      name: '初学者',
      description: '完成第一次诗词测验',
      icon: '🌱',
      unlocked: true,
      progress: 100
    },
    {
      id: 2,
      name: '诗词达人',
      description: '累计答对100道诗词题目',
      icon: '📚',
      unlocked: false,
      progress: 65
    },
    {
      id: 3,
      name: '飞花令高手',
      description: '飞花令连胜10场',
      icon: '🌸',
      unlocked: false,
      progress: 30
    },
    {
      id: 4,
      name: '诗词收藏家',
      description: '收藏50首诗词',
      icon: '💎',
      unlocked: false,
      progress: 80
    }
  ]);
  
  const favoritePoems = ref([]);
  const searchKeyword = ref('');
  const sortBy = ref('time');
  const showPasswordModal = ref(false);
  
  const passwordForm = reactive({
    current: '',
    new: '',
    confirm: ''
  });


  // 认证相关方法
  const showLogin = () => {
    isLoginMode.value = true;
    showAuthModal.value = true;
    resetAuthForm();
  };

  const showRegister = () => {
    isLoginMode.value = false;
    showAuthModal.value = true;
    resetAuthForm();
  };

  const switchToLogin = () => {
    isLoginMode.value = true;
    resetAuthForm();
  };

  const switchToRegister = () => {
    isLoginMode.value = false;
    resetAuthForm();
  };

  const closeAuthModal = () => {
    showAuthModal.value = false;
    resetAuthForm();
  };

  const resetAuthForm = () => {
    authForm.username = '';
    authForm.password = '';
    authForm.confirmPassword = '';
    authError.value = '';
  };

  // 处理登录/注册
  const handleAuth = async () => {
    authError.value = '';

    // 表单验证
    if (!authForm.username.trim() || !authForm.password.trim()) {
      authError.value = '用户名和密码不能为空';
      return;
    }

    if (!isLoginMode.value) {
      if (authForm.password !== authForm.confirmPassword) {
        authError.value = '两次输入的密码不一致';
        return;
      }
      if (authForm.password.length < 6) {
        authError.value = '密码长度至少6位';
        return;
      }
    }

    authLoading.value = true;

    try {
      if (isLoginMode.value) {
        await handleLogin();
      } else {
        await handleRegister();
      }
    } catch (error) {
      console.error('认证失败:', error);
      authError.value = '网络错误，请稍后重试';
    } finally {
      authLoading.value = false;
    }
  };

  // 登录处理
  const handleLogin = async () => {
    authLoading.value = true;
    authError.value = '';
    
    try {
      const result = await userStore.apiLogin({
        username: authForm.username,
        password: authForm.password
      });
      
      if (result.success) {
        closeAuthModal();
        await loadUserData();
        alert('登录成功！');
      } else {
        authError.value = result.message;
      }
    } catch (error) {
      console.error('登录失败:', error);
      authError.value = '登录失败，请稍后重试';
    } finally {
      authLoading.value = false;
    }
  };

  // 注册处理
  // 🔧 完全替换：使用新的 API 注册方法
const handleRegister = async () => {
    authLoading.value = true;
    authError.value = '';
    
    try {
      const result = await userStore.apiRegister({
        username: authForm.username,
        password: authForm.password,
        nickname: authForm.username, // 默认昵称为用户名
        email: '' // 暂时为空
      });
      
      if (result.success) {
        alert('注册成功！请登录');
        switchToLogin();
      } else {
        authError.value = result.message;
      }
    } catch (error) {
      console.error('注册失败:', error);
      authError.value = '注册失败，请稍后重试';
    } finally {
      authLoading.value = false;
    }
  };


  // 退出登录
  const logout = () => {
    if (confirm('确定要退出登录吗？')) {
      userStore.logout();
      // 重置本地数据
      Object.assign(userStats, {
        poemsRead: 0,
        favoriteCount: 0,
        daysActive: 0
      });
      favoritePoems.value = [];
      alert('已成功退出登录');
    }
  };

  // 检查登录状态
  const checkLoginStatus = () => {
  return userStore.isAuthenticated;
  };

  // 计算屏幕样式
  const screenStyles = computed(() => {
    return {
      userScreen: {
        transform: currentScreen.value === 0 ? 'translateY(0)' : 'translateY(-100vh)',
        transition: 'transform 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94)'
      },
      gameScreen: {
        transform: currentScreen.value === 1 ? 'translateY(0)' : 'translateY(100vh)',
        transition: 'transform 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94)'
      }
    };
  });
  
  // 滚轮事件处理
  const handleWheel = (event) => {
    if (isScrolling.value) return;
    
    event.preventDefault();
    
    const delta = event.deltaY;
    
    if (delta > 0 && currentScreen.value === 0) {
      // 向下滚动，切换到游戏页面
      goToScreen(1);
    } else if (delta < 0 && currentScreen.value === 1) {
      // 向上滚动，切换到用户资料页面
      goToScreen(0);
    }
  };
  
  // 切换屏幕
  const goToScreen = (screenIndex) => {
    if (isScrolling.value || currentScreen.value === screenIndex) return;
    
    isScrolling.value = true;
    currentScreen.value = screenIndex;
    
    console.log('切换到屏幕:', screenIndex); // 调试用
    
    // 防止快速连续滚动
    setTimeout(() => {
      isScrolling.value = false;
    }, 800);
  };
  
  // 计算属性
  const filteredFavorites = computed(() => {
    let filtered = favoritePoems.value;
    
    // 搜索过滤
    if (searchKeyword.value) {
      const keyword = searchKeyword.value.toLowerCase();
      filtered = filtered.filter(poem => 
        poem.title?.toLowerCase().includes(keyword) ||
        poem.poet?.toLowerCase().includes(keyword) ||
        poem.text?.toLowerCase().includes(keyword)
      );
    }
    
    // 排序
    filtered.sort((a, b) => {
      switch (sortBy.value) {
        case 'time':
          return new Date(b.favoriteTime) - new Date(a.favoriteTime);
        case 'poet':
          return (a.poet || '').localeCompare(b.poet || '');
        case 'title':
          return (a.title || '').localeCompare(b.title || '');
        default:
          return 0;
      }
    });
    
    return filtered;
  });
  
  // 方法
  const loadUserData = async () => {
    try {
      // 如果未登录，尝试从存储恢复状态
      if (!userStore.isLoggedIn) {
        userStore.initFromStorage();
      }
      
      // 如果仍未登录，直接返回
      if (!userStore.isAuthenticated) {
        return;
      }

      // 🔧 新增：验证用户状态
      const isValid = await userStore.validateUser();
      if (!isValid) {
        return;
      }

      // 加载用户统计数据（模拟数据，后续可以从后端获取）
      userStats.poemsRead = 42;
      userStats.favoriteCount = 15;
      userStats.daysActive = 30;

      // 加载游戏统计数据（模拟数据）
      gameStats.totalGames = 28;
      gameStats.wins = 19;
      gameStats.winRate = Math.round((gameStats.wins / gameStats.totalGames) * 100);
      gameStats.highestScore = 9850;

      // 加载游戏详细数据
      gameDetails.quiz.attempts = 15;
      gameDetails.quiz.accuracy = 85;
      gameDetails.quiz.bestScore = 9850;
      gameDetails.quiz.avgTime = 45;

      gameDetails.feihua.battles = 8;
      gameDetails.feihua.wins = 5;
      gameDetails.feihua.maxStreak = 3;
      gameDetails.feihua.bestTheme = '春天';

      gameDetails.game.plays = 5;
      gameDetails.game.completed = 3;
      gameDetails.game.maxLevel = 12;
      gameDetails.game.collected = 25;

      // 🔧 新增：从后端加载收藏的诗词列表
      await loadFavoritePoems();

    } catch (error) {
      console.error('加载用户数据失败:', error);
    }
  };
  
  const loadFavoritePoems = async () => {
    try {
      const saved = localStorage.getItem('poetryBookmarks');
      if (saved) {
        favoritePoems.value = JSON.parse(saved).map(poem => ({
          ...poem,
          favoriteTime: poem.favoriteTime || new Date().toISOString()
        }));
        userStats.favoriteCount = favoritePoems.value.length;
      }
    } catch (error) {
      console.error('加载收藏列表失败:', error);
    }
  };
  
  const handleAvatarError = (event) => {
    event.target.src = '/default-avatar.png';
  };
  
  const getPreviewText = (text) => {
    if (!text) return '暂无内容';
    return text.length > 50 ? text.substring(0, 50) + '...' : text;
  };
  
  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('zh-CN');
  };
  
  const editProfile = () => {
    alert('编辑资料功能开发中...');
  };
  
  // 🔧 修改：刷新数据功能
  const refreshData = async () => {
    try {
      // 验证用户状态
      const isValid = await userStore.validateUser();
      if (!isValid) {
        alert('用户状态异常，请重新登录');
        return;
      }

      // 重新加载用户数据
      await loadUserData();
      alert('数据已刷新！');
    } catch (error) {
      console.error('刷新数据失败:', error);
      alert('刷新失败，请稍后重试');
    }
  };
  
  const viewPoem = (poem) => {
    if (poem.pid) {
      router.push(`/poem/${poem.pid}`);
    }
  };
  
  const removeFavorite = (pid) => {
    if (confirm('确定要取消收藏这首诗词吗？')) {
      favoritePoems.value = favoritePoems.value.filter(poem => poem.pid !== pid);
      localStorage.setItem('poetryBookmarks', JSON.stringify(favoritePoems.value));
      userStats.favoriteCount = favoritePoems.value.length;
    }
  };
  
  const goToRecommend = () => {
    router.push('/recommend');
  };
  
  const closePasswordModal = () => {
    showPasswordModal.value = false;
    passwordForm.current = '';
    passwordForm.new = '';
    passwordForm.confirm = '';
  };
  
  // 🔧 新增：修改密码功能
const changePassword = async () => {
  if (!passwordForm.current || !passwordForm.new || !passwordForm.confirm) {
    alert('请填写完整信息');
    return;
  }
  
  if (passwordForm.new !== passwordForm.confirm) {
    alert('两次输入的新密码不一致');
    return;
  }
  
  if (passwordForm.new.length < 6) {
    alert('新密码长度至少6位');
    return;
  }

  try {
    // 🔧 新增：调用后端修改密码接口
    const response = await fetch('http://localhost:8081/user/changePWD', {
      method: 'GET', // 注意：您的后端使用 GET 方法
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        UserName: userStore.username,
        PassWord: passwordForm.new
      })
    });

    const result = await response.text();

    if (result.includes('修改成功')) {
      alert('密码修改成功！');
      closePasswordModal();
    } else {
      alert('密码修改失败：' + result);
    }
  } catch (error) {
    console.error('修改密码失败:', error);
    alert('修改密码失败，请稍后重试');
  }
};
  
  // 键盘事件处理
  const handleKeydown = (event) => {
    if (event.key === 'ArrowDown' && currentScreen.value === 0) {
      goToScreen(1);
    } else if (event.key === 'ArrowUp' && currentScreen.value === 1) {
      goToScreen(0);
    }
  };
  
// 🔧 修改：生命周期 - 初始化时恢复用户状态
  onMounted(() => {
    // 首先初始化 Store 状态
    userStore.initFromStorage();
    // 然后加载用户数据
    loadUserData();
    window.addEventListener('keydown', handleKeydown);
  });
  
  </script>
  
  <style scoped>
  /* 基础布局 */
  .profile-layout {
    height: 100vh;
    overflow: hidden;
    background: #f5efe6;
    font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
    position: relative;
  }
  
  /* 屏幕系统 - 简化并直接使用内联样式 */
  .screen {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100vh;
    overflow-y: auto;
    /* transition 现在在 computed 中动态设置 */
  }
  
  /* 页面样式 */
  .profile-header, .game-header {
    text-align: center;
    padding: 1.5rem 0;
    background: linear-gradient(to right, #8c7853, #6e5773);
    color: white;
    box-shadow: 0 4px 8px rgba(0,0,0,0.1);
  }
  
  .profile-header h1, .game-header h1 {
    margin: 0;
    font-size: 2rem;
    font-weight: normal;
  }
  
  .subtitle {
    margin: 0.5rem 0 0;
    font-size: 0.9rem;
    opacity: 0.9;
    font-style: italic;
  }
  
  .profile-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 2rem;
    display: grid;
    grid-template-columns: 400px 1fr;
    gap: 2rem;
  }

  /* 用户信息卡片样式 - 修复抖动问题 */
  .user-info-card {
    background: #fffaf2;
    border-radius: 20px;
    padding: 2rem;
    box-shadow: 0 8px 24px rgba(140,120,83,0.1);
    width: 400px;
    height: 600px; /* 固定高度 */
    box-sizing: border-box;
    position: relative;
    overflow: hidden;
    /* 移除所有可能引起抖动的属性 */
    transform: none !important;
    transition: none !important;
    animation: none !important;
  }

  /* 头像区域样式 - 固定布局 */
  .avatar-section {
    text-align: center;
    margin-bottom: 2rem;
    height: 300px; /* 固定高度 */
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: flex-start;
    /* 防止抖动 */
    transform: none !important;
    transition: none !important;
    animation: none !important;
  }

  /* 头像容器 - 稳定定位 */
  .avatar-wrapper {
    position: relative;
    display: inline-block;
    margin-bottom: 1rem;
    width: 128px;
    height: 128px;
    /* 防止抖动 */
    transform: none !important;
    transition: none !important;
    animation: none !important;
  }

  /* 用户头像 - 稳定显示 */
  .user-avatar {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    object-fit: cover;
    border: 4px solid #fff;
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    /* 防止抖动 */
    transform: none !important;
    transition: none !important;
    animation: none !important;
    display: block;
  }

  /* 头像边框 - 稳定定位 */
  .avatar-border {
    position: absolute;
    top: -4px;
    left: -4px;
    right: -4px;
    bottom: -4px;
    border-radius: 50%;
    background: linear-gradient(45deg, #8c7853, #6e5773);
    z-index: -1;
    /* 防止抖动 */
    transform: none !important;
    transition: none !important;
    animation: none !important;
  }

  /* 用户详情 - 固定布局 */
  .user-details {
    width: 100%;
    height: 150px; /* 固定高度 */
    /* 防止抖动 */
    transform: none !important;
    transition: none !important;
    animation: none !important;
  }


  /* 用户名 - 稳定显示 */
  .username {
    margin: 0 0 0.5rem 0;
    font-size: 1.5rem;
    color: #8c7853;
    /* 防止抖动 */
    transform: none !important;
    transition: none !important;
    animation: none !important;
    line-height: 1.2;
    height: 1.8rem; /* 固定行高 */
  }

  /* 用户ID - 稳定显示 */
  .user-id {
    margin: 0 0 1.5rem 0;
    color: #666;
    font-size: 0.9rem;
    /* 防止抖动 */
    transform: none !important;
    transition: none !important;
    animation: none !important;
    line-height: 1.2;
    height: 1.1rem; /* 固定行高 */
  }

  /* 用户统计 - 固定布局 */
  .user-stats {
    display: flex;
    justify-content: space-around;
    margin-bottom: 2rem;
    height: 60px; /* 固定高度 */
    align-items: center;
    /* 防止抖动 */
    transform: none !important;
    transition: none !important;
    animation: none !important;
  }

  /* 统计项 - 稳定显示 */
  .stat-item {
    text-align: center;
    flex: 1;
    /* 防止抖动 */
    transform: none !important;
    transition: none !important;
    animation: none !important;
  }

  /* 统计数字 - 稳定显示 */
  .stat-number {
    display: block;
    font-size: 1.5rem;
    font-weight: bold;
    color: #8c7853;
    /* 防止抖动 */
    transform: none !important;
    transition: none !important;
    animation: none !important;
    line-height: 1.2;
    height: 1.8rem; /* 固定行高 */
  }

  /* 统计标签 - 稳定显示 */
  .stat-label {
    font-size: 0.8rem;
    color: #666;
    /* 防止抖动 */
    transform: none !important;
    transition: none !important;
    animation: none !important;
    line-height: 1.2;
    height: 1rem; /* 固定行高 */
  }

  /* 按钮区域 - 稳定布局 */
  .action-buttons {
    display: flex;
    flex-direction: column;
    gap: 0.8rem;
    height: 180px; /* 固定高度 */
    justify-content: flex-start;
    position: absolute;
    bottom: 2rem;
    left: 2rem;
    right: 2rem;
    /* 防止抖动 */
    transform: none !important;
    transition: none !important;
    animation: none !important;
  }

  /* 按钮 - 移除悬停效果 */
  .action-btn {
    padding: 0.8rem 1.2rem;
    border: none;
    border-radius: 10px;
    cursor: pointer;
    font-size: 0.9rem;
    width: 100%;
    height: 48px;
    box-sizing: border-box;
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
    white-space: nowrap;
    /* 完全移除可能引起抖动的效果 */
    transform: none !important;
    transition: none !important;
    animation: none !important;
  }
  
  .action-btn.primary {
    background: linear-gradient(135deg, #8c7853, #6e5773);
    color: white;
  }
  
  .action-btn.secondary {
    background: #e3d9c6;
    color: #8c7853;
  }
  
  .action-btn.normal {
    background: #f0ebe0;
    color: #8c7853;
  }
  
  .action-btn:active {
    opacity: 0.8;
  }

  /* 移除所有悬停效果和动画 */
  .action-btn:hover,
  .action-btn:focus,
  .action-btn:active {
    transform: none !important;
    transition: none !important;
    animation: none !important;
  }

  /* 收藏区域样式 */
  .favorites-section {
    background: #fffaf2;
    border-radius: 20px;
    padding: 2rem;
    box-shadow: 0 8px 24px rgba(140,120,83,0.1);
  }
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.5rem;
    flex-wrap: wrap;
    gap: 1rem;
  }
  
  .section-header h3 {
    margin: 0;
    color: #8c7853;
    font-size: 1.3rem;
  }
  
  .section-controls {
    display: flex;
    gap: 1rem;
    flex-wrap: wrap;
  }
  
  .search-input, .sort-select {
    padding: 0.5rem;
    border: 1px solid #d6cab4;
    border-radius: 8px;
    background: #f8f4ed;
  }
  
  .favorites-list {
    max-height: 500px;
    overflow-y: auto;
  }
  
  .favorite-item {
    background: #f8f4ed;
    border-radius: 12px;
    margin-bottom: 1rem;
    padding: 1.2rem;
    cursor: pointer;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    transition: background-color 0.2s ease;
  }
  
  .favorite-item:hover {
    background: #e3d9c6;
  }
  
  .poem-info {
    flex: 1;
  }
  
  .poem-title {
    margin: 0 0 0.5rem 0;
    color: #8c7853;
    font-size: 1.1rem;
  }
  
  .poem-author {
    margin: 0 0 0.5rem 0;
    color: #666;
    font-size: 0.9rem;
  }
  
  .poem-preview {
    margin: 0;
    color: #555;
    font-size: 0.85rem;
    line-height: 1.4;
  }
  
  .favorite-actions {
    display: flex;
    align-items: center;
    gap: 1rem;
  }
  
  .favorite-time {
    font-size: 0.8rem;
    color: #999;
  }
  
  .remove-btn {
    background: none;
    border: none;
    cursor: pointer;
    font-size: 0.8rem;
    opacity: 0.6;
  }
  
  .remove-btn:hover {
    opacity: 1;
  }
  
  .empty-state {
    text-align: center;
    padding: 3rem;
    color: #666;
  }
  
  .empty-icon {
    font-size: 3rem;
    margin-bottom: 1rem;
  }
  
  .goto-recommend-btn {
    background: linear-gradient(135deg, #8c7853, #6e5773);
    color: white;
    border: none;
    padding: 0.8rem 1.5rem;
    border-radius: 25px;
    cursor: pointer;
    margin-top: 1rem;
  }
  
  /* 游戏统计页面样式 */
  .game-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 2rem;
  }
  
  .game-overview-card {
    background: #fffaf2;
    border-radius: 20px;
    padding: 2rem;
    box-shadow: 0 8px 24px rgba(140,120,83,0.1);
    margin-bottom: 2rem;
  }
  
  .game-overview-card h3 {
    margin: 0 0 1.5rem 0;
    color: #8c7853;
    font-size: 1.5rem;
    text-align: center;
  }
  
  .game-stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
    gap: 1.5rem;
  }
  
  .game-stat-item {
    display: flex;
    align-items: center;
    gap: 1rem;
    padding: 1rem;
    background: #f8f4ed;
    border-radius: 12px;
  }
  
  .stat-icon {
    font-size: 2rem;
    min-width: 60px;
    text-align: center;
  }
  
  .stat-info {
    flex: 1;
  }
  
  .game-details-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 2rem;
    margin-bottom: 2rem;
  }
  
  .game-detail-card {
    background: #fffaf2;
    border-radius: 20px;
    padding: 1.5rem;
    box-shadow: 0 8px 24px rgba(140,120,83,0.1);
  }
  
  .card-header {
    border-bottom: 1px solid #e3d9c6;
    padding-bottom: 1rem;
    margin-bottom: 1rem;
  }
  
  .card-header h4 {
    margin: 0;
    color: #8c7853;
    font-size: 1.2rem;
  }
  
  .detail-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0.5rem 0;
    border-bottom: 1px solid #f0f0f0;
  }
  
  .detail-row:last-child {
    border-bottom: none;
  }
  
  .highlight {
    font-weight: bold;
    color: #8c7853;
  }
  
  /* 成就系统 */
  .achievements-section {
    background: #fffaf2;
    border-radius: 20px;
    padding: 2rem;
    box-shadow: 0 8px 24px rgba(140,120,83,0.1);
  }
  
  .achievements-section h3 {
    margin: 0 0 1.5rem 0;
    color: #8c7853;
    font-size: 1.5rem;
    text-align: center;
  }
  
  .achievements-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 1.5rem;
  }
  
  .achievement-item {
    display: flex;
    gap: 1rem;
    padding: 1.5rem;
    background: #f8f4ed;
    border-radius: 12px;
    opacity: 0.6;
    transition: all 0.3s ease;
  }
  
  .achievement-item.unlocked {
    opacity: 1;
    background: linear-gradient(135deg, #f8f4ed, #e3d9c6);
  }
  
  .achievement-icon {
    font-size: 2rem;
    min-width: 60px;
    text-align: center;
  }
  
  .achievement-info {
    flex: 1;
  }
  
  .achievement-info h5 {
    margin: 0 0 0.5rem 0;
    color: #8c7853;
    font-size: 1.1rem;
  }
  
  .achievement-info p {
    margin: 0 0 0.5rem 0;
    color: #666;
    font-size: 0.9rem;
  }
  
  .achievement-progress {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }
  
  .progress-bar {
    flex: 1;
    height: 6px;
    background: #e0e0e0;
    border-radius: 3px;
    overflow: hidden;
  }
  
  .progress-fill {
    height: 100%;
    background: linear-gradient(90deg, #8c7853, #6e5773);
    transition: width 0.3s ease;
  }
  
  .progress-text {
    font-size: 0.8rem;
    color: #666;
    min-width: 35px;
  }
  
  /* 滚动提示 */
  .scroll-hint {
    position: absolute;
    bottom: 20px;
    left: 50%;
    transform: translateX(-50%);
    text-align: center;
    color: #8c7853;
    opacity: 0.7;
    animation: bounce 2s infinite;
    z-index: 10;
  }
  

  
  .scroll-arrow {
    font-size: 1.5rem;
    margin-bottom: 0.5rem;
  }
  

  
  @keyframes bounce {
    0%, 20%, 50%, 80%, 100% {
      transform: translateX(-50%) translateY(0);
    }
    40% {
      transform: translateX(-50%) translateY(-10px);
    }
    60% {
      transform: translateX(-50%) translateY(-5px);
    }
  }
  
  /* 页面指示器 */
  .screen-indicators {
    position: fixed;
    right: 20px;
    top: 50%;
    transform: translateY(-50%);
    display: flex;
    flex-direction: column;
    gap: 10px;
    z-index: 100;
  }
  
  .indicator {
    width: 12px;
    height: 12px;
    border-radius: 50%;
    background: rgba(140, 120, 83, 0.3);
    cursor: pointer;
    transition: all 0.3s ease;
  }
  
  .indicator.active {
    background: #8c7853;
    transform: scale(1.2);
  }
  
  /* 模态框样式 */
  .modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0,0,0,0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
  }
  
  .modal-content {
    background: white;
    border-radius: 20px;
    width: 90%;
    max-width: 500px;
    box-shadow: 0 20px 40px rgba(0,0,0,0.3);
  }
  
  .modal-header {
    padding: 1.5rem;
    border-bottom: 1px solid #eee;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .modal-header h3 {
    margin: 0;
    color: #8c7853;
  }
  
  .close-btn {
    background: none;
    border: none;
    font-size: 1.5rem;
    cursor: pointer;
    color: #999;
  }
  
  .modal-body {
    padding: 1.5rem;
  }
  
  .form-group {
    margin-bottom: 1rem;
  }
  
  .form-group label {
    display: block;
    margin-bottom: 0.5rem;
    color: #666;
    font-size: 0.9rem;
  }
  
  .form-input {
    width: 100%;
    padding: 0.8rem;
    border: 1px solid #ddd;
    border-radius: 8px;
    font-size: 0.9rem;
    box-sizing: border-box;
  }
  
  .modal-footer {
    padding: 1.5rem;
    border-top: 1px solid #eee;
    display: flex;
    justify-content: flex-end;
    gap: 1rem;
  }
  
  .btn-cancel, .btn-confirm {
    padding: 0.8rem 1.5rem;
    border: none;
    border-radius: 8px;
    cursor: pointer;
    font-size: 0.9rem;
  }
  
  .btn-cancel {
    background: #eee;
    color: #666;
  }
  
  .btn-confirm {
    background: linear-gradient(135deg, #8c7853, #6e5773);
    color: white;
  }
  
  /* 响应式设计 */
  @media (max-width: 768px) {
    .profile-container, .game-container {
      grid-template-columns: 1fr;
      padding: 1rem;
    }
    
    .user-info-card {
      width: 100%;
      min-height: auto;
      max-height: none;
    }
    
    .game-stats-grid {
      grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    }
    
    .game-details-grid {
      grid-template-columns: 1fr;
    }
    
    .achievements-grid {
      grid-template-columns: 1fr;
    }
  }

  /* 登录提示 - 固定布局 */
  .login-prompt {
    text-align: center;
    padding: 3rem 2rem;
    height: 100%; /* 占满整个卡片 */
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;
    /* 防止抖动 */
    transform: none !important;
    transition: none !important;
    animation: none !important;
    box-sizing: border-box;
  }

  /* 登录提示图标 - 稳定显示 */
  .login-prompt-icon {
    font-size: 4rem;
    margin-bottom: 1.5rem;
    opacity: 0.6;
    /* 防止抖动 */
    transform: none !important;
    transition: none !important;
    animation: none !important;
    line-height: 1;
    height: 4rem; /* 固定高度 */
  }

  /* 登录提示标题 - 稳定显示 */
  .login-prompt h2 {
    color: #8c7853;
    margin: 0 0 1rem 0;
    font-size: 1.5rem;
    /* 防止抖动 */
    transform: none !important;
    transition: none !important;
    animation: none !important;
    line-height: 1.2;
    height: 1.8rem; /* 固定高度 */
  }

  /* 登录提示文本 - 稳定显示 */
  .login-prompt p {
    color: #666;
    margin: 0 0 2rem 0;
    font-size: 1rem;
    /* 防止抖动 */
    transform: none !important;
    transition: none !important;
    animation: none !important;
    line-height: 1.2;
    height: 1.2rem; /* 固定高度 */
  }

  .login-prompt-buttons {
    display: flex;
    flex-direction: column;
    gap: 1rem;
    width: 100%;
    max-width: 200px;
  }

  .auth-prompt-btn {
    padding: 1rem 1.5rem;
    border: none;
    border-radius: 12px;
    cursor: pointer;
    font-size: 1rem;
    font-weight: 500;
    transition: all 0.3s ease;
  }

  .auth-prompt-btn.login {
    background: linear-gradient(135deg, #8c7853, #6e5773);
    color: white;
  }

  .auth-prompt-btn.register {
    background: #f0ebe0;
    color: #8c7853;
    border: 2px solid #e3d9c6;
  }

  .auth-prompt-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  }

  .action-btn.logout {
    background: #ff6b6b;
    color: white;
  }

  .action-btn.logout:hover {
    background: #ff5252;
  }

  /* 认证弹窗样式 */
  .auth-modal-content {
    background: white;
    border-radius: 20px;
    width: 90%;
    max-width: 450px;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  }

  .auth-modal-header {
    padding: 2rem 2rem 1rem;
    border-bottom: 1px solid #f0f0f0;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .auth-modal-header h3 {
    margin: 0;
    color: #8c7853;
    font-size: 1.5rem;
  }

  .auth-modal-body {
    padding: 2rem;
  }

  .auth-submit-btn {
    width: 100%;
    padding: 1rem;
    background: linear-gradient(135deg, #8c7853, #6e5773);
    color: white;
    border: none;
    border-radius: 12px;
    font-size: 1rem;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.3s ease;
    margin-top: 1rem;
  }

  .auth-submit-btn:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(140, 120, 83, 0.3);
  }

  .auth-submit-btn:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none;
  }

  .auth-switch {
    text-align: center;
    margin-top: 1.5rem;
    color: #666;
  }

  .switch-btn {
    background: none;
    border: none;
    color: #8c7853;
    text-decoration: underline;
    cursor: pointer;
    font-size: inherit;
  }

  .switch-btn:hover {
    color: #6e5773;
  }

  .auth-error {
    background: #ffe6e6;
    color: #d32f2f;
    padding: 1rem;
    border-radius: 8px;
    margin-top: 1rem;
    text-align: center;
    font-size: 0.9rem;
  }


  </style>