<template>
  <div class="game-container">
    <div ref="gameCanvas"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useGameLogic } from '@/composables/useGameLogic'

const gameCanvas = ref(null)
const { initGame, destroyGame } = useGameLogic(gameCanvas)

onMounted(initGame)
onBeforeUnmount(destroyGame)
</script>