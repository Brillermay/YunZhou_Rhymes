<template>
  <div class="game-header-compact">
    <div class="header-content">
      <!-- 游戏信息 -->
      <div class="game-info">
        <span class="mode-tag" :class="gameMode">{{ getModeLabel(gameMode) }}</span>
        <span class="difficulty-tag" :class="difficulty">{{ getDifficultyLabel(difficulty) }}</span>
      </div>
      
      <!-- 当前关键词 -->
      <div class="keyword-display">
        <span class="keyword-char" :class="{ pulsing: isNewKeyword }">{{ currentKeyword }}</span>
      </div>
      
      <!-- 🔧 游戏控制按钮 -->
      <div class="game-controls">
        <button 
          class="control-btn pause-btn"
          @click="togglePause"
          :title="isPaused ? '继续游戏' : '暂停游戏'"
          :class="{ active: isPaused }"
        >
          <i :class="isPaused ? 'icon-play' : 'icon-pause'"></i>
        </button>
        <button 
          class="control-btn exit-btn"
          @click="showExitConfirm"
          title="退出游戏"
        >
          <i class="icon-x"></i>
        </button>
      </div>
      
      <!-- 倒计时 -->
      <div class="countdown-compact">
        <div class="countdown-circle" :class="{ warning: countdown <= 10, danger: countdown <= 5, paused: isPaused }">
          <span class="countdown-num">{{ isPaused ? '⏸' : countdown }}</span>
        </div>
      </div>
      
      <!-- 游戏状态 -->
      <div class="game-status-compact">
        <span class="stat-item">{{ answerCount }}题</span>
        <span class="stat-item">{{ formatTime(gameTime) }}</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'GameHeader',
  props: {
    gameMode: { type: String, required: true },
    difficulty: { type: String, required: true },
    currentKeyword: { type: String, required: true },
    countdown: { type: Number, required: true },
    maxTime: { type: Number, required: true },
    answerCount: { type: Number, default: 0 },
    currentRound: { type: Number, default: 1 },
    roundProgress: { type: Number, default: 0 },
    roundTarget: { type: Number, default: 3 },
    gameTime: { type: Number, default: 0 },
    keywordStats: { type: Object, default: null },
    isPaused: { type: Boolean, default: false }
  },
  emits: [
    'toggle-pause', 
    'show-exit-confirm'  // 🔧 修改：只发送显示退出确认事件
  ],
  data() {
    return {
      isNewKeyword: false
    }
  },
  watch: {
    currentKeyword(newKeyword, oldKeyword) {
      if (oldKeyword && newKeyword !== oldKeyword) {
        this.isNewKeyword = true
        setTimeout(() => { this.isNewKeyword = false }, 1000)
      }
    }
  },
  methods: {
    getModeLabel(mode) {
      return { endless: '无尽', challenge: '闯关' }[mode] || '未知'
    },
    getDifficultyLabel(difficulty) {
      return { easy: '简单', normal: '普通', hard: '困难' }[difficulty] || '未知'
    },
    formatTime(seconds) {
      const mins = Math.floor(seconds / 60)
      const secs = seconds % 60
      return `${mins}:${secs.toString().padStart(2, '0')}`
    },
    
    // 🔧 切换暂停状态
    togglePause() {
      this.$emit('toggle-pause')
    },
    
    // 🔧 修改：显示退出确认 - 发送事件给父组件
    showExitConfirm() {
      this.$emit('show-exit-confirm')
    }
  }
}
</script>

<style lang="scss" scoped>
@import './styles/game-common.scss';

// 🚀 超紧凑头部设计 - 固定高度
.game-header-compact {
  height: 80px;
  min-height: 80px;
  max-height: 80px;
  background: rgba(245, 239, 230, 0.98);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(140, 120, 83, 0.2);
  display: flex;
  align-items: center;
  overflow: hidden;
  position: relative;
  
  @media (max-width: 768px) {
    height: 65px;
    min-height: 65px;
    max-height: 65px;
  }
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
  width: 100%;
  padding: 0 1rem;
  height: 100%;
  
  @media (max-width: 768px) {
    gap: 0.5rem;
    padding: 0 0.5rem;
  }
}

.game-info {
  display: flex;
  gap: 0.4rem;
  align-items: center;
  flex-shrink: 0;
}

.mode-tag, .difficulty-tag {
  padding: 0.2rem 0.5rem;
  border-radius: 10px;
  font-size: 0.65rem;
  font-weight: 600;
  line-height: 1;
  white-space: nowrap;
  
  &.endless { background: #3498db; color: white; }
  &.challenge { background: #e74c3c; color: white; }
  &.easy { background: #27ae60; color: white; }
  &.normal { background: #f39c12; color: white; }
  &.hard { background: #e74c3c; color: white; }
}

.keyword-display {
  flex: 1;
  text-align: center;
  min-width: 0;
}

.keyword-char {
  @include ancient-title;
  font-size: 1.5rem;
  color: var(--primary-color);
  font-weight: bold;
  text-shadow: 0 2px 4px rgba(140, 120, 83, 0.3);
  transition: all 0.3s ease;
  line-height: 1;
  
  &.pulsing {
    animation: keywordPulse 1s ease-out;
  }
  
  @media (max-width: 768px) {
    font-size: 1.2rem;
  }
}

// 🔧 游戏控制按钮区域
.game-controls {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  flex-shrink: 0;
}

.control-btn {
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
  
  &.pause-btn {
    background: linear-gradient(135deg, #f39c12, #e67e22);
    color: white;
    
    &:hover {
      background: linear-gradient(135deg, #e67e22, #d35400);
      transform: scale(1.1);
    }
    
    &.active {
      background: linear-gradient(135deg, #27ae60, #229954);
      animation: pulseGreen 2s infinite;
    }
  }
  
  &.exit-btn {
    background: linear-gradient(135deg, #e74c3c, #c0392b);
    color: white;
    
    &:hover {
      background: linear-gradient(135deg, #c0392b, #a93226);
      transform: scale(1.1);
    }
  }
  
  @media (max-width: 768px) {
    width: 32px;
    height: 32px;
    font-size: 0.8rem;
  }
}

.countdown-compact {
  flex-shrink: 0;
}

.countdown-circle {
  width: 35px;
  height: 35px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  transition: all 0.3s ease;
  
  &.warning {
    background: linear-gradient(135deg, #f39c12, #e67e22);
    animation: pulseWarning 2s infinite;
  }
  
  &.danger {
    background: linear-gradient(135deg, #e74c3c, #c0392b);
    animation: pulseDanger 1s infinite;
  }
  
  &.paused {
    background: linear-gradient(135deg, #95a5a6, #7f8c8d);
    animation: pulsePaused 2s infinite;
  }
  
  @media (max-width: 768px) {
    width: 30px;
    height: 30px;
  }
}

.countdown-num {
  color: white;
  font-size: 0.8rem;
  font-weight: bold;
  line-height: 1;
  
  @media (max-width: 768px) {
    font-size: 0.7rem;
  }
}

.game-status-compact {
  display: flex;
  flex-direction: column;
  gap: 0.1rem;
  align-items: flex-end;
  flex-shrink: 0;
  
  @media (max-width: 768px) {
    flex-direction: row;
    gap: 0.3rem;
  }
}

.stat-item {
  font-size: 0.65rem;
  color: var(--primary-color);
  font-weight: 600;
  line-height: 1;
  white-space: nowrap;
}

// 动画效果
@keyframes keywordPulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

@keyframes pulseWarning {
  0%, 100% { 
    box-shadow: 0 0 0 0 rgba(243, 156, 18, 0.7);
  }
  50% { 
    box-shadow: 0 0 0 6px rgba(243, 156, 18, 0);
  }
}

@keyframes pulseDanger {
  0%, 100% { 
    box-shadow: 0 0 0 0 rgba(231, 76, 60, 0.7);
  }
  50% { 
    box-shadow: 0 0 0 6px rgba(231, 76, 60, 0);
  }
}

@keyframes pulseGreen {
  0%, 100% { 
    box-shadow: 0 0 0 0 rgba(39, 174, 96, 0.7);
  }
  50% { 
    box-shadow: 0 0 0 6px rgba(39, 174, 96, 0);
  }
}

@keyframes pulsePaused {
  0%, 100% { 
    box-shadow: 0 0 0 0 rgba(149, 165, 166, 0.7);
  }
  50% { 
    box-shadow: 0 0 0 6px rgba(149, 165, 166, 0);
  }
}

// 移动端适配
@media (max-width: 768px) {
  .header-content {
    justify-content: space-between;
  }
  
  .game-controls {
    gap: 0.25rem;
  }
}
</style>