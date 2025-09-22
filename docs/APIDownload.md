# API Jar Download Script
## Whats Its Use | 它是干嘛的
正如主要文档中描述的那样，它用来下载源码jar，其中包含了api的使用方式和(部分)实现，您可以通过它更好的开发MT插件。在[此处](mtp-api-latest-dl.sh)查看脚本代码。

脚本实现较为简陋，如果您有更好的想法，欢迎帮助我们改进它！

## How To Use | 怎么用
有两种使用方式，可根据您的网络环境自由选择。
1. 直接运行 (推荐)

一般这种方式GFW是没有封锁的，所以更推荐使用此方式。
```bash
curl -s "https://raw.githubusercontent.com/guobao2333/MT-Plugin/main/docs/mtp-api-latest-dl.sh" | bash
```
2. 本地运行脚本

我们编写的逻辑可能不适合所有人，您可以下载[脚本](mtp-api-latest-dl.sh)修改或以您满意的方式运行。
```bash
./mtp-api-latest-dl.sh -O
```

### Script Parameters | 脚本参数
原始脚本支持传入curl的参数以供调试。

参数插入位置：
curl *`Params`* link

```bash
Use:
  bash mtp-api-latest-dl.sh [CurlParams]

Example:
  -O      显示下载进度条
  -s      静默输出
  -sS     (默认) 静默输出显示错误信息

更多参数请查看curl程序或其官方文档。
```
