package org.robolectric.res;

public class StringResources {

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
}
