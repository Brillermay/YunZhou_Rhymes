<template>
  <div class="poetry-layout">
    <!-- 标题部分不变 -->
    <header class="poetry-header">
      <h1>瑶池集萃</h1>
      <p class="subtitle">"翻阅古韵今朝读，妙句良篇入眼来"</p>
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
              <!-- 添加诗词背景和赏析信息 -->
              <div v-if="poem?.background" class="poem-info">
                <h4>背景</h4>
                <p>{{ poem.background }}</p>
              </div>
<!--              <div v-if="poem?.appreciation" class="poem-info">-->
<!--                <h4>赏析</h4>-->
<!--                <p>{{ poem.appreciation }}</p>-->
<!--              </div>-->
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
import { ref, reactive, onMounted } from 'vue';

const MAX_POEMS = 10; // 改为10首
const API_BASE_URL = 'http://localhost:8081/poem'; // 后端API地址
const allPoems = reactive([]);
const currentIndex = ref(0);
const loading = ref(false);
const error = ref('');

// 生成随机不重复的ID数组
const generateRandomIds = (max, count) => {
  const ids = new Set();
  while (ids.size < count) {
    ids.add(Math.floor(Math.random() * max) + 1);
  }
  return Array.from(ids);
};

// 格式化诗词文本（将长文本按句号、逗号等分行）
const formatPoemText = (text) => {
  if (!text) return [];
  // 按照诗词的标点符号分行
  return text.split(/[。！？；]/).filter(line => line.trim()).map(line => line.trim() + (line.includes('，') ? '' : ''));
};

// 从后端API获取诗词
const fetchPoemById = async (id) => {
  try {
    const response = await fetch(`${API_BASE_URL}/${id}`);
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    const data = await response.json();
    return data;
  } catch (err) {
    console.error(`Failed to fetch poem with id ${id}:`, err);
    return null;
  }
};

// 加载诗词数据
const loadPoems = async () => {
  loading.value = true;
  error.value = '';
  allPoems.splice(0, allPoems.length);
  
  try {
    // 生成10个随机不重复的ID（范围1-500）
    const randomIds = generateRandomIds(500, MAX_POEMS);
    
    // 并发请求所有诗词
    const promises = randomIds.map(id => fetchPoemById(id));
    const results = await Promise.all(promises);
    
    // 过滤掉失败的请求，添加成功的诗词
    const validPoems = results.filter(poem => poem !== null);
    
    if (validPoems.length === 0) {
      throw new Error('未能获取到任何诗词数据');
    }
    
    // 为诗词添加背景图
    const poemsWithBg = validPoems.map(poem => ({
      ...poem,
      backgroundImage: '/poetry_bg_1.jpg'
    }));
    
    allPoems.push(...poemsWithBg);
    
    // 如果获取的诗词少于10首，用占位符补充
    const remaining = MAX_POEMS - allPoems.length;
    for (let i = 0; i < remaining; i++) {
      allPoems.push({
        pid: `placeholder-${i}`,
        title: `敬请期待 ${i + 1}`,
        category: '未来',
        poet: '诗词爱好者',
        text: '',
        backgroundImage: '/poetry_bg_1.jpg'
      });
    }
    
    // 重置当前索引
    currentIndex.value = 0;
    
  } catch (err) {
    console.error('Error loading poems:', err);
    error.value = '加载诗词失败，请检查网络连接或稍后重试';
  } finally {
    loading.value = false;
  }
};

// 修改 getCardBackgroundStyle 函数
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
  loadPoems();
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

/* 加载和错误状态样式 */
.loading-container, .error-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 400px;
  color: #8c7853;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #8c7853;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.retry-btn, .refresh-btn {
  background: linear-gradient(135deg, #8c7853, #6e5773);
  color: white;
  border: none;
  padding: 0.8rem 1.5rem;
  border-radius: 25px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
  margin-top: 1rem;
}

.retry-btn:hover, .refresh-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(140, 120, 83, 0.3);
}

.refresh-container {
  margin-top: 1rem;
}

/* 轮播区域 */
.carousel {
  position: relative;
  width: 100%;
  max-width: 1400px;
  height: 600px; /* 稍微减小高度 */
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
  height: 600px; /* 调整卡片高度 */
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
  background: rgba(255, 255, 255, 0.3);
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
  display: flex;
  flex-direction: column;
  justify-content: center;
  margin-bottom: 1rem;
}

.poem-line {
  text-align: center;
  margin: 0.3rem 0;
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

.poem-info {
  margin-top: 0.5rem;
  padding: 0.5rem;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 8px;
}

.poem-info h4 {
  margin: 0 0 0.3rem 0;
  font-size: 0.8rem;
  color: #8c7853;
  font-weight: bold;
}

.poem-info p {
  margin: 0;
  font-size: 0.7rem;
  line-height: 1.4;
  color: #5a4634;
  max-height: 3em;
  overflow: hidden;
  text-overflow: ellipsis;
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