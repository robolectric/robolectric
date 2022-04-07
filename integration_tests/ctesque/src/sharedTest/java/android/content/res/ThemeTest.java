package android.content.res;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.content.res.Resources.Theme;
import android.graphics.Color;
import android.util.TypedValue;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SdkSuppress;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.testapp.R;

/**
 * Compatibility test for {@link Resources.Theme}
 */
@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public class ThemeTest {

  private Resources resources;
  private Context context;

  @Before
  public void setup() throws Exception {
    context = InstrumentationRegistry.getInstrumentation().getTargetContext();
    resources = context.getResources();
  }

  @Test
  public void withEmptyTheme_returnsEmptyAttributes() {
    assertThat(resources.newTheme().obtainStyledAttributes(new int[] {R.attr.string1}).hasValue(0))
        .isFalse();
  }

  @Test
  public void shouldLookUpStylesFromStyleResId() {
    Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_AnotherTheme, true);
    TypedArray a = theme.obtainStyledAttributes(R.style.MyCustomView, R.styleable.CustomView);

    boolean enabled = a.getBoolean(R.styleable.CustomView_aspectRatioEnabled, false);
    assertThat(enabled).isTrue();
  }

  @Test
  public void shouldApplyStylesFromResourceReference() {
    Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_AnotherTheme, true);
    TypedArray a =
        theme.obtainStyledAttributes(null, R.styleable.CustomView, R.attr.animalStyle, 0);

    int animalStyleId = a.getResourceId(R.styleable.CustomView_animalStyle, 0);
    assertThat(animalStyleId).isEqualTo(R.style.Gastropod);
    assertThat(a.getFloat(R.styleable.CustomView_aspectRatio, 0.2f)).isEqualTo(1.69f);
  }

  @Test
  public void shouldApplyStylesFromAttributeReference() {
    Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_ThirdTheme, true);
    TypedArray a =
        theme.obtainStyledAttributes(null, R.styleable.CustomView, R.attr.animalStyle, 0);

    int animalStyleId = a.getResourceId(R.styleable.CustomView_animalStyle, 0);
    assertThat(animalStyleId).isEqualTo(R.style.Gastropod);
    assertThat(a.getFloat(R.styleable.CustomView_aspectRatio, 0.2f)).isEqualTo(1.69f);
  }

  @Test
  public void shouldGetValuesFromAttributeReference() {
    Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_ThirdTheme, true);

    TypedValue value1 = new TypedValue();
    TypedValue value2 = new TypedValue();
    boolean resolved1 = theme.resolveAttribute(R.attr.someLayoutOne, value1, true);
    boolean resolved2 = theme.resolveAttribute(R.attr.someLayoutTwo, value2, true);

    assertThat(resolved1).isTrue();
    assertThat(resolved2).isTrue();
    assertThat(value1.resourceId).isEqualTo(R.layout.activity_main);
    assertThat(value2.resourceId).isEqualTo(R.layout.activity_main);
    assertThat(value1.coerceToString()).isEqualTo(value2.coerceToString());
  }

  @Test
  public void withResolveRefsFalse_shouldResolveValue() {
    Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_AnotherTheme, true);

    TypedValue value = new TypedValue();
    boolean resolved = theme.resolveAttribute(R.attr.logoWidth, value, false);

    assertThat(resolved).isTrue();
    assertThat(value.type).isEqualTo(TypedValue.TYPE_REFERENCE);
    assertThat(value.data).isEqualTo(R.dimen.test_dp_dimen);
  }

  @Test
  public void withResolveRefsFalse_shouldNotResolveResource() {
    Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_AnotherTheme, true);

    TypedValue value = new TypedValue();
    boolean resolved = theme.resolveAttribute(R.attr.logoHeight, value, false);

    assertThat(resolved).isTrue();
    assertThat(value.type).isEqualTo(TypedValue.TYPE_REFERENCE);
    assertThat(value.data).isEqualTo(R.dimen.test_dp_dimen);
  }

  @Test
  public void withResolveRefsTrue_shouldResolveResource() {
    Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_AnotherTheme, true);

    TypedValue value = new TypedValue();
    boolean resolved = theme.resolveAttribute(R.attr.logoHeight, value, true);

    assertThat(resolved).isTrue();
    assertThat(value.type).isEqualTo(TypedValue.TYPE_DIMENSION);
    assertThat(value.resourceId).isEqualTo(R.dimen.test_dp_dimen);
    assertThat(value.coerceToString()).isEqualTo("8.0dip");
  }

  @Test
  public void failToResolveCircularReference() {
    Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_AnotherTheme, true);

    TypedValue value = new TypedValue();
    boolean resolved = theme.resolveAttribute(R.attr.isSugary, value, false);

    assertThat(resolved).isFalse();
  }

  @Test
  public void canResolveAttrReferenceToDifferentPackage() {
    Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_AnotherTheme, true);

    TypedValue value = new TypedValue();
    boolean resolved = theme.resolveAttribute(R.attr.styleReference, value, false);

    assertThat(resolved).isTrue();
    assertThat(value.type).isEqualTo(TypedValue.TYPE_REFERENCE);
    assertThat(value.data).isEqualTo(R.style.Widget_AnotherTheme_Button);
  }

  @SdkSuppress(minSdkVersion = O)
  @Test
  public void forStylesWithImplicitParents_shouldInheritValuesNotDefinedInChild() {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_Robolectric_ImplicitChild, true);
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.string1}).getString(0))
        .isEqualTo("string 1 from Theme.Robolectric");
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.string3}).getString(0))
        .isEqualTo("string 3 from Theme.Robolectric.ImplicitChild");
  }

  @Test
  public void whenAThemeHasExplicitlyEmptyParentAttr_shouldHaveNoParent() {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_Robolectric_EmptyParent, true);
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.string1}).hasValue(0)).isFalse();
  }

  @SdkSuppress(minSdkVersion = O)
  @Test
  public void shouldApplyParentStylesFromAttrs() {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_AnotherTheme, true);
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.string1}).getString(0))
        .isEqualTo("string 1 from Theme.AnotherTheme");
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.string3}).getString(0))
        .isEqualTo("string 3 from Theme.Robolectric");
  }

  @SdkSuppress(minSdkVersion = O)
  @Test
  public void setTo_shouldCopyAllAttributesToEmptyTheme() {
    Resources.Theme theme1 = resources.newTheme();
    theme1.applyStyle(R.style.Theme_Robolectric, false);
    assertThat(theme1.obtainStyledAttributes(new int[] {R.attr.string1}).getString(0))
        .isEqualTo("string 1 from Theme.Robolectric");

    Resources.Theme theme2 = resources.newTheme();
    theme2.setTo(theme1);

    assertThat(theme2.obtainStyledAttributes(new int[] {R.attr.string1}).getString(0))
        .isEqualTo("string 1 from Theme.Robolectric");
  }

  @SdkSuppress(minSdkVersion = O)
  @Test
  public void setTo_whenDestThemeIsModified_sourceThemeShouldNotMutate() {
    Resources.Theme sourceTheme = resources.newTheme();
    sourceTheme.applyStyle(R.style.Theme_Robolectric, false);
    assertThat(sourceTheme.obtainStyledAttributes(new int[] {R.attr.string1}).getString(0))
        .isEqualTo("string 1 from Theme.Robolectric");

    Resources.Theme destTheme = resources.newTheme();
    destTheme.setTo(sourceTheme);
    destTheme.applyStyle(R.style.Theme_AnotherTheme, true);

    assertThat(sourceTheme.obtainStyledAttributes(new int[] {R.attr.string1}).getString(0))
        .isEqualTo("string 1 from Theme.Robolectric");
  }

  @SdkSuppress(minSdkVersion = O)
  @Test
  public void setTo_whenSourceThemeIsModified_destThemeShouldNotMutate() {
    Resources.Theme sourceTheme = resources.newTheme();
    sourceTheme.applyStyle(R.style.Theme_Robolectric, false);
    assertThat(sourceTheme.obtainStyledAttributes(new int[] {R.attr.string1}).getString(0))
        .isEqualTo("string 1 from Theme.Robolectric");

    Resources.Theme destTheme = resources.newTheme();
    destTheme.setTo(sourceTheme);
    sourceTheme.applyStyle(R.style.Theme_AnotherTheme, true);

    assertThat(destTheme.obtainStyledAttributes(new int[] {R.attr.string1}).getString(0))
        .isEqualTo("string 1 from Theme.Robolectric");
  }

  @Test
  @SdkSuppress(minSdkVersion = O)
  public void applyStyle_withForceFalse_shouldApplyButNotOverwriteExistingAttributeValues() {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_Robolectric, false);
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.string1}).getString(0))
        .isEqualTo("string 1 from Theme.Robolectric");

    theme.applyStyle(R.style.Theme_AnotherTheme, false);
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.string1}).getString(0))
        .isEqualTo("string 1 from Theme.Robolectric");
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.string2}).getString(0))
        .isEqualTo("string 2 from Theme.AnotherTheme");
  }

  @Test
  @SdkSuppress(minSdkVersion = O)
  public void applyStyle_withForceTrue_shouldApplyAndOverwriteExistingAttributeValues() {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_Robolectric, false);
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.string1}).getString(0))
        .isEqualTo("string 1 from Theme.Robolectric");

    theme.applyStyle(R.style.Theme_AnotherTheme, true);
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.string1}).getString(0))
        .isEqualTo("string 1 from Theme.AnotherTheme");

    // force apply the original theme; values should be overwritten
    theme.applyStyle(R.style.Theme_Robolectric, true);
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.string1}).getString(0))
        .isEqualTo("string 1 from Theme.Robolectric");
  }

  @Test
  public void shouldFindInheritedAndroidAttributeInTheme() {
    context.setTheme(R.style.Theme_AnotherTheme);
    Resources.Theme theme1 = context.getTheme();

    TypedArray typedArray =
        theme1.obtainStyledAttributes(new int[] {R.attr.typeface, android.R.attr.buttonStyle});
    assertThat(typedArray.hasValue(0)).isTrue(); // animalStyle
    assertThat(typedArray.hasValue(1)).isTrue(); // layout_height
  }

  @Test
  public void themesShouldBeApplyableAcrossResources() {
    Resources.Theme themeFromSystem = Resources.getSystem().newTheme();
    themeFromSystem.applyStyle(android.R.style.Theme_Light, true);

    Resources.Theme themeFromApp = resources.newTheme();
    themeFromApp.applyStyle(android.R.style.Theme, true);

    // themeFromSystem is Theme_Light, which has a white background...
    assertThat(
            themeFromSystem
                .obtainStyledAttributes(new int[] {android.R.attr.colorBackground})
                .getColor(0, 123))
        .isEqualTo(Color.WHITE);

    // themeFromApp is Theme, which has a black background...
    assertThat(
            themeFromApp
                .obtainStyledAttributes(new int[] {android.R.attr.colorBackground})
                .getColor(0, 123))
        .isEqualTo(Color.BLACK);

    themeFromApp.setTo(themeFromSystem);

    // themeFromApp now has style values from themeFromSystem, so now it has a black background...
    assertThat(
            themeFromApp
                .obtainStyledAttributes(new int[] {android.R.attr.colorBackground})
                .getColor(0, 123))
        .isEqualTo(Color.WHITE);
  }

  @Test
  public void styleResolutionShouldIgnoreThemes() {
    Resources.Theme themeFromSystem = resources.newTheme();
    themeFromSystem.applyStyle(android.R.style.Theme_DeviceDefault, true);
    themeFromSystem.applyStyle(R.style.ThemeWithSelfReferencingTextAttr, true);
    assertThat(
            themeFromSystem
                .obtainStyledAttributes(new int[] {android.R.attr.textAppearance})
                .getResourceId(0, 0))
        .isEqualTo(0);
  }
}
