package org.robolectric.res;

import org.jetbrains.annotations.NotNull;
import org.robolectric.res.builder.XmlBlock;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public abstract class ResourceTable {

  public abstract Integer getResourceId(ResName resName);

  public abstract ResName getResName(int resourceId);

  public abstract TypedResource getValue(int resId, String qualifiers);

  public abstract TypedResource getValue(@NotNull ResName resName, String qualifiers) ;

  public abstract XmlBlock getXml(ResName resName, String qualifiers);

  public abstract InputStream getRawValue(ResName resName, String qualifiers);

  public abstract InputStream getRawValue(int resId, String qualifiers);

  public abstract boolean hasValue(ResName resName, String qualifiers);

  public abstract void addResource(int resId, String type, String name);

  public abstract void receive(Visitor visitor);

  @NotNull
  public List<TypedResource> grep(String regex) {
    return grep(Pattern.compile(regex));
  }

  @NotNull
  public List<TypedResource> grep(final Pattern pattern) {
    final ArrayList<TypedResource> matches = new ArrayList<>();
    receive(new Visitor<TypedResource>() {
      @Override
      public void visit(ResName resName, Iterable<TypedResource> items) {
        boolean match = pattern.matcher(resName.getFullyQualifiedName()).find();
        if (!match && resName.type.equals("style")) {
          for (TypedResource typedResource : items) {
            TypedResource<StyleData> style = (TypedResource<StyleData>) typedResource;
            if (style.getData().grep(pattern)) {
              match = true;
              break;
            }
          }
        }

        if (match) {
          for (TypedResource typedResource : items) {
            matches.add(typedResource);
          }
        }
      }
    });
    return matches;
  }

  interface Visitor<T> {
    void visit(ResName key, Iterable<T> values);
  }
}
