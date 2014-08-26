package org.robolectric.shadows;

import android.app.KeyguardManager;
import android.content.Context;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static android.content.Context.KEYGUARD_SERVICE;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class KeyguardManagerTest {

  @Test
  public void testIsInRestrcitedInputMode() {
    Context context = Robolectric.application;
    KeyguardManager mgr = ( KeyguardManager ) context.getSystemService( KEYGUARD_SERVICE );
    assertThat(mgr.inKeyguardRestrictedInputMode()).isFalse();
    ShadowKeyguardManager shadowMgr = shadowOf(mgr);
    shadowMgr.setinRestrictedInputMode( true );
    assertThat(mgr.inKeyguardRestrictedInputMode()).isTrue();
  }

  @Test
  public void testShouldBeAbleToDisableTheKeyguardLock() throws Exception {
    Context context = Robolectric.application;
    KeyguardManager mgr = ( KeyguardManager ) context.getSystemService( KEYGUARD_SERVICE );
    KeyguardManager.KeyguardLock lock = mgr.newKeyguardLock(KEYGUARD_SERVICE);
    assertTrue(shadowOf(lock).isEnabled());

    lock.disableKeyguard();
    assertFalse(shadowOf(lock).isEnabled());

    lock.reenableKeyguard();
    assertTrue(shadowOf(lock).isEnabled());
  }
}
