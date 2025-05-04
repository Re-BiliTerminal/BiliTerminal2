<!--suppress HtmlDeprecatedAttribute -->
<div align="center">

# BiliTerminal2

轻量的第三方B站Android客户端

Fork of [RobinNotBad/BiliClient](https://gitee.com/RobinNotBad/BiliClient)

</div>

# 介绍
这是一个**极其轻量级**的**B站客户端**，[哔哩终端](https://gitee.com/RobinNotBad/BiliClient) 的 Fork。最低支持**安卓4.2**。本项目借鉴了 [WearBili](https://github.com/SpaceXC/WearBili) 和 [腕上哔哩](https://github.com/luern0313/WristBilibili) 的部分开源代码和它们收集的部分 API ，界面曾使用 [WearBili](https://github.com/SpaceXC/WearBili) 的布局。**本项目与 WearBili 无任何关系**。

播放视频可选择使用内置播放器、小电视播放器或凉腕播放器，内置播放器会优先支持部分功能。

# 关于此分支
此分支试图重写大部分代码，以增强原项目的可读性和维护性。同时，使用 ``Material3`` 并改进界面观感和操作逻辑。

使用了 ``Kotlin`` 、`Okhttp3 + Retrofit2` 、部分 ``Android Jetpack`` 组件和一些其他库。

~~其实本来还想同时支持原主题和 ``Material3`` 主题，但是被控件适配问题击败了~~

# 问题反馈
***不要向原项目反馈该分支的问题。***

由于对项目的大幅度更改，可能会导致对于 Android 5.0 以下的兼容性出现一些问题。

开发者没有条件进行测试，所以如果你遇到了问题，请提交 Issue。

如果你有好的建议，欢迎提交 Issue 或 Pull Request。

> [!IMPORTANT]
> 注意：开发者可能只会处理此仓库的 Issues 中提出的问题，通过其他任何渠道反馈的问题都不保证会有回复。

# 关于 ijkplayer Native 库
使用了通过 Github Actions 预先构建的 so 库。

[ijkplayer](https://github.com/bilibili/ijkplayer)

[Workflow File](https://github.com/huanli233/ijkplayer-autobuild/blob/main/.github/workflows/run_on_stared.yml)

[使用的构建](https://github.com/huanli233/ijkplayer-autobuild/actions/runs/14823590303)