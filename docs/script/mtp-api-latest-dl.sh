#!/bin/bash

# 下载项
components=(
"api" # 3.0.0-alpha4
#"gradle-plugin"
#"pusher"
#"packer"
)

script_path=$0
args=$@
args=${args:=-sSO}

dl() {
  local component=$1
  local version_var=$2
  local current=${!version_var:-None}
  local latest=$(curl -sS https://maven.mt2.cn/bin/mt/plugin/$component/maven-metadata.xml | grep -oP '(?<=<latest>).*(?=</latest>)')

  if [[ -f $component-$latest-sources.jar ]]; then
    echo "$component 已是最新版"
    return 0
  elif [[ $current != $latest ]]; then
    echo "检测到新版本: $latest"
  fi

  echo "正在下载: $component 版本: $latest"
  curl $args "https://maven.mt2.cn/bin/mt/plugin/$component/$latest/$component-$latest-sources.jar"

  if [[ $? -eq 0 ]]; then
    echo "下载完成: $component-$latest-sources.jar"
  else
    echo "下载 $component 失败"
    return 1
  fi
}

for component in "${components[@]}"; do
  # 跳过空行和注释行
  if [[ -z "$component" || "$component" =~ ^[[:space:]]*\# ]]; then
    continue
  fi
  # 去除多余空格
  component=$(echo "$component" | xargs)

  version_var="${component}_version"
  dl "$component" "$version_var"
done
