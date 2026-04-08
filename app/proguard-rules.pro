# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Strip debug/verbose logs in release builds
-assumenosideeffects class android.util.Log {
    public static int d(...);
    public static int v(...);
}
-keep class com.uniktek.meshmarket.protocol.** { *; }
-keep class com.uniktek.meshmarket.crypto.** { *; }
-dontwarn org.bouncycastle.**
-keep class org.bouncycastle.** { *; }

# Keep SecureIdentityStateManager from being obfuscated to prevent reflection issues
-keep class com.uniktek.meshmarket.identity.SecureIdentityStateManager {
    private android.content.SharedPreferences prefs;
    *;
}

# Keep all classes that might use reflection
-keep class com.uniktek.meshmarket.favorites.** { *; }
-keep class com.uniktek.meshmarket.nostr.** { *; }
-keep class com.uniktek.meshmarket.identity.** { *; }

# Keep Tor implementation (always included)
-keep class com.uniktek.meshmarket.net.RealTorProvider { *; }

# Arti (Custom Tor implementation in Rust) ProGuard rules
-keep class info.guardianproject.arti.** { *; }
-keep class org.torproject.arti.** { *; }
-keepnames class org.torproject.arti.**
-dontwarn info.guardianproject.arti.**
-dontwarn org.torproject.arti.**

# Fix for AbstractMethodError on API < 29 where LocationListener methods are abstract
-keepclassmembers class * implements android.location.LocationListener {
    public <methods>;
}
