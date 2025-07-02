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
      
      <!-- 排行榜类型切换 -->
      <div class="leaderboard-tabs">
        <button 
          v-for="tab in tabs" 
          :key="tab.value"
          class="tab-button"
          :class="{ active: activeTab === tab.value }"
          @click="switchTab(tab.value)"
        >
          <i :class="tab.icon"></i>
          <span>{{ tab.label }}</span>
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
      activeTab: 'all',
      leaderboardData: [],
      userRank: null,
      totalPlayers: 0,
      lastUpdateTime: '',
      tabs: [
        {
          value: 'all',
          label: '总榜',
          icon: 'icon-trophy'
        },
        {
          value: 'today',
          label: '今日',
          icon: 'icon-calendar'
        },
        {
          value: 'week',
          label: '本周',
          icon: 'icon-chart-bar'
        }
      ]
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
    
    activeTab() {
      this.loadLeaderboard()
    }
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
            limit: 50,
            type: this.activeTab
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
    
    switchTab(tab) {
      this.activeTab = tab
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

<style lang="scss" scoped>
@import './styles/game-common.scss';

.leaderboard-modal {
  width: 90vw;
  max-width: 800px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 1rem;
  border-bottom: 2px solid var(--border-color);
  margin-bottom: 1.5rem;
}

.modal-title {
  @include ancient-title;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0;
  font-size: 1.5rem;
  
  i {
    color: #ffd700;
    font-size: 1.8rem;
  }
}

.leaderboard-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1.5rem;
  
  .tab-button {
    @include modern-button;
    padding: 0.5rem 1rem;
    font-size: 0.9rem;
    display: flex;
    align-items: center;
    gap: 0.3rem;
    background: rgba(140, 120, 83, 0.1);
    color: var(--text-color);
    
    &.active {
      background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
      color: white;
    }
    
    &:hover:not(.active) {
      background: rgba(140, 120, 83, 0.2);
    }
  }
}

.my-rank-section {
  margin-bottom: 1.5rem;
}

.my-rank-card {
  @include modern-card;
  padding: 1rem 1.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
  background: linear-gradient(135deg, 
    rgba(140, 120, 83, 0.05), 
    rgba(110, 87, 115, 0.05)
  );
  border: 2px solid rgba(140, 120, 83, 0.2);
}

.rank-badge {
  @include achievement-badge;
  width: 60px;
  height: 60px;
  font-size: 1rem;
  font-weight: bold;
  
  &.gold {
    background: linear-gradient(135deg, #ffd700, #ffed4e);
    color: #8b4513;
  }
  
  &.silver {
    background: linear-gradient(135deg, #c0c0c0, #a8a8a8);
    color: white;
  }
  
  &.bronze {
    background: linear-gradient(135deg, #cd7f32, #b87333);
    color: white;
  }
  
  &.top-10 {
    background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
    color: white;
  }
  
  &.normal {
    background: #ddd;
    color: #666;
  }
}

.rank-info {
  flex: 1;
  
  .player-name {
    display: block;
    font-weight: 600;
    font-size: 1.1rem;
    color: var(--text-color);
    margin-bottom: 0.2rem;
  }
  
  .player-score {
    color: var(--primary-color);
    font-weight: 500;
  }
}

.rank-trend {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  font-size: 0.9rem;
  
  &.up {
    color: #27ae60;
  }
  
  &.down {
    color: #e74c3c;
  }
  
  &.stable {
    color: #666;
  }
}

.leaderboard-content {
  flex: 1;
  overflow-y: auto;
  min-height: 300px;
  
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-track {
    background: rgba(140, 120, 83, 0.1);
    border-radius: 3px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(140, 120, 83, 0.3);
    border-radius: 3px;
  }
}

.list-header {
  display: grid;
  grid-template-columns: 80px 1fr 100px 120px;
  gap: 1rem;
  padding: 0.75rem 1rem;
  background: rgba(140, 120, 83, 0.1);
  border-radius: 8px;
  margin-bottom: 0.5rem;
  font-weight: 600;
  font-size: 0.9rem;
  color: var(--primary-color);
  
  @media (max-width: 768px) {
    grid-template-columns: 60px 1fr 80px;
    
    .header-time {
      display: none;
    }
  }
}

.leaderboard-list {
  animation: fadeInUp 0.6s ease-out;
}

.leaderboard-row {
  @include leaderboard-row;
  display: grid;
  grid-template-columns: 80px 1fr 100px 120px;
  gap: 1rem;
  align-items: center;
  
  &.highlight {
    background: rgba(140, 120, 83, 0.1);
    border: 2px solid rgba(140, 120, 83, 0.3);
    border-radius: 8px;
  }
  
  &.top-3 {
    background: linear-gradient(90deg, 
      rgba(255, 215, 0, 0.1), 
      rgba(255, 215, 0, 0.05)
    );
  }
  
  @media (max-width: 768px) {
    grid-template-columns: 60px 1fr 80px;
    
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
  @include achievement-badge;
  width: 40px;
  height: 40px;
  font-size: 0.9rem;
  
  &.gold i {
    color: #ffd700;
    font-size: 1.2rem;
  }
  
  &.silver i {
    color: #c0c0c0;
    font-size: 1.1rem;
  }
  
  &.bronze i {
    color: #cd7f32;
    font-size: 1rem;
  }
}

.player-column {
  .player-info {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }
  
  .player-name {
    font-weight: 500;
    color: var(--text-color);
  }
  
  .player-badges {
    display: flex;
    gap: 0.2rem;
  }
  
  .badge {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 0.7rem;
    
    &.champion {
      background: #ffd700;
      color: #8b4513;
    }
    
    &.master {
      background: #8b008b;
      color: white;
    }
    
    &.expert {
      background: #4169e1;
      color: white;
    }
  }
}

.score-column {
  text-align: center;
  
  .score-value {
    font-weight: bold;
    font-size: 1.1rem;
    color: var(--primary-color);
  }
  
  .score-unit {
    font-size: 0.8rem;
    color: #666;
    margin-left: 0.2rem;
  }
}

.time-column {
  text-align: center;
  
  .achieved-time {
    display: block;
    font-size: 0.9rem;
    color: var(--text-color);
  }
  
  .game-mode {
    font-size: 0.7rem;
    color: #666;
    text-transform: uppercase;
  }
}

.empty-state {
  text-align: center;
  padding: 3rem 1rem;
  color: #666;
  
  .empty-icon {
    font-size: 4rem;
    color: #ddd;
    margin-bottom: 1rem;
  }
  
  .empty-text {
    font-size: 1.2rem;
    margin-bottom: 0.5rem;
  }
  
  .empty-subtitle {
    font-size: 0.9rem;
    opacity: 0.8;
  }
}

.leaderboard-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 1rem;
  border-top: 1px solid var(--border-color);
  margin-top: 1rem;
  
  @media (max-width: 768px) {
    flex-direction: column;
    gap: 1rem;
  }
}

.footer-stats {
  display: flex;
  gap: 1.5rem;
  
  @media (max-width: 768px) {
    gap: 1rem;
  }
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  font-size: 0.8rem;
  color: #666;
  
  i {
    color: var(--primary-color);
  }
}

.footer-actions {
  display: flex;
  gap: 0.5rem;
}

@media (max-width: 768px) {
  .leaderboard-modal {
    width: 95vw;
    margin: 1rem;
  }
  
  .modal-header {
    padding-bottom: 0.5rem;
    margin-bottom: 1rem;
  }
  
  .modal-title {
    font-size: 1.2rem;
  }
  
  .my-rank-card {
    padding: 0.75rem 1rem;
    gap: 0.75rem;
  }
  
  .rank-badge {
    width: 50px;
    height: 50px;
    font-size: 0.9rem;
  }
}
</style>