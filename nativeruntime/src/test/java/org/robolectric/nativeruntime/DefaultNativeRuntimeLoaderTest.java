package org.robolectric.nativeruntime;

import static org.robolectric.annotation.Config.ALL_SDKS;

import android.database.CursorWindow;
import android.database.sqlite.SQLiteDatabase;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@Config(sdk = ALL_SDKS)
@RunWith(RobolectricTestRunner.class)
public final class DefaultNativeRuntimeLoaderTest {
  ExecutorService executor = Executors.newSingleThreadExecutor();

  @Test
  public void concurrentLoad() throws Exception {
    executor.execute(() -> SQLiteDatabase.create(null));
    CursorWindow cursorWindow = new CursorWindow("sdfsdf");
    cursorWindow.close();
  }
}
