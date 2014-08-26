package org.robolectric.shadows;

import android.text.SpannedString;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@RunWith(TestRunners.WithDefaults.class)
public class SpannedStringTest {

  @Test
  public void toString_shouldDelegateToUnderlyingCharSequence() {
    SpannedString spannedString = new SpannedString("foo");
    assertEquals("foo", spannedString.toString());
  }

  @Test
  public void valueOfSpannedString_shouldReturnItself() {
    SpannedString spannedString = new SpannedString("foo");
    assertSame(spannedString, SpannedString.valueOf(spannedString));
  }

  @Test
  public void valueOfCharSequence_shouldReturnNewSpannedString() {
    assertEquals("foo", SpannedString.valueOf("foo").toString());
  }


}

