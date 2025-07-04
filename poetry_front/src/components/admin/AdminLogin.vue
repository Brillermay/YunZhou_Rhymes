<!-- filepath: c:\Users\Administrator\Desktop\YunZhou_Rhymes\poetry_front\src\components\admin\AdminLogin.vue -->
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

        <!-- 默认账号提示 -->
        <div class="demo-info">
          <p>🔧 开发模式 - 默认登录信息：</p>
          <p><strong>账号：</strong> admin</p>
          <p><strong>密码：</strong> admin123</p>
        </div>

        <!-- 登录表单 -->
        <form @submit.prevent="handleLogin" class="login-form">
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

          <!-- 快速登录按钮 -->
          <button
              type="button"
              @click="quickLogin"
              class="quick-login-btn"
          >
            🚀 一键登录
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
import API_BASE_URL from '@/config/api';

const router = useRouter();

// 默认账号配置
const DEFAULT_CREDENTIALS = {
  username: 'admin',
  password: 'admin123'
};

// 响应式数据
const loginForm = reactive({
  username: '',
  password: '',
  captcha: ''
});

const loginLoading = ref(false);
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

// 快速登录（自动填入默认账号密码）
const quickLogin = () => {
  loginForm.username = DEFAULT_CREDENTIALS.username;
  loginForm.password = DEFAULT_CREDENTIALS.password;
  loginForm.captcha = captchaCode.value;
  
  // 自动提交
  handleLogin();
};

// 登录处理（使用默认验证，不调用接口）
const handleLogin = async () => {
  loginError.value = '';
  loginSuccess.value = '';

  // 验证码检查
  if (loginForm.captcha.toUpperCase() !== captchaCode.value) {
    loginError.value = '验证码错误';
    refreshCaptcha();
    return;
  }

  // 账号密码验证（本地验证）
  if (loginForm.username !== DEFAULT_CREDENTIALS.username) {
    loginError.value = '管理员账号错误';
    refreshCaptcha();
    return;
  }

  if (loginForm.password !== DEFAULT_CREDENTIALS.password) {
    loginError.value = '密码错误';
    refreshCaptcha();
    return;
  }

  loginLoading.value = true;

  try {
    // 模拟登录加载时间
    await new Promise(resolve => setTimeout(resolve, 1000));

    // 模拟登录成功，保存管理员信息
    const adminInfo = {
      id: 1,
      username: 'admin',
      role: 'super_admin',
      permissions: ['all'],
      loginTime: new Date().toISOString()
    };

    // 生成模拟token
    const token = 'admin_token_' + Date.now();

    // 保存到localStorage
    localStorage.setItem('adminToken', token);
    localStorage.setItem('adminInfo', JSON.stringify(adminInfo));

    loginSuccess.value = '登录成功，正在跳转...';

    // 延迟跳转，显示成功信息
    setTimeout(() => {
      router.push('/admin/dashboard');
    }, 1000);

  } catch (error) {
    console.error('登录模拟失败:', error);
    loginError.value = '登录处理异常，请重试';
    refreshCaptcha();
  } finally {
    loginLoading.value = false;
  }
};

// 真实接口登录（暂时注释）
const handleRealLogin = async () => {
  /*
  loginError.value = '';

  // 验证码检查
  if (loginForm.captcha.toUpperCase() !== captchaCode.value) {
    loginError.value = '验证码错误';
    refreshCaptcha();
    return;
  }

  loginLoading.value = true;

  try {
    // 调用管理员登录接口
    const response = await fetch(`${API_BASE_URL}/admin/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        username: loginForm.username,
        password: loginForm.password
      })
    });

    const result = await response.json();

    if (result.success) {
      // 保存管理员信息
      localStorage.setItem('adminToken', result.token);
      localStorage.setItem('adminInfo', JSON.stringify(result.adminInfo));

      // 跳转到管理后台
      router.push('/admin/dashboard');
    } else {
      loginError.value = result.message || '登录失败';
      refreshCaptcha();
    }
  } catch (error) {
    console.error('登录失败:', error);
    loginError.value = '网络错误，请稍后重试';
    refreshCaptcha();
  } finally {
    loginLoading.value = false;
  }
  */
};

// 生命周期
onMounted(() => {
  refreshCaptcha();

  // 检查是否已经登录
  const token = localStorage.getItem('adminToken');
  if (token) {
    router.push('/admin/dashboard');
  }

  // 开发模式提示
  console.log('🔧 开发模式 - 默认登录信息:');
  console.log('账号: admin');
  console.log('密码: admin123');
  console.log('或者直接点击"一键登录"按钮');
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

/* 新增：开发模式提示 */
.demo-info {
  background: linear-gradient(135deg, #e8f5e8, #f0f8f0);
  border: 1px solid #4caf50;
  border-radius: 12px;
  padding: 1rem;
  margin-bottom: 1.5rem;
  text-align: center;
}

.demo-info p {
  margin: 0.3rem 0;
  color: #2e7d32;
  font-size: 0.85rem;
}

.demo-info p:first-child {
  font-weight: bold;
  margin-bottom: 0.5rem;
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

/* 新增：快速登录按钮 */
.quick-login-btn {
  width: 100%;
  padding: 1rem;
  background: linear-gradient(135deg, #4caf50, #45a049);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.quick-login-btn:hover {
  background: linear-gradient(135deg, #45a049, #3d8b40);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(76, 175, 80, 0.4);
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

/* 新增：成功提示 */
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

  .demo-info {
    font-size: 0.8rem;
  }
}
</style>