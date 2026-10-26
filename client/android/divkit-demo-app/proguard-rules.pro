# PhPlus release R8 rules — shrink + obfuscate without breaking DivKit / Retrofit / Push.

# Signature is REQUIRED: without it Retrofit/Gson see raw Class instead of
# ParameterizedType → "Class cannot be cast to ParameterizedType" on release only.
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod,InnerClasses,Exceptions
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable

# --- Android / Kotlin ---
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keep class kotlin.Metadata { *; }
-keep class kotlin.coroutines.Continuation { *; }
-keep class kotlin.coroutines.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# --- DivKit / generated models ---
-keep class com.yandex.div.** { *; }
-keep class com.yandex.div2.** { *; }
-keep class com.yandex.div.json.** { *; }
-keep class com.yandex.div.internal.** { *; }
-keep class com.yandex.divkit.regression.** { *; }

# App code: keep from shrinking (SDUI + reflection), but do NOT keep the whole
# classpath with "-keep class *" (that previously disabled R8 entirely).
-keep class com.yandex.divkit.demo.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <fields>;
}

# --- Networking (Retrofit + Gson + OkHttp) ---
# Fix release-only: ClassCastException Class → ParameterizedType on suspend APIs
# and anonymous TypeToken<Map<String,String>> used in SingletonObjects.
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-dontwarn retrofit2.**
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
# Preserve service-method generic signatures (suspend → Continuation<Response<T>>)
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keep class com.google.gson.reflect.TypeToken { *; }
# Critical: anonymous/named TypeToken subclasses must retain generic superclass
-keep class * extends com.google.gson.reflect.TypeToken { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-dontwarn com.google.gson.**

-keep class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.**

# --- Push / MQTT ---
-keep class ir.naja.pushsdk.android.** { *; }
-keep class org.eclipse.paho.** { *; }

# --- Mapbox / native ---
-keep class com.mapbox.** { *; }
-dontwarn com.mapbox.**

# --- Biometric / crypto ---
-keep class androidx.biometric.** { *; }

# --- Misc third-party used reflectively ---
-keep class com.journeyapps.barcodescanner.** { *; }
-keep class com.google.zxing.** { *; }
-dontwarn com.yalantis.ucrop.**

# Compose / Window optional APIs
-dontwarn androidx.compose.animation.tooling.ComposeAnimation
-dontwarn androidx.compose.animation.tooling.ComposeAnimationType
-dontwarn androidx.window.extensions.WindowExtensions
-dontwarn androidx.window.extensions.WindowExtensionsProvider
-dontwarn androidx.window.extensions.layout.WindowLayoutComponent
-dontwarn androidx.window.sidecar.SidecarDisplayFeature

# Strip verbose logs in release
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}
