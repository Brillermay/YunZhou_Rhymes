<template>
  <div class="poetry-container">
    <!-- 顶部横幅 -->
    <header class="poetry-header">
      <h1>📜 诗词搜索</h1>
      <p class="subtitle">案头轻点觅古篇，屏上翰墨入云烟</p>
    </header>

    <!-- 搜索框区域 -->
    <div class="search-panel">
      <div class="search-box">
        <span class="search-icon">🔍</span>
        <input
          v-model="searchQuery"
          @keyup.enter="searchPoetry"
          class="search-input"
          placeholder="请输入作者、标题或诗句片段…"
        />
      </div>
    </div>

    <!-- 操作按钮区域 -->
    <div class="actions-panel">
      <button @click="adjustFontSize(-2)">A⁻</button>
      <button @click="adjustFontSize(2)">A⁺</button>
      <button v-if="favorites.length" @click="exportFavorites">📥 导出收藏</button>
      <button v-if="favorites.length" @click="clearFavorites">🗑 清空收藏</button>
      <button @click="toggleDarkMode">
        {{ isDarkMode ? '☀️ 日间模式' : '🌙 夜间模式' }}
      </button>
    </div>

    <!-- 搜索历史 -->
    <div class="history-panel" v-if="history.length">
      <h3>搜索历史</h3>
      <div class="history-tags">
        <button
          v-for="h in history"
          :key="h"
          @click="searchFromHistory(h)"
        >
          {{ h }}
        </button>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div class="results-panel">
      <!-- 欢迎展示：未搜索时显示精选诗词 -->
      <div v-if="!searched && !loading" class="welcome-poems">
        <h2 class="welcome-title">🌸 精选诗词欣赏 🌸</h2>
        <div class="poem-grid">
          <div v-for="poem in featuredPoems" :key="poem.pid" class="poem-card">
            <button @click.stop="toggleFavorite(poem.pid)" class="fav-btn">
              {{ isFavorite(poem.pid) ? '❤️' : '🤍' }}
            </button>
            <h3 class="poem-title">{{ poem.title }}</h3>
            <p class="meta">{{ poem.poet }} • {{ poem.category }}</p>
            <p class="poem-content" :style="{ fontSize: fontSize + 'px' }">
              {{ formatPoemText(poem.text) }}
            </p>
            <div v-if="poem.appreciation" class="brief-analysis">📝 {{ poem.appreciation }}</div>
          </div>
        </div>
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="status-text">正在加载，请稍候…</div>

      <!-- 无搜索结果 -->
      <div v-if="searched && !loading && !sortedResults.length" class="status-text">
        🤔 没有找到相关诗词，尝试其他关键词吧
      </div>

      <!-- 搜索结果展示 -->
      <div v-if="sortedResults.length" class="poem-grid">
        <div
          v-for="poem in sortedResults"
          :key="poem.pid"
          class="poem-card"
          @click="goToDetail(poem.pid)"
        >
          <button @click.stop="toggleFavorite(poem.pid)" class="fav-btn">
            {{ isFavorite(poem.pid) ? '❤️' : '🤍' }}
          </button>
          <h3>{{ poem.title }}</h3>
          <p class="meta">{{ poem.poet }} • {{ poem.category }}</p>
          <p :style="{ fontSize: fontSize + 'px' }">{{ formatPoemText(poem.text) }}</p>
          <div v-if="poem.background" class="brief-analysis">📝 {{ poem.background }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PoetrySearch',
  data() {
    return {
      searchQuery: '',
      results: [],
      searched: false,
      loading: false,
      favorites: JSON.parse(localStorage.getItem('favorites') || '[]'),
      history: JSON.parse(localStorage.getItem('history') || '[]'),
      fontSize: 18,
      isDarkMode: JSON.parse(localStorage.getItem('darkMode') || 'false'),
      expandedIds: [],
      sidebarOpen: true,
      poetryData: [], // 存储搜索结果
      featuredPoems: [], // 精选诗词
      API_BASE_URL: 'http://localhost:8081/poem'
    };
  },
  
  computed: {
    sortedResults() {
      return this.results;
    }
  },

  async mounted() {
    // 页面加载时获取精选诗词
    await this.loadFeaturedPoems();
  },

  methods: {
    // 格式化诗词文本
    formatPoemText(text) {
      if (!text) return '';
      // 如果文本太长，可以添加换行
      return text.replace(/[。！？；]/g, '$&\n');
    },

    // 加载精选诗词（页面初始显示）
    async loadFeaturedPoems() {
      try {
        // 获取几首经典诗词作为精选展示，这里使用固定ID 1
        const response = await fetch(`${this.API_BASE_URL}/1`);
        if (response.ok) {
          const poem = await response.json();
          // 创建几个变体作为精选展示
          this.featuredPoems = [
            { ...poem, pid: `featured-1` },
          ];
        }
      } catch (error) {
        console.error('加载精选诗词失败:', error);
        // 如果API失败，可以设置一些默认的精选诗词
        this.featuredPoems = [];
      }
    },

    // 调整字体大小
    adjustFontSize(delta) {
      this.fontSize = Math.max(12, Math.min(24, this.fontSize + delta));
    },

    // 搜索诗词
    async searchPoetry() {
      this.searched = false;
      this.results = [];
      const q = this.searchQuery.trim();
      
      if (!q) {
        alert('请输入关键词');
        return;
      }
      
      this.loading = true;
      
      try {
        console.log(`正在搜索关键词: ${q}`); // 调试日志
        
        // 调用后端API搜索
        const response = await fetch(`${this.API_BASE_URL}/keyword/${encodeURIComponent(q)}`);
        
        console.log(`搜索响应状态: ${response.status}`); // 调试日志
        
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        console.log('搜索结果:', data); // 调试日志
        
        // 检查返回数据格式
        if (Array.isArray(data)) {
          this.results = data;
          this.poetryData = data; // 保存全部数据，方便导出收藏
        } else {
          console.error('返回数据格式不正确:', data);
          this.results = [];
        }
        
        // 记录搜索历史
        if (!this.history.includes(q)) {
          this.history.unshift(q);
          this.history = this.history.slice(0, 5);
          localStorage.setItem('history', JSON.stringify(this.history));
        }
        
      } catch (error) {
        console.error('搜索失败:', error);
        alert('搜索失败，请检查网络连接或后端服务是否启动');
        this.results = [];
      } finally {
        this.loading = false;
        this.searched = true;
      }
    },

    // 从历史记录搜索
    searchFromHistory(h) {
      this.searchQuery = h;
      this.searchPoetry();
    },

    // 跳转到详情页
    goToDetail(id) {
      // 这里可以根据需要实现详情页跳转
      console.log('查看诗词详情:', id);
      // this.$router.push(`/poem/${id}`);
    },

    // 检查是否收藏
  isFavorite(id) {
  return this.favorites.some(p => p.pid === id);
},


    // 切换收藏状态
    toggleFavorite(id) {
  const allPoems = [...this.featuredPoems, ...this.results];
  const poem = allPoems.find(p => p.pid === id);
  if (!poem) return;

  if (this.isFavorite(id)) {
    this.favorites = this.favorites.filter(p => p.pid !== id);
  } else {
    this.favorites.push(poem);
  }

  localStorage.setItem('favorites', JSON.stringify(this.favorites));
}
,

    // 清空收藏
    clearFavorites() {
      if (confirm('确认清空所有收藏？')) {
        this.favorites = [];
        localStorage.setItem('favorites', '[]');
      }
    },

    // 导出收藏
   exportFavorites() {
  if (!this.favorites.length) {
    alert('收藏列表为空，无法导出。');
    return;
  }

  const md = this.favorites.map(p =>
    `## ${p.title} — ${p.poet}\n\n${this.formatPoemText(p.text)}\n`
  ).join('\n---\n');

  const blob = new Blob([md], { type: 'text/markdown' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'favorites.md';
  a.click();
  URL.revokeObjectURL(url);
}

  }
};
</script>

<style scoped>
.poetry-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f5efe6;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  overflow: hidden;
}

.poetry-header {
  text-align: center;
  padding: 1.2rem;
  background: linear-gradient(to right, #8c7853, #6e5773);
  color: white;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.poetry-header h1 {
  margin: 0;
  font-size: 2rem;
}

.poetry-header .subtitle {
  margin: 0.5rem 0 0;
  font-size: 0.9rem;
  font-style: italic;
  opacity: 0.9;
}

.search-panel {
  display: flex;
  justify-content: center;
  margin-top: 1.5rem;
}

.search-box {
  position: relative;
  width: 90%;
  max-width: 600px;
}

.search-icon {
  position: absolute;
  left: 1rem;
  top: 50%;
  transform: translateY(-50%);
  font-size: 1.2rem;
  color: #8c7853;
}

.search-input {
  width: 100%;
  padding: 12px 16px 12px 40px;
  font-size: 1rem;
  border: 1px solid #d6cab4;
  border-radius: 24px;
  background: #f8f4ed;
  outline: none;
  transition: all 0.3s;
}

.search-input:focus {
  border-color: #8c7853;
  box-shadow: 0 0 0 2px rgba(140, 120, 83, 0.2);
}

.actions-panel {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin: 1rem 0;
  flex-wrap: wrap;
}

.actions-panel button {
  padding: 8px 16px;
  border: none;
  border-radius: 20px;
  background: linear-gradient(to right, #8c7853, #6e5773);
  color: white;
  cursor: pointer;
  font-size: 0.9rem;
  box-shadow: 0 4px 8px rgba(0,0,0,0.08);
  transition: all 0.2s;
}

.actions-panel button:hover {
  transform: translateY(-2px);
}

.history-panel {
  text-align: center;
  margin-bottom: 1rem;
}

.history-panel h3 {
  margin: 0.5rem 0;
  color: #5a4634;
}

.history-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.history-tags button {
  padding: 6px 12px;
  font-size: 0.85rem;
  background: #f8f4ed;
  color: #5a4634;
  border: 1px solid #d6cab4;
  border-radius: 16px;
  cursor: pointer;
}

.results-panel {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
}

.status-text {
  text-align: center;
  font-size: 1.2rem;
  color: #8c7853;
  padding: 2rem 0;
}

.welcome-poems {
  background: #f9f4ed;
  padding: 2rem 1rem;
  border-top: 2px solid #e1d8c9;
  border-bottom: 2px solid #e1d8c9;
  margin-bottom: 2rem;
}

.welcome-title {
  text-align: center;
  font-size: 1.6rem;
  color: #8c7853;
  margin-bottom: 1.5rem;
  font-weight: bold;
}

.poem-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 1.5rem;
}

.poem-card {
  position: relative;
  background: #fffaf2;
  border-radius: 16px;
  padding: 1.25rem 1rem;
  box-shadow: 0 6px 12px rgba(0,0,0,0.05);
  transition: transform 0.3s ease;
  cursor: pointer;
}

.poem-card:hover {
  transform: translateY(-4px);
}

.poem-title {
  color: #8c7853;
  font-size: 1.1rem;
  margin-bottom: 0.25rem;
}

.poem-card h3 {
  font-size: 1.1rem;
  color: #8c7853;
  margin: 0 0 0.5rem;
}

.meta {
  color: #a68b6d;
  font-size: 0.85rem;
  margin-bottom: 0.75rem;
}

.poem-content {
  color: #4a3b2c;
  line-height: 1.8;
  white-space: pre-line;
  margin-bottom: 0.75rem;
}

.brief-analysis {
  font-size: 0.85rem;
  color: #5a4634;
  background: #f5efe6;
  padding: 6px 10px;
  border-radius: 12px;
  text-align: center;
  font-style: italic;
}

.fav-btn {
  position: absolute;
  top: 10px;
  right: 12px;
  background: none;
  border: none;
  font-size: 1.3rem;
  cursor: pointer;
}
</style>
