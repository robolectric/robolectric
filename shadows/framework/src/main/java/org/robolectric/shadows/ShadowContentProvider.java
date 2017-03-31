package org.robolectric.shadows;

import android.content.ContentProvider;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import static org.robolectric.shadow.api.Shadow.directlyOn;

/**
 * Shadow for {@link ContentProvider}.
 */
@Implements(ContentProvider.class)
public class ShadowContentProvider {
  @RealObject private ContentProvider realContentProvider;

  private String callingPackage;

  public void setCallingPackage(String callingPackage) {
    this.callingPackage = callingPackage;
  }

  @Implementation
  public String getCallingPackage() {
    if (callingPackage != null) {
      return callingPackage;
    } else {
      return directlyOn(realContentProvider, ContentProvider.class, "getCallingPackage");
    }
  }
}
