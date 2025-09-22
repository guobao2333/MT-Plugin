# MT Plugin[![Repo](https://img.shields.io/badge/Github-%230A0A0A.svg?&style=flat-square&logo=Github&logoColor=white)](https://github.com/guobao2333/MT-Plugin)
适用于[MT管理器](https://mt2.cn)的插件，为MT添加更多扩展功能。

基于[官方项目](https://github.com/L-JINBIN/MT-Translation-Plugin)修改而来。最新[插件V3版本](https://mt2.cn/guide/plugin/introduction.html#v3-%E7%89%88%E6%9C%AC)仅发布至[gitee仓库](https://gitee.com/L-JINBIN/mt-plugin-v3-demo)但暂未同步github。由于官方项目打包逻辑已变，且几乎无法自定义，所以我已将代码同步至[`upstream-v3`](https://github.com/guobao2333/MT-Plugin/tree/upstream-v3)分支来进一步开发，您可以自行前往查看。

## Develop | 开发
> [!IMPORTANT]
> 部分插件使用api-v3-alpha版本，在MT正式版中可能无法使用，推荐切换至测试版进行开发。

所有插件都放 `plugins` 这个目录下，要增加新的插件直接丢进去就行了，可以随便复制一个项目内的模块级配置，不需要动任何项目级配置。

## Prerequisites | 基本要求
1. Java 8+ (可以但不推荐更旧版本)
2. (可选) Gradle 8.14 (推荐最新版)
> 直接运行gradlew脚本则不需要提前安装

构建插件后将会自动打包文件到 `outputs/` 这个目录中。

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

#### Setup termux development environment | termux开发环境
如果你是一个初学者，不知道如何设置插件的开发环境，请看[这篇教程](docs/AndroidSDK.md)。不过由于针对于termux编写，它并不能适用于其他Linux系统，您需要修改其中termux的特有命令和环境路径以适用您的系统。

#### Wheres API docs | API文档在哪
截止9月22日，官方文档还没有更新，但我们可以通过直接查看API接口源码的方式，在手机端也可以获得与PC端语法补全中相同的用法文档。

我们已经为您准备好了一个脚本，它用于快速下载官方插件API的jar包，您可以[在此查看](docs/APIDownload.md)使用教程或下载代码。

## Push to device | 推送到设备

你需要修改项目根目录下的`config.json`配置文件，根据配置的字面意思理解即可。
> 后续会补充关于配置的文档，烦请稍安勿躁。  
> 或许会改为v3版本仓库的推送方式？

## Contribute | 贡献
1. 点击上方`Fork`仓库后，修改或添加你的代码
2. 点击`Pull requests`创建新的拉取请求后根据提示进行操作。
3. 提交合并请求后，接下来请等待代码审查，如果审查结束将会合并代码。

如果合并完成，恭喜你🎉您完成了对本项目的贡献！我们由衷的感谢为每个开源项目做出贡献的人。
