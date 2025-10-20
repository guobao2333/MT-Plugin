#!/bin/bash

if [ "$1" = "-h" ]; then
    echo "欢迎使用自动设置Android SDK CLI Tool的脚本 (by 是果宝呐)"
    echo "以下是可用参数："
    echo "\t\t[DOWNLOAD_DIR: path]\t\t可选项，命令行工具的存放路径，默认'/sdcard/Download'"
    exit 0
else
    # 设置下载的文件目录
    DOWNLOAD_DIR=${1:-"/sdcard/Download"}
fi


########### 初始化 ###########
echo "如果你已经授予存储权限可以输入n"
termux-setup-storage
echo "正在初始化依赖..."
pkg update -y && pkg upgrade -y
pkg install -y curl unzip git openjdk-17 aapt2

if [ ! -d "$DOWNLOAD_DIR" ]; then
  echo "错误：无法访问 $DOWNLOAD_DIR"
  exit 1
fi

######## 下载CLI tool #########
# 技术有限，还请自行手动下载
# URL="https://developer.android.com/studio#command-line-tools-only"
# URL_CN="https://developer.android.google.cn/studio#command-line-tools-only"

########## 设置shell ##########
echo "请选择你的Shell以准确配置："
echo "1) Ash"
echo "2) Bash (默认)"
echo "3) Dash"
echo "4) Fish"
echo "5) Zsh"
echo "6) 其他"
read -p "输入数字选择: " shell_choice

case "$shell_choice" in
    1)
        shellrc=".ashrc"
        ;;
    2|"")
        shellrc=".bashrc"
        ;;
    3)
        shellrc=".dashrc"
        ;;
    4)
        shellrc=".fishrc"
        ;;
    5)
        shellrc=".zshrc"
        ;;
    6)
        read -p "请输入你的Shell配置文件（小写，例如 .bashrc）: " custom_shell
        shellrc="${custom_shell:-.bashrc}"  # 默认使用.bashrc
        ;;
    *)
        echo "无效操作，默认使用Bash"
        shellrc=".bashrc"
        ;;
esac


######## 安装CLI tool #########
echo "正在解压Android SDK..."
ANDROID_SDK_DIR="$HOME/android_sdk"

ZIP_FILE=$(ls "$DOWNLOAD_DIR"/commandlinetools-linux-*.zip | head -1)
if [ -z "$ZIP_FILE" ]; then
  echo -e "错误：未找到压缩包，请前往国区官网 \033[4mhttps://developer.android.google.cn/studio#command-line-tools-only\033[0m 或外区官网 \033[4mhttps://developer.android.com/studio#command-line-tools-only\033[0m 下载Linux版本压缩包"
  exit 1
fi
unzip "$ZIP_FILE" -d "$ANDROID_SDK_DIR"
if [ $? -ne 0 ]; then
  echo "错误：解压失败！"
  exit 1
fi
rm -v "$ZIP_FILE"
echo "解压完成！已删除压缩包，正在移动文件..."

mkdir -p "$ANDROID_SDK_DIR/cmdline-tools/latest"
shopt -s extglob
mv "$ANDROID_SDK_DIR"/cmdline-tools/!(latest) "$ANDROID_SDK_DIR/cmdline-tools/latest"


######### 设置环境变量 #########
echo "正在设置环境变量..."

# 临时导出
export ANDROID_HOME="$ANDROID_SDK_DIR"
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin"
# 输出配置
echo 'export ANDROID_HOME="$HOME/android_sdk"' >> "$HOME/$shellrc"
echo 'export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools' >> "$HOME/$shellrc"
echo "\n# 覆盖 AAPT2 路径" >> ./gradle.properties
echo "android.aapt2FromMavenOverride=$PREFIX/bin/aapt2\n" >> ./gradle.properties

echo "同意所有许可并安装平台工具..."
chmod -R 755 $ANDROID_HOME
yes | sdkmanager --licenses && sdkmanager "platform-tools"

# 安装adb
echo "需要安装adb工具吗？[y/N]"
read -r adbtool
if [[ "$adbtool" =~ ^[Yy]$ ]]; then
    pkg install -y android-tools
fi

# 创建local.properties
echo "是否创建local.properties文件？[y/N]"
read -r answer
if [[ "$answer" =~ ^[Yy]$ ]]; then
    echo "输入文件保存路径（默认为当前目录）："
    read -r path
    if [ -z "$path" ]; then
        path="."
    fi
    mkdir -p "$path"
    echo "sdk.dir=$ANDROID_SDK_DIR" > "$path/local.properties"
    echo "已创建 $path/local.properties 文件"
fi

echo "Android SDK 安装完成！位于：$ANDROID_SDK_DIR"
echo "请重启终端或执行 source ~/$shellrc 使配置生效"
