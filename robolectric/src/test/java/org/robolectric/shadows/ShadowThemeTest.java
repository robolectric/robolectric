package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

@RunWith(RobolectricTestRunner.class)
public class ShadowThemeTest {

  private Resources resources;

  @Before
  public void setUp() throws Exception {
    resources = RuntimeEnvironment.application.getResources();
  }

  @Test public void withEmptyTheme_returnsEmptyAttributes() throws Exception {
    assertThat(resources.newTheme().obtainStyledAttributes(new int[] {R.attr.string1}).hasValue(0)).isFalse();
  }

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
    assertThat(theButton.getMinWidth()).isEqualTo(8);
    assertThat(theButton.getMinHeight()).isEqualTo(8);
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

  @Test public void obtainStyledAttributes_findsAttributeValueDefinedInDependencyLibrary() throws Exception {
    TestActivity activity = buildActivity(TestActivityWithAThirdTheme.class).create().get();

    TypedArray a  = activity.getTheme().obtainStyledAttributes(new int[]{org.robolectric.R.attr.attrFromLib1});
    assertThat(a.getString(0)).isEqualTo("value from theme");
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

  @Test public void withResolveRefsFalse_shouldResolveValue() throws Exception {
    TestActivity activity = buildActivity(TestActivityWithAnotherTheme.class).create().get();

    TypedValue value = new TypedValue();
    boolean resolved = activity.getTheme().resolveAttribute(R.attr.logoWidth, value, false);

    assertThat(resolved).isTrue();
    assertThat(value.type).isEqualTo(TypedValue.TYPE_REFERENCE);
    assertThat(value.data).isEqualTo(R.dimen.test_dp_dimen);
  }

  @Test public void withResolveRefsFalse_shouldNotResolveResource() throws Exception {
    TestActivity activity = buildActivity(TestActivityWithAnotherTheme.class).create().get();

    TypedValue value = new TypedValue();
    boolean resolved = activity.getTheme().resolveAttribute(R.attr.logoHeight, value, false);

    assertThat(resolved).isTrue();
    assertThat(value.type).isEqualTo(TypedValue.TYPE_REFERENCE);
    assertThat(value.data).isEqualTo(R.dimen.test_dp_dimen);
  }

  @Test public void withResolveRefsTrue_shouldResolveResource() throws Exception {
    TestActivity activity = buildActivity(TestActivityWithAnotherTheme.class).create().get();

    TypedValue value = new TypedValue();
    boolean resolved = activity.getTheme().resolveAttribute(R.attr.logoHeight, value, true);

    assertThat(resolved).isTrue();
    assertThat(value.type).isEqualTo(TypedValue.TYPE_DIMENSION);
    assertThat(value.resourceId).isEqualTo(R.dimen.test_dp_dimen);
    assertThat(value.coerceToString()).isEqualTo("8.0dip");
  }

  @Test public void failToResolveCircularReference() throws Exception {
    TestActivity activity = buildActivity(TestActivityWithAnotherTheme.class).create().get();

    TypedValue value = new TypedValue();
    boolean resolved = activity.getTheme().resolveAttribute(R.attr.isSugary, value, false);

    assertThat(resolved).isFalse();
  }

  @Test public void canResolveAttrReferenceToDifferentPackage() throws Exception {
    TestActivity activity = buildActivity(TestActivityWithAnotherTheme.class).create().get();

    TypedValue value = new TypedValue();
    boolean resolved = activity.getTheme().resolveAttribute(R.attr.styleReference, value, false);

    assertThat(resolved).isTrue();
    assertThat(value.type).isEqualTo(TypedValue.TYPE_REFERENCE);
    assertThat(value.data).isEqualTo(R.style.Widget_AnotherTheme_Button);
  }

  @Test public void forStylesWithImplicitParents_shouldInheritValuesNotDefinedInChild() throws Exception {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_Robolectric_ImplicitChild, true);
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.string1}).getString(0))
        .isEqualTo("string 1 from Theme.Robolectric");
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.string3}).getString(0))
        .isEqualTo("string 3 from Theme.Robolectric.ImplicitChild");
  }

  @Test public void whenAThemeHasExplicitlyEmptyParentAttr_shouldHaveNoParent() throws Exception {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_Robolectric_EmptyParent, true);
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.string1}).hasValue(0)).isFalse();
  }

  @Test public void shouldApplyParentStylesFromAttrs() throws Exception {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_AnotherTheme, true);
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.string1}).getString(0))
        .isEqualTo("string 1 from Theme.AnotherTheme");
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.string3}).getString(0))
        .isEqualTo("string 3 from Theme.Robolectric");
  }

  @Test
  public void setTo_shouldCopyAllAttributesToEmptyTheme() throws Exception {
    Resources.Theme theme1 = resources.newTheme();
    theme1.applyStyle(R.style.Theme_Robolectric, false);
    assertThat(theme1.obtainStyledAttributes(new int[]{R.attr.string1}).getString(0))
        .isEqualTo("string 1 from Theme.Robolectric");

    Resources.Theme theme2 = resources.newTheme();
    theme2.setTo(theme1);

    assertThat(theme2.obtainStyledAttributes(new int[]{R.attr.string1}).getString(0))
        .isEqualTo("string 1 from Theme.Robolectric");
  }

  @Test
  public void setTo_whenDestThemeIsModified_sourceThemeShouldNotMutate() throws Exception {
    Resources.Theme sourceTheme = resources.newTheme();
    sourceTheme.applyStyle(R.style.Theme_Robolectric, false);
    assertThat(sourceTheme.obtainStyledAttributes(new int[]{R.attr.string1}).getString(0))
        .isEqualTo("string 1 from Theme.Robolectric");

    Resources.Theme destTheme = resources.newTheme();
    destTheme.setTo(sourceTheme);
    destTheme.applyStyle(R.style.Theme_AnotherTheme, true);

    assertThat(sourceTheme.obtainStyledAttributes(new int[]{R.attr.string1}).getString(0))
        .isEqualTo("string 1 from Theme.Robolectric");
  }

  @Test
  public void setTo_whenSourceThemeIsModified_destThemeShouldNotMutate() throws Exception {
    Resources.Theme sourceTheme = resources.newTheme();
    sourceTheme.applyStyle(R.style.Theme_Robolectric, false);
    assertThat(sourceTheme.obtainStyledAttributes(new int[]{R.attr.string1}).getString(0))
        .isEqualTo("string 1 from Theme.Robolectric");

    Resources.Theme destTheme = resources.newTheme();
    destTheme.setTo(sourceTheme);
    sourceTheme.applyStyle(R.style.Theme_AnotherTheme, true);

    assertThat(destTheme.obtainStyledAttributes(new int[]{R.attr.string1}).getString(0))
        .isEqualTo("string 1 from Theme.Robolectric");
  }

  @Test
  public void applyStyle_withForceFalse_shouldApplyButNotOverwriteExistingAttributeValues() throws Exception {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_Robolectric, false);
    assertThat(theme.obtainStyledAttributes(new int[]{R.attr.string1}).getString(0))
        .isEqualTo("string 1 from Theme.Robolectric");

    theme.applyStyle(R.style.Theme_AnotherTheme, false);
    assertThat(theme.obtainStyledAttributes(new int[]{R.attr.string1}).getString(0))
        .isEqualTo("string 1 from Theme.Robolectric");
    assertThat(theme.obtainStyledAttributes(new int[]{R.attr.string2}).getString(0))
        .isEqualTo("string 2 from Theme.AnotherTheme");
  }

  @Test
  public void applyStyle_withForceTrue_shouldApplyAndOverwriteExistingAttributeValues() throws Exception {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_Robolectric, false);
    assertThat(theme.obtainStyledAttributes(new int[]{R.attr.string1}).getString(0))
        .isEqualTo("string 1 from Theme.Robolectric");

    theme.applyStyle(R.style.Theme_AnotherTheme, true);
    assertThat(theme.obtainStyledAttributes(new int[]{R.attr.string1}).getString(0))
        .isEqualTo("string 1 from Theme.AnotherTheme");

    // force apply the original theme; values should be overwritten
    theme.applyStyle(R.style.Theme_Robolectric, true);
    assertThat(theme.obtainStyledAttributes(new int[]{R.attr.string1}).getString(0))
        .isEqualTo("string 1 from Theme.Robolectric");
  }

  @Test
  public void whenStyleSpecifiesAttr_obtainStyledAttribute_findsCorrectValue() throws Exception {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_Robolectric, false);
    theme.applyStyle(R.style.Theme_ThemeContainingStyleReferences, true);

    assertThat(theme.obtainStyledAttributes(
        Robolectric.buildAttributeSet().setStyleAttribute("?attr/styleReference").build(), new int[]{R.attr.string2}, 0, 0).getString(0))
        .isEqualTo("string 2 from StyleReferredToByParentAttrReference");

    assertThat(theme.obtainStyledAttributes(
        Robolectric.buildAttributeSet().setStyleAttribute("?styleReference").build(), new int[]{R.attr.string2}, 0, 0).getString(0))
        .isEqualTo("string 2 from StyleReferredToByParentAttrReference");
  }

  @Test
  public void whenStyleSpecifiesAttrWithoutExplicitType_obtainStyledAttribute_findsCorrectValue() throws Exception {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_Robolectric, false);
    theme.applyStyle(R.style.Theme_ThemeContainingStyleReferences, true);

    assertThat(theme.obtainStyledAttributes(
        Robolectric.buildAttributeSet().setStyleAttribute("?attr/styleReferenceWithoutExplicitType").build(), new int[]{R.attr.string2}, 0, 0).getString(0))
        .isEqualTo("string 2 from StyleReferredToByParentAttrReference");

    assertThat(theme.obtainStyledAttributes(
        Robolectric.buildAttributeSet().setStyleAttribute("?styleReferenceWithoutExplicitType").build(), new int[]{R.attr.string2}, 0, 0).getString(0))
        .isEqualTo("string 2 from StyleReferredToByParentAttrReference");
  }

  @Test
  public void whenAttrSetAttrSpecifiesAttr_obtainStyledAttribute_returnsItsValue() throws Exception {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_Robolectric, false);
    theme.applyStyle(R.style.Theme_ThemeContainingStyleReferences, true);

    assertThat(theme.obtainStyledAttributes(
        Robolectric.buildAttributeSet().addAttribute(R.attr.string2, "?attr/string1").build(), new int[]{R.attr.string2}, 0, 0).getString(0))
        .isEqualTo("string 1 from Theme.Robolectric");
  }

  @Test
  public void whenAttrSetAttrSpecifiesUnknownAttr_obtainStyledAttribute_usesNull() throws Exception {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_Robolectric, false);
    theme.applyStyle(R.style.Theme_ThemeContainingStyleReferences, true);

    assertThat(theme.obtainStyledAttributes(
        Robolectric.buildAttributeSet().addAttribute(R.attr.string2, "?attr/noSuchAttr").build(),
        new int[]{R.attr.string2}, 0, 0).getString(0))
        .isNull();

    // todo: assert that a strict warning was displayed
  }

  @Test
  public void forStrict_whenAttrSetAttrSpecifiesUnknownAttr_obtainStyledAttribute_throwsException() throws Exception {
    shadowOf(resources.getAssets()).strictErrors = true;

    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_Robolectric, false);
    theme.applyStyle(R.style.Theme_ThemeContainingStyleReferences, true);

    try {
      theme.obtainStyledAttributes(
          Robolectric.buildAttributeSet().addAttribute(R.attr.string2, "?attr/noSuchAttr").build(),
          new int[]{R.attr.string2}, 0, 0);
      fail();
    } catch (Exception e) {
      assertThat(e.getMessage()).contains("no such attr ?org.robolectric:attr/noSuchAttr");
      assertThat(e.getMessage()).contains("Theme_ThemeContainingStyleReferences");
      assertThat(e.getMessage()).contains("Theme_Robolectric");
      assertThat(e.getMessage()).contains("while resolving value for org.robolectric:attr/string2");
    }
  }

  @Test
  public void shouldFindInheritedAndroidAttributeInTheme() throws Exception {
    RuntimeEnvironment.application.setTheme(R.style.Theme_AnotherTheme);
    Resources.Theme theme1 = RuntimeEnvironment.application.getTheme();

//    Resources.Theme theme1 = resources.newTheme();
//    theme1.setTo(RuntimeEnvironment.application.getTheme());
//    theme1.applyStyle(R.style.Theme_AnotherTheme, false);

    TypedArray typedArray = theme1.obtainStyledAttributes(
        new int[]{R.attr.typeface, android.R.attr.buttonStyle});
    assertThat(typedArray.hasValue(0)).isTrue(); // animalStyle
    assertThat(typedArray.hasValue(1)).isTrue(); // layout_height
  }

  public static class TestActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.styles_button_layout);
    }
  }

  @Test
  public void themesShouldBeApplyableAcrossResources() throws Exception {
    Resources.Theme themeFromSystem = Resources.getSystem().newTheme();
    themeFromSystem.applyStyle(android.R.style.Theme_Light, true);

    Resources.Theme themeFromApp = RuntimeEnvironment.application.getResources().newTheme();
    themeFromApp.applyStyle(android.R.style.Theme, true);

    // themeFromSystem is Theme_Light, which has a white background...
    assertThat(shadowOf(themeFromSystem).obtainStyledAttributes(new int[]{android.R.attr.colorBackground}).getColor(0, 123))
        .isEqualTo(Color.WHITE);

    // themeFromApp is Theme, which has a black background...
    assertThat(shadowOf(themeFromApp).obtainStyledAttributes(new int[]{android.R.attr.colorBackground}).getColor(0, 123))
        .isEqualTo(Color.BLACK);

    themeFromApp.setTo(themeFromSystem);

    // themeFromApp now has style values from themeFromSystem, so now it has a black background...
    assertThat(shadowOf(themeFromApp).obtainStyledAttributes(new int[]{android.R.attr.colorBackground}).getColor(0, 123))
        .isEqualTo(Color.WHITE);
  }

  @Test
  public void styleResolutionShouldIgnoreThemes() throws Exception {
    Resources.Theme themeFromSystem = resources.newTheme();
    themeFromSystem.applyStyle(android.R.style.Theme_DeviceDefault, true);
    themeFromSystem.applyStyle(R.style.ThemeWithSelfReferencingTextAttr, true);
    assertThat(themeFromSystem.obtainStyledAttributes(new int[]{android.R.attr.textAppearance})
        .getResourceId(0, 0)).isEqualTo(0);
  }

  @Test
  public void dimenRef() throws Exception {
    AttributeSet attributeSet = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.layout_height, "@dimen/test_px_dimen")
        .build();
    TypedArray typedArray = resources.newTheme().obtainStyledAttributes(
        attributeSet, new int[]{android.R.attr.layout_height}, 0, 0);
    assertThat(typedArray.getDimensionPixelSize(0, -1)).isEqualTo(15);
  }

  @Test
  public void dimenRefRef() throws Exception {
    AttributeSet attributeSet = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.layout_height, "@dimen/ref_to_px_dimen")
        .build();
    TypedArray typedArray = resources.newTheme().obtainStyledAttributes(
        attributeSet, new int[]{android.R.attr.layout_height}, 0, 0);
    assertThat(typedArray.getDimensionPixelSize(0, -1)).isEqualTo(15);
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
