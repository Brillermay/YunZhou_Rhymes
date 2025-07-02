// filepath: c:\Users\Administrator\Desktop\YunZhou_Rhymes\poetry_front\src\components\homepage\utils\gsapAnimations.js

import { gsap } from 'gsap'
import { TextPlugin } from 'gsap/TextPlugin'
import { ScrollTrigger } from 'gsap/ScrollTrigger'

// 注册GSAP插件
gsap.registerPlugin(TextPlugin, ScrollTrigger)

// ===== 🎬 动画配置常量 =====

export const ANIMATION_CONFIG = {
  // 缓动函数
  easing: {
    smooth: "power2.out",
    bouncy: "back.out(1.7)",
    elastic: "elastic.out(1, 0.3)",
    expo: "expo.out",
    sine: "sine.inOut"
  },
  
  // 持续时间
  duration: {
    fast: 0.3,
    medium: 0.8,
    slow: 1.5,
    extra: 2.5
  },
  
  // 延迟时间
  delay: {
    short: 0.1,
    medium: 0.3,
    long: 0.8
  }
}

// ===== 🎭 标题动画系统 =====

/**
 * 汉字标题入场动画
 * @param {Array} elements - 汉字元素数组
 * @param {Function} onComplete - 完成回调
 */
export const animateChineseTitle = (elements, onComplete) => {
  const tl = gsap.timeline({
    onComplete: onComplete
  })
  
  // 预设初始状态
  gsap.set(elements, {
    opacity: 0,
    y: 120,
    rotationX: 90,
    scale: 0.3,
    filter: 'blur(15px)'
  })
  
  // 逐字动画
  tl.to(elements, {
    opacity: 1,
    y: 0,
    rotationX: 0,
    scale: 1,
    filter: 'blur(0px)',
    duration: ANIMATION_CONFIG.duration.slow,
    stagger: {
      amount: 1.5,
      from: "start",
      ease: ANIMATION_CONFIG.easing.bouncy
    },
    ease: ANIMATION_CONFIG.easing.bouncy
  })
  
  // 添加微妙的摇摆效果
  .to(elements, {
    rotation: "random(-3, 3)",
    duration: ANIMATION_CONFIG.duration.medium,
    stagger: 0.1,
    ease: ANIMATION_CONFIG.easing.sine,
    repeat: 1,
    yoyo: true
  }, "-=0.5")
  
  return tl
}

/**
 * 英文副标题动画
 * @param {Element} element - 英文标题元素
 */
export const animateEnglishSubtitle = (element) => {
  return gsap.fromTo(element,
    {
      opacity: 0,
      y: 40,
      filter: 'blur(8px)'
    },
    {
      opacity: 1,
      y: 0,
      filter: 'blur(0px)',
      duration: ANIMATION_CONFIG.duration.medium,
      ease: ANIMATION_CONFIG.easing.smooth,
      delay: ANIMATION_CONFIG.delay.medium
    }
  )
}

/**
 * 毛笔笔画效果
 * @param {Element} element - 笔画元素
 */
export const animateBrushStroke = (element) => {
  return gsap.fromTo(element,
    {
      scaleX: 0,
      opacity: 0,
      transformOrigin: "left center"
    },
    {
      scaleX: 1,
      opacity: 1,
      duration: ANIMATION_CONFIG.duration.slow,
      ease: ANIMATION_CONFIG.easing.expo
    }
  )
}

// ===== 🎮 按钮动画系统 =====

/**
 * 按钮入场动画
 * @param {Element} element - 按钮元素
 */
export const animateButtonEntrance = (element) => {
  return gsap.fromTo(element,
    {
      scale: 0,
      rotation: -180,
      opacity: 0,
      filter: 'blur(10px)'
    },
    {
      scale: 1,
      rotation: 0,
      opacity: 1,
      filter: 'blur(0px)',
      duration: ANIMATION_CONFIG.duration.slow,
      ease: ANIMATION_CONFIG.easing.bouncy
    }
  )
}

/**
 * 按钮悬浮效果
 * @param {Element} element - 按钮元素
 */
export const animateButtonHover = (element) => {
  return gsap.to(element, {
    scale: 1.05,
    y: -3,
    duration: ANIMATION_CONFIG.duration.fast,
    ease: ANIMATION_CONFIG.easing.smooth
  })
}

/**
 * 按钮离开效果
 * @param {Element} element - 按钮元素
 */
export const animateButtonLeave = (element) => {
  return gsap.to(element, {
    scale: 1,
    y: 0,
    duration: ANIMATION_CONFIG.duration.fast,
    ease: ANIMATION_CONFIG.easing.smooth
  })
}

/**
 * 按钮点击水墨扩散效果
 * @param {Element} element - 扩散元素
 * @param {number} x - 点击X坐标
 * @param {number} y - 点击Y坐标
 */
export const animateInkRipple = (element, x = 0, y = 0) => {
  gsap.set(element, {
    x: x,
    y: y,
    scale: 0,
    opacity: 1
  })
  
  return gsap.to(element, {
    scale: 3,
    opacity: 0,
    duration: ANIMATION_CONFIG.duration.medium,
    ease: ANIMATION_CONFIG.easing.expo
  })
}

// ===== 🌊 背景动画系统 =====

/**
 * 粒子漂浮动画
 * @param {Array} particles - 粒子元素数组
 */
export const animateParticleFloat = (particles) => {
  particles.forEach((particle, index) => {
    gsap.to(particle, {
      y: "random(-20, 20)",
      x: "random(-15, 15)",
      rotation: "random(-10, 10)",
      duration: "random(3, 6)",
      ease: ANIMATION_CONFIG.easing.sine,
      repeat: -1,
      yoyo: true,
      delay: index * 0.1
    })
  })
}

/**
 * 云层飘动动画
 * @param {Array} clouds - 云层元素数组
 */
export const animateCloudDrift = (clouds) => {
  clouds.forEach((cloud, index) => {
    const duration = 15 + Math.random() * 10
    const amplitude = 20 + Math.random() * 30
    
    gsap.to(cloud, {
      x: `+=${amplitude}`,
      y: `+=${amplitude * 0.3}`,
      rotation: "random(-2, 2)",
      duration: duration,
      ease: ANIMATION_CONFIG.easing.sine,
      repeat: -1,
      yoyo: true,
      delay: index * 0.5
    })
  })
}

// ===== 🎭 装饰动画系统 =====

/**
 * 印章旋转动画
 * @param {Element} element - 印章元素
 */
export const animateSealRotation = (element) => {
  return gsap.to(element, {
    rotation: 360,
    duration: 20,
    ease: "none",
    repeat: -1
  })
}

/**
 * 装饰点闪烁动画
 * @param {Array} dots - 装饰点数组
 */
export const animateDecorativeDots = (dots) => {
  dots.forEach((dot, index) => {
    gsap.to(dot, {
      opacity: "random(0.3, 0.8)",
      scale: "random(0.8, 1.2)",
      duration: "random(2, 4)",
      ease: ANIMATION_CONFIG.easing.sine,
      repeat: -1,
      yoyo: true,
      delay: index * 0.3
    })
  })
}

// ===== 🔄 页面转换动画 =====

/**
 * 页面淡出动画
 * @param {Element} element - 页面容器元素
 * @param {Function} onComplete - 完成回调
 */
export const animatePageExit = (element, onComplete) => {
  const tl = gsap.timeline({
    onComplete: onComplete
  })
  
  tl.to(element, {
    scale: 0.95,
    opacity: 0.7,
    duration: ANIMATION_CONFIG.duration.medium,
    ease: ANIMATION_CONFIG.easing.smooth
  })
  .to(element, {
    scale: 1.1,
    opacity: 0,
    duration: ANIMATION_CONFIG.duration.slow,
    ease: ANIMATION_CONFIG.easing.expo
  }, "-=0.3")
  
  return tl
}

/**
 * 加载完成动画
 * @param {Element} loader - 加载器元素
 * @param {Function} onComplete - 完成回调
 */
export const animateLoadingComplete = (loader, onComplete) => {
  return gsap.to(loader, {
    opacity: 0,
    scale: 0.8,
    filter: 'blur(5px)',
    duration: ANIMATION_CONFIG.duration.medium,
    ease: ANIMATION_CONFIG.easing.expo,
    onComplete: onComplete
  })
}

// ===== 🎵 音效配合动画 =====

/**
 * 带音效的按钮反馈
 * @param {Element} element - 按钮元素
 * @param {Function} playSound - 播放音效函数
 */
export const animateButtonWithSound = (element, playSound) => {
  const tl = gsap.timeline()
  
  tl.to(element, {
    scale: 0.95,
    duration: 0.1,
    ease: ANIMATION_CONFIG.easing.smooth,
    onStart: playSound
  })
  .to(element, {
    scale: 1.05,
    duration: 0.2,
    ease: ANIMATION_CONFIG.easing.bouncy
  })
  .to(element, {
    scale: 1,
    duration: 0.3,
    ease: ANIMATION_CONFIG.easing.smooth
  })
  
  return tl
}

// ===== 📱 响应式动画适配 =====

/**
 * 根据设备调整动画参数
 */
export const getResponsiveConfig = () => {
  const isMobile = window.innerWidth <= 768
  const isTablet = window.innerWidth <= 1024 && window.innerWidth > 768
  
  if (isMobile) {
    return {
      duration: {
        fast: 0.2,
        medium: 0.6,
        slow: 1.2
      },
      stagger: 0.05,
      displacement: 0.7
    }
  } else if (isTablet) {
    return {
      duration: {
        fast: 0.25,
        medium: 0.7,
        slow: 1.3
      },
      stagger: 0.08,
      displacement: 0.85
    }
  } else {
    return {
      duration: ANIMATION_CONFIG.duration,
      stagger: 0.1,
      displacement: 1
    }
  }
}

// ===== 🔧 动画工具函数 =====

/**
 * 创建时间轴动画
 * @param {Array} animations - 动画配置数组
 */
export const createTimeline = (animations) => {
  const tl = gsap.timeline()
  
  animations.forEach(({ element, props, timing = ">" }) => {
    tl.to(element, props, timing)
  })
  
  return tl
}

/**
 * 批量设置元素初始状态
 * @param {Array} elements - 元素数组
 * @param {Object} props - 属性对象
 */
export const setInitialState = (elements, props) => {
  gsap.set(elements, props)
}

/**
 * 清理所有动画
 */
export const killAllAnimations = () => {
  gsap.killTweensOf("*")
  ScrollTrigger.killAll()
}

/**
 * 暂停所有动画
 */
export const pauseAllAnimations = () => {
  gsap.globalTimeline.pause()
}

/**
 * 恢复所有动画
 */
export const resumeAllAnimations = () => {
  gsap.globalTimeline.resume()
}

// ===== 📊 性能监控 =====

/**
 * 获取动画性能信息
 */
export const getAnimationPerformance = () => {
  return {
    activeTweens: gsap.globalTimeline.getChildren().length,
    fps: gsap.ticker.fps,
    lagSmoothing: gsap.ticker.lagSmoothing()
  }
}

// 导出默认配置
export default {
  ANIMATION_CONFIG,
  animateChineseTitle,
  animateEnglishSubtitle,
  animateBrushStroke,
  animateButtonEntrance,
  animateButtonHover,
  animateButtonLeave,
  animateInkRipple,
  animateParticleFloat,
  animateCloudDrift,
  animateSealRotation,
  animateDecorativeDots,
  animatePageExit,
  animateLoadingComplete,
  animateButtonWithSound,
  getResponsiveConfig,
  createTimeline,
  setInitialState,
  killAllAnimations,
  pauseAllAnimations,
  resumeAllAnimations,
  getAnimationPerformance
}