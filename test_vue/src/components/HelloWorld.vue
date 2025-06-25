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
        <!-- 顶部边栏 -->
        <div class="top-bar">
          <div class="slot sell-slot" @dragover.prevent @drop="handleSellCard">
            <div class="slot-content">
              <span class="slot-icon">💰</span>
              <span class="slot-text">拖动至此出售卡牌</span>
            </div>
          </div>
          
          <div class="slot buy-slot" @click="handleBuyPack">
            <div class="slot-content">
              <span class="slot-icon">🎁</span>
              <span class="slot-text">购买卡包</span>
              <span class="price">100金币</span>
            </div>
          </div>
          
          <div class="currency">
            <span class="coin-icon">💰</span>
            <span class="coin-amount">{{ coins }}</span>
          </div>
        </div>
        
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
    card1: { src: new URL('../assets/cards/card1.png', import.meta.url).href },
    card2: { src: new URL('../assets/cards/card2.png', import.meta.url).href },
    result: { src: new URL('../assets/cards/card3.png', import.meta.url).href }
  }
])

// 卡片素材列表（实际素材请替换自己的文件名）
const cardImages = [
  { key: 'card1', src: new URL('../assets/cards/card1.png', import.meta.url).href },
  { key: 'card2', src: new URL('../assets/cards/card2.png', import.meta.url).href },
  { key: 'card3', src: new URL('../assets/cards/card3.png', import.meta.url).href }
]

const coins = ref(1000) // 初始金币数量

const cardPrices = {
  card1: 30,
  card2: 50,
  card3: 100
}

// 修改处理出售卡牌的逻辑
const handleSellCard = (event) => {
  event.preventDefault()
  const cardType = event.dataTransfer.getData('cardType')
  const cardId = event.dataTransfer.getData('cardId')
  
  // 获取卡片售价
  const price = cardPrices[cardType] || 0
  
  if (price > 0) {
    coins.value += price
    // 在游戏场景中移除被出售的卡片
    const scene = game.scene.scenes[0]
    const soldCard = scene.cards.find(card => card.getData('id') === cardId)
    if (soldCard) {
      soldCard.destroy()
      scene.cards = scene.cards.filter(card => card.getData('id') !== cardId)
    }
  }
}

// 处理购买卡包
const handleBuyPack = () => {
  const packPrice = 100
  if (coins.value >= packPrice) {
    coins.value -= packPrice
    // TODO: 添加购买卡包的逻辑
  }
}

const gameCanvas = ref(null)
let game = null

onMounted(() => {
  // 获取游戏容器的尺寸
  const container = gameCanvas.value;
  const containerWidth = container.clientWidth;
  const containerHeight = container.clientHeight;

  const config = {
    type: Phaser.AUTO,
    // 使用容器的尺寸
    width: containerWidth,
    height: containerHeight,
    parent: gameCanvas.value,
    backgroundColor: '#f5f5f5',
    scale: {
      mode: Phaser.Scale.RESIZE,  // 添加自适应缩放
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
    // 加载所有卡片素材
    cardImages.forEach(card => {
      this.load.image(card.key, card.src)
    })
  }

  function create() {
    this.cards = []
    // 创建三张卡片，分开放置
    for (let i = 0; i < cardImages.length; i++) {
      const card = this.physics.add.image(180 + i * 120, 250, cardImages[i].key)
        .setDisplaySize(100, 140)
        .setInteractive({ cursor: 'pointer', useHandCursor: true })
        .setCollideWorldBounds(true)
        .setBounce(0.8)
        .setData('type', cardImages[i].key)

      this.input.setDraggable(card)
      this.cards.push(card)
    }

      // 拖拽逻辑
    this.input.on('dragstart', (pointer, gameObject) => {
      gameObject.setDepth(10)
      gameObject.body.moves = false
    })

    this.input.on('drag', (pointer, gameObject, dragX, dragY) => {
      gameObject.x = dragX
      gameObject.y = dragY
    })

    this.input.on('dragend', (pointer, gameObject) => {
      gameObject.setDepth(1)
      gameObject.body.moves = true
      
      // 在拖拽结束时检查与其他卡片的重叠
      this.cards.forEach(otherCard => {
        if (otherCard !== gameObject && Phaser.Geom.Intersects.RectangleToRectangle(gameObject.getBounds(), otherCard.getBounds())) {
          // 获取两张卡片的类型
          const card1Type = gameObject.getData('type')
          const card2Type = otherCard.getData('type')
          
          // 检查是否是card1和card2的组合
          if ((card1Type === 'card1' && card2Type === 'card2') ||
              (card1Type === 'card2' && card2Type === 'card1')) {
            
            // 在中间位置创建card3
            const x = (gameObject.x + otherCard.x) / 2
            const y = (gameObject.y + otherCard.y) / 2
            const merged = this.physics.add.image(x, y, 'card3')
              .setDisplaySize(100, 140)
              .setInteractive({ cursor: 'pointer', useHandCursor: true })
              .setCollideWorldBounds(true)
              .setBounce(0.8)
              .setData('type', 'card3')
            
            // 设置新卡片为可拖动
            this.input.setDraggable(merged)
            
            // // 添加合成动画效果
            // this.tweens.add({
            //   targets: merged,
            //   scale: 1.2,
            //   duration: 200,
            //   yoyo: true,
            //   onComplete: () => {
            //     merged.setScale(1)
            //   }
            // })
            
            // 移除原有卡片
            gameObject.destroy()
            otherCard.destroy()
            
            // 更新cards数组
            this.cards = this.cards.filter(card => card !== gameObject && card !== otherCard)
            this.cards.push(merged)
          }
        }
      })
    })

  }

  function update() {
    // 这里可以添加后续的状态刷新逻辑
  }
})

onBeforeUnmount(() => {
  if (game) game.destroy(true)
})
</script>

<style scoped>
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

.top-bar {
  height: 180px; /* 增加高度 */
  background-color: #2c3e50;
  color: white;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 20px;
}

.slot {
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
  background-color: #34495e;
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
  background-color: #34495e;
  color: white;
  display: flex;
  flex-direction: column;
}

.tab-buttons {
  display: flex;
  border-bottom: 1px solid #456789;
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
  background-color: #456789;
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
  border-bottom: 1px solid #456789;
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
  border-bottom: 1px solid #456789;
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
  padding: 20px;
  background-color: #f5f5f5;
}

.game-container {
  flex: 1;
  padding: 20px;
  background-color: #f5f5f5;
  display: flex;  /* 添加 flex 布局 */
}

.game-container > div {
  flex: 1;       /* 游戏画布占满容器 */
  min-height: 0; /* 防止溢出 */
}
</style>