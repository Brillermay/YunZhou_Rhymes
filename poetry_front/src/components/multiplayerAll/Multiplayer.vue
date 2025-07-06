<template>
  <div class="screen-wrapper" @wheel.passive.prevent="handleWheel">
    <div class="screens" :style="containerStyle">
      <!-- 第一个游戏页面 -->
      <div class="screen" ref="screen0"></div>
      <!-- 第二个游戏页面 -->
      <div class="screen" ref="screen1"></div>
      <teleport to="body">
        <div id="countdown-timer" class="countdown">
          <div class="round">
            回合 <span class="round-num">{{ round }}</span> / {{ maxRound }}
          </div>
          <div class="timer">
            <span>倒计时：</span>
            <span class="time-num">{{ countdown }}</span>
            <span>秒</span>
          </div>
        </div>
        <div v-if="showGameResult" class="game-result-indicator" :class="gameResultClass">
          <span class="result-text">{{ gameResultText }}</span>
          <button class="return-btn" @click="handleReturnToGameCenter">返回大厅</button>
        </div>

        <!-- 调试按钮，浮动在左上角 -->
        <button class="fetchall-debug-btn" @click="handleFetchAll">
          fetchall
        </button>

      </teleport>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import Phaser from 'phaser';
import { useRouter } from 'vue-router';
import { isLoggedIn, getCurrentUid, requireLogin } from '@/utils/auth';
import { saveData, getData, updateData, removeData, hasData, clearAllData } from '../util/storageUtil';

console.log('🏁 script setup 运行了');

function handleReturnToGameCenter() {
  router.push('/game-center')
}

function handleFetchAll() {
  console.log('666')
  sendMessage({
    type: "fetchall",
    room: {
      roomId: getData('current_game_room')?.roomId,
    }
  });
}

const router = useRouter();

const isGameOver = ref(false)

const showGameResult = ref(false)
const gameResult = ref("") // "win" | "lose" | "draw" | ""

const gameResultText = computed(() => {
  if (gameResult.value === "win") return "胜利"
  if (gameResult.value === "lose") return "失败"
  if (gameResult.value === "draw") return "平局"
  return ""
})
const gameResultClass = computed(() => {
  if (gameResult.value === "win") return "result-win"
  if (gameResult.value === "lose") return "result-lose"
  if (gameResult.value === "draw") return "result-draw"
  return ""
})


let websocket = ref(null);
let isConnected = ref(false);
let connectionStatus = ref('disconnected');
let connectionStatusText = ref('未连接');
let reconnectAttempts = ref(0);
const maxReconnectAttempts = 5;
let reconnectTimer = null;

function connectWebSocket() {
  try {
    if (websocket.value) {
      try { websocket.value.close(); } catch (e) { }
    }
    connectionStatus.value = 'connecting';
    connectionStatusText.value = '连接中...';

    const wsUrl = 'ws://192.168.181.251:8081/ws/game'; // 按你后端实际端口
    websocket.value = new WebSocket(wsUrl);
    websocket.value.onopen = onOpen;
    websocket.value.onmessage = onMessage;
    websocket.value.onclose = onClose;
    websocket.value.onerror = onError;
  } catch (error) {
    connectionStatus.value = 'error';
    connectionStatusText.value = '连接错误';
    resetReconnection();
  }
}

function disconnectWebSocket() {
  if (websocket.value) {
    try { websocket.value.close(); } catch (e) { }
  }
  isConnected.value = false;
  connectionStatus.value = 'disconnected';
  connectionStatusText.value = '未连接';
  resetReconnection();
}

function resetReconnection() {
  reconnectAttempts.value = 0;
  if (reconnectTimer) {
    clearTimeout(reconnectTimer);
    reconnectTimer = null;
  }
}


function onMessage(event) {
  try {
    const data = JSON.parse(event.data);
    // 根据data.type处理消息
    // 例如：if (data.type === "xxx") { ... }

    if (data.type === "round_end_result") {
      const myUid = getData('multiGame_userInfo')?.uid;
      let myCards = [];
      let enemyCards = [];
      if (String(data.uid1) === String(myUid)) {
        myCards = data.list1.filter(item => item.cardNum > 0);
        enemyCards = data.list2.filter(item => item.cardNum > 0);
      } else if (String(data.uid2) === String(myUid)) {
        myCards = data.list2.filter(item => item.cardNum > 0);
        enemyCards = data.list1.filter(item => item.cardNum > 0);
      }
      console.log('本回合我打出的牌:', myCards);
      console.log('本回合对手打出的牌:', enemyCards);
      // TODO: 这里可以触发UI更新或动画

      const battleCard = enemyList.find(item => item.cardType === 'battle' && item.cardName);
      const defenseCard = enemyList.find(item => item.cardType === 'defense' && item.cardName);
      // profit/decrease 可能有多个，这里只取第一个
      const profitOrDecreaseCard = enemyList.find(item =>
        (item.cardType === 'profit' || item.cardType === 'decrease') && item.cardName
      );

      // 构建顺序
      const newRow = [
        battleCard ? battleCard.cardName : "cardBack",
        defenseCard ? defenseCard.cardName : "cardBack",
        profitOrDecreaseCard ? profitOrDecreaseCard.cardName : "cardBack"
      ];

      // 替换第二行
      gameState_one.value.cardGrid[1] = newRow;
    }


    // 监听 game_over 广播
    if (data.type === "game_over") {
      // 你自己的uid
      const uid = getData('multiGame_userInfo')?.uid
      if (data.winner_id == -1) {
        gameResult.value = "draw"
      } else if (String(data.winner_id) === String(uid)) {
        gameResult.value = "win"
      } else {
        gameResult.value = "lose"
      }
      showGameResult.value = true
      isGameOver.value = true
      clearInterval(countdownInterval)
      clearTimeout(turnTimeout)
    }


    //解析开卡包等逻辑
    if (data.type === "open_card_groups_result" && data.success && Array.isArray(data.cards)) {
      // 清空全局数组
      backendCardNames = [];

      // 解析并添加，每种卡牌按cardNum数量依次push
      data.cards.forEach(card => {
        for (let i = 0; i < card.cardNum; i++) {
          backendCardNames.push(card.cardName);
        }
      });

      console.log(backendCardNames);
    }
    if (data.type === "round_begin_result") {
      // 获取本地roomId和uid
      const roomId = getData('current_game_room')?.roomId;
      const uid = getData('multiGame_userInfo')?.uid;

      // 初始化临时变量
      let roundBeginData = null;

      // 判断roomNumber（消息中叫roomNumber，而不是roomId）和本地roomId是否一致
      // player1
      if (
        data.player1 &&
        data.player1.roomNumber === roomId &&
        String(data.uid1) === String(uid)
      ) {
        roundBeginData = {
          hp: data.player1.hp,
          cards: data.player1.cards,
          wealthy: data.player1.wealthy,
          statusesBegin: data.player1.statusesBegin,
          shield: data.player1.shield,
          hpMax: data.player1.hpMax,
          shieldMax: data.player1.shieldMax
        };
      }
      // player2
      else if (
        data.player2 &&
        data.player2.roomNumber === roomId &&
        String(data.uid2) === String(uid)
      ) {
        roundBeginData = {
          hp: data.player2.hp,
          cards: data.player2.cards,
          wealthy: data.player2.wealthy,
          statusesBegin: data.player2.statusesBegin,
          shield: data.player2.shield,
          hpMax: data.player2.hpMax,
          shieldMax: data.player2.shieldMax
        };
      }
      // 否则不保存
    }

    if (data.type === "round_begin_result") {
      // 获取本地roomId和uid
      const roomId = getData('current_game_room')?.roomId;
      const uid = getData('multiGame_userInfo')?.uid;

      // 初始化临时变量
      let roundBeginData = null;

      // 判断roomNumber（消息中叫roomNumber，而不是roomId）和本地roomId是否一致
      // player1



      if (data.player1 &&
        data.player1.roomNumber === roomId) {
        if (String(data.uid1) === String(uid)) {
          roundBeginData = [{
            hp: data.player1.hp,
            cards: data.player1.cards,
            wealthy: data.player1.wealthy,
            statusesBegin: data.player1.statusesBegin,
            shield: data.player1.shield,
            hpMax: data.player1.hpMax,
            shieldMax: data.player1.shieldMax
          }, {
            hp: data.player2.hp,
            cards: data.player2.cards,
            wealthy: data.player2.wealthy,
            statusesBegin: data.player2.statusesBegin,
            shield: data.player2.shield,
            hpMax: data.player2.hpMax,
            shieldMax: data.player2.shieldMax
          }
          ]
        }
        else if (String(data.uid2) === String(uid)) {
          roundBeginData = [{

            hp: data.player2.hp,
            cards: data.player2.cards,
            wealthy: data.player2.wealthy,
            statusesBegin: data.player2.statusesBegin,
            shield: data.player2.shield,
            hpMax: data.player2.hpMax,
            shieldMax: data.player2.shieldMax
          }, {
            hp: data.player1.hp,
            cards: data.player1.cards,
            wealthy: data.player1.wealthy,
            statusesBegin: data.player1.statusesBegin,
            shield: data.player1.shield,
            hpMax: data.player1.hpMax,
            shieldMax: data.player1.shieldMax
          }
          ]
        }
      }

      if (roundBeginData.length > 0) {
        // 写入己方
        gameState_one.value.ally.health = roundBeginData[0].hp;
        gameState_one.value.ally.maxHealth = roundBeginData[0].hpMax;
        gameState_one.value.ally.armor = roundBeginData[0].shield;
        gameState_one.value.ally.maxArmor = roundBeginData[0].shieldMax;
        coins.value = roundBeginData[0].wealthy;
        gameState_one.value.ally.effects = roundBeginData[0].statusesBegin;
        // 可以根据需要扩展，这里只写数值型，效果数组如有需要可额外处理

        // 写入敌方
        gameState_one.value.enemy.health = roundBeginData[1].hp;
        gameState_one.value.enemy.maxHealth = roundBeginData[1].hpMax;
        gameState_one.value.enemy.armor = roundBeginData[1].shield;
        gameState_one.value.enemy.maxArmor = roundBeginData[1].shieldMax;
        gameState_one.value.ally.effects = roundBeginData[1].statusesBegin;

        //刷新渲染
        // 重新绘制
        updateStatus(true, roundBeginData[0].hp, roundBeginData[0].shield, roundBeginData[0].hpMax, roundBeginData[0].shieldMax);
        updateStatus(false, roundBeginData[1].hp, roundBeginData[1].shield, roundBeginData[1].hpMax, roundBeginData[1].shieldMax);
        updateEffects(true, roundBeginData[0].statusesBegin);
        updateEffects(false, roundBeginData[1].statusesBegin);

      }
    }

    console.log('收到消息:', data);
  } catch (error) {
    console.error('解析消息失败', error, event.data);
  }
}

//刷新绘制生命值护甲
function updateStatus(isAlly, newHealth, newArmor, newMaxHealth, newMaxArmor) {
  // 获取场景对象
  const scene = battleScene && battleScene.scene && battleScene.scene.scenes[0];
  if (!scene) return;

  // 己方
  if (isAlly) {
    // 先移除旧的血条和护甲条
    if (scene.allyHealthBar) scene.allyHealthBar.destroy();
    if (scene.allyArmorBar) scene.allyArmorBar.destroy();
    if (scene.allyHpText) scene.allyHpText.destroy();
    if (scene.allyArmorText) scene.allyArmorText.destroy();

    // 重新绘制
    const allyAvatarY = scene.cameras.main.height - 100;
    const allyBarX = 250;
    const healthWidth = (newHealth / newMaxHealth) * 200;
    const armorWidth = (newArmor / newMaxArmor) * 200;

    scene.allyHealthBar = scene.add.rectangle(allyBarX, allyAvatarY - 25, healthWidth, 30, 0x38A169);
    scene.allyArmorBar = scene.add.rectangle(allyBarX, allyAvatarY + 25, armorWidth, 30, 0x3182CE);

    scene.allyHpText = scene.add.text(allyBarX, allyAvatarY - 25, `HP: ${newHealth}`, {
      fontSize: '16px', color: '#ffffff', resolution: 2,
    }).setOrigin(0.5);

    scene.allyArmorText = scene.add.text(allyBarX, allyAvatarY + 25, `Armor: ${newArmor}`, {
      fontSize: '16px', color: '#ffffff', resolution: 2,
    }).setOrigin(0.5);
  } else {
    // 敌方
    if (scene.enemyHealthBar) scene.enemyHealthBar.destroy();
    if (scene.enemyArmorBar) scene.enemyArmorBar.destroy();
    if (scene.enemyHpText) scene.enemyHpText.destroy();
    if (scene.enemyArmorText) scene.enemyArmorText.destroy();

    const enemyAvatarY = 100;
    const enemyBarX = scene.cameras.main.width - 250;
    const healthWidth = (newHealth / newMaxHealth) * 200;
    const armorWidth = (newArmor / newMaxArmor) * 200;

    scene.enemyHealthBar = scene.add.rectangle(enemyBarX, enemyAvatarY - 25, healthWidth, 30, 0x38A169);
    scene.enemyArmorBar = scene.add.rectangle(enemyBarX, enemyAvatarY + 25, armorWidth, 30, 0x3182CE);

    scene.enemyHpText = scene.add.text(enemyBarX, enemyAvatarY - 25, `HP: ${newHealth}`, {
      fontSize: '16px', color: '#ffffff', resolution: 2,
    }).setOrigin(0.5);

    scene.enemyArmorText = scene.add.text(enemyBarX, enemyAvatarY + 25, `Armor: ${newArmor}`, {
      fontSize: '16px', color: '#ffffff', resolution: 2,
    }).setOrigin(0.5);
  }
}
//刷新绘制状态栏
function updateEffects(isAlly, effects) {
  // 获取scene
  const scene = battleScene && battleScene.scene && battleScene.scene.scenes[0];
  if (!scene) return;

  // 先清理以前的buff图标
  if (!scene.allyBuffIcons) scene.allyBuffIcons = [];
  if (!scene.enemyBuffIcons) scene.enemyBuffIcons = [];

  if (isAlly) {
    scene.allyBuffIcons.forEach(icon => icon.destroy());
    scene.allyBuffIcons = [];
  } else {
    scene.enemyBuffIcons.forEach(icon => icon.destroy());
    scene.enemyBuffIcons = [];
  }

  // 重新绘制
  const spacing = 60;
  const iconSize = 50;
  if (isAlly) {
    const allyAvatarY = scene.cameras.main.height - 100;
    const allyBarX = 250;
    const allyStatusBarY = allyAvatarY - 80;
    effects.forEach((effectKey, index) => {
      const buff = buffs.find(b => b.key === effectKey);
      if (buff) {
        const iconX = allyBarX - 160 + (index * spacing) + 100;
        const icon = scene.add.image(iconX, allyStatusBarY, buff.key)
          .setDisplaySize(iconSize, iconSize)
          .setOrigin(0.5, 0.5)
          .setData('type', effectKey);
        scene.allyBuffIcons.push(icon);
      }
    });
  } else {
    const enemyAvatarY = 100;
    const enemyBarX = scene.cameras.main.width - 250;
    const enemyStatusBarY = enemyAvatarY + 80;
    effects.forEach((effectKey, index) => {
      const buff = buffs.find(b => b.key === effectKey);
      if (buff) {
        const iconX = enemyBarX - 160 - (index * spacing) + 150;
        const icon = scene.add.image(iconX, enemyStatusBarY, buff.key)
          .setDisplaySize(iconSize, iconSize)
          .setOrigin(0.5, 0.5)
          .setData('type', effectKey);
        scene.enemyBuffIcons.push(icon);
      }
    });
  }
}

function onClose(event) {
  isConnected.value = false;
  if (event.code === 1000 || reconnectAttempts.value >= maxReconnectAttempts) {
    connectionStatus.value = 'disconnected';
    connectionStatusText.value = '未连接';
  } else {
    connectionStatus.value = 'connecting';
    connectionStatusText.value = `重连中(${reconnectAttempts.value + 1}/${maxReconnectAttempts})...`;
    reconnectAttempts.value++;
    reconnectTimer = setTimeout(connectWebSocket, reconnectAttempts.value * 1000);
  }
}

function onError(event) {
  connectionStatus.value = 'error';
  connectionStatusText.value = '连接错误';
}

function sendMessage(message) {
  if (websocket.value && isConnected.value) {
    try {
      websocket.value.send(JSON.stringify(message));
    } catch (err) {
      console.error('发送消息失败', err);
    }
  } else {
    console.error('WebSocket未连接，无法发送消息');
  }
}
// 回合时间（秒）
const TURN_DURATION = 30 * 1000
// 结算延迟（毫秒）
const SETTLE_DELAY = 5 * 1000
//回合数
const turnCount = ref(0);


let turnTimeout = null;
let countdownInterval = null;
let timerEl = null;

const countdown = ref(TURN_DURATION / 1000)
const round = ref(1)        // 当前回合，从1开始
const maxRound = 20         // 总回合数（可根据实际改）


//对战双方游戏状态
const gameState_one = ref({
  // 己方角色状态
  ally: {
    health: 20,
    maxHealth: 20,
    armor: 10,
    maxArmor: 10,
    effects: [], // 状态效果数组
  },

  // 敌方角色状态
  enemy: {
    health: 20,
    maxHealth: 20,
    armor: 10,
    maxArmor: 10,
    effects: [], // 状态效果数组
  },

  // 卡牌网格 3*4，初始化为全是 'cardBack'
  cardGrid: Array(4).fill(null).map(() => Array(3).fill('cardBack'))
});


// 禁止/恢复页面滚动
function disablePageScroll() { document.body.style.overflow = 'hidden'; }
function enablePageScroll() { document.body.style.overflow = ''; }

// 强制滚动到第一个 Phaser 容器
function scrollToFirst() {
  const el = document.querySelector('.screen-wrapper > .screens .screen');
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function rearrangeGrid(grid, backValue = 'cardBack') {
  // 1. 提取原第4排
  const extracted = grid[3].slice();   // 记下原第四排

  // 2. 构造新网格
  const newGrid = [
    grid[0].slice(),              // 新第1排：原第1排
    grid[1].slice(),              // 新第2排：原第2排
    grid[3].slice(),              // 新第3排：原第4排
    Array(3).fill(backValue),     // 新第4排：全'cardBack'
  ];

  return { newGrid, extracted };
}

// 结算逻辑：根据你的 game1/game2 场景状态来写
function settlement() {
  console.log('执行回合结算！')
  // …在这里调用你的分数计算或状态重置…
  // 1. 调用重排函数，拿到新的网格和提取出的卡牌
  const { newGrid, extracted } = rearrangeGrid(gameState_one.value.cardGrid)

  const roomId = getData('current_game_room')?.roomId;
  const uid = getData('multiGame_userInfo')?.uid;

  console.log(extracted);

  if (roomId && uid && extracted && extracted.length > 0) {
    sendMessage({
      type: "RoundEnd",
      room: {
        roomId: roomId,
        uid: uid,
        cardList1: extracted // string数组
      }
    });
  }
  //
  //
  //
  //

  // 2. 用新网格更新组件状态
  gameState_one.value.cardGrid = newGrid

  // 3. （可选）把 extracted 发给后端、或者存到另一个 ref 里显示
  // console.log('提取出的卡牌：', extracted)

  // 4. 刷新页面
  if (battleScene && battleScene.scene && battleScene.scene.scenes[0]) {
    const sceneObj = battleScene.scene.scenes[0];
    const grid = gameState_one.value.cardGrid;
    for (let row = 0; row < grid.length; row++) {
      for (let col = 0; col < grid[row].length; col++) {
        updateBattleFieldDisplay(sceneObj, row, col, grid[row][col]);
      }
    }
  }
}

// 回合结束时的流程
function onTurnEnd() {
  if (isGameOver.value) return
  clearInterval(countdownInterval)
  settlement()
  if (round.value < maxRound) {
    round.value++
    startTurn()
    // updateGold(5)
    // coins.value += 5
  } else {
    // 游戏结束，可以加其他逻辑
    // alert('游戏结束！')
  }
}

// 启动（或重启）一个回合
function startTurn() {
  if (isGameOver.value) return;
  clearInterval(countdownInterval)
  clearTimeout(turnTimeout)

  countdown.value = TURN_DURATION / 1000

  countdownInterval = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      countdown.value = 0
      clearInterval(countdownInterval)
    }
  }, 1000)

  turnTimeout = setTimeout(() => {
    countdown.value = 0
    onTurnEnd()
  }, TURN_DURATION)
}
//-----------------------------------------
let buySlot1Animating = false
let buySlot1OriginalX

// 添加角色选择相关的响应式变量
const selectedPoet = ref('libai') // 默认李白，可以通过路由参数或props传入


const isStackingMode = ref(false)
const gameState = ref({ gold: 0 })

// 更新金币数量的函数
const updateGold = (amount) => {
  gameState.value.gold += amount
  if (gameState.value.gold < 0) gameState.value.gold = 0
  console.log('当前金币:', gameState.value.gold)
}

// 卡牌图片资源列表
const cardImages = [
  { key: 'card_pack_poem', src: new URL('../../assets/cards/诗意卡包(2).png', import.meta.url).href },
  { key: 'card_pack_poet', src: new URL('../../assets/cards/诗人卡包(1).png', import.meta.url).href },
  { key: 'card_worker', src: new URL('../../assets/cards/书生.png', import.meta.url).href },
  { key: 'factory', src: new URL('../../assets/cards/工厂/书斋.png', import.meta.url).href },
  { key: 'unknown_card', src: new URL('../../assets/cards/未知卡片.png', import.meta.url).href },

  { key: 'baijuyi', src: new URL('../../assets/cards/诗人/白居易.png', import.meta.url).href },
  { key: 'dufu', src: new URL('../../assets/cards/诗人/杜甫.png', import.meta.url).href },
  { key: 'libai', src: new URL('../../assets/cards/诗人/李白.png', import.meta.url).href },
  { key: 'lishangyin', src: new URL('../../assets/cards/诗人/李商隐.png', import.meta.url).href },
  { key: 'liuyuxi', src: new URL('../../assets/cards/诗人/刘禹锡.png', import.meta.url).href },
  { key: 'luyou', src: new URL('../../assets/cards/诗人/陆游.png', import.meta.url).href },
  { key: 'sushi', src: new URL('../../assets/cards/诗人/苏轼.png', import.meta.url).href },
  { key: 'taoyuanming', src: new URL('../../assets/cards/诗人/陶渊明.png', import.meta.url).href },
  { key: 'wanganshi', src: new URL('../../assets/cards/诗人/王安石.png', import.meta.url).href },
  { key: 'wangwei', src: new URL('../../assets/cards/诗人/王维.png', import.meta.url).href },
  { key: 'xinqiji', src: new URL('../../assets/cards/诗人/辛弃疾.png', import.meta.url).href },
  { key: 'yanshu', src: new URL('../../assets/cards/诗人/晏殊.png', import.meta.url).href },

  { key: 'love', src: new URL('../../assets/cards/诗意/爱情.png', import.meta.url).href },
  { key: 'sad', src: new URL('../../assets/cards/诗意/悲.png', import.meta.url).href },
  { key: 'spring', src: new URL('../../assets/cards/诗意/春.png', import.meta.url).href },
  { key: 'danbo', src: new URL('../../assets/cards/诗意/淡泊.png', import.meta.url).href },
  { key: 'home', src: new URL('../../assets/cards/诗意/故乡.png', import.meta.url).href },
  { key: 'yellowriver', src: new URL('../../assets/cards/诗意/黄河.png', import.meta.url).href },
  { key: 'fire', src: new URL('../../assets/cards/诗意/火.png', import.meta.url).href },
  { key: 'wine', src: new URL('../../assets/cards/诗意/酒.png', import.meta.url).href },
  { key: 'byebye', src: new URL('../../assets/cards/诗意/离别.png', import.meta.url).href },
  { key: 'liu', src: new URL('../../assets/cards/诗意/柳.png', import.meta.url).href },
  { key: 'bird', src: new URL('../../assets/cards/诗意/鸟.png', import.meta.url).href },
  { key: 'autumn', src: new URL('../../assets/cards/诗意/秋.png', import.meta.url).href },
  { key: 'sun', src: new URL('../../assets/cards/诗意/日.png', import.meta.url).href },
  { key: 'mountain', src: new URL('../../assets/cards/诗意/山.png', import.meta.url).href },
  { key: 'water', src: new URL('../../assets/cards/诗意/水.png', import.meta.url).href },
  { key: 'missing', src: new URL('../../assets/cards/诗意/思念.png', import.meta.url).href },
  { key: 'flower', src: new URL('../../assets/cards/诗意/桃花.png', import.meta.url).href },
  { key: 'goose', src: new URL('../../assets/cards/诗意/雁.png', import.meta.url).href },
  { key: 'friend', src: new URL('../../assets/cards/诗意/友情.png', import.meta.url).href },
  { key: 'rain', src: new URL('../../assets/cards/诗意/雨.png', import.meta.url).href },
  { key: 'moon', src: new URL('../../assets/cards/诗意/月.png', import.meta.url).href },
  { key: 'war', src: new URL('../../assets/cards/诗意/战争.png', import.meta.url).href },
  { key: 'longriver', src: new URL('../../assets/cards/诗意/长江.png', import.meta.url).href },
  { key: 'bamboo', src: new URL('../../assets/cards/诗意/竹.png', import.meta.url).href },
  { key: 'zhuangzhinanchou', src: new URL('../../assets/cards/诗意/壮志难酬.png', import.meta.url).href },
  { key: 'nature', src: new URL('../../assets/cards/诗意/自然.png', import.meta.url).href },

  { key: 'jiangjinjiu', src: new URL('../../assets/cards/诗词/将进酒.png', import.meta.url).href },
  { key: 'shudaonan', src: new URL('../../assets/cards/诗词/蜀道难.png', import.meta.url).href },
  { key: 'xinglunan', src: new URL('../../assets/cards/诗词/行路难.png', import.meta.url).href },
  { key: 'huanghelousongmenghaoranzhiguangling', src: new URL('../../assets/cards/诗词/黄鹤楼送孟浩然之广陵.png', import.meta.url).href },

  { key: 'shizhisaishang', src: new URL('../../assets/cards/诗词/使至塞上.png', import.meta.url).href },
  { key: 'xiangsi', src: new URL('../../assets/cards/诗词/相思.png', import.meta.url).href },
  { key: 'shanjuqiuming', src: new URL('../../assets/cards/诗词/山居秋暝.png', import.meta.url).href },
  { key: 'zhuliguan', src: new URL('../../assets/cards/诗词/竹里馆.png', import.meta.url).href },

  { key: 'shuidiaogetou_mingyuejishiyou', src: new URL('../../assets/cards/诗词/水调歌头·明月几时有.png', import.meta.url).href },
  { key: 'chibifu', src: new URL('../../assets/cards/诗词/赤壁赋.png', import.meta.url).href },
  { key: 'jichengtansiyeyou', src: new URL('../../assets/cards/诗词/记承天寺夜游.png', import.meta.url).href },
  { key: 'dingfengbo_motingchuanlindayesheng', src: new URL('../../assets/cards/诗词/定风波·莫听穿林打叶声.png', import.meta.url).href },

]

//合成表
const recipeMapping = {
  'autumn_bird': 'goose',
  'bird_bird': 'friend',
  'fire_fire': 'war',
  'fire_moon': 'sun',
  'friend_missing': 'love',
  'home_byebye': 'missing',
  'mountain_water': 'nature',
  'nature_spring': 'flower',
  'nature_water': 'bamboo',
  'rain_rain': 'yellowriver',
  'spring_water': 'liu',
  'bamboo_water': 'danbo',
  'fire_water': 'wine',
  'water_water': 'rain',
  'autumn_wine': 'zhuangzhinanchou',
  'water_yellowriver': 'longriver',
  'moon_moon': 'home',
  'home_moon': 'byebye',
  'autumn_autumn': 'sad',
};
const craftingRecipes = {
  'bird_libai_mountain': 'shudaonan',
  'byebye_libai_longriver': 'huanghelousongmenghaoranzhiguangling',
  'libai_mountain_wine': 'xinglunan',
  'libai_wine_yellowriver': 'jiangjinjiu',

  'goose_wangwei_yellowriver': 'shizhisaishang',
  'missing_spring_wangwei': 'xiangsi',
  'autumn_rain_wangwei': 'shanjuqiuming',
  'bamboo_danbo_wangwei': 'zhuliguan',

  'moon_sushi_water': 'shuidiaogetou_mingyuejishiyou',
  'fire_mountain_sushi': 'chibifu',
  'moon_sushi_zhuangzhinanchou': 'jichengtansiyeyou',
  'bamboo_rain_sushi': 'dingfengbo_motingchuanlindayesheng'

};

const cardDescriptions = {
  // 诗意卡片描述
  'love': '爱情：恢复5点血量，失去4点金币，下2回合开始时获得3点护盾且免疫所有新增减益,当对手卡牌的附加效果包含针对你的负面状态时，免疫该附加效果（卡牌的主要功能仍正常生效，且不会触发新的负面状态）。，同时每回合开始时减少3枚金币。',
  'sad': '悲：令对手弃1张手牌。若对手手牌少于3张，失去3点护盾且下一回合无法获得护盾。若对手手牌大于等于5张，那么获得一点金币',
  'spring': '春天：恢复2点血量。若本回合未受攻击，下回合开始时获得3金币，且抽1张1-3费牌。',
  'danbo': '淡泊：获得4点护盾。若使用者护盾≥5且本回合没使用战斗类卡牌，恢复3点血量且护盾+1,如果已经满护盾,那么上限加1，同时下回合战斗类牌面值减2。',
  'home': '故乡：获得2点护盾。若使用后护盾满了，恢复3点血量且下回合抽1张1-2费牌。',
  'yellowriver': '黄河：造成4点真实伤害。若对方护盾≥5，摧毁所有护盾,该效果不可被免疫。',
  'fire': '火：造成1点伤害。若对方无护盾，下回合抽1张1-2牌并使其下回合战斗类卡牌伤害+1。',
  'wine': '酒：造成2点伤害。若本回合受到攻击，抽2张1-2费牌且下回合战斗类卡牌效果+1。',
  'byebye': '离别：令对手弃2张手牌。若对手护盾小于等于3，造成4点真实伤害且下两回合获得金币-2。',
  'liu': '柳：获得2点护盾。若本回合受到攻击，恢复2点护盾并免疫下回合1点伤害。',
  'bird': '鸟：造成1点伤害。若本回合对面使用防守类卡，追加1点真实伤害。',
  'autumn': '秋：对手金币-2。若其本回合未获得新护盾，弃其1张牌并让他失去1点护盾。',
  'sun': '日：造成2点伤害。伤害前，若对方有护盾，额外破坏2点护盾并使其下回合防守效果减半。',
  'mountain': '山：获得1点护盾。若本回合未受伤害，下三回合各+1护盾。',
  'water': '水：获得1点护盾。若未使用战斗类卡牌，恢复2点血量。',
  'missing': '思念：造成5点伤害。若对方血量≤10，追加3点真实伤害且无视免疫效果。',
  'flower': '桃花：恢复3点血量，下三回合各获得2点护盾且每回合回1点血。',
  'goose': '雁：获得2点护盾。下两回合受到伤害减少1点，若护盾被破则反弹1点真实伤害,同时移除本buff。',
  'friend': '友情：随机获得1张1-2费牌。若手牌少于3张，再抽2张牌，但下回合攻击卡牌面值-1。',
  'rain': '雨：造成2点伤害。若对方下回合使用防守卡，该卡无效且追加3点伤害。',
  'moon': '月：令对手失去1点护盾并随机弃1张牌',
  'war': '战争：造成2点伤害。若对方下回合使用了进攻，再造成3点真实伤害。',
  'longriver': '长江：获得5点护盾。护盾上限+3。下三回合每回合恢复4点血量。',
  'bamboo': '竹：造成3点真实伤害。若未使用其他卡，下回合开始时抽3张1-2费牌并破坏对手1点护盾。',
  'zhuangzhinanchou': '壮志难酬：对手本回合无法获得护盾。若其下回合获得一定量护盾，则同时给己方添加等量护盾。',
  'nature': '自然：恢复两点血量.若护盾≥3，获得3点护盾。否则下回合受到伤害时免疫1次破盾的额外伤害。',
};

class TooltipManager {
  constructor(scene) {
    this.scene = scene;
    this.tooltipTimer = null;
    this.tooltipDelay = 400; // 悬停多久后显示提示（毫秒）

    // 创建工具提示容器
    this.tooltip = scene.add.container(0, 0).setVisible(false).setDepth(1000);

    // 创建工具提示背景
    this.tooltipBg = scene.add.rectangle(0, 0, 200, 80, 0x000000, 0.8)
      .setStrokeStyle(1, 0xffffff, 0.8)
      .setOrigin(0.5);

    // 创建工具提示文本
    this.tooltipText = scene.add.text(0, 0, '', {
      fontSize: '14px',
      color: '#ffffff',
      align: 'center',
      lineSpacing: 5,
      wordWrap: { width: 180 },
      padding: { x: 10, y: 5 }
    }).setOrigin(0.5);

    // 添加到容器
    this.tooltip.add([this.tooltipBg, this.tooltipText]);
  }

  // 添加卡片悬停提示功能
  addTooltipToCard(card) {
    const self = this;

    // 鼠标悬停事件
    card.on('pointerover', function (pointer) {
      // 清除之前的计时器
      if (self.tooltipTimer) {
        clearTimeout(self.tooltipTimer);
      }

      // 创建新计时器
      self.tooltipTimer = setTimeout(() => {
        const cardType = card.getData('type');
        const description = cardDescriptions[cardType] || `${cardType}卡`;

        // 更新工具提示文本
        self.tooltipText.setText(description);

        // 调整背景大小以适应文本
        const padding = 20;
        self.tooltipBg.width = self.tooltipText.width + padding * 2;
        self.tooltipBg.height = self.tooltipText.height + padding;

        // 定位工具提示（在卡片右侧）
        const tooltipX = card.x + card.displayWidth / 2 + 70;
        const tooltipY = card.y;

        // 检查是否超出屏幕右侧
        const rightEdge = tooltipX + self.tooltipBg.width / 2;
        if (rightEdge > self.scene.scale.width) {
          // 如果超出，则显示在卡片左侧
          self.tooltip.setPosition(card.x - card.displayWidth / 2 - 70, tooltipY);
        } else {
          self.tooltip.setPosition(tooltipX, tooltipY);
        }

        self.tooltip.setVisible(true);
      }, self.tooltipDelay);
    });

    // 鼠标移出事件
    card.on('pointerout', function () {
      if (self.tooltipTimer) {
        clearTimeout(self.tooltipTimer);
        self.tooltipTimer = null;
      }
      self.tooltip.setVisible(false);
    });

    // 拖拽开始事件
    card.on('dragstart', function () {
      if (self.tooltipTimer) {
        clearTimeout(self.tooltipTimer);
        self.tooltipTimer = null;
      }
      self.tooltip.setVisible(false);
    });

    return card;
  }

  // 隐藏工具提示
  hide() {
    if (this.tooltipTimer) {
      clearTimeout(this.tooltipTimer);
      this.tooltipTimer = null;
    }
    this.tooltip.setVisible(false);
  }
}
// 检查两张卡是否可以合成
const checkRecipe = (card1Type, card2Type) => {
  // 确保类型按字母顺序排序以保持一致性
  const types = [card1Type, card2Type].sort()
  const recipeKey = types.join('_')
  return recipeMapping[recipeKey]
}

// 检查三张卡是否可以合成
const checkCrafting = (cards) => {
  if (cards.length !== 3) return null;
  const types = cards.map(card => card.getData('type')).sort()
  const recipeKey = types.join('_')
  console.log('Crafting Recipe Key:', recipeKey); // 调试信息
  return craftingRecipes[recipeKey]
}

// 卡牌价格
const cardPrices = {
  card_pack_poem: 10,

  love: 7,
  sad: 2,
  spring: 1,
  danbo: 4,
  home: 2,
  yellowriver: 4,
  fire: 1,
  wine: 2,
  byebye: 3,
  liu: 2,
  bird: 1,
  autumn: 1,
  sun: 2,
  mountain: 1,
  water: 1,
  missing: 5,
  flower: 3,
  goose: 2,
  friend: 2,
  rain: 2,
  moon: 1,
  war: 2,
  longriver: 5,
  bamboo: 3,
  zhuangzhinanchou: 3,
  nature: 2,
};
let backendCardNames = [];

let lastCoinValue = 100
const coins = ref(0) // 初始金币数量

// 购买诗意卡包
const handleBuyPack = () => {
  const packPrice = 5
  if (coins.value >= packPrice) {
    coins.value -= packPrice

    updateGold(-packPrice)

    const scene = game.scene.scenes[0]
    //位置1：向后端发送结构化消息
    sendMessage({
      type: "openCardGroups",
      room: {
        //uid: `getCurrentUid()` 

        uid: getData('multiGame_userInfo')?.uid
      }
    });

    // 在随机位置创建卡包
    const x = Math.random() * (scene.scale.width - 100) + 50
    const y = Math.random() * (scene.scale.height - 140 - 180) + 250

    const cardPack = scene.physics.add.image(x, y, 'card_pack_poem')
      .setDisplaySize(100, 140)
      .setInteractive({ cursor: 'pointer', useHandCursor: true, draggable: true })
      .setCollideWorldBounds(true)
      .setBounce(0.8)
      .setData('clickCount', 0)
      .setData('type', 'card_pack_poem')
      .setData('isDragging', false)
      .setData('pointerDown', false)
      .setData('dragStartX', 0)
      .setData('dragStartY', 0)

    scene.input.setDraggable(cardPack)

    // 添加指针按下事件
    cardPack.on('pointerdown', (pointer) => {
      cardPack.setData('pointerDown', true)
      cardPack.setData('dragStartX', pointer.x)
      cardPack.setData('dragStartY', pointer.y)
    })

    // 添加拖动开始事件
    cardPack.on('dragstart', () => {
      cardPack.setData('isDragging', true)
    })

    // 添加拖动结束事件
    cardPack.on('dragend', () => {
      if (cardPack.getData('isDragging')) {
        setTimeout(() => {
          cardPack.setData('isDragging', false)
          cardPack.setData('pointerDown', false)
        }, 100)
      }
    })

    // 添加指针抬起事件
    cardPack.on('pointerup', (pointer) => {
      const isDragging = cardPack.getData('isDragging')
      const startX = cardPack.getData('dragStartX')
      const startY = cardPack.getData('dragStartY')
      const distance = Phaser.Math.Distance.Between(startX, startY, pointer.x, pointer.y)

      // 如果移动距离小于5像素且没有处于拖动状态，则认为是点击
      if (distance < 5 && !isDragging) {
        const clickCount = cardPack.getData('clickCount')

        if (clickCount === 0) {
          // 第一次点击：添加震动效果
          scene.tweens.add({
            targets: cardPack,
            x: cardPack.x + 5,
            duration: 50,
            yoyo: true,
            repeat: 3
          })
          cardPack.setData('clickCount', 1)
        } else {
          // 第二次点击：生成随机卡片并销毁卡包
          // const allCards = ['love', 'sad', 'spring', 'danbo', 'home', 'yellowriver', 'fire', 'wine',
          //   'byebye', 'liu', 'bird', 'autumn', 'sun', 'mountain', 'water', 'missing', 'flower',
          //   'goose', 'friend', 'rain', 'moon', 'war', 'longriver', 'bamboo', 'zhuangzhinanchou', 'nature']
          const numCards = 5

          // 创建闪光效果
          const flash = scene.add.sprite(cardPack.x, cardPack.y, 'card_pack_poem')
            .setScale(0.1)
            .setAlpha(0.8)
            .setTint(0xffffff)
            .setBlendMode(Phaser.BlendModes.ADD)

          scene.tweens.add({
            targets: flash,
            alpha: 0,
            scale: 1,
            duration: 500,
            onComplete: () => flash.destroy()
          })
          // 生成随机卡片
          for (let i = 0; i < numCards; i++) {
            const angle = (i / numCards) * Math.PI * 2
            const radius = 80
            const randomCard = backendCardNames[Math.floor(i)]

            const newX = cardPack.x + Math.cos(angle) * radius
            const newY = cardPack.y + Math.sin(angle) * radius

            const card = scene.physics.add.image(cardPack.x, cardPack.y, randomCard)
              .setDisplaySize(100, 140)
              .setInteractive({ cursor: 'pointer', useHandCursor: true })
              .setCollideWorldBounds(true)
              .setBounce(0.8)
              .setData('type', randomCard)
              .setData('id', Date.now().toString() + i)

            scene.input.setDraggable(card)
            scene.cards.push(card)

            // 添加卡片出现动画
            scene.tweens.add({
              targets: card,
              x: newX,
              y: newY,
              // scale: { from: 0.5, to: 1 },
              alpha: { from: 0.5, to: 1 },
              duration: 500,
              ease: 'Back.easeOut'
            })
          }

          // 销毁卡包
          scene.tweens.add({
            targets: cardPack,
            alpha: 0,
            scale: 0.5,
            duration: 300,
            onComplete: () => cardPack.destroy()
          })
        }
      }
      cardPack.setData('pointerDown', false)
    })
  }
}

let game = null

//-----------------------------------------
const currentScreen = ref(0);
const isScrolling = ref(false);

//buff列表
const buffs = [
  { key: 'armor_minus', src: new URL('../../assets/cards/buff/armor_minus.png', import.meta.url).href },
  { key: 'armor_plus', src: new URL('../../assets/cards/buff/armor_plus.png', import.meta.url).href },
  { key: 'attack_minus', src: new URL('../../assets/cards/buff/attack_minus.png', import.meta.url).href },
  { key: 'attack_plus', src: new URL('../../assets/cards/buff/attack_plus.png', import.meta.url).href },
  { key: 'bounce_back', src: new URL('../../assets/cards/buff/bounce_back.png', import.meta.url).href },
  { key: 'break_armor', src: new URL('../../assets/cards/buff/break_armor.png', import.meta.url).href },
  { key: 'cant_armor', src: new URL('../../assets/cards/buff/cant_armor.png', import.meta.url).href },
  { key: 'copy_armor', src: new URL('../../assets/cards/buff/copy_armor.png', import.meta.url).href },
  { key: 'gold_minus', src: new URL('../../assets/cards/buff/gold_minus.png', import.meta.url).href },
  { key: 'gold_plus', src: new URL('../../assets/cards/buff/gold_plus.png', import.meta.url).href },
  { key: 'heal', src: new URL('../../assets/cards/buff/heal.png', import.meta.url).href },
  { key: 'immune_damage_point', src: new URL('../../assets/cards/buff/immune_damage_point.png', import.meta.url).href },
  { key: 'immune_damage_time', src: new URL('../../assets/cards/buff/immune_damage_time.png', import.meta.url).href },
  { key: 'immune_debuff', src: new URL('../../assets/cards/buff/immune_debuff.png', import.meta.url).href },
  { key: 'rebound_armor', src: new URL('../../assets/cards/buff/rebound_armor.png', import.meta.url).href },
]

//映射
const cardToBuff = {
  'sun': ['armor_minus', 'armor_minus', 'armor_minus'],
  'spring': ['gold_plus'],
  'fire': ['attack_plus'],
  'mountain': ['armor_plus'],
  'sad': ['cant_armor'],
  'wine': ['attack_plus'],
  'liu': ['immune_damage_point'],
  'goose': ['rebound_armor'],
  'friend': ['attack_minus'],
  'rain_next': ['break_armor'],
  'war_next': ['bounce_back'],
  'nature': ['immune_damage_time', 'armor_plus', 'armor_plus', 'heal'],
  'byebye': ['gold_minus', 'gold_minus'],
  'flower': ['armor_plus', 'armor_plus', 'heal'],
  'zhuangzhinanchou_next': ['copy_armor'],
  'danbo': ['attack_minus', 'attack_minus'],
  'longriver': ['heal', 'heal', 'heal', 'heal'],
  'love': ['armor_plus', 'armor_plus', 'armor_plus', 'armor_plus', 'immune_debuff', 'gold_minus', 'gold_minus', 'gold_minus'],
}

// 添加buff描述对象
const buffDescriptions = {
  'armor_minus': '减少1点护甲',
  'armor_plus': '增加1点护甲',
  'attack_minus': '战斗类卡牌伤害-1',
  'attack_plus': '战斗类卡牌伤害+1',
  'bounce_back': '反弹',
  'break_armor': '护甲无效',
  'cant_armor': '护甲-1',
  'copy_armor': '获得与对方相同的护甲',
  'gold_minus': '金币-1',
  'gold_plus': '金币+1',
  'heal': 'HP+1',
  'immune_damage_point': '免疫破甲伤害的一点伤害',
  'immune_damage_time': '免疫破甲伤害',
  'immune_debuff': '负面效果免疫',
  'rebound_armor': '反弹对方造成的伤害',
};

const cardSlotMapping = {
  // BUFF槽位卡片
  'spring': 'buff',
  'autumn': 'buff',
  'moon': 'buff',
  'sad': 'buff',
  'home': 'buff',
  'friend': 'buff',
  'byebye': 'buff',
  'flower': 'buff',
  'love': 'buff',

  // 攻击槽位卡片
  'fire': 'attack',
  'bird': 'attack',
  'wine': 'attack',
  'sun': 'attack',
  'rain': 'attack',
  'war': 'attack',
  'bamboo': 'attack',
  'yellowriver': 'attack',
  'missing': 'attack',

  // 防御槽位卡片
  'mountain': 'defense',
  'water': 'defense',
  'liu': 'defense',
  'goose': 'defense',
  'nature': 'defense',
  'zhuangzhinanchou': 'defense',
  'danbo': 'defense',
  'longriver': 'defense',
}
// 验证卡片是否可以放入指定槽位
const canPlaceInSlot = (cardType, slotType) => {
  return cardSlotMapping[cardType] === slotType
}
const heads = [
  { key: 'aiboy', src: new URL('../../assets/cards/aiboy.png', import.meta.url).href },
  { key: 'aigirl', src: new URL('../../assets/cards/aiboy.png', import.meta.url).href },
]

//updateCard函数，添加对第一个场景的更新
const updateCard = (row, col, cardType) => {
  gameState_one.value.cardGrid[row][col] = cardType;

  if (battleScene && battleScene.scene.scenes[0]) {
    updateBattleFieldDisplay(battleScene.scene.scenes[0], row, col, cardType);
  }
};

// 添加更新战场显示的函数
const updateBattleFieldDisplay = (scene, row, col, cardType) => {
  const width = scene.cameras.main.width;
  const height = scene.cameras.main.height;
  const centerX = width / 2;
  const centerY = height / 2;

  // 计算卡槽位置（与创建时相同的逻辑）
  const slotWidth = 100;
  const slotHeight = 140;
  const horizontalGap = 60;
  const verticalGap = 20;

  const totalWidth = (slotWidth * 3) + (horizontalGap * 2);
  const totalHeight = (slotHeight * 4) + (verticalGap * 3);

  const startX = centerX - (totalWidth / 2);
  const startY = (height - totalHeight) / 2;

  const x = startX + (col * (slotWidth + horizontalGap));
  const y = startY + (row * (slotHeight + verticalGap));

  // 查找并更新对应位置的卡片
  const cardKey = `card_${row}_${col}`;
  const existingCard = scene.children.getByName(cardKey);

  if (existingCard) {
    // 如果卡片已存在，更新纹理
    existingCard.setTexture(cardType);
    existingCard.setDisplaySize(slotWidth, slotHeight);

    // 关键修复：更新卡片的类型数据，这样提示系统才能正确显示卡片描述
    existingCard.setData('type', cardType);

    // 添加更新动画
    scene.tweens.add({
      targets: existingCard,
      duration: 300,
      ease: 'Back.easeOut'
    });
  } else {
    console.log(`Card ${cardKey} not found in scene`);
  }
};

// const removeCardFromSlot = (row, col) => {
//   // 重置游戏状态
//   gameState_one.value.cardGrid[row][col] = 'cardBack';

//   // 更新显示
//   if (battleScene && battleScene.scene.scenes[0]) {
//     updateBattleFieldDisplay(battleScene.scene.scenes[0], row, col, 'cardBack');
//   }

//   console.log(`Removed card from slot [${row}][${col}]`);
// };



// 计算容器的 translateY，实现滚动切换
const containerStyle = computed(() => ({
  transform: `translateY(-${currentScreen.value * 100}vh)`,
}));

// 处理滚轮事件
const handleWheel = (event) => {
  if (isScrolling.value) return;

  const delta = event.deltaY;
  if (delta > 0 && currentScreen.value === 0) {
    goToScreen(1);
  } else if (delta < 0 && currentScreen.value === 1) {
    goToScreen(0);
  }
};

// 切换屏幕，并加防抖
const goToScreen = (idx) => {
  if (idx === currentScreen.value) return;
  isScrolling.value = true;
  currentScreen.value = idx;
  // 与 CSS transition 时长保持一致
  setTimeout(() => {
    isScrolling.value = false;
  }, 800);
};

// Phaser 容器引用
const screen0 = ref(null);
const screen1 = ref(null);

let battleScene = null // 添加战斗场景的引用
function onOpen() {
  isConnected.value = true;
  connectionStatus.value = 'connected';
  connectionStatusText.value = '已连接';
  reconnectAttempts.value = 0;

  // 连接成功后发送RoundBegin消息
  sendMessage({
    type: "RoundBegin",
    room: {
      roomId: getData('current_game_room')?.roomId,
      uid: getData('multiGame_userInfo')?.uid
    }
  });
}
onMounted(() => {
  connectWebSocket();

  console.log("hey");

  //页面初始化
  const commonConfig = {
    type: Phaser.AUTO,
    width: '100%',
    height: '100%',
    physics: { default: 'arcade' },
    audio: {
      disableWebAudio: true,  // 禁用 Web Audio
      noAudio: true          // 完全禁用音频
    }
  };

  timerEl = document.getElementById('countdown-timer');
  // 第一个 Phaser 实例：对战界面
  battleScene = new Phaser.Game({
    ...commonConfig,
    parent: screen0.value,
    scene: {

      //预加载
      preload() {
        // 只加载图片资源，不生成纹理
        cardImages.forEach(card => {
          this.load.image(card.key, card.src);
        });

        // 加载状态效果图片
        buffs.forEach(buff => {
          this.load.image(buff.key, buff.src);
        });

        heads.forEach(head => {
          this.load.image(head.key, head.src);
        });

      },


      create() {

        const graphics = this.add.graphics();

        // 绘制卡牌背面的花纹
        graphics.lineStyle(2, 0xC5A880); // 柔和古金边框
        graphics.fillStyle(0x7D1D29); // 深酒红背景
        graphics.fillRect(0, 0, 100, 140);
        graphics.strokeRect(0, 0, 100, 140);

        // 添加一些装饰图案
        graphics.lineStyle(1, 0xffd700);
        graphics.strokeRect(10, 10, 80, 120);
        graphics.beginPath();
        graphics.arc(50, 70, 30, 0, Math.PI * 2);
        graphics.strokePath();

        // 将绘制的图形生成为纹理
        graphics.generateTexture('cardBack', 100, 140);
        graphics.destroy();

        // 加载状态效果图片
        buffs.forEach(buff => {
          this.load.image(buff.key, buff.src);
        });


        // 获取游戏画布的中心点和尺寸
        const width = this.cameras.main.width;
        const height = this.cameras.main.height;
        const centerX = width / 2;
        const centerY = height / 2;

        // 创建牌桌外边框（浅褐色）
        const tableFrame = this.add.rectangle(
          centerX,
          centerY,
          width - 60,  // 左右各留30px边距，比原来的100px更宽
          height - 60, // 上下各留30px边距，比原来的100px更宽
          0xC5A880  // 淡雅米褐色
        ).setOrigin(0.5, 0.5);

        // 创建牌桌内部（米色）
        const tableInner = this.add.rectangle(
          centerX,
          centerY,
          width - 100, // 与外框保持20px的间距
          height - 100,
          0xF5EBE0  // 温暖米色
        ).setOrigin(0.5, 0.5);

        // 设置卡槽的尺寸和间距
        const slotWidth = 100;
        const slotHeight = 140;
        const horizontalGap = 60;
        const verticalGap = 20;

        // 计算整个卡槽区域的尺寸
        const totalWidth = (slotWidth * 3) + (horizontalGap * 2);
        const totalHeight = (slotHeight * 4) + (verticalGap * 3);

        // 计算起始位置（左上角第一个卡槽的位置）
        const startX = centerX - (totalWidth / 2);
        const startY = (height - totalHeight) / 2;

        // 创建列背景
        for (let col = 0; col < 3; col++) {
          let columnColor;
          switch (col) {
            case 0:
              columnColor = 0xA05252; // 红色
              break;
            case 1:
              columnColor = 0x6A8A9E; // 蓝色
              break;
            case 2:
              columnColor = 0x6E8B3D; // 绿色
              break;
          }

          // 创建列背景
          const columnX = startX - 20 + (col * (slotWidth + horizontalGap));
          const columnWidth = slotWidth + 40;
          const columnHeight = totalHeight + 40;

          this.add.rectangle(
            columnX,
            startY - 20,
            columnWidth,
            columnHeight,
            columnColor
          ).setOrigin(0, 0).setAlpha(0.4); // 设置半透明
        }

        // 创建卡槽网格
        for (let row = 0; row < 4; row++) {
          for (let col = 0; col < 3; col++) {
            const x = startX + (col * (slotWidth + horizontalGap));
            const y = startY + (row * (slotHeight + verticalGap));
            const cardType = gameState_one.value.cardGrid[row][col];

            const card = this.add.image(x, y, cardType)
              .setOrigin(0, 0)
              .setDisplaySize(slotWidth, slotHeight)
              .setName(`card_${row}_${col}`)
              .setData('type', cardType) // 保留类型数据，这是显示提示的关键
              .setInteractive(); // 保留交互性

            // 为卡片添加鼠标悬停和移出事件
            card.on('pointerover', () => {

              // 如果是有效卡片，显示提示
              if (cardType !== 'cardBack' && cardDescriptions[cardType]) {
                if (this.tooltipTimer) clearTimeout(this.tooltipTimer);

                this.tooltipTimer = setTimeout(() => {
                  // 计算提示位置 - 向右侧显示，除非右侧空间不足
                  const tooltipX = x + slotWidth + 50;
                  const tooltipY = y + slotHeight / 2;

                  this.showCardTooltip(tooltipX, tooltipY, cardDescriptions[cardType]);
                }, 400);
              }
            });

            card.on('pointerout', () => {
              // 隐藏提示
              if (this.tooltipTimer) clearTimeout(this.tooltipTimer);
              if (this.cardTooltip) this.cardTooltip.setVisible(false);
            });
          }
        }
        // 创建中央分界线
        const dividerLine = this.add.rectangle(60, centerY, width - 120, 4, 0xC5A880)
          .setOrigin(0, 0.5)
          .setAlpha(0.5); // 降低分界线透明度使其不那么显眼

        // 2. 创建己方单位（左下角）
        const allyAvatarY = height - 100;
        const allyBarX = 250;

        // 创建己方头像
        this.allyAvatar = this.add.image(
          100,
          allyAvatarY,
          'aiboy'
        )
          .setOrigin(0.5, 0.5)
          .setDisplaySize(80, 80)   // ← 指定宽高
          .setAlpha(0.8);

        // 己方血条和护甲条
        const allyHealthWidth = (gameState_one.value.ally.health / gameState_one.value.ally.maxHealth) * 200;
        const allyArmorWidth = (gameState_one.value.ally.armor / gameState_one.value.ally.maxArmor) * 200;
        const allyHealthBar = this.add.rectangle(allyBarX, allyAvatarY - 25, allyHealthWidth, 30, 0x38A169);
        const allyArmorBar = this.add.rectangle(allyBarX, allyAvatarY + 25, allyArmorWidth, 30, 0x3182CE);

        // 创建己方状态栏
        const allyStatusBarY = allyAvatarY - 80;
        const allyStatusBar = this.add.rectangle(
          allyBarX,
          allyStatusBarY,
          400,
          60,
          0x2D3436
        ).setOrigin(0.5, 0.5);

        // 添加己方状态栏边框
        this.add.rectangle(
          allyBarX,
          allyStatusBarY,
          400,
          60,
          0xC5A880
        ).setOrigin(0.5, 0.5)
          .setStrokeStyle(1, 0xC5A880);

        // 3. 创建敌方单位（右上角）
        const enemyAvatarY = 100;
        const enemyBarX = width - 250;

        // 创建敌方头像
        this.enemyAvatar = this.add.image(
          width - 100,
          enemyAvatarY,
          'aigirl'        // ← 纹理 key
        )
          .setOrigin(0.5, 0.5)
          .setDisplaySize(80, 80)   // ← 指定宽高
          .setAlpha(0.8);

        // 敌方血条和护甲条
        const enemyHealthWidth = (gameState_one.value.enemy.health / gameState_one.value.enemy.maxHealth) * 200;
        const enemyArmorWidth = (gameState_one.value.enemy.armor / gameState_one.value.enemy.maxArmor) * 200;
        const enemyHealthBar = this.add.rectangle(enemyBarX, enemyAvatarY - 25, enemyHealthWidth, 30, 0x38A169);
        const enemyArmorBar = this.add.rectangle(enemyBarX, enemyAvatarY + 25, enemyArmorWidth, 30, 0x3182CE);

        // 创建敌方状态栏
        const enemyStatusBarY = enemyAvatarY + 80;
        const enemyStatusBar = this.add.rectangle(
          enemyBarX,
          enemyStatusBarY,
          400,
          60,
          0x2D3436
        ).setOrigin(0.5, 0.5);

        // 添加敌方状态栏边框
        this.add.rectangle(
          enemyBarX,
          enemyStatusBarY,
          400,
          60,
          0xC5A880
        ).setOrigin(0.5, 0.5)
          .setStrokeStyle(1, 0xC5A880);

        // 己方文本显示
        this.add.text(allyBarX, allyAvatarY - 25, `HP: ${gameState_one.value.ally.health}`, {
          fontSize: '16px',
          color: '#ffffff',
          resolution: 2,
        }).setOrigin(0.5);

        this.add.text(allyBarX, allyAvatarY + 25, `Armor: ${gameState_one.value.ally.armor}`, {
          fontSize: '16px',
          color: '#ffffff',
          resolution: 2,
        }).setOrigin(0.5);

        this.add.text(allyBarX - 180, allyStatusBarY, '状态效果', {
          fontSize: '18px',
          padding: { x: 10, y: 5 },
          color: '#ffffff',
          resolution: 2,
        }).setOrigin(0, 0.5);

        // 敌方文本显示
        this.add.text(enemyBarX, enemyAvatarY - 25, `HP: ${gameState_one.value.enemy.health}`, {
          fontSize: '16px',
          color: '#ffffff',
          resolution: 2,
        }).setOrigin(0.5);

        this.add.text(enemyBarX, enemyAvatarY + 25, `Armor: ${gameState_one.value.enemy.armor}`, {
          fontSize: '16px',
          color: '#ffffff',
          resolution: 2,
        }).setOrigin(0.5);

        this.add.text(enemyBarX - 180, enemyStatusBarY, '状态效果', {
          fontSize: '18px',
          color: '#ffffff',
          padding: { x: 0, y: 5 },
          resolution: 2,
        }).setOrigin(0, 0.5);

        // 渲染状态效果的函数
        const renderEffects = (effects, x, y, isAlly = true) => {
          const spacing = 60; // 图标之间的间距
          const iconSize = 50; // 图标大小

          effects.forEach((effectKey, index) => {
            // 查找对应的 buff 图片
            const buff = buffs.find(b => b.key === effectKey);
            if (buff) {
              const iconX = isAlly ? x + (index * spacing) + 100 : x - (index * spacing) + 150;
              const icon = this.add.image(iconX, y, buff.key)
                .setDisplaySize(iconSize, iconSize)
                .setOrigin(0.5, 0.5)
                .setData('type', effectKey); // 存储buff类型，便于获取描述

              // 添加鼠标悬停效果
              icon.setInteractive()
                .on('pointerover', () => {
                  if (this.tooltipTimer) clearTimeout(this.tooltipTimer);

                  // 添加延迟显示
                  this.tooltipTimer = setTimeout(() => {
                    const description = buffDescriptions[effectKey] || `${effectKey} 效果`;
                    // 使用现有的提示显示函数
                    this.showCardTooltip(iconX, y - 40, description);
                  }, 400);
                })
                .on('pointerout', () => {
                  // 隐藏提示
                  if (this.tooltipTimer) clearTimeout(this.tooltipTimer);
                  if (this.cardTooltip) this.cardTooltip.setVisible(false);
                });
            }
          });
        };

        // 渲染己方状态效果
        renderEffects(
          gameState_one.value.ally.effects,
          allyBarX - 160, // 状态栏文字右侧
          allyStatusBarY,
          true
        );

        // 渲染敌方状态效果
        renderEffects(
          gameState_one.value.enemy.effects,
          enemyBarX - 160,
          enemyStatusBarY,
          false
        );

        //显示ui
        if (this.cardTooltip) {
          this.cardTooltip.destroy();
          this.cardTooltip = null;
        }

        // 创建新的提示UI工具函数
        this.showCardTooltip = (x, y, text) => {
          // 每次都创建新的提示组
          if (this.cardTooltip) {
            this.cardTooltip.destroy();
          }

          // 创建新提示容器
          this.cardTooltip = this.add.container(x, y).setDepth(2000);

          // 创建文本 - 确保启用自动换行
          const tooltipText = this.add.text(0, 0, text, {
            fontSize: '14px',
            color: '#ffffff',
            resolution: 2,
            align: 'left',         // 左对齐使多行文本更易读
            padding: { x: 10, y: 8 },
            wordWrap: {
              width: 250,          // 设置适当的宽度以允许文本换行
              useAdvancedWrap: true // 使用高级换行以处理中文等语言
            },
            lineSpacing: 3         // 行间距，使多行文本更清晰
          }).setOrigin(0.5);

          // 创建背景 - 尺寸会自动适应换行后的文本
          const textBounds = tooltipText.getBounds();
          const tooltipBg = this.add.rectangle(
            0,
            0,
            textBounds.width + 20,
            textBounds.height + 16,
            0x000000,
            0.85              // 增强对比度
          ).setOrigin(0.5).setStrokeStyle(1, 0xffffff, 0.7);

          // 先添加背景再添加文本
          this.cardTooltip.add(tooltipBg);
          this.cardTooltip.add(tooltipText);

          // 智能调整位置，避免提示框超出屏幕
          let finalX = x;
          let finalY = y;

          // 水平方向调整
          if (x + textBounds.width / 2 + 10 > this.scale.width) {
            finalX = this.scale.width - textBounds.width / 2 - 20;
          }
          if (x - textBounds.width / 2 - 10 < 0) {
            finalX = textBounds.width / 2 + 20;
          }

          // 垂直方向调整 - 确保长文本也不会超出屏幕底部
          if (y + textBounds.height / 2 + 10 > this.scale.height) {
            finalY = this.scale.height - textBounds.height / 2 - 20;
          }

          this.cardTooltip.setPosition(finalX, finalY);
        };


        // 修改鼠标悬停事件
        this.input.on('gameobjectover', (pointer, gameObject) => {
          if (gameObject.getData && gameObject.getData('type')) {
            const cardType = gameObject.getData('type');
            if (cardType === 'cardBack' || !cardDescriptions[cardType]) return;

            if (this.tooltipTimer) clearTimeout(this.tooltipTimer);

            this.tooltipTimer = setTimeout(() => {
              const tooltipX = gameObject.x > this.scale.width / 2 ?
                gameObject.x - 100 : gameObject.x + 100;
              this.showCardTooltip(tooltipX, gameObject.y, cardDescriptions[cardType]);
            }, 400);
          }
        });

        this.input.on('gameobjectout', () => {
          if (this.tooltipTimer) clearTimeout(this.tooltipTimer);
          if (this.cardTooltip) this.cardTooltip.setVisible(false);
        });

        // 拖动开始时隐藏提示
        this.input.on('dragstart', () => {
          if (this.tooltipTimer) clearTimeout(this.tooltipTimer);
          if (this.cardTooltip) this.cardTooltip.setVisible(false);
        });
      },
    },
  });

  // 第二个 Phaser 实例
  //------------------------------
  const container = screen1.value;
  const containerWidth = container.clientWidth;
  const containerHeight = container.clientHeight;
  //------------------------------
  game = new Phaser.Game({
    type: Phaser.AUTO,
    width: containerWidth,
    height: containerHeight,
    parent: screen1.value,  // 改为 screen1.value 而不是 gameCanvas.value
    backgroundColor: '#f5efe6',
    scale: {
      mode: Phaser.Scale.RESIZE,
      autoCenter: Phaser.Scale.CENTER_BOTH
    },
    physics: {
      default: 'arcade',
      arcade: {
        gravity: { y: 0 },
        debug: false
      }
    },
    scene: {
      // 预加载资源
      preload() {
        cardImages.forEach(card => {
          this.load.image(card.key, card.src)
        })
      },
      create() {
        this.cards = []
        let cardId = 1
        const topBarHeight = 180
        const padding = 20
        const STACK_OFFSET_Y = 20 // 堆叠时卡片垂直偏移量
        const STACK_DETECTION_DISTANCE = 80 // 增加堆叠检测距离
        const STACK_SNAP_DURATION = 150 // 吸附动画持续时间

        this.shiftKey = this.input.keyboard.addKey(Phaser.Input.Keyboard.KeyCodes.SHIFT)

        // 创建工具提示管理器
        this.tooltipManager = new TooltipManager(this);

        // 创建顶部边栏背景，并添加交互效果
        const topBar = this.add.rectangle(0, 0, this.scale.width, topBarHeight, 0xa3916a)
          .setOrigin(0, 0)
          .setDepth(100)
          .setStrokeStyle(2, 0x8c7853) // 使用渐变色的深色部分
          .setInteractive()

        // 创建出售槽
        const sellSlot = this.add.rectangle(padding, padding, 100, 140, 0x8c7853) // 使用主题色
          .setOrigin(0, 0)
          .setDepth(101)
          .setInteractive()
          .setStrokeStyle(2, 0x6e5773) // 使用渐变色的深色部分

        // 出售槽文本
        const sellIcon = this.add.text(sellSlot.x + 50, sellSlot.y + 40, '💰', {  // y位置上移
          fontSize: '28px',  // 稍微减小字体
          resolution: 2, // 提高分辨率
          padding: { x: 2, y: 2 } // 添加内边距
        }).setOrigin(0.5).setDepth(102)

        const sellText = this.add.text(sellSlot.x + 50, sellSlot.y + 90, '出售卡牌', {  // y位置上移
          fontSize: '14px',
          resolution: 2,
          color: '#ffffff',
          align: 'center',
          padding: { y: 5 }  // 添加垂直内边距
        }).setOrigin(0.5).setDepth(102)

        // 创建第一个购买槽 - 简化版本
        const buySlot = this.add.rectangle(padding * 2 + 100, padding, 100, 140, 0x6e5773)
          .setOrigin(0, 0)
          .setDepth(101)
          .setInteractive({ useHandCursor: true })
          .setStrokeStyle(3, 0x8c7853, 0.9)

        buySlot1OriginalX = buySlot.x
        // 购买槽文本 - 直接使用最终位置
        const buyIcon = this.add.text(padding * 2 + 100 + 50, padding + 40, '🎁', {
          fontSize: '32px',
          resolution: 2,
          padding: { x: 2, y: 2 }
        }).setOrigin(0.5).setDepth(102)

        const buyText = this.add.text(padding * 2 + 100 + 50, padding + 90 - 200, '诗意卡包\n5金币', {
          fontSize: '16px',
          resolution: 2,
          color: '#ffffff',
          align: 'center',
          fontWeight: 'bold',
          lineSpacing: 2,
          padding: { y: 5 }
        }).setOrigin(0.5).setDepth(102)

        // 直接设置最终位置，无动画
        buySlot.y = padding
        buyIcon.y = padding + 40
        buyText.y = padding + 90

        // 添加购买槽的悬浮效果
        buySlot.on('pointerover', () => {
          if (coins.value >= 5) {
            // 简单的悬浮效果 - 只改变边框颜色和透明度
            buySlot.setStrokeStyle(3, 0xffffff, 1) // 白色边框
            buySlot.setAlpha(0.9) // 轻微透明

            // 简单的文字轻微放大
            this.tweens.add({
              targets: [buyIcon, buyText],
              scale: 1.05,
              duration: 150,
              ease: 'Power2.easeOut'
            })
          } else {
            // 金币不足的简单提示
            buySlot.setStrokeStyle(3, 0xff5722, 1) // 警告色边框
            buySlot.setAlpha(0.8)
          }
        })

        buySlot.on('pointerout', () => {
          // 恢复原状
          buySlot.setStrokeStyle(3, 0x8c7853, 0.9)
          buySlot.setAlpha(1)

          this.tweens.add({
            targets: [buyIcon, buyText],
            scale: 1,
            duration: 150,
            ease: 'Power2.easeOut'
          })
        })



        // 修改Shift键监听为点击切换
        this.shiftKey = this.input.keyboard.addKey(Phaser.Input.Keyboard.KeyCodes.SHIFT)

        // 添加Shift键点击事件监听
        this.shiftKey.on('down', () => {
          // 切换模式状态
          isStackingMode.value = !isStackingMode.value

          const newColor = isStackingMode.value ? 0xffb74d : 0x4caf50
          const newStrokeColor = isStackingMode.value ? 0xff9800 : 0x388e3c
          const newText = isStackingMode.value ? '📚 堆叠模式' : '🔧 合成模式'

          // 颜色渐变动画 - 不改变位置和大小
          this.tweens.add({
            targets: modeHintBackground,
            duration: 300,
            ease: 'Power2.easeOut',
            onUpdate: function () {
              const progress = this.progress
              const currentColor = Phaser.Display.Color.Interpolate.ColorWithColor(
                Phaser.Display.Color.ValueToColor(modeHintBackground.fillColor),
                Phaser.Display.Color.ValueToColor(newColor),
                1,
                progress
              )
              modeHintBackground.setFillStyle(Phaser.Display.Color.GetColor(currentColor.r, currentColor.g, currentColor.b))
              modeHintBackground.setStrokeStyle(2, newStrokeColor, 0.5 + progress * 0.5)
            }
          })

          // 文字淡入淡出
          this.tweens.add({
            targets: modeHintText,
            alpha: 0,
            duration: 150,
            ease: 'Power2.easeOut',
            onComplete: () => {
              modeHintText.setText(newText)
              this.tweens.add({
                targets: modeHintText,
                alpha: 1,
                duration: 150,
                ease: 'Power2.easeOut'
              })
            }
          })
        })

        // 第一个购买槽处理函数
        const handleBuyClick = () => {
          if (coins.value >= 5) {
            this.tweens.killTweensOf([buySlot, buyIcon, buyText])
            buySlot.setScale(1)
            buyIcon.setScale(1)
            buyText.setScale(1)
            handleBuyPack()

            // 简单的按下反馈
            this.tweens.add({
              targets: buySlot,
              scaleX: 0.95,
              scaleY: 0.95,
              duration: 100,
              ease: 'Power2.easeOut',
              yoyo: true,
              onComplete: () => {
                buySlot.setScale(1) // 确保动画完成后重置
              }
            })

            // 简单的文字反馈
            this.tweens.add({
              targets: [buyIcon, buyText],
              scale: 0.9,
              duration: 100,
              ease: 'Power2.easeOut',
              yoyo: true,
              onComplete: () => {
                buyIcon.setScale(1) // 确保动画完成后重置
                buyText.setScale(1)
              }
            })

            // 简洁的边框闪烁
            buySlot.setStrokeStyle(3, 0xffffff)

            // 保留金币消费提示（这个比较实用）
            const costText = this.add.text(
              buySlot.x + 50,
              buySlot.y + 120,
              '-5',
              {
                fontSize: '18px',
                color: '#ff5722',
                fontWeight: 'bold',
                resolution: 2
              }
            ).setDepth(104).setOrigin(0.5)

            this.tweens.add({
              targets: costText,
              y: '-=30',
              alpha: 0,
              duration: 800,
              ease: 'Power2',
              onComplete: () => costText.destroy()
            })

          } else {
            if (buySlot1Animating) return
            buySlot1Animating = true

            buySlot.x = buySlot1OriginalX

            buySlot.setScale(1)
            buyIcon.setScale(1)
            buyText.setScale(1)
            // 简化的金币不足反馈
            this.tweens.add({
              targets: buySlot,
              x: buySlot1OriginalX + 3,
              scaleX: 0.95, // 添加缩放动画
              scaleY: 0.95,
              duration: 100,
              ease: 'Power2',
              yoyo: true,
              repeat: 2,
              onComplete: () => {
                buySlot.x = buySlot1OriginalX
                buySlot.setScale(1)
                buySlot1Animating = false
              }
            })

            this.tweens.add({
              targets: [buyIcon, buyText],
              x: '+=3',
              scale: 0.9,
              duration: 100,
              ease: 'Power2',
              yoyo: true,
              repeat: 2,
              onComplete: () => {
                buyIcon.setScale(1) // 确保回到原始缩放
                buyText.setScale(1)
              }
            })

            // 简单的警告提示
            const warningText = this.add.text(
              buySlot.x + 50,
              buySlot.y + 120,
              '金币不足',
              {
                fontSize: '14px',
                color: '#ff5722',
                fontWeight: 'bold',
                resolution: 2
              }
            ).setDepth(104).setOrigin(0.5)

            this.tweens.add({
              targets: warningText,
              y: '-=20',
              alpha: 0,
              duration: 800,
              ease: 'Power2',
              onComplete: () => warningText.destroy()
            })

            buySlot.setStrokeStyle(3, 0xff5722)
          }
        }

          // 为所有相关元素添加点击事件
          ;[buySlot, buyIcon, buyText].forEach(element => {
            element.on('pointerdown', handleBuyClick)
            element.on('pointerover', () => {
              buySlot.setStrokeStyle(2, 0xffffff)
            })
            element.on('pointerout', () => {
              buySlot.setStrokeStyle(2, 0x8c7853)
            })
          })

        // 创建攻击卡槽
        const attackSlot = this.add.rectangle(padding * 3 + 200, padding, 100, 140, 0x8b0000)
          .setOrigin(0, 0)
          .setDepth(101)
          .setInteractive({ dropZone: true })
          .setStrokeStyle(3, 0xff4444, 0.9)

        // 攻击槽文本和图标
        const attackIcon = this.add.text(padding * 3 + 200 + 50, padding + 40, '⚔️', {
          fontSize: '32px',
          resolution: 2,
          padding: { x: 2, y: 2 }
        }).setOrigin(0.5).setDepth(102)

        const attackText = this.add.text(padding * 3 + 200 + 50, padding + 90, '攻击卡槽', {
          fontSize: '16px',
          resolution: 2,
          color: '#ffffff',
          align: 'center',
          fontWeight: 'bold',
          lineSpacing: 2,
          padding: { y: 5 }
        }).setOrigin(0.5).setDepth(102)


        // 创建防守卡槽
        const defenseSlot = this.add.rectangle(padding * 4 + 300, padding, 100, 140, 0x0066cc)
          .setOrigin(0, 0)
          .setDepth(101)
          .setInteractive({ dropZone: true })
          .setStrokeStyle(3, 0x4488ff, 0.9)

        const defenseIcon = this.add.text(padding * 4 + 300 + 50, padding + 40, '🛡️', {
          fontSize: '32px',
          resolution: 2,
          padding: { x: 2, y: 2 }
        }).setOrigin(0.5).setDepth(102)

        const defenseText = this.add.text(padding * 4 + 300 + 50, padding + 90, '防守卡槽', {
          fontSize: '16px',
          resolution: 2,
          color: '#ffffff',
          align: 'center',
          fontWeight: 'bold',
          lineSpacing: 2,
          padding: { y: 5 }
        }).setOrigin(0.5).setDepth(102)

        // 创建BUFF卡槽
        const buffSlot = this.add.rectangle(padding * 5 + 400, padding, 100, 140, 0x228b22)
          .setOrigin(0, 0)
          .setDepth(101)
          .setInteractive({ dropZone: true })
          .setStrokeStyle(3, 0x44cc44, 0.9)

        const buffIcon = this.add.text(padding * 5 + 400 + 50, padding + 40, '✨', {
          fontSize: '32px',
          resolution: 2,
          padding: { x: 2, y: 2 }
        }).setOrigin(0.5).setDepth(102)

        const buffText = this.add.text(padding * 5 + 400 + 50, padding + 90, 'BUFF卡槽', {
          fontSize: '16px',
          color: '#ffffff',
          align: 'center',
          resolution: 1.5,
          fontWeight: 'bold',
          lineSpacing: 2,
          padding: { y: 5 }
        }).setOrigin(0.5).setDepth(102)

        // 攻击卡槽处理函数
        const handleAttackSlot = (card) => {
          // 检查攻击槽位[3][0]是否已被占用
          if (gameState_one.value.cardGrid[3][0] !== 'cardBack') {
            console.log('攻击槽位已被占用');
            return
          }

          const cardType = card.getData('type')
          console.log('卡片放入攻击槽:', cardType)

          // 更新游戏状态
          setTimeout(() => {
            updateCard(3, 0, cardType);
          }, 100);


          // 添加视觉效果
          const flash = this.add.circle(attackSlot.x + 50, attackSlot.y + 70, 40, 0xff4444, 0.8)
            .setDepth(150)
            .setBlendMode(Phaser.BlendModes.ADD)

          this.tweens.add({
            targets: flash,
            scale: { from: 0.1, to: 2 },
            alpha: { from: 0.8, to: 0 },
            duration: 500,
            onComplete: () => flash.destroy()
          })

          // 显示效果文本
          const effectText = this.add.text(attackSlot.x + 50, attackSlot.y + 30, '⚔️ 攻击!', {
            fontSize: '16px',
            color: '#ffffff',
            backgroundColor: '#ff4444',
            padding: { x: 8, y: 4 }
          }).setOrigin(0.5).setDepth(200)

          this.tweens.add({
            targets: effectText,
            y: '-=30',
            alpha: 0,
            duration: 1000,
            onComplete: () => effectText.destroy()
          })

          // 直接销毁卡片
          card.destroy()
          this.cards = this.cards.filter(c => c !== card)
        }

        // 防御卡槽处理函数
        const handleDefenseSlot = (card) => {
          // 检查防御槽位[3][1]是否已被占用
          if (gameState_one.value.cardGrid[3][1] !== 'cardBack') {
            console.log('防御槽位已被占用');
            return
          }

          const cardType = card.getData('type')
          console.log('卡片放入防御槽:', cardType)

          // 更新游戏状态
          setTimeout(() => {
            updateCard(3, 1, cardType);
          }, 100);

          // 添加视觉效果
          const flash = this.add.circle(defenseSlot.x + 50, defenseSlot.y + 70, 40, 0x4488ff, 0.8)
            .setDepth(150)
            .setBlendMode(Phaser.BlendModes.ADD)

          this.tweens.add({
            targets: flash,
            scale: { from: 0.1, to: 2 },
            alpha: { from: 0.8, to: 0 },
            duration: 500,
            onComplete: () => flash.destroy()
          })

          // 显示效果文本
          const effectText = this.add.text(defenseSlot.x + 50, defenseSlot.y + 30, '🛡️ 防御!', {
            fontSize: '16px',
            color: '#ffffff',
            backgroundColor: '#4488ff',
            padding: { x: 8, y: 4 }
          }).setOrigin(0.5).setDepth(200)

          this.tweens.add({
            targets: effectText,
            y: '-=30',
            alpha: 0,
            duration: 1000,
            onComplete: () => effectText.destroy()
          })

          // 直接销毁卡片
          card.destroy()
          this.cards = this.cards.filter(c => c !== card)
        }

        // BUFF卡槽处理函数
        const handleBuffSlot = (card) => {
          // 检查BUFF槽位[3][2]是否已被占用
          if (gameState_one.value.cardGrid[3][2] !== 'cardBack') {
            console.log('BUFF槽位已被占用');
            return
          }

          const cardType = card.getData('type')
          console.log('卡片放入BUFF槽:', cardType)

          // 更新游戏状态
          setTimeout(() => {
            updateCard(3, 2, cardType);
          }, 100);

          // 添加视觉效果
          const flash = this.add.circle(buffSlot.x + 50, buffSlot.y + 70, 40, 0x44cc44, 0.8)
            .setDepth(150)
            .setBlendMode(Phaser.BlendModes.ADD)

          this.tweens.add({
            targets: flash,
            scale: { from: 0.1, to: 2 },
            alpha: { from: 0.8, to: 0 },
            duration: 500,
            onComplete: () => flash.destroy()
          })

          // 显示效果文本
          const effectText = this.add.text(buffSlot.x + 50, buffSlot.y + 30, '✨ BUFF!', {
            fontSize: '16px',
            color: '#ffffff',
            backgroundColor: '#44cc44',
            padding: { x: 8, y: 4 }
          }).setOrigin(0.5).setDepth(200)

          this.tweens.add({
            targets: effectText,
            y: '-=30',
            alpha: 0,
            duration: 1000,
            onComplete: () => effectText.destroy()
          })

          // 直接销毁卡片
          card.destroy()
          this.cards = this.cards.filter(c => c !== card)
        }

        // 创建合成台背景 - 直接设置最终位置
        const finalCraftingX = padding * 6 + 500 + padding;
        const craftingStation = this.add.rectangle(
          finalCraftingX, // 直接设置最终位置，不需要动画
          padding,
          400,
          140,
          0xa3916a,
          1
        )
          .setOrigin(0, 0)
          .setDepth(100)
          .setStrokeStyle(2, 0xa3916a);

        // 创建四个合成槽
        const craftingSlots = []
        const slotWidth = 80
        const cardWidth = 100;
        const cardHeight = 140;
        const slotSpacing = 20
        const slotTypes = [null, null, null, null]

        for (let i = 0; i < 4; i++) {
          // 直接使用最终位置，删除动画
          const finalX = finalCraftingX + slotSpacing + i * (cardWidth + slotSpacing);
          const y = craftingStation.y + (craftingStation.height - cardHeight) / 2;

          const slot = this.add.rectangle(finalX, y, cardWidth, cardHeight, 0x8c7853)
            .setOrigin(0, 0)
            .setDepth(101)
            .setStrokeStyle(1, 0xffffff)
            .setData('type', slotTypes[i])
            .setData('occupied', false)
            .setData('card', null)
            .setInteractive({ dropZone: true });

          craftingSlots.push(slot);

          // 添加槽位标识 - 直接设置最终位置
          let operatorText = null;
          if (i < 3) {
            operatorText = this.add.text(
              finalX + cardWidth + 5,
              y + cardHeight / 2,
              i < 2 ? '+' : '=',
              {
                fontSize: '24px',
                resolution: 5,
                color: '#ffffff'
              }
            )
              .setOrigin(0, 0.5)
              .setDepth(101);
          }

          // 如果是第3个槽位（索引2），直接放置选择的诗人
          if (i === 2) {
            const poetCard = this.physics.add.image(
              finalX + cardWidth / 2,
              y + cardHeight / 2,
              selectedPoet.value
            )
              .setDisplaySize(100, 140)
              .setDepth(102)
              .setData('type', selectedPoet.value)
              .setData('id', 'selected_poet')
              .setData('isFixed', true);

            slot.setData('occupied', true);
            slot.setData('card', poetCard);
          }
        }

        // 添加拖放事件
        // 修改合成槽的拖放逻辑
        this.input.on('drop', (pointer, gameObject, dropZone) => {
          const cardType = gameObject.getData('type');
          const slotType = dropZone.getData('type');

          const canPlace = (slotType === null) ||
            (slotType === cardType) ||
            !dropZone.getData('occupied');

          if (canPlace && !dropZone.getData('occupied')) {
            // 放置卡牌到槽位
            dropZone.setData('occupied', true);
            dropZone.setData('card', gameObject);

            // 调整卡牌位置到槽位中心
            gameObject.x = dropZone.x + dropZone.width / 2;
            gameObject.y = dropZone.y + dropZone.height / 2;
            gameObject.setDepth(102); // 确保在槽位上方

            // 检查是否可以合成
            const materials = craftingSlots.slice(0, 3)
              .map(slot => slot.getData('card'))
              .filter(Boolean);

            if (materials.length === 3) {
              console.log('Materials ready:', materials.map(card => card.getData('type')));
              const resultType = checkCrafting(materials);

              if (resultType) {
                console.log('Creating result card:', resultType);

                // 创建结果卡牌
                const resultCard = this.physics.add.image(
                  craftingSlots[3].x + craftingSlots[3].width / 2,
                  craftingSlots[3].y + craftingSlots[3].height / 2,
                  resultType
                )
                  .setDisplaySize(100, 140)
                  .setInteractive({ cursor: 'pointer', useHandCursor: true })
                  .setCollideWorldBounds(true)
                  .setBounce(0.8)
                  .setData('type', resultType)
                  .setData('id', Date.now().toString())
                  .setDepth(102); // 确保可见

                this.input.setDraggable(resultCard);
                this.cards.push(resultCard);

                // 添加合成效果
                const flash = this.add.sprite(resultCard.x, resultCard.y, resultType)
                  .setScale(0.1)
                  .setAlpha(0.8)
                  .setTint(0xffd700)
                  .setBlendMode(Phaser.BlendModes.ADD)
                  .setDepth(103);

                this.tweens.add({
                  targets: flash,
                  alpha: 0,
                  scale: 1,
                  duration: 500,
                  onComplete: () => flash.destroy()
                });

                // 清空材料槽，但保留诗人槽（索引2）
                materials.forEach(card => {
                  // 检查是否是固定的诗人卡片
                  if (!card.getData('isFixed')) {
                    // 从cards数组中移除
                    this.cards = this.cards.filter(c => c !== card);
                    card.destroy();
                  }
                });

                // 只清空非诗人槽
                craftingSlots.forEach((slot, index) => {
                  if (index !== 2) { // 不清空诗人槽（索引2）
                    slot.setData('occupied', false);
                    slot.setData('card', null);
                  }
                });

                console.log('Crafting completed successfully!');
              } else {
                console.log('No matching recipe found for materials:', materials.map(card => card.getData('type')));
              }
            }
          } else {
            console.log('Cannot place card:', cardType, 'in slot:', slotType);
          }
        });


        // 对应地修改金币文本的深度值
        const coinDisplay = this.add.text(
          this.scale.width - padding - 10,
          padding + 20,
          `💰 ${coins.value}`,
          {
            fontSize: '24px',
            resolution: 2,
            color: '#ffffff'
          }
        )
          .setOrigin(1, 0.5)
          .setDepth(101);

        // 添加模式提示背景框
        const modeHintBackground = this.add.rectangle(
          this.scale.width - padding,
          padding + 55, // 在金币显示下方
          100, // 宽度
          40,  // 高度
          0x4caf50 // 默认绿色（合成模式）
        )
          .setOrigin(1, 0)
          .setDepth(100)
          .setAlpha(0.9)
          .setStrokeStyle(2, 0x388e3c);

        // 添加模式提示文本（优化版）
        const modeHintText = this.add.text(
          this.scale.width - padding - 10,
          padding + 75,
          '🔧 合成模式',
          {
            fontSize: '13px',
            color: '#ffffff',
            resolution: 2,
            fontWeight: 'bold'
          }
        )
          .setOrigin(1, 0.5)
          .setDepth(101);

        // 更新显示和背景（优化版）
        this.events.on('update', () => {
          const currentCoins = coins.value
          const displayText = `💰 ${currentCoins}`

          // 只更新右上角的金币显示
          if (coinDisplay.text !== displayText) {
            const oldValue = parseInt(coinDisplay.text.replace('💰 ', '')) || 0
            coinDisplay.setText(displayText)

            // 添加金币变化动画
            if (currentCoins !== oldValue && oldValue > 0) {
              const diff = currentCoins - oldValue

              // 创建变化提示文本
              if (diff !== 0) {
                const changeText = this.add.text(
                  coinDisplay.x - 80,
                  coinDisplay.y,
                  diff > 0 ? `+${diff}` : `${diff}`,
                  {
                    fontSize: '18px',
                    color: diff > 0 ? '#00ff00' : '#ff0000',
                    resolution: 2
                  }
                ).setDepth(1001)

                this.tweens.add({
                  targets: changeText,
                  y: changeText.y - 30,
                  alpha: 0,
                  duration: 1000,
                  ease: 'Power2',
                  onComplete: () => changeText.destroy()
                })
              }

              // 金币数字跳动效果
              this.tweens.add({
                targets: coinDisplay,
                scale: { from: 1, to: 1.1 },
                duration: 150,
                ease: 'Power2',
                yoyo: true
              })
            }
          }


          // 实时检查Shift键状态并更新模式显示
          const newText = isStackingMode.value ? '📚 堆叠模式' : '🔧 合成模式'
          const newColor = isStackingMode.value ? 0xffb74d : 0x4caf50
          const newStrokeColor = isStackingMode.value ? 0xff9800 : 0x388e3c

          // 只在模式真正改变时更新，避免每帧都执行
          if (modeHintText.text !== newText) {
            modeHintText.setText(newText)
            modeHintBackground.setFillStyle(newColor)
            modeHintBackground.setStrokeStyle(2, newStrokeColor)

            // 添加轻微的更新动画
            this.tweens.add({
              targets: [modeHintText, modeHintBackground],
              scale: { from: 1.1, to: 1 },
              duration: 200,
              ease: 'Back.easeOut'
            })
          }
        })

        // 添加窗口缩放事件处理
        this.scale.on('resize', (gameSize) => {
          // 更新顶部边栏
          topBar.width = gameSize.width;

          // 更新金币显示位置
          coinDisplay.x = gameSize.width - padding - 10;

          modeHintBackground.x = gameSize.width - padding;
          modeHintText.x = gameSize.width - padding - 10;
        });
        const initialCards = []
        for (let i = 0; i < initialCards.length; i++) {
          const cardKey = initialCards[i]
          const card = this.physics.add.image(180 + i * 120, 250 + topBarHeight, cardKey)
            .setDisplaySize(100, 140)
            .setInteractive({ cursor: 'pointer', useHandCursor: true })
            .setCollideWorldBounds(true)
            .setBounce(0.8)
            .setData('type', cardKey)
            .setData('id', cardId++)

          this.input.setDraggable(card)
          this.cards.push(card)
        }

        // 设置游戏区域边界
        this.physics.world.setBounds(0, 0, this.scale.width, this.scale.height)

        // 添加堆叠相关属性
        this.cardStacks = [] // 用于存储卡牌堆叠组


        // 拖拽结束事件
        this.input.on('dragend', (pointer, gameObject) => {

          attackSlot.setStrokeStyle(3, 0xff4444, 0.9)
          defenseSlot.setStrokeStyle(3, 0x4488ff, 0.9)
          buffSlot.setStrokeStyle(3, 0x44cc44, 0.9)

          this.tweens.add({
            targets: gameObject,
            alpha: 1,
            duration: 200,
            ease: 'Power2'
          })

          gameObject.setAlpha(1)
          gameObject.body.moves = true

          const cardType = gameObject.getData('type')
          let isStacked = false

          // 获取当前拖动的卡片所在堆叠组
          const currentStack = this.cardStacks.find(s => s.includes(gameObject))
          const currentStackIndex = this.cardStacks.indexOf(currentStack)

          // 只有在按住 Shift 键时才执行堆叠逻辑
          if (isStackingMode.value) {
            // 查找最近的同类型卡片或堆叠组
            let closestCard = null
            let minDistance = STACK_DETECTION_DISTANCE

            // 遍历所有卡片和堆叠组
            this.cards.forEach(otherCard => {
              if (otherCard !== gameObject &&
                otherCard.getData('type') === cardType &&
                otherCard.active) {

                // 获取目标卡片所在的堆叠组
                const targetStack = this.cardStacks.find(s => s.includes(otherCard))

                // 如果是不同的堆叠组或者未堆叠的卡片
                if (!targetStack || targetStack !== currentStack) {
                  const distance = Phaser.Math.Distance.Between(
                    gameObject.x, gameObject.y,
                    otherCard.x, otherCard.y
                  )
                  if (distance < minDistance) {
                    minDistance = distance
                    closestCard = otherCard
                  }
                }
              }
            })

            // 如果找到可堆叠的卡片
            if (closestCard) {
              let targetStack = this.cardStacks.find(s => s.includes(closestCard))
              let cardsToAdd = [gameObject]

              // 如果当前卡片在堆叠组中，获取它和它上面的所有卡片
              if (currentStack) {
                const cardIndex = currentStack.indexOf(gameObject)
                cardsToAdd = currentStack.splice(cardIndex)

                // 如果原堆叠组只剩一张卡，移除该堆叠组
                if (currentStack.length <= 1) {
                  this.cardStacks.splice(currentStackIndex, 1)
                }
              }

              // 如果目标卡片不在任何堆叠组中，创建新的堆叠组
              if (!targetStack) {
                targetStack = [closestCard]
                this.cardStacks.push(targetStack)
              }

              // 将所有需要添加的卡片加入目标堆叠组
              cardsToAdd.forEach(card => {
                if (!targetStack.includes(card)) {
                  targetStack.push(card)
                }
              })

              // 更新堆叠位置
              const baseY = Math.min(...targetStack.map(card => card.y))
              updateStackPosition.call(this, targetStack, closestCard.x, baseY, true)

              isStacked = true
            }

            if (!isStacked && currentStack) {
              // 如果没有找到新的堆叠目标，更新当前堆叠组的位置
              updateStackPosition.call(this, currentStack, gameObject.x, gameObject.y, true)
            }
          }
          //const cardType = gameObject.getData('type')

          // 检查是否在攻击槽区域
          if (pointer.y < topBarHeight &&
            pointer.x >= attackSlot.x &&
            pointer.x <= attackSlot.x + attackSlot.width) {

            if (canPlaceInSlot(cardType, 'attack')) {
              handleAttackSlot(gameObject)
            } else {
              showSlotError(gameObject, '此卡片不能放入攻击槽', attackSlot)
            }
            return
          }

          // 检查是否在防御槽区域
          if (pointer.y < topBarHeight &&
            pointer.x >= defenseSlot.x &&
            pointer.x <= defenseSlot.x + defenseSlot.width) {

            if (canPlaceInSlot(cardType, 'defense')) {
              handleDefenseSlot(gameObject)
            } else {
              showSlotError(gameObject, '此卡片不能放入防御槽', defenseSlot)
            }
            return
          }

          // 检查是否在BUFF槽区域
          if (pointer.y < topBarHeight &&
            pointer.x >= buffSlot.x &&
            pointer.x <= buffSlot.x + buffSlot.width) {

            if (canPlaceInSlot(cardType, 'buff')) {
              handleBuffSlot(gameObject)
            } else {
              showSlotError(gameObject, '此卡片不能放入BUFF槽', buffSlot)
            }
            return
          }
          // 检查是否在出售槽区域
          if (pointer.y < topBarHeight &&
            pointer.x >= sellSlot.x &&
            pointer.x <= sellSlot.x + sellSlot.width) {

            // 获取当前卡片所在的堆叠组
            const currentStack = this.cardStacks.find(s => s.includes(gameObject))
            let cardsToSell = currentStack ? [...currentStack] : [gameObject]

            // 计算总价格
            let totalPrice = 0
            cardsToSell.forEach(card => {
              const cardType = card.getData('type')
              const price = cardPrices[cardType] || 0
              totalPrice += price
            })

            if (totalPrice > 0) {
              coins.value += totalPrice
              updateGold(totalPrice)
              sellSlot.setStrokeStyle(2, 0x6e5773)

              // 添加金币动画
              const priceText = this.add.text(pointer.x, pointer.y, `+${totalPrice}`, {
                fontSize: '24px',
                resolution: 2,
                color: '#ffd700'
              }).setDepth(102)

              this.tweens.add({
                targets: priceText,
                y: '-=50',
                alpha: 0,
                duration: 1000,
                onComplete: () => priceText.destroy()
              })

              // 添加出售动画
              sellIcon.setScale(1.2)
              this.tweens.add({
                targets: sellIcon,
                scale: 1,
                duration: 200,
                ease: 'Back.easeOut'
              })

              // 发送出售消息并销毁卡牌
              cardsToSell.forEach(card => {
                const cardType = card.getData('type');
                const price = cardPrices[cardType] || 0;
                sendMessage({
                  type: "discardCard",
                  room: {
                    uid: getData('multiGame_userInfo')?.uid,
                    card: cardType,
                    money: price
                  }
                });
                card.destroy()
                this.cards = this.cards.filter(c => c !== card)
              })
              return
            }
            else {
              sellSlot.setStrokeStyle(2, 0x6e5773)
            }
          }

          // 检查合成 - 默认行为，不按 Shift 时执行
          if (!isStackingMode.value) {
            this.cards.forEach(otherCard => {
              if (otherCard !== gameObject &&
                Phaser.Geom.Intersects.RectangleToRectangle(gameObject.getBounds(), otherCard.getBounds())) {

                // 获取两张卡片所在的堆叠组
                const card1Stack = this.cardStacks.find(s => s.includes(gameObject))
                const card2Stack = this.cardStacks.find(s => s.includes(otherCard))

                const card1Type = gameObject.getData('type')
                const card2Type = otherCard.getData('type')

                const resultType = checkRecipe(card1Type, card2Type)

                //在这里完善卡牌合成的消息机制



                if (resultType) {

                  sendMessage({
                    type: "synthesize",
                    room: {
                      //uid: `getCurrentUid()` 
                      uid: getData('multiGame_userInfo')?.uid,
                      cardA: card1Type,
                      cardB: card2Type,
                      cardC: resultType
                    }
                  });
                  const x = (gameObject.x + otherCard.x) / 2
                  const y = (gameObject.y + otherCard.y) / 2

                  // 简单的震动效果
                  this.tweens.add({
                    targets: [gameObject, otherCard],
                    x: '+=5',
                    duration: 80,
                    ease: 'Power2.easeInOut',
                    yoyo: true,
                    repeat: 2
                  })

                  // 延迟执行合成
                  this.time.delayedCall(300, () => {
                    // 创建新卡片
                    const merged = this.physics.add.image(x, y, resultType)
                      .setDisplaySize(100, 140)
                      .setInteractive({ cursor: 'pointer', useHandCursor: true })
                      .setCollideWorldBounds(true)
                      .setBounce(0.8)
                      .setData('type', resultType)
                      .setData('id', Date.now().toString())

                    this.input.setDraggable(merged)

                    // 简单的闪光效果
                    const flash = this.add.circle(x, y, 60, 0xffd700, 0.8)
                      .setDepth(150)
                      .setBlendMode(Phaser.BlendModes.ADD)

                    this.tweens.add({
                      targets: flash,
                      scale: { from: 0.1, to: 1.5 },
                      alpha: { from: 0.8, to: 0 },
                      duration: 400,
                      onComplete: () => flash.destroy()
                    })

                    // 新卡片弹性出现
                    merged.setScale(0)
                    this.tweens.add({
                      targets: merged,
                      displayWidth: { from: 0, to: 100 },
                      displayHeight: { from: 0, to: 140 },
                      duration: 300,
                      ease: 'Back.easeOut'
                    })

                    // 清理原卡片
                    if (card1Stack) {
                      const index = card1Stack.indexOf(gameObject)
                      card1Stack.splice(index, 1)
                      if (card1Stack.length <= 1) {
                        const stackIndex = this.cardStacks.indexOf(card1Stack)
                        if (stackIndex !== -1) {
                          this.cardStacks.splice(stackIndex, 1)
                        }
                      }
                    }

                    if (card2Stack) {
                      const index = card2Stack.indexOf(otherCard)
                      card2Stack.splice(index, 1)
                      if (card2Stack.length <= 1) {
                        const stackIndex = this.cardStacks.indexOf(card2Stack)
                        if (stackIndex !== -1) {
                          this.cardStacks.splice(stackIndex, 1)
                        }
                      }
                    }

                    // 移除原卡片
                    gameObject.destroy()
                    otherCard.destroy()

                    // 更新数组
                    this.cards = this.cards.filter(card => card !== gameObject && card !== otherCard)
                    this.cards.push(merged)
                  })

                  return // 找到合成后立即返回
                }
              }
            })
          }

          // 如果没有找到合适的堆叠目标，保持原位置
          if (!isStacked && !currentStack) {
            gameObject.x = gameObject.x
            gameObject.y = gameObject.y
          }
        })

        // 修改拖拽开始事件
        this.input.on('dragstart', (pointer, gameObject) => {
          if (gameObject.getData('isFixed')) {
            return; // 不允许拖动固定的诗人卡片
          }
          gameObject.setDepth(150)
          gameObject.body.moves = false

          // 添加拖拽动画，但保持大小不变
          this.tweens.add({
            targets: gameObject,
            alpha: 0.9,
            duration: 200,
            ease: 'Power2'
          })

          craftingSlots.forEach(slot => {
            if (slot.getData('card') === gameObject) {
              slot.setData('occupied', false);
              slot.setData('card', null);
              console.log('Freed crafting slot for card:', gameObject.getData('type'));
            }
          });

          // 查找卡片所在的堆叠组
          const stackIndex = this.cardStacks.findIndex(s => s.includes(gameObject))
          if (stackIndex !== -1) {
            const stack = this.cardStacks[stackIndex]
            const cardIndex = stack.indexOf(gameObject)

            // 从原堆叠组中移除当前卡片及其上方的所有卡片
            const removedCards = stack.splice(cardIndex)

            // 如果原堆叠组只剩一张卡，移除该堆叠组
            if (stack.length === 1) {
              this.cardStacks.splice(stackIndex, 1)
            }

            // 为移除的卡片创建新的堆叠组
            if (removedCards.length > 1) {
              this.cardStacks.push(removedCards)
            }

            // 设置拖动卡片组的层级
            removedCards.forEach((card, index) => {
              card.setDepth(150 + index)
            })
          }

          // 查找卡片所在的堆叠组
          const stack = this.cardStacks.find(s => s.includes(gameObject))
          if (stack) {
            const cardIndex = stack.indexOf(gameObject)
            // 将当前卡片及其上方的卡片提升层级
            for (let i = cardIndex; i < stack.length; i++) {
              stack[i].setDepth(150 + i - cardIndex)
            }
          }
        })

        // 修改拖拽中事件
        this.input.on('drag', (pointer, gameObject, dragX, dragY) => {

          // 添加槽位高亮逻辑
          const cardType = gameObject.getData('type')
          const allowedSlotType = cardSlotMapping[cardType]

          // 重置所有槽位样式
          attackSlot.setStrokeStyle(3, 0xff4444, 0.9)
          defenseSlot.setStrokeStyle(3, 0x4488ff, 0.9)
          buffSlot.setStrokeStyle(3, 0x44cc44, 0.9)

          // 高亮可用槽位
          if (allowedSlotType === 'attack') {
            attackSlot.setStrokeStyle(3, 0xffffff, 1)
          } else if (allowedSlotType === 'defense') {
            defenseSlot.setStrokeStyle(3, 0xffffff, 1)
          } else if (allowedSlotType === 'buff') {
            buffSlot.setStrokeStyle(3, 0xffffff, 1)
          }
          // 添加出售槽状态检测
          const isInSellArea = dragY < topBarHeight &&
            dragX >= sellSlot.x &&
            dragX <= sellSlot.x + sellSlot.width

          //const cardType = gameObject.getData('type')
          const canSell = cardPrices[cardType] && cardPrices[cardType] > 0

          // 更新出售槽样式
          if (isInSellArea && canSell) {
            sellSlot.setStrokeStyle(2, 0xffffff)
          } else {
            sellSlot.setStrokeStyle(2, 0x6e5773)
          }
          gameObject.x = dragX
          gameObject.y = dragY

          // 更新堆叠组中跟随的卡片位置
          const stack = this.cardStacks.find(s => s.includes(gameObject))
          if (stack) {
            const cardIndex = stack.indexOf(gameObject)
            // 移动当前卡片上方的所有卡片
            for (let i = cardIndex + 1; i < stack.length; i++) {
              stack[i].x = dragX
              stack[i].y = dragY + (i - cardIndex) * STACK_OFFSET_Y
              stack[i].setDepth(150 + i - cardIndex)
            }
          }
        })

        // 更新堆叠位置的辅助函数
        function updateStackPosition(stack, baseX, baseY, animate = false) {
          stack.forEach((card, index) => {
            if (!card.active) return // 检查卡片是否还存在

            if (animate) {
              // 使用动画更新位置
              this.tweens.add({
                targets: card,
                x: baseX,
                y: baseY + (index * STACK_OFFSET_Y),
                duration: STACK_SNAP_DURATION,
                ease: 'Power2',
                onComplete: () => card.setDepth(150 + index)
              })
            } else {
              // 直接更新位置
              card.x = baseX
              card.y = baseY + (index * STACK_OFFSET_Y)
              card.setDepth(150 + index)
            }
          })
        }
        //显示ui
        if (this.cardTooltip) {
          this.cardTooltip.destroy();
          this.cardTooltip = null;
        }

        // 创建新的提示UI工具函数
        this.showCardTooltip = (x, y, text) => {
          // 每次都创建新的提示组
          if (this.cardTooltip) {
            this.cardTooltip.destroy();
          }

          // 创建新提示容器
          this.cardTooltip = this.add.container(x, y).setDepth(2000);

          // 创建文本 - 确保启用自动换行
          const tooltipText = this.add.text(0, 0, text, {
            fontSize: '14px',
            color: '#ffffff',
            resolution: 2,
            align: 'left',         // 左对齐使多行文本更易读
            padding: { x: 10, y: 8 },
            wordWrap: {
              width: 250,          // 设置适当的宽度以允许文本换行
              useAdvancedWrap: true // 使用高级换行以处理中文等语言
            },
            lineSpacing: 3         // 行间距，使多行文本更清晰
          }).setOrigin(0.5);

          // 创建背景 - 尺寸会自动适应换行后的文本
          const textBounds = tooltipText.getBounds();
          const tooltipBg = this.add.rectangle(
            0,
            0,
            textBounds.width + 20,
            textBounds.height + 16,
            0x000000,
            0.85              // 增强对比度
          ).setOrigin(0.5).setStrokeStyle(1, 0xffffff, 0.7);

          // 先添加背景再添加文本
          this.cardTooltip.add(tooltipBg);
          this.cardTooltip.add(tooltipText);

          // 智能调整位置，避免提示框超出屏幕
          let finalX = x;
          let finalY = y;

          // 水平方向调整
          if (x + textBounds.width / 2 + 10 > this.scale.width) {
            finalX = this.scale.width - textBounds.width / 2 - 20;
          }
          if (x - textBounds.width / 2 - 10 < 0) {
            finalX = textBounds.width / 2 + 20;
          }

          // 垂直方向调整 - 确保长文本也不会超出屏幕底部
          if (y + textBounds.height / 2 + 10 > this.scale.height) {
            finalY = this.scale.height - textBounds.height / 2 - 20;
          }

          this.cardTooltip.setPosition(finalX, finalY);
        };

        // 修改鼠标悬停事件
        this.input.on('gameobjectover', (pointer, gameObject) => {
          if (gameObject.getData && gameObject.getData('type')) {
            const cardType = gameObject.getData('type');
            if (cardType === 'cardBack' || !cardDescriptions[cardType]) return;

            if (this.tooltipTimer) clearTimeout(this.tooltipTimer);

            this.tooltipTimer = setTimeout(() => {
              const tooltipX = gameObject.x > this.scale.width / 2 ?
                gameObject.x - 100 : gameObject.x + 100;
              this.showCardTooltip(tooltipX, gameObject.y, cardDescriptions[cardType]);
            }, 400);
          }
        });

        this.input.on('gameobjectout', () => {
          if (this.tooltipTimer) clearTimeout(this.tooltipTimer);
          if (this.cardTooltip) this.cardTooltip.setVisible(false);
        });

        // 拖动开始时隐藏提示
        this.input.on('dragstart', () => {
          if (this.tooltipTimer) clearTimeout(this.tooltipTimer);
          if (this.cardTooltip) this.cardTooltip.setVisible(false);
        });
      },
    },
  });

  startTurn();
});

// 在组件卸载时销毁游戏实例
onBeforeUnmount(() => {
  if (game) {
    // 清除任何悬停计时器
    if (game.scene.scenes[0].tooltipTimer) {
      clearTimeout(game.scene.scenes[0].tooltipTimer);
    }
    if (battleScene && battleScene.scene.scenes[0].tooltipTimer) {
      clearTimeout(battleScene.scene.scenes[0].tooltipTimer);
    }

    game.destroy(true);
  }

  clearInterval(countdownInterval);
  clearTimeout(turnTimeout);
  if (reconnectTimer) clearTimeout(reconnectTimer);
  if (websocket.value && isConnected.value) {
    disconnectWebSocket();
  }
});
</script>

<style scoped>
.screen-wrapper {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  position: relative;
}

.screens {
  width: 100%;
  height: 200vh;
  /* 两个视口叠加 */
  transition: transform 0.8s ease;
}

.screen {
  width: 100vw;
  height: 100vh;
  position: relative;
}

.game-wrapper {
  position: relative;
}

/* 倒计时样式 */
.countdown {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 24px;
  padding: 6px 12px;
  border-radius: 4px;
  pointer-events: none;
  /* 不拦截点击 */
}

#countdown-timer.countdown {
  position: fixed;
  bottom: 56px;
  right: 56px;
  z-index: 10000;
  background: #c59d66;
  color: #fff;
  border-radius: 14px;
  box-shadow: 0 4px 18px 0 rgba(155, 204, 21, 0.15);
  padding: 16px 28px 12px 28px;
  min-width: 158px;
  font-family: "Segoe UI", Arial, sans-serif;
  user-select: none;
  pointer-events: none;
  text-align: center;
  transition: background 0.3s;
}

#countdown-timer .round {
  font-size: 18px;
  font-weight: 500;
  letter-spacing: 1px;
  margin-bottom: 6px;
  color: white;
  text-shadow: 0 2px 8px #222c;
}

#countdown-timer .timer {
  font-size: 17px;
  letter-spacing: 1px;
  color: #eee;
}

#countdown-timer .time-num {
  font-size: 2.3em;
  font-weight: bold;
  color: #fff238;
  margin: 0 8px;
  text-shadow: 0 2px 12px #443;
}

#countdown-timer .round-num {
  color: #fff238;
  font-weight: bold;
  font-size: 1.4em;
}

.game-result-indicator {
  position: fixed;
  left: 50%;
  top: 50%;
  z-index: 10001;
  min-width: 240px;
  background: #333b;
  color: #fff;
  border-radius: 18px;
  box-shadow: 0 4px 18px 0 rgba(21, 204, 155, 0.20);
  padding: 40px 60px 32px 60px;
  font-size: 2.6em;
  font-weight: bold;
  letter-spacing: 6px;
  text-align: center;
  user-select: none;
  transform: translate(-50%, -50%);
  transition: background-color 0.3s;
}

.game-result-indicator .result-text {
  font-size: 2.1em;
  color: #fff;
  text-shadow: 0 2px 12px #222c;
}

.game-result-indicator.result-win {
  background: #ffd700;
  color: #222;
}

.game-result-indicator.result-lose {
  background: #b0b0b0;
  color: #222;
}

.game-result-indicator.result-draw {
  background: #87ceeb;
  color: #222;
}

.fetchall-debug-btn {
  position: fixed;
  top: 18px;
  left: 18px;
  z-index: 10010;
  background: #ffd700;
  color: #333;
  border: none;
  border-radius: 7px;
  font-size: 18px;
  font-weight: bold;
  padding: 11px 26px;
  box-shadow: 0 2px 10px #0002;
  cursor: pointer;
  outline: none;
  transition: background-color 0.2s;
}

.fetchall-debug-btn:hover {
  background: #ffb800;
  color: #222;

  .return-btn {
    margin-top: 28px;
    padding: 12px 38px;
    font-size: 1.2em;
    border-radius: 10px;
    border: none;
    background: #ffd700;
    color: #222;
    font-weight: bold;
    cursor: pointer;
    transition: background-color 0.2s;
  }

  .return-btn:hover {
    background: #ffb800;
    color: #111;
  }
}
</style>
