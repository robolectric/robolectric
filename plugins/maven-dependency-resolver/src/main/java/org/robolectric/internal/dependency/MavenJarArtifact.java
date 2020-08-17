package org.robolectric.internal.dependency;

/**
 * Encapsulates some parts of a Maven artifact. This assumes all artifacts are of type jar and do
 * not have a classifier.
 */
public class MavenJarArtifact {
  private final String groupId;
  private final String artifactId;
  private final String version;
  private final String jarPath;
  private final String jarSha1Path;
  private final String pomPath;
  private final String pomSha1Path;

  public MavenJarArtifact(DependencyJar dependencyJar) {
    this.groupId = dependencyJar.getGroupId();
    this.artifactId = dependencyJar.getArtifactId();
    this.version = dependencyJar.getVersion();
    String basePath =
        String.format("%s/%s/%s", this.groupId.replace(".", "/"), this.artifactId, this.version);
    String baseName = String.format("%s-%s", this.artifactId, this.version);
    this.jarPath = String.format("%s/%s.jar", basePath, baseName);
    this.jarSha1Path = String.format("%s/%s.jar.sha1", basePath, baseName);
    this.pomPath = String.format("%s/%s.pom", basePath, baseName);
    this.pomSha1Path = String.format("%s/%s.pom.sha1", basePath, baseName);
  }

  public String jarPath() {
    return jarPath;
  }

  public String jarSha1Path() {
    return jarSha1Path;
  }

  public String pomPath() {
    return pomPath;
  }

  public String pomSha1Path() {
    return pomSha1Path;
  }

  @Override
  public String toString() {
    // return coordinates
    return String.format("%s:%s:%s", groupId, artifactId, version);
  }
}
