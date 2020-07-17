package org.robolectric.shadows;

import android.hardware.location.ContextHubClient;
import android.hardware.location.ContextHubInfo;
import android.hardware.location.ContextHubManager;
import android.hardware.location.ContextHubMessage;
import android.os.Build.VERSION_CODES;
import android.os.Handler;
import android.util.Pair;
import java.util.ArrayList;
import java.util.List;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

/** Shadow for {@link ContextHubManager}. */
@Implements(
    value = ContextHubManager.class,
    minSdk = VERSION_CODES.N,
    isInAndroidSdk = false,
    looseSignatures = true)
public class ShadowContextHubManager {
  private static final List<ContextHubInfo> contextHubInfoList = new ArrayList<>();
  private static Pair<ContextHubManager.Callback, Handler> registeredCallback;
  private static final List<NanoAppMessageInfo> nanoAppMessages = new ArrayList<>();

  @Resetter
  public static void reset() {
    registeredCallback = null;
    nanoAppMessages.clear();
  }

  static {
    // Populate certain fields of the contexthub to ensure valid info is used by tests.
    ContextHubInfo info = new ContextHubInfo();
    ReflectionHelpers.setField(ContextHubInfo.class, info, "mMaxPacketLengthBytes", 4096);
    ReflectionHelpers.setField(ContextHubInfo.class, info, "mName", "test");
    ReflectionHelpers.setField(ContextHubInfo.class, info, "mVendor", "robolectric");
    contextHubInfoList.add(info);
  }

  /** Consume the current list of messages sent to the context hub and return them. */
  public static List<NanoAppMessageInfo> consumeNanoAppMessages() {
    List<NanoAppMessageInfo> returnedMessages = new ArrayList<>(nanoAppMessages);
    nanoAppMessages.clear();
    return returnedMessages;
  }

  /**
   * Returns the currently registered callback, if any. This is only useful if the app invokes
   * {@link registerCallback}. Otherwise, no callback will be registered.
   *
   * @return A {@link Pair} containing the registered callback and the handler it was registered
   *     with.
   */
  public static Pair<ContextHubManager.Callback, Handler> getRegisteredCallback() {
    return registeredCallback;
  }

  /**
   * Provides a list with fake {@link ContextHubInfo}s.
   *
   * <p>{@link ContextHubInfo} describes an optional physical chip on the device. This does not
   * exist in test; this implementation allows to avoid possible NPEs.
   */
  @Implementation(minSdk = VERSION_CODES.P)
  @HiddenApi
  protected List<ContextHubInfo> getContextHubs() {
    return contextHubInfoList;
  }

  @Implementation(minSdk = VERSION_CODES.P)
  @HiddenApi
  protected Object /* ContextHubClient */ createClient(
      Object /* ContextHubInfo */ contextHubInfo,
      Object /* ContextHubClientCallback */ contextHubClientCallback) {
    return ReflectionHelpers.newInstance(ContextHubClient.class);
  }

  @Implementation(minSdk = VERSION_CODES.P)
  @HiddenApi
  protected Object /* ContextHubClient */ createClient(
      Object /* ContextHubInfo */ contextHubInfo,
      Object /* ContextHubClientCallback */ contextHubClientCallback,
      Object /* Executor */ executor) {
    return ReflectionHelpers.newInstance(ContextHubClient.class);
  }

  /**
   * Provides an array of fake handles.
   *
   * <p>These describe an optional physical chip on the device which does not exist during testing.
   * This implementation enables testing of classes that utilize these APIs.
   */
  @Implementation
  @HiddenApi
  protected int[] getContextHubHandles() {
    int[] handles = new int[contextHubInfoList.size()];
    for (int i = 0; i < handles.length; i++) {
      handles[i] = i;
    }
    return handles;
  }

  @Implementation
  @HiddenApi
  protected ContextHubInfo getContextHubInfo(int hubHandle) {
    if (hubHandle < 0 || hubHandle >= contextHubInfoList.size()) {
      return null;
    }

    return contextHubInfoList.get(hubHandle);
  }

  @Implementation
  @HiddenApi
  protected int registerCallback(ContextHubManager.Callback callback, Handler handler) {
    if (registeredCallback != null) {
      return -1;
    }
    registeredCallback = new Pair<>(callback, handler);
    return 0;
  }

  @Implementation
  @HiddenApi
  public int unregisterCallback(ContextHubManager.Callback callback) {
    if (registeredCallback == null || registeredCallback.first != callback) {
      return -1;
    }

    registeredCallback = null;
    return 0;
  }

  @Implementation
  @HiddenApi
  public int sendMessage(int hubHandle, int nanoAppHandle, ContextHubMessage message) {
    nanoAppMessages.add(new NanoAppMessageInfo(hubHandle, nanoAppHandle, message));
    return 0;
  }

  /**
   * Class used to wrap information sent alongside a message to nanoapps to make it easier for
   * clients to consume it.
   */
  public static class NanoAppMessageInfo {
    public int hubHandle;
    public int nanoAppHandle;
    public ContextHubMessage message;

    public NanoAppMessageInfo(int hubHandle, int nanoAppHandle, ContextHubMessage message) {
      this.hubHandle = hubHandle;
      this.nanoAppHandle = nanoAppHandle;
      this.message = message;
    }
  }
}
