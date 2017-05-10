package org.robolectric.shadows;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.MultiApiSelfTest.class)
public class ShadowPendingIntentTest {

  @Test
  public void getBroadcast_shouldCreateIntentForBroadcast() throws Exception {
    Intent intent = new Intent();
    PendingIntent pendingIntent = PendingIntent.getBroadcast(RuntimeEnvironment.application, 99, intent, 100);
    ShadowPendingIntent shadow = shadowOf(pendingIntent);
    assertThat(shadow.isActivityIntent()).isFalse();
    assertThat(shadow.isBroadcastIntent()).isTrue();
    assertThat(shadow.isServiceIntent()).isFalse();
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
    assertThat((Context) RuntimeEnvironment.application).isEqualTo(shadow.getSavedContext());
    assertThat(shadow.getRequestCode()).isEqualTo(99);
    assertThat(shadow.getFlags()).isEqualTo(100);
  }

  @Test
  public void getActivities_shouldCreateIntentForActivity() throws Exception {
    Intent[] intents = new Intent[] {new Intent(Intent.ACTION_VIEW), new Intent(Intent.ACTION_PICK)};
    PendingIntent pendingIntent = PendingIntent.getActivities(RuntimeEnvironment.application, 99, intents, 100);

    pendingIntent.send();
    ShadowApplication application = shadowOf(RuntimeEnvironment.application);
    assertThat(application.getNextStartedActivity().filterEquals(intents[0])).isTrue();
    assertThat(application.getNextStartedActivity().filterEquals(intents[1])).isTrue();
  }

  @Test
  public void getActivities_withBundle_shouldCreateIntentForActivity() throws Exception {
    Intent[] intents = new Intent[] {new Intent(Intent.ACTION_VIEW), new Intent(Intent.ACTION_PICK)};
    PendingIntent pendingIntent = PendingIntent.getActivities(RuntimeEnvironment.application, 99, intents, 100, new Bundle());

    pendingIntent.send();
    ShadowApplication application = shadowOf(RuntimeEnvironment.application);
    assertThat(application.getNextStartedActivity().filterEquals(intents[0])).isTrue();
    assertThat(application.getNextStartedActivity().filterEquals(intents[1])).isTrue();
  }

  @Test
  public void getService_shouldCreateIntentForService() throws Exception {
    Intent intent = new Intent();
    PendingIntent pendingIntent = PendingIntent.getService(RuntimeEnvironment.application, 99, intent, 100);
    ShadowPendingIntent shadow = shadowOf(pendingIntent);
    assertThat(shadow.isActivityIntent()).isFalse();
    assertThat(shadow.isBroadcastIntent()).isFalse();
    assertThat(shadow.isServiceIntent()).isTrue();
    assertThat((Context) RuntimeEnvironment.application).isEqualTo(shadow.getSavedContext());
    assertThat(shadow.getRequestCode()).isEqualTo(99);
    assertThat(shadow.getFlags()).isEqualTo(100);
  }

  @Test
  public void send_shouldFillInIntentData() throws Exception {
    Intent intent = new Intent();
    PendingIntent forActivity = PendingIntent.getActivity(RuntimeEnvironment.application, 99, intent, 100);

    Intent fillIntent = new Intent();
    fillIntent.putExtra("TEST", 23);
    forActivity.send(RuntimeEnvironment.application, 0, fillIntent);

    Intent i = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
    assertThat(i).isNotNull();
    assertThat(i.getIntExtra("TEST", -1)).isEqualTo(23);
  }

  @Test
  public void send_shouldBeCancelable() throws Exception {
    Intent intent = new Intent();
    PendingIntent forActivity = PendingIntent.getActivity(RuntimeEnvironment.application, 0, intent, 0);
    forActivity.cancel();
    try {
      forActivity.send();
      fail();
    } catch (PendingIntent.CanceledException e) {}

    assertThat(shadowOf(RuntimeEnvironment.application).getNextStartedActivity()).isNull();
  }

  @Test
  public void send_cannotChangeBaseIntent() throws Exception {
    Intent intent = new Intent("ACTION");
    PendingIntent forActivity = PendingIntent.getActivity(RuntimeEnvironment.application, 0, intent, 0);

    Intent fillIntent1 = new Intent();
    fillIntent1.putExtra("TEST1", 1);
    forActivity.send(RuntimeEnvironment.application, 0, fillIntent1);


    Intent fillIntent2 = new Intent();
    fillIntent2.putExtra("TEST2", 1);
    forActivity.send(RuntimeEnvironment.application, 0, fillIntent2);

    intent.setAction("INACTION");

    Intent i1 = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
    assertThat(i1).isNotNull();
    assertThat(i1.getAction()).isEqualTo("ACTION");
    assertThat(i1.hasExtra("TEST1")).isTrue();
    assertThat(i1.hasExtra("TEST2")).isFalse();

    Intent i2 = shadowOf(RuntimeEnvironment.application).getNextStartedActivity();
    assertThat(i2).isNotNull();
    assertThat(i2.getAction()).isEqualTo("ACTION");
    assertThat(i2.hasExtra("TEST1")).isFalse();
    assertThat(i2.hasExtra("TEST2")).isTrue();
  }


  @Test
  public void send_callOnFinished() throws Exception {
    Intent intent = new Intent();
    PendingIntent forBroadcast = PendingIntent.getBroadcast(RuntimeEnvironment.application, 99, intent, 0);

    PendingIntent.OnFinished onFinished = mock(PendingIntent.OnFinished.class);
    forBroadcast.send(101, onFinished, null);

    ArgumentCaptor<Intent> intentCaptor = ArgumentCaptor.forClass(Intent.class);
    verify(onFinished).onSendFinished(eq(forBroadcast), intentCaptor.capture(), eq(101), anyString(), any(Bundle.class));
    assertThat(intentCaptor.getValue().filterEquals(intent)).isTrue();
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
    PendingIntent saved = PendingIntent.getActivity(RuntimeEnvironment.application, 99, new Intent(), 100);
    PendingIntent identical = PendingIntent.getActivity(RuntimeEnvironment.application, 99, new Intent(), PendingIntent.FLAG_NO_CREATE);

    assertThat(saved).isNotNull();
    assertThat(identical).isEqualTo(saved);
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

    PendingIntent saved = PendingIntent.getActivities(RuntimeEnvironment.application, 99, intents, 100);

    Intent[] identicalIntents = new Intent[] { new Intent(Intent.ACTION_VIEW), new Intent(Intent.ACTION_PICK) };
    PendingIntent identical = PendingIntent.getActivities(RuntimeEnvironment.application, 99, identicalIntents,
        PendingIntent.FLAG_NO_CREATE);

    assertThat(saved).isNotNull();
    assertThat(identical).isEqualTo(saved);
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
    PendingIntent saved = PendingIntent.getBroadcast(RuntimeEnvironment.application, 99, new Intent(), 100);
    PendingIntent identical = PendingIntent.getBroadcast(RuntimeEnvironment.application, 99, new Intent(),
        PendingIntent.FLAG_NO_CREATE);

    assertThat(saved).isNotNull();
    assertThat(identical).isEqualTo(saved);
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
    PendingIntent saved = PendingIntent.getService(RuntimeEnvironment.application, 99, new Intent(), 100);
    PendingIntent identical = PendingIntent.getService(RuntimeEnvironment.application, 99, new Intent(), PendingIntent.FLAG_NO_CREATE);

    assertThat(saved).isNotNull();
    assertThat(identical).isEqualTo(saved);
  }

  @Test
  public void testEquals() {
    Context ctx = RuntimeEnvironment.application;
    PendingIntent pendingIntent1 = PendingIntent.getActivity(ctx, 99, new Intent("activity"), 100);

    assertThat(pendingIntent1)
        .isEqualTo(PendingIntent.getActivity(ctx, 99, new Intent("activity"), 100));

    assertThat(pendingIntent1)
        .isNotEqualTo(PendingIntent.getActivity(ctx, 99, new Intent("activity2"), 100));

    assertThat(pendingIntent1)
        .isNotEqualTo(PendingIntent.getActivity(ctx, 999, new Intent("activity"), 100));
  }

  @Test
  @Config(minSdk = 17)
  public void testGetCreatorPackage_nothingSet() {
    Context ctx = RuntimeEnvironment.application;
    PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 99, new Intent("activity"), 100);

    assertThat(pendingIntent.getCreatorPackage()).isEqualTo(ctx.getPackageName());
    assertThat(pendingIntent.getTargetPackage()).isEqualTo(ctx.getPackageName());
  }

  @Test
  @Config(minSdk = 17)
  public void testGetCreatorPackage_explicitlySetPackage() {
    String fakePackage = "some.fake.package";
    Context ctx = RuntimeEnvironment.application;
    PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 99, new Intent("activity"), 100);
    shadowOf(pendingIntent).setCreatorPackage(fakePackage);

    assertThat(pendingIntent.getCreatorPackage()).isEqualTo(fakePackage);
    assertThat(pendingIntent.getTargetPackage()).isEqualTo(fakePackage);
  }
}
