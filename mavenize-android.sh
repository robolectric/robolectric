#!/bin/sh

ANDROID_SOURCES_BASE=/Volumes/AndroidSource
ANDROID_VERSION=4.1.2_r1
OUT=/tmp/android-1234

SRC_JAR=$OUT/android-base-sources.jar

BASE_DIR=$ANDROID_SOURCES_BASE/frameworks/base

rm -rf $OUT
mkdir -p $OUT
( cd $OUT && mkdir from )
( cd $OUT/from && jar cf $SRC_JAR . )

for lib in $BASE_DIR/*/java; do
  echo $lib
  ( cd $lib && jar uf $SRC_JAR . )
done


# install android-base
mvn install:install-file \
    -Dfile=$ANDROID_SOURCES_BASE/out/target/common/obj/JAVA_LIBRARIES/framework_intermediates/classes.jar \
    -DgroupId=org.robolectric \
    -DartifactId=android-base \
    -Dversion=$ANDROID_VERSION \
    -Dpackaging=jar \
    -Dclassifier=real

mvn install:install-file \
    -Dfile=$SRC_JAR \
    -DgroupId=org.robolectric \
    -DartifactId=android-base \
    -Dversion=$ANDROID_VERSION \
    -Dpackaging=jar \
    -Dclassifier=sources

mvn org.apache.maven.plugins:maven-deploy-plugin:2.7:deploy-file \
    -DgroupId=org.robolectric \
    -DartifactId=android-base \
    -Dversion=$ANDROID_VERSION \
    -Dpackaging=jar \
    -Dclassifier=real \
    -Durl=http://data01.mtv.squareup.com/nexus/content/repositories/releases \
    -DrepositoryId=square-nexus \
    -Dfile=$ANDROID_SOURCES_BASE/out/target/common/obj/JAVA_LIBRARIES/framework_intermediates/classes.jar




echo "building jar for libcore luni..."

cd $ANDROID_SOURCES_BASE/libcore/luni/src/main/java && javac -cp /tmp -d /tmp/out \
    libcore/icu/CollationElementIteratorICU.java libcore/icu/CollationKeyICU.java libcore/icu/ErrorCode.java libcore/icu/ICU.java libcore/icu/LocaleData.java libcore/icu/NativeBreakIterator.java libcore/icu/NativeCollation.java libcore/icu/NativeConverter.java libcore/icu/NativeDecimalFormat.java libcore/icu/NativeIDN.java libcore/icu/NativeNormalizer.java libcore/icu/NativePluralRules.java libcore/icu/RuleBasedCollatorICU.java libcore/util/BasicLruCache.java java/util/LinkedHashMap.java java/util/HashMap.java libcore/util/Objects.java /Volumes/AndroidSource/libcore/luni/src/main/java (android-4.1.2_r1) ls -l /tmp/out

# install android-luni
mvn install:install-file \
    -Dfile=/tmp/android-luni-$ANDROID_VERSION.jar \
    -DgroupId=org.robolectric \
    -DartifactId=android-luni \
    -Dversion=$ANDROID_VERSION \
    -Dpackaging=jar \
    -Dclassifier=real

mvn org.apache.maven.plugins:maven-deploy-plugin:2.7:deploy-file \
    -DgroupId=org.robolectric \
    -DartifactId=android-luni \
    -Dversion=$ANDROID_VERSION \
    -Dpackaging=jar \
    -Dclassifier=real \
    -Durl=http://data01.mtv.squareup.com/nexus/content/repositories/releases \
    -DrepositoryId=square-nexus \
    -Dfile=/tmp/android-luni-$ANDROID_VERSION.jar


# install android-kxml2
mvn install:install-file \
    -Dfile=$ANDROID_SOURCES_BASE/prebuilt/common/kxml2/kxml2-2.3.0.jar \
    -DgroupId=org.robolectric \
    -DartifactId=android-kxml2 \
    -Dversion=$ANDROID_VERSION \
    -Dpackaging=jar \
    -Dclassifier=real

mvn org.apache.maven.plugins:maven-deploy-plugin:2.7:deploy-file \
    -DgroupId=org.robolectric \
    -DartifactId=android-kxml2 \
    -Dversion=$ANDROID_VERSION \
    -Dpackaging=jar \
    -Dclassifier=real \
    -Durl=http://data01.mtv.squareup.com/nexus/content/repositories/releases \
    -DrepositoryId=square-nexus \
    -Dfile=$ANDROID_SOURCES_BASE/prebuilt/common/kxml2/kxml2-2.3.0.jar
