package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.test.Assertions.assertThat;
import static org.robolectric.util.TestUtil.TEST_PACKAGE;
import static org.robolectric.util.TestUtil.assertInstanceOf;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.TestRunners;
import org.robolectric.android.CustomStateView;
import org.robolectric.android.CustomView;
import org.robolectric.android.CustomView2;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

@RunWith(TestRunners.MultiApiSelfTest.class)
public class ShadowLayoutInflaterTest {
  private Activity context;

  @Before
  public void setUp() throws Exception {
    context = buildActivity(Activity.class).create().get();
  }

  @Test
  public void testCreatesCorrectClasses() throws Exception {
    int layoutResId = context.getResources().getIdentifier("media", "layout", TEST_PACKAGE);
    ViewGroup view = (ViewGroup) LayoutInflater.from(context).inflate(layoutResId, null);
    assertInstanceOf(LinearLayout.class, view);

    assertSame(context, view.getContext());
  }

  @Test
  public void testChoosesLayoutBasedOnDefaultScreenSize() throws Exception {
    int layoutResId = context.getResources().getIdentifier("different_screen_sizes", "layout", TEST_PACKAGE);
    ViewGroup view = (ViewGroup) LayoutInflater.from(context).inflate(layoutResId, null);
    TextView textView = (TextView) view.findViewById(android.R.id.text1);
    assertThat(textView.getText().toString()).isEqualTo("default");
  }

  @Test @Config(qualifiers = "xlarge-land")
  public void testChoosesLayoutBasedOnSearchPath_choosesFirstFileFoundOnPath() throws Exception {
    int layoutResId = context.getResources().getIdentifier("different_screen_sizes", "layout", TEST_PACKAGE);
    ViewGroup view = (ViewGroup) LayoutInflater.from(context).inflate(layoutResId, null);
    TextView textView = (TextView) view.findViewById(android.R.id.text1);
    assertThat(textView.getText().toString()).isEqualTo("xlarge");
  }

  @Test @Config(qualifiers = "doesnotexist-land-xlarge")
  public void testChoosesLayoutBasedOnSearchPath_respectsOrderOfPath() throws Exception {
    int layoutResId = context.getResources().getIdentifier("different_screen_sizes", "layout", TEST_PACKAGE);
    ViewGroup view = (ViewGroup) LayoutInflater.from(context).inflate(layoutResId, null);
    TextView textView = (TextView) view.findViewById(android.R.id.text1);
    assertThat(textView.getText().toString()).isEqualTo("land");
  }

  @Test
  public void testWebView() throws Exception {
    int layoutResId = context.getResources().getIdentifier("webview_holder", "layout", TEST_PACKAGE);
    ViewGroup view = (ViewGroup) LayoutInflater.from(context).inflate(layoutResId, null);
    WebView webView = (WebView) view.findViewById(R.id.web_view);

    webView.loadUrl("www.example.com");

    assertThat(shadowOf(webView).getLastLoadedUrl()).isEqualTo("www.example.com");
  }

  @Test
  public void testAddsChildren() throws Exception {
    int layoutResId = context.getResources().getIdentifier("media", "layout", TEST_PACKAGE);
    ViewGroup view = (ViewGroup) LayoutInflater.from(context).inflate(layoutResId, null);
    assertTrue(view.getChildCount() > 0);

    assertSame(context, view.getChildAt(0).getContext());
  }

  @Test
  public void testFindsChildrenById() throws Exception {
    int layoutResId1 = context.getResources().getIdentifier("media", "layout", TEST_PACKAGE);
    ViewGroup mediaView = (ViewGroup) LayoutInflater.from(context).inflate(layoutResId1, null);
    assertInstanceOf(TextView.class, mediaView.findViewById(R.id.title));

    int layoutResId = context.getResources().getIdentifier("main", "layout", TEST_PACKAGE);
    ViewGroup mainView = (ViewGroup) LayoutInflater.from(context).inflate(layoutResId, null);
    assertInstanceOf(View.class, mainView.findViewById(R.id.title));
  }

  @Test
  public void testInflatingConflictingSystemAndLocalViewsWorks() throws Exception {
    int layoutResId1 = context.getResources().getIdentifier("activity_list_item", "layout", TEST_PACKAGE);
    ViewGroup view = (ViewGroup) LayoutInflater.from(context).inflate(layoutResId1, null);
    assertInstanceOf(ImageView.class, view.findViewById(R.id.icon));

    int layoutResId = context.getResources().getIdentifier("activity_list_item", "layout", "android");
    view = (ViewGroup) LayoutInflater.from(context).inflate(layoutResId, null);
    assertInstanceOf(ImageView.class, view.findViewById(android.R.id.icon));
  }

  @Test
  public void testInclude() throws Exception {
    int layoutResId = context.getResources().getIdentifier("media", "layout", TEST_PACKAGE);
    ViewGroup mediaView = (ViewGroup) LayoutInflater.from(context).inflate(layoutResId, null);
    assertInstanceOf(TextView.class, mediaView.findViewById(R.id.include_id));
  }

  @Test
  public void testIncludeShouldRetainAttributes() throws Exception {
    int layoutResId = context.getResources().getIdentifier("media", "layout", TEST_PACKAGE);
    ViewGroup mediaView = (ViewGroup) LayoutInflater.from(context).inflate(layoutResId, null);
    assertThat(mediaView.findViewById(R.id.include_id).getVisibility()).isEqualTo(View.GONE);
  }

  @Test
  public void shouldOverwriteIdOnIncludedNonMerge() throws Exception {
    int layoutResId = context.getResources().getIdentifier("media", "layout", TEST_PACKAGE);
    ViewGroup mediaView = (ViewGroup) LayoutInflater.from(context).inflate(layoutResId, null);
    assertNull(mediaView.findViewById(R.id.snippet_text));
  }

  @Test
  public void shouldRetainIdOnIncludedMergeWhenIncludeSpecifiesNoId() throws Exception {
    int layoutResId = context.getResources().getIdentifier("override_include", "layout", TEST_PACKAGE);
    ViewGroup mediaView = (ViewGroup) LayoutInflater.from(context).inflate(layoutResId, null);
    assertInstanceOf(TextView.class, mediaView.findViewById(R.id.inner_text));
  }

  @Test
  public void shouldRetainIdOnIncludedNonMergeWhenIncludeSpecifiesNoId() throws Exception {
    int layoutResId = context.getResources().getIdentifier("override_include", "layout", TEST_PACKAGE);
    ViewGroup mediaView = (ViewGroup) LayoutInflater.from(context).inflate(layoutResId, null);
    assertInstanceOf(TextView.class, mediaView.findViewById(R.id.snippet_text));
  }

  @Test
  public void testIncludedIdShouldNotBeFoundWhenIncludedIsMerge() throws Exception {
    int layoutResId = context.getResources().getIdentifier("outer", "layout", TEST_PACKAGE);
    ViewGroup overrideIncludeView = (ViewGroup) LayoutInflater.from(context).inflate(layoutResId, null);
    assertInstanceOf(LinearLayout.class, overrideIncludeView.findViewById(R.id.outer_merge));
    assertInstanceOf(TextView.class, overrideIncludeView.findViewById(R.id.inner_text));
    assertNull(overrideIncludeView.findViewById(R.id.include_id));
    assertEquals(1, overrideIncludeView.getChildCount());
  }

  @Test
  public void testIncludeShouldOverrideAttributesOfIncludedRootNode() throws Exception {
    int layoutResId = context.getResources().getIdentifier("override_include", "layout", TEST_PACKAGE);
    ViewGroup overrideIncludeView = (ViewGroup) LayoutInflater.from(context).inflate(layoutResId, null);
    assertThat(overrideIncludeView.findViewById(R.id.snippet_text).getVisibility()).isEqualTo(View.INVISIBLE);
  }

  @Test
  public void shouldNotCountRequestFocusElementAsChild() throws Exception {
    int layoutResId = context.getResources().getIdentifier("request_focus", "layout", TEST_PACKAGE);
    ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(context).inflate(layoutResId, null);
    ViewGroup frameLayout = (ViewGroup) viewGroup.getChildAt(1);
    assertEquals(0, frameLayout.getChildCount());
  }

  @Test
  public void focusRequest_shouldNotExplodeOnViewRootImpl() throws Exception {
    LinearLayout parent = new LinearLayout(context);
    shadowOf(parent).setMyParent(ReflectionHelpers.createNullProxy(ViewParent.class));
    int layoutResId = context.getResources().getIdentifier("request_focus", "layout", TEST_PACKAGE);
    LayoutInflater.from(context).inflate(layoutResId, parent);
  }

  @Test
  public void shouldGiveFocusToElementContainingRequestFocusElement() throws Exception {
    int layoutResId = context.getResources().getIdentifier("request_focus", "layout", TEST_PACKAGE);
    ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(context).inflate(layoutResId, null);
    EditText editText = (EditText) viewGroup.findViewById(R.id.edit_text);
    assertFalse(editText.isFocused());
  }

  @Test
  public void testMerge() throws Exception {
    int layoutResId = context.getResources().getIdentifier("outer", "layout", TEST_PACKAGE);
    ViewGroup mediaView = (ViewGroup) LayoutInflater.from(context).inflate(layoutResId, null);
    assertInstanceOf(TextView.class, mediaView.findViewById(R.id.inner_text));
  }

  @Test
  public void mergeIncludesShouldNotCreateAncestryLoops() throws Exception {
    int layoutResId = context.getResources().getIdentifier("outer", "layout", TEST_PACKAGE);
    ViewGroup mediaView = (ViewGroup) LayoutInflater.from(context).inflate(layoutResId, null);
    mediaView.hasFocus();
  }

  @Test
  public void testViewGroupsLooksAtItsOwnId() throws Exception {
    int layoutResId = context.getResources().getIdentifier("snippet", "layout", TEST_PACKAGE);
    TextView mediaView = (TextView) LayoutInflater.from(context).inflate(layoutResId, null);
    assertSame(mediaView, mediaView.findViewById(R.id.snippet_text));
  }

  @Test
  public void shouldConstructCustomViewsWithAttributesConstructor() throws Exception {
    int layoutResId = context.getResources().getIdentifier("custom_layout", "layout", TEST_PACKAGE);
    CustomView view = (CustomView) LayoutInflater.from(context).inflate(layoutResId, null);
    assertThat(view.attributeResourceValue).isEqualTo(R.string.hello);
  }

  @Test
  public void shouldConstructCustomViewsWithCustomState() throws Exception {
    int layoutResId = context.getResources().getIdentifier("custom_layout6", "layout", TEST_PACKAGE);
    CustomStateView view = (CustomStateView) LayoutInflater.from(context).inflate(layoutResId, null);
    assertThat(view.getDrawableState()).doesNotContain(R.attr.stateFoo);

    view.isFoo = true;
    view.refreshDrawableState();

    assertThat(view.getDrawableState()).contains(R.attr.stateFoo);
  }

  @Test
  public void shouldConstructCustomViewsWithAttributesInResAutoNamespace() throws Exception {
    int layoutResId = context.getResources().getIdentifier("custom_layout5", "layout", TEST_PACKAGE);
    CustomView view = (CustomView) LayoutInflater.from(context).inflate(layoutResId, null);
    assertThat(view.attributeResourceValue).isEqualTo(R.string.hello);
  }

  @Test
  public void shouldConstructCustomViewsWithAttributesWithURLEncodedNamespaces() throws Exception {
    int layoutResId = context.getResources().getIdentifier("custom_layout4", "layout", TEST_PACKAGE);
    CustomView view = (CustomView) LayoutInflater.from(context).inflate(layoutResId, null)
        .findViewById(R.id.custom_view);
    assertThat(view.namespacedResourceValue).isEqualTo(R.layout.text_views);
  }

  @Test
  public void testViewVisibilityIsSet() throws Exception {
    int layoutResId = context.getResources().getIdentifier("media", "layout", TEST_PACKAGE);
    View mediaView = LayoutInflater.from(context).inflate(layoutResId, null);
    assertThat(mediaView.findViewById(R.id.title).getVisibility()).isEqualTo(View.VISIBLE);
    assertThat(mediaView.findViewById(R.id.subtitle).getVisibility()).isEqualTo(View.GONE);
  }

  @Test
  public void testTextViewTextIsSet() throws Exception {
    int layoutResId = context.getResources().getIdentifier("main", "layout", TEST_PACKAGE);
    View mediaView = LayoutInflater.from(context).inflate(layoutResId, null);
    assertThat(((TextView) mediaView.findViewById(R.id.title)).getText().toString()).isEqualTo("Main Layout");
    assertThat(((TextView) mediaView.findViewById(R.id.subtitle)).getText().toString()).isEqualTo("Hello");
  }

  @Test
  public void testTextViewCompoundDrawablesAreSet() throws Exception {
    int layoutResId = context.getResources().getIdentifier("main", "layout", TEST_PACKAGE);
    View mediaView = LayoutInflater.from(context).inflate(layoutResId, null);
    TextView view = (TextView) mediaView.findViewById(R.id.title);

    assertThat(view.getCompoundDrawables()[0]).isEqualTo(drawable(R.drawable.fourth_image));
    assertThat(view.getCompoundDrawables()[1]).isEqualTo(drawable(R.drawable.an_image));
    assertThat(view.getCompoundDrawables()[2]).isEqualTo(drawable(R.drawable.an_other_image));
    assertThat(view.getCompoundDrawables()[3]).isEqualTo(drawable(R.drawable.third_image));
  }

  @Test
  public void testCheckBoxCheckedIsSet() throws Exception {
    int layoutResId = context.getResources().getIdentifier("main", "layout", TEST_PACKAGE);
    View mediaView = LayoutInflater.from(context).inflate(layoutResId, null);
    assertThat(((CheckBox) mediaView.findViewById(R.id.true_checkbox)).isChecked()).isTrue();
    assertThat(((CheckBox) mediaView.findViewById(R.id.false_checkbox)).isChecked()).isFalse();
    assertThat(((CheckBox) mediaView.findViewById(R.id.default_checkbox)).isChecked()).isFalse();
  }

  @Test
  public void testImageViewSrcIsSet() throws Exception {
    int layoutResId = context.getResources().getIdentifier("main", "layout", TEST_PACKAGE);
    View mediaView = LayoutInflater.from(context).inflate(layoutResId, null);
    ImageView imageView = (ImageView) mediaView.findViewById(R.id.image);
    BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
    assertThat(shadowOf(drawable.getBitmap()).getCreatedFromResId()).isEqualTo(R.drawable.an_image);
  }

  @Test
  public void testImageViewSrcIsSetFromMipmap() throws Exception {
    int layoutResId = context.getResources().getIdentifier("main", "layout", TEST_PACKAGE);
    View mediaView = LayoutInflater.from(context).inflate(layoutResId, null);
    ImageView imageView = (ImageView) mediaView.findViewById(R.id.mipmapImage);
    BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
    assertThat(shadowOf(drawable.getBitmap()).getCreatedFromResId()).isEqualTo(R.mipmap.robolectric);
  }

  @Test
  public void shouldInflateMergeLayoutIntoParent() throws Exception {
    LinearLayout linearLayout = new LinearLayout(context);
    int layoutResId = context.getResources().getIdentifier("inner_merge", "layout", TEST_PACKAGE);
    View innerMerge = LayoutInflater.from(context).inflate(layoutResId, linearLayout);
    assertThat(linearLayout.getChildAt(0)).isInstanceOf(TextView.class);
  }

  @Test
  public void testMultiOrientation() throws Exception {
    context = buildActivity(Activity.class).create().start().resume().get();

    // Default screen orientation should be portrait.
    int layoutResId1 = context.getResources().getIdentifier("multi_orientation", "layout", TEST_PACKAGE);
    ViewGroup view = (ViewGroup) LayoutInflater.from(context).inflate(layoutResId1, null);
    assertInstanceOf(LinearLayout.class, view);
    assertEquals(view.getId(), R.id.portrait);
    assertSame(context, view.getContext());

    // Confirm explicit "orientation = portrait" works.
    context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    int layoutResId = context.getResources().getIdentifier("multi_orientation", "layout", TEST_PACKAGE);
    view = (ViewGroup) LayoutInflater.from(context).inflate(layoutResId, null);
    assertInstanceOf(LinearLayout.class, view);
    assertEquals(view.getId(), R.id.portrait);
    assertSame(context, view.getContext());
  }

  @Test
  @Config(qualifiers = "land")
  public void testMultiOrientation_explicitLandscape() throws Exception {
    context = buildActivity(Activity.class).create().start().resume().get();

    // Confirm explicit "orientation = landscape" works.
    context.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    int layoutResId = context.getResources().getIdentifier("multi_orientation", "layout", TEST_PACKAGE);
    ViewGroup view = (ViewGroup) LayoutInflater.from(context).inflate(layoutResId, null);
    assertEquals(view.getId(), R.id.landscape);
    assertInstanceOf(LinearLayout.class, view);
  }

  @Test
  @Config(qualifiers = "w0dp")
  public void testSetContentViewByItemResource() throws Exception {
    Activity activity = buildActivity(Activity.class).create().get();
    activity.setContentView(R.layout.main_layout);

    TextView tv1 = (TextView) activity.findViewById(R.id.hello);
    TextView tv2 = (TextView) activity.findViewById(R.id.world);
    assertNotNull(tv1);
    assertNull(tv2);
  }

  @Test
  @Config(qualifiers = "w820dp")
  public void testSetContentViewByItemResourceWithW820dp() throws Exception {
    Activity activity = buildActivity(Activity.class).create().get();
    activity.setContentView(R.layout.main_layout);

    TextView tv1 = (TextView) activity.findViewById(R.id.hello);
    TextView tv2 = (TextView) activity.findViewById(R.id.world);
    assertNotNull(tv1);
    assertNotNull(tv2);
  }

  @Test
  public void testViewEnabled() throws Exception {
    int layoutResId = context.getResources().getIdentifier("main", "layout", TEST_PACKAGE);
    View mediaView = LayoutInflater.from(context).inflate(layoutResId, null);
    assertThat(mediaView.findViewById(R.id.time).isEnabled()).isFalse();
  }

  @Test
  public void testContentDescriptionIsSet() throws Exception {
    int layoutResId = context.getResources().getIdentifier("main", "layout", TEST_PACKAGE);
    View mediaView = LayoutInflater.from(context).inflate(layoutResId, null);
    assertThat(mediaView.findViewById(R.id.time).getContentDescription().toString()).isEqualTo("Howdy");
  }

  @Test
  public void testAlphaIsSet() throws Exception {
    int layoutResId = context.getResources().getIdentifier("main", "layout", TEST_PACKAGE);
    View mediaView = LayoutInflater.from(context).inflate(layoutResId, null);
    assertThat(mediaView.findViewById(R.id.time).getAlpha()).isEqualTo(.3f);
  }

  @Test
  public void testViewBackgroundIdIsSet() throws Exception {
    int layoutResId = context.getResources().getIdentifier("main", "layout", TEST_PACKAGE);
    View mediaView = LayoutInflater.from(context).inflate(layoutResId, null);
    ImageView imageView = (ImageView) mediaView.findViewById(R.id.image);

    assertThat(imageView.getBackground()).isResource(R.drawable.image_background);
    assertThat(shadowOf(imageView).getBackgroundResourceId()).isEqualTo(R.drawable.image_background);
  }

  @Test
  public void testOnClickAttribute() throws Exception {
    ClickActivity activity = buildActivity(ClickActivity.class).create().get();

    assertThat(activity.clicked).isFalse();

    Button button = (Button)activity.findViewById(R.id.button);
    button.performClick();

    assertThat(activity.clicked).isTrue();
  }

  @Test
  public void testInvalidOnClickAttribute() throws Exception {
    Activity activity = buildActivity(Activity.class).create().get();
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
    int layoutResId = context.getResources().getIdentifier("custom_layout2", "layout", TEST_PACKAGE);
    CustomView2 outerCustomView = (CustomView2) LayoutInflater.from(context).inflate(layoutResId, null);
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
    int layoutResId = context.getResources().getIdentifier("custom_layout3", "layout", TEST_PACKAGE);
    CustomView3 outerCustomView = (CustomView3) LayoutInflater.from(context).inflate(layoutResId, null);
    assertThat(outerCustomView.getText().toString()).isEqualTo("Hello bonjour");
  }

  @Test
  public void testIncludesLinearLayoutsOnlyOnce() throws Exception {
    int layoutResId = context.getResources().getIdentifier("included_layout_parent", "layout", TEST_PACKAGE);
    ViewGroup parentView = (ViewGroup) LayoutInflater.from(context).inflate(layoutResId, null);
    assertEquals(1, parentView.getChildCount());
  }

  @Test
  public void testConverterAcceptsEnumOrdinal() throws Exception {
    int layoutResId = context.getResources().getIdentifier("ordinal_scrollbar", "layout", TEST_PACKAGE);
    ViewGroup view = (ViewGroup) LayoutInflater.from(context).inflate(layoutResId, null);
    assertInstanceOf(RelativeLayout.class, view);
    ListView listView = (ListView)
        view.findViewById(org.robolectric.R.id.list_view_with_enum_scrollbar);
    assertInstanceOf(ListView.class, listView);
  }

  /////////////////////////

  private Drawable drawable(int id) {
    Drawable drawable = context.getResources().getDrawable(id);
    drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
    return drawable;
  }

  public static class ClickActivity extends Activity {
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
