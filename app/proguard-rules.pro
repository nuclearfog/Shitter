# use dictionaries to create random class/package names
-obfuscationdictionary dict/obfuscation-dictionary.txt
-classobfuscationdictionary dict/class-dictionary.txt
-packageobfuscationdictionary dict/package-dictionary.txt

# keep these libraries but allow obfuscating
-dontwarn org.conscrypt.Conscrypt
-keep,allowobfuscation, allowoptimization class org.conscrypt.Conscrypt {*;}

-dontwarn org.conscrypt.OpenSSLProvider
-keep,allowobfuscation, allowoptimization class org.conscrypt.OpenSSLProvider {*;}

-dontwarn org.conscrypt.Conscrypt$Version
-keep,allowobfuscation, allowoptimization class org.conscrypt.Conscrypt$Version {*;}

-dontwarn org.conscrypt.ConscryptHostnameVerifier
-keep,allowobfuscation, allowoptimization class org.conscrypt.ConscryptHostnameVerifier {*;}

-dontwarn android.support.v8.renderscript.**
-keep,allowobfuscation, allowoptimization class android.support.v8.renderscript.** {*;}

-dontwarn org.bouncycastle.jsse.BCSSLParameters
-keep,allowobfuscation, allowoptimization class org.bouncycastle.jsse.BCSSLParameters {*;}

-dontwarn org.bouncycastle.jsse.BCSSLSocket
-keep,allowobfuscation, allowoptimization class org.bouncycastle.jsse.BCSSLSocket {*;}

-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-keep,allowobfuscation, allowoptimization class org.bouncycastle.jsse.provider.BouncyCastleJsseProvider {*;}

-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-keep,allowobfuscation, allowoptimization class org.openjsse.javax.net.ssl.SSLParameters {*;}

-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-keep,allowobfuscation, allowoptimization class org.openjsse.javax.net.ssl.SSLSocket {*;}

-dontwarn org.openjsse.net.ssl.OpenJSSE
-keep,allowobfuscation, allowoptimization class org.openjsse.net.ssl.OpenJSSE {*;}

-keep class pl.droidsonroids.gif.** {*;}

-dontwarn javax.annotation.Nullable

-keepclassmembers class * implements android.os.Parcelable {*;}