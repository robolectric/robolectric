package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RecordingCanvas;
import android.graphics.Rect;
import android.graphics.RenderNode;
import android.view.View;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;
import org.robolectric.util.reflector.WithType;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = Q)
public class ShadowNativeRenderNodeTest {
  @Test
  public void testDefaults() {
    final RenderNode renderNode = new RenderNode(null);
    assertEquals(0, renderNode.getLeft());
    assertEquals(0, renderNode.getRight());
    assertEquals(0, renderNode.getTop());
    assertEquals(0, renderNode.getBottom());
    assertEquals(0, renderNode.getWidth());
    assertEquals(0, renderNode.getHeight());

    assertEquals(0, renderNode.getTranslationX(), 0.01f);
    assertEquals(0, renderNode.getTranslationY(), 0.01f);
    assertEquals(0, renderNode.getTranslationZ(), 0.01f);
    assertEquals(0, renderNode.getElevation(), 0.01f);

    assertEquals(0, renderNode.getRotationX(), 0.01f);
    assertEquals(0, renderNode.getRotationY(), 0.01f);
    assertEquals(0, renderNode.getRotationZ(), 0.01f);

    assertEquals(1, renderNode.getScaleX(), 0.01f);
    assertEquals(1, renderNode.getScaleY(), 0.01f);

    assertEquals(1, renderNode.getAlpha(), 0.01f);

    assertEquals(0, renderNode.getPivotX(), 0.01f);
    assertEquals(0, renderNode.getPivotY(), 0.01f);

    assertEquals(Color.BLACK, renderNode.getAmbientShadowColor());
    assertEquals(Color.BLACK, renderNode.getSpotShadowColor());

    assertEquals(8, renderNode.getCameraDistance(), 0.01f);

    assertTrue(renderNode.isForceDarkAllowed());
    assertTrue(renderNode.hasIdentityMatrix());
    assertTrue(renderNode.getClipToBounds());
    assertFalse(renderNode.getClipToOutline());
    assertFalse(renderNode.isPivotExplicitlySet());
    assertFalse(renderNode.hasDisplayList());
    assertFalse(renderNode.hasOverlappingRendering());
    assertFalse(renderNode.hasShadow());
    assertFalse(renderNode.getUseCompositingLayer());
  }

  @Test
  @Config(sdk = 31)
  public void testBasicDraw() {
    final Rect rect = new Rect(10, 10, 80, 80);

    final RenderNode renderNode = new RenderNode("Blue rect");
    assertTrue(renderNode.setPosition(rect.left, rect.top, rect.right, rect.bottom));
    assertEquals(rect.left, renderNode.getLeft());
    assertEquals(rect.top, renderNode.getTop());
    assertEquals(rect.right, renderNode.getRight());
    assertEquals(rect.bottom, renderNode.getBottom());
    renderNode.setClipToBounds(true);

    {
      Canvas canvas = renderNode.beginRecording();
      assertEquals(rect.width(), canvas.getWidth());
      assertEquals(rect.height(), canvas.getHeight());
      assertTrue(canvas.isHardwareAccelerated());
      canvas.drawColor(Color.BLUE);
      renderNode.endRecording();
    }

    assertTrue(renderNode.hasDisplayList());
    assertTrue(renderNode.hasIdentityMatrix());
  }

  @Test
  public void testTranslationGetSet() {
    final RenderNode renderNode = new RenderNode("translation");

    assertTrue(renderNode.hasIdentityMatrix());

    assertFalse(renderNode.setTranslationX(0.0f));
    assertFalse(renderNode.setTranslationY(0.0f));
    assertFalse(renderNode.setTranslationZ(0.0f));

    assertTrue(renderNode.hasIdentityMatrix());

    assertTrue(renderNode.setTranslationX(1.0f));
    assertEquals(1.0f, renderNode.getTranslationX(), 0.0f);
    assertTrue(renderNode.setTranslationY(1.0f));
    assertEquals(1.0f, renderNode.getTranslationY(), 0.0f);
    assertTrue(renderNode.setTranslationZ(1.0f));
    assertEquals(1.0f, renderNode.getTranslationZ(), 0.0f);

    assertFalse(renderNode.hasIdentityMatrix());

    assertTrue(renderNode.setTranslationX(0.0f));
    assertTrue(renderNode.setTranslationY(0.0f));
    assertTrue(renderNode.setTranslationZ(0.0f));

    assertTrue(renderNode.hasIdentityMatrix());
  }

  @Test
  public void testAlphaGetSet() {
    final RenderNode renderNode = new RenderNode("alpha");

    assertFalse(renderNode.setAlpha(1.0f));
    assertTrue(renderNode.setAlpha(.5f));
    assertEquals(.5f, renderNode.getAlpha(), 0.0001f);
    assertTrue(renderNode.setAlpha(1.0f));
  }

  @Test
  public void testRotationGetSet() {
    final RenderNode renderNode = new RenderNode("rotation");

    assertFalse(renderNode.setRotationX(0.0f));
    assertFalse(renderNode.setRotationY(0.0f));
    assertFalse(renderNode.setRotationZ(0.0f));
    assertTrue(renderNode.hasIdentityMatrix());

    assertTrue(renderNode.setRotationX(1.0f));
    assertEquals(1.0f, renderNode.getRotationX(), 0.0f);
    assertTrue(renderNode.setRotationY(1.0f));
    assertEquals(1.0f, renderNode.getRotationY(), 0.0f);
    assertTrue(renderNode.setRotationZ(1.0f));
    assertEquals(1.0f, renderNode.getRotationZ(), 0.0f);
    assertFalse(renderNode.hasIdentityMatrix());

    assertTrue(renderNode.setRotationX(0.0f));
    assertTrue(renderNode.setRotationY(0.0f));
    assertTrue(renderNode.setRotationZ(0.0f));
    assertTrue(renderNode.hasIdentityMatrix());
  }

  @Test
  public void testScaleGetSet() {
    final RenderNode renderNode = new RenderNode("scale");

    assertFalse(renderNode.setScaleX(1.0f));
    assertFalse(renderNode.setScaleY(1.0f));

    assertTrue(renderNode.setScaleX(2.0f));
    assertEquals(2.0f, renderNode.getScaleX(), 0.0f);
    assertTrue(renderNode.setScaleY(2.0f));
    assertEquals(2.0f, renderNode.getScaleY(), 0.0f);

    assertTrue(renderNode.setScaleX(1.0f));
    assertTrue(renderNode.setScaleY(1.0f));
  }

  @Test
  public void testStartEndRecordingEmpty() {
    final RenderNode renderNode = new RenderNode(null);
    assertEquals(0, renderNode.getWidth());
    assertEquals(0, renderNode.getHeight());
    RecordingCanvas canvas = renderNode.beginRecording();
    assertTrue(canvas.isHardwareAccelerated());
    assertEquals(0, canvas.getWidth());
    assertEquals(0, canvas.getHeight());
    renderNode.endRecording();
  }

  @Test
  public void testStartEndRecordingWithBounds() {
    final RenderNode renderNode = new RenderNode(null);
    renderNode.setPosition(10, 20, 30, 50);
    assertEquals(20, renderNode.getWidth());
    assertEquals(30, renderNode.getHeight());
    RecordingCanvas canvas = renderNode.beginRecording();
    assertTrue(canvas.isHardwareAccelerated());
    assertEquals(20, canvas.getWidth());
    assertEquals(30, canvas.getHeight());
    renderNode.endRecording();
  }

  @Test
  public void testStartEndRecordingEmptyWithSize() {
    final RenderNode renderNode = new RenderNode(null);
    assertEquals(0, renderNode.getWidth());
    assertEquals(0, renderNode.getHeight());
    RecordingCanvas canvas = renderNode.beginRecording(5, 10);
    assertTrue(canvas.isHardwareAccelerated());
    assertEquals(5, canvas.getWidth());
    assertEquals(10, canvas.getHeight());
    renderNode.endRecording();
  }

  @Test
  public void testStartEndRecordingWithBoundsWithSize() {
    final RenderNode renderNode = new RenderNode(null);
    renderNode.setPosition(10, 20, 30, 50);
    assertEquals(20, renderNode.getWidth());
    assertEquals(30, renderNode.getHeight());
    RecordingCanvas canvas = renderNode.beginRecording(5, 10);
    assertTrue(canvas.isHardwareAccelerated());
    assertEquals(5, canvas.getWidth());
    assertEquals(10, canvas.getHeight());
    renderNode.endRecording();
  }

  @Test
  public void testGetUniqueId() {
    final RenderNode r1 = new RenderNode(null);
    final RenderNode r2 = new RenderNode(null);
    assertNotEquals(r1.getUniqueId(), r2.getUniqueId());
    final Set<Long> usedIds = new HashSet<>();
    assertTrue(usedIds.add(r1.getUniqueId()));
    assertTrue(usedIds.add(r2.getUniqueId()));
    for (int i = 0; i < 100; i++) {
      assertTrue(usedIds.add(new RenderNode(null).getUniqueId()));
    }
  }

  @Test
  public void testInvalidCameraDistance() {
    final RenderNode renderNode = new RenderNode(null);
    assertThrows(IllegalArgumentException.class, () -> renderNode.setCameraDistance(-1f));
  }

  @Test
  public void testCameraDistanceSetGet() {
    final RenderNode renderNode = new RenderNode(null);
    renderNode.setCameraDistance(100f);
    assertEquals(100f, renderNode.getCameraDistance(), 0.0f);
  }

  @Test
  @Config(minSdk = O, maxSdk = P)
  public void testIsValid() {
    Object renderNode = reflector(RenderNodeOpReflector.class).create("name", null);
    RenderNodeOpReflector renderNodeOpReflector =
        reflector(RenderNodeOpReflector.class, renderNode);
    Object displayListCanvas = renderNodeOpReflector.start(100, 100);
    renderNodeOpReflector.end(displayListCanvas);
    assertThat(renderNodeOpReflector.isValid()).isTrue();
  }

  @ForType(className = "android.view.RenderNode")
  interface RenderNodeOpReflector {
    @Static
    Object create(String name, View owningView);

    Object start(int width, int height);

    void end(@WithType("android.view.DisplayListCanvas") Object displayListCanvas);

    boolean isValid();
  }
}
