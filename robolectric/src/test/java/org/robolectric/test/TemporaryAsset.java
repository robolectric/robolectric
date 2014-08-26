package org.robolectric.test;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import org.junit.rules.ExternalResource;
import org.robolectric.AndroidManifest;
import org.robolectric.res.FileFsFile;

/**
 * The TemporaryAsset Rule allows creation of assets based on an ApplicationManifest.
 *
 * <pre>
 * public static class HasTempFolder {
 *   &#064;Rule
 *  public TemporaryAsset temporaryAsset = new TemporaryAsset();

 *
 *   &#064;Test
 *   public void testUsingTempFolder() throws IOException {
 *     AndroidManifest appManifest = shadowOf(Robolectric.application).getAppManifest();
 *     fontFile = temporaryAsset.createFile(appManifest, &quot;myFont.ttf&quot;, &quot;myFontData&quot;);
 *     // ...
 *   }
 * }
 * </pre>
 */
public class TemporaryAsset extends ExternalResource {
  List<File> assetsToDelete = new ArrayList<File>();

  @Override protected void after() {
    for (File file : assetsToDelete) {
      file.delete();
    }
  }

  public File createFile(AndroidManifest manifest, String fileName, String contents) throws Exception {
    File assetBase = ((FileFsFile) manifest.getAssetsDirectory()).getFile();
    File file = new File(assetBase, fileName);
    file.getParentFile().mkdirs();

    FileWriter fileWriter = new FileWriter(file);
    try {
       fileWriter.write(contents);
    } finally {
      fileWriter.close();
    }

    assetsToDelete.add(file);
    return file;
  }
}
