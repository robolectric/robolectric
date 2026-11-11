package org.robolectric.junit.platform

import org.junit.platform.engine.TestDescriptor
import org.junit.platform.engine.TestTag
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor
import org.junit.platform.launcher.TestIdentifier

internal class MirrorDescriptor(
  id: UniqueId,
  identifier: TestIdentifier,
  private val suffix: String = "",
) : AbstractTestDescriptor(id, identifier.displayName + suffix, identifier.source.orElse(null)) {
  private val descriptorType =
    when {
      identifier.isTest && identifier.isContainer -> TestDescriptor.Type.CONTAINER_AND_TEST
      identifier.isTest -> TestDescriptor.Type.TEST
      else -> TestDescriptor.Type.CONTAINER
    }
  private val descriptorTags = identifier.tags
  private val legacyName = identifier.legacyReportingName + suffix

  override fun getType(): TestDescriptor.Type = descriptorType

  override fun getTags(): Set<TestTag> = descriptorTags

  override fun mayRegisterTests(): Boolean =
    uniqueId.lastSegment.type in
      setOf("test-template", "test-factory", "class-template", "dynamic-container")

  override fun getLegacyReportingName(): String = legacyName
}
