# Online Battle Game

## 项目简介
这是一个使用 Vue 3 和 Vite 构建的多人在线对战游戏。玩家可以创建房间，与其他玩家进行实时对战。

## 项目结构
```
online-battle-game-frontend
├── src
│   ├── assets          # 存放静态资源，如图片、字体等
│   ├── components      # Vue 组件
│   │   └── GameBoard.vue  # 渲染游戏棋盘的组件
│   ├── views          # 页面视图
│   │   ├── Home.vue   # 主页组件
│   │   └── GameRoom.vue # 游戏房间组件
│   ├── router         # 路由配置
│   │   └── index.ts   # 定义应用的路由
│   ├── store          # 状态管理
│   │   └── index.ts   # 管理游戏的全局状态
│   ├── types          # TypeScript 类型定义
│   │   └── index.ts   # 定义接口和类型
│   ├── utils          # 工具函数
│   │   └── socket.ts   # WebSocket 逻辑
│   ├── App.vue        # 应用的根组件
│   └── main.ts        # 应用的入口文件
├── public
│   └── favicon.ico    # 网站图标
├── index.html         # 主 HTML 文件
├── package.json       # npm 配置文件
├── tsconfig.json      # TypeScript 配置文件
├── vite.config.ts     # Vite 配置文件
└── README.md          # 项目文档
```

## 安装与运行
1. 克隆项目：
   ```
   git clone <repository-url>
   ```
2. 进入项目目录：
   ```
   cd online-battle-game-frontend
   ```
3. 安装依赖：
   ```
   npm install
   ```
4. 启动开发服务器：
   ```
   npm run dev
   ```

## 贡献
欢迎提交问题和功能请求，或直接提交代码贡献。

## 许可证
该项目遵循 MIT 许可证。