package org.robolectric.annotation.processing.ksp

/**
 * Data model for a shadow class discovered by the KSP processor.
 *
 * [actualIsPublic] is `true` when the shadowed ("actual") class was resolved on the processor
 * classpath and is public. Together with [isInAndroidSdk] it gates `shadowOf()` helper generation,
 * mirroring the javac processor: a `shadowOf(Actual)` method is only emitted when `Actual` is
 * referenceable from the generated `Shadows` class.
 */
internal data class ShadowInfo(
  val actualName: String,
  val shadowBinaryName: String,
  val shadowPickerBinaryName: String?,
  val isInAndroidSdk: Boolean,
  val minSdk: Int,
  val maxSdk: Int,
  val actualIsPublic: Boolean = false,
)

/**
 * Data model for a resetter method discovered inside a shadow class.
 *
 * [shadowClassName] and [actualBinaryName] are the binary names of the shadow class and the class
 * it shadows; the generated `reset(ClassTracker)` only invokes [methodCall] when one of them has
 * been loaded, mirroring the javac annotation processor.
 */
internal data class ResetterInfo(
  val methodCall: String,
  val minSdk: Int,
  val maxSdk: Int,
  val shadowClassName: String,
  val actualBinaryName: String,
)
