<template>
  <div class="top-bar">
    <SellSlot />
    <BuyPackButton />
    <div class="currency">
      <span class="coin-icon">💰</span>
      <span class="coin-amount">{{ coins }}</span>
    </div>
  </div>
</template>

<script setup>
import SellSlot from '@/components/topbar/SellSlot.vue'
import BuyPackButton from '@/components/topbar/BuyPackButton.vue'
import { useCardActions } from '@/composables/useCardActions'

const { coins } = useCardActions()
</script>

<style scoped>
.top-bar {
  height: 180px;
  background-color: #2c3e50;
  color: white;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 20px;
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
</style>