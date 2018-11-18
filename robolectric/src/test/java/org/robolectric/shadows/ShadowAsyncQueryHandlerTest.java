package org.robolectric.shadows;

import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.fakes.RoboCursor;

/** Unit tests for {@link ShadowAsyncQueryHandler}. */
@RunWith(AndroidJUnit4.class)
public final class ShadowAsyncQueryHandlerTest {

  private static final int TOKEN = 22;
  private static final Object COOKIE = new Object();
  private static final RoboCursor CURSOR = new RoboCursor();

  private ContentResolver contentResolver;

  @Before
  public void setUp() {
    contentResolver = ApplicationProvider.getApplicationContext().getContentResolver();
  }

  @Test
  public void startQuery_callbackIsCalled() throws Exception {
    FakeAsyncQueryHandler asyncQueryHandler = new FakeAsyncQueryHandler(contentResolver);
    shadowOf(contentResolver).setCursor(EXTERNAL_CONTENT_URI, CURSOR);

    asyncQueryHandler.startQuery(
        TOKEN,
        COOKIE,
        EXTERNAL_CONTENT_URI,
        null /* projection */,
        null /* selection */,
        null /* selectionArgs */,
        null /* orderBy */);

    assertThat(asyncQueryHandler.token).isEqualTo(TOKEN);
    assertThat(asyncQueryHandler.cookie).isEqualTo(COOKIE);
    assertThat(asyncQueryHandler.cursor).isEqualTo(CURSOR);
  }

  @Test
  public void startInsert_callbackIsCalled() throws Exception {
    FakeAsyncQueryHandler asyncQueryHandler = new FakeAsyncQueryHandler(contentResolver);

    asyncQueryHandler.startInsert(TOKEN, COOKIE, EXTERNAL_CONTENT_URI, null /* initialValues */);

    assertThat(asyncQueryHandler.token).isEqualTo(TOKEN);
    assertThat(asyncQueryHandler.cookie).isEqualTo(COOKIE);
    assertThat(asyncQueryHandler.uri)
        .isEqualTo(ContentUris.withAppendedId(EXTERNAL_CONTENT_URI, 1));
  }

  @Test
  public void startUpdate_callbackIsCalled() throws Exception {
    FakeAsyncQueryHandler asyncQueryHandler = new FakeAsyncQueryHandler(contentResolver);
    contentResolver.insert(EXTERNAL_CONTENT_URI, new ContentValues());

    asyncQueryHandler.startUpdate(
        TOKEN,
        COOKIE,
        EXTERNAL_CONTENT_URI,
        null /* values */,
        null /* selection */,
        null /* selectionArgs */);

    assertThat(asyncQueryHandler.token).isEqualTo(TOKEN);
    assertThat(asyncQueryHandler.cookie).isEqualTo(COOKIE);
    assertThat(asyncQueryHandler.result).isEqualTo(1);
  }

  @Test
  public void startDelete_callbackIsCalled() throws Exception {
    FakeAsyncQueryHandler asyncQueryHandler = new FakeAsyncQueryHandler(contentResolver);
    contentResolver.insert(EXTERNAL_CONTENT_URI, new ContentValues());

    asyncQueryHandler.startDelete(
        TOKEN, COOKIE, EXTERNAL_CONTENT_URI, null /* selection */, null /* selectionArgs */);

    assertThat(asyncQueryHandler.token).isEqualTo(TOKEN);
    assertThat(asyncQueryHandler.cookie).isEqualTo(COOKIE);
    assertThat(asyncQueryHandler.result).isEqualTo(1);
  }

  private static class FakeAsyncQueryHandler extends AsyncQueryHandler {

    int token;
    Object cookie;
    Cursor cursor;
    Uri uri;
    int result;

    FakeAsyncQueryHandler(ContentResolver contentResolver) {
      super(contentResolver);
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
      this.token = token;
      this.cookie = cookie;
      this.cursor = cursor;
    }

    @Override
    protected void onInsertComplete(int token, Object cookie, Uri uri) {
      this.token = token;
      this.cookie = cookie;
      this.uri = uri;
    }

    @Override
    protected void onUpdateComplete(int token, Object cookie, int result) {
      this.token = token;
      this.cookie = cookie;
      this.result = result;
    }

    @Override
    protected void onDeleteComplete(int token, Object cookie, int result) {
      this.token = token;
      this.cookie = cookie;
      this.result = result;
    }
  }
}
