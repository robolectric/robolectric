package org.robolectric.res.android;

import static org.robolectric.res.android.LocaleDataTables.LIKELY_SCRIPTS;
import static org.robolectric.res.android.LocaleDataTables.MAX_PARENT_DEPTH;
import static org.robolectric.res.android.LocaleDataTables.REPRESENTATIVE_LOCALES;
import static org.robolectric.res.android.LocaleDataTables.SCRIPT_CODES;
import static org.robolectric.res.android.LocaleDataTables.SCRIPT_PARENTS;

import java.util.Arrays;
import java.util.Map;

// transliterated from https://android.googlesource.com/platform/frameworks/base/+/android-9.0.0_r12/libs/androidfw/LocaleData.cpp
public class LocaleData {

  private static int packLocale(final byte[] language, final byte[] region) {
    return ((language[0] & 0xff) << 24) | ((language[1] & 0xff) << 16) |
        ((region[0] & 0xff) << 8) | (region[1] & 0xff);
  }

  private static int dropRegion(int packed_locale) {
    return packed_locale & 0xFFFF0000;
  }

  private static boolean hasRegion(int packed_locale) {
    return (packed_locale & 0x0000FFFF) != 0;
  }

  static final int SCRIPT_LENGTH = 4;
  private static final int PACKED_ROOT = 0; // to represent the root locale

  private static int findParent(int packed_locale, final String script) {
    if (hasRegion(packed_locale)) {
      for (Map.Entry<String, Map<Integer, Integer>> entry : SCRIPT_PARENTS.entrySet()) {
        if (script.equals(entry.getKey())) {
          Map<Integer, Integer> map = entry.getValue();
          Integer lookup_result = map.get(packed_locale);
          if (lookup_result != null) {
            return lookup_result;
          }
          break;
        }
      }
      return dropRegion(packed_locale);
    }
    return PACKED_ROOT;
  }

  // Find the ancestors of a locale, and fill 'out' with it (assumes out has enough
  // space). If any of the members of stop_list was seen, write it in the
  // output but stop afterwards.
  //
  // This also outputs the index of the last written ancestor in the stop_list
  // to stop_list_index, which will be -1 if it is not found in the stop_list.
  //
  // Returns the number of ancestors written in the output, which is always
  // at least one.
  //
  // (If 'out' is null, we do everything the same way but we simply don't write
  // any results in 'out'.)
  static int findAncestors(int[] out, Ref<Long> stop_list_index,
      int packed_locale, final String script,
      final int[] stop_list, int stop_set_length) {
    int ancestor = packed_locale;
    int count = 0;
    do {
      if (out != null) {
        out[count] = ancestor;
      }
      count++;
      for (int i = 0; i < stop_set_length; i++) {
        if (stop_list[i] == ancestor) {
          stop_list_index.set((long) i);
          return count;
        }
      }
      ancestor = findParent(ancestor, script);
    } while (ancestor != PACKED_ROOT);
    stop_list_index.set((long) -1);
    return count;
  }

  static int findDistance(int supported,
      final String script,
      final int[] request_ancestors,
      int request_ancestors_count) {
    final Ref<Long> request_ancestors_indexRef = new Ref<>(null);
    final int supported_ancestor_count = findAncestors(
        null, request_ancestors_indexRef,
        supported, script,
        request_ancestors, request_ancestors_count);
    // Since both locales share the same root, there will always be a shared
    // ancestor, so the distance in the parent tree is the sum of the distance
    // of 'supported' to the lowest common ancestor (number of ancestors
    // written for 'supported' minus 1) plus the distance of 'request' to the
    // lowest common ancestor (the index of the ancestor in request_ancestors).
    return (int) (supported_ancestor_count + request_ancestors_indexRef.get() - 1);
  }

  static boolean isRepresentative(int language_and_region, final String script) {
    final long packed_locale = (
        (((long) language_and_region) << 32) |
            (((long) script.charAt(0) & 0xff) << 24) |
            (((long) script.charAt(1) & 0xff) << 16) |
            (((long) script.charAt(2) & 0xff) << 8) |
            ((long) script.charAt(3) & 0xff));
    return (REPRESENTATIVE_LOCALES.contains(packed_locale));
  }

  private static final int US_SPANISH = 0x65735553; // es-US
  private static final int MEXICAN_SPANISH = 0x65734D58; // es-MX
  private static final int LATIN_AMERICAN_SPANISH = 0x6573A424; // es-419

  // The two locales es-US and es-MX are treated as special fallbacks for es-419.
// If there is no es-419, they are considered its equivalent.
  private static boolean isSpecialSpanish(int language_and_region) {
    return (language_and_region == US_SPANISH || language_and_region == MEXICAN_SPANISH);
  }

  static int localeDataCompareRegions(
      final byte[] left_region, final byte[] right_region,
      final byte[] requested_language, final String requested_script,
      final byte[] requested_region) {
    if (left_region[0] == right_region[0] && left_region[1] == right_region[1]) {
      return 0;
    }
    int left = packLocale(requested_language, left_region);
    int right = packLocale(requested_language, right_region);
    final int request = packLocale(requested_language, requested_region);

    // If one and only one of the two locales is a special Spanish locale, we
    // replace it with es-419. We don't do the replacement if the other locale
    // is already es-419, or both locales are special Spanish locales (when
    // es-US is being compared to es-MX).
    final boolean leftIsSpecialSpanish = isSpecialSpanish(left);
    final boolean rightIsSpecialSpanish = isSpecialSpanish(right);
    if (leftIsSpecialSpanish && !rightIsSpecialSpanish && right != LATIN_AMERICAN_SPANISH) {
      left = LATIN_AMERICAN_SPANISH;
    } else if (rightIsSpecialSpanish && !leftIsSpecialSpanish && left != LATIN_AMERICAN_SPANISH) {
      right = LATIN_AMERICAN_SPANISH;
    }

    int[] request_ancestors = new int[MAX_PARENT_DEPTH + 1];
    final Ref<Long> left_right_indexRef = new Ref<Long>(null);
    // Find the parents of the request, but stop as soon as we saw left or right
    final int left_and_right[] = {left, right};
    final int ancestor_count = findAncestors(
        request_ancestors, left_right_indexRef,
        request, requested_script,
        left_and_right, sizeof(left_and_right));
    if (left_right_indexRef.get() == 0) { // We saw left earlier
      return 1;
    }
    if (left_right_indexRef.get() == 1) { // We saw right earlier
      return -1;
    }
    // If we are here, neither left nor right are an ancestor of the
    // request. This means that all the ancestors have been computed and
    // the last ancestor is just the language by itself. We will use the
    // distance in the parent tree for determining the better match.
    final int left_distance = findDistance(
        left, requested_script, request_ancestors, ancestor_count);
    final int right_distance = findDistance(
        right, requested_script, request_ancestors, ancestor_count);
    if (left_distance != right_distance) {
      return (int) right_distance - (int) left_distance; // smaller distance is better
    }
    // If we are here, left and right are equidistant from the request. We will
    // try and see if any of them is a representative locale.
    final boolean left_is_representative = isRepresentative(left, requested_script);
    final boolean right_is_representative = isRepresentative(right, requested_script);
    if (left_is_representative != right_is_representative) {
      return (left_is_representative ? 1 : 0) - (right_is_representative ? 1 : 0);
    }
    // We have no way of figuring out which locale is a better match. For
    // the sake of stability, we consider the locale with the lower region
    // code (in dictionary order) better, with two-letter codes before
    // three-digit codes (since two-letter codes are more specific).
    return right - left;
  }

  static void localeDataComputeScript(byte[] out, final byte[] language, final byte[] region) {
    if (language[0] == '\0') {
//      memset(out, '\0', SCRIPT_LENGTH);
      Arrays.fill(out, (byte) 0);
      return;
    }
    int lookup_key = packLocale(language, region);
    Byte lookup_result = LIKELY_SCRIPTS.get(lookup_key);
    if (lookup_result == null) {
      // We couldn't find the locale. Let's try without the region
      if (region[0] != '\0') {
        lookup_key = dropRegion(lookup_key);
        lookup_result = LIKELY_SCRIPTS.get(lookup_key);
        if (lookup_result != null) {
//          memcpy(out, SCRIPT_CODES[lookup_result.second], SCRIPT_LENGTH);
          System.arraycopy(SCRIPT_CODES[lookup_result], 0, out, 0, SCRIPT_LENGTH);
          return;
        }
      }
      // We don't know anything about the locale
//      memset(out, '\0', SCRIPT_LENGTH);
      Arrays.fill(out, (byte) 0);
      return;
    } else {
      // We found the locale.
//      memcpy(out, SCRIPT_CODES[lookup_result.second], SCRIPT_LENGTH);
      System.arraycopy(SCRIPT_CODES[lookup_result], 0, out, 0, SCRIPT_LENGTH);
    }
  }

  static final int[] ENGLISH_STOP_LIST = {
      0x656E0000, // en
      0x656E8400, // en-001
  };

  static final byte[] ENGLISH_CHARS = {'e', 'n'};

  static final String LATIN_CHARS = "Latn";

  static boolean localeDataIsCloseToUsEnglish(final byte[] region) {
    final int locale = packLocale(ENGLISH_CHARS, region);
    final Ref<Long> stop_list_indexRef = new Ref<>(null);
    findAncestors(null, stop_list_indexRef, locale, LATIN_CHARS, ENGLISH_STOP_LIST, 2);
    // A locale is like US English if we see "en" before "en-001" in its ancestor list.
    return stop_list_indexRef.get() == 0; // 'en' is first in ENGLISH_STOP_LIST
  }


  private static int sizeof(int[] array) {
    return array.length;
  }

}
