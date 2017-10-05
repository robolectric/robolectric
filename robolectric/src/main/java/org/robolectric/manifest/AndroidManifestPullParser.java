package org.robolectric.manifest;

import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Package;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.DisplayMetrics;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import libcore.io.IoUtils;
import org.robolectric.util.ReflectionHelpers;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AndroidManifestPullParser implements AndroidManifestParser {
  private static final String ANDROID_MANIFEST_FILENAME = "AndroidManifest.xml";
  private final AssetManager assets;

  public AndroidManifestPullParser(AssetManager assets) {
    this.assets = assets;
  }

  @Override
  public void parse(AndroidManifest androidManifest) {
    String apkPath = ANDROID_MANIFEST_FILENAME;

    Resources res = new Resources(assets, new DisplayMetrics(), new Configuration());
    XmlResourceParser parser = null;
    try {
      parser = assets.openXmlResourceParser(0, ANDROID_MANIFEST_FILENAME);

      int flags = 0;
      final String[] outError = new String[1];

      PackageParser packageParser = new PackageParser();
      // final Package pkg = parseBaseApk(apkPath, res, parser, flags, outError);
      final Package pkg =
          ReflectionHelpers.callInstanceMethod(PackageParser.class, packageParser, "parseBaseApk",
          from(String.class, "dunno"),
          from(Resources.class, res),
          from(XmlResourceParser.class, parser),
          from(int.class, flags),
          from(String[].class, outError)
      );
      if (pkg == null) {
        throw new RuntimeException("dunno (at " +  parser.getPositionDescription() + "): " + outError[0]);
        // throw new PackageParserException(mParseError,
        //     apkPath + " (at " + parser.getPositionDescription() + "): " + outError[0]);
      }

      // pkg.setVolumeUuid(volumeUuid);
      // pkg.setApplicationVolumeUuid(volumeUuid);
      // pkg.setBaseCodePath(apkPath);
      pkg.setSignatures(null);

      System.out.println("pkg = " + pkg);

    } catch (Exception e) {
      throw new RuntimeException(e);
    // } catch (Exception e) {
    //   throw new PackageParserException(INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION,
    //       "Failed to read manifest from " + apkPath, e);
    } finally {
      IoUtils.closeQuietly(parser);
    }

    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

      DocumentBuilder db = dbf.newDocumentBuilder();
      InputStream inputStream = androidManifest.androidManifestFile.getInputStream();
      Document manifestDocument = db.parse(inputStream);
      inputStream.close();

      if (!androidManifest.packageNameIsOverridden()) {
        androidManifest.packageName = AndroidManifestPullParser
            .getTagAttributeText(manifestDocument, "manifest", "package");
      }

      androidManifest.versionCode = getTagAttributeIntValue(manifestDocument, "manifest", "android:versionCode", 0);
      androidManifest.versionName = AndroidManifestPullParser
          .getTagAttributeText(manifestDocument, "manifest", "android:versionName");
      androidManifest.rClassName = androidManifest.packageName + ".R";

      Node applicationNode = findApplicationNode(manifestDocument);
      if (applicationNode != null) {
        NamedNodeMap attributes = applicationNode.getAttributes();
        int attrCount = attributes.getLength();
        for (int i = 0; i < attrCount; i++) {
          Node attr = attributes.item(i);
          androidManifest.applicationAttributes.put(attr.getNodeName(), attr.getTextContent());
        }

        androidManifest.applicationName = androidManifest.applicationAttributes.get("android:name");
        androidManifest.applicationLabel = androidManifest.applicationAttributes.get("android:label");
        androidManifest.processName = androidManifest.applicationAttributes.get("android:process");
        androidManifest.themeRef = androidManifest.applicationAttributes.get("android:theme");
        androidManifest.labelRef = androidManifest.applicationAttributes.get("android:label");

        parseReceivers(applicationNode, androidManifest);
        parseServices(applicationNode, androidManifest);
        parseActivities(applicationNode, androidManifest);
        parseApplicationMetaData(applicationNode, androidManifest);
        parseContentProviders(applicationNode, androidManifest);
      }

      androidManifest.minSdkVersion =
          getTagAttributeIntValue(manifestDocument, "uses-sdk", "android:minSdkVersion");

      String targetSdkText = AndroidManifestPullParser
          .getTagAttributeText(manifestDocument, "uses-sdk",
              "android:targetSdkVersion");
      if (targetSdkText != null) {
        // Support Android O Preview. This can be removed once Android O is officially launched.
        androidManifest.targetSdkVersion = targetSdkText.equals("O") ? 26 : Integer.parseInt(targetSdkText);
      }

      androidManifest.maxSdkVersion =
          getTagAttributeIntValue(manifestDocument, "uses-sdk", "android:maxSdkVersion");
      if (androidManifest.processName == null) {
        androidManifest.processName = androidManifest.packageName;
      }

      parseUsedPermissions(manifestDocument, androidManifest);
      parsePermissions(manifestDocument, androidManifest);
    } catch (Exception ignored) {
      ignored.printStackTrace();
    }
  }

  private void parseApplicationMetaData(Node applicationNode,
      AndroidManifest androidManifest) {
    androidManifest.applicationMetaData = new MetaData(getChildrenTags(applicationNode, "meta-data"));
  }

  private String resolveClassRef(String maybePartialClassName,
      AndroidManifest androidManifest) {
    return (maybePartialClassName.startsWith(".")) ? androidManifest.packageName + maybePartialClassName : maybePartialClassName;
  }

  private List<Node> getChildrenTags(final Node node, final String tagName) {
    List<Node> children = new ArrayList<>();
    for (int i = 0; i < node.getChildNodes().getLength(); i++) {
      Node childNode = node.getChildNodes().item(i);
      if (childNode.getNodeName().equalsIgnoreCase(tagName)) {
        children.add(childNode);
      }
    }
    return children;
  }

  private Integer getTagAttributeIntValue(final Document doc, final String tag, final String attribute) {
    return getTagAttributeIntValue(doc, tag, attribute, null);
  }

  Integer getTagAttributeIntValue(final Document doc, final String tag, final String attribute,
      final Integer defaultValue) {
    String valueString = getTagAttributeText(doc, tag, attribute);
    if (valueString != null) {
      return Integer.parseInt(valueString);
    }
    return defaultValue;
  }


  private void parseUsedPermissions(Document manifestDocument,
      AndroidManifest androidManifest) {
    NodeList elementsByTagName = manifestDocument.getElementsByTagName("uses-permission");
    int length = elementsByTagName.getLength();
    for (int i = 0; i < length; i++) {
      Node node = elementsByTagName.item(i).getAttributes().getNamedItem("android:name");
      androidManifest.usedPermissions.add(node.getNodeValue());
    }
  }

  private void parsePermissions(final Document manifestDocument,
      AndroidManifest androidManifest) {
    NodeList elementsByTagName = manifestDocument.getElementsByTagName("permission");

    for (int i = 0; i < elementsByTagName.getLength(); i++) {
      Node permissionNode = elementsByTagName.item(i);
      final MetaData metaData = new MetaData(getChildrenTags(permissionNode, "meta-data"));
      String name = getAttributeValue(permissionNode, "android:name");
      androidManifest.permissions.put(name,
          new PermissionItemData(
              name,
              getAttributeValue(permissionNode, "android:label"),
              getAttributeValue(permissionNode, "android:description"),
              getAttributeValue(permissionNode, "android:permissionGroup"),
              getAttributeValue(permissionNode, "android:protectionLevel"),
              metaData
          ));
    }
  }

  private void parseContentProviders(Node applicationNode,
      AndroidManifest androidManifest) {
    for (Node contentProviderNode : getChildrenTags(applicationNode, "provider")) {
      String name = getAttributeValue(contentProviderNode, "android:name");
      String authorities = getAttributeValue(contentProviderNode, "android:authorities");
      MetaData metaData = new MetaData(getChildrenTags(contentProviderNode, "meta-data"));

      List<PathPermissionData> pathPermissionDatas = new ArrayList<>();
      for (Node node : getChildrenTags(contentProviderNode, "path-permission")) {
        pathPermissionDatas.add(new PathPermissionData(
            getAttributeValue(node, "android:path"),
            getAttributeValue(node, "android:pathPrefix"),
            getAttributeValue(node, "android:pathPattern"),
            getAttributeValue(node, "android:readPermission"),
            getAttributeValue(node, "android:writePermission")
        ));
      }

      androidManifest.providers.add(new ContentProviderData(resolveClassRef(name, androidManifest),
          metaData,
          authorities,
          getAttributeValue(contentProviderNode, "android:readPermission"),
          getAttributeValue(contentProviderNode, "android:writePermission"),
          pathPermissionDatas));
    }
  }

  private @Nullable
  String getAttributeValue(Node parentNode, String attributeName) {
    Node attributeNode = parentNode.getAttributes().getNamedItem(attributeName);
    return attributeNode == null ? null : attributeNode.getTextContent();
  }

  private void parseReceivers(Node applicationNode,
      AndroidManifest androidManifest) {
    for (Node receiverNode : getChildrenTags(applicationNode, "receiver")) {
      final HashMap<String, String> receiverAttrs = parseNodeAttributes(receiverNode);

      String receiverName = resolveClassRef(receiverAttrs.get("android:name"), androidManifest);
      receiverAttrs.put("android:name", receiverName);

      MetaData metaData = new MetaData(getChildrenTags(receiverNode, "meta-data"));

      final List<IntentFilterData> intentFilterData = parseIntentFilters(receiverNode);
      BroadcastReceiverData receiver =
          new BroadcastReceiverData(receiverAttrs, metaData, intentFilterData);
      List<Node> intentFilters = getChildrenTags(receiverNode, "intent-filter");
      for (Node intentFilterNode : intentFilters) {
        for (Node actionNode : getChildrenTags(intentFilterNode, "action")) {
          Node nameNode = actionNode.getAttributes().getNamedItem("android:name");
          if (nameNode != null) {
            receiver.addAction(nameNode.getTextContent());
          }
        }
      }

      androidManifest.receivers.add(receiver);
    }
  }

  private void parseServices(Node applicationNode,
      AndroidManifest androidManifest) {
    for (Node serviceNode : getChildrenTags(applicationNode, "service")) {
      final HashMap<String, String> serviceAttrs = parseNodeAttributes(serviceNode);

      String serviceName = resolveClassRef(serviceAttrs.get("android:name"), androidManifest);
      serviceAttrs.put("android:name", serviceName);

      MetaData metaData = new MetaData(getChildrenTags(serviceNode, "meta-data"));

      final List<IntentFilterData> intentFilterData = parseIntentFilters(serviceNode);
      ServiceData service = new ServiceData(serviceAttrs, metaData, intentFilterData);
      List<Node> intentFilters = getChildrenTags(serviceNode, "intent-filter");
      for (Node intentFilterNode : intentFilters) {
        for (Node actionNode : getChildrenTags(intentFilterNode, "action")) {
          Node nameNode = actionNode.getAttributes().getNamedItem("android:name");
          if (nameNode != null) {
            service.addAction(nameNode.getTextContent());
          }
        }
      }

      androidManifest.serviceDatas.put(serviceName, service);
    }
  }

  private void parseActivities(Node applicationNode,
      AndroidManifest androidManifest) {
    for (Node activityNode : getChildrenTags(applicationNode, "activity")) {
      parseActivity(activityNode, false, androidManifest);
    }

    for (Node activityNode : getChildrenTags(applicationNode, "activity-alias")) {
      parseActivity(activityNode, true, androidManifest);
    }
  }

  Node findApplicationNode(Document manifestDocument) {
    NodeList applicationNodes = manifestDocument.getElementsByTagName("application");
    if (applicationNodes.getLength() > 1) {
      throw new RuntimeException("found " + applicationNodes.getLength() + " application elements");
    }
    return applicationNodes.item(0);
  }

  private void parseActivity(Node activityNode, boolean isAlias,
      AndroidManifest androidManifest) {
    final List<IntentFilterData> intentFilterData = parseIntentFilters(activityNode);
    final MetaData metaData = new MetaData(getChildrenTags(activityNode, "meta-data"));
    final HashMap<String, String> activityAttrs = parseNodeAttributes(activityNode);

    String activityName = resolveClassRef(activityAttrs.get(ActivityData.getNameAttr("android")),
        androidManifest);
    if (activityName == null) {
      return;
    }
    ActivityData targetActivity = null;
    if (isAlias) {
      String targetName = resolveClassRef(activityAttrs.get(ActivityData.getTargetAttr("android")),
          androidManifest);
      if (activityName == null) {
        return;
      }
      // The target activity should have been parsed already so if it exists we should find it in
      // activityDatas.
      targetActivity = androidManifest.activityDatas.get(targetName);
      activityAttrs.put(ActivityData.getTargetAttr("android"), targetName);
    }
    activityAttrs.put(ActivityData.getNameAttr("android"), activityName);
    androidManifest.activityDatas.put(activityName, new ActivityData("android", activityAttrs, intentFilterData, targetActivity, metaData));
  }

  private List<IntentFilterData> parseIntentFilters(final Node activityNode) {
    ArrayList<IntentFilterData> intentFilterDatas = new ArrayList<>();
    for (Node n : getChildrenTags(activityNode, "intent-filter")) {
      ArrayList<String> actionNames = new ArrayList<>();
      ArrayList<String> categories = new ArrayList<>();
      //should only be one action.
      for (Node action : getChildrenTags(n, "action")) {
        NamedNodeMap attributes = action.getAttributes();
        Node actionNameNode = attributes.getNamedItem("android:name");
        if (actionNameNode != null) {
          actionNames.add(actionNameNode.getNodeValue());
        }
      }
      for (Node category : getChildrenTags(n, "category")) {
        NamedNodeMap attributes = category.getAttributes();
        Node categoryNameNode = attributes.getNamedItem("android:name");
        if (categoryNameNode != null) {
          categories.add(categoryNameNode.getNodeValue());
        }
      }
      IntentFilterData intentFilterData = new IntentFilterData(actionNames, categories);
      intentFilterData = parseIntentFilterData(n, intentFilterData);
      intentFilterDatas.add(intentFilterData);
    }

    return intentFilterDatas;
  }

  private IntentFilterData parseIntentFilterData(final Node intentFilterNode, IntentFilterData intentFilterData) {
    for (Node n : getChildrenTags(intentFilterNode, "data")) {
      NamedNodeMap attributes = n.getAttributes();
      String host = null;
      String port = null;

      Node schemeNode = attributes.getNamedItem("android:scheme");
      if (schemeNode != null) {
        intentFilterData.addScheme(schemeNode.getNodeValue());
      }

      Node hostNode = attributes.getNamedItem("android:host");
      if (hostNode != null) {
        host = hostNode.getNodeValue();
      }

      Node portNode = attributes.getNamedItem("android:port");
      if (portNode != null) {
        port = portNode.getNodeValue();
      }
      intentFilterData.addAuthority(host, port);

      Node pathNode = attributes.getNamedItem("android:path");
      if (pathNode != null) {
        intentFilterData.addPath(pathNode.getNodeValue());
      }

      Node pathPatternNode = attributes.getNamedItem("android:pathPattern");
      if (pathPatternNode != null) {
        intentFilterData.addPathPattern(pathPatternNode.getNodeValue());
      }

      Node pathPrefixNode = attributes.getNamedItem("android:pathPrefix");
      if (pathPrefixNode != null) {
        intentFilterData.addPathPrefix(pathPrefixNode.getNodeValue());
      }

      Node mimeTypeNode = attributes.getNamedItem("android:mimeType");
      if (mimeTypeNode != null) {
        intentFilterData.addMimeType(mimeTypeNode.getNodeValue());
      }
    }
    return intentFilterData;
  }

  static HashMap<String, String> parseNodeAttributes(Node node) {
    final NamedNodeMap attributes = node.getAttributes();
    final int attrCount = attributes.getLength();
    final HashMap<String, String> receiverAttrs = new HashMap<>(attributes.getLength());
    for (int i = 0; i < attrCount; i++) {
      Node attribute = attributes.item(i);
      String value = attribute.getNodeValue();
      if (value != null) {
        receiverAttrs.put(attribute.getNodeName(), value);
      }
    }
    return receiverAttrs;
  }

  static String getTagAttributeText(final Document doc, final String tag, final String attribute) {
    NodeList elementsByTagName = doc.getElementsByTagName(tag);
    for (int i = 0; i < elementsByTagName.getLength(); ++i) {
      Node item = elementsByTagName.item(i);
      Node namedItem = item.getAttributes().getNamedItem(attribute);
      if (namedItem != null) {
        return namedItem.getTextContent();
      }
    }
    return null;
  }
}
