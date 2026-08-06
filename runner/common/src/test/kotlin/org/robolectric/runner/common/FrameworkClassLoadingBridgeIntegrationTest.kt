package org.robolectric.runner.common

import com.example.LoaderBridgeFixture
import com.google.common.truth.Truth.assertThat
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import org.junit.jupiter.api.Test

@ExperimentalRunnerApi
class FrameworkClassLoadingBridgeIntegrationTest {

  @Test
  fun `integration can bootstrap transformed class bytes from framework classloader`() {
    val targetClass = LoaderBridgeFixture::class.java
    val resourceName = targetClass.name.replace('.', '/') + ".class"
    val sourceLoader = TransformingResourceClassLoader(targetClass.classLoader, resourceName)
    val bridge =
      object : FrameworkClassLoadingBridge {
        override fun resolveSourceClassLoader(testClass: Class<*>): ClassLoader? = sourceLoader
      }
    val transformedBytes =
      bridge.openClassBytes(resourceName, sourceLoader)!!.use { it.readBytes() }
    assertThat(transformedBytes.indexOfSubsequence("modified".toByteArray(StandardCharsets.UTF_8)))
      .isAtLeast(0)

    val integration =
      RobolectricIntegrationBuilder()
        .sandboxSharing(SandboxSharingStrategy.PER_TEST)
        .classLoadingBridge(bridge)
        .build() as DefaultRobolectricIntegration
    val method = targetClass.getMethod("marker")

    integration.beforeClass(targetClass)
    integration.beforeTest(targetClass, method)
    val value =
      try {
        integration.executeInSandbox(targetClass, method) { context ->
          val bootstrappedClass = TestBootstrapper.bootstrapClass<Any>(context.sandbox, targetClass)
          val testInstance = TestBootstrapper.createTestInstance(bootstrappedClass)
          TestBootstrapper.invokeTestMethod(testInstance, method, emptyArray()) as String
        }
      } finally {
        integration.afterTest(targetClass, method, success = true)
        integration.afterClass(targetClass)
      }

    assertThat(value).isEqualTo("modified")
  }

  private inner class TransformingResourceClassLoader(
    parent: ClassLoader,
    private val targetResource: String,
  ) : ClassLoader(parent) {
    override fun getResourceAsStream(name: String): InputStream? {
      val stream = super.getResourceAsStream(name) ?: return null
      return if (name == targetResource) {
        val original = stream.use { it.readBytes() }
        val transformed = replaceUtf8Literal(original, "original", "modified")
        ByteArrayInputStream(transformed)
      } else {
        stream
      }
    }
  }

  private fun replaceUtf8Literal(classBytes: ByteArray, from: String, to: String): ByteArray {
    check(from.length == to.length) { "Replacement strings must have equal length" }
    val fromBytes = from.toByteArray(StandardCharsets.UTF_8)
    val toBytes = to.toByteArray(StandardCharsets.UTF_8)
    var replaced = 0
    return classBytes.copyOf().also {
      var index = it.indexOfSubsequence(fromBytes)
      while (index >= 0) {
        System.arraycopy(toBytes, 0, it, index, toBytes.size)
        replaced++
        index = it.indexOfSubsequence(fromBytes, index + toBytes.size)
      }
      check(replaced > 0) { "Could not find '$from' in class bytes" }
    }
  }

  private fun ByteArray.indexOfSubsequence(target: ByteArray, startIndex: Int = 0): Int {
    if (target.isEmpty() || target.size > size) {
      return -1
    }
    var foundIndex = -1
    for (start in startIndex..(size - target.size)) {
      var matches = true
      for (offset in target.indices) {
        if (this[start + offset] != target[offset]) {
          matches = false
          break
        }
      }
      if (matches) {
        foundIndex = start
        break
      }
    }
    return foundIndex
  }
}
