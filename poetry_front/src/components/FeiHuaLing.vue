<template>
  <div class="feihua-game-container">
    <!-- 游戏模式选择阶段 -->
    <GameModeSelector
      v-if="gameState === 'mode-selection'"
      :game-stats="gameStats"
      :is-loading="isLoadingStats"
      @mode-selected="onModeSelected"
      @start-game="startGame"
    />
    
    <!-- 游戏进行阶段 -->
    <div v-else-if="gameState === 'playing'" class="game-playing-layout">
      <!-- 🔧 游戏头部 - 固定在顶部 -->
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
</template>

<!-- script 部分保持不变 -->
<script>
import axios from 'axios'
import API_BASE_URL from '@/config/api'

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
  data() {
    return {
      // 游戏状态
      gameState: 'mode-selection', // 'mode-selection', 'playing', 'ended'
      
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
  async mounted() {
    await this.loadGameStats()
    this.initFloatingElements()
    this.userId = this.getCurrentUserId() // 获取当前用户ID
  },
  beforeUnmount() {
    this.clearTimers()
  },
  methods: {
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
    
    // 模式选择处理
    onModeSelected(selection) {
      this.selectedMode = selection.mode
      this.selectedDifficulty = selection.difficulty
    },
    
    // 开始游戏
    startGame(gameConfig) {
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
    
    // 开始计时器
    startTimers() {
      // 游戏总时间计时器
      this.gameTimer = setInterval(() => {
        this.gameTime++
      }, 1000)
      
      // 倒计时计时器
      this.countdownTimer = setInterval(() => {
        this.countdown--
        if (this.countdown <= 0) {
          this.timeUp()
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
    
    // 处理用户消息
    async handleMessage(message) {
      // 添加用户消息
      this.messages.push({
        type: 'user',
        text: message,
        timestamp: Date.now()
      })
      
      // 验证诗句
      await this.validatePoetry(message)
    },
    
    // 验证诗句
    async validatePoetry(message) {
      this.isValidating = true
      
      try {
        // 模拟验证API调用
        await new Promise(resolve => setTimeout(resolve, 1000))
        
        const isValid = message.includes(this.currentKeyword) && message.length >= 5
        const isCorrect = Math.random() > 0.3 // 模拟70%正确率
        
        if (isValid && isCorrect) {
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
    
    // 提交分数
    async submitScore() {
      if (!this.userId) return
      
      try {
        const payload = {
          userId: this.userId,
          playerName: '用户' + this.userId, // 从用户信息获取
          score: this.finalScore,
          mode: this.selectedMode,
          difficulty: this.selectedDifficulty,
          keywordsUsed: '月,花,春' // 收集实际使用的关键词
        }
        
        await axios.post(`${API_BASE_URL}/api/feihua/submit-score`, payload)
      } catch (error) {
        console.error('提交分数失败:', error)
      }
    },
    
    // 请求提示
    requestHint() {
      if (this.hintCount <= 0) return
      
      this.hintCount--
      // 模拟提示
      const hints = [
        '明月几时有，把酒问青天',
        '花间一壶酒，独酌无相亲',
        '春眠不觉晓，处处闻啼鸟'
      ]
      
      const hint = hints[Math.floor(Math.random() * hints.length)]
      this.messages.push({
        type: 'system',
        text: `提示：${hint}`,
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
    
    // 重置游戏数据
    resetGameData() {
      this.selectedMode = ''
      this.selectedDifficulty = ''
      this.currentKeyword = '月'
      this.answerCount = 0
      this.gameTime = 0
      this.finalScore = 0
      this.messages = []
      this.clearTimers()
    },
    
    // 工具方法
    formatTime(seconds) {
      const mins = Math.floor(seconds / 60)
      const secs = seconds % 60
      return `${mins}:${secs.toString().padStart(2, '0')}`
    },
    
    getCurrentUserId() {
      // 从 Vuex store 或 localStorage 获取用户ID
      return localStorage.getItem('userId') ? parseInt(localStorage.getItem('userId')) : null
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
    }
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
}

// 🚀 重构：游戏进行时的布局
.game-playing-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: transparent;
  position: relative;
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
  z-index: 10;
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
  z-index: 10;
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
</style>