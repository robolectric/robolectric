package org.robolectric.fakes;

import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.TruthJUnit.assume;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import java.io.OutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = Q)
public final class FakeMediaProviderTest {
  private static final Uri QUERY_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
  private static final String[] projection = {MediaStore.Images.Media._ID};

  @Test
  public void query_empty() {
    assertThat(queryImages(null)).isEmpty();
  }

  @Test
  public void query_afterInsert() throws Exception {
    Uri savedImageUri = saveImageToMediaStore();

    assertThat(savedImageUri).isNotNull();
    assertThat(queryImages(null)).containsExactly(savedImageUri);
  }

  @Test
  public void query_withSort() throws Exception {
    Uri image1 = saveImageToMediaStore();
    Uri image2 = saveImageToMediaStore();
    Uri image3 = saveImageToMediaStore();

    assertThat(queryImagesSorted(MediaStore.Images.Media._ID + " DESC"))
        .containsExactly(image3, image2, image1)
        .inOrder();
  }

  @Test
  public void query_withOffsetAndLimit() throws Exception {
    // For some reason, "1 OFFSET 1" is an invalid limit clause on API 29 only.
    assume().that(Build.VERSION.SDK_INT).isNotEqualTo(29);
    Bundle queryArgs = new Bundle();
    queryArgs.putStringArray(
        ContentResolver.QUERY_ARG_SORT_COLUMNS, new String[] {MediaStore.Images.Media._ID});
    queryArgs.putInt(
        ContentResolver.QUERY_ARG_SORT_DIRECTION, ContentResolver.QUERY_SORT_DIRECTION_DESCENDING);
    queryArgs.putInt(ContentResolver.QUERY_ARG_LIMIT, 1);
    queryArgs.putInt(ContentResolver.QUERY_ARG_OFFSET, 1);
    Uri unused1 = saveImageToMediaStore();
    Uri photo = saveImageToMediaStore();
    Uri unused2 = saveImageToMediaStore();

    assertThat(queryImages(queryArgs)).containsExactly(photo);
  }

  private static ImmutableList<Uri> queryImagesSorted(String sortOrder) {
    ContentResolver resolver = ApplicationProvider.getApplicationContext().getContentResolver();
    try (Cursor cursor = resolver.query(QUERY_URI, projection, null, null, sortOrder)) {
      return readCursor(cursor);
    }
  }

  private static ImmutableList<Uri> queryImages(Bundle queryArgs) {
    ContentResolver resolver = ApplicationProvider.getApplicationContext().getContentResolver();
    try (Cursor cursor = resolver.query(QUERY_URI, projection, queryArgs, null)) {
      return readCursor(cursor);
    }
  }

  private static ImmutableList<Uri> readCursor(Cursor cursor) {
    ImmutableList.Builder<Uri> uris = ImmutableList.builder();
    assertThat(cursor).isNotNull();
    while (cursor.moveToNext()) {
      uris.add(
          ContentUris.withAppendedId(
              MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
              cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID))));
    }
    return uris.build();
  }

  public static Uri saveImageToMediaStore() throws Exception {
    ContentResolver resolver = ApplicationProvider.getApplicationContext().getContentResolver();
    ContentValues contentValues = new ContentValues();
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
