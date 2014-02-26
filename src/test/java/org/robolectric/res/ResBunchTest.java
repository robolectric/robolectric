package org.robolectric.res;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.fest.assertions.api.Assertions.*;

@RunWith(TestRunners.WithDefaults.class)
public class ResBunchTest extends ResBunch {

  @Test
  public void closestMatchIsPicked() {
    Values vals = new Values();
    Value val1 = new Value("v16", mock(TypedResource.class), null);
    vals.add(val1);
    Value val2 = new Value("v17", mock(TypedResource.class), null);
    vals.add(val2);

    Value v = ResBunch.pick(vals, "v18");
    assertThat(v).isEqualTo(val2);
  }

  @Test
  public void firstValIsPickedWhenNoMatch() {
    Values vals = new Values();
    Value val1 = new Value("en", mock(TypedResource.class), null);
    vals.add(val1);
    Value val2 = new Value("fr", mock(TypedResource.class), null);
    vals.add(val2);

    Value v = ResBunch.pick(vals, "v18");
    assertThat(v).isEqualTo(val1);
  }

  @Test
  public void firstValIsPickedWhenNoQualifiersGiven() {
    Values vals = new Values();
    Value val1 = new Value("v16", mock(TypedResource.class), null);
    vals.add(val1);
    Value val2 = new Value("v17", mock(TypedResource.class), null);
    vals.add(val2);

    Value v = ResBunch.pick(vals, "");
    assertThat(v).isEqualTo(val1);
  }

  @Test
  public void firstValIsPickedWhenNoVersionQualifiersGiven() {
    Values vals = new Values();
    Value val1 = new Value("v16", mock(TypedResource.class), null);
    vals.add(val1);
    Value val2 = new Value("v17", mock(TypedResource.class), null);
    vals.add(val2);

    Value v = ResBunch.pick(vals, "en");
    assertThat(v).isEqualTo(val1);
  }

  @Test
  public void eliminatedValuesAreNotPickedForVersion() {
    Values vals = new Values();
    Value val1 = new Value("en-v16", mock(TypedResource.class), null);
    vals.add(val1);
    Value val2 = new Value("v17", mock(TypedResource.class), null);
    vals.add(val2);

    Value v = ResBunch.pick(vals, "en-v18");
    assertThat(v).isEqualTo(val1);
  }

  @Test
  public void greaterVersionsAreNotPicked() {
    Values vals = new Values();
    Value val1 = new Value("v11", mock(TypedResource.class), null);
    vals.add(val1);
    Value val2 = new Value("v19", mock(TypedResource.class), null);
    vals.add(val2);

    Value v = ResBunch.pick(vals, "v18");
    assertThat(v).isEqualTo(val1);
  }
  
  @Test
  public void illegalResourceQualifierThrowsException() {
    Values vals = new Values();
    Value val1 = new Value("v11-en-v12", mock(TypedResource.class), null);
    vals.add(val1);
    
    try {
      ResBunch.pick(vals, "v18");
      fail("Expected exception to be caught");
    } catch (IllegalStateException e) {
      assertThat(e).hasMessageStartingWith("A resource file was found that had two API level qualifiers: ");
    }
  }

  // Extend ResBunch.Values to make its constructor visible to this class
  @SuppressWarnings("serial")
  private static class Values extends ResBunch.Values {
  }
}
