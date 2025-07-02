/**
 * 🎨 水墨渲染引擎
 * 提供高级水墨效果渲染和粒子系统
 * 支持WebGL加速和Canvas降级
 */

// ===== 🔧 引擎配置 =====

export const ENGINE_CONFIG = {
  // 渲染模式
  preferWebGL: true,
  fallbackToCanvas: true,
  
  // 性能配置
  maxParticles: 200,
  maxTrails: 50,
  updateFrequency: 60,
  
  // 水墨参数
  inkDensity: 0.8,
  flowSpeed: 1.2,
  viscosity: 0.6,
  diffusionRate: 0.3,
  
  // 颜色主题
  themes: {
    classic: {
      primary: [44, 62, 80],      // #2c3e50
      secondary: [140, 120, 83],   // #8c7853
      accent: [110, 87, 115]       // #6e5773
    },
    elegant: {
      primary: [52, 58, 64],       // #343a40
      secondary: [108, 117, 125],  // #6c757d
      accent: [189, 195, 199]      // #bdc3c7
    },
    nature: {
      primary: [39, 55, 70],       // #274356
      secondary: [95, 158, 160],   // #5f9ea0
      accent: [152, 193, 217]      // #98c1d9
    }
  }
}

// ===== 🌊 水墨粒子类 =====

export class InkParticle {
  constructor(x, y, theme = 'classic') {
    this.reset(x, y, theme)
    this.id = Math.random().toString(36).substr(2, 9)
  }
  
  reset(x = 0, y = 0, theme = 'classic') {
    this.x = x || Math.random() * window.innerWidth
    this.y = y || Math.random() * window.innerHeight
    this.vx = (Math.random() - 0.5) * 2
    this.vy = (Math.random() - 0.5) * 2
    this.size = 1 + Math.random() * 8
    this.opacity = 0.1 + Math.random() * 0.4
    this.life = 0
    this.maxLife = 120 + Math.random() * 240
    this.theme = theme
    this.color = this.generateColor()
    this.trail = []
    this.maxTrailLength = 5 + Math.random() * 10
    
    // 水墨特性
    this.viscosity = 0.3 + Math.random() * 0.4
    this.diffusion = 0.1 + Math.random() * 0.3
    this.absorption = 0.05 + Math.random() * 0.1
  }
  
  generateColor() {
    const colors = ENGINE_CONFIG.themes[this.theme]
    const colorArray = Object.values(colors)[Math.floor(Math.random() * 3)]
    const [r, g, b] = colorArray
    
    // 添加随机变化
    const variance = 20
    const newR = Math.max(0, Math.min(255, r + (Math.random() - 0.5) * variance))
    const newG = Math.max(0, Math.min(255, g + (Math.random() - 0.5) * variance))
    const newB = Math.max(0, Math.min(255, b + (Math.random() - 0.5) * variance))
    
    return `rgba(${newR}, ${newG}, ${newB}, ${this.opacity})`
  }
  
  update(mousePos = { x: 0, y: 0 }, canvasWidth, canvasHeight) {
    // 记录轨迹
    if (this.trail.length >= this.maxTrailLength) {
      this.trail.shift()
    }
    this.trail.push({ x: this.x, y: this.y, opacity: this.opacity })
    
    // 基础移动
    this.x += this.vx
    this.y += this.vy
    this.life++
    
    // 鼠标交互力场
    const dx = mousePos.x - this.x
    const dy = mousePos.y - this.y
    const distance = Math.sqrt(dx * dx + dy * dy)
    
    if (distance < 150) {
      const force = (150 - distance) / 150
      const angle = Math.atan2(dy, dx)
      
      // 吸引力
      this.vx += Math.cos(angle) * force * 0.02
      this.vy += Math.sin(angle) * force * 0.02
      
      // 增强扩散
      this.diffusion = Math.min(1, this.diffusion + force * 0.1)
    }
    
    // 重力效果（轻微）
    this.vy += 0.01
    
    // 阻力和粘滞
    this.vx *= (1 - this.viscosity * 0.1)
    this.vy *= (1 - this.viscosity * 0.1)
    
    // 边界处理 - 弹性碰撞
    if (this.x <= 0 || this.x >= canvasWidth) {
      this.vx *= -0.8
      this.x = Math.max(0, Math.min(canvasWidth, this.x))
    }
    if (this.y <= 0 || this.y >= canvasHeight) {
      this.vy *= -0.8
      this.y = Math.max(0, Math.min(canvasHeight, this.y))
    }
    
    // 生命周期处理
    if (this.life > this.maxLife) {
      return false // 粒子死亡
    }
    
    // 透明度衰减
    const lifeRatio = this.life / this.maxLife
    this.opacity = Math.max(0, this.opacity * (1 - lifeRatio * 0.005))
    
    return true // 粒子存活
  }
  
  draw(ctx) {
    if (!ctx || this.opacity <= 0) return
    
    ctx.save()
    
    // 绘制轨迹
    this.drawTrail(ctx)
    
    // 绘制主体
    this.drawBody(ctx)
    
    // 绘制扩散效果
    this.drawDiffusion(ctx)
    
    ctx.restore()
  }
  
  drawTrail(ctx) {
    if (this.trail.length < 2) return
    
    ctx.globalCompositeOperation = 'multiply'
    
    for (let i = 1; i < this.trail.length; i++) {
      const current = this.trail[i]
      const previous = this.trail[i - 1]
      const alpha = (i / this.trail.length) * 0.3
      
      ctx.globalAlpha = alpha
      ctx.strokeStyle = this.color
      ctx.lineWidth = this.size * 0.3
      ctx.lineCap = 'round'
      
      ctx.beginPath()
      ctx.moveTo(previous.x, previous.y)
      ctx.lineTo(current.x, current.y)
      ctx.stroke()
    }
    
    ctx.globalCompositeOperation = 'source-over'
  }
  
  drawBody(ctx) {
    const gradient = ctx.createRadialGradient(
      this.x, this.y, 0,
      this.x, this.y, this.size
    )
    
    gradient.addColorStop(0, this.color)
    gradient.addColorStop(0.7, this.color.replace(/[\d.]+\)$/, '0.1)'))
    gradient.addColorStop(1, 'transparent')
    
    ctx.globalAlpha = this.opacity
    ctx.fillStyle = gradient
    
    ctx.beginPath()
    ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2)
    ctx.fill()
  }
  
  drawDiffusion(ctx) {
    if (this.diffusion <= 0.2) return
    
    const diffusionRadius = this.size * (1 + this.diffusion * 2)
    const diffusionGradient = ctx.createRadialGradient(
      this.x, this.y, this.size,
      this.x, this.y, diffusionRadius
    )
    
    const baseColor = this.color.match(/\d+/g)
    if (baseColor && baseColor.length >= 3) {
      const [r, g, b] = baseColor
      diffusionGradient.addColorStop(0, `rgba(${r}, ${g}, ${b}, 0)`)
      diffusionGradient.addColorStop(0.5, `rgba(${r}, ${g}, ${b}, ${this.opacity * 0.1})`)
      diffusionGradient.addColorStop(1, 'transparent')
    }
    
    ctx.globalAlpha = this.opacity * 0.5
    ctx.fillStyle = diffusionGradient
    
    ctx.beginPath()
    ctx.arc(this.x, this.y, diffusionRadius, 0, Math.PI * 2)
    ctx.fill()
  }
}

// ===== 🌀 流体效果类 =====

export class FluidField {
  constructor(width, height, resolution = 32) {
    this.width = width
    this.height = height
    this.resolution = resolution
    this.cols = Math.floor(width / resolution)
    this.rows = Math.floor(height / resolution)
    
    // 创建向量场
    this.field = []
    this.initField()
  }
  
  initField() {
    for (let y = 0; y < this.rows; y++) {
      this.field[y] = []
      for (let x = 0; x < this.cols; x++) {
        this.field[y][x] = {
          vx: (Math.random() - 0.5) * 0.1,
          vy: (Math.random() - 0.5) * 0.1,
          pressure: 0,
          density: 0
        }
      }
    }
  }
  
  update(mousePos, mousePressed = false) {
    // 鼠标影响
    if (mousePressed && mousePos) {
      const gridX = Math.floor(mousePos.x / this.resolution)
      const gridY = Math.floor(mousePos.y / this.resolution)
      
      if (gridX >= 0 && gridX < this.cols && gridY >= 0 && gridY < this.rows) {
        this.addForce(gridX, gridY, 0.5, 0.3)
      }
    }
    
    // 流体动力学计算（简化版）
    this.diffuse()
    this.advect()
    this.project()
  }
  
  addForce(x, y, vx, vy, radius = 3) {
    for (let dy = -radius; dy <= radius; dy++) {
      for (let dx = -radius; dx <= radius; dx++) {
        const nx = x + dx
        const ny = y + dy
        
        if (nx >= 0 && nx < this.cols && ny >= 0 && ny < this.rows) {
          const distance = Math.sqrt(dx * dx + dy * dy)
          if (distance <= radius) {
            const force = 1 - (distance / radius)
            this.field[ny][nx].vx += vx * force
            this.field[ny][nx].vy += vy * force
            this.field[ny][nx].density += force * 0.5
          }
        }
      }
    }
  }
  
  diffuse() {
    const diffusion = 0.01
    const newField = JSON.parse(JSON.stringify(this.field))
    
    for (let y = 1; y < this.rows - 1; y++) {
      for (let x = 1; x < this.cols - 1; x++) {
        const neighbors = [
          this.field[y-1][x], this.field[y+1][x],
          this.field[y][x-1], this.field[y][x+1]
        ]
        
        let avgVx = 0, avgVy = 0, avgDensity = 0
        neighbors.forEach(n => {
          avgVx += n.vx
          avgVy += n.vy
          avgDensity += n.density
        })
        
        avgVx /= 4
        avgVy /= 4
        avgDensity /= 4
        
        newField[y][x].vx += (avgVx - this.field[y][x].vx) * diffusion
        newField[y][x].vy += (avgVy - this.field[y][x].vy) * diffusion
        newField[y][x].density += (avgDensity - this.field[y][x].density) * diffusion
      }
    }
    
    this.field = newField
  }
  
  advect() {
    // 平流步骤（简化）
    for (let y = 0; y < this.rows; y++) {
      for (let x = 0; x < this.cols; x++) {
        this.field[y][x].vx *= 0.99
        this.field[y][x].vy *= 0.99
        this.field[y][x].density *= 0.995
      }
    }
  }
  
  project() {
    // 无散度投影（简化）
    const divergence = 0.1
    for (let y = 1; y < this.rows - 1; y++) {
      for (let x = 1; x < this.cols - 1; x++) {
        const div = (
          this.field[y][x+1].vx - this.field[y][x-1].vx +
          this.field[y+1][x].vy - this.field[y-1][x].vy
        ) * 0.5
        
        this.field[y][x].pressure = -div * divergence
      }
    }
  }
  
  getVelocityAt(x, y) {
    const gridX = Math.max(0, Math.min(this.cols - 1, Math.floor(x / this.resolution)))
    const gridY = Math.max(0, Math.min(this.rows - 1, Math.floor(y / this.resolution)))
    
    return this.field[gridY][gridX]
  }
  
  draw(ctx) {
    ctx.save()
    ctx.strokeStyle = 'rgba(140, 120, 83, 0.1)'
    ctx.lineWidth = 1
    
    for (let y = 0; y < this.rows; y++) {
      for (let x = 0; x < this.cols; x++) {
        const cell = this.field[y][x]
        const centerX = x * this.resolution + this.resolution / 2
        const centerY = y * this.resolution + this.resolution / 2
        
        if (Math.abs(cell.vx) > 0.01 || Math.abs(cell.vy) > 0.01) {
          const endX = centerX + cell.vx * this.resolution * 10
          const endY = centerY + cell.vy * this.resolution * 10
          
          ctx.beginPath()
          ctx.moveTo(centerX, centerY)
          ctx.lineTo(endX, endY)
          ctx.stroke()
        }
      }
    }
    
    ctx.restore()
  }
}

// ===== 🎨 水墨渲染引擎主类 =====

export class WaterInkEngine {
  constructor(canvas, options = {}) {
    this.canvas = canvas
    this.ctx = canvas.getContext('2d')
    this.options = { ...ENGINE_CONFIG, ...options }
    
    this.particles = []
    this.fluidField = null
    this.mousePosition = { x: 0, y: 0 }
    this.mousePressed = false
    this.animationId = null
    this.lastTime = 0
    
    this.init()
  }
  
  init() {
    // 创建流体场
    this.fluidField = new FluidField(
      this.canvas.width, 
      this.canvas.height, 
      32
    )
    
    // 初始化粒子
    this.initParticles()
    
    // 绑定事件
    this.bindEvents()
    
    console.log('🎨 水墨引擎初始化完成')
  }
  
  initParticles() {
    const particleCount = Math.min(
      this.options.maxParticles,
      Math.floor((this.canvas.width * this.canvas.height) / 8000)
    )
    
    for (let i = 0; i < particleCount; i++) {
      this.particles.push(new InkParticle(
        Math.random() * this.canvas.width,
        Math.random() * this.canvas.height,
        'classic'
      ))
    }
  }
  
  bindEvents() {
    this.canvas.addEventListener('mousemove', (e) => {
      const rect = this.canvas.getBoundingClientRect()
      this.mousePosition.x = e.clientX - rect.left
      this.mousePosition.y = e.clientY - rect.top
    })
    
    this.canvas.addEventListener('mousedown', () => {
      this.mousePressed = true
    })
    
    this.canvas.addEventListener('mouseup', () => {
      this.mousePressed = false
    })
  }
  
  addInkDrop(x, y, intensity = 1) {
    const dropCount = Math.floor(intensity * 5)
    
    for (let i = 0; i < dropCount; i++) {
      const angle = (Math.PI * 2 / dropCount) * i
      const distance = Math.random() * 20
      const dropX = x + Math.cos(angle) * distance
      const dropY = y + Math.sin(angle) * distance
      
      const particle = new InkParticle(dropX, dropY, 'classic')
      particle.size *= intensity
      particle.opacity *= intensity
      particle.vx += (Math.random() - 0.5) * intensity
      particle.vy += (Math.random() - 0.5) * intensity
      
      this.particles.push(particle)
    }
    
    // 限制粒子数量
    while (this.particles.length > this.options.maxParticles) {
      this.particles.shift()
    }
  }
  
  update(deltaTime) {
    // 更新流体场
    this.fluidField.update(this.mousePosition, this.mousePressed)
    
    // 更新粒子
    this.particles = this.particles.filter(particle => {
      const velocity = this.fluidField.getVelocityAt(particle.x, particle.y)
      
      // 流体场影响
      particle.vx += velocity.vx * 0.1
      particle.vy += velocity.vy * 0.1
      
      return particle.update(
        this.mousePosition, 
        this.canvas.width, 
        this.canvas.height
      )
    })
    
    // 随机生成新粒子
    if (this.particles.length < this.options.maxParticles * 0.8 && Math.random() < 0.1) {
      this.particles.push(new InkParticle(
        Math.random() * this.canvas.width,
        Math.random() * this.canvas.height,
        'classic'
      ))
    }
  }
  
  render() {
    // 清除画布（带透明度，产生拖尾效果）
    this.ctx.save()
    this.ctx.globalAlpha = 0.1
    this.ctx.fillStyle = '#f8f9fa'
    this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height)
    this.ctx.restore()
    
    // 绘制流体场（可选）
    if (this.options.showFluidField) {
      this.fluidField.draw(this.ctx)
    }
    
    // 绘制粒子
    this.particles.forEach(particle => {
      particle.draw(this.ctx)
    })
    
    // 绘制连接线
    this.drawConnections()
  }
  
  drawConnections() {
    const maxDistance = 80
    
    for (let i = 0; i < this.particles.length; i++) {
      for (let j = i + 1; j < this.particles.length; j++) {
        const p1 = this.particles[i]
        const p2 = this.particles[j]
        
        const dx = p1.x - p2.x
        const dy = p1.y - p2.y
        const distance = Math.sqrt(dx * dx + dy * dy)
        
        if (distance < maxDistance) {
          const opacity = (maxDistance - distance) / maxDistance * 0.1
          
          this.ctx.save()
          this.ctx.globalAlpha = opacity
          this.ctx.strokeStyle = 'rgba(140, 120, 83, 0.8)'
          this.ctx.lineWidth = 0.5
          this.ctx.beginPath()
          this.ctx.moveTo(p1.x, p1.y)
          this.ctx.lineTo(p2.x, p2.y)
          this.ctx.stroke()
          this.ctx.restore()
        }
      }
    }
  }
  
  start() {
    const animate = (currentTime) => {
      const deltaTime = currentTime - this.lastTime
      this.lastTime = currentTime
      
      this.update(deltaTime)
      this.render()
      
      this.animationId = requestAnimationFrame(animate)
    }
    
    this.animationId = requestAnimationFrame(animate)
  }
  
  stop() {
    if (this.animationId) {
      cancelAnimationFrame(this.animationId)
      this.animationId = null
    }
  }
  
  resize(width, height) {
    this.canvas.width = width
    this.canvas.height = height
    
    this.fluidField = new FluidField(width, height, 32)
  }
  
  destroy() {
    this.stop()
    this.particles = []
    this.fluidField = null
  }
}

// ===== 📊 性能监控工具 =====

export class PerformanceMonitor {
  constructor() {
    this.fps = 0
    this.frameCount = 0
    this.lastTime = performance.now()
    this.samples = []
    this.maxSamples = 60
  }
  
  update() {
    this.frameCount++
    const currentTime = performance.now()
    
    if (currentTime - this.lastTime >= 1000) {
      this.fps = this.frameCount
      this.frameCount = 0
      this.lastTime = currentTime
      
      this.samples.push(this.fps)
      if (this.samples.length > this.maxSamples) {
        this.samples.shift()
      }
    }
  }
  
  getAverageFPS() {
    return this.samples.length > 0 
      ? this.samples.reduce((a, b) => a + b) / this.samples.length 
      : 0
  }
  
  getPerformanceLevel() {
    const avgFPS = this.getAverageFPS()
    if (avgFPS >= 55) return 'high'
    if (avgFPS >= 30) return 'medium'
    return 'low'
  }
}

// ===== 🔧 工具函数 =====

export const WaterInkUtils = {
  // 颜色混合
  blendColors(color1, color2, ratio) {
    const [r1, g1, b1, a1] = color1
    const [r2, g2, b2, a2] = color2
    
    return [
      Math.floor(r1 * (1 - ratio) + r2 * ratio),
      Math.floor(g1 * (1 - ratio) + g2 * ratio),
      Math.floor(b1 * (1 - ratio) + b2 * ratio),
      a1 * (1 - ratio) + a2 * ratio
    ]
  },
  
  // 噪声函数
  noise(x, y, z = 0) {
    // 简化的Perlin噪声
    return (Math.sin(x * 0.01) + Math.cos(y * 0.01) + Math.sin(z * 0.01)) / 3
  },
  
  // 距离计算
  distance(x1, y1, x2, y2) {
    return Math.sqrt((x2 - x1) ** 2 + (y2 - y1) ** 2)
  },
  
  // 角度计算
  angle(x1, y1, x2, y2) {
    return Math.atan2(y2 - y1, x2 - x1)
  },
  
  // 线性插值
  lerp(start, end, factor) {
    return start + (end - start) * factor
  }
}

// 默认导出
export default {
  WaterInkEngine,
  InkParticle,
  FluidField,
  PerformanceMonitor,
  WaterInkUtils,
  ENGINE_CONFIG
}