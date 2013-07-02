package org.robolectric;

import android.net.Uri;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collection;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Parameterized tests using an Android class.
 *
 * @author John Ferlisi
 */
@RunWith(ParameterizedRobolectricTestRunner.class)
public final class ParameterizedRobolectricTestRunner_uriTest {

    private final String basePath;
    private final String resourcePath;
    private final Uri expectedUri;

    public ParameterizedRobolectricTestRunner_uriTest(String basePath,
                                                      String resourcePath,
                                                      String expectedUri) {
        this.basePath = basePath;
        this.resourcePath = resourcePath;
        this.expectedUri = Uri.parse(expectedUri);
    }

    @Test
    public void parse() {
        assertThat(Uri.parse(basePath).buildUpon().path(resourcePath).build()).isEqualTo(expectedUri);
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "URI Test: {0} + {1}")
    public static Collection getTestData() {
        Object[][] data = {
                { "http://host", "resource", "http://host/resource" },
                { "http://host/", "resource","http://host/resource" },
                { "http://host", "/resource","http://host/resource" },
                { "http://host/", "/resource","http://host/resource" }
        };
        return Arrays.asList(data);
    }
}
