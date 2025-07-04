<!-- filepath: c:\Users\Administrator\Desktop\YunZhou_Rhymes\poetry_front\src\components\feihualing\GameModeSelector.vue -->
<template>
  <div class="feihua-component mode-selector-container">
    <div class="component-container">
      <!-- 🎯 游戏标题 - 简化版 -->
      <div class="game-header-section">
        <h1 class="game-title">
          <i class="icon-flower"></i>
          飞花令
        </h1>
        <p class="game-subtitle">古典诗词接龙游戏</p>
        
        <!-- 🏆 操作按钮 - 移到标题下方 -->
        <div class="header-actions">
            <!-- 🔧 新增：返回游戏中心按钮 -->
          <button 
            class="btn btn-outline action-btn return-btn"
            @click="returnToGameCenter"
          >
            <i class="icon-arrow-left"></i>
            返回游戏中心
          </button>
          <button 
            class="btn btn-outline action-btn"
            @click="showGameIntro"
          >
            <i class="icon-book"></i>
            游戏介绍
          </button>
          <button 
            class="btn btn-outline action-btn"
            @click="showLeaderboard"
          >
            <i class="icon-trophy"></i>
            排行榜
          </button>
        </div>
      </div>

      <!-- 🎮 主要选择区域 - 并排布局 -->
      <div class="selection-main-area">
        <!-- 左侧：游戏模式选择 -->
        <div class="mode-selection-section">
          <h2 class="section-title">选择游戏模式</h2>
          <div class="mode-grid">
            <div 
              v-for="mode in modes" 
              :key="mode.value"
              class="mode-card"
              :class="{ active: selectedMode === mode.value }"
              @click="selectMode(mode.value)"
            >
              <div class="mode-icon">
                <i :class="mode.icon"></i>
              </div>
              <h3 class="mode-title">{{ mode.label }}</h3>
              <p class="mode-description">{{ mode.description }}</p>
              <div class="mode-features">
                <span class="feature-tag" v-for="feature in mode.features" :key="feature">
                  {{ feature }}
                </span>
              </div>
              <!-- 🔧 新增：选中状态指示器 -->
              <div class="selection-indicator" v-if="selectedMode === mode.value">
                <i class="icon-check-circle"></i>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：难度选择 -->
        <div class="difficulty-selection-section">
          <h2 class="section-title">选择难度</h2>
          <div class="difficulty-grid">
            <div 
              v-for="diff in difficulties" 
              :key="diff.value"
              class="difficulty-card"
              :class="{ 
                active: selectedDifficulty === diff.value,
                disabled: !selectedMode
              }"
              @click="selectDifficulty(diff.value)"
            >
              <div class="difficulty-icon">
                <i :class="diff.icon"></i>
              </div>
              <div class="difficulty-info">
                <span class="difficulty-name">{{ diff.label }}</span>
                <span class="difficulty-time">{{ diff.time }}秒限时</span>
                <span class="difficulty-hints">{{ diff.hints }}次提示</span>
              </div>
              <div class="difficulty-badge" :style="{ backgroundColor: diff.color }">
                {{ diff.label }}
              </div>

            </div>
          </div>
        </div>
      </div>

      <!-- 🚀 开始游戏按钮 - 固定在底部 -->
      <div class="start-game-section">
        <button 
          class="btn btn-primary start-game-btn"
          :class="{ ready: selectedMode && selectedDifficulty }"
          @click="startGame"
          :disabled="!selectedMode || !selectedDifficulty || isLoading"
        >
          <i class="icon-play"></i>
          <span v-if="!isLoading">
            {{ selectedMode && selectedDifficulty ? '开始游戏' : '请选择模式和难度' }}
          </span>
          <span v-else>加载中...</span>
        </button>
        
        <!-- 快速开始选项 -->
        <div class="quick-start-options">
          <button 
            class="quick-start-btn"
            @click="quickStart('endless', 'normal')"
            :disabled="isLoading"
          >
            <i class="icon-zap"></i>
            快速开始：无尽·普通
          </button>
          <button 
            class="quick-start-btn"
            @click="quickStart('challenge', 'easy')"
            :disabled="isLoading"
          >
            <i class="icon-play-circle"></i>
            快速开始：闯关·简单
          </button>
        </div>
      </div>
    </div>
    
    <!-- 🎮 游戏介绍弹窗 - 修复层级问题 -->
    <div class="intro-modal-overlay" v-if="showIntroModal" @click.self="closeIntroModal">
      <div class="intro-modal-content">
        <div class="intro-modal-header">
          <h2 class="intro-modal-title">
            <i class="icon-flower"></i>
            飞花令游戏介绍
          </h2>
          <button class="intro-modal-close" @click="closeIntroModal">
            <i class="icon-times"></i>
          </button>
        </div>
        
        <div class="intro-modal-body">
          <div class="intro-section">
            <h3>历史背景</h3>
            <p>飞花令源自古人的诗词游戏，因古诗"春城无处不飞花"而得名。原本是古代文人墨客在饮酒聚会时进行的一种文字游戏，既考验参与者的诗词功底，也增加了聚会的雅趣。</p>
          </div>
          
          <div class="intro-section">
            <h3>游戏玩法</h3>
            <div class="gameplay-steps">
              <div class="step">
                <div class="step-number">1</div>
                <div class="step-content">
                  <h4>获取关键词</h4>
                  <p>系统随机给出一个关键词，如"月"、"花"、"春"等</p>
                </div>
              </div>
              <div class="step">
                <div class="step-number">2</div>
                <div class="step-content">
                  <h4>思考诗句</h4>
                  <p>在限定时间内想出包含该关键词的古诗词句</p>
                </div>
              </div>
              <div class="step">
                <div class="step-number">3</div>
                <div class="step-content">
                  <h4>输入答案</h4>
                  <p>输入你想到的诗句，系统会自动验证</p>
                </div>
              </div>
              <div class="step">
                <div class="step-number">4</div>
                <div class="step-content">
                  <h4>获得分数</h4>
                  <p>答对获得分数，继续挑战更多关键词</p>
                </div>
              </div>
            </div>
          </div>
          
          <div class="intro-section">
            <h3>评分规则</h3>
            <div class="scoring-rules">
              <div class="rule-item">
                <i class="icon-check-circle"></i>
                <span>答对一题：基础分 5 分</span>
              </div>
              <div class="rule-item">
                <i class="icon-clock"></i>
                <span>时间奖励：剩余时间 × 0.1 分</span>
              </div>
              <div class="rule-item">
                <i class="icon-star"></i>
                <span>难度系数：简单×1，普通×1.5，困难×2</span>
              </div>
            </div>
          </div>
          
          <div class="intro-section">
            <h3>小贴士</h3>
            <div class="tips">
              <div class="tip-item">
                <i class="icon-lightbulb"></i>
                <span>多背诵经典诗词，积累词汇量</span>
              </div>
              <div class="tip-item">
                <i class="icon-lightbulb"></i>
                <span>注意关键词在诗句中的位置</span>
              </div>
              <div class="tip-item">
                <i class="icon-lightbulb"></i>
                <span>可以使用提示功能，但会消耗提示次数</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 🏆 排行榜弹窗 -->
    <LeaderboardModal
      :visible="showLeaderboardModal"
      :user-id="currentUserId"
      @close="closeLeaderboardModal"
      @share-rank="shareRank"
    />
  </div>
</template>

<script>
import LeaderboardModal from './LeaderboardModal.vue'

export default {
  name: 'GameModeSelector',
  components: {
    LeaderboardModal
  },
  props: {
    gameStats: {
      type: Object,
      default: () => ({})
    },
    isLoading: {
      type: Boolean,
      default: false
    }
  },
  emits: ['mode-selected', 'start-game', 'return-to-center'],
  data() {
    return {
      selectedMode: '',
      selectedDifficulty: '',
      showIntroModal: false,
      showLeaderboardModal: false,
      currentUserId: null,
      modes: [
        {
          value: 'endless',
          label: '无尽模式',
          description: '连续答题，挑战你的诗词储备量',
          icon: 'icon-infinity',
          features: ['无时间限制', '累计积分', '挑战极限']
        },
        {
          value: 'challenge',
          label: '闯关模式',
          description: '三轮挑战，逐步提升难度',
          icon: 'icon-target',
          features: ['分轮挑战', '难度递增', '通关奖励']
        }
      ],
      difficulties: [
        {
          value: 'easy',
          label: '简单',
          time: 45,
          hints: 5,
          icon: 'icon-leaf',
          color: '#27ae60'
        },
        {
          value: 'normal',
          label: '普通',
          time: 30,
          hints: 3,
          icon: 'icon-fire',
          color: '#f39c12'
        },
        {
          value: 'hard',
          label: '困难',
          time: 15,
          hints: 1,
          icon: 'icon-lightning',
          color: '#e74c3c'
        }
      ]
    }
  },
  mounted() {
    this.currentUserId = this.getCurrentUserId()
  },
  methods: {
    selectMode(mode) {
      this.selectedMode = mode
      this.$emit('mode-selected', { mode, difficulty: this.selectedDifficulty })
    },
    
    selectDifficulty(difficulty) {
      if (!this.selectedMode) return
      this.selectedDifficulty = difficulty
      this.$emit('mode-selected', { 
        mode: this.selectedMode, 
        difficulty 
      })
    },
    
    startGame() {
      if (this.selectedMode && this.selectedDifficulty) {
        this.$emit('start-game', {
          mode: this.selectedMode,
          difficulty: this.selectedDifficulty
        })
      }
    },
    
    quickStart(mode, difficulty) {
      this.selectedMode = mode
      this.selectedDifficulty = difficulty
      this.$emit('start-game', {
        mode,
        difficulty
      })
    },
    
    getModeLabel(mode) {
      return { endless: '无尽', challenge: '闯关' }[mode] || '未知'
    },
    
    getDifficultyLabel(difficulty) {
      return { easy: '简单', normal: '普通', hard: '困难' }[difficulty] || '未知'
    },
    
    showGameIntro() {
      this.showIntroModal = true
    },
    
    closeIntroModal() {
      this.showIntroModal = false
    },
    
    showLeaderboard() {
      this.showLeaderboardModal = true
    },
    
    closeLeaderboardModal() {
      this.showLeaderboardModal = false
    },
    
    shareRank(rankData) {
      console.log('分享排名:', rankData)
    },
    
    getCurrentUserId() {
      return localStorage.getItem('userId') ? parseInt(localStorage.getItem('userId')) : null
    },

      // 🔧 新增：返回游戏中心
    returnToGameCenter() {
      this.$emit('return-to-center')
    },
  

  }
}
</script>

<style lang="scss" scoped>
@import './styles/game-common.scss';

.mode-selector-container {
  @extend .feihua-component;
  min-height: 100vh;
  padding: 2rem 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.component-container {
  max-width: 1400px;
  width: 100%;
  padding: 0 2rem;
}

// 🎯 游戏标题区域
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
    color: #ff6b6b;
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
  
  &.btn-outline {
    background: rgba(255, 255, 255, 0.9);
    border: 2px solid rgba(140, 120, 83, 0.3);
    color: var(--primary-color);
    
    &:hover {
      background: rgba(140, 120, 83, 0.1);
      border-color: var(--primary-color);
      transform: translateY(-2px);
    }
  }
}

// 🎮 主要选择区域
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

// 🎮 游戏模式选择区域
.mode-selection-section {
  .section-title {
    @include ancient-title;
    text-align: center;
    margin-bottom: 2rem;
    font-size: 1.5rem;
    color: var(--primary-color);
  }
}

.mode-grid {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.mode-card {
  @include modern-card;
  padding: 2rem;
  cursor: pointer;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.9);
  border: 2px solid transparent;
  position: relative;
  overflow: hidden;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.15);
    border-color: rgba(140, 120, 83, 0.3);
  }
  
  &.active {
    @include ancient-border;
    transform: translateY(-2px);
    background: 
      linear-gradient(135deg, 
        rgba(255, 255, 255, 1) 0%, 
        rgba(140, 120, 83, 0.05) 100%
      );
    border: 3px solid var(--primary-color) !important;
    box-shadow: 
      0 15px 35px rgba(140, 120, 83, 0.3),
      0 0 0 4px rgba(140, 120, 83, 0.1),
      inset 0 0 20px rgba(140, 120, 83, 0.08);
    
    .mode-icon i {
      color: var(--primary-color);
      transform: scale(1.2);
      text-shadow: 0 2px 8px rgba(140, 120, 83, 0.3);
    }
    
    .mode-title {
      color: var(--primary-color);
      text-shadow: 0 1px 3px rgba(140, 120, 83, 0.2);
    }
    
    .feature-tag {
      background: rgba(140, 120, 83, 0.2);
      border-color: var(--primary-color);
      color: var(--primary-color);
      font-weight: 600;
    }
    
    // 🔧 新增：光晕效果
    &::before {
      content: '';
      position: absolute;
      top: -2px;
      left: -2px;
      right: -2px;
      bottom: -2px;
      background: linear-gradient(45deg, var(--primary-color), transparent, var(--primary-color));
      border-radius: 16px;
      z-index: -1;
      animation: glowing 2s ease-in-out infinite alternate;
    }
  }
}

.mode-icon {
  text-align: center;
  margin-bottom: 1rem;
  
  i {
    font-size: 2.5rem;
    color: #bbb;
    transition: all 0.3s ease;
  }
}

.mode-title {
  @include ancient-title;
  margin-bottom: 0.5rem;
  font-size: 1.3rem;
  color: var(--text-color);
  text-align: center;
  transition: all 0.3s ease;
}

.mode-description {
  @include ancient-text;
  color: #666;
  margin-bottom: 1rem;
  line-height: 1.6;
  text-align: center;
}

.mode-features {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  justify-content: center;
}

.feature-tag {
  padding: 0.25rem 0.75rem;
  background: rgba(140, 120, 83, 0.1);
  border: 1px solid rgba(140, 120, 83, 0.3);
  border-radius: 12px;
  font-size: 0.8rem;
  color: var(--primary-color);
  font-weight: 500;
  transition: all 0.3s ease;
}

// 🎯 难度选择区域
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

// 🎯 难度选择区域 - 修改选中效果
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
  
  &.active {
    border: 3px solid var(--primary-color) !important;
    background: 
      linear-gradient(135deg, 
        rgba(140, 120, 83, 0.08) 0%, 
        rgba(140, 120, 83, 0.15) 100%
      );
    transform: translateY(-2px);
    box-shadow: 
      0 10px 25px rgba(140, 120, 83, 0.25),
      0 0 0 3px rgba(140, 120, 83, 0.15),
      inset 0 0 15px rgba(140, 120, 83, 0.08);
    
    .difficulty-icon i {
      color: var(--primary-color);
      transform: scale(1.2);
      text-shadow: 0 2px 8px rgba(140, 120, 83, 0.3);
    }
    
    .difficulty-name {
      color: var(--primary-color);
      font-weight: 700;
      text-shadow: 0 1px 3px rgba(140, 120, 83, 0.2);
    }
    
    .difficulty-time, .difficulty-hints {
      color: var(--primary-color);
      font-weight: 600;
    }
    
    .difficulty-badge {
      box-shadow: 0 4px 15px rgba(0, 0, 0, 0.4);
      transform: scale(1.1);
      border: 2px solid rgba(255, 255, 255, 0.9);
    }
    
    // 🔧 修改：左侧边框高亮效果
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      width: 6px;
      height: 100%;
      background: linear-gradient(
        to bottom,
        var(--primary-color) 0%,
        rgba(140, 120, 83, 0.8) 50%,
        var(--primary-color) 100%
      );
      animation: pulseGlow 2s ease-in-out infinite alternate;
    }
    
    // 🔧 新增：右侧选中指示条
    &::after {
      content: '';
      position: absolute;
      top: 50%;
      right: 0.5rem;
      transform: translateY(-50%);
      width: 0;
      height: 0;
      border-left: 8px solid var(--primary-color);
      border-top: 6px solid transparent;
      border-bottom: 6px solid transparent;
      animation: slideInRight 0.5s ease-out;
    }
  }
  
  &.disabled {
    opacity: 0.5;
    cursor: not-allowed;
    background: rgba(240, 240, 240, 0.5);
  }
}

.difficulty-icon {
  i {
    font-size: 1.5rem;
    color: #bbb;
    transition: all 0.3s ease;
  }
}

.difficulty-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
}

.difficulty-name {
  font-weight: 600;
  color: var(--text-color);
  font-size: 1.1rem;
  transition: all 0.3s ease;
}

.difficulty-time, .difficulty-hints {
  font-size: 0.9rem;
  color: #666;
}

.difficulty-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 12px;
  color: white;
  font-size: 0.8rem;
  font-weight: 600;
  position: absolute;
  top: 1rem;
  right: 1rem;
  transition: all 0.3s ease;
}

// 🔧 新增：选中状态指示器
.selection-indicator {
  position: absolute;
  top: 1rem;
  left: 1rem;
  width: 30px;
  height: 30px;
  background: linear-gradient(135deg, #27ae60, #2ecc71);
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

// 🚀 开始游戏区域
.start-game-section {
  text-align: center;
  padding: 2rem;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 16px;
  border: 2px solid rgba(140, 120, 83, 0.2);
}

.start-game-btn {
  @extend .btn;
  @extend .btn-primary;
  padding: 1.2rem 3rem;
  font-size: 1.2rem;
  display: inline-flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
  min-width: 280px;
  justify-content: center;
  
  background: linear-gradient(135deg, #95a5a6, #7f8c8d) !important;
  color: white !important;
  border: none !important;
  transition: all 0.3s ease;
  
  &.ready {
    background: linear-gradient(135deg, #8c7853, #6e5773) !important;
    
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

.quick-start-options {
  display: flex;
  justify-content: center;
  gap: 1rem;
  flex-wrap: wrap;
}

.quick-start-btn {
  padding: 0.6rem 1.2rem;
  background: rgba(255, 255, 255, 0.9);
  border: 2px solid rgba(140, 120, 83, 0.3);
  border-radius: 8px;
  color: var(--primary-color);
  font-size: 0.9rem;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  
  &:hover:not(:disabled) {
    background: rgba(140, 120, 83, 0.1);
    border-color: var(--primary-color);
    transform: translateY(-2px);
  }
  
  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

// 🎮 游戏介绍弹窗样式 - 修复层级问题
.intro-modal-overlay {
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
  overflow-y: auto;
  animation: fadeIn 0.3s ease-out;
}

.intro-modal-content {
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

.intro-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  border-bottom: 2px solid rgba(140, 120, 83, 0.2);
}

.intro-modal-title {
  @include ancient-title;
  margin: 0;
  font-size: 1.5rem;
  color: var(--primary-color);
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.intro-modal-close {
  width: 44px;
  height: 44px;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #e74c3c 0%, #c0392b 100%);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.3rem;
  transition: all 0.3s ease;
  box-shadow: 0 3px 10px rgba(231, 76, 60, 0.4);
  
  &:hover {
    background: linear-gradient(135deg, #c0392b 0%, #a93226 100%);
    transform: scale(1.1);
    box-shadow: 0 5px 15px rgba(231, 76, 60, 0.6);
  }
}

.intro-modal-body {
  padding: 2rem;
}

.intro-section {
  margin-bottom: 2rem;
  
  h3 {
    @include ancient-title;
    color: var(--primary-color);
    margin-bottom: 1rem;
    font-size: 1.3rem;
  }
  
  p {
    line-height: 1.8;
    color: var(--text-color);
    margin-bottom: 1rem;
  }
}

.gameplay-steps {
  display: grid;
  gap: 1.5rem;
}

.step {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  padding: 1.5rem;
  background: rgba(140, 120, 83, 0.05);
  border-radius: 12px;
  border-left: 4px solid var(--primary-color);
}

.step-number {
  width: 40px;
  height: 40px;
  background: var(--primary-color);
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
    color: var(--primary-color);
    font-size: 1.1rem;
  }
  
  p {
    margin: 0;
    color: #666;
    line-height: 1.6;
  }
}

.scoring-rules, .tips {
  display: grid;
  gap: 1rem;
}

.scoring-rules .rule-item, .tips .tip-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem;
  background: rgba(140, 120, 83, 0.05);
  border-radius: 8px;
  
  i {
    color: var(--primary-color);
    font-size: 1.2rem;
  }
  
  span {
    color: var(--text-color);
    font-weight: 500;
  }
}

// 🔧 新增：动画效果
@keyframes glowing {
  0% {
    opacity: 0.3;
    filter: blur(2px);
  }
  100% {
    opacity: 0.8;
    filter: blur(0px);
  }
}

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

// 移动端适配
@media (max-width: 768px) {
  .component-container {
    padding: 0 1rem;
  }
  
  .game-title {
    font-size: 2rem;
  }
  
  .selection-main-area {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .mode-card, .difficulty-card {
    padding: 1.5rem;
  }
  
  .start-game-btn {
    padding: 1rem 2rem;
    font-size: 1.1rem;
    min-width: 240px;
  }
  
  .quick-start-options {
    flex-direction: column;
    align-items: center;
  }
  
  .quick-start-btn {
    width: 100%;
    max-width: 300px;
    justify-content: center;
  }
  
  .return-btn {
    &.btn-outline {
      border-color: rgba(52, 152, 219, 0.4) !important;
      color: #3498db !important;
      
      &:hover {
        background: rgba(52, 152, 219, 0.1) !important;
        border-color: #3498db !important;
      }
    }
  }

  .intro-modal-content {
    width: 95vw;
    margin: 1rem;
  }
  
  .intro-modal-header {
    padding: 1rem;
  }
  
  .intro-modal-body {
    padding: 1rem;
  }
}

// 🔧 新增：脉冲发光动画
@keyframes pulseGlow {
  0% {
    box-shadow: 0 0 5px rgba(140, 120, 83, 0.3);
    opacity: 0.8;
  }
  100% {
    box-shadow: 0 0 15px rgba(140, 120, 83, 0.6);
    opacity: 1;
  }
}

// 🔧 新增：右侧指示器滑入动画
@keyframes slideInRight {
  0% {
    transform: translateY(-50%) translateX(10px);
    opacity: 0;
  }
  100% {
    transform: translateY(-50%) translateX(0);
    opacity: 1;
  }
}
</style>