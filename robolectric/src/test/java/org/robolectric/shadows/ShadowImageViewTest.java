package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowImageViewTest {

  @Test
  public void getDrawableResourceId_shouldWorkWhenTheDrawableWasCreatedFromAResource() throws Exception {

    Resources resources = RuntimeEnvironment.application.getResources();
    Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.an_image);
    ImageView imageView = new ImageView(RuntimeEnvironment.application);
    imageView.setImageBitmap(bitmap);

    imageView.setImageResource(R.drawable.an_image);
    assertThat(shadowOf(imageView.getDrawable()).getCreatedFromResId()).isEqualTo(R.drawable.an_image);
  }
}
