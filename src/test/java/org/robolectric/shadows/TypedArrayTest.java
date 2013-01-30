package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.TestRunners;
import com.xtremelabs.robolectric.tester.android.util.Attribute;
import com.xtremelabs.robolectric.tester.android.util.TestAttributeSet;
import com.xtremelabs.robolectric.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(TestRunners.WithDefaults.class)
public class TypedArrayTest {
    private Context context;

    @Before
    public void setUp() throws Exception {
        context = new Activity();
    }

    @Test
    public void getResources() throws Exception {
        assertNotNull(context.obtainStyledAttributes(null).getResources());
    }

    @Test
    public void getInt_shouldReturnDefaultValue() throws Exception {
        assertThat(context.obtainStyledAttributes(new int[]{android.R.attr.alpha}).getInt(0, -1), equalTo(-1));
    }

    @Test
    public void getInteger_shouldReturnDefaultValue() throws Exception {
        assertThat(context.obtainStyledAttributes(new int[]{android.R.attr.alpha}).getInteger(0, -1), equalTo(-1));
    }

    @Test
    public void getResourceId_shouldReturnDefaultValue() throws Exception {
        assertThat(context.obtainStyledAttributes(new int[]{android.R.attr.alpha}).getResourceId(0, -1), equalTo(-1));
    }

    @Test
    public void getDimension_shouldReturnDefaultValue() throws Exception {
        assertThat(context.obtainStyledAttributes(new int[]{android.R.attr.alpha}).getDimension(0, -1f), equalTo(-1f));
    }

    @Test
    public void getTextArray_whenNoSuchAttribute_shouldReturnNull() throws Exception {
        Resources resources = Robolectric.application.getResources();
        TestAttributeSet attributeSet = new TestAttributeSet(
                asList(new Attribute(TestUtil.TEST_PACKAGE + ":attr/not_items", "@array/greetings", TestUtil.TEST_PACKAGE)
                ), shadowOf(resources).getResourceLoader(), null);
        TypedArray typedArray = ShadowTypedArray.create(resources, attributeSet, new int[]{R.attr.items});
        assertNull(typedArray.getTextArray(0));
    }

    @Test
    public void getTextArray_shouldReturnValues() throws Exception {
        Resources resources = Robolectric.application.getResources();
        TestAttributeSet attributeSet = new TestAttributeSet(
                asList(new Attribute(TestUtil.TEST_PACKAGE + ":attr/items", "@array/greetings", TestUtil.TEST_PACKAGE)
                ), shadowOf(resources).getResourceLoader(), null);
        TypedArray typedArray = ShadowTypedArray.create(resources, attributeSet, new int[]{R.attr.items});
        assertThat(asList(typedArray.getTextArray(0)), hasItems((CharSequence)"hola", "Hello"));
    }
}
