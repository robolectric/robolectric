package android.database;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.internal.DoNotInstrument;

@RunWith(AndroidJUnit4.class)
@DoNotInstrument
public class SQLiteOpenHelperTest {
  @Test
  public void closeThenOpen() {
    ExclusiveLockingOpenHelper helper =
        new ExclusiveLockingOpenHelper(getApplicationContext(), "testdb");
    SQLiteDatabase db = helper.getWritableDatabase();
    db.close();

    db = helper.getWritableDatabase();
    assertThat(db.isOpen()).isTrue();
    db.close();
    helper.close();
  }

  private static class ExclusiveLockingOpenHelper extends SQLiteOpenHelper {
    private boolean configured;

    ExclusiveLockingOpenHelper(Context context, String databaseName) {
      super(context, databaseName, null, 1);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
      configured = true;
      // We want to test EXCLUSIVE behavior on both Emulator and Robolectric.
      Cursor cursor = db.rawQuery("PRAGMA locking_mode = EXCLUSIVE", new String[0]);
      cursor.close();
    }

    @Override
    public synchronized void close() {
      super.close();
    }

    private void ensureConfigured(SQLiteDatabase db) {
      if (!configured) {
        onConfigure(db);
      }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      ensureConfigured(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      ensureConfigured(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      ensureConfigured(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
      ensureConfigured(db);
    }
  }
}
