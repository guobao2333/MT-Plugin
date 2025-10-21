# Install Android SDK in termux
我们正在改进本教程以便新手更易理解，欢迎贡献或提问以帮助我们改进！

## Automatically setup | 自动设置
如果你不想一步一步来，这里提供一个一键设置脚本，您可以在[这里](script/install-android-sdk.sh)查看源代码。它完全按照本文档中的手动设置步骤运行，所以您得到的效果几乎与手动设置一样！  
我们正在改进它以便更好的在新环境中自动设置，欢迎贡献代码或报告问题来帮助我们改进！

您还可以通过下面的命令一键运行它，不过在这之前你需要先手动下载 CLI Tool（命令行工具）  
请根据网络环境自行选择访问地址：
| 全球 | 大陆 |
| :---: | :---: |
| <https://developer.android.com/studio#command-line-tools-only> | <https://developer.android.google.cn/studio#command-line-tools-only> |
> [!NOTE]
> 如果访问后没看到下载链接，请继续下滑页面，应该在页面接近底部的部分可以找到。

接下来你可以运行脚本开始自动设置了，运行失败请检查错误信息，如果有未知错误信息请在`Issues`中报告。
```bash
curl -s "https://raw.githubusercontent.com/guobao2333/MT-Plugin/main/docs/script/install-android-sdk.sh" | bash
```
💡添加参数`-h`可查看所有参数

## Manually setup in termux | 在termux中手动设置
0. 在开始之前你需要授予权限并安装依赖：
   ```bash
   termux-setup-storage
   pkg update && pkg upgrade
   pkg install unzip git openjdk-17 aapt2
   ```

1. 首先下载[命令行工具](https://developer.android.google.cn/studio?hl=zh-cn#command-line-tools-only)
2. 接下来解压并删除压缩包，以下命令中的压缩包位于`/sdcard/Download/`，将SDK的根目录设置在`~/android_sdk/`其等同于`/data/data/com.termux/files/home/`
   ```bash
   unzip /sdcard/Download/commandlinetools-linux-*.zip -d ~/android_sdk && rm -v "$(ls /sdcard/Download/commandlinetools-linux-*.zip | head -1)"
   mv ~/android_sdk/cmdline-tools ~/android_sdk/latest
   mkdir -p ~/android_sdk/cmdline-tools
   mv ~/android_sdk/latest ~/android_sdk/cmdline-tools/latest
   ```

3. 然后把sdk路径写入配置（如果用zsh就改成`.zshrc`）
   ```bash
   echo 'export ANDROID_HOME=$HOME/android_sdk' >> ~/.bashrc
   echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools' >> ~/.bashrc
   ```

4. 重载配置
   | Bash | Zsh | Fish |
   | :---: | :---: | :---: |
   | `source ~/.bashrc` | `source ~/.zshrc` | `source ~/.fishrc`
5. 修改权限 `chmod -R 755 $ANDROID_HOME`
6. 同意所有许可并安装平台工具和Android 4.0 SDK 
   ```bash
   yes | sdkmanager --licenses
   sdkmanager "platform-tools" "platforms;android-14"
   ```

adb相关工具可以直接安装使用： `pkg install android-tools`

如果需要 `local.properties` 文件：
```bash
echo "sdk.dir=$ANDROID_HOME" > local.properties
```
