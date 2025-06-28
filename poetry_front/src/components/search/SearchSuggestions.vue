<template>
    <div class="search-suggestions">
      <div
        v-for="(suggestion, index) in suggestions"
        :key="index"
        @click="$emit('select', suggestion)"
        class="suggestion-item"
        :class="{ 'highlighted': highlightedIndex === index }"
      >
        <span class="suggestion-icon">{{ getSuggestionIcon(suggestion.type) }}</span>
        <div class="suggestion-content">
          <div class="suggestion-main">{{ suggestion.text }}</div>
          <div class="suggestion-sub">{{ suggestion.subtitle }}</div>
        </div>
        <div class="suggestion-arrow">→</div>
      </div>
    </div>
  </template>
  
  <script setup>
  defineProps({
    suggestions: Array,
    highlightedIndex: Number
  })
  
  defineEmits(['select'])
  
  const getSuggestionIcon = (type) => {
    const icons = {
      history: '🕒',
      poet: '👤',
      theme: '🎭',
      dynasty: '🏛️',
      season: '🌸',
      scene: '🌙',
      keyword: '🔍'
    }
    return icons[type] || '🔍'
  }
  </script>
  
  <style scoped>
  .search-suggestions {
    position: absolute;
    top: 100%;
    left: 0;
    right: 0;
    background: white;
    border: 1px solid #e1d8c9;
    border-top: none;
    border-radius: 0 0 25px 25px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
    z-index: 1000;
    max-height: 400px;
    overflow-y: auto;
  }
  
  .suggestion-item {
    display: flex;
    align-items: center;
    padding: 1rem 1.5rem;
    cursor: pointer;
    transition: all 0.2s;
    border-bottom: 1px solid #f5f5f5;
  }
  
  .suggestion-item:hover,
  .suggestion-item.highlighted {
    background: linear-gradient(135deg, #f8f4ed 0%, #f0ede3 100%);
    transform: translateX(5px);
  }
  
  .suggestion-icon {
    margin-right: 1rem;
    font-size: 1.3rem;
    width: 24px;
    text-align: center;
  }
  
  .suggestion-content {
    flex: 1;
  }
  
  .suggestion-main {
    font-weight: 500;
    color: #333;
    margin-bottom: 0.2rem;
  }
  
  .suggestion-sub {
    font-size: 0.85rem;
    color: #666;
  }
  
  .suggestion-arrow {
    color: #999;
    font-size: 1.2rem;
    opacity: 0;
    transition: opacity 0.2s;
  }
  
  .suggestion-item:hover .suggestion-arrow,
  .suggestion-item.highlighted .suggestion-arrow {
    opacity: 1;
  }
  </style>