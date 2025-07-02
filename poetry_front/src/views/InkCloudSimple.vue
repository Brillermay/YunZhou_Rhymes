<template>
  <div class="simple-test">
    <h1>水墨云图系统 - 简单测试</h1>
    
    <div class="test-section">
      <h2>基础组件测试</h2>
      
      <!-- 基础水墨画布 -->
      <div class="component-test">
        <h3>基础水墨画布</h3>
        <div class="test-container">
          <InkCloudCanvas 
            :theme="'classic'"
            :mouse-position="{ x: 100, y: 100 }"
            :active="true"
          />
        </div>
      </div>
      
      <!-- 云层系统 -->
      <div class="component-test">
        <h3>云层粒子系统</h3>
        <div class="test-container">
          <CloudSystem 
            :intensity="1"
            :time-of-day="'day'"
            :mouse-position="{ x: 100, y: 100 }"
          />
        </div>
      </div>
      
      <!-- 古典装饰元素 -->
      <div class="component-test">
        <h3>古典装饰元素</h3>
        <div class="test-container">
          <AncientElements 
            :theme="'classic'"
            :intensity="1"
            :show-poetry="true"
          />
        </div>
      </div>
      
      <!-- 导航系统 -->
      <div class="component-test">
        <h3>印章导航系统</h3>
        <div class="test-container">
          <NavigationSeal 
            :theme="'classic'"
            :position="'bottom-right'"
            :auto-close="true"
          />
        </div>
      </div>
    </div>
    
    <div class="controls">
      <button @click="testAll">测试所有组件</button>
      <button @click="goToMain">前往主界面</button>
      <button @click="goHome">返回首页</button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import InkCloudCanvas from '@/components/inkcloud/InkCloudCanvas.vue'
import CloudSystem from '@/components/inkcloud/CloudSystem.vue'
import AncientElements from '@/components/inkcloud/AncientElements.vue'
import NavigationSeal from '@/components/inkcloud/NavigationSeal.vue'

const router = useRouter()

const testAll = () => {
  console.log('开始测试所有组件...')
  alert('所有组件测试完成！请查看控制台输出。')
}

const goToMain = () => {
  router.push('/inkcloud')
}

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.simple-test {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
  background: #f5f5f5;
  min-height: 100vh;
}

h1 {
  text-align: center;
  color: #8B4513;
  font-family: 'STKaiti', 'KaiTi', serif;
  margin-bottom: 30px;
}

h2 {
  color: #2F4F4F;
  border-bottom: 2px solid #8B4513;
  padding-bottom: 10px;
  margin: 30px 0 20px 0;
}

.component-test {
  margin-bottom: 40px;
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.component-test h3 {
  color: #8B4513;
  margin-bottom: 15px;
  font-size: 18px;
}

.test-container {
  position: relative;
  width: 100%;
  height: 300px;
  border: 2px dashed #ddd;
  border-radius: 8px;
  overflow: hidden;
  background: linear-gradient(135deg, #FFF8DC 0%, #F5F5DC 100%);
}

.controls {
  text-align: center;
  margin-top: 40px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.controls button {
  margin: 0 10px;
  padding: 12px 24px;
  background: #8B4513;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s ease;
}

.controls button:hover {
  background: #A0522D;
  transform: translateY(-2px);
}

.controls button:active {
  transform: translateY(0);
}
</style>
