import { ref } from 'vue'

// 基础成就
export const basicAchievements = ref([
  { id: 1, name: '首次购买卡包', description: '第一次购买诗意卡包', unlocked: false },
  { id: 2, name: '首次出售卡片', description: '第一次出售卡片', unlocked: false },
  { id: 3, name: '首次合成', description: '第一次合成卡片', unlocked: false },
  { id: 4, name: '首次建造书斋', description: '第一次建造书斋', unlocked: false },
  { id: 5, name: '首次雇佣书生', description: '第一次雇佣书生', unlocked: false },
  { id: 6, name: '首次合成诗词', description: '第一次合成诗词', unlocked: false },
  { id: 7, name: '首次解锁配方', description: '第一次解锁配方', unlocked: false },
  { id: 8, name: '首次解锁诗意卡', description: '第一次解锁诗意卡', unlocked: false },
  { id: 9, name: '诗词收藏家', description: '解锁10首诗词', unlocked: false },
  { id: 10, name: '爱情的发现', description: '获得“爱情”诗意卡', unlocked: false }
])

// 诗词成就（补全所有诗词成就）
export const poemAchievements = ref([
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

/**
 * 解锁成就
 * @param {number|string} achievementId
 * @param {Function} [showAchievementUnlocked] - 可选，成就提示回调
 */
export function unlockAchievement(achievementId, showAchievementUnlocked) {
  const idNum = parseInt(achievementId)
  if (idNum <= 10) {
    const basic = basicAchievements.value.find(a => a.id === idNum)
    if (basic && !basic.unlocked) {
      basic.unlocked = true
      showAchievementUnlocked && showAchievementUnlocked(basic.name)
    }
  } else {
    const poem = poemAchievements.value.find(a => a.id === idNum)
    if (poem && !poem.unlocked) {
      poem.unlocked = true
      showAchievementUnlocked && showAchievementUnlocked(poem.name)
    }
  }
}

/**
 * 检查诗词收藏家成就
 * @param {Function} [showAchievementUnlocked]
 */
export function checkPoemCollectorAchievement(showAchievementUnlocked) {
  const unlockedPoemCount = poemAchievements.value.filter(a => a.unlocked).length
  if (unlockedPoemCount >= 10 && !basicAchievements.value[8].unlocked) {
    unlockAchievement(9, showAchievementUnlocked)
  }
}

/**
 * 解锁诗词成就
 * @param {string} poemName
 * @param {Function} [showAchievementUnlocked]
 */
export function unlockPoemAchievement(poemName, showAchievementUnlocked) {
  const achievement = poemAchievements.value.find(a => a.name === poemName)
  if (achievement && !achievement.unlocked) {
    achievement.unlocked = true
    showAchievementUnlocked && showAchievementUnlocked(achievement.name)
  }
}