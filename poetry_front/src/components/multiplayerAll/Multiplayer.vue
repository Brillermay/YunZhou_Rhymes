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

const buffs = [
  { key: 'buff1', src: new URL('../../assets/cards/1.png', import.meta.url).href },
  { key: 'buff2', src: new URL('../../assets/cards/2.png', import.meta.url).href },
  { key: 'buff3', src: new URL('../../assets/cards/3.png', import.meta.url).href },
  { key: 'buff4', src: new URL('../../assets/cards/4.png', import.meta.url).href },
  { key: 'buff5', src: new URL('../../assets/cards/5.png', import.meta.url).href },
]

const gameState = ref({
  // 己方角色状态
  ally: {
    health: 100,
    maxHealth: 100,
    armor: 80,
    maxArmor: 100,
    effects: ['buff1', 'buff2'], // 状态效果数组
  },

  // 敌方角色状态
  enemy: {
    health: 100,
    maxHealth: 100,
    armor: 80,
    maxArmor: 100,
    effects: ['buff3', 'buff4'], // 状态效果数组
  },

  // 卡牌网格 3*4，初始化为全是 'cardBack'
  cardGrid: Array(4).fill(null).map(() => Array(3).fill('cardBack'))
});

//更新卡牌
const updateCard = (row, col, cardType) => {
  gameState.value.cardGrid[row][col] = cardType;
  // 这里可以添加更新 Phaser 显示的逻辑
};

//更新血条护甲
const updateStatus = (isAlly, newHealth, newArmor) => {
  if (isAlly) {
    gameState.value.ally.health = newHealth;
    gameState.value.ally.armor = newArmor;
  } else {
    gameState.value.enemy.health = newHealth;
    gameState.value.enemy.armor = newArmor;
  }
  // 注意：这里需要配合 Phaser 的场景更新机制来更新显示
};

const updateEffects = (isAlly, effects) => {
  if (isAlly) {
    gameState.value.ally.effects = effects;
  } else {
    gameState.value.enemy.effects = effects;
  }
  // 注意：这里需要配合 Phaser 的场景更新机制来更新显示
};

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
        graphics.lineStyle(2, 0xC5A880); // 柔和古金边框
        graphics.fillStyle(0x7D1D29); // 深酒红背景
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

        // 加载状态效果图片
        buffs.forEach(buff => {
          this.load.image(buff.key, buff.src);
        });
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
          0xC5A880  // 淡雅米褐色
        ).setOrigin(0.5, 0.5);

        // 创建牌桌内部（米色）
        const tableInner = this.add.rectangle(
          centerX,
          centerY,
          width - 100, // 与外框保持20px的间距
          height - 100,
          0xF5EBE0  // 温暖米色
        ).setOrigin(0.5, 0.5);

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
          switch (col) {
            case 0:
              columnColor = 0xA05252; // 红色
              break;
            case 1:
              columnColor = 0x6A8A9E; // 蓝色
              break;
            case 2:
              columnColor = 0x6E8B3D; // 绿色
              break;
          }

          // 创建列背景
          const columnX = startX - 20 + (col * (slotWidth + horizontalGap));
          const columnWidth = slotWidth + 40;
          const columnHeight = totalHeight + 40;

          this.add.rectangle(
            columnX,
            startY - 20,
            columnWidth,
            columnHeight,
            columnColor
          ).setOrigin(0, 0).setAlpha(0.4); // 设置半透明
        }

        // 创建卡槽网格
        for (let row = 0; row < 4; row++) {
          for (let col = 0; col < 3; col++) {
            const x = startX + (col * (slotWidth + horizontalGap));
            const y = startY + (row * (slotHeight + verticalGap));

            // 根据 gameState 中的数据创建卡牌
            const cardType = gameState.value.cardGrid[row][col];

            // 创建卡牌图像
            const card = this.add.image(x, y, cardType)
              .setOrigin(0, 0);

            // 添加互动效果
            card.setInteractive()
              .on('pointerover', () => {
                card.setTint(0xffff00);
              })
              .on('pointerout', () => {
                card.clearTint();
              })
              .on('pointerdown', () => {
                // 可以在这里添加点击事件，比如更新 gameState
                console.log(`Clicked card at row ${row}, col ${col}`);
              });
          }
        }

        // 创建中央分界线
        const dividerLine = this.add.rectangle(60, centerY, width - 120, 4, 0xC5A880)
          .setOrigin(0, 0.5)
          .setAlpha(0.5); // 降低分界线透明度使其不那么显眼



        // 2. 创建己方单位（左下角）
        const allyAvatarY = height - 100;
        const allyBarX = 250;

        // 创建己方头像
        const allyAvatar = this.add.circle(100, allyAvatarY, 40, 0x4A5568);

        // 创建己方血条和护甲条
        // 己方血条和护甲条
        const allyHealthWidth = (gameState.value.ally.health / gameState.value.ally.maxHealth) * 200;
        const allyArmorWidth = (gameState.value.ally.armor / gameState.value.ally.maxArmor) * 200;

        const allyHealthBar = this.add.rectangle(allyBarX, allyAvatarY - 25, allyHealthWidth, 30, 0x38A169);
        const allyArmorBar = this.add.rectangle(allyBarX, allyAvatarY + 25, allyArmorWidth, 30, 0x3182CE);
        // 创建己方状态栏
        const allyStatusBarY = allyAvatarY - 80;
        const allyStatusBar = this.add.rectangle(
          allyBarX,
          allyStatusBarY,
          400,
          60,
          0x2D3436
        ).setOrigin(0.5, 0.5);

        // 添加己方状态栏边框
        this.add.rectangle(
          allyBarX,
          allyStatusBarY,
          400,
          60,
          0xC5A880
        ).setOrigin(0.5, 0.5)
          .setStrokeStyle(1, 0xC5A880);

        // 3. 创建敌方单位（右上角）
        const enemyAvatarY = 100;
        const enemyBarX = width - 250;

        // 创建敌方头像
        const enemyAvatar = this.add.circle(width - 100, enemyAvatarY, 40, 0xE53E3E);

        // 敌方血条和护甲条
        const enemyHealthWidth = (gameState.value.enemy.health / gameState.value.enemy.maxHealth) * 200;
        const enemyArmorWidth = (gameState.value.enemy.armor / gameState.value.enemy.maxArmor) * 200;

        const enemyHealthBar = this.add.rectangle(enemyBarX, enemyAvatarY - 25, enemyHealthWidth, 30, 0x38A169);
        const enemyArmorBar = this.add.rectangle(enemyBarX, enemyAvatarY + 25, enemyArmorWidth, 30, 0x3182CE);

        // 创建敌方状态栏
        const enemyStatusBarY = enemyAvatarY + 80;
        const enemyStatusBar = this.add.rectangle(
          enemyBarX,
          enemyStatusBarY,
          400,
          60,
          0x2D3436
        ).setOrigin(0.5, 0.5);

        // 添加敌方状态栏边框
        this.add.rectangle(
          enemyBarX,
          enemyStatusBarY,
          400,
          60,
          0xC5A880
        ).setOrigin(0.5, 0.5)
          .setStrokeStyle(1, 0xC5A880);

        // 4. 添加所有文本
        // 己方文本显示
        this.add.text(allyBarX, allyAvatarY - 25, `HP: ${gameState.value.ally.health}%`, {
          fontSize: '16px',
          color: '#ffffff',
        }).setOrigin(0.5);

        this.add.text(allyBarX, allyAvatarY + 25, `Armor: ${gameState.value.ally.armor}%`, {
          fontSize: '16px',
          color: '#ffffff',
        }).setOrigin(0.5);

        this.add.text(allyBarX - 180, allyStatusBarY, '状态效果', {
          fontSize: '14px',
          color: '#ffffff',
        }).setOrigin(0, 0.5);

        // 敌方文本显示
        this.add.text(enemyBarX, enemyAvatarY - 25, `HP: ${gameState.value.enemy.health}%`, {
          fontSize: '16px',
          color: '#ffffff',
        }).setOrigin(0.5);

        this.add.text(enemyBarX, enemyAvatarY + 25, `Armor: ${gameState.value.enemy.armor}%`, {
          fontSize: '16px',
          color: '#ffffff',
        }).setOrigin(0.5);

        this.add.text(enemyBarX - 180, enemyStatusBarY, '状态效果', {
          fontSize: '14px',
          color: '#ffffff',
        }).setOrigin(0, 0.5);

        // 渲染状态效果的函数
        const renderEffects = (effects, x, y, isAlly = true) => {
          const spacing = 60; // 图标之间的间距
          const iconSize = 50; // 图标大小

          effects.forEach((effectKey, index) => {
            // 查找对应的 buff 图片
            const buff = buffs.find(b => b.key === effectKey);
            if (buff) {
              const iconX = isAlly ? x + (index * spacing)+100 : x - (index * spacing)+150;
              const icon = this.add.image(iconX, y, buff.key)
                .setDisplaySize(iconSize, iconSize)
                .setOrigin(0.5, 0.5);

              // 添加鼠标悬停效果
              icon.setInteractive()
                .on('pointerover', () => {
                  icon.setScale(1.2);
                })
                .on('pointerout', () => {
                  icon.setScale(1);
                });
            }
          });
        };

        // 渲染己方状态效果
        renderEffects(
          gameState.value.ally.effects,
          allyBarX - 160, // 状态栏文字右侧
          allyStatusBarY,
          true
        );

        // 渲染敌方状态效果
        renderEffects(
          gameState.value.enemy.effects,
          enemyBarX - 160,
          enemyStatusBarY,
          false
        );

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
  height: 200vh;
  /* 两个视口叠加 */
  transition: transform 0.8s ease;
}

.screen {
  width: 100%;
  height: 100vh;
}
</style>
