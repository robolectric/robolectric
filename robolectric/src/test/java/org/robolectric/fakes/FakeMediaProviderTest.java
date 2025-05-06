package org.robolectric.fakes;

import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.OutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = Q)
public final class FakeMediaProviderTest {

  private final Context context = ApplicationProvider.getApplicationContext();

  @Test
  public void getExternalImageCount_zeroWhenNoImages() {
    assertThat(getExternalImageCount(context)).isEqualTo(0);
  }

  @Test
  public void saveImageToMediaStore_savesImage() throws Exception {
    Uri savedImageUri = saveImageToMediaStore(context, "Test.jpg");
    assertThat(savedImageUri).isNotNull();
    assertThat(getExternalImageCount(context)).isEqualTo(1);
  }

  private static int getExternalImageCount(Context context) {
    ContentResolver contentResolver = context.getContentResolver();
    Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    String[] projection = {MediaStore.Images.Media._ID};
    try (Cursor cursor = contentResolver.query(queryUri, projection, null, null, null)) {
      if (cursor == null) {
        return -1;
      }
      return cursor.getCount();
    }
  }

  public static Uri saveImageToMediaStore(Context context, String displayName) throws Exception {
    ContentResolver resolver = context.getContentResolver();
    ContentValues contentValues = new ContentValues();
    contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, displayName);
    contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
    contentValues.put(
        MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/test");
    contentValues.put(MediaStore.Images.Media.IS_PENDING, 1);
    Uri imageCollectionUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    Uri imageUri = resolver.insert(imageCollectionUri, contentValues);
    assertThat(imageUri).isNotNull();
    OutputStream outputStream = resolver.openOutputStream(imageUri);
    assertThat(outputStream).isNotNull();
    Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
    assertThat(bitmap.compress(Bitmap.CompressFormat.JPEG, /* quality= */ 95, outputStream))
        .isTrue();
    outputStream.close();
    return imageUri;
  }
}
