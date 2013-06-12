package org.robolectric.shadows;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.util.TypedValue;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.robolectric.AndroidManifest;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.HiddenApi;
import org.robolectric.res.Attribute;
import org.robolectric.res.DrawableNode;
import org.robolectric.res.DrawableResourceLoader;
import org.robolectric.res.FsFile;
import org.robolectric.res.ResName;
import org.robolectric.res.ResType;
import org.robolectric.res.ResourceIndex;
import org.robolectric.res.ResourceLoader;
import org.robolectric.res.Style;
import org.robolectric.res.StyleData;
import org.robolectric.res.TypedResource;
import org.robolectric.res.builder.XmlFileBuilder;

import static org.robolectric.Robolectric.shadowOf;

@SuppressWarnings({"UnusedDeclaration"})
@Implements(AssetManager.class)
public final class ShadowAssetManager {
  // taken from AssetManager:
  public static final int STYLE_NUM_ENTRIES = 6;
  public static final int STYLE_TYPE = 0;
  public static final int STYLE_DATA = 1;
  public static final int STYLE_ASSET_COOKIE = 2;
  public static final int STYLE_RESOURCE_ID = 3;
  public static final int STYLE_CHANGING_CONFIGURATIONS = 4;
  public static final int STYLE_DENSITY = 5;

  private String qualifiers = "";
  private Map<Integer, Resources.Theme> themesById = new LinkedHashMap<Integer, Resources.Theme>();
  private int nextInternalThemeId = 1000;

  static AssetManager bind(AssetManager assetManager, AndroidManifest androidManifest, ResourceLoader resourceLoader) {
    ShadowAssetManager shadowAssetManager = shadowOf(assetManager);
    if (shadowAssetManager.appManifest != null) throw new RuntimeException("ResourceLoader already set!");
    shadowAssetManager.appManifest = androidManifest;
    shadowAssetManager.resourceLoader = resourceLoader;
    return assetManager;
  }

  private AndroidManifest appManifest;
  private ResourceLoader resourceLoader;

  @HiddenApi @Implementation
  public CharSequence getResourceText(int ident) {
    TypedResource value = getAndResolve(ident, getQualifiers(), true);
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
  public boolean getResourceValue(int ident, int density, TypedValue outValue, boolean resolveRefs) {
    TypedResource value = getAndResolve(ident, getQualifiers(), resolveRefs);
    if (value == null) return false;

    getConverter(value).fillTypedValue(value.getData(), outValue);
    return true;
  }

  private Converter getConverter(TypedResource value) {
    return Converter.getConverter(value.getResType());
  }

  @HiddenApi @Implementation
  public CharSequence[] getResourceTextArray(final int id) {
    ResName resName = resourceLoader.getResourceIndex().getResName(id);
    if (resName == null) throw new Resources.NotFoundException("unknown resource " + id);
    TypedResource value = getAndResolve(resName, getQualifiers(), true);
    if (value == null) return null;
    TypedResource[] items = getConverter(value).getItems(value);
    CharSequence[] charSequences = new CharSequence[items.length];
    for (int i = 0; i < items.length; i++) {
      TypedResource typedResource = resolve(items[i], getQualifiers(), resName);
      charSequences[i] = getConverter(typedResource).asCharSequence(typedResource);
    }
    return charSequences;
  }

  @HiddenApi @Implementation
  public boolean getThemeValue(int theme, int ident, TypedValue outValue, boolean resolveRefs) {
    ResourceIndex resourceIndex = resourceLoader.getResourceIndex();
    ResName resName = resourceIndex.getResName(ident);
    Resources.Theme theTheme = getThemeByInternalId(theme);
    // Load the style for the theme we represent. E.g. "@style/Theme.Robolectric"
    ResName themeStyleName = resourceIndex.getResName(shadowOf(theTheme).getStyleResourceId());
    if (themeStyleName == null) return false; // is this right?

    Style themeStyle = resolveStyle(resourceLoader, themeStyleName, getQualifiers());

    //// Load the theme attribute for the default style attributes. E.g., attr/buttonStyle
    //ResName defStyleName = resourceLoader.getResourceIndex().getResName(ident);
    //
    //// Load the style for the default style attribute. E.g. "@style/Widget.Robolectric.Button";
    //String defStyleNameValue = themeStyle.getAttrValue(defStyleName);
    //ResName defStyleResName = new ResName(defStyleName.packageName, "style", defStyleName.name);
    //Style style = resolveStyle(resourceLoader, defStyleResName, getQualifiers());
    if (themeStyle != null) {
      Attribute attrValue = themeStyle.getAttrValue(resName);
      if (attrValue == null) {
        System.out.println("Couldn't find " + resName + " in " + themeStyleName);
      } else {
        TypedResource attrDataValue = resourceLoader.getValue(resName, getQualifiers());
        Converter.convertAndFill(attrValue, outValue, resourceLoader, getQualifiers());
        return true;
      }
    }
    return false;
  }

  @HiddenApi @Implementation
  public void ensureStringBlocks() {
  }

  @Implementation
  public final InputStream open(String fileName) throws IOException {
    return appManifest.getAssetsDirectory().join(fileName).getInputStream();
  }

  @Implementation
  public final String[] list(String path) throws IOException {
    FsFile file = appManifest.getAssetsDirectory().join(path);
    if (file.isDirectory()) {
      return file.listFileNames();
    }
    return new String[0];
  }

  @HiddenApi @Implementation
  public final InputStream openNonAsset(int cookie, String fileName, int accessMode) {
//        ResName resName = new ResName(fileName);
//        resourceLoader.getDrawableNode(resName)
    return new ByteArrayInputStream(fileName.getBytes()); // todo: something better
  }

  @HiddenApi @Implementation
  public final AssetFileDescriptor openNonAssetFd(int cookie, String fileName) throws IOException {
    throw new UnsupportedOperationException();
  }

  @Implementation
  public final XmlResourceParser openXmlResourceParser(int cookie, String fileName) throws IOException {
    return XmlFileBuilder.getXmlResourceParser(fileName, "fixme", null);
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
  public int[] getArrayIntResource(int arrayRes) {
    ResName resName = resourceLoader.getResourceIndex().getResName(arrayRes);
    if (resName == null) throw new Resources.NotFoundException("unknown resource " + arrayRes);
    TypedResource value = getAndResolve(resName, getQualifiers(), true);
    if (value == null) return null;
    TypedResource[] items = getConverter(value).getItems(value);
    int[] ints = new int[items.length];
    for (int i = 0; i < items.length; i++) {
      TypedResource typedResource = resolve(items[i], getQualifiers(), resName);
      ints[i] = getConverter(typedResource).asInt(typedResource);
    }
    return ints;
  }

  @HiddenApi @Implementation
  synchronized public int createTheme() {
    return nextInternalThemeId++;
  }

  @HiddenApi @Implementation
  synchronized public void releaseTheme(int theme) {
    themesById.remove(theme);
  }

  @HiddenApi @Implementation
  public static void applyThemeStyle(int theme, int styleRes, boolean force) {
    throw new UnsupportedOperationException();
  }

  @HiddenApi @Implementation
  public static void copyTheme(int dest, int source) {
    throw new UnsupportedOperationException();
  }

  /////////////////////////

  synchronized public void setTheme(int internalThemeId, Resources.Theme theme) {
    themesById.put(internalThemeId, theme);
  }

  synchronized private Resources.Theme getThemeByInternalId(int internalThemeId) {
    return themesById.get(internalThemeId);
  }

  static Style resolveStyle(ResourceLoader resourceLoader, @NotNull ResName themeStyleName, String qualifiers) {
    TypedResource themeStyleResource = resourceLoader.getValue(themeStyleName, qualifiers);
    if (themeStyleResource == null) return null;
    StyleData themeStyleData = (StyleData) themeStyleResource.getData();
    return new StyleResolver(resourceLoader, themeStyleData, themeStyleName, qualifiers);
  }

  TypedResource getAndResolve(int resId, String qualifiers, boolean resolveRefs) {
    ResName resName = resourceLoader.getResourceIndex().getResName(resId);
    if (resName == null) throw new Resources.NotFoundException("unknown resource " + resId);
    return getAndResolve(resName, qualifiers, resolveRefs);
  }

  TypedResource getAndResolve(@NotNull ResName resName, String qualifiers, boolean resolveRefs) {
    TypedResource value = resourceLoader.getValue(resName, qualifiers);
    if (resolveRefs) {
      value = resolve(value, qualifiers, resName);
    }

    // todo: make the drawable loader put stuff into the normal spot...
    if (value == null && DrawableResourceLoader.isStillHandledHere(resName)) {
      DrawableNode drawableNode = resourceLoader.getDrawableNode(resName, qualifiers);
      return new TypedResource<FsFile>(drawableNode.getFsFile(), ResType.FILE);
    }

    // todo: gross. this is so resources.getString(R.layout.foo) works for ABS.
    if (value == null && "layout".equals(resName.type)) {
      throw new UnsupportedOperationException("ugh, this doesn't work still?");
    }

    return value;
  }

  TypedResource resolve(TypedResource value, String qualifiers, ResName contextResName) {
    while (true) {
      if (value == null) return null;

      Object data = value.getData();
      if (data instanceof String) {
        String s = (String) data;
        if (s.equals("@null")) {
          return null;
        } else if (s.startsWith("@")) {
          String refStr = s.substring(1).replace("+", "");
          contextResName = ResName.qualifyResName(refStr, contextResName);
          value = resourceLoader.getValue(contextResName, qualifiers);
          // back through...
        } else {
          return value;
        }
      } else {
        return value;
      }
    }
  }

  public FsFile getAssetsDirectory() {
    return appManifest.getAssetsDirectory();
  }

  public String getQualifiers() {
    return qualifiers;
  }

  public void setQualifiers(String qualifiers) {
    this.qualifiers = qualifiers;
  }

  static class StyleResolver implements Style {
    private final ResourceLoader resourceLoader;
    private final StyleData leafStyle;
    private final ResName myResName;
    private final String qualifiers;

    public StyleResolver(ResourceLoader resourceLoader, StyleData styleData, ResName myResName, String qualifiers) {
      this.resourceLoader = resourceLoader;
      this.leafStyle = styleData;
      this.myResName = myResName;
      this.qualifiers = qualifiers;
    }

    @Override public Attribute getAttrValue(ResName resName) {
      resName.mustBe("attr");
      StyleData currentStyle = leafStyle;
      while (currentStyle != null) {
        Attribute value = currentStyle.getAttrValue(resName);
        if (value != null) return value;
        currentStyle = getParent(currentStyle);
      }
      return null;
    }

    private StyleData getParent(StyleData currentStyle) {
      String parent = currentStyle.getParent();

      if (parent == null || parent.isEmpty()) return null;

      if (parent.startsWith("@")) parent = parent.substring(1);

      ResName style = ResName.qualifyResName(parent, currentStyle.getPackageName(), "style");
      TypedResource typedResource = resourceLoader.getValue(style, qualifiers);
      if (typedResource == null) {
        throw new RuntimeException("huh? can't find parent for " + currentStyle);
      }
      return (StyleData) typedResource.getData();
    }
  }
}
