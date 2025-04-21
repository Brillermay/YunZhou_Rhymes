<template>
  <div class="poem-container">
    <div
        v-for="(line, index) in poem.content"
        :key="index"
        class="poem-line"
    >
      {{ line }}
    </div>
  </div>
</template>

<script setup>
defineProps({
  poem: {
    type: Object,
    required: true
  }
})
</script>

<style>
.poem-line {
  text-indent: 2em;
  margin: 8px 0;
  font-family: '楷体', cursive;
}
</style>