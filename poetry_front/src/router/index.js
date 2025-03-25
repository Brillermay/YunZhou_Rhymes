import { createRouter, createWebHistory } from 'vue-router'
import PoetryRecommend from '@/components/PoetryRecommend.vue'
import PoetrySearch from '@/components/PoetrySearch.vue'
import PoetryGame from '@/components/PoetryTest.vue'
import Forum from '@/components/Forum.vue'
import feihua from '@/components/FeiHuaLing.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes :
   [
    {path: '/', redirect: '/recommend'},
  { path: '/recommend', component: PoetryRecommend },
  { path: '/search', component: PoetrySearch },
  { path: '/game', component: PoetryGame },
  { path: '/feihua', component: feihua },
  { path: '/forum', component: Forum }
]
})

export default router