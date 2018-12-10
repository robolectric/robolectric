package org.robolectric.gradle;

import static org.gradle.api.internal.artifacts.ArtifactAttributes.ARTIFACT_FORMAT;

import com.android.build.gradle.internal.dependency.ExtractAarTransform;
import com.google.common.base.Joiner;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.compile.JavaCompile;

/**
 * Resolve aar dependencies into jars for non-Android projects.
 */
public class AarDepsPlugin implements Plugin<Project> {
  Pattern classpathConfig = Pattern.compile("[cC]lasspath$");

  @Override
  public void apply(Project project) {
    project.getDependencies().registerTransform(
        reg -> {
          reg.getFrom().attribute(ARTIFACT_FORMAT, "aar");
          reg.getTo().attribute(ARTIFACT_FORMAT, "jar");
          reg.artifactTransform(ClassesJarExtractor.class);
        });

    project.afterEvaluate(p ->
        project.getConfigurations().forEach(c -> {
          // I suspect we're meant to use the org.gradle.usage attribute, but this works.
          String lowerName = c.getName().toLowerCase();
          if (lowerName.endsWith("classpath") || lowerName.endsWith("compileonly")) {
            c.attributes(cfgAttrs -> cfgAttrs.attribute(ARTIFACT_FORMAT, "jar"));
          }
        }));

    // warn if any AARs do make it through somehow; there must be a gradle configuration
    // that isn't matched above.
    project.getTasks().withType(JavaCompile.class).all(t -> {
      t.doFirst(task -> {
        List<File> aarFiles = findAarFiles(t.getClasspath());
        if (!aarFiles.isEmpty()) {
          throw new IllegalStateException("AARs on classpath: " + Joiner.on("\n  ").join(aarFiles));
        }
      });
    });
  }

  private List<File> findAarFiles(FileCollection files) {
    List<File> bad = new ArrayList<>();
    for (File file : files.getFiles()) {
      if (file.getName().toLowerCase().endsWith(".aar")) {
        bad.add(file);
      }
    }
    return bad;
  }

  static class ClassesJarExtractor extends ExtractAarTransform {
    @Override
    public List<File> transform(File input) {
      List<File> out = super.transform(input);
      File classesJar = new File(out.get(0), "jars/classes.jar");
      return Collections.singletonList(classesJar);
    }
  }
}
