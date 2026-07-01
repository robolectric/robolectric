plugins { alias(libs.plugins.robolectric.java.module) }

dependencies {
  testImplementation(project(":simulator"))
  testImplementation(libs.junit4)
  testImplementation(libs.truth)
  testImplementation(libs.guava)
  testImplementation(AndroidSdk.MAX_SDK.coordinates)
}

val createBundleJar =
  tasks.register<Jar>("createBundleJar") {
    dependsOn(":integration_tests:simulator:app:assembleDebug")

    // Add classes
    from(
      project(":integration_tests:simulator:app")
        .layout
        .buildDirectory
        .dir("intermediates/javac/debug/compileDebugJavaWithJavac/classes")
    )

    // Add binary AndroidManifest.xml and resources.arsc from processed resources
    val processedRes =
      project(":integration_tests:simulator:app")
        .layout
        .buildDirectory
        .file(
          "intermediates/linked_resources_binary_format/debug/processDebugResources/linked-resources-binary-format-debug.ap_"
        )
    from(zipTree(processedRes)) {
      include("AndroidManifest.xml")
      include("resources.arsc")
    }

    archiveFileName.set("simulator-app.apk")
    destinationDirectory.set(layout.buildDirectory.dir("generated/resources/apk"))
  }

sourceSets { test { resources { srcDir(layout.buildDirectory.dir("generated/resources/apk")) } } }

tasks.named("processTestResources") { dependsOn(createBundleJar) }
