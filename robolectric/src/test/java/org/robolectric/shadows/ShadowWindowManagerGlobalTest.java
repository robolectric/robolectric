package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.shadows.ShadowLooper.idleMainLooper;
import static org.robolectric.shadows.SystemUi.STANDARD_STATUS_BAR;
import static org.robolectric.shadows.SystemUi.THREE_BUTTON_NAVIGATION;
import static org.robolectric.shadows.SystemUi.systemUiForDefaultDisplay;

import android.app.Activity;
import android.content.ClipData;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import javax.annotation.Nullable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.annotation.GraphicsMode.Mode;
import org.robolectric.junit.rules.SetSystemPropertyRule;
import org.robolectric.shadows.SystemUi.NavigationBar;
import org.robolectric.shadows.SystemUi.StatusBar;

@RunWith(AndroidJUnit4.class)
@GraphicsMode(Mode.LEGACY)
public class ShadowWindowManagerGlobalTest {
  @Rule public SetSystemPropertyRule setSystemPropertyRule = new SetSystemPropertyRule();

  @Before
  public void setup() {
    setSystemPropertyRule.set("robolectric.areWindowsMarkedVisible", "true");
  }

  @Test
  public void getWindowSession_shouldReturnSession() {
    assertThat(ShadowWindowManagerGlobal.getWindowSession()).isNotNull();
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

  @Test
  public void windowInsets() {
    systemUiForDefaultDisplay().setBehavior(STANDARD_STATUS_BAR, THREE_BUTTON_NAVIGATION);
    ActivityController<WindowInsetsActivity> controller = buildActivity(WindowInsetsActivity.class);

    controller.setup();
    idleMainLooper();

    StatusBar statusBar = systemUiForDefaultDisplay().getStatusBar();
    NavigationBar navBar = systemUiForDefaultDisplay().getNavigationBar();
    assertThat(controller.get().systemInsets)
        .isEqualTo(new Rect(0, statusBar.getSize(), 0, navBar.getSize()));
    assertThat(controller.get().windowInsets).isNotNull();
  }

  @Config(minSdk = R)
  @Test
  public void windowInsetsController_hideStatusBar() {
    systemUiForDefaultDisplay().setBehavior(STANDARD_STATUS_BAR, THREE_BUTTON_NAVIGATION);
    ActivityController<WindowInsetsActivity> controller = buildActivity(WindowInsetsActivity.class);
    controller.setup();
    idleMainLooper();

    controller.get().getWindow().getInsetsController().hide(WindowInsets.Type.statusBars());
    idleMainLooper();

    assertThat(controller.get().windowInsets.getInsets(WindowInsets.Type.statusBars()).top)
        .isEqualTo(0);
    assertThat(controller.get().windowInsets.isVisible(WindowInsets.Type.statusBars())).isFalse();
    assertThat(controller.get().windowInsets.isVisible(WindowInsets.Type.navigationBars()))
        .isTrue();
  }

  @Config(minSdk = R)
  @Test
  public void windowInsetsController_hideSystemBars() {
    systemUiForDefaultDisplay().setBehavior(STANDARD_STATUS_BAR, THREE_BUTTON_NAVIGATION);
    ActivityController<WindowInsetsActivity> controller = buildActivity(WindowInsetsActivity.class);
    controller.setup();
    idleMainLooper();

    controller.get().getWindow().getInsetsController().hide(WindowInsets.Type.systemBars());
    idleMainLooper();

    assertThat(controller.get().windowInsets.isVisible(WindowInsets.Type.statusBars())).isFalse();
    assertThat(controller.get().windowInsets.isVisible(WindowInsets.Type.navigationBars()))
        .isFalse();
  }

  @Config(minSdk = S) // TODO(hoisie): investigate why this fails on R on GitHub CI.
  @Test
  public void windowInsetsController_toggleStatusBar() {
    ActivityController<WindowInsetsActivity> controller = buildActivity(WindowInsetsActivity.class);
    controller.setup();
    idleMainLooper();

    controller.get().getWindow().getInsetsController().hide(WindowInsets.Type.statusBars());
    idleMainLooper();
    controller.get().getWindow().getInsetsController().show(WindowInsets.Type.statusBars());
    idleMainLooper();

    assertThat(controller.get().windowInsets.isVisible(WindowInsets.Type.statusBars())).isTrue();
  }

  @Config(minSdk = R)
  @Test
  public void windowInsetsController_twoWindows_toggleStatusBar() {
    ActivityController<WindowInsetsActivity> controller = buildActivity(WindowInsetsActivity.class);
    controller.setup();
    idleMainLooper();
    ActivityController<WindowInsetsActivity> controller2 =
        buildActivity(WindowInsetsActivity.class);
    controller2.setup();
    idleMainLooper();

    controller2.get().getWindow().getInsetsController().hide(WindowInsets.Type.statusBars());
    idleMainLooper();

    assertThat(controller2.get().windowInsets.isVisible(WindowInsets.Type.statusBars())).isFalse();
  }

  public static final class WindowInsetsActivity extends Activity {
    Rect systemInsets;
    WindowInsets windowInsets;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
      setEdgeToEdge(getWindow());
      super.onCreate(savedInstanceState);
      setContentView(
          new View(this) {
            @Override
            public WindowInsets onApplyWindowInsets(WindowInsets insets) {
              windowInsets = insets;
              return super.onApplyWindowInsets(insets);
            }

            @Override
            protected boolean fitSystemWindows(Rect insets) {
              systemInsets = new Rect(insets);
              return super.fitSystemWindows(insets);
            }
          });
    }
  }

  // This sets similar properties to the androidx edgeToEdge API.
  private static void setEdgeToEdge(Window window) {
    if (RuntimeEnvironment.getApiLevel() <= Q) {
      window
          .getDecorView()
          .setSystemUiVisibility(
              window.getDecorView().getSystemUiVisibility()
                  | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                  | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                  | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    } else if (RuntimeEnvironment.getApiLevel() <= UPSIDE_DOWN_CAKE) {
      window
          .getDecorView()
          .setSystemUiVisibility(
              window.getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
      window.setDecorFitsSystemWindows(false);
    } else {
      window.setDecorFitsSystemWindows(false);
    }

    window.setStatusBarColor(Color.TRANSPARENT);
    window.setNavigationBarColor(Color.TRANSPARENT);

    if (RuntimeEnvironment.getApiLevel() > R) {
      window.getAttributes().layoutInDisplayCutoutMode =
          WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
    } else if (RuntimeEnvironment.getApiLevel() > P) {
      window.getAttributes().layoutInDisplayCutoutMode =
          WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
    }
  }
}
