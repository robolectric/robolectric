package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;

import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import java.io.IOException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = O)
public class MediaStoreTest {

  @Test
  public void getBitmap_legacyAPI_alwaysReturnsABitmap() throws IOException {
    Uri screenshotUri = new Uri.Builder().scheme("content").appendPath("invalid_file").build();
    Bitmap bitmap =
        MediaStore.Images.Media.getBitmap(
            RuntimeEnvironment.getApplication().getContentResolver(), screenshotUri);
    assertThat(bitmap).isNotNull();
  }
}
