package org.robolectric.annotation.processing.generator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import org.robolectric.annotation.processing.DocumentedPackage;
import org.robolectric.annotation.processing.DocumentedType;
import org.robolectric.annotation.processing.RobolectricModel;
import org.robolectric.annotation.processing.RobolectricModel.ShadowInfo;

/**
 * Primarily used by the Robolectric Chrome extension for Robolectric docs alongside of Android SDK
 * docs.
 */
public class JavadocJsonGenerator extends Generator {
  private final RobolectricModel model;
  private final Messager messager;
  private final Gson gson;
  private final File jsonDocsDir;

  public JavadocJsonGenerator(
      RobolectricModel model, ProcessingEnvironment environment, File jsonDocsDir) {
    super();

    this.model = model;
    this.messager = environment.getMessager();
    gson = new GsonBuilder()
        .setPrettyPrinting()
        .create();
    this.jsonDocsDir = jsonDocsDir;
  }

  @Override
  public void generate() {
    Map<String, String> shadowedTypes = new HashMap<>();
    for (ShadowInfo entry : model.getVisibleShadowTypes()) {
      shadowedTypes.put(entry.getShadowName().replace('$', '.'), entry.getActualName());
    }

    for (Map.Entry<String, String> entry : model.getExtraShadowTypes().entrySet()) {
      String shadowType = entry.getKey().replace('$', '.');
      String shadowedType = entry.getValue();
      shadowedTypes.put(shadowType, shadowedType);
    }

    for (DocumentedPackage documentedPackage : model.getDocumentedPackages()) {
      for (DocumentedType documentedType : documentedPackage.getDocumentedTypes()) {
        String shadowedType = shadowedTypes.get(documentedType.getName());
        if (shadowedType == null) {
          messager.printMessage(Kind.WARNING,
              "Couldn't find shadowed type for " + documentedType.getName());
        } else {
          writeJson(documentedType, new File(jsonDocsDir, shadowedType + ".json"));
        }
      }
    }
  }

  private void writeJson(Object object, File file) {
    try {
      file.getParentFile().mkdirs();

      try (BufferedWriter writer =
          new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
        gson.toJson(object, writer);
      }
    } catch (IOException e) {
      messager.printMessage(Diagnostic.Kind.ERROR, "Failed to write javadoc JSON file: " + e);
      throw new RuntimeException(e);
    }
  }

}
