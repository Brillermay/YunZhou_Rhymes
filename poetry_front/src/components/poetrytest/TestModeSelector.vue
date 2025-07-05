<template>
  <div class="feihua-component mode-selector-container">
    <div class="component-container">
      <!-- 🎯 游戏标题 - 仿照飞花令简洁设计 -->
      <div class="game-header-section">
        <h1 class="game-title">
          <i class="icon-book-open"></i>
          问韵寻章
        </h1>
        <p class="game-subtitle">一卷诗书藏古意，半帘花影读春秋</p>
        
        <!-- 🏆 操作按钮 - 仿照飞花令布局 -->
        <div class="header-actions">
          <button 
            class="btn btn-outline action-btn"
            @click="$emit('show-stats')"
          >
            <i class="icon-chart-bar"></i>
            学习统计
          </button>
          <button 
            class="btn btn-outline action-btn"
            @click="$emit('show-ranking')"
          >
            <i class="icon-trophy"></i>
            排行榜
          </button>
          <!-- 🔧 新增：测试说明按钮 -->
          <button 
            class="btn btn-outline action-btn"
            @click="showTestInfo"
          >
            <i class="icon-info-circle"></i>
            测试说明
          </button>
          <!-- 🔧 新增：返回游戏中心按钮 -->
          <button 
            class="btn btn-outline action-btn"
            @click="$emit('back-to-center')"
          >
            <i class="icon-arrow-left"></i>
            返回中心
          </button>
        </div>
      </div>

      <!-- 🎮 主要选择区域 - 仿照飞花令的横向布局 -->
      <div class="selection-main-area">
        <!-- 左侧：难度选择 -->
        <div class="difficulty-selection-section">
          <h2 class="section-title">选择难度</h2>
          <div class="difficulty-grid">
            <div 
              v-for="difficulty in difficultyOptions" 
              :key="difficulty.value"
              class="difficulty-card"
              :class="{ active: selectedDifficulty === difficulty.value }"
              @click="selectDifficulty(difficulty.value)"
            >
              <div class="difficulty-icon">
                <i :class="getDifficultyIcon(difficulty.value)"></i>
              </div>
              <div class="difficulty-info">
                <span class="difficulty-name">{{ difficulty.label }}</span>
                <span class="difficulty-desc">{{ getDifficultyDesc(difficulty.value) }}</span>
              </div>
              <div 
                class="difficulty-badge"
                :class="getDifficultyClass(difficulty.value)"
              >
                {{ difficulty.label }}
              </div>
              <!-- 🔧 选中状态指示器 -->
              <div class="selection-indicator" v-if="selectedDifficulty === difficulty.value">
                <i class="icon-check-circle"></i>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：题目数量选择 -->
        <div class="question-count-section">
          <h2 class="section-title">题目数量</h2>
          <div class="question-count-grid">
            <div 
              v-for="count in questionCountOptions" 
              :key="count"
              class="count-card"
              :class="{ 
                active: selectedQuestionCount === count,
                disabled: !selectedDifficulty
              }"
              @click="selectQuestionCount(count)"
            >
              <div class="count-number">{{ count }}</div>
              <div class="count-label">题</div>
              <div class="count-time">约 {{ getEstimatedTime(count) }} 分钟</div>
              <!-- 🔧 选中状态指示器 -->
              <div class="selection-indicator" v-if="selectedQuestionCount === count">
                <i class="icon-check-circle"></i>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 🚀 开始游戏按钮 - 仿照飞花令样式 -->
      <div class="start-game-section">
        <button 
          class="btn btn-primary start-game-btn"
          :class="{ ready: canStart }"
          @click="startTest"
          :disabled="!canStart"
        >
          <i class="icon-play"></i>
          <span v-if="canStart">开始测试</span>
          <span v-else>请选择难度和题目数量</span>
        </button>
      </div>
    </div>

    <!-- 🎮 测试说明弹窗 - 修复定位问题 -->
    <Teleport to="body">
      <div v-if="showTestInfoModal" class="modal-overlay" @click="closeTestInfo">
        <div class="modal-container" @click.stop>
          <div class="modal-header">
            <h3>
              <i class="icon-info-circle"></i>
              测试说明
            </h3>
            <button class="modal-close-btn" @click="closeTestInfo">
              <i class="icon-times"></i>
            </button>
          </div>
          
          <div class="modal-content">
            <div class="info-steps">
              <div class="info-step">
                <div class="step-number">1</div>
                <div class="step-content">
                  <h4>选择难度</h4>
                  <p>根据个人水平选择合适的难度级别</p>
                </div>
              </div>
              <div class="info-step">
                <div class="step-number">2</div>
                <div class="step-content">
                  <h4>设置题量</h4>
                  <p>选择想要挑战的题目数量</p>
                </div>
              </div>
              <div class="info-step">
                <div class="step-number">3</div>
                <div class="step-content">
                  <h4>开始答题</h4>
                  <p>仔细阅读题目，选择正确答案</p>
                </div>
              </div>
              <div class="info-step">
                <div class="step-number">4</div>
                <div class="step-content">
                  <h4>查看结果</h4>
                  <p>测试结束后查看成绩和排行榜</p>
                </div>
              </div>
            </div>
            
            <div class="info-features">
              <div class="feature-item">
                <i class="icon-clock"></i>
                <span>无时间限制，但会记录答题用时</span>
              </div>
              <div class="feature-item">
                <i class="icon-check-circle"></i>
                <span>包含选择题、对句题、字词填空题</span>
              </div>
              <div class="feature-item">
                <i class="icon-award"></i>
                <span>答题结果会影响排行榜和个人统计</span>
              </div>
            </div>
          </div>
          
          <div class="modal-footer">
            <button class="btn btn-primary" @click="closeTestInfo">
              <i class="icon-check"></i>
              我知道了
            </button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script>
export default {
  name: 'TestModeSelector',
  // 🔧 明确定义 props
  props: {
    difficultyOptions: {
      type: Array,
      default: () => []
    },
    questionCountOptions: {
      type: Array,
      default: () => []
    },
    selectedDifficulty: {
      type: Number,
      default: 1
    },
    selectedQuestionCount: {
      type: Number,
      default: 10
    }
  },
  
  data() {
    return {
      showTestInfoModal: false
    }
  },
  
  emits: [
    'difficulty-changed',
    'question-count-changed',
    'start-test',
    'show-ranking',
    'show-stats',
    'back-to-center'
  ],
  
  computed: {
    canStart() {
      return this.selectedDifficulty && this.selectedQuestionCount
    }
  },
  
  methods: {
    selectDifficulty(difficulty) {
      this.$emit('difficulty-changed', difficulty)
    },
    
    selectQuestionCount(count) {
      if (!this.selectedDifficulty) return
      this.$emit('question-count-changed', count)
    },
    
    startTest() {
      if (this.canStart) {
        this.$emit('start-test')
      }
    },
    
    // 🔧 新增：显示测试说明弹窗
    showTestInfo() {
      this.showTestInfoModal = true
      // 🔧 防止页面滚动
      document.body.style.overflow = 'hidden'
    },
    
    // 🔧 新增：关闭测试说明弹窗
    closeTestInfo() {
      this.showTestInfoModal = false
      // 🔧 恢复页面滚动
      document.body.style.overflow = 'auto'
    },
    
    getDifficultyIcon(difficulty) {
      const icons = {
        1: 'icon-leaf',
        2: 'icon-fire',
        3: 'icon-bolt'
      }
      return icons[difficulty] || 'icon-star'
    },
    
    getDifficultyDesc(difficulty) {
      const descs = {
        1: '基础题目，适合入门',
        2: '中等难度，考验积累',
        3: '高难题目，挑战自我'
      }
      return descs[difficulty] || ''
    },
    
    getDifficultyClass(difficulty) {
      const classes = {
        1: 'easy',
        2: 'normal',
        3: 'hard'
      }
      return classes[difficulty] || 'normal'
    },
    
    getEstimatedTime(count) {
      return Math.ceil(count * 1.5)
    }
  },
  
  // 🔧 组件销毁时恢复页面滚动
  beforeUnmount() {
    document.body.style.overflow = 'auto'
  }
}
</script>

<style lang="scss" scoped>
// 导入飞花令样式
@import '../feihualing/styles/game-common.scss';

.mode-selector-container {
  @extend .feihua-component;
  min-height: 100vh;
  padding: 2rem 0;
  display: flex;
  align-items: center;
  justify-content: center;
  /* 🔧 确保不影响 fixed 定位 */
  position: static;
  transform: none;
}

.component-container {
  max-width: 1400px;
  width: 100%;
  padding: 0 2rem;
  /* 🔧 确保不创建新的定位上下文 */
  position: relative;
  z-index: 1;
}

// 🎯 游戏标题区域 - 仿照飞花令样式
.game-header-section {
  text-align: center;
  margin-bottom: 3rem;
}

.game-title {
  @include ancient-title;
  font-size: 3rem;
  color: var(--primary-color);
  margin-bottom: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  
  i {
    color: var(--secondary-color);
    font-size: 2.5rem;
  }
}

.game-subtitle {
  font-size: 1.2rem;
  color: #666;
  margin-bottom: 2rem;
  font-style: italic;
}

.header-actions {
  display: flex;
  justify-content: center;
  gap: 1rem;
  flex-wrap: wrap;
  
  @media (max-width: 768px) {
    flex-direction: column;
    align-items: center;
  }
}

.action-btn {
  padding: 0.75rem 1.5rem;
  font-size: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  min-width: 140px;
  justify-content: center;
  
  @media (max-width: 1024px) {
    min-width: 120px;
    padding: 0.6rem 1.2rem;
    font-size: 0.9rem;
  }
}

// 🎮 主要选择区域 - 仿照飞花令横向布局
.selection-main-area {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 3rem;
  margin-bottom: 3rem;
  
  @media (max-width: 1024px) {
    grid-template-columns: 1fr;
    gap: 2rem;
  }
}

// 🎯 难度选择区域 - 仿照飞花令卡片样式
.difficulty-selection-section {
  .section-title {
    @include ancient-title;
    text-align: center;
    margin-bottom: 2rem;
    font-size: 1.5rem;
    color: var(--primary-color);
  }
}

.difficulty-grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.difficulty-card {
  @include modern-card;
  padding: 1.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.9);
  border: 2px solid transparent;
  display: flex;
  align-items: center;
  gap: 1rem;
  position: relative;
  overflow: hidden;
  
  &:hover:not(.disabled) {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    border-color: rgba(140, 120, 83, 0.3);
  }
  
  // 🎨 选中状态 - 仿照飞花令的活跃状态
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
    
    .difficulty-icon i {
      color: var(--primary-color);
      transform: scale(1.2);
    }
    
    .difficulty-name {
      color: var(--primary-color);
      font-weight: 700;
    }
    
    .difficulty-desc {
      color: var(--primary-color);
      font-weight: 500;
    }
    
    // 🔧 左侧边框高亮效果
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      width: 6px;
      height: 100%;
      background: var(--primary-color);
      border-radius: 3px 0 0 3px;
    }
  }
  
  .difficulty-icon {
    font-size: 2rem;
    color: #bbb;
    transition: all 0.3s ease;
  }
  
  .difficulty-info {
    flex: 1;
    
    .difficulty-name {
      display: block;
      font-size: 1.2rem;
      font-weight: 600;
      color: var(--text-color);
      margin-bottom: 0.3rem;
      transition: all 0.3s ease;
    }
    
    .difficulty-desc {
      display: block;
      font-size: 0.9rem;
      color: #666;
      transition: all 0.3s ease;
    }
  }
  
  .difficulty-badge {
    padding: 0.4rem 0.8rem;
    border-radius: 15px;
    font-size: 0.8rem;
    font-weight: 600;
    color: white;
    position: absolute;
    top: 1rem;
    right: 3rem;
    
    &.easy {
      background: var(--success-color);
    }
    
    &.normal {
      background: var(--warning-color);
    }
    
    &.hard {
      background: var(--error-color);
    }
  }
}

// 🎯 题目数量选择区域 - 仿照飞花令样式
.question-count-section {
  .section-title {
    @include ancient-title;
    text-align: center;
    margin-bottom: 2rem;
    font-size: 1.5rem;
    color: var(--primary-color);
  }
}

.question-count-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 1rem;
}

.count-card {
  @include modern-card;
  padding: 1.5rem;
  cursor: pointer;
  text-align: center;
  position: relative;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.9);
  border: 2px solid transparent;
  
  &:hover:not(.disabled) {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
    border-color: rgba(140, 120, 83, 0.3);
  }
  
  // 🎨 选中状态 - 仿照飞花令风格
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
    
    .count-number {
      color: var(--primary-color);
      transform: scale(1.1);
    }
    
    .count-label {
      color: var(--primary-color);
      font-weight: 700;
    }
    
    .count-time {
      color: var(--primary-color);
      font-weight: 500;
    }
  }
  
  &.disabled {
    opacity: 0.5;
    cursor: not-allowed;
    background: rgba(240, 240, 240, 0.5);
  }
  
  .count-number {
    font-size: 2.5rem;
    font-weight: 700;
    color: var(--secondary-color);
    margin-bottom: 0.5rem;
    transition: all 0.3s ease;
  }
  
  .count-label {
    font-size: 1.1rem;
    color: var(--text-color);
    font-weight: 600;
    margin-bottom: 0.3rem;
    transition: all 0.3s ease;
  }
  
  .count-time {
    font-size: 0.9rem;
    color: #666;
    transition: all 0.3s ease;
  }
}

// 🔧 选中状态指示器 - 仿照飞花令样式
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

// 🚀 开始游戏区域 - 仿照飞花令样式
.start-game-section {
  text-align: center;
  padding: 2rem;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  border: 2px solid rgba(140, 120, 83, 0.2);
  margin-bottom: 3rem;
}

.start-game-btn {
  @extend .btn;
  @extend .btn-primary;
  padding: 1.2rem 3rem;
  font-size: 1.2rem;
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
  min-width: 280px;
  justify-content: center;
  
  background: linear-gradient(135deg, #95a5a6, #7f8c8d) !important;
  color: white !important;
  border: none !important;
  transition: all 0.3s ease;
  
  &.ready {
    background: linear-gradient(135deg, #8c7853, #6e5773) !important;
    animation: pulse 2s ease-in-out infinite alternate;
    
    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 10px 30px rgba(140, 120, 83, 0.4);
    }
  }
  
  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none !important;
  }
}

// 🎨 动画效果 - 仿照飞花令的动画
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

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 🎨 响应式设计
@media (max-width: 1200px) {
  .header-actions {
    gap: 0.75rem;
  }
  
  .action-btn {
    min-width: 110px;
    padding: 0.6rem 1rem;
    font-size: 0.85rem;
  }
}

@media (max-width: 1024px) {
  .selection-main-area {
    grid-template-columns: 1fr;
    gap: 2rem;
  }
  
  .question-count-grid {
    grid-template-columns: repeat(3, 1fr);
  }
  
  .header-actions {
    grid-template-columns: repeat(2, 1fr);
    gap: 0.5rem;
  }
}

@media (max-width: 768px) {
  .component-container {
    padding: 0 1rem;
  }
  
  .game-title {
    font-size: 2rem;
  }
  
  .difficulty-card {
    padding: 1.2rem;
  }
  
  .count-card {
    padding: 1.2rem;
  }
  
  .start-game-btn {
    padding: 1rem 2rem !important;
    font-size: 1.1rem !important;
    min-width: 240px !important;
  }
  
  .question-count-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .header-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .action-btn {
    width: 100%;
    max-width: 200px;
  }
}

@media (max-width: 480px) {
  .question-count-grid {
    grid-template-columns: 1fr;
  }
}
</style>

<!-- 🔧 弹窗样式 - 独立的全局样式 -->
<style lang="scss">
// 🎮 测试说明弹窗 - 修复定位问题的全局样式
.modal-overlay {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
  background: rgba(0, 0, 0, 0.8) !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  z-index: 99999 !important;
  padding: 1rem;
  backdrop-filter: blur(5px);
  animation: fadeIn 0.3s ease-out;
}

.modal-container {
  background: white !important;
  border-radius: 16px !important;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3) !important;
  max-height: 90vh;
  overflow-y: auto;
  width: 90vw;
  max-width: 800px;
  animation: slideIn 0.3s ease-out;
  position: relative !important;
  z-index: 100000 !important;
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.5rem 2rem;
  border-bottom: 2px solid rgba(140, 120, 83, 0.2);
  background: linear-gradient(135deg, 
    rgba(140, 120, 83, 0.05) 0%, 
    rgba(140, 120, 83, 0.1) 100%
  );
  border-radius: 16px 16px 0 0;
  
  h3 {
    font-family: 'KaiTi', 'STKaiti', serif;
    font-weight: 700;
    letter-spacing: 2px;
    text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    color: #8c7853;
    font-size: 1.5rem;
    margin: 0;
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }
}

.modal-close-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: rgba(231, 76, 60, 0.1);
  color: #e74c3c;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  
  &:hover {
    background: #e74c3c;
    color: white;
    transform: scale(1.1);
  }
  
  i {
    font-size: 1.2rem;
  }
}

.modal-content {
  padding: 2rem;
  
  .info-steps {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 1.5rem;
    margin-bottom: 2rem;
  }
  
  .info-step {
    display: flex;
    align-items: flex-start;
    gap: 1rem;
    padding: 1.5rem;
    background: rgba(140, 120, 83, 0.05);
    border-radius: 12px;
    border-left: 4px solid #8c7853;
    
    .step-number {
      width: 40px;
      height: 40px;
      background: #8c7853;
      color: white;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: bold;
      font-size: 1.2rem;
      flex-shrink: 0;
    }
    
    .step-content {
      h4 {
        margin: 0 0 0.5rem 0;
        color: #8c7853;
        font-size: 1.1rem;
      }
      
      p {
        margin: 0;
        color: #666;
        line-height: 1.6;
      }
    }
  }
  
  .info-features {
    display: grid;
    gap: 1rem;
    
    .feature-item {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 1rem;
      background: rgba(140, 120, 83, 0.05);
      border-radius: 8px;
      
      i {
        color: #8c7853;
        font-size: 1.2rem;
      }
      
      span {
        color: #333;
        font-weight: 500;
      }
    }
  }
}

.modal-footer {
  display: flex;
  justify-content: center;
  padding: 1.5rem 2rem;
  border-top: 2px solid rgba(140, 120, 83, 0.2);
  background: rgba(248, 246, 240, 0.5);
  border-radius: 0 0 16px 16px;
  
  .btn {
    min-width: 150px;
    padding: 0.75rem 1.5rem;
    font-size: 1rem;
    font-weight: 600;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    text-decoration: none;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    border: none;
    outline: none;
    position: relative;
    overflow: hidden;
    
    &.btn-primary {
      background: linear-gradient(135deg, #8c7853, #6e5773);
      color: white;
      box-shadow: 0 4px 16px rgba(140, 120, 83, 0.3);
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 8px 24px rgba(140, 120, 83, 0.4);
      }
      
      &:active {
        transform: translateY(0);
      }
    }
  }
}

// 🎨 响应式设计
@media (max-width: 768px) {
  .modal-container {
    width: 95vw !important;
    max-height: 85vh;
  }
  
  .modal-content {
    padding: 1.5rem;
    
    .info-steps {
      grid-template-columns: 1fr;
    }
  }
  
  .modal-header {
    padding: 1.25rem 1.5rem;
  }
  
  .modal-footer {
    padding: 1.25rem 1.5rem;
  }
}

@media (max-width: 480px) {
  .modal-content {
    padding: 1rem;
  }
  
  .modal-header {
    padding: 1rem;
  }
  
  .modal-footer {
    padding: 1rem;
  }
}
</style>