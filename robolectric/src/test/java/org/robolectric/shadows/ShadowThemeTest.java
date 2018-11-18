package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Robolectric.buildActivity;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
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

  @Test public void forStylesWithImplicitParents_shouldInheritValuesNotDefinedInChild() throws Exception {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_Robolectric_EmptyParent, true);
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.string1}).hasValue(0)).isFalse();
  }

  @Test public void shouldApplyParentStylesFromAttrs() throws Exception {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.SimpleParent, true);
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.parent_string}).getString(0))
        .isEqualTo("parent string");
  }

  @Test public void applyStyle_shouldOverrideParentAttrs() throws Exception {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.SimpleChildWithOverride, true);
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.parent_string}).getString(0))
        .isEqualTo("parent string overridden by child");
  }

  @Test public void applyStyle_shouldOverrideImplicitParentAttrs() throws Exception {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.SimpleParent_ImplicitChild, true);
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.parent_string}).getString(0))
        .isEqualTo("parent string overridden by child");
  }

  @Test public void applyStyle_shouldInheritParentAttrs() throws Exception {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.SimpleChildWithAdditionalAttributes, true);
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.child_string}).getString(0))
        .isEqualTo("child string");
    assertThat(theme.obtainStyledAttributes(new int[] {R.attr.parent_string}).getString(0))
        .isEqualTo("parent string");
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
  public void setTo_whenSourceThemeIsModified_destThemeShouldNotMutate() throws Exception {
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
  public void applyStyle_withForceFalse_shouldApplyButNotOverwriteExistingAttributeValues() throws Exception {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.StyleA, false);
    assertThat(theme.obtainStyledAttributes(new int[]{R.attr.string1}).getString(0))
        .isEqualTo("string 1 from style A");

    theme.applyStyle(R.style.StyleB, false);
    assertThat(theme.obtainStyledAttributes(new int[]{R.attr.string1}).getString(0))
        .isEqualTo("string 1 from style A");
  }

  @Test
  public void applyStyle_withForceTrue_shouldApplyAndOverwriteExistingAttributeValues() throws Exception {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.StyleA, false);
    assertThat(theme.obtainStyledAttributes(new int[]{R.attr.string1}).getString(0))
        .isEqualTo("string 1 from style A");

    theme.applyStyle(R.style.StyleB, true);
    assertThat(theme.obtainStyledAttributes(new int[]{R.attr.string1}).getString(0))
        .isEqualTo("string 1 from style B");
  }

  @Test
  public void whenStyleSpecifiesAttr_obtainStyledAttribute_findsCorrectValue() throws Exception {
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
  public void whenAttrSetAttrSpecifiesAttr_obtainStyledAttribute_returnsItsValue() throws Exception {
    Resources.Theme theme = resources.newTheme();
    theme.applyStyle(R.style.Theme_Robolectric, false);
    theme.applyStyle(R.style.Theme_ThemeContainingStyleReferences, true);

    assertThat(theme.obtainStyledAttributes(
        Robolectric.buildAttributeSet().addAttribute(R.attr.string2, "?attr/string1").build(),
        new int[]{R.attr.string2}, 0, 0).getString(0))
        .isEqualTo("string 1 from Theme.Robolectric");
  }

  public static class TestActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.styles_button_layout);
    }
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

  @Test public void obtainStyledAttributes_shouldFindAttributeInDefaultStyle() throws Exception {
    Theme theme = resources.newTheme();
    TypedArray typedArray = theme.obtainStyledAttributes(R.style.StyleA, new int[]{R.attr.string1});
    assertThat(typedArray.getString(0)).isEqualTo("string 1 from style A");
  }

  @Test public void shouldApplyFromStyleAttribute() throws Exception {
    TestWithStyleAttrActivity activity = buildActivity(TestWithStyleAttrActivity.class).create().get();
    View button = activity.findViewById(R.id.button);
    assertThat(button.getLayoutParams().width).isEqualTo(42); // comes via style attr
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
