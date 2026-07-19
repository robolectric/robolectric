package org.robolectric.runner.common

import java.io.InputStream

/**
 * Bridge for frameworks that run tests from custom classloaders or transformed bytecode.
 *
 * A framework can provide a custom implementation to direct `:runner:common` to the source
 * classloader and byte stream for test classes before Robolectric bootstraps them into sandbox.
 *
 * Why this works with Robolectric sandbox:
 * - the sandbox classloader still defines and instruments classes
 * - this bridge only supplies *source bytes* to that loader when requested
 * - shadowing and Robolectric internals remain unchanged because class definition still happens
 *   inside sandbox.
 */
@ExperimentalRunnerApi
interface FrameworkClassLoadingBridge {
  /**
   * Resolves the source classloader for a given test class.
   *
   * The returned classloader is used as the source for class bytes when Robolectric bootstraps the
   * test class into sandbox classloader.
   */
  fun resolveSourceClassLoader(testClass: Class<*>): ClassLoader? = testClass.classLoader

  /**
   * Opens the byte stream for a class resource name (for example `a/b/MyTest.class`).
   *
   * Returning null falls back to Robolectric's default class/resource lookup.
   */
  fun openClassBytes(classResourceName: String, sourceClassLoader: ClassLoader?): InputStream? =
    sourceClassLoader?.getResourceAsStream(classResourceName)
}

@ExperimentalRunnerApi object DefaultFrameworkClassLoadingBridge : FrameworkClassLoadingBridge
