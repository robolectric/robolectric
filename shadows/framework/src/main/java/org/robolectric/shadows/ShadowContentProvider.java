package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.content.ContentProvider;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

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
