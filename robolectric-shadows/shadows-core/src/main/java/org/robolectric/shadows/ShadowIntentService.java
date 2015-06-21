package org.robolectric.shadows;

import android.app.IntentService;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

import static org.robolectric.internal.Shadow.directlyOn;

/**
 * Shadow for {@link android.app.IntentService}.
 */
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
  public void setIntentRedelivery(boolean enabled) {
    mRedelivery = enabled;
    directlyOn(realIntentService, IntentService.class, "setIntentRedelivery", ClassParameter.from(boolean.class, enabled));
  }
}
