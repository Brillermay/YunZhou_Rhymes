<!-- filepath: c:\Users\Administrator\Desktop\YunZhou_Rhymes\poetry_front\src\components\homepage\FeatureShowcase.vue -->
<template>
  <div class="feature-showcase" :class="{ active: isActive }">
    <!-- 功能展示背景 -->
    <div class="features-background">
      <div class="dynamic-bg"></div>
      <div class="showcase-particles">
        <div 
          v-for="(particle, index) in particles" 
          :key="index"
          class="floating-particle"
          :style="particle.style"
        ></div>
      </div>
    </div>
    
    <!-- 功能展示内容 - 优化为单屏显示 -->
    <div class="features-content">
      <!-- 标题区域 -->
      <div class="showcase-header">
        <h1 class="showcase-title">
          <span class="title-char" v-for="(char, index) in titleChars" :key="index">
            {{ char }}
          </span>
        </h1>
        <p class="showcase-subtitle">探索中华诗词之美，体验古典文化魅力</p>
      </div>
      
      <!-- 功能卡片网格 - 2x3布局 -->
      <div class="features-grid">
        <div 
          v-for="(feature, index) in features" 
          :key="feature.id"
          class="feature-card"
          :class="{ visible: feature.visible }"
          @mouseenter="onCardHover(index, true)"
          @mouseleave="onCardHover(index, false)"
          @click="handleCardClick(feature)"
        >
          <!-- 卡片装饰边框 -->
          <div class="card-border">
            <div class="border-corner top-left"></div>
            <div class="border-corner top-right"></div>
            <div class="border-corner bottom-left"></div>
            <div class="border-corner bottom-right"></div>
          </div>
          
          <!-- 卡片内容 -->
          <div class="card-content">
            <!-- 功能状态标识 -->
            <div class="feature-status">
              <span class="status-text">可用</span>
            </div>
            
            <!-- 图标区域 -->
            <div class="feature-icon">
              <div class="icon-shape" :class="feature.iconClass">
                <i :class="feature.icon"></i>
              </div>
            </div>
            
            <!-- 功能标题 -->
            <h3 class="feature-title">{{ feature.title }}</h3>
            
            <!-- 功能描述 -->
            <p class="feature-description">{{ feature.description }}</p>
            
            <!-- 悬停效果 -->
            <div class="card-hover-effect">
              <div class="hover-content">
                <div class="hover-icon">
                  <i class="fas fa-hand-pointer"></i>
                </div>
                <div class="hover-text">点击体验</div>
              </div>
            </div>
          </div>
          
          <!-- 印章装饰 -->
          <div class="seal-decoration">
            <div class="seal-stamp">{{ feature.sealText }}</div>
          </div>
        </div>
      </div>
      
      <!-- 底部导航按钮 -->
      <div class="navigation-section">
        <button 
          class="explore-button"
          @click="navigateToRecommend"
        >
          <span class="button-bg"></span>
          <span class="button-text">
            <i class="fas fa-compass"></i>
            开始诗词之旅
          </span>
        </button>
      </div>
    </div>
    
    <!-- 返回按钮 -->
    <div class="back-button" @click="$emit('back-to-home')">
      <i class="fas fa-arrow-up"></i>
      <span>返回首页</span>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted,onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// Props
const props = defineProps({
  isActive: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['back-to-home'])

// 响应式数据
const titleChars = ref('功能展示'.split(''))
const particles = ref([])

// 功能列表 - 6个卡片，2x3布局
const features = reactive([
  {
    id: 'poetry-recommend',
    title: '智能推荐',
    description: '基于AI算法的个性化诗词推荐',
    icon: 'fas fa-brain',
    iconClass: 'icon-ai',
    sealText: '智',
    visible: false
  },
  {
    id: 'poetry-search',
    title: '诗词搜索',
    description: '海量诗词库，精准快速检索',
    icon: 'fas fa-search',
    iconClass: 'icon-search',
    sealText: '寻',
    visible: false
  },
  {
    id: 'game-center', // 🔧 修改：整合为游戏中心
    title: '游戏中心',
    description: '多款精品诗词游戏，寓教于乐',
    icon: 'fas fa-gamepad',
    iconClass: 'icon-game',
    sealText: '趣',
    visible: false,
    isHovered: false
  },
  {
    id: 'forum',
    title: '文学论坛',
    description: '诗友交流，共建诗词社区',
    icon: 'fas fa-comments',
    iconClass: 'icon-forum',
    sealText: '聚',
    visible: false
  },
    {
    id: 'userinfo',
    title: '个人中心',
    description: '管理个人信息与学习记录',
    icon: 'fas fa-user',
    iconClass: 'icon-user',
    sealText: '我',
    visible: false,
    isHovered: false
  },
  {
    id: 'ai-assistant',
    title: 'AI助手',
    description: '智能诗词创作指导助手',
    icon: 'fas fa-robot',
    iconClass: 'icon-robot',
    sealText: '助',
    visible: false
  }
])

// 创建背景粒子
const createParticles = () => {
  particles.value = []
  for (let i = 0; i < 20; i++) {
    particles.value.push({
      style: {
        left: `${Math.random() * 100}%`,
        top: `${Math.random() * 100}%`,
        animationDelay: `${Math.random() * 10}s`,
        animationDuration: `${8 + Math.random() * 4}s`
      }
    })
  }
}

// 开始卡片动画
const startCardsAnimation = () => {
  console.log('🎬 开始功能卡片动画')
  features.forEach((feature, index) => {
    setTimeout(() => {
      feature.visible = true
      console.log(`✨ 显示卡片 ${index + 1}: ${feature.title}`)
    }, index * 200)
  })
}

// 卡片悬停处理
const onCardHover = (index, isHovering) => {
  console.log(`🎯 卡片 ${index + 1} 悬停:`, isHovering)
}

// 修改卡片点击处理
const handleCardClick = (feature) => {
  console.log('🎯 点击功能卡片:', feature.title)
  
  const cardElement = event.currentTarget
  if (cardElement) {
    cardElement.style.transform = 'scale(0.95)'
    
    setTimeout(() => {
      if (cardElement) {
        cardElement.style.transform = ''
      }
    }, 150)
  }
  
  const routeMap = {
    'poetry-recommend': '/recommend',
    'poetry-search': '/search',
    'game-center': '/game-center', // 新增游戏中心路由
    'forum': '/forum',
    'userinfo': '/userinfo',
    'ai-assistant': '/qwenllm'
  }
  
  const route = routeMap[feature.id]
  if (route) {
    createPageTransition(() => {
      router.push(route)
    })
  }
}

// 修改导航到推荐页面
const navigateToRecommend = () => {
  console.log('🧭 导航到推荐页面')
  createPageTransition(() => {
    router.push('/recommend')
  })
}

// 页面切换动画 - 修复版本
const createPageTransition = (callback) => {
  console.log('🚀 开始页面过渡动画')
  
  // 首先清理现有的过渡元素
  const existingOverlay = document.querySelector('.page-transition-overlay')
  const existingStyle = document.querySelector('#page-transition-style')
  if (existingOverlay) existingOverlay.remove()
  if (existingStyle) existingStyle.remove()
  
  // 临时解除HomePage的滚动锁定
  const restoreScrollSettings = () => {
    document.documentElement.style.overflow = ''
    document.body.style.overflow = ''
    document.body.style.height = ''
    document.body.style.position = ''
    document.body.style.width = ''
  }
  
  const overlay = document.createElement('div')
  overlay.className = 'page-transition-overlay'
  overlay.style.cssText = `
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: radial-gradient(circle, transparent 0%, rgba(15,15,35,0.95) 100%);
    z-index: 9999;
    animation: pageTransitionFade 1s ease-in-out forwards;
    pointer-events: auto;
  `
  
  const style = document.createElement('style')
  style.id = 'page-transition-style'
  style.textContent = `
    @keyframes pageTransitionFade {
      0% {
        transform: scale(0);
        opacity: 0;
        border-radius: 50%;
      }
      50% {
        opacity: 1;
        border-radius: 20%;
      }
      100% {
        transform: scale(5);
        opacity: 1;
        border-radius: 0;
      }
    }
  `
  
  document.head.appendChild(style)
  document.body.appendChild(overlay)
  
  // 确保在跳转前清理所有状态
  setTimeout(() => {
    console.log('🔄 执行页面跳转')
    
    // 恢复滚动设置
    restoreScrollSettings()
    
    // 移除事件监听器（如果存在）
    try {
      document.removeEventListener('wheel', handleWheel, { capture: true })
    } catch (e) {
      console.log('清理事件监听器:', e.message)
    }
    
    // 执行跳转
    callback()
    
    // 延迟清理过渡元素
    setTimeout(() => {
      if (overlay && overlay.parentNode) {
        overlay.remove()
      }
      if (style && style.parentNode) {
        style.remove()
      }
      console.log('✅ 页面过渡完成，元素已清理')
    }, 500)
  }, 800)
}

// 监听激活状态
watch(() => props.isActive, (newVal) => {
  if (newVal) {
    console.log('📖 功能展示页面激活')
    // 延迟启动动画，确保页面切换完成
    setTimeout(() => {
      startCardsAnimation()
    }, 500)
  }
})

// 组件挂载
onMounted(() => {
  console.log('🎨 功能展示组件挂载')
  createParticles()
})

// 组件卸载时确保清理
onUnmounted(() => {
  console.log('🧹 功能展示组件卸载，清理资源')
  
  // 清理可能残留的过渡元素
  const overlay = document.querySelector('.page-transition-overlay')
  const style = document.querySelector('#page-transition-style')
  if (overlay) overlay.remove()
  if (style) style.remove()
  
  // 确保恢复滚动设置
  document.documentElement.style.overflow = ''
  document.body.style.overflow = ''
  document.body.style.height = ''
  document.body.style.position = ''
  document.body.style.width = ''
})
</script>

<style lang="scss" scoped>
.feature-showcase {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

// 功能展示背景
.features-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.dynamic-bg {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: 
    radial-gradient(circle at 20% 80%, rgba(120, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 80% 20%, rgba(255, 119, 198, 0.3) 0%, transparent 50%),
    radial-gradient(circle at 40% 40%, rgba(120, 219, 255, 0.2) 0%, transparent 50%);
  animation: backgroundShift 20s ease-in-out infinite;
}

.showcase-particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 2;
}

.floating-particle {
  position: absolute;
  width: 4px;
  height: 4px;
  background: radial-gradient(circle, #7777c6, #ff77c6);
  border-radius: 50%;
  animation: particleFloat 12s linear infinite;
  opacity: 0.6;
}

.features-content {
  position: relative;
  z-index: 10;
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 1.5rem;
  box-sizing: border-box;
}

// 标题区域
.showcase-header {
  text-align: center;
  margin-bottom: 2rem;
}

.showcase-title {
  font-size: 2.5rem;
  font-weight: 900;
  color: transparent;
  background: linear-gradient(45deg, #7777c6, #ff77c6, #77ddf6);
  background-size: 400% 400%;
  background-clip: text;
  -webkit-background-clip: text;
  animation: gradientShift 8s ease-in-out infinite;
  margin: 0 0 0.5rem;
  letter-spacing: 0.2rem;
  
  .title-char {
    display: inline-block;
    animation: titleReveal 0.8s ease-out forwards;
    opacity: 0;
    transform: translateY(50px);
    
    @for $i from 1 through 10 {
      &:nth-child(#{$i}) {
        animation-delay: #{$i * 0.1}s;
      }
    }
  }
}

.showcase-subtitle {
  font-size: 1rem;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
  letter-spacing: 0.1rem;
  animation: subtitleFade 1s ease-out 1s forwards;
  opacity: 0;
}

// 功能卡片网格 - 2x3布局
.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  grid-template-rows: repeat(2, 1fr);
  gap: 1.5rem;
  max-width: 900px;
  width: 100%;
  margin: 2rem 0;
}

.feature-card {
  position: relative;
  background: linear-gradient(145deg, 
    rgba(248, 243, 225, 0.95) 0%,
    rgba(245, 235, 210, 0.92) 100%
  );
  border-radius: 15px;
  border: 2px solid rgba(140, 120, 83, 0.3);
  backdrop-filter: blur(15px);
  overflow: hidden;
  transform: translateY(60px) rotateX(20deg);
  opacity: 0;
  transition: all 0.8s cubic-bezier(0.23, 1, 0.32, 1);
  cursor: pointer;
  min-height: 200px;
  
  &.visible {
    transform: translateY(0) rotateX(0);
    opacity: 1;
  }
  
  &:hover {
    transform: translateY(-8px) scale(1.05);
    border-color: rgba(140, 120, 83, 0.7);
    box-shadow: 
      0 15px 35px rgba(140, 120, 83, 0.4),
      0 8px 20px rgba(44, 62, 80, 0.3);
  }
}

// 卡片装饰边框
.card-border {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 3;
}

.border-corner {
  position: absolute;
  width: 25px;
  height: 25px;
  
  &::before, &::after {
    content: '';
    position: absolute;
    background: linear-gradient(45deg, #8c7853, #2c3e50);
    border-radius: 2px;
  }
  
  &.top-left {
    top: 10px;
    left: 10px;
    
    &::before {
      width: 3px;
      height: 15px;
      top: 0;
      left: 0;
    }
    &::after {
      width: 15px;
      height: 3px;
      top: 0;
      left: 0;
    }
  }
  
  &.top-right {
    top: 10px;
    right: 10px;
    
    &::before {
      width: 3px;
      height: 15px;
      top: 0;
      right: 0;
    }
    &::after {
      width: 15px;
      height: 3px;
      top: 0;
      right: 0;
    }
  }
  
  &.bottom-left {
    bottom: 10px;
    left: 10px;
    
    &::before {
      width: 3px;
      height: 15px;
      bottom: 0;
      left: 0;
    }
    &::after {
      width: 15px;
      height: 3px;
      bottom: 0;
      left: 0;
    }
  }
  
  &.bottom-right {
    bottom: 10px;
    right: 10px;
    
    &::before {
      width: 3px;
      height: 15px;
      bottom: 0;
      right: 0;
    }
    &::after {
      width: 15px;
      height: 3px;
      bottom: 0;
      right: 0;
    }
  }
}

.card-content {
  position: relative;
  z-index: 10;
  padding: 1.5rem;
  text-align: center;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

// 功能状态
.feature-status {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 10;
  
  .status-text {
    padding: 0.3rem 0.8rem;
    border-radius: 12px;
    font-size: 0.75rem;
    font-weight: 600;
    background: linear-gradient(45deg, #43e97b, #38f9d7);
    color: #2c3e50;
    box-shadow: 0 3px 10px rgba(67, 233, 123, 0.4);
  }
}

// 图标样式
.feature-icon {
  margin-bottom: 1rem;
  flex-shrink: 0;
}

.icon-shape {
  width: 60px;
  height: 60px;
  margin: 0 auto;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8rem;
  color: white;
  border: 3px solid rgba(255, 255, 255, 0.3);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
  
  &.icon-ai { background: linear-gradient(45deg, #667eea, #764ba2); }
  &.icon-search { background: linear-gradient(45deg, #f093fb, #f5576c); }
  &.icon-game { background: linear-gradient(45deg, #4facfe, #00f2fe); }
  &.icon-feather { background: linear-gradient(45deg, #43e97b, #38f9d7); }
  &.icon-forum { background: linear-gradient(45deg, #fa709a, #fee140); }
  &.icon-robot { background: linear-gradient(45deg, #a8edea, #fed6e3); }
}

// 标题样式
.feature-title {
  margin: 0 0 1rem;
  font-size: 1.3rem;
  font-weight: 700;
  color: #2c3e50;
  letter-spacing: 0.05rem;
  flex-shrink: 0;
}

.feature-description {
  font-size: 0.9rem;
  color: #666;
  line-height: 1.5;
  margin: 0;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

// 悬停效果
.card-hover-effect {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(45deg, 
    rgba(140, 120, 83, 0.95), 
    rgba(44, 62, 80, 0.9)
  );
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.4s ease;
  border-radius: 15px;
  
  .hover-content {
    text-align: center;
    color: white;
    
    .hover-icon {
      font-size: 2rem;
      margin-bottom: 0.8rem;
      animation: hoverPulse 1.5s ease-in-out infinite;
    }
    
    .hover-text {
      font-size: 1.1rem;
      font-weight: 700;
      letter-spacing: 0.05rem;
    }
  }
}

.feature-card:hover .card-hover-effect {
  opacity: 1;
}

// 印章装饰
.seal-decoration {
  position: absolute;
  bottom: 10px;
  right: 10px;
  z-index: 10;
}

.seal-stamp {
  width: 35px;
  height: 35px;
  background: linear-gradient(45deg, #8c7853, #2c3e50);
  border: 2px solid #2c3e50;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 700;
  font-size: 1rem;
  font-family: 'KaiTi', '楷体', serif;
  box-shadow: 0 3px 10px rgba(44, 62, 80, 0.4);
  transform: rotate(-8deg);
}

// 导航按钮区域
.navigation-section {
  text-align: center;
  margin-top: 2rem;
}

.explore-button {
  position: relative;
  padding: 1rem 2.5rem;
  background: transparent;
  border: 2px solid rgba(140, 120, 83, 0.7);
  border-radius: 30px;
  color: white;
  font-size: 1.1rem;
  font-weight: 600;
  letter-spacing: 0.1rem;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.4s ease;
  
  &:hover {
    border-color: rgba(140, 120, 83, 1);
    transform: translateY(-3px);
    box-shadow: 
      0 12px 25px rgba(140, 120, 83, 0.4),
      0 6px 15px rgba(0, 0, 0, 0.3);
  }
  
  .button-bg {
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, 
      transparent, 
      rgba(140, 120, 83, 0.8), 
      transparent
    );
    transition: left 0.6s ease;
  }
  
  .button-text {
    position: relative;
    z-index: 2;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.8rem;
  }
  
  &:hover .button-bg {
    left: 100%;
  }
}

// 返回按钮
.back-button {
  position: absolute;
  right: 2rem;
  bottom: 2rem;
  background: rgba(140, 120, 83, 0.9);
  color: white;
  padding: 0.8rem 1.2rem;
  border-radius: 25px;
  cursor: pointer;
  z-index: 100;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  
  &:hover {
    transform: translateY(-5px);
    background: rgba(140, 120, 83, 1);
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.4);
  }
  
  i {
    font-size: 1rem;
  }
}

// 动画定义
@keyframes backgroundShift {
  0%, 100% { transform: translateX(0) translateY(0); }
  50% { transform: translateX(-5px) translateY(5px); }
}

@keyframes particleFloat {
  0% { transform: translateY(100vh) rotate(0deg); opacity: 0; }
  10% { opacity: 0.6; }
  90% { opacity: 0.6; }
  100% { transform: translateY(-100px) rotate(360deg); opacity: 0; }
}

@keyframes gradientShift {
  0%, 100% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
}

@keyframes titleReveal {
  0% { opacity: 0; transform: translateY(50px); }
  100% { opacity: 1; transform: translateY(0); }
}

@keyframes subtitleFade {
  0% { opacity: 0; transform: translateY(20px); }
  100% { opacity: 1; transform: translateY(0); }
}

@keyframes hoverPulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.2); }
}

// 响应式设计
@media (max-width: 1024px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
    grid-template-rows: repeat(3, 1fr);
    max-width: 700px;
    gap: 1.2rem;
  }
  
  .showcase-title {
    font-size: 2.2rem;
  }
  
  .feature-card {
    min-height: 180px;
  }
}

@media (max-width: 768px) {
  .features-content {
    padding: 1rem;
  }
  
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
    grid-template-rows: repeat(3, 1fr);
    max-width: 500px;
    gap: 1rem;
  }
  
  .showcase-title {
    font-size: 1.8rem;
    letter-spacing: 0.15rem;
  }
  
  .feature-card {
    min-height: 150px;
  }
  
  .card-content {
    padding: 1.2rem;
  }
  
  .icon-shape {
    width: 50px;
    height: 50px;
    font-size: 1.5rem;
  }
  
  .feature-title {
    font-size: 1.1rem;
  }
  
  .feature-description {
    font-size: 0.8rem;
  }
}

@media (max-width: 480px) {
  .features-grid {
    grid-template-columns: 1fr;
    grid-template-rows: repeat(6, 1fr);
    max-width: 300px;
    gap: 0.8rem;
  }
  
  .showcase-title {
    font-size: 1.6rem;
  }
  
  .feature-card {
    min-height: 120px;
  }
  
  .card-content {
    padding: 1rem;
  }
  
  .icon-shape {
    width: 40px;
    height: 40px;
    font-size: 1.2rem;
  }
  
  .feature-title {
    font-size: 1rem;
    margin-bottom: 0.5rem;
  }
  
  .feature-description {
    font-size: 0.75rem;
  }
  
  .seal-stamp {
    width: 28px;
    height: 28px;
    font-size: 0.85rem;
  }
  
  .back-button {
    right: 1rem;
    bottom: 1rem;
    padding: 0.6rem 1rem;
    font-size: 0.8rem;
  }
}
</style>