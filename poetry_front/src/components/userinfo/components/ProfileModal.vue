<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="profile-modal-content">
      <div class="modal-header">
        <h3>编辑个人资料</h3>
        <button class="close-btn" @click="$emit('close')">×</button>
      </div>
      
      <div class="modal-body">
        <form @submit.prevent="handleSubmit" class="profile-form">
          <!-- 头像上传区域 -->
          <div class="avatar-upload-section">
            <div class="avatar-preview" @click="triggerFileInput">
              <img 
                :src="form.avatar || defaultAvatar" 
                alt="头像预览" 
                class="avatar-preview-img"
                @error="handleAvatarError"
              >
              <div class="avatar-upload-overlay">
                <span v-if="avatarUploading" class="upload-loading">
                  <span class="loading-spinner">🔄</span>
                  <span>压缩中...</span>
                </span>
                <template v-else>
                  <span class="upload-icon">📷</span>
                  <span class="upload-text">点击更换</span>
                </template>
              </div>
            </div>
            <input 
              ref="fileInput"
              type="file"
              accept="image/*"
              @change="handleAvatarUpload"
              style="display: none"
            >
          </div>

          <!-- 基本信息 -->
          <div class="form-group">
            <label for="username">用户名</label>
            <input
              id="username"
              v-model="form.username"
              type="text"
              class="form-input"
              disabled
              placeholder="用户名不可修改"
            >
          </div>
          
          <div class="form-group">
            <label for="nickname">昵称</label>
            <input
              id="nickname"
              v-model="form.nickname"
              type="text"
              class="form-input"
              placeholder="请输入昵称"
              maxlength="20"
            >
            <span class="form-hint">{{ form.nickname.length }}/20</span>
          </div>
          
          <div class="form-group">
            <label for="email">邮箱</label>
            <input
              id="email"
              v-model="form.email"
              type="email"
              class="form-input"
              placeholder="请输入邮箱地址"
            >
            <span v-if="emailError" class="form-error">{{ emailError }}</span>
          </div>

          <!-- 操作按钮 -->
          <div class="form-actions">
            <button type="button" @click="$emit('close')" class="btn-cancel">
              取消
            </button>
            <button type="button" @click="handleReset" class="btn-reset">
              重置
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
import { ref, reactive, computed, watch } from 'vue'

// Props
const props = defineProps({
  userInfo: {
    type: Object,
    required: true
  }
})

// Emits
const emit = defineEmits(['close', 'confirm'])

// 响应式数据
const loading = ref(false)
const fileInput = ref(null)
const avatarUploading = ref(false)

// 默认头像
const defaultAvatar = 'https://api.dicebear.com/7.x/avataaars/svg?seed=default'

// 表单数据
const form = reactive({
  username: '',
  nickname: '',
  email: '',
  avatar: ''
})

// 原始数据（用于重置）
const originalData = reactive({})

// 初始化表单
const initializeForm = () => {
  if (!props.userInfo) return
  
  form.username = props.userInfo.username || ''
  form.nickname = props.userInfo.nickname || props.userInfo.username || ''
  form.email = props.userInfo.email || ''
  form.avatar = props.userInfo.avatar || ''
  
  // 保存原始数据
  Object.assign(originalData, {
    nickname: form.nickname,
    email: form.email,
    avatar: form.avatar
  })
}

const triggerFileInput = () => {
  fileInput.value.click()
}

const handleAvatarUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return
  
  if (!file.type.startsWith('image/')) {
    alert('请选择图片文件')
    return
  }
  
  if (file.size > 5 * 1024 * 1024) {
    alert('头像文件不能超过5MB')
    return
  }
  
  avatarUploading.value = true
  
  try {
    const compressedBase64 = await compressImageToBase64(file)
    form.avatar = compressedBase64
    console.log('✅ 头像上传成功，压缩后大小:', compressedBase64.length)
  } catch (error) {
    console.error('💥 头像上传失败:', error)
    alert('头像上传失败，请稍后重试')
  } finally {
    avatarUploading.value = false
    event.target.value = ''
  }
}

const compressImageToBase64 = (file) => {
  return new Promise((resolve, reject) => {
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    const img = new Image()
    
    img.onload = () => {
      const maxWidth = 150
      const maxHeight = 150
      
      let { width, height } = img
      
      if (width > height) {
        if (width > maxWidth) {
          height = (height * maxWidth) / width
          width = maxWidth
        }
      } else {
        if (height > maxHeight) {
          width = (width * maxHeight) / height
          height = maxHeight
        }
      }
      
      canvas.width = width
      canvas.height = height
      ctx.drawImage(img, 0, 0, width, height)
      
      let quality = 0.8
      let compressedBase64 = canvas.toDataURL('image/jpeg', quality)
      
      while (compressedBase64.length > 50000 && quality > 0.1) {
        quality -= 0.1
        compressedBase64 = canvas.toDataURL('image/jpeg', quality)
      }
      
      if (compressedBase64.length > 50000) {
        canvas.width = 100
        canvas.height = 100
        ctx.drawImage(img, 0, 0, 100, 100)
        compressedBase64 = canvas.toDataURL('image/jpeg', 0.3)
      }
      
      resolve(compressedBase64)
    }
    
    img.onerror = reject
    img.src = URL.createObjectURL(file)
  })
}

const handleAvatarError = (event) => {
  event.target.src = defaultAvatar
}

const handleReset = () => {
  if (hasChanges.value) {
    if (confirm('确定要重置所有更改吗？')) {
      form.nickname = originalData.nickname
      form.email = originalData.email
      form.avatar = originalData.avatar
    }
  }
}

const handleSubmit = async () => {
  if (!isFormValid.value) return
  
  loading.value = true
  
  try {
    const submitData = {
      uid: props.userInfo.uid,
      nickname: form.nickname.trim(),
      email: form.email.trim(),
      avatar: form.avatar
    }
    
    await emit('confirm', submitData)
    
    Object.assign(originalData, {
      nickname: form.nickname,
      email: form.email,
      avatar: form.avatar
    })
    
    console.log('✅ 个人资料更新成功')
    
  } catch (error) {
    console.error('💥 个人资料更新失败:', error)
  } finally {
    loading.value = false
  }
}

// 计算属性
const emailError = computed(() => {
  if (!form.email) return ''
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(form.email) ? '' : '请输入有效的邮箱地址'
})

const isFormValid = computed(() => {
  return form.nickname.trim().length > 0 && 
         (form.email === '' || emailError.value === '')
})

const hasChanges = computed(() => {
  return form.nickname !== originalData.nickname ||
         form.email !== originalData.email ||
         form.avatar !== originalData.avatar
})

// 监听器
watch(() => props.userInfo, (newUserInfo) => {
  if (newUserInfo) {
    initializeForm()
  }
}, { immediate: true })

// 暴露方法给父组件
defineExpose({
  initializeForm
})
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

.profile-modal-content {
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.95), rgba(248, 246, 240, 0.95));
  border-radius: 20px;
  width: 100%;
  max-width: 420px;
  max-height: 90vh;
  overflow: hidden;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  animation: slideUp 0.3s ease-out;
  border: 2px solid rgba(140, 120, 83, 0.3);
}

.modal-header {
  padding: 1.2rem 1.5rem;
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
  font-size: 1.2rem;
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
  width: 32px;
  height: 32px;
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
  padding: 1.5rem;
  overflow-y: auto;
  max-height: calc(90vh - 80px);
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

/* 头像上传区域 */
.avatar-upload-section {
  text-align: center;
  padding: 1rem;
  background: rgba(140, 120, 83, 0.08);
  border-radius: 8px;
  border: 1px solid rgba(140, 120, 83, 0.2);
  transition: all 0.3s ease;
}

.avatar-upload-section:hover {
  background: rgba(140, 120, 83, 0.12);
}

.avatar-preview {
  position: relative;
  width: 80px;
  height: 80px;
  margin: 0 auto;
  cursor: pointer;
  border-radius: 50%;
  overflow: hidden;
  border: 3px solid rgba(140, 120, 83, 0.3);
  transition: all 0.3s ease;
}

.avatar-preview:hover {
  border-color: #8c7853;
  transform: scale(1.05);
}

.avatar-preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  color: white;
  gap: 0.3rem;
}

.avatar-preview:hover .avatar-upload-overlay {
  opacity: 1;
}

.upload-icon {
  font-size: 1.2rem;
}

.upload-text {
  font-size: 0.7rem;
  font-weight: 500;
}

.upload-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.3rem;
  font-size: 0.7rem;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.form-group label {
  color: #8c7853;
  font-weight: 500;
  font-size: 0.85rem;
  font-family: 'Noto Serif SC', serif;
}

.form-input {
  width: 100%;
  padding: 0.7rem 0.8rem;
  border: 2px solid rgba(140, 120, 83, 0.3);
  border-radius: 6px;
  font-size: 0.85rem;
  box-sizing: border-box;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.9);
}

.form-input:focus {
  outline: none;
  border-color: #8c7853;
  background: white;
  box-shadow: 0 0 0 2px rgba(140, 120, 83, 0.1);
}

.form-input:disabled {
  background: rgba(140, 120, 83, 0.05);
  color: rgba(140, 120, 83, 0.6);
  cursor: not-allowed;
}

.form-input::placeholder {
  color: rgba(140, 120, 83, 0.5);
}

.form-hint {
  font-size: 0.75rem;
  color: rgba(140, 120, 83, 0.7);
  text-align: right;
}

.form-error {
  font-size: 0.75rem;
  color: #e74c3c;
  animation: shake 0.3s ease-in-out;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.8rem;
  margin-top: 1rem;
  padding-top: 1rem;
  border-top: 1px solid rgba(140, 120, 83, 0.2);
}

.btn-cancel,
.btn-reset,
.btn-confirm {
  padding: 0.6rem 1rem;
  border-radius: 6px;
  font-size: 0.85rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  min-width: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.3rem;
}


.btn-cancel {
  background: linear-gradient(135deg, #dc3545, #c82333);
  color: white;
  border: 2px solid rgba(220, 53, 69, 0.3);
}

.btn-cancel:hover {
  background: linear-gradient(135deg, #c82333, #bd2130);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(220, 53, 69, 0.3);
}

.btn-reset {
  background: linear-gradient(135deg, #8c7853, #6e5773);
  color: white;
  border: 2px solid rgba(140, 120, 83, 0.3);
}

.btn-reset:hover {
  background: linear-gradient(135deg, #6e5773, #5a4a5f);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(140, 120, 83, 0.3);
}

.btn-confirm {
  background: linear-gradient(135deg, #8c7853, #6e5773);
  color: white;
}

.btn-confirm:hover:not(:disabled) {
  background: linear-gradient(135deg, #6e5773, #5a4a5f);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(140, 120, 83, 0.3);
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
    transform: translateY(30px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

@keyframes shake {
  0%, 100% { transform: translateX(0); }
  25% { transform: translateX(-3px); }
  75% { transform: translateX(3px); }
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
    padding: 1rem 1.2rem;
  }
  
  .modal-body {
    padding: 1.2rem;
  }
  
  .form-actions {
    flex-direction: column;
    gap: 0.6rem;
  }
  
  .btn-cancel,
  .btn-reset,
  .btn-confirm {
    width: 100%;
  }
  
  .avatar-preview {
    width: 70px;
    height: 70px;
  }
}
</style>