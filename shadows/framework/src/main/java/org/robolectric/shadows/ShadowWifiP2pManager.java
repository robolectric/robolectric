package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.Context;
import android.net.wifi.p2p.IWifiP2pListener;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

@Implements(WifiP2pManager.class)
public class ShadowWifiP2pManager {

  private static final int NO_FAILURE = -1;

  private static int listeningChannel;
  private static int operatingChannel;
  private static WifiP2pManager.GroupInfoListener groupInfoListener;
  private static Handler handler;
  private static int nextActionFailure = NO_FAILURE;
  private static final Map<Channel, WifiP2pGroup> p2pGroupmap = new HashMap<>();
  private static final Map<Channel, WifiP2pInfo> p2pConnectionInfoMap = new HashMap<>();

  @RealObject private WifiP2pManager realWifiP2pManager;

  public int getListeningChannel() {
    return listeningChannel;
  }

  public int getOperatingChannel() {
    return operatingChannel;
  }

  public WifiP2pManager.GroupInfoListener getGroupInfoListener() {
    return groupInfoListener;
  }

  @Implementation
  protected void setWifiP2pChannels(
      Channel c, int listeningChannel, int operatingChannel, ActionListener al) {
    Objects.requireNonNull(c);
    Objects.requireNonNull(al);
    ShadowWifiP2pManager.listeningChannel = listeningChannel;
    ShadowWifiP2pManager.operatingChannel = operatingChannel;
  }

  @Implementation
  protected Channel initialize(
      Context context, Looper looper, WifiP2pManager.ChannelListener listener) {
    handler = new Handler(looper);
    if (RuntimeEnvironment.getApiLevel() >= VANILLA_ICE_CREAM) {
      // Needed for registerWifiP2pListener to work.
      reflector(WifiP2pManagerReflector.class, realWifiP2pManager).setContext(context);
    }
    return ReflectionHelpers.newInstance(Channel.class);
  }

  @Implementation
  protected void createGroup(Channel c, ActionListener al) {
    postActionListener(al);
  }

  @Implementation(minSdk = Q)
  protected void createGroup(Channel c, WifiP2pConfig config, ActionListener al) {
    postActionListener(al);
  }

  @Implementation
  protected void connect(Channel c, WifiP2pConfig config, ActionListener al) {
    postActionListener(al);
  }

  private void postActionListener(final ActionListener al) {
    if (al == null) {
      return;
    }

    handler.post(
        () -> {
          if (nextActionFailure == -1) {
            al.onSuccess();
          } else {
            al.onFailure(nextActionFailure);
          }
          nextActionFailure = NO_FAILURE;
        });
  }

  @Implementation
  protected void requestGroupInfo(final Channel c, final WifiP2pManager.GroupInfoListener gl) {
    if (gl == null) {
      return;
    }

    handler.post(() -> gl.onGroupInfoAvailable(p2pGroupmap.get(c)));
  }

  @Implementation
  protected void requestConnectionInfo(
      final Channel c, final WifiP2pManager.ConnectionInfoListener listener) {
    if (listener == null) {
      return;
    }

    handler.post(
        () ->
            listener.onConnectionInfoAvailable(
                p2pConnectionInfoMap.getOrDefault(c, new WifiP2pInfo())));
  }

  @Implementation
  protected void removeGroup(Channel c, ActionListener al) {
    postActionListener(al);
  }

  public List<WifiP2pManager.WifiP2pListener> getWifiP2pListeners() {
    SparseArray<IWifiP2pListener> sparseArray =
        reflector(WifiP2pManagerReflector.class).getWifiP2pListenerMap();
    ImmutableList.Builder<WifiP2pManager.WifiP2pListener> result = ImmutableList.builder();
    for (int i = 0; i < sparseArray.size(); i++) {
      result.add(
          reflector(OnWifiP2pListenerProxyReflector.class, sparseArray.valueAt(i)).getListener());
    }
    return result.build();
  }

  public void setNextActionFailure(int nextActionFailure) {
    ShadowWifiP2pManager.nextActionFailure = nextActionFailure;
  }

  public void setGroupInfo(Channel channel, WifiP2pGroup wifiP2pGroup) {
    p2pGroupmap.put(channel, wifiP2pGroup);
  }

  public void setConnectionInfo(Channel channel, WifiP2pInfo wifiP2pInfo) {
    p2pConnectionInfoMap.put(channel, wifiP2pInfo);
  }

  @Resetter
  public static void reset() {
    listeningChannel = 0;
    operatingChannel = 0;
    groupInfoListener = null;
    handler = null;
    nextActionFailure = NO_FAILURE;
    p2pGroupmap.clear();
    p2pConnectionInfoMap.clear();
    if (RuntimeEnvironment.getApiLevel() >= VANILLA_ICE_CREAM) {
      reflector(WifiP2pManagerReflector.class).getWifiP2pListenerMap().clear();
    }
  }

  @ForType(WifiP2pManager.class)
  interface WifiP2pManagerReflector {
    @Accessor("sWifiP2pListenerMap")
    SparseArray<IWifiP2pListener> getWifiP2pListenerMap();

    @Accessor("mContext")
    void setContext(Context context);
  }

  @ForType(className = "android.net.wifi.p2p.WifiP2pManager$OnWifiP2pListenerProxy")
  interface OnWifiP2pListenerProxyReflector {
    @Accessor("mListener")
    WifiP2pManager.WifiP2pListener getListener();
  }
}
