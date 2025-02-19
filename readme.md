<div align="center">

# BiliTerminal2

轻量的第三方B站Android客户端

Fork of [RobinNotBad/BiliClient](https://gitee.com/RobinNotBad/BiliClient)

</div>

# 介绍
这是一个**极其轻量级**的**B站客户端**，[哔哩终端](https://gitee.com/RobinNotBad/BiliClient)的 Fork。使用 `java` + `xml`开发，最低支持**安卓4.2**。本项目借鉴了 [WearBili](https://github.com/SpaceXC/WearBili) 和 [腕上哔哩](https://github.com/luern0313/WristBilibili) 的部分开源代码和它们收集的部分 API ，界面曾使用 [WearBili](https://github.com/SpaceXC/WearBili) 的布局（现已重做）。**本项目与 WearBili 无任何关系**。

播放视频可选择使用内置播放器、小电视播放器或凉腕播放器，内置播放器会优先支持部分功能。

### 为啥不是在那两位前辈的基础上改？

- [腕上哔哩](https://github.com/luern0313/WristBilibili) 的开源代码**不完整**，它的数据处理部分多处用到 luern 自己的 **Lson** 库，然而 Github 上的版本似乎不管用。
- [WearBili](https://github.com/SpaceXC/WearBili) 的界面确实好看，但是体积大、在许多手表上卡顿严重，而且**仅支持安卓7.1**以上，~~最重要的是 Robin 看不懂 kotlin~~。

# 问题反馈

由于对项目的大幅度更改，可能会导致对于 Android 5.0 以下的兼容性出现一些问题。

开发者没有条件进行测试，所以如果你遇到了问题，请提交 Issue。

如果你有好的建议，欢迎提交 Issue 或 Pull Request。

> 注意：开发者可能只会处理此仓库的 Issues 中提出的问题，通过其他任何渠道反馈的问题都不保证会有回复。