<template>
  <div id="app">
    <!-- 🔧 修改：根据路由元信息决定是否显示导航 -->
    <Navigation v-if="!isBlankLayout" />
    
    <!-- 内容区域 -->
    <div class="content" :class="{ 'blank-layout': isBlankLayout }">
      <router-view></router-view>
    </div>
  </div>
</template>


<script>
import Navigation from '@/components/Navigation.vue'
import { computed } from 'vue'
import { useRoute } from 'vue-router'

export default {
  name: 'home',
  components: {
    Navigation
  },
  setup() {
    const route = useRoute()
    
    // 计算是否为空白布局（不显示导航）
    const isBlankLayout = computed(() => {
      return route.meta?.layout === 'blank'
    })
    
    return {
      isBlankLayout
    }
  }
}
</script>

<style>
@import '../assets/style.css';

/* 🆕 添加：空白布局样式 */
.content.blank-layout {
  padding: 0;
  margin: 0;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

/* 确保正常布局下的样式 */
.content:not(.blank-layout) {
  padding: 20px;
  margin-top: 0;
}
</style>