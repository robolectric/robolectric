package org.robolectric.res;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.google.android.maps.MapView;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;
import org.robolectric.res.builder.LayoutBuilder;
import org.robolectric.shadows.ShadowImageView;
import org.robolectric.shadows.StubViewRoot;
import org.robolectric.util.CustomView;
import org.robolectric.util.CustomView2;
import org.robolectric.util.I18nException;
import org.robolectric.util.TestUtil;

import java.util.ArrayList;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.robolectric.Robolectric.shadowOf;
import static org.robolectric.test.Assertions.assertThat;
import static org.robolectric.util.TestUtil.TEST_PACKAGE;
import static org.robolectric.util.TestUtil.assertInstanceOf;

@RunWith(TestRunners.WithDefaults.class)
public class LayoutLoaderTest {
  private Activity context;
  private ResourceLoader resourceLoader;

  @Before
  public void setUp() throws Exception {
    resourceLoader = Robolectric.getShadowApplication().getResourceLoader();
    context = new Activity() {};
  }

  @Test
  public void testCreatesCorrectClasses() throws Exception {
    ViewGroup view = (ViewGroup) inflate("media");
    TestUtil.assertInstanceOf(LinearLayout.class, view);

    assertSame(context, view.getContext());
  }

  @Test
  public void testChoosesLayoutBasedOnDefaultScreenSize() throws Exception {
    ViewGroup view = (ViewGroup) inflate("different_screen_sizes");
    TextView textView = (TextView) view.findViewById(android.R.id.text1);
    assertThat(textView.getText().toString()).isEqualTo("default");
  }

  @Test @Config(qualifiers = "xlarge-land")
  public void testChoosesLayoutBasedOnSearchPath_choosesFirstFileFoundOnPath() throws Exception {
//        resourceLoader.setLayoutQualifierSearchPath("xlarge", "land");
    ViewGroup view = (ViewGroup) inflate("different_screen_sizes", "xlarge-land");
    TextView textView = (TextView) view.findViewById(android.R.id.text1);
    assertThat(textView.getText().toString()).isEqualTo("xlarge");
  }

  @Test @Config(qualifiers = "doesnotexist-land-xlarge")
  public void testChoosesLayoutBasedOnSearchPath_respectsOrderOfPath() throws Exception {
    ViewGroup view = (ViewGroup) inflate("different_screen_sizes", "doesnotexist-land-xlarge");
    TextView textView = (TextView) view.findViewById(android.R.id.text1);
    assertThat(textView.getText().toString()).isEqualTo("land");
  }

  @Test
  public void testWebView() throws Exception {
    ViewGroup view = (ViewGroup) inflate("webview_holder");
    WebView webView = (WebView) view.findViewById(R.id.web_view);

    webView.loadUrl("www.example.com");

    assertThat(shadowOf(webView).getLastLoadedUrl()).isEqualTo("www.example.com");
  }

  @Test
  public void testAddsChildren() throws Exception {
    ViewGroup view = (ViewGroup) inflate("media");
    assertTrue(view.getChildCount() > 0);

    assertSame(context, view.getChildAt(0).getContext());
  }

  @Test
  public void testFindsChildrenById() throws Exception {
    ViewGroup mediaView = (ViewGroup) inflate("media");
    TestUtil.assertInstanceOf(TextView.class, mediaView.findViewById(R.id.title));

    ViewGroup mainView = (ViewGroup) inflate("main");
    assertInstanceOf(View.class, mainView.findViewById(R.id.title));
  }

  @Test
  public void testInflatingConflictingSystemAndLocalViewsWorks() throws Exception {
    ViewGroup view = (ViewGroup) inflate("activity_list_item");
    assertInstanceOf(ImageView.class, view.findViewById(R.id.icon));

    view = (ViewGroup) inflate("android", "activity_list_item", "");
    assertInstanceOf(ImageView.class, view.findViewById(android.R.id.icon));
  }

  @Test
  public void testInclude() throws Exception {
    ViewGroup mediaView = (ViewGroup) inflate("media");
    assertInstanceOf(TextView.class, mediaView.findViewById(R.id.include_id));
  }

  @Test
  public void testIncludeShouldRetainAttributes() throws Exception {
    ViewGroup mediaView = (ViewGroup) inflate("media");
    assertThat(mediaView.findViewById(R.id.include_id).getVisibility()).isEqualTo(View.GONE);
  }

  @Test
  public void shouldOverwriteIdOnIncludedNonMerge() throws Exception {
    ViewGroup mediaView = (ViewGroup) inflate("media");
    assertNull(mediaView.findViewById(R.id.snippet_text));
  }

  @Test
  public void shouldRetainIdOnIncludedMergeWhenIncludeSpecifiesNoId() throws Exception {
    ViewGroup mediaView = (ViewGroup) inflate("override_include");
    assertInstanceOf(TextView.class, mediaView.findViewById(R.id.inner_text));
  }

  @Test
  public void shouldRetainIdOnIncludedNonMergeWhenIncludeSpecifiesNoId() throws Exception {
    ViewGroup mediaView = (ViewGroup) inflate("override_include");
    assertInstanceOf(TextView.class, mediaView.findViewById(R.id.snippet_text));
  }

  @Test
  public void testIncludedIdShouldNotBeFoundWhenIncludedIsMerge() throws Exception {
    ViewGroup overrideIncludeView = (ViewGroup) inflate("outer");
    assertInstanceOf(LinearLayout.class, overrideIncludeView.findViewById(R.id.outer_merge));
    assertInstanceOf(TextView.class, overrideIncludeView.findViewById(R.id.inner_text));
    assertNull(overrideIncludeView.findViewById(R.id.include_id));
    assertEquals(1, overrideIncludeView.getChildCount());
  }

  @Test
  public void testIncludeShouldOverrideAttributesOfIncludedRootNode() throws Exception {
    ViewGroup overrideIncludeView = (ViewGroup) inflate("override_include");
    assertThat(overrideIncludeView.findViewById(R.id.snippet_text).getVisibility()).isEqualTo(View.INVISIBLE);
  }

  @Test
  public void shouldNotCountRequestFocusElementAsChild() throws Exception {
    ViewGroup viewGroup = (ViewGroup) inflate("request_focus");
    ViewGroup frameLayout = (ViewGroup) viewGroup.getChildAt(1);
    assertEquals(0, frameLayout.getChildCount());
  }

  @Test
  public void focusRequest_shouldNotExplodeOnViewRootImpl() throws Exception {
    LinearLayout parent = new LinearLayout(context);
    shadowOf(parent).setMyParent(new StubViewRoot());
    ViewGroup viewGroup = (ViewGroup) inflate(context, TEST_PACKAGE, "request_focus", parent, "");
    ViewGroup frameLayout = (ViewGroup) viewGroup.getChildAt(1);
    assertEquals(0, frameLayout.getChildCount());
  }

  @Ignore("maybe not a valid test in the 2.0 world?") // todo  2.0-cleanup
  @Test
  public void shouldGiveFocusToElementContainingRequestFocusElement() throws Exception {
    ViewGroup viewGroup = (ViewGroup) inflate("request_focus");
    EditText editText = (EditText) viewGroup.findViewById(R.id.edit_text);
    assertFalse(editText.isFocused());
  }

  @Test
  public void shouldGiveFocusToFirstFocusableElement_butThisMightBeTheWrongBehavior() throws Exception {
    ViewGroup viewGroup = (ViewGroup) inflate("request_focus_with_two_edit_texts");
    assertTrue(viewGroup.findViewById(R.id.edit_text).isFocused());
    assertFalse(viewGroup.findViewById(R.id.edit_text2).isFocused());
  }

  @Test
  public void testMerge() throws Exception {
    ViewGroup mediaView = (ViewGroup) inflate("outer");
    TestUtil.assertInstanceOf(TextView.class, mediaView.findViewById(R.id.inner_text));
  }

  @Test
  public void mergeIncludesShouldNotCreateAncestryLoops() throws Exception {
    ViewGroup mediaView = (ViewGroup) inflate("outer");
    mediaView.hasFocus();
  }

  @Test
  public void testViewGroupsLooksAtItsOwnId() throws Exception {
    TextView mediaView = (TextView) inflate("snippet");
    assertSame(mediaView, mediaView.findViewById(R.id.snippet_text));
  }

  @Test
  public void shouldConstructCustomViewsWithAttributesConstructor() throws Exception {
    CustomView view = (CustomView) inflate("custom_layout");
    assertThat(view.attributeResourceValue).isEqualTo(R.string.hello);
  }

  @Test
  public void shouldConstructCustomViewsWithAttributesWithURLEncodedNamespaces() throws Exception {
    CustomView view = (CustomView) inflate("custom_layout4")
        .findViewById(R.id.custom_view);
    assertThat(view.namespacedResourceValue).isEqualTo(R.layout.text_views);
  }

  @Test
  public void testViewVisibilityIsSet() throws Exception {
    View mediaView = inflate("media");
    assertThat(mediaView.findViewById(R.id.title).getVisibility()).isEqualTo(View.VISIBLE);
    assertThat(mediaView.findViewById(R.id.subtitle).getVisibility()).isEqualTo(View.GONE);
  }

  @Test
  public void testTextViewTextIsSet() throws Exception {
    View mediaView = inflate("main");
    assertThat(((TextView) mediaView.findViewById(R.id.title)).getText().toString()).isEqualTo("Main Layout");
    assertThat(((TextView) mediaView.findViewById(R.id.subtitle)).getText().toString()).isEqualTo("Hello");
  }

  @Test
  public void testTextViewCompoundDrawablesAreSet() throws Exception {
    View mediaView = inflate("main");
    TextView view = (TextView) mediaView.findViewById(R.id.title);

    assertThat(view.getCompoundDrawables()[0]).isEqualTo(drawable(R.drawable.fourth_image));
    assertThat(view.getCompoundDrawables()[1]).isEqualTo(drawable(R.drawable.an_image));
    assertThat(view.getCompoundDrawables()[2]).isEqualTo(drawable(R.drawable.an_other_image));
    assertThat(view.getCompoundDrawables()[3]).isEqualTo(drawable(R.drawable.third_image));
  }

  @Test
  public void testCheckBoxCheckedIsSet() throws Exception {
    View mediaView = inflate("main");
    assertThat(((CheckBox) mediaView.findViewById(R.id.true_checkbox)).isChecked()).isTrue();
    assertThat(((CheckBox) mediaView.findViewById(R.id.false_checkbox)).isChecked()).isFalse();
    assertThat(((CheckBox) mediaView.findViewById(R.id.default_checkbox)).isChecked()).isFalse();
  }

  @Test
  public void testImageViewSrcIsSet() throws Exception {
    View mediaView = inflate("main");
    ImageView imageView = (ImageView) mediaView.findViewById(R.id.image);
    BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
    assertThat(shadowOf(drawable.getBitmap()).getCreatedFromResId()).isEqualTo(R.drawable.an_image);
  }

  @Test
  public void shouldInflateMergeLayoutIntoParent() throws Exception {
    View innerMerge = new LayoutBuilder(resourceLoader).inflateView(context, R.layout.inner_merge, new LinearLayout(Robolectric.application), "");
    assertNotNull(innerMerge);
  }

  @Test
  public void testMapView() throws Exception {
    RelativeLayout mainView = (RelativeLayout) inflate("mapview");
    assertThat(mainView.findViewById(R.id.map_view)).isInstanceOf(MapView.class);
  }

  @Test
  @Ignore
  public void testFragment() throws Exception {
    FragmentActivity fragmentActivity = new FragmentActivity();
    context = fragmentActivity;
    View v = inflate("fragment");
    TestUtil.assertInstanceOf(TextView.class, v);
    final FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
    Fragment fragment = fragmentManager.findFragmentById(R.id.my_fragment);
    assertNotNull(fragment);
  }

  @Test
  public void testMultiOrientation() throws Exception {
    context = new FragmentActivity();
    shadowOf(context).callOnCreate(null);
    shadowOf(context).callOnStart();
    shadowOf(context).callOnResume();

    // Default screen orientation should be portrait.
    ViewGroup view = (ViewGroup) inflate("multi_orientation");
    TestUtil.assertInstanceOf(LinearLayout.class, view);
    assertEquals(view.getId(), R.id.portrait);
    assertSame(context, view.getContext());

    // Confirm explicit "orientation = portrait" works.
    context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    view = (ViewGroup) inflate("multi_orientation");
    TestUtil.assertInstanceOf(LinearLayout.class, view);
    assertEquals(view.getId(), R.id.portrait);
    assertSame(context, view.getContext());

    // Confirm explicit "orientation = landscape" works.
    context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    view = (ViewGroup) inflate("multi_orientation", "land");
    assertEquals(view.getId(), R.id.landscape);
    TestUtil.assertInstanceOf(LinearLayout.class, view);
  }

  @Test
  public void testViewEnabled() throws Exception {
    View mediaView = inflate("main");
    assertThat(mediaView.findViewById(R.id.time).isEnabled()).isFalse();
  }

  @Test
  public void testContentDescriptionIsSet() throws Exception {
    View mediaView = inflate("main");
    assertThat(mediaView.findViewById(R.id.time).getContentDescription().toString()).isEqualTo("Howdy");
  }

  @Test
  public void testAlphaIsSet() throws Exception {
    View mediaView = inflate("main");
    assertThat(mediaView.findViewById(R.id.time).getAlpha()).isEqualTo(.3f);
  }

  @Test
  public void testViewBackgroundIdIsSet() throws Exception {
    View mediaView = inflate("main");
    ImageView imageView = (ImageView) mediaView.findViewById(R.id.image);
    ShadowImageView shadowImageView = Robolectric.shadowOf(imageView);

    assertThat(imageView.getBackground()).isResource(R.drawable.image_background);
    assertThat(shadowImageView.getBackgroundResourceId()).isEqualTo(R.drawable.image_background);
  }

  @Test
  public void testOnClickAttribute() throws Exception {
    ClickActivity activity = new ClickActivity();
    activity.onCreate(null);

    assertThat(activity.clicked).isFalse();

    Button button = (Button)activity.findViewById(R.id.button);
    button.performClick();

    assertThat(activity.clicked).isTrue();
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
    }
    assertNotNull(exception);
    assertThat(exception.getMessage())
        .as("The error message should contain the id name of the faulty button")
        .contains("invalid_onclick_button");
  }

  @Test
  public void shouldInvokeOnFinishInflate() throws Exception {
    CustomView2 outerCustomView = (CustomView2) inflate("custom_layout2");
    CustomView2 innerCustomView = (CustomView2) outerCustomView.getChildAt(0);
    assertThat(outerCustomView.childCountAfterInflate).isEqualTo(1);
    assertThat(innerCustomView.childCountAfterInflate).isEqualTo(3);
  }

  @SuppressWarnings("UnusedDeclaration")
  public static class CustomView3 extends TextView {
    public CustomView3(Context context) { super(context); }
    public CustomView3(Context context, AttributeSet attrs) { super(context, attrs); }
    public CustomView3(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }
  }

  @Test
  public void shouldInflateViewsWithClassAttr() throws Exception {
    CustomView3 outerCustomView = (CustomView3) inflate("custom_layout3");
    assertThat(outerCustomView.getText().toString()).isEqualTo("Hello bonjour");
  }

  @Test
  public void testIncludesLinearLayoutsOnlyOnce() throws Exception {
    ViewGroup parentView = (ViewGroup) inflate("included_layout_parent");
    assertEquals(1, parentView.getChildCount());
  }

  @Test(expected=I18nException.class)
  public void shouldThrowI18nExceptionOnLayoutWithBareStrings() throws Exception {
    Robolectric.getShadowApplication().setStrictI18n(true);
    inflate("text_views");
  }

  private View inflate(String packageName, String layoutName, String qualifiers) {
    return inflate(context, packageName, layoutName, null, qualifiers);
  }

  public View inflate(Context context, String packageName, String key, ViewGroup parent, String qualifiers) {
    ResourceLoader resourceLoader = shadowOf(context.getResources()).getResourceLoader();
    return new LayoutBuilder(resourceLoader).inflateView(context, new ResName(packageName + ":layout/" + key),
        new ArrayList<Attribute>(), parent, qualifiers);
  }

  private View inflate(String layoutName) {
    return inflate(layoutName, "");
  }

  private View inflate(String layoutName, String qualifiers) {
    return inflate(TEST_PACKAGE, layoutName, qualifiers);
  }

  private Drawable drawable(int id) {
    Drawable drawable = context.getResources().getDrawable(id);
    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
    return drawable;
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
