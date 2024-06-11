package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import android.graphics.Interpolator;
import android.graphics.Interpolator.Result;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = O)
public class ShadowNativeInterpolatorTest {
  private static final int DEFAULT_KEYFRAME_COUNT = 2;
  private static final float TOLERANCE = 0.1f;

  @Test
  public void testConstructor() {
    Interpolator interpolator = new Interpolator(10);
    assertEquals(10, interpolator.getValueCount());
    assertEquals(DEFAULT_KEYFRAME_COUNT, interpolator.getKeyFrameCount());

    interpolator = new Interpolator(15, 20);
    assertEquals(15, interpolator.getValueCount());
    assertEquals(20, interpolator.getKeyFrameCount());
  }

  @Test
  public void testReset1() {
    final int expected = 100;
    Interpolator interpolator = new Interpolator(10);
    assertEquals(DEFAULT_KEYFRAME_COUNT, interpolator.getKeyFrameCount());
    interpolator.reset(expected);
    assertEquals(expected, interpolator.getValueCount());
    assertEquals(DEFAULT_KEYFRAME_COUNT, interpolator.getKeyFrameCount());
  }

  @Test
  public void testReset2() {
    int expected1 = 100;
    int expected2 = 200;
    // new the Interpolator instance
    Interpolator interpolator = new Interpolator(10);
    interpolator.reset(expected1, expected2);
    assertEquals(expected1, interpolator.getValueCount());
    assertEquals(expected2, interpolator.getKeyFrameCount());
  }

  @Test
  public void testTimeToValues2() {
    Interpolator interpolator = new Interpolator(1);
    interpolator.setKeyFrame(0, 2000, new float[] {1.0f});
    interpolator.setKeyFrame(1, 4000, new float[] {2.0f});
    verifyValue(1000, 1.0f, Result.FREEZE_START, interpolator);
    verifyValue(3000, 1.5f, Result.NORMAL, interpolator);
    verifyValue(6000, 2.0f, Result.FREEZE_END, interpolator);

    // known bug: time argument is unsigned 32bit in graphics library
    verifyValue(-1000, 2.0f, Result.FREEZE_END, interpolator);

    interpolator.reset(1, 3);
    interpolator.setKeyFrame(0, 2000, new float[] {1.0f});
    interpolator.setKeyFrame(1, 4000, new float[] {2.0f});
    interpolator.setKeyFrame(2, 6000, new float[] {4.0f});
    verifyValue(0, 1.0f, Result.FREEZE_START, interpolator);
    verifyValue(3000, 1.5f, Result.NORMAL, interpolator);
    verifyValue(5000, 3.0f, Result.NORMAL, interpolator);
    verifyValue(8000, 4.0f, Result.FREEZE_END, interpolator);

    final int valueCount = 2;
    final int validTime = 0;
    interpolator.reset(valueCount);
    assertEquals(valueCount, interpolator.getValueCount());
    try {
      // value array too short
      interpolator.timeToValues(validTime, new float[valueCount - 1]);
      fail("should throw out ArrayStoreException");
    } catch (ArrayStoreException e) {
      // expected
    }

    interpolator.reset(2, 2);
    interpolator.setKeyFrame(0, 4000, new float[] {1.0f, 1.0f});
    interpolator.setKeyFrame(1, 6000, new float[] {2.0f, 4.0f});
    verifyValues(2000, new float[] {1.0f, 1.0f}, Result.FREEZE_START, interpolator);
    verifyValues(5000, new float[] {1.5f, 2.5f}, Result.NORMAL, interpolator);
    verifyValues(8000, new float[] {2.0f, 4.0f}, Result.FREEZE_END, interpolator);
  }

  @Test
  public void testSetRepeatMirror() {
    Interpolator interpolator = new Interpolator(1, 3);
    interpolator.setKeyFrame(0, 2000, new float[] {1.0f});
    interpolator.setKeyFrame(1, 4000, new float[] {2.0f});
    interpolator.setKeyFrame(2, 6000, new float[] {4.0f});

    verifyValue(1000, 1.0f, Result.FREEZE_START, interpolator);
    verifyValue(3000, 1.5f, Result.NORMAL, interpolator);
    verifyValue(5000, 3.0f, Result.NORMAL, interpolator);
    verifyValue(7000, 4.0f, Result.FREEZE_END, interpolator);

    // repeat once, no mirror
    interpolator.setRepeatMirror(2, false);
    verifyValue(1000, 4.0f, Result.FREEZE_END, interpolator); // known bug
    verifyValue(3000, 1.5f, Result.NORMAL, interpolator);
    verifyValue(5000, 3.0f, Result.NORMAL, interpolator);
    verifyValue(7000, 1.5f, Result.NORMAL, interpolator);
    verifyValue(9000, 3.0f, Result.NORMAL, interpolator);
    verifyValue(11000, 4.0f, Result.FREEZE_END, interpolator);

    // repeat once, mirror
    interpolator.setRepeatMirror(2, true);
    verifyValue(1000, 4.0f, Result.FREEZE_END, interpolator); // known bug
    verifyValue(3000, 1.5f, Result.NORMAL, interpolator);
    verifyValue(5000, 3.0f, Result.NORMAL, interpolator);
    verifyValue(7000, 3.0f, Result.NORMAL, interpolator);
    verifyValue(9000, 1.5f, Result.NORMAL, interpolator);
    verifyValue(11000, 4.0f, Result.FREEZE_END, interpolator);
  }

  @Test
  public void testSetKeyFrame() {
    final float[] aZero = new float[] {0.0f};
    final float[] aOne = new float[] {1.0f};

    Interpolator interpolator = new Interpolator(1);
    interpolator.setKeyFrame(0, 2000, aZero);
    interpolator.setKeyFrame(1, 4000, aOne);
    verifyValue(1000, 0.0f, Result.FREEZE_START, interpolator);
    verifyValue(3000, 0.5f, Result.NORMAL, interpolator);
    verifyValue(5000, 1.0f, Result.FREEZE_END, interpolator);

    final float[] linearBlend = new float[] {0.0f, 0.0f, 1.0f, 1.0f};
    final float[] accelerateBlend =
        new float[] {
          // approximate circle at PI/6 and PI/3
          0.5f, 1.0f - 0.866f, 0.866f, 0.5f
        };
    final float[] decelerateBlend =
        new float[] {
          // approximate circle at PI/6 and PI/3
          1.0f - 0.866f, 0.5f, 0.5f, 0.866f
        };

    // explicit linear blend should yield the same values
    interpolator.setKeyFrame(0, 2000, aZero, linearBlend);
    interpolator.setKeyFrame(1, 4000, aOne, linearBlend);
    verifyValue(1000, 0.0f, Result.FREEZE_START, interpolator);
    verifyValue(3000, 0.5f, Result.NORMAL, interpolator);
    verifyValue(5000, 1.0f, Result.FREEZE_END, interpolator);

    // blend of end key frame is not used
    interpolator.setKeyFrame(0, 2000, aZero);
    interpolator.setKeyFrame(1, 4000, aOne, accelerateBlend);
    verifyValue(1000, 0.0f, Result.FREEZE_START, interpolator);
    verifyValue(3000, 0.5f, Result.NORMAL, interpolator);
    verifyValue(5000, 1.0f, Result.FREEZE_END, interpolator);

    final float[] result = new float[1];

    interpolator.setKeyFrame(0, 2000, aZero, accelerateBlend);
    interpolator.setKeyFrame(1, 4000, aOne);
    verifyValue(1000, 0.0f, Result.FREEZE_START, interpolator);
    assertEquals(Result.NORMAL, interpolator.timeToValues(3000, result));
    assertThat(result[0]).isLessThan(0.5f); // exact blend algorithm not known
    verifyValue(5000, 1.0f, Result.FREEZE_END, interpolator);

    interpolator.setKeyFrame(0, 2000, aZero, decelerateBlend);
    interpolator.setKeyFrame(1, 4000, aOne);
    verifyValue(1000, 0.0f, Result.FREEZE_START, interpolator);
    assertEquals(Result.NORMAL, interpolator.timeToValues(3000, result));
    assertThat(result[0]).isGreaterThan(0.5f); // exact blend algorithm not known
    verifyValue(5000, 1.0f, Result.FREEZE_END, interpolator);

    final int validTime = 0;
    final int valueCount = 2;
    interpolator.reset(valueCount);
    assertEquals(valueCount, interpolator.getValueCount());
    try {
      // value array too short
      interpolator.setKeyFrame(0, validTime, new float[valueCount - 1]);
      fail("should throw ArrayStoreException");
    } catch (ArrayStoreException e) {
      // expected
    }

    try {
      // index too small
      interpolator.setKeyFrame(-1, validTime, new float[valueCount]);
      fail("should throw IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException e) {
      // expected
    }

    try {
      // index too large
      interpolator.setKeyFrame(2, validTime, new float[valueCount]);
      fail("should throw IndexOutOfBoundsException");
    } catch (IndexOutOfBoundsException e) {
      // expected
    }

    try {
      // blend array too short
      interpolator.setKeyFrame(0, validTime, new float[valueCount], new float[3]);
      fail("should throw ArrayStoreException");
    } catch (ArrayStoreException e) {
      // expected
    }
  }

  private void verifyValue(
      int time, float expected, Result expectedResult, Interpolator interpolator) {
    float[] values = new float[1];
    assertEquals(expectedResult, interpolator.timeToValues(time, values));
    assertEquals(expected, values[0], TOLERANCE);
  }

  private void verifyValues(
      int time, float[] expected, Result expectedResult, Interpolator interpolator) {
    float[] values = new float[expected.length];
    assertEquals(expectedResult, interpolator.timeToValues(time, values));
    assertArrayEquals(expected, values, TOLERANCE);
  }
}
