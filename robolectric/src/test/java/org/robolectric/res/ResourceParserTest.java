package org.robolectric.res;

import org.junit.Before;
import org.junit.Test;
import org.robolectric.internal.SdkConfig;
import org.robolectric.internal.dependency.MavenDependencyResolver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.util.TestUtil.gradleAppResources;
import static org.robolectric.util.TestUtil.testResources;

public class ResourceParserTest {

  private ResourceTable resourceTable;
  private ResourceTable gradleResourceTable;

  @Before
  public void setUp() {
    ResourceTableFactory resourceTableFactory = new ResourceTableFactory();
    resourceTable = resourceTableFactory.newResourceTable("org.robolectric", testResources());
    gradleResourceTable = resourceTableFactory.newResourceTable("org.robolectric.gradleapp", gradleAppResources());
  }

  @Test
  public void compareApp() throws Exception {
    Fs sdkResFs = Fs.fromJar(new MavenDependencyResolver().getLocalArtifactUrl(new SdkConfig(25).getAndroidSdkDependency()));
    ResourcePath sdkRes = new ResourcePath(null, sdkResFs.join("res"), null, null);

    PackageResourceTable staxResources = new ResourceTableFactory(true)
        .newResourceTable("org.robolectric", testResources());
    assertThat(stringify(staxResources)).isEqualTo(stringify(resourceTable));
  }

  @Test
  public void compareSdk() throws Exception {
    Fs sdkResFs = Fs.fromJar(new MavenDependencyResolver().getLocalArtifactUrl(new SdkConfig(25).getAndroidSdkDependency()));
    final ResourcePath sdkRes = new ResourcePath(null, sdkResFs.join("res"), null, null);

    PackageResourceTable oldResources;
//    oldResources = new ResourceTableFactory(false).newResourceTable("android", sdkRes);

    PackageResourceTable staxResources;
    staxResources = new ResourceTableFactory(true).newResourceTable("android", sdkRes);

//    try (BufferedWriter out = new BufferedWriter(new FileWriter(new File("vtd.txt")))) {
//      out.write(stringify(oldResources));
//    }

    try (BufferedWriter out = new BufferedWriter(new FileWriter(new File("stax.txt")))) {
      out.write(stringify(staxResources));
    }

    time("old", new Runnable() {
      @Override
      public void run() {
        new ResourceTableFactory().newResourceTable("android", sdkRes);
      }
    });
    time("new", new Runnable() {
      @Override
      public void run() {
        new ResourceTableFactory(true).newResourceTable("android", sdkRes);
      }
    });

    time("old", new Runnable() {
      @Override
      public void run() {
        new ResourceTableFactory().newResourceTable("android", sdkRes);
      }
    });
    time("new", new Runnable() {
      @Override
      public void run() {
        new ResourceTableFactory(true).newResourceTable("android", sdkRes);
      }
    });

//    assertThat(stringify(staxResources)).isEqualTo(stringify(oldResources));
  }

  private void time(String message, Runnable runnable) {
    long startTime = System.nanoTime();
    for (int i = 0; i < 10; i++) {
      runnable.run();
    }
    long elapsed = System.nanoTime() - startTime;
    System.out.println("elapsed " + message + ": " + (elapsed / 1000000.0) + "ms");
  }

  private static String stringify(ResourceTable resourceTable) {
    final HashMap<String, List<TypedResource>> map = new HashMap<>();
    resourceTable.receive(new ResourceTable.Visitor() {
      @Override
      public void visit(ResName key, Iterable<TypedResource> values) {
        List<TypedResource> v = new ArrayList<>();
        for (TypedResource value : values) {
          v.add(value);
        }
        map.put(key.getFullyQualifiedName(), v);
      }
    });
    StringBuilder buf = new StringBuilder();
    TreeSet<String> keys = new TreeSet<>(map.keySet());
    for (String key : keys) {
//      if (!key.contains(":plurals/")) {
//        continue;
//      }
      buf.append(key).append(":\n");
      for (TypedResource typedResource : map.get(key)) {
        Object data = typedResource.getData();
        if (data instanceof List) {
          ArrayList<String> newList = new ArrayList<>();
          for (Object item : ((List) data)) {
            if (item.getClass().equals(TypedResource.class)) {
              TypedResource typedResourceItem = (TypedResource) item;
              newList.add(typedResourceItem.getData().toString() + " (" + typedResourceItem.getResType() + ")");
            } else {
              newList.add(item.toString());
            }
          }
          data = newList.toString();
        }
        buf.append("  ").append(data).append(" {").append(typedResource.getResType())
            .append("/").append(typedResource.getQualifiers()).append(": ")
            .append(shortContext(typedResource)).append("}").append("\n");
      }
    }
    return buf.toString();
  }

  private static String shortContext(TypedResource typedResource) {
    return typedResource.getXmlContext().toString().replaceAll("jar:/usr/local/google/home/.*\\.jar\\!", "jar:");
  }

  @Test
  public void shouldLoadDrawableXmlResources() throws Exception {
    TypedResource value = resourceTable.getValue(new ResName("org.robolectric", "drawable", "rainbow"), "");
    assertThat(value).isNotNull();
    assertThat(value.getResType()).isEqualTo(ResType.DRAWABLE);
    assertThat(value.isFile()).isTrue();
    assertThat((String) value.getData()).contains("rainbow.xml");
  }

  @Test
  public void shouldLoadDrawableBitmapResources() throws Exception {
    TypedResource value = resourceTable.getValue(new ResName("org.robolectric", "drawable", "an_image"), "");
    assertThat(value).isNotNull();
    assertThat(value.getResType()).isEqualTo(ResType.DRAWABLE);
    assertThat(value.isFile()).isTrue();
    assertThat((String) value.getData()).contains("an_image.png");
  }

  @Test
  public void shouldLoadDrawableBitmapResourcesDefinedByItemTag() throws Exception {
    TypedResource value = resourceTable.getValue(new ResName("org.robolectric", "drawable", "example_item_drawable"), "");
    assertThat(value).isNotNull();
    assertThat(value.getResType()).isEqualTo(ResType.DRAWABLE);
    assertThat(value.isReference()).isTrue();
    assertThat((String) value.getData()).isEqualTo("@drawable/an_image");
  }

  @Test
  public void shouldLoadIdResourcesDefinedByItemTag() throws Exception {
    TypedResource value = resourceTable.getValue(new ResName("org.robolectric", "id", "id_declared_in_item_tag"), "");
    assertThat(value).isNotNull();
    assertThat(value.getResType()).isEqualTo(ResType.CHAR_SEQUENCE);
    assertThat(value.isReference()).isFalse();
    assertThat(value.asString()).isEqualTo("");
    assertThat((String) value.getData()).isEqualTo("");
  }

  @Test
  public void shouldLoadIdResourcesDefinedByIdTag() throws Exception {
    TypedResource value = resourceTable.getValue(new ResName("org.robolectric", "id", "id_declared_in_id_tag"), "");
    assertThat(value).isNotNull();
    assertThat(value.getResType()).isEqualTo(ResType.CHAR_SEQUENCE);
    assertThat(value.isReference()).isFalse();
    assertThat(value.asString()).isEqualTo("");
    assertThat((String) value.getData()).isEqualTo("");
  }

  @Test
  public void whenIdItemsHaveStringContent_shouldLoadIdResourcesDefinedByItemTag() throws Exception {
    TypedResource value2 = resourceTable.getValue(new ResName("org.robolectric", "id", "id_with_string_value"), "");
    assertThat(value2.asString()).isEqualTo("string value");
  }

  @Test
  public void shouldLoadResourcesFromGradleOutputDirectories() {
    TypedResource value = gradleResourceTable.getValue(new ResName("org.robolectric.gradleapp", "string", "from_gradle_output"), "");
    assertThat(value).describedAs("String from gradle output is not loaded").isNotNull();
    assertThat(value.asString()).isEqualTo("string example taken from gradle output directory");
  }

  @Test
  public void shouldLoadDimenResourcesFromGradleOutputDirectoriesDefinedByDimenTag() {
    TypedResource value = gradleResourceTable.getValue(new ResName("org.robolectric.gradleapp", "dimen", "example_dimen"), "");
    assertThat(value).describedAs("Dimen from gradle output is not loaded").isNotNull();
    assertThat(value.asString()).isEqualTo("8dp");
  }

  @Test
  public void shouldLoadDimenResourcesFromGradleOutputDirectoriesDefinedByItemTag() {
    TypedResource value = gradleResourceTable.getValue(new ResName("org.robolectric.gradleapp", "dimen", "example_item_dimen"), "");
    assertThat(value).describedAs("Item dimen from gradle output is not loaded").isNotNull();
    assertThat(value.asString()).isEqualTo("3.14");
  }

  @Test
  public void shouldLoadStringResourcesFromGradleOutputDirectoriesDefinedByItemTag() {
    TypedResource value = gradleResourceTable.getValue(new ResName("org.robolectric.gradleapp", "string", "item_from_gradle_output"), "");
    assertThat(value).describedAs("Item string from gradle output is not loaded").isNotNull();
    assertThat(value.asString()).isEqualTo("3.14");
  }

  @Test
  public void shouldLoadColorResourcesFromGradleOutputDirectoriesDefinedByColorTag() {
    TypedResource value = gradleResourceTable.getValue(new ResName("org.robolectric.gradleapp", "color", "example_color"), "");
    assertThat(value).describedAs("Color from gradle output is not loaded").isNotNull();
    assertThat(value.asString()).isEqualTo("#00FF00FF");
  }

  @Test
  public void shouldLoadColorResourcesFromGradleOutputDirectoriesDefinedByItemTag() {
    TypedResource value = gradleResourceTable.getValue(new ResName("org.robolectric.gradleapp", "color", "example_item_color"), "");
    assertThat(value).describedAs("Item color from gradle output is not loaded").isNotNull();
    assertThat(value.asString()).isEqualTo("1.0");
  }
}
