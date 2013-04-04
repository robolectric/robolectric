// Not in master, can maybe be deleted
package org.robolectric.shadows;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import java.io.File;
import java.io.FileWriter;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class ShadowTypefaceTest {
    private File fontFile;

    @Before
    public void setup() throws Exception {
        File assetsBase = shadowOf(Robolectric.application).getAppManifest().getAssetsDirectory();
        fontFile = new File(assetsBase, "myFont.ttf");
        FileWriter fileWriter = new FileWriter(fontFile);
        fileWriter.write("fontdata");
        fileWriter.close();
    }

    @After
    public void teardown() throws Exception {
        fontFile.delete();
    }

    @Test
    public void canAnswerAssetUsedDuringCreation() throws Exception {
        AssetManager assetManager = Robolectric.application.getAssets();
        Typeface typeface = Typeface.createFromAsset(assetManager, "myFont.ttf");
        assertThat(shadowOf(typeface).getAssetPath()).isEqualTo(fontFile.getPath());
    }

    @Test(expected = RuntimeException.class)
    public void createFromAsset_throwsExceptionWhenFontNotFound() throws Exception {
        Typeface.createFromAsset(Robolectric.application.getAssets(), "nonexistent.ttf");
    }
}
