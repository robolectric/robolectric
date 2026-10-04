@file:OptIn(ExperimentalRunnerApi::class)

package org.robolectric.kotest

import io.kotest.core.extensions.ConstructorExtension
import io.kotest.core.extensions.SpecExtension
import io.kotest.core.extensions.TestCaseExtension
import io.kotest.core.spec.Spec
import io.kotest.core.test.TestCase
import io.kotest.engine.test.TestResult
import kotlin.reflect.KClass
import org.robolectric.kotest.internal.SpecEnvironments
import org.robolectric.runner.common.ExperimentalRunnerApi

/**
 * Runs a Kotest spec in Robolectric's Android environment:
 * ```
 * @ApplyExtension(RobolectricExtension::class)
 * class MySpec : FunSpec({
 *   val context = ApplicationProvider.getApplicationContext<Context>()
 *
 *   test("uses Android") {
 *     context.packageName.shouldNotBeEmpty()
 *   }
 * })
 * ```
 *
 * A spec is configured as a test class is with `RobolectricTestRunner`, for example with
 * [org.robolectric.annotation.Config] on the class.
 * - The spec is created in Robolectric's sandbox, so its body, its callbacks and its tests run on
 *   Android's main thread and can use Android. It has to be a class with a constructor without
 *   parameters.
 * - It runs on every SDK that is selected for it. On several SDKs, each root test is reported once
 *   for each SDK: `uses Android[33]`, and `uses Android` on the last one.
 * - An Android environment lives as long as the spec instance, so Kotest's isolation mode decides
 *   which tests share Android state.
 * - A test can launch coroutines in its scope, and `Dispatchers.Main` is Android's main dispatcher.
 *
 * `runner/README.md` has the details and the limits. This extension is experimental: its behavior
 * may change in future releases.
 */
public class RobolectricExtension : ConstructorExtension, SpecExtension, TestCaseExtension {
  /** Creates the spec in an Android environment of its own. */
  override fun <T : Spec> instantiate(clazz: KClass<T>): Spec = SpecEnvironments.create(clazz.java)

  /** Runs the spec in its environments, and closes them once the spec is done. */
  override suspend fun intercept(spec: Spec, execute: suspend (Spec) -> Unit) {
    SpecEnvironments.runSpec(spec) { execute(spec) }
  }

  /** Runs the test in the environment of its spec instance. */
  override suspend fun intercept(
    testCase: TestCase,
    execute: suspend (TestCase) -> TestResult,
  ): TestResult = SpecEnvironments.runTest(testCase, execute)
}
