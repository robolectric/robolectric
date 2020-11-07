package org.robolectric.integrationtests.axt;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Process;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Integration tests for {@link androidx.test.rule.GrantPermissionRule} that verify it behaves
 * consistently on device and Robolectric.
 */
@RunWith(AndroidJUnit4.class)
public class GrantPermissionRuleTest {

  @Rule
  public final GrantPermissionRule rule =
      GrantPermissionRule.grant(android.Manifest.permission.READ_CONTACTS);

  @Test
  public void some_test_with_permissions() {
    Context context = getApplicationContext();
    assertThat(context.checkPermission(permission.READ_CONTACTS, Process.myPid(), Process.myUid()))
        .isEqualTo(PackageManager.PERMISSION_GRANTED);
    assertThat(context.checkPermission(permission.WRITE_CONTACTS, Process.myPid(), Process.myUid()))
        .isEqualTo(PackageManager.PERMISSION_DENIED);
  }
}
