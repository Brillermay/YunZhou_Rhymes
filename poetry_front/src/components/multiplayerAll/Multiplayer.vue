<template>
  <div class="screen-wrapper" @wheel.passive.prevent="handleWheel">
    <div class="screens" :style="containerStyle">
      <!-- 第一个游戏页面 -->
      <div class="screen" ref="screen0"></div>
      <!-- 第二个游戏页面 -->
      <div class="screen" ref="screen1">
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import Phaser from 'phaser';

//-----------------------------------------
let buySlot1Animating = false
let buySlot1OriginalX

// 添加角色选择相关的响应式变量
const selectedPoet = ref('libai') // 默认李白，可以通过路由参数或props传入


const isStackingMode = ref(false)
const gameState = ref({ gold: 100 })

// 更新金币数量的函数
const updateGold = (amount) => {
  gameState.value.gold += amount
  if (gameState.value.gold < 0) gameState.value.gold = 0
  console.log('当前金币:', gameState.value.gold)
}

// 卡牌图片资源列表
const cardImages = [
  { key: 'card_pack_poem', src: new URL('../../assets/cards/诗意卡包(2).png', import.meta.url).href },
  { key: 'card_pack_poet', src: new URL('../../assets/cards/诗人卡包(1).png', import.meta.url).href },
  { key: 'card_worker', src: new URL('../../assets/cards/书生.png', import.meta.url).href },
  { key: 'factory', src: new URL('../../assets/cards/工厂/书斋.png', import.meta.url).href },
  { key: 'unknown_card', src: new URL('../../assets/cards/未知卡片.png', import.meta.url).href },

  { key: 'baijuyi', src: new URL('../../assets/cards/诗人/白居易.png', import.meta.url).href },
  { key: 'dufu', src: new URL('../../assets/cards/诗人/杜甫.png', import.meta.url).href },
  { key: 'libai', src: new URL('../../assets/cards/诗人/李白.png', import.meta.url).href },
  { key: 'lishangyin', src: new URL('../../assets/cards/诗人/李商隐.png', import.meta.url).href },
  { key: 'liuyuxi', src: new URL('../../assets/cards/诗人/刘禹锡.png', import.meta.url).href },
  { key: 'luyou', src: new URL('../../assets/cards/诗人/陆游.png', import.meta.url).href },
  { key: 'sushi', src: new URL('../../assets/cards/诗人/苏轼.png', import.meta.url).href },
  { key: 'taoyuanming', src: new URL('../../assets/cards/诗人/陶渊明.png', import.meta.url).href },
  { key: 'wanganshi', src: new URL('../../assets/cards/诗人/王安石.png', import.meta.url).href },
  { key: 'wangwei', src: new URL('../../assets/cards/诗人/王维.png', import.meta.url).href },
  { key: 'xinqiji', src: new URL('../../assets/cards/诗人/辛弃疾.png', import.meta.url).href },
  { key: 'yanshu', src: new URL('../../assets/cards/诗人/晏殊.png', import.meta.url).href },

  { key: 'love', src: new URL('../../assets/cards/诗意/爱情.png', import.meta.url).href },
  { key: 'sad', src: new URL('../../assets/cards/诗意/悲.png', import.meta.url).href },
  { key: 'spring', src: new URL('../../assets/cards/诗意/春.png', import.meta.url).href },
  { key: 'danbo', src: new URL('../../assets/cards/诗意/淡泊.png', import.meta.url).href },
  { key: 'home', src: new URL('../../assets/cards/诗意/故乡.png', import.meta.url).href },
  { key: 'yellowriver', src: new URL('../../assets/cards/诗意/黄河.png', import.meta.url).href },
  { key: 'fire', src: new URL('../../assets/cards/诗意/火.png', import.meta.url).href },
  { key: 'wine', src: new URL('../../assets/cards/诗意/酒.png', import.meta.url).href },
  { key: 'byebye', src: new URL('../../assets/cards/诗意/离别.png', import.meta.url).href },
  { key: 'liu', src: new URL('../../assets/cards/诗意/柳.png', import.meta.url).href },
  { key: 'bird', src: new URL('../../assets/cards/诗意/鸟.png', import.meta.url).href },
  { key: 'autumn', src: new URL('../../assets/cards/诗意/秋.png', import.meta.url).href },
  { key: 'sun', src: new URL('../../assets/cards/诗意/日.png', import.meta.url).href },
  { key: 'mountain', src: new URL('../../assets/cards/诗意/山.png', import.meta.url).href },
  { key: 'water', src: new URL('../../assets/cards/诗意/水.png', import.meta.url).href },
  { key: 'missing', src: new URL('../../assets/cards/诗意/思念.png', import.meta.url).href },
  { key: 'flower', src: new URL('../../assets/cards/诗意/桃花.png', import.meta.url).href },
  { key: 'goose', src: new URL('../../assets/cards/诗意/雁.png', import.meta.url).href },
  { key: 'friend', src: new URL('../../assets/cards/诗意/友情.png', import.meta.url).href },
  { key: 'rain', src: new URL('../../assets/cards/诗意/雨.png', import.meta.url).href },
  { key: 'moon', src: new URL('../../assets/cards/诗意/月.png', import.meta.url).href },
  { key: 'war', src: new URL('../../assets/cards/诗意/战争.png', import.meta.url).href },
  { key: 'longriver', src: new URL('../../assets/cards/诗意/长江.png', import.meta.url).href },
  { key: 'bamboo', src: new URL('../../assets/cards/诗意/竹.png', import.meta.url).href },
  { key: 'zhuangzhinanchou', src: new URL('../../assets/cards/诗意/壮志难酬.png', import.meta.url).href },
  { key: 'nature', src: new URL('../../assets/cards/诗意/自然.png', import.meta.url).href },

  { key: 'jiangjinjiu', src: new URL('../../assets/cards/诗词/将进酒.png', import.meta.url).href },
  { key: 'shudaonan', src: new URL('../../assets/cards/诗词/蜀道难.png', import.meta.url).href },
  { key: 'xinglunan', src: new URL('../../assets/cards/诗词/行路难.png', import.meta.url).href },
  { key: 'huanghelousongmenghaoranzhiguangling', src: new URL('../../assets/cards/诗词/黄鹤楼送孟浩然之广陵.png', import.meta.url).href },
  
  { key: 'shizhisaishang', src: new URL('../../assets/cards/诗词/使至塞上.png', import.meta.url).href },
  { key: 'xiangsi', src: new URL('../../assets/cards/诗词/相思.png', import.meta.url).href },
  { key: 'shanjuqiuming', src: new URL('../../assets/cards/诗词/山居秋暝.png', import.meta.url).href },
  { key: 'zhuliguan', src: new URL('../../assets/cards/诗词/竹里馆.png', import.meta.url).href },
  
  { key: 'shuidiaogetou_mingyuejishiyou', src: new URL('../../assets/cards/诗词/水调歌头·明月几时有.png', import.meta.url).href },
  { key: 'chibifu', src: new URL('../../assets/cards/诗词/赤壁赋.png', import.meta.url).href },
  { key: 'jichengtansiyeyou', src: new URL('../../assets/cards/诗词/记承天寺夜游.png', import.meta.url).href },
  { key: 'dingfengbo_motingchuanlindayesheng', src: new URL('../../assets/cards/诗词/定风波·莫听穿林打叶声.png', import.meta.url).href },
  
]

//合成表
const recipeMapping = {
  'autumn_bird': 'goose',
  'bird_bird': 'friend',
  'fire_fire': 'war',
  'fire_moon': 'sun',
  'friend_missing': 'love',
  'home_byebye': 'missing',
  'mountain_water': 'nature',
  'nature_spring': 'flower',
  'nature_water': 'bamboo',
  'rain_rain': 'yellowriver',
  'spring_water': 'liu',
  'bamboo_water': 'danbo',
  'fire_water': 'wine',
  'water_water': 'rain',
  'autumn_wine': 'zhuangzhinanchou',
  'water_yellowriver': 'longriver',
  'moon_moon': 'home',
  'home_moon': 'byebye',
  'autumn_autumn': 'sad',
};
const craftingRecipes = {
  'bird_libai_mountain': 'shudaonan',
  'byebye_libai_longriver': 'huanghelousongmenghaoranzhiguangling',
  'libai_mountain_wine': 'xinglunan',
  'libai_wine_yellowriver': 'jiangjinjiu',

  'goose_wangwei_yellowriver': 'shizhisaishang',
  'missing_spring_wangwei': 'xiangsi',
  'autumn_rain_wangwei': 'shanjuqiuming',
  'bamboo_danbo_wangwei': 'zhuliguan',

  'moon_sushi_water': 'shuidiaogetou_mingyuejishiyou',
  'fire_mountain_sushi': 'chibifu',
  'moon_sushi_zhuangzhinanchou': 'jichengtansiyeyou',
  'bamboo_rain_sushi': 'dingfengbo_motingchuanlindayesheng'

};

// 检查两张卡是否可以合成
const checkRecipe = (card1Type, card2Type) => {
  // 确保类型按字母顺序排序以保持一致性
  const types = [card1Type, card2Type].sort()
  const recipeKey = types.join('_')
  return recipeMapping[recipeKey]
}

// 检查三张卡是否可以合成
const checkCrafting = (cards) => {
  if (cards.length !== 3) return null;
  const types = cards.map(card => card.getData('type')).sort()
  const recipeKey = types.join('_')
  console.log('Crafting Recipe Key:', recipeKey); // 调试信息
  return craftingRecipes[recipeKey]
}

// 卡牌价格
const cardPrices = {
  card_pack_poem: 10,
                         
  love: 7,
  sad: 2,
  spring: 1,
  danbo: 4,
  home: 2,
  yellowriver: 4,
  fire: 1,
  wine: 2,
  byebye: 3,
  liu: 2,
  bird: 1,
  autumn: 1,
  sun: 2,
  mountain: 1,
  water: 1,
  missing: 5,
  flower: 3,
  goose: 2,
  friend: 2,
  rain: 2,
  moon: 1,
  war: 2,
  longriver: 5,
  bamboo: 3,
  zhuangzhinanchou: 3,
  nature: 2,
};

let lastCoinValue = 100
const coins = ref(100) // 初始金币数量

// 购买诗意卡包
const handleBuyPack = () => {
  const packPrice = 10
  if (coins.value >= packPrice) {
    coins.value -= packPrice

    updateGold(-packPrice)

    const scene = game.scene.scenes[0]

    // 在随机位置创建卡包
    const x = Math.random() * (scene.scale.width - 100) + 50
    const y = Math.random() * (scene.scale.height - 140 - 180) + 250

    const cardPack = scene.physics.add.image(x, y, 'card_pack_poem')
      .setDisplaySize(100, 140)
      .setInteractive({ cursor: 'pointer', useHandCursor: true, draggable: true })
      .setCollideWorldBounds(true)
      .setBounce(0.8)
      .setData('clickCount', 0)
      .setData('type', 'card_pack_poem')
      .setData('isDragging', false)
      .setData('pointerDown', false)
      .setData('dragStartX', 0)
      .setData('dragStartY', 0)

    scene.input.setDraggable(cardPack)

    // 添加指针按下事件
    cardPack.on('pointerdown', (pointer) => {
      cardPack.setData('pointerDown', true)
      cardPack.setData('dragStartX', pointer.x)
      cardPack.setData('dragStartY', pointer.y)
    })

    // 添加拖动开始事件
    cardPack.on('dragstart', () => {
      cardPack.setData('isDragging', true)
    })

    // 添加拖动结束事件
    cardPack.on('dragend', () => {
      if (cardPack.getData('isDragging')) {
        setTimeout(() => {
          cardPack.setData('isDragging', false)
          cardPack.setData('pointerDown', false)
        }, 100)
      }
    })

    // 添加指针抬起事件
    cardPack.on('pointerup', (pointer) => {
      const isDragging = cardPack.getData('isDragging')
      const startX = cardPack.getData('dragStartX')
      const startY = cardPack.getData('dragStartY')
      const distance = Phaser.Math.Distance.Between(startX, startY, pointer.x, pointer.y)

      // 如果移动距离小于5像素且没有处于拖动状态，则认为是点击
      if (distance < 5 && !isDragging) {
        const clickCount = cardPack.getData('clickCount')

        if (clickCount === 0) {
          // 第一次点击：添加震动效果
          scene.tweens.add({
            targets: cardPack,
            x: cardPack.x + 5,
            duration: 50,
            yoyo: true,
            repeat: 3
          })
          cardPack.setData('clickCount', 1)
        } else {
          // 第二次点击：生成随机卡片并销毁卡包
          const allCards = ['love', 'sad', 'spring', 'danbo', 'home', 'yellowriver', 'fire', 'wine',
           'byebye', 'liu', 'bird', 'autumn', 'sun', 'mountain', 'water', 'missing', 'flower', 
           'goose', 'friend', 'rain', 'moon', 'war', 'longriver', 'bamboo', 'zhuangzhinanchou', 'nature']
          const numCards = 5

          // 创建闪光效果
          const flash = scene.add.sprite(cardPack.x, cardPack.y, 'card_pack_poem')
            .setScale(0.1)
            .setAlpha(0.8)
            .setTint(0xffffff)
            .setBlendMode(Phaser.BlendModes.ADD)

          scene.tweens.add({
            targets: flash,
            alpha: 0,
            scale: 1,
            duration: 500,
            onComplete: () => flash.destroy()
          })

          // 生成随机卡片
          for (let i = 0; i < numCards; i++) {
            const angle = (i / numCards) * Math.PI * 2
            const radius = 80
            const randomCard = allCards[Math.floor(Math.random() * allCards.length)]

            const newX = cardPack.x + Math.cos(angle) * radius
            const newY = cardPack.y + Math.sin(angle) * radius

            const card = scene.physics.add.image(cardPack.x, cardPack.y, randomCard)
              .setDisplaySize(100, 140)
              .setInteractive({ cursor: 'pointer', useHandCursor: true })
              .setCollideWorldBounds(true)
              .setBounce(0.8)
              .setData('type', randomCard)
              .setData('id', Date.now().toString() + i)

            scene.input.setDraggable(card)
            scene.cards.push(card)

            // 添加卡片出现动画
            scene.tweens.add({
              targets: card,
              x: newX,
              y: newY,
              // scale: { from: 0.5, to: 1 },
              alpha: { from: 0.5, to: 1 },
              duration: 500,
              ease: 'Back.easeOut'
            })
          }

          // 销毁卡包
          scene.tweens.add({
            targets: cardPack,
            alpha: 0,
            scale: 0.5,
            duration: 300,
            onComplete: () => cardPack.destroy()
          })
        }
      }
      cardPack.setData('pointerDown', false)
    })
  }
}

let game = null

//-----------------------------------------
const currentScreen = ref(0);
const isScrolling = ref(false);

//buff列表
const buffs = [
  { key: 'armor_minus', src: new URL('../../assets/cards/buff/armor_minus.png', import.meta.url).href },
  { key: 'armor_plus', src: new URL('../../assets/cards/buff/armor_plus.png', import.meta.url).href },
  { key: 'attack_minus', src: new URL('../../assets/cards/buff/attack_minus.png', import.meta.url).href },
  { key: 'attack_plus', src: new URL('../../assets/cards/buff/attack_plus.png', import.meta.url).href },
  { key: 'bounce_back', src: new URL('../../assets/cards/buff/bounce_back.png', import.meta.url).href },
  { key: 'break_armor', src: new URL('../../assets/cards/buff/break_armor.png', import.meta.url).href },
  { key: 'cant_armor', src: new URL('../../assets/cards/buff/cant_armor.png', import.meta.url).href },
  { key: 'copy_armor', src: new URL('../../assets/cards/buff/copy_armor.png', import.meta.url).href },
  { key: 'gold_minus', src: new URL('../../assets/cards/buff/gold_minus.png', import.meta.url).href },
  { key: 'gold_plus', src: new URL('../../assets/cards/buff/gold_plus.png', import.meta.url).href },
  { key: 'heal', src: new URL('../../assets/cards/buff/heal.png', import.meta.url).href },
  { key: 'immune_damage_point', src: new URL('../../assets/cards/buff/immune_damage_point.png', import.meta.url).href },
  { key: 'immune_damage_time', src: new URL('../../assets/cards/buff/immune_damage_time.png', import.meta.url).href },
  { key: 'immune_debuff', src: new URL('../../assets/cards/buff/immune_debuff.png', import.meta.url).href },
  { key: 'rebound_armor', src: new URL('../../assets/cards/buff/rebound_armor.png', import.meta.url).href },
]

//对战双方游戏状态
const gameState_one = ref({
  // 己方角色状态
  ally: {
    health: 20,
    maxHealth: 20,
    armor: 10,
    maxArmor: 10,
    effects: ['rebound_armor', 'copy_armor'], // 状态效果数组
  },

  // 敌方角色状态
  enemy: {
    health: 20,
    maxHealth: 20,
    armor: 10,
    maxArmor: 10,
    effects: ['armor_plus', 'cant_armor'], // 状态效果数组
  },

  // 卡牌网格 3*4，初始化为全是 'cardBack'
  cardGrid: Array(4).fill(null).map(() => Array(3).fill('cardBack'))
});

//更新3*4卡牌展示
const updateCard = (row, col, cardType) => {
  gameState_one.value.cardGrid[row][col] = cardType;
  // 这里可以添加更新 Phaser 显示的逻辑
};

//更新血条护甲
const updateStatus = (isAlly, newHealth, newArmor) => {
  if (isAlly) {
    gameState_one.value.ally.health = newHealth;
    gameState_one.value.ally.armor = newArmor;
  } else {
    gameState_one.value.enemy.health = newHealth;
    gameState_one.value.enemy.armor = newArmor;
  }
  // 注意：这里需要配合 Phaser 的场景更新机制来更新显示
};

//更新buff
const updateEffects = (isAlly, effects) => {
  if (isAlly) {
    gameState_one.value.ally.effects = effects;
  } else {
    gameState_one.value.enemy.effects = effects;
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

  //页面初始化
  const commonConfig = {
    type: Phaser.AUTO,
    width: '100%',
    height: '100%',
    physics: { default: 'arcade' },
  };

  // 第一个 Phaser 实例：对战界面
  new Phaser.Game({
    ...commonConfig,
    parent: screen0.value,
    scene: {

      //预加载
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

        // 创建列背景
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

            // 根据 gameState_one 中的数据创建卡牌
            const cardType = gameState_one.value.cardGrid[row][col];

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
                // 可以在这里添加点击事件，比如更新 gameState_one
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

        // 己方血条和护甲条
        const allyHealthWidth = (gameState_one.value.ally.health / gameState_one.value.ally.maxHealth) * 200;
        const allyArmorWidth = (gameState_one.value.ally.armor / gameState_one.value.ally.maxArmor) * 200;
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
        const enemyHealthWidth = (gameState_one.value.enemy.health / gameState_one.value.enemy.maxHealth) * 200;
        const enemyArmorWidth = (gameState_one.value.enemy.armor / gameState_one.value.enemy.maxArmor) * 200;
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

        // 己方文本显示
        this.add.text(allyBarX, allyAvatarY - 25, `HP: ${gameState_one.value.ally.health}`, {
          fontSize: '16px',
          color: '#ffffff',
          resolution: 2,
        }).setOrigin(0.5);

        this.add.text(allyBarX, allyAvatarY + 25, `Armor: ${gameState_one.value.ally.armor}`, {
          fontSize: '16px',
          color: '#ffffff',
          resolution: 2,
        }).setOrigin(0.5);

        this.add.text(allyBarX - 180, allyStatusBarY, '状态效果', {
          fontSize: '18px',
          color: '#ffffff',
          resolution: 2,
        }).setOrigin(0, 0.5);

        // 敌方文本显示
        this.add.text(enemyBarX, enemyAvatarY - 25, `HP: ${gameState_one.value.enemy.health}`, {
          fontSize: '16px',
          color: '#ffffff',
          resolution: 2,
        }).setOrigin(0.5);

        this.add.text(enemyBarX, enemyAvatarY + 25, `Armor: ${gameState_one.value.enemy.armor}`, {
          fontSize: '16px',
          color: '#ffffff',
          resolution: 2,
        }).setOrigin(0.5);

        this.add.text(enemyBarX - 180, enemyStatusBarY, '状态效果', {
          fontSize: '18px',
          color: '#ffffff',
          resolution: 2,
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
                  //预留显示效果详情
                })
                .on('pointerout', () => {
                  //预留取消显示效果详情
                });
            }
          });
        };

        // 渲染己方状态效果
        renderEffects(
          gameState_one.value.ally.effects,
          allyBarX - 160, // 状态栏文字右侧
          allyStatusBarY,
          true
        );

        // 渲染敌方状态效果
        renderEffects(
          gameState_one.value.enemy.effects,
          enemyBarX - 160,
          enemyStatusBarY,
          false
        );

      },
    },
  });

  // 第二个 Phaser 实例
  //------------------------------
  const container = screen1.value;
  const containerWidth = container.clientWidth;
  const containerHeight = container.clientHeight;
  //------------------------------
  game = new Phaser.Game({
    type: Phaser.AUTO,
    width: containerWidth,
    height: containerHeight,
    parent: screen1.value,  // 改为 screen1.value 而不是 gameCanvas.value
    backgroundColor: '#f5efe6',
    scale: {
      mode: Phaser.Scale.RESIZE,
      autoCenter: Phaser.Scale.CENTER_BOTH
    },
    physics: {
      default: 'arcade',
      arcade: {
        gravity: { y: 0 },
        debug: false
      }
    },
    scene: {
      // 预加载资源
      preload() {
        cardImages.forEach(card => {
          this.load.image(card.key, card.src)
        })
      },
      create() {
        this.cards = []
        let cardId = 1
        const topBarHeight = 180
        const padding = 20
        const STACK_OFFSET_Y = 20 // 堆叠时卡片垂直偏移量
        const STACK_DETECTION_DISTANCE = 80 // 增加堆叠检测距离
        const STACK_SNAP_DURATION = 150 // 吸附动画持续时间

        this.shiftKey = this.input.keyboard.addKey(Phaser.Input.Keyboard.KeyCodes.SHIFT)

            // 创建顶部边栏背景，并添加交互效果
        const topBar = this.add.rectangle(0, 0, this.scale.width, topBarHeight, 0xa3916a)
          .setOrigin(0, 0)
          .setDepth(100)
          .setStrokeStyle(2, 0x8c7853) // 使用渐变色的深色部分
          .setInteractive()

        // 创建出售槽
        const sellSlot = this.add.rectangle(padding, padding, 100, 140, 0x8c7853) // 使用主题色
          .setOrigin(0, 0)
          .setDepth(101)
          .setInteractive()
          .setStrokeStyle(2, 0x6e5773) // 使用渐变色的深色部分

        // 出售槽文本
        const sellIcon = this.add.text(sellSlot.x + 50, sellSlot.y + 40, '💰', {  // y位置上移
          fontSize: '28px',  // 稍微减小字体
          resolution: 2, // 提高分辨率
          padding: { x: 2, y: 2 } // 添加内边距
        }).setOrigin(0.5).setDepth(102)

        const sellText = this.add.text(sellSlot.x + 50, sellSlot.y + 90, '出售卡牌', {  // y位置上移
          fontSize: '14px',
          resolution: 2, 
          color: '#ffffff',
          align: 'center',
          padding: { y: 5 }  // 添加垂直内边距
        }).setOrigin(0.5).setDepth(102)

        // 创建第一个购买槽 - 简化版本
        const buySlot = this.add.rectangle(padding * 2 + 100, padding, 100, 140, 0x6e5773)
          .setOrigin(0, 0)
          .setDepth(101)
          .setInteractive({ useHandCursor: true })
          .setStrokeStyle(3, 0x8c7853, 0.9)

        buySlot1OriginalX = buySlot.x
        // 购买槽文本 - 直接使用最终位置
        const buyIcon = this.add.text(padding * 2 + 100 + 50, padding + 40, '🎁', {
          fontSize: '32px',
          resolution: 2,
          padding: { x: 2, y: 2 }
        }).setOrigin(0.5).setDepth(102)

        const buyText = this.add.text(padding * 2 + 100 + 50, padding + 90 - 200, '诗意卡包\n10金币', {
          fontSize: '16px',
          resolution: 2,
          color: '#ffffff',
          align: 'center',
          fontWeight: 'bold',
          lineSpacing: 2,
          padding: { y: 5 }
        }).setOrigin(0.5).setDepth(102)

        // 直接设置最终位置，无动画
        buySlot.y = padding
        buyIcon.y = padding + 40
        buyText.y = padding + 90

        // 添加购买槽的悬浮效果
        buySlot.on('pointerover', () => {
          if (coins.value >= 10) {
            // 简单的悬浮效果 - 只改变边框颜色和透明度
            buySlot.setStrokeStyle(3, 0xffffff, 1) // 白色边框
            buySlot.setAlpha(0.9) // 轻微透明
            
            // 简单的文字轻微放大
            this.tweens.add({
              targets: [buyIcon, buyText],
              scale: 1.05,
              duration: 150,
              ease: 'Power2.easeOut'
            })
          } else {
            // 金币不足的简单提示
            buySlot.setStrokeStyle(3, 0xff5722, 1) // 警告色边框
            buySlot.setAlpha(0.8)
          }
        })

        buySlot.on('pointerout', () => {
          // 恢复原状
          buySlot.setStrokeStyle(3, 0x8c7853, 0.9)
          buySlot.setAlpha(1)
          
          this.tweens.add({
            targets: [buyIcon, buyText],
            scale: 1,
            duration: 150,
            ease: 'Power2.easeOut'
          })
        })



        // 修改Shift键监听为点击切换
        this.shiftKey = this.input.keyboard.addKey(Phaser.Input.Keyboard.KeyCodes.SHIFT)

        // 添加Shift键点击事件监听
        this.shiftKey.on('down', () => {
          // 切换模式状态
          isStackingMode.value = !isStackingMode.value
          
          const newColor = isStackingMode.value ? 0xffb74d : 0x4caf50
          const newStrokeColor = isStackingMode.value ? 0xff9800 : 0x388e3c
          const newText = isStackingMode.value ? '📚 堆叠模式' : '🔧 合成模式'
          
          // 颜色渐变动画 - 不改变位置和大小
          this.tweens.add({
            targets: modeHintBackground,
            duration: 300,
            ease: 'Power2.easeOut',
            onUpdate: function() {
              const progress = this.progress
              const currentColor = Phaser.Display.Color.Interpolate.ColorWithColor(
                Phaser.Display.Color.ValueToColor(modeHintBackground.fillColor),
                Phaser.Display.Color.ValueToColor(newColor),
                1,
                progress
              )
              modeHintBackground.setFillStyle(Phaser.Display.Color.GetColor(currentColor.r, currentColor.g, currentColor.b))
              modeHintBackground.setStrokeStyle(2, newStrokeColor, 0.5 + progress * 0.5)
            }
          })
          
          // 文字淡入淡出
          this.tweens.add({
            targets: modeHintText,
            alpha: 0,
            duration: 150,
            ease: 'Power2.easeOut',
            onComplete: () => {
              modeHintText.setText(newText)
              this.tweens.add({
                targets: modeHintText,
                alpha: 1,
                duration: 150,
                ease: 'Power2.easeOut'
              })
            }
          })
        })

        // 第一个购买槽处理函数
        const handleBuyClick = () => {
          if (coins.value >= 10) {
            this.tweens.killTweensOf([buySlot, buyIcon, buyText])
            buySlot.setScale(1)
            buyIcon.setScale(1)
            buyText.setScale(1)
            handleBuyPack()
            
            // 简单的按下反馈
            this.tweens.add({
              targets: buySlot,
              scaleX: 0.95,
              scaleY: 0.95,
              duration: 100,
              ease: 'Power2.easeOut',
              yoyo: true,
              onComplete: () => {
                buySlot.setScale(1) // 确保动画完成后重置
              }
            })
            
            // 简单的文字反馈
            this.tweens.add({
              targets: [buyIcon, buyText],
              scale: 0.9,
              duration: 100,
              ease: 'Power2.easeOut',
              yoyo: true,
              onComplete: () => {
                buyIcon.setScale(1) // 确保动画完成后重置
                buyText.setScale(1)
              }
            })
            
            // 简洁的边框闪烁
            buySlot.setStrokeStyle(3, 0xffffff)
            
            // 保留金币消费提示（这个比较实用）
            const costText = this.add.text(
              buySlot.x + 50,
              buySlot.y + 120,
              '-10',
              {
                fontSize: '18px',
                color: '#ff5722',
                fontWeight: 'bold',
                resolution: 2
              }
            ).setDepth(104).setOrigin(0.5)
            
            this.tweens.add({
              targets: costText,
              y: '-=30',
              alpha: 0,
              duration: 800,
              ease: 'Power2',
              onComplete: () => costText.destroy()
            })
            
          } else {
            if (buySlot1Animating) return
            buySlot1Animating = true

            buySlot.x = buySlot1OriginalX

            buySlot.setScale(1)
            buyIcon.setScale(1)
            buyText.setScale(1)
            // 简化的金币不足反馈
            this.tweens.add({
              targets: buySlot,
              x: buySlot1OriginalX + 3,
              scaleX: 0.95, // 添加缩放动画
              scaleY: 0.95,
              duration: 100,
              ease: 'Power2',
              yoyo: true,
              repeat: 2,
              onComplete: () => {
                buySlot.x = buySlot1OriginalX
                buySlot.setScale(1)
                buySlot1Animating = false
              }
            })
            
            this.tweens.add({
              targets: [buyIcon, buyText],
              x: '+=3',
              scale: 0.9,
              duration: 100,
              ease: 'Power2',
              yoyo: true,
              repeat: 2,
              onComplete: () => {
                buyIcon.setScale(1) // 确保回到原始缩放
                buyText.setScale(1)
              }
            })
            
            // 简单的警告提示
            const warningText = this.add.text(
              buySlot.x + 50,
              buySlot.y + 120,
              '金币不足',
              {
                fontSize: '14px',
                color: '#ff5722',
                fontWeight: 'bold',
                resolution: 2
              }
            ).setDepth(104).setOrigin(0.5)
            
            this.tweens.add({
              targets: warningText,
              y: '-=20',
              alpha: 0,
              duration: 800,
              ease: 'Power2',
              onComplete: () => warningText.destroy()
            })
            
            buySlot.setStrokeStyle(3, 0xff5722)
          }
        }

          // 为所有相关元素添加点击事件
          ;[buySlot, buyIcon, buyText].forEach(element => {
            element.on('pointerdown', handleBuyClick)
            element.on('pointerover', () => {
              buySlot.setStrokeStyle(2, 0xffffff)
            })
            element.on('pointerout', () => {
              buySlot.setStrokeStyle(2, 0x8c7853)
            })
          })

        // 创建攻击卡槽
        const attackSlot = this.add.rectangle(padding * 3 + 200, padding, 100, 140, 0x8b0000)
          .setOrigin(0, 0)
          .setDepth(101)
          .setInteractive({ dropZone: true })
          .setStrokeStyle(3, 0xff4444, 0.9)

        // 攻击槽文本和图标
        const attackIcon = this.add.text(padding * 3 + 200 + 50, padding + 40, '⚔️', {
          fontSize: '32px',
          resolution: 2,
          padding: { x: 2, y: 2 }
        }).setOrigin(0.5).setDepth(102)

        const attackText = this.add.text(padding * 3 + 200 + 50, padding + 90 , '攻击卡槽', {
          fontSize: '16px',
          resolution: 2,
          color: '#ffffff',
          align: 'center',
          fontWeight: 'bold',
          lineSpacing: 2,
          padding: { y: 5 }
        }).setOrigin(0.5).setDepth(102)


        // 创建防守卡槽
        const defenseSlot = this.add.rectangle(padding * 4 + 300, padding , 100, 140, 0x0066cc)
          .setOrigin(0, 0)
          .setDepth(101)
          .setInteractive({ dropZone: true })
          .setStrokeStyle(3, 0x4488ff, 0.9)

        const defenseIcon = this.add.text(padding * 4 + 300 + 50, padding + 40 , '🛡️', {
          fontSize: '32px',
          resolution: 2,
          padding: { x: 2, y: 2 }
        }).setOrigin(0.5).setDepth(102)

        const defenseText = this.add.text(padding * 4 + 300 + 50, padding + 90 , '防守卡槽', {
          fontSize: '16px',
          resolution: 2,
          color: '#ffffff',
          align: 'center',
          fontWeight: 'bold',
          lineSpacing: 2,
          padding: { y: 5 }
        }).setOrigin(0.5).setDepth(102)

        // 创建BUFF卡槽
        const buffSlot = this.add.rectangle(padding * 5 + 400, padding , 100, 140, 0x228b22)
          .setOrigin(0, 0)
          .setDepth(101)
          .setInteractive({ dropZone: true })
          .setStrokeStyle(3, 0x44cc44, 0.9)

        const buffIcon = this.add.text(padding * 5 + 400 + 50, padding + 40 , '✨', {
          fontSize: '32px',
          resolution: 2,
          padding: { x: 2, y: 2 }
        }).setOrigin(0.5).setDepth(102)

        const buffText = this.add.text(padding * 5 + 400 + 50, padding + 90 , 'BUFF卡槽', {
          fontSize: '16px',
          color: '#ffffff',
          align: 'center',
          resolution: 1.5,
          fontWeight: 'bold',
          lineSpacing: 2,
          padding: { y: 5 }
        }).setOrigin(0.5).setDepth(102)

        // 攻击卡槽处理函数
        const handleAttackSlot = (card) => {
          console.log('卡片放入攻击槽:', card.getData('type'))
          // 在这里添加攻击逻辑
          
          

          attackSlot.y = padding
          attackIcon.y = padding + 40
          attackText.y = padding + 90

          // 销毁卡片
          card.destroy()
          this.cards = this.cards.filter(c => c !== card)
        }

        // 防守卡槽处理函数
        const handleDefenseSlot = (card) => {
          console.log('卡片放入防守槽:', card.getData('type'))
          // 在这里添加防守逻辑
          
          
          defenseSlot.y = padding
          defenseIcon.y = padding + 40
          defenseText.y = padding + 90
          
          card.destroy()
          this.cards = this.cards.filter(c => c !== card)
        }

        // BUFF卡槽处理函数
        const handleBuffSlot = (card) => {
          console.log('卡片放入BUFF槽:', card.getData('type'))
          // 在这里添加BUFF逻辑
          

          
          buffSlot.y = padding
          buffIcon.y = padding + 40
          buffText.y = padding + 90
          
          card.destroy()
          this.cards = this.cards.filter(c => c !== card)
        }

        // 创建合成台背景 - 直接设置最终位置
        const finalCraftingX = padding * 6 + 500 + padding;
        const craftingStation = this.add.rectangle(
          finalCraftingX, // 直接设置最终位置，不需要动画
          padding, 
          400, 
          140, 
          0xa3916a, 
          1 
        )
          .setOrigin(0, 0)
          .setDepth(100)
          .setStrokeStyle(2, 0xa3916a);

        // 创建四个合成槽
        const craftingSlots = []
        const slotWidth = 80
        const cardWidth = 100; 
        const cardHeight = 140; 
        const slotSpacing = 20
        const slotTypes = [null, null, null, null] 

        for (let i = 0; i < 4; i++) {
          // 直接使用最终位置，删除动画
          const finalX = finalCraftingX + slotSpacing + i * (cardWidth + slotSpacing);
          const y = craftingStation.y + (craftingStation.height - cardHeight) / 2; 

          const slot = this.add.rectangle(finalX, y, cardWidth, cardHeight, 0x8c7853) 
            .setOrigin(0, 0)
            .setDepth(101)
            .setStrokeStyle(1, 0xffffff)
            .setData('type', slotTypes[i])
            .setData('occupied', false)
            .setData('card', null)
            .setInteractive({ dropZone: true });

          craftingSlots.push(slot);

          // 添加槽位标识 - 直接设置最终位置
          let operatorText = null;
          if (i < 3) {
            operatorText = this.add.text(
              finalX + cardWidth + 5, 
              y + cardHeight / 2, 
              i < 2 ? '+' : '=', 
              {
                fontSize: '24px',
                resolution: 5,
                color: '#ffffff'
              }
            )
              .setOrigin(0, 0.5)
              .setDepth(101);
          }

          // 如果是第3个槽位（索引2），直接放置选择的诗人
          if (i === 2) {
            const poetCard = this.physics.add.image(
              finalX + cardWidth / 2,
              y + cardHeight / 2,
              selectedPoet.value
            )
              .setDisplaySize(100, 140)
              .setDepth(102)
              .setData('type', selectedPoet.value)
              .setData('id', 'selected_poet')
              .setData('isFixed', true);

            slot.setData('occupied', true);
            slot.setData('card', poetCard);
          }
        }

          // 添加拖放事件
          // 修改合成槽的拖放逻辑
          this.input.on('drop', (pointer, gameObject, dropZone) => {
            const cardType = gameObject.getData('type');
            const slotType = dropZone.getData('type');

            const canPlace = (slotType === null) || 
                            (slotType === cardType) || 
                            !dropZone.getData('occupied');

            if (canPlace && !dropZone.getData('occupied')) {
              // 放置卡牌到槽位
              dropZone.setData('occupied', true);
              dropZone.setData('card', gameObject);

              // 调整卡牌位置到槽位中心
              gameObject.x = dropZone.x + dropZone.width / 2;
              gameObject.y = dropZone.y + dropZone.height / 2;
              gameObject.setDepth(102); // 确保在槽位上方

              // 检查是否可以合成
              const materials = craftingSlots.slice(0, 3)
                .map(slot => slot.getData('card'))
                .filter(Boolean);

              if (materials.length === 3) {
                console.log('Materials ready:', materials.map(card => card.getData('type')));
                const resultType = checkCrafting(materials);
                
                if (resultType) {
                  console.log('Creating result card:', resultType);
                  
                  // 创建结果卡牌
                  const resultCard = this.physics.add.image(
                    craftingSlots[3].x + craftingSlots[3].width / 2,
                    craftingSlots[3].y + craftingSlots[3].height / 2,
                    resultType
                  )
                    .setDisplaySize(100, 140)
                    .setInteractive({ cursor: 'pointer', useHandCursor: true })
                    .setCollideWorldBounds(true)
                    .setBounce(0.8)
                    .setData('type', resultType)
                    .setData('id', Date.now().toString())
                    .setDepth(102); // 确保可见

                  this.input.setDraggable(resultCard);
                  this.cards.push(resultCard);

                  // 添加合成效果
                  const flash = this.add.sprite(resultCard.x, resultCard.y, resultType)
                    .setScale(0.1)
                    .setAlpha(0.8)
                    .setTint(0xffd700)
                    .setBlendMode(Phaser.BlendModes.ADD)
                    .setDepth(103);

                  this.tweens.add({
                    targets: flash,
                    alpha: 0,
                    scale: 1,
                    duration: 500,
                    onComplete: () => flash.destroy()
                  });

                  // 清空材料槽，但保留诗人槽（索引2）
                  materials.forEach(card => {
                    // 检查是否是固定的诗人卡片
                    if (!card.getData('isFixed')) {
                      // 从cards数组中移除
                      this.cards = this.cards.filter(c => c !== card);
                      card.destroy();
                    }
                  });

                  // 只清空非诗人槽
                  craftingSlots.forEach((slot, index) => {
                    if (index !== 2) { // 不清空诗人槽（索引2）
                      slot.setData('occupied', false);
                      slot.setData('card', null);
                    }
                  });
                  
                  console.log('Crafting completed successfully!');
                } else {
                  console.log('No matching recipe found for materials:', materials.map(card => card.getData('type')));
                }
              }
            } else {
              console.log('Cannot place card:', cardType, 'in slot:', slotType);
            }
          });


        // 对应地修改金币文本的深度值
        const coinDisplay = this.add.text(
          this.scale.width - padding - 10, 
          padding + 20, 
          `💰 ${coins.value}`, 
          {
            fontSize: '24px',
            resolution: 2,
            color: '#ffffff'
          }
        )
          .setOrigin(1, 0.5)
          .setDepth(101); 

        // 添加模式提示背景框
        const modeHintBackground = this.add.rectangle(
          this.scale.width - padding,
          padding + 55, // 在金币显示下方
          100, // 宽度
          40,  // 高度
          0x4caf50 // 默认绿色（合成模式）
        )
          .setOrigin(1, 0)
          .setDepth(100)
          .setAlpha(0.9)
          .setStrokeStyle(2, 0x388e3c);

        // 添加模式提示文本（优化版）
        const modeHintText = this.add.text(
          this.scale.width - padding - 10,
          padding + 75,
          '🔧 合成模式',
          {
            fontSize: '13px',
            color: '#ffffff',
            resolution: 2,
            fontWeight: 'bold'
          }
        )
          .setOrigin(1, 0.5)
          .setDepth(101);

        // 更新显示和背景（优化版）
        this.events.on('update', () => {
          const currentCoins = coins.value
          const displayText = `💰 ${currentCoins}`
          
          // 只更新右上角的金币显示
          if (coinDisplay.text !== displayText) {
            const oldValue = parseInt(coinDisplay.text.replace('💰 ', '')) || 0
            coinDisplay.setText(displayText)
            
            // 添加金币变化动画
            if (currentCoins !== oldValue && oldValue > 0) {
              const diff = currentCoins - oldValue
              
              // 创建变化提示文本
              if (diff !== 0) {
                const changeText = this.add.text(
                  coinDisplay.x - 80,
                  coinDisplay.y,
                  diff > 0 ? `+${diff}` : `${diff}`,
                  {
                    fontSize: '18px',
                    color: diff > 0 ? '#00ff00' : '#ff0000',
                    resolution: 2
                  }
                ).setDepth(1001)
                
                this.tweens.add({
                  targets: changeText,
                  y: changeText.y - 30,
                  alpha: 0,
                  duration: 1000,
                  ease: 'Power2',
                  onComplete: () => changeText.destroy()
                })
              }
              
              // 金币数字跳动效果
              this.tweens.add({
                targets: coinDisplay,
                scale: { from: 1, to: 1.1 },
                duration: 150,
                ease: 'Power2',
                yoyo: true
              })
            }
          }

          
          // 实时检查Shift键状态并更新模式显示
          const newText = isStackingMode.value ? '📚 堆叠模式' : '🔧 合成模式'
          const newColor = isStackingMode.value ? 0xffb74d : 0x4caf50
          const newStrokeColor = isStackingMode.value ? 0xff9800 : 0x388e3c
          
          // 只在模式真正改变时更新，避免每帧都执行
          if (modeHintText.text !== newText) {
            modeHintText.setText(newText)
            modeHintBackground.setFillStyle(newColor)
            modeHintBackground.setStrokeStyle(2, newStrokeColor)
            
            // 添加轻微的更新动画
            this.tweens.add({
              targets: [modeHintText, modeHintBackground],
              scale: { from: 1.1, to: 1 },
              duration: 200,
              ease: 'Back.easeOut'
            })
          }
        })

        // 添加窗口缩放事件处理
        this.scale.on('resize', (gameSize) => {
          // 更新顶部边栏
          topBar.width = gameSize.width;

          // 更新金币显示位置
          coinDisplay.x = gameSize.width - padding - 10;

          modeHintBackground.x = gameSize.width - padding ;
          modeHintText.x = gameSize.width - padding -10;
        });
        const initialCards = ['spring', 'fire', 'bird', 'autumn', 'mountain', 'water', 'moon']
        for (let i = 0; i < initialCards.length; i++) {
          const cardKey = initialCards[i]
          const card = this.physics.add.image(180 + i * 120, 250 + topBarHeight, cardKey)
            .setDisplaySize(100, 140)
            .setInteractive({ cursor: 'pointer', useHandCursor: true })
            .setCollideWorldBounds(true)
            .setBounce(0.8)
            .setData('type', cardKey)
            .setData('id', cardId++)

          this.input.setDraggable(card)
          this.cards.push(card)
        }

            // 设置游戏区域边界
        this.physics.world.setBounds(0, 0, this.scale.width, this.scale.height)

        // 添加堆叠相关属性
        this.cardStacks = [] // 用于存储卡牌堆叠组


        // 拖拽结束事件
        this.input.on('dragend', (pointer, gameObject) => {

          this.tweens.add({
            targets: gameObject,
            alpha: 1,
            duration: 200,
            ease: 'Power2'
          })

          gameObject.setAlpha(1)
          gameObject.body.moves = true

          const cardType = gameObject.getData('type')
          let isStacked = false

          // 获取当前拖动的卡片所在堆叠组
          const currentStack = this.cardStacks.find(s => s.includes(gameObject))
          const currentStackIndex = this.cardStacks.indexOf(currentStack)

          // 只有在按住 Shift 键时才执行堆叠逻辑
          if (isStackingMode.value) {
            // 查找最近的同类型卡片或堆叠组
            let closestCard = null
            let minDistance = STACK_DETECTION_DISTANCE

            // 遍历所有卡片和堆叠组
            this.cards.forEach(otherCard => {
              if (otherCard !== gameObject && 
                  otherCard.getData('type') === cardType && 
                  otherCard.active) {
                
                // 获取目标卡片所在的堆叠组
                const targetStack = this.cardStacks.find(s => s.includes(otherCard))
                
                // 如果是不同的堆叠组或者未堆叠的卡片
                if (!targetStack || targetStack !== currentStack) {
                  const distance = Phaser.Math.Distance.Between(
                    gameObject.x, gameObject.y,
                    otherCard.x, otherCard.y
                  )
                  if (distance < minDistance) {
                    minDistance = distance
                    closestCard = otherCard
                  }
                }
              }
            })

            // 如果找到可堆叠的卡片
            if (closestCard) {
              let targetStack = this.cardStacks.find(s => s.includes(closestCard))
              let cardsToAdd = [gameObject]
              
              // 如果当前卡片在堆叠组中，获取它和它上面的所有卡片
              if (currentStack) {
                const cardIndex = currentStack.indexOf(gameObject)
                cardsToAdd = currentStack.splice(cardIndex)
                
                // 如果原堆叠组只剩一张卡，移除该堆叠组
                if (currentStack.length <= 1) {
                  this.cardStacks.splice(currentStackIndex, 1)
                }
              }

              // 如果目标卡片不在任何堆叠组中，创建新的堆叠组
              if (!targetStack) {
                targetStack = [closestCard]
                this.cardStacks.push(targetStack)
              }

              // 将所有需要添加的卡片加入目标堆叠组
              cardsToAdd.forEach(card => {
                if (!targetStack.includes(card)) {
                  targetStack.push(card)
                }
              })

              // 更新堆叠位置
              const baseY = Math.min(...targetStack.map(card => card.y))
              updateStackPosition.call(this, targetStack, closestCard.x, baseY, true)
              
              isStacked = true
            }

            if (!isStacked && currentStack) {
              // 如果没有找到新的堆叠目标，更新当前堆叠组的位置
              updateStackPosition.call(this, currentStack, gameObject.x, gameObject.y, true)
            }
          }

          // 检查是否在攻击槽区域
          if (pointer.y < topBarHeight &&
              pointer.x >= attackSlot.x &&
              pointer.x <= attackSlot.x + attackSlot.width) {
            handleAttackSlot(gameObject)
            return
          }

          // 检查是否在防守槽区域
          if (pointer.y < topBarHeight &&
              pointer.x >= defenseSlot.x &&
              pointer.x <= defenseSlot.x + defenseSlot.width) {
            handleDefenseSlot(gameObject)
            return
          }

          // 检查是否在BUFF槽区域
          if (pointer.y < topBarHeight &&
              pointer.x >= buffSlot.x &&
              pointer.x <= buffSlot.x + buffSlot.width) {
            handleBuffSlot(gameObject)
            return
          }
          // 检查是否在出售槽区域
          if (pointer.y < topBarHeight &&
              pointer.x >= sellSlot.x &&
              pointer.x <= sellSlot.x + sellSlot.width) {
            
            // 获取当前卡片所在的堆叠组
            const currentStack = this.cardStacks.find(s => s.includes(gameObject))
            let cardsToSell = currentStack ? [...currentStack] : [gameObject]
            
            // 计算总价格
            let totalPrice = 0
            cardsToSell.forEach(card => {
              const cardType = card.getData('type')
              const price = cardPrices[cardType] || 0
              totalPrice += price
            })

            if (totalPrice > 0) {
              coins.value += totalPrice
              updateGold(totalPrice)
              sellSlot.setStrokeStyle(2, 0x6e5773)

              // 添加金币动画
              const priceText = this.add.text(pointer.x, pointer.y, `+${totalPrice}`, {
                fontSize: '24px',
                resolution: 2,
                color: '#ffd700'
              }).setDepth(102)

              this.tweens.add({
                targets: priceText,
                y: '-=50',
                alpha: 0,
                duration: 1000,
                onComplete: () => priceText.destroy()
              })

              // 添加出售动画
              sellIcon.setScale(1.2)
              this.tweens.add({
                targets: sellIcon,
                scale: 1,
                duration: 200,
                ease: 'Back.easeOut'
              })

              // 移除堆叠组
              if (currentStack) {
                const stackIndex = this.cardStacks.indexOf(currentStack)
                if (stackIndex !== -1) {
                  this.cardStacks.splice(stackIndex, 1)
                }
              }

              // 销毁所有要出售的卡片
              cardsToSell.forEach(card => {
                card.destroy()
                this.cards = this.cards.filter(c => c !== card)
              })
              return
            }
            else{
              sellSlot.setStrokeStyle(2, 0x6e5773)
            }
          }

          // 检查合成 - 默认行为，不按 Shift 时执行
          if (!isStackingMode.value) {
            this.cards.forEach(otherCard => {
              if (otherCard !== gameObject &&
                Phaser.Geom.Intersects.RectangleToRectangle(gameObject.getBounds(), otherCard.getBounds())) {
                
                // 获取两张卡片所在的堆叠组
                const card1Stack = this.cardStacks.find(s => s.includes(gameObject))
                const card2Stack = this.cardStacks.find(s => s.includes(otherCard))
                
                const card1Type = gameObject.getData('type')
                const card2Type = otherCard.getData('type')

                const resultType = checkRecipe(card1Type, card2Type)

                if (resultType) {

                  const x = (gameObject.x + otherCard.x) / 2
                  const y = (gameObject.y + otherCard.y) / 2

                  // 简单的震动效果
                  this.tweens.add({
                    targets: [gameObject, otherCard],
                    x: '+=5',
                    duration: 80,
                    ease: 'Power2.easeInOut',
                    yoyo: true,
                    repeat: 2
                  })

                  // 延迟执行合成
                  this.time.delayedCall(300, () => {
                    // 创建新卡片
                    const merged = this.physics.add.image(x, y, resultType)
                      .setDisplaySize(100, 140)
                      .setInteractive({ cursor: 'pointer', useHandCursor: true })
                      .setCollideWorldBounds(true)
                      .setBounce(0.8)
                      .setData('type', resultType)
                      .setData('id', Date.now().toString())

                    this.input.setDraggable(merged)

                    // 简单的闪光效果
                    const flash = this.add.circle(x, y, 60, 0xffd700, 0.8)
                      .setDepth(150)
                      .setBlendMode(Phaser.BlendModes.ADD)

                    this.tweens.add({
                      targets: flash,
                      scale: { from: 0.1, to: 1.5 },
                      alpha: { from: 0.8, to: 0 },
                      duration: 400,
                      onComplete: () => flash.destroy()
                    })

                    // 新卡片弹性出现
                    merged.setScale(0)
                    this.tweens.add({
                      targets: merged,
                      displayWidth: { from: 0, to: 100 },
                      displayHeight: { from: 0, to: 140 },
                      duration: 300,
                      ease: 'Back.easeOut'
                    })

                    // 清理原卡片
                    if (card1Stack) {
                      const index = card1Stack.indexOf(gameObject)
                      card1Stack.splice(index, 1)
                      if (card1Stack.length <= 1) {
                        const stackIndex = this.cardStacks.indexOf(card1Stack)
                        if (stackIndex !== -1) {
                          this.cardStacks.splice(stackIndex, 1)
                        }
                      }
                    }
                    
                    if (card2Stack) {
                      const index = card2Stack.indexOf(otherCard)
                      card2Stack.splice(index, 1)
                      if (card2Stack.length <= 1) {
                        const stackIndex = this.cardStacks.indexOf(card2Stack)
                        if (stackIndex !== -1) {
                          this.cardStacks.splice(stackIndex, 1)
                        }
                      }
                    }

                    // 移除原卡片
                    gameObject.destroy()
                    otherCard.destroy()

                    // 更新数组
                    this.cards = this.cards.filter(card => card !== gameObject && card !== otherCard)
                    this.cards.push(merged)
                  })

                  return // 找到合成后立即返回
                }
              }
            })
          }

          // 如果没有找到合适的堆叠目标，保持原位置
          if (!isStacked && !currentStack) {
            gameObject.x = gameObject.x
            gameObject.y = gameObject.y
          }
        })

        // 修改拖拽开始事件
        this.input.on('dragstart', (pointer, gameObject) => {
          if (gameObject.getData('isFixed')) {
            return; // 不允许拖动固定的诗人卡片
          }
          gameObject.setDepth(150)
          gameObject.body.moves = false

          // 添加拖拽动画，但保持大小不变
          this.tweens.add({
            targets: gameObject,
            alpha: 0.9,
            duration: 200,
            ease: 'Power2'
          })

          craftingSlots.forEach(slot => {
            if (slot.getData('card') === gameObject) {
              slot.setData('occupied', false);
              slot.setData('card', null);
              console.log('Freed crafting slot for card:', gameObject.getData('type'));
            }
          });

          // 查找卡片所在的堆叠组
          const stackIndex = this.cardStacks.findIndex(s => s.includes(gameObject))
          if (stackIndex !== -1) {
            const stack = this.cardStacks[stackIndex]
            const cardIndex = stack.indexOf(gameObject)
            
            // 从原堆叠组中移除当前卡片及其上方的所有卡片
            const removedCards = stack.splice(cardIndex)
            
            // 如果原堆叠组只剩一张卡，移除该堆叠组
            if (stack.length === 1) {
              this.cardStacks.splice(stackIndex, 1)
            }
            
            // 为移除的卡片创建新的堆叠组
            if (removedCards.length > 1) {
              this.cardStacks.push(removedCards)
            }
            
            // 设置拖动卡片组的层级
            removedCards.forEach((card, index) => {
              card.setDepth(150 + index)
            })
          }

          // 查找卡片所在的堆叠组
          const stack = this.cardStacks.find(s => s.includes(gameObject))
          if (stack) {
            const cardIndex = stack.indexOf(gameObject)
            // 将当前卡片及其上方的卡片提升层级
            for (let i = cardIndex; i < stack.length; i++) {
              stack[i].setDepth(150 + i - cardIndex)
            }
          }
        })

        // 修改拖拽中事件
        this.input.on('drag', (pointer, gameObject, dragX, dragY) => {
        // 添加出售槽状态检测
        const isInSellArea = dragY < topBarHeight && 
                            dragX >= sellSlot.x && 
                            dragX <= sellSlot.x + sellSlot.width
        
        const cardType = gameObject.getData('type')
        const canSell = cardPrices[cardType] && cardPrices[cardType] > 0
        
        // 更新出售槽样式
        if (isInSellArea && canSell) {
          sellSlot.setStrokeStyle(2, 0xffffff)
        } else {
          sellSlot.setStrokeStyle(2, 0x6e5773)
        }
          gameObject.x = dragX
          gameObject.y = dragY

          // 更新堆叠组中跟随的卡片位置
          const stack = this.cardStacks.find(s => s.includes(gameObject))
          if (stack) {
            const cardIndex = stack.indexOf(gameObject)
            // 移动当前卡片上方的所有卡片
            for (let i = cardIndex + 1; i < stack.length; i++) {
              stack[i].x = dragX
              stack[i].y = dragY + (i - cardIndex) * STACK_OFFSET_Y
              stack[i].setDepth(150 + i - cardIndex)
            }
          }
        })

        // 更新堆叠位置的辅助函数
        function updateStackPosition(stack, baseX, baseY, animate = false) {
          stack.forEach((card, index) => {
            if (!card.active) return // 检查卡片是否还存在
            
            if (animate) {
              // 使用动画更新位置
              this.tweens.add({
                targets: card,
                x: baseX,
                y: baseY + (index * STACK_OFFSET_Y),
                duration: STACK_SNAP_DURATION,
                ease: 'Power2',
                onComplete: () => card.setDepth(150 + index)
              })
            } else {
              // 直接更新位置
              card.x = baseX
              card.y = baseY + (index * STACK_OFFSET_Y)
              card.setDepth(150 + index)
            }
          })
        }
      },

    },
  });
});

// 在组件卸载时销毁游戏实例
onBeforeUnmount(() => {
  if (game) game.destroy(true)
})
</script>

<style scoped>
.screen-wrapper {
  width: 100vw;
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
  width: 100vw;
  height: 100vh;
  position: relative;
}
</style>
