<template>
  <div class="layout">
    <div class="main-content">
      <!-- 左侧边栏 -->
      <div class="side-bar">
        <div class="tab-buttons">
          <button 
            :class="['tab-button', { active: activeTab === 'achievements' }]"
            @click="activeTab = 'achievements'"
          >
            成就
          </button>
          <button 
            :class="['tab-button', { active: activeTab === 'recipes' }]"
            @click="activeTab = 'recipes'"
          >
            合成表
          </button>
        </div>
        
        <div class="tab-content">
          <div v-if="activeTab === 'achievements'" class="achievements">
            <h3>成就列表</h3>
            <div v-for="achievement in achievements" :key="achievement.id" class="achievement-item">
              <span :class="['achievement-icon', { unlocked: achievement.unlocked }]">🏆</span>
              <div class="achievement-info">
                <div class="achievement-name">{{ achievement.name }}</div>
                <div class="achievement-desc">{{ achievement.description }}</div>
              </div>
            </div>
          </div>
          
          <div v-if="activeTab === 'recipes'" class="recipes">
            <h3>合成配方</h3>
            <div v-for="recipe in recipes" :key="recipe.id" class="recipe-item">
              <div class="recipe-cards">
                <img :src="recipe.card1.src" class="recipe-card" />
                <span class="plus">+</span>
                <img :src="recipe.card2.src" class="recipe-card" />
                <span class="equals">=</span>
                <img :src="recipe.result.src" class="recipe-card" />
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <div class="right-section">
        <!-- 游戏容器 -->
        <div class="game-container">
          <div ref="gameCanvas"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import Phaser from 'phaser'

// 添加边栏相关的数据
const activeTab = ref('achievements')

// 成就列表数据
const achievements = ref([
  { 
    id: 1, 
    name: '初次合成', 
    description: '完成第一次卡片合成',
    unlocked: false 
  },
  { 
    id: 2, 
    name: '合成大师', 
    description: '完成10次合成',
    unlocked: false 
  }
])

// 合成表数据
const recipes = ref([
  {
    id: 1,
    card1: { src: new URL('../assets/cards/card2.png', import.meta.url).href },
    card2: { src: new URL('../assets/cards/card4.png', import.meta.url).href },
    result: { src: new URL('../assets/cards/cardj.png', import.meta.url).href }
  },
  {
    id: 2, 
    card1: { src: new URL('../assets/cards/card3.png', import.meta.url).href },
    card2: { src: new URL('../assets/cards/card5.png', import.meta.url).href },
    result: { src: new URL('../assets/cards/cardq.png', import.meta.url).href }
  },
  {
    id: 3,
    card1: { src: new URL('../assets/cards/cardj.png', import.meta.url).href },
    card2: { src: new URL('../assets/cards/cardq.png', import.meta.url).href },
    result: { src: new URL('../assets/cards/cardk.png', import.meta.url).href }
  },
])

// 卡片素材列表
const cardImages = [
  { key: 'card2', src: new URL('../assets/cards/card2.png', import.meta.url).href },
  { key: 'card3', src: new URL('../assets/cards/card3.png', import.meta.url).href },
  { key: 'card4', src: new URL('../assets/cards/card4.png', import.meta.url).href },
  { key: 'card5', src: new URL('../assets/cards/card5.png', import.meta.url).href },
  { key: 'cardj', src: new URL('../assets/cards/cardj.png', import.meta.url).href },
  { key: 'cardq', src: new URL('../assets/cards/cardq.png', import.meta.url).href },
  { key: 'cardk', src: new URL('../assets/cards/cardk.png', import.meta.url).href },
  { key: 'card_pack', src: new URL('../assets/cards/card_pack.png', import.meta.url).href },
]

// 合成配方映射
const recipeMapping = {
  // key为两张卡的类型组合(按字母顺序排序),value为合成结果
  'card2_card4': 'cardj',
  'card3_card5': 'cardq', 
  'cardj_cardq': 'cardk'
}

// 检查两张卡是否可以合成
const checkRecipe = (card1Type, card2Type) => {
  // 确保类型按字母顺序排序以保持一致性
  const types = [card1Type, card2Type].sort()
  const recipeKey = types.join('_')
  return recipeMapping[recipeKey]
}

const cardPrices = {
  card2: 3,
  card3: 3,
  card4: 5,
  card5: 5,
  cardj: 10,
  cardq: 10,
  cardk: 10,
}

const coins = ref(100) // 初始金币数量


// 处理购买卡包
const handleBuyPack = () => {
  const packPrice = 10
  if (coins.value >= packPrice) {
    coins.value -= packPrice
    const scene = game.scene.scenes[0]
    
    // 在随机位置创建卡包
    const x = Math.random() * (scene.scale.width - 100) + 50
    const y = Math.random() * (scene.scale.height - 140 - 180) + 250
    
    const cardPack = scene.physics.add.image(x, y, 'card_pack')
      .setDisplaySize(100, 140)
      .setInteractive({ cursor: 'pointer', useHandCursor: true })
      .setData('clickCount', 0)
      
    // 添加点击事件
    cardPack.on('pointerdown', () => {
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
        const allCards = ['card2', 'card3', 'card4', 'card5', 'cardj', 'cardq', 'cardk']
        const numCards = 5
        
        // 创建闪光效果
        const flash = scene.add.sprite(cardPack.x, cardPack.y, 'card3')
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
    })
  }
}

const gameCanvas = ref(null)
let game = null

onMounted(() => {
  const container = gameCanvas.value
  const containerWidth = container.clientWidth
  const containerHeight = container.clientHeight

  const config = {
    type: Phaser.AUTO,
    width: containerWidth,
    height: containerHeight,
    parent: gameCanvas.value,
    backgroundColor: '#f5efe6', // 使用与 Forum 相同的背景色
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
      preload,
      create,
      update
    }
  }
  
  game = new Phaser.Game(config)

  function preload() {
    cardImages.forEach(card => {
      this.load.image(card.key, card.src)
    })
  }

  function create() {
  this.cards = []
  let cardId = 1
  const topBarHeight = 180
  const padding = 20
  
  // 创建顶部边栏背景，并添加交互效果
  const topBar = this.add.rectangle(0, 0, this.scale.width, topBarHeight, 0xa3916a)
    .setOrigin(0, 0)
    .setDepth(100)
    .setStrokeStyle(2, 0x8c7853) // 使用渐变色的深色部分
    .setInteractive()
    .on('pointerover', () => {
      topBar.setFillStyle(0xb39c73) // 悬停时更亮的棕色
    })
    .on('pointerout', () => {
      topBar.setFillStyle(0xa3916a) // 原始棕色
    });

  // 创建出售槽
  const sellSlot = this.add.rectangle(padding, padding, 100, 140, 0x8c7853) // 使用主题色
    .setOrigin(0, 0)
    .setDepth(101)
    .setInteractive()
    .setStrokeStyle(2, 0x6e5773) // 使用渐变色的深色部分

  // 出售槽文本
  const sellIcon = this.add.text(sellSlot.x + 50, sellSlot.y + 40, '💰', {  // y位置上移
    fontSize: '28px'  // 稍微减小字体
  }).setOrigin(0.5).setDepth(102)

  const sellText = this.add.text(sellSlot.x + 50, sellSlot.y + 90, '出售卡牌', {  // y位置上移
    fontSize: '14px',
    color: '#ffffff',
    align: 'center',
    padding: { y: 5 }  // 添加垂直内边距
  }).setOrigin(0.5).setDepth(102)

  // 为出售槽添加交互效果
  ;[sellIcon, sellText].forEach(element => {
    element.setInteractive()
    element.on('pointerover', () => {
      sellSlot.setStrokeStyle(2, 0xffffff)
    })
    element.on('pointerout', () => {
      sellSlot.setStrokeStyle(2, 0x6e5773)
    })
  })

  // 创建购买槽
  const buySlot = this.add.rectangle(padding * 2 + 100, padding, 100, 140, 0x6e5773) // 使用渐变色的深色部分
    .setOrigin(0, 0)
    .setDepth(101)
    .setInteractive({ useHandCursor: true }) // 添加手型光标
    .setStrokeStyle(2, 0x8c7853)


  // 购买槽文本
  const buyIcon = this.add.text(buySlot.x + 50, buySlot.y + 40, '🎁', {  // y位置上移
    fontSize: '28px'  // 稍微减小字体
  }).setOrigin(0.5).setDepth(102)

  const buyText = this.add.text(buySlot.x + 50, buySlot.y + 90, '购买卡包\n10金币', {  // y位置上移
    fontSize: '14px',
    color: '#ffffff',
    align: 'center',
    lineSpacing: 2,  // 减小行间距
    padding: { y: 5 }  // 添加垂直内边距
  }).setOrigin(0.5).setDepth(102)



  // 统一的购买处理函数
  const handleBuyClick = () => {
    if (coins.value >= 10) {
      handleBuyPack()
      // 添加点击反馈动画
      this.tweens.add({
        targets: [buyIcon, buyText],
        scale: { from: 0.95, to: 1 },
        duration: 200,
        ease: 'Back.easeOut'
      })
      // 添加槽位反馈
      buySlot.setStrokeStyle(2, 0xffffff)
      this.time.delayedCall(100, () => {
        buySlot.setStrokeStyle(2, 0x8c7853)
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

  // 创建金币显示背景并添加交互效果
  const coinBackground = this.add.rectangle(
    this.scale.width - padding - 100,
    padding,
    100,
    40,
    0x8c7853 // 使用主题色
  )
    .setOrigin(1, 0)
    .setDepth(101)
    .setAlpha(0.9) // 略微提高不透明度
    .setStrokeStyle(1, 0x6e5773) // 使用渐变色的深色部分
    .setInteractive()
    .on('pointerover', () => {
      coinBackground.setAlpha(1)
    })
    .on('pointerout', () => {
      coinBackground.setAlpha(0.9)
    });

  // 创建金币显示
  const coinDisplay = this.add.text(this.scale.width - padding - 10, padding + 20, `💰 ${coins.value}`, {
    fontSize: '24px',
    color: '#ffffff'
  }).setOrigin(1, 0.5).setDepth(102)

  // 更新金币显示和背景
  this.events.on('update', () => {
    coinDisplay.setText(`💰 ${coins.value}`)
    // 动态调整背景宽度以适应文本
    const padding = 20
    coinBackground.width = coinDisplay.width + padding * 2
    coinBackground.x = this.scale.width - padding
  })

  // 添加窗口缩放事件处理
  this.scale.on('resize', (gameSize) => {
    // 更新顶部边栏
    topBar.width = gameSize.width
    // 更新金币显示位置
    coinDisplay.x = gameSize.width - padding - 10
    coinBackground.x = gameSize.width - padding
  })

  // 创建初始卡片
  for (let i = 0; i < cardImages.length; i++) {
    const card = this.physics.add.image(180 + i * 120, 250 + topBarHeight, cardImages[i].key)
      .setDisplaySize(100, 140)
      .setInteractive({ cursor: 'pointer', useHandCursor: true })
      .setCollideWorldBounds(true)
      .setBounce(0.8)
      .setData('type', cardImages[i].key)
      .setData('id', cardId++)

    this.input.setDraggable(card)
    this.cards.push(card)
  }

  // 拖拽开始事件
  this.input.on('dragstart', (pointer, gameObject) => {
    gameObject.setDepth(10)
    gameObject.setAlpha(0.8)
    gameObject.body.moves = false
  })

  // 拖拽中事件
  this.input.on('drag', (pointer, gameObject, dragX, dragY) => {
    const minY = topBarHeight
    gameObject.x = dragX
    gameObject.y = Math.max(minY, dragY)
  })

  // 拖拽结束事件
  this.input.on('dragend', (pointer, gameObject) => {
    gameObject.setDepth(1)
    gameObject.setAlpha(1)
    gameObject.body.moves = true

    // 检查是否在出售槽区域
    if (pointer.y < topBarHeight && 
        pointer.x >= sellSlot.x && 
        pointer.x <= sellSlot.x + sellSlot.width) {
      // 处理出售逻辑
      const cardType = gameObject.getData('type')
      const price = cardPrices[cardType] || 0

      if (price > 0) {
        coins.value += price
        // 添加金币动画
        const priceText = this.add.text(pointer.x, pointer.y, `+${price}`, {
          fontSize: '24px',
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

        gameObject.destroy()
        this.cards = this.cards.filter(card => card !== gameObject)
        return
      }
    }

    // 检查合成
    this.cards.forEach(otherCard => {
      if (otherCard !== gameObject && 
          Phaser.Geom.Intersects.RectangleToRectangle(gameObject.getBounds(), otherCard.getBounds())) {
        const card1Type = gameObject.getData('type')
        const card2Type = otherCard.getData('type')
        
        const resultType = checkRecipe(card1Type, card2Type)
        
        if (resultType) {
          const x = (gameObject.x + otherCard.x) / 2
          const y = (gameObject.y + otherCard.y) / 2

          // 创建合成后的卡片
          const merged = this.physics.add.image(x, y, resultType)
            .setDisplaySize(100, 140)
            .setInteractive({ cursor: 'pointer', useHandCursor: true })
            .setCollideWorldBounds(true)
            .setBounce(0.8)
            .setData('type', resultType)
            .setData('id', Date.now().toString())

          this.input.setDraggable(merged)


          // 添加闪光效果
          const flash = this.add.sprite(x, y, 'card3')
            .setScale(0.1)
            .setAlpha(0.2)
            .setTint(0x8c7853) // 使用主题色
            .setBlendMode(Phaser.BlendModes.ADD)

          this.tweens.add({
            targets: flash,
            alpha: 0,
            scale: 0.5,
            duration: 300,
            onComplete: () => flash.destroy()
          })

          // 移除原卡片
          gameObject.destroy()
          otherCard.destroy()

          // 更新数组
          this.cards = this.cards.filter(card => card !== gameObject && card !== otherCard)
          this.cards.push(merged)

          // 解锁成就
          if (!achievements.value[0].unlocked) {
            achievements.value[0].unlocked = true
          }
        }
      }
    })
  })

  // 设置游戏区域边界
  this.physics.world.setBounds(0, topBarHeight, this.scale.width, this.scale.height - topBarHeight)
}

  function update() {
    // 可以添加需要持续更新的逻辑
  }
})

onBeforeUnmount(() => {
  if (game) game.destroy(true)
})
</script>

<style scoped>
/* 添加全局重置样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.layout {
  width: 100%;
  height: 100vh;
  display: flex;
  /* 添加以下属性 */
  position: absolute;
  top: 0;
  left: 0;
  overflow: hidden;
}

.main-content {
  flex: 1;
  display: flex;
  /* 添加以下属性 */
  min-height: 100%;
}
.right-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
}

.top-bar {
  position: relative;
  z-index: 1000;
  height: 180px; /* 增加高度 */
  background-color: #a3916a;
  color: white;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 20px;
}

.slot {
  position: relative;
  z-index: 1001;
  width: 100px;  /* 与卡片大小一致 */
  height: 140px; /* 与卡片大小一致 */
  border: 2px dashed #456789;
  border-radius: 8px;
  display: flex;
  flex-direction: column; /* 改为纵向排列 */
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  padding: 10px;
}

.slot-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  text-align: center;
}

.slot-icon {
  font-size: 2em;
}

.slot-text {
  font-size: 0.9em;
  font-weight: bold;
  line-height: 1.2;
}

.sell-slot {
  background-color: #c0392b;
  transition: all 0.3s ease;
}

.sell-slot.drag-over {
  transform: scale(1.05);
  box-shadow: 0 0 15px rgba(255,255,255,0.3);
  border: 2px solid #fff;
}

.buy-slot {
  background-color: #27ae60;
}

.slot:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
}

.slot-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.slot-icon {
  font-size: 1.2em;
}

.slot-text {
  font-size: 0.9em;
  font-weight: bold;
}

.price {
  font-size: 0.8em;
  opacity: 0.8;
  margin-left: 5px;
}

.currency {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 5px;
  background-color: #b39c73;
  padding: 8px 12px;
  border-radius: 20px;
}

.coin-icon {
  font-size: 1.2em;
}

.coin-amount {
  font-weight: bold;
}

.content {
  flex: 1;
  display: flex;
}

.side-bar {
  width: 250px;
  background-color: #a3916a;
  color: white;
  display: flex;
  flex-direction: column;
}

.tab-buttons {
  display: flex;
  border-bottom: 1px solid #8c7853;
}

.tab-button {
  flex: 1;
  padding: 10px;
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  transition: background-color 0.3s;
}

.tab-button.active {
  background-color: #b39c73;
}

.tab-content {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
}

.achievement-item {
  display: flex;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #8c7853;
}

.achievement-icon {
  opacity: 0.5;
  margin-right: 10px;
}

.achievement-icon.unlocked {
  opacity: 1;
}

.achievement-info {
  flex: 1;
}

.achievement-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.achievement-desc {
  font-size: 0.9em;
  opacity: 0.8;
}

.recipe-item {
  padding: 10px;
  border-bottom: 1px solid #8c7853;
}

.recipe-cards {
  display: flex;
  align-items: center;
  justify-content: center;
}

.recipe-card {
  width: 50px;
  height: 70px;
  object-fit: contain;
}

.plus, .equals {
  margin: 0 10px;
}

.game-container {
  flex: 1;
  display: flex;
  position: relative;
  /* 添加以下属性 */
  margin: 0;
  padding: 0;
  background-color: #f5efe6;
}

.game-container > div {
  flex: 1;
  min-height: 0;
  touch-action: none;
  /* 添加以下属性 */
  margin: 0;
  padding: 0;
}
</style>