package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.hardware.location.ContextHubClient;
import android.hardware.location.ContextHubInfo;
import android.hardware.location.ContextHubManager;
import android.hardware.location.ContextHubMessage;
import android.os.Build;
import android.os.Handler;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowContextHubManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.N)
public class ShadowContextHubManagerTest {
  // Do not reference a non-public field in a test, because those get loaded outside the Robolectric
  // sandbox
  // DO NOT DO: private ContextHubManager contextHubManager;

  private Context context;
  private final Handler handler = new Handler();

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
  public void registerCallback_savesCallback() {
    ContextHubManager contextHubManager =
        (ContextHubManager) context.getSystemService(Context.CONTEXTHUB_SERVICE);
    ContextHubManager.Callback callback =
        new ContextHubManager.Callback() {
          @Override
          public void onMessageReceipt(
              final int hubId, final int nanoAppId, final ContextHubMessage message) {
            // do nothing
          }
        };

    assertThat(contextHubManager.registerCallback(callback, handler)).isEqualTo(0);

    assertThat(ShadowContextHubManager.getRegisteredCallback().first).isEqualTo(callback);
    assertThat(ShadowContextHubManager.getRegisteredCallback().second).isEqualTo(handler);
  }

  @Test
  public void registerCallback_failsAfterMultipleCalls() {
    ContextHubManager contextHubManager =
        (ContextHubManager) context.getSystemService(Context.CONTEXTHUB_SERVICE);
    ContextHubManager.Callback callback =
        new ContextHubManager.Callback() {
          @Override
          public void onMessageReceipt(
              final int hubId, final int nanoAppId, final ContextHubMessage message) {
            // do nothing
          }
        };

    assertThat(contextHubManager.registerCallback(callback, handler)).isEqualTo(0);
    assertThat(contextHubManager.registerCallback(callback, handler)).isEqualTo(-1);
  }

  @Test
  public void unregisterCallback_removesCallback() {
    ContextHubManager contextHubManager =
        (ContextHubManager) context.getSystemService(Context.CONTEXTHUB_SERVICE);
    ContextHubManager.Callback callback =
        new ContextHubManager.Callback() {
          @Override
          public void onMessageReceipt(
              final int hubId, final int nanoAppId, final ContextHubMessage message) {
            // do nothing
          }
        };
    contextHubManager.registerCallback(callback, handler);

    assertThat(contextHubManager.unregisterCallback(callback)).isEqualTo(0);
    assertThat(ShadowContextHubManager.getRegisteredCallback()).isNull();
  }

  @Test
  public void unregisterCallback_failsIfRegisterNotCalled() {
    ContextHubManager contextHubManager =
        (ContextHubManager) context.getSystemService(Context.CONTEXTHUB_SERVICE);
    ContextHubManager.Callback callback =
        new ContextHubManager.Callback() {
          @Override
          public void onMessageReceipt(
              final int hubId, final int nanoAppId, final ContextHubMessage message) {
            // do nothing
          }
        };

    assertThat(contextHubManager.unregisterCallback(callback)).isEqualTo(-1);
  }

  @Test
  public void sendMessage_isSaved() {
    ContextHubManager contextHubManager =
        (ContextHubManager) context.getSystemService(Context.CONTEXTHUB_SERVICE);
    ContextHubMessage message =
        new ContextHubMessage(0 /* messageType */, 0 /* version */, new byte[0] /* data */);
    int hubHandle = 0;
    int nanoAppHandle = 0;

    contextHubManager.sendMessage(hubHandle, nanoAppHandle, message);

    List<ShadowContextHubManager.NanoAppMessageInfo> messages =
        ShadowContextHubManager.consumeNanoAppMessages();
    assertThat(messages).isNotNull();
    assertThat(messages).hasSize(1);
    ShadowContextHubManager.NanoAppMessageInfo messageInfo = messages.get(0);
    assertThat(messageInfo.hubHandle).isEqualTo(hubHandle);
    assertThat(messageInfo.nanoAppHandle).isEqualTo(nanoAppHandle);
    assertThat(messageInfo.message).isEqualTo(message);
  }

  @Test
  public void sendMessage_isConsumedWhenRetrieved() {
    ContextHubManager contextHubManager =
        (ContextHubManager) context.getSystemService(Context.CONTEXTHUB_SERVICE);
    ContextHubMessage message =
        new ContextHubMessage(0 /* messageType */, 0 /* version */, new byte[0] /* data */);
    int hubHandle = 0;
    int nanoAppHandle = 0;

    contextHubManager.sendMessage(hubHandle, nanoAppHandle, message);

    ShadowContextHubManager.consumeNanoAppMessages();
    List<ShadowContextHubManager.NanoAppMessageInfo> messages =
        ShadowContextHubManager.consumeNanoAppMessages();
    assertThat(messages).isEmpty();
  }
}
