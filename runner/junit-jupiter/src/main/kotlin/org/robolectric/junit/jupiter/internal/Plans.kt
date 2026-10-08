package org.robolectric.junit.jupiter.internal

import java.lang.reflect.Method
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace
import org.junit.platform.commons.annotation.Testable
import org.junit.platform.commons.support.AnnotationSupport
import org.junit.platform.commons.support.HierarchyTraversalMode
import org.robolectric.annotation.Config
import org.robolectric.junit.jupiter.RobolectricExtension
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration
import org.robolectric.runner.common.ExperimentalRunnerApi
import org.robolectric.runner.common.RobolectricSession

/** Finds out which Android environments a class or a test is configured to run in. */
@OptIn(ExperimentalRunnerApi::class)
internal object Plans {
  /** The namespace of what the extension keeps in JUnit's stores. */
  val NAMESPACE: Namespace = Namespace.create(RobolectricExtension::class.java)

  private const val SESSION = "session"
  private const val CLASS_SDK = "classSdk"

  /** Returns the session of the test run. */
  fun session(context: ExtensionContext): RobolectricSession =
    context.root
      .getStore(NAMESPACE)
      .computeIfAbsent(
        SESSION,
        // Tests throw the errors that JUnit acts on, such as that of a failed assumption.
        { RobolectricSession.builder().sharePackage("org.opentest4j.").build() },
        RobolectricSession::class.java,
      )

  /**
   * Returns the environments that the class or the test of the context is configured for. If its
   * class runs once for each SDK, these are the ones for the SDK of the current run.
   */
  fun of(
    context: ExtensionContext,
    testClass: Class<*>,
    testMethod: Method?,
  ): List<Configuration> {
    val configurations = session(context).plan(testClass, testMethod)
    val classSdk = classSdk(context)
    return if (classSdk == null) configurations
    else configurations.filter { it.apiLevel == classSdk }
  }

  /**
   * Returns the SDKs that a class runs once for each on: those of its tests, or those of the class
   * if its tests share the environments of the class.
   */
  fun sdksOfClass(context: ExtensionContext): List<Int> {
    val testClass = context.requiredTestClass
    val tests =
      AnnotationSupport.findAnnotatedMethods(
        testClass,
        Testable::class.java,
        HierarchyTraversalMode.TOP_DOWN,
      )
    val session = session(context)
    val configurations =
      if (tests.isEmpty() || sharesEnvironments(context)) {
        session.plan(testClass, null)
      } else {
        tests.flatMap { session.plan(testClass, it) }
      }
    return configurations.map { it.apiLevel }.distinct().sorted()
  }

  /** Makes everything in the context, a run of a class for one SDK, run on that SDK. */
  fun setClassSdk(context: ExtensionContext, apiLevel: Int) {
    context.getStore(NAMESPACE).put(CLASS_SDK, apiLevel)
  }

  private fun classSdk(context: ExtensionContext): Int? =
    context.getStore(NAMESPACE).get(CLASS_SDK, Int::class.javaObjectType)

  /**
   * Returns whether the tests of the class share their environments: if something of the class runs
   * outside of a single test, or its test instance outlives a test.
   */
  fun sharesEnvironments(context: ExtensionContext): Boolean {
    val testClass = context.requiredTestClass
    return context.testInstanceLifecycle.orElse(null) == Lifecycle.PER_CLASS ||
      listOf(BeforeAll::class.java, AfterAll::class.java).any {
        AnnotationSupport.findAnnotatedMethods(testClass, it, HierarchyTraversalMode.TOP_DOWN)
          .isNotEmpty()
      }
  }

  /** Returns why the class or the test can't run, if no SDK is selected for it. */
  fun skipReason(context: ExtensionContext): String? {
    val testClass = context.testClass.orElse(null)
    val testMethod = context.testMethod.orElse(null)
    // A class only needs an SDK itself if its tests share environments, and has them all before
    // it runs for one of them.
    val needsSdk =
      testClass != null &&
        (testMethod != null || sharesEnvironments(context) && classSdk(context) == null)
    val hasNoSdk =
      needsSdk &&
        try {
          of(context, testClass, testMethod).isEmpty()
        } catch (_: IllegalArgumentException) {
          // An invalid configuration is reported when the test runs.
          false
        }
    return if (hasNoSdk) noSdkReason(context, testClass, testMethod) else null
  }

  /** Returns why no SDK is selected for the class or the test. */
  fun noSdkReason(context: ExtensionContext, testClass: Class<*>, testMethod: Method?): String {
    val subject = if (testMethod != null) describe(testClass, testMethod) else testClass.name
    val classSdk = classSdk(context)
    val enabledSdks = System.getProperty("robolectric.enabledSdks")
    return when {
      classSdk != null -> "$subject is not configured for the Android SDK $classSdk"
      enabledSdks.isNullOrBlank() ->
        "None of the Android SDKs that $subject is configured for can run on this JVM"
      else ->
        "None of the Android SDKs that $subject is configured for is enabled by " +
          "robolectric.enabledSdks=$enabledSdks"
    }
  }

  fun describe(testClass: Class<*>, testMethod: Method) = "${testClass.name}.${testMethod.name}"
}

/** The SDK of the environment that a planned configuration is for. */
internal val Configuration.apiLevel: Int
  get() = get(Config::class.java).sdk.single()
