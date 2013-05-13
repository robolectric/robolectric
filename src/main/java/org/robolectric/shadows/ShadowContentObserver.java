package org.robolectric.shadows;

import android.database.ContentObserver;
import android.net.Uri;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;


@Implements(ContentObserver.class)
public class ShadowContentObserver {

  @RealObject
  private ContentObserver realObserver;

  @Implementation
  public void dispatchChange( boolean selfChange, Uri uri ) {
    realObserver.onChange(selfChange, uri);
  }

  @Implementation
  public void dispatchChange( boolean selfChange ) {
    realObserver.onChange(selfChange);
  }

}
