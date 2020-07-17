package org.robolectric.shadows;

import android.hardware.location.ContextHubClient;
import android.hardware.location.ContextHubTransaction;
import android.hardware.location.NanoAppMessage;
import android.os.Build.VERSION_CODES;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow for {@link ContextHubClient}. */
@Implements(
    value = ContextHubClient.class,
    minSdk = VERSION_CODES.P,
    isInAndroidSdk = false,
    looseSignatures = true)
public class ShadowContextHubClient {
  private static final List<NanoAppMessage> nanoAppMessages = new ArrayList<>();

  /** Consume the current list of messages sent to the context hub and return them. */
  public static List<NanoAppMessage> consumeNanoAppMessages() {
    List<NanoAppMessage> returnedMessages = new ArrayList<>(nanoAppMessages);
    nanoAppMessages.clear();
    return returnedMessages;
  }

  @Resetter
  public static void reset() {
    nanoAppMessages.clear();
  }

  @Implementation(minSdk = VERSION_CODES.P)
  @HiddenApi
  protected int sendMessageToNanoApp(NanoAppMessage message) {
    nanoAppMessages.add(message);
    return ContextHubTransaction.RESULT_SUCCESS;
  }

  @Implementation(minSdk = VERSION_CODES.P)
  @HiddenApi
  protected void close() {}
}
