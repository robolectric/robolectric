package org.robolectric.shadows;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteCallbackList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class RemoteCallbackListTest {
  @Test
  public void testBasicWiring() throws Exception {
    RemoteCallbackList<Foo> fooRemoteCallbackList = new RemoteCallbackList<Foo>();
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