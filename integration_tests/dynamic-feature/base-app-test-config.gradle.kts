// AGP only gives a dynamic feature module's unit tests the feature's own resources APK and
// manifest. Point Robolectric at the base app's resources APK and manifest too, so it can run the
// tests against the base app with the feature installed as a split. AGP has already built both,
// because the feature's resources are linked against them.
val baseApp = ":integration_tests:dynamic-feature:app"
val baseIntermediates = project(baseApp).layout.buildDirectory.dir("intermediates")
val baseResourceApk = baseIntermediates.map {
  it.file(
    "linked_resources_binary_format/debug/processDebugResources/" +
      "linked-resources-binary-format-debug.ap_",
  )
}
val baseManifest = baseIntermediates.map {
  it.file("packaged_manifests/debug/processDebugManifestForPackage/AndroidManifest.xml")
}

tasks
  .matching { it.name == "generateDebugUnitTestConfig" }
  .configureEach {
    val apk = baseResourceApk
    val manifest = baseManifest
    dependsOn("$baseApp:processDebugResources", "$baseApp:processDebugManifestForPackage")
    inputs.file(apk)
    inputs.file(manifest)
    doLast {
      val config =
        outputs.files.asFileTree.matching { include("**/test_config.properties") }.singleFile
      config.appendText(
        "android_base_resource_apk=${apk.get().asFile.absolutePath}\n" +
          "android_base_merged_manifest=${manifest.get().asFile.absolutePath}\n",
      )
    }
  }
