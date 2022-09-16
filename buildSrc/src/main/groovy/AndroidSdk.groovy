class AndroidSdk implements Comparable<AndroidSdk> {
    static final PREINSTRUMENTED_VERSION = 4

    static final JELLY_BEAN = new AndroidSdk(16, "4.1.2_r1", "r1")
    static final JELLY_BEAN_MR1 = new AndroidSdk(17, "4.2.2_r1.2", "r1")
    static final JELLY_BEAN_MR2 = new AndroidSdk(18, "4.3_r2", "r1")
    static final KITKAT = new AndroidSdk(19, "4.4_r1", "r2")
    static final LOLLIPOP = new AndroidSdk(21, "5.0.2_r3", "r0")
    static final LOLLIPOP_MR1 = new AndroidSdk(22, "5.1.1_r9", "r2")
    static final M = new AndroidSdk(23, "6.0.1_r3", "r1")
    static final N = new AndroidSdk(24, "7.0.0_r1", "r1")
    static final N_MR1 = new AndroidSdk(25, "7.1.0_r7", "r1")
    static final O = new AndroidSdk(26, "8.0.0_r4", "r1")
    static final O_MR1 = new AndroidSdk(27, "8.1.0", "4611349")
    static final P = new AndroidSdk(28, "9", "4913185-2");
    static final Q = new AndroidSdk(29, "10", "5803371");
    static final R = new AndroidSdk(30, "11", "6757853");
    static final S = new AndroidSdk(31, "12", "7732740");
    static final S_V2 = new AndroidSdk(32, "12.1", "8229987");
    static final TIRAMISU = new AndroidSdk(33, "13", "9030017");


    static final List<AndroidSdk> ALL_SDKS = [
            JELLY_BEAN, JELLY_BEAN_MR1, JELLY_BEAN_MR2, KITKAT,
            LOLLIPOP, LOLLIPOP_MR1, M, N, N_MR1, O, O_MR1, P, Q, R, S, S_V2,
            TIRAMISU
    ]

    static final MAX_SDK = Collections.max(ALL_SDKS)

    public final int apiLevel
    private final String androidVersion
    private final String frameworkSdkBuildVersion

    AndroidSdk(int apiLevel, String androidVersion, String frameworkSdkBuildVersion) {
        this.apiLevel = apiLevel
        this.androidVersion = androidVersion
        this.frameworkSdkBuildVersion = frameworkSdkBuildVersion
    }

    String getGroupId() {
        return "org.robolectric"
    }

    String getArtifactId() {
        return "android-all"
    }

    String getPreinstrumentedArtifactId() {
        return "android-all-instrumented"
    }

    String getVersion() {
        return "${androidVersion}-robolectric-${frameworkSdkBuildVersion}"
    }

    String getPreinstrumentedVersion() {
        return "${androidVersion}-robolectric-${frameworkSdkBuildVersion}-i${PREINSTRUMENTED_VERSION}"
    }

    String getCoordinates() {
        return "${groupId}:${artifactId}:${version}"
    }

    String getPreinstrumentedCoordinates() {
        return "${groupId}:${preinstrumentedArtifactId}:${preinstrumentedVersion}"
    }

    String getJarFileName() {
        return "android-all-${androidVersion}-robolectric-${frameworkSdkBuildVersion}.jar"
    }

    String getPreinstrumentedJarFileName() {
        return "android-all-instrumented-${preinstrumentedVersion}.jar"
    }

    @Override
    int compareTo(AndroidSdk other) {
        return apiLevel - other.apiLevel
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        AndroidSdk that = (AndroidSdk) o

        if (apiLevel != that.apiLevel) return false

        return true
    }

    int hashCode() {
        return apiLevel
    }
}
