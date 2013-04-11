package org.robolectric.shadows;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.res.Attribute;
import org.robolectric.util.TestUtil;

import static java.util.Arrays.asList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.robolectric.Robolectric.shadowOf;

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
        assertThat(context.obtainStyledAttributes(new int[]{android.R.attr.alpha}).getInt(0, -1)).isEqualTo(-1);
    }

    @Test
    public void getInteger_shouldReturnDefaultValue() throws Exception {
        assertThat(context.obtainStyledAttributes(new int[]{android.R.attr.alpha}).getInteger(0, -1)).isEqualTo(-1);
    }

    @Test
    public void getResourceId_shouldReturnDefaultValue() throws Exception {
        assertThat(context.obtainStyledAttributes(new int[]{android.R.attr.alpha}).getResourceId(0, -1)).isEqualTo(-1);
    }

    @Test
    public void getResourceId_shouldReturnActualValue() throws Exception {
        Resources resources = Robolectric.application.getResources();
        RoboAttributeSet attributeSet = new RoboAttributeSet(
                asList(new Attribute("android:attr/id", "@+id/snippet_text", TestUtil.TEST_PACKAGE)
                ), shadowOf(resources).getResourceLoader(), null);
        TypedArray typedArray = ShadowTypedArray.create(resources, attributeSet, new int[]{android.R.attr.id});
        assertThat(typedArray.getResourceId(0, -1)).isEqualTo(R.id.snippet_text);
    }

    @Test
    public void getDimension_shouldReturnDefaultValue() throws Exception {
        assertThat(context.obtainStyledAttributes(new int[]{android.R.attr.alpha}).getDimension(0, -1f)).isEqualTo(-1f);
    }

    @Test
    public void getTextArray_whenNoSuchAttribute_shouldReturnNull() throws Exception {
        Resources resources = Robolectric.application.getResources();
        RoboAttributeSet attributeSet = new RoboAttributeSet(
                asList(new Attribute(TestUtil.TEST_PACKAGE + ":attr/keycode", "@array/greetings", TestUtil.TEST_PACKAGE)
                ), shadowOf(resources).getResourceLoader(), null);
        TypedArray typedArray = ShadowTypedArray.create(resources, attributeSet, new int[]{R.attr.items});
        assertNull(typedArray.getTextArray(0));
    }

    @Test
    public void getTextArray_shouldReturnValues() throws Exception {
        Resources resources = Robolectric.application.getResources();
        RoboAttributeSet attributeSet = new RoboAttributeSet(
                asList(new Attribute(TestUtil.TEST_PACKAGE + ":attr/items", "@array/greetings", TestUtil.TEST_PACKAGE)
                ), shadowOf(resources).getResourceLoader(), null);
        TypedArray typedArray = ShadowTypedArray.create(resources, attributeSet, new int[]{R.attr.items});
        assertThat(typedArray.getTextArray(0)).containsExactly("hola", "Hello");
    }

    @Test public void shouldEnumeratePresentValues() throws Exception {
        Resources resources = Robolectric.application.getResources();
        RoboAttributeSet attributeSet = new RoboAttributeSet(
                asList(new Attribute(TestUtil.TEST_PACKAGE + ":attr/items", "@array/greetings", TestUtil.TEST_PACKAGE),
                        new Attribute(TestUtil.TEST_PACKAGE + ":attr/aspectRatio", "1", TestUtil.TEST_PACKAGE)
                ), shadowOf(resources).getResourceLoader(), null);
        TypedArray typedArray = ShadowTypedArray.create(resources, attributeSet, new int[]{R.attr.scrollBars, R.attr.items, R.attr.isSugary});
        assertThat(typedArray.getIndexCount()).isEqualTo(1);
        assertThat(typedArray.getIndex(0)).isEqualTo(1);
    }
}
