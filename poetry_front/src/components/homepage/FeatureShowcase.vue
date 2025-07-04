<!-- filepath: c:\Users\Administrator\Desktop\YunZhou_Rhymes\poetry_front\src\components\homepage\FeatureShowcase.vue -->
<template>
  <div class="feature-showcase" :class="{ active: isActive }">
    <!-- 背景装饰 -->
    <div class="background-container">
      <div class="floating-elements">
        <div v-for="(element, index) in floatingElements" :key="index" class="floating-element" :style="element.style">
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
          <span class="title-icon">🎋</span>
          功能展示
          <span class="title-decoration">Feature Showcase</span>
        </h1>
        <p class="subtitle">探索中华诗词之美，体验古典文化魅力</p>
      </div>

      <!-- 功能卡片网格 -->
      <div class="features-grid">
        <div v-for="(feature, index) in features" :key="feature.id" class="feature-card" :class="{
          visible: feature.visible,
          clicked: feature.isClicked
        }" @click="handleCardClick(feature, index)" @mouseenter="onCardHover(index, true)"
          @mouseleave="onCardHover(index, false)">
          <!-- 卡片背景装饰 -->
          <div class="card-background">
            <div class="card-pattern"></div>
          </div>

          <!-- 卡片内容 -->
          <div class="card-content">
            <!-- 功能图标 -->
            <div class="feature-icon">
              <div class="icon-container" :class="feature.iconClass">
                <!-- 使用自定义logo图片 -->
                <img :src="feature.logo" :alt="feature.title" class="feature-logo" />
              </div>
              <div class="icon-glow"></div>
            </div>

            <!-- 功能信息 -->
            <div class="feature-info">
              <h3 class="feature-title">{{ feature.title }}</h3>
              <p class="feature-description">{{ feature.description }}</p>

              <!-- 功能状态标签 -->
              <div class="feature-status">
                <span v-for="(tag, tagIndex) in feature.tags" :key="tagIndex" class="status-tag"
                  :class="`tag-${tagIndex + 1}`">
                  {{ tag }}
                </span>
              </div>
            </div>

            <!-- 悬停效果 -->
            <div class="hover-overlay" :class="{ active: feature.isHovered }">
              <div class="hover-content">
                <div class="play-icon">
                  <i class="fas fa-hand-pointer"></i>
                </div>
                <span class="play-text">点击体验</span>
              </div>
            </div>

            <!-- 装饰印章 -->
            <div class="feature-seal">
              <div class="seal-text">{{ feature.sealText }}</div>
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
            <i class="fas fa-star"></i>
            <span>6大核心功能</span>
          </div>
          <div class="stat-item">
            <i class="fas fa-book-open"></i>
            <span>诗词文化</span>
          </div>
          <div class="stat-item">
            <i class="fas fa-palette"></i>
            <span>古典美学</span>
          </div>
        </div>

        <!-- 导航按钮 -->
        <div class="navigation-section">
          <button class="explore-button" @click="navigateToRecommend">
            <span class="button-bg"></span>
            <span class="button-text">
              <i class="fas fa-compass"></i>
              开始诗词之旅
            </span>
          </button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
// filepath: /Users/motao/Desktop/yunzhou/YunZhou_Rhymes/poetry_front/src/components/homepage/FeatureShowcase.vue
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'

// 导入logo图片
import tuijianLogo from '@/assets/modelogo/tuijianlg.png'
import searchLogo from '@/assets/modelogo/sousuolg.png'
import gameLogo from '@/assets/modelogo/youxilg.png'
import forumLogo from '@/assets/modelogo/luntanlg.png'
import userLogo from '@/assets/modelogo/gerenlg.png'
import aiLogo from '@/assets/modelogo/ailg.png'

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

// 动画状态控制
const animationStarted = ref(false) // 新增：记录动画是否已开始
const floatingElements = ref([])

// 功能列表
const features = reactive([
  {
    id: 'poetry-recommend',
    title: '智能推荐',
    description: '基于AI算法的个性化诗词推荐系统',
    logo: tuijianLogo,
    iconClass: 'icon-ai',
    sealText: '智',
    tags: ['AI推荐', '个性化'],
    visible: false,
    isHovered: false,
    isClicked: false
  },
  {
    id: 'poetry-search',
    title: '诗词搜索',
    description: '海量诗词库，精准快速检索体验',
    logo: searchLogo,
    iconClass: 'icon-search',
    sealText: '寻',
    tags: ['海量资源', '快速检索'],
    visible: false,
    isHovered: false,
    isClicked: false
  },
  {
    id: 'game-center',
    title: '诗词游戏',
    description: '多款精品诗词游戏，寓教于乐',
    logo: gameLogo,
    iconClass: 'icon-game',
    sealText: '趣',
    tags: ['寓教于乐', '互动游戏'],
    visible: false,
    isHovered: false,
    isClicked: false
  },
  {
    id: 'forum',
    title: '文学论坛',
    description: '诗友交流互动，共建诗词社区',
    logo: forumLogo,
    iconClass: 'icon-forum',
    sealText: '聚',
    tags: ['社区交流', '诗友互动'],
    visible: false,
    isHovered: false,
    isClicked: false
  },
  {
    id: 'ai-assistant',
    title: 'AI诗友',
    description: '智能诗词创作指导与文学助手',
    logo: aiLogo,
    iconClass: 'icon-robot',
    sealText: '灵',
    tags: ['创作指导', '智能助手'],
    visible: false,
    isHovered: false,
    isClicked: false
  },
  {
    id: 'userinfo',
    title: '个人中心',
    description: '管理个人信息与学习记录',
    logo: userLogo,
    iconClass: 'icon-user',
    sealText: '心',
    tags: ['个人管理', '学习记录'],
    visible: false,
    isHovered: false,
    isClicked: false
  },
])

// 页面跳转状态管理
const isNavigating = ref(false)

// 创建浮动装饰元素
const createFloatingElements = () => {
  const symbols = ['诗', '词', '雅', '韵', '文', '墨', '🌸', '🍃', '🌙', '☁️', '📚', '🎋']
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

// 重置动画状态
const resetAnimation = () => {
  console.log('🔄 重置动画状态')
  animationStarted.value = false
  features.forEach(feature => {
    feature.visible = false
    feature.isHovered = false
    feature.isClicked = false
  })
}

// 卡片显示动画
const startCardsAnimation = () => {
  if (animationStarted.value) {
    console.log('⚠️ 动画已经开始，跳过重复执行')
    return
  }
  
  console.log('🎬 开始卡片动画')
  animationStarted.value = true
  
  features.forEach((feature, index) => {
    setTimeout(() => {
      feature.visible = true
    }, index * 300)
  })
}

// 卡片悬停处理
const onCardHover = (index, isHovering) => {
  if (!isNavigating.value) {
    features[index].isHovered = isHovering
  }
}

// 卡片点击处理
const handleCardClick = (feature, index) => {
  if (isNavigating.value) {
    return
  }

  console.log('🎯 点击功能卡片:', feature.title)
  
  isNavigating.value = true
  features[index].isClicked = true
  features.forEach(f => f.isHovered = false)

  const routeMap = {
    'poetry-recommend': '/recommend',
    'poetry-search': '/search',
    'game-center': '/game-center',
    'forum': '/forum',
    'userinfo': '/userinfo',
    'ai-assistant': '/qwenllm'
  }

  const route = routeMap[feature.id]
  if (route) {
    setTimeout(() => {
      router.push(route).then(() => {
        console.log('🎯 跳转成功:', route)
        features[index].isClicked = false
        isNavigating.value = false
      }).catch((error) => {
        console.error('🎯 跳转失败:', error)
        features[index].isClicked = false
        isNavigating.value = false
      })
    }, 500)
  }
}

// 导航到推荐页面
const navigateToRecommend = () => {
  console.log('🧭 导航到推荐页面')
  if (!isNavigating.value) {
    isNavigating.value = true
    router.push('/recommend').then(() => {
      isNavigating.value = false
    }).catch(() => {
      isNavigating.value = false
    })
  }
}

// 监听激活状态 - 这是主要的动画触发点
watch(() => props.isActive, (newVal, oldVal) => {
  console.log('📖 页面激活状态变化:', { newVal, oldVal })
  
  if (newVal && !oldVal) {
    // 只有从非激活状态变为激活状态时才触发动画
    console.log('🎬 页面激活，开始动画')
    setTimeout(() => {
      startCardsAnimation()
    }, 300) // 减少延迟，让动画更及时
  } else if (!newVal && oldVal) {
    // 页面离开时重置动画状态
    console.log('🔄 页面离开，重置动画状态')
    resetAnimation()
  }
})

// 组件挂载 - 只初始化，不触发动画
onMounted(() => {
  console.log('🎨 功能展示组件挂载')
  
  // 确保页面状态正常
  isNavigating.value = false
  
  // 初始化浮动元素
  createFloatingElements()
  
  // 如果挂载时就是激活状态，立即开始动画
  if (props.isActive) {
    console.log('🎬 挂载时页面已激活，开始动画')
    setTimeout(() => {
      startCardsAnimation()
    }, 300)
  }
})

// 组件卸载时清理
onUnmounted(() => {
  console.log('🧹 功能展示组件卸载，清理资源')
  
  // 重置状态
  isNavigating.value = false
  animationStarted.value = false
})
</script>

<style lang="scss" scoped>
.feature-showcase {
  min-height: 100vh;
  background:
    linear-gradient(135deg, #e8eceb 0%, #d5d8d6 60%, #f3efe6 100%),
    radial-gradient(circle at 60% 40%, rgba(120, 130, 120, 0.08) 0%, transparent 70%),
    url('https://img2.baidu.com/it/u=1229568987,2188854087&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=800');
  background-blend-mode: lighten, normal;
  background-size: cover, cover, 400px 400px;
  background-repeat: repeat;
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
  font-size: 1.9rem; // 从 1.8rem 调大一点
  color: rgba(80, 80, 80, 0.22);
  font-family: 'STKaiti', 'KaiTi', '楷体', serif;
  font-weight: bold;
  text-shadow: 0 2px 8px rgba(60, 60, 60, 0.10);
  animation: floatUpDown linear infinite;
  pointer-events: none;
  user-select: none;
}

.gradient-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background:
    radial-gradient(circle at 20% 20%, rgba(120, 130, 120, 0.10) 0%, transparent 60%),
    radial-gradient(circle at 80% 80%, rgba(120, 130, 120, 0.08) 0%, transparent 60%);
}

// ============ 内容容器 ============
.content-container {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 10;
  max-width: 1100px; // 从 1000px 调大一点
  width: 90%;
  padding: 1.8rem; // 从 1.5rem 调大一点
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

// ============ 页面标题 ============
.header-section {
  text-align: center;
  margin-bottom: 2.5rem; // 从 2rem 调大一点
  width: 100%;
}

.main-title {
  font-size: 2.5rem; // 从 2.2rem 调大一点
  font-weight: 900;
  color: #3e3a2f;
  margin-bottom:1rem; // 从 0.8rem 调大一点
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem; // 从 0.8rem 调大一点
  margin-top: 5rem;

  .title-icon {
    font-size: 2.2rem; // 从 2rem 调大一点
    background: linear-gradient(45deg, #b6a179, #e2c391);
    background-clip: text;
    -webkit-background-clip: text;
    color: transparent;
  }

  .title-decoration {
    font-size: 1rem; // 从 0.9rem 调大一点
    color: #a89c7d;
    font-weight: 400;
    margin-left: 1rem; // 从 0.8rem 调大一点
    opacity: 0;
    animation: fadeIn 1s ease-out 1s forwards;
  }
}

.subtitle {
  font-size: 1.1rem; // 从 1rem 调大一点
  color: #7c715a;
  margin: 0;
  font-weight: 300;
  letter-spacing: 0.05rem;
  animation: slideUp 1s ease-out 0.5s both;
}

// ============ 功能卡片网格 ============
.features-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.9rem; 
  margin: 1.5rem 0; 
  width: 100%;
}

.feature-card {
  position: relative;
  background:
    linear-gradient(135deg, #e3e5e1 0%, #c9ccc8 100%),
    url('https://img2.baidu.com/it/u=1229568987,2188854087&fm=253&fmt=auto&app=138&f=JPEG?w=800&h=800');
  background-blend-mode: multiply, normal;
  background-size: cover, 400px 400px;
  border-radius: 18px; // 从 16px 调大一点
  overflow: hidden;
  cursor: pointer;
  box-shadow:
    0 5px 15px rgba(80, 80, 80, 0.10), // 从 4px 12px 调大一点
    0 2px 4px rgba(80, 80, 80, 0.08); // 从 1px 3px 调大一点
  color: #2d2d2d;
  transition: all 0.6s cubic-bezier(0.23, 1, 0.32, 1);
  opacity: 0;
  transform: translateY(50px) rotateX(18deg); // 从 40px 15deg 调大一点

  &.visible {
    transform: translateY(0) rotateX(0);
    opacity: 1;
  }

  &:hover {
    transform: translateY(-8px) scale(1.018); // 从 -6px 1.015 调大一点
    box-shadow:
      0 10px 24px rgba(80, 80, 80, 0.15), // 从 8px 20px 调大一点
      0 6px 18px rgba(200, 180, 140, 0.10); // 从 4px 15px 调大一点
  }

  // 点击动画效果
  &.clicked {
    animation: gentleClick 0.6s ease-out;

    .feature-icon .icon-container {
      animation: iconBounce 0.6s ease-out;
    }

    .feature-title {
      animation: titleGlow 0.6s ease-out;
    }

    .seal-text {
      animation: sealWiggle 0.6s ease-out;
    }
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
    repeating-linear-gradient(45deg,
      transparent,
      transparent 10px,
      rgba(180, 170, 140, 0.04) 10px,
      rgba(180, 170, 140, 0.04) 20px);
}

// ============ 卡片内容 ============
.card-content {
  position: relative;
  z-index: 10;
  padding: 1.6rem;
  height: 100%;
  height: 230px;
  gap: 1.2rem;
  display: flex;
  flex-direction: column;
  gap: 1.3rem;
}

// ============ 功能图标 ============
.feature-icon {
  position: relative;
  text-align: center;
  margin-bottom: 1.2rem;
}

.icon-container {
  width: 70px;
  height: 70px;
  margin: 0 auto;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 2;
  background: rgba(255, 255, 255, 0.518);
}

// 新增logo图片样式
.feature-logo {
  width: 48px;
  height: 48px;
  object-fit: contain;
  border-radius: 8px;
  transition: all 0.3s ease;

  // 添加一些视觉效果
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.1));

  &:hover {
    transform: scale(1.1);
    filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.2));
  }
}

.icon-forum .feature-logo{
    width: 60px;
    height: 60px;
}

.icon-game .feature-logo{
    width: 60px;
    height: 60px;
}

// 悬停时logo效果
.feature-card:hover .feature-logo {
  transform: scale(1.15);
  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.3));
}

// 点击动画时logo效果
.feature-card.clicked .feature-logo {
  animation: logoSpin 0.6s ease-out;
}

.icon-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background: inherit;
  filter: blur(20px);
  opacity: 0.15;
  z-index: 1;
}

// ============ 功能信息 ============
.feature-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.feature-title {
  font-size: 1.4rem; // 从 1.3rem 调大一点
  font-weight: 700;
  color: #3e3a2f;
  margin: 0 0 0.9rem; // 从 0.8rem 调大一点
  text-align: center;
}

.feature-description {
  font-size: 0.9rem; // 从 0.85rem 调大一点
  color: #7c715a;
  line-height: 1.55; // 从 1.5 调大一点
  margin: 0 0 1.2rem; // 从 1rem 调大一点
  text-align: center;
  flex: 1;
}

.feature-status {
  display: flex;
  justify-content: center;
  margin-bottom: 1rem; // 从 0.8rem 调大一点
}

.status-tag {
  padding: 0.25rem 0.7rem; // 从 0.2rem 0.6rem 调大一点
  background: linear-gradient(45deg, #43e97b, #38f9d7);
  color: #2d2d2d;
  border-radius: 13px; // 从 12px 调大一点
  font-size: 0.75rem; // 从 0.7rem 调大一点
  font-weight: 600;
  box-shadow: 0 2px 7px rgba(67, 233, 123, 0.3); // 从 6px 调大一点
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
      rgba(120, 110, 100, 0.15) 100%);
  backdrop-filter: blur(2.6px);
  color: #2d2d2d;
  opacity: 0;
  transform: scale(0.95);
  transition: all 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  border-radius: 18px; // 从 16px 调大一点

  display: flex;
  align-items: center;
  justify-content: center;

  &.active {
    opacity: 1;
    transform: scale(1);
  }
}

.hover-content {
  text-align: center;
  z-index: 10;
  position: relative;
  transform: translateY(22px); // 从 20px 调大一点
  opacity: 0;
  transition: all 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.hover-overlay.active .hover-content {
  transform: translateY(0);
  opacity: 1;
}

.play-icon {
  width: 50px; // 从 45px 调大一点
  height: 50px; // 从 45px 调大一点
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 0.9rem; // 从 0.8rem 调大一点
  font-size: 1.3rem; // 从 1.2rem 调大一点
  animation: pulse 2s infinite;
  color: #b6a179;
}

.play-text {
  font-size: 1.1rem; // 从 1rem 调大一点
  font-weight: 600;
  letter-spacing: 0.1rem;
  color: #2d2d2d;
}

// ============ 装饰印章 ============
.feature-seal {
  position: absolute;
  top: 0.9rem; // 从 0.8rem 调大一点
  right: 0.9rem; // 从 0.8rem 调大一点
  z-index: 15;
}

.seal-text {
  width: 36px; // 从 32px 调大一点
  height: 36px; // 从 32px 调大一点
  background: linear-gradient(45deg, #e2c391, #b6a179);
  border: 2px solid #b6a179;
  border-radius: 7px; // 从 6px 调大一点
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 700;
  font-size: 1rem; // 从 0.9rem 调大一点
  font-family: 'KaiTi', '楷体', serif;
  transform: rotate(-8deg);
  box-shadow: 0 2px 7px rgba(182, 161, 121, 0.15); // 从 6px 调大一点
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
    top: 9px; // 从 8px 调大一点
    left: 9px; // 从 8px 调大一点
    width: 27px; // 从 24px 调大一点
    height: 2.5px; // 从 2px 调大一点
  }

  &.right {
    top: 9px; // 从 8px 调大一点
    right: 9px; // 从 8px 调大一点
    width: 2.5px; // 从 2px 调大一点
    height: 27px; // 从 24px 调大一点
  }

  &.bottom {
    bottom: 9px; // 从 8px 调大一点
    right: 9px; // 从 8px 调大一点
    width: 27px; // 从 24px 调大一点
    height: 2.5px; // 从 2px 调大一点
  }

  &.left {
    bottom: 9px; // 从 8px 调大一点
    left: 9px; // 从 8px 调大一点
    width: 2.5px; // 从 2px 调大一点
    height: 27px; // 从 24px 调大一点
  }
}

// ============ 底部信息 ============
.footer-section {
  text-align: center;
  margin-top: 2.5rem; // 从 2rem 调大一点
  width: 100%;
}

.stats-container {
  display: flex;
  justify-content: center;
  gap: 2.5rem; // 从 2rem 调大一点
  margin-bottom: 1rem; // 从 1.5rem 调大一点
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.45rem; // 从 0.4rem 调大一点
  color: #a89c7d;
  font-size: 0.95rem; // 从 0.9rem 调大一点

  i {
    font-size: 1.1rem; // 从 1rem 调大一点
    color: #e2c391;
  }
}

.navigation-section {
  text-align: center;

}

.explore-button {
  position: relative;
  padding: 0.9rem 2.2rem; // 从 0.8rem 2rem 调大一点
  background: transparent;
  border: 2px solid rgba(182, 161, 121, 0.7);
  border-radius: 27px; // 从 25px 调大一点
  color: #3e3a2f;
  font-size: 1.05rem; // 从 1rem 调大一点
  font-weight: 600;
  letter-spacing: 0.1rem;
  cursor: pointer;
  overflow: hidden;
  transition: all 0.4s ease;
  margin-bottom: 5rem;

  &:hover {
    border-color: rgba(182, 161, 121, 1);
    transform: translateY(-2px);
    box-shadow:
      0 10px 18px rgba(182, 161, 121, 0.4), // 从 8px 16px 调大一点
      0 5px 12px rgba(0, 0, 0, 0.1); // 从 4px 10px 调大一点
  }

  .button-bg {
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg,
        transparent,
        rgba(182, 161, 121, 0.2),
        transparent);
    transition: left 0.6s ease;
  }

  .button-text {
    position: relative;
    z-index: 2;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.7rem; // 从 0.6rem 调大一点
  }

  &:hover .button-bg {
    left: 100%;
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
  from {
    opacity: 0;
  }

  to {
    opacity: 1;
  }
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

  0%,
  100% {
    transform: scale(1);
  }

  50% {
    transform: scale(1.1);
  }
}

@keyframes gentleClick {
  0% {
    transform: translateY(-8px) scale(1.018); // 从 -6px 1.015 调大一点
  }

  30% {
    transform: translateY(-5px) scale(0.985); // 从 -4px 0.99 调大一点
  }

  60% {
    transform: translateY(-10px) scale(1.01); // 从 -8px 1.008 调大一点
  }

  100% {
    transform: translateY(-8px) scale(1.018); // 从 -6px 1.015 调大一点
  }
}

@keyframes iconBounce {
  0% {
    transform: scale(1);
  }

  50% {
    transform: scale(1.12) rotate(5deg); // 从 1.1 调大一点
  }

  100% {
    transform: scale(1) rotate(0deg);
  }
}

@keyframes titleGlow {
  0% {
    transform: scale(1);
    color: #3e3a2f;
  }

  50% {
    transform: scale(1.04); // 从 1.03 调大一点
    color: #b6a179;
    text-shadow: 0 0 7px rgba(182, 161, 121, 0.4); // 从 6px 调大一点
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
    transform: rotate(-5deg) scale(1.06); // 从 1.05 调大一点
  }

  75% {
    transform: rotate(-10deg) scale(1.03); // 从 1.02 调大一点
  }

  100% {
    transform: rotate(-8deg) scale(1);
  }
}

@keyframes logoSpin {
  0% {
    transform: scale(1.15) rotate(0deg);
  }

  50% {
    transform: scale(1.25) rotate(180deg);
  }

  100% {
    transform: scale(1.15) rotate(360deg);
  }
}


.feature-status {
  display: flex;
  justify-content: center;
  gap: 0.5rem; // 两个标签之间的间距
  margin-bottom: 1rem;
  flex-wrap: wrap; // 允许换行
}

.status-tag {
  padding: 0.2rem 0.6rem;
  border-radius: 12px;
  font-size: 0.7rem;
  font-weight: 500;
  color: #2d2d2d;
  transition: all 0.3s ease;
  
  background: linear-gradient(45deg, #b5f857, #e2c391);
  box-shadow: 0 2px 6px rgba(182, 161, 121, 0.3);
  
  // 悬停效果
  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  }
}

// 标签动画效果
.feature-card.clicked .status-tag {
  animation: tagBounce 0.6s ease-out;
}

@keyframes tagBounce {
  0% {
    transform: scale(1);
  }
  30% {
    transform: scale(1.1);
  }
  60% {
    transform: scale(0.95);
  }
  100% {
    transform: scale(1);
  }
}

// ============ 响应式设计 ============
@media (max-width: 1024px) {
  .features-grid {
    grid-template-columns: repeat(2, 1fr);
    max-width: 650px; // 从 600px 调大一点
    gap: 1.1rem; // 从 1rem 调大一点
  }

  .main-title {
    font-size: 2.2rem; // 从 2rem 调大一点
  }

  .feature-card {
    min-height: 160px; // 从 140px 调大一点
  }
}

@media (max-width: 768px) {
  .content-container {
    padding: 1.2rem; // 从 1rem 调大一点
  }

  .features-grid {
    grid-template-columns: repeat(2, 1fr);
    max-width: 500px; // 从 450px 调大一点
    gap: 1rem; // 从 0.8rem 调大一点
  }

  .main-title {
    font-size: 2rem; // 从 1.8rem 调大一点
    flex-direction: column;
    gap: 0.5rem;

    .title-decoration {
      margin-left: 0;
      font-size: 0.85rem; // 从 0.8rem 调大一点
    }
  }

  .subtitle {
    font-size: 0.95rem; // 从 0.9rem 调大一点
  }

  .feature-card {
    min-height: 140px; // 从 120px 调大一点
  }

  .card-content {
    padding: 1rem; // 从 0.8rem 调大一点
  }

  .icon-container {
    width: 55px; // 从 50px 调大一点
    height: 55px; // 从 50px 调大一点
    font-size: 1.3rem; // 从 1.2rem 调大一点
  }

  .feature-title {
    font-size: 1.2rem; // 从 1.1rem 调大一点
  }

  .feature-description {
    font-size: 0.8rem; // 从 0.75rem 调大一点
  }

  .stats-container {
    flex-direction: column;
    gap: 1rem; // 从 0.8rem 调大一点
  }
}

@media (max-width: 480px) {
  .features-grid {
    grid-template-columns: 1fr;
    max-width: 320px; // 从 280px 调大一点
    gap: 0.8rem; // 从 0.6rem 调大一点
  }

  .main-title {
    font-size: 1.8rem; // 从 1.6rem 调大一点
  }

  .feature-card {
    min-height: 130px; // 从 110px 调大一点
  }

  .card-content {
    padding: 0.8rem; // 从 0.6rem 调大一点
  }

  .icon-container {
    width: 45px; // 从 40px 调大一点
    height: 45px; // 从 40px 调大一点
    font-size: 1.1rem; // 从 1rem 调大一点
  }

  .feature-title {
    font-size: 1.1rem; // 从 1rem 调大一点
  }

  .feature-description {
    font-size: 0.75rem; // 从 0.7rem 调大一点
  }

  .seal-text {
    width: 30px; // 从 28px 调大一点
    height: 30px; // 从 28px 调大一点
    font-size: 0.85rem; // 从 0.8rem 调大一点
  }

  .explore-button {
    padding: 0.7rem 1.8rem; // 从 0.6rem 1.5rem 调大一点
    font-size: 0.95rem; // 从 0.9rem 调大一点
  }
}
</style>