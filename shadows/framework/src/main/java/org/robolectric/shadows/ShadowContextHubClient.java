package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.RequiresApi;
import android.annotation.TargetApi;
import android.hardware.location.ContextHubClient;
import android.hardware.location.ContextHubInfo;
import android.hardware.location.ContextHubTransaction;
import android.hardware.location.NanoAppMessage;
import android.os.Build.VERSION_CODES;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link ContextHubClient}. */
@Implements(value = ContextHubClient.class, minSdk = VERSION_CODES.P, isInAndroidSdk = false)
public class ShadowContextHubClient {
  @RealObject private ContextHubClient realContextHubClient;
  private final List<NanoAppMessage> messages = new ArrayList<>();

  @Implementation(minSdk = VERSION_CODES.P)
  @HiddenApi
  protected int sendMessageToNanoApp(NanoAppMessage message) {
    if (isClosed()) {
      return ContextHubTransaction.RESULT_FAILED_UNKNOWN;
    }
    messages.add(message);
    return ContextHubTransaction.RESULT_SUCCESS;
  }

  @Implementation(minSdk = VERSION_CODES.P)
  @HiddenApi
  protected void close() {
    reflector(ContextHubClientReflector.class, realContextHubClient).getIsClosed().set(true);
  }

  public List<NanoAppMessage> getMessages() {
    return ImmutableList.copyOf(messages);
  }

  @RequiresApi(VERSION_CODES.Q)
  public boolean isClosed() {
    return reflector(ContextHubClientReflector.class, realContextHubClient).getIsClosed().get();
  }

  @ForType(ContextHubClient.class)
  interface ContextHubClientReflector {
    @Constructor
    @TargetApi(VERSION_CODES.P)
    ContextHubClient newContextHubClient();

    @Constructor
    @RequiresApi(VERSION_CODES.Q)
    ContextHubClient newContextHubClient(ContextHubInfo hubInfo, boolean persistent);

    @Accessor("mIsClosed")
    AtomicBoolean getIsClosed();
  }
}
