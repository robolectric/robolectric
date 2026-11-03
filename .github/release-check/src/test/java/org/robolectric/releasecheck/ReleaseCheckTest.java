package org.robolectric.releasecheck;

import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.annotation.SQLiteMode;

/**
 * Checks a Robolectric release on each OS before it is published, including its native code.
 *
 * <p>SDK 34 and below use the native runtime from nativeruntime-dist-compat, newer SDKs the one in
 * android-all, so both are covered.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = {28, 34, Config.NEWEST_SDK})
public class ReleaseCheckTest {

  @Test
  public void activity_isResumed() {
    Activity activity = Robolectric.buildActivity(Activity.class).setup().get();

    assertThat(activity.isFinishing()).isFalse();
    assertThat(activity.getWindow()).isNotNull();
  }

  @Test
  @SQLiteMode(SQLiteMode.Mode.NATIVE)
  public void nativeSqlite_queriesData() {
    SQLiteDatabase database = SQLiteDatabase.create(null);
    database.execSQL("CREATE TABLE releases (version TEXT)");
    database.execSQL("INSERT INTO releases VALUES ('robolectric')");

    try (Cursor cursor = database.rawQuery("SELECT version FROM releases", null)) {
      assertThat(cursor.moveToFirst()).isTrue();
      assertThat(cursor.getString(0)).isEqualTo("robolectric");
    } finally {
      database.close();
    }
  }

  @Test
  @GraphicsMode(GraphicsMode.Mode.NATIVE)
  public void nativeGraphics_drawsPixels() {
    Bitmap bitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    canvas.drawColor(Color.RED);
    Paint paint = new Paint();
    paint.setColor(Color.BLUE);
    canvas.drawRect(0, 0, 5, 5, paint);

    assertThat(bitmap.getPixel(1, 1)).isEqualTo(Color.BLUE);
    assertThat(bitmap.getPixel(8, 8)).isEqualTo(Color.RED);
  }
}
