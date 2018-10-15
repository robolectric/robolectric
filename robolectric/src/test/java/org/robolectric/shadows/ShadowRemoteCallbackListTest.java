package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteCallbackList;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
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