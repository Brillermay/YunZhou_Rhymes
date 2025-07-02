<!-- filepath: c:\Users\Administrator\Desktop\YunZhou_Rhymes\poetry_front\src\components\GameCenter.vue -->
<template>
  <div class="game-center">
    <!-- 背景装饰 -->
    <div class="background-container">
      <div class="floating-elements">
        <div 
          v-for="(element, index) in floatingElements" 
          :key="index"
          class="floating-element"
          :style="element.style"
        >
          {{ element.symbol }}
        </div>
      </div>
      <div class="gradient-overlay"></div>
    </div>

    <!-- 主要内容 -->
    <div class="content-container">
      <!-- 页面标题 -->
      <div class="header-section">
        <h1 class="main-title">
          <span class="title-icon">🎮</span>
          诗词游戏中心
          <span class="title-decoration">Poetry Game Center</span>
        </h1>
        <p class="subtitle">在游戏中领略诗词之美，于娱乐中品味古典文化</p>
      </div>

      <!-- 游戏卡片网格 -->
      <div class="games-grid">
        <div 
          v-for="(game, index) in games" 
          :key="game.id"
          class="game-card"
          :class="{ visible: game.visible }"
          @click="navigateToGame(game)"
          @mouseenter="onCardHover(index, true)"
          @mouseleave="onCardHover(index, false)"
        >
          <!-- 卡片背景装饰 -->
          <div class="card-background">
            <div class="card-pattern"></div>
          </div>

          <!-- 卡片内容 -->
          <div class="card-content">
            <!-- 游戏图标 -->
            <div class="game-icon">
              <div class="icon-container" :class="game.iconClass">
                <i :class="game.icon"></i>
              </div>
              <div class="icon-glow"></div>
            </div>

            <!-- 游戏信息 -->
            <div class="game-info">
              <h3 class="game-title">{{ game.title }}</h3>
              <p class="game-description">{{ game.description }}</p>
              
              <!-- 游戏特色标签 -->
              <div class="game-tags">
                <span 
                  v-for="tag in game.tags" 
                  :key="tag"
                  class="tag"
                >
                  {{ tag }}
                </span>
              </div>

              <!-- 难度指示器 -->
              <div class="difficulty-indicator">
                <span class="difficulty-label">难度:</span>
                <div class="difficulty-stars">
                  <i 
                    v-for="star in 5" 
                    :key="star"
                    class="fas fa-star"
                    :class="{ active: star <= game.difficulty }"
                  ></i>
                </div>
              </div>
            </div>

            <!-- 悬停效果 -->
            <div class="hover-overlay" :class="{ active: game.isHovered }">
              <div class="hover-content">
                <div class="play-icon">
                  <i class="fas fa-play"></i>
                </div>
                <span class="play-text">开始游戏</span>
              </div>
            </div>

            <!-- 装饰印章 -->
            <div class="game-seal">
              <div class="seal-text">{{ game.sealText }}</div>
            </div>
          </div>

          <!-- 卡片边框装饰 -->
          <div class="card-border">
            <div class="border-line top"></div>
            <div class="border-line right"></div>
            <div class="border-line bottom"></div>
            <div class="border-line left"></div>
          </div>
        </div>
      </div>

      <!-- 底部信息 -->
      <div class="footer-section">
        <div class="stats-container">
          <div class="stat-item">
            <i class="fas fa-gamepad"></i>
            <span>3款精品游戏</span>
          </div>
          <div class="stat-item">
            <i class="fas fa-trophy"></i>
            <span>寓教于乐</span>
          </div>
          <div class="stat-item">
            <i class="fas fa-heart"></i>
            <span>诗词文化</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 返回按钮 -->
    <div class="back-button" @click="$router.go(-1)">
      <i class="fas fa-arrow-left"></i>
      <span>返回</span>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 浮动装饰元素
const floatingElements = ref([])

// 游戏数据
const games = reactive([
  {
    id: 'feihualing',
    title: '飞花令',
    description: '经典诗词对战游戏，考验诗词积累与反应速度',
    icon: 'fas fa-feather-alt',
    iconClass: 'icon-feihua',
    path: '/feihua',
    tags: ['对战', '古典', '挑战'],
    difficulty: 4,
    sealText: '雅',
    visible: false,
    isHovered: false
  },
  {
    id: 'poetry-test',
    title: '诗词测验',
    description: '全面测试您的诗词知识，提升文学素养',
    icon: 'fas fa-scroll',
    iconClass: 'icon-test',
    path: '/game',
    tags: ['学习', '测试', '进阶'],
    difficulty: 3,
    sealText: '学',
    visible: false,
    isHovered: false
  },
  {
    id: 'poetry-game',
    title: '诗词卡牌',
    description: '创新卡牌收集游戏，在娱乐中感受诗词魅力',
    icon: 'fas fa-cards',
    iconClass: 'icon-cards',
    path: '/play',
    tags: ['收集', '合成', '创新'],
    difficulty: 2,
    sealText: '趣',
    visible: false,
    isHovered: false
  },
  {
    id: 'multiplay',
    title: '多人对战',
    description: '诗词卡牌对战游戏，考验诗词理解和博弈策略',
    icon: 'fas fa-feather-alt',
    iconClass: 'icon-feihua',
    path: '/multiplay',
    tags: ['对战', '古典', '挑战'],
    difficulty: 4,
    sealText: '雅',
    visible: false,
    isHovered: false
  }
])

// 页面跳转状态管理
const isNavigating = ref(false)

// 创建浮动装饰元素
const createFloatingElements = () => {
  const symbols = ['诗', '词', '雅', '韵', '文', '墨', '🌸', '🍃', '🌙', '☁️']
  floatingElements.value = []
  
  for (let i = 0; i < 15; i++) {
    floatingElements.value.push({
      symbol: symbols[Math.floor(Math.random() * symbols.length)],
      style: {
        left: `${Math.random() * 100}%`,
        top: `${Math.random() * 100}%`,
        animationDelay: `${Math.random() * 10}s`,
        animationDuration: `${15 + Math.random() * 10}s`
      }
    })
  }
}

// 卡片显示动画
const startCardsAnimation = () => {
  games.forEach((game, index) => {
    setTimeout(() => {
      game.visible = true
    }, index * 300)
  })
}

// 卡片悬停处理
const onCardHover = (index, isHovering) => {
  if (!isNavigating.value) {
    games[index].isHovered = isHovering
  }
}

// 清理过渡元素的函数
const cleanupTransitionElements = () => {
  // 清理可能存在的过渡元素
  const existingOverlay = document.querySelector('.game-transition-overlay')
  const existingStyle = document.querySelector('#game-center-transition-style')
  
  if (existingOverlay) {
    existingOverlay.remove()
  }
  if (existingStyle) {
    existingStyle.remove()
  }
}

// 安全的页面跳转函数
const createSafePageTransition = (targetPath) => {
  return new Promise((resolve) => {
    // 防止重复跳转
    if (isNavigating.value) {
      resolve()
      return
    }
    
    isNavigating.value = true
    
    // 先清理现有的过渡元素
    cleanupTransitionElements()
    
    // 创建过渡样式
    const style = document.createElement('style')
    style.id = 'game-center-transition-style'
    style.textContent = `
      @keyframes gameTransition {
        0% { 
          transform: scale(0); 
          border-radius: 50%; 
          opacity: 0;
        }
        50% {
          opacity: 1;
        }
        100% { 
          transform: scale(3); 
          border-radius: 0; 
          opacity: 1;
        }
      }
    `
    document.head.appendChild(style)
    
    // 创建过渡覆盖层
    const overlay = document.createElement('div')
    overlay.className = 'game-transition-overlay'
    overlay.style.cssText = `
      position: fixed;
      top: 50%;
      left: 50%;
      width: 100px;
      height: 100px;
      transform: translate(-50%, -50%) scale(0);
      background: radial-gradient(circle, transparent 0%, rgba(44, 62, 80, 0.95) 100%);
      z-index: 9999;
      border-radius: 50%;
      animation: gameTransition 0.8s ease-out forwards;
      pointer-events: auto;
    `
    
    document.body.appendChild(overlay)
    
    // 在动画完成后执行跳转
    setTimeout(() => {
      console.log('🎮 导航到:', targetPath)
      
      // 执行路由跳转
      router.push(targetPath).then(() => {
        // 跳转成功后清理
        setTimeout(() => {
          cleanupTransitionElements()
          isNavigating.value = false
          resolve()
        }, 100)
      }).catch((error) => {
        console.error('路由跳转失败:', error)
        cleanupTransitionElements()
        isNavigating.value = false
        resolve()
      })
    }, 600)
  })
}

// 导航到游戏
const navigateToGame = async (game) => {
  // 防止重复点击
  if (isNavigating.value) {
    return
  }
  
  console.log('🎮 启动游戏:', game.title)
  
  // 重置所有悬停状态
  games.forEach(g => g.isHovered = false)
  
  // 添加点击动画效果
  const gameCard = event.currentTarget
  if (gameCard) {
    gameCard.style.transform = 'scale(0.95)'
    
    setTimeout(() => {
      if (gameCard) {
        gameCard.style.transform = ''
      }
    }, 150)
  }
  
  // 延迟后执行安全跳转
  setTimeout(() => {
    createSafePageTransition(game.path)
  }, 150)
}

// 安全的返回函数
const safeGoBack = () => {
  if (isNavigating.value) {
    return
  }
  
  isNavigating.value = true
  
  // 清理过渡元素
  cleanupTransitionElements()
  
  // 执行返回
  router.go(-1)
  
  // 重置状态
  setTimeout(() => {
    isNavigating.value = false
  }, 1000)
}

// 组件挂载
onMounted(() => {
  console.log('🎮 游戏中心已挂载')
  
  // 确保页面状态正常
  isNavigating.value = false
  
  // 清理可能残留的元素
  cleanupTransitionElements()
  
  // 初始化动画
  createFloatingElements()
  setTimeout(startCardsAnimation, 500)
})

// 组件卸载时清理
onUnmounted(() => {
  console.log('🎮 游戏中心正在卸载')
  
  // 清理过渡元素
  cleanupTransitionElements()
  
  // 重置状态
  isNavigating.value = false
})
</script>

<style lang="scss" scoped>
.game-center {
  min-height: 100vh;
  background: linear-gradient(
    135deg,
    #0f0f23 0%,
    #1a1a2e 30%,
    #16213e 70%,
    #0f3460 100%
  );
  position: relative;
  overflow: hidden;
}

// ============ 背景装饰 ============
.background-container {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.floating-elements {
  position: absolute;
  width: 100%;
  height: 100%;
}

.floating-element {
  position: absolute;
  font-size: 1.5rem;
  color: rgba(255, 255, 255, 0.1);
  animation: floatUpDown linear infinite;
  pointer-events: none;
}

.gradient-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: 
    radial-gradient(circle at 20% 20%, rgba(100, 150, 200, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(200, 100, 150, 0.1) 0%, transparent 50%);
}

// ============ 内容容器 ============
.content-container {
  position: relative;
  z-index: 10;
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

// ============ 页面标题 ============
.header-section {
  text-align: center;
  margin-bottom: 3rem;
}

.main-title {
  font-size: 3rem;
  font-weight: 900;
  color: #fff;
  margin: 0 0 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  
  .title-icon {
    font-size: 2.5rem;
    background: linear-gradient(45deg, #667eea, #764ba2);
    background-clip: text;
    -webkit-background-clip: text;
    color: transparent;
  }
  
  .title-decoration {
    font-size: 1rem;
    color: rgba(255, 255, 255, 0.6);
    font-weight: 400;
    margin-left: 1rem;
    opacity: 0;
    animation: fadeIn 1s ease-out 1s forwards;
  }
}

.subtitle {
  font-size: 1.2rem;
  color: rgba(255, 255, 255, 0.8);
  margin: 0;
  font-weight: 300;
  letter-spacing: 0.05rem;
  animation: slideUp 1s ease-out 0.5s both;
}

// ============ 游戏卡片网格 ============
.games-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 2rem;
  margin: 2rem 0;
}

.game-card {
  position: relative;
  background: linear-gradient(
    135deg,
    rgba(248, 243, 225, 0.95) 0%,
    rgba(245, 235, 210, 0.9) 100%
  );
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  transform: translateY(50px) rotateX(10deg);
  opacity: 0;
  transition: all 0.6s cubic-bezier(0.23, 1, 0.32, 1);
  box-shadow: 
    0 10px 30px rgba(0, 0, 0, 0.3),
    0 1px 8px rgba(0, 0, 0, 0.2);
  
  &.visible {
    transform: translateY(0) rotateX(0);
    opacity: 1;
  }
  
  &:hover {
    transform: translateY(-10px) scale(1.02);
    box-shadow: 
      0 20px 40px rgba(0, 0, 0, 0.4),
      0 8px 25px rgba(100, 150, 200, 0.3);
  }
}

// ============ 卡片背景 ============
.card-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.card-pattern {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: 
    repeating-linear-gradient(
      45deg,
      transparent,
      transparent 10px,
      rgba(140, 120, 83, 0.05) 10px,
      rgba(140, 120, 83, 0.05) 20px
    );
}

// ============ 卡片内容 ============
.card-content {
  position: relative;
  z-index: 10;
  padding: 2rem;
  height: 100%;
  min-height: 300px;
  display: flex;
  flex-direction: column;
}

// ============ 游戏图标 ============
.game-icon {
  position: relative;
  text-align: center;
  margin-bottom: 1.5rem;
}

.icon-container {
  width: 80px;
  height: 80px;
  margin: 0 auto;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 2rem;
  color: white;
  position: relative;
  z-index: 2;
  
  &.icon-feihua { 
    background: linear-gradient(45deg, #43e97b, #38f9d7); 
  }
  &.icon-test { 
    background: linear-gradient(45deg, #667eea, #764ba2); 
  }
  &.icon-cards { 
    background: linear-gradient(45deg, #f093fb, #f5576c); 
  }
}

.icon-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100px;
  height: 100px;
  border-radius: 50%;
  background: inherit;
  filter: blur(20px);
  opacity: 0.3;
  z-index: 1;
}

// ============ 游戏信息 ============
.game-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.game-title {
  font-size: 1.8rem;
  font-weight: 700;
  color: #2c3e50;
  margin: 0 0 1rem;
  text-align: center;
}

.game-description {
  font-size: 1rem;
  color: #666;
  line-height: 1.6;
  margin: 0 0 1.5rem;
  text-align: center;
  flex: 1;
}

.game-tags {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.tag {
  padding: 0.3rem 0.8rem;
  background: linear-gradient(45deg, #667eea, #764ba2);
  color: white;
  border-radius: 15px;
  font-size: 0.8rem;
  font-weight: 600;
}

.difficulty-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-top: auto;
}

.difficulty-label {
  font-size: 0.9rem;
  color: #666;
  font-weight: 600;
}

.difficulty-stars {
  display: flex;
  gap: 0.2rem;
  
  .fas.fa-star {
    color: #ddd;
    font-size: 0.9rem;
    transition: color 0.2s ease;
    
    &.active {
      color: #ffd700;
    }
  }
}

// ============ 悬停效果 ============
.hover-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    135deg,
    rgba(44, 62, 80, 0.9),
    rgba(140, 120, 83, 0.8)
  );
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
  color: white;
  opacity: 0;
  transition: opacity 0.3s ease;
  border-radius: 20px;
  
  &.active {
    opacity: 1;
  }
}

.hover-content {
  text-align: center;
}

.play-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1rem;
  font-size: 1.5rem;
  animation: pulse 2s infinite;
}

.play-text {
  font-size: 1.2rem;
  font-weight: 600;
  letter-spacing: 0.1rem;
}

// ============ 装饰印章 ============
.game-seal {
  position: absolute;
  top: 1rem;
  right: 1rem;
  z-index: 15;
}

.seal-text {
  width: 40px;
  height: 40px;
  background: linear-gradient(45deg, #8c7853, #2c3e50);
  border: 2px solid #2c3e50;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 1.1rem;
  font-family: 'KaiTi', '楷体', serif;
  transform: rotate(-8deg);
  box-shadow: 0 3px 10px rgba(44, 62, 80, 0.4);
}

// ============ 卡片边框装饰 ============
.card-border {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 20;
}

.border-line {
  position: absolute;
  background: linear-gradient(45deg, #8c7853, #2c3e50);
  
  &.top {
    top: 10px;
    left: 10px;
    width: 30px;
    height: 3px;
  }
  
  &.right {
    top: 10px;
    right: 10px;
    width: 3px;
    height: 30px;
  }
  
  &.bottom {
    bottom: 10px;
    right: 10px;
    width: 30px;
    height: 3px;
  }
  
  &.left {
    bottom: 10px;
    left: 10px;
    width: 3px;
    height: 30px;
  }
}

// ============ 底部信息 ============
.footer-section {
  text-align: center;
  margin-top: 3rem;
}

.stats-container {
  display: flex;
  justify-content: center;
  gap: 3rem;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: rgba(255, 255, 255, 0.8);
  font-size: 1rem;
  
  i {
    font-size: 1.2rem;
    color: #667eea;
  }
}

// ============ 返回按钮 ============
.back-button {
  position: fixed;
  top: 2rem;
  left: 2rem;
  background: rgba(44, 62, 80, 0.9);
  color: white;
  padding: 0.8rem 1.2rem;
  border-radius: 25px;
  cursor: pointer;
  z-index: 100;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  backdrop-filter: blur(10px);
  
  &:hover {
    background: rgba(44, 62, 80, 1);
    transform: translateY(-2px);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.3);
  }
}

// ============ 动画定义 ============
@keyframes floatUpDown {
  0% {
    transform: translateY(100vh) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-100px) rotate(360deg);
    opacity: 0;
  }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

// ============ 响应式设计 ============
@media (max-width: 768px) {
  .content-container {
    padding: 1rem;
  }
  
  .main-title {
    font-size: 2rem;
    flex-direction: column;
    gap: 0.5rem;
    
    .title-decoration {
      margin-left: 0;
      font-size: 0.9rem;
    }
  }
  
  .subtitle {
    font-size: 1rem;
  }
  
  .games-grid {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .game-card {
    min-height: 280px;
  }
  
  .card-content {
    padding: 1.5rem;
    min-height: 280px;
  }
  
  .stats-container {
    flex-direction: column;
    gap: 1rem;
  }
  
  .back-button {
    top: 1rem;
    left: 1rem;
    padding: 0.6rem 1rem;
    font-size: 0.8rem;
  }
}

@media (max-width: 480px) {
  .main-title {
    font-size: 1.8rem;
  }
  
  .game-card {
    min-height: 260px;
  }
  
  .card-content {
    padding: 1.2rem;
    min-height: 260px;
  }
  
  .game-title {
    font-size: 1.5rem;
  }
  
  .icon-container {
    width: 70px;
    height: 70px;
    font-size: 1.8rem;
  }
}
</style>