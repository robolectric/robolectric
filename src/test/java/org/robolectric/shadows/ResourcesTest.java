package org.robolectric.shadows;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.NinePatchDrawable;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;
import org.robolectric.res.EmptyResourceLoader;
import org.robolectric.res.ResourceLoader;
import org.xmlpull.v1.XmlPullParser;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertSame;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class ResourcesTest {
    private Resources resources;

    @Before
    public void setup() throws Exception {
        resources = new Activity().getResources();
    }

    @Test(expected = Resources.NotFoundException.class)
    public void getStringArray_shouldThrowExceptionIfNotFound() throws Exception {
        resources.getStringArray(-1);
    }

    @Test(expected = Resources.NotFoundException.class)
    public void getIntegerArray_shouldThrowExceptionIfNotFound() throws Exception {
        resources.getIntArray(-1);
    }

    @Test
    public void testConfiguration() {
        Configuration configuration = resources.getConfiguration();
        assertThat(configuration).isNotNull();
        assertThat(configuration.locale).isNotNull();
    }

    @Test
    public void testConfigurationReturnsTheSameInstance() {
        assertThat(resources.getConfiguration()).isSameAs(resources.getConfiguration());
    }

    @Test
    public void testNewTheme() {
        assertThat(resources.newTheme()).isNotNull();
    }

    @Test
    public void testGetAndSetConfiguration_SameInstance() throws Exception {
        Activity activity = new Activity();
        Resources resources = activity.getResources();
        assertSame(resources.getConfiguration(), resources.getConfiguration());
        Configuration diffConfig = new Configuration();
        shadowOf(resources).setConfiguration(diffConfig);
        assertSame(diffConfig, resources.getConfiguration());
    }

    @Test(expected = Resources.NotFoundException.class)
    public void testGetDrawableNullRClass() throws Exception {
        ResourceLoader resourceLoader = new EmptyResourceLoader();
        resources = new Resources(null, null, null);
        ShadowResources.bind(resources, resourceLoader);

        assertThat(resources.getDrawable(-12345)).isInstanceOf(BitmapDrawable.class);
    }

    /**
     * given an R.anim.id value, will return an AnimationDrawable
     */
    @Test
    public void testGetAnimationDrawable() {
        assertThat(resources.getDrawable(R.anim.test_anim_1)).isInstanceOf(AnimationDrawable.class);
    }

    @Test
    @Config(qualifiers = "fr")
    public void testGetValuesResFromSpecifiecQualifiers() {
        String hello = resources.getString(R.string.hello);
        assertThat(hello).isEqualTo("Bonjour");
    }

    /**
     * given an R.color.id value, will return a ColorDrawable
     */
    @Test
    public void testGetColorDrawable() {
        assertThat(resources.getDrawable(R.color.test_color_1)).isInstanceOf(ColorDrawable.class);
    }

    /**
     * given an R.color.id value, will return a Color
     */
    @Test
    public void testGetColor() {
        assertThat(resources.getColor(R.color.test_color_1)).isNotEqualTo(0);
    }

    /**
     * given an R.color.id value, will return a ColorStateList
     */
    @Test
    public void testGetColorStateList() {
        assertThat(resources.getColorStateList(R.color.test_color_1)).isInstanceOf(ColorStateList.class);
    }

    /**
     * given an R.drawable.id value, will return a BitmapDrawable
     */
    @Test
    public void testGetBitmapDrawable() {
        assertThat(resources.getDrawable(R.drawable.test_drawable_1)).isInstanceOf(BitmapDrawable.class);
    }

    /**
     * given an R.drawable.id value, will return a NinePatchDrawable for .9.png file
     */
    @Test
    public void testGetNinePatchDrawable() {
        assertThat(Robolectric.getShadowApplication().getResources().getDrawable(R.drawable.nine_patch_drawable)).isInstanceOf(NinePatchDrawable.class);
    }

    @Test(expected = Resources.NotFoundException.class)
    public void testGetBitmapDrawableForUnknownId() {
        assertThat(resources.getDrawable(Integer.MAX_VALUE)).isInstanceOf(BitmapDrawable.class);
    }

    @Test
    public void testDensity() {
        Activity activity = new Activity();
        assertThat(activity.getResources().getDisplayMetrics().density).isEqualTo(1f);

        shadowOf(activity.getResources()).setDensity(1.5f);
        assertThat(activity.getResources().getDisplayMetrics().density).isEqualTo(1.5f);

        Activity anotherActivity = new Activity();
        assertThat(anotherActivity.getResources().getDisplayMetrics().density).isEqualTo(1.5f);
    }

    @Test
    public void displayMetricsShouldNotHaveLotsOfZeros() throws Exception {
        Activity activity = new Activity();
        assertThat(activity.getResources().getDisplayMetrics().heightPixels).isEqualTo(800);
        assertThat(activity.getResources().getDisplayMetrics().widthPixels).isEqualTo(480);
    }

    @Test
    public void getSystemShouldReturnSystemResources() throws Exception {
        assertThat(Resources.getSystem()).isInstanceOf(Resources.class);
    }

    @Test
    public void multipleCallsToGetSystemShouldReturnSameInstance() throws Exception {
        assertThat(Resources.getSystem()).isEqualTo(Resources.getSystem());
    }

    @Test
    public void applicationResourcesShouldHaveBothSystemAndLocalValues() throws Exception {
        Activity activity = new Activity();
        assertThat(activity.getResources().getString(android.R.string.copy)).isEqualTo("Copy");
        assertThat(activity.getResources().getString(R.string.copy)).isEqualTo("Local Copy");
    }

    @Ignore // todo fix
    @Test
    public void systemResourcesShouldHaveSystemValuesOnly() throws Exception {
        assertThat(Resources.getSystem().getString(android.R.string.copy)).isEqualTo("Copy");
        assertThat(Resources.getSystem().getString(R.string.copy)).isNull();
    }

    @Test
    public void systemResourcesShouldReturnCorrectSystemId() throws Exception {
        assertThat(Resources.getSystem().getIdentifier("copy", "android:string", null)).isEqualTo(android.R.string.copy);
    }

    @Test
    public void systemResourcesShouldReturnZeroForLocalId() throws Exception {
        assertThat(Resources.getSystem().getIdentifier("copy", "string", null)).isEqualTo(0);
    }

    @Test
    public void testGetXml() throws Exception {
        int resId = R.xml.preferences;
        XmlResourceParser parser = Robolectric.application.getResources().getXml(resId);
        // Assert that a resource file is returned
        assertThat(parser).isNotNull();

        // Assert that the resource file is the preference screen
        int event;
        do {
            event = parser.next();
        } while (event != XmlPullParser.START_TAG);
        assertThat(parser.getName()).isEqualTo("PreferenceScreen");
    }

    @Test(expected = Resources.NotFoundException.class)
    public void testGetXml_nonexistentResource() {
        resources.getXml(0);
    }

}
