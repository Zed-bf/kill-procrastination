# procrastination-killer-web（前端）

Vue 3 前端应用，为"拖延症杀手"提供用户交互界面。

## 技术栈

- Vue 3.5（Composition API / `<script setup>`）
- Vite 6
- Vue Router 4
- Pinia 2（setup 风格）
- Element Plus 2
- Axios

## 快速启动

### 环境要求

- Node.js 18+
- 后端服务已启动（`http://localhost:8080`）

### 安装与运行

```bash
npm install
npm run dev
```

浏览器打开 `http://localhost:3000`。Vite 自动将 `/api` 请求代理到后端 `http://localhost:8080`。

## 项目结构

```
src/
├── main.js                     # 入口：注册 Pinia / Router / ElementPlus
├── App.vue                     # 根组件（fade 路由过渡）
├── styles/global.css           # 全局样式 + CSS 变量
├── api/
│   ├── request.js              # Axios 实例 + 请求/响应拦截器
│   ├── user.js                 # 用户 API
│   ├── goal.js                 # 目标 API
│   └── task.js                 # 任务 API
├── stores/
│   └── user.js                 # Pinia 用户状态（token/userId/nickname 持久化）
├── router/
│   └── index.js                # 路由配置 + beforeEach 守卫
├── views/
│   ├── LoginView.vue           # 登录 / 注册页
│   ├── HomeView.vue            # 目标主页（进行中/已完成分组）
│   └── GoalDetailView.vue      # 目标详情页（进度环 + 任务清单）
└── components/
    ├── AppLayout.vue           # 布局：顶部导航栏
    ├── GoalCard.vue            # 目标卡片（标题 + 进度条）
    ├── CreateGoalDialog.vue    # 新建目标弹窗（输入 → AI 生成中动画）
    └── TaskList.vue            # 任务列表（复选框 + 完成/撤销）
```

## 页面路由

| 路径 | 页面 | 认证 |
|---|---|---|
| `/login` | 登录 / 注册 | 否（已登录自动跳 /home） |
| `/home` | 目标列表 | 是（未登录跳 /login） |
| `/goal/:id` | 目标详情 + 任务清单 | 是 |

## 核心交互

### 新建目标

点击"新建目标" → 输入目标内容 → 点击"开始拆解" → 弹窗内显示加载动画 → AI 返回后自动关闭并刷新列表。

### 任务勾选

勾选任务复选框 → 调用 `/api/task/{id}/complete` → 后端计算进度并更新目标状态 → 前端自动刷新进度环和数据。

### 认证流程

- 请求拦截器对非公开接口自动附加 `Authorization: Bearer <token>`
- 响应拦截器检测 401/403 → 清除登录态 → 跳转 `/login`
- 路由守卫：未登录访问需认证页面 → 重定向 `/login`
- 登录页 `onMounted` 时自动清除过期 Token
