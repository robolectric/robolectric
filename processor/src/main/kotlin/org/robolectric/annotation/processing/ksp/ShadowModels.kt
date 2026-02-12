package org.robolectric.annotation.processing.ksp

/** Data model for a shadow class discovered by the KSP processor. */
internal data class ShadowInfo(
  val actualName: String,
  val shadowBinaryName: String,
  val shadowPickerBinaryName: String?,
  val isInAndroidSdk: Boolean,
  val minSdk: Int,
  val maxSdk: Int,
)

/** Data model for a resetter method discovered inside a shadow class. */
internal data class ResetterInfo(val methodCall: String, val minSdk: Int, val maxSdk: Int)
