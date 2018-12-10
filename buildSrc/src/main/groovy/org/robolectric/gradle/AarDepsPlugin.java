package org.robolectric.gradle;

import static org.gradle.api.internal.artifacts.ArtifactAttributes.ARTIFACT_FORMAT;

import com.android.build.gradle.internal.dependency.ExtractAarTransform;
import java.io.File;
import java.util.Collections;
import java.util.List;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Resolve aar dependencies into jars for non-Android projects.
 */
public class AarDepsPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    project.getDependencies().registerTransform(
        reg -> {
          reg.getFrom().attribute(ARTIFACT_FORMAT, "aar");
          reg.getTo().attribute(ARTIFACT_FORMAT, "jar");
          reg.artifactTransform(ClassesJarExtractor.class);
        });

    project.getConfigurations().all(cfg ->
        cfg.attributes(cfgAttrs ->
            cfgAttrs.attribute(ARTIFACT_FORMAT, "jar")));
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
