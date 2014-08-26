package org.robolectric.shadows;

import android.os.Binder;
import android.os.Parcel;
import android.os.RemoteException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static junit.framework.Assert.assertTrue;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowBinderTest {
  @Test
  public void transactCallsOnTransact() throws Exception {
    TestBinder testBinder = new TestBinder();
    Parcel data = Parcel.obtain();
    Parcel reply = Parcel.obtain();
    assertTrue(testBinder.transact(2, data, reply, 3));
    assertThat(testBinder.code).isEqualTo(2);
    assertThat(testBinder.data).isSameAs(data);
    assertThat(testBinder.reply).isSameAs(reply);
    assertThat(testBinder.flags).isEqualTo(3);
  }

  static class TestBinder extends Binder {
    int code;
    Parcel data;
    Parcel reply;
    int flags;

    @Override
    protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
      this.code = code;
      this.data = data;
      this.reply = reply;
      this.flags = flags;
      return true;
    }
  }

  @Test
  public void testSetCallingUid() {
    ShadowBinder.setCallingUid(37);
    assertThat(Binder.getCallingUid()).isEqualTo(37);
  }

  @Test
  public void testSetCallingPid() {
    ShadowBinder.setCallingPid(25);
    assertThat(Binder.getCallingPid()).isEqualTo(25);
  }

  @Test
  public void testGetCallingUidShouldUseProcessUidByDefault() {
    assertThat(Binder.getCallingUid()).isEqualTo(android.os.Process.myUid());
  }

  @Test
  public void testGetCallingPidShouldUseProcessPidByDefault() {
    assertThat(Binder.getCallingPid()).isEqualTo(android.os.Process.myPid());
  }

  @Test
  public void testResetUpdatesCallingUidAndPid() {
    ShadowBinder.setCallingPid(48);
    ShadowBinder.setCallingUid(49);
    ShadowBinder.reset();
    assertThat(Binder.getCallingPid()).isEqualTo(android.os.Process.myPid());
    assertThat(Binder.getCallingUid()).isEqualTo(android.os.Process.myUid());
  }
}
