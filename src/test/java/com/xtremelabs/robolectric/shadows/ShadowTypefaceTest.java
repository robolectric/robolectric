package com.xtremelabs.robolectric.shadows;

import android.graphics.Typeface;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class ShadowTypefaceTest {
    @Test
    public void canAnswerAssetUsedDuringCreation() throws Exception {
        Typeface typeface = Typeface.createFromAsset(Robolectric.application.getAssets(), "myFont.ttf");
        assertThat(shadowOf(typeface).getAssetPath(), equalTo("myFont.ttf"));
    }
}
