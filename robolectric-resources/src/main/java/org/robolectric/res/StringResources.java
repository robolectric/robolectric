package org.robolectric.res;

public class StringResources {

  private static final int CODE_POINT_LENGTH = 4;

  /**
   * Provides escaping of String resources as described
   *
   * <a href="http://developer.android.com/guide/topics/resources/string-resource.html#FormattingAndStyling">here</a>
   *
   * @param text Text to escape.
   * @return Escaped text.
   */
  public static String escape(String text) {
    if (text.length() > 1 && text.charAt(0) == '"' && text.charAt(text.length() - 1) == '"') {
      text = text.substring(1, text.length() - 1);
    } else {
      text = text.replaceAll("\\\\(['\"])", "$1");
    }
    return text;
  }

  /**
   * Processes String resource values in the same way real Android does, namely:-
   * 1) Trim leading and trailing whitespace.
   * 2) Converts code points.
   * 3) Escapes
   */
  public static String proccessStringResources(String inputValue) {
    return escape(convertCodePoints(inputValue.trim()
            .replace("\\n", String.valueOf('\n'))
            .replace("\\t", String.valueOf('\t'))
    ));
  }

  /**
   * Converts code points in a given string to actual characters. This method doesn't handle code
   * points whose char counts are 2. In other words, this method doesn't handle U+10XXXX.
   */
  private static String convertCodePoints(String src) {
    String[] tokens = src.split("\\\\u");

    StringBuilder retval = new StringBuilder(tokens[0]);
    for (int i = 1; i < tokens.length; ++i) {
      if (tokens[i].length() < CODE_POINT_LENGTH) {
        throw new IllegalArgumentException("Too short code point: \\u" + tokens[i]);
      }
      String codePoint = tokens[i].substring(0, CODE_POINT_LENGTH);
      try {
        retval.append(Character.toChars(Integer.valueOf(codePoint, 16)))
            .append(tokens[i].substring(CODE_POINT_LENGTH));
      } catch (IllegalArgumentException e) {
        // This may be caused by NumberFormatException of Integer.valueOf() or
        // IllegalArgumentException of Character.toChars().
        throw new IllegalArgumentException("Invalid code point: \\u" + codePoint, e);
      }
    }
    return retval.toString();
  }
}
