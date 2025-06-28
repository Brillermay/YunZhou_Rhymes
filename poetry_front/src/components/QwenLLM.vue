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
          <li
            class="sidebar-item"
            :class="{active: chatMode==='normal'}"
            @click="switchMode('normal')"
          >
            <div class="sidebar-icon">🤖</div>
            <div class="sidebar-texts">
              <div class="sidebar-main">AI对话</div>
              <div class="sidebar-desc">与AI畅聊诗词、答疑解惑</div>
            </div>
          </li>
          <li
            class="sidebar-item"
            :class="{active: chatMode==='ancient'}"
            @click="switchMode('ancient')"
          >
            <div class="sidebar-icon">🪶</div>
            <div class="sidebar-texts">
              <div class="sidebar-main">模拟古人对话</div>
              <div class="sidebar-desc">与古人虚拟对话，感受历史风采</div>
            </div>
          </li>
          <li class="sidebar-item">
            <div class="sidebar-icon">📜</div>
            <div class="sidebar-texts">
              <div class="sidebar-main">诗词创作评分</div>
              <div class="sidebar-desc">AI点评你的诗词创作</div>
            </div>
          </li>
          <li class="sidebar-item">
            <div class="sidebar-icon">💬</div>
            <div class="sidebar-texts">
              <div class="sidebar-main">诗词交流</div>
              <div class="sidebar-desc">与诗友畅谈诗意人生</div>
            </div>
          </li>
          <li
            class="sidebar-item"
            :class="{active: chatMode==='soul'}"
            @click="switchMode('soul')"
          >
            <div class="sidebar-icon">🧙</div>
            <div class="sidebar-texts">
              <div class="sidebar-main">前世诗魂配对</div>
              <div class="sidebar-desc">测测你是哪位诗魂</div>
            </div>
          </li>
          <li class="sidebar-item">
            <div class="sidebar-icon">✨</div>
            <div class="sidebar-texts">
              <div class="sidebar-main">更多功能...</div>
              <div class="sidebar-desc">敬请期待</div>
            </div>
          </li>
        </ul>
      </aside>
      <!-- 右侧AI对话区 -->
      <section class="ai-chat-area">
        <div v-if="showRoleSelect" class="role-select-modal">
          <div class="role-select-content">
            <h3>千年烟雨，一纸诗心。你步入词境之馆，几位古人正在等候与君执言共赏，静待你的拣选……</h3>
            <div class="poet-card-list">
              <div
                v-for="poet in ancientPoets"
                :key="poet.name"
                class="poet-card"
                @click="chooseRole(poet.name)"
              >
                <img :src="poet.avatar" :alt="poet.name" class="poet-card-avatar" />
                <div class="poet-card-info">
                  <div class="poet-card-name">{{ poet.name }}</div>
                  <div class="poet-card-intro">{{ poet.intro }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="chat-history" ref="chatHistory">
          <div
            v-for="(msg, idx) in chatList"
            :key="idx"
            :class="['chat-msg', msg.role === 'user' ? 'user-msg' : 'ai-msg']"
          >
            <div
              class="msg-avatar"
              :class="{'avatar-hoverable': msg.role === 'ai' && chatMode !== 'ancient'}"
              :title="msg.role === 'ai' && chatMode !== 'ancient' ? '点我切换AI形象' : ''"
              @click="msg.role === 'ai' && chatMode !== 'ancient' && toggleAiAvatar()"
            >
              <!-- AI头像逻辑 -->
              <template v-if="msg.role === 'ai'">
                <img
                  v-if="chatMode === 'ancient' && selectedRole && poetAvatarMap[selectedRole]"
                  :src="poetAvatarMap[selectedRole]"
                  alt="AI头像"
                  class="poet-avatar"
                />
                <img
                  v-else
                  :src="aiAvatarMap[aiAvatarType]"
                  alt="AI默认头像"
                  class="poet-avatar"
                />
                <span
                  v-if="chatMode !== 'ancient'"
                  class="avatar-tip"
                >点我切换形象哦</span>
              </template>
              <!-- 用户头像逻辑 -->
              <template v-else>
                <span style="font-size: 2rem;">🧑</span>
              </template>
            </div>
            <div class="msg-content" v-html="msg.html"></div>
          </div>
          <div v-if="isStreaming" class="chat-msg ai-msg">
            <div
              class="msg-avatar"
              :class="{'avatar-hoverable': chatMode !== 'ancient'}"
              :title="chatMode !== 'ancient' ? '点我切换AI形象' : ''"
              @click="chatMode !== 'ancient' && toggleAiAvatar()"
            >
              <img
                v-if="chatMode === 'ancient' && selectedRole && poetAvatarMap[selectedRole]"
                :src="poetAvatarMap[selectedRole]"
                alt="AI头像"
                class="poet-avatar"
              />
              <img
                v-else
                :src="aiAvatarMap[aiAvatarType]"
                alt="AI默认头像"
                class="poet-avatar"
              />
              <span
                v-if="chatMode !== 'ancient'"
                class="avatar-tip"
              >点我切换形象哦</span>
            </div>
            <div class="msg-content typing-cursor" v-html="streamingOutput"></div>
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

// 1. 引入古人头像
import libaiImg from '@/assets/poets/libai.png'
import lindaiyuImg from '@/assets/poets/lindaiyu.png'
import sushiImg from '@/assets/poets/sushi.png'
import xinqijiImg from '@/assets/poets/xinqiji.png'
import taoyuanmingImg from '@/assets/poets/taoyuanming.png'
import aiboyImg from '@/assets/poets/aiboy.jpg'
import aigirlImg from '@/assets/poets/aigirl.jpg'

// 2. 头像映射
const poetAvatarMap = {
  '李白': libaiImg,
  '林黛玉': lindaiyuImg,
  '苏轼': sushiImg,
  '辛弃疾': xinqijiImg,
  '陶渊明': taoyuanmingImg
}

const input = ref('')
const chatList = ref([]) // 多轮对话历史
const isStreaming = ref(false)
const streamingOutputRaw = ref('')
const chatMode = ref('normal') // 'normal' | 'ancient' | 'soul'
const selectedRole = ref('')
const showRoleSelect = ref(false)
const ancientRoles = ['李白', '林黛玉', '苏轼', '辛弃疾', '陶渊明']

const ancientPoets = [
  {
    name: '李白',
    avatar: libaiImg,
    intro: '诗仙，豪放不羁，诗酒趁年华，纵情山水。'
  },
  {
    name: '林黛玉',
    avatar: lindaiyuImg,
    intro: '寄情诗词，感怀人生，才情与柔情并存。'
  },
  {
    name: '苏轼',
    avatar: sushiImg,
    intro: '东坡居士，旷达乐观，诗文书画皆精。'
  },
  {
    name: '辛弃疾',
    avatar: xinqijiImg,
    intro: '稼轩词人，豪情壮志，词笔纵横。'
  },
  {
    name: '陶渊明',
    avatar: taoyuanmingImg,
    intro: '五柳先生，归隐田园，淡泊明志，爱菊饮酒。'
  }
]

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
  chatList.value.push({
    role: 'user',
    html: formatOutput(input.value),
    content: input.value
  })
  isStreaming.value = true
  streamingOutputRaw.value = ''
  await nextTick()
  scrollToBottom()

  // 构造历史
  const history = chatList.value
    .map(msg => ({
      role: msg.role === 'ai' ? 'ai' : 'user',
      content: msg.content || msg.html.replace(/<[^>]+>/g, '')
    }))

  try {
    let url = 'http://localhost:8081/ai/easy/chat/stream'
    let body = {
      question: input.value,
      history: history.slice(0, -1) // 默认
    }
    if (chatMode.value === 'ancient') {
      url = 'http://localhost:8081/ai/easy/chat/stream/role'
      body.role = selectedRole.value
    }
    if (chatMode.value === 'soul') {
      url = 'http://localhost:8081/ai/easy/soul-matcher/stream'
      body = {
        history: history // 只传history，不传question
      }
    }
    const response = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })
    input.value = ''
    if (!response.body) {
      streamingOutputRaw.value = '无法建立流式连接'
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
    chatList.value.push({
      role: 'ai',
      html: streamingOutput.value,
      content: streamingOutput.value.replace(/<[^>]+>/g, ''),
      avatar: selectedRole.value ? poetAvatarMap[selectedRole.value] : undefined
    })
    streamingOutputRaw.value = ''
    await nextTick()
    scrollToBottom()
  } catch (e) {
    streamingOutputRaw.value = 'AI接口异常，请稍后重试'
    chatList.value.push({
      role: 'ai',
      html: formatOutput(streamingOutputRaw.value),
      content: streamingOutputRaw.value,
      avatar: chatMode.value === 'ancient' ? poetAvatarMap[selectedRole.value] : undefined
    })
    streamingOutputRaw.value = ''
    await nextTick()
    scrollToBottom()
  } finally {
    isStreaming.value = false
  }
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

function switchMode(mode) {
  if (chatMode.value === mode) return
  chatMode.value = mode
  chatList.value = []
  input.value = ''
  streamingOutputRaw.value = ''
  if (mode === 'ancient') {
    showRoleSelect.value = true
  } else if (mode === 'soul') {
    showRoleSelect.value = false
    chatList.value.push({
      role: 'ai',
      html: formatOutput(
        '欢迎来到“前世诗魂配对”！我将通过10道趣味题，帮你匹配一位与你灵魂契合的古人或诗句。准备好开始了吗？（回复“开始”即可进入测试）'
      ),
      content: '欢迎来到“前世诗魂配对”！我将通过10道趣味题，帮你匹配一位与你灵魂契合的古人或诗句。准备好开始了吗？（回复“开始”即可进入测试）'
    })
  } else {
    showRoleSelect.value = false
    chatList.value.push({
      role: 'ai',
      html: formatOutput(
        '您好，我是墨卿AI，你的智能诗友，可以与您交流诗词、点评创作、模拟古人对话等。请问有什么想聊的？'
      ),
      content: '您好，我是墨卿AI，你的智能诗友，可以与您交流诗词、点评创作、模拟古人对话等。请问有什么想聊的？'
    })
  }
  nextTick(() => {
    scrollToBottom()
  })
}

const roleIntroMap = {
  '李白': '仆乃青莲居士李白，诗酒趁年华，愿与君共赏风月，畅谈人生。',
  '林黛玉': '小女子林黛玉，寄情诗词，感怀人生，愿与君共诉心曲。',
  '苏轼': '东坡居士苏轼在此，谈笑风生，诗酒自得，愿与君共论世事。',
  '辛弃疾': '稼轩辛弃疾，胸怀壮志，词笔纵横，愿与君共抒豪情。',
  '陶渊明': '五柳先生陶渊明，爱菊饮酒，归隐田园，愿与君共话清欢。'
}

function chooseRole(role) {
  selectedRole.value = role
  showRoleSelect.value = false
  // 推送该古人自我介绍，带头像
  chatList.value.push({
    role: 'ai',
    html: formatOutput(roleIntroMap[role]),
    content: roleIntroMap[role],
    avatar: poetAvatarMap[role]
  })
  nextTick(() => {
    scrollToBottom()
  })
}

const aiAvatarType = ref('girl') // 'girl' | 'boy'
const aiAvatarMap = {
  girl: aigirlImg,
  boy: aiboyImg
}

function toggleAiAvatar() {
  aiAvatarType.value = aiAvatarType.value === 'girl' ? 'boy' : 'girl'
}
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
  flex-direction: column;
  animation: fadeInPage 1.2s ease both;
}
@keyframes fadeInPage {
  0% { opacity: 0; transform: scale(0.98); }
  100% { opacity: 1; transform: scale(1); }
}

.ai-title-header {
  text-align: center;
  padding: 0.5rem;
  margin: 10px;
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

.ai-content-layout {
  display: flex;
  width: 100%;
  flex: 1 1 auto;
  min-height: 60vh;
}

.ai-sidebar {
  flex: 0 0 20%;
  background: #fff;
  border-right: 1.5px solid #e5d8c3;
  padding: 2.5rem 1.2rem 1.2rem;
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
  transition: all 0.25s ease;
  display: flex;
  align-items: center;
}
.sidebar-item:hover,
.sidebar-item.active {
  background: linear-gradient(to right, #f3f0eb, #e7e0d0);
  color: #8c7853;
  font-weight: bold;
  transform: translateX(4px);
  box-shadow: 2px 2px 6px rgba(140,120,83,0.1);
}
.sidebar-icon {
  font-size: 1.5rem;
  margin-right: 0.8rem;
}
.sidebar-texts {
  display: flex;
  flex-direction: column;
}
.sidebar-main {
  font-weight: bold;
}
.sidebar-desc {
  font-size: 0.9rem;
  color: #8c7853;
}

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

.role-select-modal {
  position: absolute;
  left: 0; top: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}
.role-select-content {
  background: #fff;
  border-radius: 12px;
  padding: 2rem 2.5rem;
  box-shadow: 0 4px 16px rgba(140,120,83,0.13);
  text-align: center;
}
.role-select-content h3 {
  margin-bottom: 1.2rem;
  color: #8c7853;
}
.role-select-content ul {
  list-style: none;
  padding: 0;
  display: flex;
  gap: 1.5rem;
  justify-content: center;
}
.role-select-content button {
  padding: 0.7rem 1.5rem;
  border-radius: 8px;
  border: none;
  background: linear-gradient(to right, #f3f0eb, #e7e0d0);
  color: #6e5773;
  font-size: 1.1rem;
  cursor: pointer;
  transition: background 0.2s;
}
.role-select-content button:hover {
  background: linear-gradient(to right, #8c7853, #6e5773);
  color: #fff;
}

.chat-header {
  display: none;
}

.chat-history {
  flex: 1 1 auto;
  overflow-y: auto;
  padding: 2rem 2.5rem 1rem;
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
  opacity: 1;
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
.ai-msg .msg-avatar {
  animation: ai-pulse 2.5s ease-in-out infinite;
}
@keyframes ai-pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 0 10px rgba(140,120,83,0.2);
  }
  50% {
    transform: scale(1.08);
    box-shadow: 0 0 20px rgba(140,120,83,0.4);
  }
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

@keyframes blinkCursor {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.1; }
}

.chat-input-row {
  display: flex;
  align-items: flex-end;
  padding: 1rem 2.5rem;
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
  padding: 0.5rem 1rem;
  font-size: 1.08rem;
  font-family: 'STKaiti', 'KaiTi', serif;
  background: #fff;
  color: #5a4634;
  resize: vertical;
  outline: none;
  box-sizing: border-box;
  line-height: 1.8;
  transition: border 0.3s, box-shadow 0.3s;
 
}
.chat-input:focus {
  border-color: #8c7853;
  box-shadow: 0 0 8px rgba(140, 120, 83, 0.3);
}
.chat-input::placeholder {
  line-height: 1.8;
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
  transition: background 0.3s, transform 0.1s;
  box-shadow: 0 2px 8px rgba(140,120,83,0.07);
}
.send-btn:hover {
  background: linear-gradient(to right, #a3916a, #7c6488);
}
.send-btn:active {
  transform: scale(0.97);
  box-shadow: 0 2px 4px rgba(140, 120, 83, 0.3);
}

.poet-avatar {
  width: 38px;
  height: 38px;
  border-radius: 50%;
  object-fit: cover;
  box-shadow: 0 2px 8px rgba(140,120,83,0.08);
}

.poet-card-list {
  display: flex;
  gap: 2rem;
  justify-content: center;
  flex-wrap: wrap;
  margin-top: 1.5rem;
}

.poet-card {
  background: linear-gradient(135deg, #f9f6f1 70%, #e7e0d0 100%);
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(140,120,83,0.10);
  padding: 1.2rem 1.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 170px;
  cursor: pointer;
  transition: transform 0.18s, box-shadow 0.18s;
  border: 2px solid transparent;
}

.poet-card:hover {
  transform: translateY(-6px) scale(1.04);
  box-shadow: 0 6px 24px rgba(140,120,83,0.18);
  border-color: #8c7853;
  background: linear-gradient(135deg, #f3f0eb 60%, #e7e0d0 100%);
}

.poet-card-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 1rem;
  box-shadow: 0 2px 8px rgba(140,120,83,0.10);
}

.poet-card-info {
  text-align: center;
}

.poet-card-name {
  font-size: 1.15rem;
  font-weight: bold;
  color: #6e5773;
  margin-bottom: 0.5rem;
}

.poet-card-intro {
  font-size: 0.98rem;
  color: #8c7853;
}

.avatar-hoverable {
  position: relative;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.2s;
}
.avatar-hoverable:hover {
  box-shadow: 0 0 16px #a3916a88, 0 2px 8px rgba(140,120,83,0.18);
  transform: scale(1.12) rotate(-6deg);
}
.avatar-tip {
  position: absolute;
  left: 50%;
  top: 105%;
  transform: translateX(-50%);
  font-size: 12px;
  color: #a3916a;
  background: #fffbe9;
  border-radius: 8px;
  padding: 2px 8px;
  box-shadow: 0 2px 8px rgba(140,120,83,0.07);
  white-space: nowrap;
  opacity: 0;
  pointer-events: none;
  transition: opacity 0.2s;
  z-index: 2;
}
.avatar-hoverable:hover .avatar-tip {
  opacity: 1;
  font-size: 10px;
}
.avatar-hoverable:hover .poet-avatar {
  animation: avatar-shake 0.4s;
}
@keyframes avatar-shake {
  0% { transform: rotate(0deg);}
  20% { transform: rotate(-8deg);}
  40% { transform: rotate(8deg);}
  60% { transform: rotate(-6deg);}
  80% { transform: rotate(6deg);}
  100% { transform: rotate(0deg);}
}
.avatar-hoverable:active .poet-avatar {
  animation: avatar-bounce 0.25s;
}
@keyframes avatar-bounce {
  0% { transform: scale(1);}
  50% { transform: scale(1.18);}
  100% { transform: scale(1);}
}
@keyframes avatar-shake {
  0% { transform: rotate(0deg);}
  20% { transform: rotate(-8deg);}
  40% { transform: rotate(8deg);}
  60% { transform: rotate(-6deg);}
  80% { transform: rotate(6deg);}
  100% { transform: rotate(0deg);}
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