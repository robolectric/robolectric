package org.robolectric.shadows;

import android.os.Binder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ShadowBinderBridge;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(android.os.Binder.class)
public class ShadowBinder {
  @RealObject
  Binder realObject;

  @Implementation
  public boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
    return new ShadowBinderBridge(realObject).onTransact(code, data, reply, flags);
  }
}
