# MT Plugin

适用于[MT管理器](https://mt2.cn)的插件，为MT添加更多扩展功能。
> MT目前仅支持翻译接口，所以只有翻译插件。

基于[官方项目](https://github.com/L-JINBIN/MT-Translation-Plugin)修改而来，但由于修改的太多 ~~*已经面目全非了*~~，所以几乎可以算脱离了吧 (?)

## Develop | 开发
> [!IMPORTANT]
> 有时插件代码在MT正式版中无法使用，推荐切换至测试版。

所有插件都放 `plugins` 这个目录下，要增加新的插件直接丢进去就行了，可以随便复制一个项目内的模块级配置，不需要动任何项目级配置。

## Prerequisites | 基本要求
1. Java 8 (可以但不推荐更旧版本)
2. Gradle 14 (按需选择，可降级)

按照以下方式运行命令后，将自动打包文件到 `outputs/` 这个目录中。

### Android Studio
在 Android Studio 上方选择 `BuildPlugin` 并点击运行按钮，也可以在项目根目录运行
```bash
./gradlew buildPlugin
```

### termux (Android)
你需要安装依赖 `pkg install openjdk-17` 并在项目根目录运行：
```bash
chmod +x gradlew
./gradlew buildPlugin
```

## Contribute | 贡献
1. 点击上方`fork`仓库后，修改或添加你的代码
2. 点击`Pull requests`创建新的拉取请求后根据提示进行操作。
3. 提交合并请求后，接下来请等待代码审查，如果审查结束将会合并代码。

如果合并完成，恭喜你🎉您完成了对本项目的贡献！我们由衷的感谢为每个开源项目做出贡献的人。
