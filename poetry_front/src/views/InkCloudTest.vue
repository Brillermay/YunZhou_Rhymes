<template>
  <div class="ink-cloud-test">
    <!-- 测试控制面板 -->
    <div class="test-controls" :class="{ hidden: controlsHidden }">
      <h3>🎨 水墨云图测试面板</h3>
      
      <div class="control-group">
        <label>主题选择:</label>
        <select v-model="currentTheme" @change="changeTheme">
          <option value="classic">古典雅韵</option>
          <option value="elegant">清雅淡墨</option>
          <option value="dream">梦幻紫韵</option>
          <option value="nature">自然清新</option>
          <option value="modern">现代简约</option>
        </select>
      </div>

      <div class="control-group">
        <label>时间段:</label>
        <select v-model="timeOfDay" @change="changeTimeOfDay">
          <option value="day">白日</option>
          <option value="evening">黄昏</option>
          <option value="night">夜晚</option>
        </select>
      </div>

      <div class="control-group">
        <label>效果强度: {{ effectIntensity }}%</label>
        <input 
          type="range" 
          v-model="effectIntensity" 
          min="10" 
          max="100" 
          @input="changeIntensity"
        />
      </div>

      <div class="control-group">
        <button @click="toggleFullscreen">
          {{ isFullscreen ? '退出全屏' : '进入全屏' }}
        </button>
        <button @click="resetSettings">重置设置</button>
        <button @click="takeScreenshot">截图保存</button>
      </div>

      <div class="control-group">
        <button @click="controlsHidden = !controlsHidden" class="toggle-btn">
          {{ controlsHidden ? '显示控制面板' : '隐藏控制面板' }}
        </button>
      </div>
    </div>

    <!-- 水墨云图主组件 -->
    <InkCloudMain 
      :theme="currentTheme"
      :time-of-day="timeOfDay"
      :effect-intensity="effectIntensity"
      :show-controls="showControls"
      @ready="onSystemReady"
      @navigation="handleNavigation"
    />

    <!-- 性能监视器 -->
    <div class="performance-monitor" v-if="showPerformance">
      <h4>性能监视器</h4>
      <div>FPS: {{ fps }}</div>
      <div>内存: {{ memoryUsage }}MB</div>
      <div>GPU: {{ gpuUsage }}%</div>
    </div>

    <!-- 状态指示器 -->
    <div class="status-indicator" :class="systemStatus">
      {{ getStatusText() }}
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
// 🔧 修复导入路径
import InkCloudMain from '../components/inkcloud/InkCloudMain.vue'

// 状态管理
const currentTheme = ref('classic')
const timeOfDay = ref('day')
const effectIntensity = ref(80)
const showControls = ref(true)
const controlsHidden = ref(false)
const isFullscreen = ref(false)
const showPerformance = ref(true)
const systemStatus = ref('loading')

// 性能数据
const fps = ref(60)
const memoryUsage = ref(0)
const gpuUsage = ref(0)

// 方法定义
const changeTheme = () => {
  console.log('🎨 切换主题:', currentTheme.value)
}

const changeTimeOfDay = () => {
  console.log('🌅 切换时间段:', timeOfDay.value)
}

const changeIntensity = () => {
  console.log('⚡ 调整效果强度:', effectIntensity.value + '%')
}

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    document.exitFullscreen()
    isFullscreen.value = false
  }
}

const resetSettings = () => {
  currentTheme.value = 'classic'
  timeOfDay.value = 'day'
  effectIntensity.value = 80
  console.log('🔄 设置已重置')
}

const takeScreenshot = () => {
  console.log('📸 截图功能启动...')
  // 这里会调用水墨云图系统的截图功能
}

const onSystemReady = () => {
  systemStatus.value = 'ready'
  console.log('✅ 水墨云图系统就绪')
  startPerformanceMonitoring()
}

const handleNavigation = (route) => {
  console.log('🧭 导航到:', route)
}

const getStatusText = () => {
  const statusMap = {
    loading: '系统加载中...',
    ready: '系统就绪',
    error: '系统错误'
  }
  return statusMap[systemStatus.value] || '未知状态'
}

const startPerformanceMonitoring = () => {
  setInterval(() => {
    // 模拟性能数据更新
    fps.value = Math.floor(Math.random() * 10) + 55
    memoryUsage.value = Math.floor(Math.random() * 50) + 80
    gpuUsage.value = Math.floor(Math.random() * 30) + 20
  }, 1000)
}

onMounted(() => {
  console.log('🚀 水墨云图测试页面启动')
})
</script>

<style scoped>
.ink-cloud-test {
  position: relative;
  width: 100vw;
  height: 100vh;
  overflow: hidden;
}

.test-controls {
  position: fixed;
  top: 20px;
  left: 20px;
  background: rgba(255, 255, 255, 0.95);
  padding: 20px;
  border-radius: 15px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  backdrop-filter: blur(10px);
  z-index: 1000;
  min-width: 300px;
  transition: transform 0.3s ease;
}

.test-controls.hidden {
  transform: translateX(-280px);
}

.test-controls h3 {
  margin: 0 0 15px 0;
  color: #2c3e50;
  font-size: 1.1rem;
}

.control-group {
  margin-bottom: 15px;
}

.control-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 600;
  color: #34495e;
}

.control-group select,
.control-group input[type="range"] {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: white;
}

.control-group button {
  padding: 8px 15px;
  margin-right: 10px;
  margin-bottom: 5px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  cursor: pointer;
  font-size: 0.9rem;
  transition: transform 0.2s ease;
}

.control-group button:hover {
  transform: translateY(-2px);
}

.toggle-btn {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%) !important;
}

.performance-monitor {
  position: fixed;
  bottom: 20px;
  left: 20px;
  background: rgba(0, 0, 0, 0.8);
  color: #00ff00;
  padding: 15px;
  border-radius: 10px;
  font-family: 'Courier New', monospace;
  font-size: 0.9rem;
  z-index: 1000;
}

.performance-monitor h4 {
  margin: 0 0 10px 0;
  color: #00ffff;
}

.status-indicator {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 10px 20px;
  border-radius: 25px;
  font-weight: 600;
  z-index: 1000;
  transition: all 0.3s ease;
}

.status-indicator.loading {
  background: linear-gradient(135deg, #ffeaa7, #fab1a0);
  color: #d63031;
}

.status-indicator.ready {
  background: linear-gradient(135deg, #55efc4, #00b894);
  color: white;
}

.status-indicator.error {
  background: linear-gradient(135deg, #fd79a8, #e84393);
  color: white;
}

/* 移动端适配 */
@media (max-width: 768px) {
  .test-controls {
    top: 10px;
    left: 10px;
    right: 10px;
    min-width: auto;
    padding: 15px;
  }
  
  .test-controls.hidden {
    transform: translateY(-90%);
  }
  
  .performance-monitor {
    bottom: 10px;
    left: 10px;
    right: 10px;
    font-size: 0.8rem;
  }
  
  .status-indicator {
    top: 10px;
    right: 10px;
    font-size: 0.8rem;
    padding: 8px 15px;
  }
}
</style>