<!-- filepath: c:\Users\Administrator\Desktop\YunZhou_Rhymes\poetry_front\src\components\feihualing\GameHeader.vue -->
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
      
      <!-- 倒计时 -->
      <div class="countdown-compact">
        <div class="countdown-circle" :class="{ warning: countdown <= 10, danger: countdown <= 5 }">
          <span class="countdown-num">{{ countdown }}</span>
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
    keywordStats: { type: Object, default: null }
  },
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
    }
  }
}
</script>

<style lang="scss" scoped>
@import './styles/game-common.scss';

// 🚀 超紧凑头部设计 - 固定高度
.game-header-compact {
  // 🔧 关键修复：确保头部高度严格控制
  height: 80px;
  min-height: 80px;
  max-height: 80px;
  background: rgba(245, 239, 230, 0.98);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(140, 120, 83, 0.2);
  display: flex;
  align-items: center;
  overflow: hidden;
  
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
  gap: 1rem;
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

@media (max-width: 768px) {
  .header-content {
    justify-content: space-around;
  }
  
  .game-info {
    order: 1;
  }
  
  .keyword-display {
    order: 2;
    flex: none;
  }
  
  .countdown-section {
    order: 3;
  }
  
  .game-status {
    order: 4;
  }
}
</style>