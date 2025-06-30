<template>
  <div class="game-room">
    <h1>游戏房间</h1>
    <GameBoard />
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import GameBoard from '../components/GameBoard.vue';

export default defineComponent({
  components: {
    GameBoard,
  },
});
</script>

<style scoped>
.game-room {
  padding: 20px;
}

.player-info {
  margin-bottom: 20px;
}
</style>