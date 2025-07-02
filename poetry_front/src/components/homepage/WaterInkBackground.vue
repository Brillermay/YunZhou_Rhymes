<template>
  <div class="water-ink-background">
    <!-- 主画布 -->
    <canvas 
      ref="canvasRef" 
      class="ink-canvas"
      :width="canvasWidth"
      :height="canvasHeight"
    ></canvas>
    
    <!-- 静态云层 -->
    <div class="static-layers">
      <div 
        v-for="(cloud, index) in cloudLayers" 
        :key="index"
        class="cloud-layer"
        :style="cloud.style"
      ></div>
    </div>
    
    <!-- 渐变遮罩 -->
    <div class="gradient-overlay" :style="gradientStyle"></div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch, nextTick } from 'vue'

// Props
const props = defineProps({
  mousePosition: {
    type: Object,
    default: () => ({ x: 0, y: 0 })
  },
  pageLoaded: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['background-ready'])

// 响应式数据
const canvasRef = ref(null)
const canvasWidth = ref(window.innerWidth)
const canvasHeight = ref(window.innerHeight)
const ctx = ref(null)
const animationId = ref(null)

// 云层数据
const cloudLayers = ref([])
const inkDrops = ref([])
const flowingLines = ref([])

// 时间相关
const timeOffset = ref(0)

// 渐变样式
const gradientStyle = ref({
  background: `
    radial-gradient(ellipse at 20% 10%, rgba(140, 120, 83, 0.1) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 90%, rgba(110, 87, 115, 0.08) 0%, transparent 50%),
    linear-gradient(135deg, rgba(248, 249, 250, 0.8) 0%, rgba(233, 236, 239, 0.6) 100%)
  `
})

// 初始化云层
const initCloudLayers = () => {
  cloudLayers.value = []
  
  for (let i = 0; i < 12; i++) {
    const size = 100 + Math.random() * 300
    const opacity = 0.02 + Math.random() * 0.08
    const duration = 20 + Math.random() * 30
    
    cloudLayers.value.push({
      style: {
        position: 'absolute',
        left: `${Math.random() * 120 - 10}%`,
        top: `${Math.random() * 120 - 10}%`,
        width: `${size}px`,
        height: `${size * 0.6}px`,
        background: `radial-gradient(ellipse, rgba(44, 62, 80, ${opacity}) 0%, rgba(140, 120, 83, ${opacity * 0.5}) 40%, transparent 70%)`,
        borderRadius: '50%',
        animation: `cloud-drift-${i % 3} ${duration}s ease-in-out infinite`,
        animationDelay: `${Math.random() * 10}s`,
        filter: `blur(${0.5 + Math.random()}px)`,
        zIndex: Math.floor(Math.random() * 3) + 1,
        transform: `rotate(${Math.random() * 360}deg)`
      }
    })
  }
}

// 初始化墨滴
const initInkDrops = () => {
  inkDrops.value = []
  
  for (let i = 0; i < 8; i++) {
    inkDrops.value.push({
      x: Math.random() * canvasWidth.value,
      y: Math.random() * canvasHeight.value,
      size: 2 + Math.random() * 6,
      opacity: 0.1 + Math.random() * 0.3,
      speed: 0.2 + Math.random() * 0.5,
      angle: Math.random() * Math.PI * 2,
      life: 0,
      maxLife: 300 + Math.random() * 200
    })
  }
}

// 初始化流动线条
const initFlowingLines = () => {
  flowingLines.value = []
  
  for (let i = 0; i < 5; i++) {
    const points = []
    const numPoints = 15 + Math.random() * 10
    
    for (let j = 0; j < numPoints; j++) {
      points.push({
        x: (j / numPoints) * canvasWidth.value + (Math.random() - 0.5) * 100,
        y: canvasHeight.value * 0.3 + Math.sin(j * 0.5) * 50 + Math.random() * 100,
        originalY: canvasHeight.value * 0.3 + Math.sin(j * 0.5) * 50
      })
    }
    
    flowingLines.value.push({
      points,
      opacity: 0.05 + Math.random() * 0.1,
      width: 1 + Math.random() * 2,
      speed: 0.01 + Math.random() * 0.02,
      offset: Math.random() * Math.PI * 2
    })
  }
}

// 绘制墨滴
const drawInkDrops = () => {
  if (!ctx.value) return
  
  inkDrops.value.forEach((drop, index) => {
    // 更新位置
    drop.x += Math.cos(drop.angle) * drop.speed
    drop.y += Math.sin(drop.angle) * drop.speed
    drop.life++
    
    // 边界检测
    if (drop.x < 0 || drop.x > canvasWidth.value || 
        drop.y < 0 || drop.y > canvasHeight.value || 
        drop.life > drop.maxLife) {
      // 重置墨滴
      drop.x = Math.random() * canvasWidth.value
      drop.y = Math.random() * canvasHeight.value
      drop.life = 0
      drop.angle = Math.random() * Math.PI * 2
    }
    
    // 鼠标交互
    const dx = props.mousePosition.x - drop.x
    const dy = props.mousePosition.y - drop.y
    const distance = Math.sqrt(dx * dx + dy * dy)
    
    if (distance < 100) {
      const force = (100 - distance) / 100
      drop.x += dx * force * 0.02
      drop.y += dy * force * 0.02
    }
    
    // 绘制墨滴
    ctx.value.save()
    ctx.value.globalAlpha = drop.opacity * (1 - drop.life / drop.maxLife)
    ctx.value.fillStyle = '#2c3e50'
    ctx.value.beginPath()
    ctx.value.arc(drop.x, drop.y, drop.size, 0, Math.PI * 2)
    ctx.value.fill()
    
    // 添加模糊效果
    const gradient = ctx.value.createRadialGradient(
      drop.x, drop.y, 0,
      drop.x, drop.y, drop.size * 2
    )
    gradient.addColorStop(0, `rgba(44, 62, 80, ${drop.opacity * 0.5})`)
    gradient.addColorStop(1, 'transparent')
    
    ctx.value.fillStyle = gradient
    ctx.value.fill()
    ctx.value.restore()
  })
}

// 绘制流动线条
const drawFlowingLines = () => {
  if (!ctx.value) return
  
  flowingLines.value.forEach(line => {
    ctx.value.save()
    ctx.value.globalAlpha = line.opacity
    ctx.value.strokeStyle = '#8c7853'
    ctx.value.lineWidth = line.width
    ctx.value.lineCap = 'round'
    
    // 更新点位置
    line.points.forEach((point, index) => {
      point.y = point.originalY + Math.sin(timeOffset.value * line.speed + index * 0.1 + line.offset) * 10
    })
    
    // 绘制贝塞尔曲线
    ctx.value.beginPath()
    ctx.value.moveTo(line.points[0].x, line.points[0].y)
    
    for (let i = 1; i < line.points.length - 2; i++) {
      const xc = (line.points[i].x + line.points[i + 1].x) / 2
      const yc = (line.points[i].y + line.points[i + 1].y) / 2
      ctx.value.quadraticCurveTo(line.points[i].x, line.points[i].y, xc, yc)
    }
    
    ctx.value.stroke()
    ctx.value.restore()
  })
}

// 动画循环
const animate = () => {
  if (!ctx.value) return
  
  // 清除画布
  ctx.value.clearRect(0, 0, canvasWidth.value, canvasHeight.value)
  
  // 更新时间
  timeOffset.value += 1
  
  // 绘制元素
  drawFlowingLines()
  drawInkDrops()
  
  animationId.value = requestAnimationFrame(animate)
}

// 窗口大小变化处理
const handleResize = () => {
  canvasWidth.value = window.innerWidth
  canvasHeight.value = window.innerHeight
  
  if (canvasRef.value) {
    canvasRef.value.width = canvasWidth.value
    canvasRef.value.height = canvasHeight.value
  }
  
  // 重新初始化
  nextTick(() => {
    initCloudLayers()
    initInkDrops()
    initFlowingLines()
  })
}

// 初始化画布
const initCanvas = () => {
  if (!canvasRef.value) return
  
  ctx.value = canvasRef.value.getContext('2d')
  canvasRef.value.width = canvasWidth.value
  canvasRef.value.height = canvasHeight.value
  
  // 开始动画
  animate()
  
  // 通知父组件背景就绪
  emit('background-ready')
}

// 监听页面加载状态
watch(() => props.pageLoaded, (newVal) => {
  if (newVal) {
    // 页面加载完成后激活更复杂的效果
    setTimeout(() => {
      // 可以添加更多动效
    }, 1000)
  }
})

// 生命周期
onMounted(() => {
  console.log('🎨 水墨背景初始化...')
  
  handleResize()
  window.addEventListener('resize', handleResize)
  
  nextTick(() => {
    initCanvas()
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (animationId.value) {
    cancelAnimationFrame(animationId.value)
  }
})
</script>

<style lang="scss" scoped>
.water-ink-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 1;
}

.ink-canvas {
  position: absolute;
  top: 0;
  left: 0;
  z-index: 2;
}

.static-layers {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 1;
}

.cloud-layer {
  position: absolute;
}

.gradient-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 3;
  pointer-events: none;
}

// 云朵动画
@keyframes cloud-drift-0 {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  25% { transform: translate(10px, -5px) rotate(1deg); }
  50% { transform: translate(-5px, 8px) rotate(-0.5deg); }
  75% { transform: translate(-8px, -3px) rotate(0.8deg); }
}

@keyframes cloud-drift-1 {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(8px, -3px) scale(1.02); }
  66% { transform: translate(-6px, 5px) scale(0.98); }
}

@keyframes cloud-drift-2 {
  0%, 100% { transform: translate(0, 0) rotate(0deg) scale(1); }
  20% { transform: translate(5px, -8px) rotate(0.5deg) scale(1.01); }
  40% { transform: translate(-3px, 6px) rotate(-0.3deg) scale(0.99); }
  60% { transform: translate(7px, -2px) rotate(0.7deg) scale(1.02); }
  80% { transform: translate(-5px, -4px) rotate(-0.4deg) scale(0.98); }
}
</style>