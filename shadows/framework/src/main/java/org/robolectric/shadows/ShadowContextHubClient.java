package org.robolectric.shadows;

import android.hardware.location.ContextHubClient;
import android.hardware.location.ContextHubTransaction;
import android.hardware.location.NanoAppMessage;
import android.os.Build.VERSION_CODES;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link ContextHubClient}. */
@Implements(
    value = ContextHubClient.class,
    minSdk = VERSION_CODES.P,
    isInAndroidSdk = false,
    looseSignatures = true)
public class ShadowContextHubClient {
  private final List<NanoAppMessage> messages = new ArrayList<>();

  @Implementation(minSdk = VERSION_CODES.P)
  @HiddenApi
  protected int sendMessageToNanoApp(NanoAppMessage message) {
    messages.add(message);
    return ContextHubTransaction.RESULT_SUCCESS;
  }

  @Implementation(minSdk = VERSION_CODES.P)
  @HiddenApi
  protected void close() {}

  public List<NanoAppMessage> getMessages() {
    return ImmutableList.copyOf(messages);
  }
}
