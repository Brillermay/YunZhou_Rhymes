<template>
  <div class="layout">
    <div class="main-content">
      <!-- 左侧边栏 -->
      <div class="side-bar">
        <div class="tab-buttons">
          <button :class="['tab-button', { active: activeTab === 'achievements' }]" @click="activeTab = 'achievements'">
            成就
          </button>
          <button :class="['tab-button', { active: activeTab === 'recipes' }]" @click="activeTab = 'recipes'">
            合成表
          </button>
        </div>

        <div class="tab-content">
          <div v-if="activeTab === 'achievements'" class="achievements">
            <!-- 基础成就分类 -->
            <div class="achievement-category">
              <div class="category-header" @click="toggleBasicAchievements">
                <span class="toggle-icon">{{ basicAchievementsExpanded ? '▼' : '▶' }}</span>
                <h3>基础成就</h3>
              </div>
              <div v-show="basicAchievementsExpanded" class="category-content">
                <div v-for="achievement in basicAchievements" 
                  :key="achievement.id" 
                  :class="['achievement-item', { unlocked: achievement.unlocked }]">
                <span :class="['achievement-icon', { unlocked: achievement.unlocked }]">
                  {{ achievement.unlocked ? '🏆' : '🔒' }}
                </span>
                <div class="achievement-info">
                  <div class="achievement-name">{{ achievement.name }}</div>
                  <div class="achievement-desc">{{ achievement.description }}</div>
                </div>
              </div>
              </div>
            </div>

            <!-- 诗词收集成就分类 -->
            <div class="achievement-category">
              <div class="category-header" @click="togglePoemAchievements">
                <span class="toggle-icon">{{ poemAchievementsExpanded ? '▼' : '▶' }}</span>
                <h3>诗词收集</h3>
              </div>
              <div v-show="poemAchievementsExpanded" class="category-content">
                <div v-for="achievement in poemAchievements" 
                  :key="achievement.id" 
                  :class="['achievement-item', { unlocked: achievement.unlocked }]">
                <span :class="['achievement-icon', { unlocked: achievement.unlocked }]">
                  {{ achievement.unlocked ? '📜' : '🔒' }}
                </span>
                <div class="achievement-info">
                  <div class="achievement-name">{{ achievement.name }}</div>
                  <div class="achievement-desc">{{ achievement.description }}</div>
                </div>
              </div>
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
import { basicAchievements, poemAchievements, unlockAchievement, unlockPoemAchievement, checkPoemCollectorAchievement } from './achievements.js'
import { cardImages, getCardImageSrc, checkRecipe, checkCrafting, cardPrices, recipes, initializeRecipes } from './cards.js'
import { checkFactoryAchievement } from './factory.js'
import { showAchievementUnlocked, getPoemName } from './utils.js'

// 侧边栏相关
const activeTab = ref('achievements')
const isStackingMode = ref(false)
const basicAchievementsExpanded = ref(true)
const poemAchievementsExpanded = ref(true)
const toggleBasicAchievements = () => {
  basicAchievementsExpanded.value = !basicAchievementsExpanded.value
}
const togglePoemAchievements = () => {
  poemAchievementsExpanded.value = !poemAchievementsExpanded.value
}

// 金币
const coins = ref(100)

// 初始化配方表
initializeRecipes()

const gameCanvas = ref(null)
let game = null

onMounted(() => {
  const container = gameCanvas.value

  game = createGame({
    container,
    coins,
    basicAchievements,
    poemAchievements,
    isStackingMode,
    recipes,
    unlockRecipe: (card1Type, card2Type, resultType) => {
      // 解锁配方时，更新配方表
      const types = [card1Type, card2Type].sort()
      const recipeKey = types.join('_')
      const recipeIndex = recipes.value.findIndex(recipe => recipe.recipeKey === recipeKey)
      if (recipeIndex !== -1) {
        recipes.value[recipeIndex].card1.src = getCardImageSrc(types[0])
        recipes.value[recipeIndex].card2.src = getCardImageSrc(types[1])
        recipes.value[recipeIndex].result.src = getCardImageSrc(resultType)
        recipes.value[recipeIndex].unlocked = true
      }
      // 解锁成就
      unlockAchievement(7, name => showAchievementUnlocked(game, name))
    }
  })
})

onBeforeUnmount(() => {
  if (game) game.destroy(true)
})

// 引入主游戏逻辑
import { createGame } from './gameMain.js'
</script>

<style scoped>
/* ...原有样式保持不变... */
.layout {
  width: 100%;
  height: 100vh;
  display: flex;
  position: absolute;
  top: 0;
  left: 0;
  overflow: hidden;
}
.main-content {
  flex: 1;
  display: flex;
  min-height: 100%;
}
.right-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
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
  margin: 8px 0;
  padding: 10px;
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  position: relative;
  display: flex;
  align-items: center;
}
.achievement-item:not(.unlocked) {
  opacity: 1;
}
.achievement-item.unlocked {
  background-color: rgba(255, 215, 0, 0.1);
  border-color: white;
  box-shadow: 0 0 10px rgb(255, 255, 255);
}
.achievement-icon.unlocked {
  animation: unlocked-pulse 1s ease-in-out;
  color: white;
}
.achievement-item.unlocked .achievement-name {
  color: white;
  font-weight: bold;
}
@keyframes unlocked-pulse {
  0% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0.8; }
  100% { transform: scale(1); opacity: 1; }
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
.plus,
.equals {
  margin: 0 10px;
}
.game-container {
  flex: 1;
  display: flex;
  position: relative;
  margin: 0;
  padding: 0;
  background-color: #f5efe6;
}
.game-container>div {
  flex: 1;
  min-height: 0;
  touch-action: none;
  margin: 0;
  padding: 0;
}
.achievement-category {
  margin-bottom: 20px;
}
.category-header {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 10px;
  background-color: #8c7853;
  border-radius: 4px;
}
.category-header:hover {
  background-color: #9c8863;
}
.toggle-icon {
  margin-right: 10px;
  font-size: 12px;
}
.category-content {
  margin-top: 10px;
}
.achievement-item:hover {
  background-color: rgba(255, 255, 255, 0.15);
}
.achievement-icon {
  font-size: 1.2em;
  margin-right: 15px;
}
.achievement-icon.unlocked {
  animation: unlocked-pulse 1s ease-in-out;
}
</style>