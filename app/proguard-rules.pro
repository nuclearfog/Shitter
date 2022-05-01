# use dictionaries to create random class/package names
-obfuscationdictionary dict/obfuscation-dictionary.txt
-classobfuscationdictionary dict/class-dictionary.txt
-packageobfuscationdictionary dict/package-dictionary.txt

# keep these libraries but allow obfuscating
-dontwarn org.conscrypt.Conscrypt
#noinspection ShrinkerUnresolvedReference
-keep,allowobfuscation, allowoptimization class org.conscrypt.Conscrypt {*;}
-adaptclassstrings org.conscrypt.Conscrypt

-dontwarn org.conscrypt.OpenSSLProvider
#noinspection ShrinkerUnresolvedReference
-keep,allowobfuscation, allowoptimization class org.conscrypt.OpenSSLProvider {*;}
-adaptclassstrings org.conscrypt.OpenSSLProvider

-dontwarn android.support.v8.renderscript.**
-keep,allowobfuscation, allowoptimization class android.support.v8.renderscript.** {*;}
-adaptclassstrings android.support.v8.renderscript.**

-dontwarn javax.annotation.Nullable