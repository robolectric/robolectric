package org.robolectric.annotation.processing;

import java.util.ArrayList;
import java.util.List;

public class DocumentedMethod extends RobolectricModel.DocumentedElement {
  public boolean isImplementation;
  public List<String> modifiers = new ArrayList<>();
  public String documentation;
  public List<String> params = new ArrayList<>();
  public String returnType;
  public List<String> exceptions = new ArrayList<>();
  public Integer minSdk;
  public Integer maxSdk;

  public DocumentedMethod(String name) {
    super(name);
  }
}
