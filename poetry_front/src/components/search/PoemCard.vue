<template>
    <div 
      class="poem-card"
      :class="{ 
        'highlighted': hasSearchMatch,
        'list-mode': viewMode === 'list',
        'grid-mode': viewMode === 'grid'
      }"
      @click="$emit('view-detail', poem.PID || poem.id)"
    >
      <!-- 🔧 修改：增强收藏按钮，保持原有样式并添加动画效果 -->
      <button
        class="favorite-btn"
        :class="{ 
          'active': isFavorite(poem.PID || poem.id),
          'favorited': isFavorite(poem.PID || poem.id)
        }"
        @click.stop="handleFavoriteClick"
        title="收藏/取消收藏"
      >
        <span class="heart-icon">❤️</span>
        <!-- 🔧 新增：收藏成功的波纹效果 -->
        <div v-if="showRipple" class="heart-ripple"></div>
      </button>
      
      <!-- 诗词头部 -->
      <div class="poem-header">
        <h3 class="poem-title" v-html="highlightText(poem.Title || poem.title)"></h3>
        <div class="poem-meta">
          <span class="poet" v-html="highlightText(poem.Poet || poem.poet)"></span>
          <span class="separator">•</span>
          <span class="dynasty">{{ poem.Dynasty || poem.dynasty || '古代' }}</span>
          <span v-if="poem.Category || poem.category" class="category">
            <span class="category-icon">📖</span>
            {{ poem.Category || poem.category }}
          </span>
        </div>
      </div>
      
      <!-- 诗词正文 -->
      <div class="poem-content" :style="{ fontSize: fontSize + 'px' }">
        <div class="poem-text" v-html="highlightText(formatPoemText(poem.Text || poem.text))"></div>
        
        <!-- 诗词装饰边框 -->
        <div class="poem-decoration">
          <div class="decoration-corner top-left"></div>
          <div class="decoration-corner top-right"></div>
          <div class="decoration-corner bottom-left"></div>
          <div class="decoration-corner bottom-right"></div>
        </div>
      </div>
      
      <!-- 诗词分析/背景 -->
      <div 
        v-if="poem.Background || poem.background || poem.Appreciation || poem.appreciation" 
        class="poem-analysis"
      >
        <div class="analysis-header">
          <div>
            <span class="analysis-icon">📝</span>
            <span class="analysis-label">作品赏析</span>
          </div>
          <button 
            @click.stop="toggleAnalysis" 
            class="toggle-analysis-btn"
            :class="{ 'expanded': showFullAnalysis }"
          >
            {{ showFullAnalysis ? '收起' : '展开' }}
          </button>
        </div>
        <div 
          class="analysis-content"
          :class="{ 'expanded': showFullAnalysis }"
        >
          <div class="analysis-text">
            {{ showFullAnalysis ? fullAnalysisText : shortAnalysisText }}
          </div>
        </div>
      </div>
  
      <!-- 标签区域 -->
      <div v-if="hasAnyTags" class="poem-tags-section">
        <div class="poem-tags">
          <span v-if="poem.Theme" class="tag theme-tag">
            <span class="tag-icon">🎭</span>
            {{ poem.Theme }}
          </span>
          <span v-if="poem.Style" class="tag style-tag">
            <span class="tag-icon">✨</span>
            {{ poem.Style }}
          </span>
          <span v-if="poem.Mood" class="tag mood-tag">
            <span class="tag-icon">🌙</span>
            {{ poem.Mood }}
          </span>
          <span v-if="poem.Season" class="tag season-tag">
            <span class="tag-icon">🌸</span>
            {{ poem.Season }}
          </span>
        </div>
      </div>
  
      <!-- 卡片底部操作 -->
      <div class="poem-footer">
        <div class="poem-stats">
          <span v-if="poem.readCount" class="stat-item">
            <span class="stat-icon">👁️</span>
            <span class="stat-value">{{ formatNumber(poem.readCount) }}</span>
          </span>
          <span v-if="poem.likeCount" class="stat-item">
            <span class="stat-icon">👍</span>
            <span class="stat-value">{{ formatNumber(poem.likeCount) }}</span>
          </span>
          <!-- 🔧 新增：显示收藏状态 -->
          <span v-if="isFavorite(poem.PID || poem.id)" class="stat-item favorited-indicator">
            <span class="stat-icon">❤️</span>
            <span class="stat-value">已收藏</span>
          </span>
        </div>
        
        <div class="card-actions">
          <button @click.stop="sharePoemCard" class="action-btn share-btn" title="分享诗词">
            <span class="action-icon">📤</span>
            <span class="action-text">分享</span>
          </button>
          <button @click.stop="copyPoemText" class="action-btn copy-btn" title="复制内容">
            <span class="action-icon">📋</span>
            <span class="action-text">复制</span>
          </button>
          <button @click.stop="playPoem" class="action-btn play-btn" title="朗读诗词">
            <span class="action-icon">🔊</span>
            <span class="action-text">朗读</span>
          </button>
          <!-- 🔧 新增：快速收藏按钮（在底部操作区） -->
          <button 
            @click.stop="handleFavoriteClick" 
            class="action-btn favorite-action-btn" 
            :class="{ 'favorited': isFavorite(poem.PID || poem.id) }"
            :title="isFavorite(poem.PID || poem.id) ? '取消收藏' : '收藏诗词'"
          >
            <span class="action-icon">{{ isFavorite(poem.PID || poem.id) ? '💖' : '🤍' }}</span>
            <span class="action-text">{{ isFavorite(poem.PID || poem.id) ? '已收藏' : '收藏' }}</span>
          </button>
        </div>
      </div>
  
      <!-- 悬停效果背景 -->
      <div class="hover-bg"></div>
    </div>
  </template>
  
  <script setup>
  import { computed, ref } from 'vue'
  
  const props = defineProps({
    poem: {
      type: Object,
      required: true
    },
    fontSize: {
      type: Number,
      default: 16
    },
    isFavorite: {
      type: Function,
      required: true
    },
    searchQuery: {
      type: String,
      default: ''
    },
    viewMode: {
      type: String,
      default: 'grid',
      validator: value => ['grid', 'list'].includes(value)
    }
  })
  
  const emit = defineEmits(['view-detail', 'toggle-favorite'])
  
  // 本地状态
  const showFullAnalysis = ref(false)
  const showRipple = ref(false)
  
  // 🔧 新增：收藏点击处理，添加动画效果
  const handleFavoriteClick = () => {
    const poemId = props.poem.PID || props.poem.id
    emit('toggle-favorite', poemId)
    
    // 触发波纹动画
    showRipple.value = true
    setTimeout(() => {
      showRipple.value = false
    }, 1000)
    
    // 触发成功提示
    const isCurrentlyFavorited = props.isFavorite(poemId)
    const message = isCurrentlyFavorited ? '已取消收藏' : '收藏成功'
    showToast(message)
  }
  
  // 计算属性
  const hasSearchMatch = computed(() => {
    if (!props.searchQuery) return false
    const query = props.searchQuery.toLowerCase()
    const poem = props.poem
    
    return [
      poem.Title || poem.title,
      poem.Poet || poem.poet,
      poem.Text || poem.text,
      poem.Category || poem.category
    ].some(field => field && field.toLowerCase().includes(query))
  })
  
  const fullAnalysisText = computed(() => {
    return props.poem.Background || props.poem.background || 
           props.poem.Appreciation || props.poem.appreciation || ''
  })
  
  const shortAnalysisText = computed(() => {
    const fullText = fullAnalysisText.value
    return fullText.length > 100 ? fullText.substring(0, 100) + '...' : fullText
  })
  
  const hasAnyTags = computed(() => {
    const poem = props.poem
    return !!(poem.Theme || poem.Style || poem.Mood || poem.Season)
  })
  
  // 方法
  const formatPoemText = (text) => {
    if (!text) return ''
    // 处理诗词格式，在标点符号后换行，保持古诗词的韵律美
    return text
      .replace(/[。！？；]/g, '$&\n')
      .replace(/[，、]/g, '$&\u00A0\u00A0') // 在逗号后添加空格
      .trim()
  }
  
  const highlightText = (text) => {
    if (!text || !props.searchQuery) return text
    
    const query = props.searchQuery.trim()
    if (!query) return text
    
    // 高亮搜索关键词
    const regex = new RegExp(`(${escapeRegExp(query)})`, 'gi')
    return text.replace(regex, '<mark class="search-highlight">$1</mark>')
  }
  
  const escapeRegExp = (string) => {
    return string.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  }
  
  const formatNumber = (num) => {
    if (!num) return '0'
    if (num >= 10000) {
      return Math.floor(num / 1000) / 10 + 'w'
    }
    if (num >= 1000) {
      return Math.floor(num / 100) / 10 + 'k'
    }
    return num.toString()
  }
  
  const toggleAnalysis = () => {
    showFullAnalysis.value = !showFullAnalysis.value
  }
  
  const sharePoemCard = async () => {
    const poem = props.poem
    const shareText = `${poem.Title || poem.title} —— ${poem.Poet || poem.poet}\n\n${poem.Text || poem.text}`
    
    try {
      if (navigator.share) {
        await navigator.share({
          title: poem.Title || poem.title,
          text: shareText,
          url: window.location.href
        })
      } else {
        await navigator.clipboard.writeText(shareText)
        showToast('诗词内容已复制到剪贴板')
      }
    } catch (error) {
      console.error('分享失败:', error)
      showToast('分享失败，请重试')
    }
  }
  
  const copyPoemText = async () => {
    const poem = props.poem
    const copyText = `${poem.Title || poem.title}\n${poem.Poet || poem.poet} (${poem.Dynasty || poem.dynasty})\n\n${poem.Text || poem.text}`
    
    try {
      await navigator.clipboard.writeText(copyText)
      showToast('诗词已复制到剪贴板')
    } catch (error) {
      console.error('复制失败:', error)
      showToast('复制失败，请手动选择文本')
    }
  }
  
  const playPoem = () => {
    const poem = props.poem
    const textToSpeak = `${poem.Title || poem.title}，${poem.Poet || poem.poet}。${poem.Text || poem.text}`
    
    if ('speechSynthesis' in window) {
      // 取消当前播放
      speechSynthesis.cancel()
      
      const utterance = new SpeechSynthesisUtterance(textToSpeak)
      utterance.lang = 'zh-CN'
      utterance.rate = 0.8
      utterance.pitch = 1.1
      
      speechSynthesis.speak(utterance)
      showToast('开始朗读诗词')
    } else {
      showToast('您的浏览器不支持语音朗读功能')
    }
  }
  
  const showToast = (message) => {
    // 简单的消息提示实现
    const toast = document.createElement('div')
    toast.className = 'toast-message'
    toast.textContent = message
    toast.style.cssText = `
      position: fixed;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      background: rgba(0, 0, 0, 0.8);
      color: white;
      padding: 1rem 2rem;
      border-radius: 8px;
      z-index: 9999;
      animation: toast-fade 2s ease-in-out forwards;
      font-size: 0.9rem;
      font-weight: 500;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
    `
    
    // 添加CSS动画
    if (!document.querySelector('#toast-styles')) {
      const style = document.createElement('style')
      style.id = 'toast-styles'
      style.textContent = `
        @keyframes toast-fade {
          0% { opacity: 0; transform: translate(-50%, -50%) scale(0.8); }
          20% { opacity: 1; transform: translate(-50%, -50%) scale(1); }
          80% { opacity: 1; transform: translate(-50%, -50%) scale(1); }
          100% { opacity: 0; transform: translate(-50%, -50%) scale(0.8); }
        }
      `
      document.head.appendChild(style)
    }
    
    document.body.appendChild(toast)
    setTimeout(() => {
      if (document.body.contains(toast)) {
        document.body.removeChild(toast)
      }
    }, 2000)
  }
  </script>
  
  <style scoped>
  .poem-card {
    position: relative;
    background: white;
    border-radius: 20px;
    padding: 1.8rem;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    cursor: pointer;
    transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    border: 2px solid transparent;
    overflow: hidden;
    backdrop-filter: blur(10px);
  }
  
  /* 顶部装饰渐变条 */
  .poem-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #8c7853 100%);
    transform: scaleX(0);
    transition: transform 0.4s ease;
    transform-origin: left;
  }
  
  .poem-card:hover::before {
    transform: scaleX(1);
  }
  
  /* 悬停效果 */
  .hover-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.03) 0%, rgba(140, 120, 83, 0.03) 100%);
    opacity: 0;
    transition: opacity 0.3s ease;
    pointer-events: none;
  }
  
  .poem-card:hover .hover-bg {
    opacity: 1;
  }
  
  .poem-card:hover {
    transform: translateY(-8px);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
    border-color: #667eea;
  }
  
  .poem-card.highlighted {
    background: linear-gradient(135deg, #fff9e6 0%, #ffffff 100%);
    border-color: #ffc107;
    box-shadow: 0 6px 25px rgba(255, 193, 7, 0.2);
  }
  
  /* 列表模式样式 */
  .poem-card.list-mode {
    display: flex;
    align-items: flex-start;
    gap: 1.5rem;
    padding: 1.5rem;
  }
  
  .poem-card.list-mode .poem-header {
    min-width: 200px;
    padding-right: 1rem;
  }
  
  .poem-card.list-mode .poem-content {
    flex: 1;
  }
  
  /* 🔧 增强：收藏按钮样式 */
  .favorite-btn {
    position: absolute;
    top: 1.2rem;
    right: 1.2rem;
    background: rgba(255, 255, 255, 0.95);
    border: none;
    border-radius: 50%;
    width: 48px;
    height: 48px;
    cursor: pointer;
    font-size: 1.4rem;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.3s ease;
    backdrop-filter: blur(15px);
    z-index: 10;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    color: #bbb;
  }
  
  .favorite-btn:hover {
    background: white;
    transform: scale(1.1);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  }
  
  /* 🔧 新增：收藏激活状态 */
  .favorite-btn.active {
    color: #e74c3c;
    background: rgba(231, 76, 60, 0.1);
    border: 2px solid rgba(231, 76, 60, 0.3);
  }
  
  .favorite-btn.favorited {
    background: linear-gradient(135deg, #ffebee 0%, #fce4ec 100%);
    animation: heartbeat 1.8s ease-in-out infinite;
  }
  
  .heart-ripple {
    position: absolute;
    top: 50%;
    left: 50%;
    width: 20px;
    height: 20px;
    border: 2px solid #e74c3c;
    border-radius: 50%;
    transform: translate(-50%, -50%);
    animation: ripple 1s ease-out;
  }
  
  @keyframes heartbeat {
    0%, 100% { transform: scale(1); }
    25% { transform: scale(1.05); }
    75% { transform: scale(0.95); }
  }
  
  @keyframes ripple {
    0% {
      width: 20px;
      height: 20px;
      opacity: 1;
    }
    100% {
      width: 60px;
      height: 60px;
      opacity: 0;
    }
  }
  
  .heart-icon {
    display: block;
    position: relative;
    z-index: 1;
  }
  
  /* 诗词头部 */
  .poem-header {
    margin-bottom: 1.2rem;
    padding-right: 3.5rem;
  }
  
  .poem-title {
    color: #8c7853;
    font-size: 1.4rem;
    margin: 0 0 0.6rem 0;
    font-weight: 600;
    line-height: 1.4;
    transition: color 0.3s ease;
  }
  
  .poem-card:hover .poem-title {
    color: #667eea;
  }
  
  .poem-meta {
    display: flex;
    align-items: center;
    gap: 0.6rem;
    color: #666;
    font-size: 0.9rem;
    flex-wrap: wrap;
  }
  
  .poet {
    font-weight: 600;
    color: #555;
    padding: 0.2rem 0.6rem;
    background: #f8f4ed;
    border-radius: 12px;
    transition: all 0.3s ease;
  }
  
  .poem-card:hover .poet {
    background: #667eea;
    color: white;
  }
  
  .separator {
    opacity: 0.5;
  }
  
  .dynasty {
    color: #888;
    font-size: 0.85rem;
  }
  
  .category {
    display: flex;
    align-items: center;
    gap: 0.3rem;
    background: linear-gradient(135deg, #e8f5e8 0%, #f0f8f0 100%);
    padding: 0.3rem 0.8rem;
    border-radius: 15px;
    font-size: 0.8rem;
    color: #2e7d32;
    font-weight: 500;
  }
  
  .category-icon {
    font-size: 0.9rem;
  }
  
  /* 诗词内容 */
  .poem-content {
    position: relative;
    margin-bottom: 1.2rem;
    padding: 1.5rem;
    background: linear-gradient(135deg, #fafafa 0%, #ffffff 100%);
    border-radius: 15px;
    border-left: 4px solid #8c7853;
  }
  
  .poem-text {
    color: #333;
    line-height: 2;
    white-space: pre-line;
    font-family: 'KaiTi', 'STKaiti', '楷体', serif;
    text-align: center;
    position: relative;
    z-index: 1;
  }
  
  /* 诗词装饰角 */
  .poem-decoration {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    pointer-events: none;
  }
  
  .decoration-corner {
    position: absolute;
    width: 20px;
    height: 20px;
    border: 2px solid #8c7853;
    opacity: 0.3;
  }
  
  .decoration-corner.top-left {
    top: 8px;
    left: 8px;
    border-right: none;
    border-bottom: none;
  }
  
  .decoration-corner.top-right {
    top: 8px;
    right: 8px;
    border-left: none;
    border-bottom: none;
  }
  
  .decoration-corner.bottom-left {
    bottom: 8px;
    left: 8px;
    border-right: none;
    border-top: none;
  }
  
  .decoration-corner.bottom-right {
    bottom: 8px;
    right: 8px;
    border-left: none;
    border-top: none;
  }
  
  /* 诗词分析 */
  .poem-analysis {
    background: linear-gradient(135deg, #f8f4ed 0%, #faf7f0 100%);
    padding: 1.2rem;
    border-radius: 15px;
    margin-bottom: 1rem;
    border-left: 4px solid #8c7853;
    transition: all 0.3s ease;
  }
  
  .analysis-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 0.8rem;
  }
  
  .analysis-header > div {
    display: flex;
    align-items: center;
    gap: 0.5rem;
  }
  
  .analysis-icon {
    font-size: 1.1rem;
  }
  
  .analysis-label {
    font-size: 0.9rem;
    font-weight: 600;
    color: #8c7853;
  }
  
  .toggle-analysis-btn {
    background: rgba(140, 120, 83, 0.1);
    border: 1px solid rgba(140, 120, 83, 0.3);
    color: #8c7853;
    padding: 0.3rem 0.8rem;
    border-radius: 12px;
    cursor: pointer;
    font-size: 0.8rem;
    transition: all 0.2s ease;
  }
  
  .toggle-analysis-btn:hover {
    background: #8c7853;
    color: white;
  }
  
  .analysis-content {
    max-height: 60px;
    overflow: hidden;
    transition: max-height 0.3s ease;
  }
  
  .analysis-content.expanded {
    max-height: 300px;
  }
  
  .analysis-text {
    font-size: 0.85rem;
    color: #5a4634;
    line-height: 1.6;
  }
  
  /* 标签区域 */
  .poem-tags-section {
    margin-bottom: 1rem;
  }
  
  .poem-tags {
    display: flex;
    gap: 0.6rem;
    flex-wrap: wrap;
  }
  
  .tag {
    display: flex;
    align-items: center;
    gap: 0.3rem;
    padding: 0.4rem 0.8rem;
    border-radius: 15px;
    font-size: 0.75rem;
    font-weight: 500;
    transition: all 0.3s ease;
  }
  
  .tag-icon {
    font-size: 0.8rem;
  }
  
  .theme-tag {
    background: linear-gradient(135deg, #e3f2fd 0%, #f0f8ff 100%);
    color: #1976d2;
    border: 1px solid rgba(25, 118, 210, 0.2);
  }
  
  .style-tag {
    background: linear-gradient(135deg, #f3e5f5 0%, #faf0fc 100%);
    color: #7b1fa2;
    border: 1px solid rgba(123, 31, 162, 0.2);
  }
  
  .mood-tag {
    background: linear-gradient(135deg, #e8f5e8 0%, #f0f8f0 100%);
    color: #2e7d32;
    border: 1px solid rgba(46, 125, 50, 0.2);
  }
  
  .season-tag {
    background: linear-gradient(135deg, #fff3e0 0%, #fff8f0 100%);
    color: #f57c00;
    border: 1px solid rgba(245, 124, 0, 0.2);
  }
  
  .tag:hover {
    transform: scale(1.05);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
  
  /* 底部操作区 */
  .poem-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 1rem;
    flex-wrap: wrap;
  }
  
  .poem-stats {
    display: flex;
    gap: 1rem;
  }
  
  .stat-item {
    display: flex;
    align-items: center;
    gap: 0.3rem;
    color: #666;
    font-size: 0.8rem;
  }
  
  .stat-icon {
    font-size: 0.9rem;
  }
  
  .stat-value {
    font-weight: 500;
  }
  
  /* 🔧 新增：收藏状态指示器 */
  .favorited-indicator {
    color: #e74c3c;
    font-weight: 600;
    background: rgba(231, 76, 60, 0.1);
    padding: 0.3rem 0.6rem;
    border-radius: 12px;
    border: 1px solid rgba(231, 76, 60, 0.2);
  }
  
  .card-actions {
    display: flex;
    gap: 0.6rem;
  }
  
  .action-btn {
    display: flex;
    align-items: center;
    gap: 0.4rem;
    padding: 0.5rem 1rem;
    background: #f8f9fa;
    border: 2px solid transparent;
    border-radius: 12px;
    cursor: pointer;
    font-size: 0.8rem;
    font-weight: 500;
    transition: all 0.3s ease;
    color: #666;
  }
  
  .action-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  }
  
  .share-btn:hover {
    background: #4CAF50;
    color: white;
    border-color: #45a049;
  }
  
  .copy-btn:hover {
    background: #2196F3;
    color: white;
    border-color: #1976D2;
  }
  
  .play-btn:hover {
    background: #FF9800;
    color: white;
    border-color: #F57C00;
  }
  
  /* 🔧 新增：收藏操作按钮样式 */
  .favorite-action-btn {
    background: #f8f9fa;
    border: 2px solid #e9ecef;
  }
  
  .favorite-action-btn:hover {
    background: #e74c3c;
    color: white;
    border-color: #c0392b;
  }
  
  .favorite-action-btn.favorited {
    background: linear-gradient(135deg, #ffebee 0%, #fce4ec 100%);
    color: #e74c3c;
    border-color: #e74c3c;
  }
  
  .favorite-action-btn.favorited:hover {
    background: #e74c3c;
    color: white;
  }
  
  .action-icon {
    font-size: 1rem;
  }
  
  .action-text {
    font-weight: 500;
  }
  
  /* 搜索高亮样式 */
  :deep(.search-highlight) {
    background: linear-gradient(135deg, #fff3cd 0%, #ffeaa7 100%);
    padding: 0.1rem 0.3rem;
    border-radius: 4px;
    font-weight: 600;
    color: #8c6200;
    box-shadow: 0 1px 3px rgba(255, 193, 7, 0.3);
  }
  
  /* 响应式设计 */
  @media (max-width: 768px) {
    .poem-card {
      padding: 1.2rem;
    }
    
    .poem-card.list-mode {
      flex-direction: column;
      gap: 1rem;
    }
    
    .poem-card.list-mode .poem-header {
      min-width: auto;
      padding-right: 3.5rem;
    }
    
    .favorite-btn {
      width: 40px;
      height: 40px;
      font-size: 1.2rem;
    }
    
    .poem-header {
      padding-right: 3rem;
    }
    
    .poem-title {
      font-size: 1.2rem;
    }
    
    .poem-content {
      padding: 1rem;
    }
    
    .card-actions .action-text {
      display: none;
    }
    
    .action-btn {
      padding: 0.5rem;
      min-width: 40px;
      justify-content: center;
    }
  }
  
  @media (max-width: 480px) {
    .poem-card {
      padding: 1rem;
      margin-bottom: 1rem;
    }
    
    .poem-footer {
      flex-direction: column;
      align-items: stretch;
      gap: 0.8rem;
    }
    
    .card-actions {
      justify-content: center;
    }
    
    .poem-meta {
      font-size: 0.8rem;
    }
    
    .poet {
      padding: 0.1rem 0.4rem;
      font-size: 0.8rem;
    }
  }
  </style>