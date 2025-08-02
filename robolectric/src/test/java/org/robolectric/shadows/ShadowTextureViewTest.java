package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNull;

import android.app.Activity;
import android.graphics.RecordingCanvas;
import android.graphics.RenderNode;
import android.os.Bundle;
import android.view.TextureView;
import android.widget.FrameLayout;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = Q)
public final class ShadowTextureViewTest {
  @Test
  public void testTextureView_draw_doesNotCrash() {
    TestActivity activity = Robolectric.setupActivity(TestActivity.class);
    TextureView textureView = activity.textureView;

    RenderNode renderNode = new RenderNode("MyRenderNode");
    RecordingCanvas canvas = renderNode.beginRecording();
    assertThat(canvas.isHardwareAccelerated()).isTrue();
    assertNull(ReflectionHelpers.callInstanceMethod(textureView, "getTextureLayer"));

    try {
      textureView.draw(canvas);
    } finally {
      renderNode.endRecording();
    }
  }

  private static class TestActivity extends Activity {
    public TextureView textureView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      FrameLayout frameLayout = new FrameLayout(this);
      textureView = new TextureView(this);
      frameLayout.addView(textureView);
      setContentView(frameLayout);
    }
  }
}
