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
const isScrolling = ref(false);

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
    width: '100%',
    height: '100%',
    physics: { default: 'arcade' },
  };

  // 第一个 Phaser 实例
  new Phaser.Game({
    ...commonConfig,
    parent: screen0.value,
    scene: {
      preload() {
        // 创建一个纹理生成器来绘制卡牌背面
        const graphics = this.add.graphics();
        
        // 绘制卡牌背面的花纹
        graphics.lineStyle(2, 0xd4af37); // 古金色边框
        graphics.fillStyle(0x800020); // 酒红色背景
        graphics.fillRect(0, 0, 100, 140);
        graphics.strokeRect(0, 0, 100, 140);
        
        // 添加一些装饰图案
        graphics.lineStyle(1, 0xffd700);
        graphics.strokeRect(10, 10, 80, 120);
        graphics.beginPath();
        graphics.arc(50, 70, 30, 0, Math.PI * 2);
        graphics.strokePath();
        
        // 将绘制的图形生成为纹理
        graphics.generateTexture('cardBack', 100, 140);
        graphics.destroy();
      },
    create() {
      // 获取游戏画布的中心点和尺寸
      const width = this.cameras.main.width;
      const height = this.cameras.main.height;
      const centerX = width / 2;
      const centerY = height / 2;

      // 创建牌桌外边框（浅褐色）
      const tableFrame = this.add.rectangle(
        centerX, 
        centerY, 
        width - 60,  // 左右各留30px边距，比原来的100px更宽
        height - 60, // 上下各留30px边距，比原来的100px更宽
        0xD4C4A8  // 淡雅米褐色
      ).setOrigin(0.5, 0.5);

      // 创建牌桌内部（米色）
      const tableInner = this.add.rectangle(
        centerX, 
        centerY, 
        width - 100, // 与外框保持20px的间距
        height - 100,
        0xF5E6D3  // 温暖米色
      ).setOrigin(0.5, 0.5);

      // 调整装饰边角的位置
      const padding = 30; // 将padding调整为与新的边框对应
      const cornerRadius = 15; // 稍微减小圆角半径
      
      // 绘制四个角的装饰
      const graphics = this.add.graphics();
      graphics.lineStyle(3, 0xd4af37); // 古金色装饰线
      // 左上角
      graphics.beginPath();
      graphics.arc(padding + cornerRadius, padding + cornerRadius, cornerRadius, Math.PI, Math.PI * 1.5);
      graphics.strokePath();
      // 右上角
      graphics.beginPath();
      graphics.arc(width - padding - cornerRadius, padding + cornerRadius, cornerRadius, Math.PI * 1.5, 0);
      graphics.strokePath();
      // 左下角
      graphics.beginPath();
      graphics.arc(padding + cornerRadius, height - padding - cornerRadius, cornerRadius, Math.PI * 0.5, Math.PI);
      graphics.strokePath();
      // 右下角
      graphics.beginPath();
      graphics.arc(width - padding - cornerRadius, height - padding - cornerRadius, cornerRadius, 0, Math.PI * 0.5);
      graphics.strokePath();


        
      // 设置卡槽的尺寸和间距
      const slotWidth = 100;
      const slotHeight = 140;
      const horizontalGap = 60;
      const verticalGap = 20;

      // 计算整个卡槽区域的尺寸
      const totalWidth = (slotWidth * 3) + (horizontalGap * 2);
      const totalHeight = (slotHeight * 4) + (verticalGap * 3);

      // 计算起始位置（左上角第一个卡槽的位置）
      const startX = centerX - (totalWidth / 2);
      const startY = (height - totalHeight) / 2;

      for (let col = 0; col < 3; col++) {
        let columnColor;
        switch(col) {
          case 0:
            columnColor = 0x8b3a3a; // 红色
            break;
          case 1:
            columnColor = 0x4a708b; // 蓝色
            break;
          case 2:
            columnColor = 0x556b2f; // 绿色
            break;
        }

        // 创建列背景
        const columnX = startX-20 + (col * (slotWidth + horizontalGap));
        const columnWidth = slotWidth+40;
        const columnHeight = totalHeight+40;
        
        this.add.rectangle(
          columnX, 
          startY-20, 
          columnWidth, 
          columnHeight, 
          columnColor
        ).setOrigin(0, 0).setAlpha(0.6); // 设置半透明
      }

      // 创建卡槽网格
      for (let row = 0; row < 4; row++) {
        for (let col = 0; col < 3; col++) {
          const x = startX + (col * (slotWidth + horizontalGap));
          const y = startY + (row * (slotHeight + verticalGap));

          // 创建卡牌背面
          const cardBack = this.add.image(x, y, 'cardBack')
            .setOrigin(0, 0);

          // 添加互动效果
          cardBack.setInteractive()
            .on('pointerover', () => {
              cardBack.setTint(0xffff00);
            })
            .on('pointerout', () => {
              cardBack.clearTint();
            });
        }
      }
      
      // 创建中央分界线
      const dividerLine = this.add.rectangle(60, centerY, width - 120, 4, 0xd4af37)
        .setOrigin(0, 0.5)
        .setAlpha(0.5); // 降低分界线透明度使其不那么显眼
    },
  }, 
  });

  // 第二个 Phaser 实例
  new Phaser.Game({
    ...commonConfig,
    parent: screen1.value,
    scene: {
      preload() {
        this.load.image('test', 'https://phaser.io/images/img.png');
      },
      create() {
        this.add.image(400, 300, 'test');
      },
    },
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
