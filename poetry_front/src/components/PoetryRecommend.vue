<template>
  <div class="recommend-view">
    <h2 class="page-title">古典诗词推荐</h2>
    <div class="carousel-container">
      <transition-group name="podium" tag="div" class="cards-wrapper">
        <CardComponent
            v-for="(poem, index) in activeTriad"
            :key="poem.id"
            :poem-data="poem"
            :class="positionClasses[index]"
            :style="cardTransforms[index]"
            @click="handleFocus(index)"
        >
          <!-- 保留原有插槽内容 -->
          <template #header>
            <div class="custom-header">
              <i class="icon-poetry"></i>
              <h3>{{ poem.dynasty  }} · {{ poem.title  }}</h3>
            </div>
          </template>

          <template #default="{ poem }">
            <PoemContent :poem="poem" />
          </template>

          <template #footer>
            <div class="action-bar">
              <button @click="handleLike(poem.id)"> 收藏</button>
              <button @click="showAnalysis(poem)">赏析</button>
            </div>
          </template>
        </CardComponent>
      </transition-group>
      <button class="nav-arrow prev" @click="rotate(-1)">❮</button>
      <button class="nav-arrow next" @click="rotate(1)">❯</button>
    </div>
  </div>
</template>

<script setup>
import {ref, computed, onMounted} from 'vue'
import CardComponent from "@/components/RecommendComponents/CardComponent.vue";
import PoemContent from "@/components/RecommendComponents/PoemContent.vue";
import samplePoems from "@/components/data/poetry-sample.js";
const poetryList = ref(samplePoems)
const currentFocus = ref(1)
const positionClasses = ['left', 'center', 'right']
// 收藏功能实现
const handleLike = (id) => {
  console.log(' 收藏操作:', id)
}
const viewportWidth = ref(window.innerWidth)
onMounted(() => {
  window.addEventListener('resize',  () => {
    viewportWidth.value  = window.innerWidth
  })
})
// 诗词解析展示
const showAnalysis = (poem) => {
  console.log(' 展示赏析:', poem.title)
}
// 计算当前显示的三联卡片
const activeTriad = computed(() => {
  const len = poetryList.value.length
  return [
    poetryList.value[(currentFocus.value  - 1 + len) % len],
    poetryList.value[currentFocus.value],
    poetryList.value[(currentFocus.value  + 1) % len]
  ]
})

// 卡片动态变换参数
const cardTransforms = computed(() => {
  const baseOffset = viewportWidth.value  * 0.3 // 30%视宽偏移
  return [
    { transform: `translateX(-${baseOffset}px) scale(0.9)` },
    { transform: 'translateY(-20px)' },
    { transform: `translateX(${baseOffset}px) scale(0.9)` }
  ]
})

// 轮播控制逻辑
function rotate(step) {
  currentFocus.value  = (currentFocus.value  + step + poetryList.value.length)  % poetryList.value.length
}

// 点击卡片聚焦
function handleFocus(index) {
  if (index === 0) rotate(-1)
  else if (index === 2) rotate(1)
}
</script>

<style scoped>
.carousel-container {
  display: flex;          /* 改为弹性布局 */
  justify-content: center;/* 水平居中 */
  height: 480px;
  margin-top: 30px;
}



.cards-wrapper {
  position: relative;
  height: 100%;
}
.cards-wrapper::before, .cards-wrapper::after {
  box-sizing: inherit;
  position: absolute;
  content: '';
  border: 2px solid transparent;
  width: 0;
  height: 0;
}

.cards-wrapper::after {
  bottom: 0;
  right: 0;
}

.cards-wrapper::before {
  top: 0;
  left: 0;
}

.cards-wrapper:hover::before, .cards-wrapper:hover::after {
  width: 100%;
  height: 100%;
}

.cards-wrapper:hover::before {
  border-top-color: #4361ee;
  border-right-color: #4361ee;
  transition: width 0.3s ease-out, height 0.3s ease-out 0.3s;
}

.cards-wrapper:hover::after {
  border-bottom-color: #4361ee;
  border-left-color: #4361ee;
  transition: border-color 0s ease-out 0.6s, width 0.3s ease-out 0.6s, height 0.3s ease-out 1s;
}


.poetry-carda {
  position: absolute;

  height: 380px;
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 过渡动画 */
.podium-move {
  transition: all 0.6s;
}
.podium-enter-active,
.podium-leave-active {
  transition: all 0.6s;
}
.podium-enter-from {
  opacity: 0;
  transform: translateX(100%) scale(0.8);
}
.podium-leave-to {
  opacity: 0;
  transform: translateX(-100%) scale(0.8);
}

.nav-arrow {
  position: absolute;
  top: 45%;
  transform: translateY(-50%);
  background: rgba(255,255,255,0.8);
  border: none;
  padding: 12px;
  cursor: pointer;
  border-radius: 50%;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}
.prev { left: 5%; }
.next { right: 5%; }
</style>