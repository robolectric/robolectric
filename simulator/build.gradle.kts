plugins {
  alias(libs.plugins.robolectric.deployed.java.module)
  alias(libs.plugins.robolectric.java.module)
}

dependencies {
  annotationProcessor(libs.error.prone.core)
  annotationProcessor(libs.auto.service)

  api(project(":robolectric"))
  compileOnly(AndroidSdk.MAX_SDK.coordinates)
  compileOnly(libs.auto.service.annotations)

  api(libs.guava)
}
