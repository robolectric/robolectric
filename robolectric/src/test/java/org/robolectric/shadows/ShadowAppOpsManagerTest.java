package org.robolectric.shadows;

import static android.app.AppOpsManager.MODE_ALLOWED;
import static android.app.AppOpsManager.OP_GPS;
import static android.app.AppOpsManager.OP_SEND_SMS;
import static android.os.Build.VERSION_CODES.KITKAT;
import static org.assertj.core.api.Assertions.assertThat;

import android.app.AppOpsManager;
import android.app.AppOpsManager.OpEntry;
import android.app.AppOpsManager.PackageOps;
import android.content.Context;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/**
 * Unit tests for {@link ShadowAppOpsManager}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = KITKAT)
public class ShadowAppOpsManagerTest {

  private static final String PACKAGE_NAME1 = "com.company1.pkg1";
  private static final String PACKAGE_NAME2 = "com.company2.pkg2";
  private static final int UID_1 = 10000;
  private static final int UID_2 = 10001;

  // Can be used as an argument of getOpsForPackage().
  private static final int[] NO_OP_FILTER = null;

  private AppOpsManager appOps;

  @Before
  public void setUp() {
    appOps = (AppOpsManager) RuntimeEnvironment.application.getSystemService(
        Context.APP_OPS_SERVICE);
  }

  @Test
  public void noteOp() {
    assertThat(appOps.noteOp(OP_GPS, UID_1, PACKAGE_NAME1)).isEqualTo(MODE_ALLOWED);
    // Use same op more than once
    assertThat(appOps.noteOp(OP_GPS, UID_1, PACKAGE_NAME1)).isEqualTo(MODE_ALLOWED);

    assertThat(appOps.noteOp(OP_SEND_SMS, UID_1, PACKAGE_NAME1)).isEqualTo(MODE_ALLOWED);
  }

  @Test
  public void getOpsForPackage_noOps() {
    List<PackageOps> results = appOps.getOpsForPackage(UID_1, PACKAGE_NAME1, NO_OP_FILTER);
    assertOps(results);
  }

  @Test
  public void getOpsForPackage_hasOps() {
    appOps.noteOp(OP_GPS, UID_1, PACKAGE_NAME1);
    appOps.noteOp(OP_SEND_SMS, UID_1, PACKAGE_NAME1);

    // PACKAGE_NAME2 has ops.
    List<PackageOps> results = appOps.getOpsForPackage(UID_1, PACKAGE_NAME1, NO_OP_FILTER);
    assertOps(results, OP_GPS, OP_SEND_SMS);

    // PACKAGE_NAME2 has no ops.
    results = appOps.getOpsForPackage(UID_2, PACKAGE_NAME2, NO_OP_FILTER);
    assertOps(results);
  }

  @Test
  public void getOpsForPackage_withOpFilter() {
    List<PackageOps> results = appOps.getOpsForPackage(
        UID_1, PACKAGE_NAME1, new int[]{OP_GPS});
    assertOps(results);

    appOps.noteOp(OP_SEND_SMS, UID_1, PACKAGE_NAME1);
    results = appOps.getOpsForPackage(UID_1, PACKAGE_NAME1, new int[]{OP_GPS});
    assertOps(results);

    appOps.noteOp(OP_GPS, UID_1, PACKAGE_NAME1);
    results = appOps.getOpsForPackage(UID_1, PACKAGE_NAME1, new int[]{OP_GPS});
    assertOps(results, OP_GPS);
  }

  /**
   * Assert that the results contain the expected op codes.
   */
  private void assertOps(List<PackageOps> pkgOps, Integer... expectedOps) {
    Set<Integer> actualOps = new HashSet<>();
    for (PackageOps pkgOp : pkgOps) {
      for (OpEntry entry : pkgOp.getOps()) {
        actualOps.add(entry.getOp());
      }
    }

    assertThat(actualOps).hasSameElementsAs(Arrays.asList(expectedOps));
  }
}
