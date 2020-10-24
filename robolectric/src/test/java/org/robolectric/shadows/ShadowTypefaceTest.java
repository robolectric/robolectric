package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.graphics.Typeface;
import android.graphics.fonts.Font;
import android.graphics.fonts.FontFamily;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog.LogItem;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.TestUtil;

@RunWith(AndroidJUnit4.class)
public class ShadowTypefaceTest {

  private File fontFile;

  @Before
  public void setup() throws Exception {
    fontFile = TestUtil.resourcesBaseDir().resolve("assets/myFont.ttf").toFile();
  }

  @Test
  public void create_withFamilyName_shouldCreateTypeface() {
    Typeface typeface = Typeface.create("roboto", Typeface.BOLD);
    assertThat(typeface.getStyle()).isEqualTo(Typeface.BOLD);
    assertThat(shadowOf(typeface).getFontDescription().getFamilyName()).isEqualTo("roboto");
    assertThat(shadowOf(typeface).getFontDescription().getStyle()).isEqualTo(Typeface.BOLD);
  }

  @Test
  public void create_withFamily_shouldCreateTypeface() {
    Typeface typeface = Typeface.create(Typeface.create("roboto", Typeface.BOLD), Typeface.ITALIC);

    assertThat(typeface.getStyle()).isEqualTo(Typeface.ITALIC);
    assertThat(shadowOf(typeface).getFontDescription().getFamilyName()).isEqualTo("roboto");
    assertThat(shadowOf(typeface).getFontDescription().getStyle()).isEqualTo(Typeface.ITALIC);
  }

  @Test
  public void create_withoutFamily_shouldCreateTypeface() {
    Typeface typeface = Typeface.create((Typeface) null, Typeface.ITALIC);
    assertThat(typeface.getStyle()).isEqualTo(Typeface.ITALIC);
    assertThat(shadowOf(typeface).getFontDescription().getFamilyName()).isEqualTo(null);
    assertThat(shadowOf(typeface).getFontDescription().getStyle()).isEqualTo(Typeface.ITALIC);
  }

  @Test
  @Config(minSdk = P)
  public void create_withFamily_customWeight_shouldCreateTypeface() {
    Typeface typeface =
        Typeface.create(
            Typeface.create("roboto", Typeface.NORMAL), /* weight= */ 400, /* italic= */ false);
    assertThat(typeface.getStyle()).isEqualTo(400);
    assertThat(shadowOf(typeface).getFontDescription().getFamilyName()).isEqualTo("roboto");
    assertThat(shadowOf(typeface).getFontDescription().getStyle()).isEqualTo(400);
  }

  @Test
  @Config(minSdk = P)
  public void create_withoutFamily_customWeight_shouldCreateTypeface() {
    Typeface typeface = Typeface.create((Typeface) null, /* weight= */ 500, /* italic= */ false);
    assertThat(typeface.getStyle()).isEqualTo(500);
    assertThat(shadowOf(typeface).getFontDescription().getFamilyName()).isEqualTo(null);
    assertThat(shadowOf(typeface).getFontDescription().getStyle()).isEqualTo(500);
  }

  @Test
  public void createFromFile_withFile_shouldCreateTypeface() {
    Typeface typeface = Typeface.createFromFile(fontFile);

    assertThat(typeface.getStyle()).isEqualTo(Typeface.NORMAL);
    assertThat(shadowOf(typeface).getFontDescription().getFamilyName()).isEqualTo("myFont.ttf");
  }

  @Test
  public void createFromFile_withPath_shouldCreateTypeface() {
    Typeface typeface = Typeface.createFromFile(fontFile.getPath());

    assertThat(typeface.getStyle()).isEqualTo(Typeface.NORMAL);
    assertThat(shadowOf(typeface).getFontDescription().getFamilyName()).isEqualTo("myFont.ttf");
    assertThat(shadowOf(typeface).getFontDescription().getStyle()).isEqualTo(Typeface.NORMAL);
  }

  @Test
  public void createFromAsset_shouldCreateTypeface() {
    Typeface typeface =
        Typeface.createFromAsset(
            ApplicationProvider.getApplicationContext().getAssets(), "myFont.ttf");

    assertThat(typeface.getStyle()).isEqualTo(Typeface.NORMAL);
    assertThat(shadowOf(typeface).getFontDescription().getFamilyName()).isEqualTo("myFont.ttf");
    assertThat(shadowOf(typeface).getFontDescription().getStyle()).isEqualTo(Typeface.NORMAL);
  }

  @Test
  public void createFromAsset_throwsExceptionWhenFontNotFound() throws Exception {
    try {
      Typeface.createFromAsset(
          ApplicationProvider.getApplicationContext().getAssets(), "nonexistent.ttf");
      fail("Expected exception");
    } catch (RuntimeException expected) {
      // Expected
    }
  }

  @Test
  public void equals_bothRoboto_shouldBeTrue() {
    Typeface roboto = Typeface.create("roboto", Typeface.BOLD);
    assertThat(roboto).isEqualTo(Typeface.create("roboto", Typeface.BOLD));
  }

  @Test
  public void equals_robotoAndDroid_shouldBeFalse() {
    Typeface roboto = Typeface.create("roboto", Typeface.BOLD);
    Typeface droid = Typeface.create("droid", Typeface.BOLD);
    assertThat(roboto).isNotEqualTo(droid);
  }

  @Test
  public void hashCode_bothRoboto_shouldBeEqual() {
    Typeface roboto = Typeface.create("roboto", Typeface.BOLD);
    assertThat(roboto.hashCode()).isEqualTo(Typeface.create("roboto", Typeface.BOLD).hashCode());
  }

  @Test
  public void hashCode_robotoAndDroid_shouldNotBeEqual() {
    Typeface roboto = Typeface.create("roboto", Typeface.BOLD);
    Typeface droid = Typeface.create("droid", Typeface.BOLD);
    assertThat(roboto.hashCode()).isNotEqualTo(droid.hashCode());
  }

  /** Check that there is no spurious error message about /system/etc/fonts.xml */
  @Test
  @Config(minSdk = LOLLIPOP, maxSdk = O)
  public void init_shouldNotComplainAboutSystemFonts() {
    ShadowLog.clear();
    ReflectionHelpers.callStaticMethod(Typeface.class, "init");
    List<LogItem> logs = ShadowLog.getLogsForTag("Typeface");
    assertThat(logs).isEmpty();
  }

  @Test
  @Config(minSdk = Q)
  public void typeface_customFallbackBuilder_afterReset() throws IOException {
    Font font = new Font.Builder(fontFile).build();
    FontFamily family = new FontFamily.Builder(font).build();
    // This invokes the Typeface static initializer, which creates some default typefaces.
    Typeface.create("roboto", Typeface.BOLD);
    // Call the resetter to clear the FONTS map in Typeface
    ShadowTypeface.reset();
    Typeface typeface =
        new Typeface.CustomFallbackBuilder(family).setStyle(font.getStyle()).build();
    assertThat(typeface).isNotNull();
  }
}
