package org.robolectric.shadows;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.res.Attribute;
import org.robolectric.util.TestUtil;

import java.util.Arrays;

import static java.util.Arrays.asList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class TypedArrayTest {
  private Context context;
  private Resources resources;

  @Before
  public void setUp() throws Exception {
    context = Robolectric.buildActivity(Activity.class).create().get();
    resources = Robolectric.application.getResources();
  }

  @Test
  public void getResources() throws Exception {
    assertNotNull(context.obtainStyledAttributes(new int[]{}).getResources());
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
  public void getInt_withFlags_shouldReturnValue() throws Exception {
    TypedArray typedArray = shadowOf(resources).createTypedArray(
        asList(new Attribute("android:attr/gravity", "top|left", TestUtil.TEST_PACKAGE)),
        new int[]{android.R.attr.gravity});
    assertThat(typedArray.getInt(0, -1)).isEqualTo(0x33);
  }

  @Test
  public void getResourceId_shouldReturnDefaultValue() throws Exception {
    assertThat(context.obtainStyledAttributes(new int[]{android.R.attr.alpha}).getResourceId(0, -1)).isEqualTo(-1);
  }

  @Test
  public void getResourceId_shouldReturnActualValue() throws Exception {
    TypedArray typedArray = shadowOf(resources).createTypedArray(
        asList(new Attribute("android:attr/id", "@+id/snippet_text", TestUtil.TEST_PACKAGE)),
        new int[]{android.R.attr.id});
    assertThat(typedArray.getResourceId(0, -1)).isEqualTo(R.id.snippet_text);
  }

  @Test
  public void getFraction_shouldReturnDefaultValue() throws Exception {
    assertThat(context.obtainStyledAttributes(new int[]{android.R.attr.width}).getDimension(0, -1f)).isEqualTo(-1f);
  }

  @Test
  public void getFraction_shouldReturnGivenValue() throws Exception {
    TypedArray typedArray = shadowOf(resources).createTypedArray(
        asList(new Attribute(TestUtil.SYSTEM_PACKAGE + ":attr/width", "50%", TestUtil.SYSTEM_PACKAGE)),
        new int[]{android.R.attr.width});
    assertThat(typedArray.getFraction(0, 100, 1, -1)).isEqualTo(50f);
  }

  @Test
  public void getDimension_shouldReturnDefaultValue() throws Exception {
    assertThat(context.obtainStyledAttributes(new int[]{android.R.attr.width}).getDimension(0, -1f)).isEqualTo(-1f);
  }

  @Test
  public void getDimension_shouldReturnGivenValue() throws Exception {
    TypedArray typedArray = shadowOf(resources).createTypedArray(
        asList(new Attribute(TestUtil.SYSTEM_PACKAGE + ":attr/width", "50dp", TestUtil.SYSTEM_PACKAGE)),
        new int[]{android.R.attr.width});
    assertThat(typedArray.getDimension(0, -1)).isEqualTo(50f);
  }

  @Test
  public void getDrawable_withExplicitColorValue_shouldReturnColorDrawable() throws Exception {
    TypedArray typedArray = shadowOf(resources).createTypedArray(
        asList(new Attribute("android:attr/background", "#ff777777", TestUtil.TEST_PACKAGE)),
        new int[]{android.R.attr.background});
    assertThat(typedArray.getDrawable(0)).isEqualTo(new ColorDrawable(0xff777777));
  }

  @Test
  public void getTextArray_whenNoSuchAttribute_shouldReturnNull() throws Exception {
    TypedArray typedArray = shadowOf(resources).createTypedArray(
        asList(new Attribute(TestUtil.TEST_PACKAGE + ":attr/keycode", "@array/greetings", TestUtil.TEST_PACKAGE)),
        new int[]{R.attr.items});
    assertNull(typedArray.getTextArray(0));
  }

  @Test
  public void getTextArray_shouldReturnValues() throws Exception {
    TypedArray typedArray = shadowOf(resources).createTypedArray(
        asList(new Attribute(TestUtil.TEST_PACKAGE + ":attr/responses", "@array/greetings", TestUtil.TEST_PACKAGE)),
        new int[]{R.attr.responses});
    assertThat(typedArray.getTextArray(0)).containsExactly("hola", "Hello");
  }

  @Test public void hasValue_withValue() throws Exception {
    TypedArray typedArray = shadowOf(resources).createTypedArray(
        asList(new Attribute(TestUtil.TEST_PACKAGE + ":attr/responses", "@array/greetings", TestUtil.TEST_PACKAGE)),
        new int[]{R.attr.responses});
    assertThat(typedArray.hasValue(0)).isTrue();
  }

  @Test public void hasValue_withoutValue() throws Exception {
    TypedArray typedArray = shadowOf(resources).createTypedArray(
        Arrays.<Attribute>asList(),
        new int[]{R.attr.items});
    assertThat(typedArray.hasValue(0)).isFalse();
  }

  @Test public void hasValue_withNullValue() throws Exception {
    TypedArray typedArray = shadowOf(resources).createTypedArray(
        asList(new Attribute(TestUtil.TEST_PACKAGE + ":attr/items", "@null", TestUtil.TEST_PACKAGE)),
        new int[]{R.attr.items});
    assertThat(typedArray.hasValue(0)).isFalse();
  }

  @Test public void shouldEnumeratePresentValues() throws Exception {
    TypedArray typedArray = shadowOf(resources).createTypedArray(
        asList(
            new Attribute(TestUtil.TEST_PACKAGE + ":attr/responses", "@array/greetings", TestUtil.TEST_PACKAGE),
            new Attribute(TestUtil.TEST_PACKAGE + ":attr/aspectRatio", "1", TestUtil.TEST_PACKAGE)
        ),
        new int[]{R.attr.scrollBars, R.attr.responses, R.attr.isSugary});
    assertThat(typedArray.getIndexCount()).isEqualTo(1);
    assertThat(typedArray.getIndex(0)).isEqualTo(1);
  }
}
