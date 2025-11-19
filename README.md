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

我已经创建了一个功能完整的窗口控制脚本 WindowController.cs ，您可以将其添加到Unity项目中使用。

脚本功能特点：

1. 窗口缩小功能 ：可以将窗口缩小到指定分辨率（默认800x600）
2. 窗口恢复功能 ：可以恢复到原始窗口大小
3. 快捷键支持 ：
   - F11：缩小窗口
   - F12：恢复窗口
4. UI按钮支持 ：可以通过UI按钮控制窗口大小
5. 自定义窗口大小 ：提供了设置自定义窗口大小的方法
使用方法：

1. 将 WindowController.cs 文件添加到Unity项目的 Assets 文件夹中
2. 在Unity场景中创建一个空游戏对象，命名为 WindowController
3. 将 WindowController.cs 脚本挂载到该游戏对象上
4. （可选）在Inspector面板中设置缩小后的窗口分辨率
5. （可选）将UI按钮拖拽到脚本的 shrinkWindowButton 和 restoreWindowButton 字段上
6. 运行游戏，使用快捷键或UI按钮控制窗口大小
脚本核心功能说明：

- 使用 Screen.SetResolution() 方法来控制窗口大小
- 自动保存原始窗口设置，便于恢复
- 提供了灵活的API接口，可以根据需要扩展功能
您可以根据实际需求调整脚本中的默认窗口分辨率或快捷键设置。