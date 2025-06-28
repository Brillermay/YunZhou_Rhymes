<template>
    <div class="welcome-section">
      <!-- AI功能介绍 -->
      <div class="ai-intro-section">
        <div class="section-header">
          <h2>🤖 AI智能搜索体验 🤖</h2>
          <p>让人工智能帮你发现最符合心境的古诗词</p>
        </div>
        
        <div class="ai-features">
          <div class="feature-card">
            <div class="feature-icon">🧠</div>
            <h3>语义理解</h3>
            <p>理解你的真实意图，不只是关键词匹配</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">🎯</div>
            <h3>精准推荐</h3>
            <p>基于诗词内容和情感色彩智能推荐</p>
          </div>
          <div class="feature-card">
            <div class="feature-icon">💡</div>
            <h3>智能联想</h3>
            <p>发现相关诗词和隐藏的文学关联</p>
          </div>
        </div>
      </div>
  
      <!-- 探索主题 -->
      <div class="themes-section">
        <div class="section-header">
          <h2>🎭 主题探索 🎭</h2>
          <p>按情感和场景发现诗词之美</p>
        </div>
        
        <div class="themes-grid">
          <button
            v-for="theme in poetryThemes"
            :key="theme.name"
            @click="$emit('search-by-theme', theme.query)"
            class="theme-card"
            :style="{ background: theme.gradient }"
          >
            <div class="theme-icon">{{ theme.icon }}</div>
            <h3>{{ theme.name }}</h3>
            <p class="theme-desc">{{ theme.description }}</p>
            <div class="theme-keywords">
              <span 
                v-for="keyword in theme.keywords" 
                :key="keyword"
                class="keyword-tag"
              >
                {{ keyword }}
              </span>
            </div>
          </button>
        </div>
      </div>
  
      <!-- 诗人画廊 -->
      <div class="poets-section">
        <div class="section-header">
          <h2>👥 诗人画廊 👥</h2>
          <p>与历史上的文学巨匠对话</p>
        </div>
        
        <div class="poets-gallery">
          <button
            v-for="poet in famousPoets"
            :key="poet.name"
            @click="$emit('search-by-poet', poet.name)"
            class="poet-card"
          >
            <div class="poet-portrait">{{ poet.avatar }}</div>
            <div class="poet-info">
              <h3>{{ poet.name }}</h3>
              <p class="poet-dynasty">{{ poet.dynasty }}</p>
              <p class="poet-style">{{ poet.style }}</p>
              <div class="poet-signature">
                <span>"{{ poet.signature }}"</span>
              </div>
            </div>
          </button>
        </div>
      </div>
  
      <!-- 示例搜索 -->
      <div class="examples-section">
        <div class="section-header">
          <h2>💫 试试这些搜索 💫</h2>
          <p>看看AI如何理解你的诗词需求</p>
        </div>
        
        <div class="examples-grid">
          <button
            v-for="example in searchExamples"
            :key="example.query"
            @click="$emit('search-by-theme', example.query)"
            class="example-card"
          >
            <div class="example-icon">{{ example.icon }}</div>
            <div class="example-query">"{{ example.query }}"</div>
            <div class="example-desc">{{ example.description }}</div>
          </button>
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  defineProps({
    fontSize: Number,
    viewMode: String,
    isFavorite: Function
  })
  
  defineEmits([
    'search-by-poet',
    'search-by-theme',
    'view-detail',
    'toggle-favorite'
  ])
  
  const poetryThemes = [
    {
      name: '思乡怀远',
      query: '思乡的诗词',
      icon: '🏠',
      description: '游子思乡，归心似箭',
      gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      keywords: ['思乡', '故乡', '归心']
    },
    {
      name: '春风花月',
      query: '春天的诗词',
      icon: '🌸',
      description: '春光明媚，花开月圆',
      gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
      keywords: ['春天', '花朵', '月亮']
    },
    {
      name: '友情离别',
      query: '送别朋友的诗',
      icon: '👋',
      description: '挥手告别，友谊长存',
      gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
      keywords: ['送别', '友情', '离别']
    },
    {
      name: '山水田园',
      query: '山水田园诗',
      icon: '🏔️',
      description: '青山绿水，田园牧歌',
      gradient: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
      keywords: ['山水', '田园', '自然']
    },
    {
      name: '爱情佳话',
      query: '爱情诗词',
      icon: '💕',
      description: '情深意长，爱意绵绵',
      gradient: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
      keywords: ['爱情', '相思', '情人']
    },
    {
      name: '壮志豪情',
      query: '豪放的诗词',
      icon: '⚔️',
      description: '豪情万丈，壮志凌云',
      gradient: 'linear-gradient(135deg, #a8edea 0%, #fed6e3 100%)',
      keywords: ['豪放', '壮志', '英雄']
    }
  ]
  
  const famousPoets = [
    {
      name: '李白',
      dynasty: '唐代',
      avatar: '🌟',
      style: '浪漫主义',
      signature: '安能摧眉折腰事权贵'
    },
    {
      name: '杜甫',
      dynasty: '唐代',
      avatar: '📝',
      style: '现实主义',
      signature: '安得广厦千万间'
    },
    {
      name: '苏轼',
      dynasty: '宋代',
      avatar: '🎭',
      style: '豪放词派',
      signature: '但愿人长久，千里共婵娟'
    },
    {
      name: '李清照',
      dynasty: '宋代',
      avatar: '🌹',
      style: '婉约词派',
      signature: '生当作人杰，死亦为鬼雄'
    },
    {
      name: '辛弃疾',
      dynasty: '宋代',
      avatar: '🛡️',
      style: '豪放词派',
      signature: '醉里挑灯看剑'
    },
    {
      name: '王维',
      dynasty: '唐代',
      avatar: '🏔️',
      style: '山水田园',
      signature: '诗中有画，画中有诗'
    }
  ]
  
  const searchExamples = [
    {
      query: '描述秋天景色的诗词',
      icon: '🍂',
      description: 'AI会找出所有描写秋景的诗'
    },
    {
      query: '表达思念情人的诗',
      icon: '💭',
      description: '理解情感需求，推荐相关诗词'
    },
    {
      query: '励志向上的古诗',
      icon: '💪',
      description: '按情感色彩分类推荐'
    },
    {
      query: '夜晚喝酒的诗词',
      icon: '🌙',
      description: '场景化搜索，精准匹配'
    }
  ]
  </script>
  
  <style scoped>
  .welcome-section {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    gap: 4rem;
  }
  
  .section-header {
    text-align: center;
    margin-bottom: 2.5rem;
  }
  
  .section-header h2 {
    color: #8c7853;
    font-size: 2.2rem;
    margin: 0 0 1rem 0;
    font-weight: 300;
  }
  
  .section-header p {
    color: #666;
    font-size: 1.1rem;
    margin: 0;
  }
  
  .ai-features {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
    gap: 2rem;
  }
  
  .feature-card {
    background: white;
    padding: 2rem;
    border-radius: 20px;
    text-align: center;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    transition: all 0.3s ease;
    border: 2px solid transparent;
  }
  
  .feature-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
    border-color: #667eea;
  }
  
  .feature-icon {
    font-size: 3rem;
    margin-bottom: 1rem;
  }
  
  .feature-card h3 {
    color: #333;
    margin-bottom: 1rem;
    font-size: 1.3rem;
  }
  
  .feature-card p {
    color: #666;
    line-height: 1.6;
  }
  
  .themes-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 1.5rem;
  }
  
  .theme-card {
    padding: 2rem;
    border: none;
    border-radius: 20px;
    cursor: pointer;
    transition: all 0.3s ease;
    text-align: center;
    color: white;
    position: relative;
    overflow: hidden;
  }
  
  .theme-card::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(255, 255, 255, 0.1);
    opacity: 0;
    transition: opacity 0.3s ease;
  }
  
  .theme-card:hover::before {
    opacity: 1;
  }
  
  .theme-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 15px 40px rgba(0, 0, 0, 0.2);
  }
  
  .theme-icon {
    font-size: 2.5rem;
    margin-bottom: 1rem;
  }
  
  .theme-card h3 {
    margin: 0 0 0.5rem 0;
    font-size: 1.4rem;
    font-weight: 600;
  }
  
  .theme-desc {
    margin: 0 0 1rem 0;
    font-size: 0.9rem;
    opacity: 0.9;
  }
  
  .theme-keywords {
    display: flex;
    justify-content: center;
    flex-wrap: wrap;
    gap: 0.5rem;
  }
  
  .keyword-tag {
    background: rgba(255, 255, 255, 0.2);
    padding: 0.3rem 0.8rem;
    border-radius: 15px;
    font-size: 0.8rem;
    border: 1px solid rgba(255, 255, 255, 0.3);
  }
  
  .poets-gallery {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
    gap: 1.5rem;
  }
  
  .poet-card {
    display: flex;
    align-items: center;
    gap: 1.5rem;
    padding: 1.5rem;
    background: white;
    border: 2px solid #f0f0f0;
    border-radius: 20px;
    cursor: pointer;
    transition: all 0.3s ease;
    text-align: left;
  }
  
  .poet-card:hover {
    border-color: #8c7853;
    transform: translateY(-5px);
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
  }
  
  .poet-portrait {
    font-size: 4rem;
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100px;
    height: 100px;
    background: linear-gradient(135deg, #f8f4ed 0%, #e1d8c9 100%);
    border-radius: 50%;
    flex-shrink: 0;
  }
  
  .poet-info h3 {
    margin: 0 0 0.5rem 0;
    color: #8c7853;
    font-size: 1.5rem;
  }
  
  .poet-dynasty {
    color: #666;
    font-size: 0.9rem;
    margin: 0 0 0.3rem 0;
  }
  
  .poet-style {
    color: #999;
    font-size: 0.85rem;
    margin: 0 0 1rem 0;
  }
  
  .poet-signature {
    font-style: italic;
    color: #8c7853;
    font-size: 0.9rem;
  }
  
  .examples-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
    gap: 1.5rem;
  }
  
  .example-card {
    background: white;
    padding: 1.5rem;
    border-radius: 15px;
    cursor: pointer;
    transition: all 0.3s ease;
    text-align: center;
    border: 2px solid #f0f0f0;
  }
  
  .example-card:hover {
    border-color: #667eea;
    transform: translateY(-3px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  }
  
  .example-icon {
    font-size: 2rem;
    margin-bottom: 1rem;
  }
  
  .example-query {
    color: #667eea;
    font-weight: 600;
    margin-bottom: 0.5rem;
    font-size: 1rem;
  }
  
  .example-desc {
    color: #666;
    font-size: 0.85rem;
    line-height: 1.4;
  }
  
  @media (max-width: 768px) {
    .welcome-section {
      gap: 3rem;
    }
    
    .section-header h2 {
      font-size: 1.8rem;
    }
    
    .ai-features {
      grid-template-columns: 1fr;
    }
    
    .themes-grid {
      grid-template-columns: 1fr;
    }
    
    .poets-gallery {
      grid-template-columns: 1fr;
    }
    
    .poet-card {
      padding: 1rem;
      gap: 1rem;
    }
    
    .poet-portrait {
      width: 80px;
      height: 80px;
      font-size: 3rem;
    }
    
    .examples-grid {
      grid-template-columns: 1fr;
    }
  }
  </style>