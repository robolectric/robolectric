package org.robolectric.plugins;

import static android.os.Build.VERSION_CODES.Q;
import static org.robolectric.plugins.DefaultSdkProvider.RUNNING_JAVA_VERSION;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;
import java.util.TreeMap;
import org.robolectric.pluginapi.Sdk;

class SdksFileSdkProvider {
  static boolean populateBuildInjectedSdks(TreeMap<Integer, Sdk> knownSdks) {
    final Properties sdkProps = new Properties();
    try (InputStream in = SdksFileSdkProvider.class.getClassLoader()
        .getResourceAsStream("org.robolectric.sdks.properties")) {
      if (in == null) {
        return false;
      }
      sdkProps.load(in);
    } catch (IOException e) {
      return false;
    }

    for (String key : sdkProps.stringPropertyNames()) {
      int apiLevel = Integer.parseInt(key);
      Path jarFile = new File(sdkProps.getProperty(key)).toPath();
      knownSdks.put(apiLevel, new InjectedSdk(apiLevel, jarFile));
    }
    return true;
  }

  static class InjectedSdk extends Sdk {

    private final Path jarPath;

    protected InjectedSdk(int apiLevel, Path jarPath) {
      super(apiLevel);
      this.jarPath = jarPath;
    }

    @Override
    public String getAndroidVersion() {
      return null;
    }

    @Override
    public String getAndroidCodeName() {
      return null;
    }

    @Override
    public Path getJarPath() {
      return jarPath;
    }

    @Override
    public boolean isSupported() {
      return RUNNING_JAVA_VERSION >= 9 || getApiLevel() >= Q;
    }

    @Override
    public String getUnsupportedMessage() {
      return String.format(
          Locale.getDefault(),
          "Android SDK %d requires Java %d (have Java %d)",
          getApiLevel(),
          9,
          RUNNING_JAVA_VERSION);
    }

    @Override
    public void verifySupportedSdk(String testClassName) {
      if (isKnown() && !isSupported()) {
        throw new UnsupportedOperationException(
            "Failed to create a Robolectric sandbox: " + getUnsupportedMessage());
      }
    }
  }

}
