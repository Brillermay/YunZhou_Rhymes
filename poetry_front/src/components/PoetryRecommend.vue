<template>
  <div class="poetry-layout">
    <!-- 标题部分不变 -->
    <header class="poetry-header">
      <h1>瑶池集萃</h1>
      <p class="subtitle">"翻阅古韵今朝读，妙句良篇入眼来"</p>
    </header>
    
    <main class="poetry-container">
      <div class="carousel">
        <!-- 每个卡片项 -->
        <div 
          v-for="(poem, index) in allPoems" 
          :key="poem.id || index"
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
              <h3>{{ poem?.dynasty || '未知' }} · {{ poem?.title || '未知' }}</h3>
              <p class="author">{{ poem?.author || '佚名' }}</p>
            </div>
            <div class="card-body">
              <div class="poem-content">
                <template v-if="poem?.content && poem.content.length">
                  <div v-for="(line, i) in poem.content" :key="i" class="poem-line">{{ line }}</div>
                </template>
                <template v-else>
                  <div class="poem-placeholder">敬请期待</div>
                </template>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 导航按钮 -->
        <button class="nav-button prev-button" @click="moveCards('prev')">&#10094;</button>
        <button class="nav-button next-button" @click="moveCards('next')">&#10095;</button>
      </div>
      
      <!-- 指示器 -->
      <div class="carousel-indicators">
        <span
          v-for="(_, index) in allPoems.length"
          :key="index"
          :class="['indicator', { active: index === currentIndex }]"
          @click="goToPoem(index)"
        ></span>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import samplePoems from "@/components/data/poetry-sample.js";

const MAX_POEMS = 20;
const allPoems = reactive([]);
const currentIndex = ref(0);

const populatePoems = () => {
  allPoems.splice(0, allPoems.length);
  if (samplePoems && Array.isArray(samplePoems)) {
    // 为已有诗词添加背景图
    const poemsWithBg = samplePoems.map(poem => ({
      ...poem,
      backgroundImage: poem.backgroundImage || '/poetry_bg_1.jpg'
    }));
    allPoems.push(...poemsWithBg);
  }
  
  const remaining = MAX_POEMS - allPoems.length;
  for (let i = 0; i < remaining; i++) {
    allPoems.push({
      id: `placeholder-${i}`,
      title: `敬请期待 ${i + 1}`,
      dynasty: '未来',
      author: '诗词爱好者',
      content: [],
      backgroundImage: '/poetry_bg_1.jpg'  // 默认背景图
    });
  }
};

// 修改 getCardBackgroundStyle 函数
const getCardBackgroundStyle = (poem) => {
  // 确保使用正确的路径
  const imagePath = poem.backgroundImage || '/poetry_bg_1.jpg';
  // 检查路径是否存在并添加调试信息
  console.log('使用图片路径:', imagePath);
  
  return {
    backgroundImage: `url(${imagePath})`,
    backgroundSize: 'cover',
    backgroundPosition: 'center',
    backgroundColor: '#f5efe6', // 备用背景色
    backgroundBlendMode: 'overlay'
  };
};

// 计算循环距离
const getCircularDistance = (index1, index2, length) => {
  const direct = index1 - index2;
  const wrap = direct > 0 ? direct - length : direct + length;
  return Math.abs(direct) < Math.abs(wrap) ? direct : wrap;
};

const moveCards = (direction) => {
  if (direction === 'next') {
    currentIndex.value = (currentIndex.value + 1) % allPoems.length;
  } else {
    currentIndex.value = (currentIndex.value - 1 + allPoems.length) % allPoems.length;
  }
};

const goToPoem = (index) => {
  currentIndex.value = index;
};

onMounted(() => {
  populatePoems();
});
</script>

<style scoped>
.poetry-layout {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5efe6;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  overflow: hidden;
}

.poetry-header {
  text-align: center;
  padding: 1.2rem 0 0.5rem 0;
  background: linear-gradient(to right, #8c7853, #6e5773);
  color: white;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
  width: 100%;
}

.poetry-header h1 {
  margin: 0;
  font-size: 2rem;
  font-weight: normal;
  color: #e5e5e5;
  text-shadow: 3px 3px 10px rgba(0, 0, 0, 0.5);
  animation: float 3s ease-in-out infinite;
}

.poetry-header .subtitle {
  margin: 0.5rem 0 0;
  font-size: 0.9rem;
  font-style: italic;
  opacity: 0.9;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0% { transform: translateY(0); }
  50% { transform: translateY(-4px); }
  100% { transform: translateY(0); }
}

/* 新的内容容器 */
.poetry-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 100%;
  padding: 20px 0;
}

/* 轮播区域 */
.carousel {
  position: relative;
  width: 100%;
  max-width: 1400px;
  height: 700px;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 诗词卡片 */
.poem-card {
  position: absolute;
  transition: all 0.5s cubic-bezier(.4,2,.6,1);
  will-change: transform, opacity;
}

.poem-card.active {
  transform: translateX(0) scale(1);
  z-index: 5;
  opacity: 1;
}

.poem-card.prev {
  transform: translateX(-260px) scale(0.85);
  z-index: 4;
  opacity: 0.8;
}

.poem-card.next {
  transform: translateX(260px) scale(0.85);
  z-index: 4;
  opacity: 0.8;
}

.poem-card.hidden {
  opacity: 0;
  transform: translateX(0) scale(0.7);
  pointer-events: none;
  z-index: 1;
}

.card-inner {
  border-radius: 24px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  width: 520px;
  height: 700px;
  transition: all 0.3s;
  position: relative;
}

/* 半透明覆盖层，确保文字可读性 */
.card-inner::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.85);
  z-index: 1;
  pointer-events: none;
}
/* 内容在覆盖层之上 */
.card-header, .card-body {
  position: relative;
  z-index: 2;
}
.card-header {
  padding: 1.5rem;
  border-bottom: 1px dashed #d6cab4;
  text-align: center;
}

.card-header h3 {
  margin: 0;
  font-size: 1.3rem;
  color: #8c7853;
  font-family: '宋体', serif;
}

.author {
  margin-top: 0.5rem;
  font-size: 1rem;
  color: #5a4634;
  font-style: italic;
}

.card-body {
  flex: 1;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.poem-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.poem-line {
  text-align: center;
  margin: 0.5rem 0;
  font-size: 0.95rem;
  line-height: 1.6;
  color: #3e2723;
  font-family: '楷体', cursive;
}

.poem-placeholder {
  text-align: center;
  color: #8c7853;
  opacity: 0.7;
  font-style: italic;
  font-size: 1.1rem;
}

.nav-button {
  background: rgba(255, 255, 255, 0.7);
  border: none;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  font-size: 18px;
  cursor: pointer;
  z-index: 100;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
}

.nav-button:hover {
  background: rgba(255, 255, 255, 0.9);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.prev-button { left: 20px; }
.next-button { right: 20px; }

.carousel-indicators {
  display: flex;
  justify-content: center;
  margin: 1rem 0 0;
  gap: 0.5rem;
  flex-wrap: wrap;
  padding: 0 1rem;
}

.indicator {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: #d6cab4;
  cursor: pointer;
  transition: all 0.3s ease;
}

.indicator.active {
  background: #8c7853;
  transform: scale(1.2);
}
</style>