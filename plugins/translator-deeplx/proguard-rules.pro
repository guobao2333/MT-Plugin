-keep class * extends bin.mt.plugin.api.** { <init>(...); }
-keep class * implements bin.mt.plugin.api.** { <init>(...); }

-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile

-keepattributes *Annotation*, Signature, InnerClasses

-keep class com.fasterxml.jackson.databind.ObjectMapper {
    <init>();
}
-keep class com.fasterxml.jackson.core.JsonFactory {
    <init>();
}