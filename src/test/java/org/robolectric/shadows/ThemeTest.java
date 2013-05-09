package org.robolectric.shadows;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Button;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.res.ResName;
import org.robolectric.res.ResourceLoader;
import org.robolectric.res.Style;
import org.robolectric.util.TestUtil;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class ThemeTest {
    @Test public void whenExplicitlySetOnActivity_activityGetsThemeFromActivityInManifest() throws Exception {
        TestActivity activity = new TestActivity();
        activity.setTheme(R.style.Theme_Robolectric);
        shadowOf(activity).callOnCreate(null);
        Button theButton = (Button) activity.findViewById(R.id.button);
        assertThat(theButton.getBackground()).isEqualTo(new ColorDrawable(0xff00ff00));
    }

    @Test public void whenSetOnActivityInManifest_activityGetsThemeFromActivityInManifest() throws Exception {
        TestActivity activity = Robolectric.buildActivity(TestActivity.class).create().get();
        Button theButton = (Button) activity.findViewById(R.id.button);
        assertThat(theButton.getBackground()).isEqualTo(new ColorDrawable(0xff00ff00));
    }

    @Test public void whenNotSetOnActivityInManifest_activityGetsThemeFromApplicationInManifest() throws Exception {
        TestActivity activity = Robolectric.buildActivity(TestActivityWithAnotherTheme.class).create().get();
        Button theButton = (Button) activity.findViewById(R.id.button);
        assertThat(theButton.getBackground()).isEqualTo(new ColorDrawable(0xffff0000));
    }

    @Test public void shouldResolveReferencesThatStartWithAQuestionMark() throws Exception {
        TestActivity activity = Robolectric.buildActivity(TestActivityWithAnotherTheme.class).create().get();
        Button theButton = (Button) activity.findViewById(R.id.button);
        assertThat(theButton.getMinWidth()).isEqualTo(42); // via AnotherTheme.Button -> logoWidth and logoHeight
//        assertThat(theButton.getMinHeight()).isEqualTo(42); todo 2.0-cleanup
    }

    @Test public void shouldLookUpStylesFromStyleResId() throws Exception {
        TestActivity activity = Robolectric.buildActivity(TestActivityWithAnotherTheme.class).create().get();
        TypedArray a = activity.obtainStyledAttributes(null, R.styleable.CustomView, 0, R.style.MyCustomView);
        boolean enabled = a.getBoolean(R.styleable.CustomView_aspectRatioEnabled, false);
        assertThat(enabled).isTrue();
    }

    @Test public void shouldInheritThemeValuesFromImplicitParents() throws Exception {
        TestActivity activity = Robolectric.buildActivity(TestActivityWithAnotherTheme.class).create().get();
        ResourceLoader resourceLoader = Robolectric.shadowOf(activity.getResources()).getResourceLoader();
        Style style = ShadowAssetManager.resolveStyle(resourceLoader,
                new ResName(TestUtil.TEST_PACKAGE, "style", "Widget.AnotherTheme.Button.Blarf"), "");
        assertThat(style.getAttrValue(new ResName("android", "attr", "background")).value)
                .isEqualTo("#ffff0000");
    }

//    @Test public void shouldPerformFastly() throws Exception {
//        TestActivity activity = Robolectric.buildActivity(TestActivityWithAnotherTheme.class).create().get();
//        Class type = type("com.android.internal.R$styleable").withClassLoader(TestActivity.class.getClassLoader()).load();
//        int[] styleableIds = field("TextView").ofType(int[].class).in(type).get();
//        // once for warm-up
//        long start = System.currentTimeMillis();
//        activity.obtainStyledAttributes(null, styleableIds, 0, R.style.MyCustomView);
//        System.out.println(String.format("Warm-up took %4dms", System.currentTimeMillis() - start));
//
//        start = System.currentTimeMillis();
//        int count = 5000;
//        for (int i = 0; i < count; i++) {
//            activity.obtainStyledAttributes(null, styleableIds, 0, R.style.MyCustomView);
//        }
//        System.out.println(String.format("Took %4.1fms", (System.currentTimeMillis() - start) / ((float) count)));
//    }

    public static class TestActivity extends Activity {
        @Override protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.styles_button_layout);
        }
    }

    public static class TestActivityWithAnotherTheme extends TestActivity {
        @Override protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.styles_button_layout);
        }
    }
}
