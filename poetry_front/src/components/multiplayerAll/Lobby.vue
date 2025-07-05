<template>
  <div class="lobby-container">
    <h1 class="title">多人游戏大厅</h1>
    <div class="card-box">
      <!-- 创建房间 -->
      <div class="lobby-card">
        <h2>创建房间</h2>
        <button @click="createRoom" :disabled="creating">{{ creating ? "正在创建..." : "创建房间" }}</button>
        <div v-if="createdRoomId" class="room-id">
          房间号：<span class="room-id-value">{{ createdRoomId }}</span>
          <button @click="joinCreatedRoom" class="join-btn">加入该房间</button>
        </div>
        <div v-if="historyRooms.length > 0" class="history-rooms">
          <h3>最近创建的房间</h3>
          <div v-for="(room, index) in historyRooms" :key="index" class="history-room-item">
            <span>{{ room }}</span>
            <button @click="useHistoryRoom(room)" class="small-btn">使用</button>
          </div>
          <button @click="clearHistoryRooms" class="clear-btn">清空历史</button>
        </div>
      </div>
      <!-- 加入房间 -->
      <div class="lobby-card">
        <h2>加入房间</h2>
        <input
            v-model="joinRoomId"
            type="text"
            placeholder="请输入房间号"
            maxlength="10"
        />
        <button @click="joinRoom" :disabled="joining">
          {{ joining ? "正在加入..." : "加入房间" }}
        </button>
        <div v-if="joinError" class="error-msg">{{ joinError }}</div>
        <div v-if="joinedSuccess" class="success-msg">
          成功加入房间：{{ joinRoomId }}
        </div>
        <div v-if="recentJoinedRooms.length > 0" class="history-rooms">
          <h3>最近加入的房间</h3>
          <div v-for="(room, index) in recentJoinedRooms" :key="index" class="history-room-item">
            <span>{{ room }}</span>
            <button @click="useRecentRoom(room)" class="small-btn">使用</button>
          </div>
          <button @click="clearRecentRooms" class="clear-btn">清空历史</button>
        </div>
      </div>
    </div>

    <!-- 房间状态 -->
    <div class="room-status-section" v-if="currentRoom">
      <h3>当前房间</h3>
      <div class="room-info">
        <p>房间ID: {{ currentRoom }}</p>
        <p>玩家数: {{ roomPlayerCount }}/2</p>
        <div v-if="roomPlayerCount >= 2" class="room-full">
          房间已满，即将进入游戏...
        </div>
      </div>
    </div>

    <!-- 用户信息 -->
    <div class="user-info-section" v-if="userInfo">
      <h3>当前用户信息</h3>
      <div>用户ID: {{ userInfo.uid }}</div>
      <button @click="clearUserInfo" class="clear-btn">清除信息</button>
    </div>

    <!-- 连接状态 -->
    <div class="connection-status">
      <div class="status-indicator" :class="connectionStatus">
        连接状态: {{ connectionStatusText }}
      </div>
      <button @click="handleConnectionToggle" class="connection-btn">
        {{ isConnected ? '断开连接' : '重新连接' }}
      </button>
    </div>

    <!-- 消息日志（调试用） -->
    <div v-if="showDebugLog" class="debug-log">
      <h3>消息日志</h3>
      <div class="log-container">
        <div v-for="(log, index) in messageLog" :key="index" class="log-entry">
          <span class="log-time">[{{ log.timestamp }}]</span>
          <span class="log-type">[{{ log.type }}]</span>
          <span class="log-content">{{ log.content }}</span>
        </div>
      </div>
      <button @click="clearLog" class="clear-btn">清空日志</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from "vue";
import { useRouter } from 'vue-router';
// 导入storageUtil中的方法
import { saveData, getData, updateData, removeData, hasData, clearAllData } from '../util/storageUtil';

// 获取路由器实例
const router = useRouter();

// 存储相关的常量键名
const STORAGE_KEYS = {
  CREATED_ROOMS: 'multiGame_createdRooms',
  JOINED_ROOMS: 'multiGame_joinedRooms',
  USER_INFO: 'multiGame_userInfo',
  WEBSOCKET_STATUS: 'multiGame_websocketStatus',
  CURRENT_ROOM: 'multiGame_currentRoom'
};

// 基础状态
const createdRoomId = ref("");
const creating = ref(false);
const joinRoomId = ref("");
const joining = ref(false);
const joinError = ref("");
const joinedSuccess = ref(false);

// 历史记录相关状态
const historyRooms = ref([]);
const recentJoinedRooms = ref([]);
const userInfo = ref(null);

// 房间状态
const currentRoom = ref(null);
const roomPlayerCount = ref(0);
const roomStatus = ref(null);

// websocket相关状态
let websocket = ref(null);
let isConnected = ref(false);
let connectionStatus = ref('disconnected');
let connectionStatusText = ref('未连接');
let messageLog = ref([]);
const showDebugLog = ref(true); // 设置为true显示调试日志

onMounted(() => {
  // 加载历史房间记录
  loadHistoryData();
  // 加载用户信息
  loadUserInfo();
  // 连接WebSocket
  connect();

  // 从URL参数中获取调试模式
  const urlParams = new URLSearchParams(window.location.search);
  if (urlParams.get('debug') === 'true') {
    showDebugLog.value = true;
  }
});

// 监听房间人数变化，如果达到2人自动跳转
watch(roomPlayerCount, (newCount) => {
  if (newCount >= 2) {
    addLog('info', `房间人数达到${newCount}人，准备跳转到游戏界面`);

    // 保存当前房间信息以便在游戏页面使用
    saveData('current_game_room', {
      roomId: currentRoom.value,
      userId: userInfo.value.uid
    });

    // 延迟1.5秒后跳转，让用户看到提示信息
    setTimeout(() => {
      router.push('/multiplay_r');
    }, 1500);
  }
});

// 加载存储的历史数据
function loadHistoryData() {
  const savedCreatedRooms = getData(STORAGE_KEYS.CREATED_ROOMS) || [];
  const savedJoinedRooms = getData(STORAGE_KEYS.JOINED_ROOMS) || [];
  historyRooms.value = savedCreatedRooms;
  recentJoinedRooms.value = savedJoinedRooms;

  // 如果有当前房间信息，可以自动填充
  const savedCurrentRoom = getData(STORAGE_KEYS.CURRENT_ROOM);
  if (savedCurrentRoom) {
    currentRoom.value = savedCurrentRoom;
    joinRoomId.value = savedCurrentRoom;
  }
}

// 加载用户信息
function loadUserInfo() {
  const savedUserInfo = getData(STORAGE_KEYS.USER_INFO);
  if (savedUserInfo) {
    userInfo.value = savedUserInfo;
  } else {
    // 如果没有用户信息，创建一个临时用户ID
    userInfo.value = {
      uid: Math.floor(Math.random() * 10000).toString() // 使用纯数字ID以匹配后端
    };
    saveData(STORAGE_KEYS.USER_INFO, userInfo.value);
  }
}

// 清除用户信息
function clearUserInfo() {
  removeData(STORAGE_KEYS.USER_INFO);
  userInfo.value = null;
  // 重新创建临时用户ID
  loadUserInfo();
}

// 使用历史创建的房间
function useHistoryRoom(roomId) {
  createdRoomId.value = roomId;
  joinRoomId.value = roomId; // 也设置加入房间的输入框
}

// 加入创建的房间
function joinCreatedRoom() {
  if (!createdRoomId.value) return;
  joinRoomId.value = createdRoomId.value;
  joinRoom();
}

// 使用历史加入的房间
function useRecentRoom(roomId) {
  joinRoomId.value = roomId;
}

// 清空历史创建的房间记录
function clearHistoryRooms() {
  historyRooms.value = [];
  saveData(STORAGE_KEYS.CREATED_ROOMS, []);
}

// 清空最近加入的房间记录
function clearRecentRooms() {
  recentJoinedRooms.value = [];
  saveData(STORAGE_KEYS.JOINED_ROOMS, []);
}

// 创建房间的逻辑
async function createRoom() {
  if (!userInfo.value) {
    addLog('error', '未找到用户信息');
    return;
  }

  creating.value = true;
  createdRoomId.value = "";

  // 使用WebSocket创建房间
  const message = {
    type: 'createRoom',
    room: {
      uid: userInfo.value.uid.toString() // 确保发送字符串类型的UID
    }
  };

  sendMessage(message);
  addLog('info', '发送创建房间请求');
}

// 加入房间逻辑
async function joinRoom() {
  joinError.value = "";
  joinedSuccess.value = false;

  if (!joinRoomId.value) {
    joinError.value = "请输入房间号";
    return;
  }

  joining.value = true;

  // 构建加入房间消息
  const message = {
    type: 'joinRoom',
    room: {
      roomId: joinRoomId.value,
      uid: userInfo.value.uid.toString() // 确保发送字符串类型的UID
    }
  };

  sendMessage(message);
  addLog('info', `发送加入房间请求: ${joinRoomId.value}`);
}

// WebSocket连接管理
async function connect() {
  try {
    // 检查之前的连接状态
    const savedStatus = getData(STORAGE_KEYS.WEBSOCKET_STATUS);
    if (savedStatus) {
      connectionStatus.value = savedStatus.status;
      connectionStatusText.value = savedStatus.text;
    }

    // 根据您的后端地址调整WebSocket URL
    const wsUrl = 'ws://localhost:8081/ws/game'; // 请根据实际端口调整
    websocket.value = new WebSocket(wsUrl);

    websocket.value.onopen = onOpen;
    websocket.value.onmessage = onMessage;
    websocket.value.onclose = onClose;
    websocket.value.onerror = onError;

    connectionStatus.value = 'connecting';
    connectionStatusText.value = '连接中...';

    // 保存连接状态
    saveData(STORAGE_KEYS.WEBSOCKET_STATUS, {
      status: 'connecting',
      text: '连接中...'
    });
  } catch (error) {
    addLog('error', `连接失败: ${error.message}`);
  }
}

function handleConnectionToggle() {
  if (isConnected.value) {
    disconnect();
  } else {
    connect();
  }
}

async function disconnect() {
  if (websocket.value) {
    websocket.value.close();
  }
}

async function onOpen(event) {
  isConnected.value = true;
  connectionStatus.value = 'connected';
  connectionStatusText.value = '已连接';

  // 保存连接状态
  saveData(STORAGE_KEYS.WEBSOCKET_STATUS, {
    status: 'connected',
    text: '已连接'
  });

  addLog('success', 'WebSocket连接已建立');
}

async function onMessage(event) {
  try {
    const data = JSON.parse(event.data);
    addLog('received', `收到消息: ${JSON.stringify(data, null, 2)}`);

    // 处理不同类型的消息
    switch (data.type) {
      case "connection":
        // 连接成功响应
        handleConnectionMessage(data);
        break;
      case "create_room_result":
        // 创建房间结果
        handleCreateRoomResult(data);
        break;
      case "join_room_result":
        // 加入房间结果
        handleJoinRoomResult(data);
        break;
      case "room_update":
        // 房间更新信息
        handleRoomUpdate(data);
        break;
      case "error":
        // 错误消息
        handleErrorMessage(data);
        break;
      default:
        addLog('info', `收到未处理的消息类型: ${data.type}`);
    }
  } catch (error) {
    addLog('error', `解析消息失败: ${error.message}, 原始消息: ${event.data}`);
  }
}

// 处理连接成功消息
function handleConnectionMessage(data) {
  addLog('info', `连接成功，会话ID: ${data.sessionId}`);
}

// 处理创建房间结果
function handleCreateRoomResult(data) {
  creating.value = false;

  if (data.success) {
    createdRoomId.value = data.roomId;

    // 保存到历史记录
    const updatedHistory = [...historyRooms.value.filter(room => room !== data.roomId), data.roomId];
    while (updatedHistory.length > 5) {
      updatedHistory.shift();
    }
    historyRooms.value = updatedHistory;
    saveData(STORAGE_KEYS.CREATED_ROOMS, updatedHistory);

    addLog('success', `成功创建房间: ${data.roomId}`);
  } else {
    addLog('error', `创建房间失败: ${data.message || '未知错误'}`);
  }
}

// 处理加入房间结果
function handleJoinRoomResult(data) {
  joining.value = false;

  if (data.success) {
    joinedSuccess.value = true;
    joinError.value = "";
    currentRoom.value = data.roomId;

    // 保存到最近加入的房间记录
    const updatedJoined = [...recentJoinedRooms.value.filter(room => room !== data.roomId), data.roomId];
    while (updatedJoined.length > 5) {
      updatedJoined.shift();
    }
    recentJoinedRooms.value = updatedJoined;
    saveData(STORAGE_KEYS.JOINED_ROOMS, updatedJoined);

    // 保存当前房间
    saveData(STORAGE_KEYS.CURRENT_ROOM, data.roomId);

    addLog('success', `成功加入房间: ${data.roomId}`);

    // 这里假设加入成功后人数至少为1
    roomPlayerCount.value = 1;
  } else {
    joinError.value = data.message || "加入失败，请检查房间号";
    addLog('error', `加入房间失败: ${data.message || '未知错误'}`);
  }
}

// 处理房间更新信息
function handleRoomUpdate(data) {
  if (data.roomId && data.roomId === currentRoom.value) {
    if (data.playerCount !== undefined) {
      roomPlayerCount.value = data.playerCount;
      addLog('info', `房间 ${data.roomId} 的玩家数量更新为: ${data.playerCount}`);
    }

    if (data.status) {
      roomStatus.value = data.status;
      addLog('info', `房间 ${data.roomId} 状态更新为: ${data.status}`);
    }
  }
}

// 处理错误消息
function handleErrorMessage(data) {
  addLog('error', `收到错误消息: ${data.message}`);

  if (creating.value) {
    creating.value = false;
  }

  if (joining.value) {
    joining.value = false;
    joinError.value = data.message || "操作失败";
  }
}

async function onClose(event) {
  isConnected.value = false;
  connectionStatus.value = 'disconnected';
  connectionStatusText.value = '未连接';

  // 保存连接状态
  saveData(STORAGE_KEYS.WEBSOCKET_STATUS, {
    status: 'disconnected',
    text: '未连接'
  });

  addLog('info', `连接已关闭: ${event.code} - ${event.reason || '无原因'}`);
}

async function onError(event) {
  connectionStatus.value = 'error';
  connectionStatusText.value = '连接错误';
  addLog('error', `WebSocket错误: ${event}`);
}

async function sendMessage(message) {
  if (websocket.value && isConnected.value) {
    try {
      const messageStr = JSON.stringify(message);
      websocket.value.send(messageStr);
      addLog('sent', `发送消息: ${messageStr}`);
    } catch (error) {
      addLog('error', `发送消息失败: ${error.message}`);
    }
  } else {
    addLog('error', 'WebSocket未连接，无法发送消息');

    // 如果WebSocket未连接，尝试重新连接
    if (!isConnected.value) {
      addLog('info', '尝试重新连接WebSocket...');
      connect();
    }
  }
}

function addLog(type, content) {
  const now = new Date();
  const timestamp = now.toLocaleTimeString();
  messageLog.value = [...messageLog.value, { type, content, timestamp }].slice(-100);
}

function clearLog() {
  messageLog.value = [];
}
</script>

<style scoped>
.lobby-container {
  max-width: 520px;
  margin: 48px auto;
  padding: 32px 20px;
  background: #fffbe9;
  border-radius: 20px;
  box-shadow: 0 4px 24px #eee5d0;
  font-family: "PingFang SC", "Microsoft Yahei", sans-serif;
}
.title {
  font-size: 2rem;
  text-align: center;
  margin-bottom: 36px;
  letter-spacing: 4px;
}
.card-box {
  display: flex;
  justify-content: space-around;
  gap: 30px;
  flex-wrap: wrap;
}
.lobby-card {
  flex: 1 1 220px;
  background: #fff;
  border-radius: 16px;
  padding: 24px 18px;
  box-shadow: 0 2px 8px #f5e7d6;
  text-align: center;
}
.lobby-card h2 {
  font-size: 1.2rem;
  margin-bottom: 18px;
}
input[type="text"] {
  border: 1px solid #eed6b4;
  border-radius: 8px;
  padding: 8px 12px;
  width: 80%;
  margin-bottom: 10px;
  font-size: 1rem;
  background: #fffbe9;
}
button {
  background: #ffeb99;
  border: none;
  border-radius: 8px;
  padding: 8px 22px;
  font-size: 1rem;
  cursor: pointer;
  margin-bottom: 8px;
  transition: background 0.2s;
}
button:disabled {
  background: #eee;
  cursor: not-allowed;
}
button:hover:not(:disabled) {
  background: #ffe066;
}
.join-btn {
  display: block;
  margin: 10px auto;
  background: #c1e1c1;
  color: #2c662d;
}
.room-id {
  margin-top: 14px;
  color: #905901;
  font-weight: bold;
  font-size: 1.12rem;
}
.room-id-value {
  font-size: 1.3rem;
  letter-spacing: 2px;
}
.error-msg {
  margin-top: 10px;
  color: #cc3300;
}
.success-msg {
  margin-top: 10px;
  color: #1a8835;
  font-weight: bold;
}
.history-rooms {
  margin-top: 20px;
  border-top: 1px dashed #eed6b4;
  padding-top: 12px;
}
.history-rooms h3 {
  font-size: 0.9rem;
  color: #666;
  margin-bottom: 8px;
}
.history-room-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  padding: 6px;
  background: #fffbe9;
  border-radius: 6px;
}
.small-btn {
  padding: 4px 8px;
  font-size: 0.8rem;
  margin: 0;
}
.clear-btn {
  margin-top: 10px;
  background: #ffebe3;
  color: #cc3300;
  font-size: 0.8rem;
  padding: 4px 8px;
}
.user-info-section, .room-status-section {
  margin-top: 30px;
  padding: 15px;
  background: #fff;
  border-radius: 16px;
  text-align: center;
}
.room-status-section .room-info {
  margin-top: 10px;
}
.room-status-section .room-full {
  color: #1a8835;
  font-weight: bold;
  margin-top: 10px;
  padding: 8px;
  background-color: #e4ffe4;
  border-radius: 6px;
}
.connection-status {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background: #fff;
  border-radius: 16px;
}
.status-indicator {
  display: inline-block;
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 0.9rem;
}
.status-indicator.connected {
  background: #e4ffe4;
  color: #1a8835;
}
.status-indicator.disconnected {
  background: #ffe4e4;
  color: #cc3300;
}
.status-indicator.connecting {
  background: #fffde4;
  color: #8f7600;
}
.status-indicator.error {
  background: #ff9e9e;
  color: #990000;
}
.connection-btn {
  padding: 4px 8px;
  font-size: 0.8rem;
}
.debug-log {
  margin-top: 20px;
  padding: 15px;
  background: #fff;
  border-radius: 16px;
}
.debug-log h3 {
  font-size: 1rem;
  margin-bottom: 10px;
}
.log-container {
  max-height: 200px;
  overflow-y: auto;
  border: 1px solid #eee;
  padding: 8px;
  border-radius: 4px;
  font-family: monospace;
  font-size: 0.85rem;
}
.log-entry {
  margin-bottom: 4px;
  border-bottom: 1px solid #f0f0f0;
  padding-bottom: 4px;
}
.log-time {
  color: #666;
  margin-right: 5px;
}
.log-type {
  font-weight: bold;
  margin-right: 5px;
}
.log-type.error {
  color: #cc3300;
}
.log-type.success {
  color: #1a8835;
}
.log-content {
  word-break: break-word;
}
</style>

<!--<template>-->
<!--  <div class="lobby-container">-->
<!--    <h1 class="title">多人游戏大厅</h1>-->
<!--    <div class="card-box">-->
<!--      &lt;!&ndash; 创建房间 &ndash;&gt;-->
<!--      <div class="lobby-card">-->
<!--        <h2>创建房间</h2>-->
<!--        <button @click="createRoom" :disabled="creating">{{ creating ? "正在创建..." : "创建房间" }}</button>-->
<!--        <div v-if="createdRoomId" class="room-id">-->
<!--          房间号：<span class="room-id-value">{{ createdRoomId }}</span>-->
<!--        </div>-->
<!--        <div v-if="historyRooms.length > 0" class="history-rooms">-->
<!--          <h3>最近创建的房间</h3>-->
<!--          <div v-for="(room, index) in historyRooms" :key="index" class="history-room-item">-->
<!--            <span>{{ room }}</span>-->
<!--            <button @click="useHistoryRoom(room)" class="small-btn">使用</button>-->
<!--          </div>-->
<!--          <button @click="clearHistoryRooms" class="clear-btn">清空历史</button>-->
<!--        </div>-->
<!--      </div>-->
<!--      &lt;!&ndash; 加入房间 &ndash;&gt;-->
<!--      <div class="lobby-card">-->
<!--        <h2>加入房间</h2>-->
<!--        <input-->
<!--            v-model="joinRoomId"-->
<!--            type="text"-->
<!--            placeholder="请输入房间号"-->
<!--            maxlength="10"-->
<!--        />-->
<!--        <button @click="joinRoom" :disabled="joining">-->
<!--          {{ joining ? "正在加入..." : "加入房间" }}-->
<!--        </button>-->
<!--        <div v-if="joinError" class="error-msg">{{ joinError }}</div>-->
<!--        <div v-if="joinedSuccess" class="success-msg">-->
<!--          成功加入房间：{{ joinRoomId }}-->
<!--        </div>-->
<!--        <div v-if="recentJoinedRooms.length > 0" class="history-rooms">-->
<!--          <h3>最近加入的房间</h3>-->
<!--          <div v-for="(room, index) in recentJoinedRooms" :key="index" class="history-room-item">-->
<!--            <span>{{ room }}</span>-->
<!--            <button @click="useRecentRoom(room)" class="small-btn">使用</button>-->
<!--          </div>-->
<!--          <button @click="clearRecentRooms" class="clear-btn">清空历史</button>-->
<!--        </div>-->
<!--      </div>-->
<!--    </div>-->

<!--    &lt;!&ndash; 房间状态 &ndash;&gt;-->
<!--    <div class="room-status-section" v-if="currentRoom">-->
<!--      <h3>当前房间</h3>-->
<!--      <div class="room-info">-->
<!--        <p>房间ID: {{ currentRoom }}</p>-->
<!--        <p>玩家数: {{ roomPlayerCount }}/2</p>-->
<!--        <div v-if="roomPlayerCount >= 2" class="room-full">-->
<!--          房间已满，即将进入游戏...-->
<!--        </div>-->
<!--      </div>-->
<!--    </div>-->

<!--    &lt;!&ndash; 用户信息 &ndash;&gt;-->
<!--    <div class="user-info-section" v-if="userInfo">-->
<!--      <h3>当前用户信息</h3>-->
<!--      <div>用户ID: {{ userInfo.uid }}</div>-->
<!--      <button @click="clearUserInfo" class="clear-btn">清除信息</button>-->
<!--    </div>-->

<!--    &lt;!&ndash; 连接状态 &ndash;&gt;-->
<!--    <div class="connection-status">-->
<!--      <div class="status-indicator" :class="connectionStatus">-->
<!--        连接状态: {{ connectionStatusText }}-->
<!--      </div>-->
<!--      <button @click="handleConnectionToggle" class="connection-btn">-->
<!--        {{ isConnected ? '断开连接' : '重新连接' }}-->
<!--      </button>-->
<!--    </div>-->

<!--    &lt;!&ndash; 消息日志（调试用） &ndash;&gt;-->
<!--    <div v-if="showDebugLog" class="debug-log">-->
<!--      <h3>消息日志</h3>-->
<!--      <div class="log-container">-->
<!--        <div v-for="(log, index) in messageLog" :key="index" class="log-entry">-->
<!--          <span class="log-time">[{{ log.timestamp }}]</span>-->
<!--          <span class="log-type">[{{ log.type }}]</span>-->
<!--          <span class="log-content">{{ log.content }}</span>-->
<!--        </div>-->
<!--      </div>-->
<!--    </div>-->
<!--  </div>-->
<!--</template>-->

<!--<script setup>-->
<!--import { ref, onMounted, watch } from "vue";-->
<!--import { useRouter } from 'vue-router';-->
<!--// 导入storageUtil中的方法-->
<!--import { saveData, getData, updateData, removeData, hasData, clearAllData } from '../util/storageUtil';-->

<!--// 获取路由器实例-->
<!--const router = useRouter();-->

<!--// 存储相关的常量键名-->
<!--const STORAGE_KEYS = {-->
<!--  CREATED_ROOMS: 'multiGame_createdRooms',-->
<!--  JOINED_ROOMS: 'multiGame_joinedRooms',-->
<!--  USER_INFO: 'multiGame_userInfo',-->
<!--  WEBSOCKET_STATUS: 'multiGame_websocketStatus',-->
<!--  CURRENT_ROOM: 'multiGame_currentRoom'-->
<!--};-->

<!--// 基础状态-->
<!--const createdRoomId = ref("");-->
<!--const creating = ref(false);-->
<!--const joinRoomId = ref("");-->
<!--const joining = ref(false);-->
<!--const joinError = ref("");-->
<!--const joinedSuccess = ref(false);-->

<!--// 历史记录相关状态-->
<!--const historyRooms = ref([]);-->
<!--const recentJoinedRooms = ref([]);-->
<!--const userInfo = ref(null);-->

<!--// 房间状态-->
<!--const currentRoom = ref(null);-->
<!--const roomPlayerCount = ref(0);-->
<!--const roomStatus = ref(null);-->

<!--// websocket相关状态-->
<!--let websocket = ref(null);-->
<!--let isConnected = ref(false);-->
<!--let connectionStatus = ref('disconnected');-->
<!--let connectionStatusText = ref('未连接');-->
<!--let messageLog = ref([]);-->
<!--const showDebugLog = ref(false); // 设置为true可以显示日志-->

<!--onMounted(() => {-->
<!--  // 加载历史房间记录-->
<!--  loadHistoryData();-->
<!--  // 加载用户信息-->
<!--  loadUserInfo();-->
<!--  // 连接WebSocket-->
<!--  connect();-->

<!--  // 从URL参数中获取调试模式-->
<!--  const urlParams = new URLSearchParams(window.location.search);-->
<!--  if (urlParams.get('debug') === 'true') {-->
<!--    showDebugLog.value = true;-->
<!--  }-->
<!--});-->

<!--// 监听房间人数变化，如果达到2人自动跳转-->
<!--watch(roomPlayerCount, (newCount) => {-->
<!--  if (newCount >= 2) {-->
<!--    addLog('info', `房间人数达到${newCount}人，准备跳转到游戏界面`);-->

<!--    // 保存当前房间信息以便在游戏页面使用-->
<!--    saveData('current_game_room', {-->
<!--      roomId: currentRoom.value,-->
<!--      userId: userInfo.value.uid-->
<!--    });-->

<!--    // 延迟1.5秒后跳转，让用户看到提示信息-->
<!--    // setTimeout(() => {-->
<!--    //   router.push('/multiplay_r');-->
<!--    // }, 1500);-->
<!--  }-->
<!--});-->

<!--// 加载存储的历史数据-->
<!--function loadHistoryData() {-->
<!--  const savedCreatedRooms = getData(STORAGE_KEYS.CREATED_ROOMS) || [];-->
<!--  const savedJoinedRooms = getData(STORAGE_KEYS.JOINED_ROOMS) || [];-->
<!--  historyRooms.value = savedCreatedRooms;-->
<!--  recentJoinedRooms.value = savedJoinedRooms;-->

<!--  // 如果有当前房间信息，可以自动填充-->
<!--  const savedCurrentRoom = getData(STORAGE_KEYS.CURRENT_ROOM);-->
<!--  if (savedCurrentRoom) {-->
<!--    currentRoom.value = savedCurrentRoom;-->
<!--    joinRoomId.value = savedCurrentRoom;-->
<!--  }-->
<!--}-->

<!--// 加载用户信息-->
<!--function loadUserInfo() {-->
<!--  const savedUserInfo = getData(STORAGE_KEYS.USER_INFO);-->
<!--  if (savedUserInfo) {-->
<!--    userInfo.value = savedUserInfo;-->
<!--  } else {-->
<!--    // 如果没有用户信息，创建一个临时用户ID-->
<!--    userInfo.value = {-->
<!--      uid: Math.floor(Math.random() * 10000).toString() // 使用纯数字ID以匹配后端-->
<!--    };-->
<!--    saveData(STORAGE_KEYS.USER_INFO, userInfo.value);-->
<!--  }-->
<!--}-->

<!--// 清除用户信息-->
<!--function clearUserInfo() {-->
<!--  removeData(STORAGE_KEYS.USER_INFO);-->
<!--  userInfo.value = null;-->
<!--  // 重新创建临时用户ID-->
<!--  loadUserInfo();-->
<!--}-->

<!--// 使用历史创建的房间-->
<!--function useHistoryRoom(roomId) {-->
<!--  createdRoomId.value = roomId;-->
<!--  currentRoom.value = roomId;-->
<!--  saveData(STORAGE_KEYS.CURRENT_ROOM, roomId);-->
<!--  // 尝试加入这个房间-->
<!--  joinSpecificRoom(roomId);-->
<!--}-->

<!--// 使用历史加入的房间-->
<!--function useRecentRoom(roomId) {-->
<!--  joinRoomId.value = roomId;-->
<!--  // 尝试加入这个房间-->
<!--  joinSpecificRoom(roomId);-->
<!--}-->

<!--// 清空历史创建的房间记录-->
<!--function clearHistoryRooms() {-->
<!--  historyRooms.value = [];-->
<!--  saveData(STORAGE_KEYS.CREATED_ROOMS, []);-->
<!--}-->

<!--// 清空最近加入的房间记录-->
<!--function clearRecentRooms() {-->
<!--  recentJoinedRooms.value = [];-->
<!--  saveData(STORAGE_KEYS.JOINED_ROOMS, []);-->
<!--}-->

<!--// 创建房间的逻辑-->
<!--async function createRoom() {-->
<!--  if (!userInfo.value) {-->
<!--    addLog('error', '未找到用户信息');-->
<!--    return;-->
<!--  }-->

<!--  creating.value = true;-->
<!--  createdRoomId.value = "";-->

<!--  // 使用WebSocket创建房间-->
<!--  const message = {-->
<!--    type: 'createRoom',-->
<!--    room: {-->
<!--      uid: userInfo.value.uid.toString() // 确保发送字符串类型的UID-->
<!--    }-->
<!--  };-->

<!--  await sendMessage(message);-->
<!--  addLog('info', '发送创建房间请求');-->
<!--}-->

<!--// 加入房间逻辑-->
<!--async function joinRoom() {-->
<!--  joinError.value = "";-->
<!--  joinedSuccess.value = false;-->

<!--  if (!joinRoomId.value) {-->
<!--    joinError.value = "请输入房间号";-->
<!--    return;-->
<!--  }-->

<!--  joining.value = true;-->
<!--  joinSpecificRoom(joinRoomId.value);-->
<!--}-->

<!--// 加入指定房间-->
<!--function joinSpecificRoom(roomId) {-->
<!--  if (!userInfo.value) {-->
<!--    addLog('error', '未找到用户信息');-->
<!--    joining.value = false;-->
<!--    return;-->
<!--  }-->

<!--  // 构建加入房间消息-->
<!--  const message = {-->
<!--    type: 'joinRoom',-->
<!--    room: {-->
<!--      roomId: roomId,-->
<!--      uid: userInfo.value.uid.toString() // 确保发送字符串类型的UID-->
<!--    }-->
<!--  };-->

<!--  sendMessage(message);-->
<!--  addLog('info', `发送加入房间请求: ${roomId}`);-->
<!--}-->

<!--// WebSocket连接管理-->
<!--async function connect() {-->
<!--  try {-->
<!--    // 检查之前的连接状态-->
<!--    const savedStatus = getData(STORAGE_KEYS.WEBSOCKET_STATUS);-->
<!--    if (savedStatus) {-->
<!--      connectionStatus.value = savedStatus.status;-->
<!--      connectionStatusText.value = savedStatus.text;-->
<!--    }-->

<!--    // 根据您的后端地址调整WebSocket URL-->
<!--    const wsUrl = 'ws://localhost:8081/ws/game'; // 请根据实际端口调整-->
<!--    websocket.value = new WebSocket(wsUrl);-->

<!--    websocket.value.onopen = onOpen;-->
<!--    websocket.value.onmessage = onMessage;-->
<!--    websocket.value.onclose = onClose;-->
<!--    websocket.value.onerror = onError;-->

<!--    connectionStatus.value = 'connecting';-->
<!--    connectionStatusText.value = '连接中...';-->

<!--    // 保存连接状态-->
<!--    saveData(STORAGE_KEYS.WEBSOCKET_STATUS, {-->
<!--      status: 'connecting',-->
<!--      text: '连接中...'-->
<!--    });-->
<!--  } catch (error) {-->
<!--    addLog('error', `连接失败: ${error.message}`);-->
<!--  }-->
<!--}-->

<!--function handleConnectionToggle() {-->
<!--  if (isConnected.value) {-->
<!--    disconnect();-->
<!--  } else {-->
<!--    connect();-->
<!--  }-->
<!--}-->

<!--async function disconnect() {-->
<!--  if (websocket.value) {-->
<!--    websocket.value.close();-->
<!--  }-->
<!--}-->

<!--async function onOpen(event) {-->
<!--  isConnected.value = true;-->
<!--  connectionStatus.value = 'connected';-->
<!--  connectionStatusText.value = '已连接';-->

<!--  // 保存连接状态-->
<!--  saveData(STORAGE_KEYS.WEBSOCKET_STATUS, {-->
<!--    status: 'connected',-->
<!--    text: '已连接'-->
<!--  });-->

<!--  addLog('success', 'WebSocket连接已建立');-->

<!--  // 如果有保存的房间ID，可以尝试自动加入-->
<!--  if (currentRoom.value) {-->
<!--    addLog('info', `尝试重新加入房间: ${currentRoom.value}`);-->
<!--    joinSpecificRoom(currentRoom.value);-->
<!--  }-->
<!--}-->

<!--async function onMessage(event) {-->
<!--  try {-->
<!--    const data = JSON.parse(event.data);-->
<!--    addLog('received', `收到消息: ${JSON.stringify(data, null, 2)}`);-->

<!--    // 处理不同类型的消息-->
<!--    switch (data.type) {-->
<!--      case "connection":-->
<!--        // 连接成功响应-->
<!--        handleConnectionMessage(data);-->
<!--        break;-->
<!--      case "create_room_result":-->
<!--        // 创建房间结果-->
<!--        handleCreateRoomResult(data);-->
<!--        break;-->
<!--      case "join_room_result":-->
<!--        // 加入房间结果-->
<!--        handleJoinRoomResult(data);-->
<!--        break;-->
<!--      case "room_update":-->
<!--        // 房间更新信息-->
<!--        handleRoomUpdate(data);-->
<!--        break;-->
<!--      case "error":-->
<!--        // 错误消息-->
<!--        handleErrorMessage(data);-->
<!--        break;-->
<!--      default:-->
<!--        addLog('info', `收到未处理的消息类型: ${data.type}`);-->
<!--    }-->
<!--  } catch (error) {-->
<!--    addLog('error', `解析消息失败: ${error.message}, 原始消息: ${event.data}`);-->
<!--  }-->
<!--}-->

<!--// 处理连接成功消息-->
<!--function handleConnectionMessage(data) {-->
<!--  addLog('info', `连接成功，会话ID: ${data.sessionId}`);-->
<!--}-->

<!--// 处理创建房间结果-->
<!--function handleCreateRoomResult(data) {-->
<!--  creating.value = false;-->

<!--  if (data.success) {-->
<!--    createdRoomId.value = data.roomId;-->
<!--    currentRoom.value = data.roomId;-->

<!--    // 保存到历史记录-->
<!--    const updatedHistory = [...historyRooms.value.filter(room => room !== data.roomId), data.roomId];-->
<!--    while (updatedHistory.length > 5) {-->
<!--      updatedHistory.shift();-->
<!--    }-->
<!--    historyRooms.value = updatedHistory;-->
<!--    saveData(STORAGE_KEYS.CREATED_ROOMS, updatedHistory);-->

<!--    // 保存当前房间-->
<!--    saveData(STORAGE_KEYS.CURRENT_ROOM, data.roomId);-->

<!--    addLog('success', `成功创建房间: ${data.roomId}`);-->

<!--    // 房间创建成功后，设置房间人数为1（创建者自己）-->
<!--    roomPlayerCount.value = 0;-->

<!--  } else {-->
<!--    addLog('error', `创建房间失败: ${data.message || '未知错误'}`);-->
<!--  }-->
<!--}-->

<!--// 处理加入房间结果-->
<!--function handleJoinRoomResult(data) {-->
<!--  joining.value = false;-->

<!--  if (data.success) {-->
<!--    joinedSuccess.value = true;-->
<!--    joinError.value = "";-->
<!--    currentRoom.value = data.roomId;-->

<!--    // 保存到最近加入的房间记录-->
<!--    const updatedJoined = [...recentJoinedRooms.value.filter(room => room !== data.roomId), data.roomId];-->
<!--    while (updatedJoined.length > 5) {-->
<!--      updatedJoined.shift();-->
<!--    }-->
<!--    recentJoinedRooms.value = updatedJoined;-->
<!--    saveData(STORAGE_KEYS.JOINED_ROOMS, updatedJoined);-->

<!--    // 保存当前房间-->
<!--    saveData(STORAGE_KEYS.CURRENT_ROOM, data.roomId);-->

<!--    addLog('success', `成功加入房间: ${data.roomId}`);-->

<!--    // 假设房间人数加1（这取决于后端是否发送了完整的房间信息）-->
<!--    // 如果后端没有提供房间人数，这里先设置为2，因为加入成功通常意味着有两个玩家了-->
<!--    roomPlayerCount.value = 2;  -->

<!--    // 这里应该有后端广播房间人数更新的消息，但如果没有，我们先假设房间满了-->
<!--    // 在真实情况下，你可能需要等待后端发送房间状态更新消息-->
<!--    addLog('info', `房间 ${data.roomId} 的玩家数量更新为: ${roomPlayerCount.value}`);-->
<!--  } else {-->
<!--    joinError.value = data.message || "加入失败，请检查房间号";-->
<!--    addLog('error', `加入房间失败: ${data.message || '未知错误'}`);-->
<!--  }-->
<!--}-->

<!--// 处理房间更新信息-->
<!--function handleRoomUpdate(data) {-->
<!--  if (data.roomId && data.roomId === currentRoom.value) {-->
<!--    if (data.playerCount !== undefined) {-->
<!--      roomPlayerCount.value = data.playerCount;-->
<!--      addLog('info', `房间 ${data.roomId} 的玩家数量更新为: ${data.playerCount}`);-->
<!--    }-->

<!--    if (data.status) {-->
<!--      roomStatus.value = data.status;-->
<!--      addLog('info', `房间 ${data.roomId} 状态更新为: ${data.status}`);-->
<!--    }-->
<!--  }-->
<!--}-->

<!--// 处理错误消息-->
<!--function handleErrorMessage(data) {-->
<!--  addLog('error', `收到错误消息: ${data.message}`);-->

<!--  if (creating.value) {-->
<!--    creating.value = false;-->
<!--  }-->

<!--  if (joining.value) {-->
<!--    joining.value = false;-->
<!--    joinError.value = data.message || "操作失败";-->
<!--  }-->
<!--}-->

<!--async function onClose(event) {-->
<!--  isConnected.value = false;-->
<!--  connectionStatus.value = 'disconnected';-->
<!--  connectionStatusText.value = '未连接';-->

<!--  // 保存连接状态-->
<!--  saveData(STORAGE_KEYS.WEBSOCKET_STATUS, {-->
<!--    status: 'disconnected',-->
<!--    text: '未连接'-->
<!--  });-->

<!--  addLog('info', `连接已关闭: ${event.code} - ${event.reason || '无原因'}`);-->
<!--}-->

<!--async function onError(event) {-->
<!--  connectionStatus.value = 'error';-->
<!--  connectionStatusText.value = '连接错误';-->
<!--  addLog('error', `WebSocket错误: ${event}`);-->
<!--}-->

<!--async function sendMessage(message) {-->
<!--  if (websocket.value && isConnected.value) {-->
<!--    try {-->
<!--      const messageStr = JSON.stringify(message);-->
<!--      websocket.value.send(messageStr);-->
<!--      addLog('sent', `发送消息: ${messageStr}`);-->
<!--    } catch (error) {-->
<!--      addLog('error', `发送消息失败: ${error.message}`);-->
<!--    }-->
<!--  } else {-->
<!--    addLog('error', 'WebSocket未连接，无法发送消息');-->

<!--    // 如果WebSocket未连接，尝试重新连接-->
<!--    if (!isConnected.value) {-->
<!--      addLog('info', '尝试重新连接WebSocket...');-->
<!--      connect();-->
<!--    }-->
<!--  }-->
<!--}-->

<!--function addLog(type, content) {-->
<!--  const now = new Date();-->
<!--  const timestamp = now.toLocaleTimeString();-->
<!--  messageLog.value = [...messageLog.value, { type, content, timestamp }].slice(-100);-->
<!--}-->

<!--function clearLog() {-->
<!--  messageLog.value = [];-->
<!--}-->
<!--</script>-->

<!--<style scoped>-->
<!--.lobby-container {-->
<!--  max-width: 520px;-->
<!--  margin: 48px auto;-->
<!--  padding: 32px 20px;-->
<!--  background: #fffbe9;-->
<!--  border-radius: 20px;-->
<!--  box-shadow: 0 4px 24px #eee5d0;-->
<!--  font-family: "PingFang SC", "Microsoft Yahei", sans-serif;-->
<!--}-->
<!--.title {-->
<!--  font-size: 2rem;-->
<!--  text-align: center;-->
<!--  margin-bottom: 36px;-->
<!--  letter-spacing: 4px;-->
<!--}-->
<!--.card-box {-->
<!--  display: flex;-->
<!--  justify-content: space-around;-->
<!--  gap: 30px;-->
<!--  flex-wrap: wrap;-->
<!--}-->
<!--.lobby-card {-->
<!--  flex: 1 1 220px;-->
<!--  background: #fff;-->
<!--  border-radius: 16px;-->
<!--  padding: 24px 18px;-->
<!--  box-shadow: 0 2px 8px #f5e7d6;-->
<!--  text-align: center;-->
<!--}-->
<!--.lobby-card h2 {-->
<!--  font-size: 1.2rem;-->
<!--  margin-bottom: 18px;-->
<!--}-->
<!--input[type="text"] {-->
<!--  border: 1px solid #eed6b4;-->
<!--  border-radius: 8px;-->
<!--  padding: 8px 12px;-->
<!--  width: 80%;-->
<!--  margin-bottom: 10px;-->
<!--  font-size: 1rem;-->
<!--  background: #fffbe9;-->
<!--}-->
<!--button {-->
<!--  background: #ffeb99;-->
<!--  border: none;-->
<!--  border-radius: 8px;-->
<!--  padding: 8px 22px;-->
<!--  font-size: 1rem;-->
<!--  cursor: pointer;-->
<!--  margin-bottom: 8px;-->
<!--  transition: background 0.2s;-->
<!--}-->
<!--button:disabled {-->
<!--  background: #eee;-->
<!--  cursor: not-allowed;-->
<!--}-->
<!--button:hover:not(:disabled) {-->
<!--  background: #ffe066;-->
<!--}-->
<!--.room-id {-->
<!--  margin-top: 14px;-->
<!--  color: #905901;-->
<!--  font-weight: bold;-->
<!--  font-size: 1.12rem;-->
<!--}-->
<!--.room-id-value {-->
<!--  font-size: 1.3rem;-->
<!--  letter-spacing: 2px;-->
<!--}-->
<!--.error-msg {-->
<!--  margin-top: 10px;-->
<!--  color: #cc3300;-->
<!--}-->
<!--.success-msg {-->
<!--  margin-top: 10px;-->
<!--  color: #1a8835;-->
<!--  font-weight: bold;-->
<!--}-->
<!--.history-rooms {-->
<!--  margin-top: 20px;-->
<!--  border-top: 1px dashed #eed6b4;-->
<!--  padding-top: 12px;-->
<!--}-->
<!--.history-rooms h3 {-->
<!--  font-size: 0.9rem;-->
<!--  color: #666;-->
<!--  margin-bottom: 8px;-->
<!--}-->
<!--.history-room-item {-->
<!--  display: flex;-->
<!--  justify-content: space-between;-->
<!--  align-items: center;-->
<!--  margin-bottom: 6px;-->
<!--  padding: 6px;-->
<!--  background: #fffbe9;-->
<!--  border-radius: 6px;-->
<!--}-->
<!--.small-btn {-->
<!--  padding: 4px 8px;-->
<!--  font-size: 0.8rem;-->
<!--  margin: 0;-->
<!--}-->
<!--.clear-btn {-->
<!--  margin-top: 10px;-->
<!--  background: #ffebe3;-->
<!--  color: #cc3300;-->
<!--  font-size: 0.8rem;-->
<!--  padding: 4px 8px;-->
<!--}-->
<!--.user-info-section, .room-status-section {-->
<!--  margin-top: 30px;-->
<!--  padding: 15px;-->
<!--  background: #fff;-->
<!--  border-radius: 16px;-->
<!--  text-align: center;-->
<!--}-->
<!--.room-status-section .room-info {-->
<!--  margin-top: 10px;-->
<!--}-->
<!--.room-status-section .room-full {-->
<!--  color: #1a8835;-->
<!--  font-weight: bold;-->
<!--  margin-top: 10px;-->
<!--  padding: 8px;-->
<!--  background-color: #e4ffe4;-->
<!--  border-radius: 6px;-->
<!--}-->
<!--.connection-status {-->
<!--  margin-top: 20px;-->
<!--  display: flex;-->
<!--  justify-content: space-between;-->
<!--  align-items: center;-->
<!--  padding: 10px 15px;-->
<!--  background: #fff;-->
<!--  border-radius: 16px;-->
<!--}-->
<!--.status-indicator {-->
<!--  display: inline-block;-->
<!--  padding: 6px 10px;-->
<!--  border-radius: 6px;-->
<!--  font-size: 0.9rem;-->
<!--}-->
<!--.status-indicator.connected {-->
<!--  background: #e4ffe4;-->
<!--  color: #1a8835;-->
<!--}-->
<!--.status-indicator.disconnected {-->
<!--  background: #ffe4e4;-->
<!--  color: #cc3300;-->
<!--}-->
<!--.status-indicator.connecting {-->
<!--  background: #fffde4;-->
<!--  color: #8f7600;-->
<!--}-->
<!--.status-indicator.error {-->
<!--  background: #ff9e9e;-->
<!--  color: #990000;-->
<!--}-->
<!--.connection-btn {-->
<!--  padding: 4px 8px;-->
<!--  font-size: 0.8rem;-->
<!--}-->
<!--.debug-log {-->
<!--  margin-top: 20px;-->
<!--  padding: 15px;-->
<!--  background: #fff;-->
<!--  border-radius: 16px;-->
<!--}-->
<!--.debug-log h3 {-->
<!--  font-size: 1rem;-->
<!--  margin-bottom: 10px;-->
<!--}-->
<!--.log-container {-->
<!--  max-height: 200px;-->
<!--  overflow-y: auto;-->
<!--  border: 1px solid #eee;-->
<!--  padding: 8px;-->
<!--  border-radius: 4px;-->
<!--  font-family: monospace;-->
<!--  font-size: 0.85rem;-->
<!--}-->
<!--.log-entry {-->
<!--  margin-bottom: 4px;-->
<!--  border-bottom: 1px solid #f0f0f0;-->
<!--  padding-bottom: 4px;-->
<!--}-->
<!--.log-time {-->
<!--  color: #666;-->
<!--  margin-right: 5px;-->
<!--}-->
<!--.log-type {-->
<!--  font-weight: bold;-->
<!--  margin-right: 5px;-->
<!--}-->
<!--.log-type.error {-->
<!--  color: #cc3300;-->
<!--}-->
<!--.log-type.success {-->
<!--  color: #1a8835;-->
<!--}-->
<!--.log-content {-->
<!--  word-break: break-word;-->
<!--}-->
<!--</style>-->