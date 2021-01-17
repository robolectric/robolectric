package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteCallbackList;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowRemoteCallbackListTest {

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
  @Config(minSdk = 17)
  public void getRegisteredCallbackCount_callbackRegistered_reflectsInReturnValue() {
    fooRemoteCallbackList.register(new Foo());

    assertThat(fooRemoteCallbackList.getRegisteredCallbackCount()).isEqualTo(1);
  }

  private static class Foo implements IInterface {

    @Override
    public IBinder asBinder() {
      return new Binder();
    }
  }
}