# Visible History - 以往冒险者的尸骸

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-8-red.svg)](https://www.oracle.com/java/)
[![SlayTheSpire](https://img.shields.io/badge/SlayTheSpire-Mod-2b2d42.svg)](https://www.megacrit.com/)

一个《杀戮尖塔》(Slay the Spire)模组，在战场上显示前玩家的死亡历史，增加游戏的沉浸感和历史感。

## 📖 项目简介

Visible History 是一个为《杀戮尖塔》开发的模组，通过在战场上显示以前玩家角色的尸体，为游戏增加了新的维度。当玩家在战斗中死亡时，他们的角色会作为尸体保留在战场上，为其他冒险者提供警示。

## ✨ 主要功能

- 💀 **死亡历史展示**: 将前玩家的死亡记录保存为战场上的尸体
- 🎮 **智能交互**: 支持与游戏原版机制无缝集成
- 📊 **数据持久化**: 可靠的死亡数据存储和检索
- 🎨 **视觉优化**: 精美的视觉效果和动画
- 📋 **配置灵活**: 可自定义的配置选项

## 🚀 快速开始

### 前置要求

- Java 8 (必需，因为 Slay the Spire 使用 Java 8)
- Steam 版《杀戮尖塔》
- 必需模组:
  - [BaseMod](https://github.com/daviscook477/BaseMod)
  - [ModTheSpire](https://github.com/kiooeht/ModTheSpire)
  - [StSLib](https://github.com/kiooeht/StSLib)

### 安装步骤

1. **安装依赖模组**:
   ```
   BaseMod.jar → mods/BaseMod.jar
   ModTheSpire.jar → mods/ModTheSpire.jar
   StSLib.jar → mods/StSLib.jar
   ```

2. **编译和安装模组**:
   ```bash
   # 克隆仓库
   git clone <repository-url>
   cd VisibleHistory

   # 编译模组
   mvn clean package

   # 模组将自动复制到 Steam 的 Slay the Spire mods 目录
   # 位置: C:\Program Files (x86)\steam\steamapps\common\SlayTheSpire\mods\VisibleHistory.jar
   ```

3. **启动游戏**: 启动 Steam 版《杀戮尖塔》，模组将自动加载

### 配置 Steam 路径

在 `pom.xml` 中修改 Steam 安装路径:

```xml
<properties>
    <!-- 改成你的steam安装路径位置，指向steamapps文件夹 -->
    <Steam.path>C:\Program Files (x86)\steam\steamapps</Steam.path>
</properties>
```

## 🏗️ 项目结构

```
VisibleHistory/
├── src/main/java/VisibleHistory/
│   ├── VisibleHistory.java          # 主模组类
│   ├── cards/                       # 卡牌实现
│   ├── helpers/                     # 工具类
│   ├── modcore/                     # 核心功能
│   ├── monstercards/               # 怪物卡牌系统
│   ├── patchs/                      # 代码补丁
│   ├── playerdeath/                # 玩家死亡处理
│   ├── powers/                      # 能力系统
│   ├── rule/                       # 规则系统
│   └── utils/                      # 实用工具
├── src/main/resources/             # 资源文件
├── pom.xml                         # Maven 配置
└── README.md                       # 项目文档
```

### 核心组件

- **主模组类** (`VisibleHistory.java`): 模组的入口点
- **怪物卡牌系统**: 基于真实怪物行为的卡牌实现
- **死亡记录系统**: 追踪和显示玩家死亡历史
- **视觉效果**: 战场尸体的渲染和动画

## 🛠️ 开发环境

### 推荐的 IDE

- **IntelliJ IDEA** (推荐)
- **Eclipse**
- **VS Code** (使用 Java 扩展)

### 开发命令

```bash
# 编译项目
mvn compile

# 运行测试
mvn test

# 打包发布
mvn clean package

# 清理构建文件
mvn clean
```

### 调试技巧

1. **查看控制台输出**: 模组会在控制台输出详细的调试信息
2. **使用游戏内控制台**: BaseMod 提供内建控制台
3. **查看日志文件**: 检查游戏日志中的错误信息


## 🤝 贡献指南

我们欢迎社区贡献！请遵循以下步骤：

1. **Fork** 这个仓库
2. **创建** 你的功能分支 (`git checkout -b feature/amazing-feature`)
3. **提交** 你的更改 (`git commit -m 'Add amazing feature'`)
4. **推送** 到分支 (`git push origin feature/amazing-feature`)
5. **提交** Pull Request

### 开发规范

- 遵循现有代码风格
- 添加适当的注释和文档
- 确保新功能经过充分测试
- 更新相关文档

### 报告 Bug

使用 GitHub Issues 报告 bug 时，请包含：
- 详细的问题描述
- 复现步骤
- 预期行为 vs 实际行为
- 环境信息 (操作系统、Java 版本等)
- 相关日志文件

## 📄 许可证

本项目采用 [MIT 许可证](LICENSE) - 详情请查看 [LICENSE](LICENSE) 文件。

## 🙏 致谢

感谢以下项目和社区：

- [BaseMod](https://github.com/daviscook477/BaseMod) - Slay the Spire 模组框架
- [ModTheSpire](https://github.com/kiooeht/ModTheSpire) - 模组管理器
- [StSLib](https://github.com/kiooeht/StSLib) - Slay the Spire 工具库
- [Mega Crit](https://www.megacrit.com/) - 创造了精彩的《杀戮尖塔》
- [REME](https://gitee.com/REMEeasy/SlayTheSpireModTutorials/tree/master/Tutorials/%E9%AB%98%E7%BA%A7%E6%8A%80%E5%B7%A7/01%20-%20Patch#byref)- 制作的杀戮尖塔mod教程和历史记录相关mod

## 📞 联系方式

- **Issues**: [GitHub Issues](https://github.com/your-repo/VisibleHistory/issues)
- **Discussions**: [GitHub Discussions](https://github.com/your-repo/VisibleHistory/discussions)
- **Email**: your-email@example.com

---

**注意**: 这是一个开源项目，遵循《杀戮尖塔》的模组开发最佳实践。使用前请确保你拥有游戏的合法副本。