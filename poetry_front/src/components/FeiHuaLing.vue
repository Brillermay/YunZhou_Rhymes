<template>
  <div class="feihua-layout">
    <div class="feihua-container">
      <header class="feihua-header">
        <h1>飞花令</h1>
        <p class="subtitle">"飞花逐月吟诗句，妙语连珠对古今"</p>
        <!-- 添加返回按钮 -->
        <button 
          v-if="gameStarted" 
          @click="returnToModeSelection" 
          class="return-btn"
        >
          ← 返回 
        </button>
      </header>
      
      <div v-if="!gameStarted" class="start-container">
        <div class="mode-selector">
          <h3>选择游戏模式</h3>
          <div class="mode-options">
            <button 
              v-for="option in modeOptions"
              :key="option.value" 
              @click="mode = option.value" 
              :class="{ active: mode === option.value  }"
            >
              {{ option.label  }}
            </button>
          </div>
        </div>
        
        <div class="difficulty-selector" v-if="mode">
          <h3>选择难度</h3>
          <div class="difficulty-options">
            <button 
              v-for="option in difficultyOptions"
              :key="option.value" 
              @click="difficulty = option.value" 
              :class="{ active: difficulty === option.value  }"
            >
              {{ option.label  }}
            </button>
          </div>
        </div>
        
        <button @click="startGame" class="start-btn" :disabled="!difficulty || !mode">
          <i class="iconfont">🌸</i> 开始游戏 
        </button>
      </div>
      
      <div v-if="gameStarted" class="chat-area">
        <div class="info-display">
          <div>
            当前关键词：<span class="keyword-mark">{{ currentKeyword }}</span>
          </div>
          <div>
            当前模式：<span class="mode-mark">{{ getModeLabel }}</span>
            <span v-if="mode === 'endless'"> | 已答题：{{ answerCount/2 }}次</span>
            <span v-if="mode === 'challenge'"> | 第{{ currentRound }}轮 ({{ roundProgress }})</span>
          </div>
          <div>
            当前难度：<span class="difficulty-mark">{{ getDifficultyLabel }}</span>
            <span class="countdown" :class="{ warning: countdown <= 10 }">
               | 剩余时间：{{ countdown }}秒 
            </span>
          </div>
        </div>
        
        <div ref="chatMessages" class="chat-messages">
          <div 
            v-for="(message, index) in chatHistory" 
            :key="index" 
            :class="['message-bubble', message.type]"   
          >
            <div class="bubble-content" :style="bubbleStyle(message.text)">  
              {{ message.text   }}
            </div>
            <div class="bubble-time">
              {{ message.type   === 'system' ? '系统' : '我' }} {{ message.time   }}
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
    
    <aside class="sidebar" v-if="gameStarted && mode !== 'endless'">
      <h2>历史记录</h2>
      <ul class="history-list">
        <li 
          v-for="(item, index) in historyRecords" 
          :key="index"
          @click="loadHistory(index)"
        >
          {{ item.date  }} - {{ item.keyword  }}
        </li>
      </ul>
      <button @click="restartGame" class="new-game-btn">🎮 新的挑战</button>
    </aside>
  </div>
</template>
 
<script>

import axios from 'axios';

export default {
  name: 'FeiHuaLingChat',
  computed: {
    getDifficultyLabel() {
      const option = this.difficultyOptions.find(opt  => opt.value  === this.difficulty); 
      return option ? option.label  : '';
    },
    getModeLabel() {
      const option = this.modeOptions.find(opt  => opt.value  === this.mode); 
      return option ? option.label  : '';
    },
    roundProgress() {
      if (this.mode  !== 'challenge') return '';
      const roundInfo = this.challengeRounds[this.currentRound  - 1];
      return `${this.currentSuccessCount}/${roundInfo.required} 次`;
    }
  },
  data() {
    return {
      gameStarted: false,
      keywords: ['月', '花', '春'],
      currentKeyword: '',
      historyRecords: [],
      userInput: '',
      chatHistory: [],
      showError: false,
      errorTimeout: null,
      apiVerses: [], // 用于存储从API获取的诗句
      usedVerses: [], // 记录已使用过的诗句 
      
      // 游戏模式相关 
      mode: '', // 'endless' 或 'challenge'
      modeOptions: [
        { value: 'endless', label: '无尽模式' },
        { value: 'challenge', label: '闯关模式' }
      ],
      
      // 难度相关 
      difficulty: '', // 'easy', 'normal', 'hard'
      difficultyOptions: [
        { value: 'easy', label: '简单', time: 45 },
        { value: 'normal', label: '普通', time: 30 },
        { value: 'hard', label: '困难', time: 15 }
      ],
      
      // 诗句数据库 
      versesList: [
        '花间一壶酒，独酌无相亲。',
        '感时花溅泪，恨别鸟惊心。',
        '春宵一刻值千金，花有清香月有阴。',
        '人闲桂花落，夜静春山空。',
        '花开堪折直须折，莫待无花空折枝。',
        '床前明月光，疑是地上霜。',
        '海上生明月，天涯共此时。',
        '露从今夜白，月是故乡明。',
        '月落乌啼霜满天，江枫渔火对愁眠。',
        '春江潮水连海平，海上明月共潮生。',
        '春眠不觉晓，处处闻啼鸟。',
        '红豆生南国，春来发几枝。',
        '好雨知时节，当春乃发生。',
        '春蚕到死丝方尽，蜡炬成灰泪始干。',
        '春色满园关不住，一枝红杏出墙来。',
        '夜来风雨声，花落知多少。',
        '野火烧不尽，春风吹又生。',
        '随风潜入夜，润物细无声。',
        '长风破浪会有时，直挂云帆济沧海。',
        '相见时难别亦难，东风无力百花残。',
        '会当凌绝顶，一览众山小。',
        '千山鸟飞绝，万径人踪灭。',
        '空山新雨后，天气晚来秋。',
        '不识庐山真面目，只缘身在此山中。',
        '山重水复疑无路，柳暗花明又一村。'
      ],
 
      // 游戏状态 
      countdown: 30,
      countdownInterval: null,
      gameEnded: false,
      
      // 无尽模式计数 
      answerCount: 0,
      
      // 闯关模式相关 
      challengeRounds: [
        { required: 3, keyword: '' },
        { required: 5, keyword: '' },
        { required: 7, keyword: '' }
      ],
      currentRound: 1,
      currentSuccessCount: 0 
    };
  },
  methods: {
    returnToModeSelection() {
      this.clearCountdown(); 
      this.gameStarted  = false;
      this.gameEnded  = false;
    },
    bubbleStyle(text) {
      const length = text.length; 
      let width = Math.min(Math.max(length  * 16, 154), 300);
      return { width: `${width}px` };
    },
 
    startCountdown() {
      this.clearCountdown(); 
      const selectedDifficulty = this.difficultyOptions.find( 
        opt => opt.value  === this.difficulty  
      );
      this.countdown  = selectedDifficulty.time; 
      this.countdownInterval  = setInterval(() => {
        this.countdown--; 
        if (this.countdown  <= 0) {
          this.gameFailed(); 
        }
      }, 1000);
    },
    
    clearCountdown() {
      if (this.countdownInterval)  {
        clearInterval(this.countdownInterval); 
        this.countdownInterval  = null;
      }
    },
    getAvailableVerses(verses) {
      // 过滤掉已用过的诗句，返回可用的
     return verses.filter(verse => !this.usedVerses.includes(verse));
    },
    gameFailed() {
      this.clearCountdown(); 
      this.gameEnded  = true;
      let message = '时间到！挑战失败。';
      
      if (this.mode  === 'challenge') {
        message += ` 您完成了第${this.currentRound} 轮 ${this.currentSuccessCount}/${this.challengeRounds[this.currentRound  - 1].required}次`;
      }
      
      this.addSystemMessage(message); 
      
      // 闯关模式下记录失败 
      if (this.mode  === 'challenge') {
        this.historyRecords.push({ 
          keyword: this.currentKeyword, 
          date: new Date().toLocaleString(),
          chatHistory: [...this.chatHistory], 
          usedVerses: [...this.usedVerses], 
          completed: false,
          round: this.currentRound, 
          successCount: this.currentSuccessCount  
        });
      }
    },
    
    loadHistory(index) {
      const record = this.historyRecords[index]; 
      this.currentKeyword  = record.keyword; 
      this.chatHistory  = [...record.chatHistory]; 
      this.usedVerses  = [...record.usedVerses]; 
      this.gameStarted  = true;
      this.gameEnded  = true;
      
      if (this.mode  === 'challenge') {
        this.currentRound  = record.round; 
        this.currentSuccessCount  = record.successCount; 
      }
      
      this.addSystemMessage(` 你正在查看 ${record.date}  的挑战记录`);
    },
    
    restartGame() {
      this.gameStarted  = false;
      this.gameEnded  = false;
      this.mode  = '';
      this.difficulty  = '';
      this.clearCountdown(); 
    },
    
    // 修改游戏成功逻辑 
    gameSuccess() {
      this.clearCountdown(); 
      
      // 无尽模式处理 
      if (this.mode  === 'endless') {
        this.answerCount++; 
        this.startCountdown(); 
        this.addSystemMessage(` 回答正确！已答题 ${this.answerCount/2}  次，继续挑战！`);
        return;
      }
      
      // 闯关模式处理 
      this.currentSuccessCount++; 
      
      const currentRoundInfo = this.challengeRounds[this.currentRound  - 1];
      if (this.currentSuccessCount  >= currentRoundInfo.required)  {
        // 完成当前轮次 
        if (this.currentRound  >= this.challengeRounds.length)  {
          // 完成全部三轮 
          this.gameEnded  = true;
          this.addSystemMessage(' 恭喜！闯关成功！你完成了全部三轮挑战！');
          this.historyRecords.push({ 
            keyword: this.currentKeyword, 
            date: new Date().toLocaleString(),
            chatHistory: [...this.chatHistory], 
            usedVerses: [...this.usedVerses], 
            completed: true,
            round: this.currentRound, 
            successCount: this.currentSuccessCount  
          });
        } else {
          // 进入下一轮 
          this.addSystemMessage(` 恭喜完成第${this.currentRound} 轮！即将进入第${this.currentRound  + 1}轮`);
          setTimeout(() => {
            this.currentRound++; 
            this.currentSuccessCount  = 0;
            this.startNewRound(); 
          }, 2000);
        }
      } else {
        // 继续当前轮次 
        this.addSystemMessage(` 回答正确！当前进度：${this.roundProgress}`); 
        this.startCountdown(); 
      }
    },
    
    async startNewRound() {
      try {
        const response = await this.fetchRandomKeyword();
       this.currentKeyword = response.keyword;
    
       // 获取新关键词对应的诗句
       const verseResponse = await this.fetchRelatedVerse(this.currentKeyword);
        this.apiVerses = verseResponse.verses;
    
       this.chatHistory = [];
        this.usedVerses = [];
        this.gameEnded = false;
    
        const roundInfo = this.challengeRounds[this.currentRound - 1];
        roundInfo.keyword = this.currentKeyword;
    
        this.addSystemMessage(`第${this.currentRound}轮开始！关键词："${this.currentKeyword}"，需要完成${roundInfo.required}次正确回答`);
       this.startCountdown();
      } catch (error) {
        console.error('开始新轮次失败:', error);
        this.addSystemMessage("开始新轮次失败，请重新开始游戏");
     }
    },
    
    async startGame() {
      try {
        const response = await this.fetchRandomKeyword(); 
        this.currentKeyword  = response.keyword; 
        const verseResponse = await this.fetchRelatedVerse(this.currentKeyword);
        this.apiVerses = verseResponse.verses;

        const availableVerses = this.getAvailableVerses(this.apiVerses);
        if (availableVerses.length === 0) {
          this.addSystemMessage("没有可用的诗句，请重新开始游戏");
          return;
        }
        this.gameStarted  = true;
        this.gameEnded  = false;
        this.chatHistory  = [];
        this.userInput  = '';
        this.usedVerses  = [];
        
        // 初始化模式相关数据 
        if (this.mode  === 'endless') {
          this.answerCount  = 0;
          this.addSystemMessage(` 无尽模式开始！已答题 0 次，请说出包含"${this.currentKeyword}" 的诗句`);
        } else if (this.mode  === 'challenge') {
          this.currentRound  = 1;
          this.currentSuccessCount  = 0;
          this.challengeRounds[0].keyword  = this.currentKeyword; 
          this.addSystemMessage(` 闯关模式开始！第1轮关键词："${this.currentKeyword}" ，需要完成3次正确回答`);
        }
        
        this.startCountdown(); 
      } catch (error) {
        console.error(' 获取关键词失败:', error);
        this.addSystemMessage(" 游戏开始失败，请刷新页面重试");
      }
    },
    
    async submitVerse() {
      if (!this.userInput.trim()  || this.gameEnded)  return;
      
      // 检查是否已经使用过该诗句 
      if (this.usedVerses.includes(this.userInput))  {
        this.showError  = true;
        if (this.errorTimeout)  clearTimeout(this.errorTimeout); 
        this.errorTimeout  = setTimeout(() => {
          this.showError  = false;
        }, 3000);
        
        setTimeout(() => {
          this.addSystemMessage(` 这句诗已经用过了，请换一句包含"${this.currentKeyword}" 的诗句`);
        }, 800);
        return;
      }
 
      // 记录用户输入 
      this.usedVerses.push(this.userInput); 
      this.addUserMessage(this.userInput); 
 
      try {
        const isValid = await this.validateVerse(this.userInput,  this.currentKeyword); 
      
        if (isValid) {
          this.showError  = false;
          
          // 无尽模式增加计数 
          if (this.mode  === 'endless') {
            this.answerCount++; 
          }
          
          this.gameSuccess(); 
        } else {
          this.usedVerses  = this.usedVerses.filter(v  => v !== this.userInput); 
          this.showError  = true;
          if (this.errorTimeout)  clearTimeout(this.errorTimeout); 
          this.errorTimeout  = setTimeout(() => {
            this.showError  = false;
          }, 3000);
 
          setTimeout(() => {
            this.addSystemMessage(` 请输入正确的完整诗句，当前关键词是"${this.currentKeyword}"`); 
          }, 800);
        }
      } catch (error) {
        this.usedVerses  = this.usedVerses.filter(v  => v !== this.userInput); 
        console.error(' 验证诗句失败:', error);
        this.showError  = true;
        this.addSystemMessage(" 系统出错，请稍后再试");
      } finally {
        this.userInput  = '';
      }
    },
 
    async validateVerse(verse, keyword) {
      return new Promise((resolve) => {
        setTimeout(() => {
          const cleanedInput = verse.trim().replace(/[ ，。！？、；：'"“”‘’「」【】（）〔〕\s]/g, '');
 
          const match = this.apiVerses.some(dbVerse  => {
            const cleanedDbVerse = dbVerse.replace(/[ ，。！？、；：'"“”‘’「」【】（）〔〕\s]/g, '');
            return cleanedDbVerse === cleanedInput && dbVerse.includes(keyword); 
          });
 
          resolve(match);
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
      return `${now.getHours()}:${now.getMinutes().toString().padStart(2,  '0')}`;
    },
 
    scrollToBottom() {
      this.$nextTick(() => {
        const container = this.$refs.chatMessages;
        if (container) {
          container.scrollTop = container.scrollHeight;
        }
      });
    },
 
    async fetchRandomKeyword() {
      return new Promise((resolve) => {
        setTimeout(() => {
          const randomKeyword = this.keywords[Math.floor(Math.random()  * this.keywords.length)]; 
          resolve({ keyword: randomKeyword });
        }, 100);
      });
    },
 
    async fetchRelatedVerse(keyword) {
      const url = `http://localhost:8081/poem/keyword/${encodeURIComponent(keyword)}`;
      try {
        const response = await axios.get(url);
        // 提取返回的Poem对象数组中的text字段组成字符串数组
        const apiVerses = [...new Set(response.data.map(poem => poem.text))];
        return { verses: apiVerses }; // 只返回数据，不处理游戏逻辑
      } catch (error) {
        console.error('获取诗句失败:', error);
        return { verses: [] };
      }
    }
  }
};
</script>
 
<style scoped>
.feihua-layout {
  display: flex;
  height: 100vh;
}
 
.sidebar {
  width: 220px;
  background: #eae1d4;
  padding: 1rem;
  border-right: 1px solid #d6cab4;
  overflow-y: auto;
}
 
.sidebar h2 {
  margin-top: 0;
  font-size: 1.2rem;
  color: #5a4634;
}
 
.history-list {
  list-style: none;
  padding: 0;
  margin: 1rem 0;
}
 
.history-list li {
  cursor: pointer;
  padding: 0.5rem 0.75rem;
  margin-bottom: 0.5rem;
  background: #f8f4ed;
  border-radius: 8px;
  transition: background 0.3s;
}
 
.history-list li:hover {
  background: #e6ddd0;
}
 
.new-game-btn {
  margin-top: 1rem;
  padding: 10px 20px;
  width: 100%;
  background: linear-gradient(to right, #8c7853, #6e5773);
  color: white;
  border: none;
  border-radius: 30px;
  cursor: pointer;
  font-size: 1rem;
}
.feihua-container {
  width: 100%;
  margin: 0;
  padding: 0;
  background: #f5efe6;
  height: 100vh;
  font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif;
  display: flex;
  flex-direction: column;
}
 
.chat-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
  min-height: 0;
  width: 100%;
}
 
.feihua-header {
  text-align: center;
  position: relative;
  padding: 1.2rem;
  background: linear-gradient(to right, #8c7853, #6e5773);
  color: white;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}
.return-btn {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  padding: 6px 12px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 15px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.3s;
}
 
.return-btn:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-50%) scale(1.05);
}
.feihua-header h1 {
  margin: 0;
  font-size: 2rem;
  font-weight: normal;
}
 
.feihua-header .subtitle {
  margin: 0.5rem 0 0;
  font-size: 0.9rem;
  font-style: italic;
  opacity: 0.9;
}
 
.start-container {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-direction: column;
}
 
.mode-selector, .difficulty-selector {
  background: white;
  padding: 1.5rem 2rem;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  width: 90%;
  max-width: 500px;
  margin-bottom: 1.5rem;
}
 
.mode-options, .difficulty-options {
  display: flex;
  justify-content: center;
  gap: 1rem;
  margin: 1.5rem 0;
  flex-wrap: wrap;
}
 
.start-btn {
  width: 90%;
  max-width: 500px;
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
  justify-content: center;
  gap: 8px;
}
 
.start-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  background: #d6cab4;
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
 
.info-display {
  padding: 0.8rem;
  background: #f8f4ed;
  font-size: 0.95rem;
  color: #5a4634;
}
 
.info-display > div {
  margin-bottom: 0.3rem;
}
 
.keyword-mark {
  font-weight: bold;
  color: #8c7853;
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
 
.message-bubble.user   {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}
 
.message-bubble.system   {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
 
.bubble-content {
  padding: 12px 18px;
  border-radius: 18px;
  background: linear-gradient(to right, #8c7853, #6e5773); 
  color: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.08);
  font-family: '楷体', cursive;
 
  white-space: normal;     
  word-break: normal;       
  word-wrap: break-word;  
  max-width: 280px;         
  line-height: 1.6;        
  
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
 
.mode-mark, .difficulty-mark {
  font-weight: bold;
  color: #8c7853;
}
 
.countdown {
  font-size: 0.9rem;
  color: #5a4634;
}
 
.countdown.warning  {
  color: #c0392b;
  font-weight: bold;
  animation: pulse 1s infinite;
}
 
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
 
.mode-selector h3, .difficulty-selector h3 {
  color: #5a4634;
  margin-bottom: 1rem;
  text-align: center;
}
 
.mode-options button, .difficulty-options button {
  padding: 8px 16px;
  border: 1px solid #d6cab4;
  border-radius: 20px;
  background: #f8f4ed;
  color: #5a4634;
  cursor: pointer;
  transition: all 0.3s;
}
 
.mode-options button.active,  .difficulty-options button.active  {
  background: linear-gradient(to right, #8c7853, #6e5773);
  color: white;
  border-color: transparent;
}
 
.mode-options button:hover, .difficulty-options button:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 6px rgba(0,0,0,0.1);
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
  
  .info-display {
    font-size: 0.85rem;
    padding: 0.6rem;
  }
}
</style>
