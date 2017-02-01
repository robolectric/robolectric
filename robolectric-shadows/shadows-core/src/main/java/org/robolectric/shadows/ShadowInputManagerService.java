package org.robolectric.shadows;

import android.content.Context;
import android.os.MessageQueue;
import android.view.InputChannel;
import com.android.server.input.InputManagerService;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = InputManagerService.class, isInAndroidSdk = false)
public class ShadowInputManagerService {

  @Implementation
  public static long nativeInit(InputManagerService inputManagerService, Context context, MessageQueue messageQueue) {
    return System.identityHashCode(inputManagerService);
  }

  @Implementation
  public InputChannel monitorInput(String inputChannelName) {
    return new InputChannel();
  }
}
