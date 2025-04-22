<template>
  <!-- 外层：整屏垂直 flex，内部可滚动 -->
  <div :class="{ dark: isDarkMode }"
       class="flex flex-col min-h-screen bg-gradient-to-br from-yellow-50 to-yellow-200
              dark:from-gray-900 dark:to-gray-800 text-gray-800 dark:text-gray-100 transition">
    <!-- 顶部横幅：固定高度 -->
    <header class="w-full bg-yellow-600 dark:bg-gray-900 text-center py-8 shadow-lg">
      <h1 class="text-5xl font-extrabold text-white dark:text-yellow-300">
        📜 诗词搜索
      </h1>
      <p class="mt-2 text-md text-yellow-100 dark:text-gray-400">
        快速检索古典诗词，支持作者、标题或诗句片段
      </p>
    </header>

    <!-- 操作区：三列 grid，搜索 / 字号 / 历史+导出 / 切换 -->
    <section class="w-full max-w-6xl mx-auto px-8 py-6 grid grid-cols-1 md:grid-cols-3 gap-6">
      <!-- 字号调节 -->
      <div class="flex items-center gap-4">
        <button @click="adjustFontSize(-2)"
                class="btn-md">A⁻</button>
        <button @click="adjustFontSize(2)"
                class="btn-md">A⁺</button>
      </div>
      <!-- 搜索框 + 按钮 -->
      <div class="relative flex items-center">
        <span class="absolute left-3 text-2xl text-yellow-500 dark:text-yellow-300">🔍</span>
        <input
          v-model="searchQuery"
          @keyup.enter="searchPoetry"
          type="text"
          :style="{ fontSize: fontSize + 'px' }"
          placeholder="作者 / 标题 / 诗句…"
          class="w-full pl-12 pr-4 py-3 border-2 border-yellow-500 rounded-lg
                 focus:ring-4 focus:ring-yellow-300 dark:border-gray-600 dark:focus:ring-gray-500
                 bg-white dark:bg-gray-800 transition"
        />
        <button @click="searchPoetry"
                class="ml-4 btn-primary">搜索</button>
      </div>
      <!-- 历史 & 导出 & 模式切换 -->
      <div class="flex flex-wrap items-center gap-4">
        <template v-for="h in history" :key="h">
          <button @click="searchFromHistory(h)"
                  class="btn-sm">{{ h }}</button>
        </template>
        <button v-if="favorites.length" @click="exportFavorites"
                class="btn-sm">📥 收藏导出</button>
        <button v-if="favorites.length" @click="clearFavorites"
                class="btn-sm">🗑 清空收藏</button>
        <button @click="toggleDarkMode"
                class="btn-icon">{{ isDarkMode ? '☀️' : '🌙' }}</button>
      </div>
    </section>

    <!-- 主内容区：结果 & 无结果提示，自动撑满剩余高度，可滚动 -->
    <main class="flex-1 overflow-auto w-full max-w-6xl mx-auto px-8 py-6 space-y-6">
      <div v-if="loading"
           class="text-center text-2xl text-yellow-500">正在加载，请稍候…</div>
      <div v-if="searched && !loading && !sortedResults.length"
           class="text-center text-2xl text-gray-500 dark:text-gray-400">🤔 没有找到相关诗词</div>

      <!-- 结果网格 -->
      <section v-if="sortedResults.length"
               class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
        <transition-group name="fade" tag="div" class="contents">
          <div v-for="poem in sortedResults" :key="poem.id"
               class="poem-card group"
               @click="goToDetail(poem.id)">
            <div class="relative">
              <button @click.stop="toggleFavorite(poem.id)"
                      class="absolute top-2 right-2 text-3xl opacity-0 group-hover:opacity-100 transition">
                <span :class="isFavorite(poem.id) ? 'text-red-500' : 'text-gray-300 dark:text-gray-400'">
                  {{ isFavorite(poem.id) ? '❤️' : '🤍' }}
                </span>
              </button>
            </div>
            <h3 class="text-xl font-bold text-yellow-600 dark:text-yellow-300 mb-1">{{ poem.title }}</h3>
            <p class="text-sm text-gray-500 dark:text-gray-400 mb-2">{{ poem.author }} • {{ poem.dynasty }}</p>
            <p class="text-base leading-relaxed overflow-hidden"
               :style="{ fontSize: (fontSize - 2) + 'px', maxHeight: expandedIds.includes(poem.id) ? 'none' : '6em' }">
              {{ poem.content }}
            </p>
            <button v-if="poem.content.length > 80"
                    @click.stop="toggleExpand(poem.id)"
                    class="mt-2 text-sm text-yellow-600 dark:text-yellow-300">
              {{ expandedIds.includes(poem.id) ? '收起 ▲' : '展开 ▼' }}
            </button>
          </div>
        </transition-group>
      </section>
    </main>

    <!-- 页脚 -->
    <footer class="w-full py-4 bg-gray-100 dark:bg-gray-900 text-center text-sm text-gray-600 dark:text-gray-400">
      © {{ new Date().getFullYear() }} 诗词搜索系统 — 仅供学习交流
    </footer>

    <!-- 回到顶部 -->
    <button v-show="scrolled" @click="scrollToTop"
            class="fixed bottom-6 right-6 p-3 bg-yellow-500 text-white rounded-full shadow-lg hover:bg-yellow-600 transition">
      ⬆️
    </button>
  </div>
</template>

<script>
import Fuse from 'fuse.js';

export default {
  name: 'PoetrySearch',
  // … your data, comput name: 'PoetrySearch',
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
      scrolled: false,
      fuse: null,
      poetryData: [
        { id: 1, title: '静夜思',    author: '李白', content: '床前明月光，疑是地上霜。\n举头望明月，低头思故乡。' },
        { id: 2, title: '春晓',      author: '孟浩然', content: '春眠不觉晓，处处闻啼鸟。\n夜来风雨声，花落知多少。' },
        { id: 3, title: '登鹳雀楼',  author: '王之涣', content: '白日依山尽，黄河入海流。\n欲穷千里目，更上一层楼。' },
        { id: 4, title: '望庐山瀑布', author: '李白', content: '日照香炉生紫烟，遥看瀑布挂前川。\n飞流直下三千尺，疑是银河落九天。' }
      ]
    };
  },
  computed: {
    sortedResults() {
      return [...this.results].sort((a, b) =>
        this.isFavorite(a.id) === this.isFavorite(b.id)
          ? 0
          : this.isFavorite(a.id) ? -1 : 1
      );
    }
  },
  created() {
    this.fuse = new Fuse(this.poetryData, {
      keys: ['title', 'author', 'content'],
      threshold: 0.3
    });
  },
  mounted() {
    window.addEventListener('scroll', this.onScroll);
  },
  beforeUnmount() {
    window.removeEventListener('scroll', this.onScroll);
  },
  methods: {
    searchPoetry() {
      this.searched = false;
      this.results = [];
      const q = this.searchQuery.trim();
      if (!q) return alert('请输入关键词');
      this.loading = true;
      // 模糊搜索
      setTimeout(() => {
        this.results = this.fuse.search(q).map(r => r.item);
        // 更新历史
        if (!this.history.includes(q)) {
          this.history.unshift(q);
          this.history = this.history.slice(0, 5);
          localStorage.setItem('history', JSON.stringify(this.history));
        }
        this.loading = false;
        this.searched = true;
      }, 200);
    },
    searchFromHistory(h) {
      this.searchQuery = h;
      this.searchPoetry();
    },
    goToDetail(id) {
this.$router.push(`/poem/${id}`);
    },
    isFavorite(id) {
      return this.favorites.includes(id);
    },
    toggleFavorite(id) {
      if (this.isFavorite(id))
        this.favorites = this.favorites.filter(x => x !== id);
      else
        this.favorites.push(id);
      localStorage.setItem('favorites', JSON.stringify(this.favorites));
    },
    clearFavorites() {
      if (confirm('确认清空所有收藏？')) {
        this.favorites = [];
        localStorage.setItem('favorites', '[]');
      }
    },
    exportFavorites() {
      const favs = this.poetryData.filter(p => this.isFavorite(p.id));
      const md = favs.map(p =>
`## ${p.title} — ${p.author}\n\n${p.content}\n`
      ).join('\n---\n');
      const blob = new Blob([md], { type: 'text/markdown' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url; a.download = 'favorites.md'; a.click();
      URL.revokeObjectURL(url);
    },
    adjustFontSize(delta) {
      this.fontSize = Math.max(14, this.fontSize + delta);
    },
    toggleDarkMode() {
      this.isDarkMode = !this.isDarkMode;
      localStorage.setItem('darkMode', JSON.stringify(this.isDarkMode));
    },
    toggleExpand(id) {
      const i = this.expandedIds.indexOf(id);
      if (i >= 0) this.expandedIds.splice(i, 1);
      else this.expandedIds.push(id);
    },
    onScroll() {
      this.scrolled = window.scrollY > 300;
    },
    scrollToTop() {
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  }
};
</script>
<style scoped>
@import url('https://fonts.googleapis.com/css2?family=ZCOOL+XiaoWei&display=swap');

:root {
  --transition: 0.3s ease;
  --card-bg: #fff;
  --card-bg-dark: #1F2937;
}

/* 全局过渡与字体 */
* {
  font-family: 'ZCOOL XiaoWei', serif;
  transition: all var(--transition);
}

/* 操作按钮 */
.btn-sm {
  padding: 0.25rem 0.5rem;
  background: #E5E7EB;
  color: #1F2937;
  border-radius: 0.375rem;
}
.btn-md {
  padding: 0.5rem 1rem;
  background: #F59E0B;
  color: #fff;
  border-radius: 0.5rem;
}
.btn-primary {
  padding: 0.5rem 1rem;
  background: #D97706;
  color: #fff;
  border-radius: 0.5rem;
}
.btn-icon {
  font-size: 1.5rem;
  padding: 0.25rem;
}

/* 卡片 */
.poem-card {
  background: var(--card-bg);
  padding: 1.5rem;
  border-radius: 0.75rem;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
  cursor: pointer;
}
.dark .poem-card {
  background: var(--card-bg-dark);
}
.poem-card:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: 0 8px 16px rgba(0,0,0,0.2);
}

/* 过渡效果 */
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to   { opacity: 1; transform: translateY(0);  }
}
.fade-enter-active, .fade-leave-active {
  animation: fadeInUp 0.4s ease forwards;
}
</style>
