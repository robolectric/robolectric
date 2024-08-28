import java.net.URI

plugins {
  alias(libs.plugins.robolectric.deployed.java.module)
  alias(libs.plugins.robolectric.java.module)
}

if (System.getenv("PUBLISH_NATIVERUNTIME_DIST_COMPAT") == "true") {
  pluginManager.apply("maven-publish")
  pluginManager.apply("signing")

  publishing {
    publications {
      register<MavenPublication>("nativeRuntimeDist") {
        val nativeRuntimeDistCompatJar = System.getenv("NATIVERUNTIME_DIST_COMPAT_JAR")
        val nativeRuntimeDistCompatVersion = System.getenv("NATIVERUNTIME_DIST_COMPAT_VERSION")

        artifact(nativeRuntimeDistCompatJar)
        artifactId = "nativeruntime-dist-compat"
        version = nativeRuntimeDistCompatVersion

        pom {
          name = "Robolectric Nativeruntime Distribution Compat"
          description = "Robolectric Nativeruntime Distribution Compat"
          url = "https://source.android.com/"
          inceptionYear = "2008"

          licenses {
            license {
              name = "Apache 2.0"
              url = "http://www.apache.org/licenses/LICENSE-2.0"
              comments =
                "While the EULA for the Android SDK restricts distribution of those binaries, the source code is licensed under Apache 2.0 which allows compiling binaries from source and then distributing those versions."
              distribution = "repo"
            }
          }

          scm {
            url = "https://android.googlesource.com/platform/manifest.git"
            connection = "https://android.googlesource.com/platform/manifest.git"
          }

          developers { developer { name = "The Android Open Source Projects" } }
        }
      }
    }

    repositories {
      maven {
        url = URI("https://oss.sonatype.org/service/local/staging/deploy/maven2/")

        credentials {
          username = System.getProperty("sonatype-login") ?: System.getenv("SONATYPE_LOGIN")
          password = System.getProperty("sonatype-password") ?: System.getenv("SONATYPE_PASSWORD")
        }
      }
    }

    signing { sign(publications.getByName("nativeRuntimeDist")) }
  }
}

dependencies {
  api(project(":utils"))
  api(project(":utils:reflector"))
  api(libs.guava)

  implementation(libs.robolectric.nativeruntime.dist.compat)

  annotationProcessor(libs.auto.service)
  compileOnly(libs.auto.service.annotations)
  compileOnly(AndroidSdk.MAX_SDK.coordinates)

  testCompileOnly(AndroidSdk.MAX_SDK.coordinates)
  testRuntimeOnly(AndroidSdk.MAX_SDK.coordinates)
  testImplementation(project(":robolectric"))
  testImplementation(libs.junit4)
  testImplementation(libs.truth)
}
