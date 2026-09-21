#!/bin/bash

echo "欢迎使用自动设置Android SDK CLI Tool的脚本 (by 是果宝呐)"

if [ "$1" = "-h" ]; then
    echo "以下是可用参数："
    echo "\t\t[DOWNLOAD_DIR: path]\t\t可选项，命令行工具的存放路径，默认'~/storage/downloads'"
    exit 0
else
    # 设置下载的文件目录
    DOWNLOAD_DIR=${1:-"$HOME/storage/downloads"}
fi

echo "请确保在您的项目根目录中运行该脚本，以便完成配置，否则可能会导致无关文件损坏！"

########### 初始化 ###########
read -rp "已授权存储权限输入 n 跳过，或回车授权: " skip
[ "$skip" != "n" ] && termux-setup-storage
echo "正在初始化依赖..."
pkg install -y curl unzip git openjdk-21 aapt2 || {
  echo "错误：依赖安装失败"
  exit 1
}

########## 设置shell ##########
echo "请选择你的Shell以寻找配置："
echo "0) Bash (默认)"
echo "1) Zsh"
echo "2) 其他"
read -p "输入数字选择: " shell_choice

case "$shell_choice" in
    0|"")
        shellrc=".bashrc"
        ;;
    1)
        shellrc=".zshrc"
        ;;
    2)
        read -p "请输入你的Shell配置文件（小写，例如 .bashrc）: " custom_shell
        shellrc="${custom_shell:-.bashrc}"  # 默认值.bashrc
        ;;
    *)
        echo "无效操作，使用默认shell(.bashrc)"
        shellrc=".bashrc"
        ;;
esac


######## 安装CLI tool #########
REPO_BASE="https://dl.google.com/android/repository"
REPO_XML="$REPO_BASE/repository2-1.xml"

echo "正在获取 CLI tool 最新版本..."
mkdir -p "$DOWNLOAD_DIR" || exit 1
[ -r "$DOWNLOAD_DIR" ] && [ -w "$DOWNLOAD_DIR" ] || {
  echo "错误：无法访问 $DOWNLOAD_DIR"
  exit 1
}

CLI_ZIP_NAME="$(
  curl -fsSL "$REPO_XML" \
    | grep -oE 'commandlinetools-linux-[0-9]+_latest\.zip' \
    | sort -u \
    | awk -F'[-_]' '{print $3, $0}' \
    | sort -n \
    | tail -n1 \
    | cut -d' ' -f2-
)"

if [ -z "$CLI_ZIP_NAME" ]; then
  echo "错误：无法从 Google 仓库获取 CLI tool 链接。"
  echo "请检查网络，或手动下载后放到 $DOWNLOAD_DIR"
  exit 1
fi

ZIP_FILE="$DOWNLOAD_DIR/$CLI_ZIP_NAME"
CLI_ZIP_URL="$REPO_BASE/$CLI_ZIP_NAME"

if [ -f "$ZIP_FILE" ]; then
  echo "压缩包已存在，跳过下载：$ZIP_FILE"
else
  echo "正在下载到 $ZIP_FILE ..."
  curl -fL --retry 3 --connect-timeout 15 -o "$ZIP_FILE" "$CLI_ZIP_URL"
fi

if [ ! -s "$ZIP_FILE" ]; then
  echo "错误：下载失败或文件为空。"
  echo "如果您频繁遇到该问题，请在Issues中向维护者反馈。"
  exit 1
fi

if ! unzip -t "$ZIP_FILE" >/dev/null 2>&1; then
  echo "错误：压缩包损坏，请重新下载：$ZIP_FILE"
  exit 1
fi

echo "下载完成：$ZIP_FILE"

######## 安装CLI tool #########
echo "正在解压Android SDK..."
ANDROID_SDK_DIR="$HOME/android_sdk"

if [ ! -f "$ZIP_FILE" ]; then
  echo "错误：未找到压缩包 $ZIP_FILE"
  exit 1
fi

unzip -o "$ZIP_FILE" -d "$ANDROID_SDK_DIR"
if [ $? -ne 0 ]; then
  echo "错误：解压失败！"
  exit 1
fi

rm -v "$ZIP_FILE"
echo "解压完成！正在更新文件..."

CLI_TOOL_DIR="$ANDROID_SDK_DIR/cmdline-tools/latest"
rm -rf "$CLI_TOOL_DIR"
mkdir -p "$CLI_TOOL_DIR"
shopt -s extglob
mv "$ANDROID_SDK_DIR"/cmdline-tools/!(latest) "$CLI_TOOL_DIR"


######### 设置环境变量 #########
echo "正在设置环境变量..."

# 临时导出
export ANDROID_HOME="$ANDROID_SDK_DIR"
export ANDROID_SDK_ROOT="$ANDROID_SDK_DIR"
export PATH="$PATH:$ANDROID_HOME/cmdline-tools/latest/bin"

# 输出配置
printf '\nexport ANDROID_HOME="$HOME/android_sdk"' >> "$HOME/$shellrc"
printf '\nexport ANDROID_SDK_ROOT="$ANDROID_HOME"' >> "$HOME/$shellrc"
printf '\nexport PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools' >> "$HOME/$shellrc"

# AAPT2 覆盖
GRADLE_PROPS="$HOME/.gradle/gradle.properties"
mkdir -p "$HOME/.gradle"
if ! grep -q "android.aapt2FromMavenOverride" "$GRADLE_PROPS" 2>/dev/null; then
    printf "\n# 覆盖 AAPT2 路径\n" >> "$GRADLE_PROPS"
    printf "android.aapt2FromMavenOverride=$PREFIX/bin/aapt2\n" >> "$GRADLE_PROPS"
fi

echo "同意所有许可并安装SDK..."
chmod -R 755 $ANDROID_HOME
yes | sdkmanager --sdk_root="$ANDROID_SDK_DIR" --licenses || { echo "错误：同意许可失败"; exit 1; }
sdkmanager --sdk_root="$ANDROID_SDK_DIR" "platform-tools" "platforms;android-36" || { echo "错误：SDK 安装失败"; exit 1; }

# 安装adb
echo -e "\033[4m需要安装adb工具吗？\033[0m[y/N]"
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
    printf "sdk.dir=$ANDROID_SDK_DIR\n" > "$path/local.properties"
    echo "已创建 $path/local.properties 文件"
fi

echo "Android SDK 安装完成！位于：$ANDROID_SDK_DIR"
echo "请重启终端或手动执行 source ~/$shellrc 使配置生效"
