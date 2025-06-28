import { ref } from 'vue'
import { cardPrices } from '@/composables/useGameData'

export function useCardActions() {
    const coins = ref(1000)

    const handleSellCard = (event, scene) => {
        event.preventDefault()
        const cardType = event.dataTransfer.getData('cardType')
        const cardId = event.dataTransfer.getData('cardId')
        const price = cardPrices[cardType] || 0

        if (price > 0) {
            coins.value  += price
            if (scene && scene.cards)  {
                const soldCard = scene.cards.find(card  => card.getData('id')  === cardId)
                if (soldCard) {
                    soldCard.destroy()
                    scene.cards  = scene.cards.filter(card  => card.getData('id')  !== cardId)
                }
            }
        }
    }

    const handleBuyPack = () => {
        const packPrice = 100
        if (coins.value  >= packPrice) {
            coins.value  -= packPrice
            // 实际购买逻辑
        }
    }

    return {
        coins,
        handleSellCard,
        handleBuyPack
    }
}