/*
 * Copyright 2016 The Error Prone Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.robolectric.errorprone.bugpatterns;

import static com.google.errorprone.BugPattern.Category.ANDROID;
import static com.google.errorprone.BugPattern.SeverityLevel.WARNING;
import static com.google.errorprone.matchers.Description.NO_MATCH;
import static com.google.errorprone.matchers.Matchers.staticMethod;
import static org.robolectric.errorprone.bugpatterns.Helpers.isInShadowClass;

import com.google.auto.service.AutoService;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.ProvidesFix;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.Fix;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.matchers.Matchers;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;

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
public final class DeprecatedMethodsCheck extends BugChecker
    implements MethodInvocationTreeMatcher {
  /** Matches calls to <code>ShadowApplication#getInstance()</code>. */
  private static final Matcher<MethodInvocationTree> shadowAppGetInstanceMatcher =
      Matchers.anyOf(
          staticMethod()
              .onClass("org.robolectric.shadows.ShadowApplication")
              .named("getInstance"),
          staticMethod()
              .onClass("xxx.XShadowApplication") // for tests
              .named("getInstance")
      );

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    if (isInShadowClass(state)) {
      return NO_MATCH;
    }

    SuggestedFix.Builder fixBuilder = SuggestedFix.builder();

    if (shadowAppGetInstanceMatcher.matches(tree, state)) {
      MethodCall surroundingMethodCall = getSurroundingMethodCall(tree, state);

      if (surroundingMethodCall != null
          && surroundingMethodCall.getName().equals("getApplicationContext")) {
        // transform `ShadowApplication.getInstance().getApplicationContext()`
        //  to `RuntimeEnvironment.application`:
        fixBuilder
            .replace(surroundingMethodCall.node, "RuntimeEnvironment.application")
            .addImport("org.robolectric.RuntimeEnvironment");
        // .removeImport("org.robolectric.shadows.ShadowApplication");
      } else {
        // transform `ShadowApplication.getInstance()`
        //  to `shadowOf(RuntimeEnvironment.application)`:
        fixBuilder
            .replace(tree, "shadowOf(RuntimeEnvironment.application)")
            .addImport("org.robolectric.RuntimeEnvironment")
            // .removeImport("org.robolectric.shadows.ShadowApplication")
            .addStaticImport("org.robolectric.Shadows.shadowOf");
      }
    }

    Fix fix = fixBuilder.build();
    return fix.isEmpty() ? NO_MATCH : describeMatch(tree, fix);
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
