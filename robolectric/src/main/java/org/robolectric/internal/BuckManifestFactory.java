package org.robolectric.internal;

import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.res.ResourcePath;
import org.robolectric.util.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;

public class BuckManifestFactory implements ManifestFactory {

  private static final String BUCK_ROBOLECTRIC_RES_DIRECTORIES = "buck.robolectric_res_directories";
  private static final String BUCK_ROBOLECTRIC_ASSETS_DIRECTORIES = "buck.robolectric_assets_directories";
  private static final String BUCK_ROBOLECTRIC_MANIFEST = "buck.robolectric_manifest";

  @Override
  public ManifestIdentifier identify(Config config) {
    String buckManifest = System.getProperty(BUCK_ROBOLECTRIC_MANIFEST);
    FsFile manifestFile = Fs.fileFromPath(buckManifest);
    return new ManifestIdentifier(manifestFile, null, null, config.packageName(), null);
  }

  @Override
  public AndroidManifest create(ManifestIdentifier manifestIdentifier) {
    String buckResDirs = System.getProperty(BUCK_ROBOLECTRIC_RES_DIRECTORIES);
    String buckAssetsDirs = System.getProperty(BUCK_ROBOLECTRIC_ASSETS_DIRECTORIES);
    String packageName = manifestIdentifier.getPackageName();
    FsFile manifestFile = manifestIdentifier.getManifestFile();

    final List<String> buckResources = buckResDirs == null ? null :
            Arrays.asList(buckResDirs.split(File.pathSeparator));
    final List<String> buckAssets = buckAssetsDirs == null ? null :
              Arrays.asList(buckAssetsDirs.split(File.pathSeparator));

    final FsFile resDir = (buckResources == null || "".equals(buckResources)) ? null :
            Fs.fileFromPath(buckResources.get(buckResources.size() - 1));
    final FsFile assetsDir = (buckAssets == null || "".equals(buckAssets)) ? null :
            Fs.fileFromPath(buckAssets.get(buckAssets.size() - 1));

    Logger.debug("Robolectric assets directory: " + (assetsDir == null ? null : assetsDir.getPath()));
    Logger.debug("   Robolectric res directory: " + (resDir == null ? null : resDir.getPath()));
    Logger.debug("   Robolectric manifest path: " + manifestFile.getPath());
    Logger.debug("    Robolectric package name: " + packageName);

    return new AndroidManifest(manifestFile, resDir, assetsDir, packageName) {
        @Override
        public List<ResourcePath> getIncludedResourcePaths() {
            Collection<ResourcePath> resourcePaths = new LinkedHashSet<>(); // Needs stable ordering and no duplicates
            resourcePaths.add(super.getResourcePath());

            // Add all the buck resource folders, no duplicates as they are being added to a set.
            if (buckResources != null) {
                ListIterator<String> it = buckResources.listIterator(buckResources.size());
                while (it.hasPrevious()) {
                    resourcePaths.add(new ResourcePath(
                            getRClass(),
                            Fs.fileFromPath(it.previous()),
                            getAssetsDirectory()));
                }
            }
            return new ArrayList<>(resourcePaths);
        }
    };
  }

  public static boolean isBuck() {
    return System.getProperty(BUCK_ROBOLECTRIC_MANIFEST) != null;
  }
}
