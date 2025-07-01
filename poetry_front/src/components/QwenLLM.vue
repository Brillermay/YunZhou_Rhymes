<template>
  <div class="ai-main-layout">
    <!-- 聊天背景和毛玻璃，覆盖整个聊天主区域（包括sidebar和chat-area） -->
    <transition name="chat-bg-fade" mode="out-in">
      <div
        class="chat-area-bg global-chat-bg"
        v-if="chatBg"
        :key="chatBg"
        :style="{ backgroundImage: chatBg }"
      ></div>
    </transition>
    <div class="chat-area-bg-mask global-chat-bg-mask" v-if="chatBg"></div>
    <!-- 标题区域 -->
    <div class="ai-title-header">
      <h1>墨卿AI· 智能诗友</h1>
      <p>与AI畅聊诗词、古人、创作灵感</p>
    </div>
    <div class="ai-content-layout">
      <!-- 左侧功能选择栏 -->
      <aside class="ai-sidebar">
        <div class="sidebar-title">AI功能区</div>
        <ul class="sidebar-list">
          <li class="sidebar-item" :class="{ active: chatMode === 'normal' }" @click="switchMode('normal')">
            <div class="sidebar-icon">🔮</div>
            <div class="sidebar-texts">
              <div class="sidebar-main">AI智能诗友</div>
              <div class="sidebar-desc">与AI畅聊诗词、答疑解惑</div>
            </div>
          </li>
          <li class="sidebar-item" :class="{ active: chatMode === 'ancient' }" @click="switchMode('ancient')">
            <div class="sidebar-icon">🪶</div>
            <div class="sidebar-texts">
              <div class="sidebar-main">模拟古人对话</div>
              <div class="sidebar-desc">与古人虚拟对话，感受历史风采</div>
            </div>
          </li>
          <li class="sidebar-item" :class="{ active: chatMode === 'rating' }" @click="switchMode('rating')">
            <div class="sidebar-icon">📜</div>
            <div class="sidebar-texts">
              <div class="sidebar-main">诗词创作评分</div>
              <div class="sidebar-desc">AI点评你的诗词创作</div>
            </div>
          </li>
          <li
            class="sidebar-item"
            :class="{ active: chatMode === 'timemachine' }"
            @click="switchMode('timemachine')"
          >
            <div class="sidebar-icon">🕰️</div>
            <div class="sidebar-texts">
              <div class="sidebar-main">诗词时光机</div>
              <div class="sidebar-desc">“角色扮演”+“灵魂转生”</div>
            </div>
          </li>
          <li class="sidebar-item" :class="{ active: chatMode === 'soul' }" @click="switchMode('soul')">
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
        <!-- 聊天内容 -->
        <div class="chat-history" ref="chatHistory">
          <div v-for="(msg, idx) in chatList" :key="idx"
            :class="['chat-msg', msg.role === 'user' ? 'user-msg' : 'ai-msg']">
            <div class="msg-avatar" :class="{ 'avatar-hoverable': msg.role === 'ai' && chatMode !== 'ancient' }"
              :title="msg.role === 'ai' && chatMode !== 'ancient' ? '点我切换AI形象' : ''"
              @click="msg.role === 'ai' && chatMode !== 'ancient' && toggleAiAvatar()">
              <!-- AI头像逻辑 -->
              <template v-if="msg.role === 'ai'">
                <img v-if="chatMode === 'ancient' && selectedRole && poetAvatarMap[selectedRole]"
                  :src="poetAvatarMap[selectedRole]" alt="AI头像" class="poet-avatar" />
                <img v-else :src="aiAvatarMap[aiAvatarType]" alt="AI默认头像" class="poet-avatar" />
                <span v-if="chatMode !== 'ancient'" class="avatar-tip">点我切换形象哦</span>
              </template>
              <!-- 用户头像逻辑 -->
              <template v-else>
                <span style="font-size: 2rem;">🧑</span>
              </template>
            </div>
            <div class="msg-content" v-html="msg.html"></div>
          </div>
          <div v-if="isStreaming" class="chat-msg ai-msg">
            <div class="msg-avatar" :class="{ 'avatar-hoverable': chatMode !== 'ancient' }"
              :title="chatMode !== 'ancient' ? '点我切换AI形象' : ''" @click="chatMode !== 'ancient' && toggleAiAvatar()">
              <img v-if="chatMode === 'ancient' && selectedRole && poetAvatarMap[selectedRole]"
                :src="poetAvatarMap[selectedRole]" alt="AI头像" class="poet-avatar" />
              <img v-else :src="aiAvatarMap[aiAvatarType]" alt="AI默认头像" class="poet-avatar" />
              <span v-if="chatMode !== 'ancient'" class="avatar-tip">点我切换形象哦</span>
            </div>
            <div class="msg-content typing-cursor" v-html="streamingOutput"></div>
          </div>
        </div>
        <div v-if="chatMode === 'normal' && inputTips.length && chatList.length === 1" class="input-tips">
          <span class="input-tips-icon">💡</span>
          <span class="input-tips-label">你可以问我：</span>
          <span v-for="(tip, i) in inputTips" :key="i" class="input-tip" @click="useTip(tip)">“{{ tip }}”</span>
        </div>
        <!-- 新增：时光机模式下的输入提示 -->
        <div
          v-if="chatMode === 'timemachine' && chatList.length === 1"
          class="input-tips"
        >
          <span class="input-tips-icon">💡</span>
          <span class="input-tips-label">你可以试试：</span>
          <span
            v-for="(tip, i) in timeMachineTips"
            :key="i"
            class="input-tip"
            @click="useTip(tip)"
          >“{{ tip }}”</span>
        </div>
        <div class="chat-input-row">
          <textarea v-model="input" placeholder="请输入您的问题或诗意畅想..." class="chat-input"
            @keydown.enter.exact.prevent="startChat" rows="1" />
          <button @click="startChat" class="send-btn">发送</button>
        </div>
      </section>
    </div>
    <!-- 角色选择弹窗放在这里 -->
    <div v-if="showRoleSelect" class="role-select-modal">
      <div class="role-select-content">
        <div class="role-select-back" @click="backToNormal" title="返回AI对话">
          <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
            <circle cx="14" cy="14" r="14" fill="#f3f0eb" />
            <path d="M16.5 20l-5-6 5-6" stroke="#8c7853" stroke-width="2" stroke-linecap="round"
              stroke-linejoin="round" />
          </svg>
          <span>返回</span>
        </div>
        <div class="role-select-content">
          <h3>千年烟雨，一纸诗心。你步入词境之馆，几位古人正在等候与君执言共赏，静待你的拣选……</h3>
          <div class="poet-card-list">
            <div v-for="poet in ancientPoets" :key="poet.name" class="poet-card" @click="chooseRole(poet.name)">
              <img :src="poet.avatar" :alt="poet.name" class="poet-card-avatar" />
              <div class="poet-card-info">
                <div class="poet-card-name">{{ poet.name }}</div>
                <div class="poet-card-intro">{{ poet.intro }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div
      v-if="(chatMode === 'normal' || chatMode === 'soul' || chatMode === 'timemachine')"
      class="chat-bg-switcher"
    >
      <button class="bg-btn" @click="prevBg" title="上一张背景">‹</button>
      <span class="bg-index">{{ normalBgIndex + 1 }}/{{ normalBgList.length }}</span>
      <button class="bg-btn" @click="nextBg" title="下一张背景">›</button>
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

import libaiBg from '@/assets/chatbackground/libai.jpg'
import lindaiyuBg from '@/assets/chatbackground/lindaiyu.jpg'
import sushiBg from '@/assets/chatbackground/sushi.jpg'
import xinqijiBg from '@/assets/chatbackground/xinqiji.jpg'
import taoyuanmingBg from '@/assets/chatbackground/taoyuanming.jpg'

import bg1 from '@/assets/chatbackground/bg1.jpg'
import bg2 from '@/assets/chatbackground/bg2.jpg'
import bg3 from '@/assets/chatbackground/bg3.jpg'
import bg4 from '@/assets/chatbackground/bg4.jpg'
import bg5 from '@/assets/chatbackground/bg5.jpg'
import bg6 from '@/assets/chatbackground/bg6.jpg'
import bg7 from '@/assets/chatbackground/bg7.jpg'
import bg8 from '@/assets/chatbackground/bg8.jpg'
import bg9 from '@/assets/chatbackground/bg9.jpg'
import bg10 from '@/assets/chatbackground/bg10.jpg'
import bg11 from '@/assets/chatbackground/bg11.jpg'

import soulbg1 from '@/assets/chatbackground/soulbg1.jpg'
import soulbg2 from '@/assets/chatbackground/soulbg2.jpg'
import soulbg3 from '@/assets/chatbackground/soulbg3.jpg'

const normalBgList = [bg1, bg2, bg3, bg4, bg5, bg6, bg7, bg8, bg9, bg10, bg11]
const soulBgList = [soulbg1, soulbg2, soulbg3]

// normal和soul模式下的背景索引
const normalBgIndex = ref(0)
const soulBgIndex = ref(0)

const poetBgMap = {
  '李白': libaiBg,
  '林黛玉': lindaiyuBg,
  '苏轼': sushiBg,
  '辛弃疾': xinqijiBg,
  '陶渊明': taoyuanmingBg
}

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
const chatMode = ref('normal') // 'normal' | 'ancient' | 'soul' | 'rating' | 'timemachine'
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

const chatBg = computed(() => {
  if (chatMode.value === 'ancient' && selectedRole.value && poetBgMap[selectedRole.value]) {
    return `url('${poetBgMap[selectedRole.value]}')`
  }
  if (chatMode.value === 'normal' || chatMode.value === 'rating') {
    return `url('${normalBgList[normalBgIndex.value]}')`
  }
  if (chatMode.value === 'soul') {
    return `url('${soulBgList[soulBgIndex.value]}')`
  }
  return ''
})

function prevBg() {
  if (chatMode.value === 'normal') {
    normalBgIndex.value = (normalBgIndex.value + normalBgList.length - 1) % normalBgList.length
  } else if (chatMode.value === 'soul') {
    soulBgIndex.value = (soulBgIndex.value + soulBgList.length - 1) % soulBgList.length
  }
}
function nextBg() {
  if (chatMode.value === 'normal') {
    normalBgIndex.value = (normalBgIndex.value + 1) % normalBgList.length
  } else if (chatMode.value === 'soul') {
    soulBgIndex.value = (soulBgIndex.value + 1) % soulBgList.length
  }
}
// 格式化输出
function formatOutput(raw) {
  // 1. 去掉所有 data: 前缀
  raw = raw.replace(/(资料|数据|data)\s*[:：]/gi, '')

  // 2. 去除 markdown 标记
  raw = raw
    .replace(/^#+\s*/gm, '') // 去标题 #
    .replace(/[*_]{1,2}([^*_]+)[*_]{1,2}/g, '$1') // 粗体、斜体
    .replace(/\*+/g, '')
    .replace(/．/g, '。')

  // 3. 分段处理（每两行空行断段）
  const blocks = raw.split(/\n{2,}/g)
  const htmlParts = []

  let isPoemSection = false

  for (let block of blocks) {
    block = block.trim()
    if (!block) continue

    // 3.1 遇到“全诗如下” → 激活诗句处理模式
    if (/全诗如下/.test(block)) {
      htmlParts.push(`<p>${block}</p>`)
      isPoemSection = true
      continue
    }

    // 3.2 若是“选项题段”，将 A/B/C/D 拆分一行一个
    if (/[ABCD][.．、)]/.test(block)) {
      const choices = block.split(/(?=[ABCD][.．、)\s])/).map(choice => choice.trim())
      for (const choice of choices) {
        if (choice) htmlParts.push(`<p>${choice}</p>`)
      }
      continue
    }

    // 3.3 若是紧接“全诗如下”的诗段，按中文标点切分
    if (isPoemSection) {
      const sentences = block
        .split(/(?<=[。！？；])/)
        .map(s => s.trim())
        .filter(s => s.length > 0)
      for (let sentence of sentences) {
        htmlParts.push(`<p>${sentence}</p>`)
      }
      isPoemSection = false // 只处理一次
      continue
    }

    // 3.4 普通段落：按行拆分
    const lines = block.split(/\n/).map(l => `<p>${l.trim()}</p>`)
    htmlParts.push(...lines)
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
      role: msg.role === 'ai' ? 'assistant' : 'user',
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
    if (chatMode.value === 'rating') {
      url = 'http://localhost:8081/ai/easy/poetry/rating'
      let ratingHistory = history
      body = {
        history: ratingHistory
      }
    }
    // 修改：timemachine模式
    if (chatMode.value === 'timemachine') {
      url = 'http://localhost:8081/ai/easy/time-machine/stream'
      body = {
        history
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
    streamingOutputRaw.value = '网络异常，请稍后重试'
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

function backToNormal() {
  showRoleSelect.value = false
  chatMode.value = 'normal'
  chatList.value = []
  input.value = ''
  streamingOutputRaw.value = ''
  chatList.value.push({
    role: 'ai',
    html: '您好，我是墨卿AI，你的智能诗友，可以与您交流诗词、点评创作、模拟古人对话等。请问有什么想聊的？'
  })
  nextTick(() => {
    scrollToBottom()
  })
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
    soulBgIndex.value = 0
    chatList.value.push({
      role: 'ai',
      html: formatOutput(
        '欢迎来到“前世诗魂配对”！我将通过10道趣味题，帮你匹配一位与你灵魂契合的古人或诗句。准备好开始了吗？（回复“开始”即可进入测试）'
      ),
      content: '欢迎来到“前世诗魂配对”！我将通过10道趣味题，帮你匹配一位与你灵魂契合的古人或诗句。准备好开始了吗？（回复“开始”即可进入测试）'
    })
  } else if (mode === 'rating') {
    showRoleSelect.value = false
    normalBgIndex.value = 0
    chatList.value.push({
      role: 'ai',
      html: formatOutput(
        '欢迎来到诗词创作评分！你可以输入你的诗词内容，我会为你点评和打分。'
      ),
      content: '欢迎来到诗词创作评分！请直接输入你的诗词内容，我会为你点评和打分。'
    })
  } else if (mode === 'timemachine') {
    showRoleSelect.value = false
    normalBgIndex.value = 0
    chatList.value.push({
      role: 'ai',
      html: formatOutput(
        '欢迎来到“诗词时光机”！请选择你想穿越的朝代和身份，格式为“朝代-身份”，如“唐朝-科举学生”。'
      ),
      content: '欢迎来到“诗词时光机”！请选择你想穿越的朝代和身份，格式为“朝代-身份”，如“唐朝-科举学生”。'
    })
  } else {
    showRoleSelect.value = false
    normalBgIndex.value = 0
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

const inputTips = ref([
  '李白的代表作有哪些？',
  '写一首关于春天的诗',
  '辛弃疾的豪放词有哪些？',
  '如何评价苏轼的诗词？',
  '给我推荐一首古诗',
])

function useTip(tip) {
  input.value = tip
  inputTips.value = [] // 点击后消失
}

// 新增：时光机模式的提示选项
const timeMachineTips = [
  '唐朝-科举学生',
  '宋朝-征战将领',
  '魏晋-隐士诗人',
  '明朝-江南才女',
  '清朝-宫廷画师'
]
</script>

<style scoped>
.ai-main-layout {
  display: flex;
  width: 100%;
  height: 100%;
  background: #f5efe6;
  box-shadow: 0 4px 16px rgba(140, 120, 83, 0.07);
  overflow: hidden;
  flex-direction: column;
  animation: fadeInPage 1.2s ease both;
  position: relative;
}

/* 聊天背景和毛玻璃，覆盖整个聊天主区域（sidebar+chat-area） */
.global-chat-bg {
  position: absolute;
  left: 0; top: 0; right: 0; bottom: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  transition: background-image 0.5s;
  pointer-events: none;
}
.global-chat-bg-mask {
  position: absolute;
  left: 0; top: 0; right: 0; bottom: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
  pointer-events: none;
  backdrop-filter: blur(0.5px) brightness(0.9);
  background: rgba(231, 231, 231, 0);
}

/* 保证内容区在背景之上 */
.ai-title-header,
.ai-content-layout,
.role-select-modal,
.chat-bg-switcher {
  position: relative;
  z-index: 2;
}

.ai-title-header {
  text-align: center;
  padding: 0.5rem;
  margin: 10px;
  background:rgba(247, 247, 247, 0.545) ;
  color: #8c7853;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  border-radius: 10px;
  position: relative;
  z-index: 2;
}

.ai-title-header h1 {
  color: #e5e5e5f0;
  font-family: eva, 'STKaiti', 'KaiTi', serif;
  font-size: 40px;
  text-shadow: 3px 3px 10px rgba(0, 0, 0, 0.5);
  animation: float 3s ease-in-out infinite;
  margin-bottom: 0.5rem;
}

.ai-title-header p {
  animation: float 3s ease-in-out infinite;
  font-size: 18px;
  color: #8c7853;
  margin-bottom: 0;
}

@keyframes float {
  0% {
    transform: translateY(0);
  }

  50% {
    transform: translateY(-4px);
  }

  100% {
    transform: translateY(0);
  }
}

.ai-content-layout {
  display: flex;
  width: 100%;
  flex: 1 1 auto;
  min-height: 60vh;
}

.ai-sidebar {
  flex: 0 0 20%;
  background: #ffffff9d;
  border-right: 1.5px solid #e5d8c3;
  padding: 2.5rem 1.2rem 1.2rem;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  min-width: 180px;
  max-width: 260px;
  box-shadow: 2px 0 8px rgba(140, 120, 83, 0.04);
  border-radius: 20px;
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
  box-shadow: 2px 2px 6px rgba(140, 120, 83, 0.1);
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
  position: relative;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
  z-index: 0;
}

/* 聊天内容区、输入区等都在z-index:2以上 */
.chat-history,
.input-tips,
.chat-input-row,
.role-select-modal {
  position: relative;
  z-index: 2;
}

.role-select-modal {
  position: absolute;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.18);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

.role-select-content {
  position: relative;
  background: #fff;
  border-radius: 12px;
  padding: 2rem 2.5rem;
  box-shadow: 0 4px 16px rgba(140, 120, 83, 0.13);
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
  box-shadow: 0 2px 8px rgba(140, 120, 83, 0.08);
}

.ai-msg .msg-avatar {
  animation: ai-pulse 2.5s ease-in-out infinite;
}

@keyframes ai-pulse {

  0%,
  100% {
    transform: scale(1);
    box-shadow: 0 0 10px rgba(140, 120, 83, 0.2);
  }

  50% {
    transform: scale(1.08);
    box-shadow: 0 0 20px rgba(140, 120, 83, 0.4);
  }
}

.msg-content {
  max-width: 70%;
  border-radius: 12px;
  padding: 1rem 1.2rem;
  font-size: 1.08rem;
  color: #5a4634;
  font-family: 'STKaiti', 'KaiTi', serif;
  line-height: 1.8;
  box-shadow: 0 2px 8px rgba(140, 120, 83, 0.07);
  word-break: break-word;
}


.user-msg .msg-content {
  background: #ffffff90;
  color: #6e5773;
  align-self: flex-end;
  padding: 0rem 1.2rem;
}

.ai-msg .msg-content {
  background: #ffffffba;
  color: #8c7853;
}

@keyframes blinkCursor {

  0%,
  100% {
    opacity: 1;
  }

  50% {
    opacity: 0.1;
  }
}

.chat-input-row {
  display: flex;
  align-items: flex-end;
  padding: 1rem 2.5rem;
  background: #f9f8f513;
  border-radius: 18px;
  gap: 1rem;
  
}

.chat-input {
  flex: 1 1 auto;
  min-height: 48px;
  max-height: 120px;
  border-radius: 12px;
  padding: 0.5rem 1rem;
  font-size: 1.08rem;
  background: repeating-linear-gradient(to bottom,
      #fffaf4,
      #fffaf4 32px,
      #f0e7d8 33px);
  font-family: 'STKaiti', 'KaiTi', serif;
  border: 1.5px dashed #cbbba0;
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
  box-shadow: 0 2px 8px rgba(140, 120, 83, 0.07);
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
  box-shadow: 0 2px 8px rgba(140, 120, 83, 0.08);
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
  box-shadow: 0 2px 12px rgba(140, 120, 83, 0.10);
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
  box-shadow: 0 6px 24px rgba(140, 120, 83, 0.18);
  border-color: #8c7853;
  background: linear-gradient(135deg, #f3f0eb 60%, #e7e0d0 100%);
}

.poet-card-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  object-fit: cover;
  margin-bottom: 1rem;
  box-shadow: 0 2px 8px rgba(140, 120, 83, 0.10);
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
  box-shadow: 0 0 16px #a3916a88, 0 2px 8px rgba(140, 120, 83, 0.18);
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
  box-shadow: 0 2px 8px rgba(140, 120, 83, 0.07);
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
  0% {
    transform: rotate(0deg);
  }

  20% {
    transform: rotate(-8deg);
  }

  40% {
    transform: rotate(8deg);
  }

  60% {
    transform: rotate(-6deg);
  }

  80% {
    transform: rotate(6deg);
  }

  100% {
    transform: rotate(0deg);
  }
}

.avatar-hoverable:active .poet-avatar {
  animation: avatar-bounce 0.25s;
}

@keyframes avatar-bounce {
  0% {
    transform: scale(1);
  }

  50% {
    transform: scale(1.18);
  }

  100% {
    transform: scale(1);
  }
}

@keyframes avatar-shake {
  0% {
    transform: rotate(0deg);
  }

  20% {
    transform: rotate(-8deg);
  }

  40% {
    transform: rotate(8deg);
  }

  60% {
    transform: rotate(-6deg);
  }

  80% {
    transform: rotate(6deg);
  }

  100% {
    transform: rotate(0deg);
  }
}


.role-select-back {
  position: absolute;
  left: 0.2rem;
  top: 0.2rem;
  display: flex;
  align-items: center;
  gap: 0.3rem;
  cursor: pointer;
  font-size: 0.8rem;
  color: #8c7853;
  background: #f3f0eb;
  border-radius: 20px;
  padding: 0.1rem 0.9rem 0.1rem 0.3rem;
  box-shadow: 0 2px 8px rgba(140, 120, 83, 0.07);
  transition: background 0.18s, color 0.18s, box-shadow 0.18s;
  z-index: 2;
}

.role-select-back:hover {
  background: #e7e0d0;
  color: #6e5773;
  box-shadow: 0 4px 16px rgba(140, 120, 83, 0.13);
}

.role-select-back svg {
  vertical-align: middle;
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

  .chat-header,
  .chat-history,
  .chat-input-row {
    padding-left: 1rem;
    padding-right: 1rem;
  }
}

.input-tips {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin: 0 0 0.5rem 0;
  padding-left: 2.5rem;
  font-size: 1rem;
  color: #a3916a;
  user-select: none;
}

.input-tips-icon {
  font-size: 1.2rem;
}

.input-tips-label {
  color: #b8a888;
  font-size: 0.85rem;
}

.input-tip {
  background: #f3f0eb;
  color: #8c7853;
  border-radius: 8px;
  padding: 2px 10px;
  margin-left: 0.3rem;
  cursor: pointer;
  font-size: 0.8rem;
  transition: background 0.18s, color 0.18s;
  border: 1px solid #e7e0d0;
}

.input-tip:hover {
  background: #e7e0d0;
  color: #6e5773;
  border-color: #a3916a;
}

/* 聊天背景切换淡入淡出动画 */
.chat-bg-fade-enter-active, .chat-bg-fade-leave-active {
  transition: opacity 0.5s;
}
.chat-bg-fade-enter-from, .chat-bg-fade-leave-to {
  opacity: 0;
}
.chat-bg-fade-enter-to, .chat-bg-fade-leave-from {
  opacity: 1;
}

.chat-bg-switcher {
  position: absolute;
  top: 160px;
  right: 28px;
  z-index: 5;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: rgba(255, 255, 255, 0.548);
  border-radius: 18px;
  padding: 2px 12px;
  box-shadow: 0 2px 8px rgba(140,120,83,0.08);
  user-select: none;
}

.bg-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  color: #8c7853;
  cursor: pointer;
  padding: 0 6px;
  border-radius: 50%;
  transition: background 0.18s;
}
.bg-btn:hover {
  background: #e7e0d0;
}

.bg-index {
  font-size: 1rem;
  color: #8c7853;
  font-family: monospace;
}
</style>