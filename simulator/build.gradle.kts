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
  compileOnly(variantOf(libs.androidx.test.monitor) { artifactType("aar") })

  api(libs.guava)

  testImplementation(libs.junit4)
  testImplementation(libs.truth)
  testImplementation(libs.mockito)
  testImplementation(variantOf(libs.androidx.test.core) { artifactType("aar") })
  testImplementation(variantOf(libs.androidx.test.ext.junit) { artifactType("aar") })
  testImplementation(AndroidSdk.MAX_SDK.coordinates)
}
