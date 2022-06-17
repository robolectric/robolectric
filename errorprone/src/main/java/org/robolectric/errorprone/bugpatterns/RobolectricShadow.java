package org.robolectric.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.SeverityLevel.SUGGESTION;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;

import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.fixes.SuggestedFixes;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.doctree.DocCommentTree;
import com.sun.source.doctree.EndElementTree;
import com.sun.source.doctree.ReferenceTree;
import com.sun.source.doctree.StartElementTree;
import com.sun.source.doctree.TextTree;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.util.DocTreePath;
import com.sun.source.util.DocTreePathScanner;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.DCTree.DCDocComment;
import com.sun.tools.javac.tree.DCTree.DCReference;
import com.sun.tools.javac.tree.DCTree.DCStartElement;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Ensure Robolectric shadow's method marked with {@code @Implemenetation} is protected
 *
 * @author christianw@google.com (Christian Williams)
 */
@BugPattern(
    name = "RobolectricShadow",
    summary = "Robolectric @Implementation methods should be protected.",
    severity = SUGGESTION,
    documentSuppression = false,
    tags = StandardTags.REFACTORING)
public final class RobolectricShadow extends BugChecker implements ClassTreeMatcher {
  private static final Matcher<ClassTree> implementsClassMatcher = hasAnnotation(Implements.class);

  private static final Matcher<MethodTree> implementationMethodMatcher =
      hasAnnotation(Implementation.class);

  private boolean doScanJavadoc = false;

  @Override
  public Description matchClass(ClassTree classTree, VisitorState state) {
    List<Optional<SuggestedFix>> fixes = new ArrayList<>();

    if (implementsClassMatcher.matches(classTree, state)) {
      boolean inSdk = true;

      JavacTrees trees = JavacTrees.instance(state.context);
      for (AnnotationTree annotationTree : classTree.getModifiers().getAnnotations()) {
        JCIdent ident = (JCIdent) annotationTree.getAnnotationType();
        String annotationClassName = ident.sym.getQualifiedName().toString();
        if ("org.robolectric.annotation.Implements".equals(annotationClassName)) {
          for (ExpressionTree expressionTree : annotationTree.getArguments()) {
            JCAssign jcAnnotation = (JCAssign) expressionTree;
            if ("isInAndroidSdk".equals(state.getSourceForNode(jcAnnotation.lhs))
                && "false".equals(state.getSourceForNode(jcAnnotation.rhs))) {
              // shadows of classes not in the public Android SDK can keep their public methods.
              inSdk = false;
            }
          }
        }
      }

      if (inSdk) {
        new ImplementationMethodScanner(state, fixes, trees).scan(state.getPath(), null);
      }
    }

    SuggestedFix.Builder builder = SuggestedFix.builder();
    for (Optional<SuggestedFix> fix : fixes) {
      fix.ifPresent(builder::merge);
    }

    if (builder.isEmpty()) {
      return Description.NO_MATCH;
    } else {
      return describeMatch(classTree, builder.build());
    }
  }

  static final class DocTreeSymbolScanner extends DocTreePathScanner<Void, Void> {
    private final JavacTrees trees;
    private final List<Optional<SuggestedFix>> fixes;

    DocTreeSymbolScanner(JavacTrees trees, List<Optional<SuggestedFix>> fixes) {
      this.trees = trees;
      this.fixes = fixes;
    }

    @Override
    public Void visitStartElement(StartElementTree startElementTree, Void aVoid) {
      if (startElementTree.getName().toString().equalsIgnoreCase("p")) {
        DCStartElement node = (DCStartElement) startElementTree;

        DocTreePath path = getCurrentPath();
        int start = (int) node.getSourcePosition((DCDocComment) path.getDocComment()) + node.pos;
        int end = node.getEndPos((DCDocComment) getCurrentPath().getDocComment());

        fixes.add(Optional.of(SuggestedFix.replace(start, end, "")));
      }
      return super.visitStartElement(startElementTree, aVoid);
    }

    @Override
    public Void visitEndElement(EndElementTree endElementTree, Void aVoid) {
      return super.visitEndElement(endElementTree, aVoid);
    }

    @Override
    public Void visitText(TextTree textTree, Void aVoid) {
      System.out.println("textTree = " + textTree);
      return super.visitText(textTree, aVoid);
    }

    @Override
    public Void visitReference(ReferenceTree referenceTree, Void sink) {
      // do this first, it attributes the referenceTree as a side-effect
      trees.getElement(getCurrentPath());
      com.sun.source.util.TreeScanner<Void, Void> nonRecursiveScanner =
          new com.sun.source.util.TreeScanner<Void, Void>() {
            @Override
            public Void visitIdentifier(IdentifierTree tree, Void sink) {
              Symbol sym = ASTHelpers.getSymbol(tree);
              if (sym != null) {
                System.out.println("sym = " + sym);
              }
              return null;
            }
          };
      DCReference reference = (DCReference) referenceTree;
      nonRecursiveScanner.scan(reference.qualifierExpression, sink);
      nonRecursiveScanner.scan(reference.paramTypes, sink);
      return null;
    }
  }

  private class ImplementationMethodScanner extends TreePathScanner<Void, Void> {

    private final com.google.errorprone.VisitorState state;
    private final List<Optional<SuggestedFix>> fixes;
    private final JavacTrees trees;

    ImplementationMethodScanner(
        com.google.errorprone.VisitorState state,
        List<Optional<SuggestedFix>> fixes,
        JavacTrees trees) {
      this.state = state;
      this.fixes = fixes;
      this.trees = trees;
    }

    @Override
    public Void visitMethod(MethodTree methodTree, Void aVoid) {
      if (implementationMethodMatcher.matches(methodTree, state)) {
        processImplementationMethod(methodTree);
      }
      return super.visitMethod(methodTree, aVoid);
    }

    private void processImplementationMethod(MethodTree methodTree) {
      String methodName = methodTree.getName().toString();
      if ("toString".equals(methodName)
          || "equals".equals(methodName)
          || "hashCode".equals(methodName)) {
        return; // they need to remain public
      }
      ModifiersTree modifiersTree = methodTree.getModifiers();
      for (AnnotationTree annotationTree : modifiersTree.getAnnotations()) {
        JCIdent ident = (JCIdent) annotationTree.getAnnotationType();
        String annotationClassName = ident.sym.getQualifiedName().toString();
        if ("java.lang.Override".equals(annotationClassName)) {
          // can't have more restrictive permissions than the overridden method.
          return;
        }
        if ("org.robolectric.annotation.HiddenApi".equals(annotationClassName)) {
          // @HiddenApi implementation methods can stay public for the convenience of tests.
          return;
        }
      }

      Set<Modifier> modifiers = modifiersTree.getFlags();
      if (!modifiers.contains(Modifier.PROTECTED)) {
        fixes.add(
            SuggestedFixes.removeModifiers(methodTree, state, Modifier.PUBLIC, Modifier.PRIVATE));
        fixes.add(SuggestedFixes.addModifiers(methodTree, state, Modifier.PROTECTED));
      }

      if (doScanJavadoc) {
        scanJavadoc();
      }
    }

    private void scanJavadoc() {
      DocCommentTree commentTree = trees.getDocCommentTree(getCurrentPath());
      if (commentTree != null) {
        DocTreePath docTrees = new DocTreePath(getCurrentPath(), commentTree);
        new DocTreeSymbolScanner(trees, fixes).scan(docTrees, null);
      }
    }
  }
}
