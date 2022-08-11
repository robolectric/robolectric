package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.assertEquals;

import android.os.Build;
import android.os.SystemClock;
import android.util.StatsEvent;
import android.util.StatsLog;
import com.google.common.collect.Range;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowStatsLog} */
@RunWith(RobolectricTestRunner.class)
@Config(minSdk = Build.VERSION_CODES.R)
public final class ShadowStatsLogTest {

  @Test
  public void testNoFields() {
    final StatsEvent statsEvent = StatsEvent.newBuilder().usePooledBuffer().build();
    long minTimestamp = SystemClock.elapsedRealtimeNanos();
    StatsLog.write(statsEvent);
    long maxTimestamp = SystemClock.elapsedRealtimeNanos();

    final int expectedAtomId = 0;

    assertEquals(1, ShadowStatsLog.getStatsLogs().size());
    assertEquals((int) expectedAtomId, (int) ShadowStatsLog.getStatsLogs().get(0).atomId());

    final ByteBuffer buffer =
        ByteBuffer.wrap(ShadowStatsLog.getStatsLogs().get(0).bytes())
            .order(ByteOrder.LITTLE_ENDIAN);

    assertWithMessage("Root element in buffer is not TYPE_OBJECT")
        .that(buffer.get())
        .isEqualTo(StatsEvent.TYPE_OBJECT);

    assertWithMessage("Incorrect number of elements in root object")
        .that(buffer.get())
        .isEqualTo(3);

    assertWithMessage("First element is not timestamp")
        .that(buffer.get())
        .isEqualTo(StatsEvent.TYPE_LONG);

    assertWithMessage("Incorrect timestamp")
        .that(buffer.getLong())
        .isIn(Range.closed(minTimestamp, maxTimestamp));

    assertWithMessage("Second element is not atom id")
        .that(buffer.get())
        .isEqualTo(StatsEvent.TYPE_INT);

    assertWithMessage("Incorrect atom id").that(buffer.getInt()).isEqualTo(expectedAtomId);

    assertWithMessage("Third element is not errors type")
        .that(buffer.get())
        .isEqualTo(StatsEvent.TYPE_ERRORS);

    final int errorMask = buffer.getInt();

    assertWithMessage("ERROR_NO_ATOM_ID should be the only error in the error mask")
        .that(errorMask)
        .isEqualTo(StatsEvent.ERROR_NO_ATOM_ID);

    assertThat(statsEvent.getNumBytes()).isEqualTo(buffer.position());

    statsEvent.release();
  }

  @Test
  public void testOnlyAtomId() {
    final int expectedAtomId = 109;

    final StatsEvent statsEvent =
        StatsEvent.newBuilder().setAtomId(expectedAtomId).usePooledBuffer().build();
    long minTimestamp = SystemClock.elapsedRealtimeNanos();
    StatsLog.write(statsEvent);
    long maxTimestamp = SystemClock.elapsedRealtimeNanos();

    assertEquals(1, ShadowStatsLog.getStatsLogs().size());
    assertEquals((int) expectedAtomId, (int) ShadowStatsLog.getStatsLogs().get(0).atomId());

    final ByteBuffer buffer =
        ByteBuffer.wrap(ShadowStatsLog.getStatsLogs().get(0).bytes())
            .order(ByteOrder.LITTLE_ENDIAN);

    assertWithMessage("Root element in buffer is not TYPE_OBJECT")
        .that(buffer.get())
        .isEqualTo(StatsEvent.TYPE_OBJECT);

    assertWithMessage("Incorrect number of elements in root object")
        .that(buffer.get())
        .isEqualTo(2);

    assertWithMessage("First element is not timestamp")
        .that(buffer.get())
        .isEqualTo(StatsEvent.TYPE_LONG);

    assertWithMessage("Incorrect timestamp")
        .that(buffer.getLong())
        .isIn(Range.closed(minTimestamp, maxTimestamp));

    assertWithMessage("Second element is not atom id")
        .that(buffer.get())
        .isEqualTo(StatsEvent.TYPE_INT);

    assertWithMessage("Incorrect atom id").that(buffer.getInt()).isEqualTo(expectedAtomId);

    assertThat(statsEvent.getNumBytes()).isEqualTo(buffer.position());

    statsEvent.release();
  }

  @Test
  public void testOnlyAtomIdMultipleAtoms() {
    List<Integer> expectedAtomIds = Arrays.asList(109, 110, 111);
    List<StatsEvent> statsEvents = new ArrayList<>();

    long minTimestamp = SystemClock.elapsedRealtimeNanos();
    for (Integer atomId : expectedAtomIds) {
      final StatsEvent statsEvent =
          StatsEvent.newBuilder().setAtomId(atomId).usePooledBuffer().build();
      StatsLog.write(statsEvent);
      statsEvents.add(statsEvent);
    }
    long maxTimestamp = SystemClock.elapsedRealtimeNanos();

    assertThat(ShadowStatsLog.getStatsLogs()).hasSize(statsEvents.size());

    for (int i = 0; i < statsEvents.size(); i++) {
      assertEquals((int) expectedAtomIds.get(i), ShadowStatsLog.getStatsLogs().get(i).atomId());

      final ByteBuffer buffer =
          ByteBuffer.wrap(ShadowStatsLog.getStatsLogs().get(i).bytes())
              .order(ByteOrder.LITTLE_ENDIAN);

      assertWithMessage("Root element in buffer is not TYPE_OBJECT")
          .that(buffer.get())
          .isEqualTo(StatsEvent.TYPE_OBJECT);

      assertWithMessage("Incorrect number of elements in root object")
          .that(buffer.get())
          .isEqualTo(2);

      assertWithMessage("First element is not timestamp")
          .that(buffer.get())
          .isEqualTo(StatsEvent.TYPE_LONG);

      assertWithMessage("Incorrect timestamp")
          .that(buffer.getLong())
          .isIn(Range.closed(minTimestamp, maxTimestamp));

      assertWithMessage("Second element is not atom id")
          .that(buffer.get())
          .isEqualTo(StatsEvent.TYPE_INT);

      assertWithMessage("Incorrect atom id")
          .that(buffer.getInt())
          .isEqualTo(expectedAtomIds.get(i));

      assertThat(statsEvents.get(i).getNumBytes()).isEqualTo(buffer.position());
    }

    for (StatsEvent statsEvent : statsEvents) {
      statsEvent.release();
    }
  }

  @Test
  public void testIntIntInt() {
    final int expectedAtomId = 109;
    final int field1 = 1;
    final int field2 = 2;
    final int field3 = 3;

    final StatsEvent statsEvent =
        StatsEvent.newBuilder()
            .setAtomId(expectedAtomId)
            .writeInt(field1)
            .writeInt(field2)
            .writeInt(field3)
            .usePooledBuffer()
            .build();
    long minTimestamp = SystemClock.elapsedRealtimeNanos();
    StatsLog.write(statsEvent);
    long maxTimestamp = SystemClock.elapsedRealtimeNanos();

    assertEquals(1, ShadowStatsLog.getStatsLogs().size());
    assertEquals((int) expectedAtomId, (int) ShadowStatsLog.getStatsLogs().get(0).atomId());

    final ByteBuffer buffer =
        ByteBuffer.wrap(ShadowStatsLog.getStatsLogs().get(0).bytes())
            .order(ByteOrder.LITTLE_ENDIAN);

    assertWithMessage("Root element in buffer is not TYPE_OBJECT")
        .that(buffer.get())
        .isEqualTo(StatsEvent.TYPE_OBJECT);

    assertWithMessage("Incorrect number of elements in root object")
        .that(buffer.get())
        .isEqualTo(5);

    assertWithMessage("First element is not timestamp")
        .that(buffer.get())
        .isEqualTo(StatsEvent.TYPE_LONG);

    assertWithMessage("Incorrect timestamp")
        .that(buffer.getLong())
        .isIn(Range.closed(minTimestamp, maxTimestamp));

    assertWithMessage("Second element is not atom id")
        .that(buffer.get())
        .isEqualTo(StatsEvent.TYPE_INT);

    assertWithMessage("Incorrect atom id").that(buffer.getInt()).isEqualTo(expectedAtomId);

    assertWithMessage("First field is not Int").that(buffer.get()).isEqualTo(StatsEvent.TYPE_INT);

    assertWithMessage("Incorrect field 1").that(buffer.getInt()).isEqualTo(field1);

    assertWithMessage("Third field is not Int").that(buffer.get()).isEqualTo(StatsEvent.TYPE_INT);

    assertWithMessage("Incorrect field 2").that(buffer.getInt()).isEqualTo(field2);

    assertWithMessage("Fourth field is not Int").that(buffer.get()).isEqualTo(StatsEvent.TYPE_INT);

    assertWithMessage("Incorrect field 3").that(buffer.getInt()).isEqualTo(field3);

    assertThat(statsEvent.getNumBytes()).isEqualTo(buffer.position());

    statsEvent.release();
  }
}
