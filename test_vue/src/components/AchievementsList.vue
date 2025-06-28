<template>
  <div class="achievements">
    <h3>成就列表</h3>
    <div v-for="achievement in achievements" :key="achievement.id"  class="achievement-item">
      <span :class="['achievement-icon', { unlocked: achievement.unlocked  }]">🏆</span>
      <div class="achievement-info">
        <div class="achievement-name">{{ achievement.name  }}</div>
        <div class="achievement-desc">{{ achievement.description  }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useGameData } from 'useGameData'

const { achievements } = useGameData()
</script>

<style scoped>
.achievement-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #456789;
}

.achievement-icon {
  opacity: 0.5;
  margin-right: 10px;
}

.achievement-icon.unlocked  {
  opacity: 1;
}

.achievement-info {
  flex: 1;
}

.achievement-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.achievement-desc {
  font-size: 0.9em;
  opacity: 0.8;
}
</style>