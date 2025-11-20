<!--suppress HtmlDeprecatedAttribute -->
<div align="center">

# BiliZepam

第三方B站Android手表客户端

</div>

## 介绍
这是一个专为 **Wear OS 手表设备**设计的**第三方 B 站客户端**，基于 [哔哩终端](https://gitee.com/RobinNotBad/BiliClient) 开发。本项目借鉴了 [Re-WearBili](https://github.com/SpaceXC/Re-WearBili) 的部分开源代码和它们收集的部分 API，**本项目与 WearBili/Re-WearBili 无任何关系**。

项目使用 **ExoPlayer** 作为内置播放器，支持在手表上直接播放视频。

## 关于此分支
此分支试图重写大部分代码，以增强原项目的可读性和维护性。同时，使用 ``Material3`` 并改进界面观感和操作逻辑。

### 架构迁移
**项目已从传统的 Android Views 架构完全迁移至 Jetpack Compose**，这是一次重大的技术革新：

- **UI 框架**：从 Hikage + Views 迁移到声明式 UI（Jetpack Compose）
- **架构模式**：采用 MVVM 架构，使用 StateFlow 进行状态管理
- **Material Design**：全面使用 Material3 组件和设计规范
- **Wear OS 支持**：针对手表设备优化，使用 Wear Compose Material3

### 技术栈
- `Kotlin` - 主要开发语言
- `Jetpack Compose` - 声明式 UI 框架
- `Wear Compose Material3` - 手表界面组件
- `Okhttp3 + Retrofit2` - 网络请求
- `Hilt` - 依赖注入
- `Paging 3` - 分页加载
- `Coil` - 图片加载
- `ExoPlayer` - 视频播放
- 其他 `Android Jetpack` 组件

### 迁移优势
- **更好的开发体验**：声明式 UI 减少样板代码
- **更高的可维护性**：响应式状态管理，代码更清晰
- **更流畅的动画**：Compose 内置动画系统
- **更好的性能**：智能重组机制，减少不必要的 UI 更新

## 问题反馈
***不要向原项目反馈该分支的问题。***

开发者没有太多条件进行测试，所以如果你遇到了问题，请提交 Issue。

如果你有好的建议，欢迎提交 Issue 或 Pull Request。

> [!IMPORTANT]
> 注意：开发者可能只会处理此仓库的 Issues 中提出的问题，通过其他任何渠道反馈的问题都不保证会有回复。

## Thanks
- [RobinNotBad/BiliClient(Gitee)](https://gitee.com/RobinNotBad/BiliClient)
- [SpaceXC/Re-WearBili](https://github.com/SpaceXC/Re-WearBili)
- [BAC-Document](https://socialsisteryi.github.io/bilibili-API-collect/)

## 关于 ijkplayer Native 库
使用了通过 Github Actions 预先构建的 so 库。

[ijkplayer](https://github.com/bilibili/ijkplayer)

[Workflow File](https://github.com/huanli233/ijkplayer-autobuild/blob/main/.github/workflows/run_on_stared.yml)

[使用的构建](https://github.com/huanli233/ijkplayer-autobuild/actions/runs/14823590303)