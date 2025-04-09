package org.robolectric.android.internal;

import static java.util.Objects.requireNonNull;

import android.app.Application;
import androidx.test.internal.platform.content.PermissionGranter;
import androidx.test.platform.app.InstrumentationRegistry;
import javax.annotation.Nonnull;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowApplication;

/** A {@link PermissionGranter} that runs on a local JVM with Robolectric. */
@SuppressWarnings("RestrictTo")
public class LocalPermissionGranter implements PermissionGranter {

  private String[] permissions;

  @Override
  public void addPermissions(@Nonnull String... permissions) {
    this.permissions = permissions;
  }

  @Override
  public void requestPermissions() {
    requireNonNull(permissions);
    Application application =
        (Application) InstrumentationRegistry.getInstrumentation().getTargetContext();
    ShadowApplication shadowApplication = Shadow.extract(application);
    shadowApplication.grantPermissions(permissions);
  }
}
