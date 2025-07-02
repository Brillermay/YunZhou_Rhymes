/**
 * 获取诗词名称
 * @param {string} resultType
 * @returns {string}
 */
export function getPoemName(resultType) {
  const poemMap = {
    jiangjinjiu: '将进酒',
    shudaonan: '蜀道难',
    xinglunan: '行路难',
    huanghelousongmenghaoranzhiguangling: '黄鹤楼送孟浩然之广陵',
    jingyesi: '静夜思',
    wanglushanpubu: '望庐山瀑布',
    zengwanglun: '赠汪伦',
    wenwangchanglingzuoqianlongbiaoyaoyouciji: '闻王昌龄左迁龙标遥有此寄',
    emeishanyuege: '峨眉山月歌',
    shizhisaishang: '使至塞上',
    xiangsi: '相思',
    songyuanershianxi: '送元二使安西',
    jiuyuejiuriyishandongxiongdi: '九月九日忆山东兄弟',
    weichengqu: '渭城曲',
    shanjuqiuming: '山居秋暝',
    niaomingjian: '鸟鸣涧',
    zhuliguan: '竹里馆',
    shuidiaogetou_mingyuejishiyou: '水调歌头·明月几时有',
    chibifu: '赤壁赋',
    jichengtansiyeyou: '记承天寺夜游',
    tixilinbi: '题西林壁',
    yinshangchuqinghouyu: '饮湖上初晴后雨',
    dingfengbo_motingchuanlindayesheng: '定风波·莫听穿林打叶声',
    busuanzi_huangzhoudinghuiyuanyujuzuo: '卜算子·黄州定慧院寓居作',
    denggao: '登高',
    maowuweiqiufengsuopoerge: '茅屋为秋风所破歌',
    chunyexiyu: '春夜喜雨',
    wangyue: '望岳',
    wenguanjushouhenanhubei: '闻官军收河南河北',
    chunwang: '春望',
    jueju_lianggehuanglimingcuiliu: '绝句·两个黄鹂鸣翠柳',
    bochuanguazhou: '泊船瓜洲',
    dengfeilaifeng: '登飞来峰',
    yuanri: '元日',
    pozhenzi_weichentongfuzhuangciziyijizhi: '破阵子·为陈同甫赋壮词以寄之',
    xijiangyue_yexinghuangshadaozhong: '西江月·夜行黄沙道中',
    chounuer_shuboshandaozhongbi: '丑奴儿·书博山道中壁',
    guiqulaixici: '归去来兮辞',
    taohuayuanji: '桃花源记',
    yinjian_jieluzairenjing: '饮酒·结庐在人境',
    changhenge: '长恨歌',
    qiantanghuchunxing: '钱塘湖春行',
    fudeguyuancaosongbie: '赋得古原草送别',
    yijiangnan: '忆江南',
    pipaxing: '琵琶行',
    dalinsitaohua: '大林寺桃花',
    loushiming: '陋室铭',
    chouletianyanzhouchufengxishangjianzheng: '酬乐天扬州初逢席上见赠',
    wangdongting: '望洞庭',
    youshanxicun: '游山西村',
    chaitoufeng_hongsushou: '钗头凤·红酥手',
    jinse: '锦瑟',
    wuti_xiangjianshinnanbieyinan: '无题 相见时难别亦难',
    yeyujibei: '夜雨寄北',
    jiasheng: '贾生',
    wanxisha_yiquxincijiuyibei: '浣溪沙·一曲新词酒一杯'
  }
  return poemMap[resultType] || ''
}

/**
 * 显示成就解锁提示
 * @param {Phaser.Game} game
 * @param {string} achievementName
 */
export function showAchievementUnlocked(game, achievementName) {
  if (!game) return
  const scene = game.scene.scenes[0]
  if (!scene) return
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