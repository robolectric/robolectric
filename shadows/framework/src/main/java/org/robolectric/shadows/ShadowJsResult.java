package org.robolectric.shadows;

import android.webkit.JsResult;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(JsResult.class)
public class ShadowJsResult {

  private boolean wasCancelled;

  @Implementation
  protected void cancel() {
    wasCancelled = true;
  }

  public boolean wasCancelled() {
    return wasCancelled;
  }
}
