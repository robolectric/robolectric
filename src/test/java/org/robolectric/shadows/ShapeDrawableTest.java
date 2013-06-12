package org.robolectric.shadows;

import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(TestRunners.WithDefaults.class)
public class ShapeDrawableTest {
  @Test
  public void getPaint_ShouldReturnTheSamePaint() throws Exception {
    ShapeDrawable shapeDrawable = new ShapeDrawable();
    Paint paint = shapeDrawable.getPaint();
    assertNotNull(paint);
    assertThat(shapeDrawable.getPaint()).isSameAs(paint);
  }
}
