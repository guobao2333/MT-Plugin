# API Jar Download Script
## Whats Its Use | 有啥用
它是一个用来下载插件接口jar的shell脚本，jar中包含了api的使用方式和(部分)实现，您可以通过它更好的开发MT插件。
- [查看脚本代码](mtp-api-latest-dl.sh) 或 <a href="https://github.com/guobao2333/MT-Plugin/raw/main/docs/mtp-api-latest-dl.sh" download>下载脚本</a>
> 如果没有自动触发浏览器下载，请在github中点击下载按钮

脚本实现较为简陋，如果您有更好的想法，欢迎帮助我们改进它！

## How To Use | 怎么用
```bash
curl -O "https://raw.githubusercontent.com/guobao2333/MT-Plugin/main/docs/mtp-api-latest-dl.sh" && bash mtp-api-latest-dl.sh
```

原始逻辑可能不适合所有人，您可以修改为满意的方式运行。

### Script Parameters | 脚本参数
原始脚本支持传入curl的参数以供调试。

curl参数插入位置：  
`curl [Params] URL`

```bash
Use:
  bash mtp-api-latest-dl.sh [Params]

Params:
  -s      不显示进度静默输出
  -sSO     (默认) 静默输出并显示错误信息

更多参数请查看curl程序或其官方文档。
```
