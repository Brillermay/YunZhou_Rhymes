<template>
  <div class="modal-overlay" v-if="visible" @click.self="closeModal">
    <div class="modal-content leaderboard-modal">
      <div class="modal-header">
        <h2 class="modal-title">
          <i class="icon-trophy"></i>
          飞花令排行榜
        </h2>
        <button class="modal-close" @click="closeModal">
          <i class="icon-times"></i>
        </button>
      </div>
      
      
      <!-- 我的排名 -->
      <div class="my-rank-section" v-if="userRank && !loading">
        <div class="my-rank-card">
          <div class="rank-badge" :class="getRankClass(userRank.rank)">
            <span class="rank-number">#{{ userRank.rank }}</span>
          </div>
          <div class="rank-info">
            <span class="player-name">{{ userRank.playerName }}</span>
            <span class="player-score">{{ userRank.score }}分</span>
          </div>
          <div class="rank-trend" :class="userRank.trend">
            <i :class="getTrendIcon(userRank.trend)"></i>
            <span>{{ getTrendText(userRank.trend) }}</span>
          </div>
        </div>
      </div>
      
      <!-- 排行榜列表 -->
      <div class="leaderboard-content">
        <div class="loading" v-if="loading">
          <div class="spinner"></div>
          <p>正在加载排行榜...</p>
        </div>
        
        <div class="leaderboard-list" v-else-if="leaderboardData.length > 0">
          <div class="list-header">
            <span class="header-rank">排名</span>
            <span class="header-player">玩家</span>
            <span class="header-score">分数</span>
            <span class="header-time">时间</span>
          </div>
          
          <div 
            v-for="(player, index) in leaderboardData" 
            :key="index"
            class="leaderboard-row"
            :class="{ 
              highlight: player.isCurrentUser,
              'top-3': index < 3 
            }"
          >
            <div class="rank-column">
              <div class="rank-display" :class="getRankClass(index + 1)">
                <i v-if="index < 3" :class="getRankIcon(index)"></i>
                <span v-else class="rank-number">{{ index + 1 }}</span>
              </div>
            </div>
            
            <div class="player-column">
              <div class="player-info">
                <span class="player-name">{{ player.playerName }}</span>
                <div class="player-badges" v-if="player.badges">
                  <span 
                    v-for="badge in player.badges" 
                    :key="badge"
                    class="badge"
                    :class="badge"
                  >
                    <i :class="getBadgeIcon(badge)"></i>
                  </span>
                </div>
              </div>
            </div>
            
            <div class="score-column">
              <span class="score-value">{{ player.score }}</span>
              <span class="score-unit">分</span>
            </div>
            
            <div class="time-column">
              <span class="achieved-time">{{ formatTime(player.achievedAt) }}</span>
              <span class="game-mode">{{ getModeLabel(player.mode) }}</span>
            </div>
          </div>
        </div>
        
        <div class="empty-state" v-else>
          <div class="empty-icon">
            <i class="icon-trophy"></i>
          </div>
          <p class="empty-text">暂无排行榜数据</p>
          <p class="empty-subtitle">快来成为第一个上榜的诗词高手吧！</p>
        </div>
      </div>
      
      <!-- 底部统计 -->
      <div class="leaderboard-footer" v-if="!loading">
        <div class="footer-stats">
          <div class="stat-item">
            <i class="icon-users"></i>
            <span>总参与人数: {{ totalPlayers }}</span>
          </div>
          <div class="stat-item">
            <i class="icon-clock"></i>
            <span>最后更新: {{ lastUpdateTime }}</span>
          </div>
        </div>
        
        <div class="footer-actions">
          <button class="btn btn-secondary" @click="refreshLeaderboard">
            <i class="icon-refresh"></i>
            刷新
          </button>
          <button class="btn btn-primary" @click="shareRank" v-if="userRank">
            <i class="icon-share"></i>
            分享排名
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'
import API_BASE_URL from '@/config/api'

export default {
  name: 'LeaderboardModal',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    userId: {
      type: Number,
      default: null
    }
  },
  emits: ['close', 'share-rank'],
  data() {
    return {
      loading: true,
      leaderboardData: [],
      userRank: null,
      totalPlayers: 0,
      lastUpdateTime: ''
    }
  },
  watch: {
    visible(newVal) {
      if (newVal) {
        this.loadLeaderboard()
        document.body.style.overflow = 'hidden'
      } else {
        document.body.style.overflow = ''
      }
    },
    

  },
  beforeUnmount() {
    document.body.style.overflow = ''
  },
  methods: {
    async loadLeaderboard() {
      this.loading = true
      try {
        // 加载排行榜数据
        const response = await axios.get(`${API_BASE_URL}/api/feihua/leaderboard`, {
          params: {
            limit: 50
          }
        })
        
        if (response.data.success) {
          this.leaderboardData = response.data.data.leaderboard.map((player, index) => ({
            ...player,
            isCurrentUser: this.userId && player.userId === this.userId,
            badges: this.generateBadges(player.score, index)
          }))
          this.totalPlayers = response.data.data.totalPlayers || 0
          this.lastUpdateTime = new Date().toLocaleTimeString('zh-CN')
          
          // 查找用户排名
          if (this.userId) {
            const userIndex = this.leaderboardData.findIndex(p => p.userId === this.userId)
            if (userIndex !== -1) {
              this.userRank = {
                rank: userIndex + 1,
                playerName: this.leaderboardData[userIndex].playerName,
                score: this.leaderboardData[userIndex].score,
                trend: this.calculateTrend(userIndex + 1)
              }
            }
          }
        }
      } catch (error) {
        console.error('加载排行榜失败:', error)
        this.leaderboardData = []
      } finally {
        this.loading = false
      }
    },
    
    
    closeModal() {
      this.$emit('close')
    },
    
    refreshLeaderboard() {
      this.loadLeaderboard()
    },
    
    shareRank() {
      this.$emit('share-rank', this.userRank)
    },
    
    generateBadges(score, index) {
      const badges = []
      if (index === 0) badges.push('champion')
      if (score >= 30) badges.push('master')
      if (score >= 20) badges.push('expert')
      return badges
    },
    
    calculateTrend(currentRank) {
      // 模拟排名变化趋势
      const random = Math.random()
      if (random < 0.3) return 'up'
      if (random < 0.6) return 'down'
      return 'stable'
    },
    
    getRankClass(rank) {
      if (rank === 1) return 'gold'
      if (rank === 2) return 'silver'
      if (rank === 3) return 'bronze'
      if (rank <= 10) return 'top-10'
      return 'normal'
    },
    
    getRankIcon(index) {
      const icons = ['icon-crown', 'icon-medal', 'icon-award']
      return icons[index] || 'icon-circle'
    },
    
    getBadgeIcon(badge) {
      const icons = {
        champion: 'icon-crown',
        master: 'icon-star',
        expert: 'icon-shield'
      }
      return icons[badge] || 'icon-tag'
    },
    
    getTrendIcon(trend) {
      const icons = {
        up: 'icon-arrow-up',
        down: 'icon-arrow-down',
        stable: 'icon-minus'
      }
      return icons[trend] || 'icon-minus'
    },
    
    getTrendText(trend) {
      const texts = {
        up: '上升',
        down: '下降',
        stable: '持平'
      }
      return texts[trend] || '未知'
    },
    
    getModeLabel(mode) {
      const labels = {
        endless: '无尽',
        challenge: '闯关'
      }
      return labels[mode] || '未知'
    },
    
    formatTime(dateString) {
      const date = new Date(dateString)
      const now = new Date()
      const diff = now - date
      
      if (diff < 86400000) { // 24小时内
        return date.toLocaleTimeString('zh-CN', { 
          hour: '2-digit', 
          minute: '2-digit' 
        })
      } else {
        return date.toLocaleDateString('zh-CN', { 
          month: '2-digit', 
          day: '2-digit' 
        })
      }
    }
  }
}
</script>

<!-- filepath: c:\Users\Administrator\Desktop\YunZhou_Rhymes\poetry_front\src\components\feihualing\LeaderboardModal.vue -->
<!-- 保持 template 和 script 不变，完全重构样式 -->

<style lang="scss" scoped>
@import './styles/game-common.scss';

// 🚀 重构弹窗覆盖层 - 添加背景效果
.modal-overlay {
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
    rgba(0, 0, 0, 0.8) !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  z-index: 9999 !important;
  padding: 1rem !important;
  overflow-y: auto !important;
  animation: fadeIn 0.4s ease-out;
}

.modal-content {
  background: 
    linear-gradient(135deg, 
      rgba(255, 255, 255, 0.98) 0%, 
      rgba(248, 245, 240, 0.95) 100%
    ) !important;
  border-radius: 16px !important;
  box-shadow: 
    0 25px 50px rgba(0, 0, 0, 0.3),
    0 0 0 1px rgba(140, 120, 83, 0.2) !important;
  position: relative !important;
  max-height: 90vh !important;
  overflow-y: auto !important;
  z-index: 10000 !important;
  animation: scaleIn 0.4s ease-out;
  backdrop-filter: blur(10px);
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes scaleIn {
  from { 
    transform: scale(0.85);
    opacity: 0;
  }
  to { 
    transform: scale(1);
    opacity: 1;
  }
}

.leaderboard-modal {
  width: 90vw;
  max-width: 900px;
  max-height: 85vh;
  display: flex;
  flex-direction: column;
  padding: 2rem;
  position: relative;
  
  // 🎨 添加装饰性背景
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: 
      radial-gradient(circle at 20% 20%, rgba(140, 120, 83, 0.03) 0%, transparent 30%),
      radial-gradient(circle at 80% 80%, rgba(110, 87, 115, 0.03) 0%, transparent 30%);
    pointer-events: none;
    border-radius: 16px;
    z-index: 0;
  }
  
  // 确保内容在装饰背景之上
  > * {
    position: relative;
    z-index: 1;
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 1.5rem;
  border-bottom: 2px solid rgba(140, 120, 83, 0.2);
  margin-bottom: 2rem;
  background: 
    linear-gradient(90deg, 
      rgba(140, 120, 83, 0.05) 0%, 
      rgba(110, 87, 115, 0.05) 100%
    );
  padding: 1rem 1.5rem;
  border-radius: 12px;
  margin: -0.5rem -0.5rem 2rem -0.5rem;
}

.modal-title {
  @include ancient-title;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin: 0;
  font-size: 1.8rem;
  color: var(--text-color);
  font-weight: 700;
  
  i {
    color: #ffd700;
    font-size: 2rem;
    text-shadow: 0 2px 4px rgba(255, 215, 0, 0.3);
  }
}

.modal-close {
  width: 44px;
  height: 44px;
  border: none;
  border-radius: 50%;
  background: 
    linear-gradient(135deg, 
      #e74c3c 0%, 
      #c0392b 100%
    );
  color: #e74c3c;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  font-size: 1.3rem;
  font-weight: bold;
  box-shadow: 0 3px 10px rgba(231, 76, 60, 0.4);

  &:hover {
    background: 
      linear-gradient(135deg, 
        #c0392b 0%, 
        #a93226 100%
      );
    color: white;
    transform: scale(1.1);
    box-shadow: 0 4px 12px rgba(231, 76, 60, 0.3);
  }
}



.my-rank-section {
  margin-bottom: 2rem;
}

.my-rank-card {
  @include modern-card;
  padding: 1.5rem 2rem;
  border-radius: 16px;
  display: flex;
  align-items: center;
  gap: 1.5rem;
  background: 
    linear-gradient(135deg, 
      rgba(140, 120, 83, 0.08) 0%, 
      rgba(110, 87, 115, 0.08) 100%
    );
  border: 2px solid rgba(140, 120, 83, 0.2);
  position: relative;
  overflow: hidden;
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 4px;
    height: 100%;
    background: linear-gradient(180deg, var(--primary-color), var(--secondary-color));
  }
}

.rank-badge {
  @include achievement-badge;
  width: 70px;
  height: 70px;
  font-size: 1.2rem;
  font-weight: bold;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  
  &.gold {
    background: linear-gradient(135deg, #ffd700 0%, #ffed4e 100%);
    color: #8b4513;
    box-shadow: 0 4px 12px rgba(255, 215, 0, 0.4);
  }
  
  &.silver {
    background: linear-gradient(135deg, #c0c0c0 0%, #a8a8a8 100%);
    color: white;
    box-shadow: 0 4px 12px rgba(192, 192, 192, 0.4);
  }
  
  &.bronze {
    background: linear-gradient(135deg, #cd7f32 0%, #b87333 100%);
    color: white;
    box-shadow: 0 4px 12px rgba(205, 127, 50, 0.4);
  }
  
  &.top-10 {
    background: linear-gradient(135deg, var(--primary-color) 0%, var(--secondary-color) 100%);
    color: white;
    box-shadow: 0 4px 12px rgba(140, 120, 83, 0.4);
  }
  
  &.normal {
    background: linear-gradient(135deg, #ddd 0%, #ccc 100%);
    color: #666;
    box-shadow: 0 4px 12px rgba(221, 221, 221, 0.4);
  }
  
  .rank-number {
    font-size: 1.3rem;
    font-weight: bold;
    text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
  }
}

.rank-info {
  flex: 1;
  
  .player-name {
    display: block;
    font-weight: 700;
    font-size: 1.3rem;
    color: var(--text-color);
    margin-bottom: 0.5rem;
  }
  
  .player-score {
    color: var(--primary-color);
    font-weight: 600;
    font-size: 1.1rem;
  }
}

.rank-trend {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  padding: 0.5rem 1rem;
  border-radius: 25px;
  font-weight: 600;
  
  &.up {
    color: #27ae60;
    background: 
      linear-gradient(135deg, 
        rgba(39, 174, 96, 0.15) 0%, 
        rgba(39, 174, 96, 0.1) 100%
      );
    border: 1px solid rgba(39, 174, 96, 0.2);
  }
  
  &.down {
    color: #e74c3c;
    background: 
      linear-gradient(135deg, 
        rgba(231, 76, 60, 0.15) 0%, 
        rgba(231, 76, 60, 0.1) 100%
      );
    border: 1px solid rgba(231, 76, 60, 0.2);
  }
  
  &.stable {
    color: #666;
    background: 
      linear-gradient(135deg, 
        rgba(102, 102, 102, 0.15) 0%, 
        rgba(102, 102, 102, 0.1) 100%
      );
    border: 1px solid rgba(102, 102, 102, 0.2);
  }
}

.leaderboard-content {
  flex: 1;
  overflow-y: auto;
  min-height: 300px;
  background: rgba(255, 255, 255, 0.5);
  border-radius: 12px;
  padding: 1rem;
  border: 1px solid rgba(140, 120, 83, 0.1);
  
  &::-webkit-scrollbar {
    width: 10px;
  }
  
  &::-webkit-scrollbar-track {
    background: rgba(140, 120, 83, 0.1);
    border-radius: 5px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: 
      linear-gradient(180deg, 
        rgba(140, 120, 83, 0.4) 0%, 
        rgba(110, 87, 115, 0.4) 100%
      );
    border-radius: 5px;
    
    &:hover {
      background: 
        linear-gradient(180deg, 
          rgba(140, 120, 83, 0.6) 0%, 
          rgba(110, 87, 115, 0.6) 100%
        );
    }
  }
}

.loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem;
  
  .spinner {
    width: 50px;
    height: 50px;
    border: 5px solid rgba(140, 120, 83, 0.1);
    border-top: 5px solid var(--primary-color);
    border-radius: 50%;
    animation: spin 1s linear infinite;
    margin-bottom: 1.5rem;
  }
  
  p {
    color: #666;
    font-size: 1.1rem;
    font-weight: 500;
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.list-header {
  display: grid;
  grid-template-columns: 90px 1fr 110px 130px;
  gap: 1rem;
  padding: 1rem 1.5rem;
  background: 
    linear-gradient(135deg, 
      rgba(140, 120, 83, 0.15) 0%, 
      rgba(110, 87, 115, 0.15) 100%
    );
  border-radius: 12px;
  margin-bottom: 1rem;
  font-weight: 700;
  font-size: 1rem;
  color: var(--primary-color);
  border: 1px solid rgba(140, 120, 83, 0.2);
  
  @media (max-width: 768px) {
    grid-template-columns: 70px 1fr 90px;
    
    .header-time {
      display: none;
    }
  }
}

.leaderboard-list {
  animation: fadeInUp 0.6s ease-out;
}

.leaderboard-row {
  display: grid;
  grid-template-columns: 90px 1fr 110px 130px;
  gap: 1rem;
  align-items: center;
  padding: 1.2rem 1.5rem;
  border-radius: 12px;
  margin-bottom: 0.8rem;
  background: 
    linear-gradient(135deg, 
      rgba(255, 255, 255, 0.8) 0%, 
      rgba(248, 245, 240, 0.6) 100%
    );
  border: 1px solid rgba(140, 120, 83, 0.15);
  transition: all 0.3s ease;
  position: relative;
  
  &:hover {
    background: 
      linear-gradient(135deg, 
        rgba(140, 120, 83, 0.08) 0%, 
        rgba(110, 87, 115, 0.08) 100%
      );
    transform: translateX(8px);
    box-shadow: 0 4px 15px rgba(140, 120, 83, 0.2);
    border-color: rgba(140, 120, 83, 0.3);
  }
  
  &.highlight {
    background: 
      linear-gradient(135deg, 
        rgba(140, 120, 83, 0.12) 0%, 
        rgba(110, 87, 115, 0.12) 100%
      );
    border: 2px solid var(--primary-color);
    transform: translateX(8px);
    box-shadow: 0 6px 20px rgba(140, 120, 83, 0.25);
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 4px;
      background: linear-gradient(180deg, var(--primary-color), var(--secondary-color));
      border-radius: 0 2px 2px 0;
    }
  }
  
  &.top-3 {
    background: 
      linear-gradient(135deg, 
        rgba(255, 215, 0, 0.15) 0%, 
        rgba(255, 215, 0, 0.08) 100%
      );
    border-color: rgba(255, 215, 0, 0.3);
  }
  
  @media (max-width: 768px) {
    grid-template-columns: 70px 1fr 90px;
    
    .time-column {
      display: none;
    }
  }
}

.rank-column {
  display: flex;
  justify-content: center;
}

.rank-display {
  width: 50px;
  height: 50px;
  font-size: 1rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.15);
  
  &.gold {
    background: linear-gradient(135deg, #ffd700, #ffed4e);
    color: #8b4513;
    
    i {
      color: #8b4513;
      font-size: 1.4rem;
    }
  }
  
  &.silver {
    background: linear-gradient(135deg, #c0c0c0, #a8a8a8);
    color: white;
    
    i {
      color: white;
      font-size: 1.3rem;
    }
  }
  
  &.bronze {
    background: linear-gradient(135deg, #cd7f32, #b87333);
    color: white;
    
    i {
      color: white;
      font-size: 1.2rem;
    }
  }
  
  &.top-10 {
    background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
    color: white;
  }
  
  &.normal {
    background: linear-gradient(135deg, #ddd, #ccc);
    color: #666;
  }
  
  .rank-number {
    font-weight: bold;
    font-size: 1.1rem;
  }
}

.player-column {
  .player-info {
    display: flex;
    align-items: center;
    gap: 0.75rem;
  }
  
  .player-name {
    font-weight: 600;
    color: var(--text-color);
    font-size: 1.1rem;
  }
  
  .player-badges {
    display: flex;
    gap: 0.3rem;
  }
  
  .badge {
    width: 24px;
    height: 24px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 0.8rem;
    font-weight: bold;
    
    &.champion {
      background: linear-gradient(135deg, #ffd700, #ffed4e);
      color: #8b4513;
    }
    
    &.master {
      background: linear-gradient(135deg, #8b008b, #9932cc);
      color: white;
    }
    
    &.expert {
      background: linear-gradient(135deg, #4169e1, #1e90ff);
      color: white;
    }
  }
}

.score-column {
  text-align: center;
  
  .score-value {
    font-weight: bold;
    font-size: 1.2rem;
    color: var(--primary-color);
  }
  
  .score-unit {
    font-size: 0.9rem;
    color: #666;
    margin-left: 0.2rem;
  }
}

.time-column {
  text-align: center;
  
  .achieved-time {
    display: block;
    font-size: 1rem;
    color: var(--text-color);
    font-weight: 500;
  }
  
  .game-mode {
    font-size: 0.8rem;
    color: #666;
    text-transform: uppercase;
    font-weight: 600;
    margin-top: 0.2rem;
  }
}

.empty-state {
  text-align: center;
  padding: 4rem 2rem;
  color: #666;
  
  .empty-icon {
    font-size: 5rem;
    color: rgba(140, 120, 83, 0.3);
    margin-bottom: 1.5rem;
  }
  
  .empty-text {
    font-size: 1.4rem;
    margin-bottom: 0.8rem;
    font-weight: 600;
    color: var(--text-color);
  }
  
  .empty-subtitle {
    font-size: 1rem;
    opacity: 0.8;
  }
}

.leaderboard-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 0 0.5rem 0;
  border-top: 2px solid rgba(140, 120, 83, 0.2);
  margin-top: 1.5rem;
  
  @media (max-width: 768px) {
    flex-direction: column;
    gap: 1rem;
  }
}

.footer-stats {
  display: flex;
  gap: 2rem;
  
  @media (max-width: 768px) {
    gap: 1rem;
  }
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  color: #666;
  font-weight: 500;
  
  i {
    color: var(--primary-color);
    font-size: 1rem;
  }
}

.footer-actions {
  display: flex;
  gap: 1rem;
  
  .btn {
    padding: 0.75rem 1.5rem;
    font-size: 1rem;
    font-weight: 600;
    border-radius: 10px;
    transition: all 0.3s ease;
    
    &.btn-secondary {
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
    
    &.btn-primary {
      background: 
        linear-gradient(135deg, 
          var(--primary-color) 0%, 
          var(--secondary-color) 100%
        );
      color: white;
      border: none;
      
      &:hover {
        background: 
          linear-gradient(135deg, 
            #9d8964 0%, 
            #7f6884 100%
          );
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(140, 120, 83, 0.3);
      }
    }
  }
}

// 动画效果
@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 移动端适配
@media (max-width: 768px) {
  .leaderboard-modal {
    width: 95vw;
    margin: 0.5rem;
    padding: 1.5rem;
  }
  
  .modal-header {
    padding: 1rem;
    margin: -0.5rem -0.5rem 1.5rem -0.5rem;
  }
  
  .modal-title {
    font-size: 1.4rem;
  }
  
  
  .my-rank-card {
    padding: 1rem 1.5rem;
    gap: 1rem;
  }
  
  .rank-badge {
    width: 60px;
    height: 60px;
    font-size: 1rem;
  }
  
  .footer-actions {
    width: 100%;
    
    .btn {
      flex: 1;
      justify-content: center;
    }
  }
}
</style>