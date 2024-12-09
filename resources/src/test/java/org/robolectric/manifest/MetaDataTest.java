package org.robolectric.manifest;

import static org.junit.Assert.assertThrows;

import com.google.common.collect.ImmutableList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.res.ResourceTable;
import org.w3c.dom.Element;

/** Tests for {@link MetaData} */
@RunWith(JUnit4.class)
public class MetaDataTest {

  @Mock private ResourceTable resourceProvider;
  private AutoCloseable mock;

  @Before
  public void setUp() {
    mock = MockitoAnnotations.openMocks(this);
  }

  @After
  public void tearDown() throws Exception {
    mock.close();
  }

  @Test
  public void testNonExistentResource_throwsResourceNotFoundException() {
    Element metaDataElement = createMetaDataNode("aName", "@xml/non_existent_resource");

    MetaData metaData = new MetaData(ImmutableList.of(metaDataElement));

    assertThrows(RoboNotFoundException.class, () -> metaData.init(resourceProvider, "a.package"));
  }

  private static Element createMetaDataNode(String name, String value) {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    Element metaDataElement;
    try {
      DocumentBuilder db = dbf.newDocumentBuilder();
      metaDataElement = db.newDocument().createElement("meta-data");
      metaDataElement.setAttribute("android:name", name);
      metaDataElement.setAttribute("android:value", value);
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    }
    return metaDataElement;
  }
}
