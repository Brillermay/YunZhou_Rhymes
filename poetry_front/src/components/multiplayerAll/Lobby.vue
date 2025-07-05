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
        </div>
      </div>
    </div>
  </template>
  
  <script setup>
  import { ref } from "vue";
  import axios from "axios";
  import {onMounted} from "vue";

  onMounted(()=>{
    connect();
  })
  const createdRoomId = ref("");
  const creating = ref(false);
  
  const joinRoomId = ref("");
  const joining = ref(false);
  const joinError = ref("");
  const joinedSuccess = ref(false);

  //websocket
  let websocket=ref(null);
  let isConnected=ref(false);
  let connectionStatus=ref('disconnected');
  let connectionStatusText=ref('未连接');
  let messageLog=ref([]);


  // async function createRoom() {
  //   creating.value = true;
  //   createdRoomId.value = "";
  //   try {
  //     // 假设后端创建房间API：POST /api/room/create
  //     const res = await axios.post("/api/room/create");
  //     createdRoomId.value = res.data.roomId; // 假设返回{ roomId: "xxxx" }
  //   } catch (err) {
  //     createdRoomId.value = "创建失败，请重试";
  //   }
  //   creating.value = false;
  // }
  
  // 加入房间逻辑
  async function joinRoom() {
    joinError.value = "";
    joinedSuccess.value = false;
    if (!joinRoomId.value) {
      joinError.value = "请输入房间号";
      return;
    }
    joining.value = true;
    try {
      // 假设后端加入房间API：POST /api/room/join，body:{ roomId }
      const res = await axios.post("/api/room/join", { roomId: joinRoomId.value });
      if (res.data.success) {
        joinedSuccess.value = true;
        joinError.value = "";
        // 此处可跳转到游戏房间页面
      } else {
        joinError.value = res.data.message || "加入失败，请检查房间号";
      }
    } catch (err) {
      joinError.value = "网络错误，请重试";
    }
    joining.value = false;
  }

  //websocket
  async function connect(){
    try {
      // 根据您的后端地址调整WebSocket URL
      const wsUrl = 'ws://localhost:8081/ws/game'; // 请根据实际端口调整
      websocket = new WebSocket(wsUrl);

      websocket.onopen = onOpen;
      websocket.onmessage = onMessage;
      websocket.onclose = onClose;
      websocket.onerror = onError;

      connectionStatus = 'connecting';
      connectionStatusText = '连接中...';

    } catch (error) {
      addLog('error', `连接失败: ${error.message}`);
    }
  }
async function disconnect() {
  if (websocket) {
    websocket.close();
  }
}
async function onOpen(event) {
  isConnected = true;
  connectionStatus = 'connected';
  connectionStatusText = '已连接';
  addLog('success', 'WebSocket连接已建立');
}
async function onMessage(event) {
  try {
    const data = JSON.parse(event.data);
    addLog('received', `收到消息: ${JSON.stringify(data, null, 2)}`);
  } catch (error) {
    addLog('received', `收到消息: ${event.data}`);
  }
}
async function onClose(event) {
  isConnected = false;
  connectionStatus = 'disconnected';
  connectionStatusText = '未连接';
  addLog('info', `连接已关闭: ${event.code} - ${event.reason}`);
}

async function onError(event) {
    addLog('error', `连接错误: ${event}`);
}
async function sendMessage(message) {
  if (websocket && isConnected) {
    const messageStr = JSON.stringify(message);
    websocket.send(messageStr);
    addLog('sent', `发送消息: ${messageStr}`);
  } else {
    addLog('error', 'WebSocket未连接');
  }
}
async function createRoom() {
  const message = {
    type: 'createRoom',
    room: {
      uid: createRoomData.uid.toString()
    }
  };
  sendMessage(message);
}

async function xx(){

}

async function addLog(type, content) {
  const now = new Date();
  const timestamp = now.toLocaleTimeString();
  // messageLog.push({
  //   type,
  //   content,
  //   timestamp
  // });
  //
  // // 限制日志数量
  // if (messageLog.length > 100) {
  //   messageLog.shift();
  // }

  // 自动滚动到底部
  // $nextTick(() => {
  //   const logElement = document.querySelector('.message-log');
  //   if (logElement) {
  //     logElement.scrollTop = logElement.scrollHeight;
  //   }
  // });
}

  // 清空日志
async function clearLog() {

  messageLog = [];
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
  </style>
  