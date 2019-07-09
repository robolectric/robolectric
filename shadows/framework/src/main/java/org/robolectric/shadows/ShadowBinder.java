package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;

import android.os.Binder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.UserHandle;
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
  private static UserHandle callingUserHandle;

  @Implementation
  protected boolean transact(int code, Parcel data, Parcel reply, int flags)
      throws RemoteException {
   if (data != null) {
     data.setDataPosition(0);
   }

   boolean result;
   try {
     result = new ShadowBinderBridge(realObject).onTransact(code, data, reply, flags);
   } catch (RemoteException e) {
     throw e;
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

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected static final UserHandle getCallingUserHandle() {
    if (callingUserHandle != null) {
      return callingUserHandle;
    }
    return android.os.Process.myUserHandle();
  }

  public static void setCallingPid(int pid) {
    ShadowBinder.callingPid = pid;
  }

  public static void setCallingUid(int uid) {
    ShadowBinder.callingUid = uid;
  }

  /**
   * Configures {@link android.os.Binder#getCallingUserHandle} to return the specified {@link
   * UserHandle} to subsequent callers on *any* thread, for testing purposes.
   */
  public static void setCallingUserHandle(UserHandle userHandle) {
    ShadowBinder.callingUserHandle = userHandle;
  }

  @Resetter
  public static void reset() {
    ShadowBinder.callingPid = null;
    ShadowBinder.callingUid = null;
    ShadowBinder.callingUserHandle = null;
  }
}
