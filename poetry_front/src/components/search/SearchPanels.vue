<template>
    <!-- 搜索历史面板 -->
    <HistoryPanel
      v-if="showHistory"
      :history="searchHistory"
      @close="$emit('close-history')"
      @search="$emit('search-from-history', $event)"
      @clear="$emit('clear-history')"
    />
  
    <!-- 收藏面板 -->
    <FavoritesPanel
      v-if="showFavorites"
      :favorites="favoritePoems"
      @close="$emit('close-favorites')"
      @view-detail="$emit('view-detail', $event)"
      @toggle-favorite="$emit('toggle-favorite', $event)"
      @clear-all="$emit('clear-all-favorites')"
    />
  </template>
  
  <script setup>
  import HistoryPanel from './panels/HistoryPanel.vue'
  import FavoritesPanel from './panels/FavoritesPanel.vue'
  
  defineProps({
    showHistory: Boolean,
    showFavorites: Boolean,
    searchHistory: Array,
    favoritePoems: Array
  })
  
  defineEmits([
    'close-history',
    'close-favorites',
    'search-from-history',
    'clear-history',
    'view-detail',
    'toggle-favorite',
    'clear-all-favorites'
  ])
  </script>