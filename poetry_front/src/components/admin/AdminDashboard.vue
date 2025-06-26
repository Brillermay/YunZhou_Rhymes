<!-- filepath: c:\Users\Administrator\Desktop\YunZhou_Rhymes\poetry_front\src\components\admin\AdminDashboard.vue -->
<template>
  <div class="admin-dashboard">
    <!-- 顶部导航 -->
    <header class="admin-header">
      <div class="header-left">
        <h1>📊 云舟词渡 - 管理后台</h1>
      </div>
      <div class="header-right">
        <div class="admin-info">
          <span>欢迎，{{ adminInfo.username }}</span>
          <button @click="logout" class="logout-btn">退出</button>
        </div>
      </div>
    </header>

    <!-- 主体内容 -->
    <div class="admin-main">
      <!-- 侧边导航 -->
      <aside class="admin-sidebar">
        <nav class="sidebar-nav">
          <div
              v-for="item in navItems"
              :key="item.key"
              class="nav-item"
              :class="{ active: activeTab === item.key }"
              @click="activeTab = item.key"
          >
            <span class="nav-icon">{{ item.icon }}</span>
            <span class="nav-text">{{ item.label }}</span>
          </div>
        </nav>
      </aside>

      <!-- 内容区域 -->
      <main class="admin-content">
        <!-- 概览统计 -->
        <div v-if="activeTab === 'overview'" class="overview-section">
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-icon">👥</div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.totalUsers }}</div>
                <div class="stat-label">总用户数</div>
                <div class="stat-change positive">+{{ stats.newUsersToday }} 今日新增</div>
              </div>
            </div>

            <div class="stat-card">
              <div class="stat-icon">📚</div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.totalPoems }}</div>
                <div class="stat-label">诗词总数</div>
                <div class="stat-change neutral">{{ stats.categoriesCount }} 个分类</div>
              </div>
            </div>

            <div class="stat-card">
              <div class="stat-icon">🎮</div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.totalGames }}</div>
                <div class="stat-label">游戏总场次</div>
                <div class="stat-change positive">+{{ stats.gamesToday }} 今日</div>
              </div>
            </div>

            <div class="stat-card">
              <div class="stat-icon">📈</div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.dailyVisits }}</div>
                <div class="stat-label">今日访问量</div>
                <div class="stat-change positive">{{ stats.visitGrowth }}% 增长</div>
              </div>
            </div>
          </div>

          <!-- 图表区域 -->
          <div class="charts-section">
            <div class="chart-card">
              <h3>用户增长趋势</h3>
              <div class="chart-placeholder">
                📊 用户增长图表 (待接入图表库)
              </div>
            </div>

            <div class="chart-card">
              <h3>热门诗词排行</h3>
              <div class="ranking-list">
                <div v-for="(poem, index) in hotPoems" :key="index" class="ranking-item">
                  <span class="rank">{{ index + 1 }}</span>
                  <span class="poem-title">{{ poem.title }}</span>
                  <span class="poem-views">{{ poem.views }} 次浏览</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 用户管理 -->
        <div v-if="activeTab === 'users'" class="users-section">
          <div class="section-header">
            <h2>用户管理</h2>
            <div class="section-actions">
              <input v-model="userSearchQuery" placeholder="搜索用户..." class="search-input">
              <button @click="refreshUsers" class="refresh-btn">🔄 刷新</button>
            </div>
          </div>

          <div class="data-table">
            <table>
              <thead>
              <tr>
                <th>用户ID</th>
                <th>用户名</th>
                <th>注册时间</th>
                <th>最后登录</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="user in filteredUsers" :key="user.id">
                <td>{{ user.id }}</td>
                <td>{{ user.username }}</td>
                <td>{{ formatDate(user.registerTime) }}</td>
                <td>{{ formatDate(user.lastLogin) }}</td>
                <td>
                    <span class="status-badge" :class="user.status">
                      {{ user.status === 'active' ? '正常' : '禁用' }}
                    </span>
                </td>
                <td>
                  <button @click="viewUser(user)" class="action-btn view">详情</button>
                  <button @click="toggleUserStatus(user)" class="action-btn toggle">
                    {{ user.status === 'active' ? '禁用' : '启用' }}
                  </button>
                </td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 内容管理 -->
        <div v-if="activeTab === 'content'" class="content-section">
          <div class="section-header">
            <h2>内容管理</h2>
            <div class="section-actions">
              <button @click="showAddPoem = true" class="add-btn">➕ 添加诗词</button>
              <button @click="importPoems" class="import-btn">📥 批量导入</button>
            </div>
          </div>

          <div class="content-filters">
            <select v-model="contentFilter.category" class="filter-select">
              <option value="">所有分类</option>
              <option value="唐诗">唐诗</option>
              <option value="宋词">宋词</option>
              <option value="元曲">元曲</option>
            </select>
            <input v-model="contentFilter.search" placeholder="搜索诗词..." class="search-input">
          </div>

          <div class="content-grid">
            <div v-for="poem in filteredPoems" :key="poem.id" class="poem-card">
              <h4>{{ poem.title }}</h4>
              <p class="poem-author">{{ poem.author }} · {{ poem.dynasty }}</p>
              <p class="poem-preview">{{ getPreview(poem.content) }}</p>
              <div class="poem-stats">
                <span>👀 {{ poem.views }}</span>
                <span>❤️ {{ poem.likes }}</span>
              </div>
              <div class="poem-actions">
                <button @click="editPoem(poem)" class="action-btn edit">编辑</button>
                <button @click="deletePoem(poem)" class="action-btn delete">删除</button>
              </div>
            </div>
          </div>
        </div>

        <!-- 系统设置 -->
        <div v-if="activeTab === 'system'" class="system-section">
          <div class="section-header">
            <h2>系统设置</h2>
          </div>

          <div class="settings-grid">
            <div class="setting-card">
              <h3>🔧 基础设置</h3>
              <div class="setting-item">
                <label>网站标题</label>
                <input v-model="systemSettings.siteTitle" class="setting-input">
              </div>
              <div class="setting-item">
                <label>网站描述</label>
                <textarea v-model="systemSettings.siteDescription" class="setting-textarea"></textarea>
              </div>
            </div>

            <div class="setting-card">
              <h3>🎮 游戏设置</h3>
              <div class="setting-item">
                <label>
                  <input type="checkbox" v-model="systemSettings.gameEnabled">
                  启用游戏功能
                </label>
              </div>
              <div class="setting-item">
                <label>每日游戏次数限制</label>
                <input type="number" v-model="systemSettings.dailyGameLimit" class="setting-input">
              </div>
            </div>

            <div class="setting-card">
              <h3>📊 数据设置</h3>
              <div class="setting-item">
                <button @click="exportData" class="export-btn">📤 导出数据</button>
                <button @click="clearCache" class="clear-btn">🗑️ 清理缓存</button>
              </div>
            </div>
          </div>

          <div class="settings-actions">
            <button @click="saveSettings" class="save-btn">💾 保存设置</button>
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

// 响应式数据
const activeTab = ref('overview');
const adminInfo = reactive({
  username: '管理员',
  role: 'super_admin'
});

// 导航菜单
const navItems = [
  { key: 'overview', label: '概览统计', icon: '📊' },
  { key: 'users', label: '用户管理', icon: '👥' },
  { key: 'content', label: '内容管理', icon: '📚' },
  { key: 'games', label: '游戏管理', icon: '🎮' },
  { key: 'system', label: '系统设置', icon: '⚙️' }
];

// 统计数据
const stats = reactive({
  totalUsers: 1234,
  newUsersToday: 23,
  totalPoems: 5678,
  categoriesCount: 12,
  totalGames: 9876,
  gamesToday: 156,
  dailyVisits: 2345,
  visitGrowth: 12.5
});

// 热门诗词
const hotPoems = ref([
  { title: '静夜思', views: 1234 },
  { title: '望庐山瀑布', views: 987 },
  { title: '春晓', views: 876 },
  { title: '登鹳雀楼', views: 765 },
  { title: '咏鹅', views: 654 }
]);

// 用户数据
const users = ref([
  {
    id: 1,
    username: 'user001',
    registerTime: '2024-01-15',
    lastLogin: '2024-06-26',
    status: 'active'
  },
  {
    id: 2,
    username: 'user002',
    registerTime: '2024-02-20',
    lastLogin: '2024-06-25',
    status: 'active'
  }
  // 更多用户数据...
]);

const userSearchQuery = ref('');

// 内容数据
const poems = ref([
  {
    id: 1,
    title: '静夜思',
    author: '李白',
    dynasty: '唐',
    content: '床前明月光，疑是地上霜。举头望明月，低头思故乡。',
    views: 1234,
    likes: 567,
    category: '唐诗'
  }
  // 更多诗词数据...
]);

const contentFilter = reactive({
  category: '',
  search: ''
});

// 系统设置
const systemSettings = reactive({
  siteTitle: '云舟词渡',
  siteDescription: '中华古典诗词学习平台',
  gameEnabled: true,
  dailyGameLimit: 10
});

// 计算属性
const filteredUsers = computed(() => {
  if (!userSearchQuery.value) return users.value;
  return users.value.filter(user =>
      user.username.toLowerCase().includes(userSearchQuery.value.toLowerCase())
  );
});

const filteredPoems = computed(() => {
  let filtered = poems.value;

  if (contentFilter.category) {
    filtered = filtered.filter(poem => poem.category === contentFilter.category);
  }

  if (contentFilter.search) {
    const query = contentFilter.search.toLowerCase();
    filtered = filtered.filter(poem =>
        poem.title.toLowerCase().includes(query) ||
        poem.author.toLowerCase().includes(query) ||
        poem.content.toLowerCase().includes(query)
    );
  }

  return filtered;
});

// 方法
const logout = () => {
  if (confirm('确定要退出管理后台吗？')) {
    localStorage.removeItem('adminToken');
    localStorage.removeItem('adminInfo');
    router.push('/admin');
  }
};

const formatDate = (dateStr) => {
  return new Date(dateStr).toLocaleDateString('zh-CN');
};

const getPreview = (content) => {
  return content.length > 50 ? content.substring(0, 50) + '...' : content;
};

const refreshUsers = () => {
  // TODO: 刷新用户数据
  console.log('刷新用户数据');
};

const viewUser = (user) => {
  // TODO: 查看用户详情
  console.log('查看用户:', user);
};

const toggleUserStatus = (user) => {
  user.status = user.status === 'active' ? 'banned' : 'active';
};

const editPoem = (poem) => {
  // TODO: 编辑诗词
  console.log('编辑诗词:', poem);
};

const deletePoem = (poem) => {
  if (confirm('确定要删除这首诗词吗？')) {
    // TODO: 删除诗词
    console.log('删除诗词:', poem);
  }
};

const saveSettings = () => {
  // TODO: 保存系统设置
  alert('设置已保存！');
};

const exportData = () => {
  // TODO: 导出数据
  alert('数据导出功能开发中...');
};

const clearCache = () => {
  if (confirm('确定要清理缓存吗？')) {
    // TODO: 清理缓存
    alert('缓存已清理！');
  }
};

// 生命周期
onMounted(() => {
  // 检查管理员权限
  const token = localStorage.getItem('adminToken');
  if (!token) {
    router.push('/admin');
  }

  // 加载管理员信息
  const savedAdminInfo = localStorage.getItem('adminInfo');
  if (savedAdminInfo) {
    Object.assign(adminInfo, JSON.parse(savedAdminInfo));
  }
});
</script>

<style scoped>
.admin-dashboard {
  min-height: 100vh;
  background: #f5f7fa;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

/* 顶部导航 */
.admin-header {
  background: white;
  height: 70px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 2rem;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left h1 {
  margin: 0;
  color: #667eea;
  font-size: 1.5rem;
}

.admin-info {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.logout-btn {
  background: #ff6b6b;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.3s ease;
}

.logout-btn:hover {
  background: #ff5252;
}

/* 主体布局 */
.admin-main {
  display: flex;
  min-height: calc(100vh - 70px);
}

/* 侧边栏 */
.admin-sidebar {
  width: 250px;
  background: white;
  box-shadow: 2px 0 10px rgba(0,0,0,0.1);
}

.sidebar-nav {
  padding: 1rem 0;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 1rem 1.5rem;
  cursor: pointer;
  transition: all 0.3s ease;
  border-right: 3px solid transparent;
}

.nav-item:hover {
  background: #f8f9ff;
  border-right-color: #667eea;
}

.nav-item.active {
  background: #667eea;
  color: white;
  border-right-color: #5a6fd8;
}

.nav-icon {
  font-size: 1.2rem;
  margin-right: 0.8rem;
  min-width: 20px;
}

.nav-text {
  font-size: 0.95rem;
}

/* 内容区域 */
.admin-content {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 1rem;
}

.stat-icon {
  font-size: 2.5rem;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 12px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-number {
  font-size: 1.8rem;
  font-weight: bold;
  color: #333;
  margin-bottom: 0.2rem;
}

.stat-label {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 0.3rem;
}

.stat-change {
  font-size: 0.8rem;
}

.stat-change.positive {
  color: #4caf50;
}

.stat-change.neutral {
  color: #666;
}

/* 图表区域 */
.charts-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
  margin-bottom: 2rem;
}

.chart-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
}

.chart-card h3 {
  margin: 0 0 1rem 0;
  color: #333;
}

.chart-placeholder {
  height: 200px;
  background: #f8f9ff;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #667eea;
  font-size: 1.1rem;
}

.ranking-list {
  max-height: 200px;
  overflow-y: auto;
}

.ranking-item {
  display: flex;
  align-items: center;
  padding: 0.8rem 0;
  border-bottom: 1px solid #f0f0f0;
}

.rank {
  background: #667eea;
  color: white;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  margin-right: 1rem;
}

.poem-title {
  flex: 1;
  color: #333;
}

.poem-views {
  color: #666;
  font-size: 0.9rem;
}

/* 通用样式 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.section-header h2 {
  margin: 0;
  color: #333;
  font-size: 1.5rem;
}

.section-actions {
  display: flex;
  gap: 1rem;
  align-items: center;
}

.search-input {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 0.9rem;
}

.refresh-btn, .add-btn, .import-btn {
  padding: 0.5rem 1rem;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background 0.3s ease;
}

.refresh-btn:hover, .add-btn:hover, .import-btn:hover {
  background: #5a6fd8;
}

/* 数据表格 */
.data-table {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
}

.data-table table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 1rem;
  text-align: left;
  border-bottom: 1px solid #f0f0f0;
}

.data-table th {
  background: #f8f9ff;
  font-weight: 500;
  color: #333;
}

.status-badge {
  padding: 0.3rem 0.8rem;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: 500;
}

.status-badge.active {
  background: #e8f5e8;
  color: #4caf50;
}

.status-badge.banned {
  background: #ffe8e8;
  color: #f44336;
}

.action-btn {
  padding: 0.3rem 0.6rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.8rem;
  margin-right: 0.5rem;
  transition: opacity 0.3s ease;
}

.action-btn.view {
  background: #e3f2fd;
  color: #1976d2;
}

.action-btn.edit {
  background: #fff3e0;
  color: #f57c00;
}

.action-btn.delete {
  background: #ffebee;
  color: #d32f2f;
}

.action-btn.toggle {
  background: #f3e5f5;
  color: #7b1fa2;
}

.action-btn:hover {
  opacity: 0.8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-main {
    flex-direction: column;
  }

  .admin-sidebar {
    width: 100%;
    order: 2;
  }

  .admin-content {
    order: 1;
    padding: 1rem;
  }

  .stats-grid {
    grid-template-columns: 1fr;
  }

  .charts-section {
    grid-template-columns: 1fr;
  }
}
</style>