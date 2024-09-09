package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.app.PendingIntent;
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
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import com.google.auto.value.AutoValue;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowContextHubClient.ContextHubClientReflector;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link ContextHubManager}. */
@Implements(value = ContextHubManager.class, minSdk = VERSION_CODES.N, isInAndroidSdk = false)
public class ShadowContextHubManager {
  private static final List<ContextHubInfo> contextHubInfoList = new ArrayList<>();
  private static final List<ContextHubClient> contextHubClientWithPendingIntentList =
      new ArrayList<>();
  private static final Map<Integer, NanoAppInstanceInfo> nanoAppUidToInfo =
      new ConcurrentHashMap<>();
  private static final Multimap<ContextHubInfo, Integer> contextHubToNanoappUid =
      Multimaps.synchronizedMultimap(HashMultimap.<ContextHubInfo, Integer>create());
  private static final HashMultimap<String, ContextHubClient> attributionTagToClientMap =
      HashMultimap.create();
  private static final Map<ContextHubClient, ContextHubClientCallbackDetails>
      contextHubClientCallbacks = new HashMap<>();

  static {
    contextHubInfoList.add(new ContextHubInfo());
  }

  /** Adds a nanoApp to the list of nanoApps that are supported by the provided contexthubinfo. */
  public void addNanoApp(ContextHubInfo info, int nanoAppUid, long nanoAppId, int nanoAppVersion) {
    contextHubToNanoappUid.put(info, nanoAppUid);
    NanoAppInstanceInfo instanceInfo =
        createInstanceInfo(info, nanoAppUid, nanoAppId, nanoAppVersion);
    nanoAppUidToInfo.put(nanoAppUid, instanceInfo);
  }

  /** Creates and returns a {@link NanoAppInstanceInfo}. */
  public NanoAppInstanceInfo createInstanceInfo(
      ContextHubInfo info, int nanoAppUid, long nanoAppId, int nanoAppVersion) {
    if (VERSION.SDK_INT >= VERSION_CODES.P) {
      return new NanoAppInstanceInfo(nanoAppUid, nanoAppId, nanoAppVersion, info.getId());
    } else {
      NanoAppInstanceInfo instanceInfo = new NanoAppInstanceInfo();
      ReflectorNanoAppInstanceInfo reflectedInfo =
          reflector(ReflectorNanoAppInstanceInfo.class, instanceInfo);
      reflectedInfo.setAppId(nanoAppId);
      reflectedInfo.setAppVersion(nanoAppVersion);
      return instanceInfo;
    }
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
  protected @ClassName("android.hardware.location.ContextHubClient") Object createClient(
      @ClassName("android.hardware.location.ContextHubInfo") Object contextHubInfo,
      @ClassName("android.hardware.location.ContextHubClientCallback") Object callback) {

    ContextHubClient client;

    if (Build.VERSION.SDK_INT >= VERSION_CODES.Q) {
      client =
          reflector(ContextHubClientReflector.class)
              .newContextHubClient((ContextHubInfo) contextHubInfo, false);
    } else {
      client = reflector(ContextHubClientReflector.class).newContextHubClient();
    }

    if (callback != null) {
      contextHubClientCallbacks.put(
          client,
          ContextHubClientCallbackDetails.create(
              (ContextHubClientCallback) callback, MoreExecutors.directExecutor()));
    }
    return client;
  }

  @Implementation(minSdk = VERSION_CODES.P)
  @HiddenApi
  protected @ClassName("android.hardware.location.ContextHubClient") Object createClient(
      @ClassName("android.hardware.location.ContextHubInfo") Object contextHubInfo,
      @ClassName("android.hardware.location.ContextHubClientCallback") Object callback,
      Executor executor) {
    ContextHubClient client;
    if (Build.VERSION.SDK_INT >= VERSION_CODES.Q) {
      client =
          reflector(ContextHubClientReflector.class)
              .newContextHubClient((ContextHubInfo) contextHubInfo, false);
    } else {
      client = reflector(ContextHubClientReflector.class).newContextHubClient();
    }

    if (callback != null) {
      contextHubClientCallbacks.put(
          client,
          ContextHubClientCallbackDetails.create((ContextHubClientCallback) callback, executor));
    }

    return client;
  }

  @Implementation(minSdk = VERSION_CODES.Q)
  @HiddenApi
  protected @ClassName("android.hardware.location.ContextHubClient") Object createClient(
      @ClassName("android.hardware.location.ContextHubInfo") Object contextHubInfo,
      PendingIntent pendingIntent,
      long nanoAppId) {
    ContextHubClient client;
    if (Build.VERSION.SDK_INT >= VERSION_CODES.Q) {
      client =
          reflector(ContextHubClientReflector.class)
              .newContextHubClient((ContextHubInfo) contextHubInfo, false);
    } else {
      client = reflector(ContextHubClientReflector.class).newContextHubClient();
    }
    contextHubClientWithPendingIntentList.add(client);
    return client;
  }

  @Implementation(minSdk = VERSION_CODES.S)
  @HiddenApi
  protected @ClassName("android.hardware.location.ContextHubClient") Object createClient(
      Context context,
      @ClassName("android.hardware.location.ContextHubInfo") Object contextHubInfo,
      Executor executor,
      @ClassName("android.hardware.location.ContextHubClientCallback") Object callback) {
    ContextHubClient client =
        reflector(ContextHubClientReflector.class)
            .newContextHubClient((ContextHubInfo) contextHubInfo, false);
    if (context != null && ((Context) context).getAttributionTag() != null) {
      attributionTagToClientMap.put(((Context) context).getAttributionTag(), client);
    }

    if (callback != null) {
      contextHubClientCallbacks.put(
          client,
          ContextHubClientCallbackDetails.create((ContextHubClientCallback) callback, executor));
    }
    return client;
  }

  @Implementation(minSdk = VERSION_CODES.S)
  @HiddenApi
  protected @ClassName("android.hardware.location.ContextHubClient") Object createClient(
      Context context, ContextHubInfo contextHubInfo, PendingIntent pendingIntent, long nanoAppId) {
    ContextHubClient client =
        Shadow.newInstance(
            ContextHubClient.class,
            new Class<?>[] {ContextHubInfo.class, Boolean.TYPE},
            new Object[] {contextHubInfo, false});
    contextHubClientWithPendingIntentList.add(client);
    return client;
  }

  @Nullable
  public List<ContextHubClient> getClientsWithAttributionTag(String attributionTag) {
    return ImmutableList.copyOf(attributionTagToClientMap.get(attributionTag));
  }

  @Nullable
  public List<ContextHubClient> getContextHubClientWithPendingIntentList() {
    return ImmutableList.copyOf(contextHubClientWithPendingIntentList);
  }

  public void resetContextHub() {
    for (Map.Entry<ContextHubClient, ContextHubClientCallbackDetails> entry :
        contextHubClientCallbacks.entrySet()) {
      entry
          .getValue()
          .getExecutor()
          .execute(() -> entry.getValue().getCallback().onHubReset(entry.getKey()));
    }
  }

  public void broadcastMessageFromNanoApp(/*NanoAppMessage*/ Object message) {
    for (Map.Entry<ContextHubClient, ContextHubClientCallbackDetails> entry :
        contextHubClientCallbacks.entrySet()) {
      entry
          .getValue()
          .getExecutor()
          .execute(
              () ->
                  entry
                      .getValue()
                      .getCallback()
                      .onMessageFromNanoApp(entry.getKey(), (NanoAppMessage) message));
    }
  }

  public void broadcastNanoAppAborted(long nanoAppId, int abortCode) {
    for (Map.Entry<ContextHubClient, ContextHubClientCallbackDetails> entry :
        contextHubClientCallbacks.entrySet()) {
      entry
          .getValue()
          .getExecutor()
          .execute(
              () ->
                  entry
                      .getValue()
                      .getCallback()
                      .onNanoAppAborted(entry.getKey(), nanoAppId, abortCode));
    }
  }

  public void broadcastNanoAppLoaded(long nanoAppId) {
    for (Map.Entry<ContextHubClient, ContextHubClientCallbackDetails> entry :
        contextHubClientCallbacks.entrySet()) {
      entry
          .getValue()
          .getExecutor()
          .execute(() -> entry.getValue().getCallback().onNanoAppLoaded(entry.getKey(), nanoAppId));
    }
  }

  public void broadcastNanoAppUnloaded(long nanoAppId) {
    for (Map.Entry<ContextHubClient, ContextHubClientCallbackDetails> entry :
        contextHubClientCallbacks.entrySet()) {
      entry
          .getValue()
          .getExecutor()
          .execute(
              () -> entry.getValue().getCallback().onNanoAppUnloaded(entry.getKey(), nanoAppId));
    }
  }

  public void broadcastNanoAppEnabled(long nanoAppId) {
    for (Map.Entry<ContextHubClient, ContextHubClientCallbackDetails> entry :
        contextHubClientCallbacks.entrySet()) {
      entry
          .getValue()
          .getExecutor()
          .execute(
              () -> entry.getValue().getCallback().onNanoAppEnabled(entry.getKey(), nanoAppId));
    }
  }

  public void broadcastNanoAppDisabled(long nanoAppId) {

    for (Map.Entry<ContextHubClient, ContextHubClientCallbackDetails> entry :
        contextHubClientCallbacks.entrySet()) {
      entry
          .getValue()
          .getExecutor()
          .execute(
              () -> entry.getValue().getCallback().onNanoAppDisabled(entry.getKey(), nanoAppId));
    }
  }

  public void broadcastClientAuthorizationChanged(long nanoAppId, int authorization) {
    for (Map.Entry<ContextHubClient, ContextHubClientCallbackDetails> entry :
        contextHubClientCallbacks.entrySet()) {
      entry
          .getValue()
          .getExecutor()
          .execute(
              () ->
                  entry
                      .getValue()
                      .getCallback()
                      .onClientAuthorizationChanged(entry.getKey(), nanoAppId, authorization));
    }
  }

  @Resetter
  public static void clearContextHubClientWithPendingIntentList() {
    contextHubClientWithPendingIntentList.clear();
    nanoAppUidToInfo.clear();
    contextHubToNanoappUid.clear();
    attributionTagToClientMap.clear();
    contextHubClientCallbacks.clear();
  }

  @Implementation(minSdk = VERSION_CODES.P)
  @HiddenApi
  protected @ClassName(
      "android.hardware.location.ContextHubTransaction<java.util.List<android.hardware.location.NanoAppState>>")
  Object queryNanoApps(ContextHubInfo contextHubInfo) {
    @SuppressWarnings("unchecked")
    ContextHubTransaction<List<NanoAppState>> transaction =
        ReflectionHelpers.callConstructor(
            ContextHubTransaction.class,
            ClassParameter.from(int.class, ContextHubTransaction.TYPE_QUERY_NANOAPPS));
    Collection<Integer> uids = contextHubToNanoappUid.get(contextHubInfo);
    List<NanoAppState> nanoAppStates = new ArrayList<>();

    for (Integer uid : uids) {
      NanoAppInstanceInfo info = nanoAppUidToInfo.get(uid);
      if (info != null) {
        nanoAppStates.add(
            new NanoAppState(info.getAppId(), info.getAppVersion(), true /* enabled */));
      }
    }
    @SuppressWarnings("unchecked")
    ContextHubTransaction.Response<List<NanoAppState>> response =
        ReflectionHelpers.newInstance(ContextHubTransaction.Response.class);
    ReflectorContextHubTransactionResponse reflectedResponse =
        reflector(ReflectorContextHubTransactionResponse.class, response);
    reflectedResponse.setResult(ContextHubTransaction.RESULT_SUCCESS);
    reflectedResponse.setContents(nanoAppStates);
    reflector(ReflectorContextHubTransaction.class, transaction).setResponse(response);
    return transaction;
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
  protected NanoAppInstanceInfo getNanoAppInstanceInfo(int nanoAppHandle) {
    return nanoAppUidToInfo.get(nanoAppHandle);
  }

  @AutoValue
  abstract static class ContextHubClientCallbackDetails {
    @Nonnull
    abstract ContextHubClientCallback getCallback();

    @Nonnull
    abstract Executor getExecutor();

    static ContextHubClientCallbackDetails create(
        ContextHubClientCallback callback, Executor executor) {
      return new AutoValue_ShadowContextHubManager_ContextHubClientCallbackDetails(
          callback, executor);
    }
  }

  /** Accessor interface for {@link NanoAppInstanceInfo}'s internals. */
  @ForType(NanoAppInstanceInfo.class)
  private interface ReflectorNanoAppInstanceInfo {
    void setAppId(long nanoAppId);

    void setAppVersion(int nanoAppVersion);
  }

  /** Accessor interface for {@link ContextHubTransaction}'s internals. */
  @ForType(ContextHubTransaction.class)
  private interface ReflectorContextHubTransaction {
    void setResponse(ContextHubTransaction.Response<List<NanoAppState>> response);
  }

  /** Accessor interface for {@link ContextHubTransaction.Response}'s internals. */
  @ForType(ContextHubTransaction.Response.class)
  private interface ReflectorContextHubTransactionResponse {
    @Accessor("mResult")
    void setResult(int result);

    @Accessor("mContents")
    void setContents(List<NanoAppState> contents);
  }
}

