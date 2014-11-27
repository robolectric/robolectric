package org.robolectric.annotation.processing;

import static com.google.common.collect.Maps.*;
import static com.google.common.collect.Sets.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import javax.lang.model.util.SimpleElementVisitor6;
import javax.lang.model.util.Types;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;

/**
 * Model describing the Robolectric source file.
 */
public class RoboModel {
  private static FQComparator fqComparator = new FQComparator();
  private static SimpleComparator comparator = new SimpleComparator();
  
  /** TypeElement representing the Robolectric.Anything interface, or null if the element isn't found. */
  final TypeElement ANYTHING;
  /** TypeMirror representing the Robolectric.Anything interface, or null if the element isn't found. */
  final TypeMirror ANYTHING_MIRROR;
    /** TypeElement representing the @Implements annotation. */
  final TypeElement IMPLEMENTS;

  /** Convenience reference for the processing environment's elements utilities. */
  private final Elements elements;
  /** Convenience reference for the processing environment's types utilities. */
  private final Types types;
  
  HashMap<TypeElement,String> referentMap = newHashMap();
  HashMultimap<String,TypeElement> typeMap = HashMultimap.create();
  TreeMap<TypeElement,TypeElement> shadowTypes = newTreeMap(fqComparator);
  TreeSet<String> imports = newTreeSet();
  TreeMap<TypeElement,ExecutableElement> resetterMap = newTreeMap(comparator);
  
  private static class FQComparator implements Comparator<TypeElement> {
    @Override
    public int compare(TypeElement o1, TypeElement o2) {
      return o1.getQualifiedName().toString().compareTo(o2.getQualifiedName().toString());
    }
  }

  private static class SimpleComparator implements Comparator<Element> {
    @Override
    public int compare(Element o1, Element o2) {
      return o1.getSimpleName().toString().compareTo(o2.getSimpleName().toString());
    }
  }

  public RoboModel(Elements elements, Types types) {
    this.elements = elements;
    this.types = types;
    ANYTHING   = elements.getTypeElement("org.robolectric.Robolectric.Anything");
    ANYTHING_MIRROR = ANYTHING == null ? null : ANYTHING.asType();
    // FIXME: check this type lookup for NPEs (and also the ones in the
    // validators)
    IMPLEMENTS = elements.getTypeElement(ImplementsValidator.IMPLEMENTS_CLASS);
  }

  public AnnotationMirror getAnnotationMirror(Element element, TypeElement annotation) {
    TypeMirror expectedType = annotation.asType();
    for (AnnotationMirror m : element.getAnnotationMirrors()) {
      if (types.isSameType(expectedType, m.getAnnotationType())) {
        return m;
      }
    }
    return null;
  }

  static ElementVisitor<TypeElement,Void> typeVisitor = new SimpleElementVisitor6<TypeElement,Void>() {
    @Override
    public TypeElement visitType(TypeElement e, Void p) {
      return e;
    }
  };
  

  static AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String key) {
    
    for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet() ) {
      if (entry.getKey().getSimpleName().toString().equals(key)) {
        return entry.getValue();
      }
    }
    return null;
  }

  static AnnotationValueVisitor<TypeMirror,Void> valueVisitor = new SimpleAnnotationValueVisitor6<TypeMirror,Void>() {
    @Override
    public TypeMirror visitType(TypeMirror t, Void arg) {
      return t;
    }
  };

  static AnnotationValueVisitor<String, Void> classNameVisitor = new SimpleAnnotationValueVisitor6<String, Void>() {
    @Override
    public String visitString(String s, Void arg) {
      return s;
    }
  };
  
  public AnnotationMirror getImplementsMirror(Element elem) {
    return getAnnotationMirror(elem, IMPLEMENTS);
  }
  
  private TypeMirror getImplementedClassName(AnnotationMirror am) {
    AnnotationValue className = getAnnotationValue(am, "className");
    if (className == null) {
      return null;
    }
    String classNameString = classNameVisitor.visit(className);
    if (classNameString == null) {
      return null;
    }
    TypeElement impElement = elements.getTypeElement(classNameString.replace('$', '.'));
    if (impElement == null) {
      return null;
    }
    return impElement.asType();
  }
  
  public TypeMirror getImplementedClass(AnnotationMirror am) {
    if (am == null) {
      return null;
    }
    // RobolectricWiringTest prefers className (if provided) to value, so we do the same here.
    TypeMirror impType = getImplementedClassName(am);
    if (impType != null) {
      return impType;
    }
    AnnotationValue av = getAnnotationValue(am, "value");
    if (av == null) {
      return null;
    }
    TypeMirror type = valueVisitor.visit(av);
    if (type == null) {
      return null;
    }
    // If the class is Robolectric.Anything, treat as if it wasn't specified at all.
    if (ANYTHING_MIRROR != null && types.isSameType(type, ANYTHING_MIRROR)) {
      return null;
    }
    
    return type;
  }
  
  /**
   * Prepares the various derived parts of the model based on the class mappings
   * that have been registered to date.
   */
  public void prepare() {
    HashMultimap<String,TypeElement> typeMap = HashMultimap.create();
    final HashMap<TypeElement,TypeElement> importMap = newHashMap();
    
    for (Map.Entry<TypeElement,TypeElement> entry : shadowTypes.entrySet()) {
      final TypeElement shadowType = entry.getKey();
      typeMap.put(shadowType.getSimpleName().toString(), shadowType);
      importMap.put(shadowType, shadowType);

      final TypeElement solidType = entry.getValue();
      if (!Objects.equal(ANYTHING, solidType)) {
        typeMap.put(solidType.getSimpleName().toString(), solidType);
        importMap.put(solidType, solidType);
      }
    }

    while (!typeMap.isEmpty()) {
      final HashMultimap<String,TypeElement> nextRound = HashMultimap.create();
      for (Map.Entry<String, Set<TypeElement>> referents : Multimaps.asMap(typeMap).entrySet()) {
        final Set<TypeElement> c = referents.getValue();
        // If there is only one type left with the given simple
        // name, then
        if (c.size() == 1) {
          final TypeElement type = c.iterator().next();
          referentMap.put(type, referents.getKey());
        }
        else {
          for (TypeElement type : c) {
            SimpleElementVisitor6<Void,TypeElement> visitor = new SimpleElementVisitor6<Void,TypeElement>() {
              @Override
              public Void visitType(TypeElement parent, TypeElement type) {
                nextRound.put(parent.getSimpleName() + "." + type.getSimpleName(), type);
                importMap.put(type, parent);
                return null;
              }
              @Override 
              public Void visitPackage(PackageElement parent, TypeElement type) {
                referentMap.put(type, type.getQualifiedName().toString());
                importMap.remove(type);
                return null;
              }
            };
            visitor.visit(importMap.get(type).getEnclosingElement(), type);
          }
        }
      }
      typeMap = nextRound;
    }
    for (TypeElement imp: importMap.values()) {
      if (imp.getModifiers().contains(Modifier.PUBLIC)) {
        imports.add(imp.getQualifiedName().toString());
      }
    }

    // Other imports that the generated class needs
    imports.add("javax.annotation.Generated");
    imports.add("org.robolectric.internal.ShadowExtractor");
  }

  public NavigableMap<TypeElement, TypeElement> getShadowMap() {
    return Maps.filterEntries(shadowTypes, new Predicate<Entry<TypeElement,TypeElement>> () {
      @Override
      public boolean apply(Entry<TypeElement, TypeElement> entry) {
        return !Objects.equal(ANYTHING, entry.getValue());
      }
    });
  }
  
  /**
   * Returns a plain string to be used in the generated source
   * to identify the given type. The returned string will have 
   * sufficient level of qualification in order to make the referent
   * unique for the source file.
   * @param type
   * @return
   */
  public String getReferentFor(TypeElement type) {
    return referentMap.get(type);
  }
}
