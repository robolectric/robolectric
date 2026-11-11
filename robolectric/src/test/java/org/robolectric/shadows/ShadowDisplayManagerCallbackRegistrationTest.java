package org.robolectric.shadows;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.hardware.display.DisplayManagerGlobal;
import android.hardware.display.IDisplayManager;
import android.hardware.display.IDisplayManagerCallback;
import android.os.RemoteException;
import android.view.DisplayInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

/** A client can change its event mask without registering for a second delivery. */
@RunWith(RobolectricTestRunner.class)
@Config(
    sdk = {36, 37},
    manifest = Config.NONE)
public class ShadowDisplayManagerCallbackRegistrationTest {
  @Test
  public void repeatedRegistrationDeliversOneEventPerClient() throws RemoteException {
    DisplayManagerGlobal global = DisplayManagerGlobal.getInstance();
    IDisplayManager service = ReflectionHelpers.getField(global, "mDm");
    IDisplayManagerCallback first = mock(IDisplayManagerCallback.class);
    IDisplayManagerCallback second = mock(IDisplayManagerCallback.class);
    service.registerCallback(first);
    service.registerCallback(second);
    service.registerCallbackWithEventMask(first, 7L);

    ShadowDisplayManagerGlobal shadow = Shadow.extract(global);
    int id = shadow.addDisplay(new DisplayInfo());

    verify(first).onDisplayEvent(eq(id), anyInt());
    verify(second).onDisplayEvent(eq(id), anyInt());
  }
}
