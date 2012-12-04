package com.xtremelabs.robolectric.res;

import org.w3c.dom.Document;

import java.io.File;

public abstract class XmlLoader {
    protected ResourceExtractor resourceExtractor;
    protected boolean strictI18n = false;

    public XmlLoader(ResourceExtractor resourceExtractor) {
        this.resourceExtractor = resourceExtractor;
    }

    protected void processResourceXml(File xmlFile, Document document, String packageName) throws Exception {
        processResourceXml(xmlFile, document, new XmlContext(packageName));
    }

    protected abstract void processResourceXml(File xmlFile, Document document, XmlContext xmlContext) throws Exception;

    public void setStrictI18n(boolean strict) {
    	this.strictI18n = strict;
    }

    public static class XmlContext {
        public final String packageName;

        public XmlContext(String packageName) {
            this.packageName = packageName;
        }
    }
}
