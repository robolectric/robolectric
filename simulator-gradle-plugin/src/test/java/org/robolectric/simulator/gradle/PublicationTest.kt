package org.robolectric.simulator.gradle

import com.google.common.truth.Truth.assertThat
import java.io.File
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class PublicationTest {
  @Test
  fun moduleMetadata_doesNotDependOnKotlinStdlib() {
    // Gradle provides its own Kotlin runtime to plugins. Depending on kotlin-stdlib would add it to
    // the build classpath of every project that applies the plugin.
    val moduleMetadata = File(System.getProperty("simulator.plugin.moduleMetadata"))

    assertThat(moduleMetadata.readText()).doesNotContain("kotlin-stdlib")
  }
}
