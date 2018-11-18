package org.robolectric.android;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import android.app.Application;
import android.content.res.XmlResourceParser;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.RuntimeEnvironment;
import org.w3c.dom.Document;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

@RunWith(AndroidJUnit4.class)
public class XmlResourceParserImplTest {

  private static final String RES_AUTO_NS = "http://schemas.android.com/apk/res-auto";
  private XmlResourceParser parser;
  private Application context;

  @Before
  public void setUp() throws Exception {
    context = ApplicationProvider.getApplicationContext();
    parser = context.getResources().getXml(R.xml.preferences);
  }

  @After
  public void tearDown() throws Exception {
    parser.close();
  }

  private void parseUntilNext(int event) throws Exception {
    while (parser.next() != event) {
      if (parser.getEventType() == XmlResourceParser.END_DOCUMENT) {
        throw new RuntimeException("Impossible to find: " +
            event + ". End of document reached.");
      }
    }
  }

  private void forgeAndOpenDocument(String xmlValue) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      factory.setIgnoringComments(true);
      factory.setIgnoringElementContentWhitespace(true);
      DocumentBuilder documentBuilder = factory.newDocumentBuilder();
      Document document = documentBuilder.parse(
          new ByteArrayInputStream(xmlValue.getBytes(UTF_8)));

      parser = new XmlResourceParserImpl(document, "file", R.class.getPackage().getName(),
          "org.robolectric", null);
      // Navigate to the root element
      parseUntilNext(XmlResourceParser.START_TAG);
    } catch (Exception parsingException) {
      // Wrap XML parsing exception with a runtime
      // exception for convenience.
      throw new RuntimeException(
          "Cannot forge a Document from an invalid XML",
          parsingException);
    }
  }

  private int attributeIndexOutOfIndex() {
    return parser.getAttributeCount() + 1;
  }

  @Test
  public void testGetXmlInt() throws Exception {
    assertThat(parser).isNotNull();
    int evt = parser.next();
    assertThat(evt).isEqualTo(XmlResourceParser.START_DOCUMENT);
  }

  @Test
  public void testGetXmlString() {
    assertThat(parser).isNotNull();
  }

  @Test
  public void testSetFeature() throws Exception {
    for (String feature : XmlResourceParserImpl.AVAILABLE_FEATURES) {
      parser.setFeature(feature, true);
      try {
        parser.setFeature(feature, false);
        fail(feature + " should be true.");
      } catch (XmlPullParserException ex) {
        // pass
      }
    }

    for (String feature : XmlResourceParserImpl.UNAVAILABLE_FEATURES) {
      try {
        parser.setFeature(feature, false);
        fail(feature + " should not be true.");
      } catch (XmlPullParserException ex) {
        // pass
      }
      try {
        parser.setFeature(feature, true);
        fail(feature + " should not be true.");
      } catch (XmlPullParserException ex) {
        // pass
      }
    }
  }

  @Test
  public void testGetFeature() {
    for (String feature : XmlResourceParserImpl.AVAILABLE_FEATURES) {
      assertThat(parser.getFeature(feature)).isTrue();
    }

    for (String feature : XmlResourceParserImpl.UNAVAILABLE_FEATURES) {
      assertThat(parser.getFeature(feature)).isFalse();
    }

    assertThat(parser.getFeature(null)).isFalse();
  }

  @Test
  public void testSetProperty() {
    try {
      parser.setProperty("foo", "bar");
      fail("Properties should not be supported");
    } catch (XmlPullParserException ex) {
      // pass
    }
  }

  @Test
  public void testGetProperty() {
    // Properties are not supported
    assertThat(parser.getProperty("foo")).isNull();
  }

  @Test
  public void testSetInput_Reader() {
    try {
      parser.setInput(new StringReader(""));
      fail("This method should not be supported");
    } catch (XmlPullParserException ex) {
      // pass
    }
  }

  @Test
  public void testSetInput_InputStreamString() throws IOException {
    try (InputStream inputStream = getClass().getResourceAsStream("src/test/resources/res/xml/preferences.xml")) {
      parser.setInput(inputStream, "UTF-8");
      fail("This method should not be supported");
    } catch (XmlPullParserException ex) {
      // pass
    }
  }

  @Test
  public void testDefineEntityReplacementText() {
    try {
      parser.defineEntityReplacementText("foo", "bar");
      fail("This method should not be supported");
    } catch (XmlPullParserException ex) {
      // pass
    }
  }

  @Test
  public void testGetNamespacePrefix() {
    try {
      parser.getNamespacePrefix(0);
      fail("This method should not be supported");
    } catch (XmlPullParserException ex) {
      // pass
    }
  }

  @Test
  public void testGetInputEncoding() {
    assertThat(parser.getInputEncoding()).isNull();
  }

  @Test
  public void testGetNamespace_String() {
    try {
      parser.getNamespace("bar");
      fail("This method should not be supported");
    } catch (RuntimeException ex) {
      // pass
    }
  }

  @Test
  public void testGetNamespaceCount() {
    try {
      parser.getNamespaceCount(0);
      fail("This method should not be supported");
    } catch (XmlPullParserException ex) {
      // pass
    }
  }

  @Test
  public void testGetNamespaceUri() {
    try {
      parser.getNamespaceUri(0);
      fail("This method should not be supported");
    } catch (XmlPullParserException ex) {
      // pass
    }
  }

  @Test
  public void testGetColumnNumber() {
    assertThat(parser.getColumnNumber()).isEqualTo(-1);
  }

  @Test
  public void testGetDepth() throws Exception {
    // Recorded depths from preference file elements
    List<Integer> expectedDepths = asList(1, 2, 3, 2, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 3);
    List<Integer> actualDepths = new ArrayList<>();
    int evt;
    while ((evt = parser.next()) != XmlResourceParser.END_DOCUMENT) {
      switch (evt) {
        case (XmlResourceParser.START_TAG): {
          actualDepths.add(parser.getDepth());
          break;
        }
      }

    }
    assertThat(actualDepths).isEqualTo(expectedDepths);
  }

  @Test
  public void testGetText() throws Exception {
    forgeAndOpenDocument("<foo/>");
    assertThat(parser.getText()).isEqualTo("");

    forgeAndOpenDocument("<foo>bar</foo>");
    assertThat(parser.getText()).isEqualTo("bar");
  }

  @Test
  public void testGetEventType() throws Exception {
    int evt;
    while ((evt = parser.next()) != XmlResourceParser.END_DOCUMENT) {
      assertThat(parser.getEventType()).isEqualTo(evt);
    }
  }

  @Test
  public void testIsWhitespace() throws Exception {
    assumeTrue(RuntimeEnvironment.useLegacyResources());

    XmlResourceParserImpl parserImpl = (XmlResourceParserImpl) parser;
    assertThat(parserImpl.isWhitespace("bar")).isFalse();
    assertThat(parserImpl.isWhitespace(" ")).isTrue();
  }

  @Test
  public void testGetPrefix() {
    try {
      parser.getPrefix();
      fail("This method should not be supported");
    } catch (RuntimeException ex) {
      // pass
    }
  }

  @Test
  public void testGetNamespace() throws Exception {
    forgeAndOpenDocument("<foo xmlns=\"http://www.w3.org/1999/xhtml\">bar</foo>");
    assertThat(parser.getNamespace()).isEqualTo("http://www.w3.org/1999/xhtml");
  }

  @Test
  public void testGetName_atStart() throws Exception {
    assertThat(parser.getName()).isEqualTo(null);
    parseUntilNext(XmlResourceParser.START_DOCUMENT);
    assertThat(parser.getName()).isEqualTo(null);
    parseUntilNext(XmlResourceParser.START_TAG);
    assertThat(parser.getName()).isEqualTo("PreferenceScreen");
  }

  @Test
  public void testGetName() throws Exception {
    forgeAndOpenDocument("<foo/>");
    assertThat(parser.getName()).isEqualTo("foo");
  }


  @Test
  public void testGetAttribute() throws Exception {
    forgeAndOpenDocument("<foo xmlns:app=\"http://schemas.android.com/apk/res-auto\" app:bar=\"bar\"/>");
    XmlResourceParserImpl parserImpl = (XmlResourceParserImpl) parser;
    assertThat(parserImpl.getAttribute(RES_AUTO_NS, "bar")).isEqualTo("bar");
  }

  @Test
  public void testGetAttributeNamespace() throws Exception {
    forgeAndOpenDocument("<foo xmlns:app=\"http://schemas.android.com/apk/res-auto\" app:bar=\"bar\"/>");
    assertThat(parser.getAttributeNamespace(0)).isEqualTo(RES_AUTO_NS);
  }

  @Test
  public void testGetAttributeName() throws Exception {
    try {
      parser.getAttributeName(0);
      fail("Expected exception");
    } catch (IndexOutOfBoundsException expected) {
      // Expected
    }

    forgeAndOpenDocument("<foo bar=\"bar\"/>");
    assertThat(parser.getAttributeName(0)).isEqualTo("bar");

    try {
      parser.getAttributeName(attributeIndexOutOfIndex());
      fail("Expected exception");
    } catch (IndexOutOfBoundsException expected) {
      // Expected
    }
  }

  @Test
  public void testGetAttributePrefix() throws Exception {
    parseUntilNext(XmlResourceParser.START_TAG);
    try {
      parser.getAttributePrefix(0);
      fail("This method should not be supported");
    } catch (RuntimeException ex) {
      // pass
    }
  }

  @Test
  public void testIsEmptyElementTag() throws Exception {
    assertThat(parser.isEmptyElementTag()).named("Before START_DOCUMENT should return false.").isEqualTo(false);

    forgeAndOpenDocument("<foo><bar/></foo>");
    assertThat(parser.isEmptyElementTag()).named("Not empty tag should return false.").isEqualTo(false);

    forgeAndOpenDocument("<foo/>");
    assertThat(parser.isEmptyElementTag()).named(
        "In the Android implementation this method always return false.").isEqualTo(false);
  }

  @Test
  public void testGetAttributeCount() throws Exception {
    assertThat(parser.getAttributeCount())
        .named("When no node is being explored the number of attributes should be -1.").isEqualTo(-1);

    forgeAndOpenDocument("<foo bar=\"bar\"/>");
    assertThat(parser.getAttributeCount()).isEqualTo(1);
  }

  @Test
  public void testGetAttributeValue_Int() throws Exception {
    forgeAndOpenDocument("<foo bar=\"bar\"/>");
    assertThat(parser.getAttributeValue(0)).isEqualTo("bar");

    try {
      parser.getAttributeValue(attributeIndexOutOfIndex());
      fail();
    } catch (IndexOutOfBoundsException ex) {
      // pass
    }
  }

  @Test
  public void testGetAttributeEscapedValue() throws Exception {
    forgeAndOpenDocument("<foo bar=\"\\'\"/>");
    assertThat(parser.getAttributeValue(0)).isEqualTo("\'");
  }

  @Test
  public void testGetAttributeEntityValue() throws Exception {
    forgeAndOpenDocument("<foo bar=\"\\u201e&#92;&#34;\"/>");
    assertThat(parser.getAttributeValue(0)).isEqualTo("„\"");
  }

  @Test
  public void testGetNodeTextEscapedValue() throws Exception {
    forgeAndOpenDocument("<foo>\'</foo>");
    assertThat(parser.getText()).isEqualTo("\'");
  }

  @Test
  public void testGetNodeTextEntityValue() throws Exception {
    forgeAndOpenDocument("<foo>\\u201e\\&#34;</foo>");
    assertThat(parser.getText()).isEqualTo("„\"");
  }

  @Test
  public void testGetAttributeType() {
    // Hardcoded to always return CDATA
    assertThat(parser.getAttributeType(attributeIndexOutOfIndex())).isEqualTo("CDATA");
  }

  @Test
  public void testIsAttributeDefault() {
    assertThat(parser.isAttributeDefault(attributeIndexOutOfIndex())).isFalse();
  }

  @Test
  public void testGetAttributeValueStringString() throws Exception {
    forgeAndOpenDocument("<foo xmlns:app=\"http://schemas.android.com/apk/res-auto\" app:bar=\"bar\"/>");
    assertThat(parser.getAttributeValue(RES_AUTO_NS, "bar")).isEqualTo("bar");
  }

  @Test
  public void testNext() throws Exception {
    // Recorded events while parsing preferences from Android
    List<String> expectedEvents = Arrays.asList(
        "<xml>",
        "<", // PreferenceScreen
        "<", // PreferenceCategory
        "<", // Preference
        ">",
        ">",

        "<", // PreferenceScreen
        "<", // Preference
        ">",
        "<", // Preference
        ">",
        ">",

        "<", // CheckBoxPreference
        ">",
        "<", // EditTextPreference
        ">",
        "<", // ListPreference
        ">",
        "<", // Preference
        ">",
        "<", //RingtonePreference
        ">",
        "<", // Preference
        ">",
        "<",
        ">",
        "<",
        "<",
        ">",
        ">",
        ">",
        "</xml>");
    List<String> actualEvents = new ArrayList<>();

    int evt;
    do {
      evt = parser.next();
      switch (evt) {
        case XmlPullParser.START_DOCUMENT:
          actualEvents.add("<xml>");
          break;
        case XmlPullParser.END_DOCUMENT:
          actualEvents.add("</xml>");
          break;
        case XmlPullParser.START_TAG:
          actualEvents.add("<");
          break;
        case XmlPullParser.END_TAG:
          actualEvents.add(">");
          break;
      }
    } while (evt != XmlResourceParser.END_DOCUMENT);
    assertThat(actualEvents).isEqualTo(expectedEvents);
  }

  @Test
  public void testRequire() throws Exception {
    parseUntilNext(XmlResourceParser.START_TAG);
    parser.require(XmlResourceParser.START_TAG,
        parser.getNamespace(), parser.getName());

    try {
      parser.require(XmlResourceParser.END_TAG,
          parser.getNamespace(), parser.getName());
      fail("Require with wrong event should have failed");
    } catch (XmlPullParserException ex) {
      // pass
    }

    try {
      parser.require(XmlResourceParser.START_TAG,
          "foo", parser.getName());
      fail("Require with wrong namespace should have failed");
    } catch (XmlPullParserException ex) {
      // pass
    }

    try {
      parser.require(XmlResourceParser.START_TAG,
          parser.getNamespace(), "foo");
      fail("Require with wrong tag name should have failed");
    } catch (XmlPullParserException ex) {
      // pass
    }
  }

  @Test
  public void testNextText_noText() throws Exception {
    forgeAndOpenDocument("<foo><bar/></foo>");
    try {
      assertThat(parser.nextText()).isEqualTo(parser.getText());
      fail("nextText on a document with no text should have failed");
    } catch (XmlPullParserException ex) {
      assertThat(parser.getEventType()).isAnyOf(XmlResourceParser.START_TAG, XmlResourceParser.END_DOCUMENT);
    }
  }

  /**
   * Test that next tag will only return tag events.
   */
  @Test
  public void testNextTag() throws Exception {
    Set<Integer> acceptableTags = new HashSet<>();
    acceptableTags.add(XmlResourceParser.START_TAG);
    acceptableTags.add(XmlResourceParser.END_TAG);

    forgeAndOpenDocument("<foo><bar/><text>message</text></foo>");
    int evt;
    do {
      evt = parser.next();
      assertTrue(acceptableTags.contains(evt));
    } while (evt == XmlResourceParser.END_TAG &&
        "foo".equals(parser.getName()));
  }

  @Test
  public void testGetAttributeListValue_StringStringStringArrayInt() throws Exception {
    String[] options = {"foo", "bar"};
    forgeAndOpenDocument("<foo xmlns:app=\"http://schemas.android.com/apk/res-auto\" app:bar=\"bar\"/>");
    assertThat(parser.getAttributeListValue(RES_AUTO_NS, "bar", options, 0)).isEqualTo(1);

    forgeAndOpenDocument("<foo xmlns:app=\"http://schemas.android.com/apk/res-auto\" app:bar=\"unexpected\"/>");
    assertThat(parser.getAttributeListValue(RES_AUTO_NS, "bar", options, 0)).isEqualTo(0);
  }

  @Test
  public void testGetAttributeBooleanValue_StringStringBoolean() throws Exception {
    forgeAndOpenDocument("<foo xmlns:app=\"http://schemas.android.com/apk/res-auto\" app:bar=\"true\"/>");
    assertThat(parser.getAttributeBooleanValue(RES_AUTO_NS, "bar", false)).isTrue();
    assertThat(parser.getAttributeBooleanValue(RES_AUTO_NS, "foo", false)).isFalse();
  }

  @Test
  public void testGetAttributeBooleanValue_IntBoolean() throws Exception {
    forgeAndOpenDocument("<foo bar=\"true\"/>");
    assertThat(parser.getAttributeBooleanValue(0, false)).isTrue();
    assertThat(parser.getAttributeBooleanValue(attributeIndexOutOfIndex(), false)).isFalse();
  }

  @Test
  public void testGetAttributeResourceValueIntInt() throws Exception {
    parser = context.getResources().getXml(R.xml.has_attribute_resource_value);
    parseUntilNext(XmlResourceParser.START_TAG);

    assertThat(parser.getAttributeResourceValue(0, 42)).isEqualTo(R.layout.main);
  }

  @Test
  public void testGetAttributeResourceValueStringStringInt() throws Exception {
    parser = context.getResources().getXml(R.xml.has_attribute_resource_value);
    parseUntilNext(XmlResourceParser.START_TAG);

    assertThat(parser.getAttributeResourceValue(RES_AUTO_NS, "bar", 42)).isEqualTo(R.layout.main);
    assertThat(parser.getAttributeResourceValue(RES_AUTO_NS, "foo", 42)).isEqualTo(42);
  }

  @Test
  public void testGetAttributeResourceValueWhenNotAResource() throws Exception {
    forgeAndOpenDocument("<foo xmlns:app=\"http://schemas.android.com/apk/res-auto\" app:bar=\"banana\"/>");
    assertThat(parser.getAttributeResourceValue(RES_AUTO_NS, "bar", 42)).isEqualTo(42);
  }

  @Test
  public void testGetAttributeIntValue_StringStringInt() throws Exception {
    forgeAndOpenDocument("<foo xmlns:app=\"http://schemas.android.com/apk/res-auto\" app:app=\"http://schemas.android.com/apk/res-auto\" app:bar=\"-12\"/>");

    assertThat(parser.getAttributeIntValue(RES_AUTO_NS, "bar", 0)).isEqualTo(-12);
    assertThat(parser.getAttributeIntValue(RES_AUTO_NS, "foo", 0)).isEqualTo(0);
  }

  @Test
  public void testGetAttributeIntValue_IntInt() throws Exception {
    forgeAndOpenDocument("<foo bar=\"-12\"/>");

    assertThat(parser.getAttributeIntValue(0, 0)).isEqualTo(-12);

    assertThat(parser.getAttributeIntValue(attributeIndexOutOfIndex(), 0)).isEqualTo(0);

    forgeAndOpenDocument("<foo bar=\"unexpected\"/>");
    assertThat(parser.getAttributeIntValue(0, 0)).isEqualTo(0);
  }

  @Test
  public void testGetAttributeUnsignedIntValue_StringStringInt() throws Exception {
    forgeAndOpenDocument("<foo xmlns:app=\"http://schemas.android.com/apk/res-auto\" app:bar=\"12\"/>");

    assertThat(parser.getAttributeUnsignedIntValue(RES_AUTO_NS, "bar", 0)).isEqualTo(12);

    assertThat(parser.getAttributeUnsignedIntValue(RES_AUTO_NS, "foo", 0)).isEqualTo(0);

    // Negative unsigned int must be
    forgeAndOpenDocument("<foo xmlns:app=\"http://schemas.android.com/apk/res-auto\" app:bar=\"-12\"/>");

    assertThat(parser.getAttributeUnsignedIntValue(RES_AUTO_NS, "bar", 0))
        .named("Getting a negative number as unsigned should return the default value.").isEqualTo(0);
  }

  @Test
  public void testGetAttributeUnsignedIntValue_IntInt() throws Exception {
    forgeAndOpenDocument("<foo bar=\"12\"/>");

    assertThat(parser.getAttributeUnsignedIntValue(0, 0)).isEqualTo(12);

    assertThat(parser.getAttributeUnsignedIntValue(attributeIndexOutOfIndex(), 0)).isEqualTo(0);

    // Negative unsigned int must be
    forgeAndOpenDocument("<foo bar=\"-12\"/>");

    assertThat(parser.getAttributeUnsignedIntValue(0, 0))
        .named("Getting a negative number as unsigned should return the default value.").isEqualTo(0);
  }

  @Test
  public void testGetAttributeFloatValue_StringStringFloat() throws Exception {
    forgeAndOpenDocument("<foo xmlns:app=\"http://schemas.android.com/apk/res-auto\" app:bar=\"12.01\"/>");

    assertThat(parser.getAttributeFloatValue(RES_AUTO_NS, "bar", 0.0f)).isEqualTo(12.01f);

    assertThat(parser.getAttributeFloatValue(RES_AUTO_NS, "foo", 0.0f)).isEqualTo(0.0f);

    forgeAndOpenDocument("<foo bar=\"unexpected\"/>");
    assertThat(parser.getAttributeFloatValue(RES_AUTO_NS, "bar", 0.0f)).isEqualTo(0.0f);
  }

  @Test
  public void testGetAttributeFloatValue_IntFloat() throws Exception {
    forgeAndOpenDocument("<foo bar=\"12.01\"/>");

    assertThat(parser.getAttributeFloatValue(0, 0.0f)).isEqualTo(12.01f);

    assertThat(parser.getAttributeFloatValue(
        attributeIndexOutOfIndex(), 0.0f)).isEqualTo(0.0f);

    forgeAndOpenDocument("<foo bar=\"unexpected\"/>");
    assertThat(parser.getAttributeFloatValue(0, 0.0f)).isEqualTo(0.0f);
  }

  @Test
  public void testGetAttributeListValue_IntStringArrayInt() throws Exception {
    String[] options = {"foo", "bar"};
    forgeAndOpenDocument("<foo xmlns:app=\"http://schemas.android.com/apk/res-auto\" app:bar=\"bar\"/>");
    assertThat(parser.getAttributeListValue(0, options, 0)).isEqualTo(1);

    forgeAndOpenDocument("<foo xmlns:app=\"http://schemas.android.com/apk/res-auto\" app:bar=\"unexpected\"/>");
    assertThat(parser.getAttributeListValue(
        0, options, 0)).isEqualTo(0);

    assertThat(parser.getAttributeListValue(
        attributeIndexOutOfIndex(), options, 0)).isEqualTo(0);
  }

  @Test
  public void testGetIdAttribute() throws Exception {
    forgeAndOpenDocument("<foo/>");
    assertThat(parser.getIdAttribute()).isEqualTo(null);

    forgeAndOpenDocument("<foo id=\"bar\"/>");
    assertThat(parser.getIdAttribute()).isEqualTo("bar");
  }

  @Test
  public void testGetClassAttribute() throws Exception {
    forgeAndOpenDocument("<foo/>");
    assertThat(parser.getClassAttribute()).isEqualTo(null);

    forgeAndOpenDocument("<foo class=\"bar\"/>");
    assertThat(parser.getClassAttribute()).isEqualTo("bar");
  }

  @Test
  public void testGetIdAttributeResourceValue_defaultValue() throws Exception {
    assertThat(parser.getIdAttributeResourceValue(12)).isEqualTo(12);

    parser = context.getResources().getXml(R.xml.has_id);
    parseUntilNext(XmlResourceParser.START_TAG);
    assertThat(parser.getIdAttributeResourceValue(12)).isEqualTo(R.id.tacos);
  }

  @Test
  public void testGetStyleAttribute() throws Exception {
    forgeAndOpenDocument("<foo/>");
    assertThat(parser.getStyleAttribute()).isEqualTo(0);
  }

  @Test
  public void getStyleAttribute_allowStyleAttrReference() throws Exception {
    parser = context.getResources().getXml(R.xml.has_style_attribute_reference);
    parseUntilNext(XmlResourceParser.START_TAG);
    assertThat(parser.getStyleAttribute()).isEqualTo(R.attr.parentStyleReference);
  }

  @Test
  public void getStyleAttribute_allowStyleAttrReferenceLackingExplicitAttrType() throws Exception {
    parser = context.getResources().getXml(R.xml.has_parent_style_reference);
    parseUntilNext(XmlResourceParser.START_TAG);
    assertThat(parser.getStyleAttribute()).isEqualTo(R.attr.parentStyleReference);
  }

  @Test
  public void getStyleAttribute_withMeaninglessString_returnsZero() throws Exception {
    forgeAndOpenDocument("<foo style=\"android:style/whatever\"/>");
    assertThat(parser.getStyleAttribute()).isEqualTo(0);
  }
}
