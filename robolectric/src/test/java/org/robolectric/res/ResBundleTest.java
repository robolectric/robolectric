package org.robolectric.res;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Strings;
import javax.annotation.Nonnull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.res.android.ConfigDescription;
import org.robolectric.res.android.ResTable_config;

@RunWith(JUnit4.class)
public class ResBundleTest {
  private ResBundle.ResMap resMap = new ResBundle.ResMap();
  private ResName resName;

  @Before
  public void setUp() throws Exception {
    resName = new ResName("a:b/c");
  }

  @Test
  public void closestMatchIsPicked() {
    TypedResource<String> val1 = createStringTypedResource("v16");
    resMap.put(resName, val1);
    TypedResource<String> val2 = createStringTypedResource("v17");
    resMap.put(resName, val2);

    TypedResource v = resMap.pick(resName, from("v18"));
    assertThat(v).isEqualTo(val2);
  }

  @Test
  public void firstValIsPickedWhenNoMatch() {
    TypedResource<String> val1 = createStringTypedResource("en");
    resMap.put(resName, val1);
    TypedResource<String> val2 = createStringTypedResource("fr");
    resMap.put(resName, val2);

    TypedResource v = resMap.pick(resName, from("en-v18"));
    assertThat(v).isEqualTo(val1);
  }

  @Test
  public void bestValIsPickedForSdkVersion() {
    TypedResource<String> val1 = createStringTypedResource("v16");
    resMap.put(resName, val1);
    TypedResource<String> val2 = createStringTypedResource("v17");
    resMap.put(resName, val2);

    TypedResource v = resMap.pick(resName, from("v26"));
    assertThat(v).isEqualTo(val2);
  }

  @Test
  public void eliminatedValuesAreNotPickedForVersion() {
    TypedResource<String> val1 = createStringTypedResource("land-v16");
    resMap.put(resName, val1);
    TypedResource<String> val2 = createStringTypedResource("v17");
    resMap.put(resName, val2);

    TypedResource v = resMap.pick(resName, from("land-v18"));
    assertThat(v).isEqualTo(val1);
  }

  @Test
  public void greaterVersionsAreNotPicked() {
    TypedResource<String> val1 = createStringTypedResource("v11");
    resMap.put(resName, val1);
    TypedResource<String> val2 = createStringTypedResource("v19");
    resMap.put(resName, val2);

    TypedResource v = resMap.pick(resName, from("v18"));
    assertThat(v).isEqualTo(val1);
  }

  @Test
  public void greaterVersionsAreNotPickedReordered() {
    TypedResource<String> val1 = createStringTypedResource("v19");
    resMap.put(resName, val1);
    TypedResource<String> val2 = createStringTypedResource("v11");
    resMap.put(resName, val2);

    TypedResource v = resMap.pick(resName, from("v18"));
    assertThat(v).isEqualTo(val2);
  }

  @Test
  public void greaterVersionsAreNotPickedMoreQualifiers() {
    // List the contradicting qualifier first, in case the algorithm has a tendency
    // to pick the first qualifier when none of the qualifiers are a "perfect" match.
    TypedResource<String> val1 = createStringTypedResource("anydpi-v21");
    resMap.put(resName, val1);
    TypedResource<String> val2 = createStringTypedResource("xhdpi-v9");
    resMap.put(resName, val2);

    TypedResource v = resMap.pick(resName, from("v18"));
    assertThat(v).isEqualTo(val2);
  }

  @Test
  public void onlyMatchingVersionsQualifiersWillBePicked() {
    TypedResource<String> val1 = createStringTypedResource("v16");
    resMap.put(resName, val1);
    TypedResource<String> val2 = createStringTypedResource("sw600dp-v17");
    resMap.put(resName, val2);

    TypedResource v = resMap.pick(resName, from("v18"));
    assertThat(v).isEqualTo(val1);
  }

  @Test
  public void illegalResourceQualifierThrowsException() {
    TypedResource<String> val1 = createStringTypedResource("en-v12");
    resMap.put(resName, val1);

    try {
      resMap.pick(resName, from("nosuchqualifier"));
      fail("Expected exception to be caught");
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessageThat().startsWith("Invalid qualifiers \"nosuchqualifier\"");
    }
  }

  @Test
  public void shouldMatchQualifiersPerAndroidSpec() throws Exception {
    assertEquals("en-port", asResMap(
        "",
        "en",
        "fr-rCA",
        "en-port",
        "en-notouch-12key",
        "port-ldpi",
        "land-notouch-12key").pick(resName,
        from("en-rGB-port-hdpi-notouch-12key-v25")).asString());
  }

  @Test
  public void shouldMatchQualifiersInSizeRange() throws Exception {
    assertEquals("sw300dp-port", asResMap(
        "",
        "sw200dp",
        "sw350dp-port",
        "sw300dp-port",
        "sw300dp").pick(resName,
        from("sw320dp-port-v25")).asString());
  }

  @Test
  public void shouldPreferWidthOverHeight() throws Exception {
    assertEquals("sw300dp-w200dp", asResMap(
        "",
        "sw200dp",
        "sw200dp-w300dp",
        "sw300dp-w200dp",
        "w300dp").pick(resName,
        from("sw320dp-w320dp-v25")).asString());
  }

  @Test
  public void shouldNotOverwriteValuesWithMatchingQualifiers() {
    ResBundle bundle = new ResBundle();
    XmlContext xmlContext = mock(XmlContext.class);
    when(xmlContext.getQualifiers()).thenReturn(Qualifiers.parse("--"));
    when(xmlContext.getConfig()).thenReturn(new ResTable_config());
    when(xmlContext.getPackageName()).thenReturn("org.robolectric");

    TypedResource firstValue = new TypedResource<>("first_value", ResType.CHAR_SEQUENCE, xmlContext);
    TypedResource secondValue = new TypedResource<>("second_value", ResType.CHAR_SEQUENCE, xmlContext);
    bundle.put(new ResName("org.robolectric", "string", "resource_name"), firstValue);
    bundle.put(new ResName("org.robolectric", "string", "resource_name"), secondValue);

    assertThat(bundle.get(new ResName("org.robolectric", "string", "resource_name"), from("")).getData()).isEqualTo("first_value");
  }

  private ResBundle.ResMap asResMap(String... qualifierses) {
    ResBundle.ResMap resMap = new ResBundle.ResMap();
    for (String qualifiers : qualifierses) {
      resMap.put(resName, createStringTypedResource(qualifiers, qualifiers));
    }
    return resMap;
  }

  private static TypedResource<String> createStringTypedResource(String qualifiers) {
    return createStringTypedResource("title from resourceLoader1", qualifiers);
  }

  @Nonnull
  private static TypedResource<String> createStringTypedResource(String str, String qualifiersStr) {
    XmlContext mockXmlContext = mock(XmlContext.class);
    Qualifiers qualifiers = Qualifiers.parse(qualifiersStr);
    when(mockXmlContext.getQualifiers()).thenReturn(qualifiers);
    when(mockXmlContext.getConfig()).thenReturn(qualifiers.getConfig());
    return new TypedResource<>(str, ResType.CHAR_SEQUENCE, mockXmlContext);
  }

  private static ResTable_config from(String qualifiers) {
    ResTable_config config = new ResTable_config();
    if (!Strings.isNullOrEmpty(qualifiers) &&
        !ConfigDescription.parse(qualifiers, config, false)) {
      throw new IllegalArgumentException("Invalid qualifiers \"" + qualifiers + "\"");
    }
    return config;
  }
}