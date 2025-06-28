<template>
  <div class="recipes">
    <h3>合成配方</h3>
    <div v-for="recipe in recipes" :key="recipe.id"  class="recipe-item">
      <div class="recipe-cards">
        <img :src="recipe.card1.src"  class="recipe-card" />
        <span class="plus">+</span>
        <img :src="recipe.card2.src"  class="recipe-card" />
        <span class="equals">=</span>
        <img :src="recipe.result.src"  class="recipe-card" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { useGameData } from '@/composables/useGameData'

const { recipes } = useGameData()
</script>

<style scoped>
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
</style>