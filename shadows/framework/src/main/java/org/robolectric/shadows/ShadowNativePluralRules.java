package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.M;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(className = "libcore.icu.NativePluralRules", isInAndroidSdk = false, maxSdk = M)
public class ShadowNativePluralRules {

  @Implementation(minSdk = KITKAT)
  protected static int quantityForIntImpl(long address, int quantity) {
    // just return the mapping for english locale for now
    if (quantity == 1) return 1;
    else return 5 /* other */;
  }

  @Implementation(maxSdk = JELLY_BEAN_MR2)
  protected static int quantityForIntImpl(int address, int quantity) {
    return quantityForIntImpl((long)address, quantity);

  }
}
