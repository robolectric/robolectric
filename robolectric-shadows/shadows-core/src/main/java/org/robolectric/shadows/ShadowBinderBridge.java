package org.robolectric.shadows;

import android.os.Binder;
import android.os.Parcel;
import android.os.RemoteException;
import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

import static org.robolectric.util.ReflectionHelpers.ClassParameter.*;

@DoNotInstrument
public class ShadowBinderBridge {
  private Binder realBinder;

  public ShadowBinderBridge(Binder realBinder) {
    this.realBinder = realBinder;
  }

  public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
    return ReflectionHelpers.callInstanceMethodReflectively(realBinder, "onTransact",
        from(code),
        from(Parcel.class, data),
        from(Parcel.class, reply),
        from(flags));
  }
}
