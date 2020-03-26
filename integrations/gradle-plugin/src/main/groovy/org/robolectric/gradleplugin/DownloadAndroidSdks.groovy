package org.robolectric.gradleplugin

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction

class DownloadAndroidSdks extends DefaultTask {

  @TaskAction
  void run() {
    GradlePlugin.Config config = getProject().getExtensions().getByType(GradlePlugin.Config.class);

    def defaultSdks = loadPropertiesResourceFile("org.robolectric.GradlePlugin.sdks.properties")
    def enabledSdks = figureSdks(config.sdks, defaultSdks)
    Properties sdkDeps = new Properties()
    enabledSdks.keySet().forEach { Integer apiLevel ->
      def sdkCoords = enabledSdks.get(apiLevel)
      if (sdkCoords instanceof File) {
        sdkCoords = project.files(sdkCoords)
      }
      def sdkCfg = project.configurations.create("sdk$apiLevel")
      project.dependencies.add("sdk$apiLevel", sdkCoords)

      def sdkFiles = sdkCfg.resolve()
      if (sdkFiles.size() != 1) {
        throw new IllegalStateException("weird, $sdkCoords returned $sdkFiles, not one file")
      }
      sdkDeps[apiLevel.toString()] = sdkFiles[0].toString()
    }

    def outDir = getGeneratedDir(project)
    outDir.mkdirs()
    def outFile = new File(outDir, 'org.robolectric.sdks.properties')
    def out = outFile.newOutputStream()
    sdkDeps.store(out, null)
    out.close()
  }

  static File getGeneratedDir(Project project) {
    return new File(project.buildDir, "generated/robolectric")
  }

  static void addGeneratedResourcesDirToTestSourceSets(Project project) {
    def outDir = getGeneratedDir(project)

    project.android.sourceSets.forEach { sourceSet ->
      if (sourceSet.name.startsWith("test")) {
        sourceSet.resources.srcDir(outDir)
      }
    }
  }

  static Map<Integer, Object> figureSdks(Object configSdks, Properties defaultSdks) {
    def map = new HashMap<Integer, Object>()

    def add = { Integer apiLevel ->
      def coordinates = defaultSdks.getProperty(apiLevel.toString())
      if (coordinates == null) {
        throw new IllegalArgumentException("Unknown API level $apiLevel")
      }
      map.put(apiLevel, coordinates)
    }

    if (configSdks instanceof String) {
      configSdks.split(",").each { add(Integer.parseInt(it)) }
    } else if (configSdks instanceof Integer) {
      add(configSdks)
    } else if (configSdks instanceof List
            || configSdks instanceof int[]
            || configSdks instanceof Object[]) {
      configSdks.iterator().forEachRemaining {
        if (it instanceof String) add(Integer.parseInt(it)) else add((int) it)
      }
    } else if (configSdks instanceof Map<Integer, String>) {
      configSdks.keySet().forEach { key ->
        def coord = configSdks.get(key)
        if (coord instanceof String) {
          if (coord.contains("/")) coord = new File(coord)
        }
        map.put(key, coord)
      }
    }
    return map
  }

  static Properties loadPropertiesResourceFile(String name) {
    def props = new Properties()
    def resourceIn = GradlePlugin.class.classLoader.getResourceAsStream(name)
    if (resourceIn == null) throw new IllegalStateException("$name not found")
    try {
      props.load(resourceIn)
    } finally {
      resourceIn.close()
    }
    return props
  }
}
