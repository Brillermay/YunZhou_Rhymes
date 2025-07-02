/**
 * 🎨 资源准备脚本
 * 用于创建必要的占位图片和检查资源完整性
 */

import fs from 'fs'
import path from 'path'

const ASSETS_DIR = './src/components/homepage/assets'

// 创建SVG占位图
const createPlaceholderSVG = (name, width, height, content) => {
  return `<svg width="${width}" height="${height}" viewBox="0 0 ${width} ${height}" xmlns="http://www.w3.org/2000/svg">
    <rect width="100%" height="100%" fill="#f8f9fa" stroke="#8c7853" stroke-width="2"/>
    <text x="50%" y="50%" text-anchor="middle" dy="0.3em" font-family="serif" font-size="14" fill="#2c3e50">
      ${content || name}
    </text>
  </svg>`
}

// 创建基础资源文件
const createBasicAssets = () => {
  // 创建目录
  const dirs = ['images', 'fonts']
  dirs.forEach(dir => {
    const dirPath = path.join(ASSETS_DIR, dir)
    if (!fs.existsSync(dirPath)) {
      fs.mkdirSync(dirPath, { recursive: true })
      console.log(`📁 创建目录: ${dirPath}`)
    }
  })
  
  // 创建占位图片
  const placeholders = [
    { name: 'seal-pattern.svg', width: 100, height: 100, content: '印章' },
    { name: 'brush-texture.svg', width: 200, height: 50, content: '毛笔纹理' },
    { name: 'decorative-border.svg', width: 200, height: 200, content: '装饰边框' },
    { name: 'cloud-shape-1.svg', width: 150, height: 80, content: '云朵1' },
    { name: 'cloud-shape-2.svg', width: 180, height: 90, content: '云朵2' }
  ]
  
  placeholders.forEach(({ name, width, height, content }) => {
    const filePath = path.join(ASSETS_DIR, 'images', name)
    const svgContent = createPlaceholderSVG(name, width, height, content)
    
    if (!fs.existsSync(filePath)) {
      fs.writeFileSync(filePath, svgContent)
      console.log(`🖼️ 创建占位图: ${name}`)
    }
  })
  
  console.log('✅ 基础资源创建完成！')
  console.log('\n📝 下一步：')
  console.log('1. 替换占位图为实际设计资源')
  console.log('2. 添加中文字体文件到 fonts/ 目录')
  console.log('3. 运行项目测试效果')
}

// 检查资源完整性
const checkAssets = () => {
  console.log('🔍 检查资源文件...')
  
  const requiredFiles = [
    'images/decorative-border.svg',
    'images/seal-pattern.svg',
    'fonts/fonts.css'
  ]
  
  requiredFiles.forEach(file => {
    const filePath = path.join(ASSETS_DIR, file)
    if (fs.existsSync(filePath)) {
      console.log(`✅ ${file}`)
    } else {
      console.log(`❌ ${file} - 缺失`)
    }
  })
}

// 如果直接运行此脚本
if (import.meta.url === `file://${process.argv[1]}`) {
  createBasicAssets()
  checkAssets()
}

export { createBasicAssets, checkAssets }