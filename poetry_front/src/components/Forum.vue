<template>
  <div class="forum-container">
    <header class="forum-header">
      <h1>墨韵诗阁</h1>
      <p>谈笑有鸿儒，往来无白丁</p>
    </header>
    
  
    <!-- 主要内容区 -->
    <div class="forum-main">
      <div class="content-area">
        
        <!-- 排序与发布按钮 -->
        <div class="top-actions">
          <div class="sorting-options">
            <button :class="{ active: sortType === 'time' }" @click="sortBy('time')">按时间排序</button>
            <button :class="{ active: sortType === 'hot' }" @click="sortBy('hot')">按热度排序</button>
          </div>
          <button class="post-btn" @click="handlePostClick">
            <i class="iconfont">✒️</i> 发布
          </button>
        </div>

        <!-- 发帖表单 -->
        <div v-if="showPostForm" class="post-form">
          <h3>发表新作</h3>
          <input v-model="newPost.title" placeholder="标题" class="input-title">
          <textarea v-model="newPost.content" placeholder="请在此泼墨..." class="input-content"></textarea>
          <div class="form-row">
            <select v-model="newPost.category" class="category-select">
              <option v-for="cat in filteredCategories.filter(c => c !== '全部')" 
                :value="cat" 
                :key="cat">
                {{ cat }}
              </option>
            </select><!-- 只有管理员可以发布每日话题 -->

            <button @click="submitPost" class="submit-btn">提交</button>
          </div>
        </div>
        
        <!-- 帖子列表 -->
        <div v-if="posts.length === 0" class="post-item">
          <p>暂无帖子，快来发表你的诗作吧！</p>
        </div>
        <div class="post-list">
          <div 
            v-for="post in sortedPosts" 
            :key="post.id" 
            :id="'post-' + post.id"
            class="post-item">
            <div class="post-header">
              <span class="author">👨 {{ post.author }}</span>
              <span class="time">📅 {{ post.time }}</span>
              <span class="category-tag" :style="{backgroundColor: getCategoryColor(post.category)}">
                {{ post.category }}
              </span>
              <!-- 只有管理员和发布者可以删除帖子 -->
              <!-- 删除按钮 -->
              <button @click="deletePost(post.id)" class="delete-btn">删除</button>
            </div>

            <!-- 帖子内容 -->
            <h3 class="post-title">{{ post.title }}</h3>
            <div 
              class="post-content" 
              v-html="post.isExpanded || post.content.length <= 150 
                ? formatPostContent(post.content) 
                : formatPostContent(post.content.slice(0, 150) + '...')">
            </div>

            <!-- 展开收起按钮 -->
            <div v-if="post.content.length > 150" class="expand-btn">
              <button @click="post.isExpanded = !post.isExpanded">
                {{ post.isExpanded ? '收起全文 ▲' : '展开全文 ▼' }}
              </button>
            </div>

            <!-- 点赞和评论 -->
            <div class="post-actions">
              <button 
                @click="likePost(post)" 
                class="like-btn" 
                :class="{ liked: post.liked }">
                👍 赞 {{ post.likes }}
              </button>
              <button class="comment-btn" @click="toggleComment(post)">
                💬 评 {{ post.commentNum }}
              </button>
            </div>

            <!-- 评论区 -->
            <div v-if="post.showComments" class="comment-section">
              <div v-for="comment in post.comments" :key="comment.id" class="comment-item">
                <span class="comment-author">👤 {{ comment.author }}：</span>
                <span class="comment-content">{{ comment.content }}</span>
              </div>
              <div class="comment-input-row">
                <input 
                  v-model="post.newComment" 
                  @keyup.enter="addComment(post)" 
                  placeholder="留一言..." 
                  class="comment-input">
                <button 
                  @click="addComment(post)" 
                  class="comment-submit-btn">
                  发布
                </button>
              </div>
              <p v-if="post.commentError" class="error-text">{{ post.commentError }}</p>  
            </div>
          </div>
        </div>
      </div>
      
      <!-- 侧边栏 -->
      <div class="sidebar">

        <!-- 登录状态显示区域 -->
        <div class="login-status">
          <span v-if="isLoggedIn">
            欢迎，{{ username }}！
            <button @click="logout" class="logout-btn">注销</button>
          </span>
          <span v-else>
            <router-link to="/forumlogin">
              <button class="login-btn">登录</button>
            </router-link>
          </span>
        </div>

        <!-- 帖子分类 -->
        <div class="category-filter">
          <h3>帖子分类</h3>
          <ul>
            <li v-for="cat in categories" :key="cat" 
                @click="selectedCategory = cat"
                :class="{active: selectedCategory === cat}">
              {{ cat }}
            </li>
          </ul>
        </div>

        <!-- 热门话题（从内容中提取） -->
        <div class="hot-tags">
          <h3>热门话题</h3>
          <div class="tag-list">
            <span 
              v-for="[tag, count] in sortedTags" 
              :key="tag" 
              class="tag-item"
              :class="{ active: selectedTag === tag }"
              @click="selectedTag = (selectedTag === tag ? null : tag)">
              #{{ tag }}（{{ count }}）
            </span>
          </div>
        </div>


        <!-- 每日话题 -->
        <div class="daily-topic-panel">
          <h3 @click="filterByDailyTopic" class="daily-topic-title">
            每日话题
          </h3>
          <ul class="daily-topic-list">
            <li 
              v-for="topic in recentDailyTopics" 
              :key="topic.id" 
              class="daily-topic-item"
              @click="scrollToPost(topic.id)">
              {{ topic.title }}
            </li>
          </ul>
        </div>

      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios';
import { useUserStore } from '@/stores/user';

export default {
  name: 'PoetryForum',
  data() {
    const username = localStorage.getItem('username') || null;
    const isAdmin = localStorage.getItem('isAdmin') === 'true';
 
    return {
      username,
      isAdmin,
      isLoggedIn: !!username,
      API_BASE_URL: 'http://localhost:8081/user', // 添加这一行

      showPostForm: false,
      selectedCategory: '全部',
      sortType: 'time',
      selectedTag: null,
      newPost: {
        title: '',
        content: '',
        category: '作品分享',
        author: username,
      },
      posts: [],
      categories: ['全部', '作品分享', '诗词赏析', '写作心得', '创作讨论', '提问求助'],
    }
  },
  created() {
    const userStore = useUserStore(); // 获取 Pinia 的 Store
    const username = localStorage.getItem('username');
    const isAdmin = localStorage.getItem('isAdmin') === 'true';
    const userId = localStorage.getItem('uid'); // 从 localStorage 获取用户 ID

    if (username) {
      const accounts = JSON.parse(localStorage.getItem('accounts') || '[]');
      const user = accounts.find(acc => acc.username === username);
      if (user) {
        userStore.login(user); // 同步用户信息到 Pinia
      }
    }

    this.newPost.author = username;

    // 从后端加载帖子数据并转换UID为用户名
    axios.get('http://localhost:8081/comment/init')
      .then(async (response) => {
        const postsData = response.data;

        // 获取用户喜欢的评论列表
        let likedCommentIds = [];
        if (userId) {
          try {
            const likedResponse = await axios.get(`http://localhost:8081/comment/getLikeIDs/${userId}`);
            likedCommentIds = likedResponse.data; // 获取喜欢的评论 ID 列表
          } catch (error) {
            console.error('加载喜欢的评论失败', error);
          }
        }

        // 处理每个帖子，将PersonID转换为用户名，并设置 liked 属性
        this.posts = await Promise.all(postsData.map(async (post) => {
          const authorName = await this.getUserName(post.PersonID);
          return {
            id: post.CommentID,
            title: post.Title,
            content: post.Content,
            category: post.Category,
            author: authorName || `用户${post.PersonID}`, // 如果获取失败，显示备用格式
            authorId: post.PersonID, // 保留原始UID用于权限判断
            time: new Date(post.Timestamp).toLocaleDateString(),
            likes: post.LikeCounts,
            liked: likedCommentIds.includes(post.CommentID), // 根据喜欢的评论列表设置 liked
            comments: [],
            commentNum: post.CommentCounts,
            showComments: false,
            isExpanded: false,
            newComment: '',
            commentError: '',
          };
        }));
      })
      .catch(error => {
        console.error('加载帖子失败', error);
      });
  },
  computed: {
    recentDailyTopics() {
      return this.posts
        .filter(p => p.category === '每日话题')
        .slice(0, 5); // 最多展示5条
    }, 
    sortedPosts() {
      const postsToSort = this.filteredPosts;
      return this.sortType === 'time'
        ? [...postsToSort].sort((a, b) => new Date(b.time) - new Date(a.time))
        : [...postsToSort].sort((a, b) => b.likes - a.likes);
    },
    filteredCategories() {//只有管理员可以发布每日话题
      if (this.isAdmin) {
        return [...this.categories, '每日话题'];
      }
      return this.categories;
    },
    filteredPosts() {
      if (this.selectedCategory === '全部') return this.posts;
      return this.posts.filter(post => post.category === this.selectedCategory);
    },
    tagStats() {
      const stats = {};
      for (const post of this.posts) {
        if (post.category && post.category !== '全部') {
          stats[post.category] = (stats[post.category] || 0) + 1;
        }
      }
      return stats;
    },
    recentUsers() {
      const seen = new Set();
      const result = [];

      for (const post of this.posts) {
        if (!seen.has(post.author)) {
          seen.add(post.author);
          result.push({
            name: post.author,
            time: post.time
          });
        }
        if (result.length >= 5) break; // 最多显示5人
      }

      return result;
    },
    contentTags() {
      const tagMap = {};
      const tagRegex = /#([\u4e00-\u9fa5_a-zA-Z0-9]+)/g;

      for (const post of this.posts) {
        const tags = post.content.match(tagRegex);
        if (tags) {
          tags.forEach(tag => {
            const cleanTag = tag.slice(1); // 去掉前面的 #
            tagMap[cleanTag] = (tagMap[cleanTag] || 0) + 1;
          });
        }
      }

      return tagMap;
    },
    sortedTags() {
      return Object.entries(this.contentTags)
        .sort((a, b) => b[1] - a[1]) // 按热度排序
        .slice(0, 10); // 限制最多展示10个热门标签
    },
    filteredPosts() {
      // 分类选择
      if (this.selectedTag) {
        const regex = new RegExp(`#${this.selectedTag}(?=\\W|$)`); // 匹配 #标签
        return this.posts.filter(post => regex.test(post.content));
      }
      if (this.selectedCategory === '全部') return this.posts;
      return this.posts.filter(post => post.category === this.selectedCategory);
    }
  },
  methods: {
    filterByDailyTopic() {
      this.selectedCategory = '每日话题';
      this.selectedTag = null;
    },
    scrollToPost(postId) {
      this.$nextTick(() => {
        const el = document.getElementById(`post-${postId}`);
        if (el) {
          el.scrollIntoView({ behavior: 'smooth', block: 'start' });
          el.classList.add('highlight-post');
          setTimeout(() => el.classList.remove('highlight-post'), 2000); // 高亮消退
        }
      });
    },
    async getUserName(uid) {
      try {
        const response = await fetch(`${this.API_BASE_URL}/loginName/${uid}`, {
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
    async submitPost() {
      const userStore = useUserStore();

      const data = {
        parentID: 0, // 为根帖子
        Category: this.newPost.category || "作品分享", // 使用表单中的分类
        Title: this.newPost.title.trim(),
        Content: this.newPost.content.trim(),
        PersonID: userStore.uid, // 从  中获取用户 ID
        hasTitle: this.newPost.title.trim().length > 0,
        isAdmin: userStore.isAdmin // 从  中获取管理员状态
      };
      console.log("hey data:\n");
      console.log(data);
      if (!data.hasTitle) {
        alert("标题不能为空！");
        return;
      }

      if (!data.Content) {
        alert("正文不能为空！");
        return;
      }

      try {
        const res = await axios.post('http://127.0.0.1:8081/comment/addComment', data);
        //console.log(res.data);

        if (res.data.status === "SUCCESS") {
          // 如果后端返回成功，可以将新帖子添加到本地列表
          const newPost = {
            id: res.data.commentId, // 使用后端返回的 commentId
            title: data.Title,
            content: data.Content,
            category: data.Category,
            author: userStore.username,
            time: new Date(res.data.createdAt).toLocaleDateString(), // 使用后端返回的创建时间
            likes: 0,
            liked: false,
            comments: [],
            commentNum:0,
            showComments: false,
            isExpanded: false,
            newComment: '',
            commentError: ''
          };

          this.posts.unshift(newPost);

          // 重置表单
          this.newPost.title = '';
          this.newPost.content = '';
          this.newPost.category = '作品分享'; // 默认分类
          this.showPostForm = false;

          alert(res.data.message); // 显示后端返回的成功消息
        } else {
          alert("提交失败，请检查后端返回状态！");
        }
      } catch (error) {
        console.error('提交失败:', error);
        alert('提交失败，请检查网络连接或后端服务！');
      }
    },
    deletePost(postId) {
      // 获取要删除的帖子
      const postToDelete = this.posts.find(post => post.id === postId);

      // 权限校验（管理员或发布者自己）
      if (!(this.isAdmin || postToDelete.authorId === userStore.uid)) {
        alert("你不能删除其他用户发布的帖子！");
        return;
      }

      // 调用后端删除接口
      axios.delete(`http://127.0.0.1:8081/del/${postId}`)
        .then(() => {
          // 删除成功后从本地 posts 中移除该帖子
          this.posts = this.posts.filter(post => post.id !== postId);
          alert("帖子删除成功！");
        })
        .catch(error => {
          console.error('删除失败:', error);
          alert('删除失败，请检查网络连接或权限设置');
        });
    },
    likePost(post) {
      if (!this.isLoggedIn) {
        alert("请先登录再点赞！");
        return;
      }

      const userId = localStorage.getItem('uid'); // 从 localStorage 获取用户 ID
      if (!userId) {
        alert("用户信息缺失，请重新登录！");
        return;
      }

      // 根据当前点赞状态调用不同的接口
      const url = post.liked
        ? `http://127.0.0.1:8081/comment/delLikeComment/${userId}/${post.id}`
        : `http://127.0.0.1:8081/comment/likeComment/${userId}/${post.id}`;

      axios.get(url)
        .then((response) => {
          if (response.status === 200) {
            // 更新点赞状态和点赞数
            post.liked = !post.liked;
            post.likes += post.liked ? 1 : -1; // 根据状态更新点赞数
          } else {
            alert("操作失败，请稍后重试！");
          }
        })
        .catch((error) => {
          console.error("操作失败:", error);
          alert("操作失败，请检查网络连接或后端服务！");
        });
    },
    async toggleComment(post) {
      // 如果评论已经展开，则直接切换显示状态
      if (post.showComments) {
        post.showComments = false;
        return;
      }

      try {
        // 向后端请求展开评论数据
        const response = await axios.get(`http://127.0.0.1:8081/comment/open_comment/${post.id}`);
        const comments = response.data;
        post.commentNum = comments.length;

        // 为每个评论获取用户名
        post.comments = await Promise.all(comments.map(async (comment) => {
          const authorName = await this.getUserName(comment.PersonID);
          return {
            id: comment.CommentID,
            author: authorName || `用户${comment.PersonID}`, // 显示用户名或备用格式
            authorId: comment.PersonID, // 保留UID
            content: comment.Content,
            time: new Date(comment.Timestamp).toLocaleDateString()
          };
        }));

        // 展开评论区
        post.showComments = true;
      } catch (error) {
        console.error('评论加载失败:', error);
        alert('评论加载失败，请检查网络连接或后端服务！');
      }
    },
    async addComment(post) {
      const userStore = useUserStore();

      if (!this.isLoggedIn) {
        alert("请先登录再发表评论！");
        return;
      }

      const content = post.newComment?.trim();
      if (!content) {
        post.commentError = "评论不能为空！";
        return;
      }

      post.commentError = "";
      //console.log(userStore.uid);
      const data = {
        parentID: post.id, // 评论的父帖子 ID
        Category: post.category, // 使用帖子分类
        Title: null, 
        Content: content, // 评论内容
        PersonID: userStore.uid, // 从中获取用户 ID
        hasTitle: false, // 评论不需要标题
        isAdmin: userStore.isAdmin // 从中获取管理员状态
      };
      console.log(data);
      try {
        const res = await axios.post('http://localhost:8081/comment/addComment', data);
        console.log(res.data);

        if (res.data.status === "SUCCESS") {
          // 如果后端返回成功，将新评论添加到帖子评论列表
          const newComment = {
            id: res.data.commentId, // 使用后端返回的 commentId
            author: userStore.username,
            content: content,
            time: new Date(res.data.createdAt).toLocaleDateString() // 使用后端返回的创建时间
          };

          post.comments.push(newComment);
          post.newComment = ''; 
          
          // 对该帖子的评论数递增
          post.commentNum++;

          alert(res.data.message); 
        } else {
          alert("评论提交失败，请检查后端返回状态！");
        }
      } catch (error) {
        console.error('评论提交失败:', error);
        alert('评论提交失败，请检查网络连接或后端服务！');
      }
    },
    sortBy(type) {
      this.sortType = type;
    },
    getCategoryColor(category) {
      const colors = {
        '作品分享': '#8c7853',
        '诗词赏析': '#6e5773',
        '写作心得': '#d45d79',
        '创作讨论': '#56949f',
        '提问求助': '#797593'
      };
      return colors[category] || '#999';
    },
    formatPostContent(content) {
      const tagRegex = /#([\u4e00-\u9fa5\w]+)/g;
      return content.replace(tagRegex, (match) => {
        return `<span class="tag-highlight">${match}</span>`;
      });
    },
    logout() {
      // 清除本地存储，设置为未登录状态
      localStorage.removeItem('username');
      this.username = null;
      this.isLoggedIn = false;

      const userStore = useUserStore();
      userStore.logout();  // 更新全局 store

      // this.$router.push('/forumlogin'); // 跳转到登录页面
    },
    handlePostClick() {
      if (!this.isLoggedIn) {
        alert("请先登录再发帖！");
        // this.$router.push('/forumlogin'); 
        return;
      }
      this.showPostForm = !this.showPostForm;
    }
  },
  mounted() {
    // // 页面加载时请求评论
    // axios.get('http://localhost:8081/comment/init')
    //   .then(res => {
    //     const cids = res.data;
    //     return axios.post('http://localhost:8081/comment/get', cids);
    //   })
    //   .then(res => {
    //     const comments = res.data;
    //   this.posts.forEach(post => {
    //     post.comments = comments.filter(c => c.postId === post.id);
    //   });  
    //   })  
    //   .catch(err => {
    //     console.error("评论加载失败", err);
    //   });
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

.forum-header {
  text-align: center;
  padding: 0.5rem;
  background: linear-gradient(to right, #8c7853, #6e5773);
  color: white;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1); /* 阴影效果 */
  border-radius: 10px;
  margin-bottom: 2rem;
}
/* 抄mt的浮动效果 */
.forum-header h1 {
  display: flex;
  justify-content: center;
  color: #e5e5e5;
  font-family: eva;
  font-size: 40px;
  -webkit-background-clip: text;
  background-clip: text;
  text-shadow: 3px 3px 10px rgba(0, 0, 0, 0.5); /* 字体的阴影效果 */
  animation: float 3s ease-in-out infinite; /* 浮动动画 */
}

.forum-header p {
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0% {
    transform: translateY(0); /* 初始位置 */
  }

  50% {
    transform: translateY(-4px); /* 向上浮动4px */
  }

  100% {
    transform: translateY(0); /* 还原回原始位置 */
  }
}

.forum-main {
  display: flex;
  gap: 2rem;
}

.content-area {
  flex: 3;
}

.sidebar {
  flex: 1;
  background: #fff;
  padding: 1rem;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.top-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.sorting-options {
  display: flex;
  gap: 1rem;
}

.sorting-options button {
  background: #e9e4da;
  border: none;
  padding: 8px 16px;
  border-radius: 20px;
  cursor: pointer;
  color: #5c4b33;
  font-weight: 500;
  transition: background 0.3s;
}

.sorting-options button:hover {
  background: #d6cab4;
}

.sorting-options button.active {
  background: #8c7853;
  color: white;
}

.post-btn {
  background: #8c7853;
  color: white;
  padding: 12px 24px;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  margin-bottom: 1rem;
}

.post-form {
  background: white;
  padding: 1.5rem;
  border-radius: 8px;
  margin-bottom: 2rem;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
}

.input-title {
  width: 100%;
  padding: 0.6rem 1rem;
  font-size: 1rem;
  height: 40px; 
  margin-bottom: 1rem;
  border: 1px solid #ddd;
  border-radius: 6px;
  box-sizing: border-box;
}

.input-content {
  width: 100%;
  padding: 1rem;
  font-size: 1rem;
  height: 180px; 
  resize: vertical;
  border: 1px solid #ddd;
  border-radius: 6px;
  box-sizing: border-box;
}

.form-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  margin-top: 1rem;
}

.category-select {
  flex: 3; /* 相当于60% */
  padding: 0.6rem 1rem;
  font-size: 1rem;
  border: 1px solid #b8a888;
  border-radius: 10px;
  background-color: #fdfaf5;
  color: #5a4634;
  appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg width='14' height='10' viewBox='0 0 14 10' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M1 3l6 6 6-6' stroke='%236e5773' stroke-width='2' fill='none' fill-rule='evenodd'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 1rem center;
  background-size: 0.75rem;
  box-sizing: border-box;
}

.submit-btn {
  flex: 2; /* 相当于40% */
  padding: 10px 0;
  background: linear-gradient(to right, #8c7853, #6e5773);
  border: none;
  border-radius: 30px;
  color: white;
  font-size: 1rem;
  font-weight: bold;
  cursor: pointer;
  box-shadow: 0 3px 6px rgba(0,0,0,0.1);
  transition: all 0.3s ease;
  text-align: center;
}

.submit-btn:hover {
  background: linear-gradient(to right, #a3916a, #7c6488);
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0,0,0,0.15);
}

.sorting-options {
  display: flex;
  gap: 1rem;
  margin-bottom: 1rem;
}

.sorting-options button {
  background: #e9e4da;
  border: none;
  padding: 8px 16px;
  border-radius: 20px;
  cursor: pointer;
  color: #5c4b33;
  font-weight: 500;
  transition: background 0.3s;
}

.sorting-options button:hover {
  background: #d6cab4;
}

.sorting-options button.active {
  background: #8c7853;
  color: white;
}

.post-item {
  background: white;
  padding: 1.5rem;
  margin-bottom: 1.5rem;
  border-radius: 8px;
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.post-content {
  white-space: pre-wrap;
  font-family: '楷体', cursive;
  font-size: 1.1rem;
  line-height: 1.6;
  color: #666;
  margin-top: 0.5rem;
}

::v-deep .tag-highlight {
  color: #2b70c9;
  background-color: #e7f0ff;
  padding: 2px 6px;
  border-radius: 6px;
  font-weight: bold;
  transition: background-color 0.2s;
}

.like-btn.liked {
  background: #ffe0e0;
  color: #c0392b;
  font-weight: bold;
}

.delete-btn {
  padding: 6px 12px;
  background: #8c7853;
  color: white;
  border: none;
  border-radius: 20px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: background 0.2s ease;
}

.delete-btn:hover {
  background: #ffd6d6;
}

.expand-btn {
  text-align: left;
  margin-top: 0.1rem;
  margin-bottom: 1rem;
  margin-left: auto;
}

.expand-btn button {
  background: none;
  border: none;
  color: #8c7853;
  cursor: pointer;
  font-size: 0.9rem;
  font-weight: bold;
}

.post-content {
  white-space: pre-wrap;
  font-family: '楷体', cursive;
  font-size: 1.1rem;
  line-height: 1.6;
  color: #666;
}

.category-tag {
  background: none !important;
  color: #8c7853;
  font-size: 0.85rem;
  padding: 0;
  border-radius: 0;
  font-weight: bold;
}

.comment-input-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 1rem;
}

.comment-input {
  flex: 1;
  height: 36px;
  padding: 0 14px;
  font-size: 14px;
  border: 1px solid #ccc;
  border-radius: 20px;
  outline: none;
  line-height: 36px;
  box-sizing: border-box;
  vertical-align: middle;
  display: inline-block;
}

.comment-submit-btn {
  height: 36px;
  padding: 0 16px;
  font-size: 14px;
  border: none;
  border-radius: 20px;
  background-color: #8c7853;
  color: white;
  cursor: pointer;
  box-sizing: border-box;
  vertical-align: middle;
  display: inline-block;
  line-height: 36px;
  transition: background 0.3s;
  margin-top: 1rem;
}

.comment-submit-btn:hover {
  background-color: #a3916a;
}

.error-text {
  color: #c0392b;
  font-size: 0.8rem;
  margin-top: 0.2rem;
}

.comment-section {
  margin-top: 1rem;
  border-top: 1px solid #eee;
  padding-top: 1rem;
}

.comment-input {
  width: 100%;
  padding: 0.8rem;
  margin-top: 1rem;
  border: 1px solid #ddd;
  border-radius: 20px;
}

.category-filter li {
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 4px;
  margin: 0.2rem 0;
}

.category-filter li.active {
  background: #f3f0eb;
  color: #8c7853;
  font-weight: bold;
}

.hot-authors .author-item {
  display: flex;
  justify-content: space-between;
  padding: 0.5rem;
  margin: 0.3rem 0;
  background: #f8f8f8;
  border-radius: 4px;
}

.like-btn, .comment-btn {
  padding: 6px 12px;
  margin-right: 1rem;
  border: none;
  border-radius: 15px;
  background: #f3f0eb;
  color: #666;
}

.like-btn:hover {
  background: #ffe7e7;
}

.comment-btn:hover {
  background: #e7f3ff;
}

.hot-tags, .active-users {
  margin-top: 1.5rem;
}

.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.tag-item {
  background: #f3f0eb;
  color: #8c7853;
  padding: 4px 10px;
  border-radius: 20px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: background 0.2s;
}

.tag-item:hover {
  background: #e3ded5;
}

.tag-item.active {
  background: #8c7853;
  color: white;
}

.active-users .author-item {
  display: flex;
  justify-content: space-between;
  padding: 0.4rem;
  margin: 0.3rem 0;
  background: #f8f8f8;
  border-radius: 4px;
}

.daily-topic-panel {
  margin-top: 2rem;
}

.daily-topic-title {
  font-weight: bold;
  cursor: pointer;
  color: #8c7853;
  margin-bottom: 0.5rem;
}

.daily-topic-title:hover {
  text-decoration: underline;
}

.daily-topic-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.daily-topic-item {
  padding: 0.4rem 0.6rem;
  background: #f9f6f1;
  border-radius: 6px;
  margin: 0.2rem 0;
  cursor: pointer;
  transition: background 0.3s;
}

.daily-topic-item:hover {
  background: #eee7dc;
}

/* 高亮跳转目标 */
.highlight-post {
  animation: highlightFade 2s ease-in-out;
  box-shadow: 0 0 0 3px #8c7853aa;
  border-radius: 8px;
}

@keyframes highlightFade {
  0% { background-color: #fff9e7; }
  100% { background-color: white; }
}

.login-status {
  background: #fffaf2;
  border: 1px solid #e5d8c3;
  padding: 12px 16px;
  border-radius: 12px;
  text-align: center;
  margin-bottom: 1.5rem;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.05);
}

.login-status span {
  display: block;
  font-size: 1rem;
  color: #5a4634;
  margin-bottom: 10px;
}

.login-btn,
.logout-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 20px;
  font-size: 1rem;
  font-weight: bold;
  border: none;
  border-radius: 30px;
  background: linear-gradient(to right, #8c7853, #6e5773);
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.login-btn:hover,
.logout-btn:hover {
  background: linear-gradient(to right, #a3916a, #7c6488);
  transform: translateY(-2px);
}



</style>