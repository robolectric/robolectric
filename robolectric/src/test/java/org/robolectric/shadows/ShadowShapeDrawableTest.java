package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowShapeDrawableTest {
  @Test
  public void getPaint_ShouldReturnTheSamePaint() throws Exception {
    ShapeDrawable shapeDrawable = new ShapeDrawable();
    Paint paint = shapeDrawable.getPaint();
    assertNotNull(paint);
    assertThat(shapeDrawable.getPaint()).isSameAs(paint);
  }
}
