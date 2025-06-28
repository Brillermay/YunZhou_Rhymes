<template>
  <div class="layout">
    <div class="main-content">
      <SideBar />
      <div class="right-section">
        <TopBar />
        <PhaserGame />
      </div>
    </div>
  </div>
</template>

<script setup>
import SideBar from '@/components/SideBar.vue'
import TopBar from '@/components/TopBar.vue'
import PhaserGame from '@/components/PhaserGame.vue'
</script>

<style scoped>
/* 全局布局样式 */
.layout {
  width: 100%;
  height: 100vh;
  display: flex;
}

.main-content {
  flex: 1;
  display: flex;
}

.right-section {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.game-container {
  flex: 1;
  padding: 20px;
  background-color: #f5f5f5;
  display: flex;
}

.game-container > div {
  flex: 1;
  min-height: 0;
}
</style>