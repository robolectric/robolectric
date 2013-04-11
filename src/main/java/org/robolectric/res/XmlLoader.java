package org.robolectric.res;

import com.ximpleware.VTDNav;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class XmlLoader {
    private static final DocumentBuilderFactory documentBuilderFactory;
    static {
        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setIgnoringComments(true);
        documentBuilderFactory.setIgnoringElementContentWhitespace(true);
    }

    private DocumentBuilder documentBuilder;

    synchronized protected Document parse(File xmlFile) {
        try {
            if (documentBuilder == null) {
                documentBuilder = documentBuilderFactory.newDocumentBuilder();
            }
            return documentBuilder.parse(xmlFile);
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void processResourceXml(File xmlFile, VTDNav vtdNav, String packageName) throws Exception {
        processResourceXml(xmlFile, new XpathResourceXmlLoader.XmlNode(vtdNav), new XmlContext(packageName, xmlFile));
    }

    protected abstract void processResourceXml(File xmlFile, XpathResourceXmlLoader.XmlNode xmlNode, XmlContext xmlContext) throws Exception;

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
