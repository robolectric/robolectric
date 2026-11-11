import org.robolectric.gradle.AndroidSdk

plugins { alias(libs.plugins.robolectric.java.module) }

dependencies {
  testImplementation(project(":simulator"))
  testImplementation(libs.junit4)
  testImplementation(libs.truth)
  testImplementation(libs.guava)
  testImplementation(AndroidSdk.MAX_SDK.coordinates)
}

// Packages a flavor of the test app the way the simulator expects: its classes, plus the binary
// AndroidManifest.xml and resources.arsc from its processed resources.
fun registerBundleJar(flavor: String, apkName: String) =
  tasks.register<Jar>("create${flavor.replaceFirstChar { it.uppercase() }}BundleJar") {
    val variant = "${flavor}Debug"
    val task = variant.replaceFirstChar { it.uppercase() }
    dependsOn(":integration_tests:simulator:app:assemble$task")

    val appBuildDir = project(":integration_tests:simulator:app").layout.buildDirectory
    from(appBuildDir.dir("intermediates/javac/$variant/compile${task}JavaWithJavac/classes"))
    from(
      zipTree(
        appBuildDir.file(
          "intermediates/linked_resources_binary_format/$variant/process${task}Resources/" +
            "linked-resources-binary-format-$variant.ap_",
        ),
      ),
    ) {
      include("AndroidManifest.xml")
      include("resources.arsc")
    }

    archiveFileName.set(apkName)
    destinationDirectory.set(layout.buildDirectory.dir("generated/resources/apk"))
  }

val bundleJars =
  listOf(
    registerBundleJar("plain", "simulator-app.apk"),
    registerBundleJar("alias", "simulator-app-alias.apk"),
    registerBundleJar("library", "simulator-app-library.apk"),
  )

sourceSets { test { resources { srcDir(layout.buildDirectory.dir("generated/resources/apk")) } } }

tasks.named("processTestResources") { dependsOn(bundleJars) }

// A simulator never returns, so give each test class its own JVM.
tasks.named<Test>("test") { forkEvery = 1 }
