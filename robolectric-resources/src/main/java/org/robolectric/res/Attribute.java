package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Node;

import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Attribute {
  public static final String ANDROID_RES_NS_PREFIX = "http://schemas.android.com/apk/res/";
  private static final int ANDROID_RES_NS_PREFIX_LENGTH = ANDROID_RES_NS_PREFIX.length();
  private static final Logger LOGGER = Logger.getLogger(Attribute.class.getName());
  public static final String RES_AUTO_NS_URI = "http://schemas.android.com/apk/res-auto";

  public final @NotNull ResName resName;
  public final @NotNull String value;
  public final @NotNull String contextPackageName;

  static String addType(String possiblyPartiallyQualifiedAttrName, String typeName) {
    return possiblyPartiallyQualifiedAttrName.contains(":") ? possiblyPartiallyQualifiedAttrName.replaceFirst(":", ":" + typeName + "/") : ":" + typeName + "/" + possiblyPartiallyQualifiedAttrName;
  }

  static String qualifyName(String possiblyQualifiedAttrName, String defaultPackage) {
    if (possiblyQualifiedAttrName.indexOf(':') == -1) {
      return defaultPackage + ":" + possiblyQualifiedAttrName;
    }
    return possiblyQualifiedAttrName;
  }

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

    return extractPackageName(namespaceUri);
  }

  public static String extractPackageName(@NotNull String namespaceUri) {
    if (namespaceUri.startsWith(ANDROID_RES_NS_PREFIX)) {
      return namespaceUri.substring(ANDROID_RES_NS_PREFIX_LENGTH);
    } else {
      if (!namespaceUri.equals("http://schemas.android.com/apk/prv/res/android")) {
        LOGGER.log(Level.WARNING, "unexpected ns uri \"" + namespaceUri + "\"");
      }
      return URLEncoder.encode(namespaceUri);
    }
  }

  public String qualifiedValue() {
    if (isResourceReference()) return "@" + getResourceReference().getFullyQualifiedName();
    if (isStyleReference()) return "?" + getStyleReference().getFullyQualifiedName();
    else return value;
  }

  public boolean isResourceReference() {
    return value.startsWith("@") && !isNull();
  }

  public @NotNull ResName getResourceReference() {
    if (!isResourceReference()) throw new RuntimeException("not a resource reference: " + this);
    return ResName.qualifyResName(value.substring(1).replace("+", ""), contextPackageName, "attr");
  }

  public boolean isStyleReference() {
    return value.startsWith("?");
  }

  public ResName getStyleReference() {
    if (!isStyleReference()) throw new RuntimeException("not a style reference: " + this);
    return ResName.qualifyResName(value.substring(1), contextPackageName, "attr");
  }

  public boolean isNull() {
    return "@null".equals(value);
  }

  public boolean isEmpty() {
    return "@empty".equals(value);
  }

  @Override
  public String toString() {
    return "Attribute{" +
        "name='" + resName + '\'' +
        ", value='" + value + '\'' +
        ", contextPackageName='" + contextPackageName + '\'' +
        '}';
  }
}
