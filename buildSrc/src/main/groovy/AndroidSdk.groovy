class AndroidSdk implements Comparable<AndroidSdk> {
    static final JELLY_BEAN = new AndroidSdk(16, "4.1.2_r1", 0)
    static final JELLY_BEAN_MR1 = new AndroidSdk(17, "4.2.2_r1.2", 0)
    static final JELLY_BEAN_MR2 = new AndroidSdk(18, "4.3_r2", 0)
    static final KITKAT = new AndroidSdk(19, "4.4_r1", 1)
    static final LOLLIPOP = new AndroidSdk(21, "5.0.0_r2", 1)
    static final LOLLIPOP_MR1 = new AndroidSdk(22, "5.1.1_r9", 1)
    static final M = new AndroidSdk(23, "6.0.1_r3", 0)
    static final N = new AndroidSdk(24, "7.0.0_r1", 0)
    static final N_MR1 = new AndroidSdk(25, "7.1.0_r7", 0)
    static final O = new AndroidSdk(26, "8.0.0_r4", 0)
    static final O_MR1 = new AndroidSdk(27, "8.1.0", 4402310)

    static final List<AndroidSdk> ALL_SDKS = [
            JELLY_BEAN, JELLY_BEAN_MR1, JELLY_BEAN_MR2, KITKAT,
            LOLLIPOP, LOLLIPOP_MR1, M, N, N_MR1, O, O_MR1
    ]

    static final MAX_SDK = Collections.max(ALL_SDKS)

    private final int apiLevel
    private final String androidVersion
    private final String frameworkSdkBuildVersion

    AndroidSdk(int apiLevel, String androidVersion, int frameworkSdkBuildVersion) {
        this.apiLevel = apiLevel
        this.androidVersion = androidVersion
        this.frameworkSdkBuildVersion = frameworkSdkBuildVersion
    }

    String getCoordinates() {
        return "org.robolectric:android-all:${androidVersion}-robolectric-${frameworkSdkBuildVersion}"
    }

    String getJarFileName() {
        return "android-all-${androidVersion}-robolectric-${frameworkSdkBuildVersion}.jar"
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