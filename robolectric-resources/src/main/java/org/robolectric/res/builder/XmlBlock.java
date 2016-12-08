package org.robolectric.res.builder;

import android.content.res.Resources;
import org.jetbrains.annotations.NotNull;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.res.XmlLoader;
import org.w3c.dom.Document;

/**
 * An XML block is a parsed representation of a resource XML file. Similar in nature
 * to Android's XmlBlock class.
 */
public class XmlBlock {
  private final Document document;
  private final String filename;
  private final String packageName;

  public static XmlBlock create(String file, String packageName) {
    return create(Fs.fileFromPath(file), packageName);
  }

  @NotNull
  public static XmlBlock create(FsFile fsFile, String packageName) {
    if (!fsFile.exists()) {
      throw new Resources.NotFoundException("couldn't find resource " + fsFile.getPath());
    }
    Document document = XmlLoader.parse(fsFile);
    return new XmlBlock(document, fsFile.getPath(), packageName);
  }

  public Document getDocument() {
    return document;
  }

  public String getFilename() {
    return filename;
  }

  public String getPackageName() {
    return packageName;
  }

  private XmlBlock(Document document, String filename, String packageName) {
    this.document = document;
    this.filename = filename;
    this.packageName = packageName;
  }
}
