<!-- filepath: c:\Users\Administrator\Desktop\YunZhou_Rhymes\poetry_front\src\components\PoetryTest.vue -->
<template>
  <div class="poetry-test-wrapper">
    <!-- 模式选择阶段 -->
    <TestModeSelector 
      v-if="currentView === 'selector'"
      :difficulty-options="difficultyOptions"
      :question-count-options="questionCountOptions"
      :selected-difficulty="selectedDifficulty"
      :selected-question-count="selectedQuestionCount"
      @difficulty-changed="onDifficultyChanged"
      @question-count-changed="onQuestionCountChanged"
      @start-test="startTest"
      @show-ranking="showRanking"
      @show-stats="showStats"
      @back-to-center="handleBackToCenter"
    />

    <!-- 🔧 加载状态 -->
    <div v-else-if="currentView === 'loading'" class="loading-view">
      <div class="loading-spinner">
        <i class="icon-spinner icon-spin"></i>
      </div>
      <p class="loading-text">正在准备题目...</p>
    </div>

    <!-- 🔧 答题阶段 -->
    <QuestionDisplay
      v-else-if="currentView === 'testing' && currentQuestion"
      :current-question="currentQuestion"
      :current-index="currentIndex"
      :total-questions="totalQuestions"
      :elapsed-seconds="elapsedSeconds"
      :is-answered="isAnswered"
      @answer-selected="handleAnswerSelected"
      @next-question="nextQuestion"
      @finish-test="finishTest"
      @back-to-selector="backToSelector"
    />

    <!-- 🔧 结果显示阶段 -->
    <div v-else-if="currentView === 'result'" class="result-view">
      <ResultModal
        :show="true"
        :correct-count="correctCount"
        :total-questions="totalQuestions"
        :elapsed-seconds="elapsedSeconds"
        :is-new-record="isNewRecord"
        :answer-records="answerRecords"
        @close="closeResultModal"
        @restart="restartTest"
        @back-to-selector="backToSelector"
      />
    </div>

    <!-- 🔧 学习统计弹窗 -->
    <UserStatsPanel 
      :show="showStatsModal"
      @close="closeStatsModal"
    />

    <!-- 🔧 排行榜弹窗 -->
    <RankingModal
      :show="showRankingModal"
      :difficulty="selectedDifficulty"
      :question-count="selectedQuestionCount"
      :rank-list="rankList"
      :my-rank-info="myRankInfo"
      :loading="rankLoading"
      @close="closeRankingModal"
    />
  </div>
</template>

<script>
import TestModeSelector from './poetrytest/TestModeSelector.vue'
import QuestionDisplay from './poetrytest/QuestionDisplay.vue'
import ResultModal from './poetrytest/ResultModal.vue'
import RankingModal from './poetrytest/RankingModal.vue'
import UserStatsPanel from './poetrytest/UserStatsPanel.vue'
import API_BASE_URL from '@/config/api'
// 🔧 导入 auth 工具函数
import { isLoggedIn, requireLogin, getCurrentUid } from '@/utils/auth'
import axios from 'axios'
import { getCurrentUser } from '@/utils/auth'

export default {
  name: 'PoetryTest',
  components: {
    TestModeSelector,
    QuestionDisplay,
    ResultModal,
    RankingModal,
    UserStatsPanel
  },
  
  data() {
    return {
      // 🔧 统一视图状态管理
      currentView: 'selector', // 'selector', 'loading', 'testing', 'result'
      
      // 配置选项
      difficultyOptions: [
        { label: '简单', value: 1 },
        { label: '普通', value: 2 },
        { label: '困难', value: 3 }
      ],
      questionCountOptions: [10, 15, 20],
      
      // 当前选择
      selectedDifficulty: 1,
      selectedQuestionCount: 10,
      
      // 题目相关
      allQuestions: [],
      questions: [],
      currentIndex: 0,
      isAnswered: false,
      correctCount: 0,
      answerRecords: [],
      
      // 计时相关
      timer: null,
      startTime: null,
      elapsedSeconds: 0,
      
      // 弹窗状态
      showStatsModal: false,
      showRankingModal: false,
      
      // 排行榜数据
      rankList: [],
      myRankInfo: null,
      rankLoading: false,
      
      // 其他状态
      isNewRecord: false
    }
  },
  
  computed: {
    currentQuestion() {
      return this.questions[this.currentIndex] || null
    },
    
    totalQuestions() {
      return this.questions.length
    }
  },
  
  async mounted() {
    await this.loadQuestions()
  },
  
  methods: {
    // 🔧 加载题目
    async loadQuestions() {
      try {
        const response = await fetch('/questions.json')
        this.allQuestions = await response.json()
      } catch (error) {
        console.error('加载题目失败:', error)
        this.$message?.error('加载题目失败，请刷新页面重试')
      }
    },
    
    // 🔧 事件处理
    onDifficultyChanged(difficulty) {
      this.selectedDifficulty = difficulty
    },
    
    onQuestionCountChanged(count) {
      this.selectedQuestionCount = count
    },
    
    // 🔧 显示学习统计
    showStats() {
      requireLogin(
        () => {
          this.showStatsModal = true
        },
        () => {
          this.$message?.warning('请先登录后查看学习统计')
        }
      )
    },
    
    // 🔧 关闭学习统计弹窗
    closeStatsModal() {
      this.showStatsModal = false
    },
    
    // 🔧 显示排行榜
        // 🔧 显示排行榜
    async showRanking() {
      try {
        this.rankingLoading = true
        
        // 🔧 使用 axios 而不是 this.$http
        const response = await axios.post(`${API_BASE_URL}/compRec/rank`, {
          Difficulty: this.selectedDifficulty,
          Sum: this.selectedQuestionCount
        })
        
        if (response.data.success) {
          this.rankList = response.data.data || []
          
          // 🔧 计算当前用户的排名
          const currentUser = getCurrentUser()
          if (currentUser) {
            const currentUid = currentUser.uid
            const myRankIndex = this.rankList.findIndex(item => 
              String(item.UID) === String(currentUid)
            )
            
            if (myRankIndex !== -1) {
              // 🔧 找到用户，设置排名信息
              this.myRankInfo = {
                ...this.rankList[myRankIndex],
                rank: myRankIndex + 1  // 数组索引+1 = 排名
              }
            } else {
              // 🔧 用户不在前20名
              this.myRankInfo = null
            }
          }
          
          this.showRankingModal = true
        } else {
          console.error('获取排行榜失败:', response.data.message)
          alert('获取排行榜失败')
        }
      } catch (error) {
        console.error('排行榜请求错误:', error)
        alert('网络错误，请稍后重试')
      } finally {
        this.rankingLoading = false
      }
    },
    
    // 🔧 关闭排行榜弹窗
    closeRankingModal() {
      this.showRankingModal = false
    },
    
    // 🔧 获取排行榜数据
    async getRankingData() {
      try {
        const uid = getCurrentUid()
        if (!uid) return null
        
        const response = await fetch(`${API_BASE_URL}/compRec/rank`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            UID: uid,
            Difficulty: this.selectedDifficulty,
            Sum: this.selectedQuestionCount
          })
        })
        
        const data = await response.json()
        return {
          success: true,
          data: {
            rankings: data || [],
            myRank: data.find(item => String(item.UID) === String(uid))
          }
        }
      } catch (error) {
        console.error('获取排行榜数据失败:', error)
        return null
      }
    },
    
    // 🔧 开始测试
    async startTest() {
      if (!this.selectedDifficulty || !this.selectedQuestionCount) {
        this.$message?.warning('请选择难度和题目数量')
        return
      }
      
      this.currentView = 'loading'
      
      try {
        // 准备题目
        await this.prepareQuestions()
        
        // 开始测试
        this.currentView = 'testing'
        this.startTimer()
        this.resetTestState()
      } catch (error) {
        console.error('开始测试失败:', error)
        this.$message?.error('开始测试失败，请稍后重试')
        this.currentView = 'selector'
      }
    },
    
    // 🔧 准备题目
    async prepareQuestions() {
      return new Promise((resolve) => {
        const count = this.selectedQuestionCount
        const difficulty = this.selectedDifficulty
        
        // 题目分配逻辑
        const duijuQuestions = this.allQuestions.filter(q => q.type === 'duiju')
        const ziciQuestions = this.allQuestions.filter(q => q.type === 'zici')
        const normalQuestions = this.allQuestions.filter(q => !q.type || (q.type !== 'duiju' && q.type !== 'zici'))
        
        // 对句题：1-3题随机
        let duijuCount = Math.min(duijuQuestions.length, Math.max(1, Math.floor(Math.random() * 3) + 1))
        duijuCount = Math.min(duijuCount, count)
        
        // 字词题数量
        let ziciCount = 0
        if (difficulty === 2 || difficulty === 3) {
          ziciCount = Math.min(ziciQuestions.length, Math.floor(Math.random() * 2) + 1)
        }
        ziciCount = Math.min(ziciCount, count - duijuCount)
        
        const selectedDuiju = this.shuffleArray(duijuQuestions).slice(0, duijuCount)
        const selectedZici = this.shuffleArray(ziciQuestions).slice(0, ziciCount)
        
        // 其余题目按难度筛选
        const remainCount = count - duijuCount - ziciCount
        const choiceQuestions = normalQuestions.filter(q => !q.type)
        const easyQuestions = choiceQuestions.filter(q => q.id < "0050")
        const hardQuestions = choiceQuestions.filter(q => q.id >= "0050")
        
        let selectedOthers = []
        if (difficulty === 1) {
          selectedOthers = this.shuffleArray(easyQuestions).slice(0, remainCount)
        } else if (difficulty === 2) {
          const half = Math.floor(remainCount / 2)
          selectedOthers = [
            ...this.shuffleArray(easyQuestions).slice(0, half),
            ...this.shuffleArray(hardQuestions).slice(0, remainCount - half)
          ]
          selectedOthers = this.shuffleArray(selectedOthers)
        } else {
          selectedOthers = this.shuffleArray(hardQuestions).slice(0, remainCount)
        }
        
        this.questions = this.shuffleArray([...selectedDuiju, ...selectedZici, ...selectedOthers])
        
        // 模拟加载时间
        setTimeout(() => {
          resolve()
        }, 1000)
      })
    },
    
    // 🔧 数组洗牌
    shuffleArray(array) {
      const shuffled = [...array]
      for (let i = shuffled.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [shuffled[i], shuffled[j]] = [shuffled[j], shuffled[i]]
      }
      return shuffled
    },
    
    // 🔧 开始计时
    startTimer() {
      this.elapsedSeconds = 0
      this.startTime = Date.now()
      this.timer = setInterval(() => {
        this.elapsedSeconds = Math.floor((Date.now() - this.startTime) / 1000)
      }, 1000)
    },
    
    // 🔧 停止计时
    stopTimer() {
      if (this.timer) {
        clearInterval(this.timer)
        this.timer = null
      }
    },
    
    // 🔧 重置测试状态
    resetTestState() {
      this.currentIndex = 0
      this.correctCount = 0
      this.answerRecords = []
      this.isAnswered = false
      this.isNewRecord = false
    },
    
    // 🔧 处理答案选择
    handleAnswerSelected(answerData) {
      this.isAnswered = true
      
      // 记录答题详情
      this.answerRecords.push({
        question: this.currentQuestion.question,
        userAnswer: answerData.userAnswer,
        correctAnswer: answerData.correctAnswer,
        isCorrect: answerData.isCorrect,
        type: this.currentQuestion.type || 'normal',
        options: this.currentQuestion.options || {}
      })
      <QuestionDisplay
      if (answerData.isCorrect) {
        this.correctCount++
      }
      
    },
    
  // 🔧 下一题
  nextQuestion() {
    if (this.currentIndex < this.questions.length - 1) {
      this.currentIndex++
      this.isAnswered = false
    } else {
      // 如果是最后一题，完成测试
      this.finishTest()
    }
  },
    
    // 🔧 完成测试
    async finishTest() {
      this.stopTimer()
      
      // 提交成绩和统计
      await this.submitResults()
      
      // 显示结果
      this.currentView = 'result'
    },
    
    // 🔧 提交成绩
    async submitResults() {
      if (!isLoggedIn()) return
      
      try {
        const uid = getCurrentUid()
        if (!uid) return
        
        // 检查是否破纪录
        const oldRecord = await this.getOldRecord(uid)
        this.isNewRecord = this.correctCount > (oldRecord?.Max || 0)
        
        // 提交成绩
        const record = {
          UID: uid,
          Difficulty: this.selectedDifficulty,
          Sum: this.selectedQuestionCount,
          Max: this.correctCount,
          Mintime: this.elapsedSeconds
        }
        
        await fetch(`${API_BASE_URL}/compRec/submit`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(record)
        })
        
        // 更新用户统计
        await this.updateUserStats(uid)
      } catch (error) {
        console.error('提交成绩失败:', error)
      }
    },
    
    // 🔧 获取历史记录
    async getOldRecord(uid) {
      try {
        const response = await fetch(`${API_BASE_URL}/compRec/rank`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            UID: uid,
            Difficulty: this.selectedDifficulty,
            Sum: this.selectedQuestionCount
          })
        })
        const data = await response.json()
        return data.find(item => String(item.UID) === String(uid))
      } catch (error) {
        console.error('获取历史记录失败:', error)
        return null
      }
    },
    
    // 🔧 更新用户统计
    async updateUserStats(uid) {
      try {
        await fetch(`${API_BASE_URL}/compRec/update-stats`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            UID: uid,
            totalQuestions: this.selectedQuestionCount,
            correctQuestions: this.correctCount
          })
        })
      } catch (error) {
        console.error('更新用户统计失败:', error)
      }
    },
    
    // 🔧 返回选择器
    backToSelector() {
      this.currentView = 'selector'
      this.stopTimer()
      this.resetTestState()
    },
    
    // 🔧 关闭结果弹窗
    closeResultModal() {
      this.currentView = 'selector'
    },
    
    // 🔧 重新开始测试
    restartTest() {
      this.startTest()
    },
    
    // 🔧 返回游戏中心
    handleBackToCenter() {
      if (this.$router) {
        this.$router.push('/game-center')
        return
      }
      
      // 如果没有路由，直接刷新到游戏中心
      window.location.href = '/game-center'
    }
  },
  
  // 🔧 组件销毁时清理计时器
  beforeUnmount() {
    this.stopTimer()
  }
}
</script>

<style lang="scss" scoped>
.poetry-test-wrapper {
  width: 100%;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--poetry-bg, #f8f6f0);
  
  /* 🎨 添加背景图片和黄色滤镜 */
  background-image: url('../assets/1.jpg'); /* 请替换为你的背景图片路径 */
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
  
  /* 🎨 黄色滤镜叠加层 */
  position: relative;
  
  &::before {
    content: '';
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 248, 220, 0.6); /* 黄色滤镜 */
    pointer-events: none;
    z-index: 0;
  }
  
  /* 🎨 确保内容在滤镜之上 */
  > * {
    position: relative;
    z-index: 1;
  }
  
  /* 其他现有样式保持不变... */
  position: static !important;
  transform: none !important;
  z-index: auto !important;
  will-change: auto !important;
  contain: none !important;
  backdrop-filter: none !important;
}

.loading-view {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  
  .loading-spinner {
    font-size: 3rem;
    color: var(--poetry-primary, #8c7853);
    margin-bottom: 1rem;
    
    .icon-spin {
      animation: spin 1s linear infinite;
    }
  }
  
  .loading-text {
    font-size: 1.2rem;
    color: var(--poetry-text, #666);
    font-family: 'KaiTi', 'STKaiti', serif;
    text-align: center;
  }
}

.result-view {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .poetry-test-wrapper {
    padding: 0.5rem;
  }
  
  .loading-view {
    padding: 2rem;
    
    .loading-spinner {
      font-size: 2rem;
    }
    
    .loading-text {
      font-size: 1rem;
    }
  }
}
</style>