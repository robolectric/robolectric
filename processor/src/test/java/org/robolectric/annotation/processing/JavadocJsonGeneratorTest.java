package org.robolectric.annotation.processing;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.common.truth.Truth.assertThat;
import static com.google.testing.compile.JavaFileObjects.forResource;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.robolectric.annotation.processing.Utils.DEFAULT_OPTS;
import static org.robolectric.annotation.processing.Utils.ROBO_SOURCE;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link org.robolectric.annotation.processing.generator.JavadocJsonGenerator} */
@RunWith(JUnit4.class)
public class JavadocJsonGeneratorTest {
  @Test
  public void shouldGenerateJavadocJson() throws Exception {
    assertAbout(javaSources())
        .that(
            ImmutableList.of(
                ROBO_SOURCE,
                forResource(
                    "org/robolectric/annotation/processing/shadows/DocumentedObjectShadow.java")))
        .processedWith(new RobolectricProcessor(DEFAULT_OPTS))
        .compilesWithoutError();
    JsonParser jsonParser = new JsonParser();
    String jsonFile = "build/docs/json/org.robolectric.Robolectric.DocumentedObject.json";
    JsonElement json = jsonParser.parse(Files.newBufferedReader(Paths.get(jsonFile), UTF_8));
    assertThat(((JsonObject) json).get("documentation").getAsString())
        .isEqualTo("Robolectric Javadoc goes here!\n");

    // must list imported classes, including inner classes...
    assertThat(((JsonObject) json).get("imports").getAsJsonArray())
        .containsExactly(
            new JsonPrimitive("org.robolectric.Robolectric"),
            new JsonPrimitive("org.robolectric.annotation.Implementation"),
            new JsonPrimitive("org.robolectric.annotation.Implements"),
            new JsonPrimitive("java.util.Map"),
            new JsonPrimitive(
                "org.robolectric.annotation.processing.shadows.DocumentedObjectShadow.SomeEnum"));
  }
}
