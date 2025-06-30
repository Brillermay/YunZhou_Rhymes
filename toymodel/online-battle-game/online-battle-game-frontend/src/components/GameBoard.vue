<template>
  <div
    class="game-board"
    tabindex="0"
    @keydown="handleKeydown"
    @click="focusBoard"
    ref="boardRef"
  >
    <h1>Game Board</h1>
    <div class="grid">
      <div v-for="(cell, index) in gameState" :key="index" class="cell">
        {{ cell }}
      </div>
    </div>
    <button @click="resetGame">Reset Game</button>
    <p>请按下键盘数字1-9，格子会显示你按下的数字，并同步到所有玩家。</p>
    <p style="color: #888;">（如无响应，请先点击棋盘区域）</p>
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, ref } from 'vue';
import { gameState, sendMove, resetGame } from '../utils/socket';

export default defineComponent({
  name: 'GameBoard',
  setup() {
    const boardRef = ref<HTMLElement | null>(null);

    const handleKeydown = (event: KeyboardEvent) => {
      console.log('keydown:', event.key); // 调试用
      if (/^[1-9]$/.test(event.key)) {
        const idx = parseInt(event.key, 10) - 1;
        sendMove(idx, event.key);
      }
    };

    const focusBoard = () => {
      boardRef.value?.focus();
    };

    onMounted(() => {
      setTimeout(() => {
        boardRef.value?.focus();
      }, 100);
    });

    return {
      gameState,
      handleKeydown,
      resetGame,
      boardRef,
      focusBoard,
    };
  },
});
</script>

<style scoped>
.game-board {
  text-align: center;
  outline: none;
}

.grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 10px;
}

.cell {
  width: 60px;
  height: 60px;
  border: 1px solid #000;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  cursor: pointer;
  background: #fff;
}
</style>