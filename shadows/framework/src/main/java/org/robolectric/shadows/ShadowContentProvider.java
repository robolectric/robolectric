package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.content.ContentProvider;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

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
      return directlyOn(realContentProvider, ContentProvider.class, "getCallingPackage");
    }
  }

  /**
   * Allows to set authorities on the provider.
   *
   * Normally it is done when the provider is loaded from the manifest automatically.
   */
  void setAuthorities(String authorities) {
    directlyOn(
        realContentProvider,
        ContentProvider.class,
        "setAuthorities",
        ClassParameter.from(String.class, authorities));
  }
}
