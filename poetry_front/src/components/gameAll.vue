<template>
  <div class="layout">
    <div class="main-content">
      <!-- 左侧边栏 -->
      <div class="side-bar">
        <div class="tab-buttons">
          <button :class="['tab-button', { active: activeTab === 'achievements' }]" @click="activeTab = 'achievements'">
            成就
          </button>
          <button :class="['tab-button', { active: activeTab === 'recipes' }]" @click="activeTab = 'recipes'">
            合成表
          </button>
        </div>

        <div class="tab-content">
          <div v-if="activeTab === 'achievements'" class="achievements">
            <!-- 基础成就分类 -->
            <div class="achievement-category">
              <div class="category-header" @click="toggleBasicAchievements">
                <span class="toggle-icon">{{ basicAchievementsExpanded ? '▼' : '▶' }}</span>
                <h3>基础成就</h3>
              </div>
              <div v-show="basicAchievementsExpanded" class="category-content">
                <div v-for="achievement in basicAchievements" 
                  :key="achievement.id" 
                  :class="['achievement-item', { unlocked: achievement.unlocked }]">
                <span :class="['achievement-icon', { unlocked: achievement.unlocked }]">
                  {{ achievement.unlocked ? '🏆' : '🔒' }}
                </span>
                <div class="achievement-info">
                  <div class="achievement-name">{{ achievement.name }}</div>
                  <div class="achievement-desc">{{ achievement.description }}</div>
                </div>
              </div>
              </div>
            </div>

            <!-- 诗词收集成就分类 -->
            <div class="achievement-category">
              <div class="category-header" @click="togglePoemAchievements">
                <span class="toggle-icon">{{ poemAchievementsExpanded ? '▼' : '▶' }}</span>
                <h3>诗词收集</h3>
              </div>
              <div v-show="poemAchievementsExpanded" class="category-content">
                <div v-for="achievement in poemAchievements" 
                  :key="achievement.id" 
                  :class="['achievement-item', { unlocked: achievement.unlocked }]">
                <span :class="['achievement-icon', { unlocked: achievement.unlocked }]">
                  {{ achievement.unlocked ? '📜' : '🔒' }}
                </span>
                <div class="achievement-info">
                  <div class="achievement-name">{{ achievement.name }}</div>
                  <div class="achievement-desc">{{ achievement.description }}</div>
                </div>
              </div>
              </div>
            </div>
          </div>

          <div v-if="activeTab === 'recipes'" class="recipes">
            <h3>合成配方</h3>
            <div v-for="recipe in recipes" :key="recipe.id" class="recipe-item">
              <div class="recipe-cards">
                <img :src="recipe.card1.src" class="recipe-card" />
                <span class="plus">+</span>
                <img :src="recipe.card2.src" class="recipe-card" />
                <span class="equals">=</span>
                <img :src="recipe.result.src" class="recipe-card" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="right-section">
        <!-- 游戏容器 -->
        <div class="game-container">
          <div ref="gameCanvas"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import Phaser from 'phaser'

// 添加边栏相关的数据
const activeTab = ref('achievements')
const isStackingMode = ref(false)
const basicAchievementsExpanded = ref(true)
const poemAchievementsExpanded = ref(true)

// 折叠/展开控制函数
const toggleBasicAchievements = () => {
  basicAchievementsExpanded.value = !basicAchievementsExpanded.value
}
const togglePoemAchievements = () => {
  poemAchievementsExpanded.value = !poemAchievementsExpanded.value
}

const purchaseCount = ref(0)  // 购买卡包计数
const sellCount = ref(0)      // 出售卡片计数
const mergeCount = ref(0)     // 合成次数计数
const factoryCount = ref(0)   // 建造书斋计数
const workerCount = ref(0)    // 雇佣书生计数


// 成就列表数据
const basicAchievements = ref([
  {
    id: 1,
    name: '初次合成',
    description: '完成第一次卡片合成',
    unlocked: false
  },
  {
    id: 2,
    name: '合成大师',
    description: '完成10次合成',
    unlocked: false
  },
  {
    id: 3,
    name: '初次购买',
    description: '购买第一个卡包', 
    unlocked: false
  },
  {
    id: 4,
    name: '初建书斋',
    description: '建造第一个书斋',
    unlocked: false
  },
  {
    id: 5,
    name: '招募书生',
    description: '雇佣第一位书生',
    unlocked: false
  },
  {
    id: 6,
    name: '我是诗人',
    description: '创作出第一首诗词',
    unlocked: false
  },
  {
    id: 7,
    name: '卡包收藏家',
    description: '购买10个卡包',
    unlocked: false
  },
  {
    id: 8,
    name: '卡牌交易员',
    description: '出售10张卡牌',
    unlocked: false
  },
  {
    id: 9,
    name: '诗词收藏家',
    description: '收集10首诗词卡片',
    unlocked: false
  },
  {
    id: 10,
    name: '我爱你',
    description: '获得“爱情”',
    unlocked: false
  }
])

const poemAchievements = ref([
  { id: 11, name: '将进酒', description: '成功合成《将进酒》', unlocked: false },
  { id: 12, name: '蜀道难', description: '成功合成《蜀道难》', unlocked: false },
  { id: 13, name: '行路难', description: '成功合成《行路难》', unlocked: false },
  { id: 14, name: '黄鹤楼送孟浩然之广陵', description: '成功合成《黄鹤楼送孟浩然之广陵》', unlocked: false },
  { id: 15, name: '静夜思', description: '成功合成《静夜思》', unlocked: false },
  { id: 16, name: '望庐山瀑布', description: '成功合成《望庐山瀑布》', unlocked: false },
  { id: 17, name: '赠汪伦', description: '成功合成《赠汪伦》', unlocked: false },
  { id: 18, name: '闻王昌龄左迁龙标遥有此寄', description: '成功合成《闻王昌龄左迁龙标遥有此寄》', unlocked: false },
  { id: 19, name: '峨眉山月歌', description: '成功合成《峨眉山月歌》', unlocked: false },
  { id: 20, name: '使至塞上', description: '成功合成《使至塞上》', unlocked: false },
  { id: 21, name: '相思', description: '成功合成《相思》', unlocked: false },
  { id: 22, name: '送元二使安西', description: '成功合成《送元二使安西》', unlocked: false },
  { id: 23, name: '九月九日忆山东兄弟', description: '成功合成《九月九日忆山东兄弟》', unlocked: false },
  { id: 24, name: '渭城曲', description: '成功合成《渭城曲》', unlocked: false },
  { id: 25, name: '山居秋暝', description: '成功合成《山居秋暝》', unlocked: false },
  { id: 26, name: '鸟鸣涧', description: '成功合成《鸟鸣涧》', unlocked: false },
  { id: 27, name: '竹里馆', description: '成功合成《竹里馆》', unlocked: false },
  { id: 28, name: '水调歌头·明月几时有', description: '成功合成《水调歌头·明月几时有》', unlocked: false },
  { id: 29, name: '赤壁赋', description: '成功合成《赤壁赋》', unlocked: false },
  { id: 30, name: '记承天寺夜游', description: '成功合成《记承天寺夜游》', unlocked: false },
  { id: 31, name: '题西林壁', description: '成功合成《题西林壁》', unlocked: false },
  { id: 32, name: '饮湖上初晴后雨', description: '成功合成《饮湖上初晴后雨》', unlocked: false },
  { id: 33, name: '定风波·莫听穿林打叶声', description: '成功合成《定风波·莫听穿林打叶声》', unlocked: false },
  { id: 34, name: '卜算子·黄州定慧院寓居作', description: '成功合成《卜算子·黄州定慧院寓居作》', unlocked: false },
  { id: 35, name: '登高', description: '成功合成《登高》', unlocked: false },
  { id: 36, name: '茅屋为秋风所破歌', description: '成功合成《茅屋为秋风所破歌》', unlocked: false },
  { id: 37, name: '春夜喜雨', description: '成功合成《春夜喜雨》', unlocked: false },
  { id: 38, name: '望岳', description: '成功合成《望岳》', unlocked: false },
  { id: 39, name: '闻官军收河南河北', description: '成功合成《闻官军收河南河北》', unlocked: false },
  { id: 40, name: '春望', description: '成功合成《春望》', unlocked: false },
  { id: 41, name: '绝句·两个黄鹂鸣翠柳', description: '成功合成《绝句·两个黄鹂鸣翠柳》', unlocked: false },
  { id: 42, name: '泊船瓜洲', description: '成功合成《泊船瓜洲》', unlocked: false },
  { id: 43, name: '登飞来峰', description: '成功合成《登飞来峰》', unlocked: false },
  { id: 44, name: '元日', description: '成功合成《元日》', unlocked: false },
  { id: 45, name: '破阵子·为陈同甫赋壮词以寄之', description: '成功合成《破阵子·为陈同甫赋壮词以寄之》', unlocked: false },
  { id: 46, name: '西江月·夜行黄沙道中', description: '成功合成《西江月·夜行黄沙道中》', unlocked: false },
  { id: 47, name: '丑奴儿·书博山道中壁', description: '成功合成《丑奴儿·书博山道中壁》', unlocked: false },
  { id: 48, name: '归去来兮辞', description: '成功合成《归去来兮辞》', unlocked: false },
  { id: 49, name: '桃花源记', description: '成功合成《桃花源记》', unlocked: false },
  { id: 50, name: '饮酒·结庐在人境', description: '成功合成《饮酒·结庐在人境》', unlocked: false },
  { id: 51, name: '长恨歌', description: '成功合成《长恨歌》', unlocked: false },
  { id: 52, name: '钱塘湖春行', description: '成功合成《钱塘湖春行》', unlocked: false },
  { id: 53, name: '赋得古原草送别', description: '成功合成《赋得古原草送别》', unlocked: false },
  { id: 54, name: '忆江南', description: '成功合成《忆江南》', unlocked: false },
  { id: 55, name: '琵琶行', description: '成功合成《琵琶行》', unlocked: false },
  { id: 56, name: '大林寺桃花', description: '成功合成《大林寺桃花》', unlocked: false },
  { id: 57, name: '陋室铭', description: '成功合成《陋室铭》', unlocked: false },
  { id: 58, name: '酬乐天扬州初逢席上见赠', description: '成功合成《酬乐天扬州初逢席上见赠》', unlocked: false },
  { id: 59, name: '望洞庭', description: '成功合成《望洞庭》', unlocked: false },
  { id: 60, name: '游山西村', description: '成功合成《游山西村》', unlocked: false },
  { id: 61, name: '钗头凤·红酥手', description: '成功合成《钗头凤·红酥手》', unlocked: false },
  { id: 62, name: '锦瑟', description: '成功合成《锦瑟》', unlocked: false },
  { id: 63, name: '无题 相见时难别亦难', description: '成功合成《无题 相见时难别亦难》', unlocked: false },
  { id: 64, name: '夜雨寄北', description: '成功合成《夜雨寄北》', unlocked: false },
  { id: 65, name: '贾生', description: '成功合成《贾生》', unlocked: false },
  { id: 66, name: '浣溪沙·一曲新词酒一杯', description: '成功合成《浣溪沙·一曲新词酒一杯》', unlocked: false }
])


// 解锁成就的函数
const unlockAchievement = (achievementId) => {
  // 将 achievementId 转换为数字类型
  const idNum = parseInt(achievementId)
  
  if (idNum <= 10) {
    // 基础成就解锁逻辑
    const basicAchievement = basicAchievements.value.find(a => a.id === idNum)
    if (basicAchievement && !basicAchievement.unlocked) {
      basicAchievement.unlocked = true
      // 显示成就解锁提示
      showAchievementUnlocked(basicAchievement.name)
    }
  } else {
    // 诗词成就解锁逻辑
    const poemAchievement = poemAchievements.value.find(a => a.id === idNum)
    if (poemAchievement && !poemAchievement.unlocked) {
      poemAchievement.unlocked = true
      // 显示成就解锁提示
      showAchievementUnlocked(poemAchievement.name)
      
      // 检查是否需要解锁"诗词收藏家"成就
      checkPoemCollectorAchievement()
    }
  }
}

// 工厂相关成就的检查逻辑
const checkFactoryAchievement = (resultType) => {
  // 检查是否是专门书斋的合成结果（以factory_开头）
  if (resultType && resultType.startsWith('factory_')) {
    factoryCount.value++
    if (factoryCount.value === 1) {
      unlockAchievement(4) // 初建书斋成就
    }
  }
}

// 添加获取诗词名称的函数
const getPoemName = (resultType) => {
  // 从resultType(key)中提取诗词名称
  const poemMap = {
    'jiangjinjiu': '将进酒',
    'shudaonan': '蜀道难',
    'xinglunan': '行路难',
    'huanghelousongmenghaoranzhiguangling': '黄鹤楼送孟浩然之广陵',
    'jingyesi': '静夜思',
    'wanglushanpubu': '望庐山瀑布',
    'zengwanglun': '赠汪伦',
    'wenwangchanglingzuoqianlongbiaoyaoyouciji': '闻王昌龄左迁龙标遥有此寄',
    'emeishanyuege': '峨眉山月歌',
    'shizhisaishang': '使至塞上',
    'xiangsi': '相思',
    'songyuanershianxi': '送元二使安西',
    'jiuyuejiuriyishandongxiongdi': '九月九日忆山东兄弟',
    'weichengqu': '渭城曲',
    'shanjuqiuming': '山居秋暝',
    'niaomingjian': '鸟鸣涧',
    'zhuliguan': '竹里馆',
    'shuidiaogetou_mingyuejishiyou': '水调歌头·明月几时有',
    'chibifu': '赤壁赋',
    'jichengtansiyeyou': '记承天寺夜游',
    'tixilinbi': '题西林壁',
    'yinshangchuqinghouyu': '饮湖上初晴后雨',
    'dingfengbo_motingchuanlindayesheng': '定风波·莫听穿林打叶声',
    'busuanzi_huangzhoudinghuiyuanyujuzuo': '卜算子·黄州定慧院寓居作',
    'denggao': '登高',
    'maowuweiqiufengsuopoerge': '茅屋为秋风所破歌',
    'chunyexiyu': '春夜喜雨',
    'wangyue': '望岳',
    'wenguanjushouhenanhubei': '闻官军收河南河北',
    'chunwang': '春望',
    'jueju_lianggehuanglimingcuiliu': '绝句·两个黄鹂鸣翠柳',
    'bochuanguazhou': '泊船瓜洲',
    'dengfeilaifeng': '登飞来峰',
    'yuanri': '元日',
    'pozhenzi_weichentongfuzhuangciziyijizhi': '破阵子·为陈同甫赋壮词以寄之',
    'xijiangyue_yexinghuangshadaozhong': '西江月·夜行黄沙道中',
    'chounuer_shuboshandaozhongbi': '丑奴儿·书博山道中壁',
    'guiqulaixici': '归去来兮辞',
    'taohuayuanji': '桃花源记',
    'yinjian_jieluzairenjing': '饮酒·结庐在人境',
    'changhenge': '长恨歌',
    'qiantanghuchunxing': '钱塘湖春行',
    'fudeguyuancaosongbie': '赋得古原草送别',
    'yijiangnan': '忆江南',
    'pipaxing': '琵琶行',
    'dalinsitaohua': '大林寺桃花',
    'loushiming': '陋室铭',
    'chouletianyanzhouchufengxishangjianzheng': '酬乐天扬州初逢席上见赠',
    'wangdongting': '望洞庭',
    'youshanxicun': '游山西村',
    'chaitoufeng_hongsushou': '钗头凤·红酥手',
    'jinse': '锦瑟',
    'wuti_xiangjianshinnanbieyinan': '无题 相见时难别亦难',
    'yeyujibei': '夜雨寄北',
    'jiasheng': '贾生',
    'wanxisha_yiquxincijiuyibei': '浣溪沙·一曲新词酒一杯',
  }
  return poemMap[resultType] || ''
}

// 显示成就解锁提示的函数
const showAchievementUnlocked = (achievementName) => {
  const scene = game.scene.scenes[0]
  if (!scene) return

  // 创建成就解锁提示文本
  const notification = scene.add.text(
    scene.scale.width / 2,
    scene.scale.height - 100,
    `🏆 解锁成就: ${achievementName}`,
    {
      fontSize: '24px',
      backgroundColor: '#ffd700',
      color: '#000000',
      padding: { x: 20, y: 10 },
      resolution: 2
    }
  ).setOrigin(0.5, 0.5).setAlpha(0).setDepth(1000)

  // 添加动画效果
  scene.tweens.add({
    targets: notification,
    y: '-=50',
    alpha: 1,
    duration: 1000,
    ease: 'Power2',
    onComplete: () => {
      scene.time.delayedCall(2000, () => {
        scene.tweens.add({
          targets: notification,
          alpha: 0,
          duration: 1000,
          onComplete: () => notification.destroy()
        })
      })
    }
  })
}

// 检查诗词收藏家成就
const checkPoemCollectorAchievement = () => {
  const unlockedPoemCount = poemAchievements.value.filter(a => a.unlocked).length
  if (unlockedPoemCount >= 10 && !basicAchievements.value[8].unlocked) {
    unlockAchievement(9) // 解锁"诗词收藏家"成就
  }
}
// 添加检查love卡片的函数
const checkLoveAchievement = (cardType) => {
  if (cardType === 'love' && !basicAchievements.value[9].unlocked) {
    unlockAchievement(10) // 解锁"我爱你"成就
  }
}


// 在合成诗词成功时调用
const unlockPoemAchievement = (poemName) => {
  const achievement = poemAchievements.value.find(a => a.name === poemName)
  if (achievement) {
    unlockAchievement(achievement.id)
    
    // 同时解锁"我是诗人"成就(如果未解锁)
    if (!basicAchievements.value[5].unlocked) {
      unlockAchievement(6)
    }
  }
}

// 卡片素材列表
const cardImages = [
  { key: 'card_pack_poem', src: new URL('../assets/cards/诗意卡包(2).png', import.meta.url).href },
  { key: 'card_pack_poet', src: new URL('../assets/cards/诗人卡包(1).png', import.meta.url).href },
  { key: 'card_worker', src: new URL('../assets/cards/书生.png', import.meta.url).href },
  { key: 'factory', src: new URL('../assets/cards/工厂/书斋.png', import.meta.url).href },
  { key: 'unknown_card', src: new URL('../assets/cards/未知卡片.png', import.meta.url).href },

  { key: 'love', src: new URL('../assets/cards/诗意/爱情.png', import.meta.url).href },
  { key: 'sad', src: new URL('../assets/cards/诗意/悲.png', import.meta.url).href },
  { key: 'spring', src: new URL('../assets/cards/诗意/春.png', import.meta.url).href },
  { key: 'danbo', src: new URL('../assets/cards/诗意/淡泊.png', import.meta.url).href },
  { key: 'home', src: new URL('../assets/cards/诗意/故乡.png', import.meta.url).href },
  { key: 'yellowriver', src: new URL('../assets/cards/诗意/黄河.png', import.meta.url).href },
  { key: 'fire', src: new URL('../assets/cards/诗意/火.png', import.meta.url).href },
  { key: 'wine', src: new URL('../assets/cards/诗意/酒.png', import.meta.url).href },
  { key: 'byebye', src: new URL('../assets/cards/诗意/离别.png', import.meta.url).href },
  { key: 'liu', src: new URL('../assets/cards/诗意/柳.png', import.meta.url).href },
  { key: 'bird', src: new URL('../assets/cards/诗意/鸟.png', import.meta.url).href },
  { key: 'autumn', src: new URL('../assets/cards/诗意/秋.png', import.meta.url).href },
  { key: 'sun', src: new URL('../assets/cards/诗意/日.png', import.meta.url).href },
  { key: 'mountain', src: new URL('../assets/cards/诗意/山.png', import.meta.url).href },
  { key: 'water', src: new URL('../assets/cards/诗意/水.png', import.meta.url).href },
  { key: 'missing', src: new URL('../assets/cards/诗意/思念.png', import.meta.url).href },
  { key: 'flower', src: new URL('../assets/cards/诗意/桃花.png', import.meta.url).href },
  { key: 'goose', src: new URL('../assets/cards/诗意/雁.png', import.meta.url).href },
  { key: 'friend', src: new URL('../assets/cards/诗意/友情.png', import.meta.url).href },
  { key: 'rain', src: new URL('../assets/cards/诗意/雨.png', import.meta.url).href },
  { key: 'moon', src: new URL('../assets/cards/诗意/月.png', import.meta.url).href },
  { key: 'war', src: new URL('../assets/cards/诗意/战争.png', import.meta.url).href },
  { key: 'longriver', src: new URL('../assets/cards/诗意/长江.png', import.meta.url).href },
  { key: 'bamboo', src: new URL('../assets/cards/诗意/竹.png', import.meta.url).href },
  { key: 'zhuangzhinanchou', src: new URL('../assets/cards/诗意/壮志难酬.png', import.meta.url).href },
  { key: 'nature', src: new URL('../assets/cards/诗意/自然.png', import.meta.url).href },

  { key: 'baijuyi', src: new URL('../assets/cards/诗人/白居易.png', import.meta.url).href },
  { key: 'dufu', src: new URL('../assets/cards/诗人/杜甫.png', import.meta.url).href },
  { key: 'libai', src: new URL('../assets/cards/诗人/李白.png', import.meta.url).href },
  { key: 'lishangyin', src: new URL('../assets/cards/诗人/李商隐.png', import.meta.url).href },
  { key: 'liuyuxi', src: new URL('../assets/cards/诗人/刘禹锡.png', import.meta.url).href },
  { key: 'luyou', src: new URL('../assets/cards/诗人/陆游.png', import.meta.url).href },
  { key: 'sushi', src: new URL('../assets/cards/诗人/苏轼.png', import.meta.url).href },
  { key: 'taoyuanming', src: new URL('../assets/cards/诗人/陶渊明.png', import.meta.url).href },
  { key: 'wanganshi', src: new URL('../assets/cards/诗人/王安石.png', import.meta.url).href },
  { key: 'wangwei', src: new URL('../assets/cards/诗人/王维.png', import.meta.url).href },
  { key: 'xinqiji', src: new URL('../assets/cards/诗人/辛弃疾.png', import.meta.url).href },
  { key: 'yanshu', src: new URL('../assets/cards/诗人/晏殊.png', import.meta.url).href },

  { key: 'factory_love', src: new URL('../assets/cards/工厂/爱情.png', import.meta.url).href },
  { key: 'factory_sad', src: new URL('../assets/cards/工厂/悲.png', import.meta.url).href },
  { key: 'factory_spring', src: new URL('../assets/cards/工厂/春.png', import.meta.url).href },
  { key: 'factory_danbo', src: new URL('../assets/cards/工厂/淡泊.png', import.meta.url).href },
  { key: 'factory_home', src: new URL('../assets/cards/工厂/故乡.png', import.meta.url).href },
  { key: 'factory_yellowriver', src: new URL('../assets/cards/工厂/黄河.png', import.meta.url).href },
  { key: 'factory_fire', src: new URL('../assets/cards/工厂/火.png', import.meta.url).href },
  { key: 'factory_wine', src: new URL('../assets/cards/工厂/酒.png', import.meta.url).href },
  { key: 'factory_byebye', src: new URL('../assets/cards/工厂/离别.png', import.meta.url).href },
  { key: 'factory_liu', src: new URL('../assets/cards/工厂/柳.png', import.meta.url).href },
  { key: 'factory_bird', src: new URL('../assets/cards/工厂/鸟.png', import.meta.url).href },
  { key: 'factory_autumn', src: new URL('../assets/cards/工厂/秋.png', import.meta.url).href },
  { key: 'factory_sun', src: new URL('../assets/cards/工厂/日.png', import.meta.url).href },
  { key: 'factory_mountain', src: new URL('../assets/cards/工厂/山.png', import.meta.url).href },
  { key: 'factory_water', src: new URL('../assets/cards/工厂/水.png', import.meta.url).href },
  { key: 'factory_missing', src: new URL('../assets/cards/工厂/思念.png', import.meta.url).href },
  { key: 'factory_flower', src: new URL('../assets/cards/工厂/桃花.png', import.meta.url).href },
  { key: 'factory_goose', src: new URL('../assets/cards/工厂/雁.png', import.meta.url).href },
  { key: 'factory_friend', src: new URL('../assets/cards/工厂/友情.png', import.meta.url).href },
  { key: 'factory_rain', src: new URL('../assets/cards/工厂/雨.png', import.meta.url).href },
  { key: 'factory_moon', src: new URL('../assets/cards/工厂/月.png', import.meta.url).href },
  { key: 'factory_war', src: new URL('../assets/cards/工厂/战争.png', import.meta.url).href },
  { key: 'factory_longriver', src: new URL('../assets/cards/工厂/长江.png', import.meta.url).href },
  { key: 'factory_bamboo', src: new URL('../assets/cards/工厂/竹.png', import.meta.url).href },
  { key: 'factory_zhuangzhinanchou', src: new URL('../assets/cards/工厂/壮志难酬.png', import.meta.url).href },
  { key: 'factory_nature', src: new URL('../assets/cards/工厂/自然.png', import.meta.url).href },

  { key: 'jiangjinjiu', src: new URL('../assets/cards/诗词/将进酒.png', import.meta.url).href },
  { key: 'shudaonan', src: new URL('../assets/cards/诗词/蜀道难.png', import.meta.url).href },
  { key: 'xinglunan', src: new URL('../assets/cards/诗词/行路难.png', import.meta.url).href },
  { key: 'huanghelousongmenghaoranzhiguangling', src: new URL('../assets/cards/诗词/黄鹤楼送孟浩然之广陵.png', import.meta.url).href },
  { key: 'jingyesi', src: new URL('../assets/cards/诗词/静夜思.png', import.meta.url).href },
  { key: 'wanglushanpubu', src: new URL('../assets/cards/诗词/望庐山瀑布.png', import.meta.url).href },
  { key: 'zengwanglun', src: new URL('../assets/cards/诗词/赠汪伦.png', import.meta.url).href },
  { key: 'wenwangchanglingzuoqianlongbiaoyaoyouciji', src: new URL('../assets/cards/诗词/闻王昌龄左迁龙标遥有此寄.png', import.meta.url).href },
  { key: 'emeishanyuege', src: new URL('../assets/cards/诗词/峨眉山月歌.png', import.meta.url).href },
  { key: 'shizhisaishang', src: new URL('../assets/cards/诗词/使至塞上.png', import.meta.url).href },
  { key: 'xiangsi', src: new URL('../assets/cards/诗词/相思.png', import.meta.url).href },
  { key: 'songyuanershianxi', src: new URL('../assets/cards/诗词/送元二使安西.png', import.meta.url).href },
  { key: 'jiuyuejiuriyishandongxiongdi', src: new URL('../assets/cards/诗词/九月九日忆山东兄弟.png', import.meta.url).href },
  { key: 'weichengqu', src: new URL('../assets/cards/诗词/渭城曲.png', import.meta.url).href },
  { key: 'shanjuqiuming', src: new URL('../assets/cards/诗词/山居秋暝.png', import.meta.url).href },
  { key: 'niaomingjian', src: new URL('../assets/cards/诗词/鸟鸣涧.png', import.meta.url).href },
  { key: 'zhuliguan', src: new URL('../assets/cards/诗词/竹里馆.png', import.meta.url).href },
  { key: 'shuidiaogetou_mingyuejishiyou', src: new URL('../assets/cards/诗词/水调歌头·明月几时有.png', import.meta.url).href },
  { key: 'chibifu', src: new URL('../assets/cards/诗词/赤壁赋.png', import.meta.url).href },
  { key: 'jichengtansiyeyou', src: new URL('../assets/cards/诗词/记承天寺夜游.png', import.meta.url).href },
  { key: 'tixilinbi', src: new URL('../assets/cards/诗词/题西林壁.png', import.meta.url).href },
  { key: 'yinshangchuqinghouyu', src: new URL('../assets/cards/诗词/饮湖上初晴后雨.png', import.meta.url).href },
  { key: 'dingfengbo_motingchuanlindayesheng', src: new URL('../assets/cards/诗词/定风波·莫听穿林打叶声.png', import.meta.url).href },
  { key: 'busuanzi_huangzhoudinghuiyuanyujuzuo', src: new URL('../assets/cards/诗词/卜算子·黄州定慧院寓居作.png', import.meta.url).href },
  { key: 'denggao', src: new URL('../assets/cards/诗词/登高.png', import.meta.url).href },
  { key: 'maowuweiqiufengsuopoerge', src: new URL('../assets/cards/诗词/茅屋为秋风所破歌.png', import.meta.url).href },
  { key: 'chunyexiyu', src: new URL('../assets/cards/诗词/春夜喜雨.png', import.meta.url).href },
  { key: 'wangyue', src: new URL('../assets/cards/诗词/望岳.png', import.meta.url).href },
  { key: 'wenguanjushouhenanhubei', src: new URL('../assets/cards/诗词/闻官军收河南河北.png', import.meta.url).href },
  { key: 'chunwang', src: new URL('../assets/cards/诗词/春望.png', import.meta.url).href },
  { key: 'jueju_lianggehuanglimingcuiliu', src: new URL('../assets/cards/诗词/绝句·两个黄鹂鸣翠柳.png', import.meta.url).href },
  { key: 'bochuanguazhou', src: new URL('../assets/cards/诗词/泊船瓜洲.png', import.meta.url).href },
  { key: 'dengfeilaifeng', src: new URL('../assets/cards/诗词/登飞来峰.png', import.meta.url).href },
  { key: 'yuanri', src: new URL('../assets/cards/诗词/元日.png', import.meta.url).href },
  { key: 'pozhenzi_weichentongfuzhuangciziyijizhi', src: new URL('../assets/cards/诗词/破阵子·为陈同甫赋壮词以寄之.png', import.meta.url).href },
  { key: 'xijiangyue_yexinghuangshadaozhong', src: new URL('../assets/cards/诗词/西江月·夜行黄沙道中.png', import.meta.url).href },
  { key: 'chounuer_shuboshandaozhongbi', src: new URL('../assets/cards/诗词/丑奴儿·书博山道中壁.png', import.meta.url).href },
  { key: 'guiqulaixici', src: new URL('../assets/cards/诗词/归去来兮辞.png', import.meta.url).href },
  { key: 'taohuayuanji', src: new URL('../assets/cards/诗词/桃花源记.png', import.meta.url).href },
  { key: 'yinjian_jieluzairenjing', src: new URL('../assets/cards/诗词/饮酒·结庐在人境.png', import.meta.url).href },
  { key: 'changhenge', src: new URL('../assets/cards/诗词/长恨歌.png', import.meta.url).href },
  { key: 'qiantanghuchunxing', src: new URL('../assets/cards/诗词/钱塘湖春行.png', import.meta.url).href },
  { key: 'fudeguyuancaosongbie', src: new URL('../assets/cards/诗词/赋得古原草送别.png', import.meta.url).href },
  { key: 'yijiangnan', src: new URL('../assets/cards/诗词/忆江南.png', import.meta.url).href },
  { key: 'pipaxing', src: new URL('../assets/cards/诗词/琵琶行.png', import.meta.url).href },
  { key: 'dalinsitaohua', src: new URL('../assets/cards/诗词/大林寺桃花.png', import.meta.url).href },
  { key: 'loushiming', src: new URL('../assets/cards/诗词/陋室铭.png', import.meta.url).href },
  { key: 'chouletianyanzhouchufengxishangjianzheng', src: new URL('../assets/cards/诗词/酬乐天扬州初逢席上见赠.png', import.meta.url).href },
  { key: 'wangdongting', src: new URL('../assets/cards/诗词/望洞庭.png', import.meta.url).href },
  { key: 'youshanxicun', src: new URL('../assets/cards/诗词/游山西村.png', import.meta.url).href },
  { key: 'chaitoufeng_hongsushou', src: new URL('../assets/cards/诗词/钗头凤·红酥手.png', import.meta.url).href },
  { key: 'jinse', src: new URL('../assets/cards/诗词/锦瑟.png', import.meta.url).href },
  { key: 'wuti_xiangjianshinnanbieyinan', src: new URL('../assets/cards/诗词/无题 相见时难别亦难.png', import.meta.url).href },
  { key: 'yeyujibei', src: new URL('../assets/cards/诗词/夜雨寄北.png', import.meta.url).href },
  { key: 'jiasheng', src: new URL('../assets/cards/诗词/贾生.png', import.meta.url).href },
  { key: 'wanxisha_yiquxincijiuyibei', src: new URL('../assets/cards/诗词/浣溪沙·一曲新词酒一杯.png', import.meta.url).href }

]

// 合成配方映射
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
  'autumn_friend_libai': 'emeishanyuege',
  'byebye_friend_libai': 'zengwanglun',
  'friend_libai_moon': 'wenwangchanglingzuoqianlongbiaoyaoyouciji',
  'home_libai_moon': 'jingyesi',
  'byebye_libai_longriver': 'huanghelousongmenghaoranzhiguangling',
  'libai_mountain_water': 'wanglushanpubu',
  'autumn_libai_wine': 'xinglunan',
  'libai_wine_yellowriver': 'jiangjinjiu',

  'autumn_dufu_rain': 'maowuweiqiufengsuopoerge',
  'bird_dufu_liu': 'jueju_lianggehuanglimingcuiliu',
  'bird_dufu_mountain': 'wangyue',
  'dufu_home_war': 'wenguanjushouhenanhubei',
  'dufu_rain_spring': 'chunyexiyu',
  'dufu_spring_war': 'chunwang',
  'autumn_dufu_zhuangzhinanchou': 'denggao',

  'goose_wangwei_yellowriver': 'shizhisaishang',
  'friend_wangwei_wine': 'songyuanershianxi',
  'spring_missing_wangwei': 'xiangsi',
  'home_missing_wangwei': 'jiuyuejiuriyishandongxiongdi',
  'byebye_friend_wangwei': 'weichengqu',
  'autumn_rain_wangwei': 'shanjuqiuming',
  'bamboo_danbo_wangwei': 'zhuliguan',
  'bird_moon_wangwei': 'niaomingjian',

  'moon_sushi_water': 'shuidiaogetou_mingyuejishiyou',
  'fire_mountain_sushi': 'chibifu',
  'moon_sushi_zhuangzhinanchou': 'jichengtansiyeyou',
  'mountain_mountain_sushi': 'tixilinbi',
  'rain_sushi_water': 'yinshangchuqinghouyu',
  'bamboo_rain_sushi': 'dingfengbo_motingchuanlindayesheng',
  'goose_moon_sushi': 'busuanzi_huangzhoudinghuiyuanyujuzuo',

  'longriver_spring_wanganshi': 'bochuanguazhou',
  'mountain_sun_wanganshi': 'dengfeilaifeng',
  'spring_spring_wanganshi': 'yuanri',

  'autumn_sad_xinqiji': 'chounuer_shuboshandaozhongbi',
  'moon_nature_xinqiji': 'xijiangyue_yexinghuangshadaozhong',
  'war_xinqiji_zhuangzhinanchou': 'pozhenzi_weichentongfuzhuangciziyijizhi',

  'nature_nature_taoyuanming': 'guiqulaixici',
  'flower_nature_taoyuanming': 'taohuayuanji',
  'nature_taoyuanming_wine': 'yinjian_jieluzairenjing',

  'baijuyi_love_sad': 'changhenge',
  'baijuyi_spring_water': 'qiantanghuchunxing',
  'baijuyi_byebye_spring': 'fudeguyuancaosongbie',
  'baijuyi_longriver_spring': 'yijiangnan',
  'baijuyi_byebye_sad': 'pipaxing',
  'baijuyi_flower_spring': 'dalinsitaohua',

  'danbo_liuyuxi_nature': 'loushiming',
  'home_liuyuxi_sad': 'chouletianyanzhouchufengxishangjianzheng',
  'liuyuxi_mountain_water': 'wangdongting',

  'luyou_nature_wine': 'youshanxicun',
  'love_luyou_sad': 'chaitoufeng_hongsushou',

  'lishangyin_love_sad': 'jinse',
  'byebye_lishangyin_love': 'wuti_xiangjianshinnanbieyinan',
  'lishangyin_missing_rain': 'yeyujibei',
  'lishangyin_zhuangzhinanchou_zhuangzhinanchou': 'jiasheng',

  'spring_wine_yanshu': 'wanxisha_yiquxincijiuyibei',

  ////////////////////////////////////////////////////////////////////////////

  'autumn_autumn_factory': 'factory_autumn',
  'bamboo_bamboo_factory': 'factory_bamboo',
  'bird_bird_factory': 'factory_bird',
  'byebye_byebye_factory': 'factory_byebye',
  'danbo_danbo_factory': 'factory_danbo',
  'factory_fire_fire': 'factory_fire',
  'factory_flower_flower': 'factory_flower',
  'factory_friend_friend': 'factory_friend',
  'factory_goose_goose': 'factory_goose',
  'factory_home_home': 'factory_home',
  'factory_liu_liu': 'factory_liu',
  'factory_longriver_longriver': 'factory_longriver',
  'factory_love_love': 'factory_love',
  'factory_missing_missing': 'factory_missing',
  'factory_moon_moon': 'factory_moon',
  'factory_mountain_mountain': 'factory_mountain',
  'factory_nature_nature': 'factory_nature',
  'factory_rain_rain': 'factory_rain',
  'factory_sad_sad': 'factory_sad',
  'factory_spring_spring': 'factory_spring',
  'factory_sun_sun': 'factory_sun',
  'factory_war_war': 'factory_war',
  'factory_water_water': 'factory_water',
  'factory_wine_wine': 'factory_wine',
  'factory_yellowriver_yellowriver': 'factory_yellowriver',
  'factory_zhuangzhinanchou_zhuangzhinanchou': 'factory_zhuangzhinanchou',

};
const unlockedRecipes = ref(new Set()) // 存储已解锁的配方

// 修改合成表数据初始化
const recipes = ref([])

// 基于 recipeMapping 初始化所有配方为未知状态
const initializeRecipes = () => {
  const allRecipeEntries = Object.entries(recipeMapping)
  recipes.value = allRecipeEntries.map((entry, index) => {
    const [recipeKey, resultType] = entry
    return {
      id: index + 1,
      recipeKey: recipeKey, // 保存配方键用于解锁检查 (例如: "autumn_bird")
      resultType: resultType, // 保存结果类型 (例如: "goose")
      card1: { src: new URL('../assets/cards/未知卡片.png', import.meta.url).href },
      card2: { src: new URL('../assets/cards/未知卡片.png', import.meta.url).href },
      result: { src: new URL('../assets/cards/未知卡片.png', import.meta.url).href },
      unlocked: false // 添加解锁状态
    }
  })
}

// 解锁配方的函数

const unlockRecipe = (card1Type, card2Type, resultType) => {
  const types = [card1Type, card2Type].sort()
  const recipeKey = types.join('_')
  
  // 检查这个配方是否在我们的19个基础配方中
  if (recipeMapping[recipeKey] && !unlockedRecipes.value.has(recipeKey)) {
    unlockedRecipes.value.add(recipeKey)
    
    // 更新合成表显示
    const recipeIndex = recipes.value.findIndex(recipe => recipe.recipeKey === recipeKey)
    if (recipeIndex !== -1) {
      // 更新配方内容
      const updatedRecipe = {
        ...recipes.value[recipeIndex],
        card1: { src: getCardImageSrc(types[0]) },
        card2: { src: getCardImageSrc(types[1]) },
        result: { src: getCardImageSrc(resultType) },
        unlocked: true // 添加解锁标记
      }
      
      // 从原位置移除
      recipes.value.splice(recipeIndex, 1)
      
      // 添加到已解锁配方列表的末尾（但仍在未解锁配方之前）
      const unlockedCount = recipes.value.filter(r => r.unlocked).length
      recipes.value.splice(unlockedCount, 0, updatedRecipe)
      
      console.log(`解锁了新配方: ${types[0]} + ${types[1]} = ${resultType}`)
    }
  }
}

// 获取卡片图片路径的辅助函数
const getCardImageSrc = (cardType) => {
  const cardImage = cardImages.find(img => img.key === cardType)
  return cardImage ? cardImage.src : new URL('../assets/cards/未知卡片.png', import.meta.url).href
}

initializeRecipes()

// 检查两张卡是否可以合成
const checkRecipe = (card1Type, card2Type) => {
  // 确保类型按字母顺序排序以保持一致性
  const types = [card1Type, card2Type].sort()
  const recipeKey = types.join('_')
  return recipeMapping[recipeKey]
}

// 在 checkRecipe 后添加三卡合成检查函数
const checkCrafting = (cards) => {
  if (cards.length !== 3) return null;
  const types = cards.map(card => card.getData('type')).sort()
  const recipeKey = types.join('_')
  console.log('Crafting Recipe Key:', recipeKey); // 调试信息
  return craftingRecipes[recipeKey]
}

const cardPrices = {
  card_pack_poem: 10,
  card_pack_poet: 10,
  card_worker: 1,
  factory: 10,
                                       
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

  baijuyi: 2,
  dufu: 2,
  libai: 1,
  lishangyin: 2,
  liuyuxi: 3,
  luyou: 3,
  sushi: 2,
  taoyuanming: 3,
  wanganshi: 3,
  wangwei: 1,
  xinqiji: 4,
  yanshu: 4,

  factory_love: 10,
  factory_sad: 10,
  factory_spring: 10,
  factory_danbo: 10,
  factory_home: 10,
  factory_yellowriver: 10,
  factory_fire: 10,
  factory_wine: 10,
  factory_byebye: 10,
  factory_liu: 10,
  factory_bird: 10,
  factory_autumn: 10,
  factory_sun: 10,
  factory_mountain: 10,
  factory_water: 10,
  factory_missing: 10,
  factory_flower: 10,
  factory_goose: 10,
  factory_friend: 10,
  factory_rain: 10,
  factory_moon: 10,
  factory_war: 10,
  factory_longriver: 10,
  factory_bamboo: 10,
  factory_zhuangzhinanchou: 10,
  factory_nature: 10,

  jiangjinjiu: 10,
  shudaonan: 10,
  xinglunan: 10,
  huanghelousongmenghaoranzhiguangling: 10,
  jingyesi: 10,
  wanglushanpubu: 10,
  zengwanglun: 10,
  wenwangchanglingzuoqianlongbiaoyaoyouciji: 10,
  emeishanyuege: 10,
  shizhisaishang: 10,
  xiangsi: 10,
  songyuanershianxi: 10,
  jiuyuejiuriyishandongxiongdi: 10,
  weichengqu: 10,
  shanjuqiuming: 10,
  niaomingjian: 10,
  zhuliguan: 10,
  shuidiaogetou_mingyuejishiyou: 10,
  chibifu: 10,
  jichengtansiyeyou: 10,
  tixilinbi: 10,
  yinshangchuqinghouyu: 10,
  dingfengbo_motingchuanlindayesheng: 10,
  busuanzi_huangzhoudinghuiyuanyujuzuo: 10,
  denggao: 10,
  maowuweiqiufengsuopoerge: 10,
  chunyexiyu: 10,
  wangyue: 10,
  wenguanjushouhenanhubei: 10,
  chunwang: 10,
  jueju_lianggehuanglimingcuiliu: 10,
  bochuanguazhou: 10,
  dengfeilaifeng: 10,
  yuanri: 10,
  pozhenzi_weichentongfuzhuangciziyijizhi: 10,
  xijiangyue_yexinghuangshadaozhong: 10,
  chounuer_shuboshandaozhongbi: 10,
  guiqulaixici: 10,
  taohuayuanji: 10,
  yinjian_jieluzairenjing: 10,
  changhenge: 10,
  qiantanghuchunxing: 10,
  fudeguyuancaosongbie: 10,
  yijiangnan: 10,
  pipaxing: 10,
  dalinsitaohua: 10,
  loushiming: 10,
  chouletianyanzhouchufengxishangjianzheng: 10,
  wangdongting: 10,
  youshanxicun: 10,
  chaitoufeng_hongsushou: 10,
  jinse: 10,
  wuti_xiangjianshinnanbieyinan: 10,
  yeyujibei: 10,
  jiasheng: 10,
  wanxisha_yiquxincijiuyibei: 10,
};

//存档用
const rec={
  
}

const coins = ref(100) // 初始金币数量

// 购买诗意卡包
const handleBuyPack = () => {
  const packPrice = 10
  if (coins.value >= packPrice) {
    coins.value -= packPrice

    // 更新购买计数并检查成就
    purchaseCount.value++
    
    // 检查初次购买成就
    if (purchaseCount.value === 1) {
      unlockAchievement(3) // 初次购买
    }
    
    // 检查卡包收藏家成就
    if (purchaseCount.value >= 10) {
      unlockAchievement(7) // 卡包收藏家
    }

    const scene = game.scene.scenes[0]

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
          const allCards = ['love', 'sad', 'spring', 'danbo', 'home', 'yellowriver', 'fire', 'wine',
           'byebye', 'liu', 'bird', 'autumn', 'sun', 'mountain', 'water', 'missing', 'flower', 
           'goose', 'friend', 'rain', 'moon', 'war', 'longriver', 'bamboo', 'zhuangzhinanchou', 'nature']
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
            const randomCard = allCards[Math.floor(Math.random() * allCards.length)]

            checkLoveAchievement(randomCard) // 检查是否获得love卡片

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

//购买诗人卡包
const handleBuyAdvancedPack = () => {
  const packPrice = 15
  if (coins.value >= packPrice) {
    //coins.value -= packPrice

    // 更新购买计数并检查成就，与普通卡包共用计数
    purchaseCount.value++
    
    // 检查初次购买成就
    if (purchaseCount.value === 1) {
      unlockAchievement(3) // 初次购买
    }
    
    // 检查卡包收藏家成就
    if (purchaseCount.value >= 10) {
      unlockAchievement(7) // 卡包收藏家
    }

  const scene = game.scene.scenes[0]
  
  // 在随机位置创建卡包
  const x = Math.random() * (scene.scale.width - 100) + 50
  const y = Math.random() * (scene.scale.height - 140 - 180) + 250

  const advancedPack = scene.physics.add.image(x, y, 'card_pack_poet')
    .setDisplaySize(100, 140)
    .setInteractive({ cursor: 'pointer', useHandCursor: true, draggable: true })
    .setCollideWorldBounds(true)
    .setBounce(0.8)
    .setData('clickCount', 0)
    .setData('type', 'card_pack_poet')
    .setData('isDragging', false)
    .setData('pointerDown', false)
    .setData('dragStartX', 0)
    .setData('dragStartY', 0)


  scene.input.setDraggable(advancedPack)

  // 添加点击处理
  advancedPack.on('pointerup', (pointer) => {
    const isDragging = advancedPack.getData('isDragging')
    const startX = advancedPack.getData('dragStartX')
    const startY = advancedPack.getData('dragStartY')
    const distance = Phaser.Math.Distance.Between(startX, startY, pointer.x, pointer.y)

    if (distance < 5 && !isDragging) {
      const clickCount = advancedPack.getData('clickCount')

      if (clickCount === 0) {
        // 第一次点击：震动效果
        scene.tweens.add({
          targets: advancedPack,
          x: advancedPack.x + 5,
          duration: 50,
          yoyo: true,
          repeat: 3
        })
        advancedPack.setData('clickCount', 1)
      } else {
        // 第二次点击：生成随机卡片
        const advancedCards = ['baijuyi', 'dufu', 'libai', 'lishangyin', 'liuyuxi',
         'luyou', 'sushi', 'taoyuanming', 'wanganshi', 'wangwei', 'xinqiji', 'yanshu']
        const numCards = 3

        // 创建闪光效果
        const flash = scene.add.sprite(advancedPack.x, advancedPack.y, 'card_pack_poet')
          .setScale(0.1)
          .setAlpha(0.8)
          .setTint(0xffd700)
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
          const randomCard = advancedCards[Math.floor(Math.random() * advancedCards.length)]
          
          const newX = advancedPack.x + Math.cos(angle) * radius
          const newY = advancedPack.y + Math.sin(angle) * radius
          
          const card = scene.physics.add.image(advancedPack.x, advancedPack.y, randomCard)
            .setDisplaySize(100, 140)
            .setInteractive({ cursor: 'pointer', useHandCursor: true })
            .setCollideWorldBounds(true)
            .setBounce(0.8)
            .setData('type', randomCard)
            .setData('id', Date.now().toString() + i)

          scene.input.setDraggable(card)
          scene.cards.push(card)

          scene.tweens.add({
            targets: card,
            x: newX,
            y: newY,
            alpha: { from: 0.5, to: 1 },
            duration: 500,
            ease: 'Back.easeOut'
          })
        }

        // 销毁卡包
        scene.tweens.add({
          targets: advancedPack,
          alpha: 0,
          scale: 0.5,
          duration: 300,
          onComplete: () => advancedPack.destroy()
        })
      }
    }
    advancedPack.setData('pointerDown', false)
  })

  // 添加与普通卡包相同的拖动事件处理
  advancedPack.on('pointerdown', (pointer) => {
    advancedPack.setData('pointerDown', true)
    advancedPack.setData('dragStartX', pointer.x)
    advancedPack.setData('dragStartY', pointer.y)
  })

  advancedPack.on('dragstart', () => {
    advancedPack.setData('isDragging', true)
  })

  advancedPack.on('dragend', () => {
    if (advancedPack.getData('isDragging')) {
      setTimeout(() => {
        advancedPack.setData('isDragging', false)
        advancedPack.setData('pointerDown', false)
      }, 100)
    }
  })
}
}

const gameCanvas = ref(null)
let game = null

// 游戏主要逻辑
onMounted(() => {
  const container = gameCanvas.value
  const containerWidth = container.clientWidth
  const containerHeight = container.clientHeight

  const config = {
    type: Phaser.AUTO,
    width: containerWidth,
    height: containerHeight,
    parent: gameCanvas.value,
    backgroundColor: '#f5efe6', // 使用与 Forum 相同的背景色
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
      preload,
      create,
      update
    }
  }

  game = new Phaser.Game(config)

  // 游戏预加载
  function preload() {
    cardImages.forEach(card => {
      this.load.image(card.key, card.src)
    })
  }

  function create() {
    this.cards = []
    let cardId = 1
    const topBarHeight = 180
    const padding = 20
    const STACK_OFFSET_Y = 20 // 堆叠时卡片垂直偏移量
    const STACK_DETECTION_DISTANCE = 80 // 增加堆叠检测距离
    const STACK_SNAP_DURATION = 150 // 吸附动画持续时间
    this.factories = [];// 工厂生产管理

    this.shiftKey = this.input.keyboard.addKey(Phaser.Input.Keyboard.KeyCodes.SHIFT)

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

    // 创建第一个购买槽
    const buySlot = this.add.rectangle(padding * 2 + 100, padding, 100, 140, 0x6e5773) // 使用渐变色的深色部分
      .setOrigin(0, 0)
      .setDepth(101)
      .setInteractive({ useHandCursor: true }) // 添加手型光标
      .setStrokeStyle(2, 0x8c7853)


    // 购买槽文本
    const buyIcon = this.add.text(buySlot.x + 50, buySlot.y + 40, '🎁', {  // y位置上移
      fontSize: '28px',  // 稍微减小字体
      resolution: 2
    }).setOrigin(0.5).setDepth(102)

    const buyText = this.add.text(buySlot.x + 50, buySlot.y + 90, '诗意卡包\n10金币', {  // y位置上移
      fontSize: '14px',
      resolution: 2,
      color: '#ffffff',
      align: 'center',
      lineSpacing: 2,  // 减小行间距
      padding: { y: 5 }  // 添加垂直内边距
    }).setOrigin(0.5).setDepth(102)



    // 修改Shift键监听为点击切换
    this.shiftKey = this.input.keyboard.addKey(Phaser.Input.Keyboard.KeyCodes.SHIFT)

    // 添加Shift键点击事件监听
    this.shiftKey.on('down', () => {
      // 切换模式状态
      isStackingMode.value = !isStackingMode.value
      
      // 可选：添加切换反馈效果
      const flash = this.add.rectangle(
        this.scale.width - padding - 50,
        padding + 75,
        100,
        40,
        0xffffff,
        0.3
      ).setDepth(200)
      
      this.tweens.add({
        targets: flash,
        alpha: 0,
        duration: 200,
        onComplete: () => flash.destroy()
      })
    })

    // 第一个购买槽处理函数
    const handleBuyClick = () => {
      if (coins.value >= 10) {
        handleBuyPack()
        // 添加点击反馈动画
        this.tweens.add({
          targets: [buyIcon, buyText],
          scale: { from: 0.95, to: 1 },
          duration: 200,
          ease: 'Back.easeOut'
        })
        // 添加槽位反馈
        buySlot.setStrokeStyle(2, 0xffffff)
        this.time.delayedCall(100, () => {
          buySlot.setStrokeStyle(2, 0x8c7853)
        })
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

      // 创建第二个购买槽
      const buySlot2 = this.add.rectangle(padding * 3 + 200, padding, 100, 140, 0x6e5773)
        .setOrigin(0, 0)
        .setDepth(101)
        .setInteractive({ useHandCursor: true })
        .setStrokeStyle(2, 0x8c7853)

      // 第二个购买槽的文本和图标
      const buyIcon2 = this.add.text(buySlot2.x + 50, buySlot2.y + 40, '📦', {
        fontSize: '28px',
        resolution: 2
      }).setOrigin(0.5).setDepth(102)

      const buyText2 = this.add.text(buySlot2.x + 50, buySlot2.y + 90, '诗人卡包\n15金币', {
        fontSize: '14px',
        resolution: 2,
        color: '#ffffff',
        align: 'center',
        lineSpacing: 2,
        padding: { y: 5 }
      }).setOrigin(0.5).setDepth(102)

      // 第二个购买槽的处理函数
      const handleBuyClick2 = () => {
        if (coins.value >= 15) { // 设置更高的价格
          coins.value -= 15
          // 添加点击反馈动画
          this.tweens.add({
            targets: [buyIcon2, buyText2],
            scale: { from: 0.95, to: 1 },
            duration: 200,
            ease: 'Back.easeOut'
          })
          // 添加槽位反馈
          buySlot2.setStrokeStyle(2, 0xffffff)
          this.time.delayedCall(100, () => {
            buySlot2.setStrokeStyle(2, 0x8c7853)
          })
          // 创建高级卡包
          handleBuyAdvancedPack()
        }
      }

      // 为第二个购买槽添加交互效果
      ;[buySlot2, buyIcon2, buyText2].forEach(element => {
        element.on('pointerdown', handleBuyClick2)
        element.on('pointerover', () => {
          buySlot2.setStrokeStyle(2, 0xffffff)
        })
        element.on('pointerout', () => {
          buySlot2.setStrokeStyle(2, 0x8c7853)
        })
      })


      // 第三个购买槽
      const buySlot3 = this.add.rectangle(padding * 4 + 300, padding, 100, 140, 0x6e5773)
        .setOrigin(0, 0)
        .setDepth(101)
        .setInteractive({ useHandCursor: true })
        .setStrokeStyle(2, 0x8c7853)

      // 第三个购买槽的文本和图标
      const buyIcon3 = this.add.text(buySlot3.x + 50, buySlot3.y + 40, '🧙', {
        fontSize: '28px',
        resolution: 2
      }).setOrigin(0.5).setDepth(102)

      const buyText3 = this.add.text(buySlot3.x + 50, buySlot3.y + 90, '书生卡\n10金币', {
        fontSize: '14px',
        resolution: 2,
        color: '#ffffff',
        align: 'center',
        lineSpacing: 2,
        padding: { y: 5 }
      }).setOrigin(0.5).setDepth(102)

      // 第四个购买槽
      const buySlot4 = this.add.rectangle(padding * 5 + 400, padding, 100, 140, 0x6e5773)
        .setOrigin(0, 0)
        .setDepth(101)
        .setInteractive({ useHandCursor: true })
        .setStrokeStyle(2, 0x8c7853)

      // 第四个购买槽的文本和图标
      const buyIcon4 = this.add.text(buySlot4.x + 50, buySlot4.y + 40, '⛩️', {
        fontSize: '28px',
        resolution: 2
      }).setOrigin(0.5).setDepth(102)

      const buyText4 = this.add.text(buySlot4.x + 50, buySlot4.y + 90, '书斋卡\n10金币', {
        fontSize: '14px',
        color: '#ffffff',
        align: 'center',
        resolution: 2,
        lineSpacing: 2,
        padding: { y: 5 }
      }).setOrigin(0.5).setDepth(102)

      // 第三个购买槽的处理函数（购买工人卡）
      const handleBuyWorker = () => {
        if (coins.value >= 10) {
          coins.value -= 10
          // 更新书生计数并检查成就
          workerCount.value++
          if (workerCount.value === 1) {
            unlockAchievement(5) // 招募书生
          }

          // 添加点击反馈动画
          this.tweens.add({
            targets: [buyIcon3, buyText3],
            scale: { from: 0.95, to: 1 },
            duration: 200,
            ease: 'Back.easeOut'
          })
          // 添加槽位反馈
          buySlot3.setStrokeStyle(2, 0xffffff)
          this.time.delayedCall(100, () => {
            buySlot3.setStrokeStyle(2, 0x8c7853)
          })

          // 创建工人卡
          const x = Math.random() * (this.scale.width - 100) + 50
          const y = Math.random() * (this.scale.height - 140 - 180) + 250

          const workerCard = this.physics.add.image(x, y, 'card_worker')
            .setDisplaySize(100, 140)
            .setInteractive({ cursor: 'pointer', useHandCursor: true })
            .setCollideWorldBounds(true)
            .setBounce(0.8)
            .setData('type', 'card_worker')
            .setData('id', Date.now().toString())

          this.input.setDraggable(workerCard)
          this.cards.push(workerCard)
        }
      }

      // 第四个购买槽的处理函数（购买工厂卡）
      const handleBuyFactory = () => {
        if (coins.value >= 10) {
          coins.value -= 10

          // 添加点击反馈动画
          this.tweens.add({
            targets: [buyIcon4, buyText4],
            scale: { from: 0.95, to: 1 },
            duration: 200,
            ease: 'Back.easeOut'
          })
          // 添加槽位反馈
          buySlot4.setStrokeStyle(2, 0xffffff)
          this.time.delayedCall(100, () => {
            buySlot4.setStrokeStyle(2, 0x8c7853)
          })

          // 创建工厂卡
          const x = Math.random() * (this.scale.width - 100) + 50
          const y = Math.random() * (this.scale.height - 140 - 180) + 250

          const factoryCard = this.physics.add.image(x, y, 'factory')
            .setDisplaySize(100, 140)
            .setInteractive({ cursor: 'pointer', useHandCursor: true })
            .setCollideWorldBounds(true)
            .setBounce(0.8)
            .setData('type', 'factory')
            .setData('id', Date.now().toString())

          this.input.setDraggable(factoryCard)
          this.cards.push(factoryCard)
        }
      }

      // 为第三个购买槽添加交互效果
      ;[buySlot3, buyIcon3, buyText3].forEach(element => {
        element.on('pointerdown', handleBuyWorker)
        element.on('pointerover', () => {
          buySlot3.setStrokeStyle(2, 0xffffff)
        })
        element.on('pointerout', () => {
          buySlot3.setStrokeStyle(2, 0x8c7853)
        })
      })

      // 为第四个购买槽添加交互效果
      ;[buySlot4, buyIcon4, buyText4].forEach(element => {
        element.on('pointerdown', handleBuyFactory)
        element.on('pointerover', () => {
          buySlot4.setStrokeStyle(2, 0xffffff)
        })
        element.on('pointerout', () => {
          buySlot4.setStrokeStyle(2, 0x8c7853)
        })
      })


      // 创建合成台背景
      const craftingStation = this.add.rectangle(
        buySlot4.x + buySlot4.width + padding, // 书斋卡右边
        buySlot4.y, // 与书斋卡垂直对齐
        400, // 合成台宽度
        140, // 合成台高度
        0xa3916a
      )
        .setOrigin(0, 0)
        .setDepth(100)
        .setStrokeStyle(2, 0xa3916a);

      // 创建四个合成槽
      const craftingSlots = []
      const slotWidth = 80
      const cardWidth = 100; // 卡牌宽度
      const cardHeight = 140; // 卡牌高度
      const slotSpacing = 20
      const slotTypes = ['card2', 'card3', 'card_worker', null] // null 表示结果槽

      for (let i = 0; i < 4; i++) {
        const x = craftingStation.x + slotSpacing + i * (cardWidth + slotSpacing);
        const y = craftingStation.y + (craftingStation.height - cardHeight) / 2; // 垂直居中

        const slot = this.add.rectangle(x, y, cardWidth, cardHeight, 0x8c7853) // 使用卡牌大小
          .setOrigin(0, 0)
          .setDepth(101)
          .setStrokeStyle(1, 0xffffff)
          .setData('type', slotTypes[i])
          .setData('occupied', false)
          .setData('card', null)
          .setInteractive({ dropZone: true });

        craftingSlots.push(slot);

        // 添加槽位标识
        if (i < 3) {
          this.add.text(x + cardWidth + 5, y + cardHeight / 2, i < 2 ? '+' : '=', {
            fontSize: '24px',
            resolution: 5,
            color: '#ffffff'
          }).setOrigin(0, 0.5).setDepth(101);
        }

        // 添加槽位提示文本
        const slotText = i === 3 ? '诗词' : i === 2 ? '诗人' : `诗意${i + 1}`;
        this.add.text(x + cardWidth / 2, y - 5, slotText, {
          fontSize: '12px',
          resolution: 5,
          color: '#ffffff'
        }).setOrigin(0.5, 1).setDepth(101);
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

              // 检查是否解锁了书斋成就
              checkFactoryAchievement(resultType)

              const poemName = getPoemName(resultType)
              if (poemName) {
                unlockPoemAchievement(poemName)
              }
            }
            
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

              // 清空材料槽
              materials.forEach(card => {
                // 从cards数组中移除
                this.cards = this.cards.filter(c => c !== card);
                card.destroy();
              });
              
              craftingSlots.forEach(slot => {
                slot.setData('occupied', false);
                slot.setData('card', null);
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


    // 创建金币显示背景并添加交互效果
    const coinBackground = this.add.rectangle(
      this.scale.width - padding - 100,
      padding,
      100,
      40,
      0x8c7853
    )
      .setOrigin(1, 0)
      .setDepth(100)  // 改为100，与topBar相同
      .setAlpha(0.9)
      .setStrokeStyle(1, 0x6e5773)
      .setInteractive()
      .on('pointerover', () => {
        coinBackground.setAlpha(1)
      })
      .on('pointerout', () => {
        coinBackground.setAlpha(0.9)
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

    // 添加模式提示文本
    const modeHintText = this.add.text(
      this.scale.width - padding -10,
      padding + 75, // 背景框的中心位置
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
    // 更新显示和背景
    this.events.on('update', () => {
      coinDisplay.setText(`💰 ${coins.value}`)
        // 实时检查Shift键状态并更新模式显示
        if (isStackingMode.value) {
        // 堆叠模式
        modeHintText.setText('📚 堆叠模式')
        modeHintBackground.setFillStyle(0xffb74d) // 橙色
        modeHintBackground.setStrokeStyle(2, 0xff9800)
      } else {
        // 合成模式
        modeHintText.setText('🔧 合成模式')
        modeHintBackground.setFillStyle(0x4caf50) // 绿色
        modeHintBackground.setStrokeStyle(2, 0x388e3c)
      }
    })

    // 添加窗口缩放事件处理
    this.scale.on('resize', (gameSize) => {
      // 更新顶部边栏
      topBar.width = gameSize.width;

      // 更新金币显示位置
      coinDisplay.x = gameSize.width - padding - 10;
      coinBackground.x = gameSize.width - padding;

      modeHintBackground.x = gameSize.width - padding ;
      modeHintText.x = gameSize.width - padding -10;


      // 更新合成台位置
      craftingStation.x = buySlot4.x + buySlot4.width + padding; // 书斋卡右边

      // 更新槽位位置
      craftingSlots.forEach((slot, i) => {
        const x = craftingStation.x + slotSpacing + i * (cardWidth + slotSpacing);
        slot.x = x;

        // 更新槽位标识和提示文本的位置
        if (i < 3) {
          const operatorText = this.add.text(x + cardWidth + 5, slot.y + cardHeight / 2, i < 2 ? '+' : '=', {
            fontSize: '24px',
            resolution: 2,
            color: '#ffffff'
          }).setOrigin(0, 0.5).setDepth(101);
        }

        const slotText = i === 3 ? '诗词' : i === 2 ? '诗人' : `诗意${i + 1}`;
        this.add.text(x + cardWidth / 2, slot.y - 5, slotText, {
          fontSize: '12px',
          resolution: 2,
          color: '#ffffff'
        }).setOrigin(0.5, 1).setDepth(101);
      });
    });

    // 创建初始卡片
    const initialCards = ['spring', 'fire', 'bird', 'autumn', 'mountain','water','moon']; 
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

          // 移除堆叠组
          if (currentStack) {
            const stackIndex = this.cardStacks.indexOf(currentStack)
            if (stackIndex !== -1) {
              this.cardStacks.splice(stackIndex, 1)
            }
          }

          // 销毁所有要出售的卡片
          cardsToSell.forEach(card => {
            card.destroy()
            this.cards = this.cards.filter(c => c !== card)
          })

          // 更新出售计数并检查成就
          sellCount.value += cardsToSell.length
          if (sellCount.value >= 10) {
            unlockAchievement(8) // 卡牌交易员
          }

          return
        }
        else{
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

            if (resultType) {

              checkLoveAchievement(resultType)  // 检查是否合成出love卡片

              // 更新合成次数并检查成就
              mergeCount.value++
              
              if (mergeCount.value === 1) {
                unlockAchievement(1) // 初次合成
              }
              
              if (mergeCount.value >= 10) {
                unlockAchievement(2) // 合成大师
              }

              const x = (gameObject.x + otherCard.x) / 2
              const y = (gameObject.y + otherCard.y) / 2

              // 创建合成后的卡片
              const merged = this.physics.add.image(x, y, resultType)
                .setDisplaySize(100, 140)
                .setInteractive({ cursor: 'pointer', useHandCursor: true })
                .setCollideWorldBounds(true)
                .setBounce(0.8)
                .setData('type', resultType)
                .setData('id', Date.now().toString())

              this.input.setDraggable(merged)

              unlockRecipe(card1Type, card2Type, resultType)

              // 添加闪光效果
              const flash = this.add.sprite(x, y, 'spring')
                .setScale(0.1)
                .setAlpha(0.2)
                .setTint(0x8c7853)
                .setBlendMode(Phaser.BlendModes.ADD)

              this.tweens.add({
                targets: flash,
                alpha: 0,
                scale: 0.5,
                duration: 300,
                onComplete: () => flash.destroy()
              })

              // 如果卡片在堆叠组中，需要从堆叠组中移除
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

              // 解锁成就
              if (!achievements.value[0].unlocked) {
                achievements.value[0].unlocked = true
              }
            }
          }
        });
      }

      // 检查是否是工人卡和工厂卡的组合
      if (cardType === 'card_worker') {
        this.cards.forEach(otherCard => {
          const otherType = otherCard.getData('type');
          if (otherType.startsWith('factory_') && 
              Phaser.Math.Distance.Between(gameObject.x, gameObject.y, otherCard.x, otherCard.y) < STACK_DETECTION_DISTANCE) {
            
            // 检查该工厂是否已有工人
            const existingFactory = this.factories.find(f => f.base === otherCard);
            if (existingFactory) {
              return;
            }

            // 创建生产进度条背景
            const progressBarBg = this.add.rectangle(
              otherCard.x,
              otherCard.y - 80, // 显示在工厂卡上方
              80, // 进度条宽度
              8, // 进度条高度
              0x000000,
              0.3
            )
            .setDepth(160)
            .setStrokeStyle(1, 0x000000, 1); // 添加白色边框

            // 创建生产进度条
            const progressBar = this.add.rectangle(
              otherCard.x - 40, // 从左边开始
              otherCard.y - 80,
              0, // 初始宽度为0
              8,
              0xffd700
            )
            .setOrigin(0, 0.5)
            .setDepth(161)

            // 创建新的工厂生产对象
            const factory = {
              worker: gameObject,
              base: otherCard,
              productType: otherType.replace('factory_', ''),
              lastProduceTime: Date.now(),
              progressBar,
              progressBarBg,
              timer: this.time.addEvent({
                delay: 10000,
                callback: () => this.produceCard(factory),
                loop: true
              })
            };
            
            // 创建一个包含工人卡和工厂卡的堆叠组
            const factoryStack = [otherCard, gameObject];
            this.cardStacks.push(factoryStack);
            
            // 使用现有的堆叠位置更新函数
            updateStackPosition.call(this, factoryStack, otherCard.x, otherCard.y, true);
            
            this.factories.push(factory);
            isStacked = true;

            // 为工厂卡添加拖动事件监听
            otherCard.on('drag', (pointer, dragX, dragY) => {
              // 更新进度条位置
              progressBarBg.x = dragX;
              progressBarBg.y = dragY - 80;
              progressBar.x = dragX - 40;
              progressBar.y = dragY - 80;
              // 使用堆叠更新函数保持相对位置
              updateStackPosition.call(this, factoryStack, dragX, dragY, false);
            });
          }
        });
      }

      // 如果没有找到合适的堆叠目标，保持原位置
      if (!isStacked && !currentStack) {
        gameObject.x = gameObject.x
        gameObject.y = gameObject.y
      }
    })

    // 修改拖拽开始事件
    this.input.on('dragstart', (pointer, gameObject) => {
      gameObject.setDepth(150)
      gameObject.setAlpha(0.8)
      gameObject.body.moves = false

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

      // 检查是否是工人卡，并且是否在工厂中工作
      const cardType = gameObject.getData('type');
      if (cardType === 'card_worker') {
        const factory = this.factories.find(f => f.worker === gameObject);
        if (factory) {
          factory.timer.destroy();
          factory.progressBar.destroy();
          factory.progressBarBg.destroy();
          this.factories = this.factories.filter(f => f !== factory);
        }
      }
    })

    // 修改拖拽中事件
    this.input.on('drag', (pointer, gameObject, dragX, dragY) => {
    // 添加出售槽状态检测
    const isInSellArea = dragY < topBarHeight && 
                        dragX >= sellSlot.x && 
                        dragX <= sellSlot.x + sellSlot.width
    
    const cardType = gameObject.getData('type')
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
    // 添加生产卡片的方法
    this.produceCard = (factory) => {
      // 重置计时
      factory.lastProduceTime = Date.now();
      factory.progressBar.width = 0; // 重置进度条
      
      // 检查工厂和工人是否还存在
      if (!factory.worker.active || !factory.base.active) {
        factory.timer.destroy();
        this.factories = this.factories.filter(f => f !== factory);
        return;
      }
      
      // 生成新卡片的随机位置（在工厂周围）
      const radius = 100;
      const angle = Math.random() * Math.PI * 2;
      const x = factory.base.x + Math.cos(angle) * radius;
      const y = factory.base.y + Math.sin(angle) * radius;
      
      // 创建新卡片
      const newCard = this.physics.add.image(x, y, factory.productType)
        .setDisplaySize(100, 140)
        .setInteractive({ cursor: 'pointer', useHandCursor: true })
        .setCollideWorldBounds(true)
        .setBounce(0.8)
        .setData('type', factory.productType)
        .setData('id', Date.now().toString())
        .setAlpha(0);
      
      // 添加出现动画
      this.tweens.add({
        targets: newCard,
        alpha: 1,
        scale: { from: 0.2, to: 0.475 },
        duration: 500,
        ease: 'Back.easeOut'
      });
      
      // 添加闪光效果
      const flash = this.add.sprite(x, y, factory.productType)
        .setScale(0.1)
        .setAlpha(0.8)
        .setTint(0xffd700)
        .setBlendMode(Phaser.BlendModes.ADD);
      
      this.tweens.add({
        targets: flash,
        alpha: 0,
        scale: 1,
        duration: 500,
        onComplete: () => flash.destroy()
      });
      
      this.input.setDraggable(newCard);
      this.cards.push(newCard);
    };
  }

  // 需要持续更新的逻辑
  function update() {
    // 检查所有工厂的状态
    this.factories.forEach(factory => {
      // 如果工人或工厂卡被移除，清理相关资源
      if (!factory.worker.active || !factory.base.active) {
        factory.timer.destroy();
        factory.progressBar.destroy();
        factory.progressBarBg.destroy();
        this.factories = this.factories.filter(f => f !== factory);
      } else {
        // 更新进度条
        const elapsed = (Date.now() - factory.lastProduceTime) % 10000; // 10秒循环
        const progress = elapsed / 10000; // 计算进度(0-1)
        factory.progressBar.width = 80 * progress; // 更新进度条宽度
      }
    });
  }
})

// 在组件卸载时销毁游戏实例
onBeforeUnmount(() => {
  if (game) game.destroy(true)
})
</script>

<style scoped>
/* 添加全局重置样式 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.layout {
  width: 100%;
  height: 100vh;
  display: flex;
  /* 添加以下属性 */
  position: absolute;
  top: 0;
  left: 0;
  overflow: hidden;
}

.main-content {
  flex: 1;
  display: flex;
  /* 添加以下属性 */
  min-height: 100%;
}

.right-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #f5f5f5;
}

.top-bar {
  position: relative;
  z-index: 1000;
  height: 180px;
  /* 增加高度 */
  background-color: #a3916a;
  color: white;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 20px;
}

.slot {
  position: relative;
  z-index: 1001;
  width: 100px;
  /* 与卡片大小一致 */
  height: 140px;
  /* 与卡片大小一致 */
  border: 2px dashed #456789;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  /* 改为纵向排列 */
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
  padding: 10px;
}

.slot-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  text-align: center;
}

.slot-icon {
  font-size: 2em;
}

.slot-text {
  font-size: 0.9em;
  font-weight: bold;
  line-height: 1.2;
}

.sell-slot {
  background-color: #c0392b;
  transition: all 0.3s ease;
}

.sell-slot.drag-over {
  transform: scale(1.05);
  box-shadow: 0 0 15px rgba(255, 255, 255, 0.3);
  border: 2px solid #fff;
}

.buy-slot {
  background-color: #27ae60;
}

.slot:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.slot-content {
  display: flex;
  align-items: center;
  gap: 8px;
}

.slot-icon {
  font-size: 1.2em;
}

.slot-text {
  font-size: 0.9em;
  font-weight: bold;
}

.price {
  font-size: 0.8em;
  opacity: 0.8;
  margin-left: 5px;
}

.currency {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 5px;
  background-color: #b39c73;
  padding: 8px 12px;
  border-radius: 20px;
}

.coin-icon {
  font-size: 1.2em;
}

.coin-amount {
  font-weight: bold;
}

.content {
  flex: 1;
  display: flex;
}

.side-bar {
  width: 250px;
  background-color: #a3916a;
  color: white;
  display: flex;
  flex-direction: column;
}

.tab-buttons {
  display: flex;
  border-bottom: 1px solid #8c7853;
}

.tab-button {
  flex: 1;
  padding: 10px;
  background: none;
  border: none;
  color: white;
  cursor: pointer;
  transition: background-color 0.3s;
}

.tab-button.active {
  background-color: #b39c73;
}

.tab-content {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
}

.achievement-item {
  margin: 8px 0;
  padding: 10px;
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  position: relative;
  display: flex; /* 添加flex布局 */
  align-items: center; /* 垂直居中 */
}
/* 未解锁成就的样式 */
.achievement-item:not(.unlocked) {
  opacity: 1;
}
/* 已解锁成就的样式 */
.achievement-item.unlocked {
  background-color: rgba(255, 215, 0, 0.1); /* 金色背景 */
  border-color: white; /* 金色边框 */
  box-shadow: 0 0 10px rgb(255, 255, 255); /* 发光效果 */
}
/* 已解锁成就图标的动画效果 */
.achievement-icon.unlocked {
  animation: unlocked-pulse 1s ease-in-out;
  color: white; /* 金色图标 */
}
/* 已解锁成就的名称样式 */
.achievement-item.unlocked .achievement-name {
  color: white; /* 金色文字 */
  font-weight: bold;
}

@keyframes unlocked-pulse {
  0% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.2); opacity: 0.8; }
  100% { transform: scale(1); opacity: 1; }
}
.achievement-icon {
  opacity: 0.5;
  margin-right: 10px;
}

.achievement-icon.unlocked {
  opacity: 1;
}

.achievement-info {
  flex: 1;
}

.achievement-name {
  font-weight: bold;
  margin-bottom: 5px;
}

.achievement-desc {
  font-size: 0.9em;
  opacity: 0.8;
}

.recipe-item {
  padding: 10px;
  border-bottom: 1px solid #8c7853;
}

.recipe-cards {
  display: flex;
  align-items: center;
  justify-content: center;
}

.recipe-card {
  width: 50px;
  height: 70px;
  object-fit: contain;
}

.plus,
.equals {
  margin: 0 10px;
}

.game-container {
  flex: 1;
  display: flex;
  position: relative;
  /* 添加以下属性 */
  margin: 0;
  padding: 0;
  background-color: #f5efe6;
}

.game-container>div {
  flex: 1;
  min-height: 0;
  touch-action: none;
  /* 添加以下属性 */
  margin: 0;
  padding: 0;
}
.achievement-category {
  margin-bottom: 20px;
}

.category-header {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 10px;
  background-color: #8c7853;
  border-radius: 4px;
}

.category-header:hover {
  background-color: #9c8863;
}

.toggle-icon {
  margin-right: 10px;
  font-size: 12px;
}

.category-content {
  margin-top: 10px;
}

.achievement-item {
  margin: 8px 0;
  padding: 10px;
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  transition: background-color 0.3s;
}

.achievement-item:hover {
  background-color: rgba(255, 255, 255, 0.15);
}

.achievement-icon {
  font-size: 1.2em;
  margin-right: 15px;
}

.achievement-icon.unlocked {
  animation: unlocked-pulse 1s ease-in-out;
}


</style>