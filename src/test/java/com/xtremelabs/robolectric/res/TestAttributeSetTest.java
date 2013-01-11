package com.xtremelabs.robolectric.res;

import android.view.View;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.TestRunners;
import com.xtremelabs.robolectric.tester.android.util.Attribute;
import com.xtremelabs.robolectric.tester.android.util.TestAttributeSet;
import com.xtremelabs.robolectric.util.CustomView;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.util.TestUtil.*;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class TestAttributeSetTest {

    private TestAttributeSet testAttributeSet;
    private ResourceLoader resourceLoader;

    @Before
    public void setUp() throws Exception {
        resourceLoader = new PackageResourceLoader(testResources(), systemResources());
    }

    @Test
    public void getSystemAttributeResourceValue_shouldReturnTheResourceValue() throws Exception {
        createTestAttributeSet(new Attribute("android:attr/text", "@android:string/ok", TEST_PACKAGE));
        assertThat(testAttributeSet.getAttributeResourceValue("android", "text", 0), equalTo(android.R.string.ok));
    }

    @Test
    public void getAttributeResourceValue_shouldReturnTheResourceValue() throws Exception {
        createTestAttributeSet(new Attribute("android:attr/text", "@string/ok", TEST_PACKAGE));
        assertThat(testAttributeSet.getAttributeResourceValue("android", "text", 0), equalTo(R.string.ok));
    }

    @Test
    public void getSystemAttributeResourceValue_shouldNotReturnTheResourceValueIfNameSpaceDoesNotMatch() throws Exception {
        createTestAttributeSet(new Attribute("com.another.domain:attr/text", "@android:string/ok", TEST_PACKAGE));
        assertThat(testAttributeSet.getAttributeResourceValue("android", "text", 0), equalTo(0));
    }

    @Test
    public void getAttributeResourceValue_shouldReturnTheResourceValueFromSystemNamespace() throws Exception {
        createTestAttributeSet(new Attribute("com.another.domain:attr/text", "@android:string/ok", TEST_PACKAGE));
        assertThat(testAttributeSet.getAttributeResourceValue("com.another.domain", "text", 0), equalTo(android.R.string.ok));
    }

    @Test
    public void getSystemAttributeResourceValue_shouldReturnDefaultValueForNullResourceId() throws Exception {
        createTestAttributeSet(new Attribute("android:attr/text", "@null", TEST_PACKAGE));
        assertThat(testAttributeSet.getAttributeResourceValue("com.some.namespace", "text", 0), equalTo(0));
    }

    @Test
    public void getSystemAttributeResourceValue_shouldReturnValueForMatchingNamespace() throws Exception {
        createTestAttributeSet(new Attribute("com.some.namespace:attr/id", "@id/burritos", TEST_PACKAGE));
        assertThat(testAttributeSet.getAttributeResourceValue("com.some.namespace", "id", 0), equalTo(R.id.burritos));
    }

    @Test
    public void getSystemAttributeResourceValue_shouldReturnDefaultValueForNonMatchingNamespaceId() throws Exception {
        createTestAttributeSet(new Attribute("com.some.namespace:attr/id", "@id/burritos", TEST_PACKAGE));
        assertThat(testAttributeSet.getAttributeResourceValue("com.some.other.namespace", "id", 0), equalTo(0));
    }

    @Test
    public void shouldCopeWithDefiningSystemIds() throws Exception {
        createTestAttributeSet(new Attribute("android:attr/id", "@+id/text1", SYSTEM_PACKAGE));
        assertThat(testAttributeSet.getAttributeResourceValue("android", "id", 0), equalTo(android.R.id.text1));
    }

    @Test
    public void shouldCopeWithDefiningLocalIds() throws Exception {
        createTestAttributeSet(new Attribute("android:attr/id", "@+id/text1", TEST_PACKAGE));
        assertThat(testAttributeSet.getAttributeResourceValue("android", "id", 0), equalTo(R.id.text1));
    }

    @Test
    public void getAttributeResourceValue_withNamespace_shouldReturnTheResourceValue() throws Exception {
        createTestAttributeSet(new Attribute(TEST_PACKAGE + ":attr/message", "@string/howdy", TEST_PACKAGE));
        assertThat(testAttributeSet.getAttributeResourceValue(TEST_PACKAGE, "message", 0), equalTo(R.string.howdy));
    }

    @Test
    public void getAttributeResourceValue_shouldReturnDefaultValueWhenNotInAttributeSet() throws Exception {
        createTestAttributeSet();
        assertThat(testAttributeSet.getAttributeResourceValue("com.some.namespace", "message", -1), equalTo(-1));
    }

    @Test
    public void getAttributeBooleanValue_shouldGetBooleanValuesFromAttributes() throws Exception {
        createTestAttributeSet(new Attribute(TEST_PACKAGE + ":attr/isSugary", "true", TEST_PACKAGE));
        assertThat(testAttributeSet.getAttributeBooleanValue(TEST_PACKAGE, "isSugary", false), equalTo(true));
    }

    @Test
    public void getAttributeBooleanValue_withNamespace_shouldGetBooleanValuesFromAttributes() throws Exception {
        createTestAttributeSet(new Attribute("xxx:attr/isSugary", "true", TEST_PACKAGE));
        assertThat(testAttributeSet.getAttributeBooleanValue("xxx", "isSugary", false), equalTo(true));
    }

    @Test
    public void getAttributeBooleanValue_shouldReturnDefaultBooleanValueWhenNotInAttributeSet() throws Exception {
        createTestAttributeSet();
        assertThat(testAttributeSet.getAttributeBooleanValue("com.some.namespace", "isSugary", true), equalTo(true));
    }

    @Test
    public void getAttributeValue_byName_shouldReturnValueFromAttribute() throws Exception {
        createTestAttributeSet(new Attribute(TEST_PACKAGE + ":attr/isSugary", "oh heck yeah", TEST_PACKAGE));
        assertThat(testAttributeSet.getAttributeValue(TEST_PACKAGE, "isSugary"), equalTo("oh heck yeah"));
    }

    @Test
    public void getAttributeValue_byId_shouldReturnValueFromAttribute() throws Exception {
        createTestAttributeSet(new Attribute(TEST_PACKAGE + ":attr/isSugary", "oh heck yeah", TEST_PACKAGE));
        assertThat(testAttributeSet.getAttributeValue(0), equalTo("oh heck yeah"));
    }

    @Test
    public void getAttributeIntValue_shouldReturnValueFromAttribute() throws Exception {
        testAttributeSet = new TestAttributeSet(asList(new Attribute(TEST_PACKAGE + ":attr/sugarinessPercent", "100", TEST_PACKAGE)),
                resourceLoader, null);
        assertThat(testAttributeSet.getAttributeIntValue(TEST_PACKAGE, "sugarinessPercent", 0), equalTo(100));
    }

    @Test
    public void getAttributeIntValue_shouldReturnHexValueFromAttribute() throws Exception {
        testAttributeSet = new TestAttributeSet(asList(new Attribute(TEST_PACKAGE + ":attr/sugarinessPercent", "0x10", TEST_PACKAGE)),
                resourceLoader, null);
        assertThat(testAttributeSet.getAttributeIntValue(TEST_PACKAGE, "sugarinessPercent", 0), equalTo(16));
    }

    @Test
    public void getAttributeIntValue_shouldReturnStyledValueFromAttribute() throws Exception {
        testAttributeSet = new TestAttributeSet(asList(
                new Attribute(TEST_PACKAGE + ":attr/gravity", "center|fill_vertical", TEST_PACKAGE),
                new Attribute("android:attr/orientation", "vertical", TEST_PACKAGE)
        ), resourceLoader, CustomView.class);
        assertThat(testAttributeSet.getAttributeIntValue(TEST_PACKAGE, "gravity", 0), equalTo(0x11 | 0x70));
        assertThat(testAttributeSet.getAttributeIntValue("android", "orientation", -1), equalTo(1)); // style from LinearLayout
    }

    @Ignore
    @Test
    public void getAttributeIntValue_shouldNotReturnStyledValueFromAttributeForSuperclass() throws Exception {
        testAttributeSet = new TestAttributeSet(asList(new Attribute(TEST_PACKAGE + ":attr/gravity", "center|fill_vertical", TEST_PACKAGE)),
                resourceLoader, View.class);
        assertThat(testAttributeSet.getAttributeIntValue(TEST_PACKAGE, "gravity", 0), equalTo(0)); // todo: what do we expect here?
    }

    @Test
    public void getAttributeIntValue_shouldReturnEnumValuesForEnumAttributes() throws Exception {
        testAttributeSet = new TestAttributeSet(asList(new Attribute(TEST_PACKAGE + ":attr/itemType", "ungulate", TEST_PACKAGE)),
                resourceLoader, CustomView.class);
        assertThat(testAttributeSet.getAttributeIntValue(TEST_PACKAGE, "itemType", 0), equalTo(1));
    }

    @Test
    public void getAttributeValue_shouldReturnAttributeAssociatedWithResourceId() throws Exception {
        createTestAttributeSet(new Attribute("ns:attr/textStyle2", "expected value", TEST_PACKAGE));
        assertThat(testAttributeSet.getAttributeValue(0), equalTo("expected value"));
    }

    @Test
    public void getAttributeValue_shouldReturnNullIfNoAttributeSet() throws Exception {
        createTestAttributeSet();
        int nonExistantResource = 12345;
        assertThat(testAttributeSet.getAttributeValue(nonExistantResource), nullValue());
    }

    @Test
    public void getAttributeIntValue_shouldReturnValueFromAttributeWhenNotInAttributeSet() throws Exception {
        createTestAttributeSet();
        assertThat(testAttributeSet.getAttributeIntValue("some namespace", "sugarinessPercent", 42), equalTo(42));
    }

    @Test
    public void getAttributeIntValue_shouldReturnEnumValuesForEnumAttributesWhenNotInAttributeSet() throws Exception {
        createTestAttributeSet();
        assertThat(testAttributeSet.getAttributeIntValue("some namespace", "itemType", 24), equalTo(24));
    }

    @Test
    public void getAttributeFloatValue_shouldGetFloatValuesFromAttributes() throws Exception {
        createTestAttributeSet(new Attribute(TEST_PACKAGE + ":attr/sugaryScale", "1234.456", TEST_PACKAGE));
        assertThat(testAttributeSet.getAttributeFloatValue(TEST_PACKAGE, "sugaryScale", 78.9f), equalTo(1234.456f));
    }

    @Test
    public void getAttributeFloatValue_withNamespace_shouldGetFloatValuesFromAttributes() throws Exception {
        createTestAttributeSet(new Attribute("xxx:attr/sugaryScale", "1234.456", TEST_PACKAGE));
        assertThat(testAttributeSet.getAttributeFloatValue("xxx", "sugaryScale", 78.9f), equalTo(1234.456f));
    }

    @Test
    public void getAttributeFloatValue_shouldReturnDefaultFloatValueWhenNotInAttributeSet() throws Exception {
        createTestAttributeSet();
        assertThat(testAttributeSet.getAttributeFloatValue(TEST_PACKAGE, "sugaryScale", 78.9f), equalTo(78.9f));
    }
    
    @Test
    public void getStyleAttribute_doesNotThrowException() throws Exception {
        createTestAttributeSet();
        testAttributeSet.getStyleAttribute();
    }

    @Test
    public void getStyleAttribute_returnsZeroWhenNoStyle() throws Exception {
        createTestAttributeSet();
        assertThat(testAttributeSet.getStyleAttribute(), equalTo(0));
    }

    @Test
    public void getStyleAttribute_returnsCorrectValue() throws Exception {
        createTestAttributeSet(new Attribute(":attr/style", "@style/FancyStyle", TEST_PACKAGE));
        assertThat(testAttributeSet.getStyleAttribute(), equalTo(R.style.FancyStyle));
    }

    @Test
    public void getStyleAttribute_doesNotThrowException_whenStyleIsBogus() throws Exception {
        createTestAttributeSet(new Attribute(":attr/style", "@style/bogus_style", TEST_PACKAGE));
        assertThat(testAttributeSet.getStyleAttribute(), equalTo(0));
    }

    private void createTestAttributeSet(Attribute... attributes) {
        testAttributeSet = new TestAttributeSet(asList(attributes), resourceLoader, null);
    }

}
