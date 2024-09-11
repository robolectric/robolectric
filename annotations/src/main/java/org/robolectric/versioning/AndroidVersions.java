package org.robolectric.versioning;

/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static java.util.Arrays.asList;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import javax.annotation.Nullable;

/**
 * Android versioning is complicated.<br>
 * 1) There is a yearly letter release with an increasing of one alpha step each year A-> B, B-> C,
 * and so on. While commonly referenced these are not the release numbers. This class calls these
 * shortcodes. Also minor version number releases (usually within the same year) will start with the
 * same letter.<br>
 * 2) There is an SDK_INT field in android.os.Build.VERSION that tracks a version of the internal
 * SDK. While useful to track the actual released versions of Android, these are not the release
 * number. More importantly, android.os.Build.VERSION uses code names to describe future versions.
 * Multiple code names may be in development at once on different branches of Android.<br>
 * 3) There is a yearly release major number followed by a minor number, which may or may not be
 * used.<br>
 * 4) Relevant logic and reasoning should match androidx.core.os.BuildCompat.java with the caveat
 * that this class guess at the future release version number and short of the current dev branch.
 * <br>
 */
public final class AndroidVersions {

  private static boolean warnOnly;

  private AndroidVersions() {}

  /** Representation of an android release, one that has occurred, or is expected. */
  public abstract static class AndroidRelease implements Comparable<AndroidRelease> {

    /**
     * true if this release has already occurred, false otherwise. If unreleased, the getSdkInt may
     * still be that of the prior release.
     */
    public abstract int getSdkInt();

    /**
     * single character short code for the release, multiple characters for minor releases (only
     * minor version numbers increment - usually within the same year).
     */
    public abstract String getShortCode();

    /**
     * true if this release has already occurred, false otherwise. If unreleased, the getSdkInt will
     * guess at the likely sdk number. Your code will need to recompile if this value changes -
     * including most modern build tools; bazle, soong all are full build systems - and as such
     * organizations using them have no concerns.
     */
    public abstract boolean isReleased();

    /** major.minor version number as String. */
    public abstract String getVersion();

    /**
     * Implements comparable.
     *
     * @param other the object to be compared.
     * @return 1 if this is greater than other, 0 if equal, -1 if less
     * @throws IllegalStateException if other is not an instance of AndroidRelease.
     */
    @Override
    public int compareTo(AndroidRelease other) {
      if (other == null) {
        throw new IllegalStateException(
            "Only "
                + AndroidVersions.class.getName()
                + " should define Releases, illegal class "
                + other.getClass());
      }
      return Integer.compare(this.getSdkInt(), other.getSdkInt());
    }

    @Override
    public String toString() {
      return "Android "
          + (this.isReleased() ? "" : "Future ")
          + "Release: "
          + this.getVersion()
          + " ( sdk: "
          + this.getSdkInt()
          + " code: "
          + this.getShortCode()
          + " )";
    }
  }

  /** A released version of Android */
  public abstract static class AndroidReleased extends AndroidRelease {
    @Override
    public boolean isReleased() {
      return true;
    }
  }

  /** An in-development version of Android */
  public abstract static class AndroidUnreleased extends AndroidRelease {
    @Override
    public boolean isReleased() {
      return false;
    }
  }

  /**
   * Version: -1 <br>
   * ShortCode: "" <br>
   * SDK API Level: "" <br>
   * release: false <br>
   */
  public static final class Unbound extends AndroidUnreleased {

    public static final int SDK_INT = -1;

    public static final String SHORT_CODE = "_";

    public static final String VERSION = "_";

    @Override
    public int getSdkInt() {
      return SDK_INT;
    }

    @Override
    public String getShortCode() {
      return SHORT_CODE;
    }

    @Override
    public String getVersion() {
      return VERSION;
    }
  }

  /**
   * Version: 4.1 <br>
   * ShortCode: J <br>
   * SDK API Level: 16 <br>
   * release: true <br>
   */
  public static final class J extends AndroidReleased {

    public static final int SDK_INT = 16;

    public static final String SHORT_CODE = "J";

    public static final String VERSION = "4.1";

    @Override
    public int getSdkInt() {
      return SDK_INT;
    }

    @Override
    public String getShortCode() {
      return SHORT_CODE;
    }

    @Override
    public String getVersion() {
      return VERSION;
    }
  }

  /**
   * Version: 4.2 <br>
   * ShortCode: JMR1 <br>
   * SDK API Level: 17 <br>
   * release: true <br>
   */
  public static final class JMR1 extends AndroidReleased {

    public static final int SDK_INT = 17;

    public static final String SHORT_CODE = "JMR1";

    public static final String VERSION = "4.2";

    @Override
    public int getSdkInt() {
      return SDK_INT;
    }

    @Override
    public String getShortCode() {
      return SHORT_CODE;
    }

    @Override
    public String getVersion() {
      return VERSION;
    }
  }

  /**
   * Version: 4.3 <br>
   * ShortCode: JMR2 <br>
   * SDK API Level: 18 <br>
   * release: true <br>
   */
  public static final class JMR2 extends AndroidReleased {

    public static final int SDK_INT = 18;

    public static final String SHORT_CODE = "JMR2";

    public static final String VERSION = "4.3";

    @Override
    public int getSdkInt() {
      return SDK_INT;
    }

    @Override
    public String getShortCode() {
      return SHORT_CODE;
    }

    @Override
    public String getVersion() {
      return VERSION;
    }
  }

  /**
   * Version: 4.4 <br>
   * ShortCode: K <br>
   * SDK API Level: 19 <br>
   * release: true <br>
   */
  public static final class K extends AndroidReleased {

    public static final int SDK_INT = 19;

    public static final String SHORT_CODE = "K";

    public static final String VERSION = "4.4";

    @Override
    public int getSdkInt() {
      return SDK_INT;
    }

    @Override
    public String getShortCode() {
      return SHORT_CODE;
    }

    @Override
    public String getVersion() {
      return VERSION;
    }
  }

  // Skipping K Watch release, which was 20.

  /**
   * Version: 5.0 <br>
   * ShortCode: L <br>
   * SDK API Level: 21 <br>
   * release: true <br>
   */
  public static final class L extends AndroidReleased {

    public static final int SDK_INT = 21;

    public static final String SHORT_CODE = "L";

    public static final String VERSION = "5.0";

    @Override
    public int getSdkInt() {
      return SDK_INT;
    }

    @Override
    public String getShortCode() {
      return SHORT_CODE;
    }

    @Override
    public String getVersion() {
      return VERSION;
    }
  }

  /**
   * Version: 5.1 <br>
   * ShortCode: LMR1 <br>
   * SDK API Level: 22 <br>
   * release: true <br>
   */
  public static final class LMR1 extends AndroidReleased {

    public static final int SDK_INT = 22;

    public static final String SHORT_CODE = "LMR1";

    public static final String VERSION = "5.1";

    @Override
    public int getSdkInt() {
      return SDK_INT;
    }

    @Override
    public String getShortCode() {
      return SHORT_CODE;
    }

    @Override
    public String getVersion() {
      return VERSION;
    }
  }

  /**
   * Version: 6.0 <br>
   * ShortCode: M <br>
   * SDK API Level: 23 <br>
   * release: true <br>
   */
  public static final class M extends AndroidReleased {

    public static final int SDK_INT = 23;

    public static final String SHORT_CODE = "M";

    public static final String VERSION = "6.0";

    @Override
    public int getSdkInt() {
      return SDK_INT;
    }

    @Override
    public String getShortCode() {
      return SHORT_CODE;
    }

    @Override
    public String getVersion() {
      return VERSION;
    }
  }

  /**
   * Version: 7.0 <br>
   * ShortCode: N <br>
   * SDK API Level: 24 <br>
   * release: true <br>
   */
  public static final class N extends AndroidReleased {

    public static final int SDK_INT = 24;

    public static final String SHORT_CODE = "N";

    public static final String VERSION = "7.0";

    @Override
    public int getSdkInt() {
      return SDK_INT;
    }

    @Override
    public String getShortCode() {
      return SHORT_CODE;
    }

    @Override
    public String getVersion() {
      return VERSION;
    }
  }

  /**
   * Release: 7.1 <br>
   * ShortCode: NMR1 <br>
   * SDK Framework: 25 <br>
   * release: true <br>
   */
  public static final class NMR1 extends AndroidReleased {

    public static final int SDK_INT = 25;

    public static final String SHORT_CODE = "NMR1";

    public static final String VERSION = "7.1";

    @Override
    public int getSdkInt() {
      return SDK_INT;
    }

    @Override
    public String getShortCode() {
      return SHORT_CODE;
    }

    @Override
    public String getVersion() {
      return VERSION;
    }
  }

  /**
   * Release: 8.0 <br>
   * ShortCode: O <br>
   * SDK API Level: 26 <br>
   * release: true <br>
   */
  public static final class O extends AndroidReleased {

    public static final int SDK_INT = 26;

    public static final String SHORT_CODE = "O";

    public static final String VERSION = "8.0";

    @Override
    public int getSdkInt() {
      return SDK_INT;
    }

    @Override
    public String getShortCode() {
      return SHORT_CODE;
    }

    @Override
    public String getVersion() {
      return VERSION;
    }
  }

  /**
   * Release: 8.1 <br>
   * ShortCode: OMR1 <br>
   * SDK API Level: 27 <br>
   * release: true <br>
   */
  public static final class OMR1 extends AndroidReleased {

    public static final int SDK_INT = 27;

    public static final String SHORT_CODE = "OMR1";

    public static final String VERSION = "8.1";

    @Override
    public int getSdkInt() {
      return SDK_INT;
    }

    @Override
    public String getShortCode() {
      return SHORT_CODE;
    }

    @Override
    public String getVersion() {
      return VERSION;
    }
  }

  /**
   * Release: 9.0 <br>
   * ShortCode: P <br>
   * SDK API Level: 28 <br>
   * release: true <br>
   */
  public static final class P extends AndroidReleased {

    public static final int SDK_INT = 28;

    public static final String SHORT_CODE = "P";

    public static final String VERSION = "9.0";

    @Override
    public int getSdkInt() {
      return SDK_INT;
    }

    @Override
    public String getShortCode() {
      return SHORT_CODE;
    }

    @Override
    public String getVersion() {
      return VERSION;
    }
  }

  /**
   * Release: 10.0 <br>
   * ShortCode: Q <br>
   * SDK API Level: 29 <br>
   * release: true <br>
   */
  public static final class Q extends AndroidReleased {

    public static final int SDK_INT = 29;

    public static final String SHORT_CODE = "Q";

    public static final String VERSION = "10.0";

    @Override
    public int getSdkInt() {
      return SDK_INT;
    }

    @Override
    public String getShortCode() {
      return SHORT_CODE;
    }

    @Override
    public String getVersion() {
      return VERSION;
    }
  }

  /**
   * Release: 11.0 <br>
   * ShortCode: R <br>
   * SDK API Level: 30 <br>
   * release: true <br>
   */
  public static final class R extends AndroidReleased {

    public static final int SDK_INT = 30;

    public static final String SHORT_CODE = "R";

    public static final String VERSION = "11.0";

    @Override
    public int getSdkInt() {
      return SDK_INT;
    }

    @Override
    public String getShortCode() {
      return SHORT_CODE;
    }

    @Override
    public String getVersion() {
      return VERSION;
    }
  }

  /**
   * Release: 12.0 <br>
   * ShortCode: S <br>
   * SDK API Level: 31 <br>
   * release: true <br>
   */
  public static final class S extends AndroidReleased {

    public static final int SDK_INT = 31;

    public static final String SHORT_CODE = "S";

    public static final String VERSION = "12.0";

    @Override
    public int getSdkInt() {
      return SDK_INT;
    }

    @Override
    public String getShortCode() {
      return SHORT_CODE;
    }

    @Override
    public String getVersion() {
      return VERSION;
    }
  }

  /**
   * Release: 12.1 <br>
   * ShortCode: Sv2 <br>
   * SDK API Level: 32 <br>
   * release: true <br>
   */
  @SuppressWarnings("UPPER_SNAKE_CASE")
  public static final class Sv2 extends AndroidReleased {

    public static final int SDK_INT = 32;

    public static final String SHORT_CODE = "Sv2";

    public static final String VERSION = "12.1";

    @Override
    public int getSdkInt() {
      return SDK_INT;
    }

    @Override
    public String getShortCode() {
      return SHORT_CODE;
    }

    @Override
    public String getVersion() {
      return VERSION;
    }
  }

  /**
   * Release: 13.0 <br>
   * ShortCode: T <br>
   * SDK API Level: 33 <br>
   * release: true <br>
   */
  public static final class T extends AndroidReleased {

    public static final int SDK_INT = 33;

    public static final String SHORT_CODE = "T";

    public static final String VERSION = "13.0";

    @Override
    public int getSdkInt() {
      return SDK_INT;
    }

    @Override
    public String getShortCode() {
      return SHORT_CODE;
    }

    @Override
    public String getVersion() {
      return VERSION;
    }
  }

  /**
   * Potential Release: 14.0 <br>
   * ShortCode: U <br>
   * SDK API Level: 34 <br>
   * release: false <br>
   */
  public static final class U extends AndroidReleased {

    public static final int SDK_INT = 34;

    public static final String SHORT_CODE = "U";

    public static final String VERSION = "14.0";

    @Override
    public int getSdkInt() {
      return SDK_INT;
    }

    @Override
    public String getShortCode() {
      return SHORT_CODE;
    }

    @Override
    public String getVersion() {
      return VERSION;
    }
  }

  /**
   * Potential Release: 15.0 <br>
   * ShortCode: V <br>
   * SDK API Level: 34+ <br>
   * release: false <br>
   */
  public static final class V extends AndroidReleased {

    public static final int SDK_INT = 35;

    public static final String SHORT_CODE = "V";

    public static final String VERSION = "15";

    @Override
    public int getSdkInt() {
      return SDK_INT;
    }

    @Override
    public String getShortCode() {
      return SHORT_CODE;
    }

    @Override
    public String getVersion() {
      return VERSION;
    }
  }

  /**
   * Placeholder for the next InDevelopment SDK after V
   *
   * <p>All values here subject to change.
   */
  public static final class W extends AndroidUnreleased {

    public static final int SDK_INT = 36;

    public static final String SHORT_CODE = "W";

    public static final String VERSION = "16";

    @Override
    public int getSdkInt() {
      return SDK_INT;
    }

    @Override
    public String getShortCode() {
      return SHORT_CODE;
    }

    @Override
    public String getVersion() {
      return VERSION;
    }
  }

  /** The current release this process is running on. */
  public static final AndroidRelease CURRENT;

  @Nullable
  public static AndroidRelease getReleaseForSdkInt(@Nullable Integer sdkInt) {
    if (sdkInt == null) {
      return null;
    } else {
      return information.sdkIntToAllReleases.get(sdkInt);
    }
  }

  public static List<AndroidRelease> getReleases() {
    return information.released;
  }

  public static List<AndroidRelease> getUnreleased() {
    return information.unreleased;
  }

  /**
   * Responsible for aggregating and interpreting the static state representing the current
   * AndroidReleases known to AndroidVersions class.
   */
  static class SdkInformation {
    final List<AndroidRelease> allReleases;
    final List<Class<? extends AndroidRelease>> classesWithIllegalNames;
    final AndroidRelease latestRelease;
    final AndroidRelease earliestUnreleased;
    final List<AndroidRelease> unreleased;
    final List<AndroidRelease> released;

    // In the future we may need a multimap for sdkInts should they stay static across releases.
    final Map<Integer, AndroidRelease> sdkIntToAllReleases = new HashMap<>();
    final Map<String, AndroidRelease> shortCodeToAllReleases = new HashMap<>();

    // detected errors
    final List<Map.Entry<AndroidRelease, AndroidRelease>> sdkIntCollisions = new ArrayList<>();
    Map.Entry<AndroidRelease, AndroidRelease> sdkApiMisordered = null;

    public SdkInformation(
        List<AndroidRelease> releases,
        List<Class<? extends AndroidRelease>> classesWithIllegalNames) {
      this.allReleases = releases;
      this.classesWithIllegalNames = classesWithIllegalNames;
      AndroidRelease latestRelease = null;
      AndroidRelease earliestUnreleased = null;
      for (AndroidRelease release : allReleases) {
        if (release.isReleased()) {
          if (latestRelease == null || release.compareTo(latestRelease) > 0) {
            latestRelease = release;
          }
        } else {
          if (earliestUnreleased == null || release.compareTo(earliestUnreleased) < 0) {
            earliestUnreleased = release;
          }
        }
      }
      this.latestRelease = latestRelease;
      this.earliestUnreleased = earliestUnreleased;
      this.unreleased =
          allReleases.stream()
              .filter(r -> !r.isReleased())
              .sorted()
              .collect(Collectors.toUnmodifiableList());
      this.released =
          allReleases.stream()
              .filter(AndroidRelease::isReleased)
              .sorted()
              .collect(Collectors.toUnmodifiableList());
      verifyStaticInformation();
    }

    private void verifyStaticInformation() {
      for (AndroidRelease release : this.allReleases) {
        // Construct a map of all sdkInts to releases and note duplicates
        AndroidRelease sdkCollision = this.sdkIntToAllReleases.put(release.getSdkInt(), release);
        if (sdkCollision != null) {
          this.sdkIntCollisions.add(new AbstractMap.SimpleEntry<>(release, sdkCollision));
        }
        // Construct a map of all short codes to releases, and note duplicates
        this.shortCodeToAllReleases.put(release.getShortCode(), release);
        // There is no need to check for shortCode duplicates as the Field name must match the
        // short code.
      }
      if (earliestUnreleased != null
          && latestRelease != null
          && latestRelease.getSdkInt() >= earliestUnreleased.getSdkInt()) {
        sdkApiMisordered = new AbstractMap.SimpleEntry<>(latestRelease, earliestUnreleased);
      }
    }

    private void handleStaticErrors() {
      StringBuilder errors = new StringBuilder();
      if (!this.classesWithIllegalNames.isEmpty()) {
        errors
            .append("The following classes do not follow the naming criteria for ")
            .append("releases or do not have the short codes in ")
            .append("their internal fields. Please correct them: ")
            .append(this.classesWithIllegalNames)
            .append("\n");
      }
      if (sdkApiMisordered != null) {
        errors
            .append("The latest released sdk ")
            .append(sdkApiMisordered.getKey().getShortCode())
            .append(" has a sdkInt greater than the earliest unreleased sdk ")
            .append(sdkApiMisordered.getValue().getShortCode())
            .append("this implies sdks were released out of order which is highly unlikely.\n");
      }
      if (!sdkIntCollisions.isEmpty()) {
        errors.append(
            "The following sdks have different shortCodes, but identical sdkInt " + "versions:\n");
        for (Map.Entry<AndroidRelease, AndroidRelease> entry : sdkIntCollisions) {
          errors
              .append("Both ")
              .append(entry.getKey().getShortCode())
              .append(" and ")
              .append(entry.getValue().getShortCode())
              .append("have the same sdkInt value of ")
              .append(entry.getKey().getSdkInt())
              .append("\n");
        }
      }
      if (errors.length() > 0) {
        errorMessage(
            errors
                .append("Please check the AndroidReleases defined ")
                .append("in ")
                .append(AndroidVersions.class.getName())
                .append("and ensure they are aligned with the versions of")
                .append(" Android.")
                .toString(),
            null);
      }
    }

    public AndroidRelease computeCurrentSdk(
        int reportedVersion, String releaseName, String codename, List<String> activeCodeNames) {
      AndroidRelease current = null;
      // Special case "REL", which means the build is not a pre-release build.
      if (Objects.equals(codename, "REL")) {
        // the first letter of the code name equal to the release number.
        current = sdkIntToAllReleases.get(reportedVersion);
        if (current != null && !current.isReleased()) {
          errorMessage(
              "The current sdk "
                  + current.getShortCode()
                  + " has been released. Please update the contents of "
                  + AndroidVersions.class.getName()
                  + " to mark sdk "
                  + current.getShortCode()
                  + " as released.",
              null);
        }
      } else {
        // Get known active code name letters

        List<String> activeCodenameLetter = new ArrayList<>();
        for (String name : activeCodeNames) {
          activeCodenameLetter.add(name.toUpperCase(Locale.getDefault()).substring(0, 1));
        }

        // If the process is operating with a code name.
        if (codename != null) {
          StringBuilder detectedProblems = new StringBuilder();
          // This is safe for minor releases ( X.1 ) as long as they have added an entry
          // corresponding to the sdk of that release and the prior major release is marked as
          // "released" on its entry in this file.  If not this class will fail to initialize.
          // The assumption is that only one of the major or minor version of a code name
          // is under development and unreleased at any give time (S or Sv2).
          String foundCode = codename.toUpperCase(Locale.getDefault()).substring(0, 1);
          int loc = activeCodenameLetter.indexOf(foundCode);
          if (loc == -1) {
            detectedProblems
                .append("The current codename's (")
                .append(codename)
                .append(") first letter (")
                .append(foundCode)
                .append(") is not in the list of active code's first letters: ")
                .append(activeCodenameLetter)
                .append("\n");
          } else {
            // attempt to find assume the fullname is the "shortCode", aka "Sv2", "OMR1".
            current = shortCodeToAllReleases.get(codename);
            // else, assume the fullname is the first letter is correct.
            if (current == null) {
              current = shortCodeToAllReleases.get(String.valueOf(foundCode));
            }
          }
          if (current == null) {
            detectedProblems
                .append("No known release is associated with the shortCode of \"")
                .append(foundCode)
                .append("\" or \"")
                .append(codename)
                .append("\"\n");
          } else if (current.isReleased()) {
            StringBuilder problem = new StringBuilder();
            problem
                .append("The current sdk ")
                .append(current.getShortCode())
                .append(" has been been marked as released. Please update the ")
                .append("contents of current sdk jar to the released version.\n");
            if (current.getSdkInt() < latestRelease.getSdkInt()) {
              // If the current sdk is lower than the latest release it should never be reported as
              // unreleased.
              detectedProblems.append(problem);
            } else {
              // If the current sdk is the latest release and it is reporting itself as unreleased
              //  we simply log as this will occur when android build devs have not yet updated the
              // branch's build definitions.  (git/main and aosp/main still claim to be the
              // unreleased version of the latest release)
              System.err.println(problem);
            }
          }
          if (detectedProblems.length() > 0) {
            errorMessage(detectedProblems.toString(), null);
          }

          if (current == null) { // only possible in warning mode
            current =
                new AndroidUnreleased() {
                  @Override
                  public int getSdkInt() {
                    return 10000; // the super large unknown sdk value.
                  }

                  @Override
                  public String getShortCode() {
                    return codename.toUpperCase(Locale.getDefault()).substring(0, 1);
                  }

                  @Override
                  public String getVersion() {
                    return "";
                  }
                };
          }
        }
      }

      return current;
    }
  }

  /**
   * Reads all AndroidReleases in this class and populates SdkInformation, checking for sanity in
   * the shortCode, sdkInt, and release information.
   *
   * <p>All errors are stored and can be reported at once by asking the SdkInformation to throw a
   * IllegalStateException after it has been populated.
   */
  static SdkInformation gatherStaticSdkInformationFromThisClass() {
    List<AndroidRelease> allReleases = new ArrayList<>();
    List<Class<? extends AndroidRelease>> classesWithIllegalNames = new ArrayList<>();
    for (Class<?> clazz : AndroidVersions.class.getClasses()) {
      if (AndroidRelease.class.isAssignableFrom(clazz)
          && !clazz.isInterface()
          && !Modifier.isAbstract(clazz.getModifiers())
          && clazz != Unbound.class) {
        try {
          AndroidRelease rel = (AndroidRelease) clazz.getDeclaredConstructor().newInstance();
          allReleases.add(rel);
          // inspect field name - as this is our only chance to inspect it.
          if (!rel.getClass().getSimpleName().equals(rel.getShortCode())) {
            classesWithIllegalNames.add(rel.getClass());
          }
        } catch (NoSuchMethodException
            | InstantiationException
            | IllegalArgumentException
            | IllegalAccessException
            | InvocationTargetException ex) {
          errorMessage(
              "Classes "
                  + clazz.getName()
                  + "should be accessible via "
                  + AndroidVersions.class.getCanonicalName()
                  + " and have a default public no-op constructor ",
              ex);
        }
      }
    }
    Collections.sort(allReleases, AndroidRelease::compareTo);

    SdkInformation sdkInformation = new SdkInformation(allReleases, classesWithIllegalNames);
    sdkInformation.handleStaticErrors();
    return sdkInformation;
  }

  static AndroidRelease computeReleaseVersion(JarFile jarFile) throws IOException {
    ZipEntry buildProp = jarFile.getEntry("build.prop");
    Properties buildProps = new Properties();
    buildProps.load(jarFile.getInputStream(buildProp));
    return computeCurrentSdkFromBuildProps(buildProps);
  }

  static AndroidRelease computeCurrentSdkFromBuildProps(Properties buildProps) {
    // 33, 34, 35 ....
    String sdkVersionString = buildProps.getProperty("ro.build.version.sdk");
    int sdk = sdkVersionString == null ? 0 : Integer.parseInt(sdkVersionString);
    // "REL"
    String release = buildProps.getProperty("ro.build.version.release");
    // "Tiramasu", "UpsideDownCake"
    String codename = buildProps.getProperty("ro.build.version.codename");
    // "Tiramasu,UpsideDownCake", "UpsideDownCake", "REL"
    String codenames = buildProps.getProperty("ro.build.version.all_codenames");
    String[] allCodeNames = codenames == null ? new String[0] : codenames.split(",");
    String[] activeCodeNames =
        allCodeNames.length > 0 && allCodeNames[0].equals("REL") ? new String[0] : allCodeNames;
    return information.computeCurrentSdk(sdk, release, codename, asList(activeCodeNames));
  }

  private static final SdkInformation information;

  private static final void errorMessage(String errorMessage, @Nullable Exception ex) {
    if (warnOnly) {
      System.err.println(errorMessage);
    } else {
      throw new IllegalStateException(errorMessage, ex);
    }
  }

  static {
    // We shouldn't break in annotation processors, only test runs.
    String cmd = System.getProperty("sun.java.command");
    // We appear to be in an annotation processor, so only warn users.
    if (cmd.contains("-Aorg.robolectric.annotation.processing.")) {
      System.err.println(
          "Robolectric's AndroidVersions is running in warning mode,"
              + " no errors will be thrown.");
      warnOnly = true;
    } else {
      warnOnly = false;
    }
    AndroidRelease currentRelease = null;
    information = gatherStaticSdkInformationFromThisClass();
    try {
      InputStream is = AndroidVersions.class.getClassLoader().getResourceAsStream("build.prop");
      if (is != null) {
        Properties buildProps = new Properties();
        buildProps.load(is);
        currentRelease = computeCurrentSdkFromBuildProps(buildProps);
      }
    } catch (IOException ioe) {
      // No op, this class should be usable outside of a Robolectric sandbox.
    }
    CURRENT = currentRelease;
  }
}
