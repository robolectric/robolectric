package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;

import android.graphics.Typeface;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.res.FileFsFile;
import org.robolectric.util.TestUtil;

@RunWith(AndroidJUnit4.class)
public class ShadowTypefaceTest {

  private File fontFile;

  @Before
  public void setup() throws Exception {
    fontFile =
        ((FileFsFile) TestUtil.resourcesBaseDir().join("assets/myFont.ttf")).getFile();
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
}
