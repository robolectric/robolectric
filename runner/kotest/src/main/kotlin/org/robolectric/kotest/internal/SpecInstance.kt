package org.robolectric.kotest.internal

import io.kotest.core.spec.Spec
import kotlinx.coroutines.MainCoroutineDispatcher
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.runner.common.RobolectricEnvironment

/**
 * An instance of a spec, the Android environment that it was created in and runs in, and Android's
 * main dispatcher for that environment, if the code under test has one.
 */
@OptIn(ExperimentalRunnerApi::class)
internal class SpecInstance(
  val spec: Spec,
  val environment: RobolectricEnvironment,
  val mainDispatcher: MainCoroutineDispatcher?,
)
