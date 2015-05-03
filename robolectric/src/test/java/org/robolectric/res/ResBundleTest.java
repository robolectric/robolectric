package org.robolectric.res;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.robolectric.res.ResBundle.Value;

public class ResBundleTest {
  private List<Value<TypedResource<String>>> vals = new ArrayList<>();

  @Test
  public void closestMatchIsPicked() {
    Value<TypedResource<String>> val1 = new Value<>("v16", createStringTypedResource());
    vals.add(val1);
    Value<TypedResource<String>> val2 = new Value<>("v17", createStringTypedResource());
    vals.add(val2);

    Value v = ResBundle.pick(vals, "v18");
    assertThat(v).isEqualTo(val2);
  }

  @Test
  public void firstValIsPickedWhenNoMatch() {
    Value<TypedResource<String>> val1 = new Value<>("en", createStringTypedResource());
    vals.add(val1);
    Value<TypedResource<String>> val2 = new Value<>("fr", createStringTypedResource());
    vals.add(val2);

    Value v = ResBundle.pick(vals, "v18");
    assertThat(v).isEqualTo(val1);
  }

  @Test
  public void firstValIsPickedWhenNoQualifiersGiven() {
    Value<TypedResource<String>> val1 = new Value<>("v16", createStringTypedResource());
    vals.add(val1);
    Value<TypedResource<String>> val2 = new Value<>("v17", createStringTypedResource());
    vals.add(val2);

    Value v = ResBundle.pick(vals, "");
    assertThat(v).isEqualTo(val1);
  }

  @Test
  public void firstValIsPickedWhenNoVersionQualifiersGiven() {
    Value<TypedResource<String>> val1 = new Value<>("v16", createStringTypedResource());
    vals.add(val1);
    Value<TypedResource<String>> val2 = new Value<>("v17", createStringTypedResource());
    vals.add(val2);

    Value v = ResBundle.pick(vals, "en");
    assertThat(v).isEqualTo(val1);
  }

  @Test
  public void eliminatedValuesAreNotPickedForVersion() {
    Value<TypedResource<String>> val1 = new Value<>("en-v16", createStringTypedResource());
    vals.add(val1);
    Value<TypedResource<String>> val2 = new Value<>("v17", createStringTypedResource());
    vals.add(val2);

    Value v = ResBundle.pick(vals, "en-v18");
    assertThat(v).isEqualTo(val1);
  }

  @Test
  public void greaterVersionsAreNotPicked() {
    Value<TypedResource<String>> val1 = new Value<>("v11", createStringTypedResource());
    vals.add(val1);
    Value<TypedResource<String>> val2 = new Value<>("v19", createStringTypedResource());
    vals.add(val2);

    Value v = ResBundle.pick(vals, "v18");
    assertThat(v).isEqualTo(val1);
  }
  
  @Test
  public void onlyMatchingVersionsQualifiersWillBePicked() {
    Value<TypedResource<String>> val1 = new Value<>("v16", createStringTypedResource());
    vals.add(val1);
    Value<TypedResource<String>> val2 = new Value<>("sw600dp-v17", createStringTypedResource());
    vals.add(val2);

    Value v = ResBundle.pick(vals, "v18");
    assertThat(v).isEqualTo(val1);
  }

  @Test
  public void illegalResourceQualifierThrowsException() {
    Value<TypedResource<String>> val1 = new Value<>("v11-en-v12", createStringTypedResource());
    vals.add(val1);

    try {
      ResBundle.pick(vals, "v18");
      fail("Expected exception to be caught");
    } catch (IllegalStateException e) {
      assertThat(e).hasMessageStartingWith("A resource file was found that had two API level qualifiers: ");
    }
  }

  @Test
  public void shouldMatchQualifiersPerAndroidSpec() throws Exception {
    assertEquals("en-port", ResBundle.pick(asValues(
        "",
        "en",
        "fr-rCA",
        "en-port",
        "en-notouch-12key",
        "port-ldpi",
        "port-notouch-12key"), "en-GB-port-hdpi-notouch-12key").getValue());
  }

  private List<Value<String>> asValues(String... qualifierses) {
    List<Value<String>> values = new ArrayList<>();
    for (String qualifiers : qualifierses) {
      values.add(new ResBundle.Value<>(qualifiers, qualifiers));
    }
    return values;
  }

  private static TypedResource<String> createStringTypedResource() {
    return new TypedResource<>("title from resourceLoader1", ResType.CHAR_SEQUENCE);
  }
}
