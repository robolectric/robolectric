package org.robolectric.shadows;

import android.annotation.RequiresApi;
import android.app.LocaleManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build.VERSION_CODES;
import android.os.LocaleList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.versioning.AndroidVersions.U;

/** Shadow of {@link LocaleManager} */
@Implements(value = LocaleManager.class, minSdk = VERSION_CODES.TIRAMISU, isInAndroidSdk = false)
public class ShadowLocaleManager {

  private static final Map<String, LocaleList> appLocales = new HashMap<>();
  private static final Set<String> packagesInstalledByCaller = new HashSet<>();
  private static boolean enforceInstallerCheck;

  @Resetter
  public static void reset() {
    appLocales.clear();
    packagesInstalledByCaller.clear();
    enforceInstallerCheck = false;
  }

  /**
   * Returns the stored locales from in-memory map for the given package when {@link
   * LocaleManager#getApplicationLocales} is invoked in source code via tests.
   *
   * <p>If {@link #enforceInstallerCheck} is set as true, this method will return locales only if
   * the package is installed by caller. Else it will throw a {@link SecurityException}.
   *
   * <p>Adds the package name in a set to record that this method was invoked for given package.
   *
   * @see #enforceInstallerCheck
   * @see #setCallerAsInstallerForPackage
   */
  @RequiresApi(api = VERSION_CODES.N)
  @Implementation
  protected LocaleList getApplicationLocales(String packageName) {
    if (enforceInstallerCheck) {
      if (!packagesInstalledByCaller.contains(packageName)) {
        throw new SecurityException(
            "Caller does not have permission to query locales for package " + packageName);
      }
    }
    return appLocales.getOrDefault(packageName, LocaleList.getEmptyLocaleList());
  }

  /**
   * Stores the passed locales for the given package in-memory.
   *
   * <p>Starting in Android U, this method just invokes the 3-arg version (below).
   *
   * <p>Use this method in tests to substitute call for {@link
   * LocaleManager#setApplicationLocales(LocaleList)}.
   */
  @Implementation(maxSdk = VERSION_CODES.TIRAMISU)
  protected void setApplicationLocales(String packageName, LocaleList locales) {
    appLocales.put(packageName, locales);
  }

  /**
   * Stores the passed locales for the given package in-memory.
   *
   * <p>Use this method in tests to substitute call for {@link
   * LocaleManager#setApplicationLocales(LocaleList)}.
   */
  @Implementation(minSdk = U.SDK_INT)
  protected void setApplicationLocales(
      String packageName, LocaleList locales, boolean fromDelegate) {
    setApplicationLocales(packageName, locales);
  }

  @RequiresApi(api = VERSION_CODES.N)
  @Implementation
  protected LocaleList getSystemLocales() {
    Configuration configuration = Resources.getSystem().getConfiguration();
    if (configuration != null) {
      return configuration.getLocales();
    }
    return LocaleList.getEmptyLocaleList();
  }

  /**
   * Sets the value of {@link #enforceInstallerCheck}.
   *
   * <p>Set this to true if the intention to invoke {@link #getApplicationLocales} is as an
   * installer of the app.
   *
   * <p>In order to mark apps as installed by the caller(installer), use {@link
   * #setCallerAsInstallerForPackage}.
   */
  public void enforceInstallerCheck(boolean value) {
    enforceInstallerCheck = value;
  }

  /**
   * Sets the caller as the installer of the given package.
   *
   * <p>We are explicitly not storing the package name of the installer. It's implied that the test
   * app is the installer if using this method.
   */
  public void setCallerAsInstallerForPackage(String packageName) {
    packagesInstalledByCaller.add(packageName);
  }
}
