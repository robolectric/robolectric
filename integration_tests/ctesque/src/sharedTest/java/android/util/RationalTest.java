/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.util;

import static android.util.Rational.NEGATIVE_INFINITY;
import static android.util.Rational.NaN;
import static android.util.Rational.POSITIVE_INFINITY;
import static android.util.Rational.ZERO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.os.Build;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * CTS tests for {@link Rational}.
 *
 * <p>Copied from <a
 * href="https://cs.android.com/android/platform/superproject/main/+/main:cts/tests/tests/util/src/android/util/cts/RationalTest.java">RationalTest</a>
 */
@RunWith(AndroidJUnit4.class)
public class RationalTest {

  /** (1,1) */
  private static final Rational UNIT = new Rational(1, 1);

  @Test
  public void testConstructor() {

    // Simple case
    Rational r = new Rational(1, 2);
    assertEquals(1, r.getNumerator());
    assertEquals(2, r.getDenominator());

    // Denominator negative
    r = new Rational(-1, 2);
    assertEquals(-1, r.getNumerator());
    assertEquals(2, r.getDenominator());

    // Numerator negative
    r = new Rational(1, -2);
    assertEquals(-1, r.getNumerator());
    assertEquals(2, r.getDenominator());

    // Both negative
    r = new Rational(-1, -2);
    assertEquals(1, r.getNumerator());
    assertEquals(2, r.getDenominator());

    // Infinity.
    r = new Rational(1, 0);
    assertEquals(1, r.getNumerator());
    assertEquals(0, r.getDenominator());

    // Negative infinity.
    r = new Rational(-1, 0);
    assertEquals(-1, r.getNumerator());
    assertEquals(0, r.getDenominator());

    // NaN.
    r = new Rational(0, 0);
    assertEquals(0, r.getNumerator());
    assertEquals(0, r.getDenominator());
  }

  @Test
  public void testEquals() {
    Rational r = new Rational(1, 2);
    assertEquals(1, r.getNumerator());
    assertEquals(2, r.getDenominator());

    assertEquals(r, r);
    assertFalse(r.equals(null));
    assertFalse(r.equals(new Object()));

    Rational twoThirds = new Rational(2, 3);
    assertFalse(r.equals(twoThirds));
    assertFalse(twoThirds.equals(r));

    Rational fourSixths = new Rational(4, 6);
    assertEquals(twoThirds, fourSixths);
    assertEquals(fourSixths, twoThirds);

    Rational moreComplicated = new Rational(5 * 6 * 7 * 8 * 9, 1 * 2 * 3 * 4 * 5);
    Rational moreComplicated2 = new Rational(5 * 6 * 7 * 8 * 9 * 78, 1 * 2 * 3 * 4 * 5 * 78);
    assertEquals(moreComplicated, moreComplicated2);
    assertEquals(moreComplicated2, moreComplicated);

    // Ensure negatives are fine
    twoThirds = new Rational(-2, 3);
    fourSixths = new Rational(-4, 6);
    assertEquals(twoThirds, fourSixths);
    assertEquals(fourSixths, twoThirds);

    moreComplicated = new Rational(-5 * 6 * 7 * 8 * 9, 1 * 2 * 3 * 4 * 5);
    moreComplicated2 = new Rational(-5 * 6 * 7 * 8 * 9 * 78, 1 * 2 * 3 * 4 * 5 * 78);
    assertEquals(moreComplicated, moreComplicated2);
    assertEquals(moreComplicated2, moreComplicated);

    // Zero is always equal to itself
    Rational zero2 = new Rational(0, 100);
    assertEquals(ZERO, zero2);
    assertEquals(zero2, ZERO);

    // NaN is always equal to itself
    Rational nan = NaN;
    Rational nan2 = new Rational(0, 0);
    assertTrue(nan.equals(nan));
    assertTrue(nan.equals(nan2));
    assertTrue(nan2.equals(nan));
    assertFalse(nan.equals(r));
    assertFalse(r.equals(nan));

    // Infinities of the same sign are equal.
    Rational posInf = POSITIVE_INFINITY;
    Rational posInf2 = new Rational(2, 0);
    Rational negInf = NEGATIVE_INFINITY;
    Rational negInf2 = new Rational(-2, 0);
    assertEquals(posInf, posInf);
    assertEquals(negInf, negInf);
    assertEquals(posInf, posInf2);
    assertEquals(negInf, negInf2);

    // Infinities aren't equal to anything else.
    assertFalse(posInf.equals(negInf));
    assertFalse(negInf.equals(posInf));
    assertFalse(negInf.equals(r));
    assertFalse(posInf.equals(r));
    assertFalse(r.equals(negInf));
    assertFalse(r.equals(posInf));
    assertFalse(posInf.equals(nan));
    assertFalse(negInf.equals(nan));
    assertFalse(nan.equals(posInf));
    assertFalse(nan.equals(negInf));
  }

  @Test
  public void testReduction() {
    Rational moreComplicated = new Rational(5 * 78, 7 * 78);
    assertEquals(new Rational(5, 7), moreComplicated);
    assertEquals(5, moreComplicated.getNumerator());
    assertEquals(7, moreComplicated.getDenominator());

    Rational posInf = new Rational(5, 0);
    assertEquals(1, posInf.getNumerator());
    assertEquals(0, posInf.getDenominator());
    assertEquals(POSITIVE_INFINITY, posInf);

    Rational negInf = new Rational(-100, 0);
    assertEquals(-1, negInf.getNumerator());
    assertEquals(0, negInf.getDenominator());
    assertEquals(NEGATIVE_INFINITY, negInf);

    Rational zero = new Rational(0, -100);
    assertEquals(0, zero.getNumerator());
    assertEquals(1, zero.getDenominator());
    assertEquals(ZERO, zero);

    Rational flipSigns = new Rational(1, -1);
    assertEquals(-1, flipSigns.getNumerator());
    assertEquals(1, flipSigns.getDenominator());

    Rational flipAndReduce = new Rational(100, -200);
    assertEquals(-1, flipAndReduce.getNumerator());
    assertEquals(2, flipAndReduce.getDenominator());
  }

  @Test
  public void testCompareTo() {
    // unit is equal to itself
    verifyCompareEquals(UNIT, new Rational(1, 1));

    // NaN is greater than anything but NaN
    verifyCompareEquals(NaN, new Rational(0, 0));
    verifyGreaterThan(NaN, UNIT);
    verifyGreaterThan(NaN, POSITIVE_INFINITY);
    verifyGreaterThan(NaN, NEGATIVE_INFINITY);
    verifyGreaterThan(NaN, ZERO);

    // Positive infinity is greater than any other non-NaN
    verifyCompareEquals(POSITIVE_INFINITY, new Rational(1, 0));
    verifyGreaterThan(POSITIVE_INFINITY, UNIT);
    verifyGreaterThan(POSITIVE_INFINITY, NEGATIVE_INFINITY);
    verifyGreaterThan(POSITIVE_INFINITY, ZERO);

    // Negative infinity is smaller than any other non-NaN
    verifyCompareEquals(NEGATIVE_INFINITY, new Rational(-1, 0));
    verifyLessThan(NEGATIVE_INFINITY, UNIT);
    verifyLessThan(NEGATIVE_INFINITY, POSITIVE_INFINITY);
    verifyLessThan(NEGATIVE_INFINITY, ZERO);

    // A finite number with the same denominator is trivially comparable
    verifyGreaterThan(new Rational(3, 100), new Rational(1, 100));
    verifyGreaterThan(new Rational(3, 100), ZERO);

    // Compare finite numbers with different divisors
    verifyGreaterThan(new Rational(5, 25), new Rational(1, 10));
    verifyGreaterThan(new Rational(5, 25), ZERO);

    // Compare finite numbers with different signs
    verifyGreaterThan(new Rational(5, 25), new Rational(-1, 10));
    verifyLessThan(new Rational(-5, 25), ZERO);
  }

  @Test
  public void testConvenienceMethods() {
    // isFinite
    verifyFinite(ZERO, true);
    verifyFinite(NaN, false);
    verifyFinite(NEGATIVE_INFINITY, false);
    verifyFinite(POSITIVE_INFINITY, false);
    verifyFinite(UNIT, true);

    // isInfinite
    verifyInfinite(ZERO, false);
    verifyInfinite(NaN, false);
    verifyInfinite(NEGATIVE_INFINITY, true);
    verifyInfinite(POSITIVE_INFINITY, true);
    verifyInfinite(UNIT, false);

    // isNaN
    verifyNaN(ZERO, false);
    verifyNaN(NaN, true);
    verifyNaN(NEGATIVE_INFINITY, false);
    verifyNaN(POSITIVE_INFINITY, false);
    verifyNaN(UNIT, false);

    // isZero
    verifyZero(ZERO, true);
    verifyZero(NaN, false);
    verifyZero(NEGATIVE_INFINITY, false);
    verifyZero(POSITIVE_INFINITY, false);
    verifyZero(UNIT, false);
  }

  @Test
  public void testValueConversions() {
    // Unit, simple case
    verifyValueEquals(UNIT, 1.0f);
    verifyValueEquals(UNIT, 1.0);
    verifyValueEquals(UNIT, 1L);
    verifyValueEquals(UNIT, 1);
    verifyValueEquals(UNIT, (short) 1);

    // Zero, simple case
    verifyValueEquals(ZERO, 0.0f);
    verifyValueEquals(ZERO, 0.0);
    verifyValueEquals(ZERO, 0L);
    verifyValueEquals(ZERO, 0);
    verifyValueEquals(ZERO, (short) 0);

    // NaN is 0 for integers, not-a-number for floating point
    verifyValueEquals(NaN, Float.NaN);
    verifyValueEquals(NaN, Double.NaN);
    verifyValueEquals(NaN, 0L);
    verifyValueEquals(NaN, 0);
    verifyValueEquals(NaN, (short) 0);

    // Positive infinity, saturates upwards for integers
    verifyValueEquals(POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
    verifyValueEquals(POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    verifyValueEquals(POSITIVE_INFINITY, Long.MAX_VALUE);
    verifyValueEquals(POSITIVE_INFINITY, Integer.MAX_VALUE);
    verifyValueEquals(POSITIVE_INFINITY, (short) -1);

    // Negative infinity, saturates downwards for integers
    verifyValueEquals(NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
    verifyValueEquals(NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
    verifyValueEquals(NEGATIVE_INFINITY, Long.MIN_VALUE);
    verifyValueEquals(NEGATIVE_INFINITY, Integer.MIN_VALUE);
    verifyValueEquals(NEGATIVE_INFINITY, (short) 0);

    // Normal finite values, round down for integers
    final Rational oneQuarter = new Rational(1, 4);
    verifyValueEquals(oneQuarter, 1.0f / 4.0f);
    verifyValueEquals(oneQuarter, 1.0 / 4.0);
    verifyValueEquals(oneQuarter, 0L);
    verifyValueEquals(oneQuarter, 0);
    verifyValueEquals(oneQuarter, (short) 0);

    final Rational nineFifths = new Rational(9, 5);
    verifyValueEquals(nineFifths, 9.0f / 5.0f);
    verifyValueEquals(nineFifths, 9.0 / 5.0);
    verifyValueEquals(nineFifths, 1L);
    verifyValueEquals(nineFifths, 1);
    verifyValueEquals(nineFifths, (short) 1);

    final Rational negativeHundred = new Rational(-1000, 10);
    verifyValueEquals(negativeHundred, -100.f / 1.f);
    verifyValueEquals(negativeHundred, -100.0 / 1.0);
    verifyValueEquals(negativeHundred, -100L);
    verifyValueEquals(negativeHundred, -100);
    verifyValueEquals(negativeHundred, (short) -100);

    // Short truncates if the result is too large
    verifyValueEquals(new Rational(Integer.MAX_VALUE, 1), (short) Integer.MAX_VALUE);
    verifyValueEquals(new Rational(0x00FFFFFF, 1), (short) 0x00FFFFFF);
    verifyValueEquals(new Rational(0x00FF00FF, 1), (short) 0x00FF00FF);
  }

  @Test
  public void testSerialize() throws ClassNotFoundException, IOException {
    /*
     * Check correct [de]serialization
     */
    verifyEqualsAfterSerializing(ZERO);
    verifyEqualsAfterSerializing(NaN);
    verifyEqualsAfterSerializing(NEGATIVE_INFINITY);
    verifyEqualsAfterSerializing(POSITIVE_INFINITY);
    verifyEqualsAfterSerializing(UNIT);
    verifyEqualsAfterSerializing(new Rational(100, 200));
    verifyEqualsAfterSerializing(new Rational(-100, 200));
    verifyEqualsAfterSerializing(new Rational(5, 1));
    verifyEqualsAfterSerializing(new Rational(Integer.MAX_VALUE, Integer.MIN_VALUE));

    // We only run following illegal Rational serialization tests as it use reflections
    // to construct illegal Rational and these private fields are restricted after
    // Android S. We have tried Test Orchestrator with --no-hidden-api-checks, but it didn't
    // work and we don't want to bring external libraries to bypass it. So just disabling it
    // for Android S+ devices.
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
      /*
       * Check bad deserialization fails
       */
      try {
        Rational badZero = createIllegalRational(0, 100); // [0, 100] , should be [0, 1]
        Rational results = serializeRoundTrip(badZero);
        fail("Deserializing " + results + " should not have succeeded");
      } catch (InvalidObjectException e) {
        // OK
      }

      try {
        Rational badPosInfinity = createIllegalRational(100, 0); // [100, 0] , should be [1, 0]
        Rational results = serializeRoundTrip(badPosInfinity);
        fail("Deserializing " + results + " should not have succeeded");
      } catch (InvalidObjectException e) {
        // OK
      }

      try {
        Rational badNegInfinity = createIllegalRational(-100, 0); // [-100, 0] , should be [-1, 0]
        Rational results = serializeRoundTrip(badNegInfinity);
        fail("Deserializing " + results + " should not have succeeded");
      } catch (InvalidObjectException e) {
        // OK
      }

      try {
        Rational badReduced = createIllegalRational(2, 4); // [2,4] , should be [1, 2]
        Rational results = serializeRoundTrip(badReduced);
        fail("Deserializing " + results + " should not have succeeded");
      } catch (InvalidObjectException e) {
        // OK
      }

      try {
        Rational badReducedNeg = createIllegalRational(-2, 4); // [-2, 4] should be [-1, 2]
        Rational results = serializeRoundTrip(badReducedNeg);
        fail("Deserializing " + results + " should not have succeeded");
      } catch (InvalidObjectException e) {
        // OK
      }
    }
  }

  @Test
  public void testParseRational() {
    assertEquals(new Rational(1, 2), Rational.parseRational("3:+6"));
    assertEquals(new Rational(1, 2), Rational.parseRational("-3:-6"));
    assertEquals(Rational.NaN, Rational.parseRational("NaN"));
    assertEquals(Rational.POSITIVE_INFINITY, Rational.parseRational("Infinity"));
    assertEquals(Rational.NEGATIVE_INFINITY, Rational.parseRational("-Infinity"));
    assertEquals(Rational.ZERO, Rational.parseRational("0/261"));
    assertEquals(Rational.NaN, Rational.parseRational("0/-0"));
    assertEquals(Rational.POSITIVE_INFINITY, Rational.parseRational("1000/+0"));
    assertEquals(Rational.NEGATIVE_INFINITY, Rational.parseRational("-1000/-0"));

    Rational r = new Rational(10, 15);
    assertEquals(r, Rational.parseRational(r.toString()));
  }

  @Test(expected = NumberFormatException.class)
  public void testParseRationalInvalid1() {
    Rational.parseRational("1.5");
  }

  @Test(expected = NumberFormatException.class)
  public void testParseRationalInvalid2() {
    Rational.parseRational("239");
  }

  private static void verifyValueEquals(Rational object, float expected) {
    assertEquals("Checking floatValue() for " + object + ";", expected, object.floatValue(), 0.0f);
  }

  private static void verifyValueEquals(Rational object, double expected) {
    assertEquals(
        "Checking doubleValue() for " + object + ";", expected, object.doubleValue(), 0.0f);
  }

  private static void verifyValueEquals(Rational object, long expected) {
    assertEquals("Checking longValue() for " + object + ";", expected, object.longValue());
  }

  private static void verifyValueEquals(Rational object, int expected) {
    assertEquals("Checking intValue() for " + object + ";", expected, object.intValue());
  }

  private static void verifyValueEquals(Rational object, short expected) {
    assertEquals("Checking shortValue() for " + object + ";", expected, object.shortValue());
  }

  private static void verifyFinite(Rational object, boolean expected) {
    verifyAction("finite", object, expected, object.isFinite());
  }

  private static void verifyInfinite(Rational object, boolean expected) {
    verifyAction("infinite", object, expected, object.isInfinite());
  }

  private static void verifyNaN(Rational object, boolean expected) {
    verifyAction("NaN", object, expected, object.isNaN());
  }

  private static void verifyZero(Rational object, boolean expected) {
    verifyAction("zero", object, expected, object.isZero());
  }

  private static <T> void verifyAction(String action, T object, boolean expected, boolean actual) {
    String expectedMessage = expected ? action : ("not " + action);
    assertEquals("Expected " + object + " to be " + expectedMessage, expected, actual);
  }

  private static <T extends Comparable<? super T>> void verifyLessThan(T left, T right) {
    assertTrue(
        "Expected (LR) left " + left + " to be less than right " + right,
        left.compareTo(right) < 0);
    assertTrue(
        "Expected (RL) left " + left + " to be less than right " + right,
        right.compareTo(left) > 0);
  }

  private static <T extends Comparable<? super T>> void verifyGreaterThan(T left, T right) {
    assertTrue(
        "Expected (LR) left " + left + " to be greater than right " + right,
        left.compareTo(right) > 0);
    assertTrue(
        "Expected (RL) left " + left + " to be greater than right " + right,
        right.compareTo(left) < 0);
  }

  private static <T extends Comparable<? super T>> void verifyCompareEquals(T left, T right) {
    assertTrue(
        "Expected (LR) left " + left + " to be compareEquals to right " + right,
        left.compareTo(right) == 0);
    assertTrue(
        "Expected (RL) left " + left + " to be compareEquals to right " + right,
        right.compareTo(left) == 0);
  }

  private static <T extends Serializable> byte[] serialize(T obj) throws IOException {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    try (ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
      objectStream.writeObject(obj);
    }
    return byteStream.toByteArray();
  }

  private static <T extends Serializable> T deserialize(byte[] array, Class<T> klass)
      throws IOException, ClassNotFoundException {
    ByteArrayInputStream bais = new ByteArrayInputStream(array);
    ObjectInputStream ois = new ObjectInputStream(bais);
    Object obj = ois.readObject();
    return klass.cast(obj);
  }

  @SuppressWarnings("unchecked")
  private static <T extends Serializable> T serializeRoundTrip(T obj)
      throws IOException, ClassNotFoundException {
    Class<T> klass = (Class<T>) obj.getClass();
    byte[] arr = serialize(obj);
    return deserialize(arr, klass);
  }

  private static <T extends Serializable> void verifyEqualsAfterSerializing(T obj)
      throws ClassNotFoundException, IOException {
    T serialized = serializeRoundTrip(obj);
    assertEquals("Expected values to be equal after serialization round-trip", obj, serialized);
  }

  private static Rational createIllegalRational(int numerator, int denominator) {
    Rational r = new Rational(numerator, denominator);
    mutateField(r, "mNumerator", numerator);
    mutateField(r, "mDenominator", denominator);
    return r;
  }

  private static <T> void mutateField(T object, String name, int value) {
    try {
      Field f = object.getClass().getDeclaredField(name);
      f.setAccessible(true);
      f.set(object, value);
    } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
      throw new AssertionError(e);
    }
  }
}
