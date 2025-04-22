<template>
  <div>
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
      <div class="ball-icon">
        <svg viewBox="0 0 24 24" width="24" height="24">
          <path d="M3 18h18v-2H3v2zm0-5h18v-2H3v2zm0-7v2h18V6H3z" fill="currentColor"/>
        </svg>
      </div>
    </div>

    <!-- 导航面板 -->
    <transition name="flip">
      <div
        class="navbar"
        v-show="isExpanded"
        ref="navbar"
        :style="navStyle"
        @click.stop
      >
        <div class="nav-container">
          <div class="nav-header">
            <h2>云舟词渡</h2>
            <div class="weather-icon">☁️</div>
          </div>
          <ul>
            <li v-for="(item, index) in navItems" :key="index">
              <router-link 
                :to="item.path" 
                class="nav-item"
                @click.native="hideNav"
              >
                <span class="link-icon">
                  <svg viewBox="0 0 24 24" width="18" height="18">
                    <path :d="item.icon" fill="currentColor"/>
                  </svg>
                </span>
                <span class="link-text">{{ item.text }}</span>
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
        { text: '诗词测验', path: '/game' ,icon:"M6 2a2 2 0 0 0-2 2v14c0 1.1.9 2 2 2h7l5-5V4a2 2 0 0 0-2-2H6m0 2h10v10h-4v4H6V4M8 6v2h8V6H8m0 3v2h8V9H8z"},
        { text: '飞花令', path: '/feihua',icon:"M12 2a2 2 0 0 1 2 2c0 .5-.2.96-.5 1.31a4.002 4.002 0 0 1 4.88 4.88c.35-.3.81-.5 1.31-.5a2 2 0 0 1 0 4c-.5 0-.96-.2-1.31-.5a4.002 4.002 0 0 1-4.88 4.88c.3.35.5.81.5 1.31a2 2 0 0 1-4 0c0-.5.2-.96.5-1.31a4.002 4.002 0 0 1-4.88-4.88c-.35.3-.81.5-1.31.5a2 2 0 0 1 0-4c.5 0 .96.2 1.31.5a4.002 4.002 0 0 1 4.88-4.88A1.99 1.99 0 0 1 12 2z" },
        { text: '交流论坛', path: '/forum',icon:"M4 4h16a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2h-4l-4 4-4-4H4a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z"}
      ],
      isExpanded: false,
      isDragging: false,
      isHiddenLeft: false,
      isHiddenRight: false,
      isTransparent: false,
      ballPosition: { x: 20, y: 20 },
      dragOffset: { x: 0, y: 0 },
      windowSize: { width: 0, height: 0 },
      hideTimeout: null
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
    handleBallClick() {
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
      if (this.isExpanded && !this.$refs.navbar.contains(event.target)) {
        this.hideNav()
      }
    }
  }
}
</script>

<style scoped>
/* 悬浮球样式 */
.floating-ball {
  position: fixed;
  width: 50px;
  height: 50px;
  background: linear-gradient(to right, #8c7853, #6e5773);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  cursor: pointer;
  z-index: 1000;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
  user-select: none;
}

.floating-ball:hover {
  background: linear-gradient(to right, #98825a, #624768);
  transform: scale(1.1);
}

.floating-ball:active {
  transform: scale(0.95);
}

.floating-ball.active {
  transform: scale(1.1);
  background:linear-gradient(to right, #766545, #9d71a7);
}

.floating-ball.hidden-left {
  transform: translateX(-20px) scale(0.8);
  opacity: 0.5;
}

.floating-ball.hidden-right {
  transform: translateX(20px) scale(0.8);
  opacity: 0.5;
}

.floating-ball.transparent {
  opacity: 0.3;
}

.floating-ball.hidden-left:hover, 
.floating-ball.hidden-right:hover {
  transform: translateX(0) scale(1);
  opacity: 0.9;
}

.floating-ball.transparent:hover {
  opacity: 0.9;
}

.ball-icon {
  transition: transform 0.3s ease;
}

.floating-ball.active .ball-icon {
  transform: rotate(90deg);
}

/* 导航面板样式 */
.navbar {
  position: fixed;
  width: 300px;
  max-height: 80vh;
  background:linear-gradient(to right, #4d422d, #37293a);
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  z-index: 999;
  border-radius: 8px;
  overflow: hidden;
  transform-origin: left center;
  margin-left: 25px;
  transition: all 0.4s ease;
}

.nav-container {
  padding: 20px;
  max-height: 80vh;
  overflow-y: auto;
}

.nav-header {
  margin-bottom: 20px;
  padding: 0 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding-bottom: 15px;
}

.nav-header h2 {
  color: white;
  font-size: 20px;
  font-weight: 500;
  margin: 0;
}

.weather-icon {
  font-size: 28px;
}

.navbar ul {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.navbar ul li {
  position: relative;
  overflow: hidden;
  border-radius: 6px;
  transition: all 0.2s ease;
  background: rgba(255,255,255,0.03);
}

.navbar ul li:hover {
  transform: translateX(4px);
  background: rgba(255,255,255,0.08);
}

/* 导航链接样式 */
.nav-item {
  color: white;
  text-decoration: none;
  font-size: 15px;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  position: relative;
  z-index: 2;
  cursor: pointer;
}

.link-icon {
  margin-right: 12px;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  opacity: 0.8;
}

.link-text {
  flex-grow: 1;
}

/* 3D翻转动画 */
.flip-enter-active {
  animation: flip-in 0.4s ease both;
}

.flip-leave-active {
  animation: flip-out 0.3s ease both;
}

@keyframes flip-in {
  0% {
    transform: perspective(800px) rotateY(-90deg);
    opacity: 0;
  }
  100% {
    transform: perspective(800px) rotateY(0);
    opacity: 1;
  }
}

@keyframes flip-out {
  0% {
    transform: perspective(800px) rotateY(0);
    opacity: 1;
  }
  100% {
    transform: perspective(800px) rotateY(90deg);
    opacity: 0;
  }
}

/* 遮罩层样式 */
.overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.4);
  z-index: 998;
}

/* 淡入淡出动画 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .navbar {
    width: 260px;
    margin-left: 15px;
  }
  
  .floating-ball {
    width: 48px;
    height: 48px;
  }
  
  .floating-ball.hidden-left {
    transform: translateX(-15px) scale(0.8);
  }
  
  .floating-ball.hidden-right {
    transform: translateX(15px) scale(0.8);
  }
  
  .nav-item {
    font-size: 14px;
    padding: 12px 16px;
  }
  
  .link-icon {
    width: 20px;
    height: 20px;
    margin-right: 10px;
  }
}
</style>