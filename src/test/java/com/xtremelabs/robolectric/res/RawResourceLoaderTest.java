package com.xtremelabs.robolectric.res;

import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

import static com.xtremelabs.robolectric.util.TestUtil.resourceFile;
import static org.junit.Assert.assertEquals;

@RunWith(WithTestDefaultsRunner.class)
public class RawResourceLoaderTest {

    private RawResourceLoader rawResourceLoader;

    @Before public void setUp() throws Exception {
        ResourceExtractor resourceExtractor = new ResourceExtractor();
        resourceExtractor.addLocalRClass(R.class);
        rawResourceLoader = new RawResourceLoader(resourceExtractor, resourceFile("res"));
    }

    @Test
    public void shouldReturnRawResourcesWithExtensions() throws Exception {
        InputStream is = rawResourceLoader.getValue(R.raw.raw_resource);
        assertEquals("raw txt file contents", readString(is));
    }

    @Test
    public void shouldReturnRawResourcesWithoutExtensions() throws Exception {
        InputStream is = rawResourceLoader.getValue(R.raw.raw_no_ext);
        assertEquals("no ext file contents", readString(is));
    }

    private static String readString(InputStream is) throws IOException {
        Writer writer = new StringWriter();
        char[] buffer = new char[1024];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }
        return writer.toString();
    }
}
