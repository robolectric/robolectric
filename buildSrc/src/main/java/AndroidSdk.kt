class AndroidSdk(
  val apiLevel: Int,
  val androidVersion: String,
  private val frameworkSdkBuildVersion: String,
) : Comparable<AndroidSdk> {
  val groupId: String
    get() = "org.robolectric"

  val artifactId: String
    get() = "android-all"

  val preinstrumentedArtifactId: String
    get() = "android-all-instrumented"

  val version: String
    get() = "$androidVersion-robolectric-$frameworkSdkBuildVersion"

  val preinstrumentedVersion: String
    get() = "$version-i$PREINSTRUMENTED_VERSION"

  val coordinates: String
    get() = "$groupId:$artifactId:$version"

  val preinstrumentedCoordinates: String
    get() = "$groupId:$preinstrumentedArtifactId:$preinstrumentedVersion"

  val jarFileName: String
    get() = "android-all-$version.jar"

  val preinstrumentedJarFileName: String
    get() = "android-all-instrumented-$preinstrumentedVersion.jar"

  override fun compareTo(other: AndroidSdk): Int {
    return apiLevel - other.apiLevel
  }

  override fun equals(other: Any?): Boolean {
    return when {
      this === other -> true
      other !is AndroidSdk -> false
      apiLevel != other.apiLevel -> false
      else -> true
    }
  }

  override fun hashCode(): Int {
    return apiLevel
  }

  companion object {
    private const val PREINSTRUMENTED_VERSION = 6

    @JvmField val LOLLIPOP = AndroidSdk(21, "5.0.2_r3", "r0")

    @JvmField val LOLLIPOP_MR1 = AndroidSdk(22, "5.1.1_r9", "r2")

    @JvmField val M = AndroidSdk(23, "6.0.1_r3", "r1")

    @JvmField val N = AndroidSdk(24, "7.0.0_r1", "r1")

    @JvmField val N_MR1 = AndroidSdk(25, "7.1.0_r7", "r1")

    @JvmField val O = AndroidSdk(26, "8.0.0_r4", "r1")

    @JvmField val O_MR1 = AndroidSdk(27, "8.1.0", "4611349")

    @JvmField val P = AndroidSdk(28, "9", "4913185-2")

    @JvmField val Q = AndroidSdk(29, "10", "5803371")

    @JvmField val R = AndroidSdk(30, "11", "6757853")

    @JvmField val S = AndroidSdk(31, "12", "7732740")

    @JvmField val S_V2 = AndroidSdk(32, "12.1", "8229987")

    @JvmField val TIRAMISU = AndroidSdk(33, "13", "9030017")

    @JvmField val U = AndroidSdk(34, "14", "10818077")

    @JvmField
    val ALL_SDKS =
      listOf(LOLLIPOP, LOLLIPOP_MR1, M, N, N_MR1, O, O_MR1, P, Q, R, S, S_V2, TIRAMISU, U)

    @JvmField val MAX_SDK = ALL_SDKS.maxBy { it.apiLevel }
  }
}
