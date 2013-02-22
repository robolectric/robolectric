package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Node;

import java.net.URLEncoder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Attribute {
    private static final Logger LOGGER = Logger.getLogger(Attribute.class.getName());
    private static final Pattern NS_URI_PATTERN = Pattern.compile("^http://schemas.android.com/apk/res/(.*)$");
    private static final String RES_AUTO_NS_URI = "http://schemas.android.com/apk/res-auto";

    public final @NotNull ResName resName;
    public final @NotNull String value;
    public final @NotNull String contextPackageName;

    public Attribute(@NotNull String fullyQualifiedName, @NotNull String value, @NotNull String contextPackageName) {
        this(new ResName(fullyQualifiedName), value, contextPackageName);
    }

    public Attribute(@NotNull ResName resName, @NotNull String value, @NotNull String contextPackageName) {
        if (!resName.type.equals("attr")) throw new IllegalStateException("\"" + resName.getFullyQualifiedName() + "\" unexpected");

        this.resName = resName;
        this.value = value;
        this.contextPackageName = contextPackageName;
    }

    public Attribute(Node attr, XmlLoader.XmlContext xmlContext) {
        this(extractPackageName(attr.getNamespaceURI(), xmlContext) + ":attr/" + attr.getLocalName(),
                attr.getNodeValue(),
                xmlContext.packageName);
    }

    private static String extractPackageName(String namespaceUri, XmlLoader.XmlContext xmlContext) {
        if (namespaceUri == null) {
            return "";
        }

        if (RES_AUTO_NS_URI.equals(namespaceUri)) {
            return xmlContext.packageName;
        }

        Matcher matcher = NS_URI_PATTERN.matcher(namespaceUri);
        if (!matcher.find()) {
            if (!namespaceUri.equals("http://schemas.android.com/apk/prv/res/android")) {
                LOGGER.log(Level.WARNING, "unexpected ns uri \"" + namespaceUri + "\"");
            }
            return URLEncoder.encode(namespaceUri);
        }
        return matcher.group(1);
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "name='" + resName + '\'' +
                ", value='" + value + '\'' +
                ", contextPackageName='" + contextPackageName + '\'' +
                '}';
    }

    public static Attribute find(List<Attribute> attributes, String fullyQualifiedName) {
        return find(attributes, new ResName(fullyQualifiedName));
    }

    public static Attribute find(List<Attribute> attributes, ResName resName) {
        for (Attribute attribute : attributes) {
            if (resName.equals(attribute.resName)) {
                return attribute;
            }
        }
        return null;
    }

    public static Attribute find(List<Attribute> attributes, int attrId, ResourceIndex resourceIndex) {
        for (Attribute attribute : attributes) {
          Integer resourceId = resourceIndex.getResourceId(attribute.resName);
          if (resourceId != null && resourceId == attrId) {
                return attribute;
            }
        }
        return null;
    }

    public static String findValue(List<Attribute> attributes, String fullyQualifiedName) {
        return findValue(attributes, fullyQualifiedName, null);
    }

    public static String findValue(List<Attribute> attributes, String fullyQualifiedName, String defaultValue) {
        Attribute attribute = find(attributes, fullyQualifiedName);
        return (attribute != null) ? attribute.value : defaultValue;
    }

    public static void put(List<Attribute> attributes, Attribute attribute) {
        remove(attributes, attribute.resName);
        attributes.add(attribute);
    }

    public static Attribute remove(List<Attribute> attributes, String fullyQualifiedName) {
        return remove(attributes, new ResName(fullyQualifiedName));
    }

    public static Attribute remove(List<Attribute> attributes, ResName resName) {
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attribute = attributes.get(i);
            if (resName.equals(attribute.resName)) {
                attributes.remove(i);
                return attribute;
            }
        }
        return null;
    }

    public static String addType(String possiblyPartiallyQualifiedAttrName, String typeName) {
        return possiblyPartiallyQualifiedAttrName.contains(":") ? possiblyPartiallyQualifiedAttrName.replaceFirst(":", ":" + typeName + "/") : ":" + typeName + "/" + possiblyPartiallyQualifiedAttrName;
    }

    public String qualifiedValue() {
        if (value.startsWith("@")) {
            return ResName.qualifyResourceName(value.substring(1), contextPackageName);
        } else {
            return value;
        }
    }
}
