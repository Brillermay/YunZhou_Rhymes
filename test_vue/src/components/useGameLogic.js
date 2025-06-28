import { ref } from 'vue'
import Phaser from 'phaser'
import { cardImages } from '@/composables/useGameData'

export function useGameLogic(gameCanvas) {
    const sceneRef = ref(null)
    let game = null

    const preload = function() {
        cardImages.value.forEach(card  => {
            this.load.image(card.key,  card.src)
        })
    }

    const create = function() {
        this.cards  = []

        for (let i = 0; i < cardImages.value.length;  i++) {
            const card = this.physics.add.image(180  + i * 120, 250, cardImages.value[i].key)
                .setDisplaySize(100, 140)
                .setInteractive({ cursor: 'pointer', useHandCursor: true })
                .setCollideWorldBounds(true)
                .setBounce(0.8)
                .setData('type', cardImages.value[i].key)

            this.input.setDraggable(card)
            this.cards.push(card)
        }

        this.input.on('dragstart',  (pointer, gameObject) => {
            gameObject.setDepth(10)
            gameObject.body.moves  = false
        })

        this.input.on('drag',  (pointer, gameObject, dragX, dragY) => {
            gameObject.x = dragX
            gameObject.y = dragY
        })

        this.input.on('dragend',  (pointer, gameObject) => {
            gameObject.setDepth(1)
            gameObject.body.moves  = true

            this.cards.forEach(otherCard  => {
                if (otherCard !== gameObject &&
                    Phaser.Geom.Intersects.RectangleToRectangle(
                        gameObject.getBounds(),
                        otherCard.getBounds()
                    )) {
                    const card1Type = gameObject.getData('type')
                    const card2Type = otherCard.getData('type')

                    if ((card1Type === 'card1' && card2Type === 'card2') ||
                        (card1Type === 'card2' && card2Type === 'card1')) {
                        const x = (gameObject.x + otherCard.x) / 2
                        const y = (gameObject.y + otherCard.y) / 2
                        const merged = this.physics.add.image(x,  y, 'card3')
                            .setDisplaySize(100, 140)
                            .setInteractive({ cursor: 'pointer', useHandCursor: true })
                            .setCollideWorldBounds(true)
                            .setBounce(0.8)
                            .setData('type', 'card3')

                        this.input.setDraggable(merged)
                        gameObject.destroy()
                        otherCard.destroy()
                        this.cards  = this.cards.filter(card  => card !== gameObject && card !== otherCard)
                        this.cards.push(merged)
                    }
                }
            })
        })

        sceneRef.value  = this
    }

    const update = function() {
        // 游戏状态更新逻辑
    }

    const initGame = () => {
        if (!gameCanvas.value)  return

        const config = {
            type: Phaser.AUTO,
            width: gameCanvas.value.clientWidth,
            height: gameCanvas.value.clientHeight,
            parent: gameCanvas.value,
            backgroundColor: '#f5f5f5',
            scale: {
                mode: Phaser.Scale.RESIZE,
                autoCenter: Phaser.Scale.CENTER_BOTH
            },
            physics: {
                default: 'arcade',
                arcade: { gravity: { y: 0 }, debug: false }
            },
            scene: { preload, create, update }
        }

        game = new Phaser.Game(config)
    }

    const destroyGame = () => {
        if (game) game.destroy(true)
    }

    return {
        sceneRef,
        initGame,
        destroyGame
    }
}