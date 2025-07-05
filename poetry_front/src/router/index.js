import { createRouter, createWebHistory } from 'vue-router'
import PoetryRecommend from '@/components/PoetryRecommend.vue'
import PoetrySearch from '@/components/PoetrySearch.vue'
import PoetryGame from '@/components/PoetryTest.vue'
import Forum from '@/components/Forum.vue'
import FeiHua from '@/components/FeiHuaLing.vue'
import GameAll from "../components/gameAll.vue";
import UserInfo from "@/components/userinfo/Userinfo.vue";
import QwenLLM  from "@/components/QwenLLM.vue";

// 导入管理员组件
import AdminLogin from '../components/admin/AdminLogin.vue'
import AdminDashboard from '../components/admin/AdminDashboard.vue'

// 🔧 修复：HomePage.vue 而不是 Homepage.vue
import HomePage from '@/components/homepage/HomePage.vue'
import Lobby from '@/components/multiplayerAll/Lobby.vue'
import multiPlayPage from '@/components/multiplayerAll/Multiplayer.vue'


// 🆕 导入游戏中心组件
import GameCenter from '@/components/GameCenter.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes :
   [
       // 🔧 修改：默认重定向到 homepage
    { 
      path: '/', 
      redirect: '/homepage' 
    },
        // 🆕 游戏中心路由
    {
      path: '/game-center',
      name: 'GameCenter',
      component: GameCenter,
      meta: { 
        title: '诗词游戏中心 - 云舟词渡',
        layout: 'blank'
      }
    },
  { path: '/recommend', component: PoetryRecommend },
  { path: '/search', component: PoetrySearch },
  { path: '/game', component: PoetryGame },
  { path: '/feihua', component: FeiHua },
  { path: '/forum', component: Forum },
  { path: '/userinfo', component: UserInfo },
  { path: '/play', component: GameAll },
  { path: '/qwenllm', component: QwenLLM },
      // 🔧 修复：使用正确的组件名
    {
      path: '/homepage',
      name: 'HomePage',
      component: HomePage,  // ✅ 修复大小写
      meta: { 
        title: '云舟词渡 - 诗意人生，书香致远',
        layout: 'blank'
      }
    },
     {
       path:'/multiplay',component:Lobby

     },
     {
       path:"/multiplay_r",component:multiPlayPage
     },
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
  // 清理游戏中心可能残留的过渡元素
  if (from.path === '/game-center') {
    const overlay = document.querySelector('.game-transition-overlay')
    const style = document.querySelector('#game-center-transition-style')
    if (overlay) overlay.remove()
    if (style) style.remove()
  }
  
  // 清理可能残留的首页样式设置
  if (from.name === 'HomePage') {
    document.documentElement.style.overflow = ''
    document.body.style.overflow = ''
    document.body.style.height = ''
    document.body.style.position = ''
    document.body.style.width = ''
  }
  
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