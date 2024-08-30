import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins { `kotlin-dsl` }

gradlePlugin {
  plugins {
    register("AarDepsPlugin") {
      id = "org.robolectric.gradle.AarDepsPlugin"
      implementationClass = "org.robolectric.gradle.AarDepsPlugin"
    }
    register("AndroidProjectConfigPlugin") {
      id = "org.robolectric.gradle.AndroidProjectConfigPlugin"
      implementationClass = "org.robolectric.gradle.AndroidProjectConfigPlugin"
    }
    register("DeployedRoboJavaModulePlugin") {
      id = "org.robolectric.gradle.DeployedRoboJavaModulePlugin"
      implementationClass = "org.robolectric.gradle.DeployedRoboJavaModulePlugin"
    }
    register("GradleManagedDevicePlugin") {
      id = "org.robolectric.gradle.GradleManagedDevicePlugin"
      implementationClass = "org.robolectric.gradle.GradleManagedDevicePlugin"
    }
    register("RoboJavaModulePlugin") {
      id = "org.robolectric.gradle.RoboJavaModulePlugin"
      implementationClass = "org.robolectric.gradle.RoboJavaModulePlugin"
    }
    register("SpotlessPlugin") {
      id = "org.robolectric.gradle.SpotlessPlugin"
      implementationClass = "org.robolectric.gradle.SpotlessPlugin"
    }
    register("ShadowsPlugin") {
      id = "org.robolectric.gradle.ShadowsPlugin"
      implementationClass = "org.robolectric.gradle.ShadowsPlugin"
    }
    register("AggregateJavadocPlugin") {
      id = "org.robolectric.gradle.AggregateJavadocPlugin"
      implementationClass = "org.robolectric.gradle.AggregateJavadocPlugin"
    }
  }
}

dependencies {
  implementation(libs.android.gradle.api)
  implementation(libs.android.tools.common)
  implementation(libs.guava)
  implementation(libs.spotless)
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

kotlin { compilerOptions { jvmTarget = JvmTarget.JVM_11 } }
