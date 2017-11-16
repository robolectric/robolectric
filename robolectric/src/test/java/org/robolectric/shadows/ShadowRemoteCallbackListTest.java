package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteCallbackList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowRemoteCallbackListTest {
  @Test
  public void testBasicWiring() throws Exception {
    RemoteCallbackList<Foo> fooRemoteCallbackList = new RemoteCallbackList<>();
    Foo callback = new Foo();
    fooRemoteCallbackList.register(callback);

    fooRemoteCallbackList.beginBroadcast();

    assertThat(fooRemoteCallbackList.getBroadcastItem(0)).isSameAs(callback);
  }

  public static class Foo implements IInterface {

    @Override
    public IBinder asBinder() {
      return new Binder();
    }
  }
}