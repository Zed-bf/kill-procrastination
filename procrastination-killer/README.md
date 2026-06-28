# procrastination-killer（后端）

Spring Boot 3 后端服务，提供用户认证、AI 目标拆解、任务管理 RESTful API。

## 技术栈

- Spring Boot 3.3.5
- Spring AI 1.0（对接 DeepSeek）
- MyBatis-Plus 3.5.7
- MySQL 8.x
- Spring Security + JWT
- Knife4j / Swagger

## 快速启动

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.x

### 1. 建库建表

```sql
CREATE DATABASE procrastination_killer DEFAULT CHARACTER SET utf8mb4;
USE procrastination_killer;
SOURCE src/main/resources/db/schema.sql;
```

### 2. 修改配置

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/procrastination_killer  # Docker 环境用 host.docker.internal
    username: root
    password: 你的密码
  ai:
    openai:
      api-key: 你的DeepSeek API Key
```

### 3. 启动

```bash
mvn spring-boot:run
```

服务启动在 `http://localhost:8080`，接口文档在 `http://localhost:8080/doc.html`。

## 项目结构

```
src/main/java/com/procrastination/killer/
├── ProcrastinationKillerApplication.java   # 启动类
├── ai/
│   └── AIPromptTemplates.java              # DeepSeek Prompt 模板
├── common/
│   ├── CurrentUserHelper.java              # 从 SecurityContext 获取当前用户
│   ├── GlobalExceptionHandler.java         # 全局异常 → JSON 响应
│   ├── JwtUtil.java                        # JWT 生成 / 解析 / 校验
│   └── Result.java                         # 统一响应体 {code, message, data}
├── config/
│   ├── AIConfig.java                       # ChatClient Bean 创建
│   ├── JwtAuthenticationFilter.java        # JWT 过滤器
│   ├── Knife4jConfig.java                  # Swagger 文档配置
│   ├── MybatisPlusConfig.java              # 自动填充时间字段
│   └── SecurityConfig.java                 # Spring Security 配置
├── controller/
│   ├── GoalController.java                 # 目标 CRUD 接口
│   ├── TaskController.java                 # 任务状态更新接口
│   └── UserController.java                 # 注册 / 登录接口
├── dto/                                    # 请求体 / 视图对象
├── entity/
│   ├── Goal.java                           # 目标实体
│   ├── Task.java                           # 任务实体
│   └── User.java                           # 用户实体
├── mapper/                                 # MyBatis-Plus Mapper
└── service/
    ├── GoalService.java                    # 核心：AI 调用 + 入库 + 层级查询
    ├── TaskService.java                    # 核心：状态联动更新
    └── UserService.java                    # 注册 / 登录逻辑
```

## API 接口

| 方法 | 路径 | 说明 | 认证 |
|---|---|---|---|
| POST | `/api/user/register` | 用户注册 | 否 |
| POST | `/api/user/login` | 用户登录，返回 JWT | 否 |
| POST | `/api/goal/create` | 创建目标（调用 AI 拆解） | 是 |
| GET | `/api/goal/list` | 获取用户目标列表 | 是 |
| GET | `/api/goal/{id}` | 获取目标详情（含子任务） | 是 |
| PUT | `/api/task/{id}/complete` | 标记任务完成 | 是 |
| PUT | `/api/task/{id}/undo` | 撤销任务完成 | 是 |

## 核心机制

### AI 目标拆解

用户发送目标 → `GoalService` 构建 Prompt → ChatClient 调用 DeepSeek → 清洗 Markdown 代码块 → 解析 JSON `{goalTitle, goalDescription, tasks[]}` → 事务性写入 goal + task 表。

### 状态联动

任务完成时：统计 goal 下 task 完成数 → 计算 progress → 原子 SQL 更新 progress + 条件更新 status（全部完成则 status="已完成"）。
