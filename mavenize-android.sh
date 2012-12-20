#!/bin/sh

ANDROID_SOURCES_BASE=/Volumes/AndroidSource
ANDROID_VERSION=4.1.2_r1
OUT=/tmp/android-1234

OUTJAR=$OUT/android-base-sources.jar

BASE_DIR=$ANDROID_SOURCES_BASE/frameworks/base

rm -rf $OUT
mkdir -p $OUT
( cd $OUT && mkdir from )
( cd $OUT/from && jar cf $OUTJAR . )

for lib in $BASE_DIR/*/java; do
  echo $lib
  ( cd $lib && jar uf $OUTJAR . )
done


mvn install:install-file \
    -Dfile=$ANDROID_SOURCES_BASE/out/target/common/obj/JAVA_LIBRARIES/framework_intermediates/classes.jar \
    -DgroupId=com.squareup.robolectric \
    -DartifactId=android-base \
    -Dversion=$ANDROID_VERSION \
    -Dpackaging=jar \
    -Dclassifier=real

mvn install:install-file \
    -Dfile=$OUTJAR \
    -DgroupId=com.squareup.robolectric \
    -DartifactId=android-base \
    -Dversion=$ANDROID_VERSION \
    -Dpackaging=jar \
    -Dclassifier=sources

mvn install:install-file \
    -Dfile=$ANDROID_SOURCES_BASE/prebuilt/common/kxml2/kxml2-2.3.0.jar \
    -DgroupId=com.squareup.robolectric \
    -DartifactId=android-kxml2 \
    -Dversion=$ANDROID_VERSION \
    -Dpackaging=jar \
    -Dclassifier=real


mvn org.apache.maven.plugins:maven-deploy-plugin:2.7:deploy-file \
    -DgroupId=com.squareup.robolectric \
    -DartifactId=android-base \
    -Dversion=4.1.2_r1 \
    -Dpackaging=jar \
    -Dclassifier=real \
    -Durl=http://data01.mtv.squareup.com/nexus/content/repositories/releases \
    -DrepositoryId=square-nexus \
    -Dfile=/Users/square/.m2/repository/com/squareup/robolectric/android-base/4.1.2_r1/android-base-4.1.2_r1-real.jar

mvn org.apache.maven.plugins:maven-deploy-plugin:2.7:deploy-file \
    -DgroupId=com.squareup.robolectric \
    -DartifactId=android-luni \
    -Dversion=4.1.2_r1 \
    -Dpackaging=jar \
    -Dclassifier=real \
    -Durl=http://data01.mtv.squareup.com/nexus/content/repositories/releases \
    -DrepositoryId=square-nexus \
    -Dfile=/Users/square/.m2/repository/com/squareup/robolectric/android-luni/4.1.2_r1/android-luni-4.1.2_r1-real.jar

mvn org.apache.maven.plugins:maven-deploy-plugin:2.7:deploy-file \
    -DgroupId=com.squareup.robolectric \
    -DartifactId=android-kxml2 \
    -Dversion=4.1.2_r1 \
    -Dpackaging=jar \
    -Dclassifier=real \
    -Durl=http://data01.mtv.squareup.com/nexus/content/repositories/releases \
    -DrepositoryId=square-nexus \
    -Dfile=/Users/square/.m2/repository/com/squareup/robolectric/android-kxml2/4.1.2_r1/android-kxml2-4.1.2_r1-real.jar


exit 1

echo "building jar for libcore luni..."

cd $ANDROID_SOURCES_BASE/libcore/luni/src/main/java && javac -cp /tmp -d /tmp/out \
    libcore/icu/CollationElementIteratorICU.java libcore/icu/CollationKeyICU.java libcore/icu/ErrorCode.java libcore/icu/ICU.java libcore/icu/LocaleData.java libcore/icu/NativeBreakIterator.java libcore/icu/NativeCollation.java libcore/icu/NativeConverter.java libcore/icu/NativeDecimalFormat.java libcore/icu/NativeIDN.java libcore/icu/NativeNormalizer.java libcore/icu/NativePluralRules.java libcore/icu/RuleBasedCollatorICU.java libcore/util/BasicLruCache.java java/util/LinkedHashMap.java java/util/HashMap.java libcore/util/Objects.java /Volumes/AndroidSource/libcore/luni/src/main/java (android-4.1.2_r1) ls -l /tmp/out

mvn install:install-file \
    -Dfile=/tmp/android-luni-2.1.2_r1.jar \
    -DgroupId=com.squareup.robolectric \
    -DartifactId=android-luni \
    -Dversion=4.1.2_r1 \
    -Dpackaging=jar \
    -Dclassifier=real
