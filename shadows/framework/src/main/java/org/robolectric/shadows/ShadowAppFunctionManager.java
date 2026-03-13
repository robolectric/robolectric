package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.appfunctions.AppFunctionManager;
import android.content.Context;
import android.os.OutcomeReceiver;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow for the {@link AppFunctionManager} framework class. */
@Implements(value = AppFunctionManager.class, minSdk = BAKLAVA)
public class ShadowAppFunctionManager {

  @RealObject private AppFunctionManager realAppFunctionManager;

  private final Map<String, Integer> appFunctionEnabledMap = new ConcurrentHashMap<>();

  @Implementation
  protected void isAppFunctionEnabledInternal(
      String functionIdentifier,
      String targetPackage,
      Executor executor,
      OutcomeReceiver<Boolean, Exception> callback) {
    String functionIdKey = functionIdKey(targetPackage, functionIdentifier);
    // TODO: Better handling of default state.
    boolean enabled =
        appFunctionEnabledMap.getOrDefault(
                functionIdKey, AppFunctionManager.APP_FUNCTION_STATE_DEFAULT)
            != AppFunctionManager.APP_FUNCTION_STATE_DISABLED;
    executor.execute(() -> callback.onResult(enabled));
  }

  @Implementation
  protected void setAppFunctionEnabled(
      String functionIdentifier,
      int state,
      Executor executor,
      OutcomeReceiver<Void, Exception> callback) {
    String functionIdKey =
        functionIdKey(
            reflector(AppFunctionManagerReflector.class, realAppFunctionManager)
                .getContext()
                .getPackageName(),
            functionIdentifier);
    // TODO: Validate state.
    appFunctionEnabledMap.put(functionIdKey, state);
    executor.execute(() -> callback.onResult(null));
  }

  private static String functionIdKey(String targetPackage, String functionIdentifier) {
    return String.format("%s:%s", targetPackage, functionIdentifier);
  }

  @ForType(AppFunctionManager.class)
  interface AppFunctionManagerReflector {
    @Accessor("mContext")
    Context getContext();
  }
}
