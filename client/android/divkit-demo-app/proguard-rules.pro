#-allowaccessmodification
#-dontskipnonpubliclibraryclasses
#-dontusemixedcaseclassnames
#-keepattributes *Annotation*,EnclosingMethod
#-keepattributes Signature
#-keepattributes SourceFile,LineNumberTable
#-printseeds
#-renamesourcefileattribute SourceFile
#-repackageclasses ''
#-verbose
#
#-dontwarn okhttp3.**
#-dontwarn okio.**
#-dontwarn kotlin.**
#-dontwarn javax.annotation.**
#-keep class * implements com.google.gson.TypeAdapterFactory
#-keep class * implements com.google.gson.JsonSerializer
#-keep class * implements com.google.gson.JsonDeserializer
#
#-keep class kotlin.reflect.**
#-keep class kotlin.Metadata { *; }
#-keep class kotlinx.**
#-keep class com.yandex.div.lottie.**
#-dontwarn com.yalantis.ucrop.UCrop$Options
#-dontwarn com.yalantis.ucrop.UCrop
#-dontwarn okhttp3.**
#-keep class okhttp3.**
#-keep class com.yandex.divkit.demo.** { *; }
#-keep class com.google.gson.reflect.TypeToken
#-keep class retrofit2.** { *; }
#-keep interface retrofit2.** { *; }
#-keepattributes Signature
#-keepattributes *Annotation*
#-keep class com.google.gson.** { *; }
#-keep class * implements java.io.Serializable {*; }
#-dontwarn com.google.gson.**
#-dontwarn retrofit2.**
#-dontwarn retrofit2.Platform$JAVA8
#-keepclassmembers class kotlinx.coroutines.** { *; }
#-dontwarn kotlinx.coroutines.**
#-keep class androidx.lifecycle.LiveData { *; }
#-dontwarn androidx.lifecycle.**
#-keep class java.util.Map { *; }
#-keep class java.util.HashMap { *; }
#-keepclassmembers class * {
#    @retrofit2.http.* <methods>;
#}
#-keep class com.google.gson.reflect.TypeToken
#
#-keep,allowobfuscation,allowshrinking interface retrofit2.Call
#-keep,allowobfuscation,allowshrinking class retrofit2.Response
#-dontwarn androidx.compose.animation.tooling.ComposeAnimation
#-dontwarn androidx.compose.animation.tooling.ComposeAnimationType
#-dontwarn androidx.window.extensions.WindowExtensions
#-dontwarn androidx.window.extensions.WindowExtensionsProvider
#-dontwarn androidx.window.extensions.layout.WindowLayoutComponent
#-dontwarn androidx.window.sidecar.SidecarDisplayFeature
#-dontoptimize
#-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
#    public static void checkExpressionValueIsNotNull(...);
#    public static void checkFieldIsNotNull(...);
#    public static void checkNotNull(...);
#    public static void checkNotNullExpressionValue(...);
#    public static void checkNotNullParameter(...);
#    public static void checkParameterIsNotNull(...);
#    public static void checkReturnedValueIsNotNull(...);
#    public static void throwUninitializedPropertyAccessException(...);
#
#}
-dontwarn androidx.compose.animation.tooling.ComposeAnimation
-dontwarn androidx.compose.animation.tooling.ComposeAnimationType
-dontwarn androidx.window.extensions.WindowExtensions
-dontwarn androidx.window.extensions.WindowExtensionsProvider
-dontwarn androidx.window.extensions.layout.WindowLayoutComponent
-dontwarn androidx.window.sidecar.SidecarDisplayFeature
-keep class * { *; }
-dontwarn **
-keepattributes Signature
-keepattributes *Annotation*
#-dontobfuscate


