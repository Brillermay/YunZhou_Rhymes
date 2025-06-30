<template>
  <router-view />
</template>

<script lang="ts">
import { defineComponent } from 'vue';

export default defineComponent({
  name: 'App',
});
</script>

<style>
/* 全局样式可以在这里定义 */
body {
  margin: 0;
  font-family: Arial, sans-serif;
  background-color: #f0f0f0;
}
</style>