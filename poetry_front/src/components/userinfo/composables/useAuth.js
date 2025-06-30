import { ref, computed } from 'vue'

export function useAuth(userStore) {
  const showAuthModal = ref(false)
  const isLoginMode = ref(true)
  const authLoading = ref(false)
  const authError = ref('')

  // 计算属性
  const isLoggedIn = computed(() => userStore.isAuthenticated)

  // 显示认证弹窗
  const showLogin = () => {
    isLoginMode.value = true
    showAuthModal.value = true
    resetAuthState()
  }

  const showRegister = () => {
    isLoginMode.value = false
    showAuthModal.value = true
    resetAuthState()
  }

  // 切换认证模式
  const switchAuthMode = () => {
    isLoginMode.value = !isLoginMode.value
    authError.value = ''
  }

  // 关闭认证弹窗
  const closeAuthModal = () => {
    showAuthModal.value = false
    resetAuthState()
  }

  // 重置认证状态
  const resetAuthState = () => {
    authError.value = ''
    authLoading.value = false
  }

  // 处理登录
  const handleLogin = async (credentials) => {
    authLoading.value = true
    authError.value = ''
    
    try {
      console.log('🔐 尝试登录:', credentials.username)
      const result = await userStore.apiLogin(credentials)
      
      if (result.success) {
        console.log('✅ 登录成功')
        closeAuthModal()
        return { success: true, message: '登录成功！' }
      } else {
        console.log('❌ 登录失败:', result.message)
        authError.value = result.message
        return { success: false, message: result.message }
      }
    } catch (error) {
      console.error('💥 登录请求异常:', error)
      authError.value = '网络错误，请稍后重试'
      return { success: false, message: '网络错误，请稍后重试' }
    } finally {
      authLoading.value = false
    }
  }

  // 处理注册
  const handleRegister = async (userData) => {
    authLoading.value = true
    authError.value = ''
    
    try {
      console.log('📝 尝试注册:', userData.username)
      const result = await userStore.apiRegister(userData)
      
      if (result.success) {
        console.log('✅ 注册成功')
        // 注册成功后切换到登录模式
        isLoginMode.value = true
        authError.value = ''
        return { success: true, message: '注册成功！请登录' }
      } else {
        console.log('❌ 注册失败:', result.message)
        authError.value = result.message
        return { success: false, message: result.message }
      }
    } catch (error) {
      console.error('💥 注册请求异常:', error)
      authError.value = '网络错误，请稍后重试'
      return { success: false, message: '网络错误，请稍后重试' }
    } finally {
      authLoading.value = false
    }
  }

  // 退出登录
  const logout = () => {
    return new Promise((resolve) => {
      if (confirm('确定要退出登录吗？')) {
        console.log('👋 用户退出登录')
        userStore.logout()
        resolve({ success: true, message: '已成功退出登录' })
      } else {
        resolve({ success: false, message: '取消退出' })
      }
    })
  }

  // 验证用户状态
  const validateUserStatus = async () => {
    try {
      if (!userStore.isAuthenticated) {
        return { valid: false, message: '用户未登录' }
      }
      
      const isValid = await userStore.validateUser()
      if (!isValid) {
        return { valid: false, message: '用户状态异常，请重新登录' }
      }
      
      return { valid: true, message: '用户状态正常' }
    } catch (error) {
      console.error('验证用户状态失败:', error)
      return { valid: false, message: '验证失败，请重新登录' }
    }
  }

  // 修改密码
  const changePassword = async (passwordData) => {
    try {
      console.log('🔐 尝试修改密码')
      
      // 验证用户状态
      const validation = await validateUserStatus()
      if (!validation.valid) {
        return { success: false, message: validation.message }
      }
      
      // 调用后端修改密码接口
      const response = await fetch('http://localhost:8081/user/changePWD', {
        method: 'POST', // 修正为POST方法
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          UserName: userStore.username,
          PassWord: passwordData.newPassword
        })
      })

      const result = await response.text()

      if (result.includes('修改成功')) {
        console.log('✅ 密码修改成功')
        return { success: true, message: '密码修改成功！' }
      } else {
        console.log('❌ 密码修改失败:', result)
        return { success: false, message: '密码修改失败：' + result }
      }
    } catch (error) {
      console.error('💥 修改密码失败:', error)
      return { success: false, message: '修改密码失败，请稍后重试' }
    }
  }

  return {
    // 状态
    showAuthModal,
    isLoginMode,
    authLoading,
    authError,
    isLoggedIn,
    
    // 方法
    showLogin,
    showRegister,
    switchAuthMode,
    closeAuthModal,
    handleLogin,
    handleRegister,
    logout,
    validateUserStatus,
    changePassword,
    resetAuthState
  }
}