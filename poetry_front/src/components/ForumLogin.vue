<template>
  <div class="forum-container">
    <div class="auth-container">
      <div class="form-title-bar">
        <h2>{{ isLoginPage ? '登录' : '注册' }}</h2>
      </div>

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
        <button type="submit" class="submit-btn" :disabled="loading">
          {{ loading ? '登录中...' : '登录' }}
        </button>
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
        <button type="submit" class="submit-btn" :disabled="loading">
          {{ loading ? '注册中...' : '注册' }}
        </button>
        <p class="toggle-link">已有账户？<span @click="toggleForm">登录</span></p>
      </form>
    </div>
  </div>
</template>
  
<script>
import { useUserStore } from '@/stores/user';
import axios from "axios";

export default {
  name: 'ForumLogin',
  data() {
    return {
      isLoginPage: true, // 控制当前显示登录或注册页面
      username: '',
      password: '',
      confirmPassword: '',
      loading: false, // 加载状态
      API_BASE_URL: 'http://localhost:8081/user' // 后端API基础URL
    };
  },
  methods: {
    // 登录方法 - 调用后端API
    // 注册方法 - 修正字段名称
    async register() {
      if (this.password !== this.confirmPassword) {
        alert("密码和确认密码不一致！");
        return;
      }

      if (!this.username || !this.password) {
        alert('请填写完整信息');
        return;
      }

      if (this.password.length < 6) {
        alert('密码长度至少6位');
        return;
      }

      this.loading = true;

      try {
        // 调用注册接口 - 修正字段名称为后端实体类字段名
        const response = await fetch(`${this.API_BASE_URL}/add`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            UserName: this.username,     // 改为 UserName（匹配后端实体类）
            PassWord: this.password      // 改为 PassWord（匹配后端实体类）
          })
        });

        const result = await response.text();
        
        if (result === '添加成功') {
          alert('注册成功！请登录');

          this.isLoginPage = true;
          this.password = '';
          this.confirmPassword = '';
        } else {
          console.log(JSON.stringify({
            UserName: this.username,     // 改为 UserName（匹配后端实体类）
            PassWord: this.password      // 改为 PassWord（匹配后端实体类）
          }))
          alert('注册失败，用户名可能已存在');
        }
      } catch (error) {
        console.error('注册失败:', error);
        alert('注册失败，请检查网络连接');
      } finally {
        this.loading = false;
      }
    },

    // 登录方法 - 同样需要修正
    async login() {
      if (!this.username || !this.password) {
        alert('请填写用户名和密码');
        return;
      }

      this.loading = true;
      
      try {
        // 调用登录接口 - 修正字段名称
        const loginResponse = await fetch(`${this.API_BASE_URL}/login`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            UserName: this.username,     // 改为 UserName
            PassWord: this.password      // 改为 PassWord
          })
        });

        const loginResult = await loginResponse.text();
        if (loginResult === '登录成功') {
          const userInfoResponse = await this.getUserInfo();
          console.log(userInfoResponse.uid);
          if (userInfoResponse) {
            localStorage.setItem('username', this.username);
            useUserStore().uid=userInfoResponse.uid;
            useUserStore().username=this.username;
            localStorage.setItem('isAdmin', userInfoResponse.isAdmin || false);
            alert('登录成功！');
            this.$router.push('/Forum');
          } else {
            alert('获取用户信息失败');
          }
        } else {
          alert('用户名或密码错误，请重新输入');
        }
      } catch (error) {
        console.error('登录失败:', error);
        alert('登录失败，请检查网络连接');
      } finally {
        this.loading = false;
      }
    },
    // 获取用户信息的辅助方法
    async getUserInfo() {
      try {
        // 这里需要根据你的数据库结构来实现
        // 由于你的登录接口没有返回用户详细信息，我们可能需要额外的接口
        // 暂时使用模拟数据，你可能需要添加一个根据用户名获取用户信息的接口
        const response = await axios.get(
            `http://127.0.0.1:8081/user/loginID/${encodeURIComponent(this.username)}`,
            {
              headers: {
                'Cache-Control': 'no-cache'
              },
              timeout: 10000  // 10秒超时
            }
        );
        // 模拟返回用户信息（实际应该从后端获取）
        return {
          uid: response.data, // 临时生成UID//await 127.0.0.1:8081/user/loginID/{username}
          name: this.username,              // 改为 name
          isAdmin: false // 默认非管理员
        };
      } catch (error) {
        console.error('获取用户信息失败:', error);
        return null;
      }
    },

    // 临时生成UID的方法（实际应该从后端获取）
    generateTemporaryUid() {
      return Date.now(); // 使用时间戳作为临时UID
    },

    // 根据UID获取用户名的方法（调用你的getName接口）
    async getUserName(uid) {
      try {
        const response = await fetch(`${this.API_BASE_URL}/getName/${uid}`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          }
        });

        if (response.ok) {
          const name = await response.text();
          return name;
        }
        return null;
      } catch (error) {
        console.error('获取用户名失败:', error);
        return null;
      }
    },

    // 切换表单（登录/注册）
    toggleForm() {
      this.isLoginPage = !this.isLoginPage;
      // 切换时清空表单
      this.username = '';
      this.password = '';
      this.confirmPassword = '';
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
  background: white;
  border-radius: 10px;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.1);
}

.form-title-bar {
  text-align: center;
  height: 80px;
  background: linear-gradient(to right, #8c7853, #6e5773);
  border-radius: 10px;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
  margin-bottom: 1.5rem;
}

.form-title-bar h2 {
  margin: 0;
  font-size: 36px;
  color: white;
  font-family: '楷体', cursive;
  line-height: 1.2;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
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
  background: #fdfaf5;
  box-sizing: border-box;
}

.form-field input:focus {
  border-color: #8c7853;
  outline: none;
  box-shadow: 0 0 0 2px rgba(140, 120, 83, 0.2);
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

.submit-btn:hover:not(:disabled) {
  background: linear-gradient(to right, #a3916a, #7c6488);
  transform: translateY(-2px);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
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
  transition: all 0.3s;
}

.toggle-link span:hover {
  letter-spacing: 1px;
  color: #6e5773;
}
</style>