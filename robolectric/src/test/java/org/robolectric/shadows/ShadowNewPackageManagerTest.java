package org.robolectric.shadows;

import android.content.pm.PackageParser.Package;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

/**
 * Test shadow package mgr with new framework manifest parser
 */
@RunWith(RobolectricTestRunner.class)
public final class ShadowNewPackageManagerTest extends ShadowPackageManagerTest {

  @Before
  public void loadManifestWithNewParser() {
    Package parsedPackage = ShadowPackageParser
        .callParsePackage(RuntimeEnvironment.getAppManifest().getAndroidManifestFile());
    shadowPackageManager.addPackage(parsedPackage);
  }
}
