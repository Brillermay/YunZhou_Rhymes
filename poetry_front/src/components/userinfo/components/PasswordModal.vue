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
              placeholder="请输入新密码（至少6位）"
              required
            >
            
            <!-- 简化的密码强度 -->
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
          
          <!-- 简化的密码要求 -->
          <div class="password-tips">
            <p>密码要求：至少6位字符，建议包含字母和数字</p>
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

// 密码匹配检查
const passwordsMatch = computed(() => {
  return form.new === form.confirm
})

// 简化的密码强度计算
const passwordStrength = computed(() => {
  let score = 0
  if (form.new.length >= 6) score += 40
  if (form.new.length >= 8) score += 20
  if (/[a-zA-Z]/.test(form.new)) score += 20
  if (/[0-9]/.test(form.new)) score += 20
  
  return Math.min(score, 100)
})

const strengthPercentage = computed(() => passwordStrength.value)

const strengthClass = computed(() => {
  const strength = passwordStrength.value
  if (strength < 40) return 'weak'
  if (strength < 70) return 'fair'
  return 'good'
})

const strengthText = computed(() => {
  const strength = passwordStrength.value
  if (strength < 40) return '弱'
  if (strength < 70) return '一般'
  return '强'
})

// 表单验证
const isFormValid = computed(() => {
  return form.current.length > 0 &&
         form.new.length >= 6 &&
         passwordsMatch.value &&
         form.new !== form.current
})

// 提交处理
const handleSubmit = async () => {
  if (!isFormValid.value) return
  
  loading.value = true
  
  try {
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
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
  animation: fadeIn 0.3s ease-out;
}

.password-modal-content {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(248, 246, 240, 0.95));
  border-radius: 20px;
  width: 100%;
  max-width: 450px;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease-out;
  border: 2px solid rgba(140, 120, 83, 0.3);
}

.modal-header {
  padding: 1.5rem 2rem;
  border-bottom: 2px solid rgba(140, 120, 83, 0.2);
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, rgba(140, 120, 83, 0.1), rgba(140, 120, 83, 0.05));
  border-radius: 18px 18px 0 0;
}

.modal-header h3 {
  margin: 0;
  color: #8c7853;
  font-size: 1.3rem;
  font-weight: 600;
  font-family: 'Noto Serif SC', serif;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #8c7853;
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
  background: rgba(140, 120, 83, 0.1);
  color: #6e5773;
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
  color: #8c7853;
  font-weight: 500;
  font-size: 0.9rem;
  font-family: 'Noto Serif SC', serif;
}

.form-input {
  width: 100%;
  padding: 0.8rem 1rem;
  border: 2px solid rgba(140, 120, 83, 0.3);
  border-radius: 8px;
  font-size: 0.9rem;
  box-sizing: border-box;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.9);
}

.form-input:focus {
  outline: none;
  border-color: #8c7853;
  background: white;
  box-shadow: 0 0 0 3px rgba(140, 120, 83, 0.1);
}

.form-input::placeholder {
  color: rgba(140, 120, 83, 0.6);
}

.password-strength {
  margin-top: 0.5rem;
}

.strength-bar {
  height: 4px;
  background: rgba(140, 120, 83, 0.2);
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
  background: linear-gradient(90deg, #e74c3c, #c0392b);
}

.strength-fill.fair {
  background: linear-gradient(90deg, #f39c12, #e67e22);
}

.strength-fill.good {
  background: linear-gradient(90deg, #27ae60, #2ecc71);
}

.strength-text {
  font-size: 0.8rem;
  color: rgba(140, 120, 83, 0.8);
}

.password-mismatch {
  color: #e74c3c;
  font-size: 0.8rem;
  margin-top: 0.3rem;
  animation: shake 0.3s ease-in-out;
}

.password-tips {
  background: rgba(140, 120, 83, 0.08);
  border-radius: 8px;
  padding: 1rem;
  border-left: 4px solid #8c7853;
  border: 1px solid rgba(140, 120, 83, 0.2);
}

.password-tips p {
  margin: 0;
  color: #8c7853;
  font-size: 0.85rem;
  line-height: 1.4;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid rgba(140, 120, 83, 0.2);
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
  background: rgba(255, 255, 255, 0.9);
  color: #8c7853;
  border: 2px solid rgba(140, 120, 83, 0.3);
}

.btn-cancel:hover {
  background: rgba(140, 120, 83, 0.1);
  transform: translateY(-1px);
}

.btn-confirm {
  background: linear-gradient(135deg, #8c7853, #6e5773);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
}

.btn-confirm:hover:not(:disabled) {
  background: linear-gradient(135deg, #6e5773, #5a4a5f);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(140, 120, 83, 0.3);
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