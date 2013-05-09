package org.robolectric.shadows;

import android.net.http.HttpResponseCache;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

@RunWith(TestRunners.WithDefaults.class)
public class ThingyTest {
    @Test public void shouldInstall() throws Exception {
        System.out.println("true = " + HttpResponseCache.class.getClassLoader());
        HttpResponseCache.install(null, 0);
    }
}
