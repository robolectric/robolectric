package com.xtremelabs.robolectric.res;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class RawResourceLoader {

    private ResourceExtractor resourceExtractor;
    private File resourceDir;

	public RawResourceLoader(ResourceExtractor resourceExtractor, File resourceDir) {
        this.resourceExtractor = resourceExtractor;
        this.resourceDir = resourceDir;
    }

    public InputStream getValue(int resourceId) {
    	// Get resource filename
        String resourceFileName = resourceExtractor.getResourceName(resourceId);
		if (resourceFileName == null) {
			throw new IllegalArgumentException(String.format(
					"Unknown resource: 0x%08x / %d",
					resourceId, resourceId));
		}

    	// Remove the raw/ prefix
		if (!resourceFileName.startsWith("raw/")) {
			throw new IllegalStateException(String.format(
					"Resource filename expected to start with /raw/\nID: 0x%08x / %d\nFilename: %s",
					resourceId, resourceId, resourceFileName));
		}
        String resourceName = resourceFileName.substring("raw/".length());

        // Find file
        File rawResourceDir = new File(resourceDir, "raw");
        try {
            File[] files = rawResourceDir.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                String name = file.getName();
                int dotIndex = name.indexOf(".");
                String fileBaseName = null;
                if (dotIndex >= 0) {
                    fileBaseName = name.substring(0, dotIndex);
                } else {
                    fileBaseName = name;
                }
                if (fileBaseName.equals(resourceName)) {
                    return new FileInputStream(file);
                }
            }
        } catch (FileNotFoundException e) {
			throw new IllegalStateException("This error shouldn't be possible.", e);
        }
		throw new IllegalStateException(String.format(
				"Unable to locate resource.\nID: 0x%08x / %d\nFilename: %s",
				resourceId, resourceId, resourceFileName));
    }

}
