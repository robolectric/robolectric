package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;

@RunWith(AndroidJUnit4.class)
public class ShadowImageViewTest {

  @Test
  public void getDrawableResourceId_shouldWorkWhenTheDrawableWasCreatedFromAResource() throws Exception {

    Resources resources = ApplicationProvider.getApplicationContext().getResources();
    Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.an_image);
    ImageView imageView = new ImageView(ApplicationProvider.getApplicationContext());
    imageView.setImageBitmap(bitmap);

    imageView.setImageResource(R.drawable.an_image);
    assertThat(shadowOf(imageView.getDrawable()).getCreatedFromResId()).isEqualTo(R.drawable.an_image);
  }
}
