<template>
  <div class="feihua-container">
    <header class="feihua-header">
      <h1>飞花令</h1>
    </header>
    
    <div v-if="!gameStarted" class="start-container">
      <button @click="startGame" class="start-btn">
        <i class="iconfont">🌸</i> 开始游戏 
      </button>
    </div>
    
    <div v-if="gameStarted" class="chat-area">
      <div class="keyword-display">
        当前关键词：<span class="keyword-mark">{{ currentKeyword }}</span>
      </div>
      
      <div ref="chatMessages" class="chat-messages">
        <div 
          v-for="(message, index) in chatHistory" 
          :key="index" 
          :class="['message-bubble', message.type]"  
        >
          <div class="bubble-content" :style="bubbleStyle(message.text)"> 
            {{ message.text  }}
          </div>
          <div class="bubble-time">
            {{ message.type  === 'system' ? '系统' : '我' }} {{ message.time  }}
          </div>
        </div>
      </div>
      
      <div class="chat-input-area">
        <input 
          v-model="userInput"
          @keyup.enter="submitVerse" 
          placeholder="请吟诗一句..."
          class="chat-input"
        />
        <span v-if="showError" class="error-mark">×</span>
      </div>
    </div>
  </div>
</template>
 
<script>
export default {
  name: 'FeiHuaLingChat',
  data() {
    return {
      gameStarted: false,
      currentKeyword: '',
      userInput: '',
      chatHistory: [],
      showError: false,
      errorTimeout: null,
      usedVerses: [], // 新增：记录已使用过的诗句
      versesMap: {
        '花': [
          '花间一壶酒，独酌无相亲。',
          '感时花溅泪，恨别鸟惊心。',
          '春宵一刻值千金，花有清香月有阴。',
          '人闲桂花落，夜静春山空。',
          '花开堪折直须折，莫待无花空折枝。'
        ],
        '月': [
          '床前明月光，疑是地上霜。',
          '海上生明月，天涯共此时。',
          '露从今夜白，月是故乡明。',
          '月落乌啼霜满天，江枫渔火对愁眠。',
          '春江潮水连海平，海上明月共潮生。'
        ],
        '春': [
          '春眠不觉晓，处处闻啼鸟。',
          '红豆生南国，春来发几枝。',
          '好雨知时节，当春乃发生。',
          '春蚕到死丝方尽，蜡炬成灰泪始干。',
          '春色满园关不住，一枝红杏出墙来。'
        ],
        '风': [
          '夜来风雨声，花落知多少。',
          '野火烧不尽，春风吹又生。',
          '随风潜入夜，润物细无声。',
          '长风破浪会有时，直挂云帆济沧海。',
          '相见时难别亦难，东风无力百花残。'
        ],
        '山': [
          '会当凌绝顶，一览众山小。',
          '千山鸟飞绝，万径人踪灭。',
          '空山新雨后，天气晚来秋。',
          '不识庐山真面目，只缘身在此山中。',
          '山重水复疑无路，柳暗花明又一村。'
        ]
      }
    };
  },
  methods: {
    bubbleStyle(text) {
      const length = text.length;
      let width = Math.min(Math.max(length * 16, 154), 300);
      return { width: `${width}px` };
    },

    async startGame() {
      try {
        // 需要调用 API 获取随机关键词
        const response = await this.fetchRandomKeyword();
        this.currentKeyword = response.keyword;
        this.gameStarted = true;
        this.chatHistory = [];
        this.userInput = '';

        this.addSystemMessage(` 飞花令游戏开始！请说出包含"${this.currentKeyword}" 的诗句`);
      } catch (error) {
        console.error(' 获取关键词失败:', error);
        this.addSystemMessage(" 游戏开始失败，请刷新页面重试");
      }
    },

    async submitVerse() {
      if (!this.userInput.trim()) return;

      // 检查是否已经使用过该诗句
      if (this.usedVerses.includes(this.userInput)) {
        this.showError = true;
        if (this.errorTimeout) clearTimeout(this.errorTimeout);
        this.errorTimeout = setTimeout(() => {
          this.showError = false;
        }, 3000);
        
        setTimeout(() => {
          this.addSystemMessage(`这句诗已经用过了，请换一句包含"${this.currentKeyword}"的诗句`);
        }, 800);
        return;
      }

      // 先记录用户输入的诗句
      this.usedVerses.push(this.userInput);
      this.addUserMessage(this.userInput);

      try {
        const isValid = await this.validateVerse(this.userInput, this.currentKeyword);

        if (isValid) {
          this.showError = false;
          
          const response = await this.fetchRelatedVerse(this.currentKeyword);
          setTimeout(() => {
            this.addSystemMessage(response.verse);
          }, 800);
        } else {
          // 如果验证失败，从已使用列表中移除
          this.usedVerses = this.usedVerses.filter(v => v !== this.userInput);
          this.showError = true;
          if (this.errorTimeout) clearTimeout(this.errorTimeout);
          this.errorTimeout = setTimeout(() => {
            this.showError = false;
          }, 3000);

          setTimeout(() => {
            this.addSystemMessage(`请输入正确的完整诗句，当前关键词是"${this.currentKeyword}"`);
          }, 800);
        }
      } catch (error) {
        // 出错时也从已使用列表中移除
        this.usedVerses = this.usedVerses.filter(v => v !== this.userInput);
        console.error('验证诗句失败:', error);
        this.showError = true;
        this.addSystemMessage("系统出错，请稍后再试");
      } finally {
        this.userInput = '';
      }
    },

    async validateVerse(verse, keyword) {
      return new Promise((resolve) => {
        setTimeout(() => {
          const verses = this.versesMap[keyword] || [];

          const cleanedInput = verse.trim()
            .replace(/[，。！？、；：'"“”‘’「」【】（）〔〕\s]/g, '');

          const isExactMatch = verses.some(dbVerse => {
            const cleanedDbVerse = dbVerse.replace(/[ ，。！？、；：'"“”‘’「」【】（）〔〕]/g, '');
            return cleanedInput === cleanedDbVerse;
          });

          resolve(isExactMatch);
        }, 100);
      });
    },

    addUserMessage(text) {
      this.chatHistory.push({
        type: 'user',
        text: text,
        time: this.getCurrentTime()
      });
      this.scrollToBottom();
    },

    addSystemMessage(text) {
      this.chatHistory.push({
        type: 'system',
        text: text,
        time: this.getCurrentTime()
      });
      this.scrollToBottom();
    },

    getCurrentTime() {
      const now = new Date();
      return `${now.getHours()}:${now.getMinutes().toString().padStart(2, '0')}`;
    },

    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.chatMessages;
        container.scrollTop = container.scrollHeight;
      });
    },

    async fetchRandomKeyword() {
      // 需要调用 API 获取随机关键词
      return new Promise((resolve) => {
        setTimeout(() => {
          const keywords = Object.keys(this.versesMap);
          const randomKeyword = keywords[Math.floor(Math.random() * keywords.length)];
          resolve({ keyword: randomKeyword });
        }, 100);
      });
    },

    async fetchRelatedVerse(keyword) {
      // 需要调用 API 获取包含该关键词的诗句
      return new Promise((resolve) => {
        setTimeout(() => {
          const verses = this.versesMap[keyword] || ['暂未找到相关诗句。'];
          // 过滤掉已经使用过的诗句
          const availableVerses = verses.filter(verse => !this.usedVerses.includes(verse));
          
          // 如果没有可用的诗句了，返回提示信息
          if (availableVerses.length === 0) {
            resolve({ verse: `所有包含"${keyword}"的诗句已用完` });
            return;
          }
          
          const randomVerse = availableVerses[Math.floor(Math.random() * availableVerses.length)];
          // 记录已使用的诗句
          this.usedVerses.push(randomVerse);
          resolve({ verse: randomVerse });
        }, 100);
      });
    }
  }
};
</script>
 
<style scoped>
.feihua-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 0;
  background: #f5efe6;
  height: 100vh;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  display: flex;
  flex-direction: column;
}
 
.feihua-header {
  text-align: center;
  padding: 1.2rem;
  background: linear-gradient(to right, #8c7853, #6e5773);
  color: white;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}
 
.feihua-header h1 {
  margin: 0;
  font-size: 2rem;
  font-weight: normal;
}
 
.start-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
 
.start-btn {
  padding: 14px 32px;
  font-size: 1.2rem;
  background: linear-gradient(to right, #8c7853, #6e5773);
  color: white;
  border: none;
  border-radius: 30px;
  cursor: pointer;
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 8px;
}
 
.start-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 12px rgba(0,0,0,0.2);
}
 
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
  min-height: 0;
}
 
.keyword-display {
  text-align: center;
  font-size: 1.1rem;
  color: #5a4634;
  padding: 0.8rem;
  background: #f8f4ed;
  border-radius: 0;
}
 
.keyword-mark {
  font-weight: bold;
  color: #8c7853;
  font-size: 1.2rem;
}
 
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 1rem;
  min-height: 0;
}
 
.message-bubble {
  margin-bottom: 1.2rem;
  animation: fadeIn 0.3s ease;
}
 
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
 
.message-bubble.user  {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}
 
.message-bubble.system  {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
 
.bubble-content {
  padding: 12px 18px;
  border-radius: 18px;
  position: relative;
  word-break: break-word;
  font-size: 1.1rem;
  line-height: 1.6;
  box-shadow: 0 2px 4px rgba(0,0,0,0.08);
  max-width: 80%;
  font-family: '楷体', cursive;
}
 
.user .bubble-content {
  background: linear-gradient(to right, #8c7853, #6e5773);
  color: white;
  border-top-right-radius: 4px;
}
 
.system .bubble-content {
  background: #f3f0eb;
  color: #5a4634;
  border-top-left-radius: 4px;
}
 
.bubble-time {
  font-size: 0.85rem;
  color: #999;
  margin-top: 4px;
  padding: 0 8px;
}
 
.chat-input-area {
  position: sticky;
  bottom: 0;
  padding: 1rem;
  background: white;
  border-top: 1px solid #f0e6d2;
}
 
.chat-input {
  width: 100%;
  padding: 14px 18px;
  font-size: 1rem;
  border: 1px solid #d6cab4;
  border-radius: 30px;
  outline: none;
  box-sizing: border-box;
  background: #f8f4ed;
  transition: all 0.3s;
  font-family: '楷体', cursive;
}
 
.chat-input:focus {
  border-color: #8c7853;
  box-shadow: 0 0 0 2px rgba(140, 120, 83, 0.2);
}
 
.error-mark {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  color: #c0392b;
  font-size: 24px;
  font-weight: bold;
  animation: shake 0.5s;
}
 
@keyframes shake {
  0%, 100% { transform: translateY(-50%); }
  20%, 60% { transform: translateY(-50%) translateX(-5px); }
  40%, 80% { transform: translateY(-50%) translateX(5px); }
}
 
/* 响应式调整 */
@media (max-width: 600px) {
  .feihua-container {
    max-width: 100%;
  }
  
  .feihua-header {
    padding: 1rem;
    border-radius: 0;
  }
  
  .start-btn {
    padding: 12px 28px;
    font-size: 1.1rem;
  }
  
  .bubble-content {
    font-size: 1rem;
    padding: 10px 16px;
    max-width: 85%;
  }
  
  .chat-input-area {
    padding: 0.8rem;
  }
}
</style>
