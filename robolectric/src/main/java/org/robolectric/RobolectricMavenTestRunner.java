package org.robolectric;

import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FsFile;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Test runner customized for running unit tests either through the Maven CLI or
 * Android Studio. The runner looks for libraries in the <code>target/unpacked-libs</code> folder instead
 * of reading <code>project.properties</code> file.
 */
public class RobolectricMavenTestRunner extends RobolectricTestRunner {

    /**
     * Creates a runner to run {@code testClass}. Looks in your working directory for your AndroidManifest.xml file
     * and res directory by default. Use the {@link Config} annotation to configure.
     *
     * @param testClass the test class to be run
     * @throws InitializationError if junit says so
     */
    public RobolectricMavenTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    public Config getConfig(Method method) {
        Config config = super.getConfig(method);
        final String manifest = config.manifest().equals(Config.DEFAULT) ?
                AndroidManifest.DEFAULT_MANIFEST_NAME : config.manifest();

        String path = "";
        String []dir = manifest.split("/");
        for(int i=0; i<dir.length-1; i++) {
            if(!dir[i].equals("") && !dir[i].equals(".")) {
                path += "../";
            }
        }
        path += "target/unpacked-libs";

        FsFile libraryRoot =  getBaseDir().join(manifest).getParent().join(path);
        FsFile[] files = libraryRoot.listFiles();
        List<String> libs = new ArrayList<>(files.length);
        for(int i=0; i<files.length; i++) {
            if(files[i].join("AndroidManifest.xml").exists()) {
                libs.add(path + "/" + files[i].getName());
            }
        }
        String[] libraries = libs.toArray(new String[libs.size()]);
        Config.Implementation imp = new Config.Implementation(
                config.emulateSdk(), config.manifest(), config.qualifiers(), config.resourceDir(), config.assetDir(), config.reportSdk(), config.shadows(), config.application(), libraries, config.constants());
        return imp;
    }
}