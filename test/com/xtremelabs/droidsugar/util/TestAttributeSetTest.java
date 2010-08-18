package com.xtremelabs.droidsugar.util;

import com.xtremelabs.droidsugar.DroidSugarAndroidTestRunner;
import com.xtremelabs.droidsugar.R;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(DroidSugarAndroidTestRunner.class)
public class TestAttributeSetTest {
    @Test
    public void getAttributeResourceValue_shouldReturnTheResourceValue() throws Exception {
        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("message", "@string/howdy");

        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addRClass(R.class);
        
        TestAttributeSet testAttributeSet = new TestAttributeSet(attributes, resourceExtractor);
        assertThat(testAttributeSet.getAttributeResourceValue("some namespace", "message", 0), equalTo(R.string.howdy));
    }

    @Test
    public void getAttributeResourceValue_withNamespace_shouldReturnTheResourceValue() throws Exception {
        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("xxx:message", "@string/howdy");

        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addRClass(R.class);

        TestAttributeSet testAttributeSet = new TestAttributeSet(attributes, resourceExtractor);
        assertThat(testAttributeSet.getAttributeResourceValue("some namespace", "message", 0), equalTo(R.string.howdy));
    }
    
    @Test
    public void getAttributeResourceValue_shouldReturnDefaultValueWhenNotInAttributeSet() throws Exception {
        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addRClass(R.class);

        TestAttributeSet testAttributeSet = new TestAttributeSet(new HashMap<String, String>(), resourceExtractor);
        assertThat(testAttributeSet.getAttributeResourceValue("some namespace", "message", -1), equalTo(-1));
    }

    @Test
    public void getAttributeBooleanValue_shouldGetBooleanValuesFromAttributes() throws Exception {
        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("isSugary", "true");

        TestAttributeSet testAttributeSet = new TestAttributeSet(attributes, null);
        assertThat(testAttributeSet.getAttributeBooleanValue("some namespace", "isSugary", false), equalTo(true));
    }

    @Test
    public void getAttributeBooleanValue_withNamespace_shouldGetBooleanValuesFromAttributes() throws Exception {
        HashMap<String, String> attributes = new HashMap<String, String>();
        attributes.put("xxx:isSugary", "true");

        TestAttributeSet testAttributeSet = new TestAttributeSet(attributes, null);
        assertThat(testAttributeSet.getAttributeBooleanValue("some namespace", "isSugary", false), equalTo(true));
    }

    @Test
    public void getAttributeBooleanValue_shouldReturnDefaultBooleanValueWhenNotInAttributeSet() throws Exception {
        TestAttributeSet testAttributeSet = new TestAttributeSet(new HashMap<String, String>(), null);
        assertThat(testAttributeSet.getAttributeBooleanValue("some namespace", "isSugary", true), equalTo(true));
    }
}
