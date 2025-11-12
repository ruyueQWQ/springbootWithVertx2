# 双人联机游戏 - Spring Boot + Vert.x + Unity + Protobuf

这是一个使用Spring Boot、MyBatis-Plus、MySQL、Vert.x和Unity开发的双人联机游戏项目。游戏内容简单，玩家可以控制方块在游戏场景中移动。

## 技术栈

- **后端**: Spring Boot 2.7.18, MyBatis-Plus 3.5.3.1, MySQL 8.0, Vert.x 4.4.5, Protobuf 3.21.12
- **前端**: Unity 2021.3+

## 项目结构

```
springbootWithVertx2/
├── src/
│   ├── main/
│   │   ├── java/com/game/       # Java源代码
│   │   │   ├── config/          # 配置类
│   │   │   ├── entity/          # 实体类
│   │   │   ├── mapper/          # MyBatis Mapper接口
│   │   │   ├── service/         # 服务接口和实现
│   │   │   ├── tcp/             # TCP服务器相关
│   │   │   ├── protobuf/        # Protobuf生成的类（编译后）
│   │   │   └── SpringbootVertxGameApplication.java  # 应用入口
│   │   ├── proto/               # Protobuf定义文件
│   │   └── resources/           # 资源文件
│   │       └── application.yml  # 配置文件
├── db_init.sql                  # 数据库初始化脚本
├── pom.xml                      # Maven配置文件
├── UnityClientExample.cs        # Unity客户端示例代码
├── MainThreadDispatcher.cs      # Unity主线程调度器
└── README.md                    # 项目说明文档
```

## 功能特性

- 用户注册和登录
- 创建游戏房间
- 加入游戏房间
- 开始游戏
- 控制方块移动
- 实时同步玩家位置

## 快速开始

### 1. 环境准备

- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Unity 2021.3+

### 2. 数据库设置

1. 创建数据库并执行初始化脚本：

```bash
mysql -u root -p < db_init.sql
```

2. 确保MySQL服务正在运行，并且可以通过配置的用户名和密码访问。

### 3. 构建和运行后端服务

1. 编译项目：

```bash
mvn clean install
```

2. 运行Spring Boot应用：

```bash
mvn spring-boot:run
```

3. 检查服务是否正常启动。如果一切顺利，你应该能看到TCP服务器在端口9000上启动的消息。

### 4. Unity客户端设置

1. 创建一个新的Unity项目。
2. 导入Google.Protobuf包（通过Package Manager或直接导入DLL）。
3. 将`UnityClientExample.cs`和`MainThreadDispatcher.cs`添加到项目中。
4. 在场景中创建必要的UI元素（输入框、按钮、文本等），并将它们与脚本中的引用关联起来。
5. 创建两个3D方块，分别代表自己和对手。
6. 运行Unity项目。

## 使用说明

1. 在Unity客户端中，首先连接到服务器。
2. 注册一个新账号或使用已有账号登录。
3. 创建一个游戏房间或加入一个已存在的房间。
4. 当两个玩家都加入房间后，房主可以开始游戏。
5. 使用方向键或WASD键控制你的方块移动。

## 注意事项

- 确保防火墙允许TCP连接到端口9000。
- 实际部署时，需要将Unity客户端中的服务器IP地址更改为实际的服务器地址。
- 为了安全起见，实际生产环境中应该对密码进行加密存储。

## 扩展开发

如果你想扩展游戏功能，可以考虑以下几点：

1. 添加游戏规则和胜负判定条件。
2. 增加多种游戏模式。
3. 优化网络同步机制，减少延迟。
4. 添加更多的游戏元素和交互功能。