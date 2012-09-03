/**
 * 
 */
package com.xtremelabs.robolectric.res;


import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;

import javassist.compiler.Parser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xmlpull.v1.XmlPullParserException;

import android.content.res.XmlResourceParser;
import com.xtremelabs.robolectric.R;

/**
 * Test class for {@link XmlFileLoader}.
 * 
 * @author michele@swiftkey.net
 *
 */
public class XmlFileLoaderTest {

	private XmlFileLoader xmlFileLoader;
	private XmlResourceParser parser;
	
	private final String[] availableFeatures = new String[]{
			XmlResourceParser.FEATURE_PROCESS_NAMESPACES,
			XmlResourceParser.FEATURE_REPORT_NAMESPACE_ATTRIBUTES
	};
	
	private final String[] unavailableFeatures = new String[]{
			XmlResourceParser.FEATURE_PROCESS_DOCDECL,
			XmlResourceParser.FEATURE_VALIDATION 
	};
	
	@Before
	public void setUp() throws Exception {
        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addLocalRClass(R.class);
        resourceExtractor.addSystemRClass(android.R.class);

        xmlFileLoader = new XmlFileLoader(resourceExtractor);
        new DocumentLoader(xmlFileLoader).loadResourceXmlDir(resourceFile("res", "xml"));
        
        parser = xmlFileLoader.getXml("xml/preferences");
	}

	@After
	public void tearDown() throws Exception {
		parser.close();
	}

	private void parseUntilNextStartTag()
			throws XmlPullParserException, IOException {
		while(parser.next() != XmlResourceParser.START_TAG) {};
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
		for (String feature: availableFeatures) {
			parser.setFeature(feature, true);
			try {
				parser.setFeature(feature, false);
				fail(feature + " should be true.");
			} catch(XmlPullParserException ex) {
				// pass
			}
		}
		
		for (String feature: unavailableFeatures) {
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
		for (String feature: availableFeatures) {
			assertThat(parser.getFeature(feature), equalTo(true));
		}
		
		for (String feature: unavailableFeatures) {
			assertThat(parser.getFeature(feature), equalTo(false));
		}
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
	public void testGetText() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetLineNumber() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetEventType() throws XmlPullParserException {
		int evt = -1;
		while (evt != XmlResourceParser.END_DOCUMENT) {
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
	public void testGetTextCharacters() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetNamespace() {
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
				case (XmlResourceParser.START_TAG): {
					index ++;
					assertThat(parser.getName(), equalTo(expected[index]));
					break;
				}
			}
		}
	}

	@Test
	public void testGetAttributeNamespace()
			throws XmlPullParserException, IOException {
		parseUntilNextStartTag();
		assertThat(parser.getAttributeNamespace(0),
				equalTo("http://www.w3c.org/2000/xmlns/"));
	}

	@Test
	public void testGetAttributeName()
			throws XmlPullParserException, IOException {
		parseUntilNextStartTag();
		assertThat(parser.getAttributeName(0),
				equalTo("xmlns:android"));
	}

	@Test
	public void testGetAttributePrefix()
			throws XmlPullParserException, IOException {
		parseUntilNextStartTag();
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
		parseUntilNextStartTag();
		assertThat(
				"Not empty tag should return false.",
				parser.isEmptyElementTag(),
				equalTo(false));
		// Navigate to an empty tag
		parseUntilNextStartTag();
		parseUntilNextStartTag();
		parseUntilNextStartTag();
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
		parseUntilNextStartTag();
		assertThat(
				parser.getAttributeCount(),
				equalTo(1));
	}

	@Test
	public void testGetAttributeValueInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAttributeType() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsAttributeDefault() {
		fail("Not yet implemented");
	}

	@Test
	public void testNextToken() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAttributeValueStringString() {
		fail("Not yet implemented");
	}

	@Test
	public void testNext() {
		fail("Not yet implemented");
	}

	@Test
	public void testRequire() {
		fail("Not yet implemented");
	}

	@Test
	public void testNextText() {
		fail("Not yet implemented");
	}

	@Test
	public void testNextTag() {
		fail("Not yet implemented");
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
	public void testGetAttributeFloatValueIntFloat() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetIdAttribute() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetClassAttribute() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetIdAttributeResourceValue() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetStyleAttribute() {
		fail("Not yet implemented");
	}

}
