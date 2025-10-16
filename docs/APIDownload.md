# API Jar Download Script
## Whats Its Use | 有啥用
它是一个用来下载插件接口jar的shell脚本，jar中包含了api的使用方式和(部分)实现，您可以通过它更好的开发MT插件。在[此处](mtp-api-latest-dl.sh?raw=true)查看脚本代码。

脚本实现较为简陋，如果您有更好的想法，欢迎帮助我们改进它！

## How To Use | 怎么用
```bash
curl -O "https://raw.githubusercontent.com/guobao2333/MT-Plugin/main/docs/mtp-api-latest-dl.sh" -o "mtp-api-latest-dl.sh" && bash mtp-api-latest-dl.sh -O
```

我们编写的逻辑可能不适合所有人，您可以修改为您满意的方式运行。

### Script Parameters | 脚本参数
原始脚本支持传入curl的参数以供调试。

curl参数插入位置：  
`curl [Params] URL`

```bash
Use:
  bash mtp-api-latest-dl.sh [Params]

Params:
  -O      显示下载进度
  -s      静默输出
  -sS     (默认) 静默输出显示错误信息

更多参数请查看curl程序或其官方文档。
```
