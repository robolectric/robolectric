package org.robolectric;

public class SdkConfig {
    private final String artifactVersionString;

    public SdkConfig(String artifactVersionString) {
        this.artifactVersionString = artifactVersionString;
    }

    public String getArtifactVersionString() {
        return artifactVersionString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SdkConfig sdkConfig = (SdkConfig) o;

        if (!artifactVersionString.equals(sdkConfig.artifactVersionString)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return artifactVersionString.hashCode();
    }
}
