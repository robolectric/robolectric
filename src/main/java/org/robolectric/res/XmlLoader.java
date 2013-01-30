package com.xtremelabs.robolectric.res;

import org.w3c.dom.Document;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class XmlLoader {
    protected void processResourceXml(File xmlFile, Document document, String packageName) throws Exception {
        processResourceXml(xmlFile, document, new XmlContext(packageName, xmlFile));
    }

    protected abstract void processResourceXml(File xmlFile, Document document, XmlContext xmlContext) throws Exception;

    public static class XmlContext {
        public static final Pattern DIR_QUALIFIER_PATTERN = Pattern.compile("^[^-]+(?:-(.*))?$");

        public final String packageName;
        private final File xmlFile;

        public XmlContext(String packageName, File xmlFile) {
            this.packageName = packageName;
            this.xmlFile = xmlFile;
        }

        public String getQualifiers() {
            String parentDir = xmlFile.getParentFile().getName();
            Matcher matcher = DIR_QUALIFIER_PATTERN.matcher(parentDir);
            if (!matcher.find()) throw new IllegalStateException(parentDir);
            return matcher.group(1);
        }
    }
}
