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

  private static Integer callingUid;
  private static Integer callingPid;

  @Implementation
  public boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
    return new ShadowBinderBridge(realObject).onTransact(code, data, reply, flags);
  }

  @Implementation
  public static final int getCallingPid() {
    if (callingPid != null) {
      return callingPid;
    }
    return android.os.Process.myPid();
  }

  @Implementation
  public static final int getCallingUid() {
    if (callingUid != null) {
      return callingUid;
    }
    return android.os.Process.myUid();
  }

  public static void setCallingPid(int pid) {
    ShadowBinder.callingPid = pid;
  }

  public static void setCallingUid(int uid) {
    ShadowBinder.callingUid = uid;
  }

  public static void reset() {
    ShadowBinder.callingPid = null;
    ShadowBinder.callingUid = null;
  }
}
