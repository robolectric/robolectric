package org.robolectric.res;

import static com.google.common.base.CharMatcher.whitespace;

import com.google.common.annotations.VisibleForTesting;
import org.robolectric.util.Logger;

public class StringResources {

  private static final int CODE_POINT_LENGTH = 4;

  /**
   * Processes String resource values in the same way real Android does, namely:-
   * 1) Trim leading and trailing whitespace.
   * 2) Converts code points.
   * 3) Escapes
   */
  public static String processStringResources(String inputValue) {
    return escape(whitespace().collapseFrom(inputValue.trim(), ' '));
  }

  /**
   * Provides escaping of String resources as described
   * [here](http://developer.android.com/guide/topics/resources/string-resource.html#FormattingAndStyling).
   *
   * @param text Text to escape.
   * @return Escaped text.
   */
  @VisibleForTesting
  static String escape(String text) {
    // unwrap double quotes
    if (text.length() > 1 && text.charAt(0) == '"' && text.charAt(text.length() - 1) == '"') {
      text = text.substring(1, text.length() - 1);
    }
    int i = 0;
    int length = text.length();
    StringBuilder result = new StringBuilder(text.length());
    while (true) {
      int j = text.indexOf('\\', i);
      if (j == -1) {
        result.append(removeUnescapedDoubleQuotes(text.substring(i)));
        break;
      }
      result.append(removeUnescapedDoubleQuotes(text.substring(i, j)));
      if (j == length - 1) {
        // dangling backslash
        break;
      }
      boolean isUnicodeEscape = false;
      char escapeCode = text.charAt(j + 1);
      switch (escapeCode) {
        case '\'':
        case '"':
        case '\\':
        case '?':
        case '@':
        case '#':
          result.append(escapeCode);
          break;
        case 'n':
          result.append('\n');
          break;
        case 't':
          result.append('\t');
          break;
        case 'u':
          isUnicodeEscape = true;
          break;
        default:
          Logger.strict("Unsupported string resource escape code '%s'", escapeCode);
      }
      if (!isUnicodeEscape) {
        i = j + 2;
      } else {
        j += 2;
        if (length - j < CODE_POINT_LENGTH) {
          throw new IllegalArgumentException("Too short code point: \\u" + text.substring(j));
        }
        String codePoint = text.substring(j, j + CODE_POINT_LENGTH);
        result.append(extractCodePoint(codePoint));
        i = j + CODE_POINT_LENGTH;
      }
    }
    return result.toString();
  }

  /**
   * Converts code points in a given string to actual characters. This method doesn't handle code
   * points whose char counts are 2. In other words, this method doesn't handle U+10XXXX.
   */
  private static char[] extractCodePoint(String codePoint) {
    try {
      return Character.toChars(Integer.valueOf(codePoint, 16));
    } catch (IllegalArgumentException e) {
      // This may be caused by NumberFormatException of Integer.valueOf() or
      // IllegalArgumentException of Character.toChars().
      throw new IllegalArgumentException("Invalid code point: \\u" + codePoint, e);
    }
  }

  private static String removeUnescapedDoubleQuotes(String input) {
    return input.replaceAll("\"", "");
  }
}
