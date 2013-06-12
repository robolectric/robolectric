package org.robolectric.res;

import org.junit.Test;
import org.robolectric.util.TestUtil;
import org.w3c.dom.Node;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AttributeTest {
  @Test public void shouldConstructFromW3cNode() throws Exception {
    Node node = mockNode("http://schemas.android.com/apk/res/org.robolectric", "tagName", "contents");
    Attribute attribute = new Attribute(node, new XmlLoader.XmlContext("package.name", null));
    assertEquals(new ResName(TestUtil.TEST_PACKAGE, "attr", "tagName"), attribute.resName);
    assertEquals("contents", attribute.value);
  }

  @Test public void withResAutoNamespace_shouldConstructFromW3cNode() throws Exception {
    Node node = mockNode("http://schemas.android.com/apk/res-auto", "tagName", "contents");
    Attribute attribute = new Attribute(node, new XmlLoader.XmlContext("package.name", null));
    assertEquals(new ResName("package.name", "attr", "tagName"), attribute.resName);
    assertEquals("contents", attribute.value);
  }

  @Test public void qualifyName_shouldAddPackageIfMissing() throws Exception {
    assertThat(Attribute.qualifyName("android:id", "android")).isEqualTo("android:id");
    assertThat(Attribute.qualifyName("my:id", "android")).isEqualTo("my:id");
    assertThat(Attribute.qualifyName("id", "android")).isEqualTo("android:id");
    assertThat(Attribute.qualifyName("id", "my.package")).isEqualTo("my.package:id");
  }

  private Node mockNode(String namespace, String tagName, String contents) {
    Node node = mock(Node.class);
    when(node.getNamespaceURI()).thenReturn(namespace);
    when(node.getLocalName()).thenReturn(tagName);
    when(node.getNodeValue()).thenReturn(contents);
    return node;
  }
}
