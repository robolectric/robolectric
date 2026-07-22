package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static com.google.common.truth.Truth.assertThat;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteCallbackList;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/**
 * Unit test which verifies that RemoteCallbackList works as expected in robolectric.
 * ShadowRemoteCallback list used to exist, but this test proves that everything works without the
 * shadow.
 */
@RunWith(AndroidJUnit4.class)
public class RemoteCallbackListTest {

  private RemoteCallbackList<Foo> fooRemoteCallbackList;

  @Before
  public void setup() {
    fooRemoteCallbackList = new RemoteCallbackList<>();
  }

  @Test
  public void testBasicWiring() {
    Foo callback = new Foo();
    fooRemoteCallbackList.register(callback);

    fooRemoteCallbackList.beginBroadcast();

    assertThat(fooRemoteCallbackList.getBroadcastItem(0)).isSameInstanceAs(callback);
  }

  @Test
  public void getRegisteredCallbackCount_callbackRegistered_reflectsInReturnValue() {
    fooRemoteCallbackList.register(new Foo());

    assertThat(fooRemoteCallbackList.getRegisteredCallbackCount()).isEqualTo(1);
  }

  @Test
  @Config(minSdk = BAKLAVA)
  public void broadcast_registeredCallbacks_invokesConsumer() {
    Foo callback1 = new Foo();
    Foo callback2 = new Foo();
    fooRemoteCallbackList.register(callback1);
    fooRemoteCallbackList.register(callback2);

    List<Foo> calledCallbacks = new ArrayList<>();
    fooRemoteCallbackList.broadcast((Foo cb) -> calledCallbacks.add(cb));

    assertThat(calledCallbacks).containsExactly(callback1, callback2);
  }

  @Test
  @Config(minSdk = BAKLAVA)
  public void broadcast_emptyList_doesNotInvokeConsumer() {
    List<Foo> calledCallbacks = new ArrayList<>();
    fooRemoteCallbackList.broadcast((Foo cb) -> calledCallbacks.add(cb));

    assertThat(calledCallbacks).isEmpty();
  }

  private static class Foo implements IInterface {

    @Override
    public IBinder asBinder() {
      return new Binder();
    }
  }
}
