package org.robolectric.shadows;

import static com.google.common.base.StandardSystemProperty.LINE_SEPARATOR;
import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import android.util.Log;
import android.util.Log.TerribleFailure;
import android.util.Log.TerribleFailureHandler;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.Iterables;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog.LogItem;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.versioning.AndroidVersions.L;

@RunWith(AndroidJUnit4.class)
public class ShadowLogTest {

  @Test
  public void d_shouldLogAppropriately() {
    Log.d("tag", "msg");

    assertLogged(Log.DEBUG, "tag", "msg", null);
  }

  @Test
  public void d_shouldLogAppropriately_withThrowable() {
    Throwable throwable = new Throwable();

    Log.d("tag", "msg", throwable);

    assertLogged(Log.DEBUG, "tag", "msg", throwable);
  }

  @Test
  public void e_shouldLogAppropriately() {
    Log.e("tag", "msg");

    assertLogged(Log.ERROR, "tag", "msg", null);
  }

  @Test
  public void e_shouldLogAppropriately_withThrowable() {
    Throwable throwable = new Throwable();

    Log.e("tag", "msg", throwable);

    assertLogged(Log.ERROR, "tag", "msg", throwable);
  }

  @Test
  public void i_shouldLogAppropriately() {
    Log.i("tag", "msg");

    assertLogged(Log.INFO, "tag", "msg", null);
  }

  @Test
  public void i_shouldLogAppropriately_withThrowable() {
    Throwable throwable = new Throwable();

    Log.i("tag", "msg", throwable);

    assertLogged(Log.INFO, "tag", "msg", throwable);
  }

  @Test
  public void v_shouldLogAppropriately() {
    Log.v("tag", "msg");

    assertLogged(Log.VERBOSE, "tag", "msg", null);
  }

  @Test
  public void v_shouldLogAppropriately_withThrowable() {
    Throwable throwable = new Throwable();

    Log.v("tag", "msg", throwable);

    assertLogged(Log.VERBOSE, "tag", "msg", throwable);
  }

  @Test
  public void w_shouldLogAppropriately() {
    Log.w("tag", "msg");

    assertLogged(Log.WARN, "tag", "msg", null);
  }

  @Test
  public void w_shouldLogAppropriately_withThrowable() {
    Throwable throwable = new Throwable();

    Log.w("tag", "msg", throwable);

    assertLogged(Log.WARN, "tag", "msg", throwable);
  }

  @Test
  public void w_shouldLogAppropriately_withJustThrowable() {
    Throwable throwable = new Throwable();
    Log.w("tag", throwable);
    assertLogged(Log.WARN, "tag", null, throwable);
  }

  @Test
  public void wtf_shouldLogAppropriately() {
    Log.wtf("tag", "msg");

    assertLogged(Log.ASSERT, "tag", "msg", null);
  }

  @Test
  public void wtf_shouldLogAppropriately_withThrowable() {
    Throwable throwable = new Throwable();

    Log.wtf("tag", "msg", throwable);

    assertLogged(Log.ASSERT, "tag", "msg", throwable);
  }

  @Test
  public void wtf_wtfIsFatalIsSet_shouldThrowTerribleFailure() {
    ShadowLog.setWtfIsFatal(true);

    Throwable throwable = new Throwable();
    assertThrows(TerribleFailure.class, () -> Log.wtf("tag", "msg", throwable));
    assertLogged(Log.ASSERT, "tag", "msg", throwable);
  }

  @Test
  @Config(minSdk = L.SDK_INT)
  public void wtf_shouldLogAppropriately_withNewWtfHandler() {
    Throwable throwable = new Throwable();

    String[] captured = new String[1];

    TerribleFailureHandler prevWtfHandler =
        Log.setWtfHandler(
            ReflectionHelpers.createDelegatingProxy(
                TerribleFailureHandler.class,
                new Object() {
                  public void onTerribleFailure(String tag, TerribleFailure what, boolean system) {
                    captured[0] = what.getMessage();
                  }
                }));

    Log.wtf("tag", "msg", throwable);
    // assert that the new handler captures the message
    assertEquals("msg", captured[0]);
    // reset the handler
    Log.setWtfHandler(prevWtfHandler);

    assertLogged(Log.ASSERT, "tag", "msg", throwable);
  }

  @Test
  public void println_shouldLogAppropriately() {
    int len = Log.println(Log.ASSERT, "tag", "msg");
    assertLogged(Log.ASSERT, "tag", "msg", null);
    assertThat(len).isEqualTo(11);
  }

  @Test
  public void println_shouldLogNullTagAppropriately() {
    int len = Log.println(Log.ASSERT, null, "msg");
    assertLogged(Log.ASSERT, null, "msg", null);
    assertThat(len).isEqualTo(8);
  }

  @Test
  public void println_shouldLogNullMessageAppropriately() {
    int len = Log.println(Log.ASSERT, "tag", null);
    assertLogged(Log.ASSERT, "tag", null, null);
    assertThat(len).isEqualTo(8);
  }

  @Test
  public void println_shouldLogNullTagAndNullMessageAppropriately() {
    int len = Log.println(Log.ASSERT, null, null);
    assertLogged(Log.ASSERT, null, null, null);
    assertThat(len).isEqualTo(5);
  }

  @Test
  public void shouldLogToProvidedStream() throws Exception {
    final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintStream old = ShadowLog.stream;
    try {
      ShadowLog.stream = new PrintStream(bos);
      Log.d("tag", "msg");
      assertThat(new String(bos.toByteArray(), UTF_8))
          .isEqualTo("D/tag: msg" + System.getProperty("line.separator"));

      Log.w("tag", new RuntimeException());
      assertTrue(new String(bos.toByteArray(), UTF_8).contains("RuntimeException"));
    } finally {
      ShadowLog.stream = old;
    }
  }

  private static RuntimeException specificMethodName() {
    return new RuntimeException();
  }

  @Test
  public void shouldLogAccordingToTag() throws Exception {
    ShadowLog.reset();
    Log.d("tag1", "1");
    Log.i("tag2", "2");
    Log.e("tag3", "3");
    Log.w("tag1", "4");
    Log.i("tag1", "5");
    Log.d("tag2", "6");
    Log.d("throwable", "7", specificMethodName());

    List<LogItem> allItems = ShadowLog.getLogs();
    assertThat(allItems.size()).isEqualTo(7);
    int i = 1;
    for (LogItem item : allItems) {
      assertThat(item.msg).isEqualTo(Integer.toString(i));
      assertThat(item.toString()).contains(item.msg);
      i++;
    }
    assertThat(allItems.get(6).toString()).contains("specificMethodName");
    assertUniformLogsForTag("tag1", 3);
    assertUniformLogsForTag("tag2", 2);
    assertUniformLogsForTag("tag3", 1);
  }

  private static void assertUniformLogsForTag(String tag, int count) {
    List<LogItem> tag1Items = ShadowLog.getLogsForTag(tag);
    assertThat(tag1Items.size()).isEqualTo(count);
    int last = -1;
    for (LogItem item : tag1Items) {
      assertThat(item.tag).isEqualTo(tag);
      int current = Integer.parseInt(item.msg);
      assertThat(current > last).isTrue();
      last = current;
    }
  }

  @Test
  public void infoIsDefaultLoggableLevel() throws Exception {
    PrintStream old = ShadowLog.stream;
    ShadowLog.stream = null;
    assertFalse(Log.isLoggable("FOO", Log.VERBOSE));
    assertFalse(Log.isLoggable("FOO", Log.DEBUG));

    assertTrue(Log.isLoggable("FOO", Log.INFO));
    assertTrue(Log.isLoggable("FOO", Log.WARN));
    assertTrue(Log.isLoggable("FOO", Log.ERROR));
    assertTrue(Log.isLoggable("FOO", Log.ASSERT));
    ShadowLog.stream = old;
  }

  private static void assertLogged(int type, String tag, String msg, Throwable throwable) {
    LogItem lastLog = Iterables.getLast(ShadowLog.getLogsForTag(tag));
    assertEquals(type, lastLog.type);
    assertEquals(msg, lastLog.msg);
    assertEquals(tag, lastLog.tag);
    assertEquals(throwable, lastLog.throwable);
  }

  private static void assertLogged(
      String timeString, int type, String tag, String msg, Throwable throwable) {
    LogItem lastLog = Iterables.getLast(ShadowLog.getLogsForTag(tag));
    assertEquals(timeString, lastLog.timeString);
    assertLogged(type, tag, msg, throwable);
  }

  @Test
  public void identicalLogItemInstancesAreEqual() {
    LogItem item1 = new LogItem(Log.VERBOSE, "Foo", "Bar", null);
    LogItem item2 = new LogItem(Log.VERBOSE, "Foo", "Bar", null);
    assertThat(item1).isEqualTo(item2);
    assertThat(item2).isEqualTo(item1);
  }

  @Test
  public void logsAfterSetLoggable() {
    ShadowLog.setLoggable("Foo", Log.VERBOSE);
    assertTrue(Log.isLoggable("Foo", Log.DEBUG));
  }

  @Test
  public void noLogAfterSetLoggable() {
    PrintStream old = ShadowLog.stream;
    ShadowLog.stream = new PrintStream(new ByteArrayOutputStream());
    ShadowLog.setLoggable("Foo", Log.DEBUG);
    assertFalse(Log.isLoggable("Foo", Log.VERBOSE));
    ShadowLog.stream = old;
  }

  @Test
  public void getLogs_shouldReturnCopy() {
    Log.d("tag1", "1");
    assertThat(ShadowLog.getLogs()).isNotSameInstanceAs(ShadowLog.getLogs());
    assertThat(ShadowLog.getLogs()).isEqualTo(ShadowLog.getLogs());
  }

  @Test
  public void getLogsForTag_empty() {
    assertThat(ShadowLog.getLogsForTag("non_existent")).isEmpty();
  }

  @Test
  public void clear() {
    assertThat(ShadowLog.getLogsForTag("tag1")).isEmpty();
    Log.d("tag1", "1");
    assertThat(ShadowLog.getLogsForTag("tag1")).isNotEmpty();
    ShadowLog.clear();
    assertThat(ShadowLog.getLogsForTag("tag1")).isEmpty();
    assertThat(ShadowLog.getLogs()).isEmpty();
  }

  @Test
  public void shouldLogTimeWithTimeSupplier() {
    ShadowLog.setTimeSupplier(() -> "20 July 1969 20:17");

    Log.d("tag", "msg");

    assertLogged("20 July 1969 20:17", Log.DEBUG, "tag", "msg", null);
  }

  @Test
  public void shouldLogToProvidedStreamWithTimeSupplier() {
    ShadowLog.setTimeSupplier(() -> "20 July 1969 20:17");

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintStream old = ShadowLog.stream;
    try {
      ShadowLog.stream = new PrintStream(bos);
      Log.d("tag", "msg");
      assertThat(new String(bos.toByteArray(), UTF_8))
          .isEqualTo("20 July 1969 20:17 D/tag: msg" + LINE_SEPARATOR.value());

      Log.w("tag", new RuntimeException());
      assertThat(new String(bos.toByteArray(), UTF_8)).contains("RuntimeException");
    } finally {
      ShadowLog.stream = old;
    }
  }

  @Test
  public void resetShouldClearTimeSupplier() {
    ShadowLog.setTimeSupplier(() -> "15 June 1977 20:17");
    ShadowLog.reset();

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintStream old = ShadowLog.stream;
    try {
      ShadowLog.stream = new PrintStream(bos);
      Log.d("tag", "msg");
      assertThat(new String(bos.toByteArray(), UTF_8))
          .isEqualTo("D/tag: msg" + LINE_SEPARATOR.value());

      Log.w("tag", new RuntimeException());
      assertThat(new String(bos.toByteArray(), UTF_8)).contains("RuntimeException");
    } finally {
      ShadowLog.stream = old;
    }
  }
}
