# yt-dlp
-keep class com.yausername.** { *; }
-dontwarn com.yausername.**

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule