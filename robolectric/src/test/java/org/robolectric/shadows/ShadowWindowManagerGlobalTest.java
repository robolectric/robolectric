package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.content.ClipData;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import androidx.annotation.Nullable;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowWindowManagerGlobalTest {

  @Before
  public void setup() {
    System.setProperty("robolectric.areWindowsMarkedVisible", "true");
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void getWindowSession_shouldReturnSession() {
    assertThat(ShadowWindowManagerGlobal.getWindowSession()).isNotNull();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void getWindowSession_withLooper_shouldReturnSession() {
    // method not available in JELLY BEAN, sorry :(
    assertThat(ShadowWindowManagerGlobal.getWindowSession(Looper.getMainLooper())).isNotNull();
  }

  @Test
  public void getLastDragClipData() {
    MotionEvent downEvent = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 12f, 34f, 0);
    Robolectric.buildActivity(DragActivity.class)
        .setup()
        .get()
        .findViewById(android.R.id.content)
        .dispatchTouchEvent(downEvent);

    assertThat(ShadowWindowManagerGlobal.getLastDragClipData()).isNotNull();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void windowIsVisible() {
    View decorView =
        Robolectric.buildActivity(DragActivity.class).setup().get().getWindow().getDecorView();

    assertThat(decorView.getWindowVisibility()).isEqualTo(View.VISIBLE);
  }

  static final class DragActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle bundle) {
      super.onCreate(bundle);
      View contentView = new View(this);
      contentView.setOnTouchListener(
          (view, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
              ClipData clipData = ClipData.newPlainText("label", "text");
              DragShadowBuilder dragShadowBuilder = new DragShadowBuilder(view);
              if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.N) {
                view.startDragAndDrop(clipData, dragShadowBuilder, null, 0);
              } else {
                view.startDrag(clipData, dragShadowBuilder, null, 0);
              }
            }
            return true;
          });
      setContentView(contentView);
    }
  }
}
