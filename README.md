# RPA 爬虫运营管理系统 - 后端开发指南

## 一、项目简介

本项目是一个基于 Spring Boot + Vue 的 RPA（机器人流程自动化）爬虫运营管理系统，主要用于金融行业数据采集与管理。系统实现了用户登录认证、系统管理、爬虫任务配置、任务调度、数据采集解析等核心功能。

## 二、技术栈

### 后端核心技术
- **JDK**: 17+
- **Spring Boot**: 4.0.3
- **MyBatis-Plus**: 3.5.5
- **MySQL**: 5.7+
- **JWT**: 0.12.5 (使用 Hutool 实现)
- **Groovy**: 4.0.18 (脚本引擎)
- **Playwright**: 1.42.0 (浏览器自动化)
- **Hutool**: 5.8.26 (工具类库)
- **Lombok**: 简化代码

### 前端技术（待开发）
- **Vue**: 2.x 或 3.x
- **Element-UI / Element-Plus**
- **Axios**

## 三、快速开始

### 1. 数据库初始化

执行 `src/main/resources/db/schema.sql` 脚本：

```sql
-- 创建数据库并初始化表结构
source src/main/resources/db/schema.sql
```

**默认账号信息：**
- 用户名：`admin`
- 密码：`admin123`
- 角色：超级管理员

### 2. 修改数据库配置

编辑 `src/main/resources/application.properties`：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/rpa_system?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=你的数据库密码
```

### 3. 启动项目

```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run

# 或者直接运行主类
RpaDemoApplication.main()
```

启动成功后会显示：
```
====================================
RPA 爬虫管理系统启动成功！
接口地址：http://localhost:8080/api
====================================
```

## 四、API 接口文档

### 4.1 认证接口

#### 用户登录
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

Response:
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "userId": 1,
    "username": "admin",
    "roles": ["super_admin"],
    "permissions": ["system:user:list", "system:role:list"]
  }
}
```

#### 获取用户信息
```http
GET /api/auth/user/info?userId=1
Authorization: Bearer {token}
```

#### 获取菜单树
```http
GET /api/auth/menu/tree?userId=1
Authorization: Bearer {token}
```

### 4.2 系统管理接口

#### 用户管理
```http
# 分页查询用户
GET /api/system/user/page?current=1&size=10&username=admin

# 根据 ID 查询用户
GET /api/system/user/{id}

# 新增用户
POST /api/system/user
Content-Type: application/json

{
  "username": "test",
  "password": "test123",
  "realName": "测试用户",
  "email": "test@example.com",
  "phone": "13800138000",
  "status": 1,
  "deptId": 1
}

# 修改用户
PUT /api/system/user
Content-Type: application/json

# 删除用户
DELETE /api/system/user/{id}

# 重置密码
PUT /api/system/user/resetPwd?userId=1&password=new123

# 检查用户名唯一性
GET /api/system/user/checkUsername?username=test
```

#### 角色管理
```http
# 分页查询角色
GET /api/system/role/page?current=1&size=10

# 查询所有角色
GET /api/system/role/all

# 根据 ID 查询角色
GET /api/system/role/{id}

# 新增角色
POST /api/system/role

# 修改角色
PUT /api/system/role

# 删除角色
DELETE /api/system/role/{id}
```

#### 菜单管理
```http
# 查询菜单树
GET /api/system/menu/tree

# 根据 ID 查询菜单
GET /api/system/menu/{id}

# 新增菜单
POST /api/system/menu

# 修改菜单
PUT /api/system/menu

# 删除菜单
DELETE /api/system/menu/{id}
```

## 五、项目结构

```
src/main/java/com/example/
├── RpaDemoApplication.java          # 启动类
├── rpa/
│   ├── common/                      # 公共类
│   │   └── Result.java              # 统一返回结果
│   ├── config/                      # 配置类
│   │   ├── JwtProperties.java       # JWT 配置
│   │   ├── MybatisPlusConfig.java   # MyBatis-Plus 配置
│   │   └── WebConfig.java           # Web 配置（跨域）
│   ├── controller/                  # 控制器
│   │   ├── AuthController.java      # 认证控制器
│   │   ├── SysUserController.java   # 用户管理控制器
│   │   ├── SysRoleController.java   # 角色管理控制器
│   │   └── SysMenuController.java   # 菜单管理控制器
│   ├── dto/                         # 数据传输对象
│   │   └── LoginRequest.java        # 登录请求 DTO
│   ├── entity/                      # 实体类
│   │   ├── SysUser.java             # 用户实体
│   │   ├── SysRole.java             # 角色实体
│   │   ├── SysMenu.java             # 菜单实体
│   │   ├── SysDepartment.java       # 部门实体
│   │   ├── SpiderTask.java          # 爬虫任务实体
│   │   └── TaskExecutionLog.java    # 任务日志实体
│   ├── exception/                   # 异常处理
│   │   ├── BusinessException.java   # 业务异常
│   │   └── GlobalExceptionHandler.java # 全局异常处理器
│   ├── mapper/                      # Mapper 接口
│   │   ├── SysUserMapper.java
│   │   ├── SysRoleMapper.java
│   │   ├── SysMenuMapper.java
│   │   └── ...
│   ├── service/                     # 服务层
│   │   ├── AuthService.java         # 认证服务
│   │   ├── SysUserService.java      # 用户服务
│   │   ├── SysRoleService.java      # 角色服务
│   │   ├── SysMenuService.java      # 菜单服务
│   │   └── impl/                    # 服务实现
│   ├── util/                        # 工具类
│   │   └── JwtUtil.java             # JWT 工具
│   └── vo/                          # 视图对象
│       ├── LoginResponse.java       # 登录响应 VO
│       ├── UserInfoVO.java          # 用户信息 VO
│       ├── RoleInfoVO.java          # 角色信息 VO
│       └── MenuTreeVO.java          # 菜单树 VO
```

## 六、核心功能说明

### 6.1 登录认证流程
1. 用户提交用户名和密码
2. 后端验证用户信息（MD5 加密）
3. 生成 JWT Token 返回给前端
4. 前端后续请求携带 Token
5. 后端验证 Token 有效性

### 6.2 权限控制
- **RBAC 模型**：用户 - 角色 - 菜单三级权限
- **菜单权限**：通过 `permission` 字段标识按钮级权限
- **数据权限**：通过部门和数据范围控制

### 6.3 密码加密
使用 MD5 加密（生产环境建议使用 BCrypt）

## 七、开发计划

### 已完成 ✅
- [x] 项目基础架构搭建
- [x] 数据库设计
- [x] 登录认证功能
- [x] 系统管理模块（用户、角色、菜单）
- [x] JWT Token 认证
- [x] 全局异常处理

### 待开发 📋
- [ ] 部门管理功能
- [ ] 爬虫任务管理
- [ ] Groovy 脚本执行引擎
- [ ] Playwright 浏览器自动化
- [ ] 任务调度（Quartz/Spring Schedule）
- [ ] 数据采集结果存储
- [ ] 操作日志记录
- [ ] 前端页面开发
- [ ] 前后端联调

## 八、常见问题

### Q1: 启动时报数据库连接错误？
A: 检查 `application.properties` 中的数据库配置，确保 MySQL 服务已启动。

### Q2: 登录时提示"用户名或密码错误"？
A: 数据库中默认密码是 MD5 加密的，请确认 SQL 脚本正确执行。

### Q3: 跨域问题如何解决？
A: 项目已配置跨域支持，如仍有问题检查前端请求的 URL 是否正确。

## 九、团队协作

### Git 分支管理
- `main`: 主分支，稳定版本
- `dev`: 开发分支
- `feature/*`: 功能分支

### 代码规范
1. 遵循阿里巴巴 Java 开发手册
2. 使用 Lombok 简化代码
3. 统一使用 Result 包装返回结果
4. 异常统一由 GlobalExceptionHandler 处理

## 十、联系方式

如有问题请联系项目开发团队。

---

**注意**: 这是一个教学实训项目，用于学习企业级应用开发流程。生产环境需要加强安全措施（如验证码、BCrypt 密码加密、Token 黑名单等）。
