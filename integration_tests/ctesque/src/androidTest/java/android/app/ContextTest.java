package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.CrossProfileApps;
import android.content.pm.LauncherApps;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.biometrics.BiometricManager;
import android.hardware.camera2.CameraManager;
import android.hardware.fingerprint.FingerprintManager;
import android.media.AudioManager;
import android.media.MediaRouter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.DropBoxManager;
import android.os.UserHandle;
import android.telephony.euicc.EuiccManager;
import android.view.accessibility.CaptioningManager;
import android.view.autofill.AutofillManager;
import android.widget.RemoteViews;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import androidx.test.rule.GrantPermissionRule;
import com.google.common.truth.Truth;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

@RunWith(AndroidJUnit4.class)
public class ContextTest {
  private static final int APP_WIDGET_HOST_ID = 1;

  @Rule
  public GrantPermissionRule mRuntimePermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.BLUETOOTH,
          Manifest.permission.BLUETOOTH_ADMIN,
          Manifest.permission.MODIFY_AUDIO_SETTINGS,
          Manifest.permission.GET_ACCOUNTS,
          Manifest.permission.USE_BIOMETRIC,
          Manifest.permission.USE_BIOMETRIC,
          Manifest.permission.INTERNET,
          Manifest.permission.WRITE_EXTERNAL_STORAGE,
          Manifest.permission.READ_EXTERNAL_STORAGE);

  @Test
  public void audioManager_applicationInstance_isNotSameAsActivityInstance() {
    AudioManager applicationAudioManager =
        (AudioManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            AudioManager activityAudioManager =
                (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            assertThat(applicationAudioManager).isNotSameInstanceAs(activityAudioManager);
          });
    }
  }

  @Test
  public void audioManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            AudioManager activityAudioManager =
                (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            AudioManager anotherActivityAudioManager =
                (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            assertThat(anotherActivityAudioManager).isSameInstanceAs(activityAudioManager);
          });
    }
  }

  @Test
  public void audioManager_instance_changesAffectEachOther() {
    AudioManager applicationAudioManager =
        (AudioManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            AudioManager activityAudioManager =
                (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);

            activityAudioManager.setMode(AudioManager.MODE_RINGTONE);
            assertThat(activityAudioManager.getMode()).isEqualTo(AudioManager.MODE_RINGTONE);
            assertThat(applicationAudioManager.getMode()).isEqualTo(AudioManager.MODE_RINGTONE);

            applicationAudioManager.setMode(AudioManager.MODE_NORMAL);
            assertThat(activityAudioManager.getMode()).isEqualTo(AudioManager.MODE_NORMAL);
            assertThat(applicationAudioManager.getMode()).isEqualTo(AudioManager.MODE_NORMAL);
          });
    }
  }

  @Test
  public void accountManager_applicationInstance_isNotSameAsActivityInstance() {
    AccountManager applicationAccountManager =
        (AccountManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.ACCOUNT_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            AccountManager activityAccountManager =
                (AccountManager) activity.getSystemService(Context.ACCOUNT_SERVICE);
            assertThat(applicationAccountManager).isNotSameInstanceAs(activityAccountManager);
          });
    }
  }

  @Test
  public void accountManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            AccountManager activityAccountManager =
                (AccountManager) activity.getSystemService(Context.ACCOUNT_SERVICE);
            AccountManager anotherActivityAccountManager =
                (AccountManager) activity.getSystemService(Context.ACCOUNT_SERVICE);
            assertThat(anotherActivityAccountManager).isSameInstanceAs(activityAccountManager);
          });
    }
  }

  @Test
  public void accountManager_instance_retrievesSameAccounts() {
    AccountManager applicationAccountManager =
        (AccountManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.ACCOUNT_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            AccountManager activityAccountManager =
                (AccountManager) activity.getSystemService(Context.ACCOUNT_SERVICE);

            Account[] applicationAccounts =
                applicationAccountManager.getAccountsByType("com.example.account_type");
            Account[] activityAccounts =
                activityAccountManager.getAccountsByType("com.example.account_type");

            assertThat(activityAccounts).isEqualTo(applicationAccounts);
          });
    }
  }

  @Test
  public void batteryManager_applicationInstance_isNotSameAsActivityInstance() {
    BatteryManager applicationBatteryManager =
        (BatteryManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.BATTERY_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            BatteryManager activityBatteryManager =
                (BatteryManager) activity.getSystemService(Context.BATTERY_SERVICE);
            assertThat(applicationBatteryManager).isNotSameInstanceAs(activityBatteryManager);
          });
    }
  }

  @Test
  public void batteryManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            BatteryManager activityBatteryManager =
                (BatteryManager) activity.getSystemService(Context.BATTERY_SERVICE);
            BatteryManager anotherActivityBatteryManager =
                (BatteryManager) activity.getSystemService(Context.BATTERY_SERVICE);
            assertThat(anotherActivityBatteryManager).isSameInstanceAs(activityBatteryManager);
          });
    }
  }

  @Test
  public void alarmManager_applicationInstance_isNotSameAsActivityInstance() {
    AlarmManager applicationAlarmManager =
        (AlarmManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            AlarmManager activityAlarmManager =
                (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
            assertThat(applicationAlarmManager).isNotSameInstanceAs(activityAlarmManager);
          });
    }
  }

  @Test
  public void alarmManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            AlarmManager activityAlarmManager =
                (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
            AlarmManager anotherActivityAlarmManager =
                (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);
            assertThat(anotherActivityAlarmManager).isSameInstanceAs(activityAlarmManager);
          });
    }
  }

  @Test
  public void alarmManager_instance_retrievesSameAlarmClockInfo() {
    AlarmManager applicationAlarmManager =
        (AlarmManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            AlarmManager activityAlarmManager =
                (AlarmManager) activity.getSystemService(Context.ALARM_SERVICE);

            AlarmManager.AlarmClockInfo applicationAlarmClock =
                applicationAlarmManager.getNextAlarmClock();
            AlarmManager.AlarmClockInfo activityAlarmClock =
                activityAlarmManager.getNextAlarmClock();

            assertThat(activityAlarmClock).isEqualTo(applicationAlarmClock);
          });
    }
  }

  @Test
  public void clipboardManager_applicationInstance_isNotSameAsActivityInstance() {
    ClipboardManager applicationClipboardManager =
        (ClipboardManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            ClipboardManager activityClipboardManager =
                (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            assertThat(applicationClipboardManager).isNotSameInstanceAs(activityClipboardManager);
          });
    }
  }

  @Test
  public void clipboardManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            ClipboardManager activityClipboardManager =
                (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipboardManager anotherActivityClipboardManager =
                (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            assertThat(anotherActivityClipboardManager).isSameInstanceAs(activityClipboardManager);
          });
    }
  }

  @Test
  public void clipboardManager_instance_retrievesSamePrimaryClip() {
    ClipboardManager applicationClipboardManager =
        (ClipboardManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clipData = ClipData.newPlainText("label", "text");
    applicationClipboardManager.setPrimaryClip(clipData);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            ClipboardManager activityClipboardManager =
                (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);

            ClipData applicationClipData = applicationClipboardManager.getPrimaryClip();
            ClipData activityClipData = activityClipboardManager.getPrimaryClip();

            assertThat(activityClipData.toString()).isEqualTo(applicationClipData.toString());
          });
    }
  }

  @Test
  public void keyguardManager_applicationInstance_isNotSameAsActivityInstance() {
    KeyguardManager applicationKeyguardManager =
        (KeyguardManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            KeyguardManager activityKeyguardManager =
                (KeyguardManager) activity.getSystemService(Context.KEYGUARD_SERVICE);
            assertThat(applicationKeyguardManager).isNotSameInstanceAs(activityKeyguardManager);
          });
    }
  }

  @Test
  public void keyguardManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            KeyguardManager activityKeyguardManager =
                (KeyguardManager) activity.getSystemService(Context.KEYGUARD_SERVICE);
            KeyguardManager anotherActivityKeyguardManager =
                (KeyguardManager) activity.getSystemService(Context.KEYGUARD_SERVICE);
            assertThat(anotherActivityKeyguardManager).isSameInstanceAs(activityKeyguardManager);
          });
    }
  }

  @Test
  public void keyguardManager_isKeyguardLocked_retrievesSameState() {
    KeyguardManager applicationKeyguardManager =
        (KeyguardManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            KeyguardManager activityKeyguardManager =
                (KeyguardManager) activity.getSystemService(Context.KEYGUARD_SERVICE);

            boolean applicationIsKeyguardLocked = applicationKeyguardManager.isKeyguardLocked();
            boolean activityIsKeyguardLocked = activityKeyguardManager.isKeyguardLocked();

            assertThat(activityIsKeyguardLocked).isEqualTo(applicationIsKeyguardLocked);
          });
    }
  }

  @Test
  public void devicePolicyManager_applicationInstance_isNotSameAsActivityInstance() {
    DevicePolicyManager applicationDpm =
        (DevicePolicyManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.DEVICE_POLICY_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            DevicePolicyManager activityDpm =
                (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
            assertThat(applicationDpm).isNotSameInstanceAs(activityDpm);
          });
    }
  }

  @Test
  public void devicePolicyManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            DevicePolicyManager activityDpm =
                (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
            DevicePolicyManager anotherActivityDpm =
                (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
            assertThat(anotherActivityDpm).isSameInstanceAs(activityDpm);
          });
    }
  }

  @Test
  public void devicePolicyManager_instance_retrievesSameAdminStatus() {
    DevicePolicyManager applicationDpm =
        (DevicePolicyManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.DEVICE_POLICY_SERVICE);
    ComponentName testAdminComponent =
        new ComponentName(ApplicationProvider.getApplicationContext(), DeviceAdminReceiver.class);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            DevicePolicyManager activityDpm =
                (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);

            boolean applicationAdminActive = applicationDpm.isAdminActive(testAdminComponent);
            boolean activityAdminActive = activityDpm.isAdminActive(testAdminComponent);

            assertThat(activityAdminActive).isEqualTo(applicationAdminActive);
          });
    }
  }

  @Test
  public void autofillManager_applicationInstance_isNotSameAsActivityInstance() {
    AutofillManager applicationAutofillManager =
        ApplicationProvider.getApplicationContext().getSystemService(AutofillManager.class);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            AutofillManager activityAutofillManager =
                activity.getSystemService(AutofillManager.class);
            assertThat(applicationAutofillManager).isNotSameInstanceAs(activityAutofillManager);
          });
    }
  }

  @Test
  public void autofillManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            AutofillManager activityAutofillManager =
                activity.getSystemService(AutofillManager.class);
            AutofillManager anotherActivityAutofillManager =
                activity.getSystemService(AutofillManager.class);
            assertThat(anotherActivityAutofillManager).isSameInstanceAs(activityAutofillManager);
          });
    }
  }

  @Test
  public void autofillManager_instance_retrievesSameAutofillService() {
    AutofillManager applicationAutofillManager =
        ApplicationProvider.getApplicationContext().getSystemService(AutofillManager.class);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            AutofillManager activityAutofillManager =
                activity.getSystemService(AutofillManager.class);

            boolean applicationAutofillServiceAvailable =
                applicationAutofillManager.isAutofillSupported();
            boolean activityAutofillServiceAvailable =
                activityAutofillManager.isAutofillSupported();

            assertThat(activityAutofillServiceAvailable)
                .isEqualTo(applicationAutofillServiceAvailable);
          });
    }
  }

  @Test
  public void downloadManager_applicationInstance_isNotSameAsActivityInstance() {
    DownloadManager applicationDownloadManager =
        (DownloadManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            DownloadManager activityDownloadManager =
                (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
            assertThat(applicationDownloadManager).isNotSameInstanceAs(activityDownloadManager);
          });
    }
  }

  @Test
  public void downloadManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            DownloadManager activityDownloadManager =
                (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager anotherActivityDownloadManager =
                (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
            assertThat(anotherActivityDownloadManager).isSameInstanceAs(activityDownloadManager);
          });
    }
  }

  @Test
  public void downloadManager_instance_retrievesSameMimeTypeForDownloadedFile() {
    final long testId = 1L;
    DownloadManager applicationDownloadManager =
        (DownloadManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            DownloadManager activityDownloadManager =
                (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);

            String applicationMimeType =
                applicationDownloadManager.getMimeTypeForDownloadedFile(testId);
            String activityMimeType = activityDownloadManager.getMimeTypeForDownloadedFile(testId);

            assertThat(activityMimeType).isEqualTo(applicationMimeType);
          });
    }
  }

  @Test
  public void fingerprintManager_applicationInstance_isNotSameAsActivityInstance() {
    FingerprintManager applicationFingerprintManager =
        (FingerprintManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.FINGERPRINT_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            FingerprintManager activityFingerprintManager =
                (FingerprintManager) activity.getSystemService(Context.FINGERPRINT_SERVICE);
            assertThat(applicationFingerprintManager)
                .isNotSameInstanceAs(activityFingerprintManager);
          });
    }
  }

  @Test
  public void fingerprintManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            FingerprintManager activityFingerprintManager =
                (FingerprintManager) activity.getSystemService(Context.FINGERPRINT_SERVICE);
            FingerprintManager anotherActivityFingerprintManager =
                (FingerprintManager) activity.getSystemService(Context.FINGERPRINT_SERVICE);
            assertThat(anotherActivityFingerprintManager)
                .isSameInstanceAs(activityFingerprintManager);
          });
    }
  }

  @Test
  public void fingerprintManager_instance_hasConsistentFingerprintState() {
    FingerprintManager applicationFingerprintManager =
        (FingerprintManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.FINGERPRINT_SERVICE);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            FingerprintManager activityFingerprintManager =
                (FingerprintManager) activity.getSystemService(Context.FINGERPRINT_SERVICE);

            boolean isApplicationFingerprintAvailable =
                applicationFingerprintManager.isHardwareDetected();
            boolean isActivityFingerprintAvailable =
                activityFingerprintManager.isHardwareDetected();

            assertThat(isActivityFingerprintAvailable).isEqualTo(isApplicationFingerprintAvailable);

            boolean hasApplicationEnrolledFingerprints =
                applicationFingerprintManager.hasEnrolledFingerprints();
            boolean hasActivityEnrolledFingerprints =
                activityFingerprintManager.hasEnrolledFingerprints();

            assertThat(hasActivityEnrolledFingerprints)
                .isEqualTo(hasApplicationEnrolledFingerprints);
          });
    }
  }

  @Test
  public void activityManager_applicationInstance_isNotSameAsActivityInstance() {
    ActivityManager applicationActivityManager =
        (ActivityManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            ActivityManager activityActivityManager =
                (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
            assertThat(applicationActivityManager).isNotSameInstanceAs(activityActivityManager);
          });
    }
  }

  @Test
  public void activityManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            ActivityManager activityActivityManager =
                (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager anotherActivityActivityManager =
                (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
            assertThat(anotherActivityActivityManager).isSameInstanceAs(activityActivityManager);
          });
    }
  }

  @Test
  public void activityManager_instance_retrievesConsistentLowRamDeviceStatus() {
    ActivityManager applicationActivityManager =
        (ActivityManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            ActivityManager activityActivityManager =
                (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);

            boolean applicationLowRamStatus = applicationActivityManager.isLowRamDevice();
            boolean activityLowRamStatus = activityActivityManager.isLowRamDevice();

            assertThat(activityLowRamStatus).isEqualTo(applicationLowRamStatus);
          });
    }
  }

  @Test
  public void cameraManager_applicationInstance_isNotSameAsActivityInstance() {
    CameraManager applicationCameraManager =
        (CameraManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            CameraManager activityCameraManager =
                (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
            assertThat(applicationCameraManager).isNotSameInstanceAs(activityCameraManager);
          });
    }
  }

  @Test
  public void cameraManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            CameraManager activityCameraManager =
                (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
            CameraManager anotherActivityCameraManager =
                (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
            assertThat(anotherActivityCameraManager).isSameInstanceAs(activityCameraManager);
          });
    }
  }

  @Test
  public void appWidgetManager_applicationInstance_isNotSameAsActivityInstance() {
    AppWidgetManager applicationAppWidgetManager =
        (AppWidgetManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.APPWIDGET_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            AppWidgetManager activityAppWidgetManager =
                (AppWidgetManager) activity.getSystemService(Context.APPWIDGET_SERVICE);
            assertThat(applicationAppWidgetManager).isNotSameInstanceAs(activityAppWidgetManager);
          });
    }
  }

  @Test
  public void appWidgetManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            AppWidgetManager activityAppWidgetManager =
                (AppWidgetManager) activity.getSystemService(Context.APPWIDGET_SERVICE);
            AppWidgetManager anotherActivityAppWidgetManager =
                (AppWidgetManager) activity.getSystemService(Context.APPWIDGET_SERVICE);
            assertThat(anotherActivityAppWidgetManager).isSameInstanceAs(activityAppWidgetManager);
          });
    }
  }

  @Test
  public void appWidgetManager_instance_retrievesSameAppWidgets() {
    Context context = ApplicationProvider.getApplicationContext();
    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

    ComponentName providerComponent = new ComponentName(context, TestAppWidgetProvider.class);
    AppWidgetProviderInfo appWidgetProviderInfo = new AppWidgetProviderInfo();
    appWidgetProviderInfo.provider = providerComponent;
    appWidgetProviderInfo.updatePeriodMillis = 0;
    appWidgetProviderInfo.initialLayout = android.R.layout.simple_list_item_1;

    AppWidgetHost appWidgetHost = new AppWidgetHost(context, APP_WIDGET_HOST_ID);

    int appWidgetId = appWidgetHost.allocateAppWidgetId();
    appWidgetManager.bindAppWidgetIdIfAllowed(appWidgetId, providerComponent);

    appWidgetManager.updateAppWidget(
        appWidgetId,
        new RemoteViews(context.getPackageName(), android.R.layout.simple_list_item_1));

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            AppWidgetManager activityAppWidgetManager =
                (AppWidgetManager) activity.getSystemService(Context.APPWIDGET_SERVICE);

            int[] applicationAppWidgets = appWidgetManager.getAppWidgetIds(providerComponent);
            int[] activityAppWidgets = activityAppWidgetManager.getAppWidgetIds(providerComponent);

            Truth.assertThat(activityAppWidgets).isEqualTo(applicationAppWidgets);

            appWidgetHost.deleteAppWidgetId(appWidgetId);
          });
    }
  }

  @Test
  public void biometricManager_applicationInstance_isNotSameAsActivityInstance() {
    BiometricManager applicationBiometricManager =
        (BiometricManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.BIOMETRIC_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            BiometricManager activityBiometricManager =
                (BiometricManager) activity.getSystemService(Context.BIOMETRIC_SERVICE);
            assertThat(applicationBiometricManager).isNotSameInstanceAs(activityBiometricManager);
          });
    }
  }

  @Test
  public void biometricManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            BiometricManager activityBiometricManager =
                (BiometricManager) activity.getSystemService(Context.BIOMETRIC_SERVICE);
            BiometricManager anotherActivityBiometricManager =
                (BiometricManager) activity.getSystemService(Context.BIOMETRIC_SERVICE);
            assertThat(anotherActivityBiometricManager).isSameInstanceAs(activityBiometricManager);
          });
    }
  }

  @Test
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.R)
  public void biometricManager_instance_retrievesSameAuthenticationResult_withAuthenticators() {
    BiometricManager applicationBiometricManager =
        (BiometricManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.BIOMETRIC_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            BiometricManager activityBiometricManager =
                (BiometricManager) activity.getSystemService(Context.BIOMETRIC_SERVICE);

            int authenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK;
            int applicationCanAuthenticate =
                applicationBiometricManager.canAuthenticate(authenticators);
            int activityCanAuthenticate = activityBiometricManager.canAuthenticate(authenticators);

            assertThat(activityCanAuthenticate).isEqualTo(applicationCanAuthenticate);
          });
    }
  }

  @Test
  public void bluetoothManager_applicationInstance_isNotSameAsActivityInstance() {
    BluetoothManager applicationBluetoothManager =
        (BluetoothManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            BluetoothManager activityBluetoothManager =
                (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
            assertThat(applicationBluetoothManager).isNotSameInstanceAs(activityBluetoothManager);
          });
    }
  }

  @Test
  public void bluetoothManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            BluetoothManager activityBluetoothManager =
                (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
            BluetoothManager anotherActivityBluetoothManager =
                (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);
            assertThat(anotherActivityBluetoothManager).isSameInstanceAs(activityBluetoothManager);
          });
    }
  }

  @Test
  @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.Q)
  public void bluetoothManager_instance_retrievesSameAdapter() {
    BluetoothManager applicationBluetoothManager =
        (BluetoothManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);

    BluetoothAdapter applicationAdapter = applicationBluetoothManager.getAdapter();

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            BluetoothManager activityBluetoothManager =
                (BluetoothManager) activity.getSystemService(Context.BLUETOOTH_SERVICE);

            BluetoothAdapter activityAdapter = activityBluetoothManager.getAdapter();

            assertThat(applicationAdapter).isEqualTo(activityAdapter);
          });
    }
  }

  @Test
  public void appOpsManager_applicationInstance_isNotSameAsActivityInstance() {
    AppOpsManager applicationAppOpsManager =
        (AppOpsManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.APP_OPS_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            AppOpsManager activityAppOpsManager =
                (AppOpsManager) activity.getSystemService(Context.APP_OPS_SERVICE);
            assertThat(applicationAppOpsManager).isNotSameInstanceAs(activityAppOpsManager);
          });
    }
  }

  @Test
  public void appOpsManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            AppOpsManager activityAppOpsManager =
                (AppOpsManager) activity.getSystemService(Context.APP_OPS_SERVICE);
            AppOpsManager anotherActivityAppOpsManager =
                (AppOpsManager) activity.getSystemService(Context.APP_OPS_SERVICE);
            assertThat(anotherActivityAppOpsManager).isSameInstanceAs(activityAppOpsManager);
          });
    }
  }

  @Test
  public void appOpsManager_instance_retrievesSameOps() {
    AppOpsManager applicationAppOpsManager =
        (AppOpsManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.APP_OPS_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            AppOpsManager activityAppOpsManager =
                (AppOpsManager) activity.getSystemService(Context.APP_OPS_SERVICE);

            String opCode = AppOpsManager.OPSTR_CAMERA;
            int applicationOpMode =
                applicationAppOpsManager.checkOpNoThrow(
                    opCode, android.os.Process.myUid(), "com.example.app");
            int activityOpMode =
                activityAppOpsManager.checkOpNoThrow(
                    opCode, android.os.Process.myUid(), "com.example.app");

            assertThat(activityOpMode).isEqualTo(applicationOpMode);
          });
    }
  }

  @Test
  public void euiccManager_applicationInstance_isNotSameAsActivityInstance() {
    EuiccManager applicationEuiccManager =
        (EuiccManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.EUICC_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            EuiccManager activityEuiccManager =
                (EuiccManager) activity.getSystemService(Context.EUICC_SERVICE);
            assertThat(applicationEuiccManager).isNotSameInstanceAs(activityEuiccManager);
          });
    }
  }

  @Test
  public void euiccManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            EuiccManager activityEuiccManager =
                (EuiccManager) activity.getSystemService(Context.EUICC_SERVICE);
            EuiccManager anotherActivityEuiccManager =
                (EuiccManager) activity.getSystemService(Context.EUICC_SERVICE);
            assertThat(anotherActivityEuiccManager).isSameInstanceAs(activityEuiccManager);
          });
    }
  }

  @Test
  public void euiccManager_instance_getsEid() {
    EuiccManager applicationEuiccManager =
        (EuiccManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.EUICC_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            EuiccManager activityEuiccManager =
                (EuiccManager) activity.getSystemService(Context.EUICC_SERVICE);

            String applicationEid = applicationEuiccManager.getEid();
            String activityEid = activityEuiccManager.getEid();

            assertThat(activityEid).isEqualTo(applicationEid);
          });
    }
  }

  @Test
  public void crossProfileApps_applicationInstance_isNotSameAsActivityInstance() {
    CrossProfileApps applicationCrossProfileApps =
        (CrossProfileApps)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.CROSS_PROFILE_APPS_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            CrossProfileApps activityCrossProfileApps =
                (CrossProfileApps) activity.getSystemService(Context.CROSS_PROFILE_APPS_SERVICE);
            assertThat(applicationCrossProfileApps).isNotSameInstanceAs(activityCrossProfileApps);
          });
    }
  }

  @Test
  public void crossProfileApps_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            CrossProfileApps activityCrossProfileApps =
                (CrossProfileApps) activity.getSystemService(Context.CROSS_PROFILE_APPS_SERVICE);
            CrossProfileApps anotherActivityCrossProfileApps =
                (CrossProfileApps) activity.getSystemService(Context.CROSS_PROFILE_APPS_SERVICE);
            assertThat(anotherActivityCrossProfileApps).isSameInstanceAs(activityCrossProfileApps);
          });
    }
  }

  @Test
  public void crossProfileApps_instance_retrievesSameTargetUserProfiles() {
    CrossProfileApps applicationCrossProfileApps =
        (CrossProfileApps)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.CROSS_PROFILE_APPS_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            CrossProfileApps activityCrossProfileApps =
                (CrossProfileApps) activity.getSystemService(Context.CROSS_PROFILE_APPS_SERVICE);

            List<UserHandle> applicationTargetUserProfiles =
                applicationCrossProfileApps.getTargetUserProfiles();
            List<UserHandle> activityTargetUserProfiles =
                activityCrossProfileApps.getTargetUserProfiles();

            assertThat(activityTargetUserProfiles).isEqualTo(applicationTargetUserProfiles);
          });
    }
  }

  @Test
  public void launcherApps_applicationInstance_isNotSameAsActivityInstance() {
    LauncherApps applicationLauncherApps =
        (LauncherApps)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.LAUNCHER_APPS_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            LauncherApps activityLauncherApps =
                (LauncherApps) activity.getSystemService(Context.LAUNCHER_APPS_SERVICE);
            assertThat(applicationLauncherApps).isNotSameInstanceAs(activityLauncherApps);
          });
    }
  }

  @Test
  public void launcherApps_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            LauncherApps activityLauncherApps =
                (LauncherApps) activity.getSystemService(Context.LAUNCHER_APPS_SERVICE);
            LauncherApps anotherActivityLauncherApps =
                (LauncherApps) activity.getSystemService(Context.LAUNCHER_APPS_SERVICE);
            assertThat(anotherActivityLauncherApps).isSameInstanceAs(activityLauncherApps);
          });
    }
  }

  @Test
  public void launcherApps_instance_retrievesSameProfiles() {
    LauncherApps applicationLauncherApps =
        (LauncherApps)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.LAUNCHER_APPS_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            LauncherApps activityLauncherApps =
                (LauncherApps) activity.getSystemService(Context.LAUNCHER_APPS_SERVICE);

            List<UserHandle> applicationProfiles = applicationLauncherApps.getProfiles();
            List<UserHandle> activityProfiles = activityLauncherApps.getProfiles();

            assertThat(applicationProfiles).isNotEmpty();
            assertThat(activityProfiles).isNotEmpty();

            assertThat(activityProfiles).isEqualTo(applicationProfiles);
          });
    }
  }

  @Test
  public void dropBoxManager_applicationInstance_isNotSameAsActivityInstance() {
    DropBoxManager applicationDropBoxManager =
        (DropBoxManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.DROPBOX_SERVICE);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            DropBoxManager activityDropBoxManager =
                (DropBoxManager) activity.getSystemService(Context.DROPBOX_SERVICE);
            assertThat(applicationDropBoxManager).isNotSameInstanceAs(activityDropBoxManager);
          });
    }
  }

  @Test
  public void dropBoxManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            DropBoxManager activityDropBoxManager =
                (DropBoxManager) activity.getSystemService(Context.DROPBOX_SERVICE);
            DropBoxManager anotherActivityDropBoxManager =
                (DropBoxManager) activity.getSystemService(Context.DROPBOX_SERVICE);
            assertThat(anotherActivityDropBoxManager).isSameInstanceAs(activityDropBoxManager);
          });
    }
  }

  @Test
  public void dropBoxManager_instance_retrievesSameEntry_noPermissionRequired() {
    DropBoxManager applicationDropBoxManager =
        (DropBoxManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.DROPBOX_SERVICE);

    String tag = "testTag";
    String data = "testData";
    applicationDropBoxManager.addText(tag, data);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            DropBoxManager activityDropBoxManager =
                (DropBoxManager) activity.getSystemService(Context.DROPBOX_SERVICE);

            boolean applicationTagEnabled = applicationDropBoxManager.isTagEnabled(tag);
            boolean activityTagEnabled = activityDropBoxManager.isTagEnabled(tag);

            assertThat(activityTagEnabled).isEqualTo(applicationTagEnabled);
          });
    }
  }

  @Test
  public void mediaRouter_applicationInstance_isNotSameAsActivityInstance() {
    MediaRouter applicationMediaRouter =
        (MediaRouter)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.MEDIA_ROUTER_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            MediaRouter activityMediaRouter =
                (MediaRouter) activity.getSystemService(Context.MEDIA_ROUTER_SERVICE);
            assertThat(applicationMediaRouter).isNotSameInstanceAs(activityMediaRouter);
          });
    }
  }

  @Test
  public void mediaRouter_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            MediaRouter activityMediaRouter =
                (MediaRouter) activity.getSystemService(Context.MEDIA_ROUTER_SERVICE);
            MediaRouter anotherActivityMediaRouter =
                (MediaRouter) activity.getSystemService(Context.MEDIA_ROUTER_SERVICE);
            assertThat(anotherActivityMediaRouter).isSameInstanceAs(activityMediaRouter);
          });
    }
  }

  @Test
  public void mediaRouter_instance_retrievesSameDefaultRoute() {
    MediaRouter applicationMediaRouter =
        (MediaRouter)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.MEDIA_ROUTER_SERVICE);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            MediaRouter activityMediaRouter =
                (MediaRouter) activity.getSystemService(Context.MEDIA_ROUTER_SERVICE);

            MediaRouter.RouteInfo applicationDefaultRoute =
                applicationMediaRouter.getDefaultRoute();
            MediaRouter.RouteInfo activityDefaultRoute = activityMediaRouter.getDefaultRoute();

            assertThat(activityDefaultRoute).isEqualTo(applicationDefaultRoute);
          });
    }
  }

  @Test
  public void captioningManager_applicationInstance_isNotSameAsActivityInstance() {
    CaptioningManager applicationCaptioningManager =
        (CaptioningManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.CAPTIONING_SERVICE);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            CaptioningManager activityCaptioningManager =
                (CaptioningManager) activity.getSystemService(Context.CAPTIONING_SERVICE);

            assertThat(applicationCaptioningManager).isNotSameInstanceAs(activityCaptioningManager);
          });
    }
  }

  @Test
  public void captioningManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            CaptioningManager activityCaptioningManager =
                (CaptioningManager) activity.getSystemService(Context.CAPTIONING_SERVICE);

            CaptioningManager anotherActivityCaptioningManager =
                (CaptioningManager) activity.getSystemService(Context.CAPTIONING_SERVICE);

            assertThat(anotherActivityCaptioningManager)
                .isSameInstanceAs(activityCaptioningManager);
          });
    }
  }

  @Test
  public void captioningManager_instance_retrievesSameValues() {
    CaptioningManager applicationCaptioningManager =
        (CaptioningManager)
            ApplicationProvider.getApplicationContext()
                .getSystemService(Context.CAPTIONING_SERVICE);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            CaptioningManager activityCaptioningManager =
                (CaptioningManager) activity.getSystemService(Context.CAPTIONING_SERVICE);

            boolean applicationisEnabled = applicationCaptioningManager.isEnabled();
            boolean activityisEnabled = activityCaptioningManager.isEnabled();

            assertThat(applicationisEnabled).isEqualTo(activityisEnabled);
          });
    }
  }

  @Test
  public void sensorManager_applicationInstance_isNotSameAsActivityInstance() {
    SensorManager applicationSensorManager =
        (SensorManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            SensorManager activitySensorManager =
                (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);

            assertThat(applicationSensorManager).isNotSameInstanceAs(activitySensorManager);
          });
    }
  }

  @Test
  public void sensorManager_activityInstance_isSameAsActivityInstance() {
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            SensorManager activitySensorManager =
                (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);

            SensorManager anotherActivitySensorManager =
                (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);

            assertThat(anotherActivitySensorManager).isSameInstanceAs(activitySensorManager);
          });
    }
  }

  @Test
  public void sensorManager_instance_retrievesSameValues() {
    SensorManager applicationSensorManager =
        (SensorManager)
            ApplicationProvider.getApplicationContext().getSystemService(Context.SENSOR_SERVICE);

    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            SensorManager activitySensorManager =
                (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);

            List<Sensor> applicationSensors =
                applicationSensorManager.getSensorList(Sensor.TYPE_ALL);
            List<Sensor> activitySensors = activitySensorManager.getSensorList(Sensor.TYPE_ALL);

            for (int i = 0; i < applicationSensors.size(); i++) {
              Sensor appSensor = applicationSensors.get(i);
              Sensor actSensor = activitySensors.get(i);

              assertThat(appSensor.getName()).isEqualTo(actSensor.getName());
              assertThat(appSensor.getType()).isEqualTo(actSensor.getType());
              assertThat(appSensor.getMaximumRange()).isEqualTo(actSensor.getMaximumRange());
              assertThat(appSensor.getResolution()).isEqualTo(actSensor.getResolution());
              assertThat(appSensor.getPower()).isEqualTo(actSensor.getPower());
              assertThat(appSensor.getMinDelay()).isEqualTo(actSensor.getMinDelay());
            }
          });
    }
  }

  private static class TestAppWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
      for (int appWidgetId : appWidgetIds) {
        RemoteViews views =
            new RemoteViews(context.getPackageName(), android.R.layout.simple_list_item_1);
        views.setTextViewText(android.R.id.text1, "Test Widget");
        appWidgetManager.updateAppWidget(appWidgetId, views);
      }
    }
  }
}
