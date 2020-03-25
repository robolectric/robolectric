package org.robolectric.gradleplugin

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class DownloadAndroidSdks extends DefaultTask {

  @TaskAction
  void run() {
    GradlePlugin.Config config = getProject().getExtensions().getByType(GradlePlugin.Config.class);

    System.out.println("DownloadAndroidSdks! " + getProject());

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
      println "sdk$apiLevel = ${sdkFiles}"
    }

    def outDir = new File(project.buildDir, "generated/robolectric")
    outDir.mkdirs()
    project.android.sourceSets['test'].resources.srcDir(outDir)
    def outFile = new File(outDir, 'org.robolectric.sdks.properties')
    def out = outFile.newOutputStream()
    sdkDeps.store(out, null)
    out.close()
    println "xxx wrote to $outFile"
    println "props: $sdkDeps"

  }

  Map<Integer, String> figureSdks(Object configSdks, Properties defaultSdks) {
    def map = new HashMap<Integer, String>()

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
    } else if (configSdks instanceof Map) {
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

  Properties loadPropertiesResourceFile(String name) {
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
