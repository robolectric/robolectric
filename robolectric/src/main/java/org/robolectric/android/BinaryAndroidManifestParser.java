package org.robolectric.android;

import android.content.res.XmlResourceParser;
import com.google.common.io.ByteStreams;
import java.io.IOException;
import java.io.InputStream;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.manifest.AndroidManifestParser;
import org.robolectric.res.FsFile;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.xmlpull.v1.XmlPullParserException;

public class BinaryAndroidManifestParser implements AndroidManifestParser {

  @Override
  public void parse(FsFile androidManifestFile, AndroidManifest androidManifest) {
    try (InputStream inputStream = androidManifestFile.getInputStream()) {
      byte[] bytes = ByteStreams.toByteArray(inputStream);
      Class<?> xmlBlockClass = ReflectionHelpers
          .loadClass(this.getClass().getClassLoader(), "android.content.res.XmlBlock");

      Object xmlBlockInstance = ReflectionHelpers
          .callConstructor(xmlBlockClass, ClassParameter.from(byte[].class, bytes));

      XmlResourceParser parser = ReflectionHelpers.callInstanceMethod(xmlBlockClass, xmlBlockInstance,
          "newParser");
      assert (parser.next() == XmlResourceParser.START_DOCUMENT);
      assert (parser.nextTag() == XmlResourceParser.START_TAG);
      assert (parser.getName().equals("manifest"));
      androidManifest.packageName = parser.getAttributeValue(null, "package");


    } catch (IOException | XmlPullParserException e) {
      throw new RuntimeException(e);
    }
  }
}
