<!-- filepath: c:\Users\Administrator\Desktop\YunZhou_Rhymes\poetry_front\src\components\homepage\HomePage.vue -->
<template>
  <div class="homepage-container" ref="homepageContainer">
    <!-- 页面指示器 -->
    <div class="page-nav">
      <div 
        v-for="(page, index) in pageCount" 
        :key="index"
        class="nav-dot"
        :class="{ active: currentPage === index }"
        @click="goToPage(index)"
      >
        <span class="nav-label">{{ index === 0 ? '首页' : '功能' }}</span>
      </div>
    </div>

    <!-- 页面内容容器 -->
    <div 
      class="pages-wrapper" 
      :style="{ transform: `translateY(-${currentPage * 100}vh)` }"
      ref="pagesWrapper"
    >
      <!-- 第一页：英雄页面 -->
      <section class="page page-hero" :class="{ active: currentPage === 0 }">
        <!-- 背景层 -->
        <div class="hero-background">
          <img 
            src="@/components/homepage/assets/images/bg1.jpg" 
            alt="首页背景"
            class="bg-image"
            @load="onImageLoad"
            @error="onImageError"
          />
          <div class="bg-overlay"></div>
        </div>

        <!-- 水墨背景 -->
        <WaterInkBackground 
          v-if="!isLoading && currentPage === 0"
          :mouse-position="mousePos"
          :page-loaded="true"
          :opacity="0.6"
          @background-ready="onBgReady"
        />

        <!-- 粒子效果 -->
        <ParticleSystem 
          v-if="showParticles && currentPage === 0"
          :mouse-position="mousePos"
          :intensity="0.5"
        />

        <!-- 主要内容 -->
        <div class="hero-content" :class="{ visible: contentReady }">
          <TitleAnimation 
            :start-animation="startAnimation"
            @animation-complete="onAnimationComplete"
          />
          
          <EntranceButton 
            v-if="showButton"
            @navigate="handleNavigate"
          />
        </div>

        <!-- 向下滚动提示 -->
        <div 
          v-if="showScrollHint" 
          class="scroll-hint"
          @click="goToPage(1)"
        >
          <span>探索更多功能</span>
          <div class="scroll-arrow">↓</div>
        </div>
      </section>

      <!-- 第二页：功能展示 -->
      <section class="page page-features" :class="{ active: currentPage === 1 }">
        <FeatureShowcase 
          :is-active="currentPage === 1"
          :page-transitioning="isPageTransitioning"
          @back-to-home="goToPage(0)"
        />
      </section>
    </div>

    <!-- 加载屏幕 -->
    <div v-if="isLoading" class="loading-screen">
      <div class="loading-content">
        <div class="spinner"></div>
        <p>{{ loadingText }}</p>
      </div>
    </div>
  </div>
    <div id="app">
    <!-- 🔧 修改：根据路由元信息决定是否显示导航 -->
    <Navigation v-if="!isBlankLayout" />
    
    <!-- 内容区域 -->
    <div class="content" :class="{ 'blank-layout': isBlankLayout }">
      <router-view></router-view>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import WaterInkBackground from './WaterInkBackground.vue'
import ParticleSystem from './ParticleSystem.vue'
import TitleAnimation from './TitleAnimation.vue'
import EntranceButton from './EntranceButton.vue'
import FeatureShowcase from './FeatureShowcase.vue'

const router = useRouter()

// ============ 状态管理 ============
const homepageContainer = ref(null)
const pagesWrapper = ref(null)
const currentPage = ref(0)
const pageCount = 2
const isLoading = ref(true)
const loadingProgress = ref(0)

// 内容状态
const imageLoaded = ref(false)
const contentReady = ref(false)
const showParticles = ref(false)
const showButton = ref(false)
const showScrollHint = ref(false)
const startAnimation = ref(false)

// 鼠标位置
const mousePos = reactive({ x: 0, y: 0 })

// 滚动控制 - 更严格的状态管理
const isScrolling = ref(false)
const isPageTransitioning = ref(false)
const scrollLocked = ref(false)
const scrollCooldown = 1000 // 增加冷却时间
const lastScrollTime = ref(0)

// ============ 计算属性 ============
const loadingText = computed(() => {
  if (loadingProgress.value < 30) return '加载资源中...'
  if (loadingProgress.value < 70) return '渲染背景...'
  if (loadingProgress.value < 90) return '准备就绪...'
  return '即将完成...'
})

// ============ 页面切换逻辑 - 完全重构 ============
const goToPage = (pageIndex) => {
  // 严格的状态检查
  if (isScrolling.value || isPageTransitioning.value || 
      scrollLocked.value || pageIndex === currentPage.value) {
    console.log('🚫 页面切换被阻止:', {
      isScrolling: isScrolling.value,
      isPageTransitioning: isPageTransitioning.value,
      scrollLocked: scrollLocked.value,
      samePagee: pageIndex === currentPage.value
    })
    return
  }
  
  console.log(`🔄 开始切换到第 ${pageIndex + 1} 页`)
  
  // 设置所有锁定状态
  isScrolling.value = true
  isPageTransitioning.value = true
  scrollLocked.value = true
  
  // 立即更新页面状态
  currentPage.value = pageIndex
  
  // 确保DOM更新完成后再处理
  nextTick(() => {
    // 如果切换到功能页，给更多时间让组件准备
    const unlockDelay = pageIndex === 1 ? 1500 : scrollCooldown
    
    setTimeout(() => {
      isScrolling.value = false
      setTimeout(() => {
        isPageTransitioning.value = false
        setTimeout(() => {
          scrollLocked.value = false
          console.log(`✅ 页面 ${pageIndex + 1} 切换完成，所有锁定已解除`)
        }, 200)
      }, 200)
    }, unlockDelay)
  })
}

// ============ 滚轮控制 - 完全重构 ============
const handleWheel = (event) => {
  // 强制阻止默认行为
  event.preventDefault()
  event.stopPropagation()
  
  // 多重检查，确保不在任何锁定状态
  if (isScrolling.value || isPageTransitioning.value || scrollLocked.value) {
    console.log('🚫 滚轮事件被忽略 - 页面正在切换中')
    return
  }
  
  const now = Date.now()
  const timeSinceLastScroll = now - lastScrollTime.value
  
  // 防抖 - 至少间隔500ms
  if (timeSinceLastScroll < 500) {
    console.log('🚫 滚轮事件被忽略 - 间隔太短')
    return
  }
  
  lastScrollTime.value = now
  
  const delta = event.deltaY
  const threshold = 30 // 降低阈值提高灵敏度
  
  // 滚动量检查
  if (Math.abs(delta) < threshold) {
    console.log('🚫 滚轮事件被忽略 - 滚动量太小')
    return
  }
  
  console.log(`🎯 滚轮事件: delta=${delta}, 当前页=${currentPage.value}`)
  
  // 根据方向切换页面
  if (delta > 0 && currentPage.value < pageCount - 1) {
    // 向下滚动
    console.log('📍 准备向下切换')
    goToPage(currentPage.value + 1)
  } else if (delta < 0 && currentPage.value > 0) {
    // 向上滚动
    console.log('📍 准备向上切换')
    goToPage(currentPage.value - 1)
  }
}

// ============ 键盘控制 ============
const handleKeyPress = (event) => {
  if (isScrolling.value || isPageTransitioning.value || scrollLocked.value) return
  
  switch (event.key) {
    case 'ArrowDown':
    case 'PageDown':
    case ' ': // 空格键
      event.preventDefault()
      if (currentPage.value < pageCount - 1) {
        goToPage(currentPage.value + 1)
      }
      break
    case 'ArrowUp':
    case 'PageUp':
      event.preventDefault()
      if (currentPage.value > 0) {
        goToPage(currentPage.value - 1)
      }
      break
    case 'Home':
      event.preventDefault()
      goToPage(0)
      break
    case 'End':
      event.preventDefault()
      goToPage(pageCount - 1)
      break
  }
}

// ============ 触摸控制 ============
let touchStartY = 0
let touchEndY = 0
let touchStartTime = 0

const handleTouchStart = (event) => {
  touchStartY = event.touches[0].clientY
  touchStartTime = Date.now()
}

const handleTouchEnd = (event) => {
  if (isScrolling.value || isPageTransitioning.value || scrollLocked.value) return
  
  touchEndY = event.changedTouches[0].clientY
  const touchEndTime = Date.now()
  const diff = touchStartY - touchEndY
  const duration = touchEndTime - touchStartTime
  const minSwipeDistance = 80
  const maxDuration = 800 // 最大滑动时间
  
  if (Math.abs(diff) < minSwipeDistance || duration > maxDuration) return
  
  if (diff > 0 && currentPage.value < pageCount - 1) {
    // 向上滑动（下一页）
    goToPage(currentPage.value + 1)
  } else if (diff < 0 && currentPage.value > 0) {
    // 向下滑动（上一页）
    goToPage(currentPage.value - 1)
  }
}

// ============ 资源加载 ============
const onImageLoad = () => {
  console.log('✅ 背景图片加载完成')
  imageLoaded.value = true
  loadingProgress.value = 50
  checkLoadingComplete()
}

const onImageError = () => {
  console.warn('⚠️ 背景图片加载失败')
  imageLoaded.value = true
  loadingProgress.value = 30
  checkLoadingComplete()
}

const onBgReady = () => {
  console.log('🎨 水墨背景准备完成')
  loadingProgress.value = 80
  checkLoadingComplete()
}

const checkLoadingComplete = () => {
  if (imageLoaded.value && loadingProgress.value >= 50) {
    setTimeout(() => {
      loadingProgress.value = 100
      isLoading.value = false
      startContentAnimation()
    }, 300)
  }
}

// ============ 内容动画 ============
const startContentAnimation = () => {
  setTimeout(() => {
    contentReady.value = true
    startAnimation.value = true
  }, 200)
}

const onAnimationComplete = () => {
  console.log('✨ 标题动画完成')
  setTimeout(() => {
    showButton.value = true
    showScrollHint.value = true
    showParticles.value = true
  }, 500)
}

// ============ 导航处理 ============
const handleNavigate = (route) => {
  console.log('🚀 导航到:', route)
  
  // 设置过渡锁定
  scrollLocked.value = true
  
  // 创建页面过渡效果
  const overlay = document.createElement('div')
  overlay.className = 'page-transition'
  overlay.style.cssText = `
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: radial-gradient(
      circle,
      transparent 0%,
      rgba(44, 62, 80, 0.9) 100%
    );
    z-index: 9999;
    animation: pageTransition 0.8s ease-in-out forwards;
  `
  
  // 添加动画样式
  if (!document.querySelector('#page-transition-style')) {
    const style = document.createElement('style')
    style.id = 'page-transition-style'
    style.textContent = `
      @keyframes pageTransition {
        0% {
          transform: scale(0);
          border-radius: 50%;
        }
        50% {
          border-radius: 20%;
        }
        100% {
          transform: scale(3);
          border-radius: 0;
        }
      }
    `
    document.head.appendChild(style)
  }
  
  document.body.appendChild(overlay)
  
  setTimeout(() => {
    router.push(route)
  }, 600)
}

// ============ 鼠标跟踪 ============
const handleMouseMove = (event) => {
  // 只在首页时跟踪鼠标
  if (currentPage.value === 0) {
    mousePos.x = event.clientX
    mousePos.y = event.clientY
  }
}

// ============ 生命周期 ============
onMounted(() => {
  console.log('🏠 首页启动')
  
  // 初始化加载进度
  loadingProgress.value = 10
  
  // 确保容器获得焦点以接收键盘事件
  if (homepageContainer.value) {
    homepageContainer.value.setAttribute('tabindex', '0')
    homepageContainer.value.focus()
  }
  
  // 绑定事件 - 使用捕获模式确保优先处理
  document.addEventListener('wheel', handleWheel, { 
    passive: false, 
    capture: true 
  })
  document.addEventListener('keydown', handleKeyPress, { 
    passive: false 
  })
  document.addEventListener('mousemove', handleMouseMove, { 
    passive: true 
  })
  document.addEventListener('touchstart', handleTouchStart, { 
    passive: true 
  })
  document.addEventListener('touchend', handleTouchEnd, { 
    passive: true 
  })
  
  // 禁用页面默认滚动行为
  document.documentElement.style.overflow = 'hidden'
  document.body.style.overflow = 'hidden'
  document.body.style.height = '100vh'
  document.body.style.position = 'fixed'
  document.body.style.width = '100%'
  
  // 保险机制 - 超时强制完成加载
  setTimeout(() => {
    if (isLoading.value) {
      console.warn('⏰ 加载超时，强制完成')
      onImageError()
    }
  }, 3000)
})

onUnmounted(() => {
  console.log('👋 首页卸载')
  
  // 移除所有事件监听
  document.removeEventListener('wheel', handleWheel, { capture: true })
  document.removeEventListener('keydown', handleKeyPress)
  document.removeEventListener('mousemove', handleMouseMove)
  document.removeEventListener('touchstart', handleTouchStart)
  document.removeEventListener('touchend', handleTouchEnd)
  
  // 恢复页面滚动
  document.documentElement.style.overflow = ''
  document.body.style.overflow = ''
  document.body.style.height = ''
  document.body.style.position = ''
  document.body.style.width = ''
  
  // 清理过渡元素
  const transition = document.querySelector('.page-transition')
  if (transition) transition.remove()
  
  const style = document.querySelector('#page-transition-style')
  if (style) style.remove()
})
</script>

<style lang="scss" scoped>
// ============ 全局容器 ============
.homepage-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background: #2c3e50;
  outline: none; // 移除焦点边框
}

// ============ 页面导航 ============
.page-nav {
  position: fixed;
  right: 2rem;
  top: 50%;
  transform: translateY(-50%);
  z-index: 1000;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.nav-dot {
  position: relative;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.4);
  border: 2px solid rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    transform: scale(1.2);
    background: rgba(255, 255, 255, 0.7);
    
    .nav-label {
      opacity: 1;
      transform: translateX(-120%);
    }
  }
  
  &.active {
    background: #fff;
    box-shadow: 0 0 20px rgba(255, 255, 255, 0.8);
    transform: scale(1.3);
  }
}

.nav-label {
  position: absolute;
  right: 100%;
  top: 50%;
  transform: translateY(-50%);
  color: #fff;
  font-size: 0.8rem;
  white-space: nowrap;
  opacity: 0;
  transition: all 0.3s ease;
  pointer-events: none;
  background: rgba(0, 0, 0, 0.8);
  padding: 0.3rem 0.6rem;
  border-radius: 4px;
}

// ============ 页面容器 ============
.pages-wrapper {
  width: 100%;
  height: 100%;
  // 更平滑的过渡效果
  transition: transform 1s cubic-bezier(0.19, 1, 0.22, 1);
  will-change: transform;
}

.page {
  width: 100vw;
  height: 100vh;
  position: relative;
  // 确保页面层级正确
  z-index: 1;
  
  &.active {
    z-index: 10;
  }
}

// ============ 英雄页面 ============
.page-hero {
  background: #2c3e50;
}

.hero-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.bg-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  object-position: center;
  opacity: 0;
  animation: fadeInImage 2s ease-out forwards;
}

.bg-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    135deg,
    rgba(44, 62, 80, 0.4) 0%,
    rgba(140, 120, 83, 0.2) 50%,
    rgba(44, 62, 80, 0.5) 100%
  );
}

.hero-content {
  position: relative;
  z-index: 10;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transform: translateY(50px);
  transition: all 1.5s ease-out;
  
  &.visible {
    opacity: 1;
    transform: translateY(0);
  }
}

.scroll-hint {
  position: absolute;
  bottom: 3rem;
  left: 50%;
  transform: translateX(-50%);
  z-index: 15;
  text-align: center;
  color: rgba(255, 255, 255, 0.8);
  cursor: pointer;
  transition: all 0.3s ease;
  
  &:hover {
    color: #fff;
    transform: translateX(-50%) translateY(-5px);
  }
  
  span {
    display: block;
    font-size: 0.9rem;
    margin-bottom: 0.5rem;
    letter-spacing: 0.1rem;
  }
}

.scroll-arrow {
  font-size: 1.5rem;
  animation: bounce 2s infinite;
}

// ============ 功能页面 ============
.page-features {
  background: linear-gradient(
    135deg,
    #0f0f23 0%,
    #1a1a2e 50%,
    #16213e 100%
  );
  
  // 确保功能页面的事件能正常工作
  &.active {
    pointer-events: auto;
  }
  
  &:not(.active) {
    pointer-events: none;
  }
}

// ============ 加载屏幕 ============
.loading-screen {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(
    135deg,
    rgba(44, 62, 80, 0.95) 0%,
    rgba(140, 120, 83, 0.9) 100%
  );
  z-index: 2000;
  display: flex;
  justify-content: center;
  align-items: center;
}

.loading-content {
  text-align: center;
  color: #fff;
}

.spinner {
  width: 60px;
  height: 60px;
  border: 3px solid rgba(255, 255, 255, 0.3);
  border-top: 3px solid #fff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

.loading-content p {
  font-size: 1.1rem;
  letter-spacing: 0.1rem;
  margin: 0;
  animation: pulse 2s ease-in-out infinite;
}

// ============ 动画定义 ============
@keyframes fadeInImage {
  0% {
    opacity: 0;
    transform: scale(1.1);
    filter: blur(5px);
  }
  100% {
    opacity: 1;
    transform: scale(1);
    filter: blur(0);
  }
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-10px);
  }
  60% {
    transform: translateY(-5px);
  }
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

// ============ 响应式设计 ============
@media (max-width: 768px) {
  .page-nav {
    right: 1rem;
    gap: 1rem;
  }
  
  .nav-dot {
    width: 10px;
    height: 10px;
  }
  
  .nav-label {
    font-size: 0.7rem;
    padding: 0.2rem 0.4rem;
  }
  
  .scroll-hint {
    bottom: 2rem;
    
    span {
      font-size: 0.8rem;
    }
  }
  
  .loading-content {
    padding: 1rem;
    
    .spinner {
      width: 50px;
      height: 50px;
    }
    
    p {
      font-size: 1rem;
    }
  }
}

@media (max-width: 480px) {
  .page-nav {
    right: 0.8rem;
  }
  
  .scroll-hint span {
    font-size: 0.75rem;
  }
  
  .scroll-arrow {
    font-size: 1.2rem;
  }
}
</style>