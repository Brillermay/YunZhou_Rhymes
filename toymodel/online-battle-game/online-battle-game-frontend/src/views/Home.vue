<template>
  <div class="home">
    <h1>欢迎来到多人在线对战游戏</h1>
    <p>准备好与朋友们一起挑战了吗？</p>
    <router-link to="/game-room">
      <button class="start-button">开始游戏</button>
    </router-link>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'Home',
});
</script>

<style scoped>
.home {
  text-align: center;
  margin-top: 50px;
}

.start-button {
  padding: 10px 20px;
  font-size: 16px;
  cursor: pointer;
}
</style>