package org.robolectric.shadows;

import static android.app.AppOpsManager.MODE_ALLOWED;
import static android.app.AppOpsManager.MODE_DEFAULT;
import static android.app.AppOpsManager.MODE_ERRORED;
import static android.app.AppOpsManager.MODE_FOREGROUND;
import static android.app.AppOpsManager.MODE_IGNORED;
import static android.app.AppOpsManager.OPSTR_BODY_SENSORS;
import static android.app.AppOpsManager.OPSTR_COARSE_LOCATION;
import static android.app.AppOpsManager.OPSTR_FINE_LOCATION;
import static android.app.AppOpsManager.OPSTR_GPS;
import static android.app.AppOpsManager.OPSTR_READ_PHONE_STATE;
import static android.app.AppOpsManager.OPSTR_RECORD_AUDIO;
import static android.app.AppOpsManager.OP_FINE_LOCATION;
import static android.app.AppOpsManager.OP_GPS;
import static android.app.AppOpsManager.OP_SEND_SMS;
import static android.app.AppOpsManager.OP_VIBRATE;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.util.concurrent.MoreExecutors.directExecutor;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowAppOpsManager.DURATION;
import static org.robolectric.shadows.ShadowAppOpsManager.OP_TIME;

import android.app.AppOpsManager;
import android.app.AppOpsManager.OnOpChangedListener;
import android.app.AppOpsManager.OpEntry;
import android.app.AppOpsManager.PackageOps;
import android.app.SyncNotedAppOp;
import android.content.Context;
import android.media.AudioAttributes;
import android.os.Binder;
import android.os.Build.VERSION_CODES;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAppOpsManager.ModeAndException;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Unit tests for {@link ShadowAppOpsManager}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = KITKAT)
public class ShadowAppOpsManagerTest {

  private static final String PACKAGE_NAME1 = "com.company1.pkg1";
  private static final String PACKAGE_NAME2 = "com.company2.pkg2";
  private static final int UID_1 = 10000;
  private static final int UID_2 = 10001;

  // Can be used as an argument of getOpsForPackage().
  private static final int[] NO_OP_FILTER_BY_NUMBER = null;
  private static final String[] NO_OP_FILTER_BY_NAME = null;

  private AppOpsManager appOps;

  @Before
  public void setUp() {
    appOps =
        (AppOpsManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.APP_OPS_SERVICE);
  }

  @Test
  @Config(minSdk = VERSION_CODES.P)
  public void checkOpNoThrow_noModeSet_atLeastP_shouldReturnModeAllowed() {
    assertThat(appOps.checkOpNoThrow(OPSTR_GPS, UID_1, PACKAGE_NAME1)).isEqualTo(MODE_ALLOWED);
  }

  @Test
  @Config(minSdk = VERSION_CODES.P)
  public void setMode_withModeDefault_atLeastP_checkOpNoThrow_shouldReturnModeDefault() {
    ReflectionHelpers.callInstanceMethod(
        appOps,
        "setMode",
        ClassParameter.from(int.class, OP_GPS),
        ClassParameter.from(int.class, UID_1),
        ClassParameter.from(String.class, PACKAGE_NAME1),
        ClassParameter.from(int.class, MODE_DEFAULT));
    assertThat(appOps.checkOpNoThrow(OPSTR_GPS, UID_1, PACKAGE_NAME1)).isEqualTo(MODE_DEFAULT);
  }

  @Test
  @Config(minSdk = VERSION_CODES.KITKAT)
  public void checkOpNoThrow_noModeSet_atLeastKitKat_shouldReturnModeAllowed() {
    assertThat(appOps.checkOpNoThrow(/* op= */ 2, UID_1, PACKAGE_NAME1)).isEqualTo(MODE_ALLOWED);
  }

  @Test
  @Config(minSdk = VERSION_CODES.KITKAT)
  public void setMode_withModeDefault_atLeastKitKat_checkOpNoThrow_shouldReturnModeDefault() {
    appOps.setMode(/* op= */ 2, UID_1, PACKAGE_NAME1, MODE_DEFAULT);
    assertThat(appOps.checkOpNoThrow(/* op= */ 2, UID_1, PACKAGE_NAME1)).isEqualTo(MODE_DEFAULT);
  }

  @Test
  @Config(maxSdk = VERSION_CODES.O_MR1)
  public void setMode_checkOpNoThrow_belowP() {
    assertThat(appOps.checkOpNoThrow(OP_GPS, UID_1, PACKAGE_NAME1)).isEqualTo(MODE_ALLOWED);
    appOps.setMode(OP_GPS, UID_1, PACKAGE_NAME1, MODE_ERRORED);
    assertThat(appOps.checkOpNoThrow(OP_GPS, UID_1, PACKAGE_NAME1)).isEqualTo(MODE_ERRORED);
  }

  @Test
  @Config(minSdk = VERSION_CODES.P)
  public void setMode_checkOpNoThrow_atLeastP() {
    assertThat(appOps.checkOpNoThrow(OPSTR_GPS, UID_1, PACKAGE_NAME1)).isEqualTo(MODE_ALLOWED);
    appOps.setMode(OPSTR_GPS, UID_1, PACKAGE_NAME1, MODE_ERRORED);
    assertThat(appOps.checkOpNoThrow(OPSTR_GPS, UID_1, PACKAGE_NAME1)).isEqualTo(MODE_ERRORED);
  }

  @Test
  @Config(minSdk = VERSION_CODES.O_MR1, maxSdk = VERSION_CODES.Q)
  public void noModeSet_atLeastO_noteProxyOpNoThrow_shouldReturnModeAllowed() {
    int result =
        ReflectionHelpers.callInstanceMethod(
            appOps,
            "noteProxyOpNoThrow",
            ClassParameter.from(int.class, OP_GPS),
            ClassParameter.from(String.class, PACKAGE_NAME1));
    assertThat(result).isEqualTo(MODE_ALLOWED);
  }

  @Test
  @Config(sdk = VERSION_CODES.Q)
  public void noModeSet_q_noteProxyOpNoThrow_withproxiedUid_shouldReturnModeAllowed() {
    int result = appOps.noteProxyOpNoThrow(OPSTR_GPS, PACKAGE_NAME1, Binder.getCallingUid());
    assertThat(result).isEqualTo(MODE_ALLOWED);
  }

  @Test
  @Config(minSdk = VERSION_CODES.R)
  public void noModeSet_atLeastR_noteProxyOpNoThrow_shouldReturnModeAllowed() {
    int result =
        appOps.noteProxyOpNoThrow(OPSTR_GPS, PACKAGE_NAME1, Binder.getCallingUid(), null, null);
    assertThat(result).isEqualTo(MODE_ALLOWED);
  }

  @Test
  @Config(minSdk = VERSION_CODES.O_MR1, maxSdk = VERSION_CODES.Q)
  public void setMode_withModeDefault_atLeastO_noteProxyOpNoThrow_shouldReturnModeDefault() {
    ReflectionHelpers.callInstanceMethod(
        appOps,
        "setMode",
        ClassParameter.from(int.class, OP_GPS),
        ClassParameter.from(int.class, Binder.getCallingUid()),
        ClassParameter.from(String.class, PACKAGE_NAME1),
        ClassParameter.from(int.class, MODE_DEFAULT));
    int result =
        ReflectionHelpers.callInstanceMethod(
            appOps,
            "noteProxyOpNoThrow",
            ClassParameter.from(int.class, OP_GPS),
            ClassParameter.from(String.class, PACKAGE_NAME1));
    assertThat(result).isEqualTo(MODE_DEFAULT);
  }

  @Test
  @Config(sdk = VERSION_CODES.Q)
  public void
      setMode_withModeDefault_q_noteProxyOpNoThrow_withProxiedUid_shouldReturnModeDefault() {
    appOps.setMode(OP_GPS, Binder.getCallingUid(), PACKAGE_NAME1, MODE_DEFAULT);
    assertThat(appOps.noteProxyOpNoThrow(OPSTR_GPS, PACKAGE_NAME1, Binder.getCallingUid()))
        .isEqualTo(MODE_DEFAULT);
  }

  @Test
  @Config(minSdk = VERSION_CODES.R)
  public void setMode_withModeDefault_atLeastR_noteProxyOpNoThrow_shouldReturnModeDefault() {
    appOps.setMode(OP_GPS, Binder.getCallingUid(), PACKAGE_NAME1, MODE_DEFAULT);
    assertThat(
            appOps.noteProxyOpNoThrow(OPSTR_GPS, PACKAGE_NAME1, Binder.getCallingUid(), null, null))
        .isEqualTo(MODE_DEFAULT);
  }

  @Test
  @Config(minSdk = VERSION_CODES.P, maxSdk = VERSION_CODES.Q)
  public void setMode_noteProxyOpNoThrow_atLeastO() {
    assertThat(appOps.noteProxyOpNoThrow(OPSTR_GPS, PACKAGE_NAME1)).isEqualTo(MODE_ALLOWED);
    appOps.setMode(OP_GPS, Binder.getCallingUid(), PACKAGE_NAME1, MODE_ERRORED);
    assertThat(appOps.noteProxyOpNoThrow(OPSTR_GPS, PACKAGE_NAME1)).isEqualTo(MODE_ERRORED);
  }

  @Test
  @Config(sdk = VERSION_CODES.Q)
  public void setMode_noteProxyOpNoThrow_withProxiedUid_q() {
    assertThat(appOps.noteProxyOpNoThrow(OPSTR_GPS, PACKAGE_NAME1, Binder.getCallingUid()))
        .isEqualTo(MODE_ALLOWED);
    appOps.setMode(OP_GPS, Binder.getCallingUid(), PACKAGE_NAME1, MODE_ERRORED);
    assertThat(appOps.noteProxyOpNoThrow(OPSTR_GPS, PACKAGE_NAME1, Binder.getCallingUid()))
        .isEqualTo(MODE_ERRORED);
  }

  @Test
  @Config(minSdk = VERSION_CODES.R)
  public void setMode_noteProxyOpNoThrow_atLeastR() {
    assertThat(
            appOps.noteProxyOpNoThrow(OPSTR_GPS, PACKAGE_NAME1, Binder.getCallingUid(), null, null))
        .isEqualTo(MODE_ALLOWED);
    appOps.setMode(OP_GPS, Binder.getCallingUid(), PACKAGE_NAME1, MODE_ERRORED);
    assertThat(
            appOps.noteProxyOpNoThrow(OPSTR_GPS, PACKAGE_NAME1, Binder.getCallingUid(), null, null))
        .isEqualTo(MODE_ERRORED);
  }

  @Test
  @Config(minSdk = VERSION_CODES.KITKAT)
  public void startStopWatchingMode() {
    OnOpChangedListener callback = mock(OnOpChangedListener.class);
    appOps.startWatchingMode(OPSTR_FINE_LOCATION, PACKAGE_NAME1, callback);
    appOps.setMode(OP_FINE_LOCATION, UID_1, PACKAGE_NAME1, MODE_ERRORED);
    verify(callback).onOpChanged(OPSTR_FINE_LOCATION, PACKAGE_NAME1);

    appOps.stopWatchingMode(callback);
    appOps.setMode(OP_FINE_LOCATION, UID_1, PACKAGE_NAME1, MODE_ALLOWED);
    verifyNoMoreInteractions(callback);
  }

  @Test
  @Config(minSdk = VERSION_CODES.Q)
  public void startStopWatchingModeFlags() {
    OnOpChangedListener callback = mock(OnOpChangedListener.class);
    appOps.startWatchingMode(OPSTR_FINE_LOCATION, PACKAGE_NAME1, 0, callback);
    appOps.setMode(OP_FINE_LOCATION, UID_1, PACKAGE_NAME1, MODE_ERRORED);
    verify(callback).onOpChanged(OPSTR_FINE_LOCATION, PACKAGE_NAME1);

    appOps.stopWatchingMode(callback);
    appOps.setMode(OP_FINE_LOCATION, UID_1, PACKAGE_NAME1, MODE_ALLOWED);
    verifyNoMoreInteractions(callback);
  }

  @Test
  public void noteOp() {
    assertThat(appOps.noteOp(OP_GPS, UID_1, PACKAGE_NAME1)).isEqualTo(MODE_ALLOWED);
    // Use same op more than once
    assertThat(appOps.noteOp(OP_GPS, UID_1, PACKAGE_NAME1)).isEqualTo(MODE_ALLOWED);

    assertThat(appOps.noteOp(OP_SEND_SMS, UID_1, PACKAGE_NAME1)).isEqualTo(MODE_ALLOWED);
  }

  @Test
  @Config(minSdk = VERSION_CODES.Q)
  public void getOpsForPackageStr_noOps() {
    List<PackageOps> results = appOps.getOpsForPackage(UID_1, PACKAGE_NAME1, NO_OP_FILTER_BY_NAME);
    assertOps(results);
  }

  @Test
  @Config(minSdk = VERSION_CODES.Q)
  public void getOpsForPackageStr_hasOps() {
    appOps.noteOp(OP_GPS, UID_1, PACKAGE_NAME1);
    appOps.noteOp(OP_SEND_SMS, UID_1, PACKAGE_NAME1);

    // PACKAGE_NAME1 has ops.
    List<PackageOps> results = appOps.getOpsForPackage(UID_1, PACKAGE_NAME1, NO_OP_FILTER_BY_NAME);
    assertOps(results, OP_GPS, OP_SEND_SMS);

    // PACKAGE_NAME2 has no ops.
    results = appOps.getOpsForPackage(UID_2, PACKAGE_NAME2, NO_OP_FILTER_BY_NAME);
    assertOps(results);
  }

  @Test
  @Config(minSdk = VERSION_CODES.Q)
  public void getOpsForPackageStr_withOpFilter() {
    List<PackageOps> results = appOps.getOpsForPackage(UID_1, PACKAGE_NAME1, OPSTR_GPS);
    assertOps(results);

    appOps.noteOp(OP_SEND_SMS, UID_1, PACKAGE_NAME1);
    results = appOps.getOpsForPackage(UID_1, PACKAGE_NAME1, OPSTR_GPS);
    assertOps(results);

    appOps.noteOp(OP_GPS, UID_1, PACKAGE_NAME1);
    results = appOps.getOpsForPackage(UID_1, PACKAGE_NAME1, OPSTR_GPS);
    assertOps(results, OP_GPS);
  }

  @Test
  @Config(minSdk = VERSION_CODES.Q)
  public void getOpsForPackageStr_withOpFilter_withMeaninglessString() {
    List<PackageOps> results =
        appOps.getOpsForPackage(UID_1, PACKAGE_NAME1, OPSTR_GPS, "something");
    assertOps(results);

    appOps.noteOp(OP_SEND_SMS, UID_1, PACKAGE_NAME1);
    results = appOps.getOpsForPackage(UID_1, PACKAGE_NAME1, OPSTR_GPS, "something");
    assertOps(results);

    appOps.noteOp(OP_GPS, UID_1, PACKAGE_NAME1);
    results = appOps.getOpsForPackage(UID_1, PACKAGE_NAME1, OPSTR_GPS, "something");
    assertOps(results, OP_GPS);
  }

  @Test
  @Config(minSdk = VERSION_CODES.Q)
  public void getOpsForPackageStr_ensureTime() {
    appOps.noteOp(OP_GPS, UID_1, PACKAGE_NAME1);
    List<PackageOps> results = appOps.getOpsForPackage(UID_1, PACKAGE_NAME1, NO_OP_FILTER_BY_NAME);
    assertThat(results.get(0).getOps().get(0).getTime()).isEqualTo(OP_TIME);
  }

  @Test
  @Config(minSdk = VERSION_CODES.Q) // Earlier versions return int rather than long for duration.
  public void getOpsForPackageStr_ensureDuration() {
    appOps.noteOp(OP_GPS, UID_1, PACKAGE_NAME1);
    List<PackageOps> results = appOps.getOpsForPackage(UID_1, PACKAGE_NAME1, NO_OP_FILTER_BY_NAME);
    assertThat(results.get(0).getOps().get(0).getDuration()).isEqualTo(DURATION);
  }

  @Test
  public void getOpsForPackage_noOps() {
    List<PackageOps> results =
        appOps.getOpsForPackage(UID_1, PACKAGE_NAME1, NO_OP_FILTER_BY_NUMBER);
    assertOps(results);
  }

  @Test
  public void getOpsForPackage_hasOps() {
    appOps.noteOp(OP_GPS, UID_1, PACKAGE_NAME1);
    appOps.noteOp(OP_SEND_SMS, UID_1, PACKAGE_NAME1);

    // PACKAGE_NAME1 has ops.
    List<PackageOps> results =
        appOps.getOpsForPackage(UID_1, PACKAGE_NAME1, NO_OP_FILTER_BY_NUMBER);
    assertOps(results, OP_GPS, OP_SEND_SMS);

    // PACKAGE_NAME2 has no ops.
    results = appOps.getOpsForPackage(UID_2, PACKAGE_NAME2, NO_OP_FILTER_BY_NUMBER);
    assertOps(results);
  }

  @Test
  public void getOpsForPackage_withOpFilter() {
    List<PackageOps> results = appOps.getOpsForPackage(UID_1, PACKAGE_NAME1, new int[] {OP_GPS});
    assertOps(results);

    appOps.noteOp(OP_SEND_SMS, UID_1, PACKAGE_NAME1);
    results = appOps.getOpsForPackage(UID_1, PACKAGE_NAME1, new int[] {OP_GPS});
    assertOps(results);

    appOps.noteOp(OP_GPS, UID_1, PACKAGE_NAME1);
    results = appOps.getOpsForPackage(UID_1, PACKAGE_NAME1, new int[] {OP_GPS});
    assertOps(results, OP_GPS);
  }

  @Test
  public void getOpsForPackage_hasNoThrowOps() {
    appOps.noteOpNoThrow(OP_GPS, UID_1, PACKAGE_NAME1);
    appOps.noteOpNoThrow(OP_SEND_SMS, UID_1, PACKAGE_NAME1);

    assertOps(
        appOps.getOpsForPackage(UID_1, PACKAGE_NAME1, NO_OP_FILTER_BY_NUMBER), OP_GPS, OP_SEND_SMS);

    assertOps(appOps.getOpsForPackage(UID_2, PACKAGE_NAME2, NO_OP_FILTER_BY_NUMBER));

    appOps.setMode(OP_GPS, UID_1, PACKAGE_NAME1, MODE_ERRORED);
    assertThat(appOps.noteOpNoThrow(OP_GPS, UID_1, PACKAGE_NAME1)).isEqualTo(MODE_ERRORED);
  }

  @Test
  public void getOpsForPackage_ensureTime() {
    appOps.noteOp(OP_GPS, UID_1, PACKAGE_NAME1);
    List<PackageOps> results =
        appOps.getOpsForPackage(UID_1, PACKAGE_NAME1, NO_OP_FILTER_BY_NUMBER);
    assertThat(results.get(0).getOps().get(0).getTime()).isEqualTo(OP_TIME);
  }

  @Test
  @Config(minSdk = VERSION_CODES.Q) // Earlier versions return int rather than long for duration.
  public void getOpsForPackage_ensureDuration() {
    appOps.noteOp(OP_GPS, UID_1, PACKAGE_NAME1);
    List<PackageOps> results =
        appOps.getOpsForPackage(UID_1, PACKAGE_NAME1, NO_OP_FILTER_BY_NUMBER);
    assertThat(results.get(0).getOps().get(0).getDuration()).isEqualTo(DURATION);
  }

  @Test
  @Config(minSdk = VERSION_CODES.R)
  public void startOp_opActive() {
    appOps.startOp(OPSTR_RECORD_AUDIO, UID_1, PACKAGE_NAME1, null, null);

    assertThat(appOps.isOpActive(OPSTR_RECORD_AUDIO, UID_1, PACKAGE_NAME1)).isTrue();
  }

  @Test
  @Config(minSdk = VERSION_CODES.R)
  public void startOp_finishOp_opNotActive() {
    appOps.startOp(OPSTR_RECORD_AUDIO, UID_1, PACKAGE_NAME1, null, null);
    appOps.finishOp(OPSTR_RECORD_AUDIO, UID_1, PACKAGE_NAME1, null);

    assertThat(appOps.isOpActive(OPSTR_RECORD_AUDIO, UID_1, PACKAGE_NAME1)).isFalse();
  }

  @Test
  @Config(minSdk = VERSION_CODES.R)
  public void startOp_anotherOp_opNotActive() {
    appOps.startOp(OPSTR_READ_PHONE_STATE, UID_1, PACKAGE_NAME1, null, null);

    assertThat(appOps.isOpActive(OPSTR_RECORD_AUDIO, UID_1, PACKAGE_NAME1)).isFalse();
  }

  @Test
  @Config(minSdk = VERSION_CODES.R)
  public void startOp_anotherUid_opNotActive() {
    appOps.startOp(OPSTR_RECORD_AUDIO, UID_2, PACKAGE_NAME1, null, null);

    assertThat(appOps.isOpActive(OPSTR_RECORD_AUDIO, UID_1, PACKAGE_NAME1)).isFalse();
  }

  @Test
  @Config(minSdk = VERSION_CODES.R)
  public void startOp_anotherPackage_opNotActive() {
    appOps.startOp(OPSTR_RECORD_AUDIO, UID_1, PACKAGE_NAME2, null, null);

    assertThat(appOps.isOpActive(OPSTR_RECORD_AUDIO, UID_1, PACKAGE_NAME1)).isFalse();
  }

  @Test
  @Config(minSdk = VERSION_CODES.KITKAT, maxSdk = VERSION_CODES.Q)
  public void startOpNoThrow_setModeAllowed() {
    appOps.setMode(OP_FINE_LOCATION, UID_1, PACKAGE_NAME1, MODE_ALLOWED);

    assertThat(appOps.startOpNoThrow(OP_FINE_LOCATION, UID_1, PACKAGE_NAME1))
        .isEqualTo(MODE_ALLOWED);
  }

  @Test
  @Config(minSdk = VERSION_CODES.KITKAT, maxSdk = VERSION_CODES.Q)
  public void startOpNoThrow_setModeErrored() {
    appOps.setMode(OP_FINE_LOCATION, UID_1, PACKAGE_NAME1, MODE_ERRORED);

    assertThat(appOps.startOpNoThrow(OP_FINE_LOCATION, UID_1, PACKAGE_NAME1))
        .isEqualTo(MODE_ERRORED);
  }

  @Test
  @Config(minSdk = VERSION_CODES.R)
  public void startOpNoThrow_withAttr_opActive() {
    appOps.startOpNoThrow(OPSTR_RECORD_AUDIO, UID_1, PACKAGE_NAME1, null, null);

    assertThat(appOps.isOpActive(OPSTR_RECORD_AUDIO, UID_1, PACKAGE_NAME1)).isTrue();
  }

  @Test
  @Config(minSdk = VERSION_CODES.R)
  public void startOpNoThrow_finishOp_opNotActive() {
    appOps.startOp(OPSTR_RECORD_AUDIO, UID_1, PACKAGE_NAME1, null, null);
    appOps.finishOp(OPSTR_RECORD_AUDIO, UID_1, PACKAGE_NAME1, null);

    assertThat(appOps.isOpActive(OPSTR_RECORD_AUDIO, UID_1, PACKAGE_NAME1)).isFalse();
  }

  @Test
  @Config(minSdk = VERSION_CODES.R)
  public void checkOp_ignoreModeSet_returnIgnored() {
    appOps.setMode(OPSTR_RECORD_AUDIO, UID_1, PACKAGE_NAME1, MODE_IGNORED);

    assertThat(appOps.checkOp(OPSTR_RECORD_AUDIO, UID_1, PACKAGE_NAME1)).isEqualTo(MODE_IGNORED);
  }

  @Test
  @Config(minSdk = VERSION_CODES.LOLLIPOP)
  public void setRestrictions() {
    appOps.setRestriction(
        OP_VIBRATE, AudioAttributes.USAGE_NOTIFICATION, MODE_ERRORED, new String[] {PACKAGE_NAME1});

    ModeAndException modeAndException =
        shadowOf(appOps).getRestriction(OP_VIBRATE, AudioAttributes.USAGE_NOTIFICATION);
    assertThat(modeAndException.mode).isEqualTo(MODE_ERRORED);
    assertThat(modeAndException.exceptionPackages).containsExactly(PACKAGE_NAME1);
  }

  @Test
  public void checkPackage_doesntExist() {
    try {
      appOps.checkPackage(123, PACKAGE_NAME1);
      fail();
    } catch (SecurityException e) {
      // expected
    }
  }

  @Test
  public void checkPackage_doesntBelong() {
    shadowOf(ApplicationProvider.getApplicationContext().getPackageManager())
        .setPackagesForUid(111, PACKAGE_NAME1);
    try {
      appOps.checkPackage(123, PACKAGE_NAME1);
      fail();
    } catch (SecurityException e) {
      // expected
    }
  }

  @Test
  public void checkPackage_belongs() {
    shadowOf(ApplicationProvider.getApplicationContext().getPackageManager())
        .setPackagesForUid(123, PACKAGE_NAME1);
    appOps.checkPackage(123, PACKAGE_NAME1);
    // check passes without exception
  }

  @Config(minSdk = KITKAT)
  @Test
  public void getPackageForOps_setNone_getNull() {
    int[] intNull = null;
    List<PackageOps> packageOps = appOps.getPackagesForOps(intNull);
    assertThat(packageOps).isNull();
  }

  @Config(minSdk = KITKAT)
  @Test
  public void getPackageForOps_setOne_getOne() {
    String packageName = "com.android.package";
    int uid = 111;
    appOps.setMode(0, uid, packageName, MODE_ALLOWED);

    int[] intNull = null;
    List<PackageOps> packageOps = appOps.getPackagesForOps(intNull);
    assertThat(containsPackageOpPair(packageOps, packageName, 0, MODE_ALLOWED)).isTrue();
  }

  @Config(minSdk = KITKAT)
  @Test
  public void getPackageForOps_setMultiple_getMultiple() {
    String packageName1 = "com.android.package";
    String packageName2 = "com.android.other";
    int uid1 = 111;
    int uid2 = 112;
    appOps.setMode(0, uid1, packageName1, MODE_ALLOWED);
    appOps.setMode(1, uid1, packageName1, MODE_DEFAULT);
    appOps.setMode(2, uid1, packageName1, MODE_ERRORED);
    appOps.setMode(3, uid1, packageName1, MODE_FOREGROUND);
    appOps.setMode(4, uid1, packageName1, MODE_IGNORED);
    appOps.setMode(0, uid2, packageName2, MODE_ALLOWED);

    int[] intNull = null;
    List<PackageOps> packageOps = appOps.getPackagesForOps(intNull);
    assertThat(containsPackageOpPair(packageOps, packageName1, 0, MODE_ALLOWED)).isTrue();
    assertThat(containsPackageOpPair(packageOps, packageName1, 1, MODE_DEFAULT)).isTrue();
    assertThat(containsPackageOpPair(packageOps, packageName1, 2, MODE_ERRORED)).isTrue();
    assertThat(containsPackageOpPair(packageOps, packageName1, 3, MODE_FOREGROUND)).isTrue();
    assertThat(containsPackageOpPair(packageOps, packageName1, 4, MODE_IGNORED)).isTrue();
    assertThat(containsPackageOpPair(packageOps, packageName2, 0, MODE_ALLOWED)).isTrue();
  }

  @Config(minSdk = KITKAT)
  @Test
  public void getPackageForOps_setMultiple_onlyGetThoseAskedFor() {
    String packageName1 = "com.android.package";
    String packageName2 = "com.android.other";
    int uid1 = 111;
    int uid2 = 112;
    appOps.setMode(0, uid1, packageName1, MODE_ALLOWED);
    appOps.setMode(1, uid1, packageName1, MODE_DEFAULT);
    appOps.setMode(2, uid1, packageName1, MODE_ERRORED);
    appOps.setMode(3, uid1, packageName1, MODE_FOREGROUND);
    appOps.setMode(4, uid1, packageName1, MODE_IGNORED);
    appOps.setMode(0, uid2, packageName2, MODE_ALLOWED);

    List<PackageOps> packageOps = appOps.getPackagesForOps(new int[] {0, 1});
    assertThat(containsPackageOpPair(packageOps, packageName1, 0, MODE_ALLOWED)).isTrue();
    assertThat(containsPackageOpPair(packageOps, packageName1, 1, MODE_DEFAULT)).isTrue();
    assertThat(containsPackageOpPair(packageOps, packageName1, 2, MODE_ERRORED)).isFalse();
    assertThat(containsPackageOpPair(packageOps, packageName1, 3, MODE_FOREGROUND)).isFalse();
    assertThat(containsPackageOpPair(packageOps, packageName1, 4, MODE_IGNORED)).isFalse();
    assertThat(containsPackageOpPair(packageOps, packageName2, 0, MODE_ALLOWED)).isTrue();
  }

  @Test
  @Config(minSdk = R)
  public void setOnApNotedCallback_isCalled() {
    AppOpsManager.OnOpNotedCallback callback = mock(AppOpsManager.OnOpNotedCallback.class);
    appOps.setOnOpNotedCallback(directExecutor(), callback);
    ArgumentCaptor<SyncNotedAppOp> captor = ArgumentCaptor.forClass(SyncNotedAppOp.class);
    appOps.noteOp(
        AppOpsManager.OPSTR_MONITOR_LOCATION,
        android.os.Process.myUid(),
        ApplicationProvider.getApplicationContext().getPackageName(),
        "tag",
        "message");
    verify(callback).onSelfNoted(captor.capture());
    assertThat(captor.getValue().getOp()).isEqualTo(AppOpsManager.OPSTR_MONITOR_LOCATION);
    assertThat(captor.getValue().getAttributionTag()).isEqualTo("tag");
  }

  @Config(minSdk = Q)
  @Test
  public void getPackageForOpsStr_setNone_getEmptyList() {
    String[] stringNull = null;
    List<PackageOps> packageOps = appOps.getPackagesForOps(stringNull);
    assertThat(packageOps).isEmpty();
  }

  @Config(minSdk = Q)
  @Test
  public void getPackageForOpsStr_setOne_getOne() {
    String packageName = "com.android.package";
    int uid = 111;
    appOps.setMode(AppOpsManager.OPSTR_COARSE_LOCATION, uid, packageName, MODE_ALLOWED);

    String[] stringNull = null;
    List<PackageOps> packageOps = appOps.getPackagesForOps(stringNull);
    assertThat(containsPackageOpPair(packageOps, packageName, OPSTR_COARSE_LOCATION, MODE_ALLOWED))
        .isTrue();
  }

  @Config(minSdk = Q)
  @Test
  public void getPackageForOpsStr_setMultiple_getMultiple() {
    String packageName1 = "com.android.package";
    String packageName2 = "com.android.other";
    int uid1 = 111;
    int uid2 = 112;
    appOps.setMode(OPSTR_COARSE_LOCATION, uid1, packageName1, MODE_ALLOWED);
    appOps.setMode(OPSTR_FINE_LOCATION, uid1, packageName1, MODE_DEFAULT);
    appOps.setMode(OPSTR_READ_PHONE_STATE, uid1, packageName1, MODE_ERRORED);
    appOps.setMode(OPSTR_RECORD_AUDIO, uid1, packageName1, MODE_FOREGROUND);
    appOps.setMode(OPSTR_BODY_SENSORS, uid1, packageName1, MODE_IGNORED);
    appOps.setMode(OPSTR_COARSE_LOCATION, uid2, packageName2, MODE_ALLOWED);

    String[] stringNull = null;
    List<PackageOps> packageOps = appOps.getPackagesForOps(stringNull);
    assertThat(containsPackageOpPair(packageOps, packageName1, OPSTR_COARSE_LOCATION, MODE_ALLOWED))
        .isTrue();
    assertThat(containsPackageOpPair(packageOps, packageName1, OPSTR_FINE_LOCATION, MODE_DEFAULT))
        .isTrue();
    assertThat(
            containsPackageOpPair(packageOps, packageName1, OPSTR_READ_PHONE_STATE, MODE_ERRORED))
        .isTrue();
    assertThat(containsPackageOpPair(packageOps, packageName1, OPSTR_RECORD_AUDIO, MODE_FOREGROUND))
        .isTrue();
    assertThat(containsPackageOpPair(packageOps, packageName1, OPSTR_BODY_SENSORS, MODE_IGNORED))
        .isTrue();
    assertThat(containsPackageOpPair(packageOps, packageName2, OPSTR_COARSE_LOCATION, MODE_ALLOWED))
        .isTrue();
  }

  @Config(minSdk = Q)
  @Test
  public void getPackageForOpsStr_setMultiple_onlyGetThoseAskedFor() {
    String packageName1 = "com.android.package";
    String packageName2 = "com.android.other";
    int uid1 = 111;
    int uid2 = 112;
    appOps.setMode(OPSTR_COARSE_LOCATION, uid1, packageName1, MODE_ALLOWED);
    appOps.setMode(OPSTR_FINE_LOCATION, uid1, packageName1, MODE_DEFAULT);
    appOps.setMode(OPSTR_READ_PHONE_STATE, uid1, packageName1, MODE_ERRORED);
    appOps.setMode(OPSTR_RECORD_AUDIO, uid1, packageName1, MODE_FOREGROUND);
    appOps.setMode(OPSTR_BODY_SENSORS, uid1, packageName1, MODE_IGNORED);
    appOps.setMode(OPSTR_COARSE_LOCATION, uid2, packageName2, MODE_ALLOWED);

    List<PackageOps> packageOps =
        appOps.getPackagesForOps(new String[] {OPSTR_COARSE_LOCATION, OPSTR_FINE_LOCATION});
    assertThat(containsPackageOpPair(packageOps, packageName1, OPSTR_COARSE_LOCATION, MODE_ALLOWED))
        .isTrue();
    assertThat(containsPackageOpPair(packageOps, packageName1, OPSTR_FINE_LOCATION, MODE_DEFAULT))
        .isTrue();
    assertThat(
            containsPackageOpPair(packageOps, packageName1, OPSTR_READ_PHONE_STATE, MODE_ERRORED))
        .isFalse();
    assertThat(containsPackageOpPair(packageOps, packageName1, OPSTR_RECORD_AUDIO, MODE_FOREGROUND))
        .isFalse();
    assertThat(containsPackageOpPair(packageOps, packageName1, OPSTR_BODY_SENSORS, MODE_IGNORED))
        .isFalse();
    assertThat(containsPackageOpPair(packageOps, packageName2, OPSTR_COARSE_LOCATION, MODE_ALLOWED))
        .isTrue();
  }

  /** Assert that the results contain the expected op codes. */
  private void assertOps(List<PackageOps> pkgOps, Integer... expectedOps) {
    Set<Integer> actualOps = new HashSet<>();
    for (PackageOps pkgOp : pkgOps) {
      for (OpEntry entry : pkgOp.getOps()) {
        actualOps.add(entry.getOp());
      }
    }

    assertThat(actualOps).containsAtLeastElementsIn(expectedOps);
  }

  /** True if the given (package, op, mode) tuple is present in the given list. */
  private boolean containsPackageOpPair(
      List<PackageOps> pkgOps, String packageName, int op, int mode) {
    for (PackageOps pkgOp : pkgOps) {
      for (OpEntry entry : pkgOp.getOps()) {
        if (packageName.equals(pkgOp.getPackageName())
            && entry.getOp() == op
            && entry.getMode() == mode) {
          return true;
        }
      }
    }
    return false;
  }

  /** True if the given (package, op, mode) tuple is present in the given list. */
  private boolean containsPackageOpPair(
      List<PackageOps> pkgOps, String packageName, String op, int mode) {
    for (PackageOps pkgOp : pkgOps) {
      for (OpEntry entry : pkgOp.getOps()) {
        if (packageName.equals(pkgOp.getPackageName())
            && op.equals(entry.getOpStr())
            && entry.getMode() == mode) {
          return true;
        }
      }
    }
    return false;
  }
}
