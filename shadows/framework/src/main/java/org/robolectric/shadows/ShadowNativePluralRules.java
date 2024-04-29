package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(className = "libcore.icu.NativePluralRules", isInAndroidSdk = false, maxSdk = M)
public class ShadowNativePluralRules {

  @Implementation
  protected static int quantityForIntImpl(long address, int quantity) {
    // just return the mapping for english locale for now
    if (quantity == 1) return 1;
    else return 5 /* other */;
  }
}
