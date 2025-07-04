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
let buySlot2Animating = false  
let buySlot3Animating = false
let buySlot4Animating = false
let buySlot1OriginalX, buySlot2OriginalX, buySlot3OriginalX, buySlot4OriginalX

const isStackingMode = ref(false)
const gameState = ref({ gold: 100 })

const updateGold = (amount) => {
  gameState.value.gold += amount
  if (gameState.value.gold < 0) gameState.value.gold = 0
  console.log('当前金币:', gameState.value.gold)
}

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

// 在 checkRecipe 后添加三卡合成检查函数
const checkCrafting = (cards) => {
  if (cards.length !== 3) return null;
  const types = cards.map(card => card.getData('type')).sort()
  const recipeKey = types.join('_')
  console.log('Crafting Recipe Key:', recipeKey); // 调试信息
  return craftingRecipes[recipeKey]
}
const cardPrices = {
  card_pack_poem: 10,
  card_pack_poet: 10,
                         
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

const handleBuyAdvancedPack = () => {
  const packPrice = 15
  if (coins.value >= packPrice) {
    //coins.value -= packPrice

    updateGold(-packPrice)
  const scene = game.scene.scenes[0]
  
  // 在随机位置创建卡包
  const x = Math.random() * (scene.scale.width - 100) + 50
  const y = Math.random() * (scene.scale.height - 140 - 180) + 250

  const advancedPack = scene.physics.add.image(x, y, 'card_pack_poet')
    .setDisplaySize(100, 140)
    .setInteractive({ cursor: 'pointer', useHandCursor: true, draggable: true })
    .setCollideWorldBounds(true)
    .setBounce(0.8)
    .setData('clickCount', 0)
    .setData('type', 'card_pack_poet')
    .setData('isDragging', false)
    .setData('pointerDown', false)
    .setData('dragStartX', 0)
    .setData('dragStartY', 0)


  scene.input.setDraggable(advancedPack)

  // 添加点击处理
  advancedPack.on('pointerup', (pointer) => {
    const isDragging = advancedPack.getData('isDragging')
    const startX = advancedPack.getData('dragStartX')
    const startY = advancedPack.getData('dragStartY')
    const distance = Phaser.Math.Distance.Between(startX, startY, pointer.x, pointer.y)

    if (distance < 5 && !isDragging) {
      const clickCount = advancedPack.getData('clickCount')

      if (clickCount === 0) {
        // 第一次点击：震动效果
        scene.tweens.add({
          targets: advancedPack,
          x: advancedPack.x + 5,
          duration: 50,
          yoyo: true,
          repeat: 3
        })
        advancedPack.setData('clickCount', 1)
      } else {
        // 第二次点击：生成随机卡片
        const advancedCards = ['libai','sushi','wangwei']
        const numCards = 3

        // 创建闪光效果
        const flash = scene.add.sprite(advancedPack.x, advancedPack.y, 'card_pack_poet')
          .setScale(0.1)
          .setAlpha(0.8)
          .setTint(0xffd700)
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
          const randomCard = advancedCards[Math.floor(Math.random() * advancedCards.length)]

          const newX = advancedPack.x + Math.cos(angle) * radius
          const newY = advancedPack.y + Math.sin(angle) * radius

          const card = scene.physics.add.image(advancedPack.x, advancedPack.y, randomCard)
            .setDisplaySize(100, 140)
            .setInteractive({ cursor: 'pointer', useHandCursor: true })
            .setCollideWorldBounds(true)
            .setBounce(0.8)
            .setData('type', randomCard)
            .setData('id', Date.now().toString() + i)

          scene.input.setDraggable(card)
          scene.cards.push(card)

          scene.tweens.add({
            targets: card,
            x: newX,
            y: newY,
            alpha: { from: 0.5, to: 1 },
            duration: 500,
            ease: 'Back.easeOut'
          })
        }

        // 销毁卡包
        scene.tweens.add({
          targets: advancedPack,
          alpha: 0,
          scale: 0.5,
          duration: 300,
          onComplete: () => advancedPack.destroy()
        })
      }
    }
    advancedPack.setData('pointerDown', false)
  })

  // 添加与普通卡包相同的拖动事件处理
  advancedPack.on('pointerdown', (pointer) => {
    advancedPack.setData('pointerDown', true)
    advancedPack.setData('dragStartX', pointer.x)
    advancedPack.setData('dragStartY', pointer.y)
  })

  advancedPack.on('dragstart', () => {
    advancedPack.setData('isDragging', true)
  })

  advancedPack.on('dragend', () => {
    if (advancedPack.getData('isDragging')) {
      setTimeout(() => {
        advancedPack.setData('isDragging', false)
        advancedPack.setData('pointerDown', false)
      }, 100)
    }
  })
}
}
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
        const buySlot = this.add.rectangle(padding * 2 + 100, padding - 200, 100, 140, 0x6e5773)
          .setOrigin(0, 0)
          .setDepth(101)
          .setInteractive({ useHandCursor: true })
          .setStrokeStyle(3, 0x8c7853, 0.9)

        buySlot1OriginalX = buySlot.x
        // 购买槽文本 - 直接使用最终位置
        const buyIcon = this.add.text(padding * 2 + 100 + 50, padding + 40 - 200, '🎁', {
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

        // 购买槽和文本一起入场动画
        this.tweens.add({
          targets: buySlot,
          y: padding,
          duration: 600,
          ease: 'Back.easeOut',
          delay: 300
        })

        this.tweens.add({
          targets: buyIcon,
          y: padding + 40,
          duration: 600,
          ease: 'Back.easeOut',
          delay: 300
        })

        this.tweens.add({
          targets: buyText,
          y: padding + 90,
          duration: 600,
          ease: 'Back.easeOut',
          delay: 300
        })
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
            this.time.delayedCall(200, () => {
              buySlot.setStrokeStyle(3, 0x8c7853, 0.9)
            })
            
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
            this.time.delayedCall(300, () => {
              buySlot.setStrokeStyle(3, 0x8c7853, 0.9)
            })
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

          // 创建第二个购买槽 - 完整版本
          const buySlot2 = this.add.rectangle(padding * 3 + 200, padding - 200, 100, 140, 0x6e5773)
            .setOrigin(0, 0)
            .setDepth(101)
            .setInteractive({ useHandCursor: true })
            .setStrokeStyle(3, 0x8c7853, 0.9)

          buySlot2OriginalX = buySlot2.x
          // 第二个购买槽的文本和图标 - 直接使用最终位置
          const buyIcon2 = this.add.text(padding * 3 + 200 + 50, padding + 40 - 200, '📦', {
            fontSize: '32px',
            resolution: 2,
            padding: { x: 2, y: 2 }
          }).setOrigin(0.5).setDepth(102)

          const buyText2 = this.add.text(padding * 3 + 200 + 50, padding + 90 - 200, '诗人卡包\n15金币', {
            fontSize: '16px',
            resolution: 2,
            color: '#ffffff',
            align: 'center',
            fontWeight: 'bold',
            lineSpacing: 2,
            padding: { y: 5 }
          }).setOrigin(0.5).setDepth(102)

          // 第二个购买槽入场动画
          this.tweens.add({
            targets: buySlot2,
            y: padding,
            duration: 600,
            ease: 'Back.easeOut',
            delay: 400
          })

          this.tweens.add({
            targets: buyIcon2,
            y: padding + 40,
            duration: 600,
            ease: 'Back.easeOut',
            delay: 400
          })

          this.tweens.add({
            targets: buyText2,
            y: padding + 90,
            duration: 600,
            ease: 'Back.easeOut',
            delay: 400
          })

          // 第二个购买槽的简化悬浮效果
          buySlot2.on('pointerover', () => {
            if (coins.value >= 15) {
              buySlot2.setStrokeStyle(3, 0xffffff, 1)
              buySlot2.setAlpha(0.9)
              this.tweens.add({
                targets: [buyIcon2, buyText2],
                scale: 1.05,
                duration: 150,
                ease: 'Power2.easeOut'
              })
            } else {
              buySlot2.setStrokeStyle(3, 0xff5722, 1)
              buySlot2.setAlpha(0.8)
            }
          })

          buySlot2.on('pointerout', () => {
            buySlot2.setStrokeStyle(3, 0x8c7853, 0.9)
            buySlot2.setAlpha(1)
            this.tweens.add({
              targets: [buyIcon2, buyText2],
              scale: 1,
              duration: 150,
              ease: 'Power2.easeOut'
            })
          })

          // 第二个购买槽的处理函数
          const handleBuyClick2 = () => {
            if (coins.value >= 15) {
              this.tweens.killTweensOf([buySlot2, buyIcon2, buyText2])
              buySlot2.setScale(1)
              buyIcon2.setScale(1)
              buyText2.setScale(1)
              coins.value -= 15
              
              this.tweens.add({
                targets: buySlot2,
                scaleX: 0.95,
                scaleY: 0.95,
                duration: 100,
                ease: 'Power2.easeOut',
                yoyo: true,
                onComplete: () => {
                  buySlot2.setScale(1) // 确保动画完成后重置
                }
              })
              
              this.tweens.add({
                targets: [buyIcon2, buyText2],
                scale: 0.9,
                duration: 100,
                ease: 'Power2.easeOut',
                yoyo: true,
                onComplete: () => {
                  buyIcon.setScale(1) // 确保动画完成后重置
                  buyText.setScale(1)
                }
              })
              
              buySlot2.setStrokeStyle(3, 0xffffff)
              this.time.delayedCall(200, () => {
                buySlot2.setStrokeStyle(3, 0x8c7853, 0.9)
              })
              
              const costText = this.add.text(
                buySlot2.x + 50,
                buySlot2.y + 120,
                '-15',
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
              
              handleBuyAdvancedPack()
            } else {
              if (buySlot2Animating) return
              buySlot2Animating = true
        
              // 确保从原始位置开始动画
              buySlot2.x = buySlot2OriginalX
              buySlot2.setScale(1)
              buyIcon2.setScale(1)
              buyText2.setScale(1)
              // 简化的金币不足反馈（与第一个槽位相同）
              this.tweens.add({
                targets: buySlot2,
                x: buySlot2OriginalX + 3,
                scaleX: 0.95,
                scaleY: 0.95,
                duration: 100,
                ease: 'Power2',
                yoyo: true,
                repeat: 2,
                onComplete: () => {
                  buySlot2.x = buySlot2OriginalX  // 确保回到原始位置
                  buySlot2.setScale(1)
                  buySlot2Animating = false  // 重置防抖标记
                }
              })
              
              buyIcon2.x = buySlot2OriginalX + 50
              buyText2.x = buySlot2OriginalX + 50
              
              this.tweens.add({
                targets: [buyIcon2, buyText2],
                x: buySlot2OriginalX + 50 + 3,
                scale: 0.9,
                duration: 100,
                ease: 'Power2',
                yoyo: true,
                repeat: 2,
                onComplete: () => {
                  buyIcon2.x = buySlot2OriginalX + 50
                  buyText2.x = buySlot2OriginalX + 50
                  buyIcon2.setScale(1)
                  buyText2.setScale(1)
                }
              })
              const warningText = this.add.text(
                buySlot2.x + 50,
                buySlot2.y + 120,
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
              
              buySlot2.setStrokeStyle(3, 0xff5722)
              this.time.delayedCall(300, () => {
                buySlot2.setStrokeStyle(3, 0x8c7853, 0.9)
              })
            }
          }

          // 为第二个购买槽添加交互效果
          ;[buySlot2, buyIcon2, buyText2].forEach(element => {
            element.on('pointerdown', handleBuyClick2)
            element.on('pointerover', () => {
              buySlot2.setStrokeStyle(2, 0xffffff)
            })
            element.on('pointerout', () => {
              buySlot2.setStrokeStyle(2, 0x8c7853)
            })
          })


          // 第三个购买槽
          const buySlot3 = this.add.rectangle(padding * 4 + 300, padding - 200, 100, 140, 0x6e5773)
            .setOrigin(0, 0)
            .setDepth(101)
            .setInteractive({ useHandCursor: true })
            .setStrokeStyle(3, 0x8c7853, 0.9)

          buySlot3OriginalX = buySlot3.x  
          // 第三个购买槽的文本和图标 - 直接使用最终位置
          const buyIcon3 = this.add.text(padding * 4 + 300 + 50, padding + 40 - 200, '🧙', {
            fontSize: '32px',
            resolution: 2,
            padding: { x: 2, y: 2 }
          }).setOrigin(0.5).setDepth(102)

          const buyText3 = this.add.text(padding * 4 + 300 + 50, padding + 90 - 200, '书生卡\n10金币', {
            fontSize: '16px',
            resolution: 2,
            color: '#ffffff',
            align: 'center',
            fontWeight: 'bold',
            lineSpacing: 2,
            padding: { y: 5 }
          }).setOrigin(0.5).setDepth(102)

          // 第三个购买槽入场动画
          this.tweens.add({
            targets: buySlot3,
            y: padding,
            duration: 600,
            ease: 'Back.easeOut',
            delay: 500
          })

          this.tweens.add({
            targets: buyIcon3,
            y: padding + 40,
            duration: 600,
            ease: 'Back.easeOut',
            delay: 500
          })

          this.tweens.add({
            targets: buyText3,
            y: padding + 90,
            duration: 600,
            ease: 'Back.easeOut',
            delay: 500
          })

          // 第三个购买槽的简化悬浮效果
          buySlot3.on('pointerover', () => {
            if (coins.value >= 10) {
              buySlot3.setStrokeStyle(3, 0xffffff, 1)
              buySlot3.setAlpha(0.9)
              this.tweens.add({
                targets: [buyIcon3, buyText3],
                scale: 1.05,
                duration: 150,
                ease: 'Power2.easeOut'
              })
            } else {
              buySlot3.setStrokeStyle(3, 0xff5722, 1)
              buySlot3.setAlpha(0.8)
            }
          })

          buySlot3.on('pointerout', () => {
            buySlot3.setStrokeStyle(3, 0x8c7853, 0.9)
            buySlot3.setAlpha(1)
            this.tweens.add({
              targets: [buyIcon3, buyText3],
              scale: 1,
              duration: 150,
              ease: 'Power2.easeOut'
            })
          })
          // 第四个购买槽
          const buySlot4 = this.add.rectangle(padding * 5 + 400, padding - 200, 100, 140, 0x6e5773)
            .setOrigin(0, 0)
            .setDepth(101)
            .setInteractive({ useHandCursor: true })
            .setStrokeStyle(3, 0x8c7853, 0.9)

          buySlot4OriginalX = buySlot4.x
          // 第四个购买槽的文本和图标 - 直接使用最终位置
          const buyIcon4 = this.add.text(padding * 5 + 400 + 50, padding + 40 - 200, '⛩️', {
            fontSize: '32px',
            resolution: 2,
            padding: { x: 2, y: 2 }
          }).setOrigin(0.5).setDepth(102)

          const buyText4 = this.add.text(padding * 5 + 400 + 50, padding + 90 - 200, '书斋卡\n10金币', {
            fontSize: '16px',
            color: '#ffffff',
            align: 'center',
            resolution: 2,
            fontWeight: 'bold',
            lineSpacing: 2,
            padding: { y: 5 }
          }).setOrigin(0.5).setDepth(102)

          // 第四个购买槽入场动画
          this.tweens.add({
            targets: buySlot4,
            y: padding,
            duration: 600,
            ease: 'Back.easeOut',
            delay: 600
          })

          this.tweens.add({
            targets: buyIcon4,
            y: padding + 40,
            duration: 600,
            ease: 'Back.easeOut',
            delay: 600
          })

          this.tweens.add({
            targets: buyText4,
            y: padding + 90,
            duration: 600,
            ease: 'Back.easeOut',
            delay: 600
          })

          // 第四个购买槽的简化悬浮效果
          buySlot4.on('pointerover', () => {
            if (coins.value >= 10) {
              buySlot4.setStrokeStyle(3, 0xffffff, 1)
              buySlot4.setAlpha(0.9)
              this.tweens.add({
                targets: [buyIcon4, buyText4],
                scale: 1.05,
                duration: 150,
                ease: 'Power2.easeOut'
              })
            } else {
              buySlot4.setStrokeStyle(3, 0xff5722, 1)
              buySlot4.setAlpha(0.8)
            }
          })

          buySlot4.on('pointerout', () => {
            buySlot4.setStrokeStyle(3, 0x8c7853, 0.9)
            buySlot4.setAlpha(1)
            this.tweens.add({
              targets: [buyIcon4, buyText4],
              scale: 1,
              duration: 150,
              ease: 'Power2.easeOut'
            })
          })

          // 第三个购买槽的处理函数（购买工人卡）- 完整版本
          const handleBuyWorker = () => {
            if (coins.value >= 10) {
              this.tweens.killTweensOf([buySlot3, buyIcon3, buyText3])
              buySlot3.setScale(1)
              buyIcon3.setScale(1)
              buyText3.setScale(1)

              coins.value -= 10

              // 简单的按下反馈
              this.tweens.add({
                targets: buySlot3,
                scaleX: 0.95,
                scaleY: 0.95,
                duration: 100,
                ease: 'Power2.easeOut',
                yoyo: true,
                onComplete: () => {
                  buySlot3.setScale(1) // 确保动画完成后重置
                }
              })

              // 添加点击反馈动画
              this.tweens.add({
                targets: [buyIcon3, buyText3],
                scale: 0.9,
                duration: 100,
                ease: 'Power2.easeOut',
                yoyo: true,
                onComplete: () => {
                  buyIcon3.setScale(1) // 确保动画完成后重置
                  buyText3.setScale(1)
                }
              })
              
              buySlot3.setStrokeStyle(3, 0xffffff)
              this.time.delayedCall(200, () => {
                buySlot3.setStrokeStyle(3, 0x8c7853, 0.9)
              })
              
              const costText = this.add.text(
                buySlot3.x + 50,
                buySlot3.y + 120,
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
              if (buySlot3Animating) return
              buySlot3Animating = true
              
              buySlot3.x = buySlot3OriginalX
              buySlot3.setScale(1)
              buyIcon2.setScale(1)
              buyText3.setScale(1)
              // 金币不足的简化反馈
              this.tweens.add({
                targets: buySlot3,
                x: buySlot3OriginalX + 3,
                scaleX: 0.95,
                scaleY: 0.95,
                duration: 100,
                ease: 'Power2',
                yoyo: true,
                repeat: 2,
                onComplete: () => {
                  buySlot3.x = buySlot3OriginalX  // 确保回到原始位置
                  buySlot2.setScale(1)
                  buySlot3Animating = false  // 重置防抖标记
                }
              })
              
              buyIcon3.x = buySlot3OriginalX + 50
              buyText3.x = buySlot3OriginalX + 50
              
              this.tweens.add({
                targets: [buyIcon3, buyText3],
                x: buySlot3OriginalX + 50 + 3,
                scale: 0.9,
                duration: 100,
                ease: 'Power2',
                yoyo: true,
                repeat: 2,
                onComplete: () => {
                  buyIcon3.x = buySlot3OriginalX + 50
                  buyText3.x = buySlot3OriginalX + 50
                  buyIcon2.setScale(1)
                  buyText2.setScale(1)
                }
              })
              
              const warningText = this.add.text(
                buySlot3.x + 50,
                buySlot3.y + 120,
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
              
              buySlot3.setStrokeStyle(3, 0xff5722)
              this.time.delayedCall(300, () => {
                buySlot3.setStrokeStyle(3, 0x8c7853, 0.9)
              })
            }
          }
          // 第四个购买槽的处理函数（购买工厂卡）- 完整版本
          const handleBuyFactory = () => {
            if (coins.value >= 10) {
              this.tweens.killTweensOf([buySlot4, buyIcon4, buyText4])
              buySlot4.setScale(1)
              buyIcon4.setScale(1)
              buyText4.setScale(1)

              coins.value -= 10
              updateGold(-10)

              // 简单的按下反馈
              this.tweens.add({
                targets: buySlot4,
                scaleX: 0.95,
                scaleY: 0.95,
                duration: 100,
                ease: 'Power2.easeOut',
                yoyo: true,
                onComplete: () => {
                  buySlot4.setScale(1) // 确保动画完成后重置
                }
              })
              
              this.tweens.add({
                targets: [buyIcon4, buyText4],
                scale: 0.9,
                duration: 100,
                ease: 'Power2.easeOut',
                yoyo: true,
                onComplete: () => {
                  buyIcon4.setScale(1) // 确保动画完成后重置
                  buyText4.setScale(1)
                }
              })
              
              buySlot4.setStrokeStyle(3, 0xffffff)
              this.time.delayedCall(200, () => {
                buySlot4.setStrokeStyle(3, 0x8c7853, 0.9)
              })
              
              const costText = this.add.text(
                buySlot4.x + 50,
                buySlot4.y + 120,
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

              // 创建工厂卡
              const x = Math.random() * (this.scale.width - 100) + 50
              const y = Math.random() * (this.scale.height - 140 - 180) + 250

              const factoryCard = this.physics.add.image(x, y, 'factory')
                .setDisplaySize(100, 140)
                .setInteractive({ cursor: 'pointer', useHandCursor: true })
                .setCollideWorldBounds(true)
                .setBounce(0.8)
                .setData('type', 'factory')
                .setData('id', Date.now().toString())

              this.input.setDraggable(factoryCard)
              this.cards.push(factoryCard)
            } else {
              if (buySlot4Animating) return
              buySlot4Animating = true
              
              // 确保从原始位置开始动画
              buySlot4.x = buySlot4OriginalX
              buySlot4.setScale(1)
              buyIcon4.setScale(1)
              buyText4.setScale(1)

              // 金币不足的简化反馈
              this.tweens.add({
                targets: buySlot4,
                x: buySlot4OriginalX + 3,
                scaleX: 0.95,
                scaleY: 0.95,
                duration: 100,
                ease: 'Power2',
                yoyo: true,
                repeat: 2,
                onComplete: () => {
                  buySlot4.x = buySlot4OriginalX  // 确保回到原始位置
                  buySlot4.setScale(1)
                  buySlot4Animating = false  // 重置防抖标记
                }
              })
              
              buyIcon4.x = buySlot4OriginalX + 50
              buyText4.x = buySlot4OriginalX + 50
              
              this.tweens.add({
                targets: [buyIcon4, buyText4],
                x: buySlot4OriginalX + 50 + 3,
                scale: 0.9,
                duration: 100,
                ease: 'Power2',
                yoyo: true,
                repeat: 2,
                onComplete: () => {
                  buyIcon4.x = buySlot4OriginalX + 50
                  buyText4.x = buySlot4OriginalX + 50
                  buyIcon4.setScale(1)
                  buyText4.setScale(1)
                }
              })
              
              const warningText = this.add.text(
                buySlot4.x + 50,
                buySlot4.y + 120,
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
              
              buySlot4.setStrokeStyle(3, 0xff5722)
              this.time.delayedCall(300, () => {
                buySlot4.setStrokeStyle(3, 0x8c7853, 0.9)
              })
            }
          }


          // 为第三个购买槽添加交互效果
          ;[buySlot3, buyIcon3, buyText3].forEach(element => {
            element.on('pointerdown', handleBuyWorker)
            element.on('pointerover', () => {
              buySlot3.setStrokeStyle(2, 0xffffff)
            })
            element.on('pointerout', () => {
              buySlot3.setStrokeStyle(2, 0x8c7853)
            })
          })


          // 创建合成台背景
          const craftingStation = this.add.rectangle(
            this.scale.width + 400, // 初始位置在屏幕右侧（隐藏）
            padding, 
            400, 
            140, 
            0xa3916a, 
            1 
          )
            .setOrigin(0, 0)
            .setDepth(100)
            .setStrokeStyle(2, 0xa3916a);

          // 创建四个合成槽 - 修改初始位置
          const craftingSlots = []
          const slotWidth = 80
          const cardWidth = 100; 
          const cardHeight = 140; 
          const slotSpacing = 20
          const slotTypes = ['card2', 'card3', 'card_worker', null] 

          // 计算最终位置
          const finalCraftingX = padding * 5 + 500 + padding; // 第四个购买槽右边

          for (let i = 0; i < 4; i++) {
            // 初始位置在屏幕右侧（隐藏）
            const initialX = this.scale.width + slotSpacing + i * (cardWidth + slotSpacing);
            const finalX = finalCraftingX + slotSpacing + i * (cardWidth + slotSpacing);
            const y = craftingStation.y + (craftingStation.height - cardHeight) / 2; 

            const slot = this.add.rectangle(initialX, y, cardWidth, cardHeight, 0x8c7853) 
              .setOrigin(0, 0)
              .setDepth(101)
              .setStrokeStyle(1, 0xffffff)
              .setData('type', slotTypes[i])
              .setData('occupied', false)
              .setData('card', null)
              .setInteractive({ dropZone: true })
              .setData('finalX', finalX); // 存储最终位置

            craftingSlots.push(slot);

            // 添加槽位标识 - 初始位置也在屏幕右侧
            let operatorText = null;
            if (i < 3) {
              operatorText = this.add.text(
                initialX + cardWidth + 5, 
                y + cardHeight / 2, 
                i < 2 ? '+' : '=', 
                {
                  fontSize: '24px',
                  resolution: 5,
                  color: '#ffffff'
                }
              )
                .setOrigin(0, 0.5)
                .setDepth(101)
                .setData('finalX', finalX + cardWidth + 5); // 存储最终位置
            }

            // 添加合成台入场动画 - 在购买槽动画完成后开始
            this.time.delayedCall(700, () => {
              // 合成台背景入场动画
              this.tweens.add({
                targets: craftingStation,
                x: finalCraftingX,
                duration: 800,
                ease: 'Back.easeOut',
                delay: 0
              });

              // 槽位入场动画 - 依次出现
              this.tweens.add({
                targets: slot,
                x: finalX,
                duration: 600,
                ease: 'Back.easeOut',
                delay: i * 100, // 每个槽位延迟100ms
                onComplete: () => {
                  // 槽位到位后添加轻微的弹跳效果
                  this.tweens.add({
                    targets: slot,
                    scaleX: { from: 1, to: 1.05 },
                    scaleY: { from: 1, to: 1.05 },
                    duration: 200,
                    ease: 'Power2',
                    yoyo: true
                  });
                }
              });

              // 操作符文本入场动画
              if (operatorText) {
                this.tweens.add({
                  targets: operatorText,
                  x: finalX + cardWidth + 5,
                  alpha: { from: 0, to: 1 },
                  duration: 600,
                  ease: 'Back.easeOut',
                  delay: i * 100 + 200, // 比槽位稍晚出现
                });
              }
            });
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

                  // 清空材料槽
                  materials.forEach(card => {
                    // 从cards数组中移除
                    this.cards = this.cards.filter(c => c !== card);
                    card.destroy();
                  });
                  
                  craftingSlots.forEach(slot => {
                    slot.setData('occupied', false);
                    slot.setData('card', null);
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


          // 更新合成台位置
          craftingStation.x = buySlot4.x + buySlot4.width + padding; // 书斋卡右边

          // 更新槽位位置
          craftingSlots.forEach((slot, i) => {
            const x = craftingStation.x + slotSpacing + i * (cardWidth + slotSpacing);
            slot.x = x;

            // 更新槽位标识和提示文本的位置
            if (i < 3) {
              const operatorText = this.add.text(x + cardWidth + 5, slot.y + cardHeight / 2, i < 2 ? '+' : '=', {
                fontSize: '24px',
                resolution: 2,
                color: '#ffffff'
              }).setOrigin(0, 0.5).setDepth(101);
            }

            const slotText = i === 3 ? '诗词' : i === 2 ? '诗人' : `诗意${i + 1}`;
            this.add.text(x + cardWidth / 2, slot.y - 5, slotText, {
              fontSize: '12px',
              resolution: 2,
              color: '#ffffff'
            }).setOrigin(0.5, 1).setDepth(101);
          });
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
  width: 100vw;
  height: 200vh;       /* 两个视口叠加 */
  transition: transform 0.8s ease;
}

.screen {
  width: 100vw;
  height: 100vh;
  position: relative;
}
</style>
