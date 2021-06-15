package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Context;
import android.graphics.Typeface;
import android.provider.FontRequest;
import android.provider.FontsContract;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

@Implements(value = FontsContract.class, minSdk = O)
public class ShadowFontsContract {

  /** Returns a stub typeface immediately. */
  @Implementation
  public static Typeface getFontSync(FontRequest request) {
    return Typeface.create(request.getQuery(), Typeface.NORMAL);
  }

  @Resetter
  public static void reset() {
    reflector(FontsContractReflector.class).setContext(null);
  }

  @ForType(FontsContract.class)
  private interface FontsContractReflector {
    @Static
    @Accessor("sContext")
    void setContext(Context context);
  }
}
