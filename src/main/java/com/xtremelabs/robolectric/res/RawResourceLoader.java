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
        String resourceFileName = resourceExtractor.getResourceName(resourceId);
        String resourceName = resourceFileName.substring("/raw".length());

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
            return null;
        }
        return null;
    }

}
