package org.robolectric.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.Category.ANDROID;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.matchers.Description.NO_MATCH;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.google.errorprone.util.ASTHelpers.hasAnnotation;
import static org.robolectric.errorprone.bugpatterns.Helpers.isInShadowClass;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.ProvidesFix;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.robolectric.annotation.Implements;
import org.robolectric.errorprone.bugpatterns.ShadowUsageCheck.ShadowInliner;

/** @author christianw@google.com (Christian Williams) */
@AutoService(BugChecker.class)
@BugPattern(
    name = "DeprecatedMethods",
    summary = "Robolectric shadows shouldn't be stored to variables or fields.",
    category = ANDROID,
    severity = WARNING,
    documentSuppression = false,
    tags = StandardTags.REFACTORING,
    providesFix = ProvidesFix.REQUIRES_HUMAN_ATTENTION)
public final class DeprecatedMethodsCheck extends BugChecker implements ClassTreeMatcher {
  /** Matches calls to <code>ShadowApplication#getInstance()</code>. */
  private static final Matcher<MethodInvocationTree> shadowAppGetInstanceMatcher =
      Matchers.anyOf(
          staticMethod().onClass("org.robolectric.shadows.ShadowApplication").named("getInstance"),
          staticMethod()
              .onClass("xxx.XShadowApplication") // for tests
              .named("getInstance"));

  @Override
  public Description matchClass(ClassTree tree, VisitorState state) {
    if (isInShadowClass(state.getPath(), state)) {
      return NO_MATCH;
    }

    final SuggestedFix.Builder fixBuilder = SuggestedFix.builder();
    HashMap<Tree, Runnable> possibleFixes = new HashMap<>();

    new TreeScanner<Void, VisitorState>() {
      private boolean inShadowClass;

      @Override
      public Void visitClass(ClassTree classTree, VisitorState visitorState) {
        boolean priorInShadowClass = inShadowClass;
        inShadowClass = hasAnnotation(classTree, Implements.class, visitorState);
        try {
          return super.visitClass(classTree, visitorState);
        } finally {
          inShadowClass = priorInShadowClass;
        }
      }

      @Override
      public Void visitMethodInvocation(MethodInvocationTree tree, VisitorState state) {
        VisitorState nowState = state.withPath(TreePath.getPath(state.getPath(), tree));

        if (!inShadowClass) {
          if (shadowAppGetInstanceMatcher.matches(tree, state)) {
            MethodCall surroundingMethodCall = getSurroundingMethodCall(tree, state);

            if (surroundingMethodCall != null
                && surroundingMethodCall.getName().equals("getApplicationContext")) {
              // transform `ShadowApplication.getInstance().getApplicationContext()`
              //  to `RuntimeEnvironment.application`:

              fixBuilder
                  .replace(surroundingMethodCall.node, "RuntimeEnvironment.application")
                  .addImport("org.robolectric.RuntimeEnvironment");
            } else {
              // transform `ShadowApplication.getInstance()`
              //  to `shadowOf(RuntimeEnvironment.application)`:
              Tree parent = nowState.getPath().getParentPath().getLeaf();
              replaceAssignmentRhs(parent, createSyntheticShadowAccess(state));

              // replacements below might be removed, but always add this import...
              fixBuilder
                  .addImport("org.robolectric.RuntimeEnvironment")
                  .addImport("org.robolectric.Shadows");

              Set<String> imports = getImports(state);
              if (imports.contains("org.robolectric.Shadows")) {
                possibleFixes.put(
                    parent,
                    () ->
                        fixBuilder.replace(
                            tree, "Shadows.shadowOf(RuntimeEnvironment.application)"));
              } else {
                fixBuilder.addStaticImport("org.robolectric.Shadows.shadowOf");
                possibleFixes.put(
                    parent,
                    () -> fixBuilder.replace(tree, "shadowOf(RuntimeEnvironment.application)"));
              }
            }
          }
        }

        return super.visitMethodInvocation(tree, nowState);
      }
    }.scan(tree, state);

    if (!fixBuilder.isEmpty() || !possibleFixes.isEmpty()) {
      ShadowInliner shadowInliner = new ShadowInliner(fixBuilder, possibleFixes);
      shadowInliner.scan(tree, state);
    }

    for (Runnable runnable : possibleFixes.values()) {
      runnable.run();
    }

    Fix fix = fixBuilder.build();
    return fix.isEmpty() ? NO_MATCH : describeMatch(tree, fix);
  }

  private void replaceAssignmentRhs(Tree parent, JCExpression replacementExpr) {
    if (parent instanceof JCFieldAccess) {
      JCFieldAccess parentFieldAccess = (JCFieldAccess) parent;
      parentFieldAccess.selected = replacementExpr;
    } else if (parent instanceof JCAssign) {
      JCAssign parentAssign = (JCAssign) parent;
      parentAssign.rhs = replacementExpr;
    } else if (parent instanceof JCVariableDecl) {
      JCVariableDecl parentVariableDecl = (JCVariableDecl) parent;
      parentVariableDecl.init = replacementExpr;
    }
  }

  private JCMethodInvocation createSyntheticShadowAccess(VisitorState state) {
    TreeMaker treeMaker = state.getTreeMaker();
    JCExpression application =
        treeMaker.Select(
            treeMaker.Ident(findSymbol(state, "org.robolectric.RuntimeEnvironment")),
            findSymbol(state, "org.robolectric.RuntimeEnvironment", "application"));

    JCExpression shadowOfApp =
        treeMaker.Select(
            treeMaker.Ident(findSymbol(state, "org.robolectric.Shadows")),
            findSymbol(state, "org.robolectric.Shadows", "shadowOf(android.app.Application)"));

    JCMethodInvocation callShadowOf = treeMaker.Apply(null, shadowOfApp, List.of(application));
    callShadowOf.type = callShadowOf.meth.type;
    return callShadowOf;
  }

  private static Symbol findSymbol(VisitorState state, String className) {
    Symbol classSymbol = JavacElements.instance(state.context).getTypeElement(className);
    if (classSymbol == null) {
      throw new IllegalStateException("couldn't find symbol " + className);
    }
    return classSymbol;
  }

  private static Symbol findSymbol(VisitorState state, String className, String symbolToString) {
    Symbol classSymbol = findSymbol(state, className);

    for (Symbol symbol : classSymbol.getEnclosedElements()) {
      if (symbolToString.equals(symbol.toString())) {
        return symbol;
      }
    }

    throw new IllegalStateException("couldn't find symbol " + className + "." + symbolToString);
  }

  private static Set<String> getImports(VisitorState state) {
    Set<String> imports = new HashSet<>();
    for (ImportTree importTree : state.getPath().getCompilationUnit().getImports()) {
      imports.add(importTree.getQualifiedIdentifier().toString());
    }
    return imports;
  }

  private static MethodCall getSurroundingMethodCall(Tree node, VisitorState state) {
    TreePath nodePath = TreePath.getPath(state.getPath(), node);
    TreePath parentPath = nodePath.getParentPath();
    if (parentPath.getLeaf().getKind() == Kind.MEMBER_SELECT) {
      Tree grandparentNode = parentPath.getParentPath().getLeaf();
      if (grandparentNode.getKind() == Kind.METHOD_INVOCATION) {
        return new MethodCall((JCMethodInvocation) grandparentNode);
      }
    }

    return null;
  }

  static class MethodCall {

    private final JCMethodInvocation node;

    public MethodCall(JCMethodInvocation node) {
      this.node = node;
    }

    public String getName() {
      return ((JCFieldAccess) node.getMethodSelect()).name.toString();
    }
  }
}
