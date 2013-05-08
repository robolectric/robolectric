package org.robolectric.res;

import java.util.LinkedHashMap;
import java.util.Map;

public class StyleData implements Style {
    private final String packageName;
    private final String name;
    private final String parent;
    private final Map<ResName, Attribute> items = new LinkedHashMap<ResName, Attribute>();

    public StyleData(String packageName, String name, String parent) {
        this.packageName = packageName;
        this.name = name;
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public String getParent() {
        return parent;
    }

    public void add(ResName attrName, Attribute attribute) {
        attrName.mustBe("attr");
        items.put(attrName, attribute);
    }

    @Override public Attribute getAttrValue(ResName name) {
        name.mustBe("attr");
        return items.get(name);
    }

    @Override public String toString() {
        return "StyleData{" +
                "name='" + name + '\'' +
                ", parent='" + parent + '\'' +
                '}';
    }

    public String getPackageName() {
        return packageName;
    }
}
