import { ref, reactive, computed } from 'vue'
import { useUserStore } from '@/stores/user'
import API_BASE_URL from '@/config/api'
export function useGameStats() {
  const loading = ref(false)
  
  // 游戏总览统计
  const gameStats = reactive({
    totalGames: 0,
    wins: 0,
    winRate: 0,
    highestScore: 0,
    totalPlayTime: 0,
    averageScore: 0,
    gamesThisWeek: 0,
    gamesThisMonth: 0
  })

  // 游戏详细统计
  const gameDetails = reactive({
    quiz: {
      attempts: 0,
      accuracy: 0,
      bestScore: 0,
      avgTime: 0,
      totalQuestions: 0,
      correctAnswers: 0,
      streak: 0,
      maxStreak: 0
    },
    feihua: {
      battles: 0,
      wins: 0,
      winRate: 0,
      maxStreak: 0,
      bestTheme: '暂无',
      totalRounds: 0,
      averageRoundsPerBattle: 0,
      favoriteKeywords: []
    },
    game: {
      plays: 0,
      completed: 0,
      completionRate: 0,
      maxLevel: 0,
      collected: 0,
      totalItems: 100,
      collectionRate: 0,
      totalPlayTime: 0
    }
  })

  // 成就系统
  const achievements = ref([
    {
      id: 'first_login',
      name: '初来乍到',
      description: '首次登录系统',
      icon: '👋',
      unlocked: false,
      progress: 0,
      target: 1,
      unlockedAt: null
    },
    {
      id: 'poem_reader',
      name: '诗词爱好者',
      description: '阅读10首诗词',
      icon: '📚',
      unlocked: false,
      progress: 0,
      target: 10,
      unlockedAt: null
    },
    {
      id: 'quiz_master',
      name: '问答大师',
      description: '诗词问答连续答对10题',
      icon: '🧠',
      unlocked: false,
      progress: 0,
      target: 10,
      unlockedAt: null
    },
    {
      id: 'feihua_expert',
      name: '飞花令专家',
      description: '飞花令连胜5场',
      icon: '🌸',
      unlocked: false,
      progress: 0,
      target: 5,
      unlockedAt: null
    },
    {
      id: 'collector',
      name: '收藏家',
      description: '收藏20首诗词',
      icon: '⭐',
      unlocked: false,
      progress: 0,
      target: 20,
      unlockedAt: null
    },
    {
      id: 'daily_player',
      name: '每日一练',
      description: '连续7天使用系统',
      icon: '📅',
      unlocked: false,
      progress: 0,
      target: 7,
      unlockedAt: null
    },
    {
      id: 'high_scorer',
      name: '高分选手',
      description: '单次游戏得分超过5000',
      icon: '🏆',
      unlocked: false,
      progress: 0,
      target: 5000,
      unlockedAt: null
    },
    {
      id: 'speed_reader',
      name: '速读达人',
      description: '一天内阅读50首诗词',
      icon: '⚡',
      unlocked: false,
      progress: 0,
      target: 50,
      unlockedAt: null
    }
  ])

  // 计算属性
  const unlockedAchievements = computed(() => {
    return achievements.value.filter(a => a.unlocked)
  })

  const achievementProgress = computed(() => {
    const total = achievements.value.length
    const unlocked = unlockedAchievements.value.length
    return {
      total,
      unlocked,
      percentage: total > 0 ? Math.round((unlocked / total) * 100) : 0
    }
  })

  // 加载游戏统计数据
// 🔧 修改 loadGameStats 函数，添加真实成就数据获取
const loadGameStats = async () => {
  console.log('🎮 开始加载游戏统计数据...')
  loading.value = true
  
    try {
      // 🔧 新增：获取真实成就数据
      const userStore = useUserStore()
      if (userStore.isAuthenticated && userStore.uid) {
        console.log('🏆 获取用户成就数据...')
        const response = await fetch(`${API_BASE_URL}/user/achievements/${userStore.uid}`)
        const result = await response.json()
        
        if (result.success) {
          // 🔧 直接使用后端返回的成就数据
          achievements.value = result.achievements
          console.log('✅ 成就数据获取成功:', result.achievements.length, '个成就')
        } else {
          console.error('❌ 获取成就数据失败:', result.message)
          // 保持原有的模拟数据作为备用
          await generateMockGameStats()
          updateAchievements()
        }
      } else {
        console.log('⚠️ 用户未登录，使用模拟数据')
        await generateMockGameStats()
        updateAchievements()
      }
      
    } catch (error) {
      console.error('💥 加载成就数据失败:', error)
      // 出错时使用模拟数据
      await generateMockGameStats()
      updateAchievements()
    } finally {
      loading.value = false
    }
  }

  // 生成模拟游戏数据
  const generateMockGameStats = async () => {
    // 总览统计
    const mockGameStats = {
      totalGames: Math.floor(Math.random() * 50) + 20,
      wins: Math.floor(Math.random() * 30) + 15,
      highestScore: Math.floor(Math.random() * 5000) + 5000,
      totalPlayTime: Math.floor(Math.random() * 300) + 120, // 分钟
      gamesThisWeek: Math.floor(Math.random() * 10) + 5,
      gamesThisMonth: Math.floor(Math.random() * 30) + 15
    }
    
    // 计算胜率和平均分数
    mockGameStats.winRate = mockGameStats.totalGames > 0 
      ? Math.round((mockGameStats.wins / mockGameStats.totalGames) * 100) 
      : 0
    mockGameStats.averageScore = Math.floor(mockGameStats.highestScore * 0.7)
    
    Object.assign(gameStats, mockGameStats)
    
    // 诗词测验统计
    const mockQuizStats = {
      attempts: Math.floor(Math.random() * 20) + 10,
      totalQuestions: Math.floor(Math.random() * 200) + 100,
      correctAnswers: Math.floor(Math.random() * 150) + 80,
      avgTime: Math.floor(Math.random() * 30) + 30,
      streak: Math.floor(Math.random() * 5),
      maxStreak: Math.floor(Math.random() * 15) + 5
    }
    mockQuizStats.accuracy = mockQuizStats.totalQuestions > 0 
      ? Math.round((mockQuizStats.correctAnswers / mockQuizStats.totalQuestions) * 100)
      : 0
    mockQuizStats.bestScore = Math.floor(Math.random() * 5000) + 3000
    
    Object.assign(gameDetails.quiz, mockQuizStats)
    
    // 飞花令统计
    const mockFeihuaStats = {
      battles: Math.floor(Math.random() * 15) + 5,
      wins: Math.floor(Math.random() * 10) + 3,
      maxStreak: Math.floor(Math.random() * 5) + 1,
      bestTheme: ['春天', '秋天', '思乡', '友情', '爱情'][Math.floor(Math.random() * 5)],
      totalRounds: Math.floor(Math.random() * 100) + 50,
      favoriteKeywords: ['春', '花', '月', '风', '雨'].slice(0, Math.floor(Math.random() * 3) + 2)
    }
    mockFeihuaStats.winRate = mockFeihuaStats.battles > 0 
      ? Math.round((mockFeihuaStats.wins / mockFeihuaStats.battles) * 100)
      : 0
    mockFeihuaStats.averageRoundsPerBattle = mockFeihuaStats.battles > 0 
      ? Math.round(mockFeihuaStats.totalRounds / mockFeihuaStats.battles)
      : 0
    
    Object.assign(gameDetails.feihua, mockFeihuaStats)
    
    // 诗词游戏统计
    const mockGameDetails = {
      plays: Math.floor(Math.random() * 10) + 3,
      completed: Math.floor(Math.random() * 8) + 2,
      maxLevel: Math.floor(Math.random() * 20) + 5,
      collected: Math.floor(Math.random() * 50) + 20,
      totalPlayTime: Math.floor(Math.random() * 120) + 60 // 分钟
    }
    mockGameDetails.completionRate = mockGameDetails.plays > 0 
      ? Math.round((mockGameDetails.completed / mockGameDetails.plays) * 100)
      : 0
    mockGameDetails.collectionRate = Math.round((mockGameDetails.collected / 100) * 100)
    
    Object.assign(gameDetails.game, mockGameDetails)
    
    console.log('📊 游戏统计数据已生成:', { gameStats, gameDetails })
  }

  // 更新成就进度
  const updateAchievements = () => {
    achievements.value.forEach(achievement => {
      switch (achievement.id) {
        case 'first_login':
          achievement.progress = 1
          achievement.unlocked = true
          achievement.unlockedAt = new Date().toISOString()
          break
          
        case 'quiz_master':
          achievement.progress = gameDetails.quiz.maxStreak
          if (achievement.progress >= achievement.target) {
            achievement.unlocked = true
            achievement.unlockedAt = new Date().toISOString()
          }
          break
          
        case 'feihua_expert':
          achievement.progress = gameDetails.feihua.maxStreak
          if (achievement.progress >= achievement.target) {
            achievement.unlocked = true
            achievement.unlockedAt = new Date().toISOString()
          }
          break
          
        case 'high_scorer':
          achievement.progress = gameStats.highestScore
          if (achievement.progress >= achievement.target) {
            achievement.unlocked = true
            achievement.unlockedAt = new Date().toISOString()
          }
          break
          
        default:
          // 随机解锁一些成就用于演示
          if (Math.random() > 0.6) {
            achievement.unlocked = true
            achievement.progress = achievement.target
            achievement.unlockedAt = new Date().toISOString()
          } else {
            achievement.progress = Math.floor(Math.random() * achievement.target)
          }
      }
    })
    
    console.log('🏆 成就更新完成:', achievementProgress.value)
  }

  // 更新单个游戏的统计
  const updateGameResult = (gameType, result) => {
    console.log('🎯 更新游戏结果:', { gameType, result })
    
    gameStats.totalGames++
    if (result.won) {
      gameStats.wins++
    }
    
    // 更新胜率
    gameStats.winRate = Math.round((gameStats.wins / gameStats.totalGames) * 100)
    
    // 更新最高分
    if (result.score > gameStats.highestScore) {
      gameStats.highestScore = result.score
    }
    
    // 根据游戏类型更新详细统计
    switch (gameType) {
      case 'quiz':
        gameDetails.quiz.attempts++
        if (result.won) {
          gameDetails.quiz.streak++
          if (gameDetails.quiz.streak > gameDetails.quiz.maxStreak) {
            gameDetails.quiz.maxStreak = gameDetails.quiz.streak
          }
        } else {
          gameDetails.quiz.streak = 0
        }
        break
        
      case 'feihua':
        gameDetails.feihua.battles++
        if (result.won) {
          gameDetails.feihua.wins++
        }
        gameDetails.feihua.winRate = Math.round((gameDetails.feihua.wins / gameDetails.feihua.battles) * 100)
        break
        
      case 'game':
        gameDetails.game.plays++
        if (result.completed) {
          gameDetails.game.completed++
        }
        gameDetails.game.completionRate = Math.round((gameDetails.game.completed / gameDetails.game.plays) * 100)
        break
    }
    
    // 重新检查成就
    updateAchievements()
  }

  // 重置统计数据
  const resetGameStats = () => {
    Object.assign(gameStats, {
      totalGames: 0,
      wins: 0,
      winRate: 0,
      highestScore: 0,
      totalPlayTime: 0,
      averageScore: 0,
      gamesThisWeek: 0,
      gamesThisMonth: 0
    })
    
    Object.assign(gameDetails.quiz, {
      attempts: 0,
      accuracy: 0,
      bestScore: 0,
      avgTime: 0,
      totalQuestions: 0,
      correctAnswers: 0,
      streak: 0,
      maxStreak: 0
    })
    
    Object.assign(gameDetails.feihua, {
      battles: 0,
      wins: 0,
      winRate: 0,
      maxStreak: 0,
      bestTheme: '暂无',
      totalRounds: 0,
      averageRoundsPerBattle: 0,
      favoriteKeywords: []
    })
    
    Object.assign(gameDetails.game, {
      plays: 0,
      completed: 0,
      completionRate: 0,
      maxLevel: 0,
      collected: 0,
      totalItems: 100,
      collectionRate: 0,
      totalPlayTime: 0
    })
    
    achievements.value.forEach(achievement => {
      achievement.unlocked = false
      achievement.progress = 0
      achievement.unlockedAt = null
    })
  }

  return {
    // 状态
    loading,
    gameStats,
    gameDetails,
    achievements,
    
    // 计算属性
    unlockedAchievements,
    achievementProgress,
    
    // 方法
    loadGameStats,
    updateGameResult,
    resetGameStats,
    updateAchievements
  }
}