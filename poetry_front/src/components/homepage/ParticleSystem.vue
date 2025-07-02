<template>
  <div class="particle-system" :class="{ active }">
    <canvas 
      ref="canvasRef" 
      class="particle-canvas"
      :width="canvasWidth"
      :height="canvasHeight"
    ></canvas>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, watch } from 'vue'

// Props
const props = defineProps({
  active: {
    type: Boolean,
    default: false
  },
  mousePosition: {
    type: Object,
    default: () => ({ x: 0, y: 0 })
  }
})

// 响应式数据
const canvasRef = ref(null)
const canvasWidth = ref(window.innerWidth)
const canvasHeight = ref(window.innerHeight)
const ctx = ref(null)
const animationId = ref(null)

// 粒子数组
const particles = ref([])

// 粒子类
class Particle {
  constructor() {
    this.reset()
  }
  
  reset() {
    this.x = Math.random() * canvasWidth.value
    this.y = Math.random() * canvasHeight.value
    this.vx = (Math.random() - 0.5) * 0.5
    this.vy = (Math.random() - 0.5) * 0.5
    this.size = 1 + Math.random() * 3
    this.opacity = 0.1 + Math.random() * 0.3
    this.life = 0
    this.maxLife = 200 + Math.random() * 300
    this.color = this.getRandomColor()
  }
  
  getRandomColor() {
    const colors = [
      'rgba(44, 62, 80, 0.4)',
      'rgba(140, 120, 83, 0.3)',
      'rgba(110, 87, 115, 0.25)',
      'rgba(189, 195, 199, 0.2)'
    ]
    return colors[Math.floor(Math.random() * colors.length)]
  }
  
  update() {
    // 基础移动
    this.x += this.vx
    this.y += this.vy
    this.life++
    
    // 鼠标交互
    const dx = props.mousePosition.x - this.x
    const dy = props.mousePosition.y - this.y
    const distance = Math.sqrt(dx * dx + dy * dy)
    
    if (distance < 120) {
      const force = (120 - distance) / 120
      this.vx += dx * force * 0.001
      this.vy += dy * force * 0.001
    }
    
    // 边界处理
    if (this.x < 0 || this.x > canvasWidth.value || 
        this.y < 0 || this.y > canvasHeight.value || 
        this.life > this.maxLife) {
      this.reset()
    }
    
    // 速度衰减
    this.vx *= 0.99
    this.vy *= 0.99
  }
  
  draw() {
    if (!ctx.value) return
    
    ctx.value.save()
    
    // 根据生命周期调整透明度
    const lifeRatio = this.life / this.maxLife
    const currentOpacity = this.opacity * (1 - lifeRatio)
    
    ctx.value.globalAlpha = currentOpacity
    ctx.value.fillStyle = this.color
    
    ctx.value.beginPath()
    ctx.value.arc(this.x, this.y, this.size, 0, Math.PI * 2)
    ctx.value.fill()
    
    // 添加发光效果
    ctx.value.globalAlpha = currentOpacity * 0.5
    ctx.value.beginPath()
    ctx.value.arc(this.x, this.y, this.size * 2, 0, Math.PI * 2)
    ctx.value.fill()
    
    ctx.value.restore()
  }
}

// 初始化粒子
const initParticles = () => {
  particles.value = []
  const particleCount = Math.min(50, Math.floor((canvasWidth.value * canvasHeight.value) / 20000))
  
  for (let i = 0; i < particleCount; i++) {
    particles.value.push(new Particle())
  }
}

// 动画循环
const animate = () => {
  if (!ctx.value || !props.active) return
  
  // 清除画布
  ctx.value.clearRect(0, 0, canvasWidth.value, canvasHeight.value)
  
  // 更新和绘制粒子
  particles.value.forEach(particle => {
    particle.update()
    particle.draw()
  })
  
  // 绘制连接线
  drawConnections()
  
  animationId.value = requestAnimationFrame(animate)
}

// 绘制粒子连接线
const drawConnections = () => {
  if (!ctx.value) return
  
  for (let i = 0; i < particles.value.length; i++) {
    for (let j = i + 1; j < particles.value.length; j++) {
      const p1 = particles.value[i]
      const p2 = particles.value[j]
      
      const dx = p1.x - p2.x
      const dy = p1.y - p2.y
      const distance = Math.sqrt(dx * dx + dy * dy)
      
      if (distance < 100) {
        const opacity = (100 - distance) / 100 * 0.1
        
        ctx.value.save()
        ctx.value.globalAlpha = opacity
        ctx.value.strokeStyle = 'rgba(140, 120, 83, 0.5)'
        ctx.value.lineWidth = 0.5
        ctx.value.beginPath()
        ctx.value.moveTo(p1.x, p1.y)
        ctx.value.lineTo(p2.x, p2.y)
        ctx.value.stroke()
        ctx.value.restore()
      }
    }
  }
}

// 窗口大小变化处理
const handleResize = () => {
  canvasWidth.value = window.innerWidth
  canvasHeight.value = window.innerHeight
  
  if (canvasRef.value) {
    canvasRef.value.width = canvasWidth.value
    canvasRef.value.height = canvasHeight.value
  }
  
  initParticles()
}

// 初始化画布
const initCanvas = () => {
  if (!canvasRef.value) return
  
  ctx.value = canvasRef.value.getContext('2d')
  canvasRef.value.width = canvasWidth.value
  canvasRef.value.height = canvasHeight.value
  
  initParticles()
}

// 监听激活状态
watch(() => props.active, (newVal) => {
  if (newVal && !animationId.value) {
    animate()
  } else if (!newVal && animationId.value) {
    cancelAnimationFrame(animationId.value)
    animationId.value = null
  }
})

// 生命周期
onMounted(() => {
  console.log('✨ 粒子系统初始化...')
  
  handleResize()
  window.addEventListener('resize', handleResize)
  
  initCanvas()
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (animationId.value) {
    cancelAnimationFrame(animationId.value)
  }
})
</script>

<style lang="scss" scoped>
.particle-system {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 5;
  opacity: 0;
  transition: opacity 1s ease-in-out;
  
  &.active {
    opacity: 1;
  }
}

.particle-canvas {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
}
</style>