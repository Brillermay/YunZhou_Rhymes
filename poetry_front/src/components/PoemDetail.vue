<template>
  <div class="poem-detail-container">
    <div class="poem-detail-card">
      <button @click="$router.push('/')" class="back-btn">← 返回诗词列表</button>

      <div class="poem-heading">
        <h1>{{ poem.title }}</h1>
        <p class="subtitle">— {{ poem.author }} · {{ poem.dynasty }} —</p>
      </div>

      <div class="poem-scroll">
        <pre class="poem-text">{{ poem.content }}</pre>
      </div>

      <div v-if="poem.analysis" class="analysis-panel">
        <h2 class="analysis-heading">📖 诗词赏析</h2>
        <blockquote class="analysis-quote">
          {{ poem.analysis }}
        </blockquote>
      </div>

      <div class="poem-tags" v-if="poem.tags && poem.tags.length">
        <span class="tag" v-for="tag in poem.tags" :key="tag">{{ tag }}</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'PoemDetail',
  data() {
    return {
      poem: {}
    };
  },
  created() {
    const id = Number(this.$route.params.id);
    const poetryData = [
      {
        id: 1,
        title: '静夜思',
        author: '李白',
        dynasty: '唐代',
        content: '床前明月光，疑是地上霜。\n举头望明月，低头思故乡。',
        analysis: '此诗以月为媒，抒发诗人夜晚独处时对故乡的无限思念，语言简练却情感深沉。',
        tags: ['思乡', '月光', '夜静']
      },
      {
        id: 2,
        title: '春晓',
        author: '孟浩然',
        dynasty: '唐代',
        content: '春眠不觉晓，处处闻啼鸟。\n夜来风雨声，花落知多少。',
        analysis: '全诗描绘了春日清晨的美好意境与风雨无常的感叹，令人回味。',
        tags: ['春天', '自然', '感悟']
      }
    ];
    this.poem = poetryData.find(p => p.id === id) || {};
  }
};
</script>

<style scoped>
.poem-detail-container {
  background: #f5efe6; /* 古黄背景 */
  min-height: 100vh;
  padding: 2rem 1rem;
  display: flex;
  justify-content: center;
  font-family: 'Songti SC', '楷体', serif;
}

.poem-detail-card {
  max-width: 740px;
  width: 100%;
  background: #fffaf2;
  padding: 2rem;
  border-radius: 20px;
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.05);
}

.back-btn {
  font-size: 0.9rem;
  color: #8c7853;
  background: none;
  border: none;
  margin-bottom: 1.5rem;
  cursor: pointer;
}
.back-btn:hover {
  text-decoration: underline;
}

.poem-heading {
  text-align: center;
  margin-bottom: 1.5rem;
}
.poem-heading h1 {
  font-size: 2rem;
  color: #8c7853;
  font-weight: bold;
  margin-bottom: 0.25rem;
}
.subtitle {
  font-size: 0.95rem;
  color: #a68b6d;
  font-style: italic;
}

.poem-scroll {
  background: #fdf8ef;
  padding: 1.5rem;
  border-radius: 12px;
  margin-bottom: 1.5rem;
  box-shadow: inset 0 0 8px rgba(0, 0, 0, 0.04);
}

.poem-text {
  font-size: 1.15rem;
  color: #4a3b2c;
  line-height: 2;
  white-space: pre-line;
  text-align: center;
  font-family: '楷体', cursive;
}

/* 赏析部分 */
.analysis-panel {
  background: #f9f5ec;
  border-left: 4px solid #d6cab4;
  padding: 1rem 1.5rem;
  border-radius: 12px;
  margin-bottom: 1rem;
}

.analysis-heading {
  font-size: 1rem;
  color: #6e5773;
  font-weight: bold;
  margin-bottom: 0.5rem;
}

.analysis-quote {
  font-size: 0.95rem;
  color: #5a4634;
  line-height: 1.8;
  font-style: italic;
  margin: 0;
}

/* 标签 */
.poem-tags {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  margin-top: 1rem;
}
.tag {
  background: #eadfd2;
  color: #5a4634;
  padding: 4px 10px;
  font-size: 0.85rem;
  border-radius: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
}
</style>
