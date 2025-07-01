<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="password-modal-content">
      <div class="modal-header">
        <h3>修改密码</h3>
        <button class="close-btn" @click="$emit('close')">×</button>
      </div>
      
      <div class="modal-body">
        <form @submit.prevent="handleSubmit" class="password-form">
          <div class="form-group">
            <label for="current">当前密码</label>
            <input
              id="current"
              v-model="form.current"
              type="password"
              class="form-input"
              placeholder="请输入当前密码"
              required
            >
          </div>
          
          <div class="form-group">
            <label for="new">新密码</label>
            <input
              id="new"
              v-model="form.new"
              type="password"
              class="form-input"
              placeholder="请输入新密码"
              required
            >
            
            <!-- 密码强度指示器 -->
            <div class="password-strength">
              <div class="strength-bar">
                <div 
                  class="strength-fill"
                  :class="strengthClass"
                  :style="{ width: strengthPercentage + '%' }"
                ></div>
              </div>
              <span class="strength-text">密码强度：{{ strengthText }}</span>
            </div>
          </div>
          
          <div class="form-group">
            <label for="confirm">确认新密码</label>
            <input
              id="confirm"
              v-model="form.confirm"
              type="password"
              class="form-input"
              placeholder="请再次输入新密码"
              required
            >
            <div v-if="form.confirm && !passwordsMatch" class="password-mismatch">
              密码不一致
            </div>
          </div>
          
          <!-- 密码要求提示 -->
          <div class="password-tips">
            <h4>密码要求：</h4>
            <ul>
              <li :class="{ valid: form.new.length >= 8 }">
                至少8个字符
              </li>
              <li :class="{ valid: hasUpperCase }">
                包含大写字母（推荐）
              </li>
              <li :class="{ valid: hasLowerCase }">
                包含小写字母（推荐）
              </li>
              <li :class="{ valid: hasNumber }">
                包含数字（推荐）
              </li>
              <li :class="{ valid: hasSpecialChar }">
                包含特殊字符（推荐）
              </li>
            </ul>
          </div>
          
          <div class="form-actions">
            <button type="button" @click="$emit('close')" class="btn-cancel">
              取消
            </button>
            <button 
              type="submit" 
              class="btn-confirm"
              :disabled="!isFormValid || loading"
            >
              <span v-if="loading" class="loading-spinner">🔄</span>
              {{ loading ? '保存中...' : '保存' }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'

// 组件事件
const emit = defineEmits(['close', 'confirm'])

// 表单数据
const form = reactive({
  current: '',
  new: '',
  confirm: ''
})

const loading = ref(false)

// 计算属性 - 密码验证规则
const hasUpperCase = computed(() => /[A-Z]/.test(form.new))
const hasLowerCase = computed(() => /[a-z]/.test(form.new))
const hasNumber = computed(() => /[0-9]/.test(form.new))
const hasSpecialChar = computed(() => /[!@#$%^&*(),.?":{}|<>]/.test(form.new))

// 密码匹配检查
const passwordsMatch = computed(() => {
  return form.new === form.confirm
})

// 密码强度计算
const passwordStrength = computed(() => {
  let score = 0
  if (form.new.length >= 8) score += 20
  if (form.new.length >= 12) score += 10
  if (hasUpperCase.value) score += 20
  if (hasLowerCase.value) score += 20
  if (hasNumber.value) score += 15
  if (hasSpecialChar.value) score += 15
  
  return Math.min(score, 100)
})

const strengthPercentage = computed(() => passwordStrength.value)

const strengthClass = computed(() => {
  const strength = passwordStrength.value
  if (strength < 30) return 'weak'
  if (strength < 60) return 'fair'
  if (strength < 80) return 'good'
  return 'strong'
})

const strengthText = computed(() => {
  const strength = passwordStrength.value
  if (strength < 30) return '弱'
  if (strength < 60) return '一般'
  if (strength < 80) return '良好'
  return '强'
})

// 表单验证
const isFormValid = computed(() => {
  return form.current.length > 0 &&
         form.new.length >= 8 &&
         passwordsMatch.value &&
         form.new !== form.current
})

// 提交处理
const handleSubmit = async () => {
  if (!isFormValid.value) return
  
  loading.value = true
  
  try {
    // 发送密码数据到父组件
    await emit('confirm', {
      currentPassword: form.current,
      newPassword: form.new
    })
    
    // 重置表单
    Object.assign(form, {
      current: '',
      new: '',
      confirm: ''
    })
    
  } catch (error) {
    console.error('密码修改失败:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 🔧 修复：完整的弹窗样式 */
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

.password-modal-content {
  background: white;
  border-radius: 20px;
  width: 100%;
  max-width: 480px;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease-out;
}

.modal-header {
  padding: 1.5rem 2rem;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #fafafa, #ffffff);
  border-radius: 20px 20px 0 0;
}

.modal-header h3 {
  margin: 0;
  color: #333;
  font-size: 1.3rem;
  font-weight: 600;
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

.modal-body {
  padding: 2rem;
  overflow-y: auto;
  max-height: calc(90vh - 100px);
}

.password-form {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.form-group label {
  color: #333;
  font-weight: 500;
  font-size: 0.9rem;
}

.form-input {
  width: 100%;
  padding: 0.8rem 1rem;
  border: 2px solid #e5e7eb;
  border-radius: 8px;
  font-size: 0.9rem;
  box-sizing: border-box;
  transition: all 0.3s ease;
  background: #fafafa;
}

.form-input:focus {
  outline: none;
  border-color: #4f46e5;
  background: white;
  box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
}

.form-input::placeholder {
  color: #999;
}

.password-strength {
  margin-top: 0.5rem;
}

.strength-bar {
  height: 4px;
  background: #e0e0e0;
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 0.3rem;
}

.strength-fill {
  height: 100%;
  transition: all 0.3s ease;
  border-radius: 2px;
}

.strength-fill.weak {
  background: linear-gradient(90deg, #ff6b6b, #ff8a8a);
}

.strength-fill.fair {
  background: linear-gradient(90deg, #ffa726, #ffcc02);
}

.strength-fill.good {
  background: linear-gradient(90deg, #66bb6a, #81c784);
}

.strength-fill.strong {
  background: linear-gradient(90deg, #4caf50, #66bb6a);
}

.strength-text {
  font-size: 0.8rem;
  color: #666;
}

.password-mismatch {
  color: #dc2626;
  font-size: 0.8rem;
  margin-top: 0.3rem;
  animation: shake 0.3s ease-in-out;
}

.password-tips {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-radius: 8px;
  padding: 1rem;
  margin-top: 0.5rem;
  border-left: 4px solid #4f46e5;
}

.password-tips h4 {
  margin: 0 0 0.5rem 0;
  color: #333;
  font-size: 0.9rem;
  font-weight: 600;
}

.password-tips ul {
  margin: 0;
  padding-left: 1.2rem;
  list-style: none;
}

.password-tips li {
  position: relative;
  color: #666;
  font-size: 0.85rem;
  margin-bottom: 0.3rem;
  padding-left: 1.5rem;
}

.password-tips li::before {
  content: '✗';
  position: absolute;
  left: 0;
  color: #dc2626;
  font-weight: bold;
}

.password-tips li.valid {
  color: #16a34a;
}

.password-tips li.valid::before {
  content: '✓';
  color: #16a34a;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid #f0f0f0;
}

.btn-cancel,
.btn-confirm {
  padding: 0.8rem 1.5rem;
  border-radius: 8px;
  font-size: 0.9rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  min-width: 80px;
}

.btn-cancel {
  background: #f1f5f9;
  color: #475569;
  border: 1px solid #e2e8f0;
}

.btn-cancel:hover {
  background: #e2e8f0;
  transform: translateY(-1px);
}

.btn-confirm {
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.btn-confirm:hover:not(:disabled) {
  background: linear-gradient(135deg, #4338ca, #6d28d9);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
}

.btn-confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.loading-spinner {
  animation: spin 1s linear infinite;
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

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 响应式 */
@media (max-width: 768px) {
  .modal-overlay {
    padding: 0.5rem;
  }
  
  .modal-header {
    padding: 1rem 1.5rem;
  }
  
  .modal-body {
    padding: 1.5rem;
  }
  
  .form-actions {
    flex-direction: column;
    gap: 0.8rem;
  }
  
  .btn-cancel,
  .btn-confirm {
    width: 100%;
  }
}
</style>