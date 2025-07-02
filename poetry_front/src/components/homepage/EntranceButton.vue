<template>
  <div class="entrance-button-container" v-show="show">
    <div class="button-wrapper" ref="buttonRef">
      <!-- 主按钮 -->
      <button 
        @click="handleClick"
        @mouseenter="handleMouseEnter"
        @mouseleave="handleMouseLeave"
        class="entrance-btn"
        ref="btnRef"
      >
        <span class="btn-text">进入诗境</span>
        <span class="btn-icon">→</span>
        
        <!-- 按钮光晕效果 -->
        <div class="btn-glow" ref="glowRef"></div>
        
        <!-- 水墨扩散效果 -->
        <div class="ink-ripple" ref="rippleRef"></div>
      </button>
      
      <!-- 装饰性文字 -->
      <div class="decorative-text" ref="decorativeRef">
        <span class="text-line">点击进入</span>
        <span class="text-line">诗意世界</span>
      </div>
      
      <!-- 呼吸圆环 -->
      <div class="breath-ring" ref="ringRef"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { gsap } from 'gsap'

// Props
const props = defineProps({
  show: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['enter-clicked'])

// DOM引用
const buttonRef = ref(null)
const btnRef = ref(null)
const glowRef = ref(null)
const rippleRef = ref(null)
const decorativeRef = ref(null)
const ringRef = ref(null)

// 按钮入场动画
const showAnimation = () => {
  if (!buttonRef.value) return
  
  gsap.fromTo(buttonRef.value,
    { 
      scale: 0,
      rotation: -180,
      opacity: 0,
      filter: 'blur(10px)'
    },
    { 
      scale: 1,
      rotation: 0,
      opacity: 1,
      filter: 'blur(0px)',
      duration: 1.5,
      ease: "back.out(1.7)"
    }
  )
  
  // 装饰文字动画
  gsap.fromTo(decorativeRef.value.children,
    { 
      opacity: 0,
      x: -30
    },
    { 
      opacity: 1,
      x: 0,
      duration: 1,
      stagger: 0.2,
      delay: 0.5,
      ease: "power2.out"
    }
  )
  
  // 呼吸圆环动画
  gsap.fromTo(ringRef.value,
    { scale: 0.5, opacity: 0 },
    { 
      scale: 1, 
      opacity: 1, 
      duration: 1,
      delay: 0.8,
      ease: "power2.out"
    }
  )
}

// 鼠标悬浮效果
const handleMouseEnter = () => {
  // 按钮放大
  gsap.to(btnRef.value, {
    scale: 1.05,
    duration: 0.3,
    ease: "power2.out"
  })
  
  // 光晕效果
  gsap.to(glowRef.value, {
    opacity: 1,
    scale: 1.2,
    duration: 0.3,
    ease: "power2.out"
  })
  
  // 装饰文字高亮
  gsap.to(decorativeRef.value, {
    color: '#8c7853',
    duration: 0.3,
    ease: "power2.out"
  })
}

// 鼠标离开效果
const handleMouseLeave = () => {
  // 按钮恢复
  gsap.to(btnRef.value, {
    scale: 1,
    duration: 0.3,
    ease: "power2.out"
  })
  
  // 隐藏光晕
  gsap.to(glowRef.value, {
    opacity: 0,
    scale: 1,
    duration: 0.3,
    ease: "power2.out"
  })
  
  // 装饰文字恢复
  gsap.to(decorativeRef.value, {
    color: '#bdc3c7',
    duration: 0.3,
    ease: "power2.out"
  })
}

// 点击处理
const handleClick = () => {
  // 水墨扩散效果
  gsap.set(rippleRef.value, {
    scale: 0,
    opacity: 1
  })
  
  gsap.to(rippleRef.value, {
    scale: 3,
    opacity: 0,
    duration: 0.8,
    ease: "power2.out"
  })
  
  // 按钮反馈
  gsap.to(btnRef.value, {
    scale: 0.95,
    duration: 0.1,
    yoyo: true,
    repeat: 1,
    ease: "power2.out",
    onComplete: () => {
      // 延迟发送点击事件，让动画完成
      setTimeout(() => {
        emit('enter-clicked')
      }, 300)
    }
  })
}

// 监听显示状态
watch(() => props.show, (newVal) => {
  if (newVal) {
    nextTick(() => {
      showAnimation()
    })
  }
})
</script>

<style lang="scss" scoped>
.entrance-button-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-top: 3rem;
  position: relative;
  z-index: 10;
}

.button-wrapper {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.entrance-btn {
  position: relative;
  padding: 1rem 2.5rem;
  border: none;
  border-radius: 50px;
  background: linear-gradient(135deg, #8c7853 0%, #6e5773 100%);
  color: white;
  font-size: 1.2rem;
  font-weight: 500;
  cursor: pointer;
  overflow: hidden;
  display: flex;
  align-items: center;
  gap: 0.8rem;
  box-shadow: 0 8px 25px rgba(140, 120, 83, 0.3);
  transition: all 0.3s ease;
  font-family: 'KaiTi', '楷体', serif;
  letter-spacing: 0.1em;
  
  &:hover {
    box-shadow: 0 12px 35px rgba(140, 120, 83, 0.4);
  }
  
  &:active {
    transform: translateY(1px);
  }
}

.btn-text {
  position: relative;
  z-index: 2;
}

.btn-icon {
  position: relative;
  z-index: 2;
  font-size: 1.5rem;
  transition: transform 0.3s ease;
  
  .entrance-btn:hover & {
    transform: translateX(5px);
  }
}

.btn-glow {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(
    circle,
    rgba(255, 255, 255, 0.3) 0%,
    transparent 70%
  );
  opacity: 0;
  border-radius: 50%;
  z-index: 1;
}

.ink-ripple {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 20px;
  height: 20px;
  background: radial-gradient(
    circle,
    rgba(44, 62, 80, 0.4) 0%,
    transparent 70%
  );
  border-radius: 50%;
  transform: translate(-50%, -50%);
  opacity: 0;
  z-index: 0;
}

.decorative-text {
  margin-top: 1.5rem;
  text-align: center;
  color: #bdc3c7;
  font-size: 0.9rem;
  font-family: 'KaiTi', '楷体', serif;
  letter-spacing: 0.2em;
  transition: color 0.3s ease;
}

.text-line {
  display: block;
  margin: 0.2rem 0;
}

.breath-ring {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 120px;
  height: 120px;
  border: 2px solid rgba(140, 120, 83, 0.3);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  animation: breath-pulse 3s ease-in-out infinite;
  z-index: -1;
}

@keyframes breath-pulse {
  0%, 100% {
    scale: 1;
    opacity: 0.3;
  }
  50% {
    scale: 1.2;
    opacity: 0.1;
  }
}

// 响应式设计
@media (max-width: 768px) {
  .entrance-btn {
    padding: 0.8rem 2rem;
    font-size: 1rem;
  }
  
  .decorative-text {
    font-size: 0.8rem;
  }
  
  .breath-ring {
    width: 100px;
    height: 100px;
  }
}
</style>