<template>
  <div class="websocket-test">
    <div class="header">
      <h2>WebSocket 游戏测试面板</h2>
      <div class="connection-status">
        <span :class="['status-indicator', connectionStatus]"></span>
        <span>{{ connectionStatusText }}</span>
      </div>
    </div>

    <!-- 连接控制 -->
    <div class="section">
      <h3>连接控制</h3>
      <div class="controls">
        <button @click="connect" :disabled="isConnected" class="btn btn-primary">
          连接WebSocket
        </button>
        <button @click="disconnect" :disabled="!isConnected" class="btn btn-secondary">
          断开连接
        </button>
      </div>
      <div class="ws-debug">
        <span>WebSocket URL: </span>
        <input v-model="wsUrl" style="width: 350px;" />
      </div>
    </div>

    <!-- 常用报文快捷填充 -->
    <div class="section">
      <h3>一键填充常用消息</h3>
      <div class="form-group">
        <button @click="fillRoundEndTemplate" class="btn btn-info">填充 RoundEnd 玩家1</button>
        <button @click="fillRoundEndTemplate2" class="btn btn-info">填充 RoundEnd 玩家2</button>
        <button @click="fillJoinRoomTemplate" class="btn btn-info">填充 JoinRoom</button>
        <button @click="fillStartGameTemplate" class="btn btn-info">填充 StartGame</button>
      </div>
    </div>

    <!-- RoundEnd 专用测试 -->
    <div class="section">
      <h3>RoundEnd 回合结束测试</h3>
      <div class="form-group">
        <label>roomId:</label>
        <input v-model="roundEndData.roomId" type="text" placeholder="房间ID">
        <label>uid1:</label>
        <input v-model="roundEndData.uid1" type="number" placeholder="玩家1ID">
        <label>cardList1 (英文逗号分隔):</label>
        <input v-model="roundEndData.cardList1" type="text" placeholder="如: bird,fire,mountain">
        <button @click="sendRoundEnd" :disabled="!isConnected" class="btn btn-warning">发送 RoundEnd</button>
      </div>
    </div>

    <!-- 创建房间 -->
    <div class="section">
      <h3>创建房间</h3>
      <div class="form-group">
        <label>用户ID:</label>
        <input v-model="createRoomData.uid" type="number" placeholder="输入用户ID">
        <button @click="createRoom" :disabled="!isConnected" class="btn btn-success">
          创建房间
        </button>
      </div>
    </div>

    <!-- 加入房间 -->
    <div class="section">
      <h3>加入房间</h3>
      <div class="form-group">
        <label>房间ID:</label>
        <input v-model="joinRoomData.roomId" type="text" placeholder="输入房间ID">
        <label>用户ID:</label>
        <input v-model="joinRoomData.uid" type="number" placeholder="输入用户ID">
        <button @click="joinRoom" :disabled="!isConnected" class="btn btn-success">
          加入房间
        </button>
      </div>
    </div>

    <!-- 开始游戏 -->
    <div class="section">
      <h3>开始游戏</h3>
      <div class="form-group">
        <label>房间ID:</label>
        <input v-model="startGameData.roomId" type="text" placeholder="输入房间ID">
        <label>角色1:</label>
        <input v-model="startGameData.role1" type="text" placeholder="输入角色1">
        <label>角色2:</label>
        <input v-model="startGameData.role2" type="text" placeholder="输入角色2">
        <button @click="startGame" :disabled="!isConnected" class="btn btn-success">
          开始游戏
        </button>
      </div>
    </div>

    <!-- 合成卡牌 -->
    <div class="section">
      <h3>合成卡牌</h3>
      <div class="form-group">
        <label>用户ID:</label>
        <input v-model="synthesizeData.uid" type="number" placeholder="输入用户ID">
        <label>卡牌A:</label>
        <input v-model="synthesizeData.cardA" type="text" placeholder="输入卡牌A名称">
        <label>卡牌B:</label>
        <input v-model="synthesizeData.cardB" type="text" placeholder="输入卡牌B名称">
        <label>卡牌C:</label>
        <input v-model="synthesizeData.cardC" type="text" placeholder="输入卡牌C名称">
        <button @click="synthesizeCards" :disabled="!isConnected" class="btn btn-warning">
          合成卡牌
        </button>
      </div>
    </div>

    <!-- 自定义消息 -->
    <div class="section">
      <h3>自定义消息</h3>
      <div class="form-group">
        <label>JSON消息:</label>
        <textarea v-model="customMessage" rows="4" placeholder="输入自定义JSON消息"></textarea>
        <button @click="sendCustomMessage" :disabled="!isConnected" class="btn btn-info">
          发送自定义消息
        </button>
      </div>
    </div>

    <!-- 消息日志 -->
    <div class="section">
      <h3>消息日志</h3>
      <div class="message-log">
        <div v-for="(message, index) in messageLog" :key="index"
             :class="['message-item', message.type]"
             @dblclick="showRaw(index)">
          <span class="timestamp">{{ message.timestamp }}</span>
          <span class="message-type">[{{ message.type }}]</span>
          <span class="message-content">
            {{ message.content }}
            <span v-if="message.raw && message.showRaw" style="display:block;white-space:pre;color:#888;background:#f6f6f6;margin-top:4px;font-size:12px">
              {{ message.raw }}
            </span>
          </span>
        </div>
      </div>
      <button @click="clearLog" class="btn btn-secondary">清空日志</button>
      <p style="color:#666;font-size:12px;margin-top:10px;">* 双击日志项可展开/收起原始JSON</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'GameBoard',
  data() {
    return {
      websocket: null,
      isConnected: false,
      connectionStatus: 'disconnected',
      connectionStatusText: '未连接',
      wsUrl: 'ws://localhost:8081/ws/game',
      // RoundEnd表单
      roundEndData: {
        roomId: '',
        uid1: '',
        cardList1: '',
      },
      // 其它表单数据
      createRoomData: { uid: 1 },
      joinRoomData: { roomId: '', uid: 2 },
      startGameData: { roomId: '', role1: '战士', role2: '法师' },
      synthesizeData: { uid: 1, cardA: 'FireCard', cardB: 'WaterCard', cardC: 'EarthCard' },
      customMessage: '{\n  "type": "test",\n  "room": {\n    "uid": "1"\n  }\n}',
      messageLog: [],
    }
  },
  methods: {
    connect() {
      try {
        if (this.websocket && this.isConnected) {
          this.addLog('info', 'WebSocket已连接');
          return;
        }
        this.websocket = new window.WebSocket(this.wsUrl);
        this.websocket.onopen = this.onOpen;
        this.websocket.onmessage = this.onMessage;
        this.websocket.onclose = this.onClose;
        this.websocket.onerror = this.onError;
        this.connectionStatus = 'connecting';
        this.connectionStatusText = '连接中...';
        this.addLog('info', '正在连接到: ' + this.wsUrl);
      } catch (error) {
        this.addLog('error', `连接失败: ${error.message}`);
      }
    },
    disconnect() {
      if (this.websocket) {
        this.websocket.close();
      }
    },
    onOpen() {
      this.isConnected = true;
      this.connectionStatus = 'connected';
      this.connectionStatusText = '已连接';
      this.addLog('success', 'WebSocket连接已建立');
    },
    onMessage(event) {
      try {
        const data = JSON.parse(event.data);
        this.addLog('received', `收到: ${JSON.stringify(data, null, 2)}`, event.data);
      } catch (error) {
        this.addLog('received', `收到: ${event.data}`, event.data);
      }
    },
    onClose(event) {
      this.isConnected = false;
      this.connectionStatus = 'disconnected';
      this.connectionStatusText = '未连接';
      this.addLog('info', `连接已关闭: ${event.code} - ${event.reason}`);
    },
    onError(event) {
      this.addLog('error', `连接错误: ${event && event.message ? event.message : ''}`);
    },
    sendMessage(message) {
      if (this.websocket && this.isConnected) {
        const messageStr = JSON.stringify(message);
        this.websocket.send(messageStr);
        this.addLog('sent', `发送: ${messageStr}`, messageStr);
      } else {
        this.addLog('error', 'WebSocket未连接');
      }
    },
    // 发送RoundEnd报文
    sendRoundEnd() {
      const cards = this.roundEndData.cardList1.split(',').map(s => s.trim()).filter(Boolean);
      const message = {
        type: 'RoundEnd',
        room: {
          roomId: this.roundEndData.roomId,
          uid1: this.roundEndData.uid1 + '',
          cardList1: cards,
        },
      };
      this.sendMessage(message);
      // 同步填充到自定义区
      this.customMessage = JSON.stringify(message, null, 2);
    },
    // 一键填充 RoundEnd 玩家1
    fillRoundEndTemplate() {
      this.roundEndData.roomId = 'aaaabbbbccccdddd';
      this.roundEndData.uid1 = '1';
      this.roundEndData.cardList1 = 'bird,fire,mountain';
      this.customMessage = JSON.stringify({
        type: 'RoundEnd',
        room: { roomId: 'aaaabbbbccccdddd', uid1: '1', cardList1: ["bird", "fire", "mountain"] }
      }, null, 2);
    },
    // 一键填充 RoundEnd 玩家2
    fillRoundEndTemplate2() {
      this.roundEndData.roomId = 'aaaabbbbccccdddd';
      this.roundEndData.uid1 = '2';
      this.roundEndData.cardList1 = 'water,earth,wind';
      this.customMessage = JSON.stringify({
        type: 'RoundEnd',
        room: { roomId: 'aaaabbbbccccdddd', uid1: '2', cardList1: ["water", "earth", "wind"] }
      }, null, 2);
    },
    // 一键填充 JoinRoom
    fillJoinRoomTemplate() {
      this.customMessage = JSON.stringify({
        type: "joinRoom",
        room: { roomId: "aaaabbbbccccdddd", uid: "2" }
      }, null, 2);
    },
    // 一键填充 StartGame
    fillStartGameTemplate() {
      this.customMessage = JSON.stringify({
        type: "startGame",
        room: { roomId: "aaaabbbbccccdddd", role1: "战士", role2: "法师" }
      }, null, 2);
    },
    // 创建房间
    createRoom() {
      const message = {
        type: 'createRoom',
        room: {
          uid: this.createRoomData.uid.toString()
        }
      };
      this.sendMessage(message);
      this.customMessage = JSON.stringify(message, null, 2);
    },
    // 加入房间
    joinRoom() {
      const message = {
        type: 'joinRoom',
        room: {
          roomId: this.joinRoomData.roomId,
          uid: this.joinRoomData.uid.toString()
        }
      };
      this.sendMessage(message);
      this.customMessage = JSON.stringify(message, null, 2);
    },
    // 开始游戏
    startGame() {
      const message = {
        type: 'startGame',
        room: {
          roomId: this.startGameData.roomId,
          role1: this.startGameData.role1,
          role2: this.startGameData.role2
        }
      };
      this.sendMessage(message);
      this.customMessage = JSON.stringify(message, null, 2);
    },
    // 合成卡牌
    synthesizeCards() {
      const message = {
        type: 'synthesize',
        room: {
          uid: this.synthesizeData.uid.toString(),
          cardA: this.synthesizeData.cardA,
          cardB: this.synthesizeData.cardB,
          cardC: this.synthesizeData.cardC
        }
      };
      this.sendMessage(message);
      this.customMessage = JSON.stringify(message, null, 2);
    },
    // 发送自定义JSON
    sendCustomMessage() {
      try {
        const message = JSON.parse(this.customMessage);
        this.sendMessage(message);
      } catch (error) {
        this.addLog('error', `自定义消息格式错误: ${error.message}`);
      }
    },
    addLog(type, content, raw = null) {
      const now = new Date();
      const timestamp = now.toLocaleTimeString();
      this.messageLog.push({
        type,
        content,
        timestamp,
        raw,
        showRaw: false,
      });
      if (this.messageLog.length > 300) this.messageLog.shift();
      this.$nextTick(() => {
        const logElement = document.querySelector('.message-log');
        if (logElement) logElement.scrollTop = logElement.scrollHeight;
      });
    },
    clearLog() {
      this.messageLog = [];
    },
    showRaw(idx) {
      // 展开/收起原始内容
      this.$set(this.messageLog[idx], 'showRaw', !this.messageLog[idx].showRaw);
    },
  },
  beforeDestroy() {
    if (this.websocket) this.websocket.close();
  }
}
</script>

<style scoped>
.websocket-test {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #eee;
}

.connection-status {
  display: flex;
  align-items: center;
  gap: 10px;
}

.status-indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
}

.status-indicator.connected {
  background-color: #4CAF50;
}

.status-indicator.connecting {
  background-color: #FF9800;
}

.status-indicator.disconnected {
  background-color: #F44336;
}

.ws-debug {
  margin-top: 8px;
  color: #333;
  font-size: 13px;
}

.section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background-color: #f9f9f9;
}

.section h3 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #333;
}

.form-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.form-group label {
  min-width: 80px;
  font-weight: bold;
}

.form-group input, .form-group textarea {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.form-group input {
  min-width: 150px;
}

.form-group textarea {
  min-width: 300px;
  font-family: monospace;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  transition: background-color 0.3s;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-primary {
  background-color: #2196F3;
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background-color: #1976D2;
}

.btn-secondary {
  background-color: #757575;
  color: white;
}

.btn-secondary:hover:not(:disabled) {
  background-color: #616161;
}

.btn-success {
  background-color: #4CAF50;
  color: white;
}

.btn-success:hover:not(:disabled) {
  background-color: #388E3C;
}

.btn-warning {
  background-color: #FF9800;
  color: white;
}

.btn-warning:hover:not(:disabled) {
  background-color: #F57C00;
}

.btn-info {
  background-color: #00BCD4;
  color: white;
}

.btn-info:hover:not(:disabled) {
  background-color: #0097A7;
}

.controls {
  display: flex;
  gap: 10px;
}

.message-log {
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 10px;
  background-color: #fff;
  margin-bottom: 10px;
}

.message-item {
  margin-bottom: 5px;
  padding: 5px;
  border-radius: 3px;
  font-size: 12px;
  font-family: monospace;
  cursor: pointer;
}

.message-item.sent {
  background-color: #E3F2FD;
  border-left: 3px solid #2196F3;
}

.message-item.received {
  background-color: #E8F5E8;
  border-left: 3px solid #4CAF50;
}

.message-item.error {
  background-color: #FFEBEE;
  border-left: 3px solid #F44336;
}

.message-item.info {
  background-color: #FFF3E0;
  border-left: 3px solid #FF9800;
}

.message-item.success {
  background-color: #E8F5E8;
  border-left: 3px solid #4CAF50;
}

.timestamp {
  color: #666;
  margin-right: 10px;
}

.message-type {
  font-weight: bold;
  margin-right: 10px;
}

.message-content {
  word-break: break-all;
}
</style>