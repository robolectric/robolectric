package org.robolectric.res;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Android qualifers as defined by https://developer.android.com/guide/topics/resources/providing-resources.html
 */
public class Qualifiers {
  // Matches a version qualifier like "v14". Parentheses capture the numeric
  // part for easy retrieval with Matcher.group(2).
  private static final Pattern SCREEN_WIDTH_PATTERN = Pattern.compile("^w([0-9]+)dp");
  private static final Pattern SMALLEST_SCREEN_WIDTH_PATTERN = Pattern.compile("^sw([0-9]+)dp");
  private static final Pattern VERSION_QUALIFIER_PATTERN = Pattern.compile("(v)([0-9]+)$");
  private static final Pattern SIZE_QUALIFIER_PATTERN = Pattern.compile("(s?[wh])([0-9]+)dp");
  private static final Pattern ORIENTATION_QUALIFIER_PATTERN = Pattern.compile("(land|port)");

  // Version are matched in the end, and hence have least order
  private static final int ORDER_VERSION = 0;
  // Various size qualifies, in increasing order of importance.
  private static final List<String> INT_QUALIFIERS = Arrays.asList("v", "h", "w", "sh", "sw");
  private static final int TOTAL_ORDER_COUNT = INT_QUALIFIERS.size();

  private static final Map<String, Qualifiers> sQualifiersCache = new HashMap<>();

  private final int[] mWeights = new int[TOTAL_ORDER_COUNT];
  // Set of all the qualifiers which need exact matching.
  private final List<String> mDefaults = new ArrayList<>();

  public boolean matches(Qualifiers other) {
    if (!passesRequirements(other)) {
      return false;
    }
    return other.mDefaults.containsAll(mDefaults);
  }

  public boolean passesRequirements(Qualifiers other) {
    for (int i = 0; i < TOTAL_ORDER_COUNT; i++) {
      if (other.mWeights[i] != -1 && mWeights[i] != -1 && other.mWeights[i] < mWeights[i]) {
        return false;
      }
    }
    return true;
  }

  public boolean isBetterThan(Qualifiers other, Qualifiers context) {
    // Compare the defaults in the order they appear in the context.
    for (String qualifier : context.mDefaults) {
      if (other.mDefaults.contains(qualifier) ^ mDefaults.contains(qualifier)) {
        return mDefaults.contains(qualifier);
      }
    }

    for (int i = TOTAL_ORDER_COUNT - 1; i > ORDER_VERSION; i--) {
      if (other.mWeights[i] != mWeights[i]) {
        return mWeights[i] > other.mWeights[i];
      }
    }

    // Compare the version only if the context defines a version.
    if (context.mWeights[ORDER_VERSION] != -1
        && other.mWeights[ORDER_VERSION] != mWeights[ORDER_VERSION]) {
      return mWeights[ORDER_VERSION] > other.mWeights[ORDER_VERSION];
    }

    // The qualifiers match completely
    return false;
  }

  public static Qualifiers parse(String qualifiersStr) {
    synchronized (sQualifiersCache) {
      Qualifiers result = sQualifiersCache.get(qualifiersStr);
      if (result != null) {
        return result;
      }
      StringTokenizer st = new StringTokenizer(qualifiersStr, "-");
      result = new Qualifiers();
      // Version qualifiers are also allowed to match when only one of the qualifiers
      // defines a version restriction.
      result.mWeights[ORDER_VERSION] = -1;

      while (st.hasMoreTokens()) {
        String qualifier = st.nextToken();
        if (qualifier.isEmpty()) {
          continue;
        }

        Matcher m = VERSION_QUALIFIER_PATTERN.matcher(qualifier);
        if (!m.find()) {
          m = SIZE_QUALIFIER_PATTERN.matcher(qualifier);
          if (!m.find()) {
            m = null;
          }
        }
        if (m != null) {
          int order = INT_QUALIFIERS.indexOf(m.group(1));
          if (order == ORDER_VERSION && result.mWeights[ORDER_VERSION] != -1) {
            throw new IllegalStateException(
                "A resource file was found that had two API level qualifiers: " + qualifiersStr);
          }
          result.mWeights[order] = Integer.parseInt(m.group(2));
        } else {
          result.mDefaults.add(qualifier);
        }
      }

      sQualifiersCache.put(qualifiersStr, result);
      return result;
    }
  }

  public static int getPlatformVersion(String qualifiers) {
    Matcher m = VERSION_QUALIFIER_PATTERN.matcher(qualifiers);
    if (m.find()) {
      return Integer.parseInt(m.group(2));
    }
    return -1;
  }

  public static int getSmallestScreenWidth(String qualifiers) {
    for (String qualifier : qualifiers.split("-")) {
      Matcher matcher = SMALLEST_SCREEN_WIDTH_PATTERN.matcher(qualifier);
      if (matcher.find()) {
        return Integer.parseInt(matcher.group(1));
      }
    }

    return -1;
  }

  /*
   * If the Config already has a version qualifier, do nothing. Otherwise, add a version
   * qualifier for the target api level (which comes from the manifest or Config.sdk()).
   */
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

  /*
   * If the Config already has a version qualifier, do nothing. Otherwise, add a version
   * qualifier for the target api level (which comes from the manifest or Config.sdk()).
   */
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

  public static int getScreenWidth(String qualifiers) {
    for (String qualifier : qualifiers.split("-")) {
      Matcher matcher = SCREEN_WIDTH_PATTERN.matcher(qualifier);
      if (matcher.find()) {
        return Integer.parseInt(matcher.group(1));
      }
    }

    return -1;
  }

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

  public static String getOrientation(String qualifiers) {
    for (String qualifier : qualifiers.split("-")) {
      Matcher matcher = ORIENTATION_QUALIFIER_PATTERN.matcher(qualifier);
      if (matcher.find()) {
        return matcher.group(1);
      }
    }
    return null;
  }
}
