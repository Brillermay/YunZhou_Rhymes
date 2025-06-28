<template>
  <div id="app">
    <home />
  </div>
</template>

<script>
import home from './view/home.vue'

export default {
  name: 'App',
  components: {
    home
  }
}

import { onMounted } from 'vue'
//import { useUserStore } from '@/stores/user'

//const userStore = useUserStore()

// 应用启动时初始化用户状态
onMounted(() => {
  console.log('🚀 应用启动，初始化用户状态...')
  //userStore.initFromStorage()
})

</script>