plugins {
  alias(libs.plugins.robolectric.deployed.java.module)
  alias(libs.plugins.robolectric.java.module)
}

dependencies {
  annotationProcessor(libs.auto.service)
  annotationProcessor(libs.error.prone.core)

  api(project(":annotations"))
  api(project(":utils"))
  api(project(":shadowapi"))
  api(project(":utils:reflector"))
  compileOnly(libs.auto.service.annotations)
  api(libs.javax.annotation.api)
  api(libs.javax.inject)

  api(libs.asm)
  api(libs.asm.commons)
  api(libs.guava)
  compileOnly(libs.findbugs.jsr305)

  testImplementation(libs.findbugs.jsr305)
  testImplementation(libs.junit4)
  testImplementation(libs.truth)

  // ShadowingTest.testStaticMethodsAreDelegated fails with a combination of Mockito 5.x and the
  // sandbox grouping commit (234dc80c2df61c15504c288cd62acdec8e3dca5c).
  //
  // TODO(hoisie): figure out why this is happening and upgrade to Mockito 5.x.
  testImplementation("org.mockito:mockito-core:5.17.0")

  testImplementation(project(":junit"))
}
