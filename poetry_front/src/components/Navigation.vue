<template>
  <div class="mt-navigation">
    <!-- 悬浮球 -->
    <div
      class="floating-ball"
      :class="{ 
        'active': isExpanded,
        'hidden-left': isHiddenLeft,
        'hidden-right': isHiddenRight,
        'transparent': isTransparent
      }"
      :style="ballStyle"
      @mousedown="startDrag"
      @click.stop="handleBallClick"
      ref="ball"
    >
      <!-- 水墨云雾背景 -->
      <div class="ink-cloud-bg">
        <div class="ink-layer ink-1"></div>
        <div class="ink-layer ink-2"></div>
        <div class="ink-layer ink-3"></div>
      </div>
      
      <!-- 古典云朵图标 -->
      <div class="ball-icon">
        <svg viewBox="0 0 24 24" width="24" height="24">
          <path d="M19.35 10.04A7.49 7.49 0 0 0 12 4C9.11 4 6.6 5.64 5.35 8.04A5.994 5.994 0 0 0 0 14c0 3.31 2.69 6 6 6h13c2.76 0 5-2.24 5-5 0-2.64-2.05-4.78-4.65-4.96z" fill="currentColor"/>
        </svg>
      </div>
    </div>

    <!-- 导航面板 -->
    <transition name="scroll-unfold">
      <div
        class="navbar"
        v-show="isExpanded"
        ref="navbar"
        :style="navStyle"
        @click.stop="handleNavbarClick"
        @mouseenter="handleNavbarMouseEnter"
        @mouseleave="handleNavbarMouseLeave"
        @mousemove="handleNavbarMouseMove"
      >
        <!-- 宣纸背景纹理 -->
        <div class="paper-texture">
          <div class="paper-grain"></div>
          <div class="paper-shadow"></div>
        </div>
        
        <!-- 水墨渲染背景 -->
        <div class="ink-wash-bg">
          <div class="wash-layer wash-1"></div>
          <div class="wash-layer wash-2"></div>
          <div class="wash-layer wash-3"></div>
        </div>
        
    
        <!-- 跟随鼠标的墨点 -->
        <div class="navbar-ink-dots">
          <div 
            v-for="(dot, index) in navbarInkDots" 
            :key="`navbar-dot-${index}`"
            class="navbar-ink-dot"
            :class="{ 'clicked': dot.clicked }"
            :style="getNavbarDotStyle(dot, index)"
          ></div>
        </div>
        
        <!-- 点击墨晕效果 -->
        <div class="navbar-ink-splash-container">
          <div 
            v-for="(splash, index) in navbarInkSplashes" 
            :key="`navbar-splash-${index}`"
            class="navbar-ink-splash"
            :style="getNavbarSplashStyle(splash)"
          ></div>
        </div>
        
        <div class="nav-container">
          <div class="nav-header">
            <h2>云舟诗渡</h2>
            <div class="classical-icon">
              <div class="cloud-seal">
               <img src="/logo1.png" alt="logo" width="28" height="28" style="display:block;" />
              </div>
            </div>
          </div>
          <ul>
            <li v-for="(item, index) in navItems" :key="index" :style="{ '--delay': index * 0.06 + 's' }">
              <router-link 
                :to="item.path" 
                class="nav-item"
                @click.native="handleNavItemClick"
                @mouseenter.native="handleNavItemHover"
                @mouseleave.native="handleNavItemLeave"
              >
                <span class="link-icon">
                  <svg viewBox="0 0 24 24" width="18" height="18">
                    <path :d="item.icon" fill="currentColor"/>
                  </svg>
                </span>
                <span class="link-text">{{ item.text }}</span>
                <div class="ink-ripple"></div>
                <!-- 悬浮装饰效果 -->
                <div class="hover-decoration">
                  <div class="decoration-dot dot-1"></div>
                  <div class="decoration-dot dot-2"></div>
                  <div class="decoration-dot dot-3"></div>
                </div>
                <!-- 点击波纹效果 -->
                <div class="click-ripple"></div>
              </router-link>
            </li>
          </ul>
        </div>
      </div>
    </transition>

    <!-- 遮罩层 -->
    <transition name="fade">
      <div
        class="overlay"
        v-show="isExpanded"
        @click="hideNav"
      ></div>
    </transition>
  </div>
</template>

<script>
export default {
  name: 'Navigation',
  data() {
    return {
      navItems: [
        { text: '诗词推荐',path: '/recommend' ,icon:"M5 4h14v2H5V4m0 4h14v2H5V8m0 4h9v2H5v-2m0 4h6v2H5v-2z"},
        { text: '诗词搜索', path: '/search' ,icon:"M9.5 3a6.5 6.5 0 0 1 5.18 10.5l4.66 4.66-1.42 1.42-4.66-4.66A6.5 6.5 0 1 1 9.5 3m0 2a4.5 4.5 0 1 0 0 9a4.5 4.5 0 0 0 0-9z"},
        { 
          text: '诗词游戏', 
          path: '/game-center', 
          icon: "M12 2a2 2 0 0 1 2 2c0 .5-.2.96-.5 1.31a4.002 4.002 0 0 1 4.88 4.88c.35-.3.81-.5 1.31-.5a2 2 0 0 1 0 4c-.5 0-.96-.2-1.31-.5a4.002 4.002 0 0 1-4.88 4.88c.3.35.5.81.5 1.31a2 2 0 0 1-4 0c0-.5.2-.96.5-1.31a4.002 4.002 0 0 1-4.88-4.88c-.35.3-.81.5-1.31.5a2 2 0 0 1 0-4c.5 0 .96.2 1.31.5a4.002 4.002 0 0 1 4.88-4.88A1.99 1.99 0 0 1 12 2z" 
        },
        { text: '交流论坛', path: '/forum',icon:"M4 4h16a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2h-4l-4 4-4-4H4a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z"},
        { text: 'AI智能诗友', path: '/qwenllm', icon: "M9 9h6v6H9V9zm-4 3h2v2H5v-2zm12 0h2v2h-2v-2zm-6-6h2v2h-2V5zm0 12h2v2h-2v-2z" }, 
        { text: '个人中心', path: '/userinfo', icon: "M12 2a5 5 0 1 0 5 5 5 5 0 0 0-5-5zm0 8a3 3 0 1 1 3-3 3 3 0 0 1-3 3zm9 11v-1a7 7 0 0 0-7-7h-4a7 7 0 0 0-7 7v1h2v-1a5 5 0 0 1 5-5h4a5 5 0 0 1 5 5v1z"},
      ],
      isExpanded: false,
      isDragging: false,
      isHiddenLeft: false,
      isHiddenRight: false,
      isTransparent: false,
      ballPosition: { x: 20, y: 20 },
      dragOffset: { x: 0, y: 0 },
      windowSize: { width: 0, height: 0 },
      hideTimeout: null,
      
      // 导航面板墨点相关数据
      navbarInkDots: [],
      navbarInkSplashes: [],
      navbarMousePosition: { x: 0, y: 0 },
      isMouseInNavbar: false,
      navbarSplashCounter: 0
    }
  },
  computed: {
    ballStyle() {
      return {
        left: typeof this.ballPosition.x === 'string' ? this.ballPosition.x : `${this.ballPosition.x}px`,
        top: typeof this.ballPosition.y === 'string' ? this.ballPosition.y : `${this.ballPosition.y}px`,
        transform: this.isExpanded ? 'scale(1.1)' : 'scale(1)'
      }
    },
    navStyle() {
      const offset = this.isHiddenLeft ? 10 : this.isHiddenRight ? -10 : 0
      return {
        left: `${(typeof this.ballPosition.x === 'number' ? this.ballPosition.x : 0) + offset}px`,
        top: typeof this.ballPosition.y === 'string' ? this.ballPosition.y : `${this.ballPosition.y}px`
      }
    }
  },
  mounted() {
    this.windowSize.width = window.innerWidth
    this.windowSize.height = window.innerHeight
    this.initNavbarInkDots()
    window.addEventListener('resize', this.handleResize)
    document.addEventListener('mousemove', this.handleDrag)
    document.addEventListener('mouseup', this.stopDrag)
    document.addEventListener('click', this.handleClickOutside)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    document.removeEventListener('mousemove', this.handleDrag)
    document.removeEventListener('mouseup', this.stopDrag)
    document.removeEventListener('click', this.handleClickOutside)
    if (this.hideTimeout) clearTimeout(this.hideTimeout)
  },
  methods: {
    // 导航项悬浮事件
    handleNavItemHover(e) {
      const item = e.currentTarget
      // 轻微放大效果
      item.style.transform = 'translateX(4px) scale(1.02)'
      
      // 激活装饰点动画
      const decorationDots = item.querySelectorAll('.decoration-dot')
      decorationDots.forEach((dot, index) => {
        setTimeout(() => {
          dot.style.opacity = '0.6'
          dot.style.transform = 'scale(1.2)'
        }, index * 50)
      })
    },
    
    // 导航项离开事件
    handleNavItemLeave(e) {
      const item = e.currentTarget
      // 恢复原始状态
      item.style.transform = 'translateX(0) scale(1)'
      
      // 隐藏装饰点
      const decorationDots = item.querySelectorAll('.decoration-dot')
      decorationDots.forEach(dot => {
        dot.style.opacity = '0'
        dot.style.transform = 'scale(1)'
      })
    },
    
    // 导航项点击事件
    handleNavItemClick(e) {
      const item = e.currentTarget
      const clickRipple = item.querySelector('.click-ripple')
      
      // 点击缩放效果
      item.style.transform = 'translateX(4px) scale(0.98)'
      setTimeout(() => {
        item.style.transform = 'translateX(4px) scale(1.02)'
      }, 100)
      
      // 激活点击波纹
      clickRipple.style.opacity = '1'
      clickRipple.style.transform = 'scale(1)'
      
      setTimeout(() => {
        clickRipple.style.opacity = '0'
        clickRipple.style.transform = 'scale(0)'
      }, 400)
      
      // 延迟隐藏导航
      setTimeout(() => {
        this.hideNav()
      }, 150)
    },
    
    // 初始化导航面板墨点
    initNavbarInkDots() {
      this.navbarInkDots = Array.from({ length: 8 }, (_, index) => ({
        id: index,
        x: Math.random() * 80 + 10, // 相对于导航面板的位置（百分比）
        y: Math.random() * 80 + 10,
        targetX: Math.random() * 80 + 10,
        targetY: Math.random() * 80 + 10,
        size: Math.random() * 4 + 3,
        opacity: Math.random() * 0.4 + 0.2,
        speed: Math.random() * 0.04 + 0.02,
        clicked: false
      }))
    },
    
    // 鼠标进入导航面板
    handleNavbarMouseEnter(e) {
      this.isMouseInNavbar = true
      this.updateNavbarMousePosition(e)
    },
    
    // 鼠标离开导航面板
    handleNavbarMouseLeave() {
      this.isMouseInNavbar = false
      // 墨点回到随机位置
      this.navbarInkDots.forEach(dot => {
        dot.targetX = Math.random() * 80 + 10
        dot.targetY = Math.random() * 80 + 10
      })
    },
    
    // 鼠标在导航面板内移动
    handleNavbarMouseMove(e) {
      if (this.isMouseInNavbar && !this.isDragging) {
        this.updateNavbarMousePosition(e)
        this.updateNavbarInkDotsTarget()
      }
    },
    
    // 更新导航面板鼠标位置
    updateNavbarMousePosition(e) {
      if (!this.$refs.navbar) return
      const rect = this.$refs.navbar.getBoundingClientRect()
      
      // 转换为相对于导航面板的百分比坐标
      this.navbarMousePosition.x = ((e.clientX - rect.left) / rect.width) * 100
      this.navbarMousePosition.y = ((e.clientY - rect.top) / rect.height) * 100
      
      // 限制在合理范围内
      this.navbarMousePosition.x = Math.max(0, Math.min(100, this.navbarMousePosition.x))
      this.navbarMousePosition.y = Math.max(0, Math.min(100, this.navbarMousePosition.y))
    },
    
    // 更新导航面板墨点目标位置
    updateNavbarInkDotsTarget() {
      this.navbarInkDots.forEach((dot, index) => {
        // 围绕鼠标位置的圆形分布，带有轻微的随机偏移
        const angle = (index / this.navbarInkDots.length) * Math.PI * 2 + Date.now() * 0.0008
        const baseRadius = 12 + Math.sin(Date.now() * 0.001 + index) * 4
        const randomOffset = Math.sin(Date.now() * 0.0015 + index * 2) * 8
        
        dot.targetX = this.navbarMousePosition.x + Math.cos(angle) * (baseRadius + randomOffset)
        dot.targetY = this.navbarMousePosition.y + Math.sin(angle) * (baseRadius + randomOffset)
        
        // 限制在导航面板范围内
        dot.targetX = Math.max(5, Math.min(95, dot.targetX))
        dot.targetY = Math.max(5, Math.min(95, dot.targetY))
      })
    },
    
    // 更新导航面板墨点位置（平滑移动）
    updateNavbarInkDotsPosition() {
      this.navbarInkDots.forEach(dot => {
        const dx = dot.targetX - dot.x
        const dy = dot.targetY - dot.y
        
        dot.x += dx * dot.speed
        dot.y += dy * dot.speed
      })
      
      // 清理过期的墨晕效果
      this.navbarInkSplashes = this.navbarInkSplashes.filter(splash => 
        Date.now() - splash.createTime < 1500
      )
      
      requestAnimationFrame(this.updateNavbarInkDotsPosition)
    },
    
    // 获取导航面板墨点样式
    getNavbarDotStyle(dot, index) {
      return {
        left: `${dot.x}%`,
        top: `${dot.y}%`,
        width: `${dot.size}px`,
        height: `${dot.size}px`,
        opacity: dot.opacity,
        '--delay': `${index * 0.15}s`,
        zIndex: 10 + index
      }
    },
    
    // 导航面板点击事件
    handleNavbarClick(e) {
      // 阻止事件冒泡到hideNav
      e.stopPropagation()
      
      // 创建墨晕效果
      if (this.isMouseInNavbar) {
        this.createNavbarInkSplash(this.navbarMousePosition.x, this.navbarMousePosition.y)
      }
    },
    
    // 创建导航面板墨晕效果
    createNavbarInkSplash(x, y) {
      const splash = {
        id: this.navbarSplashCounter++,
        x: x,
        y: y,
        createTime: Date.now(),
        size: Math.random() * 25 + 20
      }
      
      this.navbarInkSplashes.push(splash)
      
      // 点击时墨点扩散
      this.navbarInkDots.forEach((dot, index) => {
        dot.clicked = true
        const angle = Math.random() * Math.PI * 2
        const distance = Math.random() * 40 + 30
        
        dot.targetX = x + Math.cos(angle) * distance
        dot.targetY = y + Math.sin(angle) * distance
        dot.targetX = Math.max(5, Math.min(95, dot.targetX))
        dot.targetY = Math.max(5, Math.min(95, dot.targetY))
        
        // 0.8秒后恢复正常状态
        setTimeout(() => {
          if (dot.clicked) {
            dot.clicked = false
            if (!this.isMouseInNavbar) {
              dot.targetX = Math.random() * 80 + 10
              dot.targetY = Math.random() * 80 + 10
            }
          }
        }, 800)
      })
    },
    
    // 获取导航面板墨晕样式
    getNavbarSplashStyle(splash) {
      const age = Date.now() - splash.createTime
      const progress = age / 1500 // 1.5秒动画
      const scale = 1 + progress * 3
      const opacity = Math.max(0, 0.5 - progress * 0.5)
      
      return {
        left: `${splash.x}%`,
        top: `${splash.y}%`,
        width: `${splash.size}px`,
        height: `${splash.size}px`,
        transform: `translate(-50%, -50%) scale(${scale})`,
        opacity: opacity
      }
    },

    handleBallClick(e) {
      if (!this.isDragging) {
        if (this.isHiddenLeft || this.isHiddenRight) {
          this.unhideBall()
        } else {
          this.isExpanded = !this.isExpanded
        }
      }
    },
    hideNav() {
      this.isExpanded = false
    },
    startDrag(e) {
      this.isDragging = true
      const rect = this.$refs.ball.getBoundingClientRect()
      this.dragOffset = {
        x: e.clientX - rect.left,
        y: e.clientY - rect.top
      }
      if (this.isHiddenLeft || this.isHiddenRight) {
        this.unhideBall()
      }
    },
    handleDrag(e) {
      if (this.isDragging) {
        let x = e.clientX - this.dragOffset.x
        let y = e.clientY - this.dragOffset.y
        
        x = Math.max(-30, Math.min(x, this.windowSize.width - 20))
        y = Math.max(0, Math.min(y, this.windowSize.height - 50))
        
        this.ballPosition = { x, y }
      }
    },
    stopDrag() {
      if (this.isDragging) {
        this.isDragging = false
        this.checkEdgeHide()
      }
    },
    checkEdgeHide() {
      const { x } = this.ballPosition
      const { width } = this.windowSize
      
      if (x < -15) {
        this.hideLeft()
      } 
      else if (x > width - 35) {
        this.hideRight()
      } else {
        this.unhideBall()
      }
    },
    hideLeft() {
      this.isHiddenLeft = true
      this.isHiddenRight = false
      this.ballPosition.x = '-25px'
      this.delayedTransparency()
    },
    hideRight() {
      this.isHiddenRight = true
      this.isHiddenLeft = false
      this.ballPosition.x = 'calc(100% - 25px)'
      this.delayedTransparency()
    },
    unhideBall() {
      this.isHiddenLeft = false
      this.isHiddenRight = false
      this.isTransparent = false
      this.ballPosition.x = Math.max(10, Math.min(this.ballPosition.x, this.windowSize.width - 50))
    },
    delayedTransparency() {
      if (this.hideTimeout) clearTimeout(this.hideTimeout)
      this.isTransparent = false
      this.hideTimeout = setTimeout(() => {
        this.isTransparent = true
      }, 1000)
    },
    handleResize() {
      this.windowSize.width = window.innerWidth
      this.windowSize.height = window.innerHeight
      this.checkEdgeHide()
    },
    handleClickOutside(event) {
      if (this.isExpanded && this.$refs.navbar && !this.$refs.navbar.contains(event.target)) {
        this.hideNav()
      }
    },
  },
  
  created() {
    // 启动导航面板墨点位置更新循环
    this.$nextTick(() => {
      this.updateNavbarInkDotsPosition()
    })
  }
}
</script>

<style scoped>
.mt-navigation {
  position: fixed;
  top: 0;
  left: 0;
  width: 0;
  height: 0;
  z-index: 9999;
  pointer-events: none; /* 不影响页面其它内容的点击 */
}

.floating-ball,
.navbar,
.overlay {
  pointer-events: auto;
}
/* 导航项增强交互效果 */
.navbar ul li {
  position: relative;
  overflow: hidden;
  border-radius: 8px;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  background: rgba(255, 255, 255, 0.4);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(139, 149, 137, 0.2);
  animation: scrollItemSlide 0.4s ease-out forwards;
  animation-delay: var(--delay);
  opacity: 0;
  transform: translateX(-40px);
}

@keyframes scrollItemSlide {
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.navbar ul li:hover {
  background: rgba(255, 255, 255, 0.7);
  border-color: rgba(139, 149, 137, 0.4);
  box-shadow: 
    0 6px 20px rgba(139, 149, 137, 0.25),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
}

/* 悬浮装饰效果 */
.hover-decoration {
  position: absolute;
  top: 50%;
  right: 15px;
  transform: translateY(-50%);
  display: flex;
  gap: 3px;
  pointer-events: none;
}

.decoration-dot {
  width: 3px;
  height: 3px;
  background: rgba(139, 149, 137, 0.6);
  border-radius: 50%;
  opacity: 0;
  transform: scale(1);
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.decoration-dot.dot-1 {
  background: rgba(152, 162, 149, 0.6);
}

.decoration-dot.dot-2 {
  background: rgba(139, 149, 137, 0.7);
}

.decoration-dot.dot-3 {
  width: 2px;
  height: 2px;
  background: rgba(105, 117, 102, 0.5);
}

/* 点击波纹效果 */
.click-ripple {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 120%;
  height: 120%;
  background: radial-gradient(circle, 
    rgba(139, 149, 137, 0.2) 0%, 
    rgba(105, 117, 102, 0.1) 30%,
    transparent 70%);
  border-radius: 50%;
  transform: translate(-50%, -50%) scale(0);
  opacity: 0;
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  pointer-events: none;
}

/* 导航链接样式增强 */
.nav-item {
  color: rgba(76, 86, 73, 0.9);
  text-decoration: none;
  font-size: 16px;
  font-weight: 500;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  position: relative;
  z-index: 2;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  font-family: '楷体', 'KaiTi', serif;
  transform-origin: left center;
 
}

.nav-item:hover {
  color: rgba(76, 86, 73, 1);
  text-shadow: 0 1px 3px rgba(139, 149, 137, 0.314);
  padding-left: 24px;
  text-decoration: none;
}

/* 图标悬浮效果增强 */
.link-icon {
  margin-right: 16px;
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(139, 149, 137, 0.8);
  transition: all 0.3s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  transform-origin: center;
}

.nav-item:hover .link-icon {
  color: rgba(139, 149, 137, 1);
  transform: scale(1.15) rotate(5deg);
  filter: drop-shadow(0 2px 4px rgba(139, 149, 137, 0.4));
}

/* 文字悬浮效果 */
.link-text {
  flex-grow: 1;
  font-weight: 500;
  transition: all 0.3s ease;
  position: relative;
}

.nav-item:hover .link-text {
  font-weight: 520;
  letter-spacing: 2px;
}

.nav-item:hover .link-text::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 30%;
  height: 1px;
  background: linear-gradient(to right, rgba(95, 120, 90, 0.6), transparent);
  animation: underlineExpand 0.3s ease-out forwards;
}

@keyframes underlineExpand {
  from {
    width: 0%;
    opacity: 0;
  }
  to {
    width: 30%;
    opacity: 1;
  }
}

/* 水墨涟漪效果增强 */
.ink-ripple {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at var(--x, 50%) var(--y, 50%), 
    rgba(139, 149, 137, 0.441) 0%, 
    rgba(105, 117, 102, 0.08) 40%,
    transparent 70%);
  opacity: 0;
  transition: opacity 0.4s ease;
  pointer-events: none;
}

.nav-item:hover .ink-ripple {
  opacity: 1;
  animation: rippleGlow 2s ease-in-out infinite;
}

@keyframes rippleGlow {
  0%, 100% {
    opacity: 0.8;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.02);
  }
}

/* 活跃状态样式 */
.nav-item.router-link-active {
  background: rgba(250, 255, 249, 0.659);
  color: rgba(76, 86, 73, 1);
  font-weight: 600;
}

.nav-item.router-link-active .link-icon {
  color: rgba(139, 149, 137, 1);
  transform: scale(1.1);
}

.nav-item.router-link-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 60%;
  background: linear-gradient(to bottom, 
    rgba(139, 149, 137, 0.8), 
    rgba(105, 117, 102, 0.6));
  border-radius: 0 2px 2px 0;
}

/* 导航面板墨点样式 */
.navbar-ink-dots {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  border-radius: 12px;
  overflow: hidden;
  z-index: 1;
}

.navbar-ink-dot {
  position: absolute;
  background: radial-gradient(circle, 
    rgba(47, 54, 44, 0.7) 0%, 
    rgba(76, 86, 73, 0.5) 30%, 
    rgba(105, 117, 102, 0.3) 60%, 
    transparent 85%);
  border-radius: 50%;
  transform: translate(-50%, -50%);
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  animation: navbarInkDotFloat 4s ease-in-out infinite;
  animation-delay: var(--delay, 0s);
}

.navbar-ink-dot.clicked {
  animation: navbarInkDotExpand 0.8s ease-out forwards;
  background: radial-gradient(circle, 
    rgba(47, 54, 44, 0.9) 0%, 
    rgba(76, 86, 73, 0.7) 25%, 
    rgba(105, 117, 102, 0.5) 50%, 
    rgba(139, 149, 137, 0.3) 75%,
    transparent 90%);
}

@keyframes navbarInkDotFloat {
  0%, 100% { 
    transform: translate(-50%, -50%) scale(1); 
    opacity: 0.4; 
  }
  50% { 
    transform: translate(-50%, -50%) scale(1.3); 
    opacity: 0.7; 
  }
}

@keyframes navbarInkDotExpand {
  0% { 
    transform: translate(-50%, -50%) scale(1); 
  }
  30% { 
    transform: translate(-50%, -50%) scale(2.5); 
  }
  100% { 
    transform: translate(-50%, -50%) scale(2); 
  }
}

/* 导航面板墨晕扩散效果 */
.navbar-ink-splash-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  border-radius: 12px;
  overflow: hidden;
  z-index: 1;
}

.navbar-ink-splash {
  position: absolute;
  background: radial-gradient(circle, 
    rgba(47, 54, 44, 0.3) 0%, 
    rgba(76, 86, 73, 0.25) 15%, 
    rgba(105, 117, 102, 0.2) 30%, 
    rgba(139, 149, 137, 0.15) 50%,
    rgba(205, 214, 190, 0.1) 70%,
    transparent 85%);
  border-radius: 50%;
  animation: navbarInkSplashExpand 1.5s ease-out forwards;
}

@keyframes navbarInkSplashExpand {
  0% {
    transform: translate(-50%, -50%) scale(0);
    opacity: 0.8;
  }
  20% {
    opacity: 0.6;
  }
  60% {
    opacity: 0.3;
  }
  100% {
    transform: translate(-50%, -50%) scale(4);
    opacity: 0;
  }
}

/* 导航面板鼠标交互增强 */
.navbar:hover .navbar-ink-dot {
  animation-duration: 3s;
  opacity: 0.6;
}


/* 悬浮球样式 */
.floating-ball {
  position: fixed;
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, 
    rgba(139, 149, 137, 0.435) 0%,
    rgba(105, 117, 102, 0.255) 30%,
    rgba(76, 86, 73, 0.251) 70%,
    rgba(47, 54, 44, 0.139) 100%);
  border: 2px solid rgba(205, 214, 190, 0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(205, 214, 190, 0.95);
  cursor: pointer;
  z-index: 1000;
  box-shadow: 
    0 6px 24px rgba(47, 54, 44, 0.4),
    inset 0 2px 6px rgba(205, 214, 190, 0.15),
    0 0 0 1px rgba(139, 149, 137, 0.2);
  transition: all 0.5s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  user-select: none;
  backdrop-filter: blur(8px);
  overflow: hidden;
  position: relative;
}

/* 水墨云雾背景 */
.ink-cloud-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 50%;
  overflow: hidden;
}

.ink-layer {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(205, 214, 190, 0.1) 20%, rgba(139, 149, 137, 0.05) 60%, transparent 80%);
  animation: inkFlow 8s ease-in-out infinite;
}

.ink-1 {
  width: 90%;
  height: 90%;
  top: 5%;
  left: 5%;
  animation-delay: 0s;
}

.ink-2 {
  width: 70%;
  height: 70%;
  top: 15%;
  left: 15%;
  animation-delay: -3s;
}

.ink-3 {
  width: 50%;
  height: 50%;
  top: 25%;
  left: 25%;
  animation-delay: -6s;
}

@keyframes inkFlow {
  0%, 100% { 
    transform: translate(0, 0) rotate(0deg) scale(1); 
    opacity: 0.2; 
  }
  33% { 
    transform: translate(-1px, -2px) rotate(120deg) scale(1.05); 
    opacity: 0.4; 
  }
  66% { 
    transform: translate(1px, -1px) rotate(240deg) scale(0.95); 
    opacity: 0.3; 
  }
}

.floating-ball:hover {
  background: linear-gradient(135deg, 
    rgba(152, 162, 149, 0.95) 0%,
    rgba(118, 130, 115, 0.9) 30%,
    rgba(89, 99, 86, 0.85) 70%,
    rgba(60, 67, 57, 0.95) 100%);
  transform: scale(1.1);
  box-shadow: 
    0 8px 32px rgba(47, 54, 44, 0.5),
    inset 0 3px 8px rgba(205, 214, 190, 0.2),
    0 0 0 2px rgba(152, 162, 149, 0.3);
  border-color: rgba(205, 214, 190, 0.5);
}

.floating-ball:active {
  transform: scale(0.95);
}

.floating-ball.active {
  transform: scale(1.15);
  background: linear-gradient(135deg, 
    rgba(162, 172, 159, 1) 0%,
    rgba(128, 140, 125, 0.95) 30%,
    rgba(99, 109, 96, 0.9) 70%,
    rgba(70, 77, 67, 0.95) 100%);
  border-color: rgba(205, 214, 190, 0.7);
}

.floating-ball.hidden-left,
.floating-ball.hidden-right {
  transform: scale(0.8);
  opacity: 0.7;
}

.floating-ball.transparent {
  opacity: 0.5;
}

.floating-ball.hidden-left:hover, 
.floating-ball.hidden-right:hover,
.floating-ball.transparent:hover {
  transform: scale(1);
  opacity: 0.9;
}

.ball-icon {
  position: relative;
  z-index: 2;
  transition: all 0.4s ease;
  filter: drop-shadow(0 2px 4px rgba(47, 54, 44, 0.3));
}

.floating-ball.active .ball-icon {
  transform: scale(1.1);
  filter: drop-shadow(0 3px 6px rgba(47, 54, 44, 0.4));
}

/* 导航面板样式 - 古典卷轴主题 */
.navbar {
  position: fixed;
  width: 320px;
  max-height: 80vh;
  background: linear-gradient(135deg, 
    rgba(250, 248, 240, 0.603) 0%,
    rgba(245, 243, 235, 0.92) 25%,
    rgba(240, 238, 230, 0.356) 50%,
    rgba(235, 233, 225, 0.541) 100%);
  border: 2px solid rgba(139, 149, 137, 0.3);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 
    0 12px 48px rgba(47, 54, 44, 0.25),
    0 0 0 1px rgba(205, 214, 190, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  z-index: 999;
  border-radius: 12px;
  overflow: hidden;
  transform-origin: left center;
  margin-left: 70px;
  position: relative;
}

/* 宣纸纹理 */
.paper-texture {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.paper-grain {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 30%, rgba(139, 149, 137, 0.03) 1px, transparent 1px),
    radial-gradient(circle at 60% 70%, rgba(105, 117, 102, 0.02) 1px, transparent 1px),
    radial-gradient(circle at 80% 20%, rgba(76, 86, 73, 0.02) 1px, transparent 1px);
  background-size: 50px 50px, 30px 30px, 40px 40px;
  animation: paperBreathe 12s ease-in-out infinite;
}

@keyframes paperBreathe {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 0.8; }
}

.paper-shadow {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 20px;
  background: linear-gradient(to bottom, rgba(139, 149, 137, 0.1), transparent);
}

/* 水墨渲染背景 */
.ink-wash-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
}

.wash-layer {
  position: absolute;
  border-radius: 50%;
  background: radial-gradient(ellipse, rgba(139, 149, 137, 0.08) 0%, rgba(105, 117, 102, 0.05) 40%, transparent 70%);
  animation: washSpread 10s ease-in-out infinite;
}

.wash-1 {
  width: 180px;
  height: 120px;
  top: -30px;
  right: -40px;
  border-radius: 60%;
  animation-delay: 0s;
}

.wash-2 {
  width: 140px;
  height: 90px;
  bottom: -20px;
  left: -30px;
  border-radius: 70%;
  animation-delay: -4s;
}

.wash-3 {
  width: 100px;
  height: 80px;
  top: 40%;
  left: 60%;
  border-radius: 50%;
  animation-delay: -8s;
}

@keyframes washSpread {
  0%, 100% { 
    transform: scale(0.8) rotate(0deg); 
    opacity: 0.3; 
  }
  50% { 
    transform: scale(1.2) rotate(180deg); 
    opacity: 0.6; 
  }
}





.nav-container {
  padding: 24px;
  max-height: 80vh;
  overflow-y: auto;
  position: relative;
  z-index: 2;
}

.nav-header {
  margin-bottom: 24px;
  padding: 0 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 2px solid rgba(139, 149, 137, 0.2);
  padding-bottom: 20px;
  position: relative;
}

.nav-header::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 40%;
  height: 2px;
  background: linear-gradient(to right, rgba(139, 149, 137, 0.4), transparent);
}

.nav-header h2 {
  color: rgba(76, 86, 73, 0.95);
  font-size: 22px;
  font-weight: 600;
  margin: 0;
  text-shadow: 1px 1px 2px rgba(205, 214, 190, 0.3);
  font-family: '楷体', 'KaiTi', serif;
  letter-spacing: 2px;
}

.cloud-seal {
  color: rgba(139, 149, 137, 0.8);
  animation: sealBreathe 4s ease-in-out infinite;
  padding: 4px;
  border-radius: 4px;
}

@keyframes sealBreathe {
  0%, 100% { transform: scale(0.98); opacity: 0.8; }
  50% { transform: scale(1.1); opacity: 0.95; }
}

.navbar ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* 卷轴展开动画 */
.scroll-unfold-enter-active {
  animation: scrollUnfold 0.62s cubic-bezier(0.25, 0.46, 0.45, 0.94) both;
}

.scroll-unfold-leave-active {
  animation: scrollFold 0.5s ease-in both;
}

@keyframes scrollUnfold {
  0% {
    transform: scale(0.1) rotateY(-90deg);
    opacity: 0;
    filter: blur(8px);
  }
  50% {
    transform: scale(1.05) rotateY(-20deg);
    opacity: 0.8;
    filter: blur(2px);
  }
  100% {
    transform: scale(1) rotateY(0deg);
    opacity: 1;
    filter: blur(0px);
  }
}

@keyframes scrollFold {
  0% {
    transform: scale(1) rotateY(0deg);
    opacity: 1;
    filter: blur(0px);
  }
  100% {
    transform: scale(0.8) rotateY(-45deg);
    opacity: 0;
    filter: blur(4px);
  }
}

/* 遮罩层样式 */
.overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle, rgba(47, 54, 44, 0.1) 0%, rgba(76, 86, 73, 0.3) 100%);
  backdrop-filter: blur(1px);
  z-index: 998;
}

/* 淡入淡出动画 */
.fade-enter-active, .fade-leave-active {
  transition: all 0.4s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
  backdrop-filter: blur(0px);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .navbar {
    width: 280px;
    margin-left: 60px;
  }
  
  .floating-ball {
    width: 50px;
    height: 50px;
  }
  
  .nav-item {
    font-size: 15px;
    padding: 14px 18px;
  }
  
  .link-icon {
    width: 20px;
    height: 20px;
    margin-right: 12px;
  }
}

/* 滚动条美化 - 古典风格 */
.nav-container::-webkit-scrollbar {
  width: 6px;
}

.nav-container::-webkit-scrollbar-track {
  background: rgba(205, 214, 190, 0.1);
  border-radius: 3px;
}

.nav-container::-webkit-scrollbar-thumb {
  background: rgba(139, 149, 137, 0.4);
  border-radius: 3px;
  transition: background 0.3s ease;
}

.nav-container::-webkit-scrollbar-thumb:hover {
  background: rgba(139, 149, 137, 0.6);
}
</style>