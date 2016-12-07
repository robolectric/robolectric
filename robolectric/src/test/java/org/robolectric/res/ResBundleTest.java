package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResBundleTest {
  private List<TypedResource> vals = new ArrayList<>();

  @Test
  public void closestMatchIsPicked() {
    TypedResource<String> val1 = createStringTypedResource("v16");
    vals.add(val1);
    TypedResource<String> val2 = createStringTypedResource("v17");
    vals.add(val2);

    TypedResource v = ResBundle.pick(vals, "v18");
    assertThat(v).isEqualTo(val2);
  }

  @Test
  public void firstValIsPickedWhenNoMatch() {
    TypedResource<String> val1 = createStringTypedResource("en");
    vals.add(val1);
    TypedResource<String> val2 = createStringTypedResource("fr");
    vals.add(val2);

    TypedResource v = ResBundle.pick(vals, "v18");
    assertThat(v).isEqualTo(val1);
  }

  @Test
  public void firstValIsPickedWhenNoQualifiersGiven() {
    TypedResource<String> val1 = createStringTypedResource("v16");
    vals.add(val1);
    TypedResource<String> val2 = createStringTypedResource("v17");
    vals.add(val2);

    TypedResource v = ResBundle.pick(vals, "");
    assertThat(v).isEqualTo(val1);
  }

  @Test
  public void firstValIsPickedWhenNoVersionQualifiersGiven() {
    TypedResource<String> val1 = createStringTypedResource("v16");
    vals.add(val1);
    TypedResource<String> val2 = createStringTypedResource("v17");
    vals.add(val2);

    TypedResource v = ResBundle.pick(vals, "en");
    assertThat(v).isEqualTo(val1);
  }

  @Test
  public void eliminatedValuesAreNotPickedForVersion() {
    TypedResource<String> val1 = createStringTypedResource("en-v16");
    vals.add(val1);
    TypedResource<String> val2 = createStringTypedResource("v17");
    vals.add(val2);

    TypedResource v = ResBundle.pick(vals, "en-v18");
    assertThat(v).isEqualTo(val1);
  }

  @Test
  public void greaterVersionsAreNotPicked() {
    TypedResource<String> val1 = createStringTypedResource("v11");
    vals.add(val1);
    TypedResource<String> val2 = createStringTypedResource("v19");
    vals.add(val2);

    TypedResource v = ResBundle.pick(vals, "v18");
    assertThat(v).isEqualTo(val1);
  }

  @Test
  public void greaterVersionsAreNotPickedReordered() {
    TypedResource<String> val1 = createStringTypedResource("v19");
    vals.add(val1);
    TypedResource<String> val2 = createStringTypedResource("v11");
    vals.add(val2);

    TypedResource v = ResBundle.pick(vals, "v18");
    assertThat(v).isEqualTo(val2);
  }

  @Test
  public void greaterVersionsAreNotPickedMoreQualifiers() {
    // List the contradicting qualifier first, in case the algorithm has a tendency
    // to pick the first qualifier when none of the qualifiers are a "perfect" match.
    TypedResource<String> val1 = createStringTypedResource("anydpi-v21");
    vals.add(val1);
    TypedResource<String> val2 = createStringTypedResource("xhdpi-v9");
    vals.add(val2);

    TypedResource v = ResBundle.pick(vals, "v18");
    assertThat(v).isEqualTo(val2);
  }

  @Test
  public void onlyMatchingVersionsQualifiersWillBePicked() {
    TypedResource<String> val1 = createStringTypedResource("v16");
    vals.add(val1);
    TypedResource<String> val2 = createStringTypedResource("sw600dp-v17");
    vals.add(val2);

    TypedResource v = ResBundle.pick(vals, "v18");
    assertThat(v).isEqualTo(val1);
  }

  @Test
  public void illegalResourceQualifierThrowsException() {
    TypedResource<String> val1 = createStringTypedResource("v11-en-v12");
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
    assertEquals("en-port", ResBundle.pick(
        asValues(
            "",
            "en",
            "fr-rCA",
            "en-port",
            "en-notouch-12key",
            "port-ldpi",
            "port-notouch-12key"),
        "en-GB-port-hdpi-notouch-12key").asString());
  }

  @Test
  public void shouldMatchQualifiersInSizeRange() throws Exception {
    assertEquals("sw300dp-port", ResBundle.pick(
        asValues(
            "",
            "sw200dp",
            "sw350dp-port",
            "sw300dp-port",
            "sw300dp"),
        "sw320dp-port").asString());
  }

  @Test
  public void shouldPreferWidthOverHeight() throws Exception {
    assertEquals("sw300dp-sh200dp", ResBundle.pick(
        asValues(
            "",
            "sw200dp",
            "sw200dp-sh300dp",
            "sw300dp-sh200dp",
            "sh300dp"),
        "sw320dp-sh320dp").asString());
  }

  private List<TypedResource> asValues(String... qualifierses) {
    List<TypedResource> values = new ArrayList<>();
    for (String qualifiers : qualifierses) {
      values.add(createStringTypedResource(qualifiers, qualifiers));
    }
    return values;
  }

  private static TypedResource<String> createStringTypedResource(String qualifiers) {
    return createStringTypedResource("title from resourceLoader1", qualifiers);
  }

  @NotNull
  private static TypedResource<String> createStringTypedResource(String str, String qualifiers) {
    XmlContext mockXmlContext = mock(XmlContext.class);
    when(mockXmlContext.getQualifiers()).thenReturn(qualifiers);
    return new TypedResource<>(str, ResType.CHAR_SEQUENCE, mockXmlContext);
  }
}
