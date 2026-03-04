# Add project specific ProGuard rules here.
-keep class com.pierregasly.app.data.model.** { *; }
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn retrofit2.**
