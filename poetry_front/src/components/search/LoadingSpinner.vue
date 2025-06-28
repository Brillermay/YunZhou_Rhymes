<template>
    <div class="loading-container">
      <div class="loading-content">
        <!-- 主加载动画 -->
        <div class="loading-spinner">
          <div class="spinner-ring"></div>
          <div class="spinner-center">
            <span class="loading-icon">🤖</span>
          </div>
        </div>
        
        <!-- 加载文本 -->
        <div class="loading-text">
          <h3 class="loading-title">{{ currentLoadingText }}</h3>
          <p class="loading-subtitle">AI正在为您精选最合适的诗词...</p>
        </div>
        
        <!-- 进度指示器 -->
        <div class="progress-indicators">
          <div 
            v-for="(step, index) in loadingSteps" 
            :key="index"
            class="progress-step"
            :class="{ 'active': currentStep >= index, 'completed': currentStep > index }"
          >
            <div class="step-icon">{{ step.icon }}</div>
            <div class="step-text">{{ step.text }}</div>
          </div>
        </div>
        
        <!-- 装饰性诗词片段 -->
        <div class="loading-poetry">
          <div class="poetry-line" v-for="(line, index) in poetryLines" :key="index">
            {{ line }}
          </div>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, onUnmounted } from 'vue'
  
  const currentLoadingText = ref('正在搜索')
  const currentStep = ref(0)
  
  const loadingTexts = [
    '正在搜索...',
    'AI分析中...',
    '语义理解中...',
    '匹配诗词...',
    '整理结果...'
  ]
  
  const loadingSteps = [
    { icon: '🔍', text: '解析查询' },
    { icon: '🧠', text: '语义分析' },
    { icon: '📚', text: '检索诗库' },
    { icon: '✨', text: '智能排序' }
  ]
  
  const poetryLines = [
    '山重水复疑无路',
    '柳暗花明又一村',
    '众里寻他千百度',
    '蓦然回首，那人却在，灯火阑珊处'
  ]
  
  let textInterval = null
  let stepInterval = null
  
  onMounted(() => {
    // 切换加载文本
    textInterval = setInterval(() => {
      const randomIndex = Math.floor(Math.random() * loadingTexts.length)
      currentLoadingText.value = loadingTexts[randomIndex]
    }, 1500)
    
    // 进度步骤动画
    stepInterval = setInterval(() => {
      currentStep.value = (currentStep.value + 1) % (loadingSteps.length + 1)
    }, 800)
  })
  
  onUnmounted(() => {
    if (textInterval) clearInterval(textInterval)
    if (stepInterval) clearInterval(stepInterval)
  })
  </script>
  
  <style scoped>
  .loading-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 400px;
    padding: 3rem 2rem;
  }
  
  .loading-content {
    text-align: center;
    max-width: 500px;
  }
  
  .loading-spinner {
    position: relative;
    width: 120px;
    height: 120px;
    margin: 0 auto 2rem;
  }
  
  .spinner-ring {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    border: 4px solid rgba(102, 126, 234, 0.1);
    border-top: 4px solid #667eea;
    border-radius: 50%;
    animation: spin 2s linear infinite;
  }
  
  .spinner-ring::before {
    content: '';
    position: absolute;
    top: -4px;
    left: -4px;
    width: calc(100% + 8px);
    height: calc(100% + 8px);
    border: 2px solid rgba(118, 75, 162, 0.1);
    border-top: 2px solid #764ba2;
    border-radius: 50%;
    animation: spin 3s linear infinite reverse;
  }
  
  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }
  
  .spinner-center {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    background: white;
    border-radius: 50%;
    width: 60px;
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  }
  
  .loading-icon {
    font-size: 2rem;
    animation: bounce 1.5s ease-in-out infinite;
  }
  
  @keyframes bounce {
    0%, 100% { transform: scale(1); }
    50% { transform: scale(1.1); }
  }
  
  .loading-text {
    margin-bottom: 2rem;
  }
  
  .loading-title {
    color: #667eea;
    font-size: 1.5rem;
    margin: 0 0 0.5rem 0;
    font-weight: 500;
  }
  
  .loading-subtitle {
    color: #666;
    font-size: 1rem;
    margin: 0;
  }
  
  .progress-indicators {
    display: flex;
    justify-content: center;
    gap: 2rem;
    margin-bottom: 2rem;
    flex-wrap: wrap;
  }
  
  .progress-step {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.5rem;
    opacity: 0.3;
    transition: all 0.3s ease;
  }
  
  .progress-step.active {
    opacity: 1;
    transform: scale(1.1);
  }
  
  .progress-step.completed {
    opacity: 0.7;
    transform: scale(1);
  }
  
  .step-icon {
    width: 40px;
    height: 40px;
    background: #f0f0f0;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 1.2rem;
    transition: all 0.3s ease;
  }
  
  .progress-step.active .step-icon {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    animation: pulse-icon 1s ease-in-out infinite;
  }
  
  .progress-step.completed .step-icon {
    background: #4caf50;
    color: white;
  }
  
  @keyframes pulse-icon {
    0%, 100% { box-shadow: 0 0 0 0 rgba(102, 126, 234, 0.7); }
    50% { box-shadow: 0 0 0 10px rgba(102, 126, 234, 0); }
  }
  
  .step-text {
    font-size: 0.8rem;
    color: #666;
    font-weight: 500;
  }
  
  .progress-step.active .step-text {
    color: #667eea;
    font-weight: 600;
  }
  
  .loading-poetry {
    padding: 1.5rem;
    background: linear-gradient(135deg, #f8f4ed 0%, #ffffff 100%);
    border-radius: 15px;
    border-left: 4px solid #8c7853;
    opacity: 0.8;
  }
  
  .poetry-line {
    color: #8c7853;
    font-size: 0.9rem;
    line-height: 1.6;
    font-family: 'KaiTi', 'STKaiti', serif;
    animation: fade-in-out 4s ease-in-out infinite;
  }
  
  .poetry-line:nth-child(2) {
    animation-delay: 1s;
  }
  
  .poetry-line:nth-child(3) {
    animation-delay: 2s;
  }
  
  .poetry-line:nth-child(4) {
    animation-delay: 3s;
  }
  
  @keyframes fade-in-out {
    0%, 100% { opacity: 0.5; }
    25%, 75% { opacity: 1; }
  }
  
  @media (max-width: 768px) {
    .loading-container {
      padding: 2rem 1rem;
      min-height: 300px;
    }
    
    .loading-spinner {
      width: 80px;
      height: 80px;
    }
    
    .spinner-center {
      width: 40px;
      height: 40px;
    }
    
    .loading-icon {
      font-size: 1.5rem;
    }
    
    .progress-indicators {
      gap: 1rem;
    }
    
    .step-icon {
      width: 30px;
      height: 30px;
      font-size: 1rem;
    }
    
    .loading-poetry {
      padding: 1rem;
    }
    
    .poetry-line {
      font-size: 0.8rem;
    }
  }
  </style>