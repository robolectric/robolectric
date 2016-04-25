package org.robolectric.shadows;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowPendingIntentTest {

  @Test
  public void getBroadcast_shouldCreateIntentForBroadcast() throws Exception {
    Intent intent = new Intent();
    PendingIntent pendingIntent = PendingIntent.getBroadcast(RuntimeEnvironment.application, 99, intent, 100);
    ShadowPendingIntent shadow = shadowOf(pendingIntent);
    assertThat(shadow.isActivityIntent()).isFalse();
    assertThat(shadow.isBroadcastIntent()).isTrue();
    assertThat(shadow.isServiceIntent()).isFalse();
    assertThat(intent).isEqualTo(shadow.getSavedIntent());
    assertThat((Context) RuntimeEnvironment.application).isEqualTo(shadow.getSavedContext());
    assertThat(shadow.getRequestCode()).isEqualTo(99);
    assertThat(shadow.getFlags()).isEqualTo(100);
  }

  @Test
  public void getActivity_shouldCreateIntentForActivity() throws Exception {
    Intent intent = new Intent();
    PendingIntent pendingIntent = PendingIntent.getActivity(RuntimeEnvironment.application, 99, intent, 100);
    ShadowPendingIntent shadow = shadowOf(pendingIntent);
    assertThat(shadow.isActivityIntent()).isTrue();
    assertThat(shadow.isBroadcastIntent()).isFalse();
    assertThat(shadow.isServiceIntent()).isFalse();
    assertThat(intent).isEqualTo(shadow.getSavedIntent());
    assertThat((Context) RuntimeEnvironment.application).isEqualTo(shadow.getSavedContext());
    assertThat(shadow.getRequestCode()).isEqualTo(99);
    assertThat(shadow.getFlags()).isEqualTo(100);
  }

  @Test
  public void getActivities_shouldCreateIntentForActivities() throws Exception {
    Intent[] intents = new Intent[] {new Intent(Intent.ACTION_VIEW), new Intent(Intent.ACTION_PICK)};
    PendingIntent pendingIntent = PendingIntent.getActivities(RuntimeEnvironment.application, 99, intents, 100);

    ShadowPendingIntent shadow = shadowOf(pendingIntent);
    assertThat(shadow.getSavedIntents()).isEqualTo(intents);

    pendingIntent.send();
    ShadowApplication application = shadowOf(RuntimeEnvironment.application);
    assertThat(application.getNextStartedActivity()).isEqualTo(intents[0]);
    assertThat(application.getNextStartedActivity()).isEqualTo(intents[1]);
  }

  @Test
  public void getActivities_withBundle_shouldCreateIntentForActivities() throws Exception {
    Intent[] intents = new Intent[] {new Intent(Intent.ACTION_VIEW), new Intent(Intent.ACTION_PICK)};
    PendingIntent pendingIntent = PendingIntent.getActivities(RuntimeEnvironment.application, 99, intents, 100, new Bundle());

    ShadowPendingIntent shadow = shadowOf(pendingIntent);
    assertThat(shadow.getSavedIntents()).isEqualTo(intents);

    pendingIntent.send();
    ShadowApplication application = shadowOf(RuntimeEnvironment.application);
    assertThat(application.getNextStartedActivity()).isEqualTo(intents[0]);
    assertThat(application.getNextStartedActivity()).isEqualTo(intents[1]);
  }

  @Test
  public void getService_shouldCreateIntentForService() throws Exception {
    Intent intent = new Intent();
    PendingIntent pendingIntent = PendingIntent.getService(RuntimeEnvironment.application, 99, intent, 100);
    ShadowPendingIntent shadow = shadowOf(pendingIntent);
    assertThat(shadow.isActivityIntent()).isFalse();
    assertThat(shadow.isBroadcastIntent()).isFalse();
    assertThat(shadow.isServiceIntent()).isTrue();
    assertThat(intent).isEqualTo(shadow.getSavedIntent());
    assertThat((Context) RuntimeEnvironment.application).isEqualTo(shadow.getSavedContext());
    assertThat(shadow.getRequestCode()).isEqualTo(99);
    assertThat(shadow.getFlags()).isEqualTo(100);
  }

  @Test
  public void send_shouldFillInIntentData() throws Exception {
    Intent intent = new Intent();
    Activity context = new Activity();
    PendingIntent forActivity = PendingIntent.getActivity(context, 99, intent, 100);

    Activity otherContext = new Activity();
    Intent fillIntent = new Intent();
    fillIntent.putExtra("TEST", 23);
    forActivity.send(otherContext, 0, fillIntent);

    Intent i = shadowOf(otherContext).getNextStartedActivity();
    assertThat(i).isNotNull();
    assertThat(i).isSameAs(intent);
    assertThat(i.getIntExtra("TEST", -1)).isEqualTo(23);
  }

  @Test
  public void getActivity_withFlagNoCreate_shouldReturnNullIfNoPendingIntentExists() {
    Intent intent = new Intent();

    PendingIntent pendingIntent = PendingIntent.getActivity(RuntimeEnvironment.application, 99, intent,
        PendingIntent.FLAG_NO_CREATE);

    assertThat(pendingIntent).isNull();
  }

  @Test
  public void getActivity_withFlagNoCreate_shouldReturnExistingIntent() {
    Intent intent = new Intent();

    PendingIntent.getActivity(RuntimeEnvironment.application, 99, intent, 100);

    Intent identical = new Intent();
    PendingIntent saved = PendingIntent.getActivity(RuntimeEnvironment.application, 99, identical,
        PendingIntent.FLAG_NO_CREATE);

    assertThat(saved).isNotNull();
    assertThat(intent).isEqualTo(shadowOf(saved).getSavedIntent());
  }

  @Test
  public void getActivities_withFlagNoCreate_shouldReturnNullIfNoPendingIntentExists() {
    Intent[] intents = new Intent[] { new Intent(Intent.ACTION_VIEW), new Intent(Intent.ACTION_PICK) };

    PendingIntent pendingIntent = PendingIntent.getActivities(RuntimeEnvironment.application, 99, intents,
        PendingIntent.FLAG_NO_CREATE);

    assertThat(pendingIntent).isNull();
  }

  @Test
  public void getActivities_withFlagNoCreate_shouldReturnExistingIntent() {
    Intent[] intents = new Intent[] { new Intent(Intent.ACTION_VIEW), new Intent(Intent.ACTION_PICK) };

    PendingIntent.getActivities(RuntimeEnvironment.application, 99, intents, 100);

    Intent[] identicalIntents = new Intent[] { new Intent(Intent.ACTION_VIEW), new Intent(Intent.ACTION_PICK) };
    PendingIntent saved = PendingIntent.getActivities(RuntimeEnvironment.application, 99, identicalIntents,
        PendingIntent.FLAG_NO_CREATE);

    assertThat(saved).isNotNull();
    assertThat(intents).isEqualTo(shadowOf(saved).getSavedIntents());
  }

  @Test
  public void getBroadcast_withFlagNoCreate_shouldReturnNullIfNoPendingIntentExists() {
    Intent intent = new Intent();

    PendingIntent pendingIntent = PendingIntent.getBroadcast(RuntimeEnvironment.application, 99, intent,
        PendingIntent.FLAG_NO_CREATE);

    assertThat(pendingIntent).isNull();
  }

  @Test
  public void getBroadcast_withFlagNoCreate_shouldReturnExistingIntent() {
    Intent intent = new Intent();

    PendingIntent.getBroadcast(RuntimeEnvironment.application, 99, intent, 100);
    Intent identical = new Intent();
    PendingIntent saved = PendingIntent.getBroadcast(RuntimeEnvironment.application, 99, identical,
        PendingIntent.FLAG_NO_CREATE);

    assertThat(saved).isNotNull();
    assertThat(intent).isEqualTo(shadowOf(saved).getSavedIntent());
  }

  @Test
  public void getService_withFlagNoCreate_shouldReturnNullIfNoPendingIntentExists() {
    Intent intent = new Intent();

    PendingIntent pendingIntent = PendingIntent.getService(RuntimeEnvironment.application, 99, intent,
        PendingIntent.FLAG_NO_CREATE);

    assertThat(pendingIntent).isNull();
  }

  @Test
  public void getService_withFlagNoCreate_shouldReturnExistingIntent() {
    Intent intent = new Intent();

    PendingIntent.getService(RuntimeEnvironment.application, 99, intent, 100);

    Intent identical = new Intent();

    PendingIntent saved = PendingIntent.getService(RuntimeEnvironment.application, 99, identical,
        PendingIntent.FLAG_NO_CREATE);

    assertThat(saved).isNotNull();
    assertThat(intent).isEqualTo(shadowOf(saved).getSavedIntent());
  }

  @Test
  public void cancel_shouldCancelIntent() {
    Intent intent = new Intent();
    PendingIntent pendingIntent = PendingIntent.getService(RuntimeEnvironment.application, 99, intent, 0);

    pendingIntent.cancel();

    Intent identical = new Intent();
    PendingIntent saved = PendingIntent.getBroadcast(RuntimeEnvironment.application, 99, identical,
        PendingIntent.FLAG_NO_CREATE);

    assertThat(saved).isNull();
  }

  @Test
  public void getService_shouldReturnSameIntentAsGetService() {
    Intent serviceIntent1 = new Intent("some.action");
    PendingIntent servicePendingIntent1 = PendingIntent.getService(RuntimeEnvironment.application, 99,
        serviceIntent1, 0);

    Intent serviceIntent2 = new Intent("some.action");
    PendingIntent servicePendingIntent2 = PendingIntent.getService(RuntimeEnvironment.application, 99,
        serviceIntent2, 0);

    assertThat(shadowOf(servicePendingIntent1)).isEqualTo(shadowOf(servicePendingIntent2));
  }

  @Test
  public void getService_shouldReturnDifferentIntentThanGetBroadcast() {
    Intent serviceIntent = new Intent("some.action");
    PendingIntent servicePendingIntent = PendingIntent.getService(RuntimeEnvironment.application, 99,
        serviceIntent, 0);

    Intent broadcastIntent = new Intent("some.action");
    PendingIntent broadcastPendingIntent = PendingIntent.getBroadcast(RuntimeEnvironment.application, 99,
        broadcastIntent, 0);

    assertThat(shadowOf(servicePendingIntent)).isNotEqualTo(shadowOf(broadcastPendingIntent));
  }

  @Test
  public void getBroadcast_withFlagCancelCurrent_shouldReturnNewIntent() {
    Intent broadcastIntent = new Intent("some.action");
    PendingIntent pendingIntent = PendingIntent.getBroadcast(RuntimeEnvironment.application, 99, broadcastIntent, 0);

    Intent updatedIntent = new Intent("another.action");
    PendingIntent updatedPendingIntent = PendingIntent.getBroadcast(RuntimeEnvironment.application, 99, updatedIntent,
        PendingIntent.FLAG_CANCEL_CURRENT);

    assertThat(pendingIntent).isNotSameAs(updatedPendingIntent);
    assertThat(shadowOf(updatedPendingIntent).getSavedIntent().getAction()).isEqualTo("another.action");
  }

  @Test
  public void getBroadcast_withFlagUpdateCurrent_shouldUpdateExistingIntent() {
    Intent broadcastIntent = new Intent("some.action");
    PendingIntent pendingIntent = PendingIntent.getBroadcast(RuntimeEnvironment.application, 99, broadcastIntent, 0);

    Intent updateIntent = new Intent("another.action");
    PendingIntent updatedPendingIntent = PendingIntent.getBroadcast(RuntimeEnvironment.application, 99, updateIntent,
        PendingIntent.FLAG_UPDATE_CURRENT);

    assertThat(pendingIntent).isSameAs(updatedPendingIntent);
    assertThat(shadowOf(updatedPendingIntent).getSavedIntent().getAction()).isEqualTo("another.action");
  }
}
