package android.os;

import org.robolectric.internal.DoNotInstrument;

@DoNotInstrument
public class ShadowBinderBridge {
  private Binder realBinder;

  public ShadowBinderBridge(Binder realBinder) {
    this.realBinder = realBinder;
  }

  public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
    return realBinder.onTransact(code, data, reply, flags);
  }
}
