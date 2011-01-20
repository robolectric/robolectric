package com.xtremelabs.robolectric.res;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.android.maps.MapView;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.shadows.ShadowImageView;
import com.xtremelabs.robolectric.shadows.ShadowTextView;
import com.xtremelabs.robolectric.util.CustomView;
import com.xtremelabs.robolectric.util.TestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static com.xtremelabs.robolectric.util.TestUtil.assertInstanceOf;
import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(WithTestDefaultsRunner.class)
public class ViewLoaderTest {
    private ViewLoader viewLoader;
    private Context context;

    @Before
    public void setUp() throws Exception {
        Robolectric.bindDefaultShadowClasses();

        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addLocalRClass(R.class);
        StringResourceLoader stringResourceLoader = new StringResourceLoader(resourceExtractor);
        new DocumentLoader(stringResourceLoader).loadLocalResourceXmlDir(resourceFile("res", "values"));
        viewLoader = new ViewLoader(resourceExtractor, new AttrResourceLoader(resourceExtractor));
        new DocumentLoader(viewLoader).loadLocalResourceXmlDir(resourceFile("res", "layout"));

        context = new Activity();
    }

    @Test
    public void testCreatesCorrectClasses() throws Exception {
        ViewGroup view = (ViewGroup) viewLoader.inflateView(context, "layout/media");
        TestUtil.assertInstanceOf(LinearLayout.class, view);

        assertSame(context, view.getContext());
    }

    @Test
    public void testWebView() throws Exception {
        ViewGroup view = (ViewGroup) viewLoader.inflateView(context, "layout/webview_holder");
        WebView webView = (WebView) view.findViewById(R.id.web_view);

        webView.loadUrl("www.example.com");

        assertThat(shadowOf(webView).getLastLoadedUrl(), equalTo("www.example.com"));
    }

    @Test
    public void testAddsChildren() throws Exception {
        ViewGroup view = (ViewGroup) viewLoader.inflateView(context, "layout/media");
        assertTrue(view.getChildCount() > 0);

        assertSame(context, view.getChildAt(0).getContext());
    }

    @Test
    public void testFindsChildrenById() throws Exception {
        ViewGroup mediaView = (ViewGroup) viewLoader.inflateView(context, "layout/media");
        TestUtil.assertInstanceOf(TextView.class, mediaView.findViewById(R.id.title));

        ViewGroup mainView = (ViewGroup) viewLoader.inflateView(context, "layout/main");
        assertInstanceOf(View.class, mainView.findViewById(R.id.title));
    }

    @Test
    public void testInclude() throws Exception {
        ViewGroup mediaView = (ViewGroup) viewLoader.inflateView(context, "layout/media");
        assertInstanceOf(TextView.class, mediaView.findViewById(R.id.include_id));
    }

    @Test
    public void testIncludeShouldRetainAttributes() throws Exception {
        ViewGroup mediaView = (ViewGroup) viewLoader.inflateView(context, "layout/media");
        assertThat(mediaView.findViewById(R.id.include_id).getVisibility(), is(View.GONE));
    }

    @Test
    public void shouldOverwriteIdOnIncludedNonMerge() throws Exception {
        ViewGroup mediaView = (ViewGroup) viewLoader.inflateView(context, "layout/media");
        assertNull(mediaView.findViewById(R.id.snippet_text));
    }

    @Test
    public void shouldRetainIdOnIncludedMergeWhenIncludeSpecifiesNoId() throws Exception {
        ViewGroup mediaView = (ViewGroup) viewLoader.inflateView(context, "layout/override_include");
        assertInstanceOf(TextView.class, mediaView.findViewById(R.id.inner_text));
    }

    @Test
    public void shouldRetainIdOnIncludedNonMergeWhenIncludeSpecifiesNoId() throws Exception {
        ViewGroup mediaView = (ViewGroup) viewLoader.inflateView(context, "layout/override_include");
        assertInstanceOf(TextView.class, mediaView.findViewById(R.id.snippet_text));
    }

    @Test
    public void testIncludedIdShouldNotBeFoundWhenIncludedIsMerge() throws Exception {
        ViewGroup overrideIncludeView = (ViewGroup) viewLoader.inflateView(context, "layout/outer");
        assertInstanceOf(LinearLayout.class, overrideIncludeView.findViewById(R.id.outer_merge));
        assertInstanceOf(TextView.class, overrideIncludeView.findViewById(R.id.inner_text));
        assertNull(overrideIncludeView.findViewById(R.id.include_id));
    }

    @Test
    public void testIncludeShouldOverrideAttributesOfIncludedRootNode() throws Exception {
        ViewGroup overrideIncludeView = (ViewGroup) viewLoader.inflateView(context, "layout/override_include");
        assertThat(overrideIncludeView.findViewById(R.id.snippet_text).getVisibility(), is(View.INVISIBLE));
    }

    @Test
    public void shouldNotCountRequestFocusElementAsChild() throws Exception {
        ViewGroup viewGroup = (ViewGroup) viewLoader.inflateView(context, "layout/request_focus");
        ViewGroup frameLayout = (ViewGroup) viewGroup.getChildAt(1);
        assertEquals(0, frameLayout.getChildCount());
    }

    @Test
    public void shouldGiveFocusToElementContainingRequestFocusElement() throws Exception {
        ViewGroup viewGroup = (ViewGroup) viewLoader.inflateView(context, "layout/request_focus");
        EditText editText = (EditText) viewGroup.findViewById(R.id.edit_text);
        assertFalse(editText.isFocused());
    }

    @Test
    public void shouldGiveFocusToFirstFocusableElement_butThisMightBeTheWrongBehavior() throws Exception {
        ViewGroup viewGroup = (ViewGroup) viewLoader.inflateView(context, "layout/request_focus_with_two_edit_texts");
        assertTrue(viewGroup.findViewById(R.id.edit_text).isFocused());
        assertFalse(viewGroup.findViewById(R.id.edit_text2).isFocused());
    }

    @Test
    public void testMerge() throws Exception {
        ViewGroup mediaView = (ViewGroup) viewLoader.inflateView(context, "layout/outer");
        TestUtil.assertInstanceOf(TextView.class, mediaView.findViewById(R.id.inner_text));
    }

    @Test
    public void mergeIncludesShouldNotCreateAncestryLoops() throws Exception {
        ViewGroup mediaView = (ViewGroup) viewLoader.inflateView(context, "layout/outer");
        mediaView.hasFocus();
    }

    @Test
    public void testViewGroupsLooksAtItsOwnId() throws Exception {
        TextView mediaView = (TextView) viewLoader.inflateView(context, "layout/snippet");
        assertSame(mediaView, mediaView.findViewById(R.id.snippet_text));
    }

    @Test
    public void shouldConstructCustomViewsWithAttributesConstructor() throws Exception {
        CustomView view = (CustomView) viewLoader.inflateView(context, "layout/custom_layout");
        assertThat(view.attributeResourceValue, equalTo(R.string.hello));
    }

    @Test
    public void testViewVisibilityIsSet() throws Exception {
        View mediaView = viewLoader.inflateView(context, "layout/media");
        assertThat(mediaView.findViewById(R.id.title).getVisibility(), equalTo(View.VISIBLE));
        assertThat(mediaView.findViewById(R.id.subtitle).getVisibility(), equalTo(View.GONE));
    }

    @Test
    public void testTextViewTextIsSet() throws Exception {
        View mediaView = viewLoader.inflateView(context, "layout/main");
        assertThat(((TextView) mediaView.findViewById(R.id.title)).getText().toString(), equalTo("Main Layout"));
        assertThat(((TextView) mediaView.findViewById(R.id.subtitle)).getText().toString(), equalTo("Hello"));
    }

    @Test
    public void testTextViewCompoundDrawablesAreSet() throws Exception {
        View mediaView = viewLoader.inflateView(context, "layout/main");
        ShadowTextView shadowTextView = shadowOf((TextView) mediaView.findViewById(R.id.title));

        assertThat(shadowTextView.getCompoundDrawablesImpl().top, equalTo(R.drawable.an_image));
        assertThat(shadowTextView.getCompoundDrawablesImpl().right, equalTo(R.drawable.an_other_image));
        assertThat(shadowTextView.getCompoundDrawablesImpl().bottom, equalTo(R.drawable.third_image));
        assertThat(shadowTextView.getCompoundDrawablesImpl().left, equalTo(R.drawable.fourth_image));
    }

    @Test
    public void testCheckBoxCheckedIsSet() throws Exception {
        View mediaView = viewLoader.inflateView(context, "layout/main");
        assertThat(((CheckBox) mediaView.findViewById(R.id.true_checkbox)).isChecked(), equalTo(true));
        assertThat(((CheckBox) mediaView.findViewById(R.id.false_checkbox)).isChecked(), equalTo(false));
        assertThat(((CheckBox) mediaView.findViewById(R.id.default_checkbox)).isChecked(), equalTo(false));
    }

    @Test
    public void testImageViewSrcIsSet() throws Exception {
        View mediaView = viewLoader.inflateView(context, "layout/main");
        assertThat(((ShadowImageView) shadowOf(mediaView.findViewById(R.id.image))).getResourceId(), equalTo(R.drawable.an_image));
    }

    @Test
    public void shouldInflateMergeLayoutIntoParent() throws Exception {
        View innerMerge = viewLoader.inflateView(context, R.layout.inner_merge, new LinearLayout(null));
        assertNotNull(innerMerge);
    }

    @Test
    public void testMapView() throws Exception {
        RelativeLayout mainView = (RelativeLayout) viewLoader.inflateView(context, "layout/mapview");
        TestUtil.assertInstanceOf(MapView.class, mainView.findViewById(R.id.map_view));
    }

    @Test
    public void testViewEnabled() throws Exception {
        View mediaView = viewLoader.inflateView(context, "layout/main");
        assertThat(mediaView.findViewById(R.id.time).isEnabled(), equalTo(false));
    }

    @Test
    public void testViewBackgroundIdIsSet() throws Exception {
        View mediaView = viewLoader.inflateView(context, "layout/main");
        ImageView imageView = (ImageView) mediaView.findViewById(R.id.image);
        ShadowImageView shadowImageView = Robolectric.shadowOf(imageView);

        assertThat(shadowImageView.getBackgroundResourceId(), equalTo(R.drawable.image_background));
    }
}
