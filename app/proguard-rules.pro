# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Agora Video SDK rules
-keep class io.agora.** { *; }
-dontwarn io.agora.**

# Kotlin serialization rules
-keepattributes *Annotation*,InnerClasses
-dontnote kotlinx.serialization.SerializationStrategy
-dontnote kotlinx.serialization.DeserializationStrategy
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName public *;
}
-keepclassmembers class ** implements kotlinx.serialization.KSerializer {
    public static final *** Companion;
    public static *** serializer();
}

# Hilt dependency injection rules
-keep class dagger.** { *; }
-keep class **_HiltModules { *; }
-keep class **_HiltComponents { *; }
-dontwarn dagger.**

# ML Kit rules (for FaceDetection and PoseDetection)
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# ZXing (for QR code generation/scanning)
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# Keep data classes used in serialization
-keep class com.example.bubtrack.models.SleepStatus { *; }
-keep class com.example.bubtrack.models.SleepFeatures { *; }