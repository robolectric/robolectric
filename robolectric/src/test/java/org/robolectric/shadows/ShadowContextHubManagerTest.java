package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.hardware.location.ContextHubClient;
import android.hardware.location.ContextHubClientCallback;
import android.hardware.location.ContextHubInfo;
import android.hardware.location.ContextHubManager;
import android.hardware.location.ContextHubTransaction;
import android.hardware.location.NanoAppInstanceInfo;
import android.hardware.location.NanoAppMessage;
import android.hardware.location.NanoAppState;
import android.os.Build;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

/** Tests for {@link ShadowContextHubManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.N)
public class ShadowContextHubManagerTest {
  // Do not reference a non-public field in a test, because those get loaded outside the Robolectric
  // sandbox
  // DO NOT DO: private ContextHubManager contextHubManager;

  private Context context;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.P)
  public void getContextHubs_returnsValidList() {
    ContextHubManager contextHubManager =
        (ContextHubManager) context.getSystemService(Context.CONTEXTHUB_SERVICE);
    List<ContextHubInfo> contextHubInfoList = contextHubManager.getContextHubs();
    assertThat(contextHubInfoList).isNotNull();
    assertThat(contextHubInfoList).isNotEmpty();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.P)
  public void createClient_returnsValidClient() {
    ContextHubManager contextHubManager =
        (ContextHubManager) context.getSystemService(Context.CONTEXTHUB_SERVICE);
    ContextHubClient contextHubClient = contextHubManager.createClient(null, null);
    assertThat(contextHubClient).isNotNull();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.P)
  public void queryNanoApps_returnsValidNanoApps() throws Exception {
    ContextHubManager contextHubManager = context.getSystemService(ContextHubManager.class);
    ShadowContextHubManager shadowManager = Shadow.extract(contextHubManager);
    List<ContextHubInfo> contextHubInfoList = contextHubManager.getContextHubs();
    long nanoAppId = 5;
    int nanoAppVersion = 1;
    shadowManager.addNanoApp(
        contextHubInfoList.get(0), /* nanoAppUid= */ 0, nanoAppId, nanoAppVersion);

    ContextHubTransaction<List<NanoAppState>> transaction =
        contextHubManager.queryNanoApps(contextHubInfoList.get(0));

    assertThat(transaction.getType()).isEqualTo(ContextHubTransaction.TYPE_QUERY_NANOAPPS);
    ContextHubTransaction.Response<List<NanoAppState>> response =
        transaction.waitForResponse(1, SECONDS);
    assertThat(response.getResult()).isEqualTo(ContextHubTransaction.RESULT_SUCCESS);
    List<NanoAppState> states = response.getContents();
    assertThat(states).isNotNull();
    assertThat(states).hasSize(1);
    NanoAppState state = states.get(0);
    assertThat(state.getNanoAppId()).isEqualTo(nanoAppId);
    assertThat(state.getNanoAppVersion()).isEqualTo(nanoAppVersion);
    assertThat(state.isEnabled()).isTrue();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.P)
  public void queryNanoApps_noNanoAppsAdded() throws Exception {
    ContextHubManager contextHubManager = context.getSystemService(ContextHubManager.class);
    List<ContextHubInfo> contextHubInfoList = contextHubManager.getContextHubs();

    ContextHubTransaction<List<NanoAppState>> transaction =
        contextHubManager.queryNanoApps(contextHubInfoList.get(0));

    assertThat(transaction.getType()).isEqualTo(ContextHubTransaction.TYPE_QUERY_NANOAPPS);
    ContextHubTransaction.Response<List<NanoAppState>> response =
        transaction.waitForResponse(1, SECONDS);
    assertThat(response.getResult()).isEqualTo(ContextHubTransaction.RESULT_SUCCESS);
    List<NanoAppState> states = response.getContents();
    assertThat(states).isNotNull();
    assertThat(states).isEmpty();
  }

  @Test
  public void getContextHubHandles_returnsValidArray() {
    ContextHubManager contextHubManager =
        (ContextHubManager) context.getSystemService(Context.CONTEXTHUB_SERVICE);
    int[] handles = contextHubManager.getContextHubHandles();
    assertThat(handles).isNotNull();
    assertThat(handles).isNotEmpty();
  }

  @Test
  public void getContextHubInfo_returnsValidInfo() {
    ContextHubManager contextHubManager =
        (ContextHubManager) context.getSystemService(Context.CONTEXTHUB_SERVICE);
    int[] handles = contextHubManager.getContextHubHandles();
    assertThat(handles).isNotNull();
    for (int handle : handles) {
      assertThat(contextHubManager.getContextHubInfo(handle)).isNotNull();
    }
  }

  @Test
  public void getContextHubInfo_returnsInvalidInfo() {
    ContextHubManager contextHubManager =
        (ContextHubManager) context.getSystemService(Context.CONTEXTHUB_SERVICE);
    int[] handles = contextHubManager.getContextHubHandles();
    assertThat(handles).isNotNull();
    assertThat(contextHubManager.getContextHubInfo(-1)).isNull();
    assertThat(contextHubManager.getContextHubInfo(handles.length)).isNull();
  }

  @Test
  public void getNanoAppInstanceInfo_returnsValidInfo() {
    ContextHubManager contextHubManager = context.getSystemService(ContextHubManager.class);
    ShadowContextHubManager shadowManager = Shadow.extract(contextHubManager);
    int[] handles = contextHubManager.getContextHubHandles();
    ContextHubInfo hubInfo = contextHubManager.getContextHubInfo(handles[0]);
    long nanoAppId = 5;
    int nanoAppVersion = 1;
    int nanoAppUid = 0;
    shadowManager.addNanoApp(hubInfo, nanoAppUid, nanoAppId, nanoAppVersion);

    NanoAppInstanceInfo info = contextHubManager.getNanoAppInstanceInfo(nanoAppUid);

    assertThat(info).isNotNull();
    assertThat(info.getAppId()).isEqualTo(nanoAppId);
    assertThat(info.getAppVersion()).isEqualTo(nanoAppVersion);
  }

  @Test
  public void getNanoAppInstanceInfo_noNanoAppsAdded() {
    ContextHubManager contextHubManager = context.getSystemService(ContextHubManager.class);

    NanoAppInstanceInfo info = contextHubManager.getNanoAppInstanceInfo(0 /* nanoAppUid */);

    assertThat(info).isNull();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.P)
  public void broadcastsFromContextHub_notifiesClient() {
    ContextHubManager contextHubManager = context.getSystemService(ContextHubManager.class);
    ContextHubClientCallback callback = mock(ContextHubClientCallback.class);

    contextHubManager.createClient(null, callback);

    verify(callback, never()).onNanoAppLoaded(any(), anyLong());

    ShadowContextHubManager shadowManager = Shadow.extract(contextHubManager);
    shadowManager.broadcastNanoAppLoaded(1);

    verify(callback).onNanoAppLoaded(any(), anyLong());
    verify(callback, never()).onMessageFromNanoApp(any(), any());

    shadowManager.broadcastMessageFromNanoApp(
        NanoAppMessage.createMessageToNanoApp(1, 1, new byte[0]));

    verify(callback).onMessageFromNanoApp(any(), any());
    verify(callback, never()).onHubReset(any());

    shadowManager.resetContextHub();

    verify(callback).onHubReset(any());
    verify(callback, never()).onNanoAppAborted(any(), anyLong(), anyInt());

    shadowManager.broadcastNanoAppAborted(1, 0);

    verify(callback).onNanoAppAborted(any(), anyLong(), anyInt());
    verify(callback, never()).onNanoAppUnloaded(any(), anyLong());

    shadowManager.broadcastNanoAppUnloaded(1);

    verify(callback).onNanoAppUnloaded(any(), anyLong());
    verify(callback, never()).onNanoAppEnabled(any(), anyLong());

    shadowManager.broadcastNanoAppEnabled(1);

    verify(callback).onNanoAppEnabled(any(), anyLong());
    verify(callback, never()).onNanoAppDisabled(any(), anyLong());

    shadowManager.broadcastNanoAppDisabled(1);

    verify(callback).onNanoAppDisabled(any(), anyLong());
    if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.S) {
      // onClientAuthorizationChanged was added in S
      verify(callback, never()).onClientAuthorizationChanged(any(), anyLong(), anyInt());
    }
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.S)
  public void broadcastsClientAuthorizationChanged_notifiesClient() {
    ContextHubManager contextHubManager = context.getSystemService(ContextHubManager.class);
    ContextHubClientCallback callback = mock(ContextHubClientCallback.class);
    contextHubManager.createClient(null, callback);

    verify(callback, never()).onClientAuthorizationChanged(any(), anyLong(), anyInt());

    ShadowContextHubManager shadowManager = Shadow.extract(contextHubManager);
    shadowManager.broadcastClientAuthorizationChanged(1, 2);

    verify(callback).onClientAuthorizationChanged(any(), anyLong(), anyInt());
    verify(callback, never()).onNanoAppLoaded(any(), anyLong());
    verify(callback, never()).onMessageFromNanoApp(any(), any());
    verify(callback, never()).onHubReset(any());
    verify(callback, never()).onNanoAppAborted(any(), anyLong(), anyInt());
    verify(callback, never()).onNanoAppUnloaded(any(), anyLong());
    verify(callback, never()).onNanoAppEnabled(any(), anyLong());
    verify(callback, never()).onNanoAppDisabled(any(), anyLong());
  }
}
