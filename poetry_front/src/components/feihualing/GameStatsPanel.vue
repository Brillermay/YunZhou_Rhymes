<template>
  <div class="feihua-component stats-panel-container">
    <div class="component-container">
      <h2 class="component-header">游戏统计</h2>
      
      <!-- 实时统计 -->
      <div class="stats-grid" v-if="!loading">
        <div class="stats-card" v-for="stat in gameStats" :key="stat.key">
          <div class="stats-number" :data-value="stat.value">
            {{ animatedNumbers[stat.key] || 0 }}{{ stat.suffix }}
          </div>
          <div class="stats-label">{{ stat.label }}</div>
          <div class="stats-icon">
            <i :class="stat.icon"></i>
          </div>
        </div>
      </div>
      
      <!-- 加载状态 -->
      <div class="loading" v-if="loading">
        <div class="spinner"></div>
        <p>正在加载统计数据...</p>
      </div>
      
      <!-- 用户成就 -->
      <div class="achievements-section" v-if="userAchievements && !loading">
        <h3 class="section-title">我的成就</h3>
        <div class="achievements-container">
          <div class="achievement-card">
            <div class="achievement-badges">
              <div 
                v-for="(badge, type) in badges" 
                :key="type"
                class="achievement-badge"
                :class="[type, { unlocked: userAchievements[type] }]"
                :title="badge.title"
              >
                <i :class="badge.icon"></i>
              </div>
            </div>
            <div class="achievement-stats">
              <div class="stat-row">
                <span class="label">最佳成绩</span>
                <span class="value">{{ userAchievements.bestScore || 0 }} 分</span>
              </div>
              <div class="stat-row">
                <span class="label">总游戏数</span>
                <span class="value">{{ userAchievements.totalGames || 0 }} 局</span>
              </div>
              <div class="progress-section">
                <div class="progress-label">
                  <span>成就进度</span>
                  <span>{{ userAchievements.progress || 0 }}%</span>
                </div>
                <div class="progress-bar">
                  <div 
                    class="progress-fill" 
                    :style="{ width: (userAchievements.progress || 0) + '%' }"
                  ></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 热门关键词 -->
      <div class="keywords-section" v-if="!loading">
        <h3 class="section-title">热门关键词</h3>
        <div class="keywords-cloud">
          <span 
            v-for="(keyword, index) in popularKeywords" 
            :key="keyword"
            class="keyword-tag"
            :style="getKeywordStyle(index)"
          >
            {{ keyword }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import axios from 'axios'
import API_BASE_URL from '@/config/api'

export default {
  name: 'GameStatsPanel',
  props: {
    userId: {
      type: Number,
      default: null
    }
  },
  setup(props) {
    // 🔧 使用 Vue 3 的响应式 API
    const loading = ref(true)
    const animatedNumbers = reactive({})
    const rawStats = reactive({})
    const userAchievements = ref(null)
    const popularKeywords = ref(['月', '花', '春', '酒', '山', '鸟', '水', '云', '风', '雪'])
    
    const badges = {
      bronze: {
        title: '青铜诗者 - 达到5分',
        icon: 'icon-medal'
      },
      silver: {
        title: '白银诗者 - 达到15分',
        icon: 'icon-star'
      },
      gold: {
        title: '黄金诗者 - 达到25分',
        icon: 'icon-crown'
      }
    }

    // 计算属性
    const gameStats = computed(() => {
      return [
        {
          key: 'todayPlayers',
          label: '今日挑战人数',
          value: rawStats.todayPlayers || 0,
          suffix: '',
          icon: 'icon-users'
        },
        {
          key: 'averageScore',
          label: '平均分数',
          value: rawStats.averageScore || 0,
          suffix: '',
          icon: 'icon-target'
        },
        {
          key: 'totalGames',
          label: '总游戏数',
          value: rawStats.totalGames || 0,
          suffix: '',
          icon: 'icon-gamepad'
        },
        {
          key: 'successRate',
          label: '通关率',
          value: Math.round(rawStats.successRate) || 0,
          suffix: '%',
          icon: 'icon-trophy'
        }
      ]
    })

    // 方法
    const loadStats = async () => {
      try {
        const response = await axios.get(`${API_BASE_URL}/api/feihua/stats`)
        if (response.data.success) {
          Object.assign(rawStats, response.data.data)
        }
      } catch (error) {
        console.error('加载统计数据失败:', error)
      }
    }
    
    const loadUserAchievements = async () => {
      if (!props.userId) return
      
      try {
        const response = await axios.get(`${API_BASE_URL}/api/feihua/achievements/${props.userId}`)
        if (response.data.success) {
          userAchievements.value = response.data.data
        }
      } catch (error) {
        console.error('加载用户成就失败:', error)
      }
    }
    
    const animateNumbers = () => {
      gameStats.value.forEach(stat => {
        animateNumberTo(stat.key, stat.value)
      })
    }
    
    // 🔧 修复 $set 问题 - 直接给 reactive 对象赋值
    const animateNumberTo = (key, target) => {
      const duration = 1500
      const steps = 60
      const stepValue = target / steps
      let current = 0
      
      const timer = setInterval(() => {
        current += stepValue
        if (current >= target) {
          current = target
          clearInterval(timer)
        }
        // 🔧 Vue 3 中直接赋值即可，不需要 $set
        animatedNumbers[key] = Math.round(current)
      }, duration / steps)
    }
    
    const getKeywordStyle = (index) => {
      const sizes = ['1.2rem', '1.4rem', '1rem', '1.3rem', '1.1rem']
      const colors = ['#8c7853', '#6e5773', '#a67c52', '#8b6f84', '#7d6a4a']
      
      return {
        fontSize: sizes[index % sizes.length],
        color: colors[index % colors.length],
        animationDelay: `${index * 0.1}s`
      }
    }

    // 生命周期钩子
    onMounted(async () => {
      await loadStats()
      await loadUserAchievements()
      animateNumbers()
      loading.value = false
    })

    // 返回模板需要的数据和方法
    return {
      loading,
      animatedNumbers,
      rawStats,
      userAchievements,
      popularKeywords,
      badges,
      gameStats,
      getKeywordStyle
    }
  }
}
</script>


<style lang="scss" scoped>
@import './styles/game-common.scss';

.stats-panel-container {
  // 🔧 移除多余的背景和 padding
  height: 100%;
  display: flex;
  flex-direction: column;
}

.component-container {
  // 🔧 重写容器样式
  padding: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.component-header {
  @include ancient-title;
  text-align: center;
  margin-bottom: 0.75rem; // 🔧 减少下边距
  font-size: 1.2rem; // 🔧 减少字体大小
  color: var(--primary-color);
  text-shadow: 0 2px 4px rgba(140, 120, 83, 0.2);
  
  @media (max-width: 768px) {
    font-size: 1.1rem;
    margin-bottom: 0.5rem;
  }
}

.stats-grid {
  // 🔧 优化网格布局
  display: grid;
  grid-template-columns: repeat(2, 1fr); // 🔧 改为2列布局
  gap: 0.5rem;
  margin-bottom: 0.75rem;
  
  @media (max-width: 1024px) {
    grid-template-columns: repeat(2, 1fr);
  }
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
  }
}

.stats-card {
  @include stats-card;
  padding: 0.75rem; // 🔧 减少内边距
  position: relative;
  overflow: hidden;
  
  .stats-number {
    font-size: 1.3rem; // 🔧 减少数字大小
    margin-bottom: 0.3rem;
  }
  
  .stats-label {
    font-size: 0.75rem; // 🔧 减少标签字体
  }
  
  &:hover {
    transform: translateY(-2px) scale(1.01); // 🔧 减少悬停效果
  }
}

.achievements-section, .keywords-section {
  margin-bottom: 0.75rem; // 🔧 减少间距
  
  &:last-child {
    margin-bottom: 0;
  }
}

.section-title {
  @include ancient-title;
  font-size: 1rem; // 🔧 减少标题大小
  margin-bottom: 0.75rem;
  color: var(--text-color);
}

.achievements-container {
  @include modern-card;
  padding: 0.75rem; // 🔧 减少内边距
}

.achievement-card {
  display: flex;
  gap: 0.75rem; // 🔧 减少间距
  align-items: center;
  
  @media (max-width: 768px) {
    flex-direction: column;
    text-align: center;
    gap: 0.5rem;
  }
}

.achievement-badges {
  display: flex;
  gap: 0.5rem; // 🔧 减少徽章间距
}

.achievement-badge {
  @include achievement-badge;
  width: 36px; // 🔧 减少徽章尺寸
  height: 36px;
  
  i {
    font-size: 1.2rem; // 🔧 减少图标大小
    color: white;
  }
  
  &.unlocked {
    cursor: pointer;
    
    &:hover {
      transform: scale(1.1) rotate(5deg); // 🔧 减少悬停效果
    }
  }
}

.achievement-stats {
  flex: 1;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.4rem; // 🔧 减少行间距
  font-size: 0.8rem; // 🔧 减少字体大小
  
  .label {
    color: #666;
  }
  
  .value {
    font-weight: bold;
    color: var(--primary-color);
  }
}

.progress-section {
  margin-top: 0.5rem; // 🔧 减少上边距
}

.progress-label {
  display: flex;
  justify-content: space-between;
  margin-bottom: 0.3rem; // 🔧 减少间距
  font-size: 0.75rem; // 🔧 减少字体大小
  color: #666;
}

.progress-bar {
  @include progress-bar;
  height: 6px; // 🔧 减少进度条高度
}

.keywords-section {
  @include modern-card;
  padding: 0.75rem; // 🔧 减少内边距
  flex: 1; // 🔧 让关键词区域占用剩余空间
  min-height: 0; // 🔧 允许收缩
  overflow: hidden;
}

.keywords-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem; // 🔧 减少间距
  justify-content: center;
  max-height: 120px; // 🔧 限制最大高度
  overflow-y: auto;
  
  &::-webkit-scrollbar {
    width: 4px;
  }
  
  &::-webkit-scrollbar-track {
    background: rgba(140, 120, 83, 0.1);
    border-radius: 2px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(140, 120, 83, 0.3);
    border-radius: 2px;
  }
}

.keyword-tag {
  @include ancient-text;
  padding: 0.3rem 0.6rem; // 🔧 减少内边距
  background: rgba(140, 120, 83, 0.1);
  border-radius: 16px; // 🔧 减少圆角
  cursor: pointer;
  transition: all 0.3s ease;
  animation: fadeInUp 0.6s ease-out both;
  font-size: 0.8rem; // 🔧 减少字体大小
  
  &:hover {
    background: var(--primary-color);
    color: white;
    transform: translateY(-1px); // 🔧 减少悬停效果
    box-shadow: 0 2px 8px rgba(140, 120, 83, 0.3);
  }
}

.loading {
  text-align: center;
  padding: 2rem 1rem; // 🔧 减少内边距
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  
  .spinner {
    width: 30px; // 🔧 减少加载器尺寸
    height: 30px;
  }
  
  p {
    margin-top: 0.75rem;
    color: #666;
    font-size: 0.9rem;
  }
}

@media (max-width: 768px) {
  .component-container {
    gap: 0.5rem;
  }
  
  .stats-grid {
    gap: 0.4rem;
  }
  
  .achievement-badges {
    justify-content: center;
  }
  
  .keywords-cloud {
    gap: 0.3rem;
    max-height: 100px;
  }
  
  .keyword-tag {
    font-size: 0.75rem !important;
    padding: 0.25rem 0.5rem;
  }
}
</style>