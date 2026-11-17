import org.robolectric.gradle.AndroidSdk

plugins {
  alias(libs.plugins.robolectric.deployed.java.module)
  alias(libs.plugins.robolectric.java.module)
  alias(libs.plugins.robolectric.shadows)
}

shadows {
  packageName = "org.robolectric"
  sdkCheckMode = "ERROR"
}

dependencies {
  api(project(":annotations"))
  api(project(":nativeruntime"))
  api(project(":resources"))
  api(project(":sandbox"))
  api(project(":shadowapi"))
  api(project(":utils"))
  api(project(":utils:reflector"))

  api(variantOf(libs.androidx.test.monitor) { artifactType("aar") })
  compileOnly(libs.findbugs.jsr305)
  compileOnly(AndroidSdk.MAX_SDK.coordinates)
  api(libs.icu4j)
  api(libs.auto.value.annotations)
  compileOnly(libs.auto.service.annotations)
  annotationProcessor(libs.auto.value)
  annotationProcessor(libs.auto.service)

  testImplementation(project(":robolectric"))
  testImplementation(libs.junit4)
  testImplementation(libs.truth)
  testImplementation(AndroidSdk.MAX_SDK.coordinates)
}
