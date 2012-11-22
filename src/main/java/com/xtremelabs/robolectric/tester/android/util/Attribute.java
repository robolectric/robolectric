package com.xtremelabs.robolectric.tester.android.util;

import com.xtremelabs.robolectric.res.ResourceExtractor;
import com.xtremelabs.robolectric.res.XmlLoader;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Attribute {
    private static final Logger LOGGER = LoggerFactory.getLogger(Attribute.class);

    private static final Pattern FQN_PATTERN = Pattern.compile("^([^:]*):([^/]+)/(.+)$");
    private static final Pattern NS_URI_PATTERN = Pattern.compile("^http://schemas.android.com/apk/res/(.*)$");

    public final @NotNull String fullyQualifiedName;
    public final @NotNull String value;
    public final @NotNull String contextPackageName;

    public Attribute(@NotNull String fullyQualifiedName, @NotNull String value, @NotNull String contextPackageName) {
        Matcher matcher = FQN_PATTERN.matcher(fullyQualifiedName);
        if (!matcher.find()) {
            throw new IllegalStateException("\"" + fullyQualifiedName + "\" is not fully qualified");
        }
        if (matcher.group(1).equals("xmlns")) {
            throw new IllegalStateException("\"" + fullyQualifiedName + "\" unexpected");
        }
        if (!matcher.group(2).equals("attr")) {
            throw new IllegalStateException("\"" + fullyQualifiedName + "\" unexpected");
        }

        this.fullyQualifiedName = fullyQualifiedName;
        this.value = value;
        this.contextPackageName = contextPackageName;
    }

    public Attribute(Node attr, XmlLoader.XmlContext xmlContext) {
        this(extractPackageName(attr.getNamespaceURI()) + ":attr/" + attr.getLocalName(), attr.getNodeValue(), xmlContext.packageName);
    }

    private static String extractPackageName(String namespaceUri) {
        if (namespaceUri == null) {
            LOGGER.warn("unexpected ns uri null");
            return "";
        }

        Matcher matcher = NS_URI_PATTERN.matcher(namespaceUri);
        if (!matcher.find()) {
            LOGGER.warn("unexpected ns uri \"" + namespaceUri + "\"");
            return URLEncoder.encode(namespaceUri);
        }
        return matcher.group(1);
    }

    public static String getResourceName(String fullyQualifiedName) {
        Matcher matcher = FQN_PATTERN.matcher(fullyQualifiedName);
        if (matcher.find()) {
            return matcher.group(3);
        } else {
            throw new IllegalStateException("unexpected: \"" + fullyQualifiedName + "\"");
        }
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "name='" + fullyQualifiedName + '\'' +
                ", value='" + value + '\'' +
                ", contextPackageName='" + contextPackageName + '\'' +
                '}';
    }

    public static Attribute find(List<Attribute> attributes, String fullyQualifiedName) {
        for (Attribute attribute : attributes) {
            if (fullyQualifiedName.equals(attribute.fullyQualifiedName)) {
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
        remove(attributes, attribute.fullyQualifiedName);
        attributes.add(attribute);
    }

    public static Attribute remove(List<Attribute> attributes, String fullyQualifiedName) {
        for (int i = 0; i < attributes.size(); i++) {
            Attribute attribute = attributes.get(i);
            if (fullyQualifiedName.equals(attribute.fullyQualifiedName)) {
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
            return ResourceExtractor.qualifyResourceName(value.substring(1), contextPackageName);
        } else {
            return value;
        }
    }
}
