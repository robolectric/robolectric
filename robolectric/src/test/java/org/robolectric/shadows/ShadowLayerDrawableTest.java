package org.robolectric.shadows;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;

@RunWith(AndroidJUnit4.class)
public class ShadowLayerDrawableTest {
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
    Resources resources = ApplicationProvider.getApplicationContext().getResources();
    drawable1000 = new BitmapDrawable(BitmapFactory.decodeResource(resources, R.drawable.an_image));
    drawable2000 =
        new BitmapDrawable(BitmapFactory.decodeResource(resources, R.drawable.an_other_image));
    drawable3000 =
        new BitmapDrawable(BitmapFactory.decodeResource(resources, R.drawable.third_image));
    drawable4000 =
        new BitmapDrawable(BitmapFactory.decodeResource(resources, R.drawable.fourth_image));

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
