import { createRouter, createWebHistory } from 'vue-router'
import PoetryRecommend from '@/components/PoetryRecommend.vue'
import PoetrySearch from '@/components/PoetrySearch.vue'
import PoemDetail from '@/components/PoemDetail.vue';
import PoetryGame from '@/components/PoetryTest.vue'
import Forum from '@/components/Forum.vue'
import ForumLogin from '@/components/ForumLogin.vue'
import FeiHua from '@/components/FeiHuaLing.vue'
import GameAll from "../components/gameAll.vue";


const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes :
   [
    {path: '/', redirect: '/recommend'},
  { path: '/recommend', component: PoetryRecommend },
  { path: '/search', component: PoetrySearch },
  { path: '/poem/:id', component: PoemDetail },
  { path: '/game', component: PoetryGame },
  { path: '/feihua', component: FeiHua },
  { path: '/forum', component: Forum },
  { path: '/forumlogin', component: ForumLogin },
     {path:'/play',component:GameAll}
]
})

export default router