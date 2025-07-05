<template>
  <Teleport to="body">
    <div v-if="show" class="ranking-modal-overlay" @click="closeModal">
      <div class="ranking-modal" @click.stop>
        <!-- 🎨 模态框头部 -->
        <div class="modal-header">
          <div class="header-content">
            <div class="header-icon">
              <i class="icon-trophy"></i>
            </div>
            <div class="header-text">
              <h2 class="modal-title">排行榜</h2>
              <p class="modal-subtitle">
                {{ getDifficultyText(difficulty) }}难度 · {{ questionCount }}题
              </p>
            </div>
          </div>
          <button class="close-button" @click="closeModal">
            <i class="icon-times"></i>
          </button>
        </div>
        
        <!-- 🎨 模态框主体 -->
        <div class="modal-body">
          <!-- 🔧 加载状态 -->
          <div v-if="loading" class="loading-container">
            <div class="loading-animation">
              <div class="spinner"></div>
            </div>
            <p class="loading-text">正在加载排行榜数据...</p>
          </div>

          <!-- 🔧 排行榜内容 -->
          <div v-else class="ranking-content">
            <!-- 我的排名卡片 -->
            <div v-if="myRankInfo" class="my-rank-card">
              <div class="rank-badge">
                <div class="rank-position">
                  <span class="rank-number">{{ myRankInfo.rank || '未上榜' }}</span>
                  <span class="rank-label">我的排名</span>
                </div>
                <div class="rank-medal">
                  <i :class="getMedalIcon(myRankInfo.rank)"></i>
                </div>
              </div>
              
              <div class="rank-details">
                <div class="user-info">
                  <div class="username">{{ myRankInfo.UserName }}</div>
                  <div class="user-stats">
                    <span class="score">{{ myRankInfo.Max }}分</span>
                    <span class="separator">·</span>
                    <span class="time">{{ formatTime(myRankInfo.Mintime) }}</span>
                  </div>
                </div>
              </div>
            </div>

            <!-- 排行榜表格 -->
            <div class="ranking-table-section">
              <div class="section-header">
                <h3 class="section-title">
                  <i class="icon-list"></i>
                  排行榜
                </h3>
                <div class="section-subtitle">前20名高手</div>
              </div>
              
              <div v-if="rankList.length > 0" class="table-container">
                <div class="table-wrapper">
                  <table class="ranking-table">
                    <thead>
                      <tr>
                        <th class="rank-header">排名</th>
                        <th class="name-header">用户名</th>
                        <th class="score-header">最高分</th>
                        <th class="time-header">最短用时</th>
                      </tr>
                    </thead>
                    <tbody>
                      <tr 
                        v-for="(item, index) in displayRankList"
                        :key="item.UID"
                        class="rank-row"
                        :class="{ 
                          'is-mine': isMyRank(item.UID),
                          'is-top-three': index < 3,
                          'is-champion': index === 0
                        }"
                      >
                        <td class="rank-cell">
                          <div class="rank-content">
                            <span class="rank-number">{{ index + 1 }}</span>
                            <i 
                              v-if="index < 3"
                              :class="getMedalIcon(index + 1)"
                              class="rank-medal"
                            ></i>
                          </div>
                        </td>
                        
                        <td class="name-cell">
                          <div class="name-content">
                            <span class="username">{{ item.UserName }}</span>
                            <i 
                              v-if="isMyRank(item.UID)"
                              class="icon-star me-indicator"
                              title="这是我"
                            ></i>
                          </div>
                        </td>
                        
                        <td class="score-cell">
                          <div class="score-content">
                            <span class="score-number">{{ item.Max }}</span>
                            <span class="score-unit">分</span>
                          </div>
                        </td>
                        
                        <td class="time-cell">
                          <div class="time-content">
                            <span class="time-value">{{ formatTime(item.Mintime) }}</span>
                          </div>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>

              <!-- 🔧 空状态 -->
              <div v-else class="empty-state">
                <div class="empty-icon">
                  <i class="icon-inbox"></i>
                </div>
                <div class="empty-content">
                  <h4 class="empty-title">暂无排行榜数据</h4>
                  <p class="empty-description">完成测试后即可查看排名</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 🎨 模态框底部 -->
        <div class="modal-footer">
          <button class="action-button secondary" @click="closeModal">
            <i class="icon-arrow-left"></i>
            <span>返回</span>
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script>
export default {
  name: 'RankingModal',
  
  props: {
    show: {
      type: Boolean,
      default: false
    },
    difficulty: {
      type: Number,
      default: 1
    },
    questionCount: {
      type: Number,
      default: 10
    },
    rankList: {
      type: Array,
      default: () => []
    },
    myRankInfo: {
      type: Object,
      default: null
    },
    loading: {
      type: Boolean,
      default: false
    }
  },
  
  emits: ['close'],
  
  computed: {
    displayRankList() {
      return this.rankList.slice(0, 20)
    }
  },
  
  watch: {
    show(newVal) {
      if (newVal) {
        document.body.style.overflow = 'hidden'
      } else {
        document.body.style.overflow = 'auto'
      }
    }
  },
  
  methods: {
    closeModal() {
      this.$emit('close')
    },
    
    getDifficultyText(difficulty) {
      const difficultyMap = {
        1: '简单',
        2: '普通',
        3: '困难'
      }
      return difficultyMap[difficulty] || '普通'
    },
    
    getMedalIcon(rank) {
      if (typeof rank === 'string' || !rank) return ''
      
      const icons = {
        1: 'icon-trophy medal-gold',
        2: 'icon-medal medal-silver',
        3: 'icon-award medal-bronze'
      }
      return icons[rank] || ''
    },
    
    isMyRank(uid) {
      return this.myRankInfo && String(this.myRankInfo.UID) === String(uid)
    },
    
    formatTime(seconds) {
      if (!seconds) return '-'
      const min = Math.floor(seconds / 60)
      const sec = seconds % 60
      return `${min}′${sec.toString().padStart(2, '0')}″`
    }
  },
  
  beforeUnmount() {
    document.body.style.overflow = 'auto'
  }
}
</script>

<style lang="scss">
// 🎨 全局变量
:root {
  --ranking-primary: #8c7853;
  --ranking-secondary: #6e5a73;
  --ranking-gold: #ffd700;
  --ranking-silver: #c0c0c0;
  --ranking-bronze: #cd7f32;
  --ranking-success: #27ae60;
  --ranking-warning: #f39c12;
  --ranking-danger: #e74c3c;
  --ranking-bg: #f8f6f0;
  --ranking-card-bg: #ffffff;
  --ranking-text: #333333;
  --ranking-text-light: #666666;
  --ranking-border: #e5e5e5;
  --ranking-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

// 🎨 弹窗遮罩
.ranking-modal-overlay {
  position: fixed !important;
  top: 0 !important;
  left: 0 !important;
  right: 0 !important;
  bottom: 0 !important;
  width: 100vw !important;
  height: 100vh !important;
  background: rgba(0, 0, 0, 0.75) !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
  z-index: 99999 !important;
  padding: 1rem;
  backdrop-filter: blur(8px);
  animation: fadeIn 0.3s ease-out;
}

// 🎨 主弹窗
.ranking-modal {
  width: 90vw;
  max-width: 900px;
  max-height: 90vh;
  background: var(--ranking-card-bg) !important;
  border-radius: 20px !important;
  box-shadow: 0 25px 50px rgba(0, 0, 0, 0.25) !important;
  overflow: hidden;
  animation: slideIn 0.4s ease-out;
  position: relative !important;
  z-index: 100000 !important;
  display: flex;
  flex-direction: column;
}

// 🎨 头部样式
.modal-header {
  background: linear-gradient(135deg, var(--ranking-primary), var(--ranking-secondary));
  color: white;
  padding: 2rem;
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .header-content {
    display: flex;
    align-items: center;
    gap: 1rem;
    
    .header-icon {
      font-size: 2.5rem;
      color: var(--ranking-gold);
      filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
    }
    
    .header-text {
      .modal-title {
        font-family: 'KaiTi', 'STKaiti', serif;
        font-size: 1.8rem;
        font-weight: 700;
        margin: 0;
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
        letter-spacing: 2px;
      }
      
      .modal-subtitle {
        font-size: 1rem;
        opacity: 0.9;
        margin: 0.5rem 0 0 0;
        font-weight: 400;
      }
    }
  }
  
  .close-button {
    background: rgba(255, 255, 255, 0.2);
    border: none;
    color: white;
    font-size: 1.5rem;
    width: 50px;
    height: 50px;
    border-radius: 50%;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
    backdrop-filter: blur(10px);
    
    &:hover {
      background: rgba(255, 255, 255, 0.3);
      transform: scale(1.1);
    }
  }
}

// 🎨 主体内容
.modal-body {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
  background: var(--ranking-bg);
}

// 🎨 加载状态
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem;
  
  .loading-animation {
    margin-bottom: 2rem;
    
    .spinner {
      width: 50px;
      height: 50px;
      border: 4px solid rgba(140, 120, 83, 0.2);
      border-top: 4px solid var(--ranking-primary);
      border-radius: 50%;
      animation: spin 1s linear infinite;
    }
  }
  
  .loading-text {
    font-size: 1.1rem;
    color: var(--ranking-text-light);
    font-weight: 500;
  }
}

// 🎨 我的排名卡片
.my-rank-card {
  background: linear-gradient(135deg, 
    rgba(140, 120, 83, 0.1) 0%, 
    rgba(140, 120, 83, 0.05) 100%
  );
  border: 2px solid var(--ranking-primary);
  border-radius: 16px;
  padding: 2rem;
  margin-bottom: 2rem;
  display: flex;
  align-items: center;
  gap: 2rem;
  box-shadow: var(--ranking-shadow);
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 30px rgba(140, 120, 83, 0.2);
  }
  
  .rank-badge {
    display: flex;
    align-items: center;
    gap: 1rem;
    
    .rank-position {
      text-align: center;
      
      .rank-number {
        display: block;
        font-size: 2.5rem;
        font-weight: 700;
        color: var(--ranking-primary);
        text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      }
      
      .rank-label {
        font-size: 0.9rem;
        color: var(--ranking-text-light);
        font-weight: 500;
      }
    }
    
    .rank-medal {
      font-size: 2rem;
      
      &.medal-gold { color: var(--ranking-gold); }
      &.medal-silver { color: var(--ranking-silver); }
      &.medal-bronze { color: var(--ranking-bronze); }
    }
  }
  
  .rank-details {
    flex: 1;
    
    .user-info {
      .username {
        font-size: 1.5rem;
        font-weight: 600;
        color: var(--ranking-primary);
        margin-bottom: 0.5rem;
      }
      
      .user-stats {
        display: flex;
        align-items: center;
        gap: 0.5rem;
        font-size: 1.1rem;
        color: var(--ranking-text-light);
        
        .score {
          font-weight: 600;
          color: var(--ranking-success);
        }
        
        .separator {
          opacity: 0.5;
        }
        
        .time {
          font-family: 'Courier New', monospace;
          font-weight: 500;
        }
      }
    }
  }
}

// 🎨 排行榜表格区域
.ranking-table-section {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 1.5rem;
    
    .section-title {
      font-family: 'KaiTi', 'STKaiti', serif;
      font-size: 1.4rem;
      font-weight: 700;
      color: var(--ranking-primary);
      margin: 0;
      display: flex;
      align-items: center;
      gap: 0.5rem;
      letter-spacing: 1px;
    }
    
    .section-subtitle {
      font-size: 0.9rem;
      color: var(--ranking-text-light);
      font-weight: 500;
    }
  }
  
  .table-container {
    background: var(--ranking-card-bg);
    border-radius: 16px;
    box-shadow: var(--ranking-shadow);
    overflow: hidden;
    border: 1px solid var(--ranking-border);
    
    .table-wrapper {
      max-height: 500px;
      overflow-y: auto;
      
      &::-webkit-scrollbar {
        width: 8px;
      }
      
      &::-webkit-scrollbar-track {
        background: #f1f1f1;
        border-radius: 4px;
      }
      
      &::-webkit-scrollbar-thumb {
        background: var(--ranking-primary);
        border-radius: 4px;
        
        &:hover {
          background: var(--ranking-secondary);
        }
      }
    }
  }
}

// 🎨 表格样式
.ranking-table {
  width: 100%;
  border-collapse: collapse;
  background: var(--ranking-card-bg);
  
  thead {
    background: linear-gradient(135deg, var(--ranking-primary), var(--ranking-secondary));
    
    th {
      padding: 1.2rem;
      text-align: center;
      font-weight: 600;
      color: white;
      font-size: 1rem;
      text-shadow: 0 1px 2px rgba(0, 0, 0, 0.2);
      border-bottom: 2px solid rgba(255, 255, 255, 0.1);
      
      &.rank-header { width: 80px; }
      &.name-header { width: auto; }
      &.score-header { width: 100px; }
      &.time-header { width: 120px; }
    }
  }
  
  tbody {
    .rank-row {
      transition: all 0.3s ease;
      border-bottom: 1px solid var(--ranking-border);
      
      &:hover {
        background: rgba(140, 120, 83, 0.05);
      }
      
      &.is-champion {
        background: linear-gradient(135deg, 
          rgba(255, 215, 0, 0.15) 0%, 
          rgba(255, 215, 0, 0.05) 100%
        );
      }
      
      &.is-top-three {
        background: linear-gradient(135deg, 
          rgba(255, 215, 0, 0.1) 0%, 
          rgba(255, 215, 0, 0.03) 100%
        );
        
        .rank-content,
        .name-content,
        .score-content,
        .time-content {
          font-weight: 600;
        }
      }
      
      &.is-mine {
        background: linear-gradient(135deg, 
          rgba(140, 120, 83, 0.15) 0%, 
          rgba(140, 120, 83, 0.05) 100%
        ) !important;
        border: 2px solid var(--ranking-primary);
        
        .rank-content,
        .name-content,
        .score-content,
        .time-content {
          color: var(--ranking-primary);
          font-weight: 600;
        }
      }
      
      td {
        padding: 1rem;
        text-align: center;
        font-size: 0.95rem;
        color: var(--ranking-text);
        
        .rank-content {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 0.5rem;
          
          .rank-number {
            font-weight: 600;
            font-size: 1.1rem;
          }
          
          .rank-medal {
            font-size: 1.2rem;
            
            &.medal-gold { color: var(--ranking-gold); }
            &.medal-silver { color: var(--ranking-silver); }
            &.medal-bronze { color: var(--ranking-bronze); }
          }
        }
        
        .name-content {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 0.5rem;
          
          .username {
            font-weight: 500;
          }
          
          .me-indicator {
            color: var(--ranking-primary);
            font-size: 0.9rem;
            animation: pulse 2s infinite;
          }
        }
        
        .score-content {
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 0.2rem;
          
          .score-number {
            font-weight: 600;
            font-size: 1.1rem;
            color: var(--ranking-success);
          }
          
          .score-unit {
            font-size: 0.8rem;
            color: var(--ranking-text-light);
          }
        }
        
        .time-content {
          .time-value {
            font-family: 'Courier New', monospace;
            font-weight: 500;
            color: var(--ranking-text-light);
          }
        }
      }
    }
  }
}

// 🎨 空状态
.empty-state {
  text-align: center;
  padding: 4rem;
  color: var(--ranking-text-light);
  
  .empty-icon {
    font-size: 4rem;
    opacity: 0.3;
    margin-bottom: 1.5rem;
  }
  
  .empty-content {
    .empty-title {
      font-size: 1.3rem;
      font-weight: 600;
      margin-bottom: 0.5rem;
      color: var(--ranking-text);
    }
    
    .empty-description {
      font-size: 1rem;
      opacity: 0.8;
      margin: 0;
    }
  }
}

// 🎨 底部按钮
.modal-footer {
  background: var(--ranking-card-bg);
  padding: 2rem;
  border-top: 1px solid var(--ranking-border);
  display: flex;
  justify-content: center;
  
  .action-button {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    padding: 1rem 2rem;
    border: none;
    border-radius: 12px;
    font-size: 1rem;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    min-width: 120px;
    justify-content: center;
    
    &.secondary {
      background: rgba(140, 120, 83, 0.1);
      color: var(--ranking-primary);
      border: 2px solid var(--ranking-primary);
      
      &:hover {
        background: var(--ranking-primary);
        color: white;
        transform: translateY(-2px);
        box-shadow: 0 4px 15px rgba(140, 120, 83, 0.3);
      }
    }
  }
}

// 🎨 动画效果
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}

// 🎨 响应式设计
@media (max-width: 768px) {
  .ranking-modal {
    width: 95vw;
    max-height: 95vh;
  }
  
  .modal-header {
    padding: 1.5rem;
    
    .header-content {
      .header-icon {
        font-size: 2rem;
      }
      
      .header-text {
        .modal-title {
          font-size: 1.5rem;
        }
        
        .modal-subtitle {
          font-size: 0.9rem;
        }
      }
    }
  }
  
  .modal-body {
    padding: 1rem;
  }
  
  .my-rank-card {
    flex-direction: column;
    text-align: center;
    gap: 1rem;
    padding: 1.5rem;
    
    .rank-badge {
      justify-content: center;
    }
  }
  
  .section-header {
    flex-direction: column;
    gap: 0.5rem;
    text-align: center;
  }
  
  .ranking-table {
    font-size: 0.85rem;
    
    th, td {
      padding: 0.8rem 0.5rem;
    }
    
    .rank-content,
    .name-content,
    .score-content,
    .time-content {
      flex-direction: column;
      gap: 0.2rem;
    }
  }
  
  .modal-footer {
    padding: 1.5rem;
  }
}

@media (max-width: 480px) {
  .ranking-table {
    th, td {
      padding: 0.6rem 0.3rem;
      font-size: 0.8rem;
    }
    
    .rank-content .rank-number {
      font-size: 1rem;
    }
    
    .score-content .score-number {
      font-size: 1rem;
    }
  }
}
</style>