package org.robolectric.shadows;

import android.app.Activity;
import android.content.res.AssetManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.util.Strings;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.robolectric.util.TestUtil.joinPath;

@RunWith(TestRunners.WithDefaults.class)
public class AssetManagerTest {
    AssetManager assetManager;

    @Before
    public void setUp() throws Exception {
        assetManager = new Activity().getAssets();
    }

    @Test
    public void assertGetAssetsNotNull() {
        assertNotNull(assetManager);

        assetManager = Robolectric.application.getAssets();
        assertNotNull(assetManager);

        assetManager = Robolectric.application.getResources().getAssets();
        assertNotNull(assetManager);
    }

    @Test
    public void assetsPathListing() throws IOException {
        List<String> files;
        String testPath;

        testPath = "";
        files = Arrays.asList(assetManager.list(testPath));
        assertTrue(files.contains("docs"));
        assertTrue(files.contains("assetsHome.txt"));

        testPath = "docs";
        files = Arrays.asList(assetManager.list(testPath));
        assertTrue(files.contains("extra"));

        testPath = joinPath("docs", "extra");
        files = Arrays.asList(assetManager.list(testPath));
        assertTrue(files.contains("testing"));

        testPath = joinPath("docs", "extra", "testing");
        files = Arrays.asList(assetManager.list(testPath));
        assertTrue(files.contains("hello.txt"));

        testPath = "assetsHome.txt";
        files = Arrays.asList(assetManager.list(testPath));
        assertFalse(files.contains(testPath));

        testPath = "bogus.file";
        files = Arrays.asList(assetManager.list(testPath));
        assertEquals(0, files.size());
    }

    @Test
    public void assetsInputStreams() throws IOException {
        String testPath;
        String fileContents;
        InputStream inputStream;

        testPath = "assetsHome.txt";
        inputStream = assetManager.open(testPath);
        fileContents = Strings.fromStream(inputStream);
        assertEquals("assetsHome!", fileContents);

        testPath = joinPath("docs", "extra", "testing", "hello.txt");
        inputStream = assetManager.open(testPath);
        fileContents = Strings.fromStream(inputStream);
        assertEquals("hello!", fileContents);
    }
}
