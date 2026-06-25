package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.junit.Assert.assertThrows;

import android.app.appfunctions.AppFunctionManager;
import android.content.Context;
import android.os.OutcomeReceiver;
import com.google.common.util.concurrent.SettableFuture;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = BAKLAVA)
public class ShadowAppFunctionManagerTest {

  @Test
  public void appFunctionManager_nullByDefault() {
    AppFunctionManager appFunctionManager = getManager();

    assertThat(appFunctionManager).isNull();
  }

  @Test
  public void appFunctionManager_enableService_returnsManager() {
    ShadowServiceManager.setServiceAvailability(
        Context.APP_FUNCTION_SERVICE, /* available= */ true);
    AppFunctionManager appFunctionManager = getManager();

    assertThat(appFunctionManager).isNotNull();
  }

  @Test
  public void isAppFunctionEnabled_trueByDefault() throws Exception {
    ShadowServiceManager.setServiceAvailability(
        Context.APP_FUNCTION_SERVICE, /* available= */ true);
    AppFunctionManager appFunctionManager = getManager();

    boolean isAppFunctionEnabled =
        isAppFunctionEnabled(appFunctionManager, "function_identifier", "com.android.packagename");

    assertThat(isAppFunctionEnabled).isTrue();
  }

  @Test
  public void setAppFunctionEnabled_true() throws Exception {
    ShadowServiceManager.setServiceAvailability(
        Context.APP_FUNCTION_SERVICE, /* available= */ true);
    AppFunctionManager appFunctionManager = getManager();

    setAppFunctionEnabled(
        appFunctionManager, "function_identifier", AppFunctionManager.APP_FUNCTION_STATE_ENABLED);

    boolean isAppFunctionEnabled = isAppFunctionEnabled(appFunctionManager, "function_identifier");
    assertThat(isAppFunctionEnabled).isTrue();
  }

  @Test
  public void setAppFunctionEnabled_false() throws Exception {
    ShadowServiceManager.setServiceAvailability(
        Context.APP_FUNCTION_SERVICE, /* available= */ true);
    AppFunctionManager appFunctionManager = getManager();

    setAppFunctionEnabled(
        appFunctionManager, "function_identifier", AppFunctionManager.APP_FUNCTION_STATE_DISABLED);

    boolean isAppFunctionEnabled = isAppFunctionEnabled(appFunctionManager, "function_identifier");
    assertThat(isAppFunctionEnabled).isFalse();
  }

  @Test
  public void setAppFunctionEnabled_invalidState_throwsIllegalArgumentException() throws Exception {
    ShadowServiceManager.setServiceAvailability(
        Context.APP_FUNCTION_SERVICE, /* available= */ true);
    AppFunctionManager appFunctionManager = getManager();

    IllegalArgumentException e =
        assertThrows(
            IllegalArgumentException.class,
            () -> setAppFunctionEnabled(appFunctionManager, "function_identifier", 12348124));
    assertThat(e).hasMessageThat().contains("Invalid state: 12348124");
  }

  @Test
  public void setAppFunctionExists_false_setsIllegalArgumentExceptionInCallbacks()
      throws Exception {
    ShadowServiceManager.setServiceAvailability(
        Context.APP_FUNCTION_SERVICE, /* available= */ true);
    AppFunctionManager appFunctionManager = getManager();

    Shadow.<ShadowAppFunctionManager>extract(appFunctionManager)
        .setAppFunctionExists("function_identifier", false);

    ExecutionException isAppFunctionEnabledException =
        assertThrows(
            ExecutionException.class,
            () -> isAppFunctionEnabled(appFunctionManager, "function_identifier"));
    assertThat(isAppFunctionEnabledException)
        .hasCauseThat()
        .isInstanceOf(IllegalArgumentException.class);
    assertThat(isAppFunctionEnabledException)
        .hasCauseThat()
        .hasMessageThat()
        .contains("App function not found.");
    ExecutionException setAppFunctionEnabledException =
        assertThrows(
            ExecutionException.class,
            () ->
                setAppFunctionEnabled(
                    appFunctionManager,
                    "function_identifier",
                    AppFunctionManager.APP_FUNCTION_STATE_ENABLED));
    assertThat(setAppFunctionEnabledException)
        .hasCauseThat()
        .isInstanceOf(IllegalArgumentException.class);
    assertThat(setAppFunctionEnabledException)
        .hasCauseThat()
        .hasMessageThat()
        .contains("Function function_identifier does not exist");
  }

  @Test
  public void setAppFunctionExists_didNotExist_resetsState() throws Exception {
    ShadowServiceManager.setServiceAvailability(
        Context.APP_FUNCTION_SERVICE, /* available= */ true);
    AppFunctionManager appFunctionManager = getManager();
    setAppFunctionEnabled(
        appFunctionManager, "function_identifier", AppFunctionManager.APP_FUNCTION_STATE_DISABLED);

    Shadow.<ShadowAppFunctionManager>extract(appFunctionManager)
        .setAppFunctionExists("function_identifier", false);
    Shadow.<ShadowAppFunctionManager>extract(appFunctionManager)
        .setAppFunctionExists("function_identifier", true);

    boolean isAppFunctionEnabled = isAppFunctionEnabled(appFunctionManager, "function_identifier");
    assertThat(isAppFunctionEnabled).isTrue();
  }

  @Test
  public void setAppFunctionExists_alreadyExists_doesNotOverrideState() throws Exception {
    ShadowServiceManager.setServiceAvailability(
        Context.APP_FUNCTION_SERVICE, /* available= */ true);
    AppFunctionManager appFunctionManager = getManager();
    setAppFunctionEnabled(
        appFunctionManager, "function_identifier", AppFunctionManager.APP_FUNCTION_STATE_DISABLED);

    Shadow.<ShadowAppFunctionManager>extract(appFunctionManager)
        .setAppFunctionExists("function_identifier", true);

    boolean isAppFunctionEnabled = isAppFunctionEnabled(appFunctionManager, "function_identifier");
    assertThat(isAppFunctionEnabled).isFalse();
  }

  private static AppFunctionManager getManager() {
    return (AppFunctionManager)
        getApplicationContext().getSystemService(Context.APP_FUNCTION_SERVICE);
  }

  private static boolean isAppFunctionEnabled(
      AppFunctionManager appFunctionManager, String functionIdentifier, String targetPackage)
      throws Exception {
    SettableFuture<Boolean> future = SettableFuture.create();
    appFunctionManager.isAppFunctionEnabled(
        functionIdentifier, targetPackage, directExecutor(), toOutcomeReceiver(future));
    return future.get();
  }

  private static boolean isAppFunctionEnabled(
      AppFunctionManager appFunctionManager, String functionIdentifier) throws Exception {
    SettableFuture<Boolean> future = SettableFuture.create();
    appFunctionManager.isAppFunctionEnabled(
        functionIdentifier, directExecutor(), toOutcomeReceiver(future));
    return future.get();
  }

  private static void setAppFunctionEnabled(
      AppFunctionManager appFunctionManager, String functionIdentifier, int state)
      throws Exception {
    SettableFuture<Void> future = SettableFuture.create();
    appFunctionManager.setAppFunctionEnabled(
        functionIdentifier, state, directExecutor(), toOutcomeReceiver(future));
    future.get();
  }

  private static <T> OutcomeReceiver<T, Exception> toOutcomeReceiver(SettableFuture<T> future) {
    return new OutcomeReceiver<T, Exception>() {
      @Override
      public void onResult(T result) {
        future.set(result);
      }

      @Override
      public void onError(Exception e) {
        future.setException(e);
      }
    };
  }
}
