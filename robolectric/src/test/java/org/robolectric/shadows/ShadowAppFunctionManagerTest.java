package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

import android.app.appfunctions.AppFunctionManager;
import android.content.Context;
import com.google.common.util.concurrent.SettableFuture;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

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

    SettableFuture<Boolean> future = SettableFuture.create();
    appFunctionManager.isAppFunctionEnabled(
        "function_identifier", "com.android.packagename", directExecutor(), future::set);

    assertThat(future.get()).isTrue();
  }

  @Test
  public void setAppFunctionEnabled_true() throws Exception {
    ShadowServiceManager.setServiceAvailability(
        Context.APP_FUNCTION_SERVICE, /* available= */ true);
    AppFunctionManager appFunctionManager = getManager();

    SettableFuture<Void> setFuture = SettableFuture.create();
    appFunctionManager.setAppFunctionEnabled(
        "function_identifier",
        AppFunctionManager.APP_FUNCTION_STATE_ENABLED,
        directExecutor(),
        setFuture::set);
    setFuture.get();

    SettableFuture<Boolean> getFuture = SettableFuture.create();
    appFunctionManager.isAppFunctionEnabled(
        "function_identifier", directExecutor(), getFuture::set);
    assertThat(getFuture.get()).isTrue();
  }

  @Test
  public void setAppFunctionEnabled_false() throws Exception {
    ShadowServiceManager.setServiceAvailability(
        Context.APP_FUNCTION_SERVICE, /* available= */ true);
    AppFunctionManager appFunctionManager = getManager();

    SettableFuture<Void> setFuture = SettableFuture.create();
    appFunctionManager.setAppFunctionEnabled(
        "function_identifier",
        AppFunctionManager.APP_FUNCTION_STATE_DISABLED,
        directExecutor(),
        setFuture::set);
    setFuture.get();

    SettableFuture<Boolean> getFuture = SettableFuture.create();
    appFunctionManager.isAppFunctionEnabled(
        "function_identifier", directExecutor(), getFuture::set);
    assertThat(getFuture.get()).isFalse();
  }

  private AppFunctionManager getManager() {
    return (AppFunctionManager)
        getApplicationContext().getSystemService(Context.APP_FUNCTION_SERVICE);
  }
}
