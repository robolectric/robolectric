package org.robolectric.shadows;

import android.app.IntentService;
import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(IntentService.class)
public class ShadowIntentService extends ShadowService {
  @RealObject IntentService realIntentService;
  private boolean mRedelivery;

  public boolean getIntentRedelivery() {
    return mRedelivery;
  }

  @Filter
  protected void setIntentRedelivery(boolean enabled) {
    mRedelivery = enabled;
  }
}
