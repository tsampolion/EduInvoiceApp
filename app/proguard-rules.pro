# ProGuard rules for EduInvoiceApp

# Keep classes annotated with Hilt entry points so Dagger can generate and locate them
-keep @dagger.hilt.android.HiltAndroidApp class *
-keep @dagger.hilt.android.AndroidEntryPoint class *

# Keep generated Hilt components and internal managers
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Jetpack Compose requires keeping composable methods and related runtime classes
-keep class androidx.compose.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# Keep Room annotations and generated schema classes
-keep @androidx.room.* class *
-keep class androidx.room.** { *; }
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}
