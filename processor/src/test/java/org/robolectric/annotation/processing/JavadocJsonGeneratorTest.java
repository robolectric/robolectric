package org.robolectric.annotation.processing;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;
import static com.google.testing.compile.JavaFileObjects.forResource;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.robolectric.annotation.processing.RobolectricProcessor.JSON_DOCS_DIR;
import static org.robolectric.annotation.processing.RobolectricProcessor.JSON_DOCS_ENABLED;
import static org.robolectric.annotation.processing.Utils.DEFAULT_OPTS;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link org.robolectric.annotation.processing.generator.JavadocJsonGenerator} */
@RunWith(JUnit4.class)
public class JavadocJsonGeneratorTest {

  @Test
  public void jsonDocs_disabledByDefault() throws Exception {
    File tmpDir = Files.createTempDirectory("JavadocJsonGeneratorTest").toFile();
    tmpDir.deleteOnExit();
    Map<String, String> options = new HashMap<>(DEFAULT_OPTS);
    options.put(JSON_DOCS_DIR, tmpDir.getAbsolutePath());
    assertAbout(javaSources())
        .that(
            ImmutableList.of(
                forResource("org/robolectric/DocumentedObjectOuter.java"),
                forResource(
                    "org/robolectric/annotation/processing/shadows/DocumentedObjectShadow.java")))
        .processedWith(new RobolectricProcessor(options))
        .compilesWithoutError();
    assertThat(tmpDir.list()).isEmpty();
  }

  @Test
  public void shouldGenerateJavadocJson() throws Exception {
    Map<String, String> options = new HashMap<>(DEFAULT_OPTS);
    options.put(JSON_DOCS_ENABLED, "true");
    assertAbout(javaSources())
        .that(
            ImmutableList.of(
                forResource("org/robolectric/DocumentedObjectOuter.java"),
                forResource(
                    "org/robolectric/annotation/processing/shadows/DocumentedObjectShadow.java")))
        .processedWith(new RobolectricProcessor(options))
        .compilesWithoutError();
    String jsonDocsDir = options.get(JSON_DOCS_DIR);
    String jsonFile = jsonDocsDir + "/org.robolectric.DocumentedObjectOuter.DocumentedObject.json";
    JsonElement json = JsonParser.parseReader(Files.newBufferedReader(Paths.get(jsonFile), UTF_8));
    assertThat(((JsonObject) json).get("documentation").getAsString())
        .isEqualTo("DocumentedObjectOuter Javadoc goes here! ");

    // must list imported classes, including inner classes...
    assertThat(((JsonObject) json).get("imports").getAsJsonArray())
        .containsExactly(
            new JsonPrimitive("org.robolectric.DocumentedObjectOuter"),
            new JsonPrimitive("org.robolectric.annotation.Implementation"),
            new JsonPrimitive("org.robolectric.annotation.Implements"),
            new JsonPrimitive("java.util.Map"),
            new JsonPrimitive(
                "org.robolectric.annotation.processing.shadows.DocumentedObjectShadow.SomeEnum"));
  }
}
