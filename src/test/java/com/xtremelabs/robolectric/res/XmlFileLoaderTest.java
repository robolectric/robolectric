/**
 * 
 */
package com.xtremelabs.robolectric.res;


import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
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
	
	private void forgeNode(String xmlValue)
			throws XmlPullParserException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        factory.setNamespaceAware(true);
	        factory.setIgnoringComments(true);
	        factory.setIgnoringElementContentWhitespace(true);
	        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
	        Document document = documentBuilder.parse(
	        		new ByteArrayInputStream(xmlValue.getBytes()));
	        
	        parser = xmlFileLoader.new XmlResourceParserImpl(document);
	        parseUntilNext(XmlResourceParser.START_TAG);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private int attributeIndexOutOfIndex() {
		return parser.getAttributeCount() + 1;
	};
	
	@Test
	public void testGetXmlInt() throws XmlPullParserException, IOException {
		assertThat(parser, notNullValue());
		int evt = parser.next();
		assertThat(evt, equalTo(XmlResourceParser.START_DOCUMENT));
	}

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
			fail("This method should not be supported");
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
			fail("This method should not be supported");
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
		forgeNode("<foo/>");
		assertThat(parser.getText(), equalTo(""));
		
		forgeNode("<foo>bar</foo>");
		assertThat(parser.getText(), equalTo("bar"));
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
		assertThat(parser.isWhitespace("bar"), equalTo(false));
		assertThat(parser.isWhitespace(" "), equalTo(true));
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
		forgeNode("<foo xmlns=\"http://www.w3.org/1999/xhtml\">bar</foo>");
		assertThat(parser.getNamespace(),
				equalTo("http://www.w3.org/1999/xhtml"));
	}

	@Test
	public void testGetName_atStart()
			throws XmlPullParserException, IOException {
		assertThat(parser.getName(), equalTo(""));
		parseUntilNext(XmlResourceParser.START_DOCUMENT);
		assertThat(parser.getName(), equalTo(""));
	}
	
	@Test
	public void testGetName() throws XmlPullParserException, IOException {
		forgeNode("<foo/>");
		assertThat(parser.getName(), equalTo("foo"));
	}
	
	
	@Test
	public void testGetAttribute() throws XmlPullParserException, IOException {
		forgeNode("<foo xmlns:bar=\"bar\"/>");
		assertThat(
				parser.getAttribute(
						"http://www.w3.org/2000/xmlns/",
						"xmlns:bar").getNodeValue(),
				equalTo("bar"));
	}

	@Test
	public void testGetAttributeNamespace()
			throws XmlPullParserException, IOException {
		forgeNode("<foo xmlns:bar=\"bar\"/>");
		assertThat(parser.getAttributeNamespace(0),
				equalTo("http://www.w3.org/2000/xmlns/"));
	}

	@Test
	public void testGetAttributeName()
			throws XmlPullParserException, IOException {
		assertThat(parser.getAttributeName(0),
				nullValue());
		
		forgeNode("<foo bar=\"bar\"/>");
		assertThat(parser.getAttributeName(0), equalTo("bar"));
		assertThat(parser.getAttributeName(attributeIndexOutOfIndex()),
				nullValue());
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
				"Before START_DOCUMENT should return false.",
				parser.isEmptyElementTag(),
				equalTo(false));
			
		forgeNode("<foo><bar/></foo>");
		assertThat(
				"Not empty tag should return false.",
				parser.isEmptyElementTag(),
				equalTo(false));
		
		forgeNode("<foo/>");
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
		
		forgeNode("<foo bar=\"bar\"/>");
		assertThat(
				parser.getAttributeCount(),
				equalTo(1));
	}

	@Test
	public void testGetAttributeValue_Int()
			throws XmlPullParserException {
		forgeNode("<foo bar=\"bar\"/>");
		assertThat(
				parser.getAttributeValue(0),
				equalTo("bar"));
		
		try {
			parser.getAttributeValue(attributeIndexOutOfIndex());
		} catch (IndexOutOfBoundsException ex) {
			// pass
		}
	}

	@Test
	public void testGetAttributeType() {
		// Hardcoded to always return CDATA
		assertThat(
				parser.getAttributeType(attributeIndexOutOfIndex()),
				equalTo("CDATA"));
	}

	@Test
	public void testIsAttributeDefault() {
		assertThat(
				parser.isAttributeDefault(attributeIndexOutOfIndex()),
				equalTo(false));
	}

	@Test
	public void testGetAttributeValueStringString()
			throws XmlPullParserException, IOException {
		forgeNode("<foo xmlns:bar=\"bar\"/>");
		assertThat(
				parser.getAttributeValue(
						"http://www.w3.org/2000/xmlns/", "xmlns:bar"),
				equalTo("bar"));
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
	@Ignore("Not yet implemented")
	public void testGetAttributeNameResource() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAttributeListValue_StringStringStringArrayInt()
			throws XmlPullParserException, IOException {
		String[] options = {"foo", "bar"};
		forgeNode("<foo xmlns:bar=\"bar\"/>");
		assertThat(
				parser.getAttributeListValue(
						"http://www.w3.org/2000/xmlns/", 
						"xmlns:bar", options, 0),
				equalTo(1));
		
		forgeNode("<foo xmlns:bar=\"unexpected\"/>");
		assertThat(
				parser.getAttributeListValue(
						"http://www.w3.org/2000/xmlns/", 
						"xmlns:bar", options, 0),
				equalTo(0));
	}

	@Test
	public void testGetAttributeBooleanValue_StringStringBoolean()
			throws XmlPullParserException, IOException {
		forgeNode("<foo xmlns:bar=\"true\"/>");
		assertThat(
				parser.getAttributeBooleanValue("http://www.w3.org/2000/xmlns/", 
				"xmlns:bar", false), equalTo(true));
		assertThat(
				parser.getAttributeBooleanValue("http://www.w3.org/2000/xmlns/", 
				"xmlns:foo", false), equalTo(false));
	}

	@Test
	public void testGetAttributeBooleanValue_IntBoolean()
			throws XmlPullParserException {
		forgeNode("<foo bar=\"true\"/>");
		assertThat(
				parser.getAttributeBooleanValue(0, false),
				equalTo(true));
		assertThat(
				parser.getAttributeBooleanValue(attributeIndexOutOfIndex(), false),
				equalTo(false));
	}
	
	@Test
	@Ignore("Not yet implemented")
	public void testGetAttributeResourceValueStringStringInt() {
		fail("Not yet implemented");
	}
	
	@Test
	@Ignore("Not yet implemented")
	public void testGetAttributeResourceValueIntInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetAttributeIntValue_StringStringInt()
			throws XmlPullParserException {
		forgeNode("<foo xmlns:bar=\"-12\"/>");
		
		assertThat(
				parser.getAttributeIntValue(
						"http://www.w3.org/2000/xmlns/", 
						"xmlns:bar", 0),
				equalTo(-12));
		
		assertThat(
				parser.getAttributeIntValue(
						"http://www.w3.org/2000/xmlns/", 
						"xmlns:foo", 0),
				equalTo(0));
	}
	

	@Test
	public void testGetAttributeIntValue_IntInt()
			throws XmlPullParserException {
		forgeNode("<foo bar=\"-12\"/>");
		
		assertThat(
				parser.getAttributeIntValue(0, 0),
				equalTo(-12));
		
		assertThat(
				parser.getAttributeIntValue(attributeIndexOutOfIndex(), 0),
				equalTo(0));
		
		forgeNode("<foo bar=\"unexpected\"/>");
		assertThat(
				parser.getAttributeIntValue(0, 0),
				equalTo(0));
	}
	
	@Test
	public void testGetAttributeUnsignedIntValue_StringStringInt()
			throws XmlPullParserException {
		forgeNode("<foo xmlns:bar=\"12\"/>");
		
		assertThat(
				parser.getAttributeUnsignedIntValue(
						"http://www.w3.org/2000/xmlns/", 
						"xmlns:bar", 0),
				equalTo(12));
		
		assertThat(
				parser.getAttributeUnsignedIntValue(
						"http://www.w3.org/2000/xmlns/", 
						"xmlns:foo", 0),
				equalTo(0));
		
		// Negative unsigned int must be
		forgeNode("<foo xmlns:bar=\"-12\"/>");
		
		assertThat(
				"Getting a negative number as unsigned should " +
						"return the default value.",
				parser.getAttributeUnsignedIntValue(
						"http://www.w3.org/2000/xmlns/", 
						"xmlns:bar", 0),
				equalTo(0));
	}

	@Test
	public void testGetAttributeUnsignedIntValue_IntInt()
			throws XmlPullParserException {
		forgeNode("<foo bar=\"12\"/>");
		
		assertThat(
				parser.getAttributeUnsignedIntValue(0, 0),
				equalTo(12));
		
		assertThat(
				parser.getAttributeUnsignedIntValue(
						attributeIndexOutOfIndex(), 0),
				equalTo(0));
		
		// Negative unsigned int must be
		forgeNode("<foo bar=\"-12\"/>");
		
		assertThat(
				"Getting a negative number as unsigned should " +
						"return the default value.",
				parser.getAttributeUnsignedIntValue(0, 0),
				equalTo(0));
	}
	
	@Test
	public void testGetAttributeFloatValue_StringStringFloat()
			throws XmlPullParserException {
		forgeNode("<foo xmlns:bar=\"12.01\"/>");
		
		assertThat(
				parser.getAttributeFloatValue(
						"http://www.w3.org/2000/xmlns/", 
						"xmlns:bar", 0.0f),
				equalTo(12.01f));
		
		assertThat(
				parser.getAttributeFloatValue(
						"http://www.w3.org/2000/xmlns/", 
						"xmlns:foo", 0.0f),
				equalTo(0.0f));
		
		forgeNode("<foo bar=\"unexpected\"/>");
		assertThat(
				parser.getAttributeFloatValue(
						"http://www.w3.org/2000/xmlns/", 
						"xmlns:bar", 0.0f),
				equalTo(0.0f));
	}

	@Test
	public void testGetAttributeFloatValue_IntFloat()
			throws XmlPullParserException, IOException {
		forgeNode("<foo bar=\"12.01\"/>");
		
		assertThat(
				parser.getAttributeFloatValue(0, 0.0f),
				equalTo(12.01f));
		
		assertThat(
				parser.getAttributeFloatValue(
						attributeIndexOutOfIndex(), 0.0f),
				equalTo(0.0f));
		
		forgeNode("<foo bar=\"unexpected\"/>");
		assertThat(
				parser.getAttributeFloatValue(0, 0.0f),
				equalTo(0.0f));
	}
	
	@Test
	public void testGetAttributeListValue_IntStringArrayInt()
			throws XmlPullParserException {
		String[] options = {"foo", "bar"};
		forgeNode("<foo xmlns:bar=\"bar\"/>");
		assertThat(
				parser.getAttributeListValue(0, options, 0),
				equalTo(1));

		forgeNode("<foo xmlns:bar=\"unexpected\"/>");
		assertThat(
				parser.getAttributeListValue(
						0, options, 0),
				equalTo(0));
		
		assertThat(
				parser.getAttributeListValue(
						attributeIndexOutOfIndex(), options, 0),
				equalTo(0));
	}

	@Test
	public void testGetIdAttribute() throws XmlPullParserException, IOException {
		forgeNode("<foo/>");
		assertThat(parser.getIdAttribute(), equalTo(null));
		
		forgeNode("<foo id=\"bar\"/>");
		assertThat(parser.getIdAttribute(), equalTo("bar"));
	}

	@Test
	public void testGetClassAttribute() throws XmlPullParserException, IOException {
		forgeNode("<foo/>");
		assertThat(parser.getClassAttribute(), equalTo(null));
		
		forgeNode("<foo class=\"bar\"/>");
		assertThat(parser.getClassAttribute(), equalTo("bar"));
	}

	@Test
	public void testGetIdAttributeResourceValue_defaultValue() {
		assertThat(
				parser.getIdAttributeResourceValue(12), equalTo(12));
	}

	@Test
	public void testGetStyleAttribute()
			throws XmlPullParserException {
		forgeNode("<foo/>");
		assertThat(parser.getStyleAttribute(), equalTo(0));
	}

}
