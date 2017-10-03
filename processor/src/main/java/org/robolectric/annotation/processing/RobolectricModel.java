package org.robolectric.annotation.processing;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newTreeMap;
import static com.google.common.collect.Sets.newTreeSet;

import com.google.common.base.Equivalence;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import javax.lang.model.util.SimpleElementVisitor6;
import javax.lang.model.util.SimpleTypeVisitor6;
import javax.lang.model.util.Types;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.processing.validator.ImplementsValidator;

/**
 * Model describing the Robolectric source file.
 */
public class RobolectricModel {
  private static FQComparator fqComparator = new FQComparator();
  private static SimpleComparator comparator = new SimpleComparator();

  /** TypeElement representing the Robolectric.Anything interface, or null if the element isn't found. */
  final TypeElement ANYTHING;
  /** TypeMirror representing the Robolectric.Anything interface, or null if the element isn't found. */
  public final TypeMirror ANYTHING_MIRROR;
  /** TypeMirror representing the Object class. */
  final TypeMirror OBJECT_MIRROR;
  /** TypeElement representing the @Implements annotation. */
  final TypeElement IMPLEMENTS;

  /** PackageElement representing the java.lang package. */
  final PackageElement JAVA_LANG;

  /** Convenience reference for the processing environment's elements utilities. */
  private final Elements elements;
  /** Convenience reference for the processing environment's types utilities. */
  private final Types types;

  private HashMap<TypeElement,String> referentMap = newHashMap();
  private HashMultimap<String,TypeElement> typeMap = HashMultimap.create();
  private HashMap<TypeElement,TypeElement> importMap = newHashMap();
  private TreeMap<TypeElement,TypeElement> shadowTypes = newTreeMap(fqComparator);
  private TreeMap<String, String> extraShadowTypes = newTreeMap();
  private TreeSet<String> imports = newTreeSet();
  private TreeMap<TypeElement,ExecutableElement> resetterMap = newTreeMap(comparator);

  private final Map<String, DocumentedPackage> documentedPackages = new TreeMap<>();

  public Collection<DocumentedPackage> getDocumentedPackages() {
    return documentedPackages.values();
  }

  public void documentPackage(String name, String documentation) {
    getDocumentedPackage(name).setDocumentation(documentation);
  }

  private DocumentedPackage getDocumentedPackage(String name) {
    DocumentedPackage documentedPackage = documentedPackages.get(name);
    if (documentedPackage == null) {
      documentedPackage = new DocumentedPackage(name);
      documentedPackages.put(name, documentedPackage);
    }
    return documentedPackage;
  }

  private DocumentedPackage getDocumentedPackage(TypeElement type) {
    Element pkgElement = type.getEnclosingElement();
    return getDocumentedPackage(pkgElement.toString());
  }

  public void documentType(TypeElement type, String documentation, List<String> imports) {
    DocumentedType documentedType = getDocumentedType(type);
    documentedType.setDocumentation(documentation);
    documentedType.imports = imports;
  }

  private DocumentedType getDocumentedType(TypeElement type) {
    DocumentedPackage documentedPackage = getDocumentedPackage(type);
    return documentedPackage.getDocumentedType(type.getQualifiedName().toString());
  }

  public void documentMethod(TypeElement shadowClass, DocumentedMethod documentedMethod) {
    DocumentedType documentedType = getDocumentedType(shadowClass);
    documentedType.methods.put(documentedMethod.getName(), documentedMethod);
  }

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

  public RobolectricModel(Elements elements, Types types) {
    this.elements = elements;
    this.types = types;
    ANYTHING   = elements.getTypeElement("org.robolectric.Robolectric.Anything");
    ANYTHING_MIRROR = ANYTHING == null ? null : ANYTHING.asType();
    // FIXME: check this type lookup for NPEs (and also the ones in the
    // validators)
    IMPLEMENTS = elements.getTypeElement(ImplementsValidator.IMPLEMENTS_CLASS);
    JAVA_LANG = elements.getPackageElement("java.lang");
    OBJECT_MIRROR = elements.getTypeElement(Object.class.getCanonicalName()).asType();
    notObject = new Predicate<TypeMirror>() {
      @Override
      public boolean apply(TypeMirror t) {
        return !RobolectricModel.this.types.isSameType(t, OBJECT_MIRROR);
      }
    };
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

  public static ElementVisitor<TypeElement,Void> typeVisitor = new SimpleElementVisitor6<TypeElement,Void>() {
    @Override
    public TypeElement visitType(TypeElement e, Void p) {
      return e;
    }
  };

  public static AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String key) {
    for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet() ) {
      if (entry.getKey().getSimpleName().toString().equals(key)) {
        return entry.getValue();
      }
    }
    return null;
  }

  public static AnnotationValueVisitor<TypeMirror,Void> valueVisitor = new SimpleAnnotationValueVisitor6<TypeMirror,Void>() {
    @Override
    public TypeMirror visitType(TypeMirror t, Void arg) {
      return t;
    }
  };

  public static AnnotationValueVisitor<String, Void> classNameVisitor = new SimpleAnnotationValueVisitor6<String, Void>() {
    @Override
    public String visitString(String s, Void arg) {
      return s;
    }
  };

  public static AnnotationValueVisitor<Integer, Void> intVisitor = new SimpleAnnotationValueVisitor6<Integer, Void>() {
    @Override
    public Integer visitInt(int i, Void aVoid) {
      return i;
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

  private static ElementVisitor<TypeElement,Void> typeElementVisitor = new SimpleElementVisitor6<TypeElement,Void>() {

    @Override
    public TypeElement visitType(TypeElement e, Void p) {
      return e;
    }
  };

  private void registerType(TypeElement type) {
    if (!Objects.equal(ANYTHING, type) && !importMap.containsKey(type)) {
      typeMap.put(type.getSimpleName().toString(), type);
      importMap.put(type, type);
      for (TypeParameterElement typeParam : type.getTypeParameters()) {
        for (TypeMirror bound : typeParam.getBounds()) {
          // FIXME: get rid of cast using a visitor
          TypeElement boundElement = typeElementVisitor.visit(types.asElement(bound));
          registerType(boundElement);
        }
      }
    }
  }

  /**
   * Prepares the various derived parts of the model based on the class mappings
   * that have been registered to date.
   */
  public void prepare() {
    for (Map.Entry<TypeElement,TypeElement> entry : getVisibleShadowTypes().entrySet()) {
      final TypeElement shadowType = entry.getKey();
      registerType(shadowType);

      final TypeElement solidType = entry.getValue();
      registerType(solidType);
    }

    for (Map.Entry<TypeElement,TypeElement> entry : getResetterShadowTypes().entrySet()) {
      final TypeElement shadowType = entry.getKey();
      registerType(shadowType);
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
      if (imp.getModifiers().contains(Modifier.PUBLIC)
          && !JAVA_LANG.equals(imp.getEnclosingElement())) {
        imports.add(imp.getQualifiedName().toString());
      }
    }

    // Other imports that the generated class needs
    imports.add("java.util.Map");
    imports.add("java.util.HashMap");
    imports.add("javax.annotation.Generated");
    imports.add("org.robolectric.internal.ShadowProvider");
    imports.add("org.robolectric.shadow.api.Shadow");
  }

  public void addShadowType(TypeElement elem, TypeElement type) {
    shadowTypes.put(elem, type);
  }

  public void addExtraShadow(String sdkClassName, String shadowClassName) {
    extraShadowTypes.put(shadowClassName, sdkClassName);
  }

  public void addResetter(TypeElement parent, ExecutableElement elem) {
    resetterMap.put(parent, elem);
  }

  public Map<TypeElement, ExecutableElement> getResetters() {
    return resetterMap;
  }

  public Set<String> getImports() {
    return imports;
  }

  public Map<TypeElement, TypeElement> getAllShadowTypes() {
    return shadowTypes;
  }

  public Map<String, String> getExtraShadowTypes() {
    return extraShadowTypes;
  }

  public Map<TypeElement, TypeElement> getResetterShadowTypes() {
    return Maps.filterEntries(shadowTypes, new Predicate<Entry<TypeElement, TypeElement>>() {
      @Override
      public boolean apply(Entry<TypeElement, TypeElement> entry) {
        return resetterMap.containsKey(entry.getKey());
      }
    });
  }

  public Map<TypeElement, TypeElement> getVisibleShadowTypes() {
    return Maps.filterEntries(shadowTypes, new Predicate<Entry<TypeElement, TypeElement>>() {
      @Override
      public boolean apply(Entry<TypeElement, TypeElement> entry) {
        return entry.getKey().getAnnotation(Implements.class).isInAndroidSdk();
      }
    });
  }

  public Map<TypeElement, TypeElement> getShadowOfMap() {
    return Maps.filterEntries(getVisibleShadowTypes(), new Predicate<Entry<TypeElement,TypeElement>> () {
      @Override
      public boolean apply(Entry<TypeElement, TypeElement> entry) {
        return !Objects.equal(ANYTHING, entry.getValue());
      }
    });
  }

  public Collection<String> getShadowedPackages() {
    Set<String> packages = new TreeSet<>();
    for (TypeElement element : shadowTypes.values()) {
      String packageName = elements.getPackageOf(element).toString();

      // org.robolectric.* should never be instrumented
      if (packageName.matches("org.robolectric(\\..*)?")) {
        continue;
      }

      packages.add("\"" + packageName + "\"");
    }
    return packages;
  }

  private Predicate<TypeMirror> notObject;
  public List<TypeMirror> getExplicitBounds(TypeParameterElement typeParam) {
    return newArrayList(Iterables.filter(typeParam.getBounds(), notObject));
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

  private TypeVisitor<String,Void> findReferent = new SimpleTypeVisitor6<String,Void>() {
    @Override
    public String visitDeclared(DeclaredType t, Void p) {
      return referentMap.get(t.asElement());
    }
  };

  public String getReferentFor(TypeMirror type) {
    return findReferent.visit(type);
  }

  private Equivalence<TypeMirror> typeMirrorEq = new Equivalence<TypeMirror>() {
    @Override
    protected boolean doEquivalent(TypeMirror a, TypeMirror b) {
      return types.isSameType(a, b);
    }

    @Override
    protected int doHash(TypeMirror t) {
      // We're not using the hash.
      return 0;
    }
  };


  private Equivalence<TypeParameterElement> typeEq = new Equivalence<TypeParameterElement>() {
    @Override
    @SuppressWarnings({"unchecked"})
    protected boolean doEquivalent(TypeParameterElement arg0,
        TypeParameterElement arg1) {
      // Casts are necessary due to flaw in pairwise equivalence implementation.
      return typeMirrorEq.pairwise().equivalent((List<TypeMirror>)arg0.getBounds(),
                                                (List<TypeMirror>)arg1.getBounds());
    }

    @Override
    protected int doHash(TypeParameterElement arg0) {
      // We don't use the hash code.
      return 0;
    }
  };

  public void appendParameterList(StringBuilder message, List<? extends TypeParameterElement> tpeList) {
    boolean first = true;
    for (TypeParameterElement tpe : tpeList) {
      if (first) {
        first = false;
      } else {
        message.append(',');
      }
      message.append(tpe.toString());
      boolean iFirst = true;
      for (TypeMirror bound : getExplicitBounds(tpe)) {
        if (iFirst) {
          message.append(" extends ");
          iFirst = false;
        } else {
          message.append(',');
        }
        message.append(bound);
      }
    }
  }

  @SuppressWarnings({"unchecked"})
  public boolean isSameParameterList(List<? extends TypeParameterElement> l1, List<? extends TypeParameterElement> l2) {
    // Cast is necessary because of a flaw in the API design of "PairwiseEquivalent",
    // a flaw that is even acknowledged in the source.
    // Our casts are safe because we're not trying to add elements to the list
    // and therefore can't violate the constraint.
    return typeEq.pairwise().equivalent((List<TypeParameterElement>)l1,
                                        (List<TypeParameterElement>)l2);
  }
}
