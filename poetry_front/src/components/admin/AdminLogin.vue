<template>
  <div class="admin-login-layout">
    <div class="login-container">
      <div class="login-card">
        <!-- 头部Logo -->
        <div class="login-header">
          <h1>🛠️ 云舟词渡</h1>
          <h2>管理后台</h2>
          <p class="subtitle">系统管理员登录</p>
        </div>

        <!-- 切换按钮 -->
        <div class="mode-switch">
          <button 
            :class="{ active: isLoginMode }" 
            @click="isLoginMode = true"
            class="mode-btn"
          >
            登录
          </button>
          <button 
            :class="{ active: !isLoginMode }" 
            @click="isLoginMode = false"
            class="mode-btn"
          >
            注册
          </button>
        </div>

        <!-- 登录表单 -->
        <form v-if="isLoginMode" @submit.prevent="handleLogin" class="login-form">
          <div class="form-group">
            <label>管理员账号</label>
            <input
                v-model="loginForm.username"
                type="text"
                placeholder="请输入管理员账号"
                class="form-input"
                required
            >
          </div>

          <div class="form-group">
            <label>密码</label>
            <input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                class="form-input"
                required
            >
          </div>

          <div class="form-group">
            <label>验证码</label>
            <div class="captcha-group">
              <input
                  v-model="loginForm.captcha"
                  type="text"
                  placeholder="请输入验证码"
                  class="form-input captcha-input"
                  required
              >
              <div class="captcha-code" @click="refreshCaptcha">
                {{ captchaCode }}
              </div>
            </div>
          </div>

          <button
              type="submit"
              :disabled="loginLoading"
              class="login-btn"
          >
            {{ loginLoading ? '登录中...' : '登录' }}
          </button>
        </form>

        <!-- 注册表单 -->
        <form v-else @submit.prevent="handleRegister" class="login-form">
          <div class="form-group">
            <label>管理员账号</label>
            <input
                v-model="registerForm.username"
                type="text"
                placeholder="请输入管理员账号"
                class="form-input"
                required
            >
          </div>

          <div class="form-group">
            <label>密码</label>
            <input
                v-model="registerForm.password"
                type="password"
                placeholder="请输入密码"
                class="form-input"
                required
            >
          </div>

          <div class="form-group">
            <label>昵称</label>
            <input
                v-model="registerForm.nickname"
                type="text"
                placeholder="请输入昵称"
                class="form-input"
                required
            >
          </div>

          <div class="form-group">
            <label>邮箱</label>
            <input
                v-model="registerForm.email"
                type="email"
                placeholder="请输入邮箱"
                class="form-input"
                required
            >
          </div>

          <div class="form-group">
            <label>验证码</label>
            <div class="captcha-group">
              <input
                  v-model="registerForm.captcha"
                  type="text"
                  placeholder="请输入验证码"
                  class="form-input captcha-input"
                  required
              >
              <div class="captcha-code" @click="refreshCaptcha">
                {{ captchaCode }}
              </div>
            </div>
          </div>

          <button
              type="submit"
              :disabled="registerLoading"
              class="login-btn"
          >
            {{ registerLoading ? '注册中...' : '注册管理员' }}
          </button>
        </form>

        <!-- 错误提示 -->
        <div v-if="loginError" class="error-message">
          {{ loginError }}
        </div>

        <!-- 成功提示 -->
        <div v-if="loginSuccess" class="success-message">
          {{ loginSuccess }}
        </div>

        <!-- 安全提示 -->
        <div class="security-notice">
          <p>🔒 仅限授权管理员访问</p>
          <p>系统会记录所有登录行为</p>
        </div>
      </div>
    </div>

    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="floating-poetry">
        <span>"欲穷千里目，更上一层楼"</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import API_BASE_URL from '@/config/api';

const router = useRouter();

// 响应式数据
const isLoginMode = ref(true);

const loginForm = reactive({
  username: '',
  password: '',
  captcha: ''
});

const registerForm = reactive({
  username: '',
  password: '',
  nickname: '',
  email: '',
  captcha: ''
});

const loginLoading = ref(false);
const registerLoading = ref(false);
const loginError = ref('');
const loginSuccess = ref('');
const captchaCode = ref('');

// 生成验证码
const generateCaptcha = () => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789';
  let result = '';
  for (let i = 0; i < 4; i++) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
};

// 刷新验证码
const refreshCaptcha = () => {
  captchaCode.value = generateCaptcha();
};

// 管理员登录处理
const handleLogin = async () => {
  loginError.value = '';
  loginSuccess.value = '';

  // 验证码检查
  if (loginForm.captcha.toUpperCase() !== captchaCode.value) {
    loginError.value = '验证码错误';
    refreshCaptcha();
    return;
  }

  loginLoading.value = true;

  try {
    // 调用管理员登录接口
    const response = await axios.post(`${API_BASE_URL}/admin/login`, {
      UserName: loginForm.username,
      PassWord: loginForm.password
    });

    console.log('登录接口返回:', response.data);

    if (response.data.success) {
      // 检查是否为管理员
      if (response.data.isAdmin) {
        // 只保存必要的验证信息到localStorage
        const adminToken = 'admin_token_' + Date.now();
        localStorage.setItem('adminToken', adminToken);
        
        loginSuccess.value = '管理员身份验证成功，正在跳转...';
        
        // 延迟跳转
        setTimeout(() => {
          router.push('/admin/dashboard');
        }, 1000);
      } else {
        loginError.value = '您不是管理员，无权访问管理后台';
        refreshCaptcha();
      }
    } else {
      loginError.value = response.data.message || '登录失败';
      refreshCaptcha();
    }
  } catch (error) {
    console.error('登录失败:', error);
    
    if (error.response) {
      loginError.value = error.response.data.message || '登录失败';
    } else if (error.request) {
      loginError.value = '网络错误，请检查网络连接';
    } else {
      loginError.value = '登录请求失败，请稍后重试';
    }
    refreshCaptcha();
  } finally {
    loginLoading.value = false;
  }
};

// 管理员注册处理
const handleRegister = async () => {
  loginError.value = '';
  loginSuccess.value = '';

  // 验证码检查
  if (registerForm.captcha.toUpperCase() !== captchaCode.value) {
    loginError.value = '验证码错误';
    refreshCaptcha();
    return;
  }

  registerLoading.value = true;

  try {
    // 调用管理员注册接口
    const response = await axios.post(`${API_BASE_URL}/admin/add`, {
      UserName: registerForm.username,
      PassWord: registerForm.password,
      Nickname: registerForm.nickname,
      Email: registerForm.email
    });

    console.log('注册接口返回:', response.data);

    if (response.data === '添加成功') {
      loginSuccess.value = '管理员注册成功，请登录';
      
      // 清空注册表单
      registerForm.username = '';
      registerForm.password = '';
      registerForm.nickname = '';
      registerForm.email = '';
      registerForm.captcha = '';
      
      // 切换到登录模式
      setTimeout(() => {
        isLoginMode.value = true;
        loginSuccess.value = '';
      }, 2000);
    } else {
      loginError.value = response.data || '注册失败';
      refreshCaptcha();
    }
  } catch (error) {
    console.error('注册失败:', error);
    
    if (error.response) {
      loginError.value = error.response.data || '注册失败';
    } else if (error.request) {
      loginError.value = '网络错误，请检查网络连接';
    } else {
      loginError.value = '注册请求失败，请稍后重试';
    }
    refreshCaptcha();
  } finally {
    registerLoading.value = false;
  }
};

// 生命周期
onMounted(() => {
  refreshCaptcha();
});
</script>

<style scoped>
.admin-login-layout {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.login-container {
  width: 100%;
  max-width: 400px;
  padding: 2rem;
  z-index: 10;
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 3rem 2rem;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
}

.login-header {
  text-align: center;
  margin-bottom: 2rem;
}

.login-header h1 {
  margin: 0;
  font-size: 2rem;
  color: #333;
  font-weight: bold;
}

.login-header h2 {
  margin: 0.5rem 0;
  font-size: 1.3rem;
  color: #667eea;
  font-weight: normal;
}

.subtitle {
  margin: 0;
  color: #666;
  font-size: 0.9rem;
}

.mode-switch {
  display: flex;
  background: #f0f0f0;
  border-radius: 12px;
  padding: 4px;
  margin-bottom: 2rem;
}

.mode-btn {
  flex: 1;
  padding: 0.8rem;
  background: transparent;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 0.9rem;
  color: #666;
}

.mode-btn.active {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  font-weight: 500;
}

.login-form {
  margin-bottom: 1.5rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #333;
  font-weight: 500;
}

.form-input {
  width: 100%;
  padding: 1rem;
  border: 2px solid #e1e5e9;
  border-radius: 12px;
  font-size: 1rem;
  box-sizing: border-box;
  transition: border-color 0.3s ease;
}

.form-input:focus {
  outline: none;
  border-color: #667eea;
}

.captcha-group {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.captcha-input {
  flex: 1;
}

.captcha-code {
  background: linear-gradient(45deg, #667eea, #764ba2);
  color: white;
  padding: 1rem;
  border-radius: 12px;
  font-weight: bold;
  cursor: pointer;
  user-select: none;
  transition: transform 0.2s ease;
  min-width: 80px;
  text-align: center;
}

.captcha-code:hover {
  transform: scale(1.05);
}

.login-btn {
  width: 100%;
  padding: 1rem;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-bottom: 1rem;
}

.login-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.error-message {
  background: #ffe6e6;
  color: #d32f2f;
  padding: 1rem;
  border-radius: 12px;
  margin-bottom: 1rem;
  text-align: center;
  font-size: 0.9rem;
}

.success-message {
  background: #e8f5e8;
  color: #2e7d32;
  padding: 1rem;
  border-radius: 12px;
  margin-bottom: 1rem;
  text-align: center;
  font-size: 0.9rem;
  border: 1px solid #4caf50;
}

.security-notice {
  text-align: center;
  color: #666;
  font-size: 0.8rem;
  line-height: 1.4;
}

.security-notice p {
  margin: 0.2rem 0;
}

.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.floating-poetry {
  position: absolute;
  top: 20%;
  right: 10%;
  color: rgba(255, 255, 255, 0.3);
  font-size: 1.2rem;
  font-style: italic;
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-20px); }
}

@media (max-width: 480px) {
  .login-container {
    padding: 1rem;
  }

  .login-card {
    padding: 2rem 1.5rem;
  }

  .captcha-group {
    flex-direction: column;
  }

  .captcha-code {
    width: 100%;
  }
}
</style>