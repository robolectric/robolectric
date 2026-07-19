package org.robolectric.enginefixtures

import java.util.stream.Stream
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.robolectric.annotation.Config
import org.robolectric.junit.jupiter.RobolectricExtension
import org.robolectric.junit.jupiter.RobolectricSdkTest

/**
 * Fixture classes for `JupiterEngineGuardsTest`. They live in a package excluded from both test
 * tasks so only explicit discovery/execution through an engine instance ever touches them.
 */
@ExtendWith(RobolectricExtension::class)
class ExtensionOptedInFixture {
  @Test fun neverRunByCustomEngine() = Unit
}

class DisabledMethodFixture {
  @Disabled("not ready") @Test fun disabledTest(): Unit = error("must not run")

  @Test fun enabledButNeverExecutedInGuardTest() = Unit
}

@Disabled("whole class off")
class DisabledClassFixture {
  @Test fun neverRuns(): Unit = error("must not run")
}

class UnsupportedKindsFixture {
  @ParameterizedTest
  @ValueSource(ints = [1])
  fun parameterized(@Suppress("unused") value: Int) = Unit

  @RobolectricSdkTest @Config(sdk = [33, 34]) fun sdkTemplate() = Unit

  @TestFactory fun factory(): Stream<DynamicTest> = Stream.empty()
}
