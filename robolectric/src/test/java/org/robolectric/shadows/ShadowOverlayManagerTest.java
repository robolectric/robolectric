package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.content.om.OverlayInfo;
import android.content.om.OverlayManager;
import android.os.UserHandle;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = UPSIDE_DOWN_CAKE)
public final class ShadowOverlayManagerTest {

  private static final OverlayInfo IMMUTABLE_ENABLED_OVERLAY =
      new OverlayInfo(
          /* packageName= */ "package.not.mutable",
          /* targetPackageName= */ "target.package",
          /* targetOverlayableName= */ "overlay.name",
          /* category= */ "category",
          /* baseCodePath= */ "code.path",
          /* state= */ OverlayInfo.STATE_ENABLED,
          /* userId= */ UserHandle.USER_SYSTEM,
          /* priority= */ 0,
          /* isMutable= */ false);
  private static final OverlayInfo MUTABLE_ENABLED_OVERLAY =
      new OverlayInfo(
          /* packageName= */ "package.found.enabled",
          /* targetPackageName= */ "target.package",
          /* targetOverlayableName= */ "overlay.name",
          /* category= */ "category",
          /* baseCodePath= */ "code.path",
          /* state= */ OverlayInfo.STATE_ENABLED,
          /* userId= */ UserHandle.USER_SYSTEM,
          /* priority= */ 0,
          /* isMutable= */ true);
  private static final OverlayInfo MUTABLE_DISABLED_OVERLAY =
      new OverlayInfo(
          /* packageName= */ "package.found.disabled",
          /* targetPackageName= */ "target.package",
          /* targetOverlayableName= */ "overlay.name",
          /* category= */ "category",
          /* baseCodePath= */ "code.path",
          /* state= */ OverlayInfo.STATE_DISABLED,
          /* userId= */ UserHandle.USER_SYSTEM,
          /* priority= */ 0,
          /* isMutable= */ true);

  @Before
  public void setUp() {
    ShadowApplication shadowApplication = Shadow.extract(getApplicationContext());
    shadowApplication.grantPermissions(android.Manifest.permission.CHANGE_OVERLAY_PACKAGES);
  }

  @Test
  public void removeOverlayInfo_noExceptionIfDoesNotExist() {
    OverlayManager overlayManager = getApplicationContext().getSystemService(OverlayManager.class);

    // Test that no exceptions are thrown.
    getShadowOverlayManager(overlayManager).removeOverlayInfo("package.does.not.exist");
  }

  @Test
  public void getOverlayInfoNotFound_returnsNull() {
    OverlayManager overlayManager = getApplicationContext().getSystemService(OverlayManager.class);

    assertThat(overlayManager.getOverlayInfo("package.does.not.exist", UserHandle.SYSTEM)).isNull();
  }

  @Test
  public void getOverlayInfoRemoved_returnsNull() {
    OverlayManager overlayManager = getApplicationContext().getSystemService(OverlayManager.class);

    ShadowOverlayManager shadowOverlayManager = getShadowOverlayManager(overlayManager);
    shadowOverlayManager.addOverlayInfo(MUTABLE_ENABLED_OVERLAY);
    shadowOverlayManager.removeOverlayInfo(MUTABLE_ENABLED_OVERLAY.packageName);

    assertThat(
            overlayManager.getOverlayInfo(MUTABLE_ENABLED_OVERLAY.packageName, UserHandle.SYSTEM))
        .isNull();
  }

  @Test
  public void getOverlayInfoFound_returnsOverlayInfo() {
    OverlayManager overlayManager = getApplicationContext().getSystemService(OverlayManager.class);

    getShadowOverlayManager(overlayManager).addOverlayInfo(MUTABLE_ENABLED_OVERLAY);

    assertThat(
            overlayManager.getOverlayInfo(MUTABLE_ENABLED_OVERLAY.packageName, UserHandle.SYSTEM))
        .isEqualTo(MUTABLE_ENABLED_OVERLAY);
  }

  @Test
  public void setEnabledNoPermissionsDoesNotExist_throwsException() {
    ShadowApplication shadowApplication = Shadow.extract(getApplicationContext());
    shadowApplication.denyPermissions(android.Manifest.permission.CHANGE_OVERLAY_PACKAGES);

    OverlayManager overlayManager = getApplicationContext().getSystemService(OverlayManager.class);

    assertThrows(
        SecurityException.class,
        () -> overlayManager.setEnabled("package.does.not.exist", false, UserHandle.SYSTEM));
  }

  @Test
  public void setEnabledNotFound_throwsException() {
    OverlayManager overlayManager = getApplicationContext().getSystemService(OverlayManager.class);

    assertThrows(
        IllegalStateException.class,
        () -> overlayManager.setEnabled("package.does.not.exist", false, UserHandle.SYSTEM));
  }

  @Test
  public void setEnabledImmutable_throwsException() {
    OverlayManager overlayManager = getApplicationContext().getSystemService(OverlayManager.class);
    getShadowOverlayManager(overlayManager).addOverlayInfo(IMMUTABLE_ENABLED_OVERLAY);

    assertThrows(
        IllegalStateException.class,
        () ->
            overlayManager.setEnabled(
                IMMUTABLE_ENABLED_OVERLAY.packageName, false, UserHandle.SYSTEM));
  }

  @Test
  public void setEnabledNoPermissionsExists_throwsException() {
    ShadowApplication shadowApplication = Shadow.extract(getApplicationContext());
    shadowApplication.denyPermissions(android.Manifest.permission.CHANGE_OVERLAY_PACKAGES);
    OverlayManager overlayManager = getApplicationContext().getSystemService(OverlayManager.class);
    getShadowOverlayManager(overlayManager).addOverlayInfo(MUTABLE_ENABLED_OVERLAY);

    assertThrows(
        SecurityException.class,
        () ->
            overlayManager.setEnabled(
                MUTABLE_ENABLED_OVERLAY.packageName, false, UserHandle.SYSTEM));
  }

  @Test
  public void setEnabled_enableEnabled_leavesEnabled() {
    OverlayManager overlayManager = getApplicationContext().getSystemService(OverlayManager.class);
    getShadowOverlayManager(overlayManager).addOverlayInfo(MUTABLE_ENABLED_OVERLAY);

    overlayManager.setEnabled(MUTABLE_ENABLED_OVERLAY.packageName, true, UserHandle.SYSTEM);

    assertThat(
            overlayManager
                .getOverlayInfo(MUTABLE_ENABLED_OVERLAY.packageName, UserHandle.SYSTEM)
                .isEnabled())
        .isTrue();
  }

  @Test
  public void setEnabled_disableDisabled_leavesDisabled() {
    OverlayManager overlayManager = getApplicationContext().getSystemService(OverlayManager.class);
    getShadowOverlayManager(overlayManager).addOverlayInfo(MUTABLE_DISABLED_OVERLAY);

    overlayManager.setEnabled(MUTABLE_DISABLED_OVERLAY.packageName, false, UserHandle.SYSTEM);

    assertThat(
            overlayManager
                .getOverlayInfo(MUTABLE_DISABLED_OVERLAY.packageName, UserHandle.SYSTEM)
                .isEnabled())
        .isFalse();
  }

  @Test
  public void setEnabled_enableDisabled_enables() {
    OverlayManager overlayManager = getApplicationContext().getSystemService(OverlayManager.class);
    getShadowOverlayManager(overlayManager).addOverlayInfo(MUTABLE_DISABLED_OVERLAY);

    overlayManager.setEnabled(MUTABLE_DISABLED_OVERLAY.packageName, true, UserHandle.SYSTEM);

    assertThat(
            overlayManager
                .getOverlayInfo(MUTABLE_DISABLED_OVERLAY.packageName, UserHandle.SYSTEM)
                .isEnabled())
        .isTrue();
  }

  @Test
  public void setEnabled_disableEnabled_disables() {
    OverlayManager overlayManager = getApplicationContext().getSystemService(OverlayManager.class);
    getShadowOverlayManager(overlayManager).addOverlayInfo(MUTABLE_ENABLED_OVERLAY);

    overlayManager.setEnabled(MUTABLE_ENABLED_OVERLAY.packageName, false, UserHandle.SYSTEM);

    assertThat(
            overlayManager
                .getOverlayInfo(MUTABLE_ENABLED_OVERLAY.packageName, UserHandle.SYSTEM)
                .isEnabled())
        .isFalse();
  }

  private ShadowOverlayManager getShadowOverlayManager(OverlayManager overlayManager) {
    return Shadow.extract(overlayManager);
  }
}
