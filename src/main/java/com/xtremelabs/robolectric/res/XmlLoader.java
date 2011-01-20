package com.xtremelabs.robolectric.res;

import org.w3c.dom.Document;

import java.io.File;

public abstract class XmlLoader {
    protected ResourceExtractor resourceExtractor;

    public XmlLoader(ResourceExtractor resourceExtractor) {
        this.resourceExtractor = resourceExtractor;
    }

    protected abstract void processResourceXml(File xmlFile, Document document, boolean isSystem) throws Exception;
}
