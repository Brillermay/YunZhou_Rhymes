<template>
  <Teleport to="body">
    <div v-if="show" class="stats-panel-overlay" @click="closePanel">
      <div class="stats-panel" @click.stop>
        <!-- 头部 -->
        <div class="panel-header">
          <h3 class="panel-title">
            <i class="icon-chart-bar"></i>
            学习统计
          </h3>
          <button class="close-btn" @click="closePanel">
            <i class="icon-times"></i>
          </button>
        </div>
        
        <!-- 统计内容 -->
        <div class="panel-body">
          <div v-if="loading" class="loading-state">
            <div class="loading-spinner">
              <i class="icon-spinner icon-spin"></i>
            </div>
            <p>正在加载统计数据...</p>
          </div>
          
          <div v-else-if="error" class="error-state">
            <div class="error-icon">
              <i class="icon-exclamation-triangle"></i>
            </div>
            <p>加载失败：{{ error }}</p>
            <button class="retry-btn" @click="loadStats">重试</button>
          </div>
          
          <div v-else class="stats-content">
            <!-- 总体统计 -->
            <div class="stats-overview">
              <div class="stat-card">
                <div class="stat-icon">
                  <i class="icon-question-circle"></i>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.total_questions || 0 }}</div>
                  <div class="stat-label">总答题数</div>
                </div>
              </div>
              
              <div class="stat-card">
                <div class="stat-icon">
                  <i class="icon-check-circle"></i>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.correct_questions || 0 }}</div>
                  <div class="stat-label">答对题数</div>
                </div>
              </div>
              
              <div class="stat-card">
                <div class="stat-icon">
                  <i class="icon-percentage"></i>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ accuracyRate }}%</div>
                  <div class="stat-label">正确率</div>
                </div>
              </div>
              
              <div class="stat-card">
                <div class="stat-icon">
                  <i class="icon-calendar"></i>
                </div>
                <div class="stat-info">
                  <div class="stat-value">{{ stats.consecutive_days || 0 }}</div>
                  <div class="stat-label">连续天数</div>
                </div>
              </div>
            </div>
            
            <!-- 详细信息 -->
            <div class="stats-details">
              <div class="detail-item">
                <span class="label">最后学习时间：</span>
                <span class="value">{{ formatLastStudyDate }}</span>
              </div>
              
              <div class="detail-item">
                <span class="label">学习状态：</span>
                <span class="value" :class="studyStatusClass">{{ studyStatus }}</span>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 底部按钮 -->
        <div class="panel-footer">
          <button class="btn btn-primary" @click="closePanel">
            <i class="icon-check"></i>
            确定
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script>
import { getCurrentUid } from '@/utils/auth'
import API_BASE_URL from '@/config/api'

export default {
  name: 'UserStatsPanel',
  
  props: {
    show: {
      type: Boolean,
      default: false
    }
  },
  
  emits: ['close'],
  
  data() {
    return {
      stats: {},
      loading: false,
      error: null
    }
  },
  
  computed: {
    accuracyRate() {
      const total = this.stats.total_questions || 0
      const correct = this.stats.correct_questions || 0
      if (total === 0) return 0
      return Math.round((correct / total) * 100)
    },
    
    formatLastStudyDate() {
      const date = this.stats.last_study_date
      if (!date) return '暂无记录'
      
      try {
        const studyDate = new Date(date)
        const today = new Date()
        const diffTime = today.getTime() - studyDate.getTime()
        const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
        
        if (diffDays === 0) return '今天'
        if (diffDays === 1) return '昨天'
        if (diffDays < 7) return `${diffDays}天前`
        
        return studyDate.toLocaleDateString('zh-CN')
      } catch (e) {
        return date
      }
    },
    
    studyStatus() {
      const consecutiveDays = this.stats.consecutive_days || 0
      if (consecutiveDays === 0) return '待开始'
      if (consecutiveDays >= 7) return '坚持中'
      if (consecutiveDays >= 3) return '进步中'
      return '起步中'
    },
    
    studyStatusClass() {
      const consecutiveDays = this.stats.consecutive_days || 0
      if (consecutiveDays >= 7) return 'status-excellent'
      if (consecutiveDays >= 3) return 'status-good'
      if (consecutiveDays > 0) return 'status-fair'
      return 'status-none'
    }
  },
  
  watch: {
    show(newVal) {
      if (newVal) {
        this.loadStats()
        document.body.style.overflow = 'hidden'
      } else {
        document.body.style.overflow = 'auto'
      }
    }
  },
  
  methods: {
    async loadStats() {
      this.loading = true
      this.error = null
      
      try {
        const uid = getCurrentUid()
        if (!uid) {
          throw new Error('用户未登录')
        }
        
        console.log('正在加载用户统计，UID:', uid)
        
        // 🔧 调用后端统计API
        const response = await fetch(`${API_BASE_URL}/compRec/stats/${uid}`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json'
          }
        })
        
        if (!response.ok) {
          throw new Error(`HTTP ${response.status}: ${response.statusText}`)
        }
        
        const data = await response.json()
        console.log('获取到的统计数据:', data)
        
        this.stats = data || {}
        
        // 🔧 如果没有数据，显示默认值
        if (!this.stats.UID) {
          this.stats = {
            UID: uid,
            total_questions: 0,
            correct_questions: 0,
            accuracy_rate: 0,
            consecutive_days: 0,
            last_study_date: null
          }
        }
        
      } catch (error) {
        console.error('加载统计数据失败:', error)
        this.error = error.message || '加载失败'
      } finally {
        this.loading = false
      }
    },
    
    closePanel() {
      this.$emit('close')
    }
  },
  
  beforeUnmount() {
    document.body.style.overflow = 'auto'
  }
}
</script>

<style lang="scss">
.stats-panel-overlay {
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
}

.stats-panel {
  width: 90vw;
  max-width: 600px;
  max-height: 90vh;
  background: white !important;
  border-radius: 16px !important;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3) !important;
  overflow: hidden;
  position: relative !important;
  z-index: 100000 !important;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e9ecef;
  background: linear-gradient(135deg, 
    rgba(140, 120, 83, 0.1) 0%, 
    rgba(140, 120, 83, 0.05) 100%
  );
  
  .panel-title {
    font-family: 'KaiTi', 'STKaiti', serif;
    font-weight: 700;
    font-size: 1.5rem;
    color: #8c7853;
    margin: 0;
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }
  
  .close-btn {
    background: none;
    border: none;
    font-size: 1.5rem;
    color: #666;
    cursor: pointer;
    padding: 0.5rem;
    border-radius: 50%;
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
    
    &:hover {
      color: #8c7853;
      background: rgba(140, 120, 83, 0.1);
    }
  }
}

.panel-body {
  padding: 2rem;
  max-height: 60vh;
  overflow-y: auto;
}

.loading-state,
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  text-align: center;
  
  .loading-spinner {
    font-size: 2rem;
    color: #8c7853;
    margin-bottom: 1rem;
    
    .icon-spin {
      animation: spin 1s linear infinite;
    }
  }
  
  .error-icon {
    font-size: 2rem;
    color: #e74c3c;
    margin-bottom: 1rem;
  }
  
  p {
    font-size: 1.1rem;
    color: #666;
    margin-bottom: 1rem;
  }
  
  .retry-btn {
    background: #8c7853;
    color: white;
    border: none;
    padding: 0.75rem 1.5rem;
    border-radius: 8px;
    cursor: pointer;
    font-size: 1rem;
    transition: all 0.3s ease;
    
    &:hover {
      background: #6e5773;
    }
  }
}

.stats-content {
  .stats-overview {
    display: grid;
    grid-template-columns: repeat(2, 1fr); // 🔧 修改为固定2列
    grid-template-rows: repeat(2, 1fr);    // 🔧 添加固定2行
    gap: 1rem;
    margin-bottom: 2rem;
    
    .stat-card {
      background: linear-gradient(135deg, 
        rgba(140, 120, 83, 0.1) 0%, 
        rgba(140, 120, 83, 0.05) 100%
      );
      border-radius: 12px;
      padding: 1.5rem;
      display: flex;
      align-items: center;
      gap: 1rem;
      border: 1px solid rgba(140, 120, 83, 0.2);
      
      .stat-icon {
        font-size: 2rem;
        color: #8c7853;
      }
      
      .stat-info {
        flex: 1;
        
        .stat-value {
          font-size: 1.5rem;
          font-weight: 700;
          color: #333;
          margin-bottom: 0.25rem;
        }
        
        .stat-label {
          font-size: 0.9rem;
          color: #666;
        }
      }
    }
  }
  
  .stats-details {
    background: rgba(248, 246, 240, 0.5);
    border-radius: 12px;
    padding: 1.5rem;
    
    .detail-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0.75rem 0;
      border-bottom: 1px solid rgba(140, 120, 83, 0.1);
      
      &:last-child {
        border-bottom: none;
      }
      
      .label {
        font-weight: 500;
        color: #666;
      }
      
      .value {
        font-weight: 600;
        color: #333;
        
        &.status-excellent { color: #27ae60; }
        &.status-good { color: #f39c12; }
        &.status-fair { color: #3498db; }
        &.status-none { color: #95a5a6; }
      }
    }
  }
}

.panel-footer {
  display: flex;
  justify-content: center;
  padding: 1.5rem;
  border-top: 1px solid #e9ecef;
  background: rgba(248, 246, 240, 0.5);
  
  .btn {
    padding: 0.75rem 2rem;
    border: none;
    border-radius: 8px;
    font-weight: 600;
    cursor: pointer;
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    gap: 0.5rem;
    
    &.btn-primary {
      background: #8c7853;
      color: white;
      
      &:hover {
        background: #6e5773;
        transform: translateY(-2px);
      }
    }
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@media (max-width: 768px) {
  .stats-panel {
    width: 95vw;
    
    .panel-body {
      padding: 1rem;
    }
  }
  
  .stats-overview {
    grid-template-columns: repeat(2, 1fr);
    
    .stat-card {
      padding: 1rem;
      
      .stat-icon {
        font-size: 1.5rem;
      }
      
      .stat-info .stat-value {
        font-size: 1.2rem;
      }
    }
  }
}
</style>