package org.robolectric.shadows;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.ParcelFileDescriptor;
import android.util.AttributeSet;
import android.util.TypedValue;
import org.jetbrains.annotations.NotNull;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.AttributeResource;
import org.robolectric.res.DrawableResourceLoader;
import org.robolectric.res.EmptyStyle;
import org.robolectric.res.FileTypedResource;
import org.robolectric.res.FsFile;
import org.robolectric.res.ResName;
import org.robolectric.res.ResType;
import org.robolectric.res.ResourceIndex;
import org.robolectric.res.ResourceLoader;
import org.robolectric.res.Style;
import org.robolectric.res.StyleData;
import org.robolectric.res.StyleResolver;
import org.robolectric.res.ThemeStyleSet;
import org.robolectric.res.TypedResource;
import org.robolectric.res.builder.ResourceParser;
import org.robolectric.res.builder.XmlBlock;
import org.robolectric.res.builder.XmlResourceParserImpl;
import org.robolectric.util.Logger;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.RuntimeEnvironment.castNativePtr;
import static org.robolectric.Shadows.shadowOf;

/**
 * Shadow for {@link android.content.res.AssetManager}.
 */
@Implements(AssetManager.class)
public final class ShadowAssetManager {
  public static final int STYLE_NUM_ENTRIES = 6;
  public static final int STYLE_TYPE = 0;
  public static final int STYLE_DATA = 1;
  public static final int STYLE_ASSET_COOKIE = 2;
  public static final int STYLE_RESOURCE_ID = 3;
  public static final int STYLE_CHANGING_CONFIGURATIONS = 4;
  public static final int STYLE_DENSITY = 5;

  boolean strictErrors = false;

  private static long nextInternalThemeId = 1000;
  private static final Map<Long, ThemeInfo> themeInfos = new HashMap<>();
  private ResourceLoader resourceLoader;

  class ThemeInfo {
    private final Resources resources;
    private ThemeStyleSet themeStyleSet;

    public ThemeInfo(Resources resources, ThemeStyleSet themeStyleSet) {
      this.resources = resources;
      this.themeStyleSet = themeStyleSet;
    }
  }

  @RealObject
  AssetManager realObject;

  public void __constructor__() {
    resourceLoader = RuntimeEnvironment.getAppResourceLoader();
  }

  public void __constructor__(boolean isSystem) {
    resourceLoader = isSystem ? RuntimeEnvironment.getSystemResourceLoader() : RuntimeEnvironment.getAppResourceLoader();
  }

  public ResourceLoader getResourceLoader() {
    return resourceLoader;
  }

  @HiddenApi @Implementation
  public CharSequence getResourceText(int ident) {
    TypedResource value = getAndResolve(ident, RuntimeEnvironment.getQualifiers(), true);
    if (value == null) return null;
    return (CharSequence) value.getData();
  }

  @HiddenApi @Implementation
  public CharSequence getResourceBagText(int ident, int bagEntryId) {
    throw new UnsupportedOperationException(); // todo
  }

  @HiddenApi @Implementation
  public String[] getResourceStringArray(final int id) {
    CharSequence[] resourceTextArray = getResourceTextArray(id);
    if (resourceTextArray == null) return null;
    String[] strings = new String[resourceTextArray.length];
    for (int i = 0; i < strings.length; i++) {
      strings[i] = resourceTextArray[i].toString();
    }
    return strings;
  }

  @HiddenApi @Implementation
  public int getResourceIdentifier(String name, String defType, String defPackage) {
    ResName resName = ResName.qualifyResName(name, defPackage, defType);

    // ids are often declared from within layouts, so a special case since we don't collect those:
    if (!ResName.ID_TYPE.equals(resName.type)
        && !resourceLoader.hasValue(resName, RuntimeEnvironment.getQualifiers())) {
      return 0;
    }

    ResourceIndex resourceIndex = resourceLoader.getResourceIndex();
    Integer resourceId = resourceIndex.getResourceId(resName);
    return resourceId == null ? 0 : resourceId;
  }

  @HiddenApi @Implementation
  public boolean getResourceValue(int ident, int density, TypedValue outValue, boolean resolveRefs) {
    TypedResource value = getAndResolve(ident, RuntimeEnvironment.getQualifiers(), resolveRefs);
    if (value == null) return false;

    getConverter(value).fillTypedValue(value.getData(), outValue);
    return true;
  }

  private Converter getConverter(TypedResource value) {
    if (value instanceof FileTypedResource.Image
        || (value instanceof FileTypedResource
            && ((FileTypedResource) value).getFsFile().getName().endsWith(".xml"))) {
      return new Converter.FromFilePath();
    }
    return Converter.getConverter(value.getResType());
  }

  @HiddenApi @Implementation
  public CharSequence[] getResourceTextArray(int resId) {
    TypedResource value = getAndResolve(resId, RuntimeEnvironment.getQualifiers(), true);
    if (value == null) return null;
    TypedResource[] items = getConverter(value).getItems(value);
    CharSequence[] charSequences = new CharSequence[items.length];
    for (int i = 0; i < items.length; i++) {
      TypedResource typedResource = resolve(items[i], RuntimeEnvironment.getQualifiers(), resId);
      charSequences[i] = getConverter(typedResource).asCharSequence(typedResource);
    }
    return charSequences;
  }

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  public boolean getThemeValue(int themePtr, int ident, TypedValue outValue, boolean resolveRefs) {
    return getThemeValue((long) themePtr, ident, outValue, resolveRefs);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  public boolean getThemeValue(long themePtr, int ident, TypedValue outValue, boolean resolveRefs) {
    ResourceIndex resourceIndex = resourceLoader.getResourceIndex();
    ResName resName = resourceIndex.getResName(ident);

    ThemeStyleSet themeStyleSet = getThemeInfo(themePtr).themeStyleSet;
    AttributeResource attrValue = themeStyleSet.getAttrValue(resName);
    while(resolveRefs && attrValue != null && attrValue.isStyleReference()) {
      ResName attrResName = new ResName(attrValue.contextPackageName, "attr", attrValue.value.substring(1));
      attrValue = themeStyleSet.getAttrValue(attrResName);
    }
    if (attrValue != null) {
      Converter.convertAndFill(attrValue, outValue, resourceLoader, RuntimeEnvironment.getQualifiers(), resolveRefs);
      return true;
    }
    return false;
  }

  @HiddenApi @Implementation
  public void ensureStringBlocks() {
  }

  @Implementation
  public final InputStream open(String fileName) throws IOException {
    return ShadowApplication.getInstance().getAppManifest().getAssetsDirectory().join(fileName).getInputStream();
  }

  @Implementation
  public final InputStream open(String fileName, int accessMode) throws IOException {
    return ShadowApplication.getInstance().getAppManifest().getAssetsDirectory().join(fileName).getInputStream();
  }

  @Implementation
  public final AssetFileDescriptor openFd(String fileName) throws IOException {
    File file = new File(ShadowApplication.getInstance().getAppManifest().getAssetsDirectory().join(fileName).getPath());
    ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
    return new AssetFileDescriptor(parcelFileDescriptor, 0, file.length());
  }

  @Implementation
  public final String[] list(String path) throws IOException {
    FsFile file = ShadowApplication.getInstance().getAppManifest().getAssetsDirectory().join(path);
    if (file.isDirectory()) {
      return file.listFileNames();
    }
    return new String[0];
  }

  @HiddenApi @Implementation
  public final InputStream openNonAsset(int cookie, String fileName, int accessMode) throws IOException {
    final ResName resName = qualifyFromNonAssetFileName(fileName);
    final FileTypedResource typedResource =
        (FileTypedResource) resourceLoader.getValue(resName, RuntimeEnvironment.getQualifiers());

    if (typedResource == null) {
      throw new IOException("Unable to find resource for " + fileName);
    }

    if (accessMode == AssetManager.ACCESS_STREAMING) {
      return typedResource.getFsFile().getInputStream();
    } else {
      return new ByteArrayInputStream(typedResource.getFsFile().getBytes());
    }
  }

  private ResName qualifyFromNonAssetFileName(String fileName) {
    if (fileName.startsWith("jar:")) {
      // Must remove "jar:" prefix, or else qualifyFromFilePath fails on Windows
      return ResName.qualifyFromFilePath("android", fileName.replaceFirst("jar:", ""));
    } else {
      return ResName.qualifyFromFilePath(ShadowApplication.getInstance().getAppManifest().getPackageName(), fileName);
    }
  }

  @HiddenApi @Implementation
  public final AssetFileDescriptor openNonAssetFd(int cookie, String fileName) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Implementation
  public final XmlResourceParser openXmlResourceParser(int cookie, String fileName) throws IOException {
    return ResourceParser.create(fileName, "fixme", "fixme", null);
  }

  public XmlResourceParser loadXmlResourceParser(int resId, String type) throws Resources.NotFoundException {
    ResName resName = getResName(resId);
    ResName resolvedResName = resolveResName(resName, RuntimeEnvironment.getQualifiers());
    if (resolvedResName == null) {
      throw new RuntimeException("couldn't resolve " + resName.getFullyQualifiedName());
    }
    resName = resolvedResName;

    XmlBlock block = getResourceLoader().getXml(resName, RuntimeEnvironment.getQualifiers());
    if (block == null) {
      throw new Resources.NotFoundException(resName.getFullyQualifiedName());
    }
    return ResourceParser.from(block, resName.packageName, getResourceLoader());
  }

  @HiddenApi @Implementation
  public int addAssetPath(String path) {
    return 1;
  }

  @HiddenApi @Implementation
  public boolean isUpToDate() {
    return true;
  }

  @HiddenApi @Implementation
  public void setLocale(String locale) {
  }

  @Implementation
  public String[] getLocales() {
    return new String[0]; // todo
  }

  @HiddenApi @Implementation
  public void setConfiguration(int mcc, int mnc, String locale,
                 int orientation, int touchscreen, int density, int keyboard,
                 int keyboardHidden, int navigation, int screenWidth, int screenHeight,
                 int smallestScreenWidthDp, int screenWidthDp, int screenHeightDp,
                 int screenLayout, int uiMode, int majorVersion) {
  }

  @HiddenApi @Implementation
  public int[] getArrayIntResource(int resId) {
    TypedResource value = getAndResolve(resId, RuntimeEnvironment.getQualifiers(), true);
    if (value == null) return null;
    TypedResource[] items = getConverter(value).getItems(value);
    int[] ints = new int[items.length];
    for (int i = 0; i < items.length; i++) {
      TypedResource typedResource = resolve(items[i], RuntimeEnvironment.getQualifiers(), resId);
      ints[i] = getConverter(typedResource).asInt(typedResource);
    }
    return ints;
  }

  @HiddenApi @Implementation
  public Number createTheme() {
    synchronized (themeInfos) {
      return castNativePtr(nextInternalThemeId++);
    }
  }

  public static void saveTheme(Number themePtr, Resources resources, ThemeStyleSet theme) {
    synchronized (themeInfos) {
      themeInfos.put(themePtr.longValue(), shadowOf(theme).getThemeStyleSet());
    }
  }

  private static ThemeInfo getThemeInfo(long themePtr) {
    ThemeInfo themeInfo;
    synchronized (themeInfos) {
      themeInfo = themeInfos.get(themePtr);
    }
    if (themeInfo == null) {
      throw new RuntimeException("no theme " + themePtr + " found in AssetManager");
    }
    return themeInfo;
  }

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  public void releaseTheme(int themePtr) {
    themeInfos.remove(themePtr);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  public void releaseTheme(long themePtr) {
    synchronized (themeInfos) {
      themeInfos.remove(themePtr);
    }
  }

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  public static void applyThemeStyle(int themePtr, int styleRes, boolean force) {
    applyThemeStyle((long) themePtr, styleRes, force);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  public static void applyThemeStyle(long themePtr, int styleRes, boolean force) {
    ThemeInfo themeInfo = getThemeInfo(themePtr);
    Style style = shadowOf(themeInfo.resources.getAssets()).resolveStyle(styleRes, null);
    themeInfo.themeStyleSet.apply(style, force);
}

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  public static void copyTheme(int destPtr, int sourcePtr) {
    copyTheme((long) destPtr, (long) sourcePtr);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  public static void copyTheme(long destPtr, long sourcePtr) {
    ThemeInfo destThemeInfo = getThemeInfo(destPtr);
    ThemeInfo sourceThemeInfo = getThemeInfo(sourcePtr);
    destThemeInfo.themeStyleSet = sourceThemeInfo.themeStyleSet.copy();
  }

  /////////////////////////

  Style resolveStyle(int resId, Style themeStyleSet) {
    return resolveStyle(getResName(resId), themeStyleSet);
  }

  Style resolveStyle(@NotNull ResName themeStyleName, Style themeStyleSet) {
    TypedResource themeStyleResource = resourceLoader.getValue(themeStyleName, RuntimeEnvironment.getQualifiers());
    if (themeStyleResource == null) return null;
    StyleData themeStyleData = (StyleData) themeStyleResource.getData();
    if (themeStyleSet == null) {
      themeStyleSet = new ThemeStyleSet();
    }
    return new StyleResolver(resourceLoader, themeStyleData, themeStyleSet, themeStyleName, RuntimeEnvironment.getQualifiers());
  }

  private TypedResource getAndResolve(int resId, String qualifiers, boolean resolveRefs) {
    TypedResource value = resourceLoader.getValue(resId, qualifiers);
    if (resolveRefs) {
      value = resolve(value, qualifiers, resId);
    }

    // todo: make the drawable loader put stuff into the normal spot...
    String resourceTypeName = getResourceTypeName(resId);
    if (value == null && DrawableResourceLoader.isStillHandledHere(resourceTypeName)) {
      FileTypedResource typedResource = (FileTypedResource) resourceLoader.getValue(resId, qualifiers);
      return new TypedResource<>(typedResource.getFsFile(), ResType.FILE);
    }

    // todo: gross. this is so resources.getString(R.layout.foo) works for ABS.
    if (value == null && "layout".equals(resourceTypeName)) {
      throw new UnsupportedOperationException("ugh, this doesn't work still?");
    }

    return value;
  }

  TypedResource resolve(TypedResource value, String qualifiers, int resId) {
    return resolveResourceValue(value, qualifiers, resId);
  }

  public ResName resolveResName(ResName resName, String qualifiers) {
    TypedResource value = getResourceLoader().getValue(resName, qualifiers);
    return resolveResource(value, qualifiers, resName);
  }

  // todo: DRY up #resolveResource vs #resolveResourceValue
  private ResName resolveResource(TypedResource value, String qualifiers, ResName resName) {
    while (value != null && value.isReference()) {
      String s = value.asString();
      if (AttributeResource.isNull(s) || AttributeResource.isEmpty(s)) {
        value = null;
      } else {
        String refStr = s.substring(1).replace("+", "");
        resName = ResName.qualifyResName(refStr, resName);
        value = getResourceLoader().getValue(resName, qualifiers);
      }
    }

    return resName;
  }

  private TypedResource resolveResourceValue(TypedResource value, String qualifiers, ResName resName) {
    while (value != null && value.isReference()) {
      String s = value.asString();
      if (AttributeResource.isNull(s) || AttributeResource.isEmpty(s)) {
        value = null;
      } else {
        String refStr = s.substring(1).replace("+", "");
        resName = ResName.qualifyResName(refStr, resName);
        value = getResourceLoader().getValue(resName, qualifiers);
      }
    }

    return value;
  }

  public TypedResource resolveResourceValue(TypedResource value, String qualifiers, int resId) {
    ResName resName = getResName(resId);
    return resolveResourceValue(value, qualifiers, resName);
  }

  private AttributeResource buildAttribute(AttributeSet set, int resId, int defStyleAttr, Style themeStyleSet, int defStyleRes) {
    /*
     * When determining the final value of a particular attribute, there are four inputs that come into play:
     *
     * 1. Any attribute values in the given AttributeSet.
     * 2. The style resource specified in the AttributeSet (named "style").
     * 3. The default style specified by defStyleAttr and defStyleRes
     * 4. The base values in this theme.
     */
    Style defStyleFromAttr = null;
    Style defStyleFromRes = null;
    Style styleAttrStyle = null;

    if (defStyleAttr != 0) {
      // Load the theme attribute for the default style attributes. E.g., attr/buttonStyle
      ResName defStyleName = getResName(defStyleAttr);

      // Load the style for the default style attribute. E.g. "@style/Widget.Robolectric.Button";
      AttributeResource defStyleAttribute = themeStyleSet.getAttrValue(defStyleName);
      if (defStyleAttribute != null) {
        while (defStyleAttribute.isStyleReference()) {
          AttributeResource other = themeStyleSet.getAttrValue(defStyleAttribute.getStyleReference());
          if (other == null) {
            throw new RuntimeException("couldn't dereference " + defStyleAttribute);
          }
          defStyleAttribute = other;
        }

        if (defStyleAttribute.isResourceReference()) {
          ResName defStyleResName = defStyleAttribute.getResourceReference();
          defStyleFromAttr = resolveStyle(defStyleResName, themeStyleSet);
        }
      }
    }

    if (set != null && set.getStyleAttribute() != 0) {
      ResName styleAttributeResName = getResName(set.getStyleAttribute());
      while (styleAttributeResName.type.equals("attr")) {
        AttributeResource attrValue = themeStyleSet.getAttrValue(styleAttributeResName);
        if (attrValue == null) {
          throw new RuntimeException(
                  "no value for " + styleAttributeResName.getFullyQualifiedName()
                      + " in " + themeStyleSet);
        }
        if (attrValue.isResourceReference()) {
          styleAttributeResName = attrValue.getResourceReference();
        } else if (attrValue.isStyleReference()) {
          styleAttributeResName = attrValue.getStyleReference();
        }
      }
      styleAttrStyle = resolveStyle(styleAttributeResName, themeStyleSet);
    }

    if (defStyleRes != 0) {
      ResName resName = getResName(defStyleRes);
      if (resName.type.equals("attr")) {
        AttributeResource attributeValue = findAttributeValue(defStyleRes, set, styleAttrStyle, defStyleFromAttr, defStyleFromAttr, themeStyleSet);
        if (attributeValue != null) {
          if (attributeValue.isStyleReference()) {
            resName = themeStyleSet.getAttrValue(attributeValue.getStyleReference()).getResourceReference();
          } else if (attributeValue.isResourceReference()) {
            resName = attributeValue.getResourceReference();
          }
        }
      }
      defStyleFromRes = resolveStyle(resName, themeStyleSet);
    }

    AttributeResource attribute = findAttributeValue(resId, set, styleAttrStyle, defStyleFromAttr, defStyleFromRes, themeStyleSet);
    while (attribute != null && attribute.isStyleReference()) {
      ResName otherAttrName = attribute.getStyleReference();
      if (attribute.resName.equals(otherAttrName)) {
        Logger.info("huh... circular reference for %s?", attribute.resName.getFullyQualifiedName());
        return null;
      }
      ResName resName = resourceLoader.getResourceIndex().getResName(resId);

      AttributeResource otherAttr = themeStyleSet.getAttrValue(otherAttrName);
      if (otherAttr == null) {
        strictError("no such attr %s in %s while resolving value for %s", attribute.value, themeStyleSet, resName.getFullyQualifiedName());
        attribute = null;
      } else {
        attribute = new AttributeResource(resName, otherAttr.value, otherAttr.contextPackageName);
      }
    }

    return attribute;
  }

  private void strictError(String message, Object... args) {
    if (strictErrors) {
      throw new RuntimeException(String.format(message, args));
    } else {
      Logger.strict(message, args);
    }
  }

  TypedArray attrsToTypedArray(Resources resources, AttributeSet set, int[] attrs, int defStyleAttr, Resources.Theme theme, int defStyleRes) {
    CharSequence[] stringData = new CharSequence[attrs.length];
    int[] data = new int[attrs.length * ShadowAssetManager.STYLE_NUM_ENTRIES];
    int[] indices = new int[attrs.length + 1];
    int nextIndex = 0;

    Style themeStyleSet = theme == null
        ? new EmptyStyle()
        : shadowOf(theme).getThemeStyleSet();

    for (int i = 0; i < attrs.length; i++) {
      int offset = i * ShadowAssetManager.STYLE_NUM_ENTRIES;

      AttributeResource attribute = buildAttribute(set, attrs[i], defStyleAttr, themeStyleSet, defStyleRes);
      if (attribute != null && !attribute.isNull()) {
        TypedValue typedValue = new TypedValue();
        // If there is an AttributeSet then use the resource loader that the attribute set was created with.
        ResourceLoader resourceLoader = set != null && set instanceof XmlResourceParserImpl
            ? ((XmlResourceParserImpl) set).getResourceLoader()
            : this.resourceLoader;
        Converter.convertAndFill(attribute, typedValue, resourceLoader, RuntimeEnvironment.getQualifiers(), true);
        //noinspection PointlessArithmeticExpression
        data[offset + ShadowAssetManager.STYLE_TYPE] = typedValue.type;
        data[offset + ShadowAssetManager.STYLE_DATA] = typedValue.type == TypedValue.TYPE_STRING ? i : typedValue.data;
        data[offset + ShadowAssetManager.STYLE_ASSET_COOKIE] = typedValue.assetCookie;
        data[offset + ShadowAssetManager.STYLE_RESOURCE_ID] = typedValue.resourceId;
        data[offset + ShadowAssetManager.STYLE_CHANGING_CONFIGURATIONS] = typedValue.changingConfigurations;
        data[offset + ShadowAssetManager.STYLE_DENSITY] = typedValue.density;
        stringData[i] = typedValue.string;

        indices[nextIndex + 1] = i;
        nextIndex++;
      }
    }

    indices[0] = nextIndex;

    TypedArray typedArray = ShadowTypedArray.create(resources, attrs, data, indices, nextIndex, stringData);
    if (set != null) {
      shadowOf(typedArray).positionDescription = set.getPositionDescription();
    }
    return typedArray;
  }

  private AttributeResource findAttributeValue(int resId, AttributeSet attributeSet, Style styleAttrStyle, Style defStyleFromAttr, Style defStyleFromRes, @NotNull Style themeStyleSet) {
    if (attributeSet != null) {
      for (int i = 0; i < attributeSet.getAttributeCount(); i++) {
        if (attributeSet.getAttributeNameResource(i) == resId && attributeSet.getAttributeValue(i) != null) {
          ResName resName = resourceLoader.getResourceIndex().getResName(resId);

          if (resName == null) {
            // We're looking for an attribute that doesn't yet exist at this SDK level just pass it back blindly and
            // assume the user knows what they're doing with it.
            resName = ResName.qualifyResName(attributeSet.getAttributeName(i), null, "attr");
          }
          return new AttributeResource(resName, attributeSet.getAttributeValue(i), "fixme!!!");
        }
      }
    }

    ResName attrName = resourceLoader.getResourceIndex().getResName(resId);
    if (attrName == null) return null;

    if (styleAttrStyle != null) {
      AttributeResource attribute = styleAttrStyle.getAttrValue(attrName);
      if (attribute != null) {
        return attribute;
      }
    }

    // else if attr in defStyleFromAttr, use its value
    if (defStyleFromAttr != null) {
      AttributeResource attribute = defStyleFromAttr.getAttrValue(attrName);
      if (attribute != null) {
        return attribute;
      }
    }

    if (defStyleFromRes != null) {
      AttributeResource attribute = defStyleFromRes.getAttrValue(attrName);
      if (attribute != null) {
        return attribute;
      }
    }

    // else if attr in theme, use its value
    return themeStyleSet.getAttrValue(attrName);
  }

  @NotNull private ResName getResName(int id) {
    ResName resName = resourceLoader.getResourceIndex().getResName(id);
    if (resName == null) {
      List<String> packages = new ArrayList<>(resourceLoader.getResourceIndex().getPackages());
      Collections.sort(packages);
      throw new Resources.NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(id)
          + " in packages " + packages);
    }
    return resName;
  }

  @Implementation
  public String getResourceName(int resid) {
    return getResName(resid).getFullyQualifiedName();
  }

  @Implementation
  public String getResourcePackageName(int resid) {
    return getResName(resid).packageName;
  }

  @Implementation
  public String getResourceTypeName(int resid) {
    return getResName(resid).type;
  }

  @Implementation
  public String getResourceEntryName(int resid) {
   return getResName(resid).name;
  }
}
