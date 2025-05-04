<!--suppress HtmlDeprecatedAttribute -->
<div align="center">

# BiliTerminal2

轻量的第三方B站Android客户端

Fork of [RobinNotBad/BiliClient](https://gitee.com/RobinNotBad/BiliClient)

</div>
 
# 介绍
这是一个**极其轻量级**的**B站客户端**，[哔哩终端](https://gitee.com/RobinNotBad/BiliClient) 的 Fork。使用 `java` + `xml`开发，最低支持**安卓4.2**。本项目借鉴了 [WearBili](https://github.com/SpaceXC/WearBili) 和 [腕上哔哩](https://github.com/luern0313/WristBilibili) 的部分开源代码和它们收集的部分 API ，界面曾使用 [WearBili](https://github.com/SpaceXC/WearBili) 的布局（现已重做）。**本项目与 WearBili 无任何关系**。

播放视频可选择使用内置播放器、小电视播放器或凉腕播放器，内置播放器会优先支持部分功能。

# 为什么创建此分支
由于原项目的代码存在过多影响可维护性的问题，且原开发者不愿引入一些优化代码规范、简化开发的库如 `Gson`、`Lombok` 等，也没有时间大幅度重构项目，故创建此分支，以实现在**保留对低版本 Android 兼容性**的同时使项目**更加规范，更易于维护**。

> [!TIP]
> 目前（发布前），此项目的开发仍然以用新代码重写原有功能为主，这意味着暂时不会添加过多新功能、特性。

## 有什么不同
- 完全重写 API 部分，使用 `Retrofit` + `Gson` （使用了兼容到 **SDK 14** 的修改版库`okhttp3-compat`与`retrofit2-compat`）
- 引入 `Kotlin` ，并将大部分代码迁移到 `Kotlin`
- 对项目包、类文件结构进行优化
- 优化代码复用，减少原项目中复制粘贴的大量重复逻辑
- 去除不规范的注释与日志，引入日志框架库
- 去除 **AppApi（如检查更新、公告）** 等
- 优化部分界面的逻辑与一些细节上的处理

（以及可能添加的更多功能）

# 问题反馈
由于对项目的大幅度更改，可能会导致对于 Android 5.0 以下的兼容性出现一些问题。

开发者没有条件进行测试，所以如果你遇到了问题，请提交 Issue。

如果你有好的建议，欢迎提交 Issue 或 Pull Request。

> [!IMPORTANT]
> 注意：开发者可能只会处理此仓库的 Issues 中提出的问题，通过其他任何渠道反馈的问题都不保证会有回复。
