package org.robolectric.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.matchers.Description.NO_MATCH;
import static com.google.errorprone.matchers.Matchers.instanceMethod;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static com.google.errorprone.util.ASTHelpers.hasAnnotation;
import static org.robolectric.errorprone.bugpatterns.Helpers.isCastableTo;
import static org.robolectric.errorprone.bugpatterns.Helpers.isInShadowClass;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.ClassTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.method.MethodMatchers.MethodNameMatcher;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.robolectric.annotation.Implements;

/**
 * Checks for the deprecated methods.
 *
 * @author christianw@google.com (Christian Williams)
 */
@AutoService(BugChecker.class)
@BugPattern(
    name = "DeprecatedMethods",
    summary = "Prefer supported APIs.",
    severity = WARNING,
    documentSuppression = false,
    tags = StandardTags.REFACTORING,
    link = "http://robolectric.org/migrating/#deprecations",
    linkType = LinkType.CUSTOM)
public class DeprecatedMethodsCheck extends BugChecker implements ClassTreeMatcher {
  private final java.util.List<MethodInvocationMatcher> matchers =
      Arrays.asList(
          // Matches calls to <code>ShadowApplication.getInstance()</code>.
          new MethodInvocationMatcher() {
            @Override
            MethodNameMatcher matcher() {
              return staticMethod()
                  .onClass(shadowName("org.robolectric.shadows.ShadowApplication"))
                  .named("getInstance");
            }

            @Override
            void replace(
                MethodInvocationTree tree,
                VisitorState state,
                SuggestedFix.Builder fixBuilder,
                HashMap<Tree, Runnable> possibleFixes) {
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
                Tree parent = state.getPath().getParentPath().getLeaf();

                possibleFixes.put(
                    parent,
                    () ->
                        fixBuilder
                            .addImport("org.robolectric.RuntimeEnvironment")
                            .replace(
                                tree,
                                wrapInShadows(
                                    state, fixBuilder, "RuntimeEnvironment.application")));
              }
            }
          },
          new AppGetLastMatcher(
              "org.robolectric.shadows.ShadowAlertDialog",
              "ShadowAlertDialog",
              "getLatestAlertDialog"),
          new AppGetLastMatcher(
              "org.robolectric.shadows.ShadowDialog", "ShadowDialog", "getLatestDialog"),
          new AppGetLastMatcher(
              "org.robolectric.shadows.ShadowPopupMenu", "ShadowPopupMenu", "getLatestPopupMenu"));

  abstract static class MethodInvocationMatcher {
    abstract MethodNameMatcher matcher();

    abstract void replace(
        MethodInvocationTree tree,
        VisitorState state,
        SuggestedFix.Builder fixBuilder,
        HashMap<Tree, Runnable> possibleFixes);
  }

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
          for (MethodInvocationMatcher matcher : matchers) {
            if (matcher.matcher().matches(tree, state)) {
              matcher.replace(tree, nowState, fixBuilder, possibleFixes);
              return null;
            }
          }
        }

        return super.visitMethodInvocation(tree, nowState);
      }
    }.scan(tree, state);

    for (Runnable runnable : possibleFixes.values()) {
      runnable.run();
    }

    Fix fix = fixBuilder.build();
    return fix.isEmpty() ? NO_MATCH : describeMatch(tree, fix);
  }

  private String wrapInShadows(
      VisitorState state, SuggestedFix.Builder fixBuilder, String content) {
    Set<String> imports = getImports(state);
    String shadowyContent;
    if (imports.contains(shadowName("org.robolectric.Shadows"))) {
      shadowyContent = shortShadowName("Shadows") + ".shadowOf(" + content + ")";
    } else {
      fixBuilder.addStaticImport(shadowName("org.robolectric.Shadows.shadowOf"));
      shadowyContent = "shadowOf(" + content + ")";
    }
    return shadowyContent;
  }

  private static Set<String> getImports(VisitorState state) {
    Set<String> imports = new HashSet<>();
    for (ImportTree importTree : state.getPath().getCompilationUnit().getImports()) {
      imports.add(state.getSourceForNode(importTree.getQualifiedIdentifier()));
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

  String shadowName(String className) {
    return className;
  }

  String shortShadowName(String shadowClassName) {
    return shadowClassName;
  }

  private class AppGetLastMatcher extends MethodInvocationMatcher {
    private final String methodName;
    private final String shadowClassName;
    private final String shadowShortClassName;

    AppGetLastMatcher(String shadowClassName, String shadowShortClassName, String methodName) {
      this.methodName = methodName;
      this.shadowClassName = shadowClassName;
      this.shadowShortClassName = shadowShortClassName;
    }

    @Override
    MethodNameMatcher matcher() {
      return instanceMethod()
          .onClass(isCastableTo(shadowName("org.robolectric.shadows.ShadowApplication")))
          .named(methodName);
    }

    @Override
    void replace(
        MethodInvocationTree tree,
        VisitorState state,
        SuggestedFix.Builder fixBuilder,
        HashMap<Tree, Runnable> possibleFixes) {
      possibleFixes.put(
          tree,
          () ->
              fixBuilder
                  .addImport(shadowName(shadowClassName))
                  .replace(
                      tree,
                      wrapInShadows(
                          state,
                          fixBuilder,
                          shortShadowName(shadowShortClassName) + "." + methodName + "()")));
    }
  }
}
