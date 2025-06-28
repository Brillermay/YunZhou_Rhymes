import { ref } from 'vue'

// 游戏数据
export const achievements = ref([
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

export const recipes = ref([
    {
        id: 1,
        card1: { src: new URL('../assets/cards/card1.png',  import.meta.url).href  },
        card2: { src: new URL('../assets/cards/card2.png',  import.meta.url).href  },
        result: { src: new URL('../assets/cards/card3.png',  import.meta.url).href  }
    }
])

export const cardImages = ref([
    { key: 'card1', src: new URL('../assets/cards/card1.png',  import.meta.url).href  },
    { key: 'card2', src: new URL('../assets/cards/card2.png',  import.meta.url).href  },
    { key: 'card3', src: new URL('../assets/cards/card3.png',  import.meta.url).href  }
])

export const cardPrices = {
    card1: 30,
    card2: 50,
    card3: 100
}

export function useGameData() {
    return {
        achievements,
        recipes,
        cardImages,
        cardPrices
    }
}