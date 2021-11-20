package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Robolectric.buildActivity;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.ColorDrawable;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.res.android.Registries;
import org.robolectric.shadows.testing.TestActivity;
import org.robolectric.util.ReflectionHelpers;
import org.xmlpull.v1.XmlPullParser;

@RunWith(AndroidJUnit4.class)
public class ShadowThemeTest {
  private Resources resources;

  @Before
  public void setUp() throws Exception {
    resources = ApplicationProvider.getApplicationContext().getResources();
  }

  @After
  public void tearDown() {
    ShadowLegacyAssetManager.strictErrors = false;
  }

  @Test
  public void
      whenExplicitlySetOnActivity_afterSetContentView_activityGetsThemeFromActivityInManifest() {
    TestActivity activity = buildActivity(TestActivityWithAnotherTheme.class).create().get();
    activity.setTheme(R.style.Theme_Robolectric);
    Button theButton = activity.findViewById(R.id.button);
    ColorDrawable background = (ColorDrawable) theButton.getBackground();
    assertThat(background.getColor()).isEqualTo(0xffff0000);
  }

  @Test
  public void whenExplicitlySetOnActivity_beforeSetContentView_activityUsesNewTheme() {
    ActivityController<TestActivityWithAnotherTheme> activityController = buildActivity(TestActivityWithAnotherTheme.class);
    TestActivity activity = activityController.get();
    activity.setTheme(R.style.Theme_Robolectric);
    activityController.create();
    Button theButton = activity.findViewById(R.id.button);
    ColorDrawable background = (ColorDrawable) theButton.getBackground();
    assertThat(background.getColor()).isEqualTo(0xff00ff00);
  }

  @Test
  public void whenSetOnActivityInManifest_activityGetsThemeFromActivityInManifest() {
    TestActivity activity = buildActivity(TestActivityWithAnotherTheme.class).create().get();
    Button theButton = activity.findViewById(R.id.button);
    ColorDrawable background = (ColorDrawable) theButton.getBackground();
    assertThat(background.getColor()).isEqualTo(0xffff0000);
  }

  @Test
  public void whenNotSetOnActivityInManifest_activityGetsThemeFromApplicationInManifest() {
    TestActivity activity = buildActivity(TestActivity.class).create().get();
    Button theButton = activity.findViewById(R.id.button);
    ColorDrawable background = (ColorDrawable) theButton.getBackground();
    assertThat(background.getColor()).isEqualTo(0xff00ff00);
  }

  @Test
  public void shouldResolveReferencesThatStartWithAQuestionMark() {
    TestActivity activity = buildActivity(TestActivityWithAnotherTheme.class).create().get();
    Button theButton = activity.findViewById(R.id.button);
    assertThat(theButton.getMinWidth()).isEqualTo(8);
    assertThat(theButton.getMinHeight()).isEqualTo(8);
  }

  @Test
  public void forStylesWithImplicitParents_shouldInheritValuesNotDefinedInChild() {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_Robolectric_EmptyParent, true);
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.string1}).hasValue(0)).isFalse();
  }

  @Test
  public void shouldApplyParentStylesFromAttrs() {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.SimpleParent, true);
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.parent_string}).getString(0))
        .isEqualTo("parent string");
  }

  @Test
  public void applyStyle_shouldOverrideParentAttrs() {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.SimpleChildWithOverride, true);
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.parent_string}).getString(0))
        .isEqualTo("parent string overridden by child");
  }

  @Test
  public void applyStyle_shouldOverrideImplicitParentAttrs() {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.SimpleParent_ImplicitChild, true);
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.parent_string}).getString(0))
        .isEqualTo("parent string overridden by child");
  }

  @Test
  public void applyStyle_shouldInheritParentAttrs() {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.SimpleChildWithAdditionalAttributes, true);
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.child_string}).getString(0))
        .isEqualTo("child string");
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.parent_string}).getString(0))
        .isEqualTo("parent string");
  }

  @Test
  public void setTo_shouldCopyAllAttributesToEmptyTheme() {
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
  public void setTo_whenDestThemeIsModified_sourceThemeShouldNotMutate() {
    Resources.Theme sourceTheme = resources.newTheme();
    sourceTheme.applyStyle(R.style.StyleA, false);

    Resources.Theme destTheme = resources.newTheme();
    destTheme.setTo(sourceTheme);
    destTheme.applyStyle(R.style.StyleB, true);

    assertThat(destTheme.obtainStyledAttributes(new int[]{R.attr.string1}).getString(0))
        .isEqualTo("string 1 from style B");
    assertThat(sourceTheme.obtainStyledAttributes(new int[]{R.attr.string1}).getString(0))
        .isEqualTo("string 1 from style A");
  }

  @Test
  public void setTo_whenSourceThemeIsModified_destThemeShouldNotMutate() {
    Resources.Theme sourceTheme = resources.newTheme();
    sourceTheme.applyStyle(R.style.StyleA, false);

    Resources.Theme destTheme = resources.newTheme();
    destTheme.setTo(sourceTheme);
    sourceTheme.applyStyle(R.style.StyleB, true);

    assertThat(destTheme.obtainStyledAttributes(new int[]{R.attr.string1}).getString(0))
        .isEqualTo("string 1 from style A");
    assertThat(sourceTheme.obtainStyledAttributes(new int[]{R.attr.string1}).getString(0))
        .isEqualTo("string 1 from style B");
  }

  @Test
  public void applyStyle_withForceFalse_shouldApplyButNotOverwriteExistingAttributeValues() {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.StyleA, false);
    assertThat(theme.obtainStyledAttributes(new int[]{R.attr.string1}).getString(0))
        .isEqualTo("string 1 from style A");

    theme.applyStyle(R.style.StyleB, false);
    assertThat(theme.obtainStyledAttributes(new int[]{R.attr.string1}).getString(0))
        .isEqualTo("string 1 from style A");
  }

  @Test
  public void applyStyle_withForceTrue_shouldApplyAndOverwriteExistingAttributeValues() {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.StyleA, false);
    assertThat(theme.obtainStyledAttributes(new int[]{R.attr.string1}).getString(0))
        .isEqualTo("string 1 from style A");

    theme.applyStyle(R.style.StyleB, true);
    assertThat(theme.obtainStyledAttributes(new int[]{R.attr.string1}).getString(0))
        .isEqualTo("string 1 from style B");
  }

  @Test
  public void whenStyleSpecifiesAttr_obtainStyledAttribute_findsCorrectValue() {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_Robolectric, false);
    theme.applyStyle(R.style.Theme_ThemeContainingStyleReferences, true);

    assertThat(theme.obtainStyledAttributes(
        Robolectric.buildAttributeSet().setStyleAttribute("?attr/styleReference").build(),
        new int[]{R.attr.string2}, 0, 0).getString(0))
        .isEqualTo("string 2 from YetAnotherStyle");

    assertThat(theme.obtainStyledAttributes(
        Robolectric.buildAttributeSet().setStyleAttribute("?styleReference").build(),
        new int[]{R.attr.string2}, 0, 0).getString(0))
        .isEqualTo("string 2 from YetAnotherStyle");
  }

  @Test
  public void xml_whenStyleSpecifiesAttr_obtainStyledAttribute_findsCorrectValue() throws Exception {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_Robolectric, false);
    theme.applyStyle(R.style.Theme_ThemeContainingStyleReferences, true);

    assertThat(theme.obtainStyledAttributes(getFirstElementAttrSet(R.xml.temp),
        new int[]{R.attr.string2}, 0, 0).getString(0))
        .isEqualTo("string 2 from YetAnotherStyle");

    assertThat(theme.obtainStyledAttributes(getFirstElementAttrSet(R.xml.temp_parent),
        new int[]{R.attr.string2}, 0, 0).getString(0))
        .isEqualTo("string 2 from YetAnotherStyle");
  }

  @Test
  public void whenAttrSetAttrSpecifiesAttr_obtainStyledAttribute_returnsItsValue() {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_Robolectric, false);
    theme.applyStyle(R.style.Theme_ThemeContainingStyleReferences, true);

    assertThat(theme.obtainStyledAttributes(
        Robolectric.buildAttributeSet().addAttribute(R.attr.string2, "?attr/string1").build(),
        new int[]{R.attr.string2}, 0, 0).getString(0))
        .isEqualTo("string 1 from Theme.Robolectric");
  }

  @Test
  public void dimenRef() {
    AttributeSet attributeSet = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.layout_height, "@dimen/test_px_dimen")
        .build();
    TypedArray typedArray = resources.newTheme().obtainStyledAttributes(
        attributeSet, new int[]{android.R.attr.layout_height}, 0, 0);
    assertThat(typedArray.getDimensionPixelSize(0, -1)).isEqualTo(15);
  }

  @Test
  public void dimenRefRef() {
    AttributeSet attributeSet = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.layout_height, "@dimen/ref_to_px_dimen")
        .build();
    TypedArray typedArray = resources.newTheme().obtainStyledAttributes(
        attributeSet, new int[]{android.R.attr.layout_height}, 0, 0);
    assertThat(typedArray.getDimensionPixelSize(0, -1)).isEqualTo(15);
  }

  @Test
  public void obtainStyledAttributes_shouldFindAttributeInDefaultStyle() {
    Theme theme = resources.newTheme();
    TypedArray typedArray = theme.obtainStyledAttributes(R.style.StyleA, new int[]{R.attr.string1});
    assertThat(typedArray.getString(0)).isEqualTo("string 1 from style A");
  }

  @Test
  public void shouldApplyFromStyleAttribute() {
    TestWithStyleAttrActivity activity = buildActivity(TestWithStyleAttrActivity.class).create().get();
    View button = activity.findViewById(R.id.button);
    assertThat(button.getLayoutParams().width).isEqualTo(42); // comes via style attr
  }

  @Test
  @Config(minSdk = VERSION_CODES.N)
  public void shouldFreeNativeObjectInRegistry() {
    final AtomicLong themeId = new AtomicLong(0);
    Supplier<Theme> themeSupplier =
        () -> {
          Theme theme = resources.newTheme();
          long nativeId =
              ReflectionHelpers.getField(ReflectionHelpers.getField(theme, "mThemeImpl"), "mTheme");
          themeId.set(nativeId);
          return theme;
        };

    WeakReference<Theme> weakRef = new WeakReference<>(themeSupplier.get());
    awaitFinalized(weakRef);
    assertThat(Registries.NATIVE_THEME9_REGISTRY.peekNativeObject(themeId.get())).isNull();
  }

  private static <T> void awaitFinalized(WeakReference<T> weakRef) {
    final CountDownLatch latch = new CountDownLatch(1);
    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
    while (System.nanoTime() < deadline) {
      if (weakRef.get() == null) {
        return;
      }
      try {
        System.gc();
        latch.await(100, TimeUnit.MILLISECONDS);
      } catch (InterruptedException e) {
        throw new AssertionError(e);
      }
    }
  }

  ////////////////////////////

  private XmlResourceParser getFirstElementAttrSet(int resId) throws Exception {
    XmlResourceParser xml = resources.getXml(resId);
    assertThat(xml.next()).isEqualTo(XmlPullParser.START_DOCUMENT);
    assertThat(xml.nextTag()).isEqualTo(XmlPullParser.START_TAG);
    return (XmlResourceParser) Xml.asAttributeSet(xml);
  }

  public static class TestActivityWithAnotherTheme extends TestActivity {
  }

  public static class TestWithStyleAttrActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.styles_button_with_style_layout);
    }
  }
}
