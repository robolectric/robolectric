package org.robolectric.shadows;

import android.os.Binder;
import android.os.Parcel;
import android.os.RemoteException;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;

@Implements(Binder.class)
public class ShadowBinder {
  @RealObject
  Binder realObject;

  private static Integer callingUid;
  private static Integer callingPid;

  @Implementation
  protected boolean transact(int code, Parcel data, Parcel reply, int flags)
      throws RemoteException {
   if (data != null) {
     data.setDataPosition(0);
   }

   boolean result;
   try {
     result = new ShadowBinderBridge(realObject).onTransact(code, data, reply, flags);
   } catch (Exception e) {
     result = true;
     if (reply != null) {
       reply.writeException(e);
     }
   }

   if (reply != null) {
     reply.setDataPosition(0);
   }
   return result;
  }

  @Implementation
  protected static final int getCallingPid() {
    if (callingPid != null) {
      return callingPid;
    }
    return android.os.Process.myPid();
  }

  @Implementation
  protected static final int getCallingUid() {
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

  @Resetter
  public static void reset() {
    ShadowBinder.callingPid = null;
    ShadowBinder.callingUid = null;
  }
}
