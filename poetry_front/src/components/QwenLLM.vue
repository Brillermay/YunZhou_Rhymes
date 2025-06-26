<template>
  <div class="ai-main-layout">
    <!-- 标题区域 -->
    <div class="ai-title-header">
      <h1>墨卿AI· 智能诗友</h1>
      <p>与AI畅聊诗词、古人、创作灵感</p>
    </div>
    <!-- 内容区域 -->
    <div class="ai-content-layout">
      <!-- 左侧功能选择栏 -->
      <aside class="ai-sidebar">
        <div class="sidebar-title">AI功能区</div>
        <ul class="sidebar-list">
          <li class="sidebar-item active"><i class="iconfont">🤖</i> AI对话</li>
          <li class="sidebar-item"> <i class="iconfont">👴</i> 模拟古人对话</li>
          <li class="sidebar-item"> <i class="iconfont">📜</i> 诗词创作评分</li>
          <li class="sidebar-item"> <i class="iconfont">💬</i> 诗词交流</li>
          <li class="sidebar-item">更多功能...</li>
        </ul>
      </aside>
      <!-- 右侧AI对话区 -->
      <section class="ai-chat-area">
        <div class="chat-history" ref="chatHistory">
          <div
            v-for="(msg, idx) in chatList"
            :key="idx"
            :class="['chat-msg', msg.role === 'user' ? 'user-msg' : 'ai-msg']"
          >
            <div class="msg-avatar">
              <span v-if="msg.role === 'user'">🧑</span>
              <span v-else>🤖</span>
            </div>
            <div class="msg-content" v-html="msg.html"></div>
          </div>
          <div v-if="isStreaming" class="chat-msg ai-msg">
            <div class="msg-avatar">🤖</div>
            <div class="msg-content" v-html="streamingOutput"></div>
          </div>
        </div>
        <div class="chat-input-row">
          <textarea
            v-model="input"
            placeholder="请输入您的问题或诗意畅想..."
            class="chat-input"
            @keydown.enter.exact.prevent="startChat"
            rows="1"
          />
          <button @click="startChat" class="send-btn">发送</button>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted } from 'vue'

const input = ref('')
const chatList = ref([]) // 多轮对话历史
const isStreaming = ref(false)
const streamingOutputRaw = ref('')

// 格式化输出
function formatOutput(raw) {
  // 1. 清除 “资料：”“数据：”“data:” 等
  raw = raw.replace(/(资料|数据|data)\s*[:：]/gi, '')
  // 2. 清除 markdown/多余符号/星号
  raw = raw
    .replace(/^#+\s*/gm, '')
    .replace(/[*_]{1,2}([^*_]+)[*_]{1,2}/g, '$1')
    .replace(/([，。！？；])\s*\*\*/g, '$1')
    .replace(/\*+/g, '')
    .replace(/．/g, '。')
  // 3. 分段处理
  const lines = raw.split(/\n{2,}/g)
  const htmlParts = []
  for (let line of lines) {
    line = line.trim()
    if (!line) continue
    line = line.replace(/\n/g, '<br>')
    htmlParts.push(`<p>${line}</p>`)
  }
  return htmlParts.join('\n')
}

const streamingOutput = computed(() => formatOutput(streamingOutputRaw.value))

const chatHistory = ref(null)

async function startChat() {
  if (!input.value.trim() || isStreaming.value) return
  // 添加用户消息
  chatList.value.push({
    role: 'user',
    html: formatOutput(input.value)
  })
  isStreaming.value = true
  streamingOutputRaw.value = ''
  await nextTick()
  scrollToBottom()
  // 发送请求
  const response = await fetch('http://localhost:8081/ai/easy/chat/stream', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ question: input.value }),
  })
  input.value = ''
  if (!response.body) {
    streamingOutputRaw.value = '无法建立流式连接'
    isStreaming.value = false
    return
  }
  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let done = false
  let buffer = ''
  while (!done) {
    const { value, done: doneReading } = await reader.read()
    done = doneReading
    if (value) {
      buffer += decoder.decode(value, { stream: true })
      let parts = buffer.split('\n\n')
      buffer = parts.pop()
      for (const part of parts) {
        const line = part.trim()
        if (line.startsWith('data:')) {
          const data = line.replace(/^data:\s*/, '')
          if (data === '[END]') {
            done = true
            break
          }
          streamingOutputRaw.value += data
          await nextTick()
          scrollToBottom()
        }
      }
    }
  }
  // 推入AI消息
  chatList.value.push({
    role: 'ai',
    html: streamingOutput.value
  })
  streamingOutputRaw.value = ''
  isStreaming.value = false
  await nextTick()
  scrollToBottom()
}

function scrollToBottom() {
  if (chatHistory.value) {
    chatHistory.value.scrollTop = chatHistory.value.scrollHeight
  }
}

// 初始自我介绍
onMounted(() => {
  chatList.value.push({
    role: 'ai',
    html: formatOutput(
      '您好，我是墨卿AI，你的智能诗友，可以与您交流诗词、点评创作、模拟古人对话等。请问有什么想聊的？'
    )
  })
})
</script>

<style scoped>
.ai-main-layout {
  display: flex;
  width: 100%;
  height: 100%;
  background: #f5efe6;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(140,120,83,0.07);
  overflow: hidden;
  flex-direction: column; /* 让标题在最上面 */
}

/* 顶部大标题样式，仿照 PoetryTest.vue */
.ai-title-header {
  text-align: center;
  padding: 0.5rem;
  margin: 10px 10px 10px 10px;
  background: linear-gradient(to right, #8c7853, #6e5773);
  color: white;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  position: relative;
  z-index: 2;
  
}
.ai-title-header h1 {
  color: #e5e5e5;
  font-family: eva, 'STKaiti', 'KaiTi', serif;
  font-size: 40px;
  text-shadow: 3px 3px 10px rgba(0, 0, 0, 0.5);
  animation: float 3s ease-in-out infinite;
  margin-bottom: 0.5rem;
}
.ai-title-header p {
  animation: float 3s ease-in-out infinite;
  font-size: 18px;
  color: #f3e9d7;
  margin-bottom: 0;
}
@keyframes float {
  0% { transform: translateY(0);}
  50% { transform: translateY(-4px);}
  100% { transform: translateY(0);}
}

/* 侧边栏和聊天区整体布局 */
.ai-content-layout {
  display: flex;
  width: 100%;
  flex: 1 1 auto;
  min-height: 60vh;
}

/* 侧边栏 */
.ai-sidebar {
  flex: 0 0 20%;
  background: #fff;
  border-right: 1.5px solid #e5d8c3;
  padding: 2.5rem 1.2rem 1.2rem 1.2rem;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  min-width: 180px;
  max-width: 260px;
  box-shadow: 2px 0 8px rgba(140,120,83,0.04);
}
.sidebar-title {
  font-size: 1.25rem;
  font-weight: bold;
  color: #8c7853;
  margin-bottom: 1.5rem;
  letter-spacing: 2px;
}
.sidebar-list {
  list-style: none;
  padding: 0;
  width: 100%;
}
.sidebar-item {
  padding: 0.7rem 1rem;
  margin-bottom: 0.5rem;
  border-radius: 8px;
  color: #6e5773;
  font-size: 1.08rem;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}
.sidebar-item.active,
.sidebar-item:hover {
  background: linear-gradient(to right, #f3f0eb, #e7e0d0);
  color: #8c7853;
  font-weight: bold;
}

/* 聊天区 */
.ai-chat-area {
  flex: 1 1 80%;
  display: flex;
  flex-direction: column;
  background: #f9f6f1;
  padding: 0;
  min-width: 0;
  border-radius: 0 0 12px 0;
  box-shadow: 0 2px 8px rgba(140,120,83,0.04);
}

/* 聊天头部（隐藏原有标题） */
.chat-header {
  display: none;
}

/* 聊天历史 */
.chat-history {
  flex: 1 1 auto;
  overflow-y: auto;
  padding: 2rem 2.5rem 1rem 2.5rem;
  background: transparent;
  max-height: 72vh;
  min-height: 320px;
  scroll-behavior: smooth;
  transition: background 0.3s;
}
.chat-msg {
  display: flex;
  align-items: flex-start;
  margin-bottom: 1.2rem;
  gap: 1rem;
}
.user-msg {
  flex-direction: row-reverse;
}
.ai-msg {
  flex-direction: row;
}
.msg-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  background: #e7e0d0;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.7rem;
  box-shadow: 0 2px 8px rgba(140,120,83,0.08);
}
.msg-content {
  max-width: 70%;
  background: #fff;
  border-radius: 12px;
  padding: 1rem 1.2rem;
  font-size: 1.08rem;
  color: #5a4634;
  font-family: 'STKaiti', 'KaiTi', serif;
  line-height: 1.8;
  box-shadow: 0 2px 8px rgba(140,120,83,0.07);
  word-break: break-word;
}
.user-msg .msg-content {
  background: linear-gradient(to right, #f3f0eb, #e7e0d0);
  color: #6e5773;
  align-self: flex-end;
  padding: 0rem 1.2rem;
}
.ai-msg .msg-content {
  background: #fff;
  color: #8c7853;
}
.ai-msg .msg-avatar {
  animation: ai-bounce 1.8s infinite;
}
@keyframes ai-bounce {
  0%, 100% { transform: translateY(0);}
  20% { transform: translateY(-6px);}
  40% { transform: translateY(0);}
}
.chat-input-row {
  display: flex;
  align-items: flex-end;
  padding: 1rem  2.5rem;
  background: #f9f6f1;
  border-bottom-right-radius: 12px;
  gap: 1rem;
}
.chat-input {
  flex: 1 1 auto;
  min-height: 48px;
  max-height: 120px;
  border-radius: 12px;
  border: 1.5px solid #e5d8c3;
  padding: 0.5rem 1rem; /* 修改这里，增加上下内边距 */
  font-size: 1.08rem;
  font-family: 'STKaiti', 'KaiTi', serif;
  background: #fff;
  color: #5a4634;
  resize: vertical;
  outline: none;
  transition: border 0.2s;
  box-sizing: border-box;
  line-height: 1.8; /* 新增或调整 */
}

/* 让 placeholder 垂直居中（兼容性好） */
.chat-input::placeholder {
  line-height: 1.8; /* 保持与 .chat-input 一致 */
  color: #b8a888;
  opacity: 1;
}
.send-btn {
  padding: 0.8rem 2.2rem;
  border-radius: 20px;
  background: linear-gradient(to right, #8c7853, #6e5773);
  color: white;
  border: none;
  font-size: 1.1em;
  font-weight: bold;
  cursor: pointer;
  transition: background 0.3s;
  box-shadow: 0 2px 8px rgba(140,120,83,0.07);
}
.send-btn:hover {
  background: linear-gradient(to right, #a3916a, #7c6488);
}
@media (max-width: 900px) {
  .ai-main-layout {
    flex-direction: column;
  }
  .ai-content-layout {
    flex-direction: column;
  }
  .ai-sidebar {
    flex: none;
    width: 100%;
    max-width: none;
    border-right: none;
    border-bottom: 1.5px solid #e5d8c3;
    padding: 1.2rem;
    min-width: 0;
  }
  .ai-chat-area {
    padding: 0;
  }
  .chat-header, .chat-history, .chat-input-row {
    padding-left: 1rem;
    padding-right: 1rem;
  }
}
</style>