// AGP only gives a dynamic feature module's unit tests the feature's own resources APK and
// manifest. Override `android_resource_apk` and `android_merged_manifest` to point at the base app
// (Properties keeps the last value) and move the feature's resources APK to
// `android_split_resource_apks`, so Robolectric runs the tests against the base app with the
// feature installed as a split. AGP has already built both base files, because the feature's
// resources are linked against them.
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
      val props = java.util.Properties()
      config.inputStream().use { props.load(it) }
      val featureApk =
        checkNotNull(props.getProperty("android_resource_apk")) {
          "Expected android_resource_apk in $config"
        }
      val baseApkPath = apk.get().asFile.absolutePath.replace("\\", "/")
      val baseManifestPath = manifest.get().asFile.absolutePath.replace("\\", "/")
      config.appendText(
        "android_resource_apk=$baseApkPath\n" +
          "android_merged_manifest=$baseManifestPath\n" +
          "android_split_resource_apks=${featureApk.replace("\\", "/")}\n",
      )
    }
  }
