package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static com.google.common.base.Preconditions.checkArgument;
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
@Implements(value = AppFunctionManager.class, minSdk = BAKLAVA, isInAndroidSdk = false)
public class ShadowAppFunctionManager {

  private static final int APP_FUNCTION_STATE_DOES_NOT_EXIST = -1234;

  @RealObject private AppFunctionManager realAppFunctionManager;

  private final Map<String, Integer> appFunctionEnabledMap = new ConcurrentHashMap<>();

  @Implementation
  protected void isAppFunctionEnabledInternal(
      String functionIdentifier,
      String targetPackage,
      Executor executor,
      OutcomeReceiver<Boolean, Exception> callback) {
    String functionIdKey = functionIdKey(targetPackage, functionIdentifier);
    int state =
        appFunctionEnabledMap.getOrDefault(
            functionIdKey, AppFunctionManager.APP_FUNCTION_STATE_DEFAULT);
    if (state == APP_FUNCTION_STATE_DOES_NOT_EXIST) {
      executor.execute(
          () -> callback.onError(new IllegalArgumentException("App function not found.")));
    } else {
      // TODO: Better handling of default state.
      executor.execute(
          () -> callback.onResult(state != AppFunctionManager.APP_FUNCTION_STATE_DISABLED));
    }
  }

  @Implementation
  protected void setAppFunctionEnabled(
      String functionIdentifier,
      int state,
      Executor executor,
      OutcomeReceiver<Void, Exception> callback) {
    checkValidAppFunctionState(state);
    String functionIdKey =
        functionIdKey(
            reflector(AppFunctionManagerReflector.class, realAppFunctionManager)
                .getContext()
                .getPackageName(),
            functionIdentifier);
    int newState =
        appFunctionEnabledMap.compute(
            functionIdKey,
            (key, oldState) -> {
              if (oldState != null && oldState == APP_FUNCTION_STATE_DOES_NOT_EXIST) {
                return oldState;
              }
              return state;
            });
    if (newState == APP_FUNCTION_STATE_DOES_NOT_EXIST) {
      executor.execute(
          () ->
              callback.onError(
                  new IllegalArgumentException(
                      String.format("Function %s does not exist", functionIdentifier))));
    } else {
      executor.execute(() -> callback.onResult(null));
    }
  }

  /**
   * Allows simulating the app function not existing in system server by making the {@link
   * AppFunctionManager} return an {@link IllegalArgumentException} in the associated error
   * callbacks if {@code exists} is {@code false}.
   */
  public void setAppFunctionExists(String functionIdentifier, boolean exists) {
    String functionIdKey =
        functionIdKey(
            reflector(AppFunctionManagerReflector.class, realAppFunctionManager)
                .getContext()
                .getPackageName(),
            functionIdentifier);
    if (exists) {
      appFunctionEnabledMap.computeIfPresent(
          functionIdKey,
          (key, oldState) -> oldState == APP_FUNCTION_STATE_DOES_NOT_EXIST ? null : oldState);
    } else {
      appFunctionEnabledMap.put(functionIdKey, APP_FUNCTION_STATE_DOES_NOT_EXIST);
    }
  }

  private static void checkValidAppFunctionState(int state) {
    checkArgument(
        state == AppFunctionManager.APP_FUNCTION_STATE_DISABLED
            || state == AppFunctionManager.APP_FUNCTION_STATE_ENABLED
            || state == AppFunctionManager.APP_FUNCTION_STATE_DEFAULT,
        "Invalid state: %s",
        state);
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
