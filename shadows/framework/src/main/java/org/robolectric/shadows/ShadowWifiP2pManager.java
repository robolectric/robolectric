package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Handler;
import android.os.Looper;
import com.google.common.base.Preconditions;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

@Implements(WifiP2pManager.class)
public class ShadowWifiP2pManager {

  private static final int NO_FAILURE = -1;

  private int listeningChannel;
  private int operatingChannel;
  private WifiP2pManager.GroupInfoListener groupInfoListener;
  private Handler handler;
  private int nextActionFailure = NO_FAILURE;
  private Map<Channel, WifiP2pGroup> p2pGroupmap = new HashMap<>();

  public int getListeningChannel() {
    return listeningChannel;
  }

  public int getOperatingChannel() {
    return operatingChannel;
  }

  public WifiP2pManager.GroupInfoListener getGroupInfoListener() {
    return groupInfoListener;
  }

  @Implementation(minSdk = KITKAT)
  protected void setWifiP2pChannels(
      Channel c, int listeningChannel, int operatingChannel, ActionListener al) {
    Preconditions.checkNotNull(c);
    Preconditions.checkNotNull(al);
    this.listeningChannel = listeningChannel;
    this.operatingChannel = operatingChannel;
  }

  @Implementation
  protected Channel initialize(
      Context context, Looper looper, WifiP2pManager.ChannelListener listener) {
    handler = new Handler(looper);
    return ReflectionHelpers.newInstance(Channel.class);
  }

  @Implementation
  protected void createGroup(Channel c, ActionListener al) {
    postActionListener(al);
  }

  private void postActionListener(final ActionListener al) {
    if (al == null) {
      return;
    }

    handler.post(new Runnable() {
      @Override
      public void run() {
        if (nextActionFailure == -1) {
          al.onSuccess();
        } else {
          al.onFailure(nextActionFailure);
        }
        nextActionFailure = NO_FAILURE;
      }
    });
  }

  @Implementation
  protected void requestGroupInfo(final Channel c, final WifiP2pManager.GroupInfoListener gl) {
    if (gl == null) {
      return;
    }

    handler.post(new Runnable() {
      @Override
      public void run() {
        gl.onGroupInfoAvailable(p2pGroupmap.get(c));
      }
    });
  }

  @Implementation
  protected void removeGroup(Channel c, ActionListener al) {
    postActionListener(al);
  }

  public void setNextActionFailure(int nextActionFailure) {
    this.nextActionFailure = nextActionFailure;
  }

  public void setGroupInfo(Channel channel, WifiP2pGroup wifiP2pGroup) {
    p2pGroupmap.put(channel, wifiP2pGroup);
  }
}
