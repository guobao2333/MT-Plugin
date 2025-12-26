<h1 align="center"><b>MT Plugin</b></h1>
<p align="center">
  适用于<a href="https://mt2.cn">MT管理器</a>的插件(<a href="https://mt2.cn/guide/plugin/introduction.html">.mtp</a>)，为MT添加更多扩展功能。<br>
  Plugins (.mtp) for <a href="https://mt2.cn">MT Manager</a> application
</p>

<p align="center">
<a href="https://github.com/guobao2333/MT-Plugin"><img alt="Github" src="https://img.shields.io/badge/Github-%230A0A0A.svg?&style=flat-square&logo=Github&logoColor=white"/></a>
<a href="https://github.com/guobao2333/MT-Plugin/LICENSE"><img alt="License" src="https://img.shields.io/github/license/guobao2333/MT-syntax-highlight?style=flat&logo=apache&label=Licence&color=blue"></a>

基于[官方项目](https://github.com/L-JINBIN/MT-Translation-Plugin)修改而来。虽然最新[插件V3版本demo](https://mt2.cn/guide/plugin/introduction.html#v3-%E7%89%88%E6%9C%AC)仅发布至[gitee仓库](https://gitee.com/L-JINBIN/mt-plugin-v3-demo)，不过我已将代码同步至[`upstream-v3`](https://github.com/guobao2333/MT-Plugin/tree/upstream-v3)分支，您可以切换分支查看。

## Develop | 开发
> [!IMPORTANT]
> 部分插件使用`v3 alpha`版本，在MT正式版中可能无法使用，推荐切换至测试版进行开发。

所有插件都放 `plugins` 这个目录下，要增加新的插件直接丢进去就行了，可以随便复制一个项目内的模块级配置，不需要动任何项目级配置。

如果不想包含某些插件模块，请将该模块`build.gradle`文件重命名为其他名字。

## Prerequisites | 基本要求
1. Java 8+ (推荐Java 17+，可以但不推荐更旧版本)
   > 官方推荐 Java 11+
2. Gradle 8.14 (可选，推荐最新版)
   > 直接运行gradlew脚本则无需提前安装。
3. Kotlin (可选，但只能使用**dex模式**打包)

> [!IMPORTANT]
> 项目要求`Java 17+`是为了使用很多高级特性，通过AGP语法脱糖(dex模式)能够使用java8以上的语法。脱糖相关配置已包含在项目中，无需修改开箱即用。

构建插件后将会自动打包文件到 `plugins/<plugin>/build/outputs/mt-plugin/` 这个目录中。

### Android Studio
打开项目后等待 Gradle 同步完成后运行 某个插件模块，将会有一个名为 `MT Plugin Pusher` 的应用程序安装到您的设备上，这时它会自启动并打开MT管理器的插件安装界面，点击安装即可。

### Command Line | 命令行
在项目根目录执行 `./gradlew :plugins:PLUGIN:packageReleaseMtp` 把`PLUGIN`替换为指定插件模块即可打包该插件。

还可以一次性打包所有插件：
```bash
./gradlew packageReleaseMtpAll
```

如果使用termux，你需要先安装`JDK`:  
```bash
pkg install openjdk-17
```

### Setup development environment | 设置开发环境
如果你是一个初学者，不知道如何设置插件的开发环境，请看[这篇教程](docs/AndroidSDK.md)。不过由于针对于termux编写，它并不能适用于其他Linux系统，您需要修改或移除其中的termux特有命令和环境路径以适用您的系统。

### API development docs | API开发文档
APIv3已经迎来了官方文档，您可以[点此](https://mt2.cn/guide/pluginv3/plugin-intro.html)前往查看。由于API暂时处于不稳定状态，文档随时都可能改变，建议您以API版本号或特定版本jar包来确定更新情况。

我们已经为您准备好了一个脚本，它用于快速下载官方插件API的jar包，您可以查看[脚本使用教程](docs/APIDownload.md)或<a href="https://github.com/guobao2333/MT-Plugin/raw/main/docs/mtp-api-latest-dl.sh" download>下载脚本源码</a>

## Push to device | 推送到设备
其实与Android Studio打包插件类似，只需要手动安装构建好的app并打开，会自动调用mt的插件安装界面，因此还可以在Android Studio之外的其他IDE(甚至命令行)中编译并打包插件。

## Contribute | 贡献
1. 点击上方`Fork`仓库后，修改或添加你的代码
2. 点击`Pull requests`创建新的拉取请求后根据提示进行操作。
3. 提交合并请求后，接下来请等待代码审查，如果审查结束将会合并代码。

如果合并完成，恭喜你🎉您完成了对本项目的贡献！我们由衷的感谢为每个开源项目做出贡献的人。
