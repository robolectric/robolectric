package com.xtremelabs.robolectric.res;

import org.w3c.dom.Document;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class XmlLoader {
    protected boolean strictI18n = false;

    protected void processResourceXml(File xmlFile, Document document, String packageName) throws Exception {
        processResourceXml(xmlFile, document, new XmlContext(packageName, xmlFile));
    }

    protected abstract void processResourceXml(File xmlFile, Document document, XmlContext xmlContext) throws Exception;

    public void setStrictI18n(boolean strict) {
    	this.strictI18n = strict;
    }

    public static class XmlContext {
        public static final Pattern LAYOUT_DIR_PATTERN = Pattern.compile("^[^-]+(?:-(.*))?$");

        public final String packageName;
        private final File xmlFile;

        public XmlContext(String packageName, File xmlFile) {
            this.packageName = packageName;
            this.xmlFile = xmlFile;
        }

        public String getQualifiers() {
            String parentDir = xmlFile.getParentFile().getName();
            Matcher matcher = LAYOUT_DIR_PATTERN.matcher(parentDir);
            if (!matcher.find()) throw new IllegalStateException(parentDir);
            return matcher.group(1);
        }
    }
}
