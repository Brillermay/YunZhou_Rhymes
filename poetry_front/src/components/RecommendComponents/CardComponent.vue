<template>
  <div class="poetry-card" :style="cardStyle">
    <!-- 标题插槽（带默认值） -->
    <slot name="header">
      <h3 class="card-title">{{ defaultTitle }}</h3>
    </slot>

    <!-- 内容渲染区 -->
    <div class="card-body">
      <slot :poem="poemData"></slot>
    </div>

    <!-- 操作区插槽 -->
    <slot name="footer" v-if="$slots.footer"  />
  </div>
</template>

<script setup>
import { computed } from 'vue'
const props = defineProps({
  poemData: {
    type: Object,
    default: () => ({
      title: '未命名诗篇',
      author: '佚名',
      content: []
    })
  },
  elevation: {
    type: Number,
    default: 2
  }
})

const defaultTitle = computed(() =>
    `${props.poemData.title}  · ${props.poemData.author}`
)

const cardStyle = computed(() => ({
  '--shadow-intensity': props.elevation  * 0.1,
  'box-shadow': `0 4px ${props.elevation  * 8}px rgba(0,0,0,${0.1 + props.elevation  * 0.02})`
}))
</script>

<style scoped>
.poetry-card {
  background: #f8f5f2;
  border-radius: 12px;
  padding: 24px;
  transition: all 0.3s ease;
  margin: 16px;
}



.card-title {
  font-family: '宋体', serif;
  color: #3e2723;
  border-bottom: 1px dashed #d7ccc8;
  padding-bottom: 8px;
}

.card-body {
  line-height: 2.2;
  font-size: 1.1em;
  margin: 20px 0;
}
</style>