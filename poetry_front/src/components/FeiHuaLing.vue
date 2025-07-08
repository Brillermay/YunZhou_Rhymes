<template>
  <div class="feihua-game-container">
    <!-- 游戏模式选择阶段 -->
    <GameModeSelector
      v-if="gameState === 'mode-selection'"
      :game-stats="gameStats"
      :is-loading="isLoadingStats"
      @mode-selected="onModeSelected"
      @start-game="startGame"
      @return-to-center="handleReturnToCenter"
    />
    
    <!-- 游戏进行阶段 -->
    <div v-else-if="gameState === 'playing'" class="game-playing-layout">
      <!-- 🔧 游戏头部 - 添加暂停状态和事件处理 -->
      <div class="game-header-fixed">
        <GameHeader
          :game-mode="selectedMode"
          :difficulty="selectedDifficulty"
          :current-keyword="currentKeyword"
          :countdown="countdown"
          :max-time="maxTime"
          :answer-count="answerCount"
          :current-round="currentRound"
          :round-progress="roundProgress"
          :round-target="roundTarget"
          :game-time="gameTime"
          :keyword-stats="keywordStats"
          :is-paused="isPaused"
          @toggle-pause="togglePause"
          @show-exit-confirm="showExitConfirm"
        />
      </div>
      
      <!-- 🔧 主游戏区域 - 占用剩余空间，避开头部 -->
      <div class="game-content-area">
        <!-- 左侧：聊天区域 -->
        <div class="game-chat-section">
          <ChatArea
            :messages="messages"
            :current-keyword="currentKeyword"
            :game-ended="gameState === 'ended'"
            :is-validating="isValidating"
            :hint-count="hintCount"
            :is-paused="isPaused"
            @send-message="handleMessage"
            @request-hint="requestHint"
          />
        </div>
        
        <!-- 右侧：统计面板 -->
        <div class="game-stats-section">
          <GameStatsPanel
            :user-id="userId"
          />
        </div>
      </div>
    </div>
    
    <!-- 游戏结束阶段 -->
    <div v-else-if="gameState === 'ended'" class="game-result">
      <div class="result-container">
        <div class="result-header">
          <h2 class="result-title">游戏结束</h2>
          <div class="final-score">
            <span class="score-label">最终得分</span>
            <span class="score-value">{{ finalScore }}</span>
          </div>
        </div>
        
        <div class="result-stats">
          <div class="stat-card">
            <div class="stat-value">{{ answerCount }}</div>
            <div class="stat-label">答对题数</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ formatTime(gameTime) }}</div>
            <div class="stat-label">游戏时长</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ accuracy }}%</div>
            <div class="stat-label">准确率</div>
          </div>
        </div>
        
        <div class="result-actions">
          <button class="btn btn-secondary" @click="restartGame">
            <i class="icon-refresh"></i>
            再来一局
          </button>
          <button class="btn btn-primary" @click="showLeaderboard">
            <i class="icon-trophy"></i>
            查看排行榜
          </button>
          <button class="btn btn-secondary" @click="backToMenu">
            <i class="icon-arrow-left"></i>
            返回菜单
          </button>
        </div>
      </div>
    </div>
    
    <!-- 排行榜弹窗 -->
    <LeaderboardModal
      :visible="isLeaderboardVisible"
      :user-id="userId"
      @close="closeLeaderboard"
      @share-rank="shareRank"
    />
    
    <!-- 🔧 新增：登录提示弹窗 -->
    <div v-if="showLoginPrompt" class="login-prompt-overlay" @click="closeLoginPrompt">
      <div class="login-prompt-modal" @click.stop>
        <div class="login-prompt-header">
          <h3>需要登录</h3>
          <button class="close-btn" @click="closeLoginPrompt">✕</button>
        </div>
        <div class="login-prompt-content">
          <div class="login-prompt-icon">🔐</div>
          <h4>游戏需要登录才能开始</h4>
          <p>登录后您可以：</p>
          <ul>
            <li>📊 保存游戏成绩和进度</li>
            <li>🏆 参与排行榜竞争</li>
            <li>🎯 解锁更多成就</li>
            <li>📈 查看详细的游戏统计</li>
          </ul>
          <div class="login-prompt-actions">
            <button class="btn btn-primary" @click="goToUserCenter">
              🚀 去登录
            </button>
            <button class="btn btn-secondary" @click="closeLoginPrompt">
              稍后再说
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 浮动装饰元素 -->
    <div class="floating-decorations">
      <div 
        v-for="(element, index) in floatingElements" 
        :key="index"
        class="floating-element"
        :style="element.style"
      >
        {{ element.char }}
      </div>
    </div>
  </div>

  <!-- 在模板最后，添加退出确认弹窗 -->
<!-- 🔧 新增：退出确认弹窗 - 移到最外层 -->
<div v-if="showExitModal" class="exit-modal-overlay" @click="closeExitModal">
  <div class="exit-modal" @click.stop>
    <div class="exit-modal-header">
      <h3>确认退出游戏？</h3>
    </div>
    <div class="exit-modal-content">
      <div class="exit-warning">
        <i class="icon-alert-triangle"></i>
        <p>退出游戏将丢失当前进度和分数</p>
      </div>
      <div class="current-progress">
        <div class="progress-item">
          <span class="label">已答题数：</span>
          <span class="value">{{ answerCount }} 题</span>
        </div>
        <div class="progress-item">
          <span class="label">游戏时长：</span>
          <span class="value">{{ formatTime(gameTime) }}</span>
        </div>
        <div class="progress-item">
          <span class="label">当前关键词：</span>
          <span class="value">{{ currentKeyword }}</span>
        </div>
        <div class="progress-item">
          <span class="label">游戏模式：</span>
          <span class="value">{{ getModeLabel(selectedMode) }} - {{ getDifficultyLabel(selectedDifficulty) }}</span>
        </div>
      </div>
    </div>
    <div class="exit-modal-actions">
      <button class="btn btn-secondary" @click="closeExitModal">
        <i class="icon-arrow-left"></i>
        继续游戏
      </button>
      <button class="btn btn-danger" @click="confirmExit">
        <i class="icon-x"></i>
        确认退出
      </button>
    </div>
  </div>
</div>

</template>

<!-- script 部分保持不变 -->
<script>
import axios from 'axios'
import API_BASE_URL from '@/config/api'
// 🔧 新增导入
import { useUserStore } from '@/stores/user'
import { isLoggedIn, getCurrentUid } from '@/utils/auth'
// 导入子组件
import GameModeSelector from './feihualing/GameModeSelector.vue'
import GameHeader from './feihualing/GameHeader.vue'
import ChatArea from './feihualing/ChatArea.vue'
import GameStatsPanel from './feihualing/GameStatsPanel.vue'
import LeaderboardModal from './feihualing/LeaderboardModal.vue'

export default {
  name: 'FeiHuaLingNew',
  components: {
    GameModeSelector,
    GameHeader,
    ChatArea,
    GameStatsPanel,
    LeaderboardModal
  },
    // 🔧 新增 setup 来使用 Pinia store
  setup() {
    const userStore = useUserStore()
    return {
      userStore
    }
  },
  data() {
    return {
      // 游戏状态
      gameState: 'mode-selection', // 'mode-selection', 'playing', 'ended'
    
      // 🔧 新增：暂停状态
       isPaused: false,
           // 🔧 新增：退出确认弹窗状态
      showExitModal: false,
          // 🔧 新增：退出前的暂停状态记录
    wasGamePausedBeforeExit: false,

      // 🔧 新增登录检查状态
      showLoginPrompt: false,

      // 游戏配置
      selectedMode: '',
      selectedDifficulty: '',
      
      // 游戏数据
      currentKeyword: '月',
      countdown: 30,
      maxTime: 30,
      gameTime: 0,
      answerCount: 0,
      currentRound: 1,
      roundProgress: 0,
      roundTarget: 3,
      finalScore: 0,
      accuracy: 100,
      hintCount: 3,
      
      // 消息和验证
      messages: [],
      isValidating: false,
      
      // 统计数据
      gameStats: {},
      isLoadingStats: true,
      keywordStats: null,
      
      // UI状态
      isLeaderboardVisible: false,
      userId: null, // 从用户登录状态获取
      
      // 装饰元素
      floatingElements: [],
      
      // 计时器
      gameTimer: null,
      countdownTimer: null
    }
  },
    // 🔧 新增计算属性
  computed: {
    isUserLoggedIn() {
      return this.userStore.isAuthenticated
    },
    currentUserId() {
      return this.userStore.uid
    }
  },
  async mounted() {
    await this.loadGameStats()
    this.initFloatingElements()
        // 🔧 修改：使用 store 中的用户ID
    this.userId = this.currentUserId
  },
  beforeUnmount() {
    this.clearTimers()
  },
  methods: {
        // 🔧 新增：登录检查方法
    checkLoginBeforeGame() {
      if (!this.isUserLoggedIn) {
        this.showLoginPrompt = true
        return false
      }
      return true
    },
    
    // 🔧 新增：跳转到用户中心登录
    goToUserCenter() {
      this.showLoginPrompt = false
      this.$router.push('/userinfo')
    },
    
    // 🔧 新增：关闭登录提示
    closeLoginPrompt() {
      this.showLoginPrompt = false
    },
    // 加载游戏统计数据
    async loadGameStats() {
      try {
        const response = await axios.get(`${API_BASE_URL}/api/feihua/stats`)
        if (response.data.success) {
          this.gameStats = response.data.data
        }
      } catch (error) {
        console.error('加载游戏统计失败:', error)
      } finally {
        this.isLoadingStats = false
      }
    },
    
    // 🔧 修改：模式选择处理 - 添加登录检查
    onModeSelected(selection) {
      if (!this.checkLoginBeforeGame()) {
        return
      }
      this.selectedMode = selection.mode
      this.selectedDifficulty = selection.difficulty
    },
    
    // 🔧 修改：开始游戏 - 添加登录检查
    startGame(gameConfig) {
      if (!this.checkLoginBeforeGame()) {
        return
      }
      
      this.selectedMode = gameConfig.mode
      this.selectedDifficulty = gameConfig.difficulty
      this.gameState = 'playing'
      
      // 设置难度参数
      const difficultySettings = {
        easy: { time: 45, hints: 5 },
        normal: { time: 30, hints: 3 },
        hard: { time: 15, hints: 1 }
      }
      
      const settings = difficultySettings[this.selectedDifficulty]
      this.maxTime = settings.time
      this.countdown = settings.time
      this.hintCount = settings.hints
      
      // 初始化游戏
      this.initGame()
    },
    
    // 初始化游戏
    initGame() {
      this.answerCount = 0
      this.gameTime = 0
      this.messages = [{
        type: 'system',
        text: `欢迎来到飞花令！请说出包含"${this.currentKeyword}"的诗句。`,
        timestamp: Date.now()
      }]
      
      this.startTimers()
    },
    
    // 🔧 修改：开始计时器 - 检查暂停状态
    startTimers() {
      if (this.isPaused) return
      
      this.gameTimer = setInterval(() => {
        if (!this.isPaused) {
          this.gameTime++
        }
      }, 1000)
      
      this.countdownTimer = setInterval(() => {
        if (!this.isPaused) {
          this.countdown--
          if (this.countdown <= 0) {
            this.timeUp()
          }
        }
      }, 1000)
    },
    
    // 清除计时器
    clearTimers() {
      if (this.gameTimer) {
        clearInterval(this.gameTimer)
        this.gameTimer = null
      }
      if (this.countdownTimer) {
        clearInterval(this.countdownTimer)
        this.countdownTimer = null
      }
    },
    
    // 🔧 修改：处理用户消息 - 检查暂停状态
    async handleMessage(message) {
      if (this.isPaused) {
        this.messages.push({
          type: 'system',
          text: '⏸️ 游戏已暂停，请先继续游戏再答题。',
          timestamp: Date.now()
        })
        return
      }
      
      this.messages.push({
        type: 'user',
        text: message,
        timestamp: Date.now()
      })
      
      await this.validatePoetry(message)
    },
    
    // 验证诗句
    async validatePoetry(message) {
      this.isValidating = true
      
      try {
        // 模拟验证API调用
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        const isValid = message.includes(this.currentKeyword) && message.length >= 5
        
        if (isValid) {
          this.handleCorrectAnswer(message)
        } else {
          this.handleIncorrectAnswer(message)
        }
      } catch (error) {
        console.error('验证失败:', error)
        this.handleIncorrectAnswer(message)
      } finally {
        this.isValidating = false
      }
    },
    
    // 处理正确答案
    handleCorrectAnswer(message) {
      this.answerCount++
      this.roundProgress++
      
      // 重置倒计时
      this.countdown = this.maxTime
      
      // 添加系统响应
      this.messages.push({
        type: 'system',
        text: '答对了！继续下一题。',
        timestamp: Date.now(),
        validation: {
          type: 'success',
          text: '诗句验证正确',
          poemInfo: {
            title: '示例诗',
            author: '示例作者'
          }
        }
      })
      
      // 检查是否需要下一轮
      if (this.selectedMode === 'challenge' && this.roundProgress >= this.roundTarget) {
        this.nextRound()
      } else {
        this.nextKeyword()
      }
    },
    
    // 处理错误答案
    handleIncorrectAnswer(message) {
      this.messages.push({
        type: 'system',
        text: '这个答案不太对，请重新思考。',
        timestamp: Date.now(),
        validation: {
          type: 'error',
          text: '诗句验证失败'
        }
      })
    },
    
    // 下一个关键词
    nextKeyword() {
      const keywords = ['月', '花', '春', '酒', '山', '鸟', '水', '云', '风', '雪']
      const currentIndex = keywords.indexOf(this.currentKeyword)
      const nextIndex = (currentIndex + 1) % keywords.length
      this.currentKeyword = keywords[nextIndex]
      
      this.messages.push({
        type: 'system',
        text: `下一个关键词是"${this.currentKeyword}"，请继续！`,
        timestamp: Date.now()
      })
    },
    
    // 下一轮
    nextRound() {
      this.currentRound++
      this.roundProgress = 0
      this.nextKeyword()
    },
    
    // 时间到
    timeUp() {
      this.endGame()
    },
    
    // 结束游戏
    async endGame() {
      this.clearTimers()
      this.gameState = 'ended'
      this.finalScore = this.calculateScore()
      this.accuracy = this.calculateAccuracy()
      
      // 提交成绩到服务器
      await this.submitScore()
    },
    
    // 计算分数
    calculateScore() {
      const baseScore = this.answerCount * 5
      const timeBonus = Math.max(0, (this.maxTime - this.gameTime) * 0.1)
      const difficultyMultiplier = {
        easy: 1,
        normal: 1.5,
        hard: 2
      }[this.selectedDifficulty]
      
      return Math.round((baseScore + timeBonus) * difficultyMultiplier)
    },
    
    // 计算准确率
    calculateAccuracy() {
      const totalAttempts = this.messages.filter(m => m.type === 'user').length
      return totalAttempts > 0 ? Math.round((this.answerCount / totalAttempts) * 100) : 100
    },
    
    // 🔧 修改：提交分数 - 使用 store 中的用户信息
    async submitScore() {
      if (!this.currentUserId) {
        console.warn('用户未登录，无法提交分数')
        return
      }
      
      try {
        const payload = {
          userId: this.currentUserId,
          playerName: this.userStore.displayName,
          score: this.finalScore,
          mode: this.selectedMode,
          difficulty: this.selectedDifficulty,
          keywordsUsed: '月,花,春' // 收集实际使用的关键词
        }
        
        await axios.post(`${API_BASE_URL}/api/feihua/submit-score`, payload)
        console.log('✅ 分数提交成功')
      } catch (error) {
        console.error('提交分数失败:', error)
      }
    },
    
    // 请求提示
    // 🔧 修改：请求提示 - 只在用户主动请求时消耗次数
    requestHint() {
      if (this.hintCount <= 0) return
      
      this.hintCount--
      this.messages.push({
        type: 'system',
        text: '诗句提示已显示在输入框上方，请查看参考。',
        timestamp: Date.now()
      })
    },
    
    // UI 控制方法
    showLeaderboard() {
      this.isLeaderboardVisible = true
    },
    
    closeLeaderboard() {
      this.isLeaderboardVisible = false
    },
    
    shareRank(rankData) {
      // 分享排名功能
      console.log('分享排名:', rankData)
    },
    
    restartGame() {
      this.gameState = 'mode-selection'
      this.resetGameData()
    },
    
    backToMenu() {
      this.$router.push('/game-center')
    },
    
    // 🔧 修改：重置游戏数据
    resetGameData() {
      this.selectedMode = ''
      this.selectedDifficulty = ''
      this.currentKeyword = '月'
      this.answerCount = 0
      this.gameTime = 0
      this.finalScore = 0
      this.messages = []
      this.isPaused = false
      this.showExitModal = false
      this.wasGamePausedBeforeExit = false
      this.clearTimers()
    },
    
    // 工具方法
    formatTime(seconds) {
      const mins = Math.floor(seconds / 60)
      const secs = seconds % 60
      return `${mins}:${secs.toString().padStart(2, '0')}`
    },
    
    // 🔧 修改：获取当前用户ID方法
    getCurrentUserId() {
      return this.currentUserId
    },
    
    // 初始化浮动装饰元素
    initFloatingElements() {
      const chars = ['月', '花', '春', '酒', '山', '鸟', '水', '云', '风', '雪']
      
      for (let i = 0; i < 8; i++) {
        this.floatingElements.push({
          char: chars[Math.floor(Math.random() * chars.length)],
          style: {
            left: Math.random() * 100 + '%',
            animationDelay: Math.random() * 10 + 's',
            fontSize: (Math.random() * 0.5 + 0.8) + 'rem',
            opacity: Math.random() * 0.3 + 0.1
          }
        })
      }
    },

  // 🔧 修改：切换暂停状态 - 如果退出弹窗打开，先关闭弹窗
  togglePause() {
    // 如果退出确认弹窗正在显示，先关闭它
    if (this.showExitModal) {
      this.closeExitModal()
      return
    }
    
    this.isPaused = !this.isPaused
    
    if (this.isPaused) {
      this.pauseGame()
    } else {
      this.resumeGame()
    }
  },
  
  // 🔧 新增：暂停游戏
  pauseGame() {
    this.clearTimers()
    
    // 添加暂停消息
    this.messages.push({
      type: 'system',
      text: '🔔 游戏已暂停，点击继续按钮恢复游戏。',
      timestamp: Date.now()
    })
  },
  
  // 🔧 新增：恢复游戏
  resumeGame() {
    this.startTimers()
    
    // 添加恢复消息
    this.messages.push({
      type: 'system',
      text: '🎮 游戏已恢复，继续挑战吧！',
      timestamp: Date.now()
    })
  },
  
  // 🔧 新增：退出游戏
  exitGame() {
    this.clearTimers()
    this.isPaused = false
    this.gameState = 'mode-selection'
    this.resetGameData()
    
    // 添加退出提示
    this.$nextTick(() => {
      console.log('游戏已退出，返回主菜单')
    })
  },

 // 🔧 修改：显示退出确认 - 自动暂停游戏
  showExitConfirm() {
    // 记录当前游戏是否已经暂停
    this.wasGamePausedBeforeExit = this.isPaused
    
    // 如果游戏正在进行，则暂停游戏
    if (!this.isPaused) {
      this.pauseGameForExit()
    }
    
    // 显示退出确认弹窗
    this.showExitModal = true
  },
  
  // 🔧 修改：关闭退出确认 - 恢复游戏状态
  closeExitModal() {
    this.showExitModal = false
    
    // 如果游戏在显示退出确认前没有暂停，则恢复游戏
    if (!this.wasGamePausedBeforeExit) {
      this.resumeGameFromExit()
    }
    
    // 重置记录状态
    this.wasGamePausedBeforeExit = false
  },
  // 🔧 修改：确认退出 - 直接退出，不需要恢复
  confirmExit() {
    this.showExitModal = false
    this.wasGamePausedBeforeExit = false
    this.exitGame()
  },
    // 🔧 新增：为退出确认暂停游戏（不显示暂停消息）
  pauseGameForExit() {
    this.isPaused = true
    this.clearTimers()
    
    // 添加退出确认提示消息
    this.messages.push({
      type: 'system',
      text: '⏸️ 游戏已暂停，正在等待您的选择...',
      timestamp: Date.now()
    })
  },

    // 🔧 新增：从退出确认恢复游戏（不显示恢复消息）
  resumeGameFromExit() {
    this.isPaused = false
    this.startTimers()
    
    // 添加恢复游戏消息
    this.messages.push({
      type: 'system',
      text: '🎮 游戏已恢复，继续挑战吧！',
      timestamp: Date.now()
    })
  },
  // 🔧 修改：退出游戏方法
  exitGame() {
    this.clearTimers()
    this.isPaused = false
    this.showExitModal = false
    this.wasGamePausedBeforeExit = false
    this.gameState = 'mode-selection'
    this.resetGameData()
    
    this.$nextTick(() => {
      console.log('游戏已退出，返回主菜单')
    })
  },

  // 🔧 新增：工具方法
  getModeLabel(mode) {
    return { endless: '无尽', challenge: '闯关' }[mode] || '未知'
  },
  
  getDifficultyLabel(difficulty) {
    return { easy: '简单', normal: '普通', hard: '困难' }[difficulty] || '未知'
  },

    // 🔧 新增：处理返回游戏中心
  handleReturnToCenter() {
    // 方法1：如果使用路由导航
    this.$router.push('/game-center')
    
    // 方法2：如果使用事件向上传递给更上层的组件
    // this.$emit('return-to-game-center')
    
    // 方法3：如果需要清理当前游戏状态
    // this.resetGameData()
    // this.$router.push('/game-center')
  },

  }

}
</script>

<!-- filepath: c:\Users\Administrator\Desktop\YunZhou_Rhymes\poetry_front\src\components\FeiHuaLing.vue -->
<!-- 保持 template 和 script 不变，只修改 style 部分 -->

<style lang="scss" scoped>
@import './feihualing/styles/game-common.scss';

.feihua-game-container {
  @extend .feihua-component;
  min-height: 100vh;
  position: relative;
  overflow: hidden;
    // 🔧 关键：降低容器层级
  z-index: 1;
}

// 🚀 重构：游戏进行时的布局
.game-playing-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: transparent;
  position: relative;
    // 🔧 确保不会遮挡弹窗
  z-index: 1;
}

// 🔧 固定头部 - 明确高度
.game-header-fixed {
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(245, 239, 230, 0.98);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid var(--border-color);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
  // 🔧 关键修复：明确设置头部高度
  height: 80px; // 固定头部高度
  min-height: 80px;
  max-height: 80px;
  overflow: hidden;
  
  @media (max-width: 768px) {
    height: 65px;
    min-height: 65px;
    max-height: 65px;
  }
}

// 🔧 游戏内容区域 - 确保从头部下方开始
.game-content-area {
  flex: 1;
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 1rem;
  padding: 1rem;
  min-height: 0;
  overflow: hidden;
  // 🔧 关键修复：确保内容不被头部遮挡
  position: relative;
    // 🔧 确保不会遮挡弹窗
  z-index: 10;
  
  @media (max-width: 1024px) {
    grid-template-columns: 1fr;
    grid-template-rows: 1fr auto;
    
    .game-stats-section {
      max-height: 200px;
      overflow-y: auto;
    }
  }
  
  @media (max-width: 768px) {
    padding: 0.5rem;
    gap: 0.5rem;
  }
}

// 🔧 聊天区域 - 确保完全可见
.game-chat-section {
  @include modern-card;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  background: rgba(255, 255, 255, 0.95);
  overflow: hidden;
  position: relative;
  z-index: 5;
  // 🔧 确保边框清晰可见
  border: 2px solid var(--border-color);
}

// 🔧 统计区域 - 独立滚动
.game-stats-section {
  @include modern-card;
  padding: 0.5rem;
  overflow-y: auto;
  background: rgba(255, 255, 255, 0.95);
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  height: 100%;
  min-height: 0;
  position: relative;
  z-index: 5;
  // 🔧 确保边框清晰可见
  border: 2px solid var(--border-color);
}

// 其他样式保持不变...
.game-result {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  background: transparent;
}

.result-container {
  @include ancient-card;
  padding: 2rem;
  text-align: center;
  max-width: 500px;
  width: 100%;
  background: rgba(255, 255, 255, 0.95);
}

.result-header {
  margin-bottom: 2rem;
  
  .result-title {
    @include ancient-title;
    font-size: 2rem;
    margin-bottom: 1rem;
    color: var(--text-color);
  }
  
  .final-score {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.5rem;
    
    .score-label {
      font-size: 1rem;
      color: #666;
    }
    
    .score-value {
      font-size: 3rem;
      font-weight: bold;
      color: var(--primary-color);
      text-shadow: 0 2px 4px rgba(140, 120, 83, 0.3);
    }
  }
}

.result-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1rem;
  margin-bottom: 2rem;
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.stat-card {
  @include stats-card;
  background: rgba(255, 255, 255, 0.9);
  
  .stat-value {
    font-size: 1.5rem;
    font-weight: bold;
    color: var(--primary-color);
    margin-bottom: 0.5rem;
  }
  
  .stat-label {
    font-size: 0.9rem;
    color: #666;
  }
}

.result-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
  
  .btn {
    padding: 0.75rem 1.5rem;
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }
}

.floating-decorations {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: -1;
}

.floating-element {
  position: absolute;
  color: rgba(140, 120, 83, 0.15);
  font-family: 'KaiTi', '楷体', serif;
  animation: float 15s infinite linear;
  user-select: none;
  font-weight: 300;
}

@keyframes float {
  0% {
    transform: translateY(100vh) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-100px) rotate(360deg);
    opacity: 0;
  }
}

@media (max-width: 768px) {
  .result-container {
    padding: 1.5rem;
    margin: 1rem;
  }
  
  .result-header .score-value {
    font-size: 2.5rem;
  }
  
  .result-actions {
    flex-direction: column;
    align-items: stretch;
    
    .btn {
      justify-content: center;
    }
  }
}
// 在 FeiHuaLing.vue 的 <style> 部分最后添加

// 🔧 新增：登录提示弹窗样式
.login-prompt-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
  padding: 1rem;
  animation: fadeIn 0.3s ease-out;
}

.login-prompt-modal {
  background: 
    linear-gradient(135deg, 
      rgba(255, 255, 255, 0.98) 0%, 
      rgba(248, 245, 240, 0.95) 100%
    );
  border-radius: 20px;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.3);
  max-width: 500px;
  width: 90%;
  overflow: hidden;
  animation: scaleIn 0.3s ease-out;
  backdrop-filter: blur(10px);
  border: 2px solid rgba(140, 120, 83, 0.2);
}

.login-prompt-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  background: 
    linear-gradient(90deg, 
      rgba(140, 120, 83, 0.1) 0%, 
      rgba(110, 87, 115, 0.1) 100%
    );
  border-bottom: 2px solid rgba(140, 120, 83, 0.2);
}

.login-prompt-header h3 {
  margin: 0;
  color: var(--primary-color);
  font-size: 1.5rem;
  font-weight: 700;
}

.close-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 50%;
  background: 
    linear-gradient(135deg, 
      #e74c3c 0%, 
      #c0392b 100%
    );
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.2rem;
  font-weight: bold;
  transition: all 0.3s ease;
  box-shadow: 0 3px 10px rgba(231, 76, 60, 0.4);
  
  &:hover {
    background: 
      linear-gradient(135deg, 
        #c0392b 0%, 
        #a93226 100%
      );
    transform: scale(1.1);
    box-shadow: 0 5px 15px rgba(231, 76, 60, 0.6);
  }
}

.login-prompt-content {
  padding: 2rem;
  text-align: center;
}

.login-prompt-icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

.login-prompt-content h4 {
  color: var(--text-color);
  margin: 0 0 1rem 0;
  font-size: 1.3rem;
}

.login-prompt-content p {
  color: #666;
  margin: 0 0 1rem 0;
  font-size: 1rem;
}

.login-prompt-content ul {
  text-align: left;
  margin: 1rem 0 2rem 0;
  padding: 1rem 1.5rem;
  background: rgba(140, 120, 83, 0.05);
  border-radius: 10px;
  border-left: 4px solid var(--primary-color);
}

.login-prompt-content li {
  margin: 0.5rem 0;
  color: var(--text-color);
  font-size: 0.95rem;
  line-height: 1.4;
}

.login-prompt-actions {
  display: flex;
  gap: 1rem;
  justify-content: center;
}

.login-prompt-actions .btn {
  padding: 1rem 2rem;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 10px;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  min-width: 120px;
  justify-content: center;
}

.login-prompt-actions .btn.btn-primary {
  background: 
    linear-gradient(135deg, 
      var(--primary-color) 0%, 
      var(--secondary-color) 100%
    );
  color: white;
  
  &:hover {
    background: 
      linear-gradient(135deg, 
        #9d8964 0%, 
        #7f6884 100%
      );
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(140, 120, 83, 0.3);
  }
}

.login-prompt-actions .btn.btn-secondary {
  background: 
    linear-gradient(135deg, 
      rgba(255, 255, 255, 0.9) 0%, 
      rgba(248, 245, 240, 0.8) 100%
    );
  color: var(--primary-color);
  border: 2px solid rgba(140, 120, 83, 0.3);
  
  &:hover {
    background: 
      linear-gradient(135deg, 
        rgba(140, 120, 83, 0.1) 0%, 
        rgba(110, 87, 115, 0.1) 100%
      );
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(140, 120, 83, 0.2);
  }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes scaleIn {
  from { 
    transform: scale(0.9);
    opacity: 0;
  }
  to { 
    transform: scale(1);
    opacity: 1;
  }
}

// 移动端适配
@media (max-width: 768px) {
  .login-prompt-modal {
    margin: 1rem;
    width: calc(100% - 2rem);
  }
  
  .login-prompt-header {
    padding: 1rem 1.5rem;
  }
  
  .login-prompt-content {
    padding: 1.5rem;
  }
  
  .login-prompt-actions {
    flex-direction: column;
    
    .btn {
      width: 100%;
    }
  }
}


// 🔧 新增：退出确认弹窗样式 - 最外层覆盖
.exit-modal-overlay {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
  background: 
    radial-gradient(circle at 30% 40%, rgba(140, 120, 83, 0.15) 0%, transparent 50%),
    radial-gradient(circle at 70% 60%, rgba(110, 87, 115, 0.15) 0%, transparent 50%),
    rgba(0, 0, 0, 0.85) !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  z-index: 999999 !important;
  animation: fadeIn 0.3s ease-out;
  padding: 1rem;
  pointer-events: auto !important;
  overflow-y: auto !important;
  transform: translateZ(0);
  will-change: transform;
}

.exit-modal {
  background: 
    linear-gradient(135deg, 
      rgba(255, 255, 255, 0.98) 0%, 
      rgba(248, 245, 240, 0.95) 100%
    ) !important;
  border-radius: 16px !important;
  box-shadow: 
    0 25px 50px rgba(0, 0, 0, 0.4),
    0 10px 25px rgba(140, 120, 83, 0.3),
    0 0 0 1px rgba(140, 120, 83, 0.2) !important;
  max-width: 450px;
  width: 90%;
  max-height: 90vh;
  overflow: hidden;
  animation: scaleIn 0.3s ease-out;
  backdrop-filter: blur(15px);
  border: 2px solid rgba(140, 120, 83, 0.3);
  position: relative !important;
  z-index: 1000000 !important;
  transform: translateZ(0);
  will-change: transform;
}

.exit-modal-header {
  padding: 1.5rem 1.5rem 1rem 1.5rem;
  text-align: center;
  border-bottom: 1px solid rgba(140, 120, 83, 0.15);
  background: 
    linear-gradient(90deg, 
      rgba(140, 120, 83, 0.08) 0%, 
      rgba(110, 87, 115, 0.08) 100%
    );
  
  h3 {
    margin: 0;
    color: var(--primary-color);
    font-size: 1.3rem;
    font-weight: 700;
    @include ancient-title;
  }
}

.exit-modal-content {
  padding: 1.5rem;
  
  .exit-warning {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    padding: 1rem;
    background: rgba(231, 76, 60, 0.12);
    border-radius: 8px;
    border-left: 4px solid #e74c3c;
    margin-bottom: 1.5rem;
    
    i {
      color: #e74c3c;
      font-size: 1.2rem;
      flex-shrink: 0;
    }
    
    p {
      margin: 0;
      color: #c0392b;
      font-weight: 500;
      font-size: 0.9rem;
    }
  }
  
  .current-progress {
    background: rgba(140, 120, 83, 0.08);
    padding: 1rem;
    border-radius: 8px;
    border: 1px solid rgba(140, 120, 83, 0.15);
    
    .progress-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 0.75rem;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .label {
        color: #666;
        font-size: 0.9rem;
        font-weight: 500;
      }
      
      .value {
        color: var(--primary-color);
        font-weight: 600;
        font-size: 0.9rem;
      }
    }
  }
}

.exit-modal-actions {
  display: flex;
  gap: 1rem;
  padding: 1rem 1.5rem 1.5rem 1.5rem;
  border-top: 1px solid rgba(140, 120, 83, 0.15);
  background: rgba(248, 245, 240, 0.5);
  
  .btn {
    flex: 1;
    padding: 0.75rem 1rem;
    font-size: 0.9rem;
    font-weight: 600;
    border-radius: 8px;
    border: none;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    position: relative;
    z-index: 1000001 !important;
  }
  
  .btn-secondary {
    background: 
      linear-gradient(135deg, 
        rgba(255, 255, 255, 0.95) 0%, 
        rgba(248, 245, 240, 0.9) 100%
      );
    color: var(--primary-color);
    border: 2px solid rgba(140, 120, 83, 0.4) !important;
    
    &:hover {
      background: 
        linear-gradient(135deg, 
          rgba(140, 120, 83, 0.15) 0%, 
          rgba(110, 87, 115, 0.15) 100%
        );
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(140, 120, 83, 0.3);
    }
  }
  
  .btn-danger {
    background: linear-gradient(135deg, #e74c3c, #c0392b) !important;
    color: white !important;
    
    &:hover {
      background: linear-gradient(135deg, #c0392b, #a93226) !important;
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(231, 76, 60, 0.4);
    }
  }
}

// 动画效果
@keyframes fadeIn {
  from { 
    opacity: 0; 
    backdrop-filter: blur(0px);
  }
  to { 
    opacity: 1; 
    backdrop-filter: blur(15px);
  }
}

@keyframes scaleIn {
  from { 
    opacity: 0; 
    transform: scale(0.85) translateY(-20px);
  }
  to { 
    opacity: 1; 
    transform: scale(1) translateY(0);
  }
}

// 移动端适配
@media (max-width: 768px) {
  .exit-modal {
    margin: 1rem;
    width: calc(100% - 2rem);
    max-width: none;
  }
  
  .exit-modal-actions {
    flex-direction: column;
    
    .btn {
      width: 100%;
    }
  }
  
  .exit-modal-header {
    padding: 1rem 1.5rem 0.75rem 1.5rem;
    
    h3 {
      font-size: 1.1rem;
    }
  }
  
  .exit-modal-content {
    padding: 1rem 1.5rem;
  }
}

</style>