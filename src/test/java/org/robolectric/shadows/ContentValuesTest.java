package org.robolectric.shadows;

import android.content.ContentValues;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class ContentValuesTest {
  private static final String KEY = "key";

  private ContentValues contentValues;

  @Before
  public void setUp() {
    contentValues = new ContentValues();
  }

  @Test
  public void shouldBeEqualIfContentValuesAreEquivalent() {
    ContentValues valuesA = new ContentValues();
    assertThat(valuesA).isEqualTo(valuesA);
  }

  @Test
  public void shouldBeEqualIfBothContentValuesAreEmpty() {
    ContentValues valuesA = new ContentValues();
    ContentValues valuesB = new ContentValues();
    assertThat(valuesA).isEqualTo(valuesB);
  }

  @Test
  public void shouldBeEqualIfBothContentValuesHaveSameValues() {
    ContentValues valuesA = new ContentValues();
    valuesA.put("String", "A");
    valuesA.put("Integer", 23);
    valuesA.put("Boolean", false);
    ContentValues valuesB = new ContentValues();
    valuesB.putAll(valuesA);
    assertThat(valuesA).isEqualTo(valuesB);
  }

  @Test
  public void shouldNotBeEqualIfContentValuesHaveDifferentValue() {
    ContentValues valuesA = new ContentValues();
    valuesA.put("String", "A");
    ContentValues valuesB = new ContentValues();
    assertThat(valuesA).isNotEqualTo(valuesB);
    valuesB.put("String", "B");
    assertThat(valuesA).isNotEqualTo(valuesB);
  }

  @Test
  public void getAsBoolean_zero() {
    contentValues.put(KEY, 0);
    assertThat(contentValues.getAsBoolean(KEY)).isFalse();
  }

  @Test
  public void getAsBoolean_one() {
    contentValues.put(KEY, 1);
    assertThat(contentValues.getAsBoolean(KEY)).isTrue();
  }

  @Test
  public void getAsBoolean_false() {
    contentValues.put(KEY, false);
    assertThat(contentValues.getAsBoolean(KEY)).isFalse();
  }

  @Test
  public void getAsBoolean_true() {
    contentValues.put(KEY, true);
    assertThat(contentValues.getAsBoolean(KEY)).isTrue();
  }

  @Test
  public void getAsBoolean_falseString() {
    contentValues.put(KEY, "false");
    assertThat(contentValues.getAsBoolean(KEY)).isFalse();
  }

  @Test
  public void getAsBoolean_trueString() {
    contentValues.put(KEY, "true");
    assertThat(contentValues.getAsBoolean(KEY)).isTrue();
  }
}
