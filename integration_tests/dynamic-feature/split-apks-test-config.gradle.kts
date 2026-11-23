// AGP only gives a base app module's unit tests its own resources APK and manifest. Point
// Robolectric at each dynamic feature's linked resources APK via `android_split_resource_apks` so
// base app tests can run with the dynamic feature splits installed.
val featureProjects =
  listOf(
    ":integration_tests:dynamic-feature:feature1",
    ":integration_tests:dynamic-feature:feature2",
  )
val featureResourceApks = featureProjects.map { feature ->
  project(feature)
    .layout
    .buildDirectory
    .file(
      "intermediates/linked_resources_binary_format/debug/processDebugResources/" +
        "linked-resources-binary-format.ap_",
    )
}

tasks
  .matching { it.name == "generateDebugUnitTestConfig" }
  .configureEach {
    featureProjects.forEach { feature -> dependsOn("$feature:processDebugResources") }
    featureResourceApks.forEach { apk -> inputs.file(apk) }
    val apks = featureResourceApks
    doLast {
      val config =
        outputs.files.asFileTree.matching { include("**/test_config.properties") }.singleFile
      val splitPaths = apks.joinToString(",") { it.get().asFile.absolutePath.replace("\\", "/") }
      config.appendText("android_split_resource_apks=$splitPaths\n")
    }
  }
