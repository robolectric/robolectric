package org.robolectric.runner.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ExecutionResultTest {
  @Test
  fun `Success has isSuccess true`() {
    val result = ExecutionResult.Success(100)
    assertThat(result.isSuccess).isTrue()
    assertThat(result.durationMs).isEqualTo(100)
  }

  @Test
  fun `Failure has isSuccess false`() {
    val error = RuntimeException("test error")
    val result = ExecutionResult.Failure(error, 50)
    assertThat(result.isSuccess).isFalse()
    assertThat(result.durationMs).isEqualTo(50)
    assertThat(result.error).isEqualTo(error)
    assertThat(result.message).isEqualTo("test error")
  }

  @Test
  fun `Failure message defaults to Unknown error when null`() {
    val error = RuntimeException()
    val result = ExecutionResult.Failure(error)
    assertThat(result.message).isEqualTo("Unknown error")
  }

  @Test
  fun `Skipped has isSuccess false and durationMs zero`() {
    val result = ExecutionResult.Skipped("no matching SDK")
    assertThat(result.isSuccess).isFalse()
    assertThat(result.durationMs).isEqualTo(0)
    assertThat(result.reason).isEqualTo("no matching SDK")
  }

  @Test
  fun `success companion function creates Success`() {
    val result = ExecutionResult.success(200)
    assertThat(result).isInstanceOf(ExecutionResult.Success::class.java)
    assertThat(result.durationMs).isEqualTo(200)
  }

  @Test
  fun `failure companion function creates Failure`() {
    val error = IllegalStateException("test")
    val result = ExecutionResult.failure(error, 75)
    assertThat(result).isInstanceOf(ExecutionResult.Failure::class.java)
    assertThat((result as ExecutionResult.Failure).error).isEqualTo(error)
    assertThat(result.durationMs).isEqualTo(75)
  }

  @Test
  fun `skipped companion function creates Skipped`() {
    val result = ExecutionResult.skipped("disabled")
    assertThat(result).isInstanceOf(ExecutionResult.Skipped::class.java)
    assertThat((result as ExecutionResult.Skipped).reason).isEqualTo("disabled")
  }
}
