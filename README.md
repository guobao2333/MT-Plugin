# MT Plugin
<p align="center">
<a href="https://github.com/guobao2333/MT-Plugin"><img alt="Repository" src="https://img.shields.io/badge/Github-%230A0A0A.svg?&style=flat-square&logo=Github&logoColor=white"/></a>
</p>

适用于[MT管理器](https://mt2.cn)的插件，为MT添加更多扩展功能。
> MT目前仅支持翻译接口，所以只有翻译插件。  
> 好消息是 MT 2.19.1 提供了更多接口，但目前处于开发状态，正式版暂时无福消受 |･_･`)

基于[官方项目](https://github.com/L-JINBIN/MT-Translation-Plugin)修改而来。最新[插件V3版本](https://mt2.cn/guide/plugin/introduction.html#v3-%E7%89%88%E6%9C%AC)仅发布至gitee但暂未同步github，由于打包逻辑已变，且几乎无法自定义，所以暂不考虑同步。

## Develop | 开发
> [!IMPORTANT]
> 部分插件使用V3版本，目前在MT正式版(2.19.0)中无法使用，推荐切换至测试版进行开发。

所有插件都放 `plugins` 这个目录下，要增加新的插件直接丢进去就行了，可以随便复制一个项目内的模块级配置，不需要动任何项目级配置。

## Prerequisites | 基本要求
1. Java 8 (可以但不推荐更旧版本)
2. Gradle 14 (按需选择，推荐最新版)

按照以下方式运行命令后，将自动打包文件到 `outputs/` 这个目录中。

### Android Studio
找到 `BuildPlugin` 并点击运行按钮，也可以在项目根目录运行：
```bash
./gradlew buildPlugin
```

### termux (Android)
你需要安装依赖 `pkg install openjdk-17` 并在项目根目录运行：
```bash
chmod +x gradlew
./gradlew buildPlugin
```

#### Setup termux development environment
如果你是一个初学者，不知道如何为你的termux设置插件的开发环境，请看[这篇教程](docs/AndroidSDK.md)。不过由于部分命令的局限性，它并不能适用于其他Linux系统，您需要修改其中termux的特有命令和环境路径以适用您的系统。

## Contribute | 贡献
1. 点击上方`fork`仓库后，修改或添加你的代码
2. 点击`Pull requests`创建新的拉取请求后根据提示进行操作。
3. 提交合并请求后，接下来请等待代码审查，如果审查结束将会合并代码。

如果合并完成，恭喜你🎉您完成了对本项目的贡献！我们由衷的感谢为每个开源项目做出贡献的人。
