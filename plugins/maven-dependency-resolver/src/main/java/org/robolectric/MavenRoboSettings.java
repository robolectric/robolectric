package org.robolectric;

/**
 * Class that encapsulates reading global configuration options from the Java system properties file.
 *
 * @deprecated Don't put more stuff here.
 */
@Deprecated
public class MavenRoboSettings {

  private static String mavenRepositoryId;
  private static String mavenRepositoryUrl;
  private static String mavenRepositoryUserName;
  private static String mavenRepositoryPassword;

  static {
    mavenRepositoryId = System.getProperty("robolectric.dependency.repo.id", "mavenCentral");
    mavenRepositoryUrl =
        System.getProperty("robolectric.dependency.repo.url", "https://repo1.maven.org/maven2");
    mavenRepositoryUserName = System.getProperty("robolectric.dependency.repo.username");
    mavenRepositoryPassword = System.getProperty("robolectric.dependency.repo.password");
  }

  public static String getMavenRepositoryId() {
    return mavenRepositoryId;
  }

  public static void setMavenRepositoryId(String mavenRepositoryId) {
    MavenRoboSettings.mavenRepositoryId = mavenRepositoryId;
  }

  public static String getMavenRepositoryUrl() {
    return mavenRepositoryUrl;
  }

  public static void setMavenRepositoryUrl(String mavenRepositoryUrl) {
    MavenRoboSettings.mavenRepositoryUrl = mavenRepositoryUrl;
  }

  public static String getMavenRepositoryUserName() {
    return mavenRepositoryUserName;
  }

  public static void setMavenRepositoryUserName(String mavenRepositoryUserName) {
    MavenRoboSettings.mavenRepositoryUserName = mavenRepositoryUserName;
  }

  public static String getMavenRepositoryPassword() {
    return mavenRepositoryPassword;
  }

  public static void setMavenRepositoryPassword(String mavenRepositoryPassword) {
    MavenRoboSettings.mavenRepositoryPassword = mavenRepositoryPassword;
  }
}
