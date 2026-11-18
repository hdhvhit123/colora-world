# kotlinx.serialization: keep generated serializers for content/doc models.
-keepattributes *Annotation*, InnerClasses
-keep,includedescriptorclasses class com.colora.world.**$$serializer { *; }
-keepclassmembers class com.colora.world.** { *** Companion; }
-keepclasseswithmembers class com.colora.world.** { kotlinx.serialization.KSerializer serializer(...); }
