/**
 * 
 */
package com.xtremelabs.robolectric.res;


import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import android.content.res.XmlResourceParser;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.res.XmlFileLoader.XmlResourceParserImpl;

/**
 * Test class for {@link XmlFileLoader} and its inner 
 * class {@link XmlResourceParserImpl}. The tests verify
 * that this implementation will behave exactly as 
 * the android implementation.
 * 
 * <p>Please not that this implementation uses the resource file "xml/preferences"
 * to test the parser implementation. If that file is changed
 * some test may start failing.
 * 
 * @author msama (michele@swiftkey.net)
 */
public class XmlFileLoaderTest {

	private static final int ATTRIBUTE_OUT_OF_INDEX = 999;
	
	private XmlFileLoader xmlFileLoader;
	private XmlResourceParserImpl parser;
	
	@Before
	public void setUp() throws Exception {
        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addLocalRClass(R.class);
        resourceExtractor.addSystemRClass(android.R.class);

        xmlFileLoader = new XmlFileLoader(resourceExtractor);
        new DocumentLoader(xmlFileLoader).loadResourceXmlDir(resourceFile("res", "xml"));
        
        parser = (XmlResourceParserImpl)xmlFileLoader.getXml(R.xml.preferences);
	}

	@After
	public void tearDown() throws Exception {
		parser.close();
	}

	private void parseUntilNext(int event)
			throws XmlPullParserException, IOException {
		while(parser.next() != event) {
			if (parser.getEventType() == XmlResourceParser.END_DOCUMENT) {
				throw new RuntimeException("Impossible to find: " + 
						event + ". End of document reached.");
			}
		};
	}
	
	/**
	 * Test method for {@link com.xtremelabs.robolectric.res.XmlFileLoader#getXml(int)}.
	 * @throws IOException 
	 * @throws XmlPullParserException 
	 */
	@Test
	public void testGetXmlInt() throws XmlPullParserException, IOException {
		assertThat(parser, notNullValue());
		int evt = parser.next();
		assertThat(evt, equalTo(XmlResourceParser.START_DOCUMENT));
	}

	/**
	 * Test method for {@link com.xtremelabs.robolectric.res.XmlFileLoader#getXml(java.lang.String)}.
	 */
	@Test
	public void testGetXmlString() {
		XmlResourceParser parser = xmlFileLoader.getXml("xml/preferences");
		assertThat(parser, notNullValue());
	}

	@Test
	public void testSetFeature() throws XmlPullParserException {
		for (String feature: XmlFileLoader.AVAILABLE_FEATURES) {
			parser.setFeature(feature, true);
			try {
				parser.setFeature(feature, false);
				fail(feature + " should be true.");
			} catch(XmlPullParserException ex) {
				// pass
			}
		}
		
		for (String feature: XmlFileLoader.UNAVAILABLE_FEATURES) {
			try {
				parser.setFeature(feature, false);
				fail(feature + " should not be true.");
			} catch(XmlPullParserException ex) {
				// pass
			}
			try {
				parser.setFeature(feature, true);
				fail(feature + " should not be true.");
			} catch(XmlPullParserException ex) {
				// pass
			}
		}
	}

	@Test
	public void testGetFeature() {
		for (String feature: XmlFileLoader.AVAILABLE_FEATURES) {
			assertThat(parser.getFeature(feature), equalTo(true));
		}
		
		for (String feature: XmlFileLoader.UNAVAILABLE_FEATURES) {
			assertThat(parser.getFeature(feature), equalTo(false));
		}
		
		assertThat(parser.getFeature(null), equalTo(false));
	}

	@Test
	public void testSetProperty() {
		try {
			parser.setProperty("foo", "bar");
			fail("Properties should not be supported");
		} catch(XmlPullParserException ex) {
			// pass
		}
	}

	@Test
	public void testGetProperty() {
		// Properties are not supported
		assertThat(parser.getProperty("foo"), nullValue());
	}

	@Test
	public void testSetInput_Reader() {
		try {
			parser.setInput(new StringReader(""));
			fail("Properties should not be supported");
		} catch(XmlPullParserException ex) {
			// pass
		}
	}

	@Test
	public void testSetInput_InputStreamString() throws IOException {
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream("src/test/resources/res/xml/preferences.xml");
			parser.setInput(inputStream, "UTF-8");
			fail("Properties should not be supported");
		} catch(XmlPullParserException ex) {
			// pass
		} finally {
			inputStream.close();
		}
	}

	@Test
	public void testDefineEntityReplacementText() {
		try {
			parser.defineEntityReplacementText("foo", "bar");
			fail("This method should not be supported");
		} catch(XmlPullParserException ex) {
			// pass
		}
	}

	@Test
	public void testGetNamespacePrefix() {
		try {
			parser.getNamespacePrefix(0);
			fail("This method should not be supported");
		} catch(XmlPullParserException ex) {
			// pass
		}
	}

	@Test
	public void testGetInputEncoding() {
		assertThat(parser.getInputEncoding(), nullValue());
	}

	@Test
	public void testGetNamespace_String() {
		try {
			parser.getNamespace("bar");
			fail("This method should not be supported");
		} catch(RuntimeException ex) {
			// pass
		}
	}

	@Test
	public void testGetNamespaceCount() {
		try {
			parser.getNamespaceCount(0);
			fail("This method should not be supported");
		} catch(XmlPullParserException ex) {
			// pass
		}
	}

	@Test
	public void testGetNamespaceUri() {
		try {
			parser.getNamespaceUri(0);
			fail("This method should not be supported");
		} catch(XmlPullParserException ex) {
			// pass
		}
	}

	@Test
	public void testGetColumnNumber() {
		assertThat(parser.getColumnNumber(), equalTo(-1));
	}

	@Test
	public void testGetDepth() throws XmlPullParserException, IOException {
		int[] expected = new int[] {
				// Depths of preference file elements
				1, 2, 3, 2, 2, 2, 2, 2
		};
		int index = -1;
		int evt;
		while ((evt = parser.next()) != XmlResourceParser.END_DOCUMENT) {
			switch (evt) {
				case (XmlResourceParser.START_TAG): {
					index ++;
					assertThat(parser.getDepth(), equalTo(expected[index]));
					break;
				}
			}
			
		}
	}

	@Test
	public void testGetText() throws XmlPullParserException, IOException {
		assertThat(parser.getText(), equalTo(null));
		parseUntilNext(XmlResourceParser.START_TAG);
		assertThat(parser.getText(), equalTo(null));
		// TODO(msama): Test a node with text
		// parseUntilNext(XmlResourceParser.TEXT);
		// assertThat(parser.getText(), notNullValue());
	}

	@Test
	@Ignore("Not implemented yet")
	public void testGetLineNumber() throws XmlPullParserException, IOException {
		assertThat(parser.getLineNumber(), equalTo(-1));
		parseUntilNext(XmlResourceParser.START_TAG);
		assertThat(
				"The root element should be at line 1.",
				parser.getLineNumber(), equalTo(1));
	}

	@Test
	public void testGetEventType() throws XmlPullParserException, IOException {
		int evt;
		while ((evt = parser.next()) != XmlResourceParser.END_DOCUMENT) {
			assertThat(parser.getEventType(), equalTo(evt));
		}
	}

	@Test
	public void testIsWhitespace() throws XmlPullParserException {
		assertThat(parser.isWhitespace(), equalTo(false));
	}

	@Test
	public void testGetPrefix() {
		try {
			parser.getPrefix();
			fail("This method should not be supported");
		} catch(RuntimeException ex) {
			// pass
		}
	}

	@Test
	public void testGetNamespace() throws XmlPullParserException, IOException {
		assertThat(parser.getNamespace(), equalTo(""));
		parseUntilNext(XmlResourceParser.START_TAG);
		assertThat(parser.getNamespace(), equalTo(""));
	}

	@Test
	public void testGetName() throws XmlPullParserException, IOException {
		String[] expected = new String[] {
				"PreferenceScreen",
				"PreferenceCategory",
				"Preference",
				"CheckBoxPreference",
				"EditTextPreference",
				"ListPreference",
				"Preference",
				"RingtonePreference"
		};
		int index = -1;
		int evt;
		while ((evt = parser.next()) != XmlResourceParser.END_DOCUMENT) {
			switch (evt) {
				case (XmlResourceParser.START_DOCUMENT): {
					assertThat(parser.getName(), equalTo(""));
					break;
				}
				case (XmlResourceParser.START_TAG): {
					index ++;
					assertThat(parser.getName(), equalTo(expected[index]));
					break;
				}
			}
		}
		assertThat(
				"End document name should be empty.",
				parser.getName(), equalTo(""));
	}
	
	@Test
	public void testGetAttribute() throws XmlPullParserException, IOException {
		parseUntilNext(XmlResourceParser.START_TAG);
		assertThat(
				parser.getAttribute(
						"http://www.w3.org/2000/xmlns/",
						"xmlns:android"),
				notNullValue());
	}

	@Test
	public void testGetAttributeNamespace()
			throws XmlPullParserException, IOException {
		parseUntilNext(XmlResourceParser.START_TAG);
		assertThat(parser.getAttributeNamespace(0),
				equalTo("http://www.w3.org/2000/xmlns/"));
	}

	@Test
	public void testGetAttributeName()
			throws XmlPullParserException, IOException {
		parseUntilNext(XmlResourceParser.START_TAG);
		assertThat(parser.getAttributeName(0),
				equalTo("xmlns:android"));
		try {
			parser.getAttributeName(ATTRIBUTE_OUT_OF_INDEX);
		} catch (IndexOutOfBoundsException ex) {
			// pass
		}
	}

	@Test
	public void testGetAttributePrefix()
			throws XmlPullParserException, IOException {
		parseUntilNext(XmlResourceParser.START_TAG);
		try {
			parser.getAttributePrefix(0);
			fail("This method should not be supported");
		} catch(RuntimeException ex) {
			// pass
		}
	}

	@Test
	public void testIsEmptyElementTag()
			throws XmlPullParserException, IOException {
		assertThat(
				"Not START_TAG should return false.",
				parser.isEmptyElementTag(),
				equalTo(false));
		parseUntilNext(XmlResourceParser.START_TAG);
		assertThat(
				"Not empty tag should return false.",
				parser.isEmptyElementTag(),
				equalTo(false));
		// Navigate to an empty tag
		parseUntilNext(XmlResourceParser.START_TAG);
		parseUntilNext(XmlResourceParser.START_TAG);
		parseUntilNext(XmlResourceParser.START_TAG);
		assertThat(
				"Expected CheckBoxPreference.",
				parser.getName(),
				equalTo("CheckBoxPreference"));
		assertThat(
				"In the Android implementation this method always return false.",
				parser.isEmptyElementTag(),
				equalTo(false));
	}

	@Test
	public void testGetAttributeCount()
			throws XmlPullParserException, IOException {
		assertThat(
				"When no node is being explored the number " +
				"of attributes should be -1.",
				parser.getAttributeCount(),
				equalTo(-1));
		parseUntilNext(XmlResourceParser.START_TAG);
		assertThat(
				parser.getAttributeCount(),
				equalTo(1));
	}

	@Test
	public void testGetAttributeValueInt() {
		assertThat(
				parser.getAttributeBooleanValue(ATTRIBUTE_OUT_OF_INDEX, true),
				equalTo(true));
	}

	@Test
	public void testGetAttributeType() {
		assertThat(
				parser.getAttributeType(ATTRIBUTE_OUT_OF_INDEX),
				equalTo("CDATA"));
	}

	@Test
	public void testIsAttributeDefault() {
		assertThat(
				parser.isAttributeDefault(ATTRIBUTE_OUT_OF_INDEX),
				equalTo(false));
	}

	@Test
	public void testGetAttributeValueStringString()
			throws XmlPullParserException, IOException {
		parseUntilNext(XmlResourceParser.START_TAG);
		assertThat(parser.getAttributeValue("http://www.w3.org/2000/xmlns/", "xmlns:android"),
				equalTo("http://schemas.android.com/apk/res/android"));
	}

	@Test
	public void testNext() throws XmlPullParserException, IOException {
		int lastEvent = -1;
		int evt = -1;
		Stack<String> tags = new Stack<String>(); 
		
		while ((evt = parser.next()) != XmlResourceParser.END_DOCUMENT) {
			switch (evt) {
				case (XmlResourceParser.START_DOCUMENT): {
					assertThat(lastEvent, equalTo(-1));
					break;
				}
				case (XmlResourceParser.START_TAG): {
					tags.push(parser.getName());
					break;
				}
				case (XmlResourceParser.END_TAG): {
					String tag = tags.pop();
					String current = parser.getName();
					assertThat(
							"Closing the wrong tag: found: " + 
									current + ", expected: " + tag + ".",
							current, equalTo(tag));
					break;
				}
				case (XmlResourceParser.TEXT): {
					assertThat(lastEvent,
							anyOf(
								equalTo(XmlResourceParser.START_TAG),
								equalTo(XmlResourceParser.END_TAG)));
					break;
				}
			}
			lastEvent = evt;
		}
	}

	@Test
	public void testRequire() throws XmlPullParserException, IOException {
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
	public void testNextText_noText() throws XmlPullParserException, IOException {
		parseUntilNext(XmlResourceParser.START_TAG);
		try {
			assertThat(parser.nextText(), equalTo(parser.getText()));
			fail("nextText on a document with no text should have failed");
		} catch (XmlPullParserException ex) {
			assertThat(parser.getEventType(),
					anyOf(equalTo(XmlResourceParser.START_TAG),
							equalTo(XmlResourceParser.END_DOCUMENT)));
		}
	}

	@Test
	public void testNextTag() throws XmlPullParserException, IOException {
		Set<Integer> acceptableTags = new HashSet<Integer>();
		acceptableTags.add(XmlResourceParser.START_TAG);
		acceptableTags.add(XmlResourceParser.END_TAG);
		parseUntilNext(XmlResourceParser.START_DOCUMENT);
		int evt;
		int max = 5;
		for (int i = max; i > 0; i--) {
			evt = parser.nextTag();
			assertTrue(acceptableTags.contains(evt));
		}
	}

	@Test
	public void testGetAttributeNameResource() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAttributeListValueStringStringStringArrayInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAttributeBooleanValueStringStringBoolean() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAttributeResourceValueStringStringInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAttributeIntValueStringStringInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAttributeUnsignedIntValueStringStringInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAttributeFloatValueStringStringFloat() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAttributeListValueIntStringArrayInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAttributeBooleanValueIntBoolean() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAttributeResourceValueIntInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAttributeIntValueIntInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAttributeUnsignedIntValueIntInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAttributeFloatValueIntFloat()
			throws XmlPullParserException, IOException {
		parseUntilNext(XmlResourceParser.START_TAG);
		assertThat(parser.getAttributeFloatValue(0, 1.0f), equalTo(1.0f));
		// TODO(msama): test an actual read value
	}

	@Test
	public void testGetIdAttribute() throws XmlPullParserException, IOException {
		assertThat(
				"Document should have no id.",
				parser.getIdAttribute(), equalTo(null));
		parseUntilNext(XmlResourceParser.START_TAG);
		assertThat(
				"Root element have no id.",
				parser.getIdAttribute(), equalTo(null));
		// TODO(msama): Test an element with a real ID
		// None of the preferences elements have id
	}

	@Test
	public void testGetClassAttribute() throws XmlPullParserException, IOException {
		assertThat(
				"Document should have no class.",
				parser.getClassAttribute(), equalTo(null));
		parseUntilNext(XmlResourceParser.START_TAG);
		assertThat(
				"Root element have no id.",
				parser.getClassAttribute(), equalTo(null));
		// TODO(msama): Test an element with a class attribute
		// None of the preferences elements have a class attribute
	}

	@Test
	public void testGetIdAttributeResourceValue() {
		assertThat(
				parser.getIdAttributeResourceValue(12), equalTo(12));
	}

	@Test
	public void testGetStyleAttribute() {
		assertThat(
				parser.getStyleAttribute(), equalTo(0));
		// TODO(msama): test with an element with style attribute
		// None of the preferences elements have a style attribute
	}

}
