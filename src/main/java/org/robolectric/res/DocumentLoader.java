package org.robolectric.res;

import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentLoader {
  public static boolean DEBUG_PERF = false;
  private Map<String, Long> perfResponsibleParties = new HashMap<String, Long>();

  private static final FsFile.Filter ENDS_WITH_XML = new FsFile.Filter() {
    @Override public boolean accept(@NotNull FsFile fsFile) {
      return fsFile.getName().endsWith(".xml");
    }
  };

  private final FsFile resourceBase;
  private final String packageName;
  private final VTDGen vtdGen;

  public DocumentLoader(ResourcePath resourcePath) {
    this.resourceBase = resourcePath.resourceBase;
    this.packageName = resourcePath.getPackageName();
    vtdGen = new VTDGen();
  }

  public void load(String folderBaseName, XmlLoader... xmlLoaders) throws Exception {
    long startTime = System.currentTimeMillis();
    if (DEBUG_PERF) perfResponsibleParties.clear();

    FsFile[] files = resourceBase.listFiles(new DirectoryMatchingFilter(folderBaseName));
    if (files == null) {
      throw new RuntimeException(resourceBase.join(folderBaseName) + " is not a directory");
    }
    for (FsFile dir : files) {
      loadFile(dir, xmlLoaders);
    }

    if (DEBUG_PERF) {
      System.out.println(String.format("%4dms spent in %s", System.currentTimeMillis() - startTime, folderBaseName));
      List<String> keys = new ArrayList<String>(perfResponsibleParties.keySet());
      Collections.sort(keys);
      for (String key : keys) {
        System.out.println(String.format("* %-20s: %4dms", key, perfResponsibleParties.get(key)));
      }
    }
  }

  private void loadFile(FsFile dir, XmlLoader[] xmlLoaders) throws Exception {
    if (!dir.exists()) {
      throw new RuntimeException("no such directory " + dir);
    }

    for (FsFile file : dir.listFiles(ENDS_WITH_XML)) {
      loadResourceXmlFile(file, xmlLoaders);
    }
  }

  private void loadResourceXmlFile(FsFile fsFile, XmlLoader... xmlLoaders) throws Exception {
    long startTime = DEBUG_PERF ? System.currentTimeMillis() : 0;
    VTDNav vtdNav = parse(fsFile);
    if (DEBUG_PERF) perfBlame("DocumentLoader.parse", startTime);

    for (XmlLoader xmlLoader : xmlLoaders) {
      startTime = DEBUG_PERF ? System.currentTimeMillis() : 0;
      xmlLoader.processResourceXml(fsFile, vtdNav, packageName);
      if (DEBUG_PERF) perfBlame(xmlLoader.getClass().getName(), startTime);
    }
  }

  private void perfBlame(String responsibleParty, long startTime) {
    long myElapsedMs = System.currentTimeMillis() - startTime;
    Long totalElapsedMs = perfResponsibleParties.get(responsibleParty);
    perfResponsibleParties.put(responsibleParty, totalElapsedMs == null ? myElapsedMs : totalElapsedMs + myElapsedMs);
  }

  private VTDNav parse(FsFile xmlFile) throws Exception {
    byte[] bytes = xmlFile.getBytes();
    vtdGen.setDoc(bytes);
    vtdGen.parse(true);

    return vtdGen.getNav();
  }

  private static class DirectoryMatchingFilter implements FsFile.Filter {
    private final String folderBaseName;

    public DirectoryMatchingFilter(String folderBaseName) {
      this.folderBaseName = folderBaseName;
    }

    @Override
    public boolean accept(FsFile file) {
      return file.getName().startsWith(folderBaseName);
    }
  }

}
