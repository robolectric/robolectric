package org.robolectric.junit.jupiter.internal

import java.util.stream.Stream
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ClassTemplateInvocationContext
import org.junit.jupiter.api.extension.ClassTemplateInvocationContextProvider
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestTemplateInvocationContext
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider
import org.junit.platform.commons.support.AnnotationSupport
import org.robolectric.junit.jupiter.RobolectricSdkTest
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration
import org.robolectric.runner.common.ExperimentalRunnerApi

/**
 * Runs a test that is a [RobolectricSdkTest] once for each Android SDK that is selected for it, and
 * a class that is one once for each SDK that is selected for its tests.
 */
@OptIn(ExperimentalRunnerApi::class)
internal class SdkTestInvocationContextProvider :
  TestTemplateInvocationContextProvider, ClassTemplateInvocationContextProvider {
  override fun supportsClassTemplate(context: ExtensionContext): Boolean =
    AnnotationSupport.isAnnotated(context.requiredTestClass, RobolectricSdkTest::class.java)

  override fun provideClassTemplateInvocationContexts(
    context: ExtensionContext
  ): Stream<ClassTemplateInvocationContext> =
    Plans.sdksOfClass(context).map<Int, ClassTemplateInvocationContext> { SdkRun(it) }.stream()

  // A class that no SDK is selected for has no tests to run, which is not a failure.
  override fun mayReturnZeroClassTemplateInvocationContexts(context: ExtensionContext): Boolean =
    true

  private class SdkRun(private val apiLevel: Int) : ClassTemplateInvocationContext {
    override fun getDisplayName(invocationIndex: Int): String = "SDK $apiLevel"

    override fun prepareInvocation(context: ExtensionContext) {
      Plans.setClassSdk(context, apiLevel)
    }
  }

  override fun supportsTestTemplate(context: ExtensionContext): Boolean =
    context.testMethod
      .map { AnnotationSupport.isAnnotated(it, RobolectricSdkTest::class.java) }
      .orElse(false)

  override fun provideTestTemplateInvocationContexts(
    context: ExtensionContext
  ): Stream<TestTemplateInvocationContext> {
    val testMethod = context.requiredTestMethod
    val configurations = Plans.of(context, context.requiredTestClass, testMethod)
    val alwaysMarkSdk =
      java.lang.Boolean.getBoolean("robolectric.alwaysIncludeVariantMarkersInTestName")
    return configurations
      .mapIndexed<Configuration, TestTemplateInvocationContext> { index, configuration ->
        // As the test runner does, the last SDK goes unmarked, so that tools find the test by name.
        val marksSdk = alwaysMarkSdk || index < configurations.lastIndex
        SdkInvocation(
          testMethod.name + if (marksSdk) "[${configuration.apiLevel}]" else "",
          configuration,
        )
      }
      .stream()
  }

  private class SdkInvocation(private val name: String, private val configuration: Configuration) :
    TestTemplateInvocationContext {
    override fun getDisplayName(invocationIndex: Int): String = name

    override fun getAdditionalExtensions(): List<Extension> =
      listOf(
        BeforeEachCallback { context ->
          Environments.setSdkTestConfiguration(context, configuration)
        }
      )
  }
}
