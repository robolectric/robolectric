package org.robolectric.util

import com.google.common.truth.Truth.assertThat
import java.io.IOException
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class TempDirectoryTest {
  @Test
  @Throws(IOException::class)
  fun createsDirsWithSameParent() {
    val tempDir = TempDirectory("temp_dir")
    val path = tempDir.create("dir1")
    val path2 = tempDir.create("dir2")
    assertThat(path.parent.toString()).isEqualTo(path2.parent.toString())
  }

  @Test
  fun clearAllDirectories_removesDirectories() {
    val tempDir = TempDirectory("temp_dir")
    val dir = tempDir.create("dir1")
    val file = tempDir.create("file1")
    TempDirectory.clearAllDirectories()
    assertThat(dir.toFile().exists()).isFalse()
    assertThat(file.toFile().exists()).isFalse()
    assertThat(dir.parent.toFile().exists()).isFalse()
  }
}
