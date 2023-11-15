package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.hardware.location.ContextHubClient;
import android.hardware.location.ContextHubClientCallback;
import android.hardware.location.ContextHubInfo;
import android.hardware.location.ContextHubManager;
import android.hardware.location.ContextHubTransaction;
import android.hardware.location.NanoAppMessage;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = Q)
public final class ShadowContextHubClientTest {

  @Test
  public void whenAMessageIsSent_ensureItIsReturnedInTheMessageList() {
    ContextHubClient contextHubClient = (ContextHubClient) createContextHubClient();
    ShadowContextHubClient shadowContextHubClient = Shadow.extract(contextHubClient);

    // Send a message to the nano app.
    NanoAppMessage message =
        NanoAppMessage.createMessageToNanoApp(
            /* targetNanoAppId= */ 0L, /* messageType= */ 0, /* messageBody= */ new byte[10]);
    int returnValue = contextHubClient.sendMessageToNanoApp(message);

    // Ensure we have a successful result and our sent message is captured.
    assertThat(returnValue).isEqualTo(ContextHubTransaction.RESULT_SUCCESS);
    assertThat(shadowContextHubClient.getMessages()).containsExactly(message);
  }

  @Test
  public void whenContextHubIsClosed_ensureIsClosedIsTrue() {
    ContextHubClient contextHubClient = (ContextHubClient) createContextHubClient();
    ShadowContextHubClient shadowContextHubClient = Shadow.extract(contextHubClient);

    // We should be not closed on start.
    assertThat(shadowContextHubClient.isClosed()).isFalse();

    // Call close.
    contextHubClient.close();

    // After call to close, we should be closed.
    assertThat(shadowContextHubClient.isClosed()).isTrue();
  }

  @Test
  public void whenContextHubIsClosed_ensureSendingAMessageReturnsAnError() {
    ContextHubClient contextHubClient = (ContextHubClient) createContextHubClient();

    // Call close.
    contextHubClient.close();

    // Send a message to the nano app.
    NanoAppMessage message =
        NanoAppMessage.createMessageToNanoApp(
            /* targetNanoAppId= */ 0L, /* messageType= */ 0, /* messageBody= */ new byte[10]);
    int returnValue = contextHubClient.sendMessageToNanoApp(message);

    // Ensure an error was returned while attempting to send a message to the nano app.
    assertThat(returnValue).isEqualTo(ContextHubTransaction.RESULT_FAILED_UNKNOWN);
  }

  private Object createContextHubClient() {
    Context context = ApplicationProvider.getApplicationContext();
    ContextHubManager contextHubManager = context.getSystemService(ContextHubManager.class);
    ContextHubInfo contextHubInfo = contextHubManager.getContextHubs().get(0);
    ContextHubClientCallback contextHubClientCallback = new ContextHubClientCallback();
    ContextHubClient contextHubClient =
        contextHubManager.createClient(contextHubInfo, contextHubClientCallback);
    return contextHubClient;
  }
}
