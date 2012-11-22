package com.xtremelabs.robolectric.res;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.*;
import com.google.android.maps.MapView;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.TestRunners;
import com.xtremelabs.robolectric.shadows.ShadowImageView;
import com.xtremelabs.robolectric.shadows.ShadowTextView;
import com.xtremelabs.robolectric.util.CustomView;
import com.xtremelabs.robolectric.util.CustomView2;
import com.xtremelabs.robolectric.util.I18nException;
import com.xtremelabs.robolectric.util.TestUtil;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.DEFAULT_SDK_VERSION;
import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static com.xtremelabs.robolectric.util.TestUtil.*;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;

@RunWith(TestRunners.WithDefaults.class)
public class ViewLoaderTest {
    private ViewLoader viewLoader;
    private FragmentActivity context;

    @Before
    public void setUp() throws Exception {
        ResourcePath localResourcePath = new ResourcePath(R.class, resourceFile("res"), null);
        ResourcePath systemResourcePath = ResourceLoader.getSystemResourcePath(DEFAULT_SDK_VERSION, asList(localResourcePath));
        ResourceExtractor resourceExtractor = new ResourceExtractor(localResourcePath, systemResourcePath);

        StringResourceLoader stringResourceLoader = new StringResourceLoader(resourceExtractor);
        new DocumentLoader(stringResourceLoader).loadResourceXmlDir(localResourcePath, "values");
        new DocumentLoader(stringResourceLoader).loadResourceXmlDir(systemResourcePath, "values");
        
        viewLoader =  new ViewLoader(resourceExtractor, new AttrResourceLoader(resourceExtractor));
        new DocumentLoader(viewLoader).loadResourceXmlDir(localResourcePath, "layout");
        new DocumentLoader(viewLoader).loadResourceXmlDir(localResourcePath, "layout-xlarge");
        new DocumentLoader(viewLoader).loadResourceXmlDir(localResourcePath, "layout-land");
        new DocumentLoader(viewLoader).loadResourceXmlDir(systemResourcePath, "layout");

        context = new FragmentActivity();
    }

    @Test
    public void testCreatesCorrectClasses() throws Exception {
        ViewGroup view = (ViewGroup) inflate("layout/media");
        TestUtil.assertInstanceOf(LinearLayout.class, view);

        assertSame(context, view.getContext());
    }

    @Test
    public void testChoosesLayoutBasedOnDefaultScreenSize() throws Exception {
        ViewGroup view = (ViewGroup) inflate("layout/different_screen_sizes");
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        assertThat(textView.getText().toString(), equalTo("default"));
    }

    @Test
    public void testChoosesLayoutBasedOnSearchPath_choosesFirstFileFoundOnPath() throws Exception {
        viewLoader.setLayoutQualifierSearchPath("xlarge", "land");
        ViewGroup view = (ViewGroup) inflate("layout/different_screen_sizes");
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        assertThat(textView.getText().toString(), equalTo("xlarge"));
    }

    @Test
    public void testChoosesLayoutBasedOnSearchPath_respectsOrderOfPath() throws Exception {
        viewLoader.setLayoutQualifierSearchPath("does-not-exist", "land", "xlarge");
        ViewGroup view = (ViewGroup) inflate("layout/different_screen_sizes");
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        assertThat(textView.getText().toString(), equalTo("land"));
    }

    @Test
    public void testWebView() throws Exception {
        ViewGroup view = (ViewGroup) inflate("layout/webview_holder");
        WebView webView = (WebView) view.findViewById(R.id.web_view);

        webView.loadUrl("www.example.com");

        assertThat(shadowOf(webView).getLastLoadedUrl(), equalTo("www.example.com"));
    }

    @Test
    public void testAddsChildren() throws Exception {
        ViewGroup view = (ViewGroup) inflate("layout/media");
        assertTrue(view.getChildCount() > 0);

        assertSame(context, view.getChildAt(0).getContext());
    }

    @Test
    public void testFindsChildrenById() throws Exception {
        ViewGroup mediaView = (ViewGroup) inflate("layout/media");
        TestUtil.assertInstanceOf(TextView.class, mediaView.findViewById(R.id.title));

        ViewGroup mainView = (ViewGroup) inflate("layout/main");
        assertInstanceOf(View.class, mainView.findViewById(R.id.title));
    }

    @Test
    public void testInflatingConflictingSystemAndLocalViewsWorks() throws Exception {
        ViewGroup view = (ViewGroup) inflate("layout/activity_list_item");
        assertInstanceOf(ImageView.class, view.findViewById(R.id.icon));

        view = (ViewGroup) inflate("android:layout/activity_list_item");
        assertInstanceOf(ImageView.class, view.findViewById(android.R.id.icon));
    }

    @Test
    public void testInclude() throws Exception {
        ViewGroup mediaView = (ViewGroup) inflate("layout/media");
        assertInstanceOf(TextView.class, mediaView.findViewById(R.id.include_id));
    }

    @Test
    public void testIncludeShouldRetainAttributes() throws Exception {
        ViewGroup mediaView = (ViewGroup) inflate("layout/media");
        assertThat(mediaView.findViewById(R.id.include_id).getVisibility(), is(View.GONE));
    }

    @Test
    public void shouldOverwriteIdOnIncludedNonMerge() throws Exception {
        ViewGroup mediaView = (ViewGroup) inflate("layout/media");
        assertNull(mediaView.findViewById(R.id.snippet_text));
    }

    @Test
    public void shouldRetainIdOnIncludedMergeWhenIncludeSpecifiesNoId() throws Exception {
        ViewGroup mediaView = (ViewGroup) inflate("layout/override_include");
        assertInstanceOf(TextView.class, mediaView.findViewById(R.id.inner_text));
    }

    @Test
    public void shouldRetainIdOnIncludedNonMergeWhenIncludeSpecifiesNoId() throws Exception {
        ViewGroup mediaView = (ViewGroup) inflate("layout/override_include");
        assertInstanceOf(TextView.class, mediaView.findViewById(R.id.snippet_text));
    }

    @Test
    public void testIncludedIdShouldNotBeFoundWhenIncludedIsMerge() throws Exception {
        ViewGroup overrideIncludeView = (ViewGroup) inflate("layout/outer");
        assertInstanceOf(LinearLayout.class, overrideIncludeView.findViewById(R.id.outer_merge));
        assertInstanceOf(TextView.class, overrideIncludeView.findViewById(R.id.inner_text));
        assertNull(overrideIncludeView.findViewById(R.id.include_id));
        assertEquals(1, overrideIncludeView.getChildCount());
    }

    @Test
    public void testIncludeShouldOverrideAttributesOfIncludedRootNode() throws Exception {
        ViewGroup overrideIncludeView = (ViewGroup) inflate("layout/override_include");
        assertThat(overrideIncludeView.findViewById(R.id.snippet_text).getVisibility(), is(View.INVISIBLE));
    }

    @Test
    public void shouldNotCountRequestFocusElementAsChild() throws Exception {
        ViewGroup viewGroup = (ViewGroup) inflate("layout/request_focus");
        ViewGroup frameLayout = (ViewGroup) viewGroup.getChildAt(1);
        assertEquals(0, frameLayout.getChildCount());
    }

    @Test
    public void shouldGiveFocusToElementContainingRequestFocusElement() throws Exception {
        ViewGroup viewGroup = (ViewGroup) inflate("layout/request_focus");
        EditText editText = (EditText) viewGroup.findViewById(R.id.edit_text);
        assertFalse(editText.isFocused());
    }

    @Test
    public void shouldGiveFocusToFirstFocusableElement_butThisMightBeTheWrongBehavior() throws Exception {
        ViewGroup viewGroup = (ViewGroup) inflate("layout/request_focus_with_two_edit_texts");
        assertTrue(viewGroup.findViewById(R.id.edit_text).isFocused());
        assertFalse(viewGroup.findViewById(R.id.edit_text2).isFocused());
    }

    @Test
    public void testMerge() throws Exception {
        ViewGroup mediaView = (ViewGroup) inflate("layout/outer");
        TestUtil.assertInstanceOf(TextView.class, mediaView.findViewById(R.id.inner_text));
    }

    @Test
    public void mergeIncludesShouldNotCreateAncestryLoops() throws Exception {
        ViewGroup mediaView = (ViewGroup) inflate("layout/outer");
        mediaView.hasFocus();
    }

    @Test
    public void testViewGroupsLooksAtItsOwnId() throws Exception {
        TextView mediaView = (TextView) inflate("layout/snippet");
        assertSame(mediaView, mediaView.findViewById(R.id.snippet_text));
    }

    @Test
    public void shouldConstructCustomViewsWithAttributesConstructor() throws Exception {
        CustomView view = (CustomView) inflate("layout/custom_layout");
        assertThat(view.attributeResourceValue, equalTo(R.string.hello));
    }

    @Test
    public void testViewVisibilityIsSet() throws Exception {
        View mediaView = inflate("layout/media");
        assertThat(mediaView.findViewById(R.id.title).getVisibility(), equalTo(View.VISIBLE));
        assertThat(mediaView.findViewById(R.id.subtitle).getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void testTextViewTextIsSet() throws Exception {
        View mediaView = inflate("layout/main");
        assertThat(((TextView) mediaView.findViewById(R.id.title)).getText().toString(), equalTo("Main Layout"));
        assertThat(((TextView) mediaView.findViewById(R.id.subtitle)).getText().toString(), equalTo("Hello"));
    }

    @Test
    public void testTextViewCompoundDrawablesAreSet() throws Exception {
        View mediaView = inflate("layout/main");
        ShadowTextView shadowTextView = shadowOf((TextView) mediaView.findViewById(R.id.title));

        assertThat(shadowTextView.getCompoundDrawablesImpl().getTop(), equalTo(R.drawable.an_image));
        assertThat(shadowTextView.getCompoundDrawablesImpl().getRight(), equalTo(R.drawable.an_other_image));
        assertThat(shadowTextView.getCompoundDrawablesImpl().getBottom(), equalTo(R.drawable.third_image));
        assertThat(shadowTextView.getCompoundDrawablesImpl().getLeft(), equalTo(R.drawable.fourth_image));
    }

    @Test
    public void testCheckBoxCheckedIsSet() throws Exception {
        View mediaView = inflate("layout/main");
        assertThat(((CheckBox) mediaView.findViewById(R.id.true_checkbox)).isChecked(), equalTo(true));
        assertThat(((CheckBox) mediaView.findViewById(R.id.false_checkbox)).isChecked(), equalTo(false));
        assertThat(((CheckBox) mediaView.findViewById(R.id.default_checkbox)).isChecked(), equalTo(false));
    }

    @Test
    public void testImageViewSrcIsSet() throws Exception {
        View mediaView = inflate("layout/main");
        assertThat(((ShadowImageView) shadowOf(mediaView.findViewById(R.id.image))).getResourceId(), equalTo(R.drawable.an_image));
    }

    @Test
    public void shouldInflateMergeLayoutIntoParent() throws Exception {
        View innerMerge = viewLoader.inflateView(context, R.layout.inner_merge, new LinearLayout(null));
        assertNotNull(innerMerge);
    }

    @Test
    public void testMapView() throws Exception {
        RelativeLayout mainView = (RelativeLayout) inflate("layout/mapview");
        TestUtil.assertInstanceOf(MapView.class, mainView.findViewById(R.id.map_view));
    }

    @Test
    @Ignore
    public void testFragment() throws Exception {
        View v = inflate("layout/fragment");
        TestUtil.assertInstanceOf(TextView.class, v);
        final FragmentManager fragmentManager = context.getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.my_fragment);
        assertNotNull(fragment);
    }

    @Test
    public void testMultiOrientation() throws Exception {
        // Default screen orientation should be portrait.
        ViewGroup view = (ViewGroup) inflate("layout/multi_orientation");
        TestUtil.assertInstanceOf(LinearLayout.class, view);
        assertEquals(view.getId(), R.id.portrait);
        assertSame(context, view.getContext());

        // Confirm explicit "orientation = portrait" works.
        context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        view = (ViewGroup) inflate("layout/multi_orientation");
        TestUtil.assertInstanceOf(LinearLayout.class, view);
        assertEquals(view.getId(), R.id.portrait);
        assertSame(context, view.getContext());

        viewLoader.setLayoutQualifierSearchPath("land");

        // Confirm explicit "orientation = landscape" works.
        context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        view = (ViewGroup) inflate("layout/multi_orientation");
        assertEquals(view.getId(), R.id.landscape);
        TestUtil.assertInstanceOf(LinearLayout.class, view);
    }

    @Test
    public void testViewEnabled() throws Exception {
        View mediaView = inflate("layout/main");
        assertThat(mediaView.findViewById(R.id.time).isEnabled(), equalTo(false));
    }

    @Test
    public void testContentDescriptionIsSet() throws Exception {
        View mediaView = inflate("layout/main");
        assertThat(mediaView.findViewById(R.id.time).getContentDescription().toString(), equalTo("Howdy"));
    }

    @Test
    public void testViewBackgroundIdIsSet() throws Exception {
        View mediaView = inflate("layout/main");
        ImageView imageView = (ImageView) mediaView.findViewById(R.id.image);
        ShadowImageView shadowImageView = Robolectric.shadowOf(imageView);

        assertThat(shadowImageView.getBackgroundResourceId(), equalTo(R.drawable.image_background));
    }

    @Test
    public void testOnClickAttribute() throws Exception {
        ClickActivity activity = new ClickActivity();
        activity.onCreate(null);

        assertThat(activity.clicked, equalTo(false));

        Button button = (Button)activity.findViewById(R.id.button);
        button.performClick();

        assertThat(activity.clicked, equalTo(true));
    }

    @Test
    public void testInvalidOnClickAttribute() throws Exception {
        Activity activity = new Activity();
        activity.setContentView(R.layout.with_invalid_onclick);

        Button button =
            (Button)activity.findViewById(R.id.invalid_onclick_button);

        IllegalStateException exception = null;
        try {
            button.performClick();
        } catch (IllegalStateException e) {
            exception = e;
        } finally {
            assertNotNull(exception);
            assertThat("The error message should contain the id name of the "
                       + "faulty button",
                       exception.getMessage(),
                       containsString("invalid_onclick_button"));
        }
    }

    @Test
    public void shouldInvokeOnFinishInflate() throws Exception {
        CustomView2 outerCustomView = (CustomView2) inflate("layout/custom_layout2");
        CustomView2 innerCustomView = (CustomView2) outerCustomView.getChildAt(0);
        assertThat(outerCustomView.childCountAfterInflate, equalTo(1));
        assertThat(innerCustomView.childCountAfterInflate, equalTo(3));
    }

    @Test
    public void testIncludesLinearLayoutsOnlyOnce() throws Exception {
        ViewGroup parentView = (ViewGroup) inflate("layout/included_layout_parent");
        assertEquals(1, parentView.getChildCount());
    }
    
    @Test(expected=I18nException.class)
    public void shouldThrowI18nExceptionOnLayoutWithBareStrings() throws Exception {
    	viewLoader.setStrictI18n(true);
        inflate("layout/text_views");
    }

    private View inflate(String key) {
        String layoutName = ResourceExtractor.qualifyResourceName(key, R.class.getPackage().getName());
        return viewLoader.inflateView(context, layoutName);
    }

    public static class ClickActivity extends FragmentActivity {
        public boolean clicked = false;

        @Override protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.main);
        }

        public void onButtonClick(View v) {
            clicked = true;
        }
    }
}
