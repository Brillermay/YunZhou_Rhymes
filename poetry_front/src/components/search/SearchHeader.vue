<template>
    <header class="poetry-header">
      <div class="title-container">
        <h1 ref="titleRef">诗韵搜索</h1>
        <p ref="subtitleRef" class="subtitle">"AI智能搜索，古今诗词尽在指尖"</p>
      </div>
    </header>
  </template>
  
  <script setup>
  import { ref, onMounted, nextTick } from 'vue'
  import gsap from 'gsap'
  
  // DOM 引用
  const titleRef = ref(null)
  const subtitleRef = ref(null)
  
  // 标题下落动画
  const animateTitleDrop = () => {
    const tl = gsap.timeline()
    
    // 初始状态：将标题和副标题移到容器上方（不可见区域）
    gsap.set(titleRef.value, { 
      y: -120, // 移到上方120px（完全看不见）
      opacity: 0,
      rotationX: -90 // 添加3D旋转效果
    })
    
    gsap.set(subtitleRef.value, { 
      y: -80,
      opacity: 0,
      scale: 0.8
    })
    
    // 标题下落动画
    tl.to(titleRef.value, {
      y: 0,
      opacity: 1,
      rotationX: 0,
      duration: 1.2,
      ease: "bounce.out",
      delay: 0.5
    })
    // 副标题跟随动画
    .to(subtitleRef.value, {
      y: 0,
      opacity: 1,
      scale: 1,
      duration: 0.8,
      ease: "back.out(1.7)"
    }, "-=0.6") // 在主标题动画进行到一半时开始
    
    // 添加一个轻微的浮动效果
    tl.to([titleRef.value, subtitleRef.value], {
      y: "+=5",
      duration: 2,
      ease: "power1.inOut",
      yoyo: true,
      repeat: -1
    })
  }
  
  onMounted(async () => {
    console.log('搜索头部组件已挂载，开始标题动画')
    
    // 等待DOM更新
    await nextTick()
    
    // 播放标题下落动画
    animateTitleDrop()
  })
  </script>
  
  <style scoped>
  /* 头部区域 */
  .poetry-header {
    text-align: center;
    padding: 2rem 0 1rem 0;
    background: linear-gradient(
      135deg, 
      rgba(140, 120, 83, 0.9) 0%, 
      rgba(110, 87, 115, 0.9) 100%
    );
    color: white;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
    width: 100%;
    position: relative;
    overflow: hidden;
    backdrop-filter: blur(10px);
    border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  }
  
  .title-container {
    position: relative;
    max-width: 800px;
    margin: 0 auto;
    padding: 0 1rem;
  }
  
  .poetry-header h1 {
    margin: 0;
    font-size: 2.5rem;
    font-weight: 300;
    color: white;
    text-shadow: 0 4px 8px rgba(0, 0, 0, 0.3);
    transform-origin: center center;
    transform-style: preserve-3d;
    letter-spacing: 0.1em;
  }
  
  .poetry-header .subtitle {
    margin: 1rem 0 0;
    font-size: 1rem;
    font-style: italic;
    opacity: 0.9;
    transform-origin: center center;
    color: rgba(255, 255, 255, 0.9);
  }
  
  /* 响应式设计 */
  @media (max-width: 768px) {
    .poetry-header {
      padding: 1.5rem 0 0.8rem 0;
    }
    
    .poetry-header h1 {
      font-size: 2rem;
    }
    
    .poetry-header .subtitle {
      font-size: 0.9rem;
    }
    
    .title-container {
      padding: 0 0.5rem;
    }
  }
  
  @media (max-width: 480px) {
    .poetry-header h1 {
      font-size: 1.8rem;
    }
    
    .poetry-header .subtitle {
      font-size: 0.85rem;
    }
  }
  
  /* 平滑滚动 */
  * {
    scroll-behavior: smooth;
  }
  
  /* 选择文本样式 */
  ::selection {
    background: rgba(255, 255, 255, 0.2);
    color: white;
  }
  </style>