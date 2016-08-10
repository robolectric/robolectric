package org.robolectric.shadows;

import android.app.Activity;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.res.ResName;
import org.robolectric.res.Style;
import org.robolectric.util.ActivityController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowThemeTest {
  @Test public void whenExplicitlySetOnActivity_afterSetContentView_activityGetsThemeFromActivityInManifest() throws Exception {
    TestActivity activity = buildActivity(TestActivityWithAnotherTheme.class).create().get();
    activity.setTheme(R.style.Theme_Robolectric);
    Button theButton = (Button) activity.findViewById(R.id.button);
    ColorDrawable background = (ColorDrawable) theButton.getBackground();
    assertThat(background.getColor()).isEqualTo(0xffff0000);
  }

  @Test public void whenExplicitlySetOnActivity_beforeSetContentView_activityUsesNewTheme() throws Exception {
    ActivityController<TestActivityWithAnotherTheme> activityController = buildActivity(TestActivityWithAnotherTheme.class);
    TestActivity activity = activityController.get();
    activity.setTheme(R.style.Theme_Robolectric);
    activityController.create();
    Button theButton = (Button) activity.findViewById(R.id.button);
    ColorDrawable background = (ColorDrawable) theButton.getBackground();
    assertThat(background.getColor()).isEqualTo(0xff00ff00);
  }

  @Test public void whenSetOnActivityInManifest_activityGetsThemeFromActivityInManifest() throws Exception {
    TestActivity activity = buildActivity(TestActivityWithAnotherTheme.class).create().get();
    Button theButton = (Button) activity.findViewById(R.id.button);
    ColorDrawable background = (ColorDrawable) theButton.getBackground();
    assertThat(background.getColor()).isEqualTo(0xffff0000);
  }

  @Test public void whenNotSetOnActivityInManifest_activityGetsThemeFromApplicationInManifest() throws Exception {
    TestActivity activity = buildActivity(TestActivity.class).create().get();
    Button theButton = (Button) activity.findViewById(R.id.button);
    ColorDrawable background = (ColorDrawable) theButton.getBackground();
    assertThat(background.getColor()).isEqualTo(0xff00ff00);
  }

  @Test public void shouldResolveReferencesThatStartWithAQuestionMark() throws Exception {
    TestActivity activity = buildActivity(TestActivityWithAnotherTheme.class).create().get();
    Button theButton = (Button) activity.findViewById(R.id.button);
    assertThat(theButton.getMinWidth()).isEqualTo(42); // via AnotherTheme.Button -> logoWidth and logoHeight
//        assertThat(theButton.getMinHeight()).isEqualTo(42); todo 2.0-cleanup
  }

  @Test public void shouldLookUpStylesFromStyleResId() throws Exception {
    TestActivity activity = buildActivity(TestActivityWithAnotherTheme.class).create().get();
    TypedArray a = activity.obtainStyledAttributes(null, R.styleable.CustomView, 0, R.style.MyCustomView);
    boolean enabled = a.getBoolean(R.styleable.CustomView_aspectRatioEnabled, false);
    assertThat(enabled).isTrue();
  }

  @Test public void shouldApplyStylesFromResourceReference() throws Exception {
    TestActivity activity = buildActivity(TestActivityWithAnotherTheme.class).create().get();
    TypedArray a = activity.obtainStyledAttributes(null, R.styleable.CustomView, 0, R.attr.animalStyle);
    int animalStyleId = a.getResourceId(R.styleable.CustomView_animalStyle, 0);
    assertThat(animalStyleId).isEqualTo(R.style.Gastropod);
    assertThat(a.getFloat(R.styleable.CustomView_aspectRatio, 0.2f)).isEqualTo(1.69f);
  }

  @Test public void shouldApplyStylesFromAttributeReference() throws Exception {
    TestActivity activity = buildActivity(TestActivityWithAThirdTheme.class).create().get();
    TypedArray a = activity.obtainStyledAttributes(null, R.styleable.CustomView, 0, R.attr.animalStyle);
    int animalStyleId = a.getResourceId(R.styleable.CustomView_animalStyle, 0);
    assertThat(animalStyleId).isEqualTo(R.style.Gastropod);
    assertThat(a.getFloat(R.styleable.CustomView_aspectRatio, 0.2f)).isEqualTo(1.69f);
  }

  @Test public void obtainTypedArrayFromDependencyLibrary() throws Exception {
    TestActivity activity = buildActivity(TestActivityWithAThirdTheme.class).create().get();

    // This is an Android framework attribute, see:
    // https://developer.android.com/reference/android/R.attr.html#selectableItemBackground
    // so can be accessed as android.R.attr.selectableItemBackground but it will also be merged by AAPT so
    // that it can be accessed as org.robolectric.R.attr.selectableItemBackground and will have the same ID
    // value. The application ResourceLoader contains a list of sub-package ResourceLoaders and this ID will
    // have a match in the android namespace as well as any libraries that have a dependency on android (i.e:
    // all of them) and Robolectric will find the first match which returns a ResName with a
    // package == org.robolectric - this will fail when looking up in the theme as the attribute is in the
    // android namespace.
    TypedArray a  = activity.getTheme().obtainStyledAttributes(new int[]{R.attr.selectableItemBackground});

    int resourceId = a.getResourceId(0, 0);

    // We should find the value as defined by the framework
    assertThat(resourceId).isNotEqualTo(0);
  }

  @Test public void shouldGetValuesFromAttributeReference() throws Exception {
    TestActivity activity = buildActivity(TestActivityWithAThirdTheme.class).create().get();

    TypedValue value1 = new TypedValue();
    TypedValue value2 = new TypedValue();
    boolean resolved1 = activity.getTheme().resolveAttribute(R.attr.someLayoutOne, value1, true);
    boolean resolved2 = activity.getTheme().resolveAttribute(R.attr.someLayoutTwo, value2, true);

    assertThat(resolved1).isTrue();
    assertThat(resolved2).isTrue();
    assertThat(value1.resourceId).isEqualTo(R.layout.activity_main);
    assertThat(value2.resourceId).isEqualTo(R.layout.activity_main);
    assertThat(value1.coerceToString()).isEqualTo(value2.coerceToString());
  }

  @Test public void shouldInheritThemeValuesFromImplicitParents() throws Exception {
    TestActivity activity = Robolectric.setupActivity(TestActivityWithAnotherTheme.class);
    Style style = shadowOf(activity.getAssets()).resolveStyle(
        null,
        R.style.Widget_AnotherTheme_Button);
    assertThat(style.getAttrValue(new ResName("android", "attr", "background")).value)
        .isEqualTo("#ffff0000");
  }

  @Test public void whenAThemeHasExplicitlyEmptyParentAttr_shouldHaveNoParent() throws Exception {
    TestActivity activity = Robolectric.setupActivity(TestActivityWithAnotherTheme.class);
    Style style = shadowOf(activity.getAssets()).resolveStyle(
        null,
        R.style.Theme_MyTheme);
    assertThat(style.getAttrValue(new ResName("android", "attr", "background"))).isNull();
  }


  @Test public void shouldApplyParentStylesFromAttrs() throws Exception {
    TestActivity activity = Robolectric.setupActivity(TestActivityWithAnotherTheme.class);
    ShadowAssetManager shadowAssetManager = shadowOf(activity.getAssets());
    Style theme = shadowAssetManager.resolveStyle(null,
        R.style.Theme_AnotherTheme);
    Style style = shadowAssetManager.resolveStyle(theme,
        R.style.IndirectButtonStyle);
    assertThat(style.getAttrValue(new ResName("android", "attr", "background")).value)
        .isEqualTo("#ffff0000");
  }

  public static class TestActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.styles_button_layout);
    }
  }

  public static class TestActivityWithAnotherTheme extends TestActivity {
  }

  public static class TestActivityWithAThirdTheme extends TestActivity {
  }

  @Test public void shouldApplyFromStyleAttribute() throws Exception {
    TestWithStyleAttrActivity activity = buildActivity(TestWithStyleAttrActivity.class).create().get();
    View button = activity.findViewById(R.id.button);
    assertThat(button.getLayoutParams().width).isEqualTo(42); // comes via style attr
  }

  public static class TestWithStyleAttrActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.styles_button_with_style_layout);
    }
  }
}
