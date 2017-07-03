package org.robolectric;

/**
 * Class that encapsulates reading global configuration options from the Java system properties file.
 */
public class RoboSettings {

  private static String mavenRepositoryId;
  private static String mavenRepositoryUrl;
  private static boolean useGlobalScheduler;

  static {
    mavenRepositoryId = System.getProperty("robolectric.dependency.repo.id", "sonatype");
    mavenRepositoryUrl = System.getProperty("robolectric.dependency.repo.url", "https://oss.sonatype.org/content/groups/public/");
    useGlobalScheduler = Boolean.getBoolean("robolectric.scheduling.global");
  }

  public static String getMavenRepositoryId() {
    return mavenRepositoryId;
  }

  public static void setMavenRepositoryId(String mavenRepositoryId) {
    RoboSettings.mavenRepositoryId = mavenRepositoryId;
  }

  public static String getMavenRepositoryUrl() {
    return mavenRepositoryUrl;
  }

  public static void setMavenRepositoryUrl(String mavenRepositoryUrl) {
    RoboSettings.mavenRepositoryUrl = mavenRepositoryUrl;
  }

  public static boolean isUseGlobalScheduler() {
    return useGlobalScheduler;
  }

  public static void setUseGlobalScheduler(boolean useGlobalScheduler) {
    RoboSettings.useGlobalScheduler = useGlobalScheduler;
  }
}
