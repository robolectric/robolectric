package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.widget.ImageView;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;

@RunWith(AndroidJUnit4.class)
public class ShadowImageViewTest {

  @Test
  public void getDrawableResourceId_shouldWorkWhenTheDrawableWasCreatedFromAResource() {
    Resources resources = ApplicationProvider.getApplicationContext().getResources();
    Bitmap bitmap = BitmapFactory.decodeResource(resources, R.drawable.an_image);
    ImageView imageView = new ImageView(ApplicationProvider.getApplicationContext());
    imageView.setImageBitmap(bitmap);

    imageView.setImageResource(R.drawable.an_image);
    assertThat(shadowOf(imageView.getDrawable()).getCreatedFromResId()).isEqualTo(R.drawable.an_image);
  }

  @Test
  public void imageView_draw_drawsToCanvasBitmap() {
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
    bitmap.eraseColor(Color.RED);
    ImageView imageView = new ImageView(ApplicationProvider.getApplicationContext());
    imageView.setImageBitmap(bitmap);
    Bitmap output = Bitmap.createBitmap(100, 100, Config.ARGB_8888);
    Canvas canvas = new Canvas(output);
    imageView.draw(canvas);
    assertThat(output.getPixel(0, 0)).isEqualTo(Color.RED);
  }
}
