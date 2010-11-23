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
        String resourceName = resourceExtractor.getResourceName(resourceId);
        try {
			return new FileInputStream(new File(this.resourceDir, resourceName));
		} catch (FileNotFoundException e) {
			return null;
		}
    }

}
