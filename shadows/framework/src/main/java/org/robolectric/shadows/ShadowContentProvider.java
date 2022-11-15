package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.Q;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.ContentProvider;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link ContentProvider}. */
@Implements(value = ContentProvider.class, looseSignatures = true)
public class ShadowContentProvider {
  @RealObject private ContentProvider realContentProvider;

  private String callingPackage;

  @Implementation(minSdk = Q, maxSdk = Q)
  public Object setCallingPackage(Object callingPackage) {
    this.callingPackage = (String) callingPackage;
    return callingPackage;
  }

  @Implementation(minSdk = KITKAT)
  protected String getCallingPackage() {
    if (callingPackage != null) {
      return callingPackage;
    } else {
      return reflector(ContentProviderReflector.class, realContentProvider).getCallingPackage();
    }
  }

  @ForType(ContentProvider.class)
  interface ContentProviderReflector {

    @Direct
    String getCallingPackage();
  }
}
