package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.hardware.location.ContextHubClient;
import android.hardware.location.ContextHubManager;
import android.hardware.location.ContextHubTransaction;
import android.hardware.location.NanoAppMessage;
import android.os.Build;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowContextHubManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = Build.VERSION_CODES.P)
public class ShadowContextHubClientTest {
  private Context context;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
  }

  @Test
  public void sendMessageToNanoApp_isSaved() {
    ContextHubManager contextHubManager =
        (ContextHubManager) context.getSystemService(Context.CONTEXTHUB_SERVICE);
    ContextHubClient contextHubClient = contextHubManager.createClient(null, null);
    NanoAppMessage message =
        NanoAppMessage.createMessageToNanoApp(
            0 /* targetNanoAppId */, 1 /* messageType */, new byte[0] /* messageBody */);

    assertThat(contextHubClient.sendMessageToNanoApp(message))
        .isEqualTo(ContextHubTransaction.RESULT_SUCCESS);

    List<NanoAppMessage> messages = ShadowContextHubClient.consumeNanoAppMessages();
    assertThat(messages).containsExactly(message);
  }

  @Test
  public void sendMessageToNanoApp_isDeletedAfterConsumed() {
    ContextHubManager contextHubManager =
        (ContextHubManager) context.getSystemService(Context.CONTEXTHUB_SERVICE);
    ContextHubClient contextHubClient = contextHubManager.createClient(null, null);
    NanoAppMessage message =
        NanoAppMessage.createMessageToNanoApp(
            0 /* targetNanoAppId */, 1 /* messageType */, new byte[0] /* messageBody */);

    assertThat(contextHubClient.sendMessageToNanoApp(message))
        .isEqualTo(ContextHubTransaction.RESULT_SUCCESS);

    ShadowContextHubClient.consumeNanoAppMessages();
    List<NanoAppMessage> messages = ShadowContextHubClient.consumeNanoAppMessages();
    assertThat(messages).isEmpty();
  }
}
