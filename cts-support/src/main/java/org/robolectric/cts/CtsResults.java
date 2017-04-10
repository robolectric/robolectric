package org.robolectric.cts;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

public class CtsResults {
  private final Map<String, CtsTestClass> classes = new TreeMap<>();
  private transient boolean changed = false;

  public static CtsResults load(InputStream in) {
    return getYaml().loadAs(in, CtsResults.class);
  }

  public void save(PrintWriter out) {
    getYaml().dump(this, out);
  }

  public boolean changed() {
    return changed;
  }

  private static Yaml getYaml() {
    DumperOptions dumperOptions = new DumperOptions();
    dumperOptions.setWidth(10);
    Yaml yaml = new Yaml(dumperOptions);
    yaml.setBeanAccess(BeanAccess.FIELD);
    return yaml;
  }

  public CtsTestClass getCtsClass(String name) {
    return classes.get(name);
  }

  public void setResult(String className, String methodName, CtsTestResult result) {
    changed = true;

    CtsTestClass ctsClass = classes.get(className);
    if (ctsClass == null) {
      ctsClass = new CtsTestClass();
      classes.put(className, ctsClass);
    }
    ctsClass.methods.put(methodName, result);
  }
}
