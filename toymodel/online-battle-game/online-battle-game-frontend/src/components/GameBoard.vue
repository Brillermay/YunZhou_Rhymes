<!--<template>-->
<!--  <div class="websocket-test">-->
<!--    <div class="header">-->
<!--      <h2>WebSocket 游戏测试面板</h2>-->
<!--      <div class="connection-status">-->
<!--        <span :class="['status-indicator', connectionStatus]"></span>-->
<!--        <span>{{ connectionStatusText }}</span>-->
<!--      </div>-->
<!--    </div>-->

<!--    &lt;!&ndash; 连接控制 &ndash;&gt;-->
<!--    <div class="section">-->
<!--      <h3>连接控制</h3>-->
<!--      <div class="controls">-->
<!--        <button @click="connect" :disabled="isConnected" class="btn btn-primary">-->
<!--          连接WebSocket-->
<!--        </button>-->
<!--        <button @click="disconnect" :disabled="!isConnected" class="btn btn-secondary">-->
<!--          断开连接-->
<!--        </button>-->
<!--      </div>-->
<!--      <div class="ws-debug">-->
<!--        <span>WebSocket URL: </span>-->
<!--        <input v-model="wsUrl" style="width: 350px;" />-->
<!--      </div>-->
<!--    </div>-->

<!--    &lt;!&ndash; 常用报文快捷填充 &ndash;&gt;-->
<!--    <div class="section">-->
<!--      <h3>一键填充常用消息</h3>-->
<!--      <div class="form-group">-->
<!--        <button @click="fillRoundEndTemplate" class="btn btn-info">填充 RoundEnd 玩家1</button>-->
<!--        <button @click="fillRoundEndTemplate2" class="btn btn-info">填充 RoundEnd 玩家2</button>-->
<!--        <button @click="fillJoinRoomTemplate" class="btn btn-info">填充 JoinRoom</button>-->
<!--        <button @click="fillStartGameTemplate" class="btn btn-info">填充 StartGame</button>-->
<!--        <button @click="fillOpenCardGroupsTemplate" class="btn btn-info">填充 OpenCardGroups</button>-->
<!--        <button @click="fillRoundBeginTemplate" class="btn btn-info">填充 RoundBegin</button>-->
<!--        <button @click="fillGetRoomStatusTemplate" class="btn btn-info">填充 GetRoomStatus</button>-->
<!--        <button @click="fillDiscardCardTemplate" class="btn btn-info">填充 DiscardCard</button>-->
<!--        <button @click="fillSynthesizeTemplate" class="btn btn-info">填充 Synthesize</button>-->
<!--      </div>-->
<!--    </div>-->

<!--    &lt;!&ndash; RoundBegin 专用测试 &ndash;&gt;-->
<!--    <div class="section">-->
<!--      <h3>RoundBegin 回合开始测试</h3>-->
<!--      <div class="form-group">-->
<!--        <label>roomId:</label>-->
<!--        <input v-model="roundBeginData.roomId" type="text" placeholder="房间ID">-->
<!--        <label>uid:</label>-->
<!--        <input v-model="roundBeginData.uid" type="number" placeholder="玩家ID">-->
<!--        <button @click="sendRoundBegin" :disabled="!isConnected" class="btn btn-warning">发送 RoundBegin</button>-->
<!--      </div>-->
<!--    </div>-->

<!--    &lt;!&ndash; RoundEnd 专用测试 &ndash;&gt;-->
<!--    <div class="section">-->
<!--      <h3>RoundEnd 回合结束测试</h3>-->
<!--      <div class="form-group">-->
<!--        <label>roomId:</label>-->
<!--        <input v-model="roundEndData.roomId" type="text" placeholder="房间ID">-->
<!--        <label>uid1:</label>-->
<!--        <input v-model="roundEndData.uid1" type="number" placeholder="玩家1ID">-->
<!--        <label>cardList1 (英文逗号分隔):</label>-->
<!--        <input v-model="roundEndData.cardList1" type="text" placeholder="如: bird,fire,mountain">-->
<!--        <button @click="sendRoundEnd" :disabled="!isConnected" class="btn btn-warning">发送 RoundEnd</button>-->
<!--      </div>-->
<!--    </div>-->

<!--    &lt;!&ndash; 创建房间 &ndash;&gt;-->
<!--    <div class="section">-->
<!--      <h3>创建房间</h3>-->
<!--      <div class="form-group">-->
<!--        <label>用户ID:</label>-->
<!--        <input v-model="createRoomData.uid" type="number" placeholder="输入用户ID">-->
<!--        <button @click="createRoom" :disabled="!isConnected" class="btn btn-success">-->
<!--          创建房间-->
<!--        </button>-->
<!--      </div>-->
<!--    </div>-->

<!--    &lt;!&ndash; 加入房间 &ndash;&gt;-->
<!--    <div class="section">-->
<!--      <h3>加入房间</h3>-->
<!--      <div class="form-group">-->
<!--        <label>房间ID:</label>-->
<!--        <input v-model="joinRoomData.roomId" type="text" placeholder="输入房间ID">-->
<!--        <label>用户ID:</label>-->
<!--        <input v-model="joinRoomData.uid" type="number" placeholder="输入用户ID">-->
<!--        <button @click="joinRoom" :disabled="!isConnected" class="btn btn-success">-->
<!--          加入房间-->
<!--        </button>-->
<!--      </div>-->
<!--    </div>-->

<!--    &lt;!&ndash; 开始游戏 &ndash;&gt;-->
<!--    <div class="section">-->
<!--      <h3>开始游戏</h3>-->
<!--      <div class="form-group">-->
<!--        <label>房间ID:</label>-->
<!--        <input v-model="startGameData.roomId" type="text" placeholder="输入房间ID">-->
<!--        <label>角色1:</label>-->
<!--        <input v-model="startGameData.role1" type="text" placeholder="输入角色1">-->
<!--        <label>角色2:</label>-->
<!--        <input v-model="startGameData.role2" type="text" placeholder="输入角色2">-->
<!--        <button @click="startGame" :disabled="!isConnected" class="btn btn-success">-->
<!--          开始游戏-->
<!--        </button>-->
<!--      </div>-->
<!--    </div>-->

<!--    &lt;!&ndash; 合成卡牌 &ndash;&gt;-->
<!--    <div class="section">-->
<!--      <h3>合成卡牌</h3>-->
<!--      <div class="form-group">-->
<!--        <label>用户ID:</label>-->
<!--        <input v-model="synthesizeData.uid" type="number" placeholder="输入用户ID">-->
<!--        <label>卡牌A:</label>-->
<!--        <input v-model="synthesizeData.cardA" type="text" placeholder="输入卡牌A名称">-->
<!--        <label>卡牌B:</label>-->
<!--        <input v-model="synthesizeData.cardB" type="text" placeholder="输入卡牌B名称">-->
<!--        <label>卡牌C:</label>-->
<!--        <input v-model="synthesizeData.cardC" type="text" placeholder="输入卡牌C名称">-->
<!--        <button @click="synthesizeCards" :disabled="!isConnected" class="btn btn-warning">-->
<!--          合成卡牌-->
<!--        </button>-->
<!--      </div>-->
<!--    </div>-->

<!--    &lt;!&ndash; 开启卡组/购买卡牌包 &ndash;&gt;-->
<!--    <div class="section">-->
<!--      <h3>开启卡组/购买卡牌包</h3>-->
<!--      <div class="form-group">-->
<!--        <label>用户ID:</label>-->
<!--        <input v-model="openCardGroupsData.uid" type="number" placeholder="输入用户ID">-->
<!--        <button @click="openCardGroups" :disabled="!isConnected" class="btn btn-warning">-->
<!--          发送 OpenCardGroups-->
<!--        </button>-->
<!--      </div>-->
<!--    </div>-->

<!--    &lt;!&ndash; 获取房间状态 &ndash;&gt;-->
<!--    <div class="section">-->
<!--      <h3>获取房间状态</h3>-->
<!--      <div class="form-group">-->
<!--        <label>房间ID:</label>-->
<!--        <input v-model="getRoomStatusData.roomId" type="text" placeholder="输入房间ID">-->
<!--        <button @click="getRoomStatus" :disabled="!isConnected" class="btn btn-info">-->
<!--          获取房间状态-->
<!--        </button>-->
<!--      </div>-->
<!--    </div>-->

<!--    &lt;!&ndash; 弃牌 &ndash;&gt;-->
<!--    <div class="section">-->
<!--      <h3>弃牌测试</h3>-->
<!--      <div class="form-group">-->
<!--        <label>用户ID:</label>-->
<!--        <input v-model="discardCardData.uid" type="number" placeholder="输入用户ID">-->
<!--        <label>卡牌名称:</label>-->
<!--        <input v-model="discardCardData.card" type="text" placeholder="输入要弃掉的卡牌名">-->
<!--        <label>金币变动:</label>-->
<!--        <input v-model="discardCardData.money" type="number" placeholder="金币增减(正为加)">-->
<!--        <button @click="sendDiscardCard" :disabled="!isConnected" class="btn btn-warning">-->
<!--          发送 DiscardCard-->
<!--        </button>-->
<!--      </div>-->
<!--    </div>-->

<!--    &lt;!&ndash; 自定义消息 &ndash;&gt;-->
<!--    <div class="section">-->
<!--      <h3>自定义消息</h3>-->
<!--      <div class="form-group">-->
<!--        <label>JSON消息:</label>-->
<!--        <textarea v-model="customMessage" rows="4" placeholder="输入自定义JSON消息"></textarea>-->
<!--        <button @click="sendCustomMessage" :disabled="!isConnected" class="btn btn-info">-->
<!--          发送自定义消息-->
<!--        </button>-->
<!--      </div>-->
<!--    </div>-->

<!--    &lt;!&ndash; 消息日志 &ndash;&gt;-->
<!--    <div class="section">-->
<!--      <h3>消息日志</h3>-->
<!--      <div class="message-log">-->
<!--        <div v-for="(message, index) in messageLog" :key="index"-->
<!--             :class="['message-item', message.type]"-->
<!--             @dblclick="showRaw(index)">-->
<!--          <span class="timestamp">{{ message.timestamp }}</span>-->
<!--          <span class="message-type">[{{ message.type }}]</span>-->
<!--          <span class="message-content">-->
<!--            {{ message.content }}-->
<!--            <span v-if="message.raw && message.showRaw" style="display:block;white-space:pre;color:#888;background:#f6f6f6;margin-top:4px;font-size:12px">-->
<!--              {{ message.raw }}-->
<!--            </span>-->
<!--          </span>-->
<!--        </div>-->
<!--      </div>-->
<!--      <button @click="clearLog" class="btn btn-secondary">清空日志</button>-->
<!--      <p style="color:#666;font-size:12px;margin-top:10px;">* 双击日志项可展开/收起原始JSON</p>-->
<!--    </div>-->
<!--  </div>-->
<!--</template>-->

<!--<script>-->
<!--export default {-->
<!--  name: 'GameBoard',-->
<!--  data() {-->
<!--    return {-->
<!--      websocket: null,-->
<!--      isConnected: false,-->
<!--      connectionStatus: 'disconnected',-->
<!--      connectionStatusText: '未连接',-->
<!--      wsUrl: 'ws://localhost:8081/ws/game',-->

<!--      // RoundBegin表单-->
<!--      roundBeginData: { roomId: '', uid: '' },-->
<!--      // RoundEnd表单-->
<!--      roundEndData: { roomId: '', uid1: '', cardList1: '' },-->
<!--      // 开启卡组表单-->
<!--      openCardGroupsData: { uid: '' },-->
<!--      // 获取房间状态表单-->
<!--      getRoomStatusData: { roomId: '' },-->
<!--      // 其它表单数据-->
<!--      createRoomData: { uid: 1 },-->
<!--      joinRoomData: { roomId: '', uid: 2 },-->
<!--      startGameData: { roomId: '', role1: '战士', role2: '法师' },-->
<!--      synthesizeData: { uid: 1, cardA: 'fire', cardB: 'water', cardC: 'bamboo' },-->
<!--      discardCardData: { uid: 1, card: 'fire', money: 2 },-->
<!--      customMessage: '{\n  "type": "test",\n  "room": {\n    "uid": "1"\n  }\n}',-->
<!--      messageLog: [],-->
<!--    }-->
<!--  },-->
<!--  methods: {-->
<!--    connect() {-->
<!--      try {-->
<!--        if (this.websocket && this.isConnected) {-->
<!--          this.addLog('info', 'WebSocket已连接');-->
<!--          return;-->
<!--        }-->
<!--        this.websocket = new window.WebSocket(this.wsUrl);-->
<!--        this.websocket.onopen = this.onOpen;-->
<!--        this.websocket.onmessage = this.onMessage;-->
<!--        this.websocket.onclose = this.onClose;-->
<!--        this.websocket.onerror = this.onError;-->
<!--        this.connectionStatus = 'connecting';-->
<!--        this.connectionStatusText = '连接中...';-->
<!--        this.addLog('info', '正在连接到: ' + this.wsUrl);-->
<!--      } catch (error) {-->
<!--        this.addLog('error', `连接失败: ${error.message}`);-->
<!--      }-->
<!--    },-->
<!--    disconnect() {-->
<!--      if (this.websocket) {-->
<!--        this.websocket.close();-->
<!--      }-->
<!--    },-->
<!--    onOpen() {-->
<!--      this.isConnected = true;-->
<!--      this.connectionStatus = 'connected';-->
<!--      this.connectionStatusText = '已连接';-->
<!--      this.addLog('success', 'WebSocket连接已建立');-->
<!--    },-->
<!--    onMessage(event) {-->
<!--      try {-->
<!--        const data = JSON.parse(event.data);-->
<!--        this.addLog('received', `收到: ${JSON.stringify(data, null, 2)}`, event.data);-->
<!--      } catch (error) {-->
<!--        this.addLog('received', `收到: ${event.data}`, event.data);-->
<!--      }-->
<!--    },-->
<!--    onClose(event) {-->
<!--      this.isConnected = false;-->
<!--      this.connectionStatus = 'disconnected';-->
<!--      this.connectionStatusText = '未连接';-->
<!--      this.addLog('info', `连接已关闭: ${event.code} - ${event.reason}`);-->
<!--    },-->
<!--    onError(event) {-->
<!--      this.addLog('error', `连接错误: ${event && event.message ? event.message : ''}`);-->
<!--    },-->
<!--    sendMessage(message) {-->
<!--      if (this.websocket && this.isConnected) {-->
<!--        const messageStr = JSON.stringify(message);-->
<!--        this.websocket.send(messageStr);-->
<!--        this.addLog('sent', `发送: ${messageStr}`, messageStr);-->
<!--      } else {-->
<!--        this.addLog('error', 'WebSocket未连接');-->
<!--      }-->
<!--    },-->
<!--    // 发送RoundBegin报文-->
<!--    sendRoundBegin() {-->
<!--      const message = {-->
<!--        type: 'RoundBegin',-->
<!--        room: {-->
<!--          roomId: this.roundBeginData.roomId,-->
<!--          uid: this.roundBeginData.uid + '',-->
<!--        },-->
<!--      };-->
<!--      this.sendMessage(message);-->
<!--      this.customMessage = JSON.stringify(message, null, 2);-->
<!--    },-->
<!--    fillRoundBeginTemplate() {-->
<!--      this.roundBeginData.roomId = 'aaaabbbbccccdddd';-->
<!--      this.roundBeginData.uid = '1';-->
<!--      this.customMessage = JSON.stringify({-->
<!--        type: 'RoundBegin',-->
<!--        room: { roomId: 'aaaabbbbccccdddd', uid: '1' }-->
<!--      }, null, 2);-->
<!--    },-->
<!--    sendRoundEnd() {-->
<!--      const cards = this.roundEndData.cardList1.split(',').map(s => s.trim()).filter(Boolean);-->
<!--      const message = {-->
<!--        type: 'RoundEnd',-->
<!--        room: {-->
<!--          roomId: this.roundEndData.roomId,-->
<!--          uid1: this.roundEndData.uid1 + '',-->
<!--          cardList1: cards,-->
<!--        },-->
<!--      };-->
<!--      this.sendMessage(message);-->
<!--      this.customMessage = JSON.stringify(message, null, 2);-->
<!--    },-->
<!--    fillRoundEndTemplate() {-->
<!--      this.roundEndData.roomId = 'aaaabbbbccccdddd';-->
<!--      this.roundEndData.uid1 = '1';-->
<!--      this.roundEndData.cardList1 = 'bird,fire,mountain';-->
<!--      this.customMessage = JSON.stringify({-->
<!--        type: 'RoundEnd',-->
<!--        room: { roomId: 'aaaabbbbccccdddd', uid1: '1', cardList1: ["bird", "fire", "mountain"] }-->
<!--      }, null, 2);-->
<!--    },-->
<!--    fillRoundEndTemplate2() {-->
<!--      this.roundEndData.roomId = 'aaaabbbbccccdddd';-->
<!--      this.roundEndData.uid1 = '2';-->
<!--      this.roundEndData.cardList1 = 'water,earth,wind';-->
<!--      this.customMessage = JSON.stringify({-->
<!--        type: 'RoundEnd',-->
<!--        room: { roomId: 'aaaabbbbccccdddd', uid1: '2', cardList1: ["water", "earth", "wind"] }-->
<!--      }, null, 2);-->
<!--    },-->
<!--    fillJoinRoomTemplate() {-->
<!--      this.joinRoomData.roomId = 'aaaabbbbccccdddd';-->
<!--      this.joinRoomData.uid = 2;-->
<!--      this.customMessage = JSON.stringify({-->
<!--        type: "joinRoom",-->
<!--        room: { roomId: "aaaabbbbccccdddd", uid: "2" }-->
<!--      }, null, 2);-->
<!--    },-->
<!--    fillStartGameTemplate() {-->
<!--      this.startGameData.roomId = 'aaaabbbbccccdddd';-->
<!--      this.startGameData.role1 = '战士';-->
<!--      this.startGameData.role2 = '法师';-->
<!--      this.customMessage = JSON.stringify({-->
<!--        type: "startGame",-->
<!--        room: { roomId: "aaaabbbbccccdddd", role1: "战士", role2: "法师" }-->
<!--      }, null, 2);-->
<!--    },-->
<!--    fillOpenCardGroupsTemplate() {-->
<!--      this.openCardGroupsData.uid = 1;-->
<!--      this.customMessage = JSON.stringify({-->
<!--        type: "openCardGroups",-->
<!--        room: { uid: "1" }-->
<!--      }, null, 2);-->
<!--    },-->
<!--    fillGetRoomStatusTemplate() {-->
<!--      this.getRoomStatusData.roomId = "aaaabbbbccccdddd";-->
<!--      this.customMessage = JSON.stringify({-->
<!--        type: 'getRoomStatus',-->
<!--        room: { roomId: 'aaaabbbbccccdddd' }-->
<!--      }, null, 2);-->
<!--    },-->
<!--    fillDiscardCardTemplate() {-->
<!--      this.discardCardData.uid = 1;-->
<!--      this.discardCardData.card = 'fire';-->
<!--      this.discardCardData.money = 2;-->
<!--      this.customMessage = JSON.stringify({-->
<!--        type: "discardCard",-->
<!--        room: { uid: 1, card: "fire", money: 2 }-->
<!--      }, null, 2);-->
<!--    },-->
<!--    fillSynthesizeTemplate() {-->
<!--      this.synthesizeData.uid = 1;-->
<!--      this.synthesizeData.cardA = 'fire';-->
<!--      this.synthesizeData.cardB = 'water';-->
<!--      this.synthesizeData.cardC = 'bamboo';-->
<!--      this.customMessage = JSON.stringify({-->
<!--        type: "synthesize",-->
<!--        room: { uid: 1, cardA: "fire", cardB: "water", cardC: "bamboo" }-->
<!--      }, null, 2);-->
<!--    },-->
<!--    // 创建房间-->
<!--    createRoom() {-->
<!--      const message = {-->
<!--        type: 'createRoom',-->
<!--        room: {-->
<!--          uid: this.createRoomData.uid.toString()-->
<!--        }-->
<!--      };-->
<!--      this.sendMessage(message);-->
<!--      this.customMessage = JSON.stringify(message, null, 2);-->
<!--    },-->
<!--    // 加入房间-->
<!--    joinRoom() {-->
<!--      const message = {-->
<!--        type: 'joinRoom',-->
<!--        room: {-->
<!--          roomId: this.joinRoomData.roomId,-->
<!--          uid: this.joinRoomData.uid.toString()-->
<!--        }-->
<!--      };-->
<!--      this.sendMessage(message);-->
<!--      this.customMessage = JSON.stringify(message, null, 2);-->
<!--    },-->
<!--    // 开始游戏-->
<!--    startGame() {-->
<!--      const message = {-->
<!--        type: 'startGame',-->
<!--        room: {-->
<!--          roomId: this.startGameData.roomId,-->
<!--          role1: this.startGameData.role1,-->
<!--          role2: this.startGameData.role2-->
<!--        }-->
<!--      };-->
<!--      this.sendMessage(message);-->
<!--      this.customMessage = JSON.stringify(message, null, 2);-->
<!--    },-->
<!--    // 合成卡牌-->
<!--    synthesizeCards() {-->
<!--      const message = {-->
<!--        type: 'synthesize',-->
<!--        room: {-->
<!--          uid: this.synthesizeData.uid.toString(),-->
<!--          cardA: this.synthesizeData.cardA,-->
<!--          cardB: this.synthesizeData.cardB,-->
<!--          cardC: this.synthesizeData.cardC-->
<!--        }-->
<!--      };-->
<!--      this.sendMessage(message);-->
<!--      this.customMessage = JSON.stringify(message, null, 2);-->
<!--    },-->
<!--    // 购买卡牌包-->
<!--    openCardGroups() {-->
<!--      const message = {-->
<!--        type: 'openCardGroups',-->
<!--        room: {-->
<!--          uid: this.openCardGroupsData.uid.toString()-->
<!--        }-->
<!--      };-->
<!--      this.sendMessage(message);-->
<!--      this.customMessage = JSON.stringify(message, null, 2);-->
<!--    },-->
<!--    // 获取房间状态-->
<!--    getRoomStatus() {-->
<!--      const message = {-->
<!--        type: 'getRoomStatus',-->
<!--        room: {-->
<!--          roomId: this.getRoomStatusData.roomId-->
<!--        }-->
<!--      };-->
<!--      this.sendMessage(message);-->
<!--      this.customMessage = JSON.stringify(message, null, 2);-->
<!--    },-->
<!--    // 发送弃牌-->
<!--    sendDiscardCard() {-->
<!--      const message = {-->
<!--        type: 'discardCard',-->
<!--        room: {-->
<!--          uid: this.discardCardData.uid,-->
<!--          card: this.discardCardData.card,-->
<!--          money: Number(this.discardCardData.money)-->
<!--        }-->
<!--      };-->
<!--      this.sendMessage(message);-->
<!--      this.customMessage = JSON.stringify(message, null, 2);-->
<!--    },-->
<!--    // 发送自定义JSON-->
<!--    sendCustomMessage() {-->
<!--      try {-->
<!--        const message = JSON.parse(this.customMessage);-->
<!--        this.sendMessage(message);-->
<!--      } catch (error) {-->
<!--        this.addLog('error', `自定义消息格式错误: ${error.message}`);-->
<!--      }-->
<!--    },-->
<!--    addLog(type, content, raw = null) {-->
<!--      const now = new Date();-->
<!--      const timestamp = now.toLocaleTimeString();-->
<!--      this.messageLog.push({-->
<!--        type,-->
<!--        content,-->
<!--        timestamp,-->
<!--        raw,-->
<!--        showRaw: false,-->
<!--      });-->
<!--      if (this.messageLog.length > 300) this.messageLog.shift();-->
<!--      this.$nextTick(() => {-->
<!--        const logElement = document.querySelector('.message-log');-->
<!--        if (logElement) logElement.scrollTop = logElement.scrollHeight;-->
<!--      });-->
<!--    },-->
<!--    clearLog() {-->
<!--      this.messageLog = [];-->
<!--    },-->
<!--    showRaw(idx) {-->
<!--      // 展开/收起原始内容-->
<!--      this.$set(this.messageLog[idx], 'showRaw', !this.messageLog[idx].showRaw);-->
<!--    },-->
<!--  },-->
<!--  beforeDestroy() {-->
<!--    if (this.websocket) this.websocket.close();-->
<!--  }-->
<!--}-->
<!--</script>-->

<!--<style scoped>-->
<!--.websocket-test {-->
<!--  max-width: 1200px;-->
<!--  margin: 0 auto;-->
<!--  padding: 20px;-->
<!--  font-family: Arial, sans-serif;-->
<!--}-->

<!--.header {-->
<!--  display: flex;-->
<!--  justify-content: space-between;-->
<!--  align-items: center;-->
<!--  margin-bottom: 30px;-->
<!--  padding-bottom: 20px;-->
<!--  border-bottom: 2px solid #eee;-->
<!--}-->

<!--.connection-status {-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  gap: 10px;-->
<!--}-->

<!--.status-indicator {-->
<!--  width: 12px;-->
<!--  height: 12px;-->
<!--  border-radius: 50%;-->
<!--  display: inline-block;-->
<!--}-->

<!--.status-indicator.connected {-->
<!--  background-color: #4CAF50;-->
<!--}-->

<!--.status-indicator.connecting {-->
<!--  background-color: #FF9800;-->
<!--}-->

<!--.status-indicator.disconnected {-->
<!--  background-color: #F44336;-->
<!--}-->

<!--.ws-debug {-->
<!--  margin-top: 8px;-->
<!--  color: #333;-->
<!--  font-size: 13px;-->
<!--}-->

<!--.section {-->
<!--  margin-bottom: 30px;-->
<!--  padding: 20px;-->
<!--  border: 1px solid #ddd;-->
<!--  border-radius: 8px;-->
<!--  background-color: #f9f9f9;-->
<!--}-->

<!--.section h3 {-->
<!--  margin-top: 0;-->
<!--  margin-bottom: 15px;-->
<!--  color: #333;-->
<!--}-->

<!--.form-group {-->
<!--  display: flex;-->
<!--  flex-wrap: wrap;-->
<!--  gap: 10px;-->
<!--  align-items: center;-->
<!--}-->

<!--.form-group label {-->
<!--  min-width: 80px;-->
<!--  font-weight: bold;-->
<!--}-->

<!--.form-group input, .form-group textarea {-->
<!--  padding: 8px;-->
<!--  border: 1px solid #ddd;-->
<!--  border-radius: 4px;-->
<!--  font-size: 14px;-->
<!--}-->

<!--.form-group input {-->
<!--  min-width: 150px;-->
<!--}-->

<!--.form-group textarea {-->
<!--  min-width: 300px;-->
<!--  font-family: monospace;-->
<!--}-->

<!--.btn {-->
<!--  padding: 8px 16px;-->
<!--  border: none;-->
<!--  border-radius: 4px;-->
<!--  cursor: pointer;-->
<!--  font-size: 14px;-->
<!--  font-weight: bold;-->
<!--  transition: background-color 0.3s;-->
<!--}-->

<!--.btn:disabled {-->
<!--  opacity: 0.6;-->
<!--  cursor: not-allowed;-->
<!--}-->

<!--.btn-primary {-->
<!--  background-color: #2196F3;-->
<!--  color: white;-->
<!--}-->

<!--.btn-primary:hover:not(:disabled) {-->
<!--  background-color: #1976D2;-->
<!--}-->

<!--.btn-secondary {-->
<!--  background-color: #757575;-->
<!--  color: white;-->
<!--}-->

<!--.btn-secondary:hover:not(:disabled) {-->
<!--  background-color: #616161;-->
<!--}-->

<!--.btn-success {-->
<!--  background-color: #4CAF50;-->
<!--  color: white;-->
<!--}-->

<!--.btn-success:hover:not(:disabled) {-->
<!--  background-color: #388E3C;-->
<!--}-->

<!--.btn-warning {-->
<!--  background-color: #FF9800;-->
<!--  color: white;-->
<!--}-->

<!--.btn-warning:hover:not(:disabled) {-->
<!--  background-color: #F57C00;-->
<!--}-->

<!--.btn-info {-->
<!--  background-color: #00BCD4;-->
<!--  color: white;-->
<!--}-->

<!--.btn-info:hover:not(:disabled) {-->
<!--  background-color: #0097A7;-->
<!--}-->

<!--.controls {-->
<!--  display: flex;-->
<!--  gap: 10px;-->
<!--}-->

<!--.message-log {-->
<!--  max-height: 400px;-->
<!--  overflow-y: auto;-->
<!--  border: 1px solid #ddd;-->
<!--  border-radius: 4px;-->
<!--  padding: 10px;-->
<!--  background-color: #fff;-->
<!--  margin-bottom: 10px;-->
<!--}-->

<!--.message-item {-->
<!--  margin-bottom: 5px;-->
<!--  padding: 5px;-->
<!--  border-radius: 3px;-->
<!--  font-size: 12px;-->
<!--  font-family: monospace;-->
<!--  cursor: pointer;-->
<!--}-->

<!--.message-item.sent {-->
<!--  background-color: #E3F2FD;-->
<!--  border-left: 3px solid #2196F3;-->
<!--}-->

<!--.message-item.received {-->
<!--  background-color: #E8F5E8;-->
<!--  border-left: 3px solid #4CAF50;-->
<!--}-->

<!--.message-item.error {-->
<!--  background-color: #FFEBEE;-->
<!--  border-left: 3px solid #F44336;-->
<!--}-->

<!--.message-item.info {-->
<!--  background-color: #FFF3E0;-->
<!--  border-left: 3px solid #FF9800;-->
<!--}-->

<!--.message-item.success {-->
<!--  background-color: #E8F5E8;-->
<!--  border-left: 3px solid #4CAF50;-->
<!--}-->

<!--.timestamp {-->
<!--  color: #666;-->
<!--  margin-right: 10px;-->
<!--}-->

<!--.message-type {-->
<!--  font-weight: bold;-->
<!--  margin-right: 10px;-->
<!--}-->

<!--.message-content {-->
<!--  word-break: break-all;-->
<!--}-->
<!--</style>-->
<template>
  <div class="websocket-test">
    <div class="header">
      <h2>WebSocket 游戏调试与测试面板</h2>
      <div class="connection-status">
        <span :class="['status-indicator', connectionStatus]"></span>
        <span>{{ connectionStatusText }}</span>
      </div>
      <button class="btn btn-info" @click="exportLogs" title="导出全部日志为JSON">导出日志</button>
    </div>

    <!-- 连接控制 -->
    <div class="section">
      <h3>连接控制</h3>
      <div class="controls">
        <button @click="connect" :disabled="isConnected" class="btn btn-primary">连接WebSocket</button>
        <button @click="disconnect" :disabled="!isConnected" class="btn btn-secondary">断开连接</button>
      </div>
      <div class="ws-debug">
        <span>WebSocket URL: </span>
        <input v-model="wsUrl" style="width: 350px;" />
      </div>
    </div>

    <!-- 一键填充报文 -->
    <div class="section">
      <h3>一键填充合法消息</h3>
      <div class="form-group" style="flex-wrap:wrap;gap:8px;">
        <button v-for="(tpl, key) in templates" :key="key" class="btn btn-info" @click="fillTemplate(key)">
          {{ tpl.label }}
        </button>
        <button class="btn btn-info" @click="autoFillAll">自动填充全部表单</button>
      </div>
    </div>

    <!-- 各种测试表单 -->
    <div class="section" v-for="(form, key) in forms" :key="key">
      <h3>{{ form.label }}</h3>
      <div class="form-group">
        <template v-for="(field, fieldKey) in form.fields">
          <label :for="key + '-' + fieldKey">{{ field.label }}:</label>
          <input
              v-if="field.type !== 'textarea'"
              :type="field.type"
              v-model="form.data[fieldKey]"
              :placeholder="field.placeholder"
              :id="key + '-' + fieldKey"
          />
          <textarea
              v-else
              v-model="form.data[fieldKey]"
              :placeholder="field.placeholder"
              rows="3"
              style="min-width:220px"
              :id="key + '-' + fieldKey"
          ></textarea>
        </template>
        <button
            :class="form.buttonClass"
            :disabled="!isConnected"
            @click="formSend(key)"
        >{{ form.buttonText }}</button>
      </div>
    </div>

    <!-- 自定义JSON -->
    <div class="section">
      <h3>自定义消息</h3>
      <div class="form-group">
        <label>JSON消息:</label>
        <textarea v-model="customMessage" rows="3" placeholder="输入自定义JSON消息"></textarea>
        <button @click="sendCustomMessage" :disabled="!isConnected" class="btn btn-info">发送自定义消息</button>
      </div>
    </div>

    <!-- 日志展示 -->
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
const uuid = () => Math.random().toString(36).substring(2, 10) + Math.random().toString(36).substring(2, 10);

export default {
  name: 'GameBoard',
  data() {
    return {
      websocket: null,
      isConnected: false,
      connectionStatus: 'disconnected',
      connectionStatusText: '未连接',
      wsUrl: 'ws://localhost:8081/ws/game',
      customMessage: '{\n  "type": "test",\n  "room": {\n    "uid": "1"\n  }\n}',
      messageLog: [],
      // 所有表单数据，自动填充与模板绑定
      forms: {
        createRoom: {
          label: "创建房间",
          buttonText: "创建房间",
          buttonClass: "btn btn-success",
          data: { uid: 1 },
          fields: {
            uid: { label: "用户ID", type: "number", placeholder: "用户ID" }
          }
        },
        joinRoom: {
          label: "加入房间",
          buttonText: "加入房间",
          buttonClass: "btn btn-success",
          data: { roomId: '', uid: 2 },
          fields: {
            roomId: { label: "房间ID", type: "text", placeholder: "房间ID" },
            uid: { label: "用户ID", type: "number", placeholder: "用户ID" }
          }
        },
        startGame: {
          label: "开始游戏",
          buttonText: "开始游戏",
          buttonClass: "btn btn-success",
          data: { roomId: '', role1: '战士', role2: '法师' },
          fields: {
            roomId: { label: "房间ID", type: "text", placeholder: "房间ID" },
            role1: { label: "角色1", type: "text", placeholder: "角色1" },
            role2: { label: "角色2", type: "text", placeholder: "角色2" }
          }
        },
        roundBegin: {
          label: "回合开始 RoundBegin",
          buttonText: "发送 RoundBegin",
          buttonClass: "btn btn-warning",
          data: { roomId: '', uid: '' },
          fields: {
            roomId: { label: "房间ID", type: "text", placeholder: "房间ID" },
            uid: { label: "玩家ID", type: "number", placeholder: "玩家ID" }
          }
        },
        roundEnd: {
          label: "回合结束 RoundEnd",
          buttonText: "发送 RoundEnd",
          buttonClass: "btn btn-warning",
          data: { roomId: '', uid: '', cardList: '' },
          fields: {
            roomId: { label: "房间ID", type: "text", placeholder: "房间ID" },
            uid: { label: "玩家ID", type: "number", placeholder: "玩家ID" },
            cardList: { label: "卡牌列表(英文逗号分隔)", type: "text", placeholder: "如: fire,water,earth" }
          }
        },
        fetchall: {
          label: "房间全状态 fetchall",
          buttonText: "发送 fetchall",
          buttonClass: "btn btn-info",
          data: { roomId: '' },
          fields: {
            roomId: { label: "房间ID", type: "text", placeholder: "房间ID" }
          }
        },
        openCardGroups: {
          label: "开启卡组/购买卡牌包",
          buttonText: "发送 OpenCardGroups",
          buttonClass: "btn btn-warning",
          data: { uid: 1 },
          fields: {
            uid: { label: "用户ID", type: "number", placeholder: "用户ID" }
          }
        },
        discardCard: {
          label: "弃牌",
          buttonText: "发送 DiscardCard",
          buttonClass: "btn btn-warning",
          data: { uid: 1, card: 'fire', money: 2 },
          fields: {
            uid: { label: "用户ID", type: "number", placeholder: "用户ID" },
            card: { label: "卡牌名称", type: "text", placeholder: "卡牌名称" },
            money: { label: "金币变动", type: "number", placeholder: "金币增减" }
          }
        },
        synthesize: {
          label: "合成卡牌",
          buttonText: "合成卡牌",
          buttonClass: "btn btn-warning",
          data: { uid: 1, cardA: 'fire', cardB: 'water', cardC: 'bamboo' },
          fields: {
            uid: { label: "用户ID", type: "number", placeholder: "用户ID" },
            cardA: { label: "卡牌A", type: "text", placeholder: "卡牌A名称" },
            cardB: { label: "卡牌B", type: "text", placeholder: "卡牌B名称" },
            cardC: { label: "卡牌C", type: "text", placeholder: "卡牌C名称" }
          }
        },
        getRoomStatus: {
          label: "获取房间状态",
          buttonText: "获取房间状态",
          buttonClass: "btn btn-info",
          data: { roomId: '' },
          fields: {
            roomId: { label: "房间ID", type: "text", placeholder: "房间ID" }
          }
        }
      },
      // 合法消息模板
      templates: {
        createRoom: {
          label: '填充 CreateRoom',
          getData: () => ({
            type: 'createRoom',
            room: { uid: '1' }
          })
        },
        joinRoom: {
          label: '填充 JoinRoom',
          getData: () => ({
            type: 'joinRoom',
            room: { roomId: 'testRoom123', uid: '2' }
          })
        },
        startGame: {
          label: '填充 StartGame',
          getData: () => ({
            type: 'startGame',
            room: { roomId: 'testRoom123', role1: '战士', role2: '法师' }
          })
        },
        roundBegin: {
          label: '填充 RoundBegin',
          getData: () => ({
            type: 'RoundBegin',
            room: { roomId: 'testRoom123', uid: '1' }
          })
        },
        roundEnd1: {
          label: '填充 RoundEnd 玩家1',
          getData: () => ({
            type: 'RoundEnd',
            room: { roomId: 'testRoom123', uid: '1', cardList1: ['fire','water','earth'] }
          })
        },
        roundEnd2: {
          label: '填充 RoundEnd 玩家2',
          getData: () => ({
            type: 'RoundEnd',
            room: { roomId: 'testRoom123', uid: '2', cardList1: ['wind','mountain','bird'] }
          })
        },
        synthesize: {
          label: '填充 Synthesize',
          getData: () => ({
            type: 'synthesize',
            room: { uid: '1', cardA: 'fire', cardB: 'water', cardC: 'bamboo' }
          })
        },
        openCardGroups: {
          label: '填充 OpenCardGroups',
          getData: () => ({
            type: 'openCardGroups',
            room: { uid: '1' }
          })
        },
        discardCard: {
          label: '填充 DiscardCard',
          getData: () => ({
            type: 'discardCard',
            room: { uid: 1, card: 'fire', money: 2 }
          })
        },
        fetchall: {
          label: '填充 fetchall',
          getData: () => ({
            type: 'fetchall',
            room: { roomId: 'testRoom123' }
          })
        },
        getRoomStatus: {
          label: '填充 GetRoomStatus',
          getData: () => ({
            type: 'getRoomStatus',
            room: { roomId: 'testRoom123' }
          })
        }
      }
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
      if (this.websocket) this.websocket.close();
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
    // 快捷填充模板表单数据
    fillTemplate(key) {
      const t = this.templates[key];
      let data = t.getData();
      // 填充到对应表单
      if (key === 'roundEnd1' || key === 'roundEnd2') {
        Object.assign(this.forms.roundEnd.data, {
          roomId: data.room.roomId,
          uid: data.room.uid,
          cardList: data.room.cardList1.join(',')
        });
      } else if (this.forms[key]) {
        // 通用填充
        for (const f in this.forms[key].data) {
          if (data.room && data.room[f] !== undefined) {
            this.forms[key].data[f] = data.room[f];
          }
        }
      } else if (key === 'fetchall' || key === 'getRoomStatus') {
        // 特殊表单
        Object.assign(this.forms[key].data, data.room);
      } else if (key === 'synthesize') {
        Object.assign(this.forms.synthesize.data, data.room);
      } else if (key === 'discardCard') {
        Object.assign(this.forms.discardCard.data, data.room);
      } else if (key === 'openCardGroups') {
        Object.assign(this.forms.openCardGroups.data, data.room);
      }
      this.customMessage = JSON.stringify(data, null, 2);
    },
    // 自动填充所有表单合法数据
    autoFillAll() {
      for (const key in this.templates) {
        this.fillTemplate(key);
      }
      this.addLog('info', '所有表单已自动填充合法数据');
    },
    // 按表单发送
    formSend(key) {
      const form = this.forms[key];
      // 构造报文
      let msg = {};
      switch (key) {
        case 'createRoom':
          msg = { type: 'createRoom', room: { uid: String(form.data.uid) } }; break;
        case 'joinRoom':
          msg = { type: 'joinRoom', room: { roomId: form.data.roomId, uid: String(form.data.uid) } }; break;
        case 'startGame':
          msg = { type: 'startGame', room: { roomId: form.data.roomId, role1: form.data.role1, role2: form.data.role2 } }; break;
        case 'roundBegin':
          msg = { type: 'RoundBegin', room: { roomId: form.data.roomId, uid: String(form.data.uid) } }; break;
        case 'roundEnd':
          msg = {
            type: 'RoundEnd',
            room: {
              roomId: form.data.roomId,
              uid: String(form.data.uid),
              cardList1: form.data.cardList.split(',').map(s => s.trim()).filter(Boolean)
            }
          }; break;
        case 'openCardGroups':
          msg = { type: 'openCardGroups', room: { uid: String(form.data.uid) } }; break;
        case 'fetchall':
          msg = { type: 'fetchall', room: { roomId: form.data.roomId } }; break;
        case 'discardCard':
          msg = { type: 'discardCard', room: { uid: Number(form.data.uid), card: form.data.card, money: Number(form.data.money) } }; break;
        case 'synthesize':
          msg = { type: 'synthesize', room: {
              uid: String(form.data.uid),
              cardA: form.data.cardA, cardB: form.data.cardB, cardC: form.data.cardC
            }}; break;
        case 'getRoomStatus':
          msg = { type: 'getRoomStatus', room: { roomId: form.data.roomId } }; break;
        default: return;
      }
      this.sendMessage(msg);
      this.customMessage = JSON.stringify(msg, null, 2);
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
      this.messageLog.push({ type, content, timestamp, raw, showRaw: false });
      if (this.messageLog.length > 500) this.messageLog.shift();
      this.$nextTick(() => {
        const logElement = document.querySelector('.message-log');
        if (logElement) logElement.scrollTop = logElement.scrollHeight;
      });
    },
    clearLog() {
      this.messageLog = [];
    },
    showRaw(idx) {
      this.$set(this.messageLog[idx], 'showRaw', !this.messageLog[idx].showRaw);
    },
    // 导出日志为JSON
    exportLogs() {
      const blob = new Blob([JSON.stringify(this.messageLog, null, 2)], { type: "application/json" });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `ws-debug-log-${Date.now()}.json`;
      link.click();
      window.URL.revokeObjectURL(url);
      this.addLog('info', '日志已导出');
    }
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
.status-indicator.connected { background-color: #4CAF50; }
.status-indicator.connecting { background-color: #FF9800; }
.status-indicator.disconnected { background-color: #F44336; }
.ws-debug { margin-top: 8px; color: #333; font-size: 13px; }
.section {
  margin-bottom: 28px;
  padding: 18px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background-color: #f9f9f9;
}
.section h3 { margin-top: 0; margin-bottom: 12px; color: #333; }
.form-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}
.form-group label { min-width: 78px; font-weight: bold; }
.form-group input, .form-group textarea {
  padding: 7px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}
.form-group input { min-width: 120px; }
.form-group textarea {
  min-width: 250px;
  font-family: monospace;
}
.btn {
  padding: 8px 15px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  font-weight: bold;
  transition: background-color 0.2s;
}
.btn:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-primary { background-color: #2196F3; color: white; }
.btn-primary:hover:not(:disabled) { background-color: #1976D2; }
.btn-secondary { background-color: #757575; color: white; }
.btn-secondary:hover:not(:disabled) { background-color: #616161; }
.btn-success { background-color: #4CAF50; color: white; }
.btn-success:hover:not(:disabled) { background-color: #388E3C; }
.btn-warning { background-color: #FF9800; color: white; }
.btn-warning:hover:not(:disabled) { background-color: #F57C00; }
.btn-info { background-color: #00BCD4; color: white; }
.btn-info:hover:not(:disabled) { background-color: #0097A7; }
.controls { display: flex; gap: 10px; }
.message-log {
  max-height: 340px;
  overflow-y: auto;
  border: 1px solid #ddd;
  border-radius: 4px;
  padding: 10px;
  background-color: #fff;
  margin-bottom: 10px;
}
.message-item {
  margin-bottom: 4px;
  padding: 5px;
  border-radius: 3px;
  font-size: 12px;
  font-family: monospace;
  cursor: pointer;
}
.message-item.sent { background-color: #E3F2FD; border-left: 3px solid #2196F3; }
.message-item.received { background-color: #E8F5E8; border-left: 3px solid #4CAF50; }
.message-item.error { background-color: #FFEBEE; border-left: 3px solid #F44336; }
.message-item.info { background-color: #FFF3E0; border-left: 3px solid #FF9800; }
.message-item.success { background-color: #E8F5E8; border-left: 3px solid #4CAF50; }
.timestamp { color: #666; margin-right: 10px; }
.message-type { font-weight: bold; margin-right: 10px; }
.message-content { word-break: break-all; }
</style>