package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.annotation.GraphicsMode.Mode;
import org.robolectric.junit.rules.SetSystemPropertyRule;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = TIRAMISU)
@GraphicsMode(Mode.NATIVE)
public class ShadowNativeHardwareRendererFrameCommitTest {

  @Rule public SetSystemPropertyRule setSystemPropertyRule = new SetSystemPropertyRule();

  @Before
  public void setUp() {
    setSystemPropertyRule.set("robolectric.useRealDrawTraversals", "true");
  }

  @Test
  public void frameCommitCallback_runsAfterNextFrame() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    View decorView = activity.getWindow().getDecorView();
    AtomicInteger commits = new AtomicInteger();

    decorView.getViewTreeObserver().registerFrameCommitCallback(commits::incrementAndGet);
    decorView.invalidate();
    ShadowLooper.idleMainLooper();

    assertThat(commits.get()).isEqualTo(1);
  }

  @Test
  public void frameCommitCallback_runsOnce() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    View decorView = activity.getWindow().getDecorView();
    AtomicInteger commits = new AtomicInteger();

    decorView.getViewTreeObserver().registerFrameCommitCallback(commits::incrementAndGet);
    decorView.invalidate();
    ShadowLooper.idleMainLooper();
    decorView.invalidate();
    ShadowLooper.idleMainLooper();

    assertThat(commits.get()).isEqualTo(1);
  }
}
