<template>
  <div class="chat-area-container">
    <!-- 聊天消息区域 -->
    <div class="chat-messages-wrapper">
      <div class="chat-messages" ref="messagesContainer">
        <!-- 消息列表 -->
        <div 
          v-for="(message, index) in messages" 
          :key="index"
          class="message-wrapper"
          :class="message.type"
        >
          <div class="message-bubble" :class="message.type">
            <!-- 系统消息头像 -->
            <div v-if="message.type === 'system'" class="message-avatar">
              <i class="icon-robot"></i>
            </div>
            
            <!-- 消息内容 -->
            <div class="message-content">
              <div class="message-text" v-html="formatMessage(message.text)"></div>
              
              <!-- 验证结果 -->
              <div v-if="message.validation" class="message-validation">
                <div class="validation-result" :class="message.validation.type">
                  <i :class="getValidationIcon(message.validation.type)"></i>
                  <span>{{ message.validation.text }}</span>
                </div>
                <div v-if="message.validation.poemInfo" class="poem-info">
                  <span class="poem-title">{{ message.validation.poemInfo.title }}</span>
                  <span class="poem-author">- {{ message.validation.poemInfo.author }}</span>
                </div>
              </div>
              
              <div class="message-time">{{ formatTime(message.timestamp) }}</div>
            </div>
          </div>
        </div>
        
        <!-- 正在验证指示器 -->
        <div v-if="isValidating" class="typing-indicator">
          <div class="message-bubble system">
            <div class="message-avatar">
              <i class="icon-robot"></i>
            </div>
            <div class="message-content">
              <div class="typing-dots">
                <span></span>
                <span></span>
                <span></span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 输入区域 -->
    <div class="chat-input-section">
      <!-- 快速提示 -->
      <div class="quick-hints" v-if="hints.length > 0">
        <div class="hint-label">
          <i class="icon-lightbulb"></i>
          <span>诗句提示</span>
        </div>
        <div class="hints-container">
          <span 
            v-for="hint in hints" 
            :key="hint"
            class="hint-tag"
            @click="selectHint(hint)"
          >
            {{ hint }}
          </span>
        </div>
      </div>
      
      <!-- 输入控件 -->
      <div class="input-container">
        <div class="input-wrapper">
          <input
            v-model="inputMessage"
            @keypress.enter="sendMessage"
            @input="validateInput"
            :disabled="gameEnded || isValidating"
            :placeholder="getPlaceholder()"
            class="message-input"
            maxlength="100"
          />
          <div class="input-actions">
            <button 
              @click="requestHint"
              :disabled="hintCount <= 0 || gameEnded"
              class="hint-button"
              title="获取提示"
            >
              <i class="icon-lightbulb"></i>
              <span v-if="hintCount > 0">{{ hintCount }}</span>
            </button>
            <button 
              @click="sendMessage"
              :disabled="!canSend"
              class="send-button"
              title="发送消息"
            >
              <i class="icon-paper-plane"></i>
            </button>
          </div>
        </div>
        
        <!-- 输入验证提示 -->
        <div class="input-validation" v-if="inputValidation">
          <div class="validation-hint" :class="inputValidation.type">
            <i :class="getValidationIcon(inputValidation.type)"></i>
            <span>{{ inputValidation.text }}</span>
          </div>
        </div>
        
        <!-- 输入状态 -->
        <div class="input-footer">
          <div class="char-count" :class="{ warning: inputMessage.length > 80 }">
            {{ inputMessage.length }}/100
          </div>
          <div class="keyword-check">
            <i :class="containsKeyword ? 'icon-check contains-keyword' : 'icon-x missing-keyword'"></i>
            <span>{{ containsKeyword ? '包含关键词' : '缺少关键词' }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ChatArea',
  props: {
    messages: { type: Array, default: () => [] },
    currentKeyword: { type: String, required: true },
    gameEnded: { type: Boolean, default: false },
    isValidating: { type: Boolean, default: false },
    hintCount: { type: Number, default: 3 }
  },
  emits: ['send-message', 'request-hint'],
  data() {
    return {
      inputMessage: '',
      inputValidation: null,
      hints: []
    }
  },
  computed: {
    containsKeyword() {
      return this.inputMessage.includes(this.currentKeyword)
    },
    canSend() {
      return this.inputMessage.trim().length > 0 && 
             !this.isValidating && 
             !this.gameEnded &&
             this.inputMessage.length >= 5
    }
  },
  watch: {
    messages: {
      handler() {
        this.$nextTick(() => {
          this.scrollToBottom()
        })
      },
      deep: true
    },
    currentKeyword() {
      this.loadHints()
    }
  },
  mounted() {
    this.loadHints()
  },
  methods: {
    sendMessage() {
      if (!this.canSend) return
      
      this.$emit('send-message', this.inputMessage.trim())
      this.inputMessage = ''
      this.inputValidation = null
    },
    
    requestHint() {
      this.$emit('request-hint')
    },
    
    selectHint(hint) {
      this.inputMessage = hint
      this.validateInput()
    },
    
    validateInput() {
      const message = this.inputMessage.trim()
      
      if (message.length === 0) {
        this.inputValidation = null
        return
      }
      
      if (message.length < 5) {
        this.inputValidation = {
          type: 'warning',
          text: '诗句至少需要5个字符'
        }
        return
      }
      
      if (!message.includes(this.currentKeyword)) {
        this.inputValidation = {
          type: 'warning',
          text: `请包含关键词"${this.currentKeyword}"`
        }
        return
      }
      
      this.inputValidation = {
        type: 'success',
        text: '输入格式正确'
      }
    },
    
    scrollToBottom() {
      const container = this.$refs.messagesContainer
      if (container) {
        // 🔧 强制滚动到底部，确保最新消息可见
        container.scrollTop = container.scrollHeight
      }
    },
    
    loadHints() {
      // 模拟加载提示
      const allHints = {
        '月': ['明月几时有', '海上生明月', '月下独酌'],
        '花': ['花间一壶酒', '落红不是无情物', '花开堪折直须折'],
        '春': ['春眠不觉晓', '春风又绿江南岸', '一年之计在于春'],
        '酒': ['劝君更尽一杯酒', '酒逢知己千杯少', '举杯邀明月'],
        '山': ['山重水复疑无路', '青山不老为雪头', '山外青山楼外楼'],
        '鸟': ['两个黄鹂鸣翠柳', '鸟宿池边树', '千山鸟飞绝'],
        '水': ['问君能有几多愁', '黄河之水天上来', '落花流水春去也'],
        '云': ['白云深处有人家', '云想衣裳花想容', '行到水穷处'],
        '风': ['夜来风雨声', '风乍起吹皱一池春水', '大风起兮云飞扬'],
        '雪': ['千山鸟飞绝', '雪花纷纷何所似', '忽如一夜春风来']
      }
      
      this.hints = allHints[this.currentKeyword] || []
    },
    
    formatMessage(text) {
      // 高亮关键词
      const keyword = this.currentKeyword
      if (keyword && text.includes(keyword)) {
        return text.replace(
          new RegExp(keyword, 'g'),
          `<span class="keyword-highlight">${keyword}</span>`
        )
      }
      return text
    },
    
    formatTime(timestamp) {
      const date = new Date(timestamp)
      return date.toLocaleTimeString('zh-CN', { 
        hour: '2-digit', 
        minute: '2-digit' 
      })
    },
    
    getValidationIcon(type) {
      const icons = {
        success: 'icon-check',
        error: 'icon-x',
        warning: 'icon-alert-triangle'
      }
      return icons[type] || 'icon-info'
    },
    
    getPlaceholder() {
      if (this.gameEnded) return '游戏已结束'
      if (this.isValidating) return '正在验证...'
      return `请输入包含"${this.currentKeyword}"的诗句`
    }
  }
}
</script>

<style lang="scss" scoped>
@import './styles/game-common.scss';

// 🚀 重构：确保聊天区域完全可用
.chat-area-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  position: relative;
  background: transparent;
}

// 🔧 消息区域包装器
.chat-messages-wrapper {
  flex: 1;
  min-height: 0;
  position: relative;
  overflow: hidden;
}

// 🔧 消息滚动容器 - 关键修复
.chat-messages {
  height: 100%;
  overflow-y: auto;
  padding: 1rem;
  
  // 🔧 确保从顶部开始显示，不被遮挡
  scroll-behavior: smooth;
  
  // 美化滚动条
  &::-webkit-scrollbar {
    width: 8px;
  }
  
  &::-webkit-scrollbar-track {
    background: rgba(140, 120, 83, 0.1);
    border-radius: 4px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(140, 120, 83, 0.3);
    border-radius: 4px;
    
    &:hover {
      background: rgba(140, 120, 83, 0.5);
    }
  }
  
  // 🔧 火狐浏览器滚动条
  scrollbar-width: thin;
  scrollbar-color: rgba(140, 120, 83, 0.3) rgba(140, 120, 83, 0.1);
}

.message-wrapper {
  margin-bottom: 1rem;
  animation: slideInRight 0.4s ease-out;
  
  &.user {
    display: flex;
    justify-content: flex-end;
  }
  
  &.system {
    display: flex;
    justify-content: flex-start;
  }
}

.message-bubble {
  max-width: 70%;
  position: relative;
  
  &.user {
    .message-content {
      background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
      color: white;
      border-radius: 18px 18px 4px 18px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    }
  }
  
  &.system {
    display: flex;
    align-items: flex-start;
    gap: 0.5rem;
    
    .message-content {
      background: white;
      color: var(--text-color);
      border-radius: 18px 18px 18px 4px;
      @include ancient-shadow;
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    }
  }
  
  @media (max-width: 768px) {
    max-width: 85%;
  }
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 1.1rem;
  flex-shrink: 0;
}

.message-content {
  padding: 0.75rem 1rem;
  
  .message-text {
    @include ancient-text;
    line-height: 1.6;
    margin-bottom: 0.5rem;
    
    :deep(.keyword-highlight) {
      background: linear-gradient(45deg, #ffd700, #ffed4e);
      padding: 2px 4px;
      border-radius: 4px;
      font-weight: bold;
      color: #8b4513;
    }
  }
  
  .message-time {
    font-size: 0.7rem;
    opacity: 0.7;
    text-align: right;
  }
}

.message-validation {
  margin-top: 0.5rem;
  padding-top: 0.5rem;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.validation-result {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  margin-bottom: 0.3rem;
  
  &.success {
    color: #27ae60;
  }
  
  &.error {
    color: #e74c3c;
  }
  
  &.warning {
    color: #f39c12;
  }
}

.poem-info {
  font-size: 0.8rem;
  opacity: 0.8;
  
  .poem-title {
    font-weight: 500;
    margin-right: 0.5rem;
  }
  
  .poem-author {
    font-style: italic;
  }
}

.typing-indicator {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 1rem;
  color: #666;
  font-size: 0.9rem;
}

.typing-dots {
  display: flex;
  gap: 0.2rem;
  
  span {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--primary-color);
    animation: typingDot 1.4s infinite;
    
    &:nth-child(2) {
      animation-delay: 0.2s;
    }
    
    &:nth-child(3) {
      animation-delay: 0.4s;
    }
  }
}

@keyframes typingDot {
  0%, 60%, 100% {
    transform: scale(1);
    opacity: 0.5;
  }
  30% {
    transform: scale(1.2);
    opacity: 1;
  }
}

// 🔧 输入区域 - 固定在底部
.chat-input-section {
  flex-shrink: 0;
  background: white;
  border-top: 1px solid var(--border-color);
  padding: 1rem;
  position: relative;
  z-index: 20;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
}

.quick-hints {
  margin-bottom: 1rem;
  
  .hint-label {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 0.9rem;
    color: var(--primary-color);
    margin-bottom: 0.5rem;
    font-weight: 500;
  }
  
  .hints-container {
    display: flex;
    gap: 0.5rem;
    flex-wrap: wrap;
    max-height: 80px;
    overflow-y: auto;
  }
  
  .hint-tag {
    padding: 0.3rem 0.8rem;
    background: rgba(140, 120, 83, 0.1);
    border: 1px solid rgba(140, 120, 83, 0.2);
    border-radius: 16px;
    font-size: 0.8rem;
    cursor: pointer;
    transition: all 0.3s ease;
    white-space: nowrap;
    
    &:hover {
      background: var(--primary-color);
      color: white;
      transform: translateY(-2px);
    }
  }
}

.input-container {
  .input-wrapper {
    display: flex;
    gap: 0.5rem;
    align-items: center;
    margin-bottom: 0.5rem;
  }
  
  .message-input {
    flex: 1;
    padding: 0.75rem 1rem;
    border: 2px solid var(--border-color);
    border-radius: 24px;
    background: var(--card-background);
    color: var(--text-color);
    font-size: 0.9rem;
    transition: all 0.3s ease;
    
    &:focus {
      outline: none;
      border-color: var(--primary-color);
      box-shadow: 0 0 0 3px rgba(140, 120, 83, 0.1);
    }
    
    &:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }
  }
  
  .input-actions {
    display: flex;
    gap: 0.5rem;
  }
  
  .hint-button, .send-button {
    width: 44px;
    height: 44px;
    border: none;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: all 0.3s ease;
    font-size: 1.1rem;
    
    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  }
  
  .hint-button {
    background: linear-gradient(135deg, #f39c12, #e67e22);
    color: white;
    position: relative;
    
    &:hover:not(:disabled) {
      transform: scale(1.05);
    }
    
    span {
      position: absolute;
      top: -4px;
      right: -4px;
      background: #e74c3c;
      color: white;
      font-size: 0.7rem;
      width: 18px;
      height: 18px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
    }
  }
  
  .send-button {
    background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
    color: white;
    
    &:hover:not(:disabled) {
      transform: scale(1.05);
    }
  }
}

.input-validation {
  margin-bottom: 0.5rem;
  
  .validation-hint {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    font-size: 0.8rem;
    padding: 0.3rem 0.8rem;
    border-radius: 12px;
    
    &.success {
      background: rgba(39, 174, 96, 0.1);
      color: #27ae60;
    }
    
    &.warning {
      background: rgba(243, 156, 18, 0.1);
      color: #f39c12;
    }
    
    &.error {
      background: rgba(231, 76, 60, 0.1);
      color: #e74c3c;
    }
  }
}

.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.8rem;
  color: #666;
  
  .char-count {
    &.warning {
      color: #f39c12;
      font-weight: 500;
    }
  }
  
  .keyword-check {
    display: flex;
    align-items: center;
    gap: 0.3rem;
    
    .contains-keyword {
      color: #27ae60;
    }
    
    .missing-keyword {
      color: #f39c12;
    }
  }
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(30px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@media (max-width: 768px) {
  .chat-messages {
    padding: 1rem 0.5rem;
  }
  
  .quick-hints {
    .hints-container {
      gap: 0.3rem;
    }
    
    .hint-tag {
      font-size: 0.7rem;
      padding: 0.2rem 0.6rem;
    }
  }
  
  .input-footer {
    flex-direction: column;
    gap: 0.3rem;
    align-items: flex-start;
  }
  
  .chat-input-section {
    padding: 0.75rem;
  }
}
</style>