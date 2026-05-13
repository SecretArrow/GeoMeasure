# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Keep SQLCipher
-keep class net.sqlcipher.** { *; }
-keep class net.zetetic.** { *; }

# Keep OSMDroid
-keep class org.osmdroid.** { *; }

# Keep Google API client
-keep class com.google.api.services.drive.** { *; }
-keep class com.google.api.client.** { *; }

# Keep iText - ignore missing SpongyCastle
-keep class com.itextpdf.** { *; }
-dontwarn org.spongycastle.**
-dontwarn com.itextpdf.text.pdf.security.**
-dontwarn com.itextpdf.text.pdf.crypto.**

# Keep Gson
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Keep Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep model classes
-keep class com.geomeasure.pro.domain.model.** { *; }
-keep class com.geomeasure.pro.data.local.db.entities.** { *; }

# General Android
-dontwarn com.google.common.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.apache.http.**
-dontwarn com.google.api.**
-dontwarn com.google.android.gms.**
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
