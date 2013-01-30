#!/bin/sh

ANDROID_SOURCES_BASE=/Volumes/android
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
    -Dversion=${ANDROID_VERSION}_rc \
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
    -Dversion=${ANDROID_VERSION}_rc \
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
    -Dversion=${ANDROID_VERSION}_rc \
    -Dpackaging=jar \
    -Dclassifier=real \
    -Durl=http://data01.mtv.squareup.com/nexus/content/repositories/releases \
    -DrepositoryId=square-nexus \
    -Dfile=$ANDROID_SOURCES_BASE/prebuilt/common/kxml2/kxml2-2.3.0.jar



edit poms

version=4.1.2_r1_rc
for artifactId in "android-base" "android-luni" "android-kxml2"; do
  echo cd ~/.m2/org/robolectric/$artifactId/$version
  cd ~/.m2/repository/org/robolectric/$artifactId/$version

  for ext in ".jar" "-javadoc.jar" "-sources.jar"; do
    echo cp /tmp/empty.jar $artifactId-$version$ext
    cp /tmp/empty.jar $artifactId-$version$ext
  done

  ls

  for ext in "-real.jar" ".jar" "-javadoc.jar" "-sources.jar" ".pom"; do
    echo gpg -ab $artifactId-$version$ext
    gpg -ab $artifactId-$version$ext
  done

  ls -l
done

mvn repository:bundle-pack -DgroupId=org.robolectric -DartifactId=android-base -Dversion=4.1.2_r1_rc
mvn repository:bundle-pack -DgroupId=org.robolectric -DartifactId=android-luni -Dversion=4.1.2_r1_rc
mvn repository:bundle-pack -DgroupId=org.robolectric -DartifactId=android-kxml2 -Dversion=4.1.2_r1_rc

cd ~/.m2/org/robolectric/android-base/4.1.2_r1_rc
gpg -ab android-base-4.1.2_r1_rc-real.jar
gpg -ab android-base-4.1.2_r1_rc.pom
cp /tmp/empty.jar android-base-4.1.2_r1_rc-javadoc.jar
cp /tmp/empty.jar android-base-4.1.2_r1_rc-sources.jar
gpg -ab android-base-4.1.2_r1_rc-sources.jar

