import Phaser from 'phaser'
import { cardImages, getCardImageSrc, checkRecipe, checkCrafting, cardPrices } from './cards'
import { unlockAchievement, unlockPoemAchievement, checkPoemCollectorAchievement } from './achievements'
import { checkFactoryAchievement } from './factory'
import { showAchievementUnlocked, getPoemName } from './utils'

/**
 * 创建并初始化游戏
 * @param {Object} options - 配置项
 * @param {HTMLElement} options.container - 游戏容器 DOM
 * @param {Ref<number>} options.coins - 响应式金币
 * @param {Ref} options.basicAchievements - 响应式基础成就
 * @param {Ref} options.poemAchievements - 响应式诗词成就
 * @param {Ref} options.isStackingMode - 响应式堆叠模式
 * @param {Ref} options.recipes - 响应式配方表
 * @param {Function} options.unlockRecipe - 解锁配方函数
 */
export function createGame(options) {
  const {
    container,
    coins,
    basicAchievements,
    poemAchievements,
    isStackingMode,
    recipes,
    unlockRecipe
  } = options

  let game = null

  // Phaser 配置
  const config = {
    type: Phaser.AUTO,
    width: container.clientWidth,
    height: container.clientHeight,
    parent: container,
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
      preload,
      create,
      update
    }
  }

  game = new Phaser.Game(config)

  // 预加载资源
  function preload() {
    cardImages.forEach(card => {
      this.load.image(card.key, card.src)
    })
  }

  // 创建场景
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

  // 工厂生产等持续更新
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

  // 返回 game 实例，便于外部销毁
  return game
}