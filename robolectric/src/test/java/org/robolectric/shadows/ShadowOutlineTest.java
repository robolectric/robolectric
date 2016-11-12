package org.robolectric.shadows;

import android.graphics.Outline;
import android.graphics.Path;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

@RunWith(TestRunners.MultiApiWithDefaults.class)
@Config(minSdk = LOLLIPOP)
public class ShadowOutlineTest {

    @Test
    public void setConvexPath_doesNothing() {
        final Outline outline = new Outline();
        outline.setConvexPath(new Path());
    }
}
