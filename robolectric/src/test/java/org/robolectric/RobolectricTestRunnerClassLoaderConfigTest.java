package org.robolectric;

import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLReporter;
import javax.xml.stream.XMLResolver;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.XMLEventAllocator;
import javax.xml.transform.Source;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.internal.AndroidSandbox;
import org.robolectric.internal.bytecode.SandboxClassLoader;
import org.robolectric.res.StaxDocumentLoader;
import org.robolectric.test.DummyClass;

@RunWith(AndroidJUnit4.class)
public class RobolectricTestRunnerClassLoaderConfigTest {
  @Rule public TemporaryFolder tempFolder = new TemporaryFolder();

  @Test
  public void testUsingClassLoader() {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    assertThat(classLoader.getClass().getName())
        .isEqualTo(AndroidSandbox.SdkSandboxClassLoader.class.getName());
  }

  @Test
  public void testGetPackage() {
    assertThat(DummyClass.class.getClassLoader()).isInstanceOf(SandboxClassLoader.class);
    assertThat(DummyClass.class.getPackage()).isNotNull();
    assertThat(DummyClass.class.getName()).startsWith(DummyClass.class.getPackage().getName());
  }

  @Test
  public void testPackagesFromParentClassLoaderAreMadeAvailableByName() {
    assertThat(Test.class.getPackage()).isNotNull();
    assertThat(Package.getPackage("org.junit")).isNotNull();
    assertThat(Package.getPackage("org.junit")).isEqualTo(Test.class.getPackage());
  }

  @Test
  public void staxServiceProviderInSandboxClassLoader() throws Exception {
    File root = tempFolder.newFolder("stax-services");
    Path serviceFile = root.toPath().resolve("META-INF/services/javax.xml.stream.XMLInputFactory");
    Files.createDirectories(serviceFile.getParent());
    Files.write(serviceFile, TestXmlInputFactory.class.getName().getBytes(StandardCharsets.UTF_8));

    ClassLoader priorCl = Thread.currentThread().getContextClassLoader();
    assertThat(priorCl).isInstanceOf(SandboxClassLoader.class);
    assertThat(TestXmlInputFactory.class.getClassLoader()).isSameInstanceAs(priorCl);

    try (URLClassLoader serviceCl = new URLClassLoader(new URL[] {root.toURI().toURL()}, priorCl)) {
      Thread.currentThread().setContextClassLoader(serviceCl);
      XMLInputFactory factory = XMLInputFactory.newFactory();
      assertThat(factory).isInstanceOf(TestXmlInputFactory.class);
      assertThat(factory.getClass().getClassLoader()).isSameInstanceAs(priorCl);
      new StaxDocumentLoader("pkg", null, null);
    } finally {
      Thread.currentThread().setContextClassLoader(priorCl);
    }
  }

  public static class TestXmlInputFactory extends XMLInputFactory {
    @Override
    public XMLStreamReader createXMLStreamReader(Reader reader) {
      return null;
    }

    @Override
    public XMLStreamReader createXMLStreamReader(Source source) {
      return null;
    }

    @Override
    public XMLStreamReader createXMLStreamReader(InputStream stream) {
      return null;
    }

    @Override
    public XMLStreamReader createXMLStreamReader(InputStream stream, String encoding) {
      return null;
    }

    @Override
    public XMLStreamReader createXMLStreamReader(String systemId, InputStream stream) {
      return null;
    }

    @Override
    public XMLStreamReader createXMLStreamReader(String systemId, Reader reader) {
      return null;
    }

    @Override
    public XMLEventReader createXMLEventReader(Reader reader) {
      return null;
    }

    @Override
    public XMLEventReader createXMLEventReader(String systemId, Reader reader) {
      return null;
    }

    @Override
    public XMLEventReader createXMLEventReader(XMLStreamReader reader) {
      return null;
    }

    @Override
    public XMLEventReader createXMLEventReader(Source source) {
      return null;
    }

    @Override
    public XMLEventReader createXMLEventReader(InputStream stream) {
      return null;
    }

    @Override
    public XMLEventReader createXMLEventReader(InputStream stream, String encoding) {
      return null;
    }

    @Override
    public XMLEventReader createXMLEventReader(String systemId, InputStream stream) {
      return null;
    }

    @Override
    public XMLStreamReader createFilteredReader(
        XMLStreamReader reader, javax.xml.stream.StreamFilter filter) {
      return null;
    }

    @Override
    public XMLEventReader createFilteredReader(
        XMLEventReader reader, javax.xml.stream.EventFilter filter) {
      return null;
    }

    @Override
    public XMLResolver getXMLResolver() {
      return null;
    }

    @Override
    public void setXMLResolver(XMLResolver resolver) {}

    @Override
    public XMLReporter getXMLReporter() {
      return null;
    }

    @Override
    public void setXMLReporter(XMLReporter reporter) {}

    @Override
    public void setProperty(String name, Object value) {}

    @Override
    public Object getProperty(String name) {
      return null;
    }

    @Override
    public boolean isPropertySupported(String name) {
      return false;
    }

    @Override
    public void setEventAllocator(XMLEventAllocator allocator) {}

    @Override
    public XMLEventAllocator getEventAllocator() {
      return null;
    }
  }
}
