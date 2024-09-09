package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.permission.PermissionControllerService.SERVICE_INTERFACE;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowApplicationPackageManager.PERMISSION_CONTROLLER_PACKAGE_NAME;

import android.annotation.CallbackExecutor;
import android.annotation.NonNull;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Handler;
import android.os.Process;
import android.os.UserHandle;
import android.permission.PermissionControllerManager;
import android.permission.PermissionControllerManager.OnRevokeRuntimePermissionsCallback;
import android.permission.PermissionControllerManager.Reason;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Shadow for {@link PermissionControllerManager}. */
@Implements(value = PermissionControllerManager.class, minSdk = Q, isInAndroidSdk = false)
public class ShadowPermissionControllerManager {
  private static final AtomicBoolean resolveInfoRegistered = new AtomicBoolean();

  private PackageManager packageManager;

  @RealObject PermissionControllerManager realObject;

  @Implementation
  protected void __constructor__(@NonNull Context context, @NonNull Handler handler) {
    packageManager = context.getPackageManager();
    if (resolveInfoRegistered.compareAndSet(false, true)) {
      ensureResolveInfoRegistered();
    }
    // Invoke the original constructor.
    Shadow.invokeConstructor(
        PermissionControllerManager.class,
        realObject,
        ClassParameter.from(Context.class, context),
        ClassParameter.from(Handler.class, handler));
  }

  /**
   * This is a very basic implementation of this API that simply forwards the call to the
   * PackageManager. The real implementation is part of mainline and can easily evolve over time.
   * The real implementation upholds the semantic of different permissions properties by: not
   * revoking permissions that are considered fixed, revoking more permissions than the caller
   * requested if they are part of the same group, and more.
   */
  @Implementation
  protected void revokeRuntimePermissions(
      @NonNull Map<String, List<String>> request,
      boolean doDryRun,
      @Reason int reason,
      @NonNull @CallbackExecutor Executor executor,
      @NonNull OnRevokeRuntimePermissionsCallback callback) {
    if (doDryRun) {
      throw new UnsupportedOperationException("Dry run not implemented");
    }
    Map<String, List<String>> result = new HashMap<>();
    UserHandle userHandle = Process.myUserHandle();
    for (String packageName : request.keySet()) {
      List<String> permissions = request.get(packageName);
      List<String> revokedPermissions = new ArrayList<>();
      for (String permission : permissions) {
        try {
          packageManager.revokeRuntimePermission(packageName, permission, userHandle);
          revokedPermissions.add(permission);
        } catch (RuntimeException e) {
          // Ignore errors.
        }
      }
      if (!revokedPermissions.isEmpty()) {
        result.put(packageName, revokedPermissions);
      }
    }
    executor.execute(() -> callback.onRevokeRuntimePermissions(result));
  }

  private void ensureResolveInfoRegistered() {
    Intent intent = new Intent(SERVICE_INTERFACE);
    intent.setPackage("org.robolectric.permissioncontroller");
    ResolveInfo resolveInfo = new ResolveInfo();
    resolveInfo.serviceInfo = new ServiceInfo();
    resolveInfo.serviceInfo.packageName = PERMISSION_CONTROLLER_PACKAGE_NAME;
    resolveInfo.serviceInfo.name =
        "org.robolectric.permissioncontroller.PermissionControllerManagerService";
    shadowOf(packageManager).addResolveInfoForIntent(intent, resolveInfo);
  }

  @Resetter
  public static void reset() {
    resolveInfoRegistered.set(false);
  }
}
