package com.xtremelabs.robolectric;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;

import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class IncludedDependenciesTest {
    @Test
    public void jsonShouldWork() throws Exception {
        assertEquals("value", new JSONObject("{'name':'value'}").getString("name"));
    }

    @Test
    public void xppShouldWork() throws Exception {
        XmlPullParser xmlPullParser = XmlPullParserFactory.newInstance().newPullParser();
        xmlPullParser.setInput(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?><test name=\"value\"/>"));
        assertEquals(XmlPullParser.START_TAG, xmlPullParser.nextTag());
        assertEquals(1, xmlPullParser.getAttributeCount());
        assertEquals("name", xmlPullParser.getAttributeName(0));
        assertEquals("value", xmlPullParser.getAttributeValue(0));
    }
}
