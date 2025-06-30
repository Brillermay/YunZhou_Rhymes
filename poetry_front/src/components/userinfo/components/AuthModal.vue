<!-- filepath: c:\Users\Administrator\Desktop\YunZhou_Rhymes\poetry_front\src\components\userinfo\components\AuthModal.vue -->
<template>
  <div class="modal-overlay" @click="$emit('close')">
    <div class="auth-modal-content" @click.stop>
      <!-- 弹窗头部 -->
      <div class="auth-modal-header">
        <h3>{{ isLoginMode ? '登录' : '注册' }}</h3>
        <button @click="$emit('close')" class="close-btn">×</button>
      </div>

      <!-- 弹窗内容 -->
      <div class="auth-modal-body">
        <form @submit.prevent="handleSubmit">
          <div class="form-group">
            <label>用户名</label>
            <input
              v-model="form.username"
              type="text"
              placeholder="请输入用户名"
              class="form-input"
              required
              minlength="3"
              maxlength="20"
            >
          </div>

          <div class="form-group">
            <label>密码</label>
            <input
              v-model="form.password"
              type="password"
              placeholder="请输入密码（至少6位）"
              class="form-input"
              required
              minlength="6"
            >
          </div>

          <div v-if="!isLoginMode" class="form-group">
            <label>确认密码</label>
            <input
              v-model="form.confirmPassword"
              type="password"
              placeholder="请再次输入密码"
              class="form-input"
              required
            >
            <div v-if="form.confirmPassword && !passwordsMatch" class="password-mismatch">
              密码不一致
            </div>
          </div>

          <div v-if="!isLoginMode" class="form-group">
            <label>昵称（可选）</label>
            <input
              v-model="form.nickname"
              type="text"
              placeholder="请输入昵称"
              class="form-input"
              maxlength="20"
            >
          </div>

          <div v-if="!isLoginMode" class="form-group">
            <label>邮箱（可选）</label>
            <input
              v-model="form.email"
              type="email"
              placeholder="请输入邮箱地址"
              class="form-input"
            >
          </div>

          <button
            type="submit"
            :disabled="loading || !canSubmit"
            class="auth-submit-btn"
          >
            {{ loading ? '处理中...' : (isLoginMode ? '登录' : '注册') }}
          </button>
        </form>

        <!-- 切换登录/注册 -->
        <div class="auth-switch">
          <span v-if="isLoginMode">
            还没有账号？
            <button @click="$emit('switch-mode')" class="switch-btn">立即注册</button>
          </span>
          <span v-else>
            已有账号？
            <button @click="$emit('switch-mode')" class="switch-btn">立即登录</button>
          </span>
        </div>

        <!-- 错误提示 -->
        <div v-if="error" class="auth-error">
          {{ error }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, watch } from 'vue'

const props = defineProps({
  isLoginMode: {
    type: Boolean,
    default: true
  },
  loading: {
    type: Boolean,
    default: false
  },
  error: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['close', 'submit', 'switch-mode'])

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  email: ''
})

// 密码匹配检查
const passwordsMatch = computed(() => {
  return form.password === form.confirmPassword
})

// 提交按钮可用性
const canSubmit = computed(() => {
  if (!form.username.trim() || !form.password.trim()) return false
  
  if (!props.isLoginMode) {
    return passwordsMatch.value && form.password.length >= 6
  }
  
  return true
})

// 监听模式切换，重置表单
watch(() => props.isLoginMode, () => {
  resetForm()
})

// 重置表单
const resetForm = () => {
  form.username = ''
  form.password = ''
  form.confirmPassword = ''
  form.nickname = ''
  form.email = ''
}

// 处理提交
const handleSubmit = () => {
  if (!canSubmit.value) return
  
  const submitData = {
    username: form.username.trim(),
    password: form.password
  }
  
  if (!props.isLoginMode) {
    submitData.nickname = form.nickname.trim() || form.username.trim()
    submitData.email = form.email.trim()
  }
  
  emit('submit', submitData)
}

// 暴露重置方法给父组件
defineExpose({
  resetForm
})
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
  animation: fadeIn 0.3s ease-out;
}

.auth-modal-content {
  background: white;
  border-radius: 20px;
  width: 100%;
  max-width: 450px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease-out;
}

.auth-modal-header {
  padding: 2rem 2rem 1rem;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #fafafa, #ffffff);
  border-radius: 20px 20px 0 0;
}

.auth-modal-header h3 {
  margin: 0;
  color: #8c7853;
  font-size: 1.5rem;
  font-weight: 500;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #999;
  padding: 0.5rem;
  border-radius: 50%;
  transition: all 0.3s ease;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: #f0f0f0;
  color: #666;
  transform: rotate(90deg);
}

.auth-modal-body {
  padding: 2rem;
}

.form-group {
  margin-bottom: 1.2rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #333;
  font-weight: 500;
  font-size: 0.9rem;
}

.form-input {
  width: 100%;
  padding: 0.8rem 1rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 0.9rem;
  box-sizing: border-box;
  transition: all 0.3s ease;
  background: #fafafa;
}

.form-input:focus {
  outline: none;
  border-color: #8c7853;
  background: white;
  box-shadow: 0 0 0 3px rgba(140, 120, 83, 0.1);
}

.form-input::placeholder {
  color: #999;
}

.password-mismatch {
  color: #d63031;
  font-size: 0.8rem;
  margin-top: 0.3rem;
  animation: shake 0.3s ease-in-out;
}

.auth-submit-btn {
  width: 100%;
  padding: 1rem;
  background: linear-gradient(135deg, #8c7853, #6e5773);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 1rem;
  position: relative;
  overflow: hidden;
}

.auth-submit-btn::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.2), transparent);
  transition: left 0.5s;
}

.auth-submit-btn:hover:not(:disabled)::before {
  left: 100%;
}

.auth-submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(140, 120, 83, 0.3);
}

.auth-submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.auth-switch {
  text-align: center;
  margin-top: 1.5rem;
  color: #666;
  font-size: 0.9rem;
}

.switch-btn {
  background: none;
  border: none;
  color: #8c7853;
  text-decoration: underline;
  cursor: pointer;
  font-size: inherit;
  transition: color 0.3s ease;
}

.switch-btn:hover {
  color: #6e5773;
}

.auth-error {
  background: linear-gradient(135deg, #ffe6e6, #ffebee);
  color: #d63031;
  padding: 1rem;
  border-radius: 8px;
  margin-top: 1rem;
  text-align: center;
  font-size: 0.9rem;
  border-left: 4px solid #ff5252;
  animation: shake 0.5s ease-in-out;
}

/* 动画 */
@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from {
    transform: translateY(50px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-5px); }
  75% { transform: translateX(5px); }
}

/* 响应式 */
@media (max-width: 768px) {
  .modal-overlay {
    padding: 0.5rem;
  }
  
  .auth-modal-header {
    padding: 1.5rem;
  }
  
  .auth-modal-body {
    padding: 1.5rem;
  }
}
</style>