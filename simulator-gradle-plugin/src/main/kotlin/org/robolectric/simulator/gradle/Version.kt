package org.robolectric.simulator.gradle

internal data class Version(private val version: String) : Comparable<Version> {
  private val major: Int
  private val minor: Int
  private val patch: Int
  private val tag: String

  init {
    val versionAndTag = version.split('-', limit = 2)
    val parts = versionAndTag[0].split('.').map { it.toIntOrNull() ?: 0 }

    this.major = parts.getOrElse(0) { 0 }
    this.minor = parts.getOrElse(1) { 0 }
    this.patch = parts.getOrElse(2) { 0 }
    this.tag = versionAndTag.getOrElse(1) { "" }
  }

  override fun compareTo(other: Version): Int {
    val result = compareValuesBy(this, other, Version::major, Version::minor, Version::patch)

    return when {
      result != 0 -> result
      tag.isNotEmpty() && other.tag.isEmpty() -> -1
      tag.isEmpty() && other.tag.isNotEmpty() -> 1
      else -> tag.compareTo(other.tag, ignoreCase = true)
    }
  }

  override fun toString(): String {
    return version
  }
}
