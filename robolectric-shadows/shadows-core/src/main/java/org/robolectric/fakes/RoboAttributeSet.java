package org.robolectric.fakes;

import android.content.Context;
import android.util.AttributeSet;
import com.google.android.collect.Lists;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.res.Attribute;
import org.robolectric.res.ResourceProvider;
import org.robolectric.res.builder.XmlResourceParserImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.List;

import static org.robolectric.Shadows.shadowOf;

/**
 * @deprecated Use {@link Robolectric#buildAttributeSet()}
 */
@Deprecated
public class RoboAttributeSet {

  /**
   * Robolectric implementation of {@link android.util.AttributeSet}.
   * @deprecated Use {@link Robolectric#buildAttributeSet()}
   */
  @Deprecated  public static AttributeSet create(Context context, Attribute... attrs) {
    List<Attribute> attributesList = Lists.newArrayList(attrs);
    return create(context, attributesList);
  }

  /**
   * Robolectric implementation of {@link android.util.AttributeSet}.
   * @deprecated Use {@link Robolectric#buildAttributeSet()}
   */
  @Deprecated
  public static AttributeSet create(Context context, List<Attribute> attributesList) {
    return create(context, attributesList, shadowOf(context.getAssets()).getResourceProvider());
  }

  /**
   * Robolectric implementation of {@link android.util.AttributeSet}.
   * @deprecated Use {@link Robolectric#buildAttributeSet()}
   */
  @Deprecated
  public static AttributeSet create(Context context, List<Attribute> attributesList, ResourceProvider resourceProvider) {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(true);
    factory.setIgnoringComments(true);
    factory.setIgnoringElementContentWhitespace(true);
    Document document;
    try {
      DocumentBuilder documentBuilder = factory.newDocumentBuilder();
      document = documentBuilder.newDocument();
      Element dummy = document.createElementNS("http://schemas.android.com/apk/res/" + RuntimeEnvironment.application.getPackageName(), "dummy");
      for (Attribute attribute : attributesList) {
        if ("style".equals(attribute.resName.name)) {
          dummy.setAttribute(attribute.resName.name, attribute.value);
        } else {
          dummy.setAttributeNS(attribute.resName.getNamespaceUri(), attribute.resName.packageName + ":" + attribute.resName.name, attribute.value);
        }
      }
      document.appendChild(dummy);

      XmlResourceParserImpl parser = new XmlResourceParserImpl(document, null, context.getPackageName(), context.getPackageName(), resourceProvider);
      parser.next(); // Root document element
      parser.next(); // "dummy" element
      return parser;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
