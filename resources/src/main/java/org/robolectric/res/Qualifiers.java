package org.robolectric.res;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.robolectric.res.android.ConfigDescription;
import org.robolectric.res.android.ResTable_config;

/**
 * Android qualifers as defined by https://developer.android.com/guide/topics/resources/providing-resources.html
 */
public class Qualifiers {
  private static final Pattern DIR_QUALIFIER_PATTERN = Pattern.compile("^[^-]+(?:-(.*))?$");

  // Matches a version qualifier like "v14". Parentheses capture the numeric
  // part for easy retrieval with Matcher.group(2).
  private static final Pattern SCREEN_WIDTH_PATTERN = Pattern.compile("^w([0-9]+)dp");
  private static final Pattern SMALLEST_SCREEN_WIDTH_PATTERN = Pattern.compile("^sw([0-9]+)dp");
  private static final Pattern VERSION_QUALIFIER_PATTERN = Pattern.compile("(v)([0-9]+)$");
  private static final Pattern ORIENTATION_QUALIFIER_PATTERN = Pattern.compile("(land|port)");

  private final String qualifiers;
  private final ResTable_config config;

  public static Qualifiers parse(String qualifiers) {
    return parse(qualifiers, true);
  }

  public static Qualifiers parse(String qualifiers, boolean applyVersionForCompat) {
    final ResTable_config config = new ResTable_config();
    if (!qualifiers.isEmpty()
        && !ConfigDescription.parse(qualifiers, config, applyVersionForCompat)) {
      throw new IllegalArgumentException("failed to parse qualifiers '" + qualifiers + "'."
          + " See https://developer.android.com/guide/topics/resources/providing-resources.html#QualifierRules for expected format.");
    }

    return new Qualifiers(qualifiers, config);
  }

  protected Qualifiers(String qualifiers, ResTable_config config) {
    this.qualifiers = qualifiers;
    this.config = config;
  }

  public ResTable_config getConfig() {
    return config;
  }

  @Override
  public String toString() {
    return qualifiers;
  }

  public static Qualifiers fromParentDir(FsFile parentDir) {
    if (parentDir == null) {
      return parse("");
    } else {
      String parentDirName = parentDir.getName();
      Matcher matcher = DIR_QUALIFIER_PATTERN.matcher(parentDirName);
      if (!matcher.find()) throw new IllegalStateException(parentDirName);
      String qualifiers = matcher.group(1);
      return parse(qualifiers != null ? qualifiers : "");
    }
  }

  /**
   * @deprecated Use {@link android.os.Build.VERSION#SDK_INT} instead.
   */
  @Deprecated
  public static int getPlatformVersion(String qualifiers) {
    Matcher m = VERSION_QUALIFIER_PATTERN.matcher(qualifiers);
    if (m.find()) {
      return Integer.parseInt(m.group(2));
    }
    return -1;
  }

  /**
   * @deprecated Use {@link android.content.res.Configuration#smallestScreenWidthDp} instead.
   */
  @Deprecated
  public static int getSmallestScreenWidth(String qualifiers) {
    for (String qualifier : qualifiers.split("-", 0)) {
      Matcher matcher = SMALLEST_SCREEN_WIDTH_PATTERN.matcher(qualifier);
      if (matcher.find()) {
        return Integer.parseInt(matcher.group(1));
      }
    }

    return -1;
  }

  /**
   * If the Config already has a version qualifier, do nothing. Otherwise, add a version
   * qualifier for the target api level (which comes from the manifest or Config.sdk()).
   *
   * @deprecated Figure something else out.
   */
  @Deprecated
  public static String addPlatformVersion(String qualifiers, int apiLevel) {
    int versionQualifierApiLevel = Qualifiers.getPlatformVersion(qualifiers);
    if (versionQualifierApiLevel == -1) {
      if (qualifiers.length() > 0) {
        qualifiers += "-";
      }
      qualifiers += "v" + apiLevel;
    }
    return qualifiers;
  }

  /**
   * If the Config already has a `sw` qualifier, do nothing. Otherwise, add a `sw`
   * qualifier for the given width.
   *
   * @deprecated Use {@link android.content.res.Configuration#smallestScreenWidthDp} instead.
   */
  @Deprecated
  public static String addSmallestScreenWidth(String qualifiers, int smallestScreenWidth) {
    int qualifiersSmallestScreenWidth = Qualifiers.getSmallestScreenWidth(qualifiers);
    if (qualifiersSmallestScreenWidth == -1) {
      if (qualifiers.length() > 0) {
        qualifiers += "-";
      }
      qualifiers += "sw" + smallestScreenWidth + "dp";
    }
    return qualifiers;
  }

  /**
   * @deprecated Use {@link android.content.res.Configuration#screenWidthDp} instead.
   */
  @Deprecated
  public static int getScreenWidth(String qualifiers) {
    for (String qualifier : qualifiers.split("-", 0)) {
      Matcher matcher = SCREEN_WIDTH_PATTERN.matcher(qualifier);
      if (matcher.find()) {
        return Integer.parseInt(matcher.group(1));
      }
    }

    return -1;
  }

  /**
   * @deprecated Use {@link android.content.res.Configuration#screenWidthDp} instead.
   */
  @Deprecated
  public static String addScreenWidth(String qualifiers, int screenWidth) {
    int qualifiersScreenWidth = Qualifiers.getScreenWidth(qualifiers);
    if (qualifiersScreenWidth == -1) {
      if (qualifiers.length() > 0) {
        qualifiers += "-";
      }
      qualifiers += "w" + screenWidth + "dp";
    }
    return qualifiers;
  }

  /**
   * @deprecated Use {@link android.content.res.Configuration#orientation} instead.
   */
  @Deprecated
  public static String getOrientation(String qualifiers) {
    for (String qualifier : qualifiers.split("-", 0)) {
      Matcher matcher = ORIENTATION_QUALIFIER_PATTERN.matcher(qualifier);
      if (matcher.find()) {
        return matcher.group(1);
      }
    }
    return null;
  }
}
