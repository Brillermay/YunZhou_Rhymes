import { defineStore } from 'pinia'
import API_BASE_URL from '../config/api'

export const useUserStore = defineStore('user', {
  state: () => ({
    // 基本用户信息
    uid: null,
    username: null,
    nickname: null,
    email: null,
    avatar: null, // 🔧 添加头像字段
    isAdmin: false,
    status: null,
    createTime: null, // 🔧 添加创建时间
    
    // 登录状态
    isLoggedIn: false,
    loginTime: null,
    
    // 客户端设置
    preferences: {
      theme: 'light',
      fontSize: 16
    }
  }),

  getters: {
    // 获取用户显示名称（优先昵称，其次用户名）
    displayName: (state) => {
      return state.nickname || state.username || '未登录用户'
    },
    
    // 检查是否已登录且状态正常
    isAuthenticated: (state) => {
      return state.isLoggedIn && 
             !!state.uid && 
             state.status === 'active'
    },
    
    // 检查登录是否过期（24小时）
    isLoginValid: (state) => {
      if (!state.loginTime) return false
      const now = new Date().getTime()
      const loginTime = new Date(state.loginTime).getTime()
      return (now - loginTime) < 24 * 60 * 60 * 1000
    },
    
    // 获取完整用户信息
    userInfo: (state) => ({
      uid: state.uid,
      username: state.username,
      nickname: state.nickname,
      email: state.email,
      avatar: state.avatar, // 🔧 添加头像字段
      isAdmin: state.isAdmin,
      status: state.status,
      createTime: state.createTime, // 🔧 添加创建时间
      displayName: state.nickname || state.username
    })
  },

  actions: {
    // 🔧 更新：支持新的登录响应格式
    login(userData) {
      console.log('🔑 用户登录:', userData)
      
      this.uid = userData.uid
      this.username = userData.username
      this.nickname = userData.nickname || userData.username
      this.email = userData.email || null
      this.avatar = userData.avatar || null // 🔧 添加头像字段
      this.isAdmin = userData.isAdmin || false
      this.status = userData.status || 'active'
      this.createTime = userData.createTime || null // 🔧 添加创建时间
      this.isLoggedIn = true
      this.loginTime = new Date().toISOString()
      
      // 保存到localStorage
      this.saveToStorage()
      console.log('✅ 用户状态已更新:', this.displayName)
    },

    // 用户登出
    logout() {
      console.log('🚪 用户登出:', this.displayName)
      this.$reset()
      this.clearStorage()
      console.log('✅ 用户状态已清空')
    },

    // 更新用户信息（用于个人资料修改）
    updateUserInfo(newInfo) {
      if (newInfo.nickname !== undefined) {
        this.nickname = newInfo.nickname
      }
      if (newInfo.email !== undefined) {
        this.email = newInfo.email
      }
      if (newInfo.avatar !== undefined) {
        this.avatar = newInfo.avatar // 🔧 添加头像更新
      }
      this.saveToStorage()
      console.log('🔄 用户信息已更新')
    },

    // 更新用户设置
    updatePreferences(newPreferences) {
      this.preferences = { ...this.preferences, ...newPreferences }
      this.saveToStorage()
    },

    // 从localStorage恢复状态
    initFromStorage() {
      console.log('🔄 从本地存储恢复用户状态...')
      
      try {
        const savedUserInfo = localStorage.getItem('userInfo')
        
        if (savedUserInfo) {
          const userData = JSON.parse(savedUserInfo)
          
          // 检查登录是否过期
          if (userData.loginTime) {
            const now = new Date().getTime()
            const loginTime = new Date(userData.loginTime).getTime()
            const isExpired = (now - loginTime) > 24 * 60 * 60 * 1000
            
            if (isExpired) {
              console.log('⏰ 登录已过期，清除状态')
              this.clearStorage()
              return
            }
          }
          
          // 恢复用户状态
          this.uid = userData.uid
          this.username = userData.username
          this.nickname = userData.nickname
          this.email = userData.email
          this.avatar = userData.avatar // 🔧 添加头像恢复
          this.isAdmin = userData.isAdmin
          this.status = userData.status
          this.createTime = userData.createTime // 🔧 添加创建时间恢复
          this.isLoggedIn = userData.isLoggedIn
          this.loginTime = userData.loginTime
          
          if (userData.preferences) {
            this.preferences = { ...this.preferences, ...userData.preferences }
          }
          
          console.log('✅ 用户状态恢复成功:', this.displayName)
        }
      } catch (error) {
        console.error('❌ 恢复用户状态失败:', error)
        this.clearStorage()
      }
    },

    // 保存到localStorage
    saveToStorage() {
      try {
        const userData = {
          uid: this.uid,
          username: this.username,
          nickname: this.nickname,
          email: this.email,
          avatar: this.avatar, // 🔧 添加头像保存
          isAdmin: this.isAdmin,
          status: this.status,
          createTime: this.createTime, // 🔧 添加创建时间保存
          isLoggedIn: this.isLoggedIn,
          loginTime: this.loginTime,
          preferences: this.preferences
        }
        
        localStorage.setItem('userInfo', JSON.stringify(userData))
        
        // 兼容现有代码
        localStorage.setItem('username', this.username || '')
        localStorage.setItem('uid', this.uid?.toString() || '')
        localStorage.setItem('isAdmin', this.isAdmin.toString())
        
        console.log('💾 用户状态已保存到本地存储')
      } catch (error) {
        console.error('❌ 保存用户状态失败:', error)
      }
    },

    // 清除localStorage
    clearStorage() {
      localStorage.removeItem('userInfo')
      localStorage.removeItem('username')
      localStorage.removeItem('uid')
      localStorage.removeItem('isAdmin')
      console.log('🗑️ 本地存储已清理')
    },

    // 🔧 新增：调用后端登录接口
    async apiLogin(credentials) {
      try {
        const response = await fetch(`${API_BASE_URL}/user/login`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            UserName: credentials.username,
            PassWord: credentials.password
          })
        })
        
        const data = await response.json()
        
        if (data.success) {
          this.login(data)
          return { success: true, message: '登录成功' }
        } else {
          return { success: false, message: data.message || '登录失败' }
        }
      } catch (error) {
        console.error('登录请求失败:', error)
        return { success: false, message: '网络错误，请重试' }
      }
    },

    // 🔧 新增：调用后端注册接口
    async apiRegister(userData) {
      try {
        const response = await fetch(`${API_BASE_URL}/user/add`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            UserName: userData.username,
            PassWord: userData.password,
            Nickname: userData.nickname,
            Email: userData.email
          })
        })
        
        const result = await response.text()
        
        if (result.includes('添加成功')) {
          return { success: true, message: '注册成功' }
        } else {
          return { success: false, message: result }
        }
      } catch (error) {
        console.error('注册请求失败:', error)
        return { success: false, message: '网络错误，请重试' }
      }
    },

    // 🔧 新增：验证用户状态
    async validateUser() {
      if (!this.uid) return false
      
      try {
        const response = await fetch(`${API_BASE_URL}/user/loginName/${this.uid}`)
        const username = await response.text()
        
        if (username && username === this.username) {
          return true
        } else {
          this.logout()
          return false
        }
      } catch (error) {
        console.error('用户状态验证失败:', error)
        return false
      }
    }
  }
})