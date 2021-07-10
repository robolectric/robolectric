package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.IntentService;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(IntentService.class)
public class ShadowIntentService extends ShadowService {
  @RealObject IntentService realIntentService;
  private boolean mRedelivery;

  public boolean getIntentRedelivery() {
    return mRedelivery;
  }

  @Implementation
  protected void setIntentRedelivery(boolean enabled) {
    mRedelivery = enabled;
    reflector(IntentServiceReflector.class, realIntentService).setIntentRedelivery(enabled);
  }

  @ForType(IntentService.class)
  interface IntentServiceReflector {

    @Direct
    void setIntentRedelivery(boolean enabled);
  }
}
