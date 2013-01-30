package com.xtremelabs.robolectric.res;

import com.xtremelabs.robolectric.tester.android.util.Attribute;
import com.xtremelabs.robolectric.tester.android.util.ResName;
import org.junit.Test;
import org.w3c.dom.Node;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AttributeTest {
    @Test public void shouldConstructFromW3cNode() throws Exception {
        Node node = mockNode("http://schemas.android.com/apk/res/com.xtremelabs.robolectric", "tagName", "contents");
        Attribute attribute = new Attribute(node, new XmlLoader.XmlContext("package.name", null));
        assertEquals(new ResName("com.xtremelabs.robolectric", "attr", "tagName"), attribute.resName);
        assertEquals("contents", attribute.value);
    }

    @Test public void withResAutoNamespace_shouldConstructFromW3cNode() throws Exception {
        Node node = mockNode("http://schemas.android.com/apk/res-auto", "tagName", "contents");
        Attribute attribute = new Attribute(node, new XmlLoader.XmlContext("package.name", null));
        assertEquals(new ResName("package.name", "attr", "tagName"), attribute.resName);
        assertEquals("contents", attribute.value);
    }

    private Node mockNode(String namespace, String tagName, String contents) {
        Node node = mock(Node.class);
        when(node.getNamespaceURI()).thenReturn(namespace);
        when(node.getLocalName()).thenReturn(tagName);
        when(node.getNodeValue()).thenReturn(contents);
        return node;
    }
}
