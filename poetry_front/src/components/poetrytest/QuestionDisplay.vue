<template>
  <div class="feihua-component question-display-wrapper">
    <!-- 🔧 添加题目存在性检查 -->
    <div v-if="currentQuestion" class="component-container">
      <!-- 头部信息区域 -->
      <div class="game-header-section">
        <div class="progress-info">
          <div class="question-counter">
            第 {{ currentIndex + 1 }} 题 / 共 {{ totalQuestions }} 题
          </div>
          <div class="timer-display">
            <i class="icon-clock"></i>
            {{ formatTime(elapsedSeconds) }}
          </div>
        </div>
        
        <!-- 🔧 添加退出按钮 -->
        <div class="exit-button-container">
          <button class="exit-button" @click="showExitModal = true">
            <i class="icon-times"></i>
            退出
          </button>
        </div>
        
        <!-- 🔧 优化后的进度条 -->
        <div class="progress-bar-container">
          <div class="progress-bar">
            <div 
              class="progress-fill"
              :style="{ width: progressPercent + '%' }"
            ></div>
            <!-- 🔧 优化毛笔显示 -->
            <div 
              class="progress-brush"
              :style="{ left: 'calc(' + progressPercent + '% - 25px)' }"
            >
              <img src="@/assets/image/imgtest/brush.png" alt="毛笔" />
            </div>
          </div>
          <div class="progress-text">
            {{ Math.round(progressPercent) }}% 完成
          </div>
        </div>
      </div>

      <!-- 🎨 题目卡片区域 - 仿照TestModeSelector的卡片样式 -->
      <div class="question-main-area">
        <div class="question-card">
          <div class="question-header">
            <div class="question-icon">
              <i class="icon-question-circle"></i>
            </div>
            <h2 class="question-title">{{ currentQuestion.question }}</h2>
          </div>
          
          <!-- 普通选择题 -->
          <div 
            v-if="!currentQuestion.type || currentQuestion.type === 'normal'"
            class="options-section"
          >
            <div 
              v-for="(option, key) in currentQuestion.options"
              :key="key"
              class="option-card"
              :class="{ 
                active: selectedAnswer === key,
                correct: showResult && isCorrectOption(key),
                wrong: showResult && selectedAnswer === key && !isCorrectOption(key),
                disabled: isAnswered
              }"
              @click="selectOption(key)"
            >
              <div class="option-marker">{{ key.toUpperCase() }}</div>
              <div class="option-content">{{ option }}</div>
              <div class="option-indicator">
                <i 
                  v-if="showResult && isCorrectOption(key)"
                  class="icon-check-circle"
                ></i>
                <i 
                  v-else-if="showResult && selectedAnswer === key && !isCorrectOption(key)"
                  class="icon-times-circle"
                ></i>
                <div 
                  v-else 
                  class="selection-dot"
                  :class="{ active: selectedAnswer === key }"
                ></div>
              </div>
              <!-- 选中状态指示器 -->
              <div class="selection-indicator" v-if="selectedAnswer === key">
                <i class="icon-check-circle"></i>
              </div>
            </div>
          </div>

          <!-- 对句题 -->
          <div 
            v-if="currentQuestion.type === 'duiju'"
            class="special-question-section"
          >
            <div class="prompt-card">
              <div class="prompt-icon">
                <i class="icon-lightbulb"></i>
              </div>
              <p class="prompt-text">
                {{ currentQuestion.duijuPrompt || '请选择合适的字词完成对句' }}
              </p>
            </div>
            
            <div class="answer-area">
              <div class="answer-slots">
                <div 
                  v-for="(char, index) in duijuAnswer"
                  :key="index"
                  class="answer-slot"
                  :class="{ filled: char !== '', empty: char === '' }"
                >
                  <span class="slot-content">{{ char || '？' }}</span>
                </div>
              </div>
              
              <div class="option-buttons">
                <button
                  v-for="option in currentQuestion.duijuOptions"
                  :key="option"
                  class="option-button"
                  :class="{ used: duijuAnswer.includes(option), active: !duijuAnswer.includes(option) }"
                  @click="selectDuijuChar(option)"
                  :disabled="isAnswered || duijuAnswer.includes(option)"
                >
                  {{ option }}
                </button>
              </div>
            </div>
          </div>

          <!-- 字词识诗题 -->
          <div 
            v-if="currentQuestion.type === 'zici'"
            class="special-question-section"
          >
            <div class="prompt-card">
              <div class="prompt-icon">
                <i class="icon-edit"></i>
              </div>
              <p class="prompt-text">
                {{ currentQuestion.ziciPrompt || '请选择正确的字词补全诗句' }}
              </p>
            </div>
            
            <div class="answer-area">
              <div class="answer-slots">
                <div 
                  v-for="(char, index) in ziciAnswer"
                  :key="index"
                  class="answer-slot"
                  :class="{ filled: char !== '', empty: char === '' }"
                >
                  <span class="slot-content">{{ char || '？' }}</span>
                </div>
              </div>
              
              <div class="option-buttons">
                <button
                  v-for="option in currentQuestion.ziciOptions"
                  :key="option"
                  class="option-button"
                  :class="{ used: ziciAnswer.includes(option), active: !ziciAnswer.includes(option) }"
                  @click="selectZiciChar(option)"
                  :disabled="isAnswered || ziciAnswer.includes(option)"
                >
                  {{ option }}
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 🎨 操作按钮区域 - 仿照TestModeSelector的按钮样式 -->
      <div class="action-section">
        <button
          v-if="!isAnswered && (currentQuestion.type === 'normal' || !currentQuestion.type)"
          class="btn btn-primary action-btn"
          @click="submitAnswer"
          :disabled="!selectedAnswer"
        >
          <i class="icon-paper-plane"></i>
          提交答案
        </button>
        
        <button
          v-if="!isAnswered && (currentQuestion.type === 'duiju' || currentQuestion.type === 'zici')"
          class="btn btn-primary action-btn"
          @click="submitSpecialAnswer"
          :disabled="!canSubmitSpecial"
        >
          <i class="icon-paper-plane"></i>
          提交答案
        </button>
        
        <!-- 🔧 合并结果反馈和下一题按钮 -->
        <div v-if="isAnswered" class="result-action-merged">
          <!-- 结果反馈区域 -->
          <div 
            class="result-card"
            :class="{ correct: lastAnswerCorrect, wrong: !lastAnswerCorrect }"
          >
            <div class="result-header">
              <div class="result-icon">
                <i :class="lastAnswerCorrect ? 'icon-check-circle' : 'icon-times-circle'"></i>
              </div>
              <h3 class="result-title">
                {{ lastAnswerCorrect ? '回答正确！' : '回答错误' }}
              </h3>
            </div>
            
            <div class="result-content">
              <div v-if="!lastAnswerCorrect" class="correct-answer">
                <span class="answer-label">正确答案：</span>
                <span class="answer-text">{{ correctAnswerText }}</span>
              </div>
              
              <div v-if="currentQuestion.explanation" class="explanation">
                <div class="explanation-header">
                  <i class="icon-info-circle"></i>
                  <span>解析</span>
                </div>
                <p class="explanation-text">{{ currentQuestion.explanation }}</p>
              </div>
            </div>
          </div>
          
          <!-- 下一题按钮 -->
          <div class="next-button-container">
            <button
              v-if="currentIndex < totalQuestions - 1"
              class="btn btn-primary action-btn ready"
              @click="nextQuestion"
            >
              下一题
              <i class="icon-arrow-right"></i>
            </button>
            
            <button
              v-if="currentIndex === totalQuestions - 1"
              class="btn btn-primary action-btn ready"
              @click="finishTest"
            >
              <i class="icon-flag-checkered"></i>
              完成测试
            </button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 🔧 退出确认弹窗 -->
    <div v-if="showExitModal" class="modal-overlay" @click="handleModalOverlayClick">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>退出确认</h3>
          <button class="modal-close" @click="showExitModal = false">
            <i class="icon-times"></i>
          </button>
        </div>
        <div class="modal-body">
          <p>您确定要退出当前测试吗？</p>
          <p class="warning-text">退出后当前进度将丢失，无法恢复。</p>
        </div>
        <div class="modal-footer">
          <button class="btn btn-secondary" @click="showExitModal = false">
            取消
          </button>
          <button class="btn btn-danger" @click="confirmExit">
            确认退出
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'QuestionDisplay',
  props: {
    currentQuestion: {
      type: Object,
      required: true
    },
    currentIndex: {
      type: Number,
      required: true
    },
    totalQuestions: {
      type: Number,
      required: true
    },
    elapsedSeconds: {
      type: Number,
      default: 0
    },
    isAnswered: {
      type: Boolean,
      default: false
    }
  },
  
  emits: ['answer-selected', 'next-question', 'back-to-selector', 'pause-timer', 'resume-timer'],
  
  data() {
    return {
      selectedAnswer: null,
      duijuAnswer: [],
      ziciAnswer: [],
      showResult: false,
      lastAnswerCorrect: false,
      correctAnswerText: '',
      showExitModal: false
    }
  },
  
  computed: {
    // 🔧 优化进度条百分比计算
    progressPercent() {
      if (!this.totalQuestions || this.totalQuestions === 0) {
        return 0
      }
      
      // 当前题目的进度 = (当前题目索引 + 1) / 总题目数 * 100
      const progress = ((this.currentIndex + 1) / this.totalQuestions) * 100
      
      // 确保进度在0-100之间
      return Math.max(0, Math.min(100, progress))
    },
    
    canSubmitSpecial() {
      if (this.currentQuestion.type === 'duiju') {
        return this.duijuAnswer.every(char => char !== '')
      }
      if (this.currentQuestion.type === 'zici') {
        return this.ziciAnswer.every(char => char !== '')
      }
      return false
    }
  },
  
  watch: {
    currentQuestion: {
      handler(newQuestion) {
        this.resetAnswerState()
        this.initializeSpecialAnswers()
      },
      immediate: true
    },
    
    isAnswered(newValue) {
      if (!newValue) {
        this.showResult = false
      }
    },
    
    // 🔧 监听弹窗状态，控制计时器
    showExitModal(newValue) {
      if (newValue) {
        this.$emit('pause-timer')
      } else {
        this.$emit('resume-timer')
      }
    }
  },
  
  methods: {
    resetAnswerState() {
      this.selectedAnswer = null
      this.showResult = false
      this.lastAnswerCorrect = false
      this.correctAnswerText = ''
    },
    
    initializeSpecialAnswers() {
      if (this.currentQuestion.type === 'duiju') {
        this.duijuAnswer = new Array(this.currentQuestion.duijuAnswer?.length || 0).fill('')
      }
      if (this.currentQuestion.type === 'zici') {
        this.ziciAnswer = new Array(this.currentQuestion.ziciAnswer?.length || 0).fill('')
      }
    },
    
    selectOption(key) {
      if (this.isAnswered) return
      this.selectedAnswer = key
    },
    
    selectDuijuChar(char) {
      if (this.isAnswered) return
      
      const emptyIndex = this.duijuAnswer.findIndex(c => c === '')
      if (emptyIndex !== -1) {
        this.duijuAnswer[emptyIndex] = char
      }
    },
    
    selectZiciChar(char) {
      if (this.isAnswered) return
      
      const emptyIndex = this.ziciAnswer.findIndex(c => c === '')
      if (emptyIndex !== -1) {
        this.ziciAnswer[emptyIndex] = char
      }
    },
    
    submitAnswer() {
      if (!this.selectedAnswer) return
      
      const answerData = this.currentQuestion.answer || ''
      const correctOption = answerData.replace(/\d+$/, '')
      
      const isCorrect = this.selectedAnswer === correctOption
      
      this.lastAnswerCorrect = isCorrect
      this.correctAnswerText = `${correctOption.toUpperCase()}. ${this.currentQuestion.options[correctOption]}`
      
      this.showResult = true
      
      this.$emit('answer-selected', {
        userAnswer: this.selectedAnswer,
        correctAnswer: correctOption,
        isCorrect: isCorrect
      })
    },
    
    submitSpecialAnswer() {
      if (!this.canSubmitSpecial) return
      
      let userAnswer, correctAnswer, isCorrect
      
      if (this.currentQuestion.type === 'duiju') {
        userAnswer = this.duijuAnswer.join('')
        correctAnswer = this.currentQuestion.duijuAnswer
        isCorrect = userAnswer === correctAnswer
      } else if (this.currentQuestion.type === 'zici') {
        userAnswer = this.ziciAnswer.join('')
        correctAnswer = this.currentQuestion.ziciAnswer
        isCorrect = userAnswer === correctAnswer
      }
      
      this.lastAnswerCorrect = isCorrect
      this.correctAnswerText = correctAnswer
      this.showResult = true
      
      this.$emit('answer-selected', {
        userAnswer,
        correctAnswer,
        isCorrect
      })
    },
    
    isCorrectOption(key) {
      const answerData = this.currentQuestion.answer || ''
      const correctOption = answerData.replace(/\d+$/, '')
      return key === correctOption
    },
    
    nextQuestion() {
      this.$emit('next-question')
    },
    
    finishTest() {
      this.$emit('finish-test')
    },
    
    formatTime(seconds) {
      const min = Math.floor(seconds / 60)
      const sec = seconds % 60
      return `${min}:${sec.toString().padStart(2, '0')}`
    },
    
    // 🔧 退出相关方法
    handleModalOverlayClick() {
      this.showExitModal = false
    },
    
    confirmExit() {
      this.showExitModal = false
      this.$emit('back-to-selector')
    }
  }
}
</script>

<style lang="scss" scoped>
// 导入飞花令样式 - 和TestModeSelector保持一致
@import '../feihualing/styles/game-common.scss';

// 🎨 主容器 - 完全仿照TestModeSelector
.question-display-wrapper {
  // 删除 @extend .feihua-component; 这一行
  min-height: 100vh;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  position: static;
  transform: none;
  background: transparent !important; // 🔧 强制透明背景
  // 不设置任何背景相关属性
}

.component-container {
  max-width: 1200px;
  width: 100%;
  padding: 1rem;
  position: relative;
  z-index: 1;
}

// 🎨 游戏标题区域 - 仿照TestModeSelector
.game-header-section {
  text-align: center;
  margin-bottom: 1.5rem;
  position: relative;
  
  .game-title {
    @include ancient-title;
    font-size: 2.5rem;
    color: var(--primary-color);
    margin-bottom: 1.5rem;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 1rem;
    
    i {
      color: var(--secondary-color);
      font-size: 2rem;
    }
  }
  
  .progress-info {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1rem;
    
    .question-counter {
      font-size: 1rem;
      font-weight: 600;
      color: var(--text-color);
    }
    
    .timer-display {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 1rem;
      font-weight: 600;
      color: var(--text-color);
      background: rgba(255, 255, 255, 0.8);
      padding: 0.4rem 0.8rem;
      border-radius: 20px;
      border: 2px solid rgba(140, 120, 83, 0.2);
      
      i {
        color: var(--warning-color);
        font-size: 1rem;
      }
    }
  }
  
  // 🔧 退出按钮样式
  .exit-button-container {
    position: absolute;
    top: 0;
    right: 0;
    
    .exit-button {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.5rem 1rem;
      background: rgba(231, 76, 60, 0.9);
      color: white;
      border: none;
      border-radius: 8px;
      font-size: 1rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.3s ease;
      
      &:hover {
        background: rgba(231, 76, 60, 1);
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(231, 76, 60, 0.3);
      }
      
      i {
        font-size: 1rem;
      }
    }
  }
  
  // 🔧 优化进度条样式
.progress-bar-container {
  position: relative;
  margin-bottom: 1rem;
  margin-top: 1rem;
  
  .progress-bar {
    position: relative;
    width: 100%;
    height: 15px;
    background: linear-gradient(90deg, 
      rgba(140, 120, 83, 0.15) 0%, 
      rgba(140, 120, 83, 0.25) 100%
    );
    border-radius: 8px;
    overflow: visible;
    box-shadow: 
      inset 0 2px 4px rgba(0, 0, 0, 0.1),
      0 2px 8px rgba(0, 0, 0, 0.05);
    
    .progress-fill {
      height: 100%;
      background: linear-gradient(90deg, 
        var(--primary-color) 0%, 
        var(--secondary-color) 100%
      );
      border-radius: 8px;
      transition: width 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94);
      position: relative;
    }
    
    .progress-brush {
      position: absolute;
      top: -30px;
      transition: left 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94);
      z-index: 10;
      
      img {
        width: 50px;
        height: 50px;
        object-fit: contain;
        filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.25));
        animation: brushFloat 3s ease-in-out infinite alternate;
      }
      
      &::before {
        content: '';
        position: absolute;
        top: 45px;
        left: 50%;
        transform: translateX(-50%);
        width: 5px;
        height: 20px;
        background: linear-gradient(180deg, 
          rgba(140, 120, 83, 0.9) 0%,
          rgba(140, 120, 83, 0.6) 40%,
          transparent 100%
        );
        border-radius: 2px;
        animation: inkDrop 2s ease-in-out infinite;
      }
    }
  }
  
  .progress-text {
    text-align: center;
    margin-top: 1rem;
    font-size: 1rem;
    color: var(--text-color);
    font-weight: 600;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
  }
}
}



@keyframes brushFloat {
  0% {
    transform: translateY(0px) rotate(-1deg);
  }
  100% {
    transform: translateY(-4px) rotate(1deg);
  }
}

@keyframes inkDrop {
  0%, 100% {
    opacity: 0.9;
    transform: translateX(-50%) scaleY(1);
  }
  50% {
    opacity: 0.5;
    transform: translateX(-50%) scaleY(0.8);
  }
}

// 🔧 响应式适配
@media (max-width: 768px) {
  .progress-bar-container {
    .progress-brush {
      top: -25px !important;
      
      img {
        width: 40px !important;
        height: 40px !important;
      }
      
      &::before {
        top: 35px !important;
        height: 16px !important;
        width: 4px !important;
      }
    }
  }
}

@media (max-width: 480px) {
  .progress-bar-container {
    .progress-brush {
      top: -22px !important;
      
      img {
        width: 35px !important;
        height: 35px !important;
      }
      
      &::before {
        top: 30px !important;
        height: 14px !important;
        width: 3px !important;
      }
    }
  }
}

// 🎨 题目主区域 - 仿照TestModeSelector的选择区域
.question-main-area {
  margin-bottom: 1.5rem;
  
  .question-card {
    @include modern-card;
    padding: 1.2rem;          // 原来是 2.5rem，改为 1.5rem
    background: rgba(255, 255, 255, 0.95);
    border: 2px solid rgba(140, 120, 83, 0.2);
    
    .question-header {
      display: flex;
      align-items: flex-start;
      gap: 0.8rem;
      margin-bottom: 1.2rem;  // 原来是 2rem，改为 1.5rem
      
      .question-icon {
        font-size: 1.3rem;     // 原来是 2rem，改为 1.5rem
        color: var(--secondary-color);
        margin-top: 0.2rem;
        flex-shrink: 0;
      }
      
      .question-title {
        @include ancient-title;
        font-size: 1.1rem;     // 原来是 1.4rem，改为 1.2rem
        color: var(--primary-color);
        line-height: 1.6;
        margin: 0;
        flex: 1;
      }
    }
  }
}

// 🎨 选项区域 - 仿照TestModeSelector的卡片样式
.options-section {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
  
  .option-card {
    @include modern-card;
    padding: 0.8rem;
    cursor: pointer;
    transition: all 0.3s ease;
    background: rgba(255, 255, 255, 0.9);
    border: 2px solid transparent;
    display: flex;
    align-items: center;
    gap: 0.8rem;
    position: relative;
    
    &:hover:not(.disabled) {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
      border-color: rgba(140, 120, 83, 0.3);
    }
    
    // 选中状态 - 仿照TestModeSelector的active状态
    &.active {
      border: 3px solid var(--primary-color) !important;
      background: linear-gradient(135deg, 
        rgba(140, 120, 83, 0.08) 0%, 
        rgba(140, 120, 83, 0.15) 100%
      );
      transform: translateY(-2px);
      box-shadow: 
        0 10px 25px rgba(140, 120, 83, 0.25),
        0 0 0 3px rgba(140, 120, 83, 0.15);
    }
    
    &.correct {
      border-color: var(--success-color) !important;
      background: linear-gradient(135deg, 
        rgba(39, 174, 96, 0.08) 0%, 
        rgba(39, 174, 96, 0.15) 100%
      );
    }
    
    &.wrong {
      border-color: var(--error-color) !important;
      background: linear-gradient(135deg, 
        rgba(231, 76, 60, 0.08) 0%, 
        rgba(231, 76, 60, 0.15) 100%
      );
    }
    
    &.disabled {
      cursor: not-allowed;
      opacity: 0.7;
    }
    
    .option-marker {
      width: 35px;
      height: 35px;
      background: var(--primary-color);
      color: white;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 700;
      font-size: 1rem;
      flex-shrink: 0;
    }
    
    .option-content {
      flex: 1;
      font-size: 1rem;
      color: var(--text-color);
      line-height: 1.6;
      font-weight: 500;
    }
    
    .option-indicator {
      width: 24px;
      height: 24px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      
      .icon-check-circle {
        color: var(--success-color);
        font-size: 1.5rem;
      }
      
      .icon-times-circle {
        color: var(--error-color);
        font-size: 1.5rem;
      }
      
      .selection-dot {
        width: 12px;
        height: 12px;
        border: 2px solid rgba(140, 120, 83, 0.3);
        border-radius: 50%;
        transition: all 0.3s ease;
        
        &.active {
          background: var(--primary-color);
          border-color: var(--primary-color);
        }
      }
    }
    
    // 选中状态指示器 - 仿照TestModeSelector
    .selection-indicator {
      position: absolute;
      top: 1rem;
      right: 1rem;
      width: 30px;
      height: 30px;
      background: linear-gradient(135deg, var(--success-color), #2ecc71);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 1rem;
      box-shadow: 0 4px 12px rgba(39, 174, 96, 0.4);
      animation: checkmark 0.6s ease-out;
      
      i {
        font-size: 0.9rem;
      }
    }
  }
}

// 🎨 特殊题型区域 - 仿照TestModeSelector的卡片样式
.special-question-section {
  .prompt-card {
    @include modern-card;
    padding: 1.2rem;
    margin-bottom: 1.5rem;
    background: linear-gradient(135deg, 
      rgba(140, 120, 83, 0.05) 0%, 
      rgba(140, 120, 83, 0.1) 100%
    );
    border: 2px solid rgba(140, 120, 83, 0.2);
    display: flex;
    align-items: center;
    gap: 1rem;
    
    .prompt-icon {
      font-size: 1.5rem;
      color: var(--secondary-color);
    }
    
    .prompt-text {
      font-size: 1rem;
      color: var(--text-color);
      line-height: 1.6;
      margin: 0;
    }
  }
  
  .answer-area {
    .answer-slots {
      display: flex;
      justify-content: center;
      gap: 0.8rem;
      margin-bottom: 1.5rem;
      flex-wrap: wrap;
      
      .answer-slot {
        width: 50px;
        height: 50px;
        border: 2px solid rgba(140, 120, 83, 0.3);
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 1.3rem;
        font-weight: 700;
        color: var(--text-color);
        background: rgba(255, 255, 255, 0.9);
        transition: all 0.3s ease;
        
        &.filled {
          border-color: var(--primary-color);
          background: linear-gradient(135deg, 
            rgba(140, 120, 83, 0.1) 0%, 
            rgba(140, 120, 83, 0.05) 100%
          );
          color: var(--primary-color);
        }
        
        &.empty {
          border-style: dashed;
          color: rgba(140, 120, 83, 0.5);
        }
        
        .slot-content {
          font-family: 'KaiTi', 'STKaiti', serif;
        }
      }
    }
    
    .option-buttons {
      display: flex;
      justify-content: center;
      gap: 0.8rem;
      flex-wrap: wrap;
      
      .option-button {
        @include modern-card;
        min-width: 45px;
        height: 45px;
        padding: 0.5rem;
        border: 2px solid var(--primary-color);
        background: rgba(255, 255, 255, 0.9);
        color: var(--primary-color);
        font-size: 1.1rem;
        font-weight: 700;
        cursor: pointer;
        transition: all 0.3s ease;
        
        &.active:hover {
          transform: translateY(-2px);
          box-shadow: 0 4px 12px rgba(140, 120, 83, 0.3);
          background: var(--primary-color);
          color: white;
        }
        
        &.used {
          background: rgba(140, 120, 83, 0.3);
          color: white;
          border-color: rgba(140, 120, 83, 0.3);
          cursor: not-allowed;
          opacity: 0.6;
        }
        
        &:disabled {
          cursor: not-allowed;
          opacity: 0.6;
        }
      }
    }
  }
}

// 🎨 操作按钮区域 - 仿照TestModeSelector的按钮样式
.action-section {
  text-align: center;
  padding: 1.2rem;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  border: 2px solid rgba(140, 120, 83, 0.2);
  
  .action-btn {
    padding: 0.8rem 2rem;
    font-size: 1rem;
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    min-width: 180px;
    justify-content: center;
    
    background: linear-gradient(135deg, #95a5a6, #7f8c8d) !important;
    color: white !important;
    border: none !important;
    transition: all 0.3s ease;
    
    &.ready {
      background: linear-gradient(135deg, #8c7853, #6e5773) !important;
      animation: pulse 2s ease-in-out infinite alternate;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 24px rgba(140, 120, 83, 0.4);
      }
    }
    
    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
      transform: none !important;
    }
    
    &:hover:not(:disabled) {
      transform: translateY(-2px);
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
    }
  }
}

// 🔧 合并结果和下一题按钮的样式
.result-action-merged {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
  
  .result-card {
    @include modern-card;
    padding: 1.2rem;
    background: rgba(255, 255, 255, 0.95);
    border: 2px solid rgba(140, 120, 83, 0.2);
    margin-bottom: 0;
    
    &.correct {
      border-color: var(--success-color);
      background: linear-gradient(135deg, 
        rgba(39, 174, 96, 0.05) 0%, 
        rgba(39, 174, 96, 0.1) 100%
      );
    }
    
    &.wrong {
      border-color: var(--error-color);
      background: linear-gradient(135deg, 
        rgba(231, 76, 60, 0.05) 0%, 
        rgba(231, 76, 60, 0.1) 100%
      );
    }
    
    .result-header {
      display: flex;
      align-items: center;
      gap: 0.8rem;
      margin-bottom: 0.8rem;
      
      .result-icon {
        font-size: 1.3rem;
        
        .icon-check-circle {
          color: var(--success-color);
        }
        
        .icon-times-circle {
          color: var(--error-color);
        }
      }
      
      .result-title {
        @include ancient-title;
        font-size: 1.1rem;
        color: var(--primary-color);
        margin: 0;
      }
    }
    
    .result-content {
      .correct-answer {
        margin-bottom: 0.8rem;
        padding: 0.8rem;
        background: rgba(255, 255, 255, 0.8);
        border-radius: 8px;
        font-size: 0.9rem;
        
        .answer-label {
          font-weight: 600;
          color: var(--text-color);
        }
        
        .answer-text {
          font-weight: 700;
          color: var(--primary-color);
        }
      }
      
      .explanation {
        .explanation-header {
          display: flex;
          align-items: center;
          gap: 0.5rem;
          margin-bottom: 0.5rem;
          font-weight: 600;
          color: var(--primary-color);
          
          i {
            font-size: 1.2rem;
          }
        }
        
        .explanation-text {
          font-size: 0.9rem;
          line-height: 1.6;
          color: var(--text-color);
          margin: 0;
        }
      }
    }
  }
  
  .next-button-container {
    display: flex;
    justify-content: center;
  }
}

// 🔧 退出确认弹窗样式
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  
  .modal-content {
    background: white;
    border-radius: 16px;
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
    max-width: 500px;
    width: 90%;
    max-height: 90vh;
    overflow-y: auto;
    
    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1.5rem;
      border-bottom: 1px solid rgba(140, 120, 83, 0.2);
      
      h3 {
        margin: 0;
        color: var(--primary-color);
        font-size: 1.4rem;
        font-weight: 600;
      }
      
      .modal-close {
        background: none;
        border: none;
        font-size: 1.5rem;
        color: var(--text-color);
        cursor: pointer;
        padding: 0.5rem;
        border-radius: 50%;
        transition: all 0.3s ease;
        
        &:hover {
          background: rgba(140, 120, 83, 0.1);
        }
      }
    }
    
    .modal-body {
      padding: 2rem;
      
      p {
        margin: 0 0 1rem 0;
        color: var(--text-color);
        line-height: 1.6;
        font-size: 1.1rem;
        
        &.warning-text {
          color: var(--error-color);
          font-weight: 600;
          font-size: 1rem;
        }
      }
    }
    
    .modal-footer {
      display: flex;
      justify-content: flex-end;
      gap: 1rem;
      padding: 1.5rem;
      border-top: 1px solid rgba(140, 120, 83, 0.2);
      
      .btn {
        padding: 0.8rem 1.5rem;
        border: none;
        border-radius: 8px;
        font-size: 1rem;
        font-weight: 600;
        cursor: pointer;
        transition: all 0.3s ease;
        
        &.btn-secondary {
          background: #95a5a6;
          color: white;
          
          &:hover {
            background: #7f8c8d;
          }
        }
        
        &.btn-danger {
          background: #e74c3c;
          color: white;
          
          &:hover {
            background: #c0392b;
          }
        }
      }
    }
  }
}

// 🎨 动画效果 - 仿照TestModeSelector
@keyframes checkmark {
  0% {
    transform: scale(0) rotate(0deg);
    opacity: 0;
  }
  50% {
    transform: scale(1.2) rotate(180deg);
    opacity: 1;
  }
  100% {
    transform: scale(1) rotate(360deg);
    opacity: 1;
  }
}

@keyframes pulse {
  0% {
    box-shadow: 0 4px 16px rgba(140, 120, 83, 0.3);
  }
  100% {
    box-shadow: 0 8px 24px rgba(140, 120, 83, 0.6);
  }
}

// 🎨 响应式设计 - 仿照TestModeSelector
@media (max-width: 1024px) {
  .progress-info {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }
  
  .exit-button-container {
    position: static !important;
    text-align: center;
    margin-bottom: 1rem;
  }
}

@media (max-width: 768px) {
  .component-container {
    padding: 0 1rem;
  }
  
  .game-title {
    font-size: 2rem !important;
  }
  
  .question-card {
    padding: 1.5rem !important;
  }
  
  .question-header {
    flex-direction: column;
    text-align: center;
    gap: 1rem !important;
  }
  
  .option-card {
    padding: 1rem !important;
  }
  
  .answer-slots {
    gap: 0.5rem !important;
  }
  
  .answer-slot {
    width: 50px !important;
    height: 50px !important;
    font-size: 1.2rem !important;
  }
  
  .option-buttons {
    gap: 0.5rem !important;
  }
  
  .option-button {
    min-width: 45px !important;
    height: 45px !important;
    font-size: 1rem !important;
  }
  
  .modal-content {
    width: 95% !important;
    
    .modal-header {
      padding: 1rem !important;
    }
    
    .modal-body {
      padding: 1.5rem !important;
    }
    
    .modal-footer {
      padding: 1rem !important;
      flex-direction: column;
      
      .btn {
        width: 100%;
      }
    }
  }
}

@media (max-width: 480px) {
  .question-title {
    font-size: 1.2rem !important;
  }
  
  .option-content {
    font-size: 1rem !important;
  }
  
  .answer-slots {
    justify-content: center;
  }
  
  .option-buttons {
    justify-content: center;
  }
}
</style>