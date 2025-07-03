<template>
  <div id="app">
    <Navigation />
    <div class="content">
      <router-view></router-view>
    </div>
  </div>
</template>


<script>
import Navigation from '@/components/Navigation.vue'

export default {
  name: 'home',
  components: {
    Navigation
  }
}
</script>


<style>
@import '../assets/style.css';
</style>