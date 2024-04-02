package org.robolectric.util

import com.google.common.truth.Truth.assertThat
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.robolectric.util.TempDirectory.OBSOLETE_MARKER_FILE_NAME

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

  @Test
  fun cleanupObsoleteDirectories() {
    val origOsName = System.getProperty("os.name")
    System.setProperty("os.name", "windows")

    try {
      val tempDirectory = Files.createTempDirectory("robolectric-nativeruntime")
      Files.write(tempDirectory.resolve("robolectric-nativeruntime.dll"), byteArrayOf())
      Files.write(tempDirectory.resolve("icudt68l.dat"), byteArrayOf())
      Files.write(tempDirectory.resolve(OBSOLETE_MARKER_FILE_NAME), byteArrayOf())
      TempDirectory.collectObsoleteWindowsTempDirectories()

      TempDirectory.clearAllDirectories()
      val executorService = Executors.newSingleThreadScheduledExecutor()

      val latch = CountDownLatch(1)
      waitForDirectoryDeletion(tempDirectory, latch, executorService)

      assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue()
      executorService.shutdownNow()
      assertThat(executorService.awaitTermination(5, TimeUnit.SECONDS)).isTrue()
    } finally {
      System.setProperty("os.name", origOsName)
    }
  }

  fun waitForDirectoryDeletion(
    path: Path,
    latch: CountDownLatch,
    service: ScheduledExecutorService,
  ) {
    if (!Files.exists(path)) {
      latch.countDown()
    } else {
      val ignored =
        service.schedule(
          { waitForDirectoryDeletion(path, latch, service) },
          50,
          TimeUnit.MILLISECONDS,
        )
    }
  }
}
