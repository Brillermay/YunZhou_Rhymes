<template>
    <div class="quick-search-section">
      <div class="section-title">
        <span class="fire-icon">🔥</span>
        <span>热门搜索</span>
      </div>
      <div class="quick-tags">
        <button
          v-for="tag in quickSearchTags"
          :key="tag.text"
          @click="$emit('quick-search', tag.text)"
          class="quick-tag"
          :class="tag.type"
        >
          <span class="tag-icon">{{ tag.icon }}</span>
          {{ tag.text }}
        </button>
      </div>
    </div>
  </template>
  
  <script setup>
  defineEmits(['quick-search'])
  
  const quickSearchTags = [
    { text: '李白', icon: '👤', type: 'poet' },
    { text: '杜甫', icon: '👤', type: 'poet' },
    { text: '苏轼', icon: '👤', type: 'poet' },
    { text: '思乡诗词', icon: '🏠', type: 'theme' },
    { text: '春天的诗', icon: '🌸', type: 'season' },
    { text: '月亮', icon: '🌙', type: 'scene' },
    { text: '爱情诗词', icon: '💕', type: 'theme' },
    { text: '山水田园', icon: '🏔️', type: 'theme' },
    { text: '边塞诗', icon: '🏺', type: 'theme' },
    { text: '送别诗', icon: '👋', type: 'theme' }
  ]
  </script>
  
  <style scoped>
  .quick-search-section {
    text-align: center;
  }
  
  .section-title {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 0.5rem;
    color: #666;
    font-size: 1rem;
    margin-bottom: 1rem;
    font-weight: 500;
  }
  
  .fire-icon {
    animation: flame 1.5s ease-in-out infinite alternate;
  }
  
  @keyframes flame {
    0% { transform: scale(1) rotate(-2deg); }
    100% { transform: scale(1.1) rotate(2deg); }
  }
  
  .quick-tags {
    display: flex;
    justify-content: center;
    flex-wrap: wrap;
    gap: 0.8rem;
  }
  
  .quick-tag {
    display: flex;
    align-items: center;
    gap: 0.4rem;
    padding: 0.6rem 1.2rem;
    background: white;
    border: 2px solid #e1d8c9;
    border-radius: 25px;
    cursor: pointer;
    font-size: 0.9rem;
    transition: all 0.3s ease;
    position: relative;
    overflow: hidden;
  }
  
  .quick-tag::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255,255,255,0.4), transparent);
    transition: left 0.5s;
  }
  
  .quick-tag:hover::before {
    left: 100%;
  }
  
  .quick-tag:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  }
  
  .quick-tag.poet {
    border-color: #ff6b6b;
  }
  
  .quick-tag.poet:hover {
    background: #ff6b6b;
    color: white;
  }
  
  .quick-tag.theme {
    border-color: #4ecdc4;
  }
  
  .quick-tag.theme:hover {
    background: #4ecdc4;
    color: white;
  }
  
  .quick-tag.season {
    border-color: #45b7d1;
  }
  
  .quick-tag.season:hover {
    background: #45b7d1;
    color: white;
  }
  
  .quick-tag.scene {
    border-color: #96ceb4;
  }
  
  .quick-tag.scene:hover {
    background: #96ceb4;
    color: white;
  }
  
  .tag-icon {
    font-size: 1rem;
  }
  
  @media (max-width: 768px) {
    .quick-tags {
      justify-content: center;
    }
    
    .quick-tag {
      padding: 0.5rem 1rem;
      font-size: 0.85rem;
    }
  }
  </style>