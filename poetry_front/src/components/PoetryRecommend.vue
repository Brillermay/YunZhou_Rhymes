<template>
  <div class="poetry-layout">
    <!-- 标题部分 - 添加overflow hidden容器 -->
    <header class="poetry-header">
      <div class="title-container">
        <h1 ref="titleRef">瑶池集萃</h1>
        <p ref="subtitleRef" class="subtitle">"翻阅古韵今朝读，妙句良篇入眼来"</p>
      </div>
    </header>
    
    <main class="poetry-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <div class="loading-spinner"></div>
        <p>正在加载诗词...</p>
      </div>
      
      <!-- 错误状态 -->
      <div v-else-if="error" class="error-container">
        <p>{{ error }}</p>
        <button @click="loadPoems" class="retry-btn">重新加载</button>
      </div>
      
      <!-- 诗词轮播 -->
      <div v-else class="carousel">
        <!-- 每个卡片项 -->
        <div 
          v-for="(poem, index) in allPoems" 
          :key="poem.pid || index"
          class="poem-card"
          :class="{ 
            'active': index === currentIndex,
            'prev': (index === currentIndex - 1) || (index === allPoems.length - 1 && currentIndex === 0),
            'next': (index === currentIndex + 1) || (index === 0 && currentIndex === allPoems.length - 1),
            'hidden': Math.abs(getCircularDistance(index, currentIndex, allPoems.length)) > 1
          }"
        >
          <div class="card-inner" :style="getCardBackgroundStyle(poem)">
            <div class="card-header">
              <h3>{{ poem?.category || '古典诗词' }} · {{ poem?.title || '未知' }}</h3>
              <p class="author">{{ poem?.poet || '佚名' }}</p>
            </div>
            <div class="card-body">
              <div class="poem-content">
                <template v-if="poem?.text">
                  <div v-for="(line, i) in formatPoemText(poem.text)" :key="i" class="poem-line">{{ line }}</div>
                </template>
                <template v-else>
                  <div class="poem-placeholder">敬请期待</div>
                </template>
              </div>
              <!-- 添加诗词背景信息 -->
              <div v-if="poem?.background" class="poem-info">
                <h4>背景</h4>
                <p>{{ poem.background }}</p>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 导航按钮 -->
        <button class="nav-button prev-button" @click="moveCards('prev')">&#10094;</button>
        <button class="nav-button next-button" @click="moveCards('next')">&#10095;</button>
      </div>
      
      <!-- 指示器 -->
      <div v-if="!loading && !error" class="carousel-indicators">
        <span
          v-for="(_, index) in allPoems.length"
          :key="index"
          :class="['indicator', { active: index === currentIndex }]"
          @click="goToPoem(index)"
        ></span>
      </div>
      
      <!-- 刷新按钮 -->
      <div v-if="!loading && !error" class="refresh-container">
        <button @click="loadPoems" class="refresh-btn">🔄 换一批诗词</button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue';
import gsap from 'gsap';

// DOM 引用
const titleRef = ref(null);
const subtitleRef = ref(null);

const MAX_POEMS = 10;
const API_BASE_URL = 'http://localhost:8081/poem';
const allPoems = reactive([]);
const currentIndex = ref(0);
const loading = ref(false);
const error = ref('');

// 标题下落动画
const animateTitleDrop = () => {
  const tl = gsap.timeline();
  
  // 初始状态：将标题和副标题移到容器上方（不可见区域）
  gsap.set(titleRef.value, { 
    y: -120, // 移到上方120px（完全看不见）
    opacity: 0,
    rotationX: -90 // 添加3D旋转效果
  });
  
  gsap.set(subtitleRef.value, { 
    y: -80,
    opacity: 0,
    scale: 0.8
  });
  
  // 标题下落动画
  tl.to(titleRef.value, {
    y: 0,
    opacity: 1,
    rotationX: 0,
    duration: 1.2,
    ease: "bounce.out",
    delay: 0.5
  })
  // 副标题跟随动画
  .to(subtitleRef.value, {
    y: 0,
    opacity: 1,
    scale: 1,
    duration: 0.8,
    ease: "back.out(1.7)"
  }, "-=0.6"); // 在主标题动画进行到一半时开始
  
  // 添加一个轻微的浮动效果
  tl.to([titleRef.value, subtitleRef.value], {
    y: "+=5",
    duration: 2,
    ease: "power1.inOut",
    yoyo: true,
    repeat: -1
  });
};

// 生成随机不重复的ID数组
const generateRandomIds = (max, count) => {
  const ids = new Set();
  while (ids.size < count) {
    ids.add(Math.floor(Math.random() * max) + 1);
  }
  return Array.from(ids);
};

// 格式化诗词文本
const formatPoemText = (text) => {
  if (!text) return [];
  return text.split(/[。！？；]/).filter(line => line.trim()).map(line => line.trim() + (line.includes('，') ? '' : ''));
};

// 调用Spring Boot API获取单首诗词
const fetchPoemById = async (id) => {
  try {
    console.log(`正在获取诗词ID: ${id}`);
    const response = await fetch(`${API_BASE_URL}/${id}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      }
    });
    
    if (!response.ok) {
      console.error(`API请求失败，状态码: ${response.status}`);
      return null;
    }
    
    const data = await response.json();
    console.log(`成功获取诗词ID ${id}:`, data);
    
    // 验证返回的数据是否完整
    if (!data || !data.title || !data.text) {
      console.warn(`诗词ID ${id} 数据不完整:`, data);
      return null;
    }
    
    return data;
  } catch (err) {
    console.error(`获取诗词ID ${id} 失败:`, err);
    return null;
  }
};

// 加载诗词数据
const loadPoems = async () => {
  loading.value = true;
  error.value = '';
  allPoems.splice(0, allPoems.length);
  
  try {
    console.log('开始加载诗词数据...');
    
    // 生成随机ID数组，范围根据你的数据库实际情况调整
    const randomIds = generateRandomIds(1000, MAX_POEMS * 2);
    console.log('生成的随机ID:', randomIds);
    
    // 逐个请求诗词（避免并发过多导致服务器压力）
    const validPoems = [];
    let requestCount = 0;
    
    for (const id of randomIds) {
      if (validPoems.length >= MAX_POEMS) break;
      
      requestCount++;
      const poem = await fetchPoemById(id);
      
      if (poem) {
        // 为诗词添加背景图
        validPoems.push({
          ...poem,
          backgroundImage: '/poetry_bg_1.jpg'
        });
        console.log(`成功添加诗词: ${poem.title}`);
      }
      
      // 添加小延迟，避免请求过于频繁
      if (requestCount % 3 === 0) {
        await new Promise(resolve => setTimeout(resolve, 100));
      }
    }
    
    console.log(`最终获取到 ${validPoems.length} 首有效诗词`);
    
    if (validPoems.length === 0) {
      throw new Error('未能获取到任何有效的诗词数据，请检查后端服务是否正常');
    }
    
    // 添加获取到的诗词
    allPoems.push(...validPoems);
    
    console.log(`成功加载 ${allPoems.length} 首诗词`);
    
    // 重置当前索引
    currentIndex.value = 0;
    
  } catch (err) {
    console.error('加载诗词时发生错误:', err);
    error.value = err.message || '加载诗词失败，请检查网络连接或稍后重试';
  } finally {
    loading.value = false;
  }
};

// 获取卡片背景样式
const getCardBackgroundStyle = (poem) => {
  const imagePath = '/poetry_bg_1.jpg';
  return {
    backgroundImage: `url(${imagePath})`,
    backgroundSize: 'cover',
    backgroundPosition: 'center',
    backgroundRepeat: 'no-repeat'
  };
};

// 计算循环距离
const getCircularDistance = (index1, index2, length) => {
  const direct = index1 - index2;
  const wrap = direct > 0 ? direct - length : direct + length;
  return Math.abs(direct) < Math.abs(wrap) ? direct : wrap;
};

// 切换卡片
const moveCards = (direction) => {
  if (allPoems.length === 0) return;
  
  if (direction === 'next') {
    currentIndex.value = (currentIndex.value + 1) % allPoems.length;
  } else {
    currentIndex.value = (currentIndex.value - 1 + allPoems.length) % allPoems.length;
  }
};

// 跳转到指定诗词
const goToPoem = (index) => {
  if (index >= 0 && index < allPoems.length) {
    currentIndex.value = index;
  }
};

onMounted(async () => {
  console.log('组件已挂载，开始标题动画');
  
  // 等待DOM更新
  await nextTick();
  
  // 播放标题下落动画
  animateTitleDrop();
  
  // 延迟加载诗词，让用户先看到标题动画
  setTimeout(() => {
    loadPoems();
  }, 1000);
});
</script>

<style scoped>
/* 基础布局 */
.poetry-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: linear-gradient(135deg, #f5efe6 0%, #faf8f3 100%);
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  overflow: hidden;
}

/* 头部区域 */
.poetry-header {
  text-align: center;
  padding: 2rem 0 1rem 0;
  background: linear-gradient(135deg, #8c7853 0%, #6e5773 100%);
  color: white;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  width: 100%;
  position: relative;
  overflow: hidden;
}

.title-container {
  position: relative;
  max-width: 800px;
  margin: 0 auto;
  padding: 0 1rem;
}

.poetry-header h1 {
  margin: 0;
  font-size: 2.5rem;
  font-weight: 300;
  color: white;
  text-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
  transform-origin: center center;
  transform-style: preserve-3d;
  letter-spacing: 0.1em;
}

.poetry-header .subtitle {
  margin: 1rem 0 0;
  font-size: 1rem;
  font-style: italic;
  opacity: 0.9;
  transform-origin: center center;
  color: rgba(255, 255, 255, 0.9);
}

/* 主容器 */
.poetry-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  padding: 2rem 1rem;
  position: relative;
}

/* 加载和错误状态 */
.loading-container, 
.error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  color: #8c7853;
  background: white;
  border-radius: 20px;
  padding: 2rem;
  box-shadow: 0 8px 30px rgba(140, 120, 83, 0.1);
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.loading-container:hover,
.error-container:hover {
  border-color: rgba(140, 120, 83, 0.3);
  transform: translateY(-2px);
  box-shadow: 0 12px 40px rgba(140, 120, 83, 0.15);
}

.loading-spinner {
  width: 50px;
  height: 50px;
  border: 4px solid rgba(140, 120, 83, 0.1);
  border-top: 4px solid #8c7853;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1.5rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-container p,
.error-container p {
  font-size: 1.1rem;
  color: #666;
  margin: 0;
}

/* 按钮基础样式 */
.retry-btn, 
.refresh-btn {
  background: linear-gradient(135deg, #8c7853, #6e5773);
  color: white;
  border: 2px solid transparent;
  padding: 0.8rem 2rem;
  border-radius: 25px;
  cursor: pointer;
  font-size: 1rem;
  font-weight: 500;
  transition: all 0.3s ease;
  margin-top: 1.5rem;
  box-shadow: 0 4px 15px rgba(140, 120, 83, 0.2);
  position: relative;
  overflow: hidden;
}

.retry-btn:hover, 
.refresh-btn:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(140, 120, 83, 0.3);
  border-color: rgba(255, 255, 255, 0.3);
}

.retry-btn:active, 
.refresh-btn:active {
  transform: translateY(-1px);
}

.refresh-container {
  margin-top: 2rem;
  text-align: center;
}

/* 轮播容器 */
.carousel {
  position: relative;
  width: 100%;
  max-width: 1400px;
  height: 650px;
  display: flex;
  align-items: center;
  justify-content: center;
  perspective: 1000px;
}

/* 诗词卡片 */
.poem-card {
  position: absolute;
  transition: all 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  will-change: transform, opacity, filter;
  transform-style: preserve-3d;
}

.poem-card.active {
  transform: translateX(0) translateZ(0) scale(1);
  z-index: 10;
  opacity: 1;
  filter: blur(0px);
}

.poem-card.prev {
  transform: translateX(-280px) translateZ(-100px) scale(0.85) rotateY(15deg);
  z-index: 5;
  opacity: 0.7;
  filter: blur(1px);
}

.poem-card.next {
  transform: translateX(280px) translateZ(-100px) scale(0.85) rotateY(-15deg);
  z-index: 5;
  opacity: 0.7;
  filter: blur(1px);
}

.poem-card.hidden {
  opacity: 0;
  transform: translateX(0) translateZ(-200px) scale(0.6);
  pointer-events: none;
  z-index: 1;
  filter: blur(3px);
}

/* 卡片内容 */
.card-inner {
  border-radius: 24px;
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  width: 550px;
  height: 650px;
  position: relative;
  background: white;
  
  /* 添加边框效果 */
  border: 3px solid transparent;
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  
  /* 添加背景渐变边框 */
  background-clip: padding-box;
}

/* 卡片背景遮罩 */
/*.card-inner::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.25);
  z-index: 1;
  pointer-events: none;
  transition: all 0.3s ease;
  border-radius: 21px;
}
*/




/* 悬停效果 */
.poem-card.active .card-inner:hover {
  transform: translateY(-8px) scale(1.02);
  border-color: #8c7853;
  box-shadow: 0 20px 60px rgba(140, 120, 83, 0.25);
}



/*.poem-card.active .card-inner:hover::before {
  background: rgba(255, 255, 255, 0.15);
}
*/
/* 侧边卡片悬停效果 */
.poem-card.prev .card-inner:hover,
.poem-card.next .card-inner:hover {
  transform: scale(0.88);
  border-color: rgba(140, 120, 83, 0.6);
  box-shadow: 0 15px 45px rgba(140, 120, 83, 0.2);
}

/* 卡片头部和内容 */
.card-header, 
.card-body {
  position: relative;
  z-index: 2;
  transition: transform 0.3s ease;
}

.poem-card.active .card-inner:hover .card-header {
  transform: translateY(-3px);
}

.poem-card.active .card-inner:hover .card-body {
  transform: translateY(-2px);
}

.card-header {
  padding: 2rem 2rem 1.5rem;
  border-bottom: 2px dashed rgba(140, 120, 83, 0.3);
  text-align: center;
  /*background: linear-gradient(135deg, rgba(255, 255, 255, 0.9), rgba(248, 244, 237, 0.9));*/
  /*backdrop-filter: blur(10px);*/
}

.card-header h3 {
  margin: 0;
  font-size: 1.4rem;
  color: #8c7853;
  font-family: 'SimSun', '宋体', serif;
  font-weight: 500;
  letter-spacing: 0.05em;
  transition: all 0.3s ease;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.poem-card.active .card-inner:hover .card-header h3 {
  color: #6e5773;
  transform: scale(1.05);
}

.author {
  margin-top: 0.8rem;
  font-size: 1.1rem;
  color: #5a4634;
  font-style: italic;
  font-weight: 500;
  transition: all 0.3s ease;
  opacity: 0.9;
}

.poem-card.active .card-inner:hover .author {
  color: #4a3624;
  opacity: 1;
}

/* 卡片主体 */
.card-body {
  flex: 1;
  padding: 1.5rem 2rem 2rem;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  /*background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(5px);*/
}

.poem-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
  margin-bottom: 1.5rem;
  flex: 1;
}

.poem-line {
  text-align: center;
  margin: 0.5rem 0;
  font-size: 1.1rem;
  line-height: 1.8;
  color: #2d1810;
  font-family: 'KaiTi', '楷体', 'STKaiti', serif;
  font-weight: 500;
  transition: all 0.3s ease;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.poem-card.active .card-inner:hover .poem-line {
  color: #1a0e08;
  transform: translateX(2px);
}

.poem-placeholder {
  text-align: center;
  color: #8c7853;
  opacity: 0.7;
  font-style: italic;
  font-size: 1.2rem;
  padding: 2rem;
}

/* 诗词信息 */
.poem-info {
  margin-top: 1rem;
  padding: 1rem;
  background: linear-gradient(135deg, rgba(140, 120, 83, 0.1), rgba(110, 87, 115, 0.1));
  border-radius: 12px;
  border: 1px solid rgba(140, 120, 83, 0.2);
  transition: all 0.3s ease;
}

.poem-card.active .card-inner:hover .poem-info {
  background: linear-gradient(135deg, rgba(140, 120, 83, 0.15), rgba(110, 87, 115, 0.15));
  border-color: rgba(140, 120, 83, 0.3);
}

.poem-info h4 {
  margin: 0 0 0.5rem 0;
  font-size: 0.9rem;
  color: #8c7853;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.poem-info p {
  margin: 0;
  font-size: 0.85rem;
  line-height: 1.5;
  color: #5a4634;
  max-height: 4em;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 导航按钮 */
.nav-button {
  background: rgba(255, 255, 255, 0.9);
  border: 2px solid transparent;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  font-size: 20px;
  cursor: pointer;
  z-index: 100;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  display: flex;
  align-items: center;
  justify-content: center;
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  color: #8c7853;
  font-weight: bold;
  backdrop-filter: blur(10px);
}

.nav-button:hover {
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 8px 25px rgba(140, 120, 83, 0.3);
  border-color: #8c7853;
  transform: translateY(-50%) scale(1.15);
  color: #6e5773;
}

.nav-button:active {
  transform: translateY(-50%) scale(1.05);
}

.prev-button { 
  left: 30px; 
}

.next-button { 
  right: 30px; 
}

/* 指示器 */
.carousel-indicators {
  display: flex;
  justify-content: center;
  margin: 2rem 0 0;
  gap: 0.8rem;
  flex-wrap: wrap;
  padding: 0 1rem;
}

.indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: rgba(140, 120, 83, 0.3);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  border: 2px solid transparent;
  position: relative;
  overflow: hidden;
}

.indicator::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  background: radial-gradient(circle, #8c7853, #6e5773);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: all 0.3s ease;
}

.indicator:hover {
  background: rgba(140, 120, 83, 0.5);
  transform: scale(1.3);
  border-color: rgba(140, 120, 83, 0.4);
  box-shadow: 0 2px 8px rgba(140, 120, 83, 0.3);
}

.indicator:hover::before {
  width: 100%;
  height: 100%;
}

.indicator.active {
  background: #8c7853;
  transform: scale(1.4);
  border-color: #6e5773;
  box-shadow: 0 3px 12px rgba(140, 120, 83, 0.4);
}

.indicator.active::before {
  width: 100%;
  height: 100%;
  background: linear-gradient(45deg, #6e5773, #8c7853);
}

.indicator.active:hover {
  transform: scale(1.6);
  box-shadow: 0 4px 16px rgba(140, 120, 83, 0.5);
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .carousel {
    max-width: 1000px;
  }
  
  .card-inner {
    width: 480px;
    height: 580px;
  }
  
  .poem-card.prev {
    transform: translateX(-240px) translateZ(-100px) scale(0.8) rotateY(10deg);
  }
  
  .poem-card.next {
    transform: translateX(240px) translateZ(-100px) scale(0.8) rotateY(-10deg);
  }
}

@media (max-width: 768px) {
  .poetry-header h1 {
    font-size: 2rem;
  }
  
  .poetry-header .subtitle {
    font-size: 0.9rem;
  }
  
  .poetry-container {
    padding: 1rem;
  }
  
  .carousel {
    height: 550px;
  }
  
  .card-inner {
    width: 320px;
    height: 500px;
  }
  
  .poem-card.prev {
    transform: translateX(-160px) translateZ(-50px) scale(0.75);
  }
  
  .poem-card.next {
    transform: translateX(160px) translateZ(-50px) scale(0.75);
  }
  
  .card-header {
    padding: 1.5rem 1.5rem 1rem;
  }
  
  .card-header h3 {
    font-size: 1.2rem;
  }
  
  .card-body {
    padding: 1rem 1.5rem 1.5rem;
  }
  
  .poem-line {
    font-size: 1rem;
    margin: 0.3rem 0;
  }
  
  .nav-button {
    width: 40px;
    height: 40px;
    font-size: 16px;
  }
  
  .prev-button { 
    left: 15px; 
  }
  
  .next-button { 
    right: 15px; 
  }
}

@media (max-width: 480px) {
  .card-inner {
    width: 280px;
    height: 450px;
  }
  
  .poem-card.prev,
  .poem-card.next {
    opacity: 0.3;
    pointer-events: none;
  }
  
  .poem-card.prev {
    transform: translateX(-140px) scale(0.7);
  }
  
  .poem-card.next {
    transform: translateX(140px) scale(0.7);
  }
  
  .card-header {
    padding: 1rem;
  }
  
  .card-header h3 {
    font-size: 1.1rem;
  }
  
  .author {
    font-size: 1rem;
  }
  
  .card-body {
    padding: 1rem;
  }
  
  .poem-line {
    font-size: 0.95rem;
  }
  
  .nav-button {
    width: 35px;
    height: 35px;
    font-size: 14px;
  }
  
  .prev-button { 
    left: 10px; 
  }
  
  .next-button { 
    right: 10px; 
  }
}

/* 平滑滚动 */
* {
  scroll-behavior: smooth;
}

/* 选择文本样式 */
::selection {
  background: rgba(140, 120, 83, 0.2);
  color: #2d1810;
}

/* 自定义滚动条 */
.card-body::-webkit-scrollbar {
  width: 6px;
}

.card-body::-webkit-scrollbar-track {
  background: rgba(140, 120, 83, 0.1);
  border-radius: 3px;
}

.card-body::-webkit-scrollbar-thumb {
  background: rgba(140, 120, 83, 0.3);
  border-radius: 3px;
}

.card-body::-webkit-scrollbar-thumb:hover {
  background: rgba(140, 120, 83, 0.5);
}
</style>