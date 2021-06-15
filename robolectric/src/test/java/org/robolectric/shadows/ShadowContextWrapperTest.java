package org.robolectric.shadows;

import static android.content.pm.PackageManager.PERMISSION_DENIED;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.M;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.LEGACY;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.app.Activity;
import android.app.Application;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.UserHandle;
import android.view.LayoutInflater;
import android.view.View;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.truth.IterableSubject;
import com.google.common.util.concurrent.SettableFuture;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ConfigTestReceiver;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowActivity.IntentForResult;
import org.robolectric.shadows.ShadowApplication.Wrapper;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/** Tests {@link ShadowContextWrapper} */
@Config(manifest = "TestAndroidManifestWithReceivers.xml")
@RunWith(AndroidJUnit4.class)
public class ShadowContextWrapperTest {
  public ArrayList<String> transcript;
  private ContextWrapper contextWrapper;

  private final Context context = ApplicationProvider.getApplicationContext();
  private final ShadowContextWrapper shadowContextWrapper = Shadow.extract(context);

  @Before
  public void setUp() throws Exception {
    transcript = new ArrayList<>();
    contextWrapper = new ContextWrapper(context);
  }

  @Test
  public void sendBroadcast_shouldSendToManifestReceiver() throws Exception {
    ConfigTestReceiver receiver = getReceiverOfClass(ConfigTestReceiver.class);

    contextWrapper.sendBroadcast(new Intent(context, ConfigTestReceiver.class));
    ShadowLooper.shadowMainLooper().idle();

    assertThat(receiver.intentsReceived).hasSize(1);
  }

  @Test
  public void sendBroadcastWithData_shouldSendToManifestReceiver() throws Exception {
    ConfigTestReceiver receiver = getReceiverOfClass(ConfigTestReceiver.class);

    contextWrapper.sendBroadcast(
        new Intent(context, ConfigTestReceiver.class).setData(Uri.parse("http://google.com")));
    ShadowLooper.shadowMainLooper().idle();

    assertThat(receiver.intentsReceived).hasSize(1);
  }

  @Test
  public void registerReceiver_shouldRegisterForAllIntentFilterActions() throws Exception {
    BroadcastReceiver receiver = broadcastReceiver("Larry");
    contextWrapper.registerReceiver(receiver, intentFilter("foo", "baz"));

    contextWrapper.sendBroadcast(new Intent("foo"));
    asyncAssertThat(transcript).containsExactly("Larry notified of foo");
    transcript.clear();

    contextWrapper.sendBroadcast(new Intent("womp"));
    asyncAssertThat(transcript).isEmpty();

    contextWrapper.sendBroadcast(new Intent("baz"));
    asyncAssertThat(transcript).containsExactly("Larry notified of baz");
  }

  @Test
  public void sendBroadcast_shouldSendIntentToEveryInterestedReceiver() throws Exception {
    BroadcastReceiver larryReceiver = broadcastReceiver("Larry");
    contextWrapper.registerReceiver(larryReceiver, intentFilter("foo", "baz"));

    BroadcastReceiver bobReceiver = broadcastReceiver("Bob");
    contextWrapper.registerReceiver(bobReceiver, intentFilter("foo"));

    contextWrapper.sendBroadcast(new Intent("foo"));
    shadowMainLooper().idle();
    asyncAssertThat(transcript).containsExactly("Larry notified of foo", "Bob notified of foo");
    transcript.clear();

    contextWrapper.sendBroadcast(new Intent("womp"));
    shadowMainLooper().idle();
    asyncAssertThat(transcript).isEmpty();

    contextWrapper.sendBroadcast(new Intent("baz"));
    shadowMainLooper().idle();
    asyncAssertThat(transcript).containsExactly("Larry notified of baz");
  }

  @Test
  public void sendBroadcast_supportsLegacyExactPermissionMatch() {
    BroadcastReceiver receiver = broadcastReceiver("Larry");
    contextWrapper.registerReceiver(receiver, intentFilter("foo", "baz"), "validPermission", null);

    contextWrapper.sendBroadcast(new Intent("foo"));
    asyncAssertThat(transcript).isEmpty();

    contextWrapper.sendBroadcast(new Intent("foo"), null);
    asyncAssertThat(transcript).isEmpty();

    contextWrapper.sendBroadcast(new Intent("foo"), "wrongPermission");
    asyncAssertThat(transcript).isEmpty();

    contextWrapper.sendBroadcast(new Intent("foo"), "validPermission");
    asyncAssertThat(transcript).containsExactly("Larry notified of foo");
    transcript.clear();

    contextWrapper.sendBroadcast(new Intent("baz"), "validPermission");
    asyncAssertThat(transcript).containsExactly("Larry notified of baz");
  }

  @Test
  public void sendBroadcast_shouldOnlySendIntentWhenReceiverHasPermission() throws Exception {
    Context receiverWithPermission = contextWithPermission("larryPackage", "larryPermission");
    receiverWithPermission.registerReceiver(
        broadcastReceiver("Larry"),
        intentFilter("foo"),
        /* broadcastPermission= */ null,
        /* scheduler= */ null);

    Context receiverWithoutPermission = contextWithPermission("bobPackage", "bobPermission");
    receiverWithoutPermission.registerReceiver(
        broadcastReceiver("Bob"),
        intentFilter("foo"),
        /* broadcastPermission= */ null,
        /* scheduler= */ null);

    contextWrapper.sendBroadcast(new Intent("foo"), /*receiverPermission=*/ "larryPermission");

    asyncAssertThat(transcript).containsExactly("Larry notified of foo");
  }

  @Test
  public void sendBroadcast_shouldOnlySendIntentWhenBroadcasterHasPermission() throws Exception {
    contextWrapper.registerReceiver(
        broadcastReceiver("Larry"),
        intentFilter("foo"),
        /* broadcastPermission= */ "larryPermission",
        /* scheduler= */ null);

    contextWrapper.registerReceiver(
        broadcastReceiver("Bob"),
        intentFilter("foo"),
        /* broadcastPermission= */ "bobPermission",
        /* scheduler= */ null);

    Context broadcaster = contextWithPermission("broadcasterPackage", "larryPermission");
    broadcaster.sendBroadcast(new Intent("foo"), /*receiverPermission=*/ null);

    asyncAssertThat(transcript).containsExactly("Larry notified of foo");
  }

  private Context contextWithPermission(String packageName, String permission)
      throws NameNotFoundException {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = packageName;
    packageInfo.requestedPermissions = new String[] {permission};
    shadowOf(contextWrapper.getPackageManager()).installPackage(packageInfo);
    return contextWrapper.createPackageContext(packageInfo.packageName, 0);
  }

  @Test
  @LooperMode(LEGACY)
  public void sendBroadcast_shouldSendIntentUsingHandlerIfOneIsProvided_legacy() {
    HandlerThread handlerThread = new HandlerThread("test");
    handlerThread.start();

    Handler handler = new Handler(handlerThread.getLooper());
    assertNotSame(handler.getLooper(), Looper.getMainLooper());

    BroadcastReceiver receiver = broadcastReceiver("Larry");
    contextWrapper.registerReceiver(receiver, intentFilter("foo", "baz"), null, handler);

    assertThat(shadowOf(handler.getLooper()).getScheduler().size()).isEqualTo(0);
    contextWrapper.sendBroadcast(new Intent("foo"));
    assertThat(shadowOf(handler.getLooper()).getScheduler().size()).isEqualTo(1);
    shadowOf(handlerThread.getLooper()).idle();
    assertThat(shadowOf(handler.getLooper()).getScheduler().size()).isEqualTo(0);

    asyncAssertThat(transcript).containsExactly("Larry notified of foo");
  }

  @Test
  @LooperMode(PAUSED)
  public void sendBroadcast_shouldSendIntentUsingHandlerIfOneIsProvided()
      throws InterruptedException {
    HandlerThread handlerThread = new HandlerThread("test");
    handlerThread.start();

    Handler handler = new Handler(handlerThread.getLooper());
    assertNotSame(handler.getLooper(), Looper.getMainLooper());

    BroadcastReceiver receiver =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            transcript.add(
                "notified of "
                    + intent.getAction()
                    + " on thread "
                    + Thread.currentThread().getName());
          }
        };
    contextWrapper.registerReceiver(receiver, intentFilter("foo", "baz"), null, handler);

    assertThat(transcript).isEmpty();

    contextWrapper.sendBroadcast(new Intent("foo"));

    shadowOf(handlerThread.getLooper()).idle();
    assertThat(transcript).containsExactly("notified of foo on thread " + handlerThread.getName());

    handlerThread.quit();
  }

  @Test
  public void sendBroadcast_withClassSet_shouldSendIntentToSpecifiedReceiver() throws Exception {
    BroadcastReceiver larryReceiver =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            transcript.add("Larry notified of " + intent.getAction());
          }
        };
    contextWrapper.registerReceiver(larryReceiver, intentFilter("foo"));

    BroadcastReceiver bobReceiver = broadcastReceiver("Bob");
    contextWrapper.registerReceiver(bobReceiver, intentFilter("foo"));

    contextWrapper.sendBroadcast(
        new Intent("baz").setClass(contextWrapper, larryReceiver.getClass()));

    asyncAssertThat(transcript).containsExactly("Larry notified of baz");
  }

  @Test
  public void sendOrderedBroadcast_shouldReturnValues() throws Exception {
    String action = "test";

    IntentFilter lowFilter = new IntentFilter(action);
    lowFilter.setPriority(1);
    BroadcastReceiver lowReceiver = broadcastReceiver("Low");
    contextWrapper.registerReceiver(lowReceiver, lowFilter);

    IntentFilter highFilter = new IntentFilter(action);
    highFilter.setPriority(2);
    BroadcastReceiver highReceiver = broadcastReceiver("High");
    contextWrapper.registerReceiver(highReceiver, highFilter);

    final FooReceiver resultReceiver = new FooReceiver();
    contextWrapper.sendOrderedBroadcast(
        new Intent(action), null, resultReceiver, null, 1, "initial", null);
    asyncAssertThat(transcript).containsExactly("High notified of test", "Low notified of test");
    assertThat(resultReceiver.resultCode).isEqualTo(1);
  }

  @Test
  @Config(minSdk = KITKAT)
  public void sendOrderedBroadcastAsUser_shouldReturnValues() throws Exception {
    String action = "test";

    IntentFilter lowFilter = new IntentFilter(action);
    lowFilter.setPriority(1);
    BroadcastReceiver lowReceiver = broadcastReceiver("Low");
    contextWrapper.registerReceiver(lowReceiver, lowFilter);

    IntentFilter highFilter = new IntentFilter(action);
    highFilter.setPriority(2);
    BroadcastReceiver highReceiver = broadcastReceiver("High");
    contextWrapper.registerReceiver(highReceiver, highFilter);

    final FooReceiver resultReceiver = new FooReceiver();
    contextWrapper.sendOrderedBroadcastAsUser(
        new Intent(action), null, null, resultReceiver, null, 1, "initial", null);
    asyncAssertThat(transcript).containsExactly("High notified of test", "Low notified of test");
    assertThat(resultReceiver.resultCode).isEqualTo(1);
  }

  @Test
  @Config(minSdk = M)
  public void sendOrderedBroadcastAsUser_withAppOp_shouldReturnValues() throws Exception {
    String action = "test";

    IntentFilter lowFilter = new IntentFilter(action);
    lowFilter.setPriority(1);
    BroadcastReceiver lowReceiver = broadcastReceiver("Low");
    contextWrapper.registerReceiver(lowReceiver, lowFilter);

    IntentFilter highFilter = new IntentFilter(action);
    highFilter.setPriority(2);
    BroadcastReceiver highReceiver = broadcastReceiver("High");
    contextWrapper.registerReceiver(highReceiver, highFilter);

    final FooReceiver resultReceiver = new FooReceiver();

    ReflectionHelpers.callInstanceMethod(
        contextWrapper,
        "sendOrderedBroadcastAsUser",
        ClassParameter.from(Intent.class, new Intent(action)),
        ClassParameter.from(UserHandle.class, null),
        ClassParameter.from(String.class, null),
        ClassParameter.from(int.class, 1),
        ClassParameter.from(BroadcastReceiver.class, resultReceiver),
        ClassParameter.from(Handler.class, null),
        ClassParameter.from(int.class, 1),
        ClassParameter.from(String.class, "initial"),
        ClassParameter.from(Bundle.class, null));

    asyncAssertThat(transcript).containsExactly("High notified of test", "Low notified of test");
    assertThat(resultReceiver.resultCode).isEqualTo(1);
  }

  @Test
  @Config(minSdk = M)
  public void sendOrderedBroadcastAsUser_withAppOpAndOptions_shouldReturnValues() throws Exception {
    String action = "test";

    IntentFilter lowFilter = new IntentFilter(action);
    lowFilter.setPriority(1);
    BroadcastReceiver lowReceiver = broadcastReceiver("Low");
    contextWrapper.registerReceiver(lowReceiver, lowFilter);

    IntentFilter highFilter = new IntentFilter(action);
    highFilter.setPriority(2);
    BroadcastReceiver highReceiver = broadcastReceiver("High");
    contextWrapper.registerReceiver(highReceiver, highFilter);

    final FooReceiver resultReceiver = new FooReceiver();

    ReflectionHelpers.callInstanceMethod(
        contextWrapper,
        "sendOrderedBroadcastAsUser",
        ClassParameter.from(Intent.class, new Intent(action)),
        ClassParameter.from(UserHandle.class, null),
        ClassParameter.from(String.class, null),
        ClassParameter.from(int.class, 1),
        ClassParameter.from(Bundle.class, null),
        ClassParameter.from(BroadcastReceiver.class, resultReceiver),
        ClassParameter.from(Handler.class, null),
        ClassParameter.from(int.class, 1),
        ClassParameter.from(String.class, "initial"),
        ClassParameter.from(Bundle.class, null));

    asyncAssertThat(transcript).containsExactly("High notified of test", "Low notified of test");
    assertThat(resultReceiver.resultCode).isEqualTo(1);
  }

  private static final class FooReceiver extends BroadcastReceiver {
    private int resultCode;
    private SettableFuture<Void> settableFuture = SettableFuture.create();

    @Override
    public void onReceive(Context context, Intent intent) {
      resultCode = getResultCode();
      settableFuture.set(null);
    }
  }

  @Test
  public void sendOrderedBroadcast_shouldExecuteSerially() {
    String action = "test";
    AtomicReference<BroadcastReceiver.PendingResult> midResult = new AtomicReference<>();

    IntentFilter lowFilter = new IntentFilter(action);
    lowFilter.setPriority(1);
    BroadcastReceiver lowReceiver = broadcastReceiver("Low");
    contextWrapper.registerReceiver(lowReceiver, lowFilter);

    IntentFilter midFilter = new IntentFilter(action);
    midFilter.setPriority(2);
    AsyncReceiver midReceiver = new AsyncReceiver(midResult);
    contextWrapper.registerReceiver(midReceiver, midFilter);

    IntentFilter highFilter = new IntentFilter(action);
    highFilter.setPriority(3);
    BroadcastReceiver highReceiver = broadcastReceiver("High");
    contextWrapper.registerReceiver(highReceiver, highFilter);

    contextWrapper.sendOrderedBroadcast(new Intent(action), null);
    asyncAssertThat(transcript).containsExactly("High notified of test", "Mid notified of test");
    transcript.clear();
    assertThat(midResult.get()).isNotNull();
    midResult.get().finish();

    asyncAssertThat(transcript).containsExactly("Low notified of test");
  }

  private class AsyncReceiver extends BroadcastReceiver {
    private final AtomicReference<PendingResult> reference;

    private AsyncReceiver(AtomicReference<PendingResult> reference) {
      this.reference = reference;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      reference.set(goAsync());
      transcript.add("Mid notified of " + intent.getAction());
    }
  }

  @Test
  public void sendOrderedBroadcast_shouldSendByPriority() throws Exception {
    String action = "test";

    IntentFilter lowFilter = new IntentFilter(action);
    lowFilter.setPriority(1);
    BroadcastReceiver lowReceiver = broadcastReceiver("Low");
    contextWrapper.registerReceiver(lowReceiver, lowFilter);

    IntentFilter highFilter = new IntentFilter(action);
    highFilter.setPriority(2);
    BroadcastReceiver highReceiver = broadcastReceiver("High");
    contextWrapper.registerReceiver(highReceiver, highFilter);

    contextWrapper.sendOrderedBroadcast(new Intent(action), null);
    shadowMainLooper().idle();
    asyncAssertThat(transcript).containsExactly("High notified of test", "Low notified of test");
  }

  @Test
  public void orderedBroadcasts_shouldAbort() throws Exception {
    String action = "test";

    IntentFilter lowFilter = new IntentFilter(action);
    lowFilter.setPriority(1);
    BroadcastReceiver lowReceiver = broadcastReceiver("Low");
    contextWrapper.registerReceiver(lowReceiver, lowFilter);

    IntentFilter highFilter = new IntentFilter(action);
    highFilter.setPriority(2);
    BroadcastReceiver highReceiver =
        new BroadcastReceiver() {
          @Override
          public void onReceive(Context context, Intent intent) {
            transcript.add("High" + " notified of " + intent.getAction());
            abortBroadcast();
          }
        };
    contextWrapper.registerReceiver(highReceiver, highFilter);

    contextWrapper.sendOrderedBroadcast(new Intent(action), null);
    asyncAssertThat(transcript).containsExactly("High notified of test");
  }

  @Test
  public void unregisterReceiver_shouldUnregisterReceiver() throws Exception {
    BroadcastReceiver receiver = broadcastReceiver("Larry");

    contextWrapper.registerReceiver(receiver, intentFilter("foo", "baz"));
    contextWrapper.unregisterReceiver(receiver);

    contextWrapper.sendBroadcast(new Intent("foo"));
    asyncAssertThat(transcript).isEmpty();
  }

  @Test(expected = IllegalArgumentException.class)
  public void unregisterReceiver_shouldThrowExceptionWhenReceiverIsNotRegistered()
      throws Exception {
    contextWrapper.unregisterReceiver(new AppWidgetProvider());
  }

  @Test
  public void broadcastReceivers_shouldBeSharedAcrossContextsPerApplicationContext()
      throws Exception {
    BroadcastReceiver receiver = broadcastReceiver("Larry");

    Application application = ApplicationProvider.getApplicationContext();
    new ContextWrapper(application).registerReceiver(receiver, intentFilter("foo", "baz"));
    new ContextWrapper(application).sendBroadcast(new Intent("foo"));
    application.sendBroadcast(new Intent("baz"));
    asyncAssertThat(transcript).containsExactly("Larry notified of foo", "Larry notified of baz");

    new ContextWrapper(application).unregisterReceiver(receiver);
  }

  private static IterableSubject asyncAssertThat(ArrayList<String> transcript) {
    shadowMainLooper().idle();
    return assertThat(transcript);
  }

  @Test
  public void broadcasts_shouldBeLogged() {
    Intent broadcastIntent = new Intent("foo");
    contextWrapper.sendBroadcast(broadcastIntent);

    List<Intent> broadcastIntents = shadowOf(contextWrapper).getBroadcastIntents();
    assertTrue(broadcastIntents.size() == 1);
    assertEquals(broadcastIntent, broadcastIntents.get(0));
  }

  @Test
  public void clearBroadcastIntents_clearsBroadcastIntents() {
    Intent broadcastIntent = new Intent("foo");
    contextWrapper.sendBroadcast(broadcastIntent);

    assertThat(shadowOf(contextWrapper).getBroadcastIntents()).hasSize(1);

    shadowOf(contextWrapper).clearBroadcastIntents();

    assertThat(shadowOf(contextWrapper).getBroadcastIntents()).isEmpty();
  }

  @Test
  public void sendStickyBroadcast_shouldDeliverIntentToAllRegisteredReceivers() {
    BroadcastReceiver receiver = broadcastReceiver("Larry");
    contextWrapper.registerReceiver(receiver, intentFilter("foo", "baz"));

    contextWrapper.sendStickyBroadcast(new Intent("foo"));
    asyncAssertThat(transcript).containsExactly("Larry notified of foo");
    transcript.clear();

    contextWrapper.sendStickyBroadcast(new Intent("womp"));
    asyncAssertThat(transcript).isEmpty();

    contextWrapper.sendStickyBroadcast(new Intent("baz"));
    asyncAssertThat(transcript).containsExactly("Larry notified of baz");
  }

  @Test
  public void sendStickyBroadcast_shouldStickSentIntent() {
    contextWrapper.sendStickyBroadcast(new Intent("foo"));
    asyncAssertThat(transcript).isEmpty();

    BroadcastReceiver receiver = broadcastReceiver("Larry");
    Intent sticker = contextWrapper.registerReceiver(receiver, intentFilter("foo", "baz"));
    asyncAssertThat(transcript).containsExactly("Larry notified of foo");
    assertThat(sticker).isNotNull();
    assertThat(sticker.getAction()).isEqualTo("foo");
  }

  @Test
  public void afterSendStickyBroadcast_allSentIntentsShouldBeDeliveredToNewRegistrants() {
    contextWrapper.sendStickyBroadcast(new Intent("foo"));
    contextWrapper.sendStickyBroadcast(new Intent("baz"));
    asyncAssertThat(transcript).isEmpty();

    BroadcastReceiver receiver = broadcastReceiver("Larry");
    Intent sticker = contextWrapper.registerReceiver(receiver, intentFilter("foo", "baz"));
    asyncAssertThat(transcript).containsExactly("Larry notified of foo", "Larry notified of baz");

    /*
      Note: we do not strictly test what is returned by the method in this case
            because there no guaranties what particular Intent will be returned by Android system
    */
    assertThat(sticker).isNotNull();
  }

  @Test
  public void shouldReturnSameApplicationEveryTime() throws Exception {
    Activity activity = new Activity();
    assertThat(activity.getApplication()).isSameInstanceAs(activity.getApplication());

    assertThat(activity.getApplication()).isSameInstanceAs(new Activity().getApplication());
  }

  @Test
  public void shouldReturnSameApplicationContextEveryTime() throws Exception {
    Activity activity = Robolectric.setupActivity(Activity.class);
    assertThat(activity.getApplicationContext()).isSameInstanceAs(activity.getApplicationContext());

    assertThat(activity.getApplicationContext())
        .isSameInstanceAs(Robolectric.setupActivity(Activity.class).getApplicationContext());
  }

  @Test
  public void shouldReturnApplicationContext_forViewContextInflatedWithApplicationContext()
      throws Exception {
    View view =
        LayoutInflater.from(ApplicationProvider.getApplicationContext())
            .inflate(R.layout.custom_layout, null);
    Context viewContext = new ContextWrapper(view.getContext());
    assertThat(viewContext.getApplicationContext())
        .isEqualTo(ApplicationProvider.getApplicationContext());
  }

  @Test
  public void shouldReturnSameContentResolverEveryTime() throws Exception {
    Activity activity = Robolectric.setupActivity(Activity.class);
    assertThat(activity.getContentResolver()).isSameInstanceAs(activity.getContentResolver());

    assertThat(activity.getContentResolver())
        .isSameInstanceAs(Robolectric.setupActivity(Activity.class).getContentResolver());
  }

  @Test
  public void shouldReturnSameLocationManagerEveryTime() throws Exception {
    assertSameInstanceEveryTime(Context.LOCATION_SERVICE);
  }

  @Test
  public void shouldReturnSameWifiManagerEveryTime() throws Exception {
    assertSameInstanceEveryTime(Context.WIFI_SERVICE);
  }

  @Test
  public void shouldReturnSameAlarmServiceEveryTime() throws Exception {
    assertSameInstanceEveryTime(Context.ALARM_SERVICE);
  }

  @Test
  @Config(minSdk = 23)
  public void checkSelfPermission() {
    assertThat(contextWrapper.checkSelfPermission("MY_PERMISSON"))
        .isEqualTo(PackageManager.PERMISSION_DENIED);

    shadowContextWrapper.grantPermissions("MY_PERMISSON");

    assertThat(contextWrapper.checkSelfPermission("MY_PERMISSON"))
        .isEqualTo(PackageManager.PERMISSION_GRANTED);
    assertThat(contextWrapper.checkSelfPermission("UNKNOWN_PERMISSON"))
        .isEqualTo(PackageManager.PERMISSION_DENIED);
  }

  @Test
  public void checkPermissionUidPid() {
    assertThat(contextWrapper.checkPermission("MY_PERMISSON", 1, 1))
        .isEqualTo(PackageManager.PERMISSION_DENIED);

    shadowContextWrapper.grantPermissions(1, 1, "MY_PERMISSON");

    assertThat(contextWrapper.checkPermission("MY_PERMISSON", 2, 1))
        .isEqualTo(PackageManager.PERMISSION_DENIED);

    assertThat(contextWrapper.checkPermission("MY_PERMISSON", 1, 1))
        .isEqualTo(PackageManager.PERMISSION_GRANTED);
  }

  @Test
  @Config(minSdk = 23)
  public void checkAdditionalSelfPermission() {
    shadowContextWrapper.grantPermissions("MY_PERMISSON");
    assertThat(contextWrapper.checkSelfPermission("MY_PERMISSON"))
        .isEqualTo(PackageManager.PERMISSION_GRANTED);
    assertThat(contextWrapper.checkSelfPermission("ANOTHER_PERMISSON"))
        .isEqualTo(PackageManager.PERMISSION_DENIED);

    shadowContextWrapper.grantPermissions("ANOTHER_PERMISSON");
    assertThat(contextWrapper.checkSelfPermission("ANOTHER_PERMISSON"))
        .isEqualTo(PackageManager.PERMISSION_GRANTED);
  }

  @Test
  @Config(minSdk = 23)
  public void revokeSelfPermission() {
    shadowContextWrapper.grantPermissions("MY_PERMISSON");

    assertThat(contextWrapper.checkSelfPermission("MY_PERMISSON"))
        .isEqualTo(PackageManager.PERMISSION_GRANTED);
    shadowContextWrapper.denyPermissions("MY_PERMISSON");

    assertThat(contextWrapper.checkSelfPermission("MY_PERMISSON"))
        .isEqualTo(PackageManager.PERMISSION_DENIED);
  }

  @Test
  public void revokePermissionUidPid() {
    shadowContextWrapper.grantPermissions(1, 1, "MY_PERMISSON");

    assertThat(contextWrapper.checkPermission("MY_PERMISSON", 1, 1))
        .isEqualTo(PackageManager.PERMISSION_GRANTED);
    shadowContextWrapper.denyPermissions(1, 1, "MY_PERMISSON");

    assertThat(contextWrapper.checkPermission("MY_PERMISSON", 1, 1))
        .isEqualTo(PackageManager.PERMISSION_DENIED);
  }

  private void assertSameInstanceEveryTime(String serviceName) {
    Activity activity1 = buildActivity(Activity.class).create().get();
    Activity activity2 = buildActivity(Activity.class).create().get();
    assertThat(activity1.getSystemService(serviceName))
        .isSameInstanceAs(activity1.getSystemService(serviceName));
    assertThat(activity1.getSystemService(serviceName))
        .isSameInstanceAs(activity2.getSystemService(serviceName));
  }

  @Test
  public void bindServiceDelegatesToShadowApplication() {
    contextWrapper.bindService(
        new Intent("foo").setPackage("dummy.package"), new TestService(), Context.BIND_AUTO_CREATE);
    assertEquals(
        "foo",
        shadowOf((Application) ApplicationProvider.getApplicationContext())
            .getNextStartedService()
            .getAction());
  }

  @Test
  public void startActivities_shouldStartAllActivities() {
    final Intent view = new Intent(Intent.ACTION_VIEW).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    final Intent pick = new Intent(Intent.ACTION_PICK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    contextWrapper.startActivities(new Intent[] {view, pick});

    assertThat(shadowOf(RuntimeEnvironment.getApplication()).getNextStartedActivity())
        .isEqualTo(pick);
    assertThat(shadowOf(RuntimeEnvironment.getApplication()).getNextStartedActivity())
        .isEqualTo(view);
  }

  @Test
  public void startActivities_withBundle_shouldStartAllActivities() {
    final Intent view = new Intent(Intent.ACTION_VIEW).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    final Intent pick = new Intent(Intent.ACTION_PICK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    contextWrapper.startActivities(new Intent[] {view, pick}, new Bundle());

    assertThat(shadowOf(RuntimeEnvironment.getApplication()).getNextStartedActivity())
        .isEqualTo(pick);
    assertThat(shadowOf(RuntimeEnvironment.getApplication()).getNextStartedActivity())
        .isEqualTo(view);
  }

  @Test
  public void startActivities_canGetNextStartedActivityForResult() {
    final Intent view = new Intent(Intent.ACTION_VIEW).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    final Intent pick = new Intent(Intent.ACTION_PICK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    contextWrapper.startActivities(new Intent[] {view, pick});

    IntentForResult second =
        shadowOf(RuntimeEnvironment.getApplication()).getNextStartedActivityForResult();
    IntentForResult first =
        shadowOf(RuntimeEnvironment.getApplication()).getNextStartedActivityForResult();

    assertThat(second.intent).isEqualTo(pick);
    assertThat(second.options).isNull();

    assertThat(first.intent).isEqualTo(view);
    assertThat(first.options).isNull();
  }

  @Test
  public void startActivities_withBundle_canGetNextStartedActivityForResult() {
    final Intent view = new Intent(Intent.ACTION_VIEW).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    final Intent pick = new Intent(Intent.ACTION_PICK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    Bundle options = new Bundle();
    options.putString("foo", "bar");
    contextWrapper.startActivities(new Intent[] {view, pick}, options);

    IntentForResult second =
        shadowOf(RuntimeEnvironment.getApplication()).getNextStartedActivityForResult();
    IntentForResult first =
        shadowOf(RuntimeEnvironment.getApplication()).getNextStartedActivityForResult();

    assertThat(second.intent).isEqualTo(pick);
    assertThat(second.options).isEqualTo(options);

    assertThat(first.intent).isEqualTo(view);
    assertThat(first.options).isEqualTo(options);
  }

  private BroadcastReceiver broadcastReceiver(final String name) {
    return new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        transcript.add(name + " notified of " + intent.getAction());
      }
    };
  }

  private IntentFilter intentFilter(String... actions) {
    IntentFilter larryIntentFilter = new IntentFilter();
    for (String action : actions) {
      larryIntentFilter.addAction(action);
    }
    return larryIntentFilter;
  }

  private <T> T getReceiverOfClass(Class<T> receiverClass) {
    ShadowApplication app = shadowOf((Application) context);
    List<Wrapper> receivers = app.getRegisteredReceivers();
    for (Wrapper wrapper : receivers) {
      if (receiverClass.isInstance(wrapper.getBroadcastReceiver())) {
        return receiverClass.cast(wrapper.getBroadcastReceiver());
      }
    }

    return null;
  }

  @Test
  public void packageManagerShouldNotBeNullWhenWrappingAnApplication() {
    assertThat(ApplicationProvider.getApplicationContext().getPackageManager()).isNotNull();
  }

  @Test
  public void checkCallingPermissionsShouldReturnPermissionGrantedToAddedPermissions()
      throws Exception {
    shadowOf(contextWrapper).grantPermissions("foo", "bar");
    assertThat(contextWrapper.checkCallingPermission("foo")).isEqualTo(PERMISSION_GRANTED);
    assertThat(contextWrapper.checkCallingPermission("bar")).isEqualTo(PERMISSION_GRANTED);
    assertThat(contextWrapper.checkCallingPermission("baz")).isEqualTo(PERMISSION_DENIED);
  }

  @Test
  public void checkCallingOrSelfPermissionsShouldReturnPermissionGrantedToAddedPermissions()
      throws Exception {
    shadowOf(contextWrapper).grantPermissions("foo", "bar");
    assertThat(contextWrapper.checkCallingOrSelfPermission("foo")).isEqualTo(PERMISSION_GRANTED);
    assertThat(contextWrapper.checkCallingOrSelfPermission("bar")).isEqualTo(PERMISSION_GRANTED);
    assertThat(contextWrapper.checkCallingOrSelfPermission("baz")).isEqualTo(PERMISSION_DENIED);
  }

  @Test
  public void checkCallingPermission_shouldReturnPermissionDeniedForRemovedPermissions()
      throws Exception {
    shadowOf(contextWrapper).grantPermissions("foo", "bar");
    shadowOf(contextWrapper).denyPermissions("foo", "qux");
    assertThat(contextWrapper.checkCallingPermission("foo")).isEqualTo(PERMISSION_DENIED);
    assertThat(contextWrapper.checkCallingPermission("bar")).isEqualTo(PERMISSION_GRANTED);
    assertThat(contextWrapper.checkCallingPermission("baz")).isEqualTo(PERMISSION_DENIED);
    assertThat(contextWrapper.checkCallingPermission("qux")).isEqualTo(PERMISSION_DENIED);
  }

  @Test
  public void checkCallingOrSelfPermission_shouldReturnPermissionDeniedForRemovedPermissions()
      throws Exception {
    shadowOf(contextWrapper).grantPermissions("foo", "bar");
    shadowOf(contextWrapper).denyPermissions("foo", "qux");
    assertThat(contextWrapper.checkCallingOrSelfPermission("foo")).isEqualTo(PERMISSION_DENIED);
    assertThat(contextWrapper.checkCallingOrSelfPermission("bar")).isEqualTo(PERMISSION_GRANTED);
    assertThat(contextWrapper.checkCallingOrSelfPermission("baz")).isEqualTo(PERMISSION_DENIED);
    assertThat(contextWrapper.checkCallingOrSelfPermission("qux")).isEqualTo(PERMISSION_DENIED);
  }

  @Test
  public void getSharedPreferencesShouldReturnSameInstanceWhenSameNameIsSupplied() {
    final SharedPreferences pref1 =
        contextWrapper.getSharedPreferences("pref", Context.MODE_PRIVATE);
    final SharedPreferences pref2 =
        contextWrapper.getSharedPreferences("pref", Context.MODE_PRIVATE);

    assertThat(pref1).isSameInstanceAs(pref2);
  }

  @Test
  public void getSharedPreferencesShouldReturnDifferentInstancesWhenDifferentNameIsSupplied() {
    final SharedPreferences pref1 =
        contextWrapper.getSharedPreferences("pref1", Context.MODE_PRIVATE);
    final SharedPreferences pref2 =
        contextWrapper.getSharedPreferences("pref2", Context.MODE_PRIVATE);

    assertThat(pref1).isNotSameInstanceAs(pref2);
  }

  @Test
  public void sendBroadcast_shouldOnlySendIntentWithTypeWhenReceiverMatchesType()
      throws IntentFilter.MalformedMimeTypeException {

    final BroadcastReceiver viewAllTypesReceiver =
        broadcastReceiver("ViewActionWithAnyTypeReceiver");
    final IntentFilter allTypesIntentFilter = intentFilter("view");
    allTypesIntentFilter.addDataType("*/*");
    contextWrapper.registerReceiver(viewAllTypesReceiver, allTypesIntentFilter);

    final BroadcastReceiver imageReceiver = broadcastReceiver("ImageReceiver");
    final IntentFilter imageIntentFilter = intentFilter("view");
    imageIntentFilter.addDataType("img/*");
    contextWrapper.registerReceiver(imageReceiver, imageIntentFilter);

    final BroadcastReceiver videoReceiver = broadcastReceiver("VideoReceiver");
    final IntentFilter videoIntentFilter = intentFilter("view");
    videoIntentFilter.addDataType("video/*");
    contextWrapper.registerReceiver(videoReceiver, videoIntentFilter);

    final BroadcastReceiver viewReceiver = broadcastReceiver("ViewActionReceiver");
    final IntentFilter viewIntentFilter = intentFilter("view");
    contextWrapper.registerReceiver(viewReceiver, viewIntentFilter);

    final Intent imageIntent = new Intent("view");
    imageIntent.setType("img/jpeg");
    contextWrapper.sendBroadcast(imageIntent);

    final Intent videoIntent = new Intent("view");
    videoIntent.setType("video/mp4");
    contextWrapper.sendBroadcast(videoIntent);

    asyncAssertThat(transcript)
        .containsExactly(
            "ViewActionWithAnyTypeReceiver notified of view",
            "ImageReceiver notified of view",
            "ViewActionWithAnyTypeReceiver notified of view",
            "VideoReceiver notified of view");
  }

  @Test
  public void getApplicationInfo_shouldReturnApplicationInfoForApplicationPackage() {
    final ApplicationInfo info = contextWrapper.getApplicationInfo();
    assertThat(info.packageName).isEqualTo("org.robolectric");
  }

  @Test
  public void removeSystemService_getSystemServiceReturnsNull() {
    shadowContextWrapper.removeSystemService(Context.WALLPAPER_SERVICE);
    assertThat(context.getSystemService(Context.WALLPAPER_SERVICE)).isNull();
  }
}
