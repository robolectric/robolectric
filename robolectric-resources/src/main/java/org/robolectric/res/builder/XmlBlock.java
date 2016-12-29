package org.robolectric.res.builder;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import org.jetbrains.annotations.NotNull;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.res.ResourceProvider;
import org.robolectric.res.XmlBlockLoader;
import org.w3c.dom.Document;

/**
 * An XML block is a parsed representation of a resource XML file. Similar in nature
 * to Android's XmlBlock class.
 */
public class XmlBlock {
  private final Document document;
  private final String filename;
  private final String packageName;

  public static XmlBlock create(Document document, String file, String packageName) {
    return new XmlBlock(document, file, packageName);
  }

  @NotNull
  public static XmlBlock create(String file, String packageName) {
    FsFile fsFile = Fs.fileFromPath(file);
    Document document = new XmlBlockLoader(null, "xml").parse(fsFile);
    if (document == null) {
      throw new Resources.NotFoundException("couldn't find resource " + fsFile.getPath());
    }
    return create(document, file, packageName);
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
