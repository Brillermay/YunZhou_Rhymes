/**
 * 检查工厂相关成就
 * @param {string} resultType - 合成结果类型
 * @param {Ref<number>} factoryCount - 工厂计数响应式变量
 * @param {Function} unlockAchievement - 解锁成就函数
 * @param {Function} showAchievementUnlocked - 成就提示函数
 */
export function checkFactoryAchievement(resultType, factoryCount, unlockAchievement, showAchievementUnlocked) {
  // 检查是否是工厂类卡片（以 factory_ 开头）
  if (resultType && typeof resultType === 'string' && resultType.startsWith('factory_')) {
    factoryCount.value++;
    // 只在第一次建造工厂时解锁成就
    if (factoryCount.value === 1) {
      unlockAchievement(4, showAchievementUnlocked); // 4号成就：初建书斋
    }
  }
}