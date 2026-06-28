# 拖延症杀手（Procrastination Killer）

> 把大目标拆成小行动，让拖延无处可逃。

一个 AI 驱动的目标管理工具。输入宏观目标，DeepSeek 自动将其拆解为具体可执行的小任务，通过可视化的进度追踪激励你一步步达成目标。

---

## 核心流程

```
用户输入目标 → AI 自动拆解 → 生成任务清单 → 逐项完成勾选 → 进度实时更新 → 全部完成自动归档
```

1. **注册/登录** — JWT 认证，多用户数据隔离
2. **输入目标** — 一句话描述你想达成的事
3. **AI 拆解** — 后端调用 DeepSeek，返回结构化 JSON，拆成 3-8 个可执行小任务
4. **查看进度** — 目标卡片展示完成百分比，详情页展示环形进度 + 任务清单
5. **勾选完成** — 点击复选框标记完成，后端自动重算进度；全部完成时目标状态自动变为"已完成"

---

## 技术栈

| 层 | 技术 |
|---|---|
| **后端框架** | Spring Boot 3.3 / Spring AI 1.0 / MyBatis-Plus 3.5 |
| **AI 模型** | DeepSeek（通过 OpenAI 兼容接口） |
| **数据库** | MySQL 8.x |
| **认证** | Spring Security + JWT（jjwt 0.12） |
| **接口文档** | Knife4j / Swagger |
| **前端框架** | Vue 3（Composition API / `<script setup>`） |
| **构建工具** | Vite |
| **状态管理** | Pinia（setup 风格） |
| **UI 组件库** | Element Plus |
| **HTTP 客户端** | Axios（拦截器自动注入 JWT） |

---

## 项目结构

```
task-kill/
├── procrastination-killer/          # 后端 Spring Boot 项目
│   ├── pom.xml
│   └── src/main/java/com/procrastination/killer/
│       ├── ProcrastinationKillerApplication.java
│       ├── ai/            # AI Prompt 模板
│       ├── common/        # Result / JwtUtil / 全局异常处理
│       ├── config/        # Security / JWT Filter / Knife4j / AI
│       ├── controller/    # User / Goal / Task REST 接口
│       ├── dto/           # 请求体 / 视图对象
│       ├── entity/        # User / Goal / Task 实体
│       ├── mapper/        # MyBatis-Plus Mapper
│       └── service/       # 核心业务逻辑
│
├── procrastination-killer-web/      # 前端 Vue 3 项目
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── api/           # Axios 封装 + API 模块
│       ├── stores/        # Pinia 用户状态
│       ├── router/        # Vue Router + 导航守卫
│       ├── views/         # Login / Home / GoalDetail
│       └── components/    # GoalCard / TaskList / CreateGoalDialog / AppLayout
│
└── README.md
```

---

## 数据库

建表脚本位于 `procrastination-killer/src/main/resources/db/schema.sql`，包含三张表：

| 表 | 说明 | 关键字段 |
|---|---|---|
| `user` | 用户表 | id, username, password(BCrypt), email |
| `goal` | 目标表（父表） | id, user_id, title, status(进行中/已完成), progress(0-100) |
| `task` | 任务表（子表） | id, goal_id, content, status(待办/已完成), sort_order |

`user` 1:N `goal` 1:N `task`，通过 `user_id` 实现数据隔离。

---

## 快速启动

### 1. 数据库

```sql
CREATE DATABASE procrastination_killer DEFAULT CHARACTER SET utf8mb4;
USE procrastination_killer;
SOURCE procrastination-killer/src/main/resources/db/schema.sql;
```

### 2. 后端

修改 `procrastination-killer/src/main/resources/application.yml` 中的数据库密码和 DeepSeek API Key：

```yaml
spring:
  datasource:
    password: 你的MySQL密码
  ai:
    openai:
      api-key: 你的DeepSeek API Key
```

```bash
cd procrastination-killer
mvn spring-boot:run          # 启动在 localhost:8080
```

### 3. 前端

```bash
cd procrastination-killer-web
npm install
npm run dev                   # 启动在 localhost:3000，/api 自动代理到 localhost:8080
```

浏览器打开 `http://localhost:3000`。

---

## API 接口

| 方法 | 路径 | 说明 | 认证 |
|---|---|---|---|
| POST | `/api/user/register` | 用户注册 | 否 |
| POST | `/api/user/login` | 用户登录，返回 JWT | 否 |
| POST | `/api/goal/create` | 创建目标（AI 拆解） | 是 |
| GET | `/api/goal/list` | 获取我的目标列表 | 是 |
| GET | `/api/goal/{id}` | 获取目标详情（含子任务） | 是 |
| PUT | `/api/task/{id}/complete` | 标记任务完成（触发进度联动） | 是 |
| PUT | `/api/task/{id}/undo` | 撤销任务完成 | 是 |

启动后端后访问 `http://localhost:8080/doc.html` 查看 Knife4j 接口文档。

---

## 状态联动机制

任意任务被标记为"已完成"时：

1. 统计该目标下已完成任务数 ÷ 总任务数 → 计算进度百分比
2. 原子更新 `goal.progress` 和 `goal.task_completed`
3. 若 `task_completed >= task_total`，SQL 中通过 `CASE WHEN` 自动将 `goal.status` 更新为"已完成"

所有操作在同一个数据库事务内完成，保证一致性。
