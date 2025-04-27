<template>
  <div class="forum-container">
    <div class="auth-container">
      <h2 v-if="isLoginPage">登录</h2>
      <h2 v-if="!isLoginPage">注册</h2>
  
      <!-- 登录表单 -->
      <form v-if="isLoginPage" @submit.prevent="login" class="auth-form">
        <div class="form-field">
          <label for="username">用户名</label>
          <input v-model="username" type="text" id="username" placeholder="请输入用户名" required />
        </div>
        <div class="form-field">
          <label for="password">密码</label>
          <input v-model="password" type="password" id="password" placeholder="请输入密码" required />
        </div>
        <button type="submit" class="submit-btn">登录</button>
        <p class="toggle-link">没有账户？<span @click="toggleForm">注册</span></p>
      </form>
  
      <!-- 注册表单 -->
      <form v-if="!isLoginPage" @submit.prevent="register" class="auth-form">
        <div class="form-field">
          <label for="username">用户名</label>
          <input v-model="username" type="text" id="username" placeholder="请输入用户名" required />
        </div>
        <div class="form-field">
          <label for="password">密码</label>
          <input v-model="password" type="password" id="password" placeholder="请输入密码" required />
        </div>
        <div class="form-field">
          <label for="confirmPassword">确认密码</label>
          <input v-model="confirmPassword" type="password" id="confirmPassword" placeholder="请确认密码" required />
        </div>
        <button type="submit" class="submit-btn">注册</button>
        <p class="toggle-link">已有账户？<span @click="toggleForm">登录</span></p>
      </form>
    </div>
  </div>
</template>
  
  <script>
  export default {
    name: 'ForumLogin',
    data() {
      return {
        isLoginPage: true, // 控制当前显示登录或注册页面
        username: '',
        password: '',
        confirmPassword: '',
      };
    },
    methods: {
      // 登录方法
      login() {
        if (this.username && this.password) {
          // 模拟登录成功
          localStorage.setItem('username', this.username);
          this.$router.push('/Forum'); // 登录后跳转到论坛页面
        } else {
          alert('请填写用户名和密码');
        }
      },
      // 注册方法
      register() {
        if (this.password !== this.confirmPassword) {
          alert("密码和确认密码不一致！");
          return;
        }
        if (this.username && this.password) {
          // 模拟注册成功
          localStorage.setItem('username', this.username);
          this.$router.push('/Forum'); // 注册后跳转到论坛页面
        } else {
          alert('请填写完整信息');
        }
      },
      // 切换表单（登录/注册）
      toggleForm() {
        this.isLoginPage = !this.isLoginPage;
      },
    },
  };
  </script>
  
  <style scoped>
.forum-container {
  width: 100%;
  padding: 20px;
  background: #f5efe6;
  min-height: 100vh;
  box-sizing: border-box; 
  overflow-x: hidden;     
}

.auth-container {
  width: 400px;
  margin: 100px auto;
  padding: 30px;
  background: white; /* 白色框体 */
  border-radius: 10px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1); /* 柔和阴影 */
}

.auth-container h2 {
  text-align: center;
  font-family: '楷体', cursive;
  color: #8c7853;
  font-size: 36px;
  margin-bottom: 1.5rem;
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
}

.form-field {
  display: flex;
  flex-direction: column;
}

.form-field label {
  font-weight: 500;
  margin-bottom: 5px;
  color: #6e5773;
  font-family: '楷体', cursive;
}

.form-field input {
  padding: 10px 14px;
  font-size: 1rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: #fdfaf5; /* 浅浅米黄色输入框背景 */
  box-sizing: border-box;
}

.submit-btn {
  padding: 12px 24px;
  background: linear-gradient(to right, #8c7853, #6e5773);
  border: none;
  border-radius: 30px;
  color: white;
  font-size: 1.1rem;
  font-weight: bold;
  cursor: pointer;
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  margin-top: 10px;
}

.submit-btn:hover {
  background: linear-gradient(to right, #a3916a, #7c6488);
  transform: translateY(-2px);
}

.toggle-link {
  text-align: center;
  font-size: 0.95rem;
  font-family: '楷体', cursive;
  color: #6e5773;
  margin-top: 10px;
}

.toggle-link span {
  color: #8c7853;
  cursor: pointer;
  font-weight: bold;
}

.toggle-link span:hover {
  text-decoration: underline;
}


  </style>
  