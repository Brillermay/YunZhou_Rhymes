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

/* 🔧 修改：确保应用完全平铺 */
#app {
  margin: 0;
  padding: 0;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

/* 🔧 修改：空白布局样式 - 完全平铺 */
.content.blank-layout {
  padding: 0;
  margin: 0;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  position: absolute;
  top: 0;
  left: 0;
}

/* 🔧 修改：确保正常布局下也没有边框 */
.content:not(.blank-layout) {
  padding: 0;  /* 改为 0，去掉 20px */
  margin: 0;   /* 确保没有 margin */
  width: 100%; /* 确保宽度 */
  height: 100%; /* 确保高度 */
}

/* 🆕 添加：重置全局样式，避免边框 */
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body, html {
  margin: 0;
  padding: 0;
  width: 100%;
  height: 100%;
}

/* 🆕 添加：确保导航组件没有边距 */
.navigation {
  margin: 0;
  padding: 0;
}
</style>