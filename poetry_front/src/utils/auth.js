import { useUserStore } from '@/stores/user'

// 获取当前用户UID的便捷函数
export function getCurrentUid() {
  const userStore = useUserStore()
  return userStore.uid
}

// 检查是否已登录的便捷函数
export function isLoggedIn() {
  const userStore = useUserStore()
  return userStore.isAuthenticated
}

// 要求登录的装饰器函数
export function requireLogin(callback, errorCallback) {
  if (isLoggedIn()) {
    return callback()
  } else {
    if (errorCallback) {
      errorCallback()
    } else {
      alert('请先登录')
    }
  }
}

// 获取用户信息的便捷函数
export function getCurrentUser() {
  const userStore = useUserStore()
  return userStore.userInfo
}