package org.robolectric.shadows;

import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.junit.Assert.*;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class LayerDrawableTest {
  /**
   * drawables
   */
  protected Drawable drawable1000;
  protected Drawable drawable2000;
  protected Drawable drawable3000;
  protected Drawable drawable4000;

  /**
   * drawables
   */
  protected Drawable[] drawables;

  @Before
  public void setUp() {
    drawable1000 = new BitmapDrawable(BitmapFactory.decodeResource(
        Robolectric.application.getResources(), 0x00001000));
    drawable2000 = new BitmapDrawable(BitmapFactory.decodeResource(
        Robolectric.application.getResources(), 0x00002000));
    drawable3000 = new BitmapDrawable(BitmapFactory.decodeResource(
        Robolectric.application.getResources(), 0x00003000));
    drawable4000 = new BitmapDrawable(BitmapFactory.decodeResource(
        Robolectric.application.getResources(), 0x00004000));

    drawables = new Drawable[]{drawable1000, drawable2000, drawable3000};
  }

  @Test
  public void testGetNumberOfLayers() {
    LayerDrawable layerDrawable = new LayerDrawable(drawables);
    assertEquals("count", 3, layerDrawable.getNumberOfLayers());
  }

  @Test
  public void testSetDrawableByLayerId1() throws Exception {
    LayerDrawable layerDrawable = new LayerDrawable(drawables);
    int index = 1;
    int layerId = 345;
    layerDrawable.setId(index, layerId);

    layerDrawable.setDrawableByLayerId(layerId, drawable4000);

    assertEquals(shadowOf(drawable4000).getCreatedFromResId(),
        shadowOf(layerDrawable.getDrawable(index)).getCreatedFromResId());
  }

  @Test
  public void testSetDrawableByLayerId2() throws Exception {
    LayerDrawable layerDrawable = new LayerDrawable(drawables);
    int index = 0;
    int layerId = 345;
    layerDrawable.setId(index, layerId);

    layerDrawable.setDrawableByLayerId(layerId, drawable4000);

    assertEquals(shadowOf(drawable4000).getCreatedFromResId(),
        shadowOf(layerDrawable.getDrawable(index)).getCreatedFromResId());
  }

  @Test
  public void setDrawableByLayerId_shouldReturnFalseIfIdNotFound() throws Exception {
    LayerDrawable layerDrawable = new LayerDrawable(drawables);
    boolean ret = layerDrawable.setDrawableByLayerId(123, drawable4000);
    assertFalse(ret);
  }

  @Test
  public void setDrawableByLayerId_shouldReturnTrueIfIdWasFound() throws Exception {
    LayerDrawable layerDrawable = new LayerDrawable(drawables);
    int index = 0;
    int layerId = 345;
    layerDrawable.setId(index, layerId);

    boolean ret = layerDrawable.setDrawableByLayerId(layerId, drawable4000);
    assertTrue(ret);
  }
}
