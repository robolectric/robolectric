package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.robolectric.RuntimeEnvironment.castNativePtr;
import static org.robolectric.Shadows.shadowOf;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.Build.VERSION_CODES;
import android.os.ParcelFileDescriptor;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import com.google.common.collect.Ordering;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.annotation.Nonnull;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.XmlResourceParserImpl;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.AttrData;
import org.robolectric.res.AttributeResource;
import org.robolectric.res.EmptyStyle;
import org.robolectric.res.FileTypedResource;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.res.ResName;
import org.robolectric.res.ResType;
import org.robolectric.res.ResourceIds;
import org.robolectric.res.ResourceTable;
import org.robolectric.res.Style;
import org.robolectric.res.StyleData;
import org.robolectric.res.StyleResolver;
import org.robolectric.res.ThemeStyleSet;
import org.robolectric.res.TypedResource;
import org.robolectric.res.android.ResTable_config;
import org.robolectric.res.builder.XmlBlock;
import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;

@Implements(AssetManager.class)
public final class ShadowAssetManager {

  public static final int STYLE_NUM_ENTRIES = 6;
  public static final int STYLE_TYPE = 0;
  public static final int STYLE_DATA = 1;
  public static final int STYLE_ASSET_COOKIE = 2;
  public static final int STYLE_RESOURCE_ID = 3;
  public static final int STYLE_CHANGING_CONFIGURATIONS = 4;
  public static final int STYLE_DENSITY = 5;

  public static final Ordering<String> ATTRIBUTE_TYPE_PRECIDENCE =
      Ordering.explicit(
          "reference",
          "color",
          "boolean",
          "integer",
          "fraction",
          "dimension",
          "float",
          "enum",
          "flag",
          "string");

  boolean strictErrors = false;

  private static long nextInternalThemeId = 1000;
  private static final Map<Long, NativeTheme> nativeThemes = new HashMap<>();
  private ResourceTable resourceTable;

  ResTable_config config = new ResTable_config();

  class NativeTheme {
    private ThemeStyleSet themeStyleSet;

    public NativeTheme(ThemeStyleSet themeStyleSet) {
      this.themeStyleSet = themeStyleSet;
    }

    public ShadowAssetManager getShadowAssetManager() {
      return ShadowAssetManager.this;
    }
  }

  @RealObject
  AssetManager realObject;

  private void convertAndFill(AttributeResource attribute, TypedValue outValue, ResTable_config config, boolean resolveRefs) {
    if (attribute.isNull()) {
      outValue.type = TypedValue.TYPE_NULL;
      outValue.data = TypedValue.DATA_NULL_UNDEFINED;
      return;
    } else if (attribute.isEmpty()) {
      outValue.type = TypedValue.TYPE_NULL;
      outValue.data = TypedValue.DATA_NULL_EMPTY;
      return;
    }

    // short-circuit Android caching of loaded resources cuz our string positions don't remain stable...
    outValue.assetCookie = Converter.getNextStringCookie();

    // TODO: Handle resource and style references
    if (attribute.isStyleReference()) {
      return;
    }

    while (attribute.isResourceReference()) {
      Integer resourceId;
      ResName resName = attribute.getResourceReference();
      if (attribute.getReferenceResId() != null) {
        resourceId = attribute.getReferenceResId();
      } else {
        resourceId = resourceTable.getResourceId(resName);
      }

      if (resourceId == null) {
        throw new Resources.NotFoundException("unknown resource " + resName);
      }
      outValue.type = TypedValue.TYPE_REFERENCE;
      if (!resolveRefs) {
          // Just return the resourceId if resolveRefs is false.
          outValue.data = resourceId;
          return;
      }

      outValue.resourceId = resourceId;

      TypedResource dereferencedRef = resourceTable.getValue(resName, config);
      if (dereferencedRef == null) {
        Logger.strict("couldn't resolve %s from %s", resName.getFullyQualifiedName(), attribute);
        return;
      } else {
        if (dereferencedRef.isFile()) {
          outValue.type = TypedValue.TYPE_STRING;
          outValue.data = 0;
          outValue.assetCookie = Converter.getNextStringCookie();
          outValue.string = dereferencedRef.asString();
          return;
        } else if (dereferencedRef.getData() instanceof String) {
          attribute = new AttributeResource(attribute.resName, dereferencedRef.asString(), resName.packageName);
          if (attribute.isResourceReference()) {
            continue;
          }
          if (resolveRefs) {
            Converter.getConverter(dereferencedRef.getResType()).fillTypedValue(attribute.value, outValue);
            return;
          }
        }
      }
      break;
    }

    if (attribute.isNull()) {
      outValue.type = TypedValue.TYPE_NULL;
      return;
    }

    TypedResource attrTypeData = resourceTable.getValue(attribute.resName, config);
    if (attrTypeData != null) {
      AttrData attrData = (AttrData) attrTypeData.getData();
      String format = attrData.getFormat();
      String[] types = format.split("\\|");
      Arrays.sort(types, ATTRIBUTE_TYPE_PRECIDENCE);
      for (String type : types) {
        if ("reference".equals(type)) continue; // already handled above
        Converter converter = Converter.getConverterFor(attrData, type);

        if (converter != null) {
          if (converter.fillTypedValue(attribute.value, outValue)) {
            return;
          }
        }
      }
    } else {
      /**
       * In cases where the runtime framework doesn't know this attribute, e.g: viewportHeight (added in 21) on a
       * KitKat runtine, then infer the attribute type from the value.
       *
       * TODO: When we are able to pass the SDK resources from the build environment then we can remove this
       * and replace the NullResourceLoader with simple ResourceProvider that only parses attribute type information.
       */
      ResType resType = ResType.inferFromValue(attribute.value);
      Converter.getConverter(resType).fillTypedValue(attribute.value, outValue);
    }
  }

  @Implementation
  public void __constructor__() {
    resourceTable = RuntimeEnvironment.getAppResourceTable();
  }

  @Implementation
  public void __constructor__(boolean isSystem) {
    resourceTable = isSystem ? RuntimeEnvironment.getSystemResourceTable() : RuntimeEnvironment.getAppResourceTable();
  }

  public ResourceTable getResourceTable() {
    return resourceTable;
  }

  @HiddenApi @Implementation
  public CharSequence getResourceText(int ident) {
    TypedResource value = getAndResolve(ident, config, true);
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
    Integer resourceId = resourceTable.getResourceId(ResName.qualifyResName(name, defPackage, defType));
    return resourceId == null ? 0 : resourceId;
  }

  @HiddenApi @Implementation
  public boolean getResourceValue(int ident, int density, TypedValue outValue, boolean resolveRefs) {
    TypedResource value = getAndResolve(ident, config, resolveRefs);
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
    TypedResource value = getAndResolve(resId, config, true);
    if (value == null) return null;
    List<TypedResource> items = getConverter(value).getItems(value);
    CharSequence[] charSequences = new CharSequence[items.size()];
    for (int i = 0; i < items.size(); i++) {
      TypedResource typedResource = resolve(items.get(i), config, resId);
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
    ResName resName = resourceTable.getResName(ident);

    ThemeStyleSet themeStyleSet = getNativeTheme(themePtr).themeStyleSet;
    AttributeResource attrValue = themeStyleSet.getAttrValue(resName);
    while(attrValue != null && attrValue.isStyleReference()) {
      ResName attrResName = attrValue.getStyleReference();
      if (attrValue.resName.equals(attrResName)) {
          Logger.info("huh... circular reference for %s?", attrResName.getFullyQualifiedName());
          return false;
      }
      attrValue = themeStyleSet.getAttrValue(attrResName);
    }
    if (attrValue != null) {
      convertAndFill(attrValue, outValue, config, resolveRefs);
      return true;
    }
    return false;
  }

  @HiddenApi @Implementation
  public void ensureStringBlocks() {
  }

  @Implementation
  public final InputStream open(String fileName) throws IOException {
    return findAssetFile(fileName).getInputStream();
  }

  @Implementation
  public final InputStream open(String fileName, int accessMode) throws IOException {
    return findAssetFile(fileName).getInputStream();
  }

  @Implementation
  public final AssetFileDescriptor openFd(String fileName) throws IOException {
    File file = new File(findAssetFile(fileName).getPath());
    if (file.getPath().startsWith("jar")) {
      file = getFileFromZip(file);
    }
    ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
    return new AssetFileDescriptor(parcelFileDescriptor, 0, file.length());
  }

  private final FsFile findAssetFile(String fileName) throws IOException {
    if (getAssetsDirectory().join(fileName).exists()) {
      return getAssetsDirectory().join(fileName);
    }

    // otherwise look through the library assets
    for (FsFile libraryAsset : getLibraryAssetsDirectory()) {
      if (libraryAsset.join(fileName).exists()) {
        return libraryAsset.join(fileName);
      }
    }

    throw new FileNotFoundException("Asset file " + fileName + " not found");
  }

  /**
   * Extract an asset from a zipped up assets provided by the build system, this is required because there is no
   * way to get a FileDescriptor from a zip entry. This is a temporary measure for Bazel which can be removed
   * once binary resources are supported.
   */
  private static File getFileFromZip(File file) {
    File fileFromZip = null;
    String pathString = file.getPath();
    String zipFile = pathString.substring(pathString.indexOf(":") + 1, pathString.indexOf("!"));
    String filePathInsideZip = pathString.split("!")[1].substring(1);
    byte[] buffer = new byte[1024];
    try {
      File outputDir = Files.createTempDirectory("robolectric_assets").toFile();
      ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
      ZipEntry ze = zis.getNextEntry();
      while (ze != null) {
        String currentFilename = ze.getName();
        if (!currentFilename.equals(filePathInsideZip)) {
          ze = zis.getNextEntry();
          continue;
        }
        fileFromZip = new File(outputDir + File.separator + currentFilename);
        new File(fileFromZip.getParent()).mkdirs();
        FileOutputStream fos = new FileOutputStream(fileFromZip);
        int len;
        while ((len = zis.read(buffer)) > 0) {
          fos.write(buffer, 0, len);
        }
        fos.close();
        break;
      }
      zis.closeEntry();
      zis.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return fileFromZip;
  }

  @Implementation
  public final String[] list(String path) throws IOException {
    List<String> assetFiles = new ArrayList<>();
    List<FsFile> assets = new ArrayList<>();
    if (path.isEmpty()) {
      assets.addAll(getLibraryAssetsDirectory());
    } else {
      assets.add(findAssetFile(path));
    }

    for (FsFile asset : assets) {
      if (asset.isDirectory()) {
        Collections.addAll(assetFiles, asset.listFileNames());
      }
    }

    return assetFiles.toArray(new String[assetFiles.size()]);
  }

  @HiddenApi @Implementation
  public final InputStream openNonAsset(int cookie, String fileName, int accessMode) throws IOException {
    final ResName resName = qualifyFromNonAssetFileName(fileName);

    final FileTypedResource typedResource =
        (FileTypedResource) resourceTable.getValue(resName, config);

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
    // Resources from a jar belong to the "android" namespace, except when they come from "resource_files.zip"
    // when they are application resources produced by Bazel.
    if (fileName.startsWith("jar:") && !fileName.contains("resource_files.zip")) {
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
    XmlBlock xmlBlock = XmlBlock.create(Fs.fileFromPath(fileName), "fixme");
    if (xmlBlock == null) {
      throw new Resources.NotFoundException(fileName);
    }
    return getXmlResourceParser(null, xmlBlock, "fixme");
  }

  public XmlResourceParser loadXmlResourceParser(int resId, String type) throws Resources.NotFoundException {
    ResName resName = getResName(resId);
    ResName resolvedResName = resolveResName(resName, config);
    if (resolvedResName == null) {
      throw new RuntimeException("couldn't resolve " + resName.getFullyQualifiedName());
    }
    resName = resolvedResName;

    XmlBlock block = resourceTable.getXml(resName, config);
    if (block == null) {
      throw new Resources.NotFoundException(resName.getFullyQualifiedName());
    }

    ResourceTable resourceProvider = ResourceIds.isFrameworkResource(resId) ? RuntimeEnvironment.getSystemResourceTable() : RuntimeEnvironment.getCompileTimeResourceTable();

    return getXmlResourceParser(resourceProvider, block, resName.packageName);
  }

  private XmlResourceParser getXmlResourceParser(ResourceTable resourceProvider, XmlBlock block, String packageName) {
    return new XmlResourceParserImpl(block.getDocument(), block.getFilename(), block.getPackageName(),
        packageName, resourceProvider);
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

  @HiddenApi @Implementation(maxSdk = VERSION_CODES.N_MR1)
  public void setConfiguration(int mcc, int mnc, String locale,
      int orientation, int touchscreen, int density, int keyboard,
      int keyboardHidden, int navigation, int screenWidth, int screenHeight,
      int smallestScreenWidthDp, int screenWidthDp, int screenHeightDp,
      int screenLayout, int uiMode, int majorVersion) {
    setConfiguration(mcc, mnc, locale,
        orientation, touchscreen, density, keyboard,
        keyboardHidden, navigation, screenWidth, screenHeight,
        smallestScreenWidthDp, screenWidthDp, screenHeightDp,
        screenLayout, uiMode, 0, majorVersion);
  }

  @HiddenApi @Implementation(minSdk = VERSION_CODES.O)
  public void setConfiguration(int mcc, int mnc, String locale,
      int orientation, int touchscreen, int density, int keyboard,
      int keyboardHidden, int navigation, int screenWidth, int screenHeight,
      int smallestScreenWidthDp, int screenWidthDp, int screenHeightDp,
      int screenLayout, int uiMode, int colorMode, int majorVersion) {
    // AssetManager* am = assetManagerForJavaObject(env, clazz);

    ResTable_config config = new ResTable_config();

    // Constants duplicated from Java class android.content.res.Configuration.
    final int kScreenLayoutRoundMask = 0x300;
    final int kScreenLayoutRoundShift = 8;

    config.mcc = mcc;
    config.mnc = mnc;
    config.orientation = orientation;
    config.touchscreen = touchscreen;
    config.density = density;
    config.keyboard = keyboard;
    config.inputFlags = keyboardHidden;
    config.navigation = navigation;
    config.screenWidth = screenWidth;
    config.screenHeight = screenHeight;
    config.smallestScreenWidthDp = smallestScreenWidthDp;
    config.screenWidthDp = screenWidthDp;
    config.screenHeightDp = screenHeightDp;
    config.screenLayout = screenLayout;
    config.uiMode = uiMode;
    // config.colorMode = colorMode; // todo
    config.sdkVersion = majorVersion;
    config.minorVersion = 0;

    // In Java, we use a 32bit integer for screenLayout, while we only use an 8bit integer
    // in C++. We must extract the round qualifier out of the Java screenLayout and put it
    // into screenLayout2.
    config.screenLayout2 =
            (byte)((screenLayout & kScreenLayoutRoundMask) >> kScreenLayoutRoundShift);

    if (locale != null) {
      config.setBcp47Locale(locale);
    }
    // am->setConfiguration(config, locale8);

    this.config = config;
  }

  @HiddenApi @Implementation
  public int[] getArrayIntResource(int resId) {
    TypedResource value = getAndResolve(resId, config, true);
    if (value == null) return null;
    List<TypedResource> items = getConverter(value).getItems(value);
    int[] ints = new int[items.size()];
    for (int i = 0; i < items.size(); i++) {
      TypedResource typedResource = resolve(items.get(i), config, resId);
      ints[i] = getConverter(typedResource).asInt(typedResource);
    }
    return ints;
  }

 protected TypedArray getTypedArrayResource(Resources resources, int resId) {
    TypedResource value = getAndResolve(resId, config, true);
    if (value == null) {
      return null;
    }
    List<TypedResource> items = getConverter(value).getItems(value);
    return getTypedArray(resources, items, resId);
  }

  private TypedArray getTypedArray(Resources resources, List<TypedResource> typedResources, int resId) {
    final CharSequence[] stringData = new CharSequence[typedResources.size()];
    final int totalLen = typedResources.size() * ShadowAssetManager.STYLE_NUM_ENTRIES;
    final int[] data = new int[totalLen];

    for (int i = 0; i < typedResources.size(); i++) {
      final int offset = i * ShadowAssetManager.STYLE_NUM_ENTRIES;
      TypedResource typedResource = typedResources.get(i);

      // Classify the item.
      int type = getResourceType(typedResource);
      if (type == -1) {
        // This type is unsupported; leave empty.
        continue;
      }

      final TypedValue typedValue = new TypedValue();

      if (type == TypedValue.TYPE_REFERENCE) {
        final String reference = typedResource.asString();
        ResName refResName = AttributeResource.getResourceReference(reference,
            typedResource.getXmlContext().getPackageName(), null);
        typedValue.resourceId = resourceTable.getResourceId(refResName);
        typedValue.data = typedValue.resourceId;
        typedResource = resolve(typedResource, config, typedValue.resourceId);

        if (typedResource != null) {
          // Reclassify to a non-reference type.
          type = getResourceType(typedResource);
          if (type == TypedValue.TYPE_ATTRIBUTE) {
            type = TypedValue.TYPE_REFERENCE;
          } else if (type == -1) {
            // This type is unsupported; leave empty.
            continue;
          }
        }
      }

      if (type == TypedValue.TYPE_ATTRIBUTE) {
        final String reference = typedResource.asString();
        final ResName attrResName = AttributeResource.getStyleReference(reference,
            typedResource.getXmlContext().getPackageName(), "attr");
        typedValue.data = resourceTable.getResourceId(attrResName);
      }

      if (typedResource != null && type != TypedValue.TYPE_NULL && type != TypedValue.TYPE_ATTRIBUTE) {
        getConverter(typedResource).fillTypedValue(typedResource.getData(), typedValue);
      }

      data[offset + ShadowAssetManager.STYLE_TYPE] = type;
      data[offset + ShadowAssetManager.STYLE_RESOURCE_ID] = typedValue.resourceId;
      data[offset + ShadowAssetManager.STYLE_DATA] = typedValue.data;
      data[offset + ShadowAssetManager.STYLE_ASSET_COOKIE] = typedValue.assetCookie;
      data[offset + ShadowAssetManager.STYLE_CHANGING_CONFIGURATIONS] = typedValue.changingConfigurations;
      data[offset + ShadowAssetManager.STYLE_DENSITY] = typedValue.density;
      stringData[i] = typedResource == null ? null : typedResource.asString();
    }

    int[] indices = new int[typedResources.size() + 1]; /* keep zeroed out */
    return ShadowTypedArray.create(resources, null, data, indices, typedResources.size(), stringData);
  }

  private int getResourceType(TypedResource typedResource) {
    if (typedResource == null) {
      return -1;
    }
    final ResType resType = typedResource.getResType();
    int type;
    if (typedResource.getData() == null || resType == ResType.NULL) {
      type = TypedValue.TYPE_NULL;
    } else if (typedResource.isReference()) {
      type = TypedValue.TYPE_REFERENCE;
    } else if (resType == ResType.STYLE) {
      type = TypedValue.TYPE_ATTRIBUTE;
    } else if (resType == ResType.CHAR_SEQUENCE || resType == ResType.DRAWABLE) {
      type = TypedValue.TYPE_STRING;
    } else if (resType == ResType.INTEGER) {
      type = TypedValue.TYPE_INT_DEC;
    } else if (resType == ResType.FLOAT || resType == ResType.FRACTION) {
      type = TypedValue.TYPE_FLOAT;
    } else if (resType == ResType.BOOLEAN) {
      type = TypedValue.TYPE_INT_BOOLEAN;
    } else if (resType == ResType.DIMEN) {
      type = TypedValue.TYPE_DIMENSION;
    } else if (resType == ResType.COLOR) {
      type = TypedValue.TYPE_INT_COLOR_ARGB8;
    } else if (resType == ResType.TYPED_ARRAY || resType == ResType.CHAR_SEQUENCE_ARRAY) {
      type = TypedValue.TYPE_REFERENCE;
    } else {
      type = -1;
    }
    return type;
  }

  @HiddenApi @Implementation
  public Number createTheme() {
    synchronized (nativeThemes) {
      long nativePtr = nextInternalThemeId++;
      nativeThemes.put(nativePtr, new NativeTheme(new ThemeStyleSet()));
      return castNativePtr(nativePtr);
    }
  }

  private static NativeTheme getNativeTheme(Resources.Theme theme) {
    return getNativeTheme(shadowOf(theme).getNativePtr());
  }

  private static NativeTheme getNativeTheme(long themePtr) {
    NativeTheme nativeTheme;
    synchronized (nativeThemes) {
      nativeTheme = nativeThemes.get(themePtr);
    }
    if (nativeTheme == null) {
      throw new RuntimeException("no theme " + themePtr + " found in AssetManager");
    }
    return nativeTheme;
  }

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  public void releaseTheme(int themePtr) {
    releaseTheme((long) themePtr);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  public void releaseTheme(long themePtr) {
    synchronized (nativeThemes) {
      nativeThemes.remove(themePtr);
    }
  }

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  public static void applyThemeStyle(int themePtr, int styleRes, boolean force) {
    applyThemeStyle((long) themePtr, styleRes, force);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  public static void applyThemeStyle(long themePtr, int styleRes, boolean force) {
    NativeTheme nativeTheme = getNativeTheme(themePtr);
    Style style = nativeTheme.getShadowAssetManager().resolveStyle(styleRes, null);
    nativeTheme.themeStyleSet.apply(style, force);
}

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  public static void copyTheme(int destPtr, int sourcePtr) {
    copyTheme((long) destPtr, (long) sourcePtr);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  public static void copyTheme(long destPtr, long sourcePtr) {
    NativeTheme destNativeTheme = getNativeTheme(destPtr);
    NativeTheme sourceNativeTheme = getNativeTheme(sourcePtr);
    destNativeTheme.themeStyleSet = sourceNativeTheme.themeStyleSet.copy();
  }

  /////////////////////////

  Style resolveStyle(int resId, Style themeStyleSet) {
    return resolveStyle(getResName(resId), themeStyleSet);
  }

  private Style resolveStyle(@Nonnull ResName themeStyleName, Style themeStyleSet) {
    TypedResource themeStyleResource = resourceTable.getValue(themeStyleName, config);
    if (themeStyleResource == null) return null;
    StyleData themeStyleData = (StyleData) themeStyleResource.getData();
    if (themeStyleSet == null) {
      themeStyleSet = new ThemeStyleSet();
    }
    return new StyleResolver(resourceTable, shadowOf(AssetManager.getSystem()).getResourceTable(),
        themeStyleData, themeStyleSet, themeStyleName, config);
  }

  private TypedResource getAndResolve(int resId, ResTable_config config, boolean resolveRefs) {
    TypedResource value = resourceTable.getValue(resId, config);
    if (resolveRefs) {
      value = resolve(value, config, resId);
    }
    return value;
  }

  TypedResource resolve(TypedResource value, ResTable_config config, int resId) {
    return resolveResourceValue(value, config, resId);
  }

  public ResName resolveResName(ResName resName, ResTable_config config) {
    TypedResource value = resourceTable.getValue(resName, config);
    return resolveResource(value, config, resName);
  }

  // todo: DRY up #resolveResource vs #resolveResourceValue
  private ResName resolveResource(TypedResource value, ResTable_config config, ResName resName) {
    while (value != null && value.isReference()) {
      String s = value.asString();
      if (AttributeResource.isNull(s) || AttributeResource.isEmpty(s)) {
        value = null;
      } else {
        String refStr = s.substring(1).replace("+", "");
        resName = ResName.qualifyResName(refStr, resName);
        value = resourceTable.getValue(resName, config);
      }
    }

    return resName;
  }

  private TypedResource resolveResourceValue(TypedResource value, ResTable_config config, ResName resName) {
    while (value != null && value.isReference()) {
      String s = value.asString();
      if (AttributeResource.isNull(s) || AttributeResource.isEmpty(s)) {
        value = null;
      } else {
        String refStr = s.substring(1).replace("+", "");
        resName = ResName.qualifyResName(refStr, resName);
        value = resourceTable.getValue(resName, config);
      }
    }

    return value;
  }

  public TypedResource resolveResourceValue(TypedResource value, ResTable_config config, int resId) {
    ResName resName = getResName(resId);
    return resolveResourceValue(value, config, resName);
  }

  private TypedValue buildTypedValue(AttributeSet set, int resId, int defStyleAttr, Style themeStyleSet, int defStyleRes) {
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
      ResName resName = resourceTable.getResName(resId);

      AttributeResource otherAttr = themeStyleSet.getAttrValue(otherAttrName);
      if (otherAttr == null) {
        strictError("no such attr %s in %s while resolving value for %s", attribute.value, themeStyleSet, resName.getFullyQualifiedName());
        attribute = null;
      } else {
        attribute = new AttributeResource(resName, otherAttr.value, otherAttr.contextPackageName);
      }
    }

    if (attribute == null || attribute.isNull()) {
      return null;
    } else {
      TypedValue typedValue = new TypedValue();
      convertAndFill(attribute, typedValue, config, true);
      return typedValue;
    }
  }

  private void strictError(String message, Object... args) {
    if (strictErrors) {
      throw new RuntimeException(String.format(message, args));
    } else {
      Logger.strict(message, args);
    }
  }

  TypedArray attrsToTypedArray(Resources resources, AttributeSet set, int[] attrs, int defStyleAttr, long nativeTheme, int defStyleRes) {
    CharSequence[] stringData = new CharSequence[attrs.length];
    int[] data = new int[attrs.length * ShadowAssetManager.STYLE_NUM_ENTRIES];
    int[] indices = new int[attrs.length + 1];
    int nextIndex = 0;

    Style themeStyleSet = nativeTheme == 0
        ? new EmptyStyle()
        : getNativeTheme(nativeTheme).themeStyleSet;

    for (int i = 0; i < attrs.length; i++) {
      int offset = i * ShadowAssetManager.STYLE_NUM_ENTRIES;

      TypedValue typedValue = buildTypedValue(set, attrs[i], defStyleAttr, themeStyleSet, defStyleRes);
      if (typedValue != null) {
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

  private AttributeResource findAttributeValue(int resId, AttributeSet attributeSet, Style styleAttrStyle, Style defStyleFromAttr, Style defStyleFromRes, @Nonnull Style themeStyleSet) {
    if (attributeSet != null) {
      for (int i = 0; i < attributeSet.getAttributeCount(); i++) {
        if (attributeSet.getAttributeNameResource(i) == resId && attributeSet.getAttributeValue(i) != null) {
          String defaultPackageName = ResourceIds.isFrameworkResource(resId) ? "android" : RuntimeEnvironment.application.getPackageName();
          ResName resName = ResName.qualifyResName(attributeSet.getAttributeName(i), defaultPackageName, "attr");
          Integer referenceResId = null;
          if (AttributeResource.isResourceReference(attributeSet.getAttributeValue(i))) {
            referenceResId = attributeSet.getAttributeResourceValue(i, -1);
          }
          return new AttributeResource(resName, attributeSet.getAttributeValue(i), "fixme!!!", referenceResId);
        }
      }
    }

    ResName attrName = resourceTable.getResName(resId);
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

  private FsFile getAssetsDirectory() {
    return ShadowApplication.getInstance().getAppManifest().getAssetsDirectory();
  }

  private List<FsFile> getLibraryAssetsDirectory() {
    List<FsFile> libraryAssetsDirectory = new ArrayList<>();
    for (AndroidManifest manifest : ShadowApplication.getInstance().getAppManifest().getLibraryManifests()) {
      if (manifest.getAssetsDirectory() != null) {
        libraryAssetsDirectory.add(manifest.getAssetsDirectory());
      }
    }

    return libraryAssetsDirectory;
  }

  @Nonnull private ResName getResName(int id) {
    ResName resName = resourceTable.getResName(id);
    if (resName == null) {
      throw new Resources.NotFoundException("Unable to find resource ID #0x" + Integer.toHexString(id)
          + " in packages " + resourceTable);
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

  @Implementation
  public final SparseArray<String> getAssignedPackageIdentifiers() {
    return new SparseArray<>();
  }

  @Resetter
  public static void reset() {
    ReflectionHelpers.setStaticField(AssetManager.class, "sSystem", null);
  }
}
