package com.xtremelabs.robolectric.res;


import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

public class ResourceLoaderTest {
    @Test
    public void shouldUseFileSystemSeparatorWhenEvaluatingLayoutDirectories() throws Exception {
        assertTrue(ResourceLoader.isLayoutDirectory(File.separator + "layout"));
    }
}
