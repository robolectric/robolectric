package org.robolectric.res.builder;

import android.content.res.XmlResourceParser;
import org.robolectric.res.ResourceLoader;

public class ResourceParser {

  public static XmlResourceParser from(XmlBlock block, String applicationPackageName, ResourceLoader resourceLoader) {
    return new XmlResourceParserImpl(block.getDocument(), block.getFilename(), block.getPackageName(),
        applicationPackageName, resourceLoader);
  }
}
