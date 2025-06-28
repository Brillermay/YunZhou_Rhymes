import { createRouter, createWebHistory } from 'vue-router'
import PoetryRecommend from '@/components/PoetryRecommend.vue'
import PoetrySearch from '@/components/PoetrySearch.vue'
import PoetryGame from '@/components/PoetryTest.vue'
import Forum from '@/components/Forum.vue'
import ForumLogin from '@/components/ForumLogin.vue'
import FeiHua from '@/components/FeiHuaLing.vue'
import GameAll from "../components/gameAll.vue";
import UserInfo from "@/components/UserInfo.vue";
import QwenLLM  from "@/components/QwenLLM.vue";

// 导入管理员组件
import AdminLogin from '../components/admin/AdminLogin.vue'
import AdminDashboard from '../components/admin/AdminDashboard.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes :
   [
    {path: '/', redirect: '/recommend'},
  { path: '/recommend', component: PoetryRecommend },
  { path: '/search', component: PoetrySearch },
  { path: '/game', component: PoetryGame },
  { path: '/feihua', component: FeiHua },
  { path: '/forum', component: Forum },
  { path: '/forumlogin', component: ForumLogin },
  { path: '/userinfo', component: UserInfo },
  { path: '/play', component: GameAll },
  { path: '/qwenllm', component: QwenLLM },
    // 管理员路由
    {
      path: '/admin',
      name: 'AdminLogin',
      component: AdminLogin,
      meta: { layout: 'blank' } // 不使用默认布局
    },
    {
      path: '/admin/dashboard',
      name: 'AdminDashboard',
      component: AdminDashboard,
      meta: { 
        requiresAuth: true,
        requiresAdmin: true,
        layout: 'blank'
      }
    }
]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 检查管理员权限
  if (to.meta.requiresAdmin) {
    const adminToken = localStorage.getItem('adminToken')
    if (!adminToken) {
      next('/admin')
      return
    }
  }
  
  next()
})

export default router