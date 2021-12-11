# use dictionaries to create random class/package names
-obfuscationdictionary dict/obfuscation-dictionary.txt
-classobfuscationdictionary dict/class-dictionary.txt
-packageobfuscationdictionary dict/package-dictionary.txt

# keep these libraries but allow obfuscating
-dontwarn twitter4j.**
-keep,allowobfuscation,allowoptimization class twitter4j.** {*;}
-adaptclassstrings twitter4j.**

-dontwarn javax.management.DynamicMBean
-keep,allowobfuscation class javax.management.DynamicMBean {*;}
-adaptclassstrings javax.management.DynamicMBean

-dontwarn org.conscrypt.Conscrypt
-keep,allowobfuscation class org.conscrypt.Conscrypt {*;}
-adaptclassstrings org.conscrypt.Conscrypt

-dontwarn org.conscrypt.OpenSSLProvider
-keep,allowobfuscation class org.conscrypt.OpenSSLProvider {*;}
-adaptclassstrings org.conscrypt.OpenSSLProvider

-dontwarn javax.annotation.Nullable