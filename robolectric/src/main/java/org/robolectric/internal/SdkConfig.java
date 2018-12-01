package org.robolectric.internal;

import android.os.Build;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.robolectric.internal.dependency.DependencyJar;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;

public class SdkConfig implements Comparable<SdkConfig> {

  private static final Pattern JAR_URL_PATTERN = Pattern.compile("^jar:file:(.*)!/");
  private static final Set<SdkConfig> SUPPORTED_APIS = findSdks();
  // =
  //     Collections.unmodifiableMap(
  //         new HashMap<Integer, SdkVersion>() {
  //           {
  //             addSdk(Build.VERSION_CODES.JELLY_BEAN, "4.1.2_r1", "r1", "REL");
  //             addSdk(Build.VERSION_CODES.JELLY_BEAN_MR1, "4.2.2_r1.2", "r1", "REL");
  //             addSdk(Build.VERSION_CODES.JELLY_BEAN_MR2, "4.3_r2", "r1", "REL");
  //             addSdk(Build.VERSION_CODES.KITKAT, "4.4_r1", "r2", "REL");
  //             addSdk(Build.VERSION_CODES.LOLLIPOP, "5.0.2_r3", "r0", "REL");
  //             addSdk(Build.VERSION_CODES.LOLLIPOP_MR1, "5.1.1_r9", "r2", "REL");
  //             addSdk(Build.VERSION_CODES.M, "6.0.1_r3", "r1", "REL");
  //             addSdk(Build.VERSION_CODES.N, "7.0.0_r1", "r1", "REL");
  //             addSdk(Build.VERSION_CODES.N_MR1, "7.1.0_r7", "r1", "REL");
  //             addSdk(Build.VERSION_CODES.O, "8.0.0_r4", "r1", "REL");
  //             addSdk(Build.VERSION_CODES.O_MR1, "8.1.0", "4611349", "REL");
  //             addSdk(Build.VERSION_CODES.P, "9", "4913185-2", "REL");
  //           }
  //
  //           private void addSdk(
  //               int sdkVersion,
  //               String androidVersion,
  //               String frameworkSdkBuildVersion,
  //               String codeName) {
  //             put(sdkVersion, new SdkVersion(androidVersion, frameworkSdkBuildVersion, codeName));
  //           }
  //         });

  public static final int FALLBACK_SDK_VERSION = Build.VERSION_CODES.JELLY_BEAN;
  public static final SdkConfig MAX_SDK_CONFIG = Collections.max(SUPPORTED_APIS);
  public static final int MAX_SDK_VERSION = MAX_SDK_CONFIG.apiLevel;

  private final int apiLevel;
  private final File jarFile;
  private String prefix;

  public static Set<SdkConfig> getSupportedApis() {
    return SUPPORTED_APIS;
  }

  private static Set<SdkConfig> findSdks() {
    Set<SdkConfig> supportedApis = new HashSet<>();
    try {
      Enumeration<URL> sdkPropsEnum = SdkConfig.class.getClassLoader()
          .getResources("META-INF/android-sdk.properties");
      while (sdkPropsEnum.hasMoreElements()) {
        URL url = sdkPropsEnum.nextElement();
        Properties sdkProps = new Properties();
        sdkProps.load(url.openStream());

        int apiLevel = Integer.parseInt(sdkProps.getProperty("sdk"));
        String sdkPath = sdkProps.getProperty("sdk");

        String urlString = url.toExternalForm();
        Matcher matcher = JAR_URL_PATTERN.matcher(urlString);
        if (!matcher.find()) {
          throw new IllegalStateException("huh? " + urlString);
        }
        File jarFile = new File(matcher.group(1));
        supportedApis.add(new SdkConfig(apiLevel, jarFile, sdkPath));
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    return Collections.unmodifiableSet(supportedApis);
  }

  public SdkConfig(int apiLevel) {
    this(apiLevel, null, null);
  }

  public SdkConfig(int apiLevel, File jarFile, String prefix) {
    this.apiLevel = apiLevel;
    this.jarFile = jarFile;
    this.prefix = prefix;
  }

  public int getApiLevel() {
    return apiLevel;
  }

  public String getAndroidVersion() {
    return getSdkVersion().androidVersion;
  }

  public String getAndroidCodeName() {
    return getSdkVersion().codeName;
  }

  public DependencyJar getAndroidSdkDependency() {
    return createDependency("org.robolectric", "android-all", getSdkVersion().getAndroidVersion() + "-robolectric-" + getSdkVersion().getRobolectricVersion(), null);
  }

  @Override
  public boolean equals(Object that) {
    return that == this || (that instanceof SdkConfig && ((SdkConfig) that).apiLevel == (apiLevel));
  }

  @Override
  public int hashCode() {
    return apiLevel;
  }

  @Override
  public String toString() {
    return "API Level " + apiLevel;
  }

  @Override
  public int compareTo(@Nonnull SdkConfig o) {
    return apiLevel - o.apiLevel;
  }

  private SdkVersion getSdkVersion() {
    throw new UnsupportedOperationException();
    // final SdkVersion sdkVersion = SUPPORTED_APIS.get(apiLevel);
    // if (sdkVersion == null) {
    //   throw new UnsupportedOperationException("Robolectric does not support API level " + apiLevel + ".");
    // }
    // return sdkVersion;
  }

  private DependencyJar createDependency(String groupId, String artifactId, String version, String classifier) {
    return new DependencyJar(groupId, artifactId, version, classifier);
  }

  public URL getClassPathUrl() {
    try {
      return new URL("jar:file:" + jarFile.getPath() + "!/");
    } catch (MalformedURLException e) {
      throw new IllegalStateException(e);
    }
  }

  public String getPrefix() {
    return prefix;
  }

  public FsFile getBaseFsFile() {
    return Fs.fromURL(getClassPathUrl()).join(getPrefix());
  }

  public File getJarFile() {
    return jarFile;
  }

  private static final class SdkVersion {
    private final String androidVersion;
    private final String robolectricVersion;
    private final String codeName;

    SdkVersion(String androidVersion, String robolectricVersion, String codeName) {
      this.androidVersion = androidVersion;
      this.robolectricVersion = robolectricVersion;
      this.codeName = codeName;
    }

    @Override
    public boolean equals(Object that) {
      return that == this || (that instanceof SdkVersion && isEqualTo((SdkVersion) that));
    }

    @SuppressWarnings("ReferenceEquality")
    public boolean isEqualTo(SdkVersion that) {
      return that == this ||
          (Objects.equals(that.androidVersion, androidVersion) &&
              Objects.equals(that.robolectricVersion, robolectricVersion));
    }

    @Override
    public int hashCode() {
      return androidVersion.hashCode() * 31 + robolectricVersion.hashCode();
    }

    public String getAndroidVersion() {
      return androidVersion;
    }

    public String getRobolectricVersion() {
      return robolectricVersion;
    }
  }
}
