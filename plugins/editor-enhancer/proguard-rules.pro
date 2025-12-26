# 如果要发布插件到MT的插件中心，请保留此配置以便审核
-dontobfuscate

-keepattributes SourceFile,LineNumberTable,*Annotation*

-keep class * extends bin.mt.plugin.api.** { <init>(...); }
-keep class * implements bin.mt.plugin.api.** { <init>(...); }
