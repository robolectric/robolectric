package org.robolectric.android.internal;

import static com.google.common.base.Preconditions.checkState;

import android.app.Application;
import androidx.test.internal.platform.content.PermissionGranter;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowApplication;

/** A {@link PermissionGranter} that runs on a local JVM with Robolectric. */
public class LocalPermissionGranter implements PermissionGranter {

  private final List<String> permissions = new ArrayList<>();

  @Override
  public void addPermissions(String... permissions) {
    this.permissions.addAll(Arrays.asList(permissions));
  }

  @Override
  public void addOptionalPermissions(String... permissions) {
    this.permissions.addAll(Arrays.asList(permissions));
  }

  @Override
  public void requestPermissions() {
    checkState(!permissions.isEmpty());
    Application application =
        (Application) InstrumentationRegistry.getInstrumentation().getTargetContext();
    ShadowApplication shadowApplication = Shadow.extract(application);
    shadowApplication.grantPermissions(permissions.toArray(new String[permissions.size()]));
  }
}
