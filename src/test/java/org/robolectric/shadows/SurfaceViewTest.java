package org.robolectric.shadows;

import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.buildActivity;

@RunWith(TestRunners.WithDefaults.class)
public class SurfaceViewTest {

  private SurfaceHolder.Callback callback1 = new TestCallback();
  private SurfaceHolder.Callback callback2 = new TestCallback();

  private SurfaceView view = new SurfaceView(buildActivity(Activity.class).create().get());
  private SurfaceHolder surfaceHolder = view.getHolder();
  private ShadowSurfaceView shadowSurfaceView = (ShadowSurfaceView) Robolectric.shadowOf(view);
  private ShadowSurfaceView.FakeSurfaceHolder fakeSurfaceHolder =
      shadowSurfaceView.getFakeSurfaceHolder();

  @Test
  public void addCallback() {
    assertThat(fakeSurfaceHolder.getCallbacks()).isEmpty();

    surfaceHolder.addCallback(callback1);

    assertThat(fakeSurfaceHolder.getCallbacks()).contains(callback1);

    surfaceHolder.addCallback(callback2);

    assertThat(fakeSurfaceHolder.getCallbacks()).contains(callback1);
    assertThat(fakeSurfaceHolder.getCallbacks()).contains(callback2);
  }

  @Test
  public void removeCallback() {
    surfaceHolder.addCallback(callback1);
    surfaceHolder.addCallback(callback2);

    assertThat(fakeSurfaceHolder.getCallbacks().size()).isEqualTo(2);

    surfaceHolder.removeCallback(callback1);

    assertThat(fakeSurfaceHolder.getCallbacks()).doesNotContain(callback1);
    assertThat(fakeSurfaceHolder.getCallbacks()).contains(callback2);
  }

  @Test
  public void canCreateASurfaceView_attachedToAWindowWithActionBar() throws Exception {
    TestActivity testActivity = buildActivity(TestActivity.class).create().start().resume().visible().get();
    assertThat(testActivity).isNotNull();
  }

  private static class TestCallback implements SurfaceHolder.Callback {
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    }
  }

  private static class TestActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      requestWindowFeature(Window.FEATURE_ACTION_BAR);
      setContentView(new SurfaceView(this));
    }
  }
}
