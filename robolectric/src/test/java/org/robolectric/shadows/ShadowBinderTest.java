package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.os.Binder;
import android.os.Parcel;
import android.os.RemoteException;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowBinderTest {
  @Test
  public void transactCallsOnTransact() throws Exception {
    TestBinder testBinder = new TestBinder();
    Parcel data = Parcel.obtain();
    Parcel reply = Parcel.obtain();
    data.writeString("Hello Robolectric");
    assertTrue(testBinder.transact(2, data, reply, 3));
    assertThat(testBinder.code).isEqualTo(2);
    assertThat(testBinder.data).isSameAs(data);
    assertThat(testBinder.reply).isSameAs(reply);
    assertThat(testBinder.flags).isEqualTo(3);
    reply.readException();
    assertThat(reply.readString()).isEqualTo("Hello Robolectric");
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
      String string = data.readString();
      reply.writeNoException();
      reply.writeString(string);
      return true;
    }
  }

  @Test
  public void thrownExceptionIsParceled() throws Exception {
    TestThrowingBinder testThrowingBinder = new TestThrowingBinder();
    Parcel data = Parcel.obtain();
    Parcel reply = Parcel.obtain();
    testThrowingBinder.transact(2, data, reply, 3);
    try {
      reply.readException();
      fail();  // Expect thrown
    } catch (SecurityException e) {
      assertThat(e.getMessage()).isEqualTo("Halt! Who goes there?");
    }
  }

  static class TestThrowingBinder extends Binder {

    @Override
    protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
      throw new SecurityException("Halt! Who goes there?");
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
