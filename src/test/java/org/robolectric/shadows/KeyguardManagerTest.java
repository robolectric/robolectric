package org.robolectric.shadows;

import android.app.Activity;
import android.app.KeyguardManager;
import org.junit.Test;
import org.junit.runner.RunWith;
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
    Activity activity = new Activity();
    KeyguardManager mgr = ( KeyguardManager ) activity.getSystemService( KEYGUARD_SERVICE );
    assertThat(mgr.inKeyguardRestrictedInputMode()).isFalse();
    ShadowKeyguardManager shadowMgr = shadowOf(mgr);
    shadowMgr.setinRestrictedInputMode( true );
    assertThat(mgr.inKeyguardRestrictedInputMode()).isTrue();
  }

  @Test
  public void testShouldBeAbleToDisableTheKeyguardLock() throws Exception {
    Activity activity = new Activity();
    KeyguardManager mgr = ( KeyguardManager ) activity.getSystemService( KEYGUARD_SERVICE );
    KeyguardManager.KeyguardLock lock = mgr.newKeyguardLock(KEYGUARD_SERVICE);
    assertTrue(shadowOf(lock).isEnabled());

    lock.disableKeyguard();
    assertFalse(shadowOf(lock).isEnabled());

    lock.reenableKeyguard();
    assertTrue(shadowOf(lock).isEnabled());
  }
}
