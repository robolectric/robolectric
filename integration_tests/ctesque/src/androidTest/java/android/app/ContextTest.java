package android.app;

import static com.google.common.truth.Truth.assertThat;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.os.BatteryManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.testapp.TestActivity;

@RunWith(AndroidJUnit4.class)
public class ContextTest {
  @Rule
  public GrantPermissionRule mRuntimePermissionRule =
      GrantPermissionRule.grant(
          Manifest.permission.MODIFY_AUDIO_SETTINGS, Manifest.permission.GET_ACCOUNTS);

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
}
