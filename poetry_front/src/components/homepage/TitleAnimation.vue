<template>
  <div class="title-animation-container" ref="containerRef">
    <div class="title-wrapper">
      <!-- 主标题：云舟词渡 -->
      <div class="main-title" ref="titleRef">
        <span 
          v-for="(char, index) in titleChars" 
          :key="index"
          class="title-char"
          :ref="el => titleCharRefs[index] = el"
          :data-char="char"
        >
          {{ char }}
        </span>
      </div>
      
      <!-- 英文副标题 -->
      <div class="english-title" ref="englishRef">
        <span 
          class="english-word" 
          v-for="(word, index) in englishWords" 
          :key="index"
          :ref="el => englishWordRefs[index] = el"
        >
          {{ word }}
        </span>
      </div>
      
      <!-- 毛笔效果 -->
      <div class="brush-effect" ref="brushRef">
        <div class="brush-stroke" ref="brushStrokeRef"></div>
      </div>
      
      <!-- 装饰性粒子效果 -->
      <div class="title-particles" ref="particlesRef">
        <div 
          v-for="(particle, index) in titleParticles" 
          :key="index"
          class="title-particle"
          :ref="el => particleRefs[index] = el"
          :style="particle.style"
        ></div>
      </div>
      
      <!-- 光晕效果 -->
      <div class="title-glow" ref="glowRef"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, watch, onMounted, nextTick } from 'vue'
import { gsap } from 'gsap'
import { TextPlugin } from 'gsap/TextPlugin'

gsap.registerPlugin(TextPlugin)

// Props
const props = defineProps({
  startAnimation: {
    type: Boolean,
    default: false
  },
  exitAnimation: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['animation-complete', 'exit-complete'])

// 响应式数据
const titleChars = ['云', '舟', '词', '渡']
const englishWords = ['Poetry', 'Boat', 'Journey']
const titleCharRefs = ref([])
const englishWordRefs = ref([])
const particleRefs = ref([])

// DOM引用
const containerRef = ref(null)
const titleRef = ref(null)
const englishRef = ref(null)
const brushRef = ref(null)
const brushStrokeRef = ref(null)
const particlesRef = ref(null)
const glowRef = ref(null)

// 动画状态
const isAnimating = ref(false)
const isExiting = ref(false)
const animationCompleted = ref(false)

// 装饰性粒子
const titleParticles = ref([])

// 创建装饰性粒子
const createTitleParticles = () => {
  titleParticles.value = []
  for (let i = 0; i < 12; i++) {
    titleParticles.value.push({
      style: {
        left: `${Math.random() * 100}%`,
        top: `${Math.random() * 100}%`,
        animationDelay: `${Math.random() * 3}s`,
        animationDuration: `${3 + Math.random() * 2}s`
      }
    })
  }
}

// 🎭 主入场动画
const executeEntranceAnimation = async () => {
  if (isAnimating.value) return
  
  isAnimating.value = true
  console.log('🎭 开始标题入场动画')
  
  await nextTick()
  
  const tl = gsap.timeline({
    onComplete: () => {
      animationCompleted.value = true
      isAnimating.value = false
      emit('animation-complete')
      console.log('✨ 标题入场动画完成')
    }
  })
  
  // 🔧 初始状态设置
  gsap.set(titleCharRefs.value, {
    opacity: 0,
    y: 120,
    rotation: 45,
    scale: 0.3,
    filter: 'blur(15px)',
    transformOrigin: 'center bottom'
  })
  
  gsap.set(englishWordRefs.value, {
    opacity: 0,
    x: 'random(-100, 100)',
    y: 80,
    scale: 0.8,
    filter: 'blur(8px)'
  })
  
  gsap.set(brushRef.value, {
    opacity: 0,
    scaleX: 0,
    transformOrigin: 'left center'
  })
  
  gsap.set(particleRefs.value, {
    opacity: 0,
    scale: 0
  })
  
  gsap.set(glowRef.value, {
    opacity: 0,
    scale: 0.5
  })
  
  // 🎨 动画序列
  
  // 1. 容器整体淡入
  tl.to(containerRef.value, {
    opacity: 1,
    duration: 0.8,
    ease: "power2.out"
  })
  
  // 2. 毛笔笔画效果 - 从左到右扫过
  .to(brushRef.value, {
    opacity: 1,
    scaleX: 1,
    duration: 1.8,
    ease: "power3.out"
  }, "-=0.3")
  
  .to(brushStrokeRef.value, {
    background: 'linear-gradient(90deg, rgba(140, 120, 83, 0.8) 0%, rgba(44, 62, 80, 0.6) 50%, rgba(140, 120, 83, 0.8) 100%)',
    duration: 0.8,
    ease: "power2.inOut"
  }, "-=1.2")
  
  // 3. 汉字逐个出现 - 墨迹书写效果
  .to(titleCharRefs.value, {
    opacity: 1,
    y: 0,
    rotation: 0,
    scale: 1,
    filter: 'blur(0px)',
    duration: 1.5,
    stagger: {
      amount: 2.5,
      from: "start",
      ease: "back.out(2)"
    },
    ease: "back.out(1.7)"
  }, "-=1")
  
  // 4. 汉字的墨迹扩散效果
  .to(titleCharRefs.value, {
    textShadow: '0 0 20px rgba(44, 62, 80, 0.6), 0 0 40px rgba(140, 120, 83, 0.4)',
    duration: 1,
    stagger: 0.15,
    ease: "power2.out"
  }, "-=1.5")
  
  // 5. 汉字的轻微摇摆 - 模拟毛笔书写后的自然晃动
  .to(titleCharRefs.value, {
    rotation: 'random(-3, 3)',
    y: 'random(-5, 5)',
    duration: 0.8,
    stagger: 0.1,
    ease: "elastic.out(1, 0.3)",
    repeat: 1,
    yoyo: true
  }, "-=0.8")
  
  // 6. 英文标题优雅登场
  .to(englishWordRefs.value, {
    opacity: 1,
    x: 0,
    y: 0,
    scale: 1,
    filter: 'blur(0px)',
    duration: 1.2,
    stagger: {
      amount: 0.8,
      from: "center",
      ease: "power2.out"
    },
    ease: "power3.out"
  }, "-=1.2")
  
  // 7. 装饰粒子绽放
  .to(particleRefs.value, {
    opacity: 0.8,
    scale: 'random(0.8, 1.2)',
    rotation: 'random(0, 360)',
    duration: 1.5,
    stagger: {
      amount: 1,
      from: "random",
      ease: "power2.out"
    },
    ease: "back.out(1.7)"
  }, "-=1.5")
  
  // 8. 背景光晕绽放
  .to(glowRef.value, {
    opacity: 0.6,
    scale: 1.5,
    duration: 2,
    ease: "power2.out"
  }, "-=2")
  
  // 9. 整体标题的生动浮动效果
  .to(titleRef.value, {
    y: 'random(-4, 4)',
    rotation: 'random(-0.5, 0.5)',
    duration: 4,
    ease: "power1.inOut",
    repeat: -1,
    yoyo: true
  }, "-=0.5")
  
  // 10. 英文标题的微妙呼吸效果
  .to(englishRef.value, {
    scale: 'random(0.98, 1.02)',
    opacity: 'random(0.8, 1)',
    duration: 3,
    ease: "power1.inOut",
    repeat: -1,
    yoyo: true
  }, "-=4")
}

// 🌊 退出动画
const executeExitAnimation = async () => {
  if (isExiting.value || !animationCompleted.value) return
  
  isExiting.value = true
  console.log('🌊 开始标题退出动画')
  
  await nextTick()
  
  // 停止所有循环动画
  gsap.killTweensOf([titleRef.value, englishRef.value])
  
  const exitTl = gsap.timeline({
    onComplete: () => {
      isExiting.value = false
      emit('exit-complete')
      console.log('👋 标题退出动画完成')
    }
  })
  
  // 🌊 退出动画序列
  
  // 1. 粒子先消散
  exitTl.to(particleRefs.value, {
    opacity: 0,
    scale: 0,
    y: 'random(-50, -100)',
    rotation: 'random(180, 540)',
    duration: 0.8,
    stagger: {
      amount: 0.6,
      from: "random",
      ease: "power2.in"
    },
    ease: "power2.in"
  })
  
  // 2. 背景光晕收缩消失
  .to(glowRef.value, {
    opacity: 0,
    scale: 0.3,
    filter: 'blur(20px)',
    duration: 1.2,
    ease: "power3.in"
  }, "-=0.6")
  
  // 3. 英文标题向两侧散开消失
  .to(englishWordRefs.value, {
    opacity: 0,
    x: 'random(-200, 200)',
    y: 'random(-80, -120)',
    scale: 0.5,
    rotation: 'random(-45, 45)',
    filter: 'blur(15px)',
    duration: 1,
    stagger: {
      amount: 0.4,
      from: "center",
      ease: "power3.in"
    },
    ease: "power3.in"
  }, "-=0.8")
  
  // 4. 毛笔效果反向消失
  .to(brushStrokeRef.value, {
    scaleX: 0,
    opacity: 0.3,
    transformOrigin: 'right center',
    duration: 1.2,
    ease: "power3.in"
  }, "-=1")
  
  // 5. 汉字的戏剧性退场 - 向上飞散
  .to(titleCharRefs.value, {
    opacity: 0,
    y: 'random(-150, -250)',
    x: 'random(-100, 100)',
    rotation: 'random(-180, 180)',
    scale: 0.2,
    filter: 'blur(25px)',
    duration: 1.5,
    stagger: {
      amount: 1.2,
      from: "end", // 从右到左消失
      ease: "power3.in"
    },
    ease: "power3.in"
  }, "-=0.8")
  
  // 6. 整个容器最终淡出
  .to(containerRef.value, {
    opacity: 0,
    scale: 0.8,
    y: -100,
    filter: 'blur(30px)',
    duration: 1.5,
    ease: "power3.in"
  }, "-=1")
}

// 🎬 监听动画触发
watch(() => props.startAnimation, (newVal) => {
  if (newVal && !isAnimating.value) {
    executeEntranceAnimation()
  }
})

watch(() => props.exitAnimation, (newVal) => {
  if (newVal && !isExiting.value) {
    executeExitAnimation()
  }
})

// 🔧 生命周期
onMounted(() => {
  console.log('🎭 TitleAnimation 组件挂载')
  
  // 创建装饰粒子
  createTitleParticles()
  
  // 预设置DOM引用
  nextTick(() => {
    titleCharRefs.value = titleCharRefs.value.filter(Boolean)
    englishWordRefs.value = englishWordRefs.value.filter(Boolean)
    particleRefs.value = particleRefs.value.filter(Boolean)
    
    // 初始隐藏状态
    gsap.set(containerRef.value, { opacity: 0 })
  })
})
</script>

<style lang="scss" scoped>
.title-animation-container {
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
  z-index: 10;
  width: 100%;
  height: auto;
  opacity: 0; // 初始隐藏
}

.title-wrapper {
  text-align: center;
  position: relative;
  padding: 2rem;
}

// 🎨 主标题样式
.main-title {
  font-family: 'STKaiti', 'KaiTi', '楷体', serif;
  font-size: clamp(3rem, 8vw, 8rem);
  font-weight: 400;
  color: #2c3e50;
  margin-bottom: 1.5rem;
  position: relative;
  letter-spacing: 0.3em;
  line-height: 1.2;
}

.title-char {
  display: inline-block;
  margin: 0 0.1em;
  position: relative;
  cursor: default;
  transition: all 0.4s ease;
  transform-origin: center bottom;
  
  &:hover {
    color: #8c7853;
    transform: scale(1.15) translateY(-8px) rotate(2deg);
    text-shadow: 
      0 8px 25px rgba(140, 120, 83, 0.4),
      0 0 30px rgba(44, 62, 80, 0.3);
    z-index: 10;
  }
  
  // 字符阴影效果
  &::before {
    content: attr(data-char);
    position: absolute;
    top: 3px;
    left: 3px;
    color: rgba(140, 120, 83, 0.15);
    z-index: -1;
    filter: blur(1px);
  }
  
  // 字符轮廓效果
  &::after {
    content: attr(data-char);
    position: absolute;
    top: 0;
    left: 0;
    color: transparent;
    -webkit-text-stroke: 1px rgba(44, 62, 80, 0.1);
    z-index: -2;
  }
}

// 🔤 英文标题样式
.english-title {
  font-family: 'Playfair Display', 'Georgia', serif;
  font-size: clamp(1rem, 2.5vw, 1.8rem);
  color: #7f8c8d;
  font-style: italic;
  font-weight: 300;
  letter-spacing: 0.4em;
  margin-top: 1rem;
  position: relative;
}

.english-word {
  display: inline-block;
  margin: 0 0.4em;
  transition: all 0.3s ease;
  position: relative;
  
  &:hover {
    color: #8c7853;
    transform: scale(1.1) translateY(-2px);
    text-shadow: 0 4px 15px rgba(140, 120, 83, 0.3);
  }
  
  &:not(:last-child)::after {
    content: '·';
    margin-left: 0.8em;
    color: #bdc3c7;
    font-style: normal;
    opacity: 0.6;
  }
}

// 🖌️ 毛笔效果
.brush-effect {
  position: absolute;
  bottom: -15px;
  left: 50%;
  transform: translateX(-50%);
  width: 85%;
  height: 6px;
  overflow: hidden;
  border-radius: 3px;
}

.brush-stroke {
  width: 100%;
  height: 100%;
  background: linear-gradient(
    90deg, 
    transparent 0%, 
    rgba(140, 120, 83, 0.3) 10%,
    rgba(140, 120, 83, 0.8) 30%, 
    rgba(44, 62, 80, 0.6) 50%, 
    rgba(140, 120, 83, 0.8) 70%, 
    rgba(140, 120, 83, 0.3) 90%,
    transparent 100%
  );
  border-radius: 3px;
  position: relative;
  box-shadow: 
    0 2px 10px rgba(44, 62, 80, 0.2),
    inset 0 1px 3px rgba(255, 255, 255, 0.1);
  
  // 流动墨迹效果
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(
      90deg,
      transparent 0%,
      rgba(255, 255, 255, 0.2) 20%,
      rgba(255, 255, 255, 0.4) 50%,
      rgba(255, 255, 255, 0.2) 80%,
      transparent 100%
    );
    animation: ink-flow 4s ease-in-out infinite;
    border-radius: 3px;
  }
  
  // 毛笔纹理
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: 
      repeating-linear-gradient(
        90deg,
        transparent 0px,
        rgba(0, 0, 0, 0.05) 1px,
        transparent 2px
      );
  }
}

// ✨ 装饰性粒子
.title-particles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: -1;
}

.title-particle {
  position: absolute;
  width: 6px;
  height: 6px;
  background: radial-gradient(
    circle,
    rgba(140, 120, 83, 0.8) 0%,
    rgba(44, 62, 80, 0.4) 50%,
    transparent 100%
  );
  border-radius: 50%;
  animation: particleFloat 5s ease-in-out infinite;
  
  &::before {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 20px;
    height: 20px;
    background: radial-gradient(
      circle,
      rgba(140, 120, 83, 0.1) 0%,
      transparent 70%
    );
    border-radius: 50%;
  }
}

// 🌟 光晕效果
.title-glow {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 120%;
  height: 120%;
  background: radial-gradient(
    ellipse,
    rgba(140, 120, 83, 0.1) 0%,
    rgba(44, 62, 80, 0.05) 30%,
    transparent 70%
  );
  border-radius: 50%;
  z-index: -2;
  filter: blur(10px);
}

// 🎬 动画定义
@keyframes ink-flow {
  0%, 100% {
    transform: translateX(-120%);
    opacity: 0;
  }
  10% {
    opacity: 0.6;
  }
  50% {
    transform: translateX(0%);
    opacity: 1;
  }
  90% {
    opacity: 0.6;
  }
  100% {
    transform: translateX(120%);
    opacity: 0;
  }
}

@keyframes particleFloat {
  0%, 100% {
    transform: translateY(0) rotate(0deg) scale(1);
    opacity: 0.4;
  }
  25% {
    transform: translateY(-10px) rotate(90deg) scale(1.2);
    opacity: 0.8;
  }
  50% {
    transform: translateY(5px) rotate(180deg) scale(0.8);
    opacity: 0.6;
  }
  75% {
    transform: translateY(-5px) rotate(270deg) scale(1.1);
    opacity: 0.9;
  }
}

// 📱 响应式设计
@media (max-width: 1024px) {
  .main-title {
    font-size: clamp(2.5rem, 7vw, 6rem);
    letter-spacing: 0.2em;
  }
  
  .english-title {
    font-size: clamp(0.9rem, 2vw, 1.4rem);
    letter-spacing: 0.3em;
  }
}

@media (max-width: 768px) {
  .title-wrapper {
    padding: 1.5rem;
  }
  
  .main-title {
    font-size: clamp(2rem, 6vw, 4rem);
    letter-spacing: 0.15em;
    margin-bottom: 1rem;
  }
  
  .english-title {
    font-size: clamp(0.8rem, 1.8vw, 1.2rem);
    letter-spacing: 0.25em;
  }
  
  .brush-effect {
    width: 90%;
    height: 4px;
    bottom: -10px;
  }
  
  .title-particle {
    width: 4px;
    height: 4px;
  }
}

@media (max-width: 480px) {
  .main-title {
    font-size: clamp(1.8rem, 5vw, 3rem);
    letter-spacing: 0.1em;
  }
  
  .english-title {
    font-size: clamp(0.7rem, 1.5vw, 1rem);
    letter-spacing: 0.2em;
  }
  
  .title-char {
    margin: 0 0.05em;
  }
  
  .english-word {
    margin: 0 0.3em;
    
    &:not(:last-child)::after {
      margin-left: 0.6em;
    }
  }
}
</style>