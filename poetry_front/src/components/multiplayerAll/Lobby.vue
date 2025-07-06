<template>
  <div class="lobby-container">
    <!-- 背景装饰 -->
    <div class="background-container">
      <div class="floating-elements">
        <div 
          v-for="(element, index) in floatingElements" 
          :key="index"
          class="floating-element"
          :style="element.style"
        >
          {{ element.symbol }}
        </div>
      </div>
      <div class="gradient-overlay"></div>
    </div>

    <!-- 主要内容 -->
    <div class="content-container">
      <!-- 页面标题 -->
      <div class="header-section">
        <h1 class="main-title">
          <span class="title-icon">🏮</span>
          多人游戏大厅
          <span class="title-decoration">Multiplayer Lobby</span>
        </h1>
        <p class="subtitle">邀三五好友，共赏诗词雅韵；开一室清谈，同品文墨风华。</p>
      </div>

      <!-- 游戏卡片区域 -->
      <div class="cards-container">
        <!-- 创建房间卡片 -->
        <div class="lobby-card create-room-card">
          <div class="card-background">
            <div class="card-pattern"></div>
          </div>
          
          <div class="card-content">
            <div class="card-header">
              <div class="card-icon">
                <span class="icon-symbol">🏛️</span>
              </div>
              <h2 class="card-title">创建房间</h2>
              <div class="card-seal">
                <div class="seal-text">创</div>
              </div>
            </div>
            
            <div class="card-body">
              <button @click="createRoom" :disabled="creating" class="primary-btn">
                <span class="btn-icon">{{ creating ? "⏳" : "🌸" }}</span>
                <span class="btn-text">{{ creating ? "正在创建..." : "创建房间" }}</span>
                <div class="btn-ripple"></div>
              </button>
              
              <div v-if="createdRoomId" class="room-id-section">
                <div class="room-id-header">
                  <span class="room-id-icon">🏮</span>
                  <span class="room-id-label">房间号</span>
                </div>
                <div class="room-id-display">
                  <span class="room-id-value">{{ createdRoomId }}</span>
                </div>
                <button @click="joinCreatedRoom" class="join-btn">
                  <span class="btn-icon">🚪</span>
                  <span class="btn-text">加入房间</span>
                </button>
              </div>
              
              <div v-if="historyRooms.length > 0" class="history-section">
                <h3 class="section-title">
                  <span class="section-icon">📋</span>
                  <span class="section-text">历史雅集</span>
                </h3>
                <div class="history-list">
                  <div v-for="(room, index) in historyRooms" :key="index" class="history-item">
                    <span class="room-number">{{ room }}</span>
                    <button @click="useHistoryRoom(room)" class="history-btn">
                      <span class="btn-icon">🔄</span>
                      <span class="btn-text">重用</span>
                    </button>
                  </div>
                </div>
                <button @click="clearHistoryRooms" class="clear-btn">
                  <span class="btn-icon">🗑️</span>
                  <span class="btn-text">清空记录</span>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- 加入房间卡片 -->
        <div class="lobby-card join-room-card">
          <div class="card-background">
            <div class="card-pattern"></div>
          </div>
          
          <div class="card-content">
            <div class="card-header">
              <div class="card-icon">
                <span class="icon-symbol">🎯</span>
              </div>
              <h2 class="card-title">加入房间</h2>
              <div class="card-seal">
                <div class="seal-text">入</div>
              </div>
            </div>
            
            <div class="card-body">
              <div class="input-section">
                <div class="input-group">
                  <input
                    v-model="joinRoomId"
                    type="text"
                    placeholder="请输入房间号"
                    maxlength="16"
                    class="room-input"
                  />
                  <div class="input-decoration"></div>
                  <div class="input-icon">🔍</div>
                </div>
              </div>
              
              <button @click="joinRoom" :disabled="joining" class="primary-btn">
                <span class="btn-icon">{{ joining ? "⏳" : "🎭" }}</span>
                <span class="btn-text">{{ joining ? "正在加入..." : "加入房间" }}</span>
                <div class="btn-ripple"></div>
              </button>
              
              <div v-if="joinError" class="message error-msg">
                <span class="msg-icon">❌</span>
                <span class="msg-text">{{ joinError }}</span>
              </div>
              
              <div v-if="joinedSuccess" class="message success-msg">
                <span class="msg-icon">✅</span>
                <span class="msg-text">成功加入房间：{{ joinRoomId }}</span>
              </div>
              
              <div v-if="recentJoinedRooms.length > 0" class="history-section">
                <h3 class="section-title">
                  <span class="section-icon">🕐</span>
                  <span class="section-text">近期造访</span>
                </h3>
                <div class="history-list">
                  <div v-for="(room, index) in recentJoinedRooms" :key="index" class="history-item">
                    <span class="room-number">{{ room }}</span>
                    <button @click="useRecentRoom(room)" class="history-btn">
                      <span class="btn-icon">🔄</span>
                      <span class="btn-text">重用</span>
                    </button>
                  </div>
                </div>
                <button @click="clearRecentRooms" class="clear-btn">
                  <span class="btn-icon">🗑️</span>
                  <span class="btn-text">清空记录</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 状态信息区域 -->
      <div class="status-grid">
        <!-- 房间状态 -->
        <div class="status-card room-status" v-if="currentRoom">
          <div class="status-header">
            <div class="status-icon">🏡</div>
            <h3 class="status-title">当前房间</h3>
          </div>
          <div class="status-content">
            <div class="info-item">
              <span class="info-label">房间号</span>
              <span class="info-value">{{ currentRoom }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">人数</span>
              <div class="player-count-display">
                <span class="count-text">{{ roomPlayerCount }}/2</span>
                <div class="progress-container">
                  <div class="progress-bar">
                    <div class="progress-fill" :style="{width: (roomPlayerCount / 2) * 100 + '%'}"></div>
                  </div>
                </div>
              </div>
            </div>
            <div v-if="roomPlayerCount >= 2" class="room-full-notice">
              <div class="notice-content">
                <span class="notice-icon">🎉</span>
                <span class="notice-text">房间已满，即将开始游戏...</span>
              </div>
              <div class="loading-animation">
                <div class="loading-dots">
                  <span></span><span></span><span></span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 用户信息 -->
        <div class="status-card user-info" v-if="userInfo">
          <div class="status-header">
            <div class="status-icon">👤</div>
            <h3 class="status-title">玩家信息</h3>
          </div>
          <div class="status-content">
            <div class="info-item">
              <span class="info-label">玩家ID</span>
              <span class="info-value">{{ userInfo.uid }}</span>
            </div>
            <button @click="clearUserInfo" class="action-btn">
              <span class="btn-icon">🔄</span>
              <span class="btn-text">重置信息</span>
            </button>
          </div>
        </div>

        <!-- 连接状态 -->
        <div class="status-card connection-status">
          <div class="status-header">
            <div class="status-icon">🌐</div>
            <h3 class="status-title">连接状态</h3>
          </div>
          <div class="status-content">
            <div class="connection-display">
              <div class="status-indicator" :class="connectionStatus">
                <div class="status-dot"></div>
                <span class="status-text">{{ connectionStatusText }}</span>
              </div>
              <button @click="handleConnectionToggle" class="connection-btn" :class="isConnected ? 'disconnect' : 'connect'">
                <span class="btn-icon">{{ isConnected ? '🔌' : '🔗' }}</span>
                <span class="btn-text">{{ isConnected ? '断开连接' : '重新连接' }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 调试日志 -->
      <div v-if="showDebugLog" class="debug-section">
        <div class="debug-card">
          <div class="debug-header">
            <div class="debug-icon">🔍</div>
            <h3 class="debug-title">系统日志</h3>
            <button @click="clearLog" class="debug-clear-btn">
              <span class="btn-icon">🗑️</span>
              <span class="btn-text">清空</span>
            </button>
          </div>
          <div class="log-container">
            <div v-for="(log, index) in messageLog" :key="index" class="log-entry" :class="log.type">
              <div class="log-meta">
                <span class="log-time">{{ log.timestamp }}</span>
                <span class="log-type">{{ log.type }}</span>
              </div>
              <div class="log-content">{{ log.content }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onBeforeUnmount } from "vue";
import { useRouter } from 'vue-router';
// 导入storageUtil中的方法
import { saveData, getData, updateData, removeData, hasData, clearAllData } from '../util/storageUtil';

import { isLoggedIn, getCurrentUid ,requireLogin } from '@/utils/auth'

// 获取路由器实例
const router = useRouter();

// 浮动装饰元素
const floatingElements = ref([])

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
const showDebugLog = ref(false); // 这里可以选择是否显示日志！！
const reconnectAttempts = ref(0);
const maxReconnectAttempts = 5;
let reconnectTimer = null;

// 创建浮动装饰元素
const createFloatingElements = () => {
  const symbols = ['诗', '词', '雅', '韵', '文', '墨', '🌸', '🍃', '🌙', '☁️', '🏮', '📜']
  floatingElements.value = []
  
  for (let i = 0; i < 12; i++) {
    floatingElements.value.push({
      symbol: symbols[Math.floor(Math.random() * symbols.length)],
      style: {
        left: `${Math.random() * 100}%`,
        top: `${Math.random() * 100}%`,
        animationDelay: `${Math.random() * 10}s`,
        animationDuration: `${15 + Math.random() * 10}s`
      }
    })
  }
}

onMounted(() => {
  // 创建浮动装饰元素
  createFloatingElements()
  
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

onBeforeUnmount(() => {
  // 清理定时器
  if (reconnectTimer) {
    clearTimeout(reconnectTimer);
  }
  // 关闭WebSocket连接
  if (websocket.value && isConnected.value) {
    disconnect();
  }
});

// 监听房间人数变化，如果达到2人自动跳转
watch(roomPlayerCount, (newCount) => {
  if (newCount >= 2) {
    addLog('info', `房间人数达到${newCount}人，准备开始游戏`);

    // 保存当前房间信息以便在游戏页面使用
    saveData('current_game_room', {
      roomId: currentRoom.value,
      userId: userInfo.value.uid
    });

    // 发送开始游戏的WebSocket消息
    sendMessage({
      type: 'startGame',
      room: {
        roomId: currentRoom.value,
        role1: 'libai',  // 测试用，两个角色都设为libai
        role2: 'libai',
        uid: userInfo.value.uid.toString()
      }
    });

    addLog('info', '发送开始游戏请求');

    // 延迟跳转时间调整为5秒，确保有足够时间处理初始化
    setTimeout(() => {
      router.push('/multiplay_r');
    }, 5000);
  }
});

// 加载存储的历史数据
function loadHistoryData() {
  const savedCreatedRooms = getData(STORAGE_KEYS.CREATED_ROOMS) || [];
  const savedJoinedRooms = getData(STORAGE_KEYS.JOINED_ROOMS) || [];
  historyRooms.value = savedCreatedRooms;
  recentJoinedRooms.value = savedJoinedRooms;

  // 注意：初次进入不自动加入房间
  // 只读取历史记录，但不设置currentRoom
}

// 加载用户信息（增加登录状态检测）
function loadUserInfo() {
  if (isLoggedIn()) {
    // 已登录，获取真实UID
    userInfo.value = {
      uid: getCurrentUid()
    };
    saveData(STORAGE_KEYS.USER_INFO, userInfo.value);
  } else {
    // 未登录，创建临时用户ID
    // requireLogin();
    // router.go(-1);
    userInfo.value = {
      uid: Math.floor(Math.random() * 10000).toString()
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

  if (!isConnected.value) {
    addLog('error', 'WebSocket未连接，请先连接服务器');
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

  if (!isConnected.value) {
    joinError.value = "WebSocket未连接，请先连接服务器";
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
    // 清理之前的连接
    if (websocket.value) {
      try {
        websocket.value.close();
      } catch (e) {
        console.error('关闭之前的连接失败', e);
      }
    }

    connectionStatus.value = 'connecting';
    connectionStatusText.value = '连接中...';

    // 根据您的后端地址调整WebSocket URL
    //const wsUrl = 'ws://localhost:8081/ws/game'; // 请根据实际端口调整

    const wsUrl = 'ws://192.168.181.251:8081/ws/game'; // 请根据实际端口调整

    websocket.value = new WebSocket(wsUrl);

    websocket.value.onopen = onOpen;
    websocket.value.onmessage = onMessage;
    websocket.value.onclose = onClose;
    websocket.value.onerror = onError;

    // 保存连接状态
    saveData(STORAGE_KEYS.WEBSOCKET_STATUS, {
      status: 'connecting',
      text: '连接中...'
    });

    addLog('info', '正在连接WebSocket...');
  } catch (error) {
    connectionStatus.value = 'error';
    connectionStatusText.value = '连接错误';
    addLog('error', `连接失败: ${error.message}`);

    // 重置重连尝试
    resetReconnection();
  }
}

function handleConnectionToggle() {
  if (isConnected.value) {
    disconnect();
  } else {
    // 重置重连尝试并连接
    reconnectAttempts.value = 0;
    connect();
  }
}

async function disconnect() {
  if (websocket.value) {
    try {
      websocket.value.close();
      addLog('info', '主动断开WebSocket连接');
    } catch (error) {
      addLog('error', `断开连接错误: ${error.message}`);
    }
  }

  // 重置状态
  isConnected.value = false;
  connectionStatus.value = 'disconnected';
  connectionStatusText.value = '未连接';

  // 保存连接状态
  saveData(STORAGE_KEYS.WEBSOCKET_STATUS, {
    status: 'disconnected',
    text: '未连接'
  });

  // 清理重连尝试
  resetReconnection();
}

function resetReconnection() {
  reconnectAttempts.value = 0;
  if (reconnectTimer) {
    clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }
}

async function onOpen(event) {
  isConnected.value = true;
  connectionStatus.value = 'connected';
  connectionStatusText.value = '已连接';
  reconnectAttempts.value = 0;

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
    addLog('received', `收到消息: ${JSON.stringify(data)}`);

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

    // 注意：创建房间后不自动加入，需要用户点击"加入该房间"按钮
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

    // 这里初始设置房间人数为1，后续会收到room_update消息更新
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

  // 如果是正常关闭或者已经达到最大重连次数
  if (event.code === 1000 || reconnectAttempts.value >= maxReconnectAttempts) {
    connectionStatus.value = 'disconnected';
    connectionStatusText.value = '未连接';

    if (reconnectAttempts.value >= maxReconnectAttempts) {
      addLog('error', `已达到最大重连次数(${maxReconnectAttempts})，请手动重连`);
    } else {
      addLog('info', `连接已正常关闭: ${event.code}`);
    }
  } else {
    // 尝试重连
    connectionStatus.value = 'connecting';
    connectionStatusText.value = `重连中(${reconnectAttempts.value + 1}/${maxReconnectAttempts})...`;

    reconnectAttempts.value++;
    addLog('info', `连接已关闭，${reconnectAttempts.value}秒后尝试重连...`);

    // 设置重连定时器，延迟时间随重连次数增加
    if (reconnectTimer) clearTimeout(reconnectTimer);
    reconnectTimer = setTimeout(() => {
      connect();
    }, reconnectAttempts.value * 1000);
  }

  // 保存连接状态
  saveData(STORAGE_KEYS.WEBSOCKET_STATUS, {
    status: connectionStatus.value,
    text: connectionStatusText.value
  });
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

<style lang="scss" scoped>
// ============ 基础样式 ============
* {
  box-sizing: border-box;
}

.lobby-container {
  min-height: 100vh;
  background: 
    linear-gradient(135deg, #e8eceb 0%, #d5d8d6 60%, #f3efe6 100%),
    radial-gradient(circle at 60% 40%, rgba(120,130,120,0.08) 0%, transparent 70%);
  background-size: cover, cover;
  position: relative;
  overflow: hidden;
}

// ============ 背景装饰 ============
.background-container {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%; z-index: 1;
}

.floating-elements {
  position: absolute;
  width: 100%;
  height: 100%;
}

.floating-element {
  position: absolute;
  font-size: 2rem;
  color: rgba(80, 80, 80, 0.15);
  font-family: 'STKaiti', 'KaiTi', '楷体', serif;
  font-weight: bold;
  text-shadow: 0 2px 8px rgba(60,60,60,0.08);
  animation: floatUpDown linear infinite;
  pointer-events: none;
  user-select: none;
}

.gradient-overlay {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: 
    radial-gradient(circle at 20% 20%, rgba(120,130,120,0.06) 0%, transparent 60%),
    radial-gradient(circle at 80% 80%, rgba(120,130,120,0.04) 0%, transparent 60%);
}

// ============ 内容容器 ============
.content-container {
  position: relative;
  z-index: 10;
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

// ============ 页面标题 ============
.header-section {
  text-align: center;
  margin-bottom: 3rem;
}

.main-title {
  font-size: 2.8rem;
  font-weight: 900;
  color: #3e3a2f;
  margin: 0 0 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  
  .title-icon {
    font-size: 2.2rem;
    background: linear-gradient(45deg, #b6a179, #e2c391);
    background-clip: text;
    -webkit-background-clip: text;
    color: transparent;
    animation: iconGlow 3s ease-in-out infinite;
  }
  
  .title-decoration {
    font-size: 1rem;
    color: #a89c7d;
    font-weight: 400;
    margin-left: 1rem;
    opacity: 0;
    animation: fadeIn 1s ease-out 1s forwards;
  }
}

.subtitle {
  font-size: 1.1rem;
  color: #7c715a;
  margin: 0;
  font-weight: 300;
  letter-spacing: 0.03rem;
  animation: slideUp 1s ease-out 0.5s both;
}

// ============ 卡片容器 ============
.cards-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(450px, 1fr));
  gap: 2rem;
  margin-bottom: 3rem;
}

.lobby-card {
  position: relative;
  background: 
    linear-gradient(135deg, #e3e5e1 0%, #c9ccc8 100%);
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 
    0 8px 32px rgba(80, 80, 80, 0.12),
    0 2px 8px rgba(80, 80, 80, 0.06);
  transition: all 0.6s cubic-bezier(0.23, 1, 0.32, 1);
  
  &:hover {
    transform: translateY(-8px);
    box-shadow: 
      0 16px 48px rgba(80, 80, 80, 0.16),
      0 8px 20px rgba(200, 180, 140, 0.12);
  }
}

// ============ 卡片背景 ============
.card-background {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  z-index: 1;
}

.card-pattern {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: 
    repeating-linear-gradient(
      45deg,
      transparent,
      transparent 12px,
      rgba(180, 170, 140, 0.05) 12px,
      rgba(180, 170, 140, 0.05) 24px
    );
}

// ============ 卡片内容 ============
.card-content {
  position: relative;
  z-index: 10;
  padding: 0;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  position: relative;
  padding: 2rem;
  background: linear-gradient(135deg, 
    rgba(182, 161, 121, 0.15) 0%, 
    rgba(226, 195, 145, 0.10) 100%
  );
  border-bottom: 1px solid rgba(180, 170, 140, 0.2);
  display: flex;
  align-items: center;
  gap: 1rem;
}

.card-icon {
  width: 60px;
  height: 60px;
  background: linear-gradient(135deg, #b6a179, #e2c391);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.8rem;
  color: white;
  box-shadow: 0 4px 16px rgba(182, 161, 121, 0.3);
}

.card-title {
  font-size: 1.6rem;
  font-weight: 700;
  color: #3e3a2f;
  margin: 0;
  flex: 1;
}

.card-seal {
  position: absolute;
  top: 1.5rem;
  right: 1.5rem;
}

.seal-text {
  width: 40px;
  height: 40px;
  background: linear-gradient(45deg, #e2c391, #b6a179);
  border: 2px solid #b6a179;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-weight: 700;
  font-size: 1.1rem;
  font-family: 'KaiTi', '楷体', serif;
  transform: rotate(-8deg);
  box-shadow: 0 3px 12px rgba(182, 161, 121, 0.2);
}

.card-body {
  padding: 2rem;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

// ============ 按钮样式 ============
.primary-btn {
  position: relative;
  width: 100%;
  padding: 1rem 2rem;
  background: linear-gradient(135deg, #b6a179, #e2c391);
  border: none;
  border-radius: 16px;
  font-size: 1.1rem;
  font-weight: 600;
  color: white;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.23, 1, 0.32, 1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.8rem;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(182, 161, 121, 0.2);
  
  &:hover:not(:disabled) {
    background: linear-gradient(135deg, #e2c391, #b6a179);
    transform: translateY(-2px);
    box-shadow: 0 8px 32px rgba(182, 161, 121, 0.3);
  }
  
  &:disabled {
    opacity: 0.6;
    cursor: not-allowed;
    transform: none;
  }
}

.btn-ripple {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  background: radial-gradient(circle, rgba(255,255,255,0.3) 0%, transparent 70%);
  transform: scale(0);
  opacity: 0;
  transition: all 0.6s ease;
}

.primary-btn:active .btn-ripple {
  transform: scale(1);
  opacity: 1;
}

.join-btn {
  width: 100%;
  padding: 0.8rem 1.5rem;
  background: linear-gradient(135deg, #4facfe, #00f2fe);
  border: none;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 600;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  
  &:hover {
    background: linear-gradient(135deg, #00f2fe, #4facfe);
    transform: translateY(-2px);
    box-shadow: 0 6px 24px rgba(79, 172, 254, 0.3);
  }
}

.history-btn, .action-btn {
  padding: 0.5rem 1rem;
  background: rgba(182, 161, 121, 0.1);
  border: 1px solid rgba(182, 161, 121, 0.3);
  border-radius: 8px;
  font-size: 0.9rem;
  color: #3e3a2f;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  
  &:hover {
    background: rgba(182, 161, 121, 0.2);
    transform: translateY(-1px);
  }
}

.clear-btn {
  padding: 0.6rem 1.2rem;
  background: rgba(255, 87, 87, 0.1);
  border: 1px solid rgba(255, 87, 87, 0.3);
  border-radius: 10px;
  font-size: 0.9rem;
  color: #cc3300;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  align-self: flex-start;
  
  &:hover {
    background: rgba(255, 87, 87, 0.2);
    transform: translateY(-1px);
  }
}

// ============ 输入框样式 ============
.input-section {
  margin-bottom: 1rem;
}

.input-group {
  position: relative;
}

.room-input {
  width: 100%;
  padding: 1rem 3rem 1rem 1.5rem;
  border: 2px solid rgba(182, 161, 121, 0.3);
  border-radius: 12px;
  font-size: 1rem;
  background: rgba(255, 255, 255, 0.8);
  color: #3e3a2f;
  transition: all 0.3s ease;
  
  &::placeholder {
    color: rgba(124, 113, 90, 0.6);
  }
  
  &:focus {
    outline: none;
    border-color: #b6a179;
    background: rgba(255, 255, 255, 0.95);
    box-shadow: 0 0 0 3px rgba(182, 161, 121, 0.1);
  }
}

.input-decoration {
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 2px;
  background: linear-gradient(90deg, #b6a179, #e2c391);
  transform: scaleX(0);
  transition: transform 0.3s ease;
}

.room-input:focus + .input-decoration {
  transform: scaleX(1);
}

.input-icon {
  position: absolute;
  right: 1rem;
  top: 50%;
  transform: translateY(-50%);
  font-size: 1.2rem;
  color: rgba(182, 161, 121, 0.6);
}

// ============ 房间号显示 ============
.room-id-section {
  padding: 1.5rem;
  background: linear-gradient(135deg, 
    rgba(182, 161, 121, 0.08) 0%, 
    rgba(226, 195, 145, 0.05) 100%
  );
  border-radius: 16px;
  border: 1px solid rgba(182, 161, 121, 0.2);
  text-align: center;
}

.room-id-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.room-id-icon {
  font-size: 1.2rem;
}

.room-id-label {
  font-size: 1rem;
  color: #7c715a;
  font-weight: 600;
}

.room-id-display {
  margin-bottom: 1.5rem;
}

.room-id-value {
  font-size: 2rem;
  font-weight: 700;
  color: #b6a179;
  font-family: 'Courier New', monospace;
  letter-spacing: 0.2rem;
  text-shadow: 0 2px 4px rgba(182, 161, 121, 0.2);
  background: linear-gradient(135deg, #b6a179, #e2c391);
  background-clip: text;
  -webkit-background-clip: text;
  color: transparent;
}

// ============ 消息提示 ============
.message {
  padding: 1rem;
  border-radius: 12px;
  display: flex;
  align-items: center;
  gap: 0.8rem;
  font-weight: 500;
  margin-top: 1rem;
}

.error-msg {
  background: linear-gradient(135deg, 
    rgba(255, 87, 87, 0.1) 0%, 
    rgba(255, 87, 87, 0.05) 100%
  );
  border: 1px solid rgba(255, 87, 87, 0.3);
  color: #cc3300;
}

.success-msg {
  background: linear-gradient(135deg, 
    rgba(76, 217, 100, 0.1) 0%, 
    rgba(76, 217, 100, 0.05) 100%
  );
  border: 1px solid rgba(76, 217, 100, 0.3);
  color: #1a8835;
}

.msg-icon {
  font-size: 1.2rem;
}

// ============ 历史记录 ============
.history-section {
  padding-top: 1.5rem;
  border-top: 1px solid rgba(182, 161, 121, 0.2);
}

.section-title {
  font-size: 1rem;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #7c715a;
  font-weight: 600;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 0.8rem;
  margin-bottom: 1rem;
}

.history-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.8rem 1rem;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 10px;
  border: 1px solid rgba(182, 161, 121, 0.1);
  transition: all 0.3s ease;
  
  &:hover {
    background: rgba(255, 255, 255, 0.8);
    transform: translateX(4px);
  }
}

.room-number {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  color: #3e3a2f;
  letter-spacing: 0.1rem;
}

// ============ 状态网格 ============
.status-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-bottom: 2rem;
}

.status-card {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.9) 0%, 
    rgba(255, 255, 255, 0.7) 100%
  );
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 6px 24px rgba(80, 80, 80, 0.08);
  border: 1px solid rgba(182, 161, 121, 0.2);
  transition: all 0.3s ease;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(80, 80, 80, 0.12);
  }
}

.status-header {
  padding: 1.5rem;
  background: linear-gradient(135deg, #b6a179, #e2c391);
  color: white;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.status-icon {
  font-size: 1.5rem;
}

.status-title {
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0;
}

.status-content {
  padding: 1.5rem;
}

.info-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
  padding: 0.8rem;
  background: rgba(182, 161, 121, 0.05);
  border-radius: 10px;
  border: 1px solid rgba(182, 161, 121, 0.1);
}

.info-label {
  font-weight: 600;
  color: #7c715a;
}

.info-value {
  font-family: 'Courier New', monospace;
  font-weight: 600;
  color: #3e3a2f;
}

// ============ 玩家数量显示 ============
.player-count-display {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.count-text {
  font-family: 'Courier New', monospace;
  font-weight: 700;
  color: #b6a179;
  font-size: 1.1rem;
}

.progress-container {
  flex: 1;
  max-width: 120px;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: rgba(182, 161, 121, 0.2);
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #b6a179, #e2c391);
  transition: width 0.5s ease;
  border-radius: 4px;
}

// ============ 房间满员提示 ============
.room-full-notice {
  background: linear-gradient(135deg, 
    rgba(76, 217, 100, 0.1) 0%, 
    rgba(76, 217, 100, 0.05) 100%
  );
  border: 1px solid rgba(76, 217, 100, 0.3);
  border-radius: 12px;
  padding: 1.5rem;
  text-align: center;
  margin-top: 1rem;
}

.notice-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.8rem;
  margin-bottom: 1rem;
}

.notice-icon {
  font-size: 1.5rem;
}

.notice-text {
  font-weight: 600;
  color: #1a8835;
}

.loading-animation {
  display: flex;
  justify-content: center;
}

.loading-dots {
  display: flex;
  gap: 0.3rem;
  
  span {
    width: 6px;
    height: 6px;
    background: #1a8835;
    border-radius: 50%;
    animation: loading 1.4s infinite ease-in-out;
    
    &:nth-child(1) { animation-delay: -0.32s; }
    &:nth-child(2) { animation-delay: -0.16s; }
    &:nth-child(3) { animation-delay: 0s; }
  }
}

// ============ 连接状态 ============
.connection-display {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 0.8rem;
  padding: 0.8rem 1rem;
  border-radius: 10px;
  font-weight: 500;
  flex: 1;
}

.status-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  animation: pulse 2s infinite;
}

.status-indicator.connected {
  background: rgba(76, 217, 100, 0.1);
  color: #1a8835;
  border: 1px solid rgba(76, 217, 100, 0.3);
  
  .status-dot {
    background: #1a8835;
  }
}

.status-indicator.disconnected {
  background: rgba(255, 87, 87, 0.1);
  color: #cc3300;
  border: 1px solid rgba(255, 87, 87, 0.3);
  
  .status-dot {
    background: #cc3300;
  }
}

.status-indicator.connecting {
  background: rgba(255, 193, 7, 0.1);
  color: #996600;
  border: 1px solid rgba(255, 193, 7, 0.3);
  
  .status-dot {
    background: #996600;
  }
}

.status-indicator.error {
  background: rgba(255, 87, 87, 0.2);
  color: #cc0000;
  border: 1px solid rgba(255, 87, 87, 0.4);
  
  .status-dot {
    background: #cc0000;
  }
}

.connection-btn {
  padding: 0.8rem 1.2rem;
  border: none;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  
  &.connect {
    background: linear-gradient(135deg, #4cd964, #5ac8fa);
    color: white;
  }
  
  &.disconnect {
    background: linear-gradient(135deg, #ff5757, #ff6b6b);
    color: white;
  }
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
  }
}

// ============ 调试日志 ============
.debug-section {
  margin-top: 2rem;
}

.debug-card {
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.9) 0%, 
    rgba(255, 255, 255, 0.7) 100%
  );
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(80, 80, 80, 0.1);
  border: 1px solid rgba(182, 161, 121, 0.2);
}

.debug-header {
  padding: 1.5rem;
  background: linear-gradient(135deg, #36454f, #2c3e50);
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.debug-title {
  font-size: 1.2rem;
  font-weight: 600;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 0.8rem;
}

.debug-clear-btn {
  padding: 0.5rem 1rem;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.3);
  border-radius: 8px;
  color: white;
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  
  &:hover {
    background: rgba(255, 255, 255, 0.2);
  }
}

.log-container {
  max-height: 300px;
  overflow-y: auto;
  padding: 1rem;
  background: #1a1a1a;
  font-family: 'Courier New', monospace;
  font-size: 0.85rem;
  line-height: 1.4;
  
  &::-webkit-scrollbar {
    width: 8px;
  }
  
  &::-webkit-scrollbar-track {
    background: #2a2a2a;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #555;
    border-radius: 4px;
  }
}

.log-entry {
  margin-bottom: 0.8rem;
  padding: 0.8rem;
  border-radius: 8px;
  border-left: 4px solid transparent;
  background: rgba(255, 255, 255, 0.02);
  transition: all 0.3s ease;
  
  &:hover {
    background: rgba(255, 255, 255, 0.05);
  }
  
  &.error {
    border-left-color: #ff5757;
    background: rgba(255, 87, 87, 0.05);
  }
  
  &.success {
    border-left-color: #4cd964;
    background: rgba(76, 217, 100, 0.05);
  }
  
  &.info {
    border-left-color: #4a90e2;
    background: rgba(74, 144, 226, 0.05);
  }
  
  &.sent {
    border-left-color: #ffc107;
    background: rgba(255, 193, 7, 0.05);
  }
  
  &.received {
    border-left-color: #9c27b0;
    background: rgba(156, 39, 176, 0.05);
  }
}

.log-meta {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 0.5rem;
}

.log-time {
  color: #888;
  font-size: 0.8rem;
}

.log-type {
  padding: 0.2rem 0.6rem;
  border-radius: 4px;
  font-size: 0.8rem;
  font-weight: 600;
  text-transform: uppercase;
  
  .log-entry.error & {
    background: #ff5757;
    color: white;
  }
  
  .log-entry.success & {
    background: #4cd964;
    color: white;
  }
  
  .log-entry.info & {
    background: #4a90e2;
    color: white;
  }
  
  .log-entry.sent & {
    background: #ffc107;
    color: #333;
  }
  
  .log-entry.received & {
    background: #9c27b0;
    color: white;
  }
}

.log-content {
  color: #e0e0e0;
  word-break: break-word;
}

// ============ 动画定义 ============
@keyframes floatUpDown {
  0% {
    transform: translateY(100vh) rotate(0deg);
    opacity: 0;
  }
  10% {
    opacity: 1;
  }
  90% {
    opacity: 1;
  }
  100% {
    transform: translateY(-100px) rotate(360deg);
    opacity: 0;
  }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.1);
  }
}

@keyframes loading {
  0%, 80%, 100% { 
    transform: scale(0); 
  }
  40% { 
    transform: scale(1); 
  }
}

@keyframes iconGlow {
  0%, 100% { 
    filter: drop-shadow(0 0 5px rgba(182, 161, 121, 0.3));
  }
  50% { 
    filter: drop-shadow(0 0 15px rgba(182, 161, 121, 0.6));
  }
}

// ============ 响应式设计 ============
@media (max-width: 768px) {
  .content-container {
    padding: 1rem;
  }
  
  .main-title {
    font-size: 2rem;
    flex-direction: column;
    gap: 0.5rem;
    
    .title-decoration {
      margin-left: 0;
      font-size: 0.9rem;
    }
  }
  
  .subtitle {
    font-size: 1rem;
  }
  
  .cards-container {
    grid-template-columns: 1fr;
    gap: 1.5rem;
  }
  
  .status-grid {
    grid-template-columns: 1fr;
  }
  
  .connection-display {
    flex-direction: column;
    gap: 1rem;
  }
  
  .player-count-display {
    flex-direction: column;
    gap: 0.5rem;
    align-items: flex-start;
  }
  
  .progress-container {
    width: 100%;
    max-width: none;
  }
}

@media (max-width: 480px) {
  .content-container {
    padding: 0.8rem;
  }
  
  .main-title {
    font-size: 1.8rem;
  }
  
  .cards-container {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
  
  .card-header {
    padding: 1.5rem;
  }
  
  .card-body {
    padding: 1.5rem;
  }
  
  .card-icon {
    width: 50px;
    height: 50px;
    font-size: 1.5rem;
  }
  
  .card-title {
    font-size: 1.4rem;
  }
  
  .room-id-value {
    font-size: 1.5rem;
  }
  
  .status-header {
    padding: 1rem;
  }
  
  .status-content {
    padding: 1rem;
  }
}
</style>