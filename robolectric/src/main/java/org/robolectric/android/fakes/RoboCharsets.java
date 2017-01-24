package org.robolectric.android.fakes;

import java.nio.charset.Charset;

@SuppressWarnings("UnusedDeclaration") // needed for android.net.Uri
public class RoboCharsets {
  /**
   * A cheap and type-safe constant for the ISO-8859-1 Charset.
   */
  public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

  /**
   * A cheap and type-safe constant for the US-ASCII Charset.
   */
  public static final Charset US_ASCII = Charset.forName("US-ASCII");

  /**
   * A cheap and type-safe constant for the UTF-8 Charset.
   */
  public static final Charset UTF_8 = Charset.forName("UTF-8");
}
