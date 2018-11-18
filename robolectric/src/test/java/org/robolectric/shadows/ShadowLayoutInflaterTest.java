package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Shadows.shadowOf;

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
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.R.layout;
import org.robolectric.android.CustomStateView;
import org.robolectric.android.CustomView;
import org.robolectric.android.CustomView2;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

@RunWith(AndroidJUnit4.class)
public class ShadowLayoutInflaterTest {
  private Context context;

  @Before
  public void setUp() throws Exception {
    context = ApplicationProvider.getApplicationContext();
  }

  @Test
  public void testCreatesCorrectClasses() throws Exception {
    ViewGroup view = inflate(R.layout.media);
    assertThat(view).isInstanceOf(LinearLayout.class);

    assertSame(context, view.getContext());
  }

  @Test
  public void testChoosesLayoutBasedOnDefaultScreenSize() throws Exception {
    ViewGroup view = inflate(R.layout.different_screen_sizes);
    TextView textView = view.findViewById(android.R.id.text1);
    assertThat(textView.getText().toString()).isEqualTo("default");
  }

  @Test
  @Config(qualifiers = "xlarge")
  public void testChoosesLayoutBasedOnScreenSize() throws Exception {
    ViewGroup view = inflate(R.layout.different_screen_sizes);
    TextView textView = view.findViewById(android.R.id.text1);
    assertThat(textView.getText().toString()).isEqualTo("xlarge");
  }

  @Test
  @Config(qualifiers = "land")
  public void testChoosesLayoutBasedOnQualifiers() throws Exception {
    ViewGroup view = inflate(R.layout.different_screen_sizes);
    TextView textView = view.findViewById(android.R.id.text1);
    assertThat(textView.getText().toString()).isEqualTo("land");
  }

  @Test
  public void testWebView() throws Exception {
    ViewGroup view = inflate(R.layout.webview_holder);
    WebView webView = view.findViewById(R.id.web_view);

    webView.loadUrl("www.example.com");

    assertThat(shadowOf(webView).getLastLoadedUrl()).isEqualTo("www.example.com");
  }

  @Test
  public void testAddsChildren() throws Exception {
    ViewGroup view = inflate(R.layout.media);
    assertTrue(view.getChildCount() > 0);

    assertSame(context, view.getChildAt(0).getContext());
  }

  @Test
  public void testFindsChildrenById() throws Exception {
    ViewGroup mediaView = inflate(R.layout.media);
    assertThat(mediaView.<TextView>findViewById(R.id.title)).isInstanceOf(TextView.class);

    ViewGroup mainView = inflate(R.layout.main);
    assertThat(mainView.<View>findViewById(R.id.title)).isInstanceOf(View.class);
  }

  @Test
  public void testInflatingConflictingSystemAndLocalViewsWorks() throws Exception {
    ViewGroup view = inflate(R.layout.activity_list_item);
    assertThat(view.<ImageView>findViewById(R.id.icon)).isInstanceOf(ImageView.class);

    view = inflate(android.R.layout.activity_list_item);
    assertThat(view.<ImageView>findViewById(android.R.id.icon)).isInstanceOf(ImageView.class);
  }

  @Test
  public void testInclude() throws Exception {
    ViewGroup mediaView = inflate(R.layout.media);
    assertThat(mediaView.<TextView>findViewById(R.id.include_id)).isInstanceOf(TextView.class);
  }

  @Test
  public void testIncludeShouldRetainAttributes() throws Exception {
    ViewGroup mediaView = inflate(R.layout.media);
    assertThat(mediaView.findViewById(R.id.include_id).getVisibility()).isEqualTo(View.GONE);
  }

  @Test
  public void shouldOverwriteIdOnIncludedNonMerge() throws Exception {
    ViewGroup mediaView = inflate(R.layout.media);
    assertNull(mediaView.findViewById(R.id.snippet_text));
  }

  @Test
  public void shouldRetainIdOnIncludedMergeWhenIncludeSpecifiesNoId() throws Exception {
    ViewGroup mediaView = inflate(R.layout.override_include);
    assertThat(mediaView.<TextView>findViewById(R.id.inner_text)).isInstanceOf(TextView.class);
  }

  @Test
  public void shouldRetainIdOnIncludedNonMergeWhenIncludeSpecifiesNoId() throws Exception {
    ViewGroup mediaView = inflate(R.layout.override_include);
    assertThat(mediaView.<TextView>findViewById(R.id.snippet_text)).isInstanceOf(TextView.class);
  }

  @Test
  public void testIncludedIdShouldNotBeFoundWhenIncludedIsMerge() throws Exception {
    ViewGroup overrideIncludeView = inflate(R.layout.outer);
    assertThat(overrideIncludeView.<LinearLayout>findViewById(R.id.outer_merge))
        .isInstanceOf(LinearLayout.class);
    assertThat(overrideIncludeView.<TextView>findViewById(R.id.inner_text))
        .isInstanceOf(TextView.class);
    assertNull(overrideIncludeView.findViewById(R.id.include_id));
    assertEquals(1, overrideIncludeView.getChildCount());
  }

  @Test
  public void testIncludeShouldOverrideAttributesOfIncludedRootNode() throws Exception {
    ViewGroup overrideIncludeView = inflate(R.layout.override_include);
    assertThat(overrideIncludeView.findViewById(R.id.snippet_text).getVisibility())
        .isEqualTo(View.INVISIBLE);
  }

  @Test
  public void shouldNotCountRequestFocusElementAsChild() throws Exception {
    ViewGroup viewGroup = inflate(R.layout.request_focus);
    ViewGroup frameLayout = (ViewGroup) viewGroup.getChildAt(1);
    assertEquals(0, frameLayout.getChildCount());
  }

  @Test
  public void focusRequest_shouldNotExplodeOnViewRootImpl() throws Exception {
    LinearLayout parent = new LinearLayout(context);
    shadowOf(parent).setMyParent(ReflectionHelpers.createNullProxy(ViewParent.class));
    LayoutInflater.from(context).inflate(R.layout.request_focus, parent);
  }

  @Test
  public void shouldGiveFocusToElementContainingRequestFocusElement() throws Exception {
    ViewGroup viewGroup = inflate(R.layout.request_focus);
    EditText editText = viewGroup.findViewById(R.id.edit_text);
    assertFalse(editText.isFocused());
  }

  @Test
  public void testMerge() throws Exception {
    ViewGroup mediaView = inflate(R.layout.outer);
    assertThat(mediaView.<TextView>findViewById(R.id.inner_text)).isInstanceOf(TextView.class);
  }

  @Test
  public void mergeIncludesShouldNotCreateAncestryLoops() throws Exception {
    ViewGroup mediaView = inflate(R.layout.outer);
    mediaView.hasFocus();
  }

  @Test
  public void testViewGroupsLooksAtItsOwnId() throws Exception {
    TextView mediaView = inflate(layout.snippet);
    assertSame(mediaView, mediaView.findViewById(R.id.snippet_text));
  }

  @Test
  public void shouldConstructCustomViewsWithAttributesConstructor() throws Exception {
    CustomView view = inflate(layout.custom_layout);
    assertThat(view.attributeResourceValue).isEqualTo(R.string.hello);
  }

  @Test
  public void shouldConstructCustomViewsWithCustomState() throws Exception {
    CustomStateView view = inflate(layout.custom_layout6);
    assertThat(view.getDrawableState()).asList().doesNotContain(R.attr.stateFoo);

    view.isFoo = true;
    view.refreshDrawableState();

    assertThat(view.getDrawableState()).asList().contains(R.attr.stateFoo);
  }

  @Test
  public void shouldConstructCustomViewsWithAttributesInResAutoNamespace() throws Exception {
    CustomView view = inflate(layout.custom_layout5);
    assertThat(view.attributeResourceValue).isEqualTo(R.string.hello);
  }

  @Test
  public void shouldConstructCustomViewsWithAttributesWithURLEncodedNamespaces() throws Exception {
    CustomView view = inflate(layout.custom_layout4).findViewById(R.id.custom_view);
    assertThat(view.namespacedResourceValue).isEqualTo(R.layout.text_views);
  }

  @Test
  public void testViewVisibilityIsSet() throws Exception {
    View mediaView = inflate(layout.media);
    assertThat(mediaView.findViewById(R.id.title).getVisibility()).isEqualTo(View.VISIBLE);
    assertThat(mediaView.findViewById(R.id.subtitle).getVisibility()).isEqualTo(View.GONE);
  }

  @Test
  public void testTextViewTextIsSet() throws Exception {
    View mediaView = inflate(layout.main);
    assertThat(((TextView) mediaView.findViewById(R.id.title)).getText().toString())
        .isEqualTo("Main Layout");
    assertThat(((TextView) mediaView.findViewById(R.id.subtitle)).getText().toString())
        .isEqualTo("Hello");
  }

  @Test
  public void testTextViewCompoundDrawablesAreSet() throws Exception {
    View mediaView = inflate(layout.main);
    TextView view = mediaView.findViewById(R.id.title);

    Drawable[] drawables = view.getCompoundDrawables();
    assertThat(shadowOf(drawables[0]).getCreatedFromResId()).isEqualTo(R.drawable.fourth_image);
    assertThat(shadowOf(drawables[1]).getCreatedFromResId()).isEqualTo(R.drawable.an_image);
    assertThat(shadowOf(drawables[2]).getCreatedFromResId()).isEqualTo(R.drawable.an_other_image);
    assertThat(shadowOf(drawables[3]).getCreatedFromResId()).isEqualTo(R.drawable.third_image);
  }

  @Test
  public void testCheckBoxCheckedIsSet() throws Exception {
    View mediaView = inflate(layout.main);
    assertThat(((CheckBox) mediaView.findViewById(R.id.true_checkbox)).isChecked()).isTrue();
    assertThat(((CheckBox) mediaView.findViewById(R.id.false_checkbox)).isChecked()).isFalse();
    assertThat(((CheckBox) mediaView.findViewById(R.id.default_checkbox)).isChecked()).isFalse();
  }

  @Test
  public void testImageViewSrcIsSet() throws Exception {
    View mediaView = inflate(layout.main);
    ImageView imageView = mediaView.findViewById(R.id.image);
    BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
    assertThat(shadowOf(drawable.getBitmap()).getCreatedFromResId()).isEqualTo(R.drawable.an_image);
  }

  @Test
  public void testImageViewSrcIsSetFromMipmap() throws Exception {
    View mediaView = inflate(layout.main);
    ImageView imageView = mediaView.findViewById(R.id.mipmapImage);
    BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
    assertThat(shadowOf(drawable.getBitmap()).getCreatedFromResId())
        .isEqualTo(R.mipmap.robolectric);
  }

  @Test
  public void shouldInflateMergeLayoutIntoParent() throws Exception {
    LinearLayout linearLayout = new LinearLayout(context);
    LayoutInflater.from(context).inflate(R.layout.inner_merge, linearLayout);
    assertThat(linearLayout.getChildAt(0)).isInstanceOf(TextView.class);
  }

  @Test
  public void testMultiOrientation() throws Exception {
    Activity activity = buildActivity(Activity.class).create().start().resume().get();

    // Default screen orientation should be portrait.
    ViewGroup view =
        (ViewGroup) LayoutInflater.from(activity).inflate(layout.multi_orientation, null);
    assertThat(view).isInstanceOf(LinearLayout.class);
    assertThat(view.getId()).isEqualTo(R.id.portrait);
    assertSame(activity, view.getContext());

    // Confirm explicit "orientation = portrait" works.
    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    int layoutResId = R.layout.multi_orientation;
    view = (ViewGroup) LayoutInflater.from(activity).inflate(layoutResId, null);
    assertThat(view).isInstanceOf(LinearLayout.class);
    assertThat(view.getId()).isEqualTo(R.id.portrait);
    assertSame(activity, view.getContext());
  }

  @Test
  @Config(qualifiers = "land")
  public void testMultiOrientation_explicitLandscape() throws Exception {
    Activity activity = buildActivity(Activity.class).create().start().resume().get();

    // Confirm explicit "orientation = landscape" works.
    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    ViewGroup view =
        (ViewGroup) LayoutInflater.from(activity).inflate(layout.multi_orientation, null);
    assertThat(view.getId()).isEqualTo(R.id.landscape);
    assertThat(view).isInstanceOf(LinearLayout.class);
  }

  @Test
  @Config(qualifiers = "w0dp")
  public void testSetContentViewByItemResource() throws Exception {
    Activity activity = buildActivity(Activity.class).create().get();
    activity.setContentView(R.layout.main_layout);

    TextView tv1 = activity.findViewById(R.id.hello);
    TextView tv2 = activity.findViewById(R.id.world);
    assertNotNull(tv1);
    assertNull(tv2);
  }

  @Test
  @Config(qualifiers = "w820dp")
  public void testSetContentViewByItemResourceWithW820dp() throws Exception {
    Activity activity = buildActivity(Activity.class).create().get();
    activity.setContentView(R.layout.main_layout);

    TextView tv1 = activity.findViewById(R.id.hello);
    TextView tv2 = activity.findViewById(R.id.world);
    assertNotNull(tv1);
    assertNotNull(tv2);
  }

  @Test
  public void testViewEnabled() throws Exception {
    View mediaView = inflate(layout.main);
    assertThat(mediaView.findViewById(R.id.time).isEnabled()).isFalse();
  }

  @Test
  public void testContentDescriptionIsSet() throws Exception {
    View mediaView = inflate(layout.main);
    assertThat(mediaView.findViewById(R.id.time).getContentDescription().toString())
        .isEqualTo("Howdy");
  }

  @Test
  public void testAlphaIsSet() throws Exception {
    View mediaView = inflate(layout.main);
    assertThat(mediaView.findViewById(R.id.time).getAlpha()).isEqualTo(.3f);
  }

  @Test
  public void testViewBackgroundIdIsSet() throws Exception {
    View mediaView = inflate(layout.main);
    ImageView imageView = mediaView.findViewById(R.id.image);

    assertThat(shadowOf(imageView.getBackground()).getCreatedFromResId())
        .isEqualTo(R.drawable.image_background);
  }

  @Test
  public void testOnClickAttribute() throws Exception {
    ClickActivity activity = buildActivity(ClickActivity.class).create().get();

    assertThat(activity.clicked).isFalse();

    Button button = activity.findViewById(R.id.button);
    button.performClick();

    assertThat(activity.clicked).isTrue();
  }

  @Test
  public void testInvalidOnClickAttribute() throws Exception {
    Activity activity = buildActivity(Activity.class).create().get();
    activity.setContentView(R.layout.with_invalid_onclick);

    Button button = activity.findViewById(R.id.invalid_onclick_button);

    IllegalStateException exception = null;
    try {
      button.performClick();
    } catch (IllegalStateException e) {
      exception = e;
    }
    assertNotNull(exception);
    assertThat(exception.getMessage())
        .named("The error message should contain the id name of the faulty button")
        .contains("invalid_onclick_button");
  }

  @Test
  public void shouldInvokeOnFinishInflate() throws Exception {
    int layoutResId = R.layout.custom_layout2;
    CustomView2 outerCustomView = inflate(layoutResId);
    CustomView2 innerCustomView = (CustomView2) outerCustomView.getChildAt(0);
    assertThat(outerCustomView.childCountAfterInflate).isEqualTo(1);
    assertThat(innerCustomView.childCountAfterInflate).isEqualTo(3);
  }

  @SuppressWarnings("UnusedDeclaration")
  public static class CustomView3 extends TextView {
    public CustomView3(Context context) {
      super(context);
    }

    public CustomView3(Context context, AttributeSet attrs) {
      super(context, attrs);
    }

    public CustomView3(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
    }
  }

  @Test
  public void shouldInflateViewsWithClassAttr() throws Exception {
    CustomView3 outerCustomView = inflate(layout.custom_layout3);
    assertThat(outerCustomView.getText().toString()).isEqualTo("Hello bonjour");
  }

  @Test
  public void testIncludesLinearLayoutsOnlyOnce() throws Exception {
    ViewGroup parentView = inflate(R.layout.included_layout_parent);
    assertEquals(1, parentView.getChildCount());
  }

  @Test
  public void testConverterAcceptsEnumOrdinal() throws Exception {
    ViewGroup view = inflate(R.layout.ordinal_scrollbar);
    assertThat(view).isInstanceOf(RelativeLayout.class);
    ListView listView = view.findViewById(R.id.list_view_with_enum_scrollbar);
    assertThat(listView).isInstanceOf(ListView.class);
  }

  /////////////////////////

  @SuppressWarnings("TypeParameterUnusedInFormals")
  private <T extends View> T inflate(int layoutResId) {
    return (T) LayoutInflater.from(context).inflate(layoutResId, null);
  }

  public static class ClickActivity extends Activity {
    public boolean clicked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
    }

    public void onButtonClick(View v) {
      clicked = true;
    }
  }
}
