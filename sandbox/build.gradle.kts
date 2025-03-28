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
  testImplementation(libs.mockito)
  testImplementation(libs.mockito.subclass)
  testImplementation(project(":junit"))
}
