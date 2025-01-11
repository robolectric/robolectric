package org.robolectric.android.controller;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.LEGACY;
import static org.robolectric.shadows.ShadowLooper.shadowMainLooper;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowLooper;

@RunWith(AndroidJUnit4.class)
public class IntentServiceControllerTest {
  private static final List<String> transcript = new ArrayList<>();
  private final ComponentName componentName =
      new ComponentName("org.robolectric", MyService.class.getName());
  private final IntentServiceController<MyService> controller =
      Robolectric.buildIntentService(MyService.class, new Intent());

  @Before
  public void setUp() throws Exception {
    transcript.clear();
  }

  @Test
  public void onBindShouldSetIntent() {
    MyService myService = controller.create().bind().get();
    assertThat(myService.boundIntent).isNotNull();
    assertThat(myService.boundIntent.getComponent()).isEqualTo(componentName);
  }

  @Test
  public void onStartCommandShouldSetIntent() {
    MyService myService = controller.create().startCommand(3, 4).get();
    assertThat(myService.startIntent).isNotNull();
    assertThat(myService.startIntent.getComponent()).isEqualTo(componentName);
  }

  @Test
  public void onBindShouldSetIntentComponentWithCustomIntentWithoutComponentSet() {
    MyService myService =
        Robolectric.buildIntentService(MyService.class, new Intent(Intent.ACTION_VIEW))
            .bind()
            .get();
    assertThat(myService.boundIntent.getAction()).isEqualTo(Intent.ACTION_VIEW);
    assertThat(myService.boundIntent.getComponent()).isEqualTo(componentName);
  }

  @Test
  public void shouldSetIntentForGivenServiceInstance() {
    IntentServiceController<MyService> intentServiceController =
        IntentServiceController.of(new MyService(), null).bind();
    assertThat(intentServiceController.get().boundIntent).isNotNull();
  }

  @Test
  @LooperMode(LEGACY)
  public void whenLooperIsNotPaused_shouldCreateWithMainLooperPaused() {
    ShadowLooper.unPauseMainLooper();
    controller.create();
    assertThat(shadowOf(Looper.getMainLooper()).isPaused()).isFalse();
    assertThat(transcript).containsExactly("finishedOnCreate", "onCreate");
  }

  @Test
  public void whenLooperIsAlreadyPaused_shouldCreateWithMainLooperPaused() {
    shadowMainLooper().pause();
    controller.create();
    assertThat(transcript).contains("finishedOnCreate");

    shadowMainLooper().idle();
    assertThat(transcript).contains("onCreate");
  }

  @Test
  public void unbind_callsUnbindWhilePaused() {
    controller.create().bind().unbind();
    assertThat(transcript).containsAtLeast("finishedOnUnbind", "onUnbind");
  }

  @Test
  public void rebind_callsRebindWhilePaused() {
    controller.create().bind().unbind().bind().rebind();
    assertThat(transcript).containsAtLeast("finishedOnRebind", "onRebind");
  }

  @Test
  public void destroy_callsOnDestroyWhilePaused() {
    controller.create().destroy();
    assertThat(transcript).containsAtLeast("finishedOnDestroy", "onDestroy");
  }

  @Test
  public void bind_callsOnBindWhilePaused() {
    controller.create().bind();
    assertThat(transcript).containsAtLeast("finishedOnBind", "onBind");
  }

  @Test
  public void startCommand_callsOnHandleIntentWhilePaused() {
    controller.create().startCommand(1, 2);
    assertThat(transcript).containsAtLeast("finishedOnHandleIntent", "onHandleIntent");
  }

  public static class MyService extends IntentService {
    private final Handler handler = new Handler(Looper.getMainLooper());

    public Intent boundIntent;

    public Intent reboundIntent;
    public Intent startIntent;

    public Intent unboundIntent;

    public MyService() {
      super("ThreadName");
    }

    @Override
    public IBinder onBind(Intent intent) {
      boundIntent = intent;
      transcribeWhilePaused("onBind");
      transcript.add("finishedOnBind");
      return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
      startIntent = intent;
      transcribeWhilePaused("onHandleIntent");
      transcript.add("finishedOnHandleIntent");
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
    public boolean onUnbind(Intent intent) {
      unboundIntent = intent;
      transcribeWhilePaused("onUnbind");
      transcript.add("finishedOnUnbind");
      return false;
    }

    private void transcribeWhilePaused(final String event) {
      runOnUiThread(() -> transcript.add(event));
    }

    private void runOnUiThread(Runnable action) {
      // This is meant to emulate the behavior of Activity.runOnUiThread();
      handler.post(action);
    }
  }
}
