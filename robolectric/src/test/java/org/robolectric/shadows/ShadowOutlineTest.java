package org.robolectric.shadows;

import android.graphics.Outline;
import android.graphics.Path;
import android.os.Build;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.annotation.Config;

@RunWith(TestRunners.MultiApiWithDefaults.class)
@Config(sdk = {
    Build.VERSION_CODES.LOLLIPOP,
    Build.VERSION_CODES.LOLLIPOP_MR1
})
public class ShadowOutlineTest {

    @Test
    public void setConvexPath_doesNothing() {
        final Outline outline = new Outline();
        outline.setConvexPath(new Path());
    }
}
