package org.robolectric.runner.common

import com.google.common.truth.Truth.assertThat
import java.io.IOException
import java.util.concurrent.Callable
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.robolectric.annotation.Config

@OptIn(ExperimentalRunnerApi::class)
class RobolectricEnvironmentTest {
  @Test
  fun `run executes on Android's main thread with the sandbox class loader`() {
    val testThread = Thread.currentThread()

    val result = environment.run {
      Triple(
        Thread.currentThread(),
        Thread.currentThread().contextClassLoader,
        AndroidProbe.read<Boolean>(environment, "isOnMainThread"),
      )
    }

    assertThat(result.first).isNotSameInstanceAs(testThread)
    assertThat(result.second).isSameInstanceAs(environment.classLoader)
    assertThat(result.third).isTrue()
    assertThat(Thread.currentThread().contextClassLoader)
      .isNotSameInstanceAs(environment.classLoader)
  }

  @Test
  fun `run rethrows the exception of the action as it is`() {
    val thrown = IOException("from the action")

    val caught = assertThrows<IOException> { environment.run(Callable<Unit> { throw thrown }) }

    assertThat(caught).isSameInstanceAs(thrown)
  }

  @Test
  fun `run inside an action runs in place`() {
    val threads = environment.run {
      Thread.currentThread() to environment.run { Thread.currentThread() }
    }

    assertThat(threads.second).isSameInstanceAs(threads.first)
  }

  @Test
  fun `loadClass returns the twin that the sandbox loads`() {
    val twin = environment.loadClass(AndroidProbe::class.java)

    assertThat(twin).isNotSameInstanceAs(AndroidProbe::class.java)
    assertThat(twin.name).isEqualTo(AndroidProbe::class.java.name)
    assertThat(twin.classLoader).isSameInstanceAs(environment.classLoader)
  }

  @Test
  fun `loadClass returns classes the sandbox doesn't reload as they are`() {
    assertThat(environment.loadClass(String::class.java)).isSameInstanceAs(String::class.java)
    assertThat(environment.loadClass(Int::class.javaPrimitiveType!!))
      .isSameInstanceAs(Int::class.javaPrimitiveType)
    assertThat(environment.loadClass(Array<String>::class.java))
      .isSameInstanceAs(Array<String>::class.java)
  }

  @Test
  fun `loadClass returns the twin of an array class`() {
    val twin = environment.loadClass(Array<AndroidProbe>::class.java)

    assertThat(twin.isArray).isTrue()
    assertThat(twin.componentType).isSameInstanceAs(environment.loadClass(AndroidProbe::class.java))
  }

  @Test
  fun `diagnoseFailure adds what Robolectric knows about a failure`() {
    val failure = AssertionError("from a test")
    AndroidProbe.read<Boolean>(environment, "postToMainLooper")
    try {
      environment.diagnoseFailure(failure)
    } finally {
      AndroidProbe.read<Unit>(environment, "idleMainLooper")
    }

    assertThat(failure.suppressed.single())
      .hasMessageThat()
      .contains("Main looper has queued unexecuted runnables")
  }

  @Test
  fun `diagnoseFailure adds nothing when the environment is in order`() {
    val failure = AssertionError("from a test")

    environment.diagnoseFailure(failure)

    assertThat(failure.suppressed).isEmpty()
  }

  @Test
  fun `close tears the environment down once`() {
    val configuration = session.plan(Sdk33::class.java, null).single()
    val closing = session.open(configuration)

    closing.close()
    closing.close()

    assertThrows<IllegalStateException> { closing.run { 0 } }
  }

  @Config(sdk = [34]) class Sdk34

  @Config(sdk = [33]) class Sdk33

  companion object {
    private lateinit var session: RobolectricSession
    private lateinit var environment: RobolectricEnvironment

    @JvmStatic
    @BeforeAll
    fun openEnvironment() {
      session = RobolectricSession.builder().properties(testProperties()).build()
      environment = session.open(session.plan(Sdk34::class.java, null).single())
    }

    @JvmStatic
    @AfterAll
    fun closeSession() {
      session.close()
    }
  }
}
