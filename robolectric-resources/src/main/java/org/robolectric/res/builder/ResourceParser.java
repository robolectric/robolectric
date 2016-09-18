package org.robolectric.res.builder;

import android.content.res.Resources;
import android.content.res.XmlResourceParser;

import org.robolectric.res.*;
import org.w3c.dom.Document;

public class ResourceParser {

  public static XmlResourceParser from(XmlBlock block, String applicationPackageName, ResourceLoader resourceLoader) {
    return new XmlResourceParserImpl(block.getDocument(), block.getFilename(), block.getPackageName(),
        applicationPackageName, resourceLoader);
  }

  public static XmlResourceParser create(String file, String packageName, String applicationPackageName, ResourceLoader resourceLoader) {
    FsFile fsFile = Fs.fileFromPath(file);
    Document document = new XmlBlockLoader(null, "xml").parse(fsFile);
    if (document == null) {
      throw new Resources.NotFoundException("couldn't find resource " + fsFile.getPath());
    }
    XmlBlock block = XmlBlock.create(document, file, packageName);
    return from(block, applicationPackageName, resourceLoader);
  }
}
