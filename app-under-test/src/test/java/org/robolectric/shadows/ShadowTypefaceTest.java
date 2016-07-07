package org.robolectric.shadows;

import android.graphics.Typeface;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.test.TemporaryAsset;

import java.io.File;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowTypefaceTest {
  private File fontFile;
  @Rule public TemporaryAsset temporaryAsset = new TemporaryAsset();

  @Before
  public void setup() throws Exception {
    AndroidManifest appManifest = shadowOf(RuntimeEnvironment.application).getAppManifest();
    fontFile = temporaryAsset.createFile(appManifest, "myFont.ttf", "myFontData");

    List<AndroidManifest> libraryManifests = appManifest.getLibraryManifests();
    temporaryAsset.createFile(libraryManifests.get(0), "libFont.ttf", "libFontData");
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
    Typeface typeface = Typeface.createFromAsset(RuntimeEnvironment.application.getAssets(), "libFont.ttf");

    assertThat(typeface.getStyle()).isEqualTo(Typeface.NORMAL);
    assertThat(shadowOf(typeface).getFontDescription().getFamilyName()).isEqualTo("libFont.ttf");
    assertThat(shadowOf(typeface).getFontDescription().getStyle()).isEqualTo(Typeface.NORMAL);
  }

  @Test(expected = RuntimeException.class)
  public void createFromAsset_throwsExceptionWhenFontNotFound() throws Exception {
    Typeface.createFromAsset(RuntimeEnvironment.application.getAssets(), "nonexistent.ttf");
  }
}
