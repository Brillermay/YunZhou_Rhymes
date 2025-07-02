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
            @click="switchTab(item.key)"
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
            <div class="stat-card" :class="{ loading: statsLoading }">
              <div class="stat-icon">👥</div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.totalUsers || 0 }}</div>
                <div class="stat-label">总用户数</div>
                <div class="stat-change positive">+{{ stats.newUsersToday || 0 }} 今日新增</div>
              </div>
            </div>

            <div class="stat-card" :class="{ loading: statsLoading }">
              <div class="stat-icon">📚</div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.totalPoems || 0 }}</div>
                <div class="stat-label">诗词总数</div>
                <div class="stat-change neutral">{{ stats.categoriesCount || 0 }} 个分类</div>
              </div>
            </div>

            <div class="stat-card" :class="{ loading: statsLoading }">
              <div class="stat-icon">💬</div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.totalComments || 0 }}</div>
                <div class="stat-label">评论总数</div>
                <div class="stat-change positive">+{{ stats.todayComments || 0 }} 今日</div>
              </div>
            </div>

            <div class="stat-card" :class="{ loading: statsLoading }">
              <div class="stat-icon">📝</div>
              <div class="stat-info">
                <div class="stat-number">{{ stats.totalTopics || 0 }}</div>
                <div class="stat-label">主题帖数</div>
                <div class="stat-change neutral">{{ stats.totalReplies || 0 }} 条回复</div>
              </div>
            </div>
          </div>

          <!-- 快速操作 -->
          <div class="quick-actions">
            <div class="action-card" @click="switchTab('poems')">
              <div class="action-icon">📚</div>
              <div class="action-text">
                <h4>管理诗词</h4>
                <p>添加、编辑、删除诗词内容</p>
              </div>
            </div>
            <div class="action-card" @click="switchTab('comments')">
              <div class="action-icon">💬</div>
              <div class="action-text">
                <h4>审核评论</h4>
                <p>查看和管理用户评论</p>
              </div>
            </div>
          </div>

          <!-- 最新活动 -->
          <div class="recent-activity">
            <h3>最新活动</h3>
            <div class="activity-list">
              <div v-for="activity in recentActivities" :key="activity.id" class="activity-item">
                <div class="activity-icon">{{ activity.icon }}</div>
                <div class="activity-content">
                  <div class="activity-text">{{ activity.text }}</div>
                  <div class="activity-time">{{ formatTime(activity.time) }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 诗词管理 -->
        <div v-if="activeTab === 'poems'" class="poems-section">
          <div class="section-header">
            <h2>诗词管理</h2>
            <div class="section-actions">
              <select v-model="poemFilters.category" @change="loadPoems" class="filter-select">
                <option value="">所有分类</option>
                <option v-for="category in poemCategories" :key="category" :value="category">
                  {{ category }}
                </option>
              </select>
              <input 
                v-model="poemFilters.search" 
                @input="debounceSearch"
                placeholder="搜索诗词..." 
                class="search-input"
              >
              <button @click="showAddPoemModal = true" class="add-btn">➕ 添加诗词</button>
              <button @click="loadPoems" class="refresh-btn" :disabled="poemsLoading">
                🔄 {{ poemsLoading ? '加载中...' : '刷新' }}
              </button>
            </div>
          </div>

          <!-- 诗词列表 -->
          <div class="poems-grid" v-if="!poemsLoading">
            <div v-for="poem in poems" :key="poem.PID" class="poem-card">
              <div class="poem-header">
                <h4>{{ poem.title }}</h4>
                <div class="poem-actions">
                  <button @click="editPoem(poem)" class="action-btn edit" title="编辑">✏️</button>
                  <button @click="deletePoem(poem)" class="action-btn delete" title="删除">🗑️</button>
                </div>
              </div>
              <p class="poem-author">{{ poem.poet }} · {{ poem.category }}</p>
              <div class="poem-content">{{ getPreview(poem.text, 100) }}</div>
              <div class="poem-meta">
                <span class="poem-id">ID: {{ poem.PID }}</span>
                <span class="poem-category">{{ poem.category }}</span>
              </div>
            </div>
          </div>

          <!-- 加载状态 -->
          <div v-if="poemsLoading" class="loading-state">
            <div class="loading-spinner"></div>
            <p>加载诗词数据中...</p>
          </div>

          <!-- 空状态 -->
          <div v-if="!poemsLoading && poems.length === 0" class="empty-state">
            <div class="empty-icon">📚</div>
            <p>暂无诗词数据</p>
            <button @click="showAddPoemModal = true" class="add-btn">添加第一首诗词</button>
          </div>

                    <!-- 分页 -->
                    <div v-if="!poemsLoading && poems.length > 0" class="pagination">
            <button 
              @click="changePage(1)" 
              :disabled="poemPagination.page <= 1"
              class="page-btn"
            >
              首页
            </button>
            <button 
              @click="changePage(poemPagination.page - 1)" 
              :disabled="poemPagination.page <= 1"
              class="page-btn"
            >
              上一页
            </button>
            
            <!-- 页码跳转输入框 -->
            <div class="page-jump">
              <span class="page-info">
                第 
                <input 
                  v-model.number="jumpToPage"
                  @keyup.enter="handlePageJump"
                  @blur="handlePageJump"
                  type="number" 
                  :min="1" 
                  :max="poemPagination.totalPages"
                  class="page-input"
                  :placeholder="poemPagination.page.toString()"
                > 
                页，共 {{ poemPagination.totalPages }} 页
              </span>
              <button @click="handlePageJump" class="jump-btn" :disabled="!isValidPage">
                跳转
              </button>
            </div>
            
            <button 
              @click="changePage(poemPagination.page + 1)" 
              :disabled="poemPagination.page >= poemPagination.totalPages"
              class="page-btn"
            >
              下一页
            </button>
            <button 
              @click="changePage(poemPagination.totalPages)" 
              :disabled="poemPagination.page >= poemPagination.totalPages"
              class="page-btn"
            >
              末页
            </button>
          </div>
        </div>

                <!-- 评论管理 -->
        <div v-if="activeTab === 'comments'" class="comments-section">
          <div class="section-header">
            <h2>{{ reviewMode ? '敏感内容审核' : '评论管理' }}</h2>
            <div class="section-actions">
              <!-- 审核模式切换 -->
              <button 
                @click="toggleReviewMode" 
                class="review-toggle-btn"
                :class="{ active: reviewMode }"
              >
                {{ reviewMode ? '📝 返回评论管理' : '🔍 审核模式' }}
              </button>
              
              <!-- 审核模式专用操作 -->
              <template v-if="reviewMode">
                <button 
                  @click="scanAllComments" 
                  :disabled="reviewData.scanning" 
                  class="scan-btn"
                >
                  {{ reviewData.scanning ? '扫描中...' : '🔍 扫描敏感内容' }}
                </button>
                <button 
                  @click="batchDeleteSensitive" 
                  :disabled="reviewData.processing || comments.length === 0"
                  class="batch-delete-btn"
                >
                  {{ reviewData.processing ? '删除中...' : '🗑️ 批量删除' }}
                </button>
              </template>
              
              <!-- 普通模式操作 -->
              <template v-else>
                <select v-model="commentFilters.hasTitle" @change="loadComments" class="filter-select">
                  <option value="">全部评论</option>
                  <option value="true">主题帖</option>
                  <option value="false">回复</option>
                </select>
                <select v-model="commentFilters.category" @change="loadComments" class="filter-select">
                  <option value="">所有分类</option>
                  <option v-for="category in commentCategories" :key="category" :value="category">
                    {{ category }}
                  </option>
                </select>
                <input 
                  v-model="commentFilters.search" 
                  @input="debounceCommentSearch"
                  placeholder="搜索评论..." 
                  class="search-input"
                >
              </template>
              
              <button @click="reviewMode ? loadSensitiveComments() : loadComments()" class="refresh-btn" :disabled="commentsLoading">
                🔄 {{ commentsLoading ? '加载中...' : '刷新' }}
              </button>
            </div>
          </div>

          <!-- 扫描结果概览 -->
          <div v-if="reviewMode && reviewData.scanResults.scanned" class="scan-results">
            <div class="result-summary">
              <div class="result-item">
                <span class="result-number">{{ reviewData.scanResults.total }}</span>
                <span class="result-label">总评论数</span>
              </div>
              <div class="result-item danger">
                <span class="result-number">{{ reviewData.scanResults.dangerous }}</span>
                <span class="result-label">敏感评论</span>
              </div>
              <div class="result-item safe">
                <span class="result-number">{{ reviewData.scanResults.total - reviewData.scanResults.dangerous }}</span>
                <span class="result-label">安全评论</span>
              </div>
            </div>
          </div>

          <!-- 评论列表 -->
          <div class="comments-list" v-if="!commentsLoading">
            <div v-for="comment in comments" :key="comment.CommentID" class="comment-card" :class="{ 'sensitive-card': reviewMode }">
              <!-- 敏感内容警告标识 -->
              <div v-if="reviewMode" class="sensitive-warning">
                ⚠️ 敏感内容
              </div>
              
              <div class="comment-header">
                <div class="comment-info">
                  <span class="comment-type" :class="{ 'is-topic': comment.hasTitle }">
                    {{ comment.hasTitle ? '主题帖' : '回复' }}
                  </span>
                  <span class="comment-id">ID: {{ comment.CommentID }}</span>
                  <span class="comment-category">{{ comment.Category }}</span>
                  <span class="comment-time">{{ formatTime(comment.Timestamp) }}</span>
                </div>
                <div class="comment-actions">
                  <!-- 审核模式按钮 -->
                  <template v-if="reviewMode">
                    <button @click="markCommentSafe(comment)" class="action-btn safe" title="标记为安全">
                      ✅ 标记安全
                    </button>
                    <button @click="deleteComment(comment)" class="action-btn delete" title="删除">
                      🗑️ 删除
                    </button>
                  </template>
                  <!-- 普通模式按钮 -->
                  <template v-else>
                    <button @click="viewCommentDetail(comment)" class="action-btn view" title="查看详情">👁️</button>
                    <button @click="deleteComment(comment)" class="action-btn delete" title="删除">🗑️</button>
                  </template>
                </div>
              </div>
              
              <div class="comment-title" v-if="comment.Title">
                <h4>{{ comment.Title }}</h4>
              </div>
              
              <div class="comment-content" :class="{ 'sensitive-content': reviewMode }">
                {{ getPreview(comment.Content, 150) }}
              </div>
              
              <div class="comment-stats">
                <span>👤 用户ID: {{ comment.PersonID }}</span>
                <span>👍 {{ comment.LikeCounts }} 点赞</span>
                <span>💬 {{ comment.CommentCounts }} 回复</span>
                <span v-if="comment.isAdmin" class="admin-badge">管理员</span>
              </div>
            </div>
          </div>

          <!-- 加载状态 -->
          <div v-if="commentsLoading" class="loading-state">
            <div class="loading-spinner"></div>
            <p>{{ reviewMode ? '加载敏感评论中...' : '加载评论数据中...' }}</p>
          </div>

          <!-- 空状态 -->
          <div v-if="!commentsLoading && comments.length === 0" class="empty-state">
            <div class="empty-icon">{{ reviewMode ? '🎉' : '💬' }}</div>
            <p>{{ reviewMode ? '暂无敏感评论，内容都很健康！' : '暂无评论数据' }}</p>
            <button v-if="reviewMode" @click="scanAllComments" :disabled="reviewData.scanning" class="scan-btn">
              {{ reviewData.scanning ? '扫描中...' : '重新扫描' }}
            </button>
          </div>

          <!-- 分页 -->
          <div v-if="!commentsLoading && comments.length > 0" class="pagination">
            <button 
              @click="changeCommentPage(commentPagination.page - 1)" 
              :disabled="commentPagination.page <= 1"
              class="page-btn"
            >
              上一页
            </button>
            <span class="page-info">
              第 {{ commentPagination.page }} 页，共 {{ commentPagination.totalPages }} 页
            </span>
            <button 
              @click="changeCommentPage(commentPagination.page + 1)" 
              :disabled="commentPagination.page >= commentPagination.totalPages"
              class="page-btn"
            >
              下一页
            </button>
          </div>
        </div>
      </main>
    </div>

    <!-- 添加诗词模态框 -->
    <div v-if="showAddPoemModal" class="modal-overlay" @click="closeAddPoemModal">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <h3>{{ editingPoem ? '编辑诗词' : '添加诗词' }}</h3>
          <button @click="closeAddPoemModal" class="close-btn">✕</button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="savePoem">
            <div class="form-group">
              <label>标题 *</label>
              <input v-model="poemForm.title" type="text" required class="form-input" placeholder="请输入诗词标题">
            </div>
            <div class="form-group">
              <label>作者 *</label>
              <input v-model="poemForm.poet" type="text" required class="form-input" placeholder="请输入作者姓名">
            </div>
            <div class="form-group">
              <label>分类</label>
              <select v-model="poemForm.category" class="form-input">
                <option value="">请选择分类</option>
                <option v-for="category in poemCategories" :key="category" :value="category">
                  {{ category }}
                </option>
                <option value="custom">自定义分类</option>
              </select>
              <input 
                v-if="poemForm.category === 'custom'"
                v-model="poemForm.customCategory" 
                type="text" 
                class="form-input mt-2" 
                placeholder="请输入自定义分类"
              >
            </div>
            <div class="form-group">
              <label>内容 *</label>
              <textarea v-model="poemForm.text" required class="form-textarea" rows="6" placeholder="请输入诗词内容，换行请使用回车"></textarea>
            </div>
            <div class="form-group">
              <label>背景介绍</label>
              <textarea v-model="poemForm.background" class="form-textarea" rows="4" placeholder="请输入诗词背景介绍"></textarea>
            </div>
            <div class="form-group">
              <label>赏析</label>
              <textarea v-model="poemForm.appreciation" class="form-textarea" rows="4" placeholder="请输入诗词赏析"></textarea>
            </div>
            <div class="form-group">
              <label>翻译</label>
              <textarea v-model="poemForm.translation" class="form-textarea" rows="4" placeholder="请输入诗词翻译"></textarea>
            </div>
            <div class="form-actions">
              <button type="button" @click="closeAddPoemModal" class="cancel-btn">取消</button>
              <button type="submit" :disabled="poemSaving" class="save-btn">
                {{ poemSaving ? '保存中...' : '保存' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- 评论详情模态框 -->
    <div v-if="showCommentDetailModal" class="modal-overlay" @click="closeCommentDetailModal">
      <div class="modal-content large" @click.stop>
        <div class="modal-header">
          <h3>评论详情</h3>
          <button @click="closeCommentDetailModal" class="close-btn">✕</button>
        </div>
        <div class="modal-body">
          <div v-if="selectedComment" class="comment-detail">
            <div class="detail-section">
              <h4>基本信息</h4>
              <div class="detail-grid">
                <div class="detail-item">
                  <label>评论ID:</label>
                  <span>{{ selectedComment.CommentID }}</span>
                </div>
                <div class="detail-item">
                  <label>用户ID:</label>
                  <span>{{ selectedComment.PersonID }}</span>
                </div>
                <div class="detail-item">
                  <label>类型:</label>
                  <span class="comment-type" :class="{ 'is-topic': selectedComment.hasTitle }">
                    {{ selectedComment.hasTitle ? '主题帖' : '回复' }}
                  </span>
                </div>
                <div class="detail-item">
                  <label>分类:</label>
                  <span>{{ selectedComment.Category }}</span>
                </div>
                <div class="detail-item">
                  <label>发布时间:</label>
                  <span>{{ formatTime(selectedComment.Timestamp) }}</span>
                </div>
                <div class="detail-item">
                  <label>管理员:</label>
                  <span :class="{ 'admin-badge': selectedComment.isAdmin }">
                    {{ selectedComment.isAdmin ? '是' : '否' }}
                  </span>
                </div>
              </div>
            </div>
            
            <div v-if="selectedComment.Title" class="detail-section">
              <h4>标题</h4>
              <div class="content-box">{{ selectedComment.Title }}</div>
            </div>
            
            <div class="detail-section">
              <h4>内容</h4>
              <div class="content-box">{{ selectedComment.Content }}</div>
            </div>
            
            <div class="detail-section">
              <h4>统计数据</h4>
              <div class="stats-row">
                <div class="stat-item">
                  <span class="stat-label">点赞数:</span>
                  <span class="stat-value">{{ selectedComment.LikeCounts }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">回复数:</span>
                  <span class="stat-value">{{ selectedComment.CommentCounts }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">父评论ID:</span>
                  <span class="stat-value">{{ selectedComment.parentID || '无' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import API_BASE_URL from '@/config/api';

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
  { key: 'poems', label: '诗词管理', icon: '📚' },
  { key: 'comments', label: '评论管理', icon: '💬' }
];

// 统计数据
const stats = reactive({
  totalUsers: 0,
  newUsersToday: 0,
  totalPoems: 0,
  categoriesCount: 0,
  totalComments: 0,
  todayComments: 0,
  totalTopics: 0,
  totalReplies: 0
});
const statsLoading = ref(false);

// 诗词管理
const poems = ref([]);
const poemCategories = ref([]);
const poemsLoading = ref(false);
const poemFilters = reactive({
  category: '',
  search: ''
});
const poemPagination = reactive({
  page: 1,
  size: 12,
  total: 0,
  totalPages: 0
});
// 页面跳转
const jumpToPage = ref('');

// 诗词表单
const showAddPoemModal = ref(false);
const editingPoem = ref(null);
const poemSaving = ref(false);
const poemForm = reactive({
  title: '',
  poet: '',
  text: '',
  category: '',
  customCategory: '',
  background: '',
  appreciation: '',
  translation: ''
});

// 评论管理
const comments = ref([]);
const commentCategories = ref([]);
const commentsLoading = ref(false);
const commentFilters = reactive({
  hasTitle: '',
  category: '',
  search: ''
});
const commentPagination = reactive({
  page: 1,
  size: 10,
  total: 0,
  totalPages: 0
});

// 评论详情
const showCommentDetailModal = ref(false);
const selectedComment = ref(null);

// 内容审核（集成到评论管理）
const reviewMode = ref(false); // 是否开启审核模式
const reviewData = reactive({
  scanResults: {
    total: 0,
    dangerous: 0,
    scanned: false
  },
  scanning: false,
  processing: false
});

// 最新活动
const recentActivities = ref([]);

// 搜索防抖
let searchTimeout = null;
let commentSearchTimeout = null;

// API 方法
const api = {
  // 诗词相关
  async getPoems(params = {}) {
    const query = new URLSearchParams({
      page: params.page || 1,
      size: params.size || 12,
      ...(params.search && { search: params.search }),
      ...(params.category && { category: params.category })
    });
    
    const response = await fetch(`${API_BASE_URL}/admin/poem/list?${query}`);
    return await response.json();
  },

  async getPoemCategories() {
    const response = await fetch(`${API_BASE_URL}/admin/poem/categories`);
    return await response.json();
  },

  async addPoem(poem) {
    const response = await fetch(`${API_BASE_URL}/admin/poem/add`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(poem)
    });
    return await response.json();
  },

  async updatePoem(pid, poem) {
    const response = await fetch(`${API_BASE_URL}/admin/poem/${pid}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(poem)
    });
    return await response.json();
  },

  async deletePoem(pid) {
    const response = await fetch(`${API_BASE_URL}/admin/poem/${pid}`, {
      method: 'DELETE'
    });
    return await response.json();
  },

  // 评论相关
  async getComments(params = {}) {
    const query = new URLSearchParams({
      page: params.page || 1,
      size: params.size || 10,
      ...(params.search && { search: params.search }),
      ...(params.category && { category: params.category }),
      ...(params.hasTitle !== '' && { hasTitle: params.hasTitle })
    });
    
    const response = await fetch(`${API_BASE_URL}/admin/comment/list?${query}`);
    return await response.json();
  },

  async getCommentStats() {
    const response = await fetch(`${API_BASE_URL}/admin/comment/stats`);
    return await response.json();
  },

  async deleteComment(commentId) {
    const response = await fetch(`${API_BASE_URL}/admin/comment/${commentId}`, {
      method: 'DELETE'
    });
    return await response.json();
  },

    // 内容审核相关
    async scanComments() {
    const response = await fetch(`${API_BASE_URL}/admin/review/scan`, {
      method: 'POST'
    });
    return await response.json();
  },

  async getSensitiveComments(params = {}) {
    const query = new URLSearchParams({
      page: params.page || 1,
      size: params.size || 20
    });
    
    const response = await fetch(`${API_BASE_URL}/admin/review/sensitive?${query}`);
    return await response.json();
  },

  async markCommentSafe(commentId) {
    const response = await fetch(`${API_BASE_URL}/admin/review/comment/${commentId}/mark-safe`, {
      method: 'PUT'
    });
    return await response.json();
  },

  async deleteSensitiveComment(commentId) {
    const response = await fetch(`${API_BASE_URL}/admin/review/comment/${commentId}`, {
      method: 'DELETE'
    });
    return await response.json();
  },

  async batchDeleteSensitive() {
    const response = await fetch(`${API_BASE_URL}/admin/review/sensitive/batch`, {
      method: 'DELETE'
    });
    return await response.json();
  }

};



// 数据加载方法
const loadStats = async () => {
  statsLoading.value = true;
  try {
    // 加载诗词统计
    const poemsResponse = await api.getPoems({ size: 1 });
    if (poemsResponse.success) {
      stats.totalPoems = poemsResponse.total;
    }

    // 加载分类
    const categoriesResponse = await api.getPoemCategories();
    if (categoriesResponse.success) {
      stats.categoriesCount = categoriesResponse.data.length;
    }

    // 加载评论统计
    const commentStatsResponse = await api.getCommentStats();
    if (commentStatsResponse.success) {
      Object.assign(stats, commentStatsResponse.data);
    }
  } catch (error) {
    console.error('加载统计数据失败:', error);
  } finally {
    statsLoading.value = false;
  }
};

const loadPoems = async () => {
  poemsLoading.value = true;
  try {
    const response = await api.getPoems({
      page: poemPagination.page,
      size: poemPagination.size,
      search: poemFilters.search,
      category: poemFilters.category
    });
    
    if (response.success) {
      poems.value = response.data;
      poemPagination.total = response.total;
      poemPagination.totalPages = response.totalPages;
    } else {
      console.error('加载诗词失败:', response.message);
    }
  } catch (error) {
    console.error('加载诗词失败:', error);
  } finally {
    poemsLoading.value = false;
  }
};

const loadPoemCategories = async () => {
  try {
    const response = await api.getPoemCategories();
    if (response.success) {
      poemCategories.value = response.data;
    }
  } catch (error) {
    console.error('加载诗词分类失败:', error);
  }
};

const loadComments = async () => {
  commentsLoading.value = true;
  try {
    const response = await api.getComments({
      page: commentPagination.page,
      size: commentPagination.size,
      search: commentFilters.search,
      category: commentFilters.category,
      hasTitle: commentFilters.hasTitle
    });
    
    if (response.success) {
      comments.value = response.data;
      commentPagination.total = response.total;
      commentPagination.totalPages = response.totalPages;
      
      // 提取评论分类
      const categories = [...new Set(response.data.map(c => c.Category).filter(Boolean))];
      commentCategories.value = categories;
    } else {
      console.error('加载评论失败:', response.message);
    }
  } catch (error) {
    console.error('加载评论失败:', error);
  } finally {
    commentsLoading.value = false;
  }
};

// 审核功能方法
const toggleReviewMode = () => {
  reviewMode.value = !reviewMode.value;
  if (reviewMode.value) {
    loadSensitiveComments();
  } else {
    loadComments();
  }
};

const loadSensitiveComments = async () => {
  commentsLoading.value = true;
  try {
    const response = await api.getSensitiveComments({
      page: commentPagination.page,
      size: commentPagination.size
    });
    if (response.success) {
      comments.value = response.data;
      commentPagination.total = response.total;
      commentPagination.totalPages = response.totalPages;
    }
  } catch (error) {
    console.error('加载敏感评论失败:', error);
  } finally {
    commentsLoading.value = false;
  }
};

const scanAllComments = async () => {
  reviewData.scanning = true;
  try {
    const response = await api.scanComments();
    if (response.success) {
      reviewData.scanResults = response.data;
      reviewData.scanResults.scanned = true;
      alert(`扫描完成！共检测到 ${response.data.dangerous} 条敏感评论`);
      if (reviewMode.value) {
        loadSensitiveComments();
      }
    } else {
      alert('扫描失败: ' + response.message);
    }
  } catch (error) {
    alert('扫描失败: ' + error.message);
  } finally {
    reviewData.scanning = false;
  }
};

const markCommentSafe = async (comment) => {
  if (!confirm('确定标记此评论为安全内容吗？')) return;
  
  try {
    const response = await api.markCommentSafe(comment.CommentID);
    if (response.success) {
      alert('标记成功');
      if (reviewMode.value) {
        loadSensitiveComments();
      } else {
        loadComments();
      }
      loadStats();
    } else {
      alert('操作失败: ' + response.message);
    }
  } catch (error) {
    alert('操作失败: ' + error.message);
  }
};

const batchDeleteSensitive = async () => {
  if (!confirm('确定要删除所有敏感评论吗？此操作不可撤销！')) return;
  
  reviewData.processing = true;
  
  try {
    const response = await api.batchDeleteSensitive();
    if (response.success) {
      alert(`批量删除成功，共删除 ${response.data.deletedCount} 条敏感评论`);
      if (reviewMode.value) {
        loadSensitiveComments();
      } else {
        loadComments();
      }
      loadStats();
    } else {
      alert('批量删除失败: ' + response.message);
    }
  } catch (error) {
    alert('操作失败: ' + error.message);
  } finally {
    reviewData.processing = false;
  }
};

// 工具方法
const formatTime = (timestamp) => {
  if (!timestamp) return '';
  try {
    const date = new Date(timestamp);
    return date.toLocaleString('zh-CN');
  } catch {
    return timestamp;
  }
};

const getPreview = (text, maxLength = 50) => {
  if (!text) return '';
  return text.length > maxLength ? text.substring(0, maxLength) + '...' : text;
};

const debounceSearch = () => {
  clearTimeout(searchTimeout);
  searchTimeout = setTimeout(() => {
    poemPagination.page = 1;
    loadPoems();
  }, 500);
};

const debounceCommentSearch = () => {
  clearTimeout(commentSearchTimeout);
  commentSearchTimeout = setTimeout(() => {
    commentPagination.page = 1;
    loadComments();
  }, 500);
};

// 界面切换
const switchTab = (tab) => {
  activeTab.value = tab;
  if (tab === 'poems') {
    loadPoems();
    loadPoemCategories();
  } else if (tab === 'comments') {
    loadComments();
  } else if (tab === 'overview') {
    loadStats();
  }
};

// 分页
const changePage = (page) => {
  if (page >= 1 && page <= poemPagination.totalPages) {
    poemPagination.page = page;
    loadPoems();
    // 滚动到顶部，方便查看新加载的内容
    document.querySelector('.admin-content').scrollTop = 0;
  }
};

// 页面跳转相关
const isValidPage = computed(() => {
  const page = parseInt(jumpToPage.value);
  return page >= 1 && page <= poemPagination.totalPages && page !== poemPagination.page;
});

const handlePageJump = () => {
  const page = parseInt(jumpToPage.value);
  if (page >= 1 && page <= poemPagination.totalPages) {
    changePage(page);
    jumpToPage.value = ''; // 清空输入框
  } else {
    // 如果输入无效，重置为当前页
    jumpToPage.value = '';
  }
};

const changeCommentPage = (page) => {
  if (page >= 1 && page <= commentPagination.totalPages) {
    commentPagination.page = page;
    loadComments();
  }
};

// 诗词操作
const editPoem = (poem) => {
  editingPoem.value = poem;
  Object.assign(poemForm, {
    title: poem.title,
    poet: poem.poet,
    text: poem.text,
    category: poem.category,
    background: poem.background || '',
    appreciation: poem.appreciation || '',
    translation: poem.translation || ''
  });
  showAddPoemModal.value = true;
};

const deletePoem = async (poem) => {
  if (!confirm(`确定要删除诗词《${poem.title}》吗？`)) return;
  
  try {
    const response = await api.deletePoem(poem.PID);
    if (response.success) {
      alert('删除成功');
      loadPoems();
      loadStats();
    } else {
      alert('删除失败: ' + response.message);
    }
  } catch (error) {
    alert('删除失败: ' + error.message);
  }
};

const savePoem = async () => {
  poemSaving.value = true;
  
  try {
    const poemData = {
      title: poemForm.title,
      poet: poemForm.poet,
      text: poemForm.text,
      category: poemForm.category === 'custom' ? poemForm.customCategory : poemForm.category,
      background: poemForm.background,
      appreciation: poemForm.appreciation,
      translation: poemForm.translation
    };

    let response;
    if (editingPoem.value) {
      response = await api.updatePoem(editingPoem.value.PID, poemData);
    } else {
      response = await api.addPoem(poemData);
    }

    if (response.success) {
      alert(editingPoem.value ? '更新成功' : '添加成功');
      closeAddPoemModal();
      loadPoems();
      loadPoemCategories();
      loadStats();
    } else {
      alert('操作失败: ' + response.message);
    }
  } catch (error) {
    alert('操作失败: ' + error.message);
  } finally {
    poemSaving.value = false;
  }
};

const closeAddPoemModal = () => {
  showAddPoemModal.value = false;
  editingPoem.value = null;
  Object.assign(poemForm, {
    title: '',
    poet: '',
    text: '',
    category: '',
    customCategory: '',
    background: '',
    appreciation: '',
    translation: ''
  });
};

// 评论操作
const viewCommentDetail = (comment) => {
  selectedComment.value = comment;
  showCommentDetailModal.value = true;
};

const deleteComment = async (comment) => {
  const message = comment.hasTitle 
    ? `确定要删除主题帖《${comment.Title}》及其所有回复吗？`
    : `确定要删除这条回复吗？`;
    
  if (!confirm(message)) return;
  
  try {
    const response = await api.deleteComment(comment.CommentID);
    if (response.success) {
      alert('删除成功');
      loadComments();
      loadStats();
    } else {
      alert('删除失败: ' + response.message);
    }
  } catch (error) {
    alert('删除失败: ' + error.message);
  }
};

const closeCommentDetailModal = () => {
  showCommentDetailModal.value = false;
  selectedComment.value = null;
};

// 其他操作
const logout = () => {
  if (confirm('确定要退出管理后台吗？')) {
    localStorage.removeItem('adminToken');
    localStorage.removeItem('adminInfo');
    router.push('/admin');
  }
};

// 生命周期
onMounted(() => {
  // 检查管理员权限
  const token = localStorage.getItem('adminToken');
  if (!token) {
    router.push('/admin');
    return;
  }

  // 加载管理员信息
  const savedAdminInfo = localStorage.getItem('adminInfo');
  if (savedAdminInfo) {
    Object.assign(adminInfo, JSON.parse(savedAdminInfo));
  }

  // 加载初始数据
  loadStats();
  
  // 设置最新活动示例数据
  recentActivities.value = [
    { id: 1, icon: '📚', text: '新增诗词《春晓》', time: new Date().toISOString() },
    { id: 2, icon: '💬', text: '用户发表新评论', time: new Date(Date.now() - 30000).toISOString() },
    { id: 3, icon: '👥', text: '新用户注册', time: new Date(Date.now() - 60000).toISOString() }
  ];
});
</script>

<style scoped>
/* 基础样式保持原有的 */
.admin-dashboard {
  min-height: 100vh;
  background: #f5f7fa;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
}

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

.admin-main {
  display: flex;
  min-height: calc(100vh - 70px);
}

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
  transition: transform 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-card.loading {
  opacity: 0.7;
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

/* 快速操作 */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.action-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  display: flex;
  align-items: center;
  gap: 1rem;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0,0,0,0.15);
}

.action-icon {
  font-size: 2rem;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 10px;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-text h4 {
  margin: 0 0 0.3rem 0;
  color: #333;
  font-size: 1.1rem;
}

.action-text p {
  margin: 0;
  color: #666;
  font-size: 0.9rem;
}

/* 最新活动 */
.recent-activity {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
}

.recent-activity h3 {
  margin: 0 0 1rem 0;
  color: #333;
  font-size: 1.2rem;
}

.activity-list {
  max-height: 300px;
  overflow-y: auto;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 1rem;
  padding: 0.8rem 0;
  border-bottom: 1px solid #f0f0f0;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  font-size: 1.2rem;
  background: #f8f9ff;
  border-radius: 8px;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.activity-text {
  color: #333;
  font-size: 0.9rem;
}

.activity-time {
  color: #999;
  font-size: 0.8rem;
  margin-top: 0.2rem;
}

/* 通用样式 */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
  flex-wrap: wrap;
  gap: 1rem;
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
  flex-wrap: wrap;
}

.search-input, .filter-select {
  padding: 0.5rem 1rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 0.9rem;
  min-width: 150px;
}

.refresh-btn, .add-btn, .page-btn {
  padding: 0.5rem 1rem;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background 0.3s ease;
}

.refresh-btn:hover, .add-btn:hover, .page-btn:hover {
  background: #5a6fd8;
}

.refresh-btn:disabled, .page-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* 诗词网格 */
.poems-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.poem-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  transition: transform 0.3s ease;
}

.poem-card:hover {
  transform: translateY(-2px);
}

.poem-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.8rem;
}

.poem-header h4 {
  margin: 0;
  color: #333;
  font-size: 1.1rem;
}

.poem-actions {
  display: flex;
  gap: 0.5rem;
}

.poem-author {
  color: #666;
  font-size: 0.9rem;
  margin: 0 0 1rem 0;
}

.poem-content {
  color: #555;
  line-height: 1.6;
  margin-bottom: 1rem;
  min-height: 3rem;
}

.poem-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.8rem;
  color: #999;
}

.poem-category {
  background: #f0f0f0;
  padding: 0.2rem 0.6rem;
  border-radius: 12px;
  font-size: 0.7rem;
}

/* 评论列表 */
.comments-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  margin-bottom: 2rem;
}

.comment-card {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  transition: transform 0.3s ease;
}

.comment-card:hover {
  transform: translateY(-2px);
}

.comment-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.comment-info {
  display: flex;
  gap: 1rem;
  align-items: center;
  flex-wrap: wrap;
}

.comment-type {
  background: #e3f2fd;
  color: #1976d2;
  padding: 0.2rem 0.6rem;
  border-radius: 12px;
  font-size: 0.7rem;
  font-weight: 500;
}

.comment-type.is-topic {
  background: #fff3e0;
  color: #f57c00;
}

.comment-id, .comment-category, .comment-time {
  font-size: 0.8rem;
  color: #666;
}

.comment-actions {
  display: flex;
  gap: 0.5rem;
}

.comment-title h4 {
  margin: 0 0 1rem 0;
  color: #333;
  font-size: 1.1rem;
}

.comment-content {
  color: #555;
  line-height: 1.6;
  margin-bottom: 1rem;
}

.comment-stats {
  display: flex;
  gap: 1rem;
  align-items: center;
  font-size: 0.8rem;
  color: #666;
  flex-wrap: wrap;
}

.admin-badge {
  background: #ffebee;
  color: #d32f2f;
  padding: 0.2rem 0.6rem;
  border-radius: 12px;
  font-size: 0.7rem;
  font-weight: 500;
}

/* 操作按钮 */
.action-btn {
  padding: 0.3rem 0.6rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 1rem;
  transition: opacity 0.3s ease;
  background: transparent;
}

.action-btn:hover {
  opacity: 0.7;
}

.action-btn.edit {
  color: #f57c00;
}

.action-btn.delete {
  color: #d32f2f;
}

.action-btn.view {
  color: #1976d2;
}

/* 加载和空状态 */
.loading-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 3rem;
  text-align: center;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #667eea;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.empty-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  opacity: 0.5;
}

/* 分页 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 2rem;
}

.page-info {
  color: #666;
  font-size: 0.9rem;
}

/* 模态框 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.modal-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.3);
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-content.large {
  max-width: 800px;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h3 {
  margin: 0;
  color: #333;
  font-size: 1.3rem;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #666;
  padding: 0.5rem;
  border-radius: 50%;
  transition: background 0.3s ease;
}

.close-btn:hover {
  background: #f0f0f0;
}

.modal-body {
  padding: 1.5rem;
}

/* 表单样式 */
.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #333;
  font-weight: 500;
}

.form-input, .form-textarea {
  width: 100%;
  padding: 0.8rem;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 0.9rem;
  font-family: inherit;
  box-sizing: border-box;
  transition: border-color 0.3s ease;
}

.form-input:focus, .form-textarea:focus {
  outline: none;
  border-color: #667eea;
}

.form-textarea {
  resize: vertical;
  min-height: 80px;
}

.mt-2 {
  margin-top: 0.5rem;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2rem;
}

.cancel-btn {
  padding: 0.8rem 1.5rem;
  background: #f5f5f5;
  color: #666;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background 0.3s ease;
}

.cancel-btn:hover {
  background: #eeeeee;
}

.save-btn {
  padding: 0.8rem 1.5rem;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background 0.3s ease;
}

.save-btn:hover {
  background: #5a6fd8;
}

.save-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* 评论详情样式 */
.comment-detail {
  max-height: 70vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 2rem;
}

.detail-section h4 {
  margin: 0 0 1rem 0;
  color: #333;
  font-size: 1.1rem;
  border-bottom: 2px solid #667eea;
  padding-bottom: 0.5rem;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
}

.detail-item label {
  font-weight: 500;
  color: #666;
  font-size: 0.9rem;
}

.detail-item span {
  color: #333;
}

.content-box {
  background: #f8f9ff;
  padding: 1rem;
  border-radius: 8px;
  border-left: 4px solid #667eea;
  white-space: pre-wrap;
  line-height: 1.6;
}

.stats-row {
  display: flex;
  gap: 2rem;
  flex-wrap: wrap;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.stat-label {
  color: #666;
  font-size: 0.9rem;
}

.stat-value {
  color: #333;
  font-weight: 500;
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
  
  .poems-grid {
    grid-template-columns: 1fr;
  }
  
  .section-header {
    flex-direction: column;
    align-items: stretch;
  }
  
  .section-actions {
    justify-content: stretch;
  }
  
  .section-actions > * {
    flex: 1;
    min-width: 0;
  }
  
  .modal-content {
    margin: 1rem;
    max-width: none;
  }
  
  .detail-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-row {
    flex-direction: column;
    gap: 1rem;
  }
}

@media (max-width: 480px) {
  .admin-header {
    padding: 0 1rem;
  }
  
  .header-left h1 {
    font-size: 1.2rem;
  }
  
  .admin-info {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .comment-info {
    flex-direction: column;
    align-items: stretch;
  }
  
  .comment-stats {
    flex-direction: column;
    align-items: stretch;
    gap: 0.5rem;
  }
}

/* 审核功能集成样式 */
.review-toggle-btn {
  padding: 0.5rem 1rem;
  background: #ff9800;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s ease;
}

.review-toggle-btn:hover {
  background: #f57c00;
}

.review-toggle-btn.active {
  background: #f44336;
}

.scan-btn {
  padding: 0.5rem 1rem;
  background: #2196f3;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background 0.3s ease;
}

.scan-btn:hover {
  background: #1976d2;
}

.scan-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.batch-delete-btn {
  padding: 0.5rem 1rem;
  background: #f44336;
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: background 0.3s ease;
}

.batch-delete-btn:hover {
  background: #d32f2f;
}

.batch-delete-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.scan-results {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  box-shadow: 0 4px 15px rgba(0,0,0,0.1);
  margin-bottom: 1.5rem;
}

.result-summary {
  display: flex;
  gap: 2rem;
  justify-content: center;
  flex-wrap: wrap;
}

.result-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 1rem;
  border-radius: 8px;
  background: #f8f9fa;
  min-width: 120px;
}

.result-item.danger {
  background: #ffebee;
  color: #d32f2f;
}

.result-item.safe {
  background: #e8f5e8;
  color: #4caf50;
}

.result-number {
  font-size: 1.5rem;
  font-weight: bold;
  margin-bottom: 0.3rem;
}

.result-label {
  font-size: 0.9rem;
  opacity: 0.8;
}

.sensitive-card {
  border-left: 4px solid #f44336 !important;
  background: #fff5f5 !important;
}

.sensitive-warning {
  background: #f44336;
  color: white;
  padding: 0.3rem 0.8rem;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 500;
  display: inline-block;
  margin-bottom: 1rem;
}

.sensitive-content {
  background: #ffebee;
  padding: 1rem;
  border-radius: 8px;
  border: 1px solid #ffcdd2;
}

.action-btn.safe {
  background: #4caf50;
  color: white;
  padding: 0.4rem 0.8rem;
  border-radius: 6px;
  font-size: 0.8rem;
}

.action-btn.safe:hover {
  background: #45a049;
  opacity: 1;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .result-summary {
    flex-direction: column;
    gap: 1rem;
  }
  
  .result-item {
    min-width: auto;
  }
  
  .section-actions {
    flex-direction: column;
    gap: 0.5rem;
  }
  
  .section-actions > * {
    width: 100%;
  }
}

/* 页面跳转样式 */
.page-jump {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: white;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  border: 1px solid #ddd;
}

.page-input {
  width: 60px;
  padding: 0.3rem 0.5rem;
  border: 1px solid #ddd;
  border-radius: 4px;
  text-align: center;
  font-size: 0.9rem;
}

.page-input:focus {
  outline: none;
  border-color: #667eea;
}

.jump-btn {
  padding: 0.3rem 0.8rem;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8rem;
  transition: background 0.3s ease;
}

.jump-btn:hover:not(:disabled) {
  background: #5a6fd8;
}

.jump-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* 分页样式优化 */
.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 2rem;
  flex-wrap: wrap;
}

.page-info {
  color: #666;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 0.3rem;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .pagination {
    flex-direction: column;
    gap: 0.8rem;
  }
  
  .page-jump {
    flex-direction: column;
    gap: 0.5rem;
    text-align: center;
  }
  
  .page-info {
    flex-direction: column;
    gap: 0.2rem;
  }
  
  .page-input {
    width: 80px;
  }
}

</style>