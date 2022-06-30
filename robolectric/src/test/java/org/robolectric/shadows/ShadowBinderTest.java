package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.os.Binder;
import android.os.Parcel;
import android.os.UserHandle;
import android.os.UserManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowBinderTest {

  private UserManager userManager;

  @Before
  public void setUp() {
    Context context = ApplicationProvider.getApplicationContext();
    userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
  }

  @Test
  public void transactCallsOnTransact() throws Exception {
    TestBinder testBinder = new TestBinder();
    Parcel data = Parcel.obtain();
    Parcel reply = Parcel.obtain();
    data.writeString("Hello Robolectric");
    assertTrue(testBinder.transact(2, data, reply, 3));
    assertThat(testBinder.code).isEqualTo(2);
    assertThat(testBinder.data).isSameInstanceAs(data);
    assertThat(testBinder.reply).isSameInstanceAs(reply);
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
    protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
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
      fail(); // Expect thrown
    } catch (SecurityException e) {
      assertThat(e.getMessage()).isEqualTo("Halt! Who goes there?");
    }
  }

  static class TestThrowingBinder extends Binder {

    @Override
    protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
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
  @Config(minSdk = JELLY_BEAN_MR1)
  public void testSetCallingUserHandle() {
    UserHandle newUser = shadowOf(userManager).addUser(10, "secondary_user", 0);
    ShadowBinder.setCallingUserHandle(newUser);
    assertThat(Binder.getCallingUserHandle()).isEqualTo(newUser);
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
  @Config(minSdk = Q)
  public void testGetCallingUidOrThrowWithValueSet() {
    ShadowBinder.setCallingUid(123);
    assertThat(Binder.getCallingUidOrThrow()).isEqualTo(123);
  }

  @Test
  @Config(minSdk = Q)
  public void testGetCallingUidOrThrowWithValueNotSet() {
    ShadowBinder.reset();
    IllegalStateException ex =
        assertThrows(IllegalStateException.class, () -> Binder.getCallingUidOrThrow());

    // Typo in "transaction" is intentional to match platform
    assertThat(ex).hasMessageThat().isEqualTo("Thread is not in a binder transcation");
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void testGetCallingUserHandleShouldUseThatOfProcessByDefault() {
    assertThat(Binder.getCallingUserHandle()).isEqualTo(android.os.Process.myUserHandle());
  }

  @Test
  public void testResetUpdatesCallingUidAndPid() {
    ShadowBinder.setCallingPid(48);
    ShadowBinder.setCallingUid(49);
    ShadowBinder.reset();
    assertThat(Binder.getCallingPid()).isEqualTo(android.os.Process.myPid());
    assertThat(Binder.getCallingUid()).isEqualTo(android.os.Process.myUid());
  }

  @Test
  @Config(minSdk = Q)
  public void testResetUpdatesGetCallingUidOrThrow() {
    ShadowBinder.setCallingUid(123);
    ShadowBinder.reset();

    assertThrows(IllegalStateException.class, () -> Binder.getCallingUidOrThrow());
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void testResetUpdatesCallingUserHandle() {
    UserHandle newUser = shadowOf(userManager).addUser(10, "secondary_user", 0);
    ShadowBinder.setCallingUserHandle(newUser);
    ShadowBinder.reset();
    assertThat(Binder.getCallingUserHandle()).isEqualTo(android.os.Process.myUserHandle());
  }
}
