<<<<<<< HEAD
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
            <h3>成就列表</h3>
            <div v-for="achievement in achievements" :key="achievement.id" class="achievement-item">
              <span :class="['achievement-icon', { unlocked: achievement.unlocked }]">🏆</span>
              <div class="achievement-info">
                <div class="achievement-name">{{ achievement.name }}</div>
                <div class="achievement-desc">{{ achievement.description }}</div>
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

// 成就列表数据
const achievements = ref([
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
  }
])
// 1,2,15,
// 合成表数据
const recipes = ref([
  {
    id: 1,
    card1: { src: new URL('../assets/cards/card2.png', import.meta.url).href },
    card2: { src: new URL('../assets/cards/card4.png', import.meta.url).href },
    result: { src: new URL('../assets/cards/cardj.png', import.meta.url).href }
  },
  {
    id: 2,
    card1: { src: new URL('../assets/cards/card3.png', import.meta.url).href },
    card2: { src: new URL('../assets/cards/card5.png', import.meta.url).href },
    result: { src: new URL('../assets/cards/cardq.png', import.meta.url).href }
  },
  {
    id: 3,
    card1: { src: new URL('../assets/cards/cardj.png', import.meta.url).href },
    card2: { src: new URL('../assets/cards/cardq.png', import.meta.url).href },
    result: { src: new URL('../assets/cards/cardk.png', import.meta.url).href }
  },
])

// 卡片素材列表
const cardImages = [
  { key: 'card_pack_poem', src: new URL('../assets/cards/诗意卡包(2).png', import.meta.url).href },
  { key: 'card_pack_poet', src: new URL('../assets/cards/诗人卡包(1).png', import.meta.url).href },
  { key: 'card_worker', src: new URL('../assets/cards/书生.png', import.meta.url).href },
  { key: 'factory', src: new URL('../assets/cards/factory.png', import.meta.url).href },

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
  { key: 'songyuanershi\'anxi', src: new URL('../assets/cards/诗词/送元二使安西.png', import.meta.url).href },
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
  'bird_autumn': 'goose',
  'bird_bird': 'friend',
  'fire_fire': 'war',
  'fire_moon': 'sun',
  'friend_missing': 'love',
  'hometown_leaving': 'missing',
  'mountain_water': 'nature',
  'nature_spring': 'flower',
  'nature_water': 'bamboo',
  'rain_rain': 'yellowriver',
  'spring_water': 'liu',
  'water_bamboo': 'danbo',
  'water_fire': 'wine',
  'water_water': 'rain',
  'wine_autumn': 'zhuangzhinanchou',
  'yellowriver_water': 'longriver',
  'month_month': 'home',
  'home_month': 'byebye',
  'autumn_autumn': 'sad',
};

const craftingRecipes = {
  'bird_mountain_libai': 'shudaonan',
  'autumn_friend_libai': 'emeishanyuege',
  'friend_byebye_libai': 'zengwanglun',
  'friend_moon_libai': 'wenwangchanglingzuoqianlongbiaoyaoyouciji',
  'home_moon_libai': 'jingyesi',
  'byebye_longriver_libai': 'huanghelousongmenghaoranzhiguangling',
  'mountain_water_libai': 'wanglushanpubu',
  'autumn_wine_libai': 'xinglunan',
  'wine_yellowriver_libai': 'jiangjinjiu',

  'autumn_rain_dufu': 'maowuweiqiufengsuopoerge',
  'bird_liu_dufu': 'jueju_lianggehuanglimingcuiliu',
  'bird_mountain_dufu': 'wangyue',
  'home_war_dufu': 'wenguanjushouhenanhubei',
  'rain_spring_dufu': 'chunyexiyu',
  'spring_war_dufu': 'chunwang',
  'autumn_zhuangzhinanchou_dufu': 'denggao',

  'goose_yellowriver_wangwei': 'shizhisaishang',
  'friend_wine_wangwei': 'songyuanershianxi',
  'spring_missing_wangwei': 'xiangsi',
  'home_missing_wangwei': 'jiuyuejiuriyishandongxiongdi',
  'friend_byebye_wangwei': 'weichengqu',
  'autumn_rain_wangwei': 'shanjuqiuming',
  'bamboo_danbo_wangwei': 'zhuliguan',
  'bird_moon_wangwei': 'niaomingjian',

  'moon_water_sushi': 'shuidiaogetou_mingyuejishiyou',
  'fire_mountain_sushi': 'chibifu',
  'moon_zhuangzhinanchou_sushi': 'jichengtansiyeyou',
  'mountain_mountain_sushi': 'tixilinbi',
  'rain_water_sushi': 'yinshangchuqinghouyu',
  'bamboo_rain_sushi': 'dingfengbo_motingchuanlindayesheng',
  'goose_moon_sushi': 'busuanzi_huangzhoudinghuiyuanyujuzuo',

  'spring_longriver_wanganshi': 'bochuanguazhou',
  'sun_mountain_wanganshi': 'dengfeilaifeng',
  'spring_spring_wanganshi': 'yuanri',

  'autumn_sad_xinqiji': 'chounuer_shuboshandaozhongbi',
  'moon_nature_xinqiji': 'xijiangyue_yexinghuangshadaozhong',
  'war_zhuangzhinanchou_xinqiji': 'pozhenzi_weichentongfuzhuangciziyijizhi',

  'nature_nature_taoyuanming': 'guiqulaixici',
  'flower_nature_taoyuanming': 'taohuayuanji',
  'nature_wine_taoyuanming': 'yinjian_jieluzairenjing',

  'love_sad_baijuyi': 'changhenge',
  'spring_water_baijuyi': 'qiantanghuchunxing',
  'byebye_spring_baijuyi': 'fudeguyuancaosongbie',
  'longriver_spring_baijuyi': 'yijiangnan',
  'byebye_sad_baijuyi': 'pipaxing',
  'flower_spring_baijuyi': 'dalinsitaohua',

  'danbo_nature_liuyuxi': 'loushiming',
  'home_sad_liuyuxi': 'chouletianyanzhouchufengxishangjianzheng',
  'mountain_water_liuyuxi': 'wangdongting',

  'nature_wine_luyou': 'youshanxicun',
  'love_sad_luyou': 'chaitoufeng_hongsushou',

  'love_sad_lishangyin': 'jinse',
  'love_byebye_lishangyin': 'wuti_xiangjianshinnanbieyinan',
  'missing_rain_lishangyin': 'yeyujibei',
  'zhuangzhinanchou_zhuangzhinanchou_lishangyin': 'jiasheng',

  'spring_wine_yanshu': 'wanxisha_yiquxincijiuyibei',

  ////////////////////////////////////////////////////////////////////////////

  'love_love_factory': 'factory_love',
  'sad_sad_factory': 'factory_sad',
  'spring_spring_factory': 'factory_spring',
  'danbo_danbo_factory': 'factory_danbo',
  'home_home_factory': 'factory_home',
  'yellowriver_yellowriver_factory': 'factory_yellowriver',
  'fire_fire_factory': 'factory_fire',
  'wine_wine_factory': 'factory_wine',
  'byebye_byebye_factory': 'factory_byebye',
  'liu_liu_factory': 'factory_liu',
  'bird_bird_factory': 'factory_bird',
  'autumn_autumn_factory': 'factory_autumn',
  'sun_sun_factory': 'factory_sun',
  'mountain_mountain_factory': 'factory_mountain',
  'water_water_factory': 'factory_water',
  'missing_missing_factory': 'factory_missing',
  'flower_flower_factory': 'factory_flower',
  'goose_goose_factory': 'factory_goose',
  'friend_friend_factory': 'factory_friend',
  'rain_rain_factory': 'factory_rain',
  'moon_moon_factory': 'factory_moon',
  'war_war_factory': 'factory_war',
  'longriver_longriver_factory': 'factory_longriver',
  'bamboo_bamboo_factory': 'factory_bamboo',
  'zhuangzhinanchou_zhuangzhinanchou_factory': 'factory_zhuangzhinanchou',
  'nature_nature_factory': 'factory_nature',
}

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
  card2: 3,
  card3: 3,
  card4: 5,
  card5: 5,
  cardj: 10,
  cardq: 10,
  cardk: 10,
  card8: 10,
  card9: 10,
  card10: 10,
  card_worker: 10,
  card_factory: 10,
}
const rec={
  
}
const coins = ref(100) // 初始金币数量

// 购买卡包
const handleBuyPack = () => {
  const packPrice = 10
  if (coins.value >= packPrice) {
    coins.value -= packPrice
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

const handleBuyAdvancedPack = () => {
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

const gameCanvas = ref(null)
let game = null

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
      fontSize: '28px'  // 稍微减小字体
    }).setOrigin(0.5).setDepth(102)

    const sellText = this.add.text(sellSlot.x + 50, sellSlot.y + 90, '出售卡牌', {  // y位置上移
      fontSize: '14px',
      color: '#ffffff',
      align: 'center',
      padding: { y: 5 }  // 添加垂直内边距
    }).setOrigin(0.5).setDepth(102)

      // 为出售槽添加交互效果
      ;[sellIcon, sellText].forEach(element => {
        element.setInteractive()
        element.on('pointerover', () => {
          sellSlot.setStrokeStyle(2, 0xffffff)
        })
        element.on('pointerout', () => {
          sellSlot.setStrokeStyle(2, 0x6e5773)
        })
      })

    // 创建第一个购买槽
    const buySlot = this.add.rectangle(padding * 2 + 100, padding, 100, 140, 0x6e5773) // 使用渐变色的深色部分
      .setOrigin(0, 0)
      .setDepth(101)
      .setInteractive({ useHandCursor: true }) // 添加手型光标
      .setStrokeStyle(2, 0x8c7853)


    // 购买槽文本
    const buyIcon = this.add.text(buySlot.x + 50, buySlot.y + 40, '🎁', {  // y位置上移
      fontSize: '28px'  // 稍微减小字体
    }).setOrigin(0.5).setDepth(102)

    const buyText = this.add.text(buySlot.x + 50, buySlot.y + 90, '诗意卡包\n10金币', {  // y位置上移
      fontSize: '14px',
      color: '#ffffff',
      align: 'center',
      lineSpacing: 2,  // 减小行间距
      padding: { y: 5 }  // 添加垂直内边距
    }).setOrigin(0.5).setDepth(102)



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
        fontSize: '28px'
      }).setOrigin(0.5).setDepth(102)

      const buyText2 = this.add.text(buySlot2.x + 50, buySlot2.y + 90, '诗人卡包\n15金币', {
        fontSize: '14px',
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
        fontSize: '28px'
      }).setOrigin(0.5).setDepth(102)

      const buyText3 = this.add.text(buySlot3.x + 50, buySlot3.y + 90, '书生卡\n10金币', {
        fontSize: '14px',
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
        fontSize: '28px'
      }).setOrigin(0.5).setDepth(102)

      const buyText4 = this.add.text(buySlot4.x + 50, buySlot4.y + 90, '书斋卡\n10金币', {
        fontSize: '14px',
        color: '#ffffff',
        align: 'center',
        lineSpacing: 2,
        padding: { y: 5 }
      }).setOrigin(0.5).setDepth(102)

      // 第三个购买槽的处理函数（购买工人卡）
      const handleBuyWorker = () => {
        if (coins.value >= 10) {
          coins.value -= 10
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
        0x6e5773
      )
        .setOrigin(0, 0)
        .setDepth(100)
        .setStrokeStyle(2, 0x8c7853);

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
            color: '#ffffff'
          }).setOrigin(0, 0.5).setDepth(101);
        }

        // 添加槽位提示文本
        const slotText = i === 3 ? '诗词' : i === 2 ? '诗人' : `诗意${i + 1}`;
        this.add.text(x + cardWidth / 2, y - 5, slotText, {
          fontSize: '12px',
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
        color: '#ffffff'
      }
    )
      .setOrigin(1, 0.5)
      .setDepth(101); 

    // 更新金币显示和背景
    this.events.on('update', () => {
      coinDisplay.setText(`💰 ${coins.value}`)
    })

    // 添加窗口缩放事件处理
    this.scale.on('resize', (gameSize) => {
      // 更新顶部边栏
      topBar.width = gameSize.width;

      // 更新金币显示位置
      coinDisplay.x = gameSize.width - padding - 10;
      coinBackground.x = gameSize.width - padding;

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
            color: '#ffffff'
          }).setOrigin(0, 0.5).setDepth(101);
        }

        const slotText = i === 3 ? '诗词' : i === 2 ? '诗人' : `诗意${i + 1}`;
        this.add.text(x + cardWidth / 2, slot.y - 5, slotText, {
          fontSize: '12px',
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

          // 添加金币动画
          const priceText = this.add.text(pointer.x, pointer.y, `+${totalPrice}`, {
            fontSize: '24px',
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

          return
        }
      }

      // 检查合成
      this.cards.forEach(otherCard => {
        if (otherCard !== gameObject &&
          Phaser.Geom.Intersects.RectangleToRectangle(gameObject.getBounds(), otherCard.getBounds())) {
          
          // 检查两张卡是否都不在堆叠组中
          const card1Stack = this.cardStacks.find(s => s.includes(gameObject))
          const card2Stack = this.cardStacks.find(s => s.includes(otherCard))
          
          // 只有当两张卡都不在堆叠组中时才允许合成
          if (!card1Stack && !card2Stack) {
            const card1Type = gameObject.getData('type')
            const card2Type = otherCard.getData('type')

            const resultType = checkRecipe(card1Type, card2Type)

            if (resultType) {
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
        }
      })
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
    })

    // 修改拖拽中事件
    this.input.on('drag', (pointer, gameObject, dragX, dragY) => {
      // const minY = topBarHeight
      // const newY = Math.max(minY, dragY)
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
  }

  // 需要持续更新的逻辑
  function update() {
    // 可以添加需要持续更新的逻辑
  }
})

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
  display: flex;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #8c7853;
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
=======
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
            <h3>成就列表</h3>
            <div v-for="achievement in achievements" :key="achievement.id" class="achievement-item">
              <span :class="['achievement-icon', { unlocked: achievement.unlocked }]">🏆</span>
              <div class="achievement-info">
                <div class="achievement-name">{{ achievement.name }}</div>
                <div class="achievement-desc">{{ achievement.description }}</div>
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

// 成就列表数据
const achievements = ref([
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
  }
])

// 合成表数据
const recipes = ref([
  {
    id: 1,
    card1: { src: new URL('../assets/cards/card2.png', import.meta.url).href },
    card2: { src: new URL('../assets/cards/card4.png', import.meta.url).href },
    result: { src: new URL('../assets/cards/cardj.png', import.meta.url).href }
  },
  {
    id: 2,
    card1: { src: new URL('../assets/cards/card3.png', import.meta.url).href },
    card2: { src: new URL('../assets/cards/card5.png', import.meta.url).href },
    result: { src: new URL('../assets/cards/cardq.png', import.meta.url).href }
  },
  {
    id: 3,
    card1: { src: new URL('../assets/cards/cardj.png', import.meta.url).href },
    card2: { src: new URL('../assets/cards/cardq.png', import.meta.url).href },
    result: { src: new URL('../assets/cards/cardk.png', import.meta.url).href }
  },
])

// 卡片素材列表
const cardImages = [
  { key: 'card2', src: new URL('../assets/cards/card2.png', import.meta.url).href },
  { key: 'card3', src: new URL('../assets/cards/card3.png', import.meta.url).href },
  { key: 'card4', src: new URL('../assets/cards/card4.png', import.meta.url).href },
  { key: 'card5', src: new URL('../assets/cards/card5.png', import.meta.url).href },
  { key: 'cardj', src: new URL('../assets/cards/cardj.png', import.meta.url).href },
  { key: 'cardq', src: new URL('../assets/cards/cardq.png', import.meta.url).href },
  { key: 'cardk', src: new URL('../assets/cards/cardk.png', import.meta.url).href },
  { key: 'card_pack', src: new URL('../assets/cards/card_pack.png', import.meta.url).href },
  { key: 'card_worker', src: new URL('../assets/cards/card_worker.png', import.meta.url).href },
  { key: 'card_factory', src: new URL('../assets/cards/card_factory.png', import.meta.url).href },
  { key: 'card8', src: new URL('../assets/cards/card8.png', import.meta.url).href },
  { key: 'card9', src: new URL('../assets/cards/card9.png', import.meta.url).href },
  { key: 'card10', src: new URL('../assets/cards/card10.png', import.meta.url).href },
  { key: 'card_li', src: new URL('../assets/cards/李白.png', import.meta.url).href },
]

// 合成配方映射
const recipeMapping = {
  // key为两张卡的类型组合(按字母顺序排序),value为合成结果
  'card2_card4': 'cardj',
  'card3_card5': 'cardq',
  'cardj_cardq': 'cardk'
}

const craftingRecipes = {
  'card2_card3_card_worker': 'card_li'  // 诗意1+诗意2+诗人=李白
}

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
  card2: 3,
  card3: 3,
  card4: 5,
  card5: 5,
  cardj: 10,
  cardq: 10,
  cardk: 10,
  card8: 10,
  card9: 10,
  card10: 10,
  card_worker: 10,
  card_factory: 10,
}

const coins = ref(100) // 初始金币数量

// 购买卡包
const handleBuyPack = () => {
  const packPrice = 10
  if (coins.value >= packPrice) {
    coins.value -= packPrice
    const scene = game.scene.scenes[0]

    // 在随机位置创建卡包
    const x = Math.random() * (scene.scale.width - 100) + 50
    const y = Math.random() * (scene.scale.height - 140 - 180) + 250

    const cardPack = scene.physics.add.image(x, y, 'card_pack')
      .setDisplaySize(100, 140)
      .setInteractive({ cursor: 'pointer', useHandCursor: true, draggable: true })
      .setCollideWorldBounds(true)
      .setBounce(0.8)
      .setData('clickCount', 0)
      .setData('type', 'card_pack')
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
          const allCards = ['card2', 'card3', 'card4', 'card5', 'cardj', 'cardq', 'cardk']
          const numCards = 5

          // 创建闪光效果
          const flash = scene.add.sprite(cardPack.x, cardPack.y, 'card3')
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

const handleBuyAdvancedPack = () => {
  const scene = game.scene.scenes[0]
  
  // 在随机位置创建卡包
  const x = Math.random() * (scene.scale.width - 100) + 50
  const y = Math.random() * (scene.scale.height - 140 - 180) + 250

  const advancedPack = scene.physics.add.image(x, y, 'card_pack')
    .setDisplaySize(100, 140)
    .setInteractive({ cursor: 'pointer', useHandCursor: true, draggable: true })
    .setCollideWorldBounds(true)
    .setBounce(0.8)
    .setData('clickCount', 0)
    .setData('type', 'advanced_pack')
    .setData('isDragging', false)
    .setData('pointerDown', false)
    .setData('dragStartX', 0)
    .setData('dragStartY', 0)
    .setTint(0xffd700) // 添加金色色调以区分高级卡包

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
        const advancedCards = ['card8', 'card9', 'card10']
        const numCards = 3

        // 创建闪光效果
        const flash = scene.add.sprite(advancedPack.x, advancedPack.y, 'card3')
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

const gameCanvas = ref(null)
let game = null

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
      fontSize: '28px'  // 稍微减小字体
    }).setOrigin(0.5).setDepth(102)

    const sellText = this.add.text(sellSlot.x + 50, sellSlot.y + 90, '出售卡牌', {  // y位置上移
      fontSize: '14px',
      color: '#ffffff',
      align: 'center',
      padding: { y: 5 }  // 添加垂直内边距
    }).setOrigin(0.5).setDepth(102)

      // 为出售槽添加交互效果
      ;[sellIcon, sellText].forEach(element => {
        element.setInteractive()
        element.on('pointerover', () => {
          sellSlot.setStrokeStyle(2, 0xffffff)
        })
        element.on('pointerout', () => {
          sellSlot.setStrokeStyle(2, 0x6e5773)
        })
      })

    // 创建第一个购买槽
    const buySlot = this.add.rectangle(padding * 2 + 100, padding, 100, 140, 0x6e5773) // 使用渐变色的深色部分
      .setOrigin(0, 0)
      .setDepth(101)
      .setInteractive({ useHandCursor: true }) // 添加手型光标
      .setStrokeStyle(2, 0x8c7853)


    // 购买槽文本
    const buyIcon = this.add.text(buySlot.x + 50, buySlot.y + 40, '🎁', {  // y位置上移
      fontSize: '28px'  // 稍微减小字体
    }).setOrigin(0.5).setDepth(102)

    const buyText = this.add.text(buySlot.x + 50, buySlot.y + 90, '诗意卡包\n10金币', {  // y位置上移
      fontSize: '14px',
      color: '#ffffff',
      align: 'center',
      lineSpacing: 2,  // 减小行间距
      padding: { y: 5 }  // 添加垂直内边距
    }).setOrigin(0.5).setDepth(102)



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
        fontSize: '28px'
      }).setOrigin(0.5).setDepth(102)

      const buyText2 = this.add.text(buySlot2.x + 50, buySlot2.y + 90, '诗人卡包\n15金币', {
        fontSize: '14px',
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
        fontSize: '28px'
      }).setOrigin(0.5).setDepth(102)

      const buyText3 = this.add.text(buySlot3.x + 50, buySlot3.y + 90, '诗仙卡\n10金币', {
        fontSize: '14px',
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
        fontSize: '28px'
      }).setOrigin(0.5).setDepth(102)

      const buyText4 = this.add.text(buySlot4.x + 50, buySlot4.y + 90, '书斋卡\n10金币', {
        fontSize: '14px',
        color: '#ffffff',
        align: 'center',
        lineSpacing: 2,
        padding: { y: 5 }
      }).setOrigin(0.5).setDepth(102)

      // 第三个购买槽的处理函数（购买工人卡）
      const handleBuyWorker = () => {
        if (coins.value >= 10) {
          coins.value -= 10
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

          const factoryCard = this.physics.add.image(x, y, 'card_factory')
            .setDisplaySize(100, 140)
            .setInteractive({ cursor: 'pointer', useHandCursor: true })
            .setCollideWorldBounds(true)
            .setBounce(0.8)
            .setData('type', 'card_factory')
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
        0x6e5773
      )
        .setOrigin(0, 0)
        .setDepth(100)
        .setStrokeStyle(2, 0x8c7853);

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
            color: '#ffffff'
          }).setOrigin(0, 0.5).setDepth(101);
        }

        // 添加槽位提示文本
        const slotText = i === 3 ? '诗词' : i === 2 ? '诗人' : `诗意${i + 1}`;
        this.add.text(x + cardWidth / 2, y - 5, slotText, {
          fontSize: '12px',
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
        color: '#ffffff'
      }
    )
      .setOrigin(1, 0.5)
      .setDepth(101); 

    // 更新金币显示和背景
    this.events.on('update', () => {
      coinDisplay.setText(`💰 ${coins.value}`)
    })

    // 添加窗口缩放事件处理
    this.scale.on('resize', (gameSize) => {
      // 更新顶部边栏
      topBar.width = gameSize.width;

      // 更新金币显示位置
      coinDisplay.x = gameSize.width - padding - 10;
      coinBackground.x = gameSize.width - padding;

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
            color: '#ffffff'
          }).setOrigin(0, 0.5).setDepth(101);
        }

        const slotText = i === 3 ? '诗词' : i === 2 ? '诗人' : `诗意${i + 1}`;
        this.add.text(x + cardWidth / 2, slot.y - 5, slotText, {
          fontSize: '12px',
          color: '#ffffff'
        }).setOrigin(0.5, 1).setDepth(101);
      });
    });

    // 创建初始卡片
    const initialCards = ['card2', 'card3', 'card4', 'card5'] // 只包含基础卡片

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

          // 添加金币动画
          const priceText = this.add.text(pointer.x, pointer.y, `+${totalPrice}`, {
            fontSize: '24px',
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

          return
        }
      }

      // 检查合成
      this.cards.forEach(otherCard => {
        if (otherCard !== gameObject &&
          Phaser.Geom.Intersects.RectangleToRectangle(gameObject.getBounds(), otherCard.getBounds())) {
          
          // 检查两张卡是否都不在堆叠组中
          const card1Stack = this.cardStacks.find(s => s.includes(gameObject))
          const card2Stack = this.cardStacks.find(s => s.includes(otherCard))
          
          // 只有当两张卡都不在堆叠组中时才允许合成
          if (!card1Stack && !card2Stack) {
            const card1Type = gameObject.getData('type')
            const card2Type = otherCard.getData('type')

            const resultType = checkRecipe(card1Type, card2Type)

            if (resultType) {
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

              // 添加闪光效果
              const flash = this.add.sprite(x, y, 'card3')
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
        }
      })
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
    })

    // 修改拖拽中事件
    this.input.on('drag', (pointer, gameObject, dragX, dragY) => {
      // const minY = topBarHeight
      // const newY = Math.max(minY, dragY)
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
  }

  // 需要持续更新的逻辑
  function update() {
    // 可以添加需要持续更新的逻辑
  }
})

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
  display: flex;
  align-items: center;
  padding: 10px;
  border-bottom: 1px solid #8c7853;
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
>>>>>>> 0ea949234d24bad6b49e8e960d92817ebee4d251
</style>