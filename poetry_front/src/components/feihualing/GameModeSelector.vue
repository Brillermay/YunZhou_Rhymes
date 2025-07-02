<template>
  <div class="feihua-component mode-selector-container">
    <div class="component-container">
      <h2 class="component-header">选择游戏模式</h2>
      
      <div class="mode-grid">
        <div 
          v-for="mode in modes" 
          :key="mode.value"
          class="mode-card"
          :class="{ active: selectedMode === mode.value }"
          @click="selectMode(mode.value)"
          :data-seal="mode.seal"
        >
          <div class="mode-icon">
            <i :class="mode.icon"></i>
          </div>
          <h3 class="mode-title">{{ mode.label }}</h3>
          <p class="mode-description">{{ mode.description }}</p>
          <div class="mode-stats" v-if="mode.stats">
            <span class="stat-item">
              <i class="icon-users"></i> {{ mode.stats.players }}人挑战
            </span>
            <span class="stat-item">
              <i class="icon-trophy"></i> {{ mode.stats.successRate }}%通关率
            </span>
          </div>
        </div>
      </div>
      
      <div class="difficulty-selector" v-if="selectedMode">
        <h3 class="section-title">选择难度</h3>
        <div class="difficulty-grid">
          <div 
            v-for="diff in difficulties" 
            :key="diff.value"
            class="difficulty-card"
            :class="{ active: selectedDifficulty === diff.value }"
            @click="selectDifficulty(diff.value)"
          >
            <div class="difficulty-icon">
              <i :class="diff.icon"></i>
            </div>
            <span class="difficulty-name">{{ diff.label }}</span>
            <span class="difficulty-time">{{ diff.time }}秒</span>
          </div>
        </div>
      </div>
      
      <div class="action-section" v-if="selectedMode && selectedDifficulty">
        <button 
          class="btn btn-primary start-game-btn"
          @click="startGame"
          :disabled="isLoading"
        >
          <i class="icon-play"></i>
          <span v-if="!isLoading">开始游戏</span>
          <span v-else>加载中...</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'GameModeSelector',
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
  emits: ['mode-selected', 'start-game'],
  data() {
    return {
      selectedMode: '',
      selectedDifficulty: '',
      modes: [
        {
          value: 'endless',
          label: '无尽模式',
          description: '连续答题，挑战你的诗词储备量',
          icon: 'icon-infinity',
          seal: '无',
          stats: null // 将通过 props 传入
        },
        {
          value: 'challenge',
          label: '闯关模式', 
          description: '三轮挑战，逐步提升难度',
          icon: 'icon-target',
          seal: '关',
          stats: null
        }
      ],
      difficulties: [
        {
          value: 'easy',
          label: '简单',
          time: 45,
          icon: 'icon-leaf',
          color: '#27ae60'
        },
        {
          value: 'normal',
          label: '普通',
          time: 30,
          icon: 'icon-fire',
          color: '#f39c12'
        },
        {
          value: 'hard',
          label: '困难',
          time: 15,
          icon: 'icon-lightning',
          color: '#e74c3c'
        }
      ]
    }
  },
  watch: {
    gameStats: {
      handler(newStats) {
        if (newStats && newStats.totalGames) {
          this.modes[0].stats = {
            players: newStats.todayPlayers || 0,
            successRate: Math.round(newStats.successRate) || 0
          }
          this.modes[1].stats = {
            players: Math.round(newStats.totalGames / 3) || 0,
            successRate: Math.round(newStats.successRate * 0.8) || 0
          }
        }
      },
      immediate: true
    }
  },
  methods: {
    selectMode(mode) {
      this.selectedMode = mode
      this.selectedDifficulty = '' // 重置难度选择
      this.$emit('mode-selected', { mode, difficulty: null })
    },
    
    selectDifficulty(difficulty) {
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
    }
  }
}
</script>


<style lang="scss" scoped>
@import './styles/game-common.scss';

.mode-selector-container {
  @extend .feihua-component; // 🎨 应用背景样式
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem 0;
}

.mode-grid {
  // 🔧 移除或覆盖 responsive-grid mixin
  // @include responsive-grid; // 👈 注释掉这行
  
  // 🔧 重新定义为 2 列居中布局
  display: grid;
  grid-template-columns: repeat(2, 1fr); // 👈 明确指定 2 列
  gap: 2rem;
  justify-content: center;
  align-items: start;
  max-width: 800px; // 👈 限制最大宽度
  margin: 0 auto 2rem auto; // 👈 容器本身居中
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr; // 👈 移动端单列
    gap: 1rem;
    max-width: 400px;
  }
}

.mode-card {
  @include ancient-card;
  @include ancient-seal;
  padding: 2rem 1.5rem;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  background: rgba(255, 255, 255, 0.95); // 🔧 确保卡片背景足够显眼
  
  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 12px 40px rgba(140, 120, 83, 0.2);
  }
  
  &.active {
    @include ancient-border;
    transform: translateY(-4px);
    background: rgba(255, 255, 255, 1); // 🔧 激活状态更明显
    
    .mode-icon i {
      color: var(--primary-color);
      transform: scale(1.2);
    }
  }
}

.mode-icon {
  margin-bottom: 1rem;
  
  i {
    font-size: 3rem;
    color: #bbb; // 🔧 调整图标颜色使其更明显
    transition: all 0.3s ease;
  }
}

.mode-title {
  @include ancient-title;
  margin-bottom: 0.5rem;
  font-size: 1.3rem;
  color: var(--text-color); // 🔧 确保标题颜色明显
}

.mode-description {
  @include ancient-text;
  color: #666;
  margin-bottom: 1rem;
  line-height: 1.6;
}

.mode-stats {
  display: flex;
  justify-content: space-around;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border-color);
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  font-size: 0.8rem;
  color: var(--primary-color);
  font-weight: 500;
  
  i {
    font-size: 0.9rem;
  }
}

.difficulty-selector {
  & {
    margin-bottom: 2rem;
  }
}

.section-title {
  @include ancient-title;
  font-size: 1.1rem;
  margin-bottom: 1rem;
  color: var(--text-color); // 🔧 确保标题可见
}

.difficulty-grid {
  display: flex;
  gap: 1rem;
  justify-content: center;
  flex-wrap: wrap;
}

.difficulty-card {
  @include modern-card;
  padding: 1rem 1.5rem;
  text-align: center;
  cursor: pointer;
  min-width: 120px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.95); // 🔧 确保卡片背景明显
  
  &:hover {
    transform: translateY(-4px);
    background: rgba(255, 255, 255, 1);
  }
  
  &.active {
    border-color: var(--primary-color);
    background: rgba(140, 120, 83, 0.1);
    
    .difficulty-icon i {
      transform: scale(1.1);
    }
  }
}

.difficulty-icon {
  margin-bottom: 0.5rem;
  
  i {
    font-size: 1.5rem;
    transition: transform 0.3s ease;
    color: #999; // 🔧 确保图标可见
  }
}

.difficulty-name {
  display: block;
  font-weight: 600;
  color: var(--text-color);
  margin-bottom: 0.2rem;
}

.difficulty-time {
  font-size: 0.8rem;
  color: #666;
}

.action-section {
  text-align: center;
  margin-top: 2rem; // 🔧 增加间距
}

// 🔧 特别修复开始游戏按钮
.start-game-btn {
  @extend .btn;
  @extend .btn-primary;
  padding: 1rem 2rem;
  font-size: 1.1rem;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 1rem;
  
  // 🔧 强制设置可见样式
  background: linear-gradient(135deg, #8c7853, #6e5773) !important;
  color: white !important;
  border: none !important;
  box-shadow: 0 4px 15px rgba(140, 120, 83, 0.3) !important;
  
  i {
    font-size: 1.2rem;
    color: white !important;
  }
  
  &:hover {
    background: linear-gradient(135deg, #9d8964, #7f6884) !important;
    transform: translateY(-2px) !important;
    box-shadow: 0 6px 20px rgba(140, 120, 83, 0.4) !important;
  }
  
  &:disabled {
    background: #ccc !important;
    color: #999 !important;
    cursor: not-allowed !important;
    transform: none !important;
    box-shadow: none !important;
  }
}

@media (max-width: 768px) {
  .mode-card {
    padding: 1.5rem 1rem;
  }
  
  .mode-icon i {
    font-size: 2.5rem;
  }
  
  .difficulty-grid {
    flex-direction: column;
    align-items: center;
  }
  
  .difficulty-card {
    width: 100%;
    max-width: 200px;
  }
  
  .start-game-btn {
    width: 100%;
    max-width: 300px;
  }
}
</style>