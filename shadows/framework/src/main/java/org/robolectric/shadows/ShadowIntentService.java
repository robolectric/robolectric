package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.app.IntentService;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(IntentService.class)
public class ShadowIntentService extends ShadowService {
  @RealObject
  IntentService realIntentService;
  private boolean mRedelivery;

  public boolean getIntentRedelivery() {
    return mRedelivery;
  }

  @Implementation
  protected void setIntentRedelivery(boolean enabled) {
    mRedelivery = enabled;
    directlyOn(realIntentService, IntentService.class, "setIntentRedelivery", ClassParameter.from(boolean.class, enabled));
  }
}
