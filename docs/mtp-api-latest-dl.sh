#!/bin/bash

# 下载项
components=(
"api"
#"pusher"
#"packer"
)

# 本地版本
api_version= #3.0.0-alpha4
pusher_version=
packer_version=

# 检测到新版是否移除旧版
delete_previous_version=true

script_path=$0
args=$@
args=${args:=-sS}

dl() {
  local component=$1
  local version_var=$2
  eval "local current=\$$version_var"
  local current=${current:=None}
  local latest=$(curl -sS https://maven.mt2.cn/bin/mt/plugin/$component/maven-metadata.xml | grep -oP '(?<=<latest>).*(?=</latest>)')

  if [[ $current != $latest ]]; then
    sed -i "/$version_var=/c $version_var=$latest" "$script_path"
    echo "检测到新版本: $latest (旧版本: $current)"
  elif [[ -f $component-$latest-sources.jar ]]; then
    echo "$component 已是最新版，跳过下载"
    return 0
  fi

  if [[ "$delete_previous_version" == "true" && -n "$current" && "$current" != "$latest" ]]; then
    rm -f "$component-$current-sources.jar"
    echo "已删除旧版本: $component-$current"
  fi

  echo "正在下载 $component 版本: $latest"
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
