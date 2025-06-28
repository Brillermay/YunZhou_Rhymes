<template>
  <div class="side-bar">
    <div class="tab-buttons">
      <button
          :class="['tab-button', { active: activeTab === 'achievements' }]"
          @click="activeTab = 'achievements'"
      >
        成就
      </button>
      <button
          :class="['tab-button', { active: activeTab === 'recipes' }]"
          @click="activeTab = 'recipes'"
      >
        合成表
      </button>
    </div>

    <div class="tab-content">
      <AchievementsList v-if="activeTab === 'achievements'" />
      <RecipesList v-if="activeTab === 'recipes'" />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import AchievementsList from '@/components/AchievementsList.vue'
import RecipesList from '@/components/RecipesList.vue'

const activeTab = ref('achievements')
</script>

<style scoped>
/* 侧边栏样式 */
.side-bar {
  width: 250px;
  background-color: #34495e;
  color: white;
  display: flex;
  flex-direction: column;
}

.tab-buttons {
  display: flex;
  border-bottom: 1px solid #456789;
}

.tab-button {
  flex: 1;
  padding: 10px;
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  transition: background-color 0.3s;
}

.tab-button.active  {
  background-color: #456789;
}

.tab-content {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
}
</style>