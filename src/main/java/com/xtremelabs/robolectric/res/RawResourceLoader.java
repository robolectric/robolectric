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
        
        // FIXME not sure if that is correct - but it works
        //String resourceName = resourceFileName.substring("/raw".length()); // original code
        
        String[] splittedResourceFileName = resourceFileName.split("/");
        String childPath = "";
        for(int i = 0; i < splittedResourceFileName.length - 1; i++)
        	childPath += splittedResourceFileName[i];
        String resourceName = splittedResourceFileName[splittedResourceFileName.length - 1];

        //File rawResourceDir = new File(resourceDir, "raw"); // original code
        File rawResourceDir = new File(resourceDir, childPath);

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
