package org.robolectric.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import org.robolectric.shadows.CoreShadowsAdapter;
import org.robolectric.shadows.ShadowLooper;

@RunWith(TestRunners.WithDefaults.class)
public class ServiceControllerTest {
  private static final Transcript transcript = new Transcript();
  private final ComponentName componentName = new ComponentName("org.robolectric", MyService.class.getName());
  private final ServiceController<MyService> controller = Robolectric.buildService(MyService.class);

  @Before
  public void setUp() throws Exception {
    transcript.clear();
  }

  @Test
  public void onBindShouldSetIntent() throws Exception {
    MyService myService = controller.create().bind().get();
    assertThat(myService.boundIntent).isNotNull();
    assertThat(myService.boundIntent.getComponent()).isEqualTo(componentName);
  }

  @Test
  public void onStartCommandShouldSetIntentAndFlags() throws Exception {
    MyService myService = controller.create().startCommand(3, 4).get();
    assertThat(myService.startIntent).isNotNull();
    assertThat(myService.startIntent.getComponent()).isEqualTo(componentName);
    assertThat(myService.startFlags).isEqualTo(3);
    assertThat(myService.startId).isEqualTo(4);
  }

  @Test
  public void onBindShouldSetIntentComponentWithCustomIntentWithoutComponentSet() throws Exception {
    MyService myService = controller.withIntent(new Intent(Intent.ACTION_VIEW)).bind().get();
    assertThat(myService.boundIntent.getAction()).isEqualTo(Intent.ACTION_VIEW);
    assertThat(myService.boundIntent.getComponent()).isEqualTo(componentName);
  }

  @Test
  public void shouldSetIntentForGivenServiceInstance() throws Exception {
    ServiceController<MyService> serviceController = ServiceController.of(new CoreShadowsAdapter(), new MyService()).bind();
    assertThat(serviceController.get().boundIntent).isNotNull();
  }

  @Test
  public void whenLooperIsNotPaused_shouldCreateWithMainLooperPaused() throws Exception {
    ShadowLooper.unPauseMainLooper();
    controller.create();
    assertThat(shadowOf(Looper.getMainLooper()).isPaused()).isFalse();
    transcript.assertEventsInclude("finishedOnCreate", "onCreate");
  }

  @Test
  public void whenLooperIsAlreadyPaused_shouldCreateWithMainLooperPaused() throws Exception {
    ShadowLooper.pauseMainLooper();
    controller.create();
    assertThat(shadowOf(Looper.getMainLooper()).isPaused()).isTrue();
    transcript.assertEventsInclude("finishedOnCreate");

    ShadowLooper.unPauseMainLooper();
    transcript.assertEventsInclude("onCreate");
  }

  @Test
  public void unbind_callsUnbindWhilePaused() {
    controller.attach().create().bind().unbind();
    transcript.assertEventsInclude("finishedOnUnbind", "onUnbind");
  }

  @Test
  public void rebind_callsRebindWhilePaused() {
    controller.attach().create().bind().unbind().bind().rebind();
    transcript.assertEventsInclude("finishedOnRebind", "onRebind");
  }

  @Test
  public void destroy_callsOnDestroyWhilePaused() {
    controller.attach().create().destroy();
    transcript.assertEventsInclude("finishedOnDestroy", "onDestroy");
  }

  @Test
  public void bind_callsOnBindWhilePaused() {
    controller.attach().create().bind();
    transcript.assertEventsInclude("finishedOnBind", "onBind");
  }

  @Test
  public void startCommand_callsOnStartCommandWhilePaused() {
    controller.attach().create().startCommand(1, 2);
    transcript.assertEventsInclude("finishedOnStartCommand", "onStartCommand");
  }

  public static class MyService extends Service {

    private Handler handler = new Handler(Looper.getMainLooper());

    public Intent boundIntent;
    
    public Intent reboundIntent;
    public Intent startIntent;
    public int startFlags;
    public int startId;
    
    public Intent unboundIntent;
    
    @Override
    public IBinder onBind(Intent intent) {
      boundIntent = intent;
      transcribeWhilePaused("onBind");
      transcript.add("finishedOnBind");
      return null;
    }

    @Override
    public void onCreate() {
      super.onCreate();
      transcribeWhilePaused("onCreate");
      transcript.add("finishedOnCreate");
    }

    @Override
    public void onDestroy() {
      super.onDestroy();
      transcribeWhilePaused("onDestroy");
      transcript.add("finishedOnDestroy");
    }

    @Override
    public void onRebind(Intent intent) {
      reboundIntent = intent;
      transcribeWhilePaused("onRebind");
      transcript.add("finishedOnRebind");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
      startIntent = intent;
      startFlags = flags;
      this.startId = startId;
      transcribeWhilePaused("onStartCommand");
      transcript.add("finishedOnStartCommand");
      return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent) {
      unboundIntent = intent;
      transcribeWhilePaused("onUnbind");
      transcript.add("finishedOnUnbind");
      return false;
    }

    private void transcribeWhilePaused(final String event) {
      runOnUiThread(new Runnable() {
        @Override public void run() {
          transcript.add(event);
        }
      });
    }

    private void runOnUiThread(Runnable action) {
      // This is meant to emulate the behavior of Activity.runOnUiThread();
      shadowOf(handler.getLooper()).getScheduler().post(action);
    }
  }
}
