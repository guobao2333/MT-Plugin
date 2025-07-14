# MT Plugin

适用于[MT管理器](https://mt2.cn)的插件，为MT添加更多扩展功能。
> MT目前仅支持翻译接口，所以只有翻译插件。

基于官方项目修改而来，但由于修改的太多~~*面目全非了*~~，所以几乎可以算脱离了吧 (?)

## Develop | 开发
按照下列方式执行后，将自动打包文件到 `outputs/` 这个目录中。

### Android Studio (Win/Mac/Linux)
在 Android Studio 上方选择 `BuildPlugin` 并点击运行按钮，也可以在项目根目录运行 `./gradlew buildPlugin`

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
