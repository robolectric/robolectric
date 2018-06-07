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

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** @author christianw@google.com (Christian Williams) */
@RunWith(JUnit4.class)
public class RobolectricShadowTest {
  private BugCheckerRefactoringTestHelper testHelper;

  @Before
  public void setUp() {
    this.testHelper =
        BugCheckerRefactoringTestHelper.newInstance(new RobolectricShadow(), getClass());
  }

  @Test
  public void implMethodsShouldBeProtected() throws IOException {
    testHelper
        .addInputLines(
            "in/SomeShadow.java",
            "import org.robolectric.annotation.Implementation;",
            "import org.robolectric.annotation.Implements;",
            "",
            "@Implements(Object.class)",
            "public class SomeShadow {",
            "  @Implementation public void publicMethod() {}",
            "  @Implementation protected void protectedMethod() {}",
            "  @Implementation void packageMethod() {}",
            "  @Implementation private void privateMethod() {}",
            "}")
        .addOutputLines(
            "in/SomeShadow.java",
            "import org.robolectric.annotation.Implementation;",
            "import org.robolectric.annotation.Implements;",
            "",
            "@Implements(Object.class)",
            "public class SomeShadow {",
            "  @Implementation protected void publicMethod() {}",
            "  @Implementation protected void protectedMethod() {}",
            "  @Implementation protected void packageMethod() {}",
            "  @Implementation protected void privateMethod() {}",
            "}")
        .doTest();
  }

  @Test
  public void implMethodJavadocShouldBeMarkdown() throws Exception {
    testHelper
        .addInputLines(
            "in/SomeShadow.java",
            "import org.robolectric.annotation.Implementation;",
            "import org.robolectric.annotation.Implements;",
            "",
            "@Implements(Object.class)",
            "public class SomeShadow {",
            "  /**",
            "   * <p>Should be markdown!</p>",
            "   */",
            "  @Implementation public void aMethod() {}",
            "}")
        .addOutputLines(
            "in/SomeShadow.java",
            "import org.robolectric.annotation.Implementation;",
            "import org.robolectric.annotation.Implements;",
            "",
            "@Implements(Object.class)",
            "public class SomeShadow {",
            "  /**",
            "   * Should be markdown!",
            "   */",
            "  @Implementation protected void aMethod() {}",
            "}")
        .doTest();
  }
}
