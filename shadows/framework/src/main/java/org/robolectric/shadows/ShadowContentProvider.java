package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.ContentProvider;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@Implements(ContentProvider.class)
public class ShadowContentProvider {
  @RealObject private ContentProvider realContentProvider;

  private String callingPackage;

  public void setCallingPackage(String callingPackage) {
    this.callingPackage = callingPackage;
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
