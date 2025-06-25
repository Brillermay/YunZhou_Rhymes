<template>
    <div class="game-container">
      <div ref="gameCanvas"></div>
    </div>
  </template>
  
  <script setup>
  import { ref, onMounted, onBeforeUnmount } from 'vue'
  import Phaser from 'phaser'
  
  // 卡片素材列表（实际素材请替换自己的文件名）
  const cardImages = [
    { key: 'card1', src: new URL('../assets/cards/card1.png', import.meta.url).href },
    { key: 'card2', src: new URL('../assets/cards/card2.png', import.meta.url).href },
    { key: 'card3', src: new URL('../assets/cards/card3.png', import.meta.url).href }
  ]
  
  const gameCanvas = ref(null)
  let game = null
  
  onMounted(() => {
    const config = {
      type: Phaser.AUTO,
      width: 600,
      height: 500,
      parent: gameCanvas.value,
      backgroundColor: '#f5f5f5',
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
      })
  
      // 碰撞检测及合成动画
      this.physics.add.collider(this.cards, this.cards, (obj1, obj2) => {
        // 合成动画：碰撞后展示一个新卡片（假设用card1做演示），实际可以按你的逻辑判断
        const x = (obj1.x + obj2.x) / 2
        const y = (obj1.y + obj2.y) / 2
        const merged = this.add.image(x, y, 'card1').setDisplaySize(120, 168)
        this.tweens.add({
          targets: merged,
          alpha: 0,
          scale: 1.4,
          duration: 500,
          onComplete: () => {
            merged.destroy()
          }
        })
        // 移除被合成的卡片
        obj1.destroy()
        obj2.destroy()
      }, null, this)
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
  .game-container {
    width: 600px;
    margin: 40px auto;
    border: 1px solid #eee;
    background: #fff;
    border-radius: 16px;
    box-shadow: 0 4px 16px #eee;
    padding: 10px;
  }
  </style>