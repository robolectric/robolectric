package org.robolectric.shadows;

import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static android.app.PendingIntent.FLAG_IMMUTABLE;
import static android.app.PendingIntent.FLAG_NO_CREATE;
import static android.app.PendingIntent.FLAG_ONE_SHOT;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcel;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowPendingIntentTest {

  private Context context;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
  }

  @Test
  public void getBroadcast_shouldCreateIntentForBroadcast() {
    Intent intent = new Intent();
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 99, intent, 100);

    ShadowPendingIntent shadow = shadowOf(pendingIntent);
    assertThat(shadow.isActivityIntent()).isFalse();
    assertThat(shadow.isBroadcastIntent()).isTrue();
    assertThat(shadow.isServiceIntent()).isFalse();
    assertThat(shadow.isForegroundServiceIntent()).isFalse();
    assertThat(intent).isEqualTo(shadow.getSavedIntent());
    assertThat(context).isEqualTo(shadow.getSavedContext());
    assertThat(shadow.getRequestCode()).isEqualTo(99);
    assertThat(shadow.getFlags()).isEqualTo(100);
  }

  @Test
  public void getActivity_shouldCreateIntentForBroadcast() {
    Intent intent = new Intent();
    PendingIntent pendingIntent = PendingIntent.getActivity(context, 99, intent, 100);

    ShadowPendingIntent shadow = shadowOf(pendingIntent);
    assertThat(shadow.isActivityIntent()).isTrue();
    assertThat(shadow.isBroadcastIntent()).isFalse();
    assertThat(shadow.isServiceIntent()).isFalse();
    assertThat(shadow.isForegroundServiceIntent()).isFalse();
    assertThat(intent).isEqualTo(shadow.getSavedIntent());
    assertThat(context).isEqualTo(shadow.getSavedContext());
    assertThat(shadow.getRequestCode()).isEqualTo(99);
    assertThat(shadow.getFlags()).isEqualTo(100);
  }

  @Test
  public void getActivities_shouldCreateIntentForBroadcast() throws Exception {
    Intent[] intents = {new Intent(Intent.ACTION_VIEW), new Intent(Intent.ACTION_PICK)};
    PendingIntent pendingIntent = PendingIntent.getActivities(context, 99, intents, 100);

    ShadowPendingIntent shadow = shadowOf(pendingIntent);
    assertThat(shadow.getSavedIntents()).isEqualTo(intents);

    pendingIntent.send();
    ShadowApplication application =
        shadowOf((Application) ApplicationProvider.getApplicationContext());
    assertThat(application.getNextStartedActivity()).isEqualTo(intents[1]);
    assertThat(application.getNextStartedActivity()).isEqualTo(intents[0]);
  }

  @Test
  public void getActivities_withBundle_shouldCreateIntentForBroadcast() throws Exception {
    Intent[] intents = {new Intent(Intent.ACTION_VIEW), new Intent(Intent.ACTION_PICK)};
    PendingIntent pendingIntent =
        PendingIntent.getActivities(context, 99, intents, 100, Bundle.EMPTY);

    ShadowPendingIntent shadow = shadowOf(pendingIntent);
    assertThat(shadow.getSavedIntents()).isEqualTo(intents);

    pendingIntent.send();
    ShadowApplication application =
        shadowOf((Application) ApplicationProvider.getApplicationContext());
    assertThat(application.getNextStartedActivity()).isEqualTo(intents[1]);
    assertThat(application.getNextStartedActivity()).isEqualTo(intents[0]);
  }

  @Test
  public void getService_shouldCreateIntentForBroadcast() {
    Intent intent = new Intent().setPackage("dummy.package");
    PendingIntent pendingIntent = PendingIntent.getService(context, 99, intent, 100);

    ShadowPendingIntent shadow = shadowOf(pendingIntent);
    assertThat(shadow.isActivityIntent()).isFalse();
    assertThat(shadow.isBroadcastIntent()).isFalse();
    assertThat(shadow.isForegroundServiceIntent()).isFalse();
    assertThat(shadow.isServiceIntent()).isTrue();
    assertThat(intent).isEqualTo(shadow.getSavedIntent());
    assertThat(context).isEqualTo(shadow.getSavedContext());
    assertThat(shadow.getRequestCode()).isEqualTo(99);
    assertThat(shadow.getFlags()).isEqualTo(100);
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O)
  public void getForegroundService_shouldCreateIntentForBroadcast() {
    Intent intent = new Intent().setPackage("dummy.package");
    PendingIntent pendingIntent = PendingIntent.getForegroundService(context, 99, intent, 100);

    ShadowPendingIntent shadow = shadowOf(pendingIntent);
    assertThat(shadow.isActivityIntent()).isFalse();
    assertThat(shadow.isBroadcastIntent()).isFalse();
    assertThat(shadow.isForegroundServiceIntent()).isTrue();
    assertThat(shadow.isServiceIntent()).isFalse();
    assertThat(intent).isEqualTo(shadow.getSavedIntent());
    assertThat(context).isEqualTo(shadow.getSavedContext());
    assertThat(shadow.getRequestCode()).isEqualTo(99);
    assertThat(shadow.getFlags()).isEqualTo(100);
  }

  @Test
  public void getActivities_nullIntent() {
    try {
      PendingIntent.getActivities(context, 99, null, 100);
      fail("Expected NullPointerException when creating PendingIntent with null Intent[]");
    } catch (NullPointerException ignore) {
      // expected
    }
  }

  @Test
  public void getActivities_withBundle_nullIntent() {
    try {
      PendingIntent.getActivities(context, 99, null, 100, Bundle.EMPTY);
      fail("Expected NullPointerException when creating PendingIntent with null Intent[]");
    } catch (NullPointerException ignore) {
      // expected
    }
  }

  @Test
  public void send_shouldFillInIntentData() throws Exception {
    Intent intent = new Intent("action");
    Context context = Robolectric.setupActivity(Activity.class);
    PendingIntent pendingIntent = PendingIntent.getActivity(context, 99, intent, 100);

    Activity otherContext = Robolectric.setupActivity(Activity.class);
    Intent fillIntent = new Intent().putExtra("TEST", 23);
    pendingIntent.send(otherContext, 0, fillIntent);

    Intent i = shadowOf(otherContext).getNextStartedActivity();
    assertThat(i).isNotNull();
    assertThat(i.filterEquals(intent)).isTrue(); // Ignore extras.
    assertThat(i.getIntExtra("TEST", -1)).isEqualTo(23);
  }

  @Test
  public void send_shouldNotReusePreviouslyFilledInIntentData() throws Exception {
    Intent intent = new Intent("action");
    Context context = Robolectric.setupActivity(Activity.class);
    PendingIntent pendingIntent = PendingIntent.getActivity(context, 99, intent, 100);

    Activity otherContext = Robolectric.setupActivity(Activity.class);
    Intent firstFillIntent = new Intent().putExtra("KEY1", 23).putExtra("KEY2", 24);
    pendingIntent.send(otherContext, 0, firstFillIntent);

    ShadowActivity shadowActivity = shadowOf(otherContext);
    shadowActivity.clearNextStartedActivities();

    Intent secondFillIntent = new Intent().putExtra("KEY1", 50);
    pendingIntent.send(otherContext, 0, secondFillIntent);

    Intent i = shadowActivity.getNextStartedActivity();
    assertThat(i).isNotNull();
    assertThat(i.filterEquals(intent)).isTrue(); // Ignore extras.
    assertThat(i.getIntExtra("KEY1", -1)).isEqualTo(50);
    assertThat(i.hasExtra("KEY2")).isFalse();
  }

  @Test
  public void send_shouldFillInLastIntentData() throws Exception {
    Intent[] intents = {new Intent("first"), new Intent("second")};
    Context context = Robolectric.setupActivity(Activity.class);
    PendingIntent pendingIntent = PendingIntent.getActivities(context, 99, intents, 100);

    Activity otherContext = Robolectric.setupActivity(Activity.class);
    Intent fillIntent = new Intent();
    fillIntent.putExtra("TEST", 23);
    pendingIntent.send(otherContext, 0, fillIntent);

    ShadowActivity shadowActivity = shadowOf(otherContext);
    Intent second = shadowActivity.getNextStartedActivity();
    assertThat(second).isNotNull();
    assertThat(second.filterEquals(intents[1])).isTrue(); // Ignore extras.
    assertThat(second.getIntExtra("TEST", -1)).isEqualTo(23);

    Intent first = shadowActivity.getNextStartedActivity();
    assertThat(first).isNotNull();
    assertThat(first).isSameInstanceAs(intents[0]);
    assertThat(first.hasExtra("TEST")).isFalse();
  }

  @Test
  public void send_shouldNotUsePreviouslyFilledInLastIntentData() throws Exception {
    Intent[] intents = {new Intent("first"), new Intent("second")};
    Context context = Robolectric.setupActivity(Activity.class);
    PendingIntent pendingIntent = PendingIntent.getActivities(context, 99, intents, 100);

    Activity otherContext = Robolectric.setupActivity(Activity.class);
    Intent firstFillIntent = new Intent();
    firstFillIntent.putExtra("KEY1", 23);
    firstFillIntent.putExtra("KEY2", 24);
    pendingIntent.send(otherContext, 0, firstFillIntent);

    ShadowActivity shadowActivity = shadowOf(otherContext);
    shadowActivity.clearNextStartedActivities();

    Intent secondFillIntent = new Intent();
    secondFillIntent.putExtra("KEY1", 50);
    pendingIntent.send(otherContext, 0, secondFillIntent);

    Intent second = shadowActivity.getNextStartedActivity();
    assertThat(second).isNotNull();
    assertThat(second.filterEquals(intents[1])).isTrue(); // Ignore extras.
    assertThat(second.getIntExtra("KEY1", -1)).isEqualTo(50);
    assertThat(second.hasExtra("KEY2")).isFalse();
  }

  @Test
  public void send_shouldNotFillIn_whenPendingIntentIsImmutable() throws Exception {
    Intent intent = new Intent();
    Context context = Robolectric.setupActivity(Activity.class);
    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, FLAG_IMMUTABLE);

    Activity otherContext = Robolectric.setupActivity(Activity.class);
    Intent fillIntent = new Intent().putExtra("TEST", 23);
    pendingIntent.send(otherContext, 0, fillIntent);

    Intent i = shadowOf(otherContext).getNextStartedActivity();
    assertThat(i).isNotNull();
    assertThat(i).isSameInstanceAs(intent);
    assertThat(i.hasExtra("TEST")).isFalse();
  }

  @Test
  public void updatePendingIntent() {
    Intent intent = new Intent().putExtra("whatever", 5);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
    ShadowPendingIntent shadowPendingIntent = shadowOf(pendingIntent);

    // Absent FLAG_UPDATE_CURRENT, this should fail to update the Intent extra.
    intent = new Intent().putExtra("whatever", 77);
    PendingIntent.getBroadcast(context, 0, intent, 0);
    assertThat(shadowPendingIntent.getSavedIntent().getIntExtra("whatever", -1)).isEqualTo(5);

    // With FLAG_UPDATE_CURRENT, this should succeed in updating the Intent extra.
    PendingIntent.getBroadcast(context, 0, intent, FLAG_UPDATE_CURRENT);
    assertThat(shadowPendingIntent.getSavedIntent().getIntExtra("whatever", -1)).isEqualTo(77);
  }

  @Test
  public void getActivity_withFlagNoCreate_shouldReturnNullIfNoPendingIntentExists() {
    Intent intent = new Intent();
    assertThat(PendingIntent.getActivity(context, 99, intent, FLAG_NO_CREATE)).isNull();
  }

  @Test
  public void getActivity_withFlagNoCreate_shouldReturnNullIfRequestCodeIsUnmatched() {
    Intent intent = new Intent();
    PendingIntent.getActivity(context, 99, intent, 0);
    assertThat(PendingIntent.getActivity(context, 98, intent, FLAG_NO_CREATE)).isNull();
  }

  @Test
  public void getActivity_withFlagNoCreate_shouldReturnExistingIntent() {
    Intent intent = new Intent();
    PendingIntent.getActivity(context, 99, intent, 100);

    Intent identical = new Intent();
    PendingIntent saved = PendingIntent.getActivity(context, 99, identical, FLAG_NO_CREATE);
    assertThat(saved).isNotNull();
    assertThat(intent).isSameInstanceAs(shadowOf(saved).getSavedIntent());
  }

  @Test
  public void getActivity_withNoFlags_shouldReturnExistingIntent() {
    Intent intent = new Intent();
    PendingIntent.getActivity(context, 99, intent, 100);

    Intent updated = new Intent();
    PendingIntent saved = PendingIntent.getActivity(context, 99, updated, 0);
    assertThat(saved).isNotNull();
    assertThat(intent).isSameInstanceAs(shadowOf(saved).getSavedIntent());
  }

  @Test
  public void getActivities_withFlagNoCreate_shouldReturnNullIfNoPendingIntentExists() {
    Intent[] intents = {new Intent(Intent.ACTION_VIEW), new Intent(Intent.ACTION_PICK)};
    PendingIntent pendingIntent = PendingIntent.getActivities(context, 99, intents, FLAG_NO_CREATE);
    assertThat(pendingIntent).isNull();
  }

  @Test
  public void getActivities_withFlagNoCreate_shouldReturnNullIfRequestCodeIsUnmatched() {
    Intent[] intents = {new Intent()};
    PendingIntent.getActivities(context, 99, intents, 0);
    assertThat(PendingIntent.getActivities(context, 98, intents, FLAG_NO_CREATE)).isNull();
  }

  @Test
  public void getActivities_withFlagNoCreate_shouldReturnExistingIntent() {
    Intent[] intents = {new Intent(Intent.ACTION_VIEW), new Intent(Intent.ACTION_PICK)};
    PendingIntent.getActivities(ApplicationProvider.getApplicationContext(), 99, intents, 100);

    Intent[] identicalIntents = {new Intent(Intent.ACTION_VIEW), new Intent(Intent.ACTION_PICK)};
    PendingIntent saved =
        PendingIntent.getActivities(context, 99, identicalIntents, FLAG_NO_CREATE);
    assertThat(saved).isNotNull();
    assertThat(intents).isSameInstanceAs(shadowOf(saved).getSavedIntents());
  }

  @Test
  public void getActivities_withNoFlags_shouldReturnExistingIntent() {
    Intent[] intents = {new Intent(Intent.ACTION_VIEW), new Intent(Intent.ACTION_PICK)};
    PendingIntent.getActivities(ApplicationProvider.getApplicationContext(), 99, intents, 100);

    Intent[] identicalIntents = {new Intent(Intent.ACTION_VIEW), new Intent(Intent.ACTION_PICK)};
    PendingIntent saved = PendingIntent.getActivities(context, 99, identicalIntents, 0);
    assertThat(saved).isNotNull();
    assertThat(intents).isSameInstanceAs(shadowOf(saved).getSavedIntents());
  }

  @Test
  public void getBroadcast_withFlagNoCreate_shouldReturnNullIfNoPendingIntentExists() {
    Intent intent = new Intent();
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 99, intent, FLAG_NO_CREATE);
    assertThat(pendingIntent).isNull();
  }

  @Test
  public void getBroadcast_withFlagNoCreate_shouldReturnNullIfRequestCodeIsUnmatched() {
    Intent intent = new Intent();
    PendingIntent.getBroadcast(context, 99, intent, 0);
    assertThat(PendingIntent.getBroadcast(context, 98, intent, FLAG_NO_CREATE)).isNull();
  }

  @Test
  public void getBroadcast_withFlagNoCreate_shouldReturnExistingIntent() {
    Intent intent = new Intent();
    PendingIntent.getBroadcast(context, 99, intent, 100);

    Intent identical = new Intent();
    PendingIntent saved = PendingIntent.getBroadcast(context, 99, identical, FLAG_NO_CREATE);
    assertThat(saved).isNotNull();
    assertThat(intent).isSameInstanceAs(shadowOf(saved).getSavedIntent());
  }

  @Test
  public void getBroadcast_withNoFlags_shouldReturnExistingIntent() {
    Intent intent = new Intent();
    PendingIntent.getBroadcast(context, 99, intent, 100);

    Intent identical = new Intent();
    PendingIntent saved = PendingIntent.getBroadcast(context, 99, identical, 0);
    assertThat(saved).isNotNull();
    assertThat(intent).isSameInstanceAs(shadowOf(saved).getSavedIntent());
  }

  @Test
  public void getService_withFlagNoCreate_shouldReturnNullIfNoPendingIntentExists() {
    Intent intent = new Intent().setPackage("dummy.package");
    PendingIntent pendingIntent = PendingIntent.getService(context, 99, intent, FLAG_NO_CREATE);
    assertThat(pendingIntent).isNull();
  }

  @Test
  public void getService_withFlagNoCreate_shouldReturnNullIfRequestCodeIsUnmatched() {
    Intent intent = new Intent().setPackage("dummy.package");
    PendingIntent.getService(context, 99, intent, 0);
    assertThat(PendingIntent.getService(context, 98, intent, FLAG_NO_CREATE)).isNull();
  }

  @Test
  public void getService_withFlagNoCreate_shouldReturnExistingIntent() {
    Intent intent = new Intent().setPackage("dummy.package");
    PendingIntent.getService(context, 99, intent, 100);

    Intent identical = new Intent().setPackage("dummy.package");
    PendingIntent saved = PendingIntent.getService(context, 99, identical, FLAG_NO_CREATE);
    assertThat(saved).isNotNull();
    assertThat(intent).isSameInstanceAs(shadowOf(saved).getSavedIntent());
  }

  @Test
  public void getService_withNoFlags_shouldReturnExistingIntent() {
    Intent intent = new Intent().setPackage("dummy.package");
    PendingIntent.getService(context, 99, intent, 100);

    Intent identical = new Intent().setPackage("dummy.package");
    PendingIntent saved = PendingIntent.getService(context, 99, identical, 0);
    assertThat(saved).isNotNull();
    assertThat(intent).isSameInstanceAs(shadowOf(saved).getSavedIntent());
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O)
  public void getForegroundService_withFlagNoCreate_shouldReturnNullIfNoPendingIntentExists() {
    Intent intent = new Intent();
    PendingIntent pendingIntent =
        PendingIntent.getForegroundService(context, 99, intent, FLAG_NO_CREATE);
    assertThat(pendingIntent).isNull();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O)
  public void getForegroundService_withFlagNoCreate_shouldReturnNullIfRequestCodeIsUnmatched() {
    Intent intent = new Intent();
    PendingIntent.getForegroundService(context, 99, intent, 0);
    assertThat(PendingIntent.getForegroundService(context, 98, intent, FLAG_NO_CREATE)).isNull();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O)
  public void getForegroundService_withFlagNoCreate_shouldReturnExistingIntent() {
    Intent intent = new Intent();
    PendingIntent.getForegroundService(context, 99, intent, 100);

    Intent identical = new Intent();
    PendingIntent saved =
        PendingIntent.getForegroundService(context, 99, identical, FLAG_NO_CREATE);
    assertThat(saved).isNotNull();
    assertThat(intent).isSameInstanceAs(shadowOf(saved).getSavedIntent());
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O)
  public void getForegroundService_withNoFlags_shouldReturnExistingIntent() {
    Intent intent = new Intent().setPackage("dummy.package");
    PendingIntent.getForegroundService(context, 99, intent, 100);

    Intent identical = new Intent().setPackage("dummy.package");
    PendingIntent saved = PendingIntent.getForegroundService(context, 99, identical, 0);
    assertThat(saved).isNotNull();
    assertThat(intent).isSameInstanceAs(shadowOf(saved).getSavedIntent());
  }

  @Test
  public void cancel_shouldRemovePendingIntentForBroadcast() {
    Intent intent = new Intent();
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 99, intent, 100);
    assertThat(pendingIntent).isNotNull();

    pendingIntent.cancel();
    assertThat(PendingIntent.getBroadcast(context, 99, intent, FLAG_NO_CREATE)).isNull();
  }

  @Test
  public void cancel_shouldRemovePendingIntentForActivity() {
    Intent intent = new Intent();
    PendingIntent pendingIntent = PendingIntent.getActivity(context, 99, intent, 100);
    assertThat(pendingIntent).isNotNull();

    pendingIntent.cancel();
    assertThat(PendingIntent.getActivity(context, 99, intent, FLAG_NO_CREATE)).isNull();
  }

  @Test
  public void cancel_shouldRemovePendingIntentForActivities() {
    Intent[] intents = {new Intent(Intent.ACTION_VIEW), new Intent(Intent.ACTION_PICK)};
    PendingIntent pendingIntent = PendingIntent.getActivities(context, 99, intents, 100);
    assertThat(pendingIntent).isNotNull();

    pendingIntent.cancel();
    assertThat(PendingIntent.getActivities(context, 99, intents, FLAG_NO_CREATE)).isNull();
  }

  @Test
  public void cancel_shouldRemovePendingIntentForService() {
    Intent intent = new Intent().setPackage("dummy.package");
    PendingIntent pendingIntent = PendingIntent.getService(context, 99, intent, 100);
    assertThat(pendingIntent).isNotNull();

    pendingIntent.cancel();
    assertThat(PendingIntent.getService(context, 99, intent, FLAG_NO_CREATE)).isNull();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.O)
  public void cancel_shouldRemovePendingIntentForForegroundService() {
    Intent intent = new Intent();
    PendingIntent pendingIntent = PendingIntent.getForegroundService(context, 99, intent, 100);
    assertThat(pendingIntent).isNotNull();

    pendingIntent.cancel();
    assertThat(PendingIntent.getForegroundService(context, 99, intent, FLAG_NO_CREATE)).isNull();
  }

  @Test
  public void send_canceledPendingIntent_throwsCanceledException() throws CanceledException {
    Intent intent = new Intent().setPackage("dummy.package");
    PendingIntent canceled = PendingIntent.getService(context, 99, intent, 100);
    assertThat(canceled).isNotNull();

    // Cancel the existing PendingIntent and create a new one in its place.
    PendingIntent current = PendingIntent.getService(context, 99, intent, FLAG_CANCEL_CURRENT);
    assertThat(current).isNotNull();

    assertThat(shadowOf(canceled).isCanceled()).isTrue();
    assertThat(shadowOf(current).isCanceled()).isFalse();

    // Sending the new PendingIntent should work as expected.
    current.send();

    // Sending the canceled PendingIntent should produce a CanceledException.
    try {
      canceled.send();
      fail("CanceledException expected when sending a canceled PendingIntent");
    } catch (CanceledException ignore) {
      // expected
    }
  }

  @Test
  public void send_oneShotPendingIntent_shouldCancel() throws CanceledException {
    Intent intent = new Intent().setPackage("dummy.package");
    PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, FLAG_ONE_SHOT);
    assertThat(shadowOf(pendingIntent).isCanceled()).isFalse();

    pendingIntent.send();
    assertThat(shadowOf(pendingIntent).isCanceled()).isTrue();
    assertThat(PendingIntent.getService(context, 0, intent, FLAG_ONE_SHOT | FLAG_NO_CREATE))
        .isNull();
  }

  @Test
  public void send_resultCode() throws CanceledException {
    AtomicInteger resultCode = new AtomicInteger(0);
    context.registerReceiver(
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            resultCode.set(getResultCode());
          }
        },
        new IntentFilter("foo"));

    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent("foo"), 0);
    pendingIntent.send(99);
    shadowOf(Looper.getMainLooper()).idle();
    assertThat(resultCode.get()).isEqualTo(99);
  }

  /** Verify options are sent along with the PendingIntent. */
  @Test
  @Config(minSdk = Build.VERSION_CODES.M)
  public void send_broadcastWithOptions() throws CanceledException {
    Intent intent = new Intent().setPackage("dummy.package");
    PendingIntent pendingIntent =
        PendingIntent.getBroadcast(context, /* requestCode= */ 0, intent, /* flags= */ 0);

    // Add an option for the PendingIntent broadcast.
    final String keyDontSendToRestrictedApps = "android:broadcast.dontSendToRestrictedApps";
    Bundle options = new Bundle();
    options.putBoolean(keyDontSendToRestrictedApps, true);

    // Send the pendingIntent with options.
    pendingIntent.send(
        context,
        /* code= */ 0,
        intent,
        /* onFinished= */ null,
        /* handler= */ null,
        /* requiredPermission= */ null,
        options);

    // Verify the options are used when sending the PendingIntent.
    ShadowApplication shadowApplication = shadowOf((Application) context);
    Bundle sendOptions = shadowApplication.getBroadcastOptions(intent);
    assertThat(sendOptions.getBoolean(keyDontSendToRestrictedApps)).isTrue();
  }

  @Test
  public void oneShotFlag_differentiatesPendingIntents() {
    Intent intent = new Intent().setPackage("dummy.package");
    PendingIntent oneShot = PendingIntent.getService(context, 0, intent, FLAG_ONE_SHOT);
    PendingIntent notOneShot = PendingIntent.getService(context, 0, intent, FLAG_UPDATE_CURRENT);
    assertThat(oneShot).isNotSameInstanceAs(notOneShot);
  }

  @Test
  public void immutableFlag_differentiatesPendingIntents() {
    Intent intent = new Intent().setPackage("dummy.package");
    PendingIntent immutable = PendingIntent.getService(context, 0, intent, FLAG_IMMUTABLE);
    PendingIntent notImmutable = PendingIntent.getService(context, 0, intent, FLAG_UPDATE_CURRENT);
    assertThat(immutable).isNotSameInstanceAs(notImmutable);
  }

  @Test
  public void testEquals() {
    PendingIntent pendingIntent =
        PendingIntent.getActivity(context, 99, new Intent("activity"), 100);

    // Same type, requestCode and Intent action implies equality.
    assertThat(PendingIntent.getActivity(context, 99, new Intent("activity"), FLAG_NO_CREATE))
        .isSameInstanceAs(pendingIntent);

    // Mismatched Intent action implies inequality.
    assertThat(PendingIntent.getActivity(context, 99, new Intent("activity2"), FLAG_NO_CREATE))
        .isNull();

    // Mismatched requestCode implies inequality.
    assertThat(PendingIntent.getActivity(context, 999, new Intent("activity"), FLAG_NO_CREATE))
        .isNull();

    // Mismatched types imply inequality.
    assertThat(PendingIntent.getBroadcast(context, 99, new Intent("activity"), FLAG_NO_CREATE))
        .isNull();
    assertThat(PendingIntent.getService(context, 99, new Intent("activity"), FLAG_NO_CREATE))
        .isNull();
  }

  @Test
  public void testEquals_getActivities() {
    Intent[] intents = {new Intent("activity"), new Intent("activity2")};
    PendingIntent pendingIntent = PendingIntent.getActivities(context, 99, intents, 100);

    Intent[] forward = {new Intent("activity"), new Intent("activity2")};
    assertThat(PendingIntent.getActivities(context, 99, forward, FLAG_NO_CREATE))
        .isSameInstanceAs(pendingIntent);

    Intent[] irrelevant = {new Intent("irrelevant"), new Intent("activity2")};
    assertThat(PendingIntent.getActivities(context, 99, irrelevant, FLAG_NO_CREATE))
        .isSameInstanceAs(pendingIntent);

    Intent single = new Intent("activity2");
    assertThat(PendingIntent.getActivity(context, 99, single, FLAG_NO_CREATE))
        .isSameInstanceAs(pendingIntent);

    Intent[] backward = {new Intent("activity2"), new Intent("activity")};
    assertThat(PendingIntent.getActivities(context, 99, backward, FLAG_NO_CREATE)).isNull();
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.JELLY_BEAN_MR1)
  public void testGetCreatorPackage_nothingSet() {
    PendingIntent pendingIntent =
        PendingIntent.getActivity(context, 99, new Intent("activity"), 100);
    assertThat(pendingIntent.getCreatorPackage()).isEqualTo(context.getPackageName());
    assertThat(pendingIntent.getTargetPackage()).isEqualTo(context.getPackageName());
  }

  @Test
  @Config(minSdk = Build.VERSION_CODES.JELLY_BEAN_MR1)
  public void testGetCreatorPackage_explicitlySetPackage() {
    String fakePackage = "some.fake.package";
    PendingIntent pendingIntent =
        PendingIntent.getActivity(context, 99, new Intent("activity"), 100);
    shadowOf(pendingIntent).setCreatorPackage(fakePackage);
    assertThat(pendingIntent.getCreatorPackage()).isEqualTo(fakePackage);
    assertThat(pendingIntent.getTargetPackage()).isEqualTo(fakePackage);
  }

  @Test
  public void testHashCode() {
    Context ctx = ApplicationProvider.getApplicationContext();
    PendingIntent pendingIntent1 = PendingIntent.getActivity(ctx, 99, new Intent("activity"), 100);

    assertThat(pendingIntent1.hashCode())
        .isEqualTo(PendingIntent.getActivity(ctx, 99, new Intent("activity"), 100).hashCode());

    assertThat(pendingIntent1.hashCode())
        .isNotEqualTo(PendingIntent.getActivity(ctx, 99, new Intent("activity2"), 100).hashCode());

    assertThat(pendingIntent1.hashCode())
        .isNotEqualTo(PendingIntent.getActivity(ctx, 999, new Intent("activity"), 100).hashCode());
  }

  @Test
  public void writeReadPendingIntentOrNullToParcel_returnsNull_whenWriteIsNull() {
    Parcel parcel = Parcel.obtain();
    PendingIntent.writePendingIntentOrNullToParcel(null, parcel);

    parcel.setDataPosition(0);
    PendingIntent result = PendingIntent.readPendingIntentOrNullFromParcel(parcel);

    assertThat(result).isNull();
  }

  @Test
  public void writeReadPendingIntentOrNullToParcel() {
    PendingIntent original = PendingIntent.getService(context, 100, new Intent(), 0);

    Parcel parcel = Parcel.obtain();
    PendingIntent.writePendingIntentOrNullToParcel(original, parcel);

    parcel.setDataPosition(0);
    PendingIntent result = PendingIntent.readPendingIntentOrNullFromParcel(parcel);

    assertThat(result).isEqualTo(original);
  }

  @Test
  public void testWriteToParcel() {
    Intent embedded = new Intent().setComponent(new ComponentName("pkg", "cls"));
    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, embedded, 0);
    Parcel parcel = Parcel.obtain();
    parcel.writeParcelable(pendingIntent, 0);
    parcel.setDataPosition(0);
    PendingIntent result = parcel.readParcelable(PendingIntent.class.getClassLoader());
    assertThat(result).isEqualTo(pendingIntent);
  }
}
