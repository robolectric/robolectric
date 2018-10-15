package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNotNull;

import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowShapeDrawableTest {
  @Test
  public void getPaint_ShouldReturnTheSamePaint() throws Exception {
    ShapeDrawable shapeDrawable = new ShapeDrawable();
    Paint paint = shapeDrawable.getPaint();
    assertNotNull(paint);
    assertThat(shapeDrawable.getPaint()).isSameAs(paint);
  }
}
