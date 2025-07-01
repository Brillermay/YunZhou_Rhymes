<template>
  <div v-if="show" class="scroll-hint" :class="{ 'fade-out': !show }">
    <div class="hint-icon">{{ currentScreen === 0 ? '⬇️' : '⬆️' }}</div>
    <p class="hint-text">
      {{ currentScreen === 0 ? '向下滚动查看游戏统计' : '向上滚动返回个人资料' }}
    </p>
    <div class="hint-dots">
      <span 
        v-for="n in totalScreens" 
        :key="n"
        class="dot"
        :class="{ active: n - 1 === currentScreen }"
      ></span>
    </div>
  </div>
</template>

<script setup>
defineProps({
  currentScreen: {
    type: Number,
    default: 0
  },
  totalScreens: {
    type: Number,
    default: 2
  },
  show: {
    type: Boolean,
    default: true
  }
})
</script>

<style scoped>
.scroll-hint {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  background: rgba(255, 255, 255, 0.9);
  padding: 1rem;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  z-index: 100;
  animation: bounce 2s infinite;
  text-align: center;
  min-width: 120px;
}

.scroll-hint.fade-out {
  animation: fadeOut 0.5s ease-out forwards;
}

.hint-icon {
  font-size: 1.5rem;
  margin-bottom: 0.5rem;
}

.hint-text {
  font-size: 0.8rem;
  color: #666;
  margin: 0 0 0.8rem 0;
  line-height: 1.3;
}

.hint-dots {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ddd;
  transition: all 0.3s ease;
}

.dot.active {
  background: #8c7853;
  transform: scale(1.2);
}

@keyframes bounce {
  0%, 20%, 50%, 80%, 100% {
    transform: translateY(0);
  }
  40% {
    transform: translateY(-10px);
  }
  60% {
    transform: translateY(-5px);
  }
}

@keyframes fadeOut {
  to {
    opacity: 0;
    transform: translateY(20px);
  }
}

@media (max-width: 768px) {
  .scroll-hint {
    bottom: 1rem;
    right: 1rem;
    padding: 0.8rem;
    min-width: 100px;
  }
  
  .hint-text {
    font-size: 0.7rem;
  }
}
</style>