package android.hardware.display;

import android.media.projection.IMediaProjection;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.DisplayInfo;
import android.view.Surface;

public class RoboDisplayManager implements IDisplayManager {
  @Override
  public DisplayInfo getDisplayInfo(int i) throws RemoteException {
    return null;
  }

  @Override
  public int[] getDisplayIds() throws RemoteException {
    return new int[0];
  }

  @Override
  public void registerCallback(IDisplayManagerCallback iDisplayManagerCallback) throws RemoteException {

  }

  @Override
  public void startWifiDisplayScan() throws RemoteException {

  }

  @Override
  public void stopWifiDisplayScan() throws RemoteException {

  }

  @Override
  public void connectWifiDisplay(String s) throws RemoteException {

  }

  @Override
  public void disconnectWifiDisplay() throws RemoteException {

  }

  @Override
  public void renameWifiDisplay(String s, String s1) throws RemoteException {

  }

  @Override
  public void forgetWifiDisplay(String s) throws RemoteException {

  }

  @Override
  public void pauseWifiDisplay() throws RemoteException {

  }

  @Override
  public void resumeWifiDisplay() throws RemoteException {

  }

  @Override
  public WifiDisplayStatus getWifiDisplayStatus() throws RemoteException {
    return null;
  }

  @Override
  public int createVirtualDisplay(IVirtualDisplayCallback iVirtualDisplayCallback, IMediaProjection iMediaProjection, String s, String s1, int i, int i1, int i2, Surface surface, int i3) throws RemoteException {
    return 0;
  }

  @Override
  public void resizeVirtualDisplay(IVirtualDisplayCallback iVirtualDisplayCallback, int i, int i1, int i2) throws RemoteException {

  }

  @Override
  public void setVirtualDisplaySurface(IVirtualDisplayCallback iVirtualDisplayCallback, Surface surface) throws RemoteException {

  }

  @Override
  public void releaseVirtualDisplay(IVirtualDisplayCallback iVirtualDisplayCallback) throws RemoteException {

  }

  @Override
  public IBinder asBinder() {
    return null;
  }
}
