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
          诗游小筑
          <span class="title-decoration">Poetry Game Center</span>
        </h1>
        <p class="subtitle">挥令对吟，诗酒趁年华；翻牌问答，风流动笔花。</p>
      </div>

      <!-- 游戏卡片网格 -->
      <div class="games-grid">
        <div 
          v-for="(game, index) in games" 
          :key="game.id"
          class="game-card"
          :class="{ 
            visible: game.visible,
            clicked: game.isClicked
          }"
          @click="navigateToGame(game, index)"
          @mouseenter="onCardHover(index, true)"
          @mouseleave="onCardHover(index, false)"
        >
          <!-- 其他内容保持不变 -->
          <!-- 卡片背景装饰 -->
          <div class="card-background">
            <div class="card-pattern"></div>
          </div>

          <!-- 卡片内容 -->
          <div class="card-content">
            <!-- 游戏图标 -->
            <div class="game-icon">
              <div class="icon-container" :class="game.iconClass">
                <!-- 使用图片替代字体图标 -->
                <img :src="game.iconImage" :alt="game.title" class="game-icon-image">
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
            <span>4款精品游戏</span>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

// 导入游戏logo图片
import feihuaLogo from '@/assets/gamelogo/feihualg.png'
import testLogo from '@/assets/gamelogo/Testlg.png'
import cardLogo from '@/assets/gamelogo/kapailg.png'
import multiplayLogo from '@/assets/gamelogo/duorenlg.png'

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
    iconImage: feihuaLogo,
    path: '/feihua',
    tags: ['对战', '古典', '挑战'],
    difficulty: 4,
    sealText: '雅',
    visible: false,
    isHovered: false,
    isClicked: false  // 新增点击状态
  },
  {
    id: 'poetry-test',
    title: '诗词测验',
    description: '全面测试您的诗词知识，提升文学素养',
    icon: 'fas fa-scroll',
    iconClass: 'icon-test',
    iconImage: testLogo,
    path: '/game',
    tags: ['学习', '测试', '进阶'],
    difficulty: 3,
    sealText: '学',
    visible: false,
    isHovered: false,
    isClicked: false
  },
  {
    id: 'poetry-game',
    title: '诗词卡牌',
    description: '创新卡牌收集游戏，在娱乐中感受诗词魅力',
    icon: 'fas fa-cards',
    iconClass: 'icon-cards',
    iconImage: cardLogo,
    path: '/play',
    tags: ['收集', '合成', '创新'],
    difficulty: 2,
    sealText: '趣',
    visible: false,
    isHovered: false,
    isClicked: false
  },
  {
    id: 'multiplay',
    title: '多人对战',
    description: '诗词卡牌对战游戏，考验诗词理解和博弈策略',
    icon: 'fas fa-feather-alt',
    iconClass: 'icon-multiplay',
    iconImage: multiplayLogo,
    path: '/multiplay',
    tags: ['对战', '古典', '挑战'],
    difficulty: 4,
    sealText: '智',
    visible: false,
    isHovered: false,
    isClicked: false
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

// 带点击动画的导航函数
const navigateToGame = (game, index) => {
  // 防止重复点击
  if (isNavigating.value) {
    return
  }
  
  console.log('🎮 启动游戏:', game.title)
  
  // 设置导航状态
  isNavigating.value = true
  
  // 设置点击状态
  games[index].isClicked = true
  
  // 重置所有悬停状态
  games.forEach(g => g.isHovered = false)
  
  // 等待点击动画完成后跳转
  setTimeout(() => {
    router.push(game.path).then(() => {
      console.log('🎮 跳转成功:', game.path)
      // 重置状态
      games[index].isClicked = false
      isNavigating.value = false
    }).catch((error) => {
      console.error('🎮 跳转失败:', error)
      // 重置状态
      games[index].isClicked = false
      isNavigating.value = false
    })
  }, 500) // 等待动画完成
}

// 组件挂载
onMounted(() => {
  console.log('🎮 游戏中心已挂载')
  
  // 确保页面状态正常
  isNavigating.value = false
  
  // 初始化动画
  createFloatingElements()
  setTimeout(startCardsAnimation, 500)
})

// 组件卸载时清理
onUnmounted(() => {
  console.log('🎮 游戏中心正在卸载')
  
  // 重置状态
  isNavigating.value = false
})
</script>

<style lang="scss" scoped>
.game-center {
  min-height: 100vh;
  background: 
    linear-gradient(135deg, #e8eceb 0%, #d5d8d6 60%, #f3efe6 100%),
    radial-gradient(circle at 60% 40%, rgba(120,130,120,0.08) 0%, transparent 70%),
    url('https://img2.baidu.com/it/u=1229568987,2188854087&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=800'); /* 可换为本地宣纸纹理 */
  background-blend-mode: lighten, normal;
  background-size: cover, cover, 400px 400px;
  background-repeat: repeat;
  position: relative;
  overflow: hidden;
}

// ============ 背景装饰 ============
.background-container {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%; z-index: 1;
}

.floating-elements {
  position: absolute;
  width: 100%;
  height: 100%;
}

.floating-element {
  position: absolute;
  font-size: 2.1rem;
  color: rgba(80, 80, 80, 0.22); // 更深更明显
  font-family: 'STKaiti', 'KaiTi', '楷体', serif;
  font-weight: bold;
  text-shadow: 0 2px 8px rgba(60,60,60,0.10);
  animation: floatUpDown linear infinite;
  pointer-events: none;
  user-select: none;
}

.gradient-overlay {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: 
    radial-gradient(circle at 20% 20%, rgba(120,130,120,0.10) 0%, transparent 60%),
    radial-gradient(circle at 80% 80%, rgba(120,130,120,0.08) 0%, transparent 60%);
}

// ============ 内容容器 ============
.content-container {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 10;
  max-width: 1200px;
  width: 90%;
  padding: 2rem;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

// ============ 页面标题 ============
.header-section {
  text-align: center;
  margin-bottom: 3rem;
  width: 100%;
}

.main-title {
  font-size: 3rem;
  font-weight: 900;
  color: #3e3a2f;
  margin: 0 0 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  
  .title-icon {
    font-size: 2.5rem;
    background: linear-gradient(45deg, #b6a179, #e2c391);
    background-clip: text;
    -webkit-background-clip: text;
    color: transparent;
  }
  
  .title-decoration {
    font-size: 1rem;
    color: #a89c7d;
    font-weight: 400;
    margin-left: 1rem;
    opacity: 0;
    animation: fadeIn 1s ease-out 1s forwards;
  }
}

.subtitle {
  font-size: 1.2rem;
  color: #7c715a;
  margin: 0;
  font-weight: 300;
  letter-spacing: 0.05rem;
  animation: slideUp 1s ease-out 0.5s both;
}

// ============ 游戏卡片网格 ============
.games-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1.4rem;
  margin: 2rem 0;
  width: 100%;
}

.game-card {
  position: relative;
  background: 
    linear-gradient(135deg, #e3e5e1 0%, #c9ccc8 100%),
    url('https://img2.baidu.com/it/u=1229568987,2188854087&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=800');
  background-blend-mode: multiply, normal;
  background-size: cover, 400px 400px;
  border-radius: 20px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 
    0 6px 18px rgba(80, 80, 80, 0.10),
    0 1px 4px rgba(80, 80, 80, 0.08);
  color: #2d2d2d;
  transition: all 0.6s cubic-bezier(0.23, 1, 0.32, 1);
  
  &.visible {
    transform: translateY(0) rotateX(0);
    opacity: 1;
  }
  
  &:hover {
    transform: translateY(-10px) scale(1.02);
    box-shadow: 
      0 12px 32px rgba(80, 80, 80, 0.18),
      0 8px 25px rgba(200, 180, 140, 0.10);
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
      rgba(180, 170, 140, 0.04) 10px,
      rgba(180, 170, 140, 0.04) 20px
    );
}

// ============ 卡片内容 ============
.card-content {
  position: relative;
  z-index: 10;
  padding: 1.2rem;
  height: 100%;
  min-height: 200px;
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
  color: #fff;
  position: relative;
  z-index: 2;
  
  // 保持原有的背景渐变
  &.icon-feihua { 
    background: white; 
  }
  &.icon-test { 
    background: white; 
  }
  &.icon-cards { 
    background: white; 
  }
  &.icon-multiplay { 
    background: white; 
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
  opacity: 0.15;
  z-index: 1;
}

// 新增游戏图标图片样式
.game-icon-image {
  width: 50px;
  height: 50px;
  object-fit: contain;
  border-radius: 8px;
  transition: all 0.3s ease;
  
  // 添加一些视觉效果
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.2));
  
  &:hover {
    transform: scale(1.1);
    filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.3));
  }
}

// 悬停时图标效果
.game-card:hover .game-icon-image {
  transform: scale(1.15);
  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.4));
}

// ============ 游戏信息 ============
.game-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.game-title {
  font-size: 1.6rem;
  font-weight: 700;
  color: #3e3a2f;
  margin: 0 0 1rem;
  text-align: center;
}

.game-description {
  font-size: 1rem;
  color: #7c715a;
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
  background: linear-gradient(45deg, #b6a179, #7c715a);
  color: #fff;
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
  color: #a89c7d;
  font-weight: 600;
}

.difficulty-stars {
  display: flex;
  gap: 0.2rem;
  
  .fas.fa-star {
    color: #b6a179;
    font-size: 0.9rem;
    transition: color 0.2s ease;
    
    &.active {
      color: #7c715a;
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
  background: 
    linear-gradient(135deg, 
      rgba(140, 130, 120, 0.18) 0%, 
      rgba(182, 161, 121, 0.25) 50%,
      rgba(120, 110, 100, 0.15) 100%
    );
  backdrop-filter: blur(2.6px);
  color: #2d2d2d;
  opacity: 0;
  transform: scale(0.95);
  transition: all 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  border-radius: 20px;
  
  display: flex;
  align-items: center;
  justify-content: center;
  
  // 水墨扩散效果
  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 0;
    height: 0;
    background: radial-gradient(circle, 
      rgba(182, 161, 121, 0.3) 0%, 
      rgba(140, 130, 120, 0.2) 40%, 
      transparent 70%
    );
    border-radius: 50%;
    transform: translate(-50%, -50%);
    transition: all 0.8s cubic-bezier(0.25, 0.46, 0.45, 0.94);
    opacity: 0;
  }
  
  // 水墨光晕
  &::after {
    content: '';
    position: absolute;
    top: 0; left: 0; right: 0; bottom: 0;
    background: 
      radial-gradient(circle at 30% 30%, rgba(226, 195, 145, 0.12) 0%, transparent 50%),
      radial-gradient(circle at 70% 70%, rgba(182, 161, 121, 0.08) 0%, transparent 50%);
    border-radius: 20px;
    opacity: 0;
    transition: all 0.6s ease;
  }
  
  &.active {
    opacity: 1;
    transform: scale(1);
    
    // 水墨扩散动画
    &::before {
      width: 280px;
      height: 280px;
      opacity: 1;
    }
    
    // 光晕增强
    &::after {
      opacity: 1;
    }
  }
}

.hover-content {
  text-align: center;
  z-index: 10;
  position: relative;
  transform: translateY(25px);
  opacity: 0;
  transition: all 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94);

}

// 当hover-overlay激活时，hover-content也要动画
.hover-overlay.active .hover-content {
  transform: translateY(0);
  opacity: 1;
  
  &::before {
    opacity: 1;
    transform: scale(1);
  }
  
  &::after {
    opacity: 1;
    transform: scale(1);
  }
}

// ============ 粒子浮动动画 ============
@keyframes particleFloat {
  0%, 100% {
    transform: translateY(0) scale(1);
    opacity: 0.8;
  }
  50% {
    transform: translateY(-8px) scale(2.6);
    opacity: 1;
  }
}
.play-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1rem;
  font-size: 1.5rem;
  animation: pulse 2s infinite;
  color: #b6a179;
}

.play-text {
  font-size: 1.2rem;
  font-weight: 600;
  letter-spacing: 0.1rem;
  color: #2d2d2d;
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
  background: linear-gradient(45deg, #e2c391, #b6a179);
  border: 2px solid #b6a179;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 700;
  font-size: 1.1rem;
  font-family: 'KaiTi', '楷体', serif;
  transform: rotate(-8deg);
  box-shadow: 0 3px 10px rgba(182, 161, 121, 0.15);
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
  background: linear-gradient(45deg, #e2c391, #b6a179);
  
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
  width: 100%;
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
  color: #a89c7d;
  font-size: 1rem;
  
  i {
    font-size: 1.2rem;
    color: #e2c391;
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
    width: 95%;
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
}

@media (max-width: 480px) {
  .content-container {
    width: 98%;
    padding: 0.8rem;
  }
  
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

// 在现有样式基础上添加点击动画
.game-card {
  // 原有样式保持不变...
  
  // 点击动画效果
  &.clicked {
    animation: gentleClick 0.6s ease-out;
    
    // 图标轻微旋转
    .game-icon-image {
      animation: iconBounce 0.6s ease-out;
    }
    
    // 标题轻微放大
    .game-title {
      animation: titleGlow 0.6s ease-out;
    }
    
    // 印章轻微摆动
    .seal-text {
      animation: sealWiggle 0.6s ease-out;
    }
  }
}

// ============ 点击动画定义 ============
@keyframes gentleClick {
  0% {
    transform: translateY(-10px) scale(1.02);
  }
  30% {
    transform: translateY(-8px) scale(0.98);
  }
  60% {
    transform: translateY(-12px) scale(1.01);
  }
  100% {
    transform: translateY(-10px) scale(1.02);
  }
}

@keyframes iconBounce {
  0% {
    transform: scale(1.15);
  }
  50% {
    transform: scale(1.25) rotate(5deg);
  }
  100% {
    transform: scale(1.15) rotate(0deg);
  }
}

@keyframes titleGlow {
  0% {
    transform: scale(1);
    color: #3e3a2f;
  }
  50% {
    transform: scale(1.05);
    color: #b6a179;
    text-shadow: 0 0 8px rgba(182, 161, 121, 0.4);
  }
  100% {
    transform: scale(1);
    color: #3e3a2f;
  }
}

@keyframes sealWiggle {
  0% {
    transform: rotate(-8deg);
  }
  25% {
    transform: rotate(-5deg) scale(1.1);
  }
  75% {
    transform: rotate(-10deg) scale(1.05);
  }
  100% {
    transform: rotate(-8deg) scale(1);
  }
}
</style>