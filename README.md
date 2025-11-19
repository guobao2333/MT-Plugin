# MT Plugin [![Github](https://img.shields.io/badge/Github-%230A0A0A.svg?&style=flat-square&logo=Github&logoColor=white)](https://github.com/guobao2333/MT-Plugin)
适用于[MT管理器](https://mt2.cn)的插件，为MT添加更多扩展功能。

基于[官方项目](https://github.com/L-JINBIN/MT-Translation-Plugin)修改而来。最新[插件V3版本](https://mt2.cn/guide/plugin/introduction.html#v3-%E7%89%88%E6%9C%AC)仅发布至[gitee仓库](https://gitee.com/L-JINBIN/mt-plugin-v3-demo)但暂未同步github，所以已代码同步至[`upstream-v3`](https://github.com/guobao2333/MT-Plugin/tree/upstream-v3)分支，您可以自行前往查看。

## Develop | 开发
> [!IMPORTANT]
> 部分插件使用`v3 alpha`版本，在MT正式版中可能无法使用，推荐切换至测试版进行开发。

所有插件都放 `plugins` 这个目录下，要增加新的插件直接丢进去就行了，可以随便复制一个项目内的模块级配置，不需要动任何项目级配置。

如果不想包含某些插件模块，请重命名该模块`build.gradle`文件为其他名字。

### alpha 4
在最近更新的`v3 alpha4`版本中已经支持打包`java > 8`的插件了，可以不再依赖mt内置编译器，但插件内只能包含`.dex`而非`.java`以及`.jar`

## Prerequisites | 基本要求
1. Java 8+ (推荐Java17，可以但不推荐更旧版本)
2. Gradle 8.14 (可选，推荐最新版)
> 直接运行gradlew脚本则无需提前安装。

构建插件后将会自动打包文件到 `plugins/<plugin>/build/outputs/mt-plugin/` 这个目录中。

### Android Studio
打开项目后等待 Gradle 同步完成后运行 某个插件模块，将会有一个名为 `MT Plugin Plusher` 的应用程序安装到您的设备上，这时它会自启动并打开 MT 管理器的插件安装界面，点击安装即可。

### Command Line | 命令行
在项目根目录执行 `./gradlew :plugins:PLUGIN:packageReleaseMtp` 把`PLUGIN`替换为指定插件模块即可打包该插件。

还可以一次性打包所有插件：
```bash
./gradlew packageReleaseMtpAll
```

如果使用termux，你需要先安装`JDK`:  
`pkg install openjdk-17`

### Setup development environment | 设置开发环境
如果你是一个初学者，不知道如何设置插件的开发环境，请看[这篇教程](docs/AndroidSDK.md)。不过由于针对于termux编写，它并不能适用于其他Linux系统，您需要修改或移除其中的termux特有命令和环境路径以适用您的系统。

#### Wheres API docs | API文档在哪
API暂时处于不稳定状态，官方文档会推迟至发布稳定版时更新，但我们可以查看API接口，手机端也可以获得与PC端语法补全中相同的用法文档。

我们已经为您准备好了一个脚本，它用于快速下载官方插件API的jar包，您可以查看[脚本使用教程](docs/APIDownload.md)或<a href="https://github.com/guobao2333/MT-Plugin/raw/main/docs/mtp-api-latest-dl.sh" download>下载脚本源码</a>

## Push to device | 推送到设备
其实与Android Studio打包插件类似，只需要手动安装构建好的app并打开，会自动调用mt的插件安装界面，因此还可以在Android Studio之外的其他IDE(甚至命令行)中编译并打包插件。

## Contribute | 贡献
1. 点击上方`Fork`仓库后，修改或添加你的代码
2. 点击`Pull requests`创建新的拉取请求后根据提示进行操作。
3. 提交合并请求后，接下来请等待代码审查，如果审查结束将会合并代码。

如果合并完成，恭喜你🎉您完成了对本项目的贡献！我们由衷的感谢为每个开源项目做出贡献的人。
