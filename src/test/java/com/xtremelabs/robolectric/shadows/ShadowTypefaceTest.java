package com.xtremelabs.robolectric.shadows;

import android.graphics.Typeface;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileWriter;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class ShadowTypefaceTest {
    @Test
    public void canAnswerAssetUsedDuringCreation() throws Exception {
        String fontFile = "assets/myfont.ttf";
        try {
            File assetsBase = shadowOf(Robolectric.application).getResourceLoader().getAssetsBase();
            FileWriter fileWriter = new FileWriter(new File(assetsBase, "myFont.ttf"));
            fileWriter.write("fontdata");
            fileWriter.close();
            Typeface typeface = Typeface.createFromAsset(Robolectric.application.getAssets(), "myFont.ttf");
            assertThat(shadowOf(typeface).getAssetPath(), equalTo("myFont.ttf"));
        } finally {
            new File(fontFile).delete();
        }
    }

    @Test(expected = RuntimeException.class)
    public void createFromAsset_throwsExceptionWhenFontNotFound() throws Exception {
        Typeface.createFromAsset(Robolectric.application.getAssets(), "nonexistent.ttf");
    }
}
