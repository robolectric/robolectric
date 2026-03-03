# Split APK and Android App Bundle (AAB) Support in Robolectric

## Report Overview

This report documents the Android App Bundle (AAB) build and distribution process, the split APK
delivery mechanism, and how Robolectric now supports testing apps that use these technologies.

---

## 1. Android App Bundle (AAB) Process

### What is an AAB?

An Android App Bundle (.aab) is a publishing format that includes all of an app's compiled code and
resources but defers APK generation and signing to Google Play (or bundletool). Unlike a traditional
.apk, an AAB is not directly installable on a device.

### AAB Build Process

```
Source Code + Resources
        │
        ▼
  Android Gradle Plugin (AGP)
        │
        ▼
  .aab file (Android App Bundle)
    ├── base/             ← Base module
    │   ├── manifest/
    │   ├── dex/
    │   ├── res/
    │   ├── assets/
    │   └── resources.pb   (compiled resources in protobuf format)
    ├── feature_camera/    ← Dynamic feature module
    │   ├── manifest/
    │   ├── dex/
    │   └── res/
    └── BundleConfig.pb    ← Bundle configuration
```

### Key Properties of AAB

1. **Deferred APK generation**: The AAB contains all possible configurations; the actual APK set
   is generated server-side based on the target device.
2. **Module-based**: The AAB can contain multiple modules (base + dynamic features).
3. **Resource optimization**: Resources are stored per-configuration (density, ABI, locale) enabling
   delivery of only the needed resources.
4. **Protobuf format**: Resources use protobuf (.pb) instead of binary XML (.arsc).

---

## 2. Split APK Generation and Delivery Process

### From AAB to Split APKs

When a user downloads an AAB app from Google Play (or installs via bundletool), the following happens:

```
.aab file
    │
    ▼
bundletool / Google Play
    │
    ├── Base Split APK
    │   └── base-master.apk (code, shared resources, manifest)
    │
    ├── Configuration Splits (per-device)
    │   ├── base-xxhdpi.apk        (density resources)
    │   ├── base-arm64_v8a.apk     (native libraries)
    │   └── base-en.apk            (language resources)
    │
    └── Dynamic Feature Splits (on-demand or install-time)
        ├── feature_camera-master.apk
        ├── feature_camera-xxhdpi.apk
        └── feature_maps-master.apk
```

### Types of Split APKs

| Split Type | Purpose | Example Names | Delivery |
|------------|---------|---------------|----------|
| **Base** | Core app code + resources | `base-master.apk` | Always installed |
| **Density** | Screen-density-specific resources | `base-xxhdpi.apk`, `config.hdpi` | Per device |
| **ABI** | Architecture-specific native libs | `base-arm64_v8a.apk`, `config.x86` | Per device |
| **Language** | Locale-specific resources | `base-en.apk`, `config.fr` | Per device |
| **Dynamic Feature** | On-demand or install-time features | `feature_camera-master.apk` | On demand |

### How Android Framework Handles Split APKs

When split APKs are installed, the framework tracks them through `ApplicationInfo`:

```java
// Fields populated by the package manager after installation:
ApplicationInfo.sourceDir         = "/data/app/com.example/base.apk"
ApplicationInfo.publicSourceDir   = "/data/app/com.example/base.apk"
ApplicationInfo.splitNames        = ["config.xxhdpi", "config.arm64_v8a", "config.en"]
ApplicationInfo.splitSourceDirs   = ["/data/app/.../split_config.xxhdpi.apk",
                                     "/data/app/.../split_config.arm64_v8a.apk",
                                     "/data/app/.../split_config.en.apk"]
ApplicationInfo.splitPublicSourceDirs = [...]  // same as splitSourceDirs
```

The `PackageInstaller` API is used for installation:

```java
// Create session
PackageInstaller installer = context.getPackageManager().getPackageInstaller();
int sessionId = installer.createSession(new SessionParams(MODE_FULL_INSTALL));
Session session = installer.openSession(sessionId);

// Write each split APK
for (File splitApk : splitApks) {
    OutputStream out = session.openWrite(splitApk.getName(), 0, splitApk.length());
    Files.copy(splitApk.toPath(), out);
    out.close();
}

// Commit
session.commit(statusReceiver);
```

The `LoadedApk` class provides per-split classloaders:

```java
// Available from API 26 (O)
ClassLoader splitCl = loadedApk.getSplitClassLoader("feature_camera");
```

---

## 3. How Robolectric Supports Split APKs

### Changes Made

#### 3.1 ShadowPackageManager — Split APK Installation

**File**: `shadows/framework/src/main/java/org/robolectric/shadows/ShadowPackageManager.java`

New method `installPackageWithSplits()` provides a convenient API for test setup:

```java
// Install a package with split APKs
shadowOf(packageManager).installPackageWithSplits(
    packageInfo,
    "config.xxhdpi", "config.arm64_v8a", "config.en",  // config splits
    "feature_camera", "feature_maps"                     // dynamic features
);
```

The `setUpPackageStorage()` method now handles split APK directories:
- Creates temporary directories for each split APK
- Sets `splitSourceDirs` and `splitPublicSourceDirs`
- Preserves pre-set paths (doesn't overwrite if already set)

The `populatePackageInfoWithDefaults()` method now propagates `splitNames` from
`PackageInfo` to `ApplicationInfo`.

#### 3.2 ShadowLoadedApk — Split ClassLoader Support

**File**: `shadows/framework/src/main/java/org/robolectric/shadows/ShadowLoadedApk.java`

Enhanced `getSplitClassLoader(String splitName)`:
- Validates the requested split name against registered splits
- Throws `NameNotFoundException` for unknown split names (matching real framework behaviour)
- Checks a per-split cache before falling back to the app ClassLoader (see below)
- New `registerSplitNames()` API for test setup
- New `getRegisteredSplitNames()` for assertions

**ClassLoader sharing API:**

| Method | Description |
|--------|-------------|
| `registerSplitNames(String...)` | Declare which split names exist; `getSplitClassLoader()` will throw for anything else |
| `setSplitClassLoader(String, ClassLoader)` | Register an explicit ClassLoader for a split; `getSplitClassLoader()` returns it |
| `createIsolatedSplitClassLoader(String)` | Create (and cache) a `URLClassLoader` child of the app ClassLoader, simulating Android's `isolatedSplits` mode. Repeated calls return the same instance. |

```java
ShadowLoadedApk shadowApk = Shadow.extract(loadedApk);

// Option A: register an explicit loader
shadowApk.registerSplitNames("feature_camera");
shadowApk.setSplitClassLoader("feature_camera", myCustomLoader);

// Option B: let Robolectric create an isolated child loader
ClassLoader cameraLoader = shadowApk.createIsolatedSplitClassLoader("feature_camera");
ClassLoader mapsLoader   = shadowApk.createIsolatedSplitClassLoader("feature_maps");
// cameraLoader != mapsLoader, but both delegate unknown classes to the app loader
```

#### 3.3 ShadowPackageInstaller — Split Session Tracking and Auto-Install

**File**: `shadows/framework/src/main/java/org/robolectric/shadows/ShadowPackageInstaller.java`

`ShadowSession.openWrite()` now tracks split APK names:
- Each call to `openWrite(name, ...)` records the name
- New `getWrittenSplitNames()` returns the list of names written to the session
- New `isCommitted()` returns `true` after `commit()` is called

**Session commit auto-installs into PackageManager:**

When a session succeeds (either via `commit()` or `setSessionSucceeds()`), Robolectric
automatically registers the package in `ShadowPackageManager` using the written APK names:

- `base.apk` / `base-master.apk` establish the package entry but are **not** treated as split names
- All other filenames have their `.apk` suffix stripped and become split names
- If the package already exists, new splits are appended via `addSplitToInstalledPackage()`
- Sessions with no written APKs are a no-op (backward compatible with existing tests)

```java
int sessionId = installer.createSession(params);
Session session = installer.openSession(sessionId);

session.openWrite("base.apk", 0, -1).close();
session.openWrite("split_feature_camera.apk", 0, -1).close();

session.commit(statusReceiver);   // ← auto-installs package + "split_feature_camera" split

PackageInfo info = pm.getPackageInfo("com.example.app", 0);
assertThat(info.splitNames).asList().contains("split_feature_camera");

ShadowSession shadow = Shadow.extract(session);
assertThat(shadow.isCommitted()).isTrue();
```

#### 3.4 AndroidTestEnvironment — Split Storage Setup

**File**: `robolectric/src/main/java/org/robolectric/android/internal/AndroidTestEnvironment.java`

`setUpPackageStorage()` now handles split APK directories for the test application itself,
if the parsed package has split information.

---

## 4. Testing Patterns for AAB Apps with Robolectric

### Pattern 1: Testing Config Splits

```java
@Test
@Config(minSdk = VERSION_CODES.O)
public void testAppWithConfigSplits() throws Exception {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = "com.example.myapp";

    shadowOf(packageManager).installPackageWithSplits(
        packageInfo, "config.xxhdpi", "config.arm64_v8a", "config.en");

    ApplicationInfo appInfo = packageManager.getApplicationInfo("com.example.myapp", 0);
    assertThat(appInfo.splitNames).asList().contains("config.xxhdpi");
    assertThat(appInfo.splitSourceDirs).hasLength(3);
}
```

### Pattern 2: Testing Dynamic Features

```java
@Test
@Config(minSdk = VERSION_CODES.O)
public void testDynamicFeatureAvailability() throws Exception {
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = "com.example.myapp";

    shadowOf(packageManager).installPackageWithSplits(
        packageInfo, "feature_camera", "feature_maps");

    ApplicationInfo appInfo = packageManager.getApplicationInfo("com.example.myapp", 0);
    assertThat(appInfo.splitNames).asList().contains("feature_camera");
}
```

### Pattern 3: Testing PackageInstaller Sessions

```java
@Test
public void testSplitApkInstallFlow() throws Exception {
    int sessionId = installer.createSession(params);
    Session session = installer.openSession(sessionId);

    session.openWrite("base.apk", 0, -1).close();
    session.openWrite("split_config.xxhdpi.apk", 0, -1).close();

    ShadowSession shadow = Shadow.extract(session);
    assertThat(shadow.getWrittenSplitNames())
        .containsExactly("base.apk", "split_config.xxhdpi.apk");
}
```

### Pattern 4: Testing SplitCompat-like Code

```java
@Test
@Config(minSdk = VERSION_CODES.O)
public void testSplitCompatDiscovery() throws Exception {
    // Set up the package with splits
    PackageInfo packageInfo = new PackageInfo();
    packageInfo.packageName = "com.example.myapp";
    packageInfo.applicationInfo = new ApplicationInfo();
    packageInfo.applicationInfo.packageName = "com.example.myapp";
    packageInfo.applicationInfo.splitNames = new String[]{"feature_camera"};
    packageInfo.applicationInfo.splitSourceDirs = new String[]{"/data/app/feature_camera.apk"};
    packageInfo.applicationInfo.splitPublicSourceDirs = new String[]{"/data/app/feature_camera.apk"};

    shadowOf(packageManager).installPackage(packageInfo);

    // Your SplitCompat-like code queries ApplicationInfo to discover splits
    ApplicationInfo appInfo = packageManager.getApplicationInfo("com.example.myapp", 0);
    assertThat(appInfo.splitSourceDirs).isNotNull();
}
```

### Pattern 5: Testing Per-Split ClassLoader Isolation

```java
@Test
@Config(minSdk = VERSION_CODES.O)
public void testIsolatedSplitClassLoader() throws Exception {
    android.content.Context base = RuntimeEnvironment.getApplication().getBaseContext();
    android.app.LoadedApk loadedApk = ReflectionHelpers.getField(base, "mPackageInfo");
    ShadowLoadedApk shadowApk = Shadow.extract(loadedApk);
    shadowApk.registerSplitNames("feature_camera", "feature_maps");

    // Each split gets its own isolated ClassLoader backed by the app loader
    ClassLoader cameraLoader = shadowApk.createIsolatedSplitClassLoader("feature_camera");
    ClassLoader mapsLoader   = shadowApk.createIsolatedSplitClassLoader("feature_maps");

    assertThat(cameraLoader).isNotSameInstanceAs(mapsLoader);
    // Subsequent calls return the same (cached) instance
    assertThat(shadowApk.createIsolatedSplitClassLoader("feature_camera"))
        .isSameInstanceAs(cameraLoader);
}
```

### Pattern 6: Testing PackageInstaller Session Commit

```java
@Test
@Config(minSdk = VERSION_CODES.O)
public void testCommitInstallsPackageWithSplits() throws Exception {
    SessionParams params = new SessionParams(MODE_FULL_INSTALL);
    params.setAppPackageName("com.example.myapp");

    int sessionId = installer.createSession(params);
    Session session = installer.openSession(sessionId);

    session.openWrite("base.apk", 0, -1).close();
    session.openWrite("split_feature_camera.apk", 0, -1).close();

    session.commit(statusReceiver);

    // Package + splits auto-registered in PackageManager
    PackageInfo info = pm.getPackageInfo("com.example.myapp", 0);
    assertThat(info.splitNames).asList().contains("split_feature_camera");

    ShadowSession shadow = Shadow.extract(session);
    assertThat(shadow.isCommitted()).isTrue();
}
```

---

## 5. Limitations and Future Work

### Current Limitations

1. **API Level Requirement**: Split APK support requires API level 26 (O) or higher because
   `ApplicationInfo.splitNames` was introduced in that version. The `splitSourceDirs` and
   `splitPublicSourceDirs` fields exist from API 21, but full split management requires API 26+.

2. **No Real Resource Loading**: Robolectric simulates split APK metadata (paths, names) but does
   not actually load resources from split APK files. The split source dirs point to temporary
   directories, not real APK files with resources.arsc.
   **Update**: Split source dirs now point to valid empty APK (ZIP) files. For actual resource
   loading, use `installPackageWithSplitApks()` with real APK files or `addSplitAssetPath()`
   to load assets at runtime.

3. **No Dynamic Delivery Simulation**: There is no built-in mechanism to simulate the Play Feature
   Delivery lifecycle (requesting, downloading, installing splits at runtime). Tests must manually
   set up the final state.

### Resource Loading from Split APKs

Robolectric now supports loading raw assets from split APK files. This feature enables testing
of dynamic feature modules and SplitCompat-like patterns where assets are delivered via split APKs.

#### How It Works

1. **Valid APK Files**: Split source dirs now default to valid empty ZIP files instead of directories.
   The framework's `CppApkAssets.Load()` can open them without errors.

2. **Creating Split APKs with Assets**: Use `ShadowPackageManager.createSplitApkWithAssets()` to
   build minimal APK (ZIP) files containing raw assets:

   ```java
   Map<String, byte[]> assets = new LinkedHashMap<>();
   assets.put("feature/config.json", "{\"key\": \"value\"}".getBytes());
   String splitApkPath = ShadowPackageManager.createSplitApkWithAssets("feature_camera", assets);
   ```

3. **Installing with Real Split APKs**: Use `installPackageWithSplitApks()` to install a package
   with actual split APK files (instead of empty placeholders):

   ```java
   Map<String, String> splitPaths = new LinkedHashMap<>();
   splitPaths.put("feature_camera", cameraApkPath);
   splitPaths.put("config.xxhdpi", densityApkPath);
   shadowOf(packageManager).installPackageWithSplitApks(packageInfo, splitPaths);
   ```

4. **Runtime Asset Loading**: Use `ShadowAssetManager.addSplitAssetPath()` to add split APK
   resources to a running app's AssetManager (SplitCompat pattern):

   ```java
   int cookie = ShadowAssetManager.addSplitAssetPath(context.getAssets(), splitApkPath);
   InputStream is = context.getAssets().open("feature/config.json"); // works!
   ```

#### Limitations

- Only raw assets (`assets/` directory) are supported via the test helper. For compiled resources
  (`resources.arsc`), provide a pre-built APK file from your build system.
- `addSplitAssetPath()` uses reflection to call `AssetManager.addAssetPath()`, which is a hidden
  API. This works in Robolectric's test environment but may not reflect exact production behavior.

### Future Work

1. **Play Feature Delivery Shadows**: Create shadow implementations for `SplitInstallManager`,
   `SplitInstallStateUpdatedListener`, and related Play Core APIs.

---

## 6. Dynamic Split Installation

Dynamic split installation allows tests to simulate on-demand feature delivery, where split APKs are
added to an already-installed package at runtime. This mirrors the Play Feature Delivery workflow.

### API

```java
// 1. Install base app
PackageInfo base = new PackageInfo();
base.packageName = "com.example.app";
base.applicationInfo = new ApplicationInfo();
base.applicationInfo.packageName = "com.example.app";
shadowOf(pm).installPackage(base);

// 2. Create and add a dynamic feature split
Map<String, byte[]> assets = Map.of("config.json", "{}".getBytes());
String splitPath = ShadowPackageManager.createSplitApkWithAssets("feature_camera", assets);
shadowOf(pm).addSplitToInstalledPackage("com.example.app", "feature_camera", splitPath);

// 3. Verify the split was registered
PackageInfo updated = pm.getPackageInfo("com.example.app", 0);
// updated.applicationInfo.splitNames contains "feature_camera"
// updated.applicationInfo.splitSourceDirs contains the split APK path
```

### Behavior

- `addSplitToInstalledPackage()` mutates the existing `PackageInfo` in-place
- Appends to `splitNames`, `splitSourceDirs`, and `splitPublicSourceDirs` arrays
- Multiple splits can be added incrementally
- Throws `IllegalArgumentException` if the package is not installed
- Requires API level 26+ (Android O)

---

## 7. Compiled Resource Support (ArscResourceTableBuilder)

The `ArscResourceTableBuilder` generates minimal valid `resources.arsc` binary files that can be
parsed by Robolectric's resource loading pipeline (`LoadedArsc.Load()`).

### API

```java
// Create a split APK with string resources and assets
Map<String, String> resources = new LinkedHashMap<>();
resources.put("feature_name", "Camera Feature");
resources.put("feature_desc", "Take amazing photos");

Map<String, byte[]> assets = Map.of("model.tflite", modelBytes);

String splitPath = ShadowPackageManager.createSplitApkWithResources(
    "feature_camera",        // split name
    "com.example.camera",    // package name
    0x7f,                    // package ID (0x7f for app)
    resources,               // string resources
    assets                   // optional raw assets (nullable)
);
```

### Binary Format Details

The generated `resources.arsc` follows the Android binary resource table format:

```
ResTable_header (12 bytes)
  └─ Global string pool (string values)
  └─ Package chunk
       ├─ Type string pool ("string")
       ├─ Key string pool (entry names)
       ├─ TypeSpec (configuration flags)
       └─ Type (default config, entries referencing global pool)
```

- All strings use UTF-8 encoding
- The `resources.arsc` entry in the ZIP uses STORED compression (per Android convention)
- Resource IDs follow the pattern `packageId:0x01:NNNN` (type 1 = string, entry 0-based)

### Limitations

- Currently only string resources are supported
- Only default configuration (no locale/density variants)
- Package ID must be specified manually

---

## 8. Bundletool Integration (BundletoolSplitApkLoader)

The `BundletoolSplitApkLoader` loads split APKs from bundletool-generated archives or directories,
making it easy to test with real or simulated bundletool output.

### Bundletool .apks Archive Format

```
app.apks (ZIP archive)
├── splits/
│   ├── base-master.apk
│   ├── base-xxhdpi.apk
│   ├── base-arm64_v8a.apk
│   └── base-en.apk
└── toc.pb
```

### API

```java
// Load from a .apks archive
Map<String, String> splits = BundletoolSplitApkLoader.loadFromApksArchive(
    Paths.get("app.apks"));

// Load from a directory of APK files
Map<String, String> splits = BundletoolSplitApkLoader.loadFromDirectory(
    Paths.get("splits/"));

// Filter by module prefix
Map<String, String> cameraSplits = BundletoolSplitApkLoader.loadFromDirectory(
    Paths.get("splits/"), "camera-");

// Create a test .apks archive
Map<String, String> splitPaths = Map.of("base-master", basePath, "base-en", enPath);
Path archive = BundletoolSplitApkLoader.createApksArchive(splitPaths);
```

### Features

- Extracts APK files from `splits/` directory within `.apks` ZIP archives
- Derives split names from APK filenames (e.g., `base-xxhdpi.apk` → `"base-xxhdpi"`)
- Supports prefix filtering to load only specific module splits
- Provides `createApksArchive()` to build test fixtures
- Returns `Map<String, String>` compatible with `installPackageWithSplitApks()`

---

## 9. Test Coverage Summary

Integration tests are modeled after the official Android App Bundle samples from
[github.com/android/app-bundle-samples/DynamicFeatures](https://github.com/android/app-bundle-samples/tree/main/DynamicFeatures).
Each test class maps to a specific pattern from the official sample:

### Unit Tests (robolectric module)

| Test Suite | Test Methods | Description |
|-----------|-------|-------------|
| `ShadowPackageManagerSplitApkTest` | 10 | Split APK installation APIs |
| `ShadowPackageInstallerSplitTest` | 5 | Session-based split tracking (`getWrittenSplitNames`) |
| `ShadowPackageManagerSplitResourceTest` | 9 | Split APK resource/asset loading |
| `ShadowPackageManagerDynamicSplitTest` | 16 | Dynamic splits, ARSC builder, bundletool |
| `ShadowLoadedApkClassLoaderTest` | 8 | Per-split ClassLoader caching and isolation |
| `ShadowPackageInstallerCommitTest` | 7 | Session commit auto-installs package + splits |

### Integration Tests (integration_tests/split-apk)

Based on the official [DynamicFeatures](https://github.com/android/app-bundle-samples/tree/main/DynamicFeatures) sample:

| Test Suite | Test Methods | Official Sample Reference |
|-----------|-------|-------------|
| `SplitCompatApplicationTest` | 3 | `MyApplication.kt` — SplitCompat.install() in attachBaseContext() |
| `DynamicFeatureDeliveryTest` | 7 | `MainActivity.kt` — SplitInstallManager on-demand delivery |
| `AssetOnlyModuleTest` | 4 | `features/assets/` — hasCode=false, asset-only module |
| `ConfigSplitsTest` | 5 | Config splits (density, ABI, language) + LanguageHelper |
| `BundletoolWorkflowTest` | 5 | Bundletool .apks archive → install → verify workflow |
| `CompiledResourcesTest` | 5 | Feature module resources.arsc (kotlin, java, initialInstall) |
| **Total** | **69** | Each method runs across multiple SDK levels (26+) |

All tests pass across all supported SDK levels (26+).
