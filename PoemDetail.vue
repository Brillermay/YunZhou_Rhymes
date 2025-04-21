<template>
  <div class="max-w-3xl mx-auto p-6">
    <button @click="$router.push('/')" class="text-sm text-blue-500 mb-4 hover:underline">← 返回搜索</button>
    <h2 class="text-2xl font-bold mb-2">{{ poem.title }}</h2>
    <p class="text-gray-500 mb-4">作者：{{ poem.author }}</p>
    <div class="bg-white p-4 rounded shadow whitespace-pre-line mb-6">
      {{ poem.content }}
    </div>
    <div v-if="poem.analysis" class="bg-gray-100 p-4 rounded text-gray-700">
      <h3 class="font-semibold mb-2">🔍 赏析</h3>
      <p>{{ poem.analysis }}</p>
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
    // 模拟数据同步或可从 Vuex/API 拿
    const poetryData = [
      {
        id: 1,
        title: '静夜思',
        author: '李白',
        content: '床前明月光，疑是地上霜。\n举头望明月，低头思故乡。',
        analysis: '这是一首表达思乡情感的诗，语言浅显，意境深远。'
      },
      {
        id: 2,
        title: '春晓',
        author: '孟浩然',
        content: '春眠不觉晓，处处闻啼鸟。\n夜来风雨声，花落知多少。',
        analysis: '通过对春日清晨景象的描写，表达对自然和时令变化的敏感感受。'
      }
      // 更多诗词数据...
    ];
    this.poem = poetryData.find(p => p.id === id) || {};
  }
};
</script>
