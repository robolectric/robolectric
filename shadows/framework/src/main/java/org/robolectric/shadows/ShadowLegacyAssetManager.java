package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT_WATCH;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static org.robolectric.RuntimeEnvironment.castNativePtr;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.shadow.api.Shadow.invokeConstructor;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.SuppressLint;
import android.content.res.ApkAssets;
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
import dalvik.system.VMRuntime;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.annotation.Nonnull;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.XmlResourceParserImpl;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.res.AttrData;
import org.robolectric.res.AttributeResource;
import org.robolectric.res.EmptyStyle;
import org.robolectric.res.FileTypedResource;
import org.robolectric.res.Fs;
import org.robolectric.res.ResName;
import org.robolectric.res.ResType;
import org.robolectric.res.ResourceIds;
import org.robolectric.res.ResourceTable;
import org.robolectric.res.Style;
import org.robolectric.res.StyleData;
import org.robolectric.res.StyleResolver;
import org.robolectric.res.ThemeStyleSet;
import org.robolectric.res.TypedResource;
import org.robolectric.res.android.Asset;
import org.robolectric.res.android.Registries;
import org.robolectric.res.android.ResTable_config;
import org.robolectric.res.builder.XmlBlock;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowAssetManager.Picker;
import org.robolectric.util.Logger;
import org.robolectric.util.TempDirectory;

@SuppressLint("NewApi")
@Implements(value = AssetManager.class, /* this one works for P too... maxSdk = VERSION_CODES.O_MR1,*/
    looseSignatures = true, shadowPicker = Picker.class)
public class ShadowLegacyAssetManager extends ShadowAssetManager {

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
          "flags",
          "string");

  static boolean strictErrors = false;

  private static long nextInternalThemeId = 1000;
  private static final Map<Long, NativeTheme> nativeThemes = new HashMap<>();

  @RealObject
  protected AssetManager realObject;

  private ResourceTable resourceTable;

  class NativeTheme {
    private ThemeStyleSet themeStyleSet;

    public NativeTheme(ThemeStyleSet themeStyleSet) {
      this.themeStyleSet = themeStyleSet;
    }

    public ShadowLegacyAssetManager getShadowAssetManager() {
      return ShadowLegacyAssetManager.this;
    }
  }

  ResTable_config config = new ResTable_config();
  private final Set<Path> assetDirs = new CopyOnWriteArraySet<>();

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
    outValue.changingConfigurations = 0;

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

    TypedResource attrTypeData = getAttrTypeData(attribute.resName);
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


  public TypedResource getAttrTypeData(ResName resName) {
    return resourceTable.getValue(resName, config);
  }

  @Implementation
  protected void __constructor__() {
    resourceTable = RuntimeEnvironment.getAppResourceTable();

    
    if (RuntimeEnvironment.getApiLevel() >= P) {
      invokeConstructor(AssetManager.class, realObject);
    }
    
  }

  @Implementation
  protected void __constructor__(boolean isSystem) {
    resourceTable = isSystem ? RuntimeEnvironment.getSystemResourceTable() : RuntimeEnvironment.getAppResourceTable();

    
    if (RuntimeEnvironment.getApiLevel() >= P) {
      invokeConstructor(AssetManager.class, realObject, from(boolean.class, isSystem));
    }
    
  }

  @Implementation(minSdk = P)
  protected static long nativeCreate() {
    // Return a fake pointer, must not be 0.
    return 1;
  }

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected void init() {
    // no op
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP, maxSdk = O_MR1)
  protected void init(boolean isSystem) {
    // no op
  }

  protected ResourceTable getResourceTable() {
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

  @HiddenApi @Implementation(maxSdk = O_MR1)
  protected int getStringBlockCount() {
    return 0;
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
            && ((FileTypedResource) value).getPath().getFileName().toString().endsWith(".xml"))) {
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

  @HiddenApi @Implementation(maxSdk = O_MR1)
  protected Object ensureStringBlocks() {
    return null;
  }

  @Implementation
  protected final InputStream open(String fileName) throws IOException {
    return Fs.getInputStream(findAssetFile(fileName));
  }

  @Implementation
  protected final InputStream open(String fileName, int accessMode) throws IOException {
    return Fs.getInputStream(findAssetFile(fileName));
  }

  @Implementation
  protected final AssetFileDescriptor openFd(String fileName) throws IOException {
    Path path = findAssetFile(fileName);
    if (path.getFileSystem().provider().getScheme().equals("jar")) {
      path = getFileFromZip(path);
    }
    ParcelFileDescriptor parcelFileDescriptor =
        ParcelFileDescriptor.open(path.toFile(), ParcelFileDescriptor.MODE_READ_ONLY);
    return new AssetFileDescriptor(parcelFileDescriptor, 0, Files.size(path));
  }

  private Path findAssetFile(String fileName) throws IOException {
    for (Path assetDir : getAllAssetDirs()) {
      Path assetFile = assetDir.resolve(fileName);
      if (Files.exists(assetFile)) {
        return assetFile;
      }
    }

    throw new FileNotFoundException("Asset file " + fileName + " not found");
  }

  /**
   * Extract an asset from a zipped up assets provided by the build system, this is required because
   * there is no way to get a FileDescriptor from a zip entry. This is a temporary measure for Bazel
   * which can be removed once binary resources are supported.
   */
  private static Path getFileFromZip(Path path) {
    byte[] buffer = new byte[1024];
    try {
      Path outputDir = new TempDirectory("robolectric_assets").create("fromzip");
      try (InputStream zis = Fs.getInputStream(path)) {
        Path fileFromZip = outputDir.resolve(path.getFileName().toString());

        try (OutputStream fos = Files.newOutputStream(fileFromZip)) {
          int len;
          while ((len = zis.read(buffer)) > 0) {
            fos.write(buffer, 0, len);
          }
        }
        return fileFromZip;
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Implementation
  protected final String[] list(String path) throws IOException {
    List<String> assetFiles = new ArrayList<>();

    for (Path assetsDir : getAllAssetDirs()) {
      Path file;
      if (path.isEmpty()) {
        file = assetsDir;
      } else {
        file = assetsDir.resolve(path);
      }

      if (Files.isDirectory(file)) {
        Collections.addAll(assetFiles, Fs.listFileNames(file));
      }
    }
    return assetFiles.toArray(new String[assetFiles.size()]);
  }

  @HiddenApi @Implementation(maxSdk = O_MR1)
  protected Number openAsset(String fileName, int mode) throws FileNotFoundException {
    return 0;
  }

  @HiddenApi @Implementation(maxSdk = O_MR1)
  protected ParcelFileDescriptor openAssetFd(String fileName, long[] outOffsets) throws IOException {
    return null;
  }

  @HiddenApi @Implementation
  public final InputStream openNonAsset(int cookie, String fileName, int accessMode) throws IOException {
    final ResName resName = qualifyFromNonAssetFileName(fileName);

    final FileTypedResource typedResource =
        (FileTypedResource) resourceTable.getValue(resName, config);

    if (typedResource == null) {
      throw new IOException("Unable to find resource for " + fileName);
    }

    InputStream stream;
    if (accessMode == AssetManager.ACCESS_STREAMING) {
      stream = Fs.getInputStream(typedResource.getPath());
    } else {
      stream = new ByteArrayInputStream(Fs.getBytes(typedResource.getPath()));
    }

    if (RuntimeEnvironment.getApiLevel() >= P) {
      Asset asset = Asset.newFileAsset(typedResource);
      long assetPtr = Registries.NATIVE_ASSET_REGISTRY.register(asset);
      // Camouflage the InputStream as an AssetInputStream so subsequent instanceof checks pass.
      stream = ShadowAssetInputStream.createAssetInputStream(stream, assetPtr, realObject);
    }

    return stream;
  }

  @HiddenApi @Implementation(maxSdk = O_MR1)
  protected Number openNonAssetNative(int cookie, String fileName, int accessMode)
      throws FileNotFoundException {
    throw new IllegalStateException();
  }

  private ResName qualifyFromNonAssetFileName(String fileName) {
    // Resources from a jar belong to the "android" namespace, except when they come from "resource_files.zip"
    // when they are application resources produced by Bazel.
    if (fileName.startsWith("jar:") && !fileName.contains("resource_files.zip")) {
      // Must remove "jar:" prefix, or else qualifyFromFilePath fails on Windows
      return ResName.qualifyFromFilePath("android", fileName.replaceFirst("jar:", ""));
    } else {
      return ResName.qualifyFromFilePath(RuntimeEnvironment.application.getPackageName(), fileName);
    }
  }

  @HiddenApi @Implementation
  public final AssetFileDescriptor openNonAssetFd(int cookie, String fileName) throws IOException {
    throw new IllegalStateException();
  }

  @HiddenApi @Implementation(maxSdk = O_MR1)
  protected ParcelFileDescriptor openNonAssetFdNative(int cookie, String fileName, long[] outOffsets)
      throws IOException {
    throw new IllegalStateException();
  }

  @HiddenApi @Implementation(maxSdk = O_MR1)
  protected Number openXmlAssetNative(int cookie, String fileName) throws FileNotFoundException {
    throw new IllegalStateException();
  }

  @Implementation
  protected final XmlResourceParser openXmlResourceParser(int cookie, String fileName)
      throws IOException {
    XmlBlock xmlBlock = XmlBlock.create(Fs.fromUrl(fileName), resourceTable.getPackageName());
    if (xmlBlock == null) {
      throw new Resources.NotFoundException(fileName);
    }
    return getXmlResourceParser(resourceTable, xmlBlock, resourceTable.getPackageName());
  }

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected final long seekAsset(int asset, long offset, int whence) {
    return seekAsset((long) asset, offset, whence);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP, maxSdk = O_MR1)
  protected long seekAsset(long asset, long offset, int whence) {
    return 0;
  }

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected final long getAssetLength(int asset) {
    return getAssetLength((long) asset);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP, maxSdk = O_MR1)
  protected long getAssetLength(long asset) {
    return 0;
  }

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected final long getAssetRemainingLength(int asset) {
    return getAssetRemainingLength((long) asset);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP, maxSdk = O_MR1)
  protected long getAssetRemainingLength(long assetHandle) {
    return 0;
  }

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected final void destroyAsset(int asset) {
    destroyAsset((long) asset);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP, maxSdk = O_MR1)
  protected void destroyAsset(long asset) {
    // no op
  }

  protected XmlResourceParser loadXmlResourceParser(int resId, String type) throws Resources.NotFoundException {
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
    return new XmlResourceParserImpl(
        block.getDocument(),
        block.getPath(),
        block.getPackageName(),
        packageName,
        resourceProvider);
  }

  @HiddenApi @Implementation
  public int addAssetPath(String path) {
    assetDirs.add(Fs.fromUrl(path));
    return 1;
  }

  @HiddenApi @Implementation(minSdk = JELLY_BEAN_MR2, maxSdk = M)
  final protected int addAssetPathNative(String path) {
    return addAssetPathNative(path, false);
  }

  @HiddenApi @Implementation(minSdk = N, maxSdk = O_MR1)
  protected int addAssetPathNative(String path, boolean appAsLib) {
    return 0;
  }

  @HiddenApi @Implementation(minSdk = P)
  public void setApkAssets(Object apkAssetsObject, Object invalidateCachesObject) {
    ApkAssets[] apkAssets = (ApkAssets[]) apkAssetsObject;
    boolean invalidateCaches = (boolean) invalidateCachesObject;

    for (ApkAssets apkAsset : apkAssets) {
      assetDirs.add(Fs.fromUrl(apkAsset.getAssetPath()));
    }
    directlyOn(realObject, AssetManager.class).setApkAssets(apkAssets, invalidateCaches);
  }

  @HiddenApi @Implementation
  public boolean isUpToDate() {
    return true;
  }

  @HiddenApi @Implementation(maxSdk = M)
  public void setLocale(String locale) {
  }

  @Implementation
  protected String[] getLocales() {
    return new String[0]; // todo
  }

  @HiddenApi @Implementation(maxSdk = N_MR1)
  final public void setConfiguration(int mcc, int mnc, String locale,
      int orientation, int touchscreen, int density, int keyboard,
      int keyboardHidden, int navigation, int screenWidth, int screenHeight,
      int smallestScreenWidthDp, int screenWidthDp, int screenHeightDp,
      int screenLayout, int uiMode, int sdkVersion) {
    setConfiguration(mcc, mnc, locale,
        orientation, touchscreen, density, keyboard,
        keyboardHidden, navigation, screenWidth, screenHeight,
        smallestScreenWidthDp, screenWidthDp, screenHeightDp,
        screenLayout, uiMode, 0, sdkVersion);
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

  @HiddenApi @Implementation(maxSdk = O_MR1)
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

  @HiddenApi @Implementation(minSdk = P)
  protected int[] getResourceIntArray(int resId) {
    return getArrayIntResource(resId);
  }

  @HiddenApi @Implementation(maxSdk = O_MR1)
  protected String[] getArrayStringResource(int arrayResId) {
    return new String[0];
  }

  @HiddenApi @Implementation(maxSdk = O_MR1)
  protected int[] getArrayStringInfo(int arrayResId) {
    return new int[0];
  }

  @HiddenApi @Implementation(maxSdk = O_MR1)
  protected Number newTheme() {
    return null;
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
    final int totalLen = typedResources.size() * STYLE_NUM_ENTRIES;
    final int[] data = new int[totalLen];

    for (int i = 0; i < typedResources.size(); i++) {
      final int offset = i * STYLE_NUM_ENTRIES;
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

      data[offset + STYLE_TYPE] = type;
      data[offset + STYLE_RESOURCE_ID] = typedValue.resourceId;
      data[offset + STYLE_DATA] = typedValue.data;
      data[offset + STYLE_ASSET_COOKIE] = typedValue.assetCookie;
      data[offset + STYLE_CHANGING_CONFIGURATIONS] = typedValue.changingConfigurations;
      data[offset + STYLE_DENSITY] = typedValue.density;
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

  @HiddenApi @Implementation(minSdk = LOLLIPOP, maxSdk = O_MR1)
  protected static void dumpTheme(long theme, int priority, String tag, String prefix) {
    throw new UnsupportedOperationException("not yet implemented");
  }

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  public void releaseTheme(int themePtr) {
    // no op
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

  @HiddenApi @Implementation(minSdk = LOLLIPOP)
  public void releaseTheme(long themePtr) {
    synchronized (nativeThemes) {
      nativeThemes.remove(themePtr);
    }
  }

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected void deleteTheme(int theme) {
    deleteTheme((long) theme);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP, maxSdk = O_MR1)
  protected void deleteTheme(long theme) {
    // no op
  }

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  public static void applyThemeStyle(int themePtr, int styleRes, boolean force) {
    applyThemeStyle((long) themePtr, styleRes, force);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP, maxSdk = O_MR1)
  public static void applyThemeStyle(long themePtr, int styleRes, boolean force) {
    NativeTheme nativeTheme = getNativeTheme(themePtr);
    Style style = nativeTheme.getShadowAssetManager().resolveStyle(styleRes, null);
    nativeTheme.themeStyleSet.apply(style, force);
  }

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  public static void copyTheme(int destPtr, int sourcePtr) {
    copyTheme((long) destPtr, (long) sourcePtr);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP, maxSdk = O_MR1)
  public static void copyTheme(long destPtr, long sourcePtr) {
    NativeTheme destNativeTheme = getNativeTheme(destPtr);
    NativeTheme sourceNativeTheme = getNativeTheme(sourcePtr);
    destNativeTheme.themeStyleSet = sourceNativeTheme.themeStyleSet.copy();
  }

  @HiddenApi @Implementation(minSdk = P)
  protected static void nativeThemeCopy(long destPtr, long sourcePtr) {
    copyTheme(destPtr, sourcePtr);
  }

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected static boolean applyStyle(int themeToken, int defStyleAttr, int defStyleRes,
      int xmlParserToken, int[] attrs, int[] outValues, int[] outIndices) {
    return applyStyle((long)themeToken, defStyleAttr, defStyleRes, (long)xmlParserToken, attrs,
        outValues, outIndices);
  }

  @HiddenApi @Implementation(minSdk = O, maxSdk = O_MR1)
  protected static void applyStyle(long themeToken, int defStyleAttr, int defStyleRes,
      long xmlParserToken, int[] inAttrs, int length, long outValuesAddress,
      long outIndicesAddress) {
    ShadowVMRuntime shadowVMRuntime = Shadow.extract(VMRuntime.getRuntime());
    int[] outValues = (int[])shadowVMRuntime.getObjectForAddress(outValuesAddress);
    int[] outIndices = (int[])shadowVMRuntime.getObjectForAddress(outIndicesAddress);
    applyStyle(themeToken, defStyleAttr, defStyleRes, xmlParserToken, inAttrs,
        outValues, outIndices);
  }

  @HiddenApi @Implementation(minSdk = P)
  protected void applyStyleToTheme(long themePtr, int resId, boolean force) {
    applyThemeStyle(themePtr, resId, force);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP, maxSdk = N_MR1)
  protected static boolean applyStyle(long themeToken, int defStyleAttr, int defStyleRes,
      long xmlParserToken, int[] attrs, int[] outValues, int[] outIndices) {
    // no-op
    return false;
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP, maxSdk = O_MR1)
  protected static boolean resolveAttrs(long themeToken,
      int defStyleAttr, int defStyleRes, int[] inValues,
      int[] attrs, int[] outValues, int[] outIndices) {
    // no-op
    return false;
  }

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected boolean retrieveAttributes(
      int xmlParserToken, int[] attrs, int[] outValues, int[] outIndices) {
    return retrieveAttributes((long)xmlParserToken, attrs, outValues, outIndices);
  }

  @Implementation(minSdk = LOLLIPOP, maxSdk = O_MR1)
  protected boolean retrieveAttributes(long xmlParserToken, int[] attrs, int[] outValues,
      int[] outIndices) {
    return false;
  }

  @HiddenApi @Implementation(maxSdk = KITKAT_WATCH)
  protected static int loadThemeAttributeValue(int themeHandle, int ident,
      TypedValue outValue, boolean resolve) {
    return loadThemeAttributeValue((long) themeHandle, ident, outValue, resolve);
  }

  @HiddenApi @Implementation(minSdk = LOLLIPOP, maxSdk = O_MR1)
  protected static int loadThemeAttributeValue(long themeHandle, int ident,
      TypedValue outValue, boolean resolve) {
    // no-op
    return 0;
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
    return new StyleResolver(resourceTable, legacyShadowOf(AssetManager.getSystem()).getResourceTable(),
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

  protected ResName resolveResName(ResName resName, ResTable_config config) {
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

  protected TypedResource resolveResourceValue(TypedResource value, ResTable_config config, int resId) {
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
        // todo: this should be a style resId, not an attr
        System.out.println("WARN: " + resName.getFullyQualifiedName() + " should be a style resId");
        // AttributeResource attributeValue = findAttributeValue(defStyleRes, set, styleAttrStyle, defStyleFromAttr, defStyleFromAttr, themeStyleSet);
        // if (attributeValue != null) {
        //   if (attributeValue.isStyleReference()) {
        //     resName = themeStyleSet.getAttrValue(attributeValue.getStyleReference()).getResourceReference();
        //   } else if (attributeValue.isResourceReference()) {
        //     resName = attributeValue.getResourceReference();
        //   }
        // }
      } else if (resName.type.equals("style")) {
        defStyleFromRes = resolveStyle(resName, themeStyleSet);
      }
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
    int[] data = new int[attrs.length * STYLE_NUM_ENTRIES];
    int[] indices = new int[attrs.length + 1];
    int nextIndex = 0;

    Style themeStyleSet = nativeTheme == 0
        ? new EmptyStyle()
        : getNativeTheme(nativeTheme).themeStyleSet;

    for (int i = 0; i < attrs.length; i++) {
      int offset = i * STYLE_NUM_ENTRIES;

      TypedValue typedValue = buildTypedValue(set, attrs[i], defStyleAttr, themeStyleSet, defStyleRes);
      if (typedValue != null) {
        //noinspection PointlessArithmeticExpression
        data[offset + STYLE_TYPE] = typedValue.type;
        data[offset + STYLE_DATA] = typedValue.type == TypedValue.TYPE_STRING ? i : typedValue.data;
        data[offset + STYLE_ASSET_COOKIE] = typedValue.assetCookie;
        data[offset + STYLE_RESOURCE_ID] = typedValue.resourceId;
        data[offset + STYLE_CHANGING_CONFIGURATIONS] = typedValue.changingConfigurations;
        data[offset + STYLE_DENSITY] = typedValue.density;
        stringData[i] = typedValue.string;

        indices[nextIndex + 1] = i;
        nextIndex++;
      }
    }

    indices[0] = nextIndex;

    TypedArray typedArray = ShadowTypedArray.create(resources, attrs, data, indices, nextIndex, stringData);
    if (set != null) {
      ShadowTypedArray shadowTypedArray = Shadow.extract(typedArray);
      shadowTypedArray.positionDescription = set.getPositionDescription();
    }
    return typedArray;
  }

  private AttributeResource findAttributeValue(int resId, AttributeSet attributeSet, Style styleAttrStyle, Style defStyleFromAttr, Style defStyleFromRes, @Nonnull Style themeStyleSet) {
    if (attributeSet != null) {
      for (int i = 0; i < attributeSet.getAttributeCount(); i++) {
        if (attributeSet.getAttributeNameResource(i) == resId) {
          String attributeValue;
          try {
            attributeValue = attributeSet.getAttributeValue(i);
          } catch (IndexOutOfBoundsException e) {
            // type is TypedValue.TYPE_NULL, ignore...
            continue;
          }
          if (attributeValue != null) {
            String defaultPackageName = ResourceIds.isFrameworkResource(resId) ? "android" : RuntimeEnvironment.application.getPackageName();
            ResName resName = ResName.qualifyResName(attributeSet.getAttributeName(i), defaultPackageName, "attr");
            Integer referenceResId = null;
            if (AttributeResource.isResourceReference(attributeValue)) {
              referenceResId = attributeSet.getAttributeResourceValue(i, -1);
              // binary AttributeSet references have a string value of @resId rather than fully qualified resource name
              if (referenceResId != 0) {
                ResName refResName = resourceTable.getResName(referenceResId);
                if (refResName != null) {
                  attributeValue = "@" + refResName.getFullyQualifiedName();
                }
              }
            }
            return new AttributeResource(resName, attributeValue, "fixme!!!", referenceResId);
          }
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

  @Override
  Collection<Path> getAllAssetDirs() {
    return assetDirs;
  }

  @Nonnull private ResName getResName(int id) {
    ResName resName = resourceTable.getResName(id);
    if (resName == null) {
      throw new Resources.NotFoundException("Resource ID #0x" + Integer.toHexString(id));
    }
    return resName;
  }

  @Implementation
  protected String getResourceName(int resid) {
    return getResName(resid).getFullyQualifiedName();
  }

  @Implementation
  protected String getResourcePackageName(int resid) {
    return getResName(resid).packageName;
  }

  @Implementation
  protected String getResourceTypeName(int resid) {
    return getResName(resid).type;
  }

  @Implementation
  protected String getResourceEntryName(int resid) {
    return getResName(resid).name;
  }

  @Implementation(maxSdk = O_MR1)
  protected int getArraySize(int id) {
    return 0;
  }

  @Implementation(maxSdk = O_MR1)
  protected int retrieveArray(int id, int[] outValues) {
    return 0;
  }

  @Implementation(maxSdk = O_MR1)
  protected Number getNativeStringBlock(int block) {
    throw new IllegalStateException();
  }

  @Implementation(minSdk = LOLLIPOP, maxSdk = O_MR1)
  protected final SparseArray<String> getAssignedPackageIdentifiers() {
    return new SparseArray<>();
  }

  @Implementation(maxSdk = O_MR1)
  protected int loadResourceValue(int ident, short density, TypedValue outValue, boolean resolve) {
    return 0;
  }

  @Implementation(maxSdk = O_MR1)
  protected int loadResourceBagValue(int ident, int bagEntryId, TypedValue outValue, boolean resolve) {
    return 0;
  }

  // static void NativeAssetDestroy(JNIEnv* /*env*/, jclass /*clazz*/, jlong asset_ptr) {
  @Implementation(minSdk = P)
  protected static void nativeAssetDestroy(long asset_ptr) {
    ShadowArscAssetManager9.nativeAssetDestroy(asset_ptr);
  }

  // static jint NativeAssetReadChar(JNIEnv* /*env*/, jclass /*clazz*/, jlong asset_ptr) {
  @Implementation(minSdk = P)
  protected static int nativeAssetReadChar(long asset_ptr) {
    return ShadowArscAssetManager9.nativeAssetReadChar(asset_ptr);
  }

  // static jint NativeAssetRead(JNIEnv* env, jclass /*clazz*/, jlong asset_ptr, jbyteArray java_buffer,
//                             jint offset, jint len) {
  @Implementation(minSdk = P)
  protected static int nativeAssetRead(long asset_ptr, byte[] java_buffer, int offset, int len)
      throws IOException {
    return ShadowArscAssetManager9.nativeAssetRead(asset_ptr, java_buffer, offset, len);
  }

  // static jlong NativeAssetSeek(JNIEnv* env, jclass /*clazz*/, jlong asset_ptr, jlong offset,
//                              jint whence) {
  @Implementation(minSdk = P)
  protected static long nativeAssetSeek(long asset_ptr, long offset, int whence) {
    return ShadowArscAssetManager9.nativeAssetSeek(asset_ptr, offset, whence);
  }

  // static jlong NativeAssetGetLength(JNIEnv* /*env*/, jclass /*clazz*/, jlong asset_ptr) {
  @Implementation(minSdk = P)
  protected static long nativeAssetGetLength(long asset_ptr) {
    return ShadowArscAssetManager9.nativeAssetGetLength(asset_ptr);
  }

  // static jlong NativeAssetGetRemainingLength(JNIEnv* /*env*/, jclass /*clazz*/, jlong asset_ptr) {
  @Implementation(minSdk = P)
  protected static long nativeAssetGetRemainingLength(long asset_ptr) {
    return ShadowArscAssetManager9.nativeAssetGetRemainingLength(asset_ptr);
  }

  @Resetter
  public static void reset() {
    // todo: ShadowPicker doesn't discriminate properly between concrete shadow classes for resetters...
    if (useLegacy()) {
      if (RuntimeEnvironment.getApiLevel() >= P) {
        _AssetManager28_ _assetManagerStatic_ = reflector(_AssetManager28_.class);
        _assetManagerStatic_.setSystemApkAssetsSet(null);
        _assetManagerStatic_.setSystemApkAssets(null);
      }
      reflector(_AssetManager_.class).setSystem(null);
    }
  }

}
