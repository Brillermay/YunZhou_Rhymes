<!-- 文件：src/components/DualGameView.vue -->
<template>
  <div class="screen-wrapper" @wheel.passive.prevent="handleWheel">
    <div class="screens" :style="containerStyle">
      <!-- 第一个游戏页面 -->
      <div class="screen" ref="screen0"></div>
      <!-- 第二个游戏页面 -->
      <div class="screen" ref="screen1"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import Phaser from 'phaser';

const currentScreen = ref(0);
const isScrolling   = ref(false);

// 计算容器的 translateY，实现滚动切换
const containerStyle = computed(() => ({
  transform: `translateY(-${currentScreen.value * 100}vh)`,
}));

// 处理滚轮事件
const handleWheel = (event) => {
  if (isScrolling.value) return;

  const delta = event.deltaY;
  if (delta > 0 && currentScreen.value === 0) {
    goToScreen(1);
  } else if (delta < 0 && currentScreen.value === 1) {
    goToScreen(0);
  }
};

// 切换屏幕，并加防抖
const goToScreen = (idx) => {
  if (idx === currentScreen.value) return;
  isScrolling.value = true;
  currentScreen.value = idx;
  // 与 CSS transition 时长保持一致
  setTimeout(() => {
    isScrolling.value = false;
  }, 800);
};

// Phaser 容器引用
const screen0 = ref(null);
const screen1 = ref(null);

onMounted(() => {
  const commonConfig = {
    type: Phaser.AUTO,
    width: 800,
    height: 600,
    physics: { default: 'arcade' },
  };

  // 第一个 Phaser 实例
  new Phaser.Game({
    ...commonConfig,
    parent: screen0.value,
    scene: {
      preload() {
        this.load.image('logo', 'https://phaser.io/images/img.png');
      },
      create() {
        const logo = this.add.image(400, 300, 'logo');
        this.tweens.add({
          targets: logo,
          y: 200,
          duration: 1500,
          yoyo: true,
          loop: -1,
        });
      }
    }
  });

  // 第二个 Phaser 实例
  new Phaser.Game({
    ...commonConfig,
    parent: screen1.value,
    scene: {
      preload() {
        this.load.image('phaser', 'https://phaser.io/images/open-graph.png');
      },
      create() {
        const img = this.add.image(400, 300, 'phaser');
        this.tweens.add({
          targets: img,
          angle: 360,
          duration: 3000,
          loop: -1,
        });
      }
    }
  });
});
</script>

<style scoped>
.screen-wrapper {
  width: 100%;
  height: 100vh;
  overflow: hidden;
  position: relative;
}

.screens {
  width: 100%;
  height: 200vh;       /* 两个视口叠加 */
  transition: transform 0.8s ease;
}

.screen {
  width: 100%;
  height: 100vh;
}
</style>
