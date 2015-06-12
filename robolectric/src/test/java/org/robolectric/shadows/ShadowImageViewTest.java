package org.robolectric.shadows;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.widget.ImageView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.TestRunners;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.RuntimeEnvironment.application;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowImageViewTest {
  private ImageView imageView;

  @Before
  public void setUp() throws Exception {
    Resources resources = RuntimeEnvironment.application.getResources();
    Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.an_image);
    imageView = new ImageView(RuntimeEnvironment.application);
    imageView.setImageBitmap(bitmap);
  }

  @Test
  public void shouldCopyMatrixSetup() throws Exception {
    Matrix matrix = imageView.getImageMatrix();
    matrix.setTranslate(15, 20);
    imageView.setImageMatrix(matrix);
    ShadowMatrix m1 = shadowOf(imageView.getImageMatrix());
    assertThat(m1.getSetOperations()).contains(entry("translate", "15.0 20.0"));

    matrix.setTranslate(30, 40);
    ShadowMatrix m2 = shadowOf(imageView.getImageMatrix());
    assertThat(m2.getSetOperations()).contains(entry("translate", "15.0 20.0"));

    imageView.setImageMatrix(matrix);
    ShadowMatrix m3 = shadowOf(imageView.getImageMatrix());
    assertThat(m3.getSetOperations()).contains(entry("translate", "30.0 40.0"));
  }

  @Test
  public void visualizeWithEmpty() throws Exception {
    assertEquals("", ShadowView.visualize(new ImageView(application)));
  }

  @Test
  public void testSetImageResource_drawable() {
    imageView.setImageResource(R.drawable.l0_red);
    assertTrue("Drawable", imageView.getDrawable() instanceof Drawable);
    assertFalse("LayerDrawable",
        imageView.getDrawable() instanceof LayerDrawable);
  }

  @Test
  public void testSetAnimatedImage_drawable() {
    imageView.setImageResource(R.anim.animation_list);
    Drawable animation = imageView.getDrawable();
    assertTrue(animation instanceof Drawable);
    assertTrue(animation instanceof AnimationDrawable);
  }

  @Test
  public void testSetImageResource_layerDrawable() {
    imageView.setImageResource(R.drawable.rainbow);
    assertTrue("Drawable", imageView.getDrawable() instanceof Drawable);
    assertTrue("LayerDrawable",
        imageView.getDrawable() instanceof LayerDrawable);
    assertThat(shadowOf(imageView.getDrawable()).getCreatedFromResId()).isEqualTo(R.drawable.rainbow);
  }

  @Test
  public void testSetImageResourceWithIdZeroDoesNothing() {
    imageView.setImageResource(0);
    assertThat(imageView.getDrawable()).isNull();
  }

  @Test
  public void testSetImageLevel() throws Exception {
    imageView.setImageLevel(2);
    assertThat(shadowOf(imageView).getImageLevel()).isEqualTo(2);
  }

  @Test
  public void testCallingLayoutOnParent(){
    imageView.layout(1,2,3,4);
    ShadowImageView shadowImageView = Shadows.shadowOf(imageView);
    assertTrue(shadowImageView.onLayoutWasCalled());
  }

  @Test
  public void getDrawableResourceId_shouldWorkWhenTheDrawableWasCreatedFromAResource() throws Exception {
    Drawable drawable = imageView.getResources().getDrawable(R.drawable.an_image);
    imageView.setImageDrawable(drawable);
    assertThat(shadowOf(imageView).getImageResourceId()).isEqualTo(R.drawable.an_image);
  }
}
