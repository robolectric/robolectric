package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;

import android.graphics.Typeface;
import android.provider.FontRequest;
import android.provider.FontsContract;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = FontsContract.class, minSdk = O)
public class ShadowFontsContract {

  /** Returns a stub typeface immediately. */
  @Implementation
  public static Typeface getFontSync(FontRequest request) {
    return Typeface.create(request.getQuery(), Typeface.NORMAL);
  }
}
