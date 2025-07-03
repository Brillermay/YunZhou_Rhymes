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
    
    <!-- 🔧 重构：输入区域 -->
    <div class="chat-input-section">
      <!-- 🔧 修复：快速提示 - 优化布局和间距 -->
      <div class="quick-hints" v-if="showHints && hints.length > 0">
        <div class="hint-header">
          <div class="hint-title">
            <i class="icon-lightbulb"></i>
            <span>诗句提示</span>
            <span class="hint-count-badge">{{ hints.length }}条</span>
          </div>
          <button 
            class="hide-hints-btn" 
            @click="hideHints"
            title="隐藏提示"
          >
            <i class="icon-eye-off"></i>
            <span>隐藏</span>
          </button>
        </div>
        
        <!-- 🔧 优化：提示内容区域 -->
        <div class="hints-content">
          <div class="hints-grid">
            <div 
              v-for="(hint, index) in hints" 
              :key="`${currentKeyword}-${index}`"
              class="hint-item"
              @click="selectHint(hint)"
              :title="`点击使用：${hint}`"
            >
              <i class="icon-quote-left hint-icon"></i>
              <span class="hint-text">{{ hint }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 🔧 修复：显示提示按钮 -->
      <div class="show-hints-section" v-if="!showHints && hints.length > 0 && hintCount > 0">
        <button 
          class="show-hints-btn"
          @click="requestShowHints"
          title="消耗1次提示机会查看诗句提示"
        >
          <div class="btn-content">
            <i class="icon-lightbulb"></i>
            <span class="btn-text">查看诗句提示</span>
          </div>
          <div class="btn-info">
            <span class="hints-count">{{ hints.length }}条提示</span>
            <span class="hint-cost">消耗1次</span>
          </div>
        </button>
      </div>
      
      <!-- 输入控件 -->
      <div class="input-container">
        <div class="input-wrapper">
          <input
            v-model="inputMessage"
            @keypress.enter="sendMessage"
            @input="validateInput"
            :disabled="gameEnded || isValidating || isPaused"
            :placeholder="getPlaceholder()"
            class="message-input"
            maxlength="100"
          />
          <div class="input-actions">
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
        
        <!-- 🔧 优化：输入状态栏 -->
        <div class="input-footer">
          <div class="footer-left">
            <div class="char-count" :class="{ warning: inputMessage.length > 80 }">
              {{ inputMessage.length }}/100
            </div>
            <div class="keyword-check">
              <i :class="containsKeyword ? 'icon-check contains-keyword' : 'icon-x missing-keyword'"></i>
              <span>{{ containsKeyword ? '包含关键词' : `需要"${currentKeyword}"` }}</span>
            </div>
          </div>
          <div class="footer-right">
            <div class="hint-status" v-if="hintCount > 0">
              <i class="icon-help-circle"></i>
              <span>剩余提示 {{ hintCount }} 次</span>
            </div>
            <div class="hint-status disabled" v-else>
              <i class="icon-help-circle"></i>
              <span>提示已用完</span>
            </div>
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
    hintCount: { type: Number, default: 3 },
      // 🔧 新增：暂停状态属性
  isPaused: { type: Boolean, default: false }
  },
  emits: ['send-message', 'request-hint'],
  data() {
    return {
      inputMessage: '',
      inputValidation: null,
      hints: [],
      showHints: false
    }
  },
  computed: {
    characterCount() {
      return this.inputMessage.length
    },
    containsKeyword() {
      return this.inputMessage.includes(this.currentKeyword)
    },
    canSend() {
      return this.inputMessage.trim().length > 0 && 
             !this.isValidating && 
             !this.gameEnded &&
             !this.isPaused && // 🔧 新增：暂停时禁用发送
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
    currentKeyword(newKeyword, oldKeyword) {
      if (newKeyword !== oldKeyword) {
        this.showHints = false
        this.loadHints()
      }
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
      this.showHints = false
    },
    
    requestShowHints() {
      if (this.hintCount <= 0) return
      
      this.showHints = true
      this.$emit('request-hint')
    },
    
    hideHints() {
      this.showHints = false
    },
    
    selectHint(hint) {
      this.inputMessage = hint
      this.validateInput()
      this.showHints = false
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
        container.scrollTop = container.scrollHeight
      }
    },
    
    loadHints() {
      const allHints = {
        '月': ['明月几时有，把酒问青天', '海上生明月，天涯共此时', '月下独酌无相亲', '举杯邀明月，对影成三人'],
        '花': ['花间一壶酒，独酌无相亲', '落红不是无情物，化作春泥更护花', '花开堪折直须折，莫待无花空折枝', '桃花潭水深千尺，不及汪伦送我情'],
        '春': ['春眠不觉晓，处处闻啼鸟', '春风又绿江南岸，明月何时照我还', '一年之计在于春，一日之计在于晨', '春花秋月何时了，往事知多少'],
        '酒': ['劝君更尽一杯酒，西出阳关无故人', '酒逢知己千杯少，话不投机半句多', '举杯邀明月，对影成三人', '金樽清酒斗十千，玉盘珍羞直万钱'],
        '山': ['山重水复疑无路，柳暗花明又一村', '青山不老，为雪白头', '山外青山楼外楼，西湖歌舞几时休', '会当凌绝顶，一览众山小'],
        '鸟': ['两个黄鹂鸣翠柳，一行白鹭上青天', '鸟宿池边树，僧敲月下门', '千山鸟飞绝，万径人踪灭', '月出惊山鸟，时鸣春涧中'],
        '水': ['问君能有几多愁，恰似一江春水向东流', '黄河之水天上来，奔流到海不复回', '落花流水春去也，天上人间', '桃花潭水深千尺，不及汪伦送我情'],
        '云': ['白云深处有人家，停车坐爱枫林晚', '云想衣裳花想容，春风拂槛露华浓', '行到水穷处，坐看云起时', '黄河远上白云间，一片孤城万仞山'],
        '风': ['夜来风雨声，花落知多少', '风乍起，吹皱一池春水', '大风起兮云飞扬，威加海内兮归故乡', '春风不度玉门关，羌笛何须怨杨柳'],
        '雪': ['千山鸟飞绝，万径人踪灭', '雪花纷纷何所似，撒盐空中差可拟', '忽如一夜春风来，千树万树梨花开', '北国风光，千里冰封，万里雪飘']
      }
      
      this.hints = allHints[this.currentKeyword] || []
    },
    
    formatMessage(text) {
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
      if (this.isPaused) return '⏸️ 游戏已暂停，请继续游戏'
      if (this.isValidating) return '正在验证...'
      return `请输入包含"${this.currentKeyword}"的诗句`
    }
  }
}
</script>

<style lang="scss" scoped>
@import './styles/game-common.scss';

.chat-area-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  position: relative;
  background: transparent;
}

.chat-messages-wrapper {
  flex: 1;
  min-height: 0;
  position: relative;
  overflow: hidden;
}

.chat-messages {
  height: 100%;
  overflow-y: auto;
  padding: 1rem;
  scroll-behavior: smooth;
  
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

// 🔧 输入区域样式
.chat-input-section {
  flex-shrink: 0;
  background: white;
  border-top: 1px solid var(--border-color);
  padding: 1rem;
  position: relative;
  z-index: 20;
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.1);
}

// 🔧 优化：提示区域样式
.quick-hints {
  margin-bottom: 1rem;
  padding: 1.25rem;
  background: 
    linear-gradient(135deg, 
      rgba(140, 120, 83, 0.05) 0%, 
      rgba(110, 87, 115, 0.05) 100%
    );
  border: 2px solid rgba(140, 120, 83, 0.2);
  border-radius: 16px;
  animation: slideDown 0.3s ease-out;
  box-shadow: 0 2px 12px rgba(140, 120, 83, 0.1);
}

.hint-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid rgba(140, 120, 83, 0.15);
}

.hint-title {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 1rem;
  color: var(--primary-color);
  font-weight: 600;
  
  i {
    color: #f39c12;
    font-size: 1.2rem;
  }
  
  .hint-count-badge {
    background: rgba(243, 156, 18, 0.15);
    color: #e67e22;
    padding: 0.25rem 0.5rem;
    border-radius: 12px;
    font-size: 0.8rem;
    font-weight: 500;
  }
}

.hide-hints-btn {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  background: rgba(231, 76, 60, 0.1);
  border: 1px solid rgba(231, 76, 60, 0.3);
  border-radius: 8px;
  color: #e74c3c;
  cursor: pointer;
  font-size: 0.85rem;
  transition: all 0.3s ease;
  
  &:hover {
    background: rgba(231, 76, 60, 0.2);
    border-color: rgba(231, 76, 60, 0.5);
    transform: translateY(-1px);
  }
}

.hints-content {
  position: relative;
}

// 🔧 优化：提示网格布局
.hints-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 0.75rem;
  
  @media (max-width: 768px) {
    grid-template-columns: 1fr;
    gap: 0.5rem;
  }
}

// 🔧 优化：提示项样式
.hint-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 1.25rem;
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(140, 120, 83, 0.2);
  border-radius: 12px;
  color: var(--text-color);
  cursor: pointer;
  transition: all 0.3s ease;
  min-height: 50px;
  
  .hint-icon {
    color: #8b4513;
    font-size: 1rem;
    flex-shrink: 0;
  }
  
  .hint-text {
    flex: 1;
    font-size: 0.9rem;
    line-height: 1.4;
    font-weight: 500;
    @include ancient-text;
  }
  
  &:hover {
    background: rgba(140, 120, 83, 0.08);
    border-color: rgba(140, 120, 83, 0.4);
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(140, 120, 83, 0.2);
  }
  
  &:active {
    transform: translateY(0);
    background: rgba(140, 120, 83, 0.15);
  }
}

// 🔧 优化：显示提示按钮
.show-hints-section {
  margin-bottom: 1rem;
  text-align: center;
}

.show-hints-btn {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 1rem 1.5rem;
  background: 
    linear-gradient(135deg, 
      rgba(243, 156, 18, 0.08) 0%, 
      rgba(230, 126, 34, 0.08) 100%
    );
  border: 2px solid rgba(243, 156, 18, 0.3);
  border-radius: 12px;
  color: #e67e22;
  cursor: pointer;
  transition: all 0.3s ease;
  
  .btn-content {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    
    i {
      color: #f39c12;
      font-size: 1.2rem;
    }
    
    .btn-text {
      font-size: 1rem;
      font-weight: 600;
    }
  }
  
  .btn-info {
    display: flex;
    align-items: center;
    gap: 0.75rem;
    
    .hints-count {
      font-size: 0.85rem;
      color: #666;
      font-weight: 500;
    }
    
    .hint-cost {
      font-size: 0.8rem;
      color: #e74c3c;
      background: rgba(231, 76, 60, 0.12);
      padding: 0.3rem 0.6rem;
      border-radius: 8px;
      font-weight: 600;
    }
  }
  
  &:hover {
    background: 
      linear-gradient(135deg, 
        rgba(243, 156, 18, 0.12) 0%, 
        rgba(230, 126, 34, 0.12) 100%
      );
    border-color: rgba(243, 156, 18, 0.5);
    transform: translateY(-2px);
    box-shadow: 0 4px 16px rgba(243, 156, 18, 0.2);
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
  
  .send-button {
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
    background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
    color: white;
    
    &:hover:not(:disabled) {
      transform: scale(1.05);
    }
    
    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
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

// 🔧 优化：输入状态栏
.input-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 0.5rem;
  padding-top: 0.5rem;
  border-top: 1px solid rgba(140, 120, 83, 0.1);
  
  .footer-left {
    display: flex;
    align-items: center;
    gap: 1rem;
  }
  
  .footer-right {
    display: flex;
    align-items: center;
  }
  
  .char-count {
    font-size: 0.8rem;
    color: #666;
    font-weight: 500;
    
    &.warning {
      color: #f39c12;
    }
  }
  
  .keyword-check {
    display: flex;
    align-items: center;
    gap: 0.4rem;
    font-size: 0.8rem;
    
    .contains-keyword {
      color: #27ae60;
    }
    
    .missing-keyword {
      color: #f39c12;
    }
  }
  
  .hint-status {
    display: flex;
    align-items: center;
    gap: 0.4rem;
    font-size: 0.8rem;
    color: #f39c12;
    font-weight: 500;
    
    &.disabled {
      color: #999;
    }
    
    i {
      font-size: 0.9rem;
    }
  }
}

// 动画效果
@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
    max-height: 0;
  }
  to {
    opacity: 1;
    transform: translateY(0);
    max-height: 500px;
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

// 移动端适配
@media (max-width: 768px) {
  .chat-messages {
    padding: 1rem 0.5rem;
  }
  
  .chat-input-section {
    padding: 0.75rem;
  }
  
  .quick-hints {
    padding: 1rem;
  }
  
  .hint-item {
    padding: 0.75rem 1rem;
    min-height: 44px;
    
    .hint-text {
      font-size: 0.85rem;
    }
  }
  
  .input-footer {
    flex-direction: column;
    gap: 0.5rem;
    align-items: flex-start;
    
    .footer-left {
      gap: 0.75rem;
    }
  }
  
  .show-hints-btn {
    flex-direction: column;
    gap: 0.5rem;
    
    .btn-info {
      gap: 0.5rem;
    }
  }
}
</style>