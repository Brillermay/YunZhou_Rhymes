# Online Battle Game

## 项目概述
这是一个多人在线对战游戏，前端使用Vue 3和TypeScript构建，后端使用Node.js和Express框架。该项目旨在提供一个简单的游戏环境，玩家可以在线对战。

## 技术栈
- **前端**:
  - Vue 3
  - TypeScript
  - Vue Router

- **后端**:
  - Node.js
  - Express
  - TypeScript

## 项目结构
```
online-battle-game
├── backend
│   ├── src
│   │   ├── app.ts          # 后端应用入口
│   │   ├── controllers
│   │   │   └── gameController.ts # 游戏控制器
│   │   ├── routes
│   │   │   └── gameRoutes.ts      # 游戏路由
│   │   └── types
│   │       └── index.ts           # 类型定义
│   ├── package.json       # 后端依赖
│   ├── tsconfig.json      # TypeScript配置
│   └── README.md          # 后端文档
├── frontend
│   ├── src
│   │   ├── main.ts         # 前端应用入口
│   │   ├── App.vue         # 主组件
│   │   ├── components
│   │   │   └── GameRoom.vue # 游戏房间组件
│   │   └── types
│   │       └── index.ts    # 类型定义
│   ├── package.json        # 前端依赖
│   ├── tsconfig.json       # TypeScript配置
│   └── README.md           # 前端文档
└── README.md               # 项目总文档
```

## 如何运行
### 后端
1. 进入 `backend` 目录。
2. 安装依赖: `npm install`
3. 启动服务器: `npm start`

### 前端
1. 进入 `frontend` 目录。
2. 安装依赖: `npm install`
3. 启动前端应用: `npm run serve`

## 贡献
欢迎任何形式的贡献！请提交问题或拉取请求。

## 许可证
本项目采用MIT许可证。