package org.robolectric.runner.common

import com.google.common.truth.Truth.assertThat
import java.util.concurrent.Callable
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalRunnerApi::class)
class RobolectricRuntimeTest {

  @Test
  fun executeRunsInsideAndroidEnvironment() {
    RobolectricRuntime.launch { sdk = 34 }
      .use { runtime ->
        val sdkInt =
          runtime.execute(
            Callable {
              val versionClass =
                Class.forName(
                  "android.os.Build\$VERSION",
                  true,
                  Thread.currentThread().contextClassLoader,
                )
              versionClass.getField("SDK_INT").getInt(null)
            }
          )
        assertThat(sdkInt).isEqualTo(34)
      }
  }

  @Test
  fun executeLoadedInvokesSandboxLoadedTwin() {
    RobolectricRuntime.launch { sdk = 34 }
      .use { runtime ->
        val packageName = runtime.executeLoaded(RuntimeFixture::class.java.name, "appPackageName")
        assertThat(packageName as String).isNotEmpty()
      }
  }

  @Test
  fun closeIsIdempotentAndExecuteAfterCloseFails() {
    val runtime = RobolectricRuntime.launch { sdk = 34 }
    runtime.close()
    runtime.close()
    assertThrows(IllegalStateException::class.java) { runtime.execute(Callable {}) }
  }

  @Test
  fun launchWithoutExplicitSdkUsesDefault() {
    RobolectricRuntime.launch().use { runtime ->
      val sdkInt =
        runtime.execute(
          Callable {
            Class.forName(
                "android.os.Build\$VERSION",
                true,
                Thread.currentThread().contextClassLoader,
              )
              .getField("SDK_INT")
              .getInt(null)
          }
        )
      assertThat(sdkInt).isGreaterThan(0)
    }
  }
}

/** Loaded through the sandbox classloader by [RobolectricRuntimeTest]; touches Android state. */
class RuntimeFixture {
  @Suppress("unused") fun appPackageName(): String = RuntimeEnvironment.getApplication().packageName
}
