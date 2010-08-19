package com.xtremelabs.droidsugar.util;

import android.view.View;
import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
import com.xtremelabs.droidsugar.R;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(DroidSugarAndroidTestRunner.class)
public class TestAttributeSetTest {
    private HashMap<String, String> attributes;
    private ResourceExtractor resourceExtractor;

    @Before public void setUp() throws Exception {
        attributes = new HashMap<String, String>();

        resourceExtractor = new ResourceExtractor();
        resourceExtractor.addRClass(R.class);
    }

    @Test
    public void getAttributeResourceValue_shouldReturnTheResourceValue() throws Exception {
        attributes.put("message", "@string/howdy");

        TestAttributeSet testAttributeSet = new TestAttributeSet(attributes, resourceExtractor, null, null);
        assertThat(testAttributeSet.getAttributeResourceValue("some namespace", "message", 0), equalTo(R.string.howdy));
    }

    @Test
    public void getAttributeResourceValue_withNamespace_shouldReturnTheResourceValue() throws Exception {
        attributes.put("xxx:message", "@string/howdy");

        TestAttributeSet testAttributeSet = new TestAttributeSet(attributes, resourceExtractor, null, null);
        assertThat(testAttributeSet.getAttributeResourceValue("some namespace", "message", 0), equalTo(R.string.howdy));
    }
    
    @Test
    public void getAttributeResourceValue_shouldReturnDefaultValueWhenNotInAttributeSet() throws Exception {
        TestAttributeSet testAttributeSet = new TestAttributeSet(attributes, resourceExtractor, null, null);
        assertThat(testAttributeSet.getAttributeResourceValue("some namespace", "message", -1), equalTo(-1));
    }

    @Test
    public void getAttributeBooleanValue_shouldGetBooleanValuesFromAttributes() throws Exception {
        attributes.put("isSugary", "true");

        TestAttributeSet testAttributeSet = new TestAttributeSet(attributes, null, null, null);
        assertThat(testAttributeSet.getAttributeBooleanValue("some namespace", "isSugary", false), equalTo(true));
    }

    @Test
    public void getAttributeBooleanValue_withNamespace_shouldGetBooleanValuesFromAttributes() throws Exception {
        attributes.put("xxx:isSugary", "true");

        TestAttributeSet testAttributeSet = new TestAttributeSet(attributes, null, null, null);
        assertThat(testAttributeSet.getAttributeBooleanValue("some namespace", "isSugary", false), equalTo(true));
    }

    @Test
    public void getAttributeBooleanValue_shouldReturnDefaultBooleanValueWhenNotInAttributeSet() throws Exception {
        TestAttributeSet testAttributeSet = new TestAttributeSet(attributes, null, null, null);
        assertThat(testAttributeSet.getAttributeBooleanValue("some namespace", "isSugary", true), equalTo(true));
    }

    @Test
    public void getAttributeValue_shouldReturnValueFromAttribute() throws Exception {
        attributes.put("isSugary", "oh heck yeah");

        TestAttributeSet testAttributeSet = new TestAttributeSet(attributes, null, null, null);
        assertThat(testAttributeSet.getAttributeValue("some namespace", "isSugary"), equalTo("oh heck yeah"));
    }

    @Test
    public void getAttributeIntValue_shouldReturnValueFromAttribute() throws Exception {
        attributes.put("sugarinessPercent", "100");

        AttrResourceLoader resourceLoader = new AttrResourceLoader(resourceExtractor);
        TestAttributeSet testAttributeSet = new TestAttributeSet(attributes, null, resourceLoader, View.class);
        assertThat(testAttributeSet.getAttributeIntValue("some namespace", "sugarinessPercent", 0), equalTo(100));
    }

    @Test
    public void getAttributeIntValue_shouldReturnEnumValuesForEnumAttributes() throws Exception {
        attributes.put("itemType", "string");

        System.out.println("" + new File(".").getAbsolutePath());
        AttrResourceLoader attrResourceLoader = new AttrResourceLoader(resourceExtractor);
        attrResourceLoader.loadDirs(new File("test/res/values"));
        TestAttributeSet testAttributeSet = new TestAttributeSet(attributes, null, attrResourceLoader, CustomView.class);
        assertThat(testAttributeSet.getAttributeIntValue("some namespace", "itemType", 0), equalTo(1));
    }
}
