package org.robolectric.android.internal;

import static com.google.common.base.Preconditions.checkNotNull;

import android.app.Application;
import androidx.test.internal.platform.content.PermissionGranter;
import androidx.test.platform.app.InstrumentationRegistry;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowApplication;

/** A {@link PermissionGranter} that runs on a local JVM with Robolectric. */
@SuppressWarnings("RestrictTo")
public class LocalPermissionGranter implements PermissionGranter {

  private String[] permissions;

  @Override
  public void addPermissions(String... permissions) {
    this.permissions = permissions;
  }

  @Override
  public void requestPermissions() {
    checkNotNull(permissions);
    Application application =
        (Application) InstrumentationRegistry.getInstrumentation().getTargetContext();
    ShadowApplication shadowApplication = Shadow.extract(application);
    shadowApplication.grantPermissions(permissions);
  }
}
