# Install Android SDK in termux
我们正在改进本教程以便新手更易理解，欢迎贡献或提问以帮助我们改进！

## Automatically setup | 自动设置
如果你不想一步一步来，这里提供一个一键设置脚本，您可以[查看源代码](script/install-android-sdk.sh) 或 <a href="https://raw.githubusercontent.com/guobao2333/MT-Plugin/main/docs/script/install-android-sdk.sh" target="_blank">下载脚本</a>。它完全按照本文档中的手动设置步骤运行，所以您得到的效果几乎与手动设置一样！  
我们正在改进它以便更好的在新环境中自动设置，欢迎贡献代码或报告问题来帮助我们改进！

您还可以通过下面的命令一键运行它，脚本运行失败时请检查错误信息，如果有未知错误信息请在`Issues`中报告。
```bash
curl -s "https://raw.githubusercontent.com/guobao2333/MT-Plugin/main/docs/script/install-android-sdk.sh" | bash
```
💡添加参数`-h`可查看帮助

## Manually setup in termux | 在termux中手动设置
0. 在开始之前你需要授予权限并安装依赖：
   ```bash
   termux-setup-storage
   pkg update && pkg upgrade
   pkg install unzip git openjdk-17 aapt2
   ```

1. 首先下载[命令行工具](https://developer.android.google.cn/studio?hl=zh-cn#command-line-tools-only)(Android Studio CLI Tool)  
   请根据网络环境自行选择访问地址：
   | 全球 | 大陆 |
   | :---: | :---: |
   | <https://developer.android.com/studio#command-line-tools-only> | <https://developer.android.google.cn/studio#command-line-tools-only> |
   > [!NOTE]
   > 如果访问后没看到下载链接，请继续下滑页面，应该在页面接近底部的部分可以找到。

2. 接下来解压并删除压缩包，以下命令中的压缩包位于`/sdcard/Download/`，将SDK的根目录设置在`~/android_sdk/`其等同于`/data/data/com.termux/files/home/android_sdk/`
   ```bash
   unzip -o /sdcard/Download/commandlinetools-linux-*.zip -d ~/android_sdk && rm -v "$(ls /sdcard/Download/commandlinetools-linux-*.zip | head -1)"
   mv ~/android_sdk/cmdline-tools ~/android_sdk/latest
   mkdir -p ~/android_sdk/cmdline-tools
   mv ~/android_sdk/latest ~/android_sdk/cmdline-tools/latest
   ```

3. 然后把sdk路径写入配置
   ```bash
   echo '\nexport ANDROID_HOME=$HOME/android_sdk' >> ~/.bashrc
   echo '\nexport PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools' >> ~/.bashrc
   ```
> [!IMPORTANT]
> 本文档默认您的Shell为`Bash`，如果您使用其他Shell程序，请自行替换`.bashrc`

4. 重载配置
   | Bash | Zsh |
   | :---: | :---: |
   | `source ~/.bashrc` | `source ~/.zshrc` |
5. 修改权限 `chmod -R 755 $ANDROID_HOME`
6. 同意所有许可并安装平台工具和Android 16 SDK 
   ```bash
   yes | sdkmanager --licenses
   sdkmanager "platform-tools" "platforms;android-36"
   ```

adb相关工具可以直接安装使用： `pkg install android-tools`

如果需要 `local.properties` 文件：
```bash
echo "sdk.dir=$ANDROID_HOME" > local.properties
```

### 关于 AAPT2
在 Termux 中构建 Android 项目时，Gradle 自动下载的 AAPT2 可能因架构不兼容而报错。建议在全局 Gradle 配置中添加覆盖：
```bash
echo 'android.aapt2FromMavenOverride=/data/data/com.termux/files/usr/bin/aapt2\n' >> ~/.gradle/gradle.properties
```
> 写入全局 `~/.gradle/gradle.properties` 而非项目级文件，可以避免在每个项目中重复配置。

> [!note]
> 若遇到 `AAPT2 aapt2-*-linux Daemon: Unexpected error`，请确认已添加上面的 `android.aapt2FromMavenOverride` 配置。
