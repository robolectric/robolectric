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
        String resourceFileName = resourceExtractor.getResName(resourceId).name;
        File rawResourceDir = new File(resourceDir, "raw");

        File[] files = rawResourceDir.listFiles();
        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                int dotIndex = name.indexOf(".");
                String fileBaseName = dotIndex >= 0 ? name.substring(0, dotIndex) : name;
                if (fileBaseName.equals(resourceFileName)) {
                    try {
                        return new FileInputStream(file);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return null;
    }

}
