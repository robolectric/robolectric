package org.robolectric.junit.platform

import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.platform.commons.support.AnnotationSupport
import org.junit.platform.engine.FilterResult
import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.support.descriptor.ClassSource
import org.junit.platform.engine.support.descriptor.MethodSource
import org.junit.platform.launcher.PostDiscoveryFilter
import org.robolectric.junit.jupiter.RobolectricExtension

/** Keeps Jupiter from also executing the classes owned by the Robolectric engine. */
public class RobolectricClaimFilter : PostDiscoveryFilter {
  override fun apply(descriptor: TestDescriptor): FilterResult {
    val engine = descriptor.uniqueId.segments.firstOrNull()?.value
    if (engine != "junit-jupiter") {
      return FilterResult.included("Not a Jupiter descriptor")
    }
    val source = descriptor.source.orElse(null)
    val testClass =
      when (source) {
        is ClassSource -> source.getJavaClass()
        is MethodSource -> source.getJavaClass()
        else -> null
      }
    return FilterResult.includedIf(
      testClass == null || !Claims.isClaimed(testClass),
      { "Not a Robolectric test" },
      { "Executed by the robolectric engine" },
    )
  }

  internal companion object {
    fun usesRobolectric(type: Class<*>): Boolean =
      AnnotationSupport.findRepeatableAnnotations(type, ExtendWith::class.java).any { annotation ->
        annotation.value.any { RobolectricExtension::class.java.isAssignableFrom(it.java) }
      } ||
        generateSequence(type) { it.superclass }
          .any { current ->
            current.declaredFields.any {
              AnnotationSupport.isAnnotated(it, RegisterExtension::class.java) &&
                RobolectricExtension::class.java.isAssignableFrom(it.type)
            }
          } ||
        (type.enclosingClass != null && usesRobolectric(type.enclosingClass))
  }
}
