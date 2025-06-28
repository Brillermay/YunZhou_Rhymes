import { createApp } from 'vue'
import { createPinia } from 'pinia';
import App from './App.vue'
import router from './router'
import pinia from './stores' // 新增：导入 Pinia

const app = createApp(App)

app.use(router)
app.use(pinia) // 新增：注册 Pinia

app.mount('#app')
