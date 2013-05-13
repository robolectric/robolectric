// Not in master, can maybe be deleted
package org.robolectric.shadows;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import java.io.File;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.AndroidManifest;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.test.TemporaryAsset;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowTypefaceTest {
  @Rule public TemporaryAsset temporaryAsset = new TemporaryAsset();
  private File fontFile;
  private File libraryFontFile;

  @Before
  public void setup() throws Exception {
    AndroidManifest appManifest = shadowOf(Robolectric.application).getAppManifest();
    fontFile = temporaryAsset.createFile(appManifest, "myFont.ttf", "myFontData");

    List<AndroidManifest> libraryManifests = appManifest.getLibraryManifests();
    libraryFontFile = temporaryAsset.createFile(libraryManifests.get(0), "libFont.ttf", "libFontData");
  }

  @Test
  public void canAnswerAssetUsedDuringCreation() throws Exception {
    AssetManager assetManager = Robolectric.application.getAssets();
    Typeface typeface = Typeface.createFromAsset(assetManager, "myFont.ttf");
    assertThat(shadowOf(typeface).getAssetPath()).isEqualTo(fontFile.getPath());
  }

  @Test
  public void canAnswerAssetFromLibraryUsedDuringCreation() throws Exception {
    AssetManager assetManager = Robolectric.application.getAssets();
    Typeface typeface = Typeface.createFromAsset(assetManager, "libFont.ttf");
    assertThat(shadowOf(typeface).getAssetPath()).isEqualTo(libraryFontFile.getPath());
  }

  @Test(expected = RuntimeException.class)
  public void createFromAsset_throwsExceptionWhenFontNotFound() throws Exception {
    Typeface.createFromAsset(Robolectric.application.getAssets(), "nonexistent.ttf");
  }
}
