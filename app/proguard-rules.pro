-keep,allowoptimization,allowobfuscation public class * extends io.github.libxposed.api.XposedModule {
    public <init>();
}

# Keep the module entry and its framework callbacks.
-keep class com.thurscvc.perappvolume.ModuleMain { *; }

# libxposed-service is accessed through its interface at runtime.
# Without these rules R8 removes the listener interface from App, and the
# module UI stays in the "未激活" state in release builds.
-keep class io.github.libxposed.service.** { *; }
-keep interface io.github.libxposed.service.XposedServiceHelper$OnServiceListener { *; }
-keep class * implements io.github.libxposed.service.XposedServiceHelper$OnServiceListener { *; }
-keepclassmembers class * implements io.github.libxposed.service.XposedServiceHelper$OnServiceListener {
    public void onServiceBind(io.github.libxposed.service.XposedService);
    public void onServiceDied(io.github.libxposed.service.XposedService);
}

-adaptresourcefilecontents META-INF/xposed/java_init.list
-dontwarn io.github.libxposed.annotation.**
