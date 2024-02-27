# Robolectric architecture

Robolectric is a unit testing framework that allows Android code to be tested on
the JVM without the need for an emulator or device. This allows tests to run
very quickly in a more hermetic environment. Robolectric has a complex
architecture and makes use of many advanced features of the JVM such as bytecode
instrumentation and custom ClassLoaders. This document provides a high level
overview of the architecture of Robolectric.

# Android framework Jars and instrumentation

At the heart of Robolectric are the Android framework Jars and the bytecode
instrumentation. The Android framework Jars are a collection of Jar files that
are built directly from Android platform sources. There is a single Jar file for
each version of Android. These Jar files can be built by checking out an AOSP
repo and building the
[robolectric-host-android\_all](https://cs.android.com/android/platform/superproject/main/+/main:external/robolectric/Android.bp;l=99)
target. Unlike the android.jar (stubs jar) files managed by Android Studio,
which only contain public method signatures, the Robolectric android-all Jars
contain the implementation of the Android Java framework. This gives Robolectric
the ability to use as much real Android code as possible. A new android-all jar
is uploaded to MavenCentral for each Android release. You can see the current
android-all jars
[here](https://repo1.maven.org/maven2/org/robolectric/android-all/).

However, the pristine android-all jars are not the ones used during tests.
Instead, Robolectric modifies the pristine android-all jars using bytecode
instrumentation (see
[ClassInstrumentor](https://github.com/robolectric/robolectric/blob/master/sandbox/src/main/java/org/robolectric/internal/bytecode/ClassInstrumentor.java)).
It performs several modifications:

1. All Android methods, including constructors and static initializers, are
   modified to support `shadowing`. This allows any method call to the Android
   framework to be intercepted by Robolectric and delegated to a shadow method.
   At a high level, this is done by iterating over each Android method and
   converting it into two methods: the original method (but renamed), and the
   `invokedynamic delegator` which can optionally invoke shadow methods if they
   are available.

1. Android constructors are specially modified to create shadow objects, if a
   shadow class is bound to the Android class being instantiated.

1. Because the Android version of Java core classes (libcore) contain subtle
   differences to the JDKs, certain problematic method calls have to be
   intercepted and rewritten. See
   [AndroidInterceptors](https://github.com/robolectric/robolectric/blob/master/sandbox/src/main/java/org/robolectric/interceptors/AndroidInterceptors.java).

1. Native methods undergo special instrumentation. Currently native methods are
   converted to no-op non-native methods that are shadowable by default.
   However, there is now a native variant of each method also created. There is
   more details about native code in a section below.

1. The `final` keyword is stripped from classes and methods.

1. Some bespoke pieces of instrumentation, such as supporting
   [SparseArray.set](https://github.com/robolectric/robolectric/blob/master/sandbox/src/main/java/org/robolectric/internal/bytecode/ClassInstrumentor.java#L201).

This instrumentation is typically performed when a new release of Robolectric is
made. These pre-instrumented Android-all jars are published on MavenCentral. See
the
[android-all-instrumented](https://repo1.maven.org/maven2/org/robolectric/android-all-instrumented/)
path. They are lazily downloaded and during tests runtime using
[MavenArtifactFetcher](https://github.com/robolectric/robolectric/blob/master/plugins/maven-dependency-resolver/src/main/java/org/robolectric/internal/dependency/MavenArtifactFetcher.java).

Although Robolectric supports shadowing for Android framework classes, it is
also possible for users to perform Robolectric instrumentation for any package
(with the exception of built in Java packages). This enables shadowing of
arbitrary third-party code.

# Shadows

By default when an Android method is invoked during a Robolectric test, the real
Android framework code is invoked. This is because a lot of Android framework
classes are pure Java code (e.g the
[Intent](https://cs.android.com/android/platform/superproject/main/+/main:frameworks/base/core/java/android/content/Intent.java)
class or the
[org.json](https://cs.android.com/android/platform/superproject/main/+/main:libcore/json/src/main/java/org/json/)
package) and that code can run on the JVM without any modifications needed.

However, there are cases where Robolectric needs to intercept and replace
Android method calls. This most commonly occurs when Android system service or
native methods are invoked. To do this, Robolectric uses a system called Shadow
classes.

Shadow classes are Java classes that contain the replacement code of Android
methods when they are invoked. Each shadow class is bound to specific Android
classes and methods through annotations. There are currently hundreds of shadow
classes that can be found
[here](https://github.com/robolectric/robolectric/tree/master/shadows/framework/src/main/java/org/robolectric/shadows).

Shadow classes may optionally contain public apis APIs that can customize the
behavior of the methods they are shadowing.

Robolectric allows tests to specify custom shadows as well to provide user
defined implementation for Android classes.

## Shadow Packages and the Robolectric Annotation Processor 

There are two categories of shadows: Robolectric’s built-in shadows that are
aggregated using the [Robolectric Annotation Processor
(RAP)](https://github.com/robolectric/robolectric/blob/master/processor/src/main/java/org/robolectric/annotation/processing/RobolectricProcessor.java),
and custom shadows that are commonly specified using `@Config(shadows = …)`. RAP
is configured to process all of the shadow files that exist in Robolectric’s
code. The main shadow package is [framework
shadows](https://github.com/robolectric/robolectric/tree/master/shadows/framework),
which contain shadows for the Android framework. There are other shadow packages
in Robolectric's code, such as [httpclient
shadows](https://github.com/robolectric/robolectric/tree/master/shadows/httpclient),
but all of them outside of framework shadows are deprecated. When Robolectric is
built, each shadow package is processed by RAP and a
[ShadowProvider](https://github.com/robolectric/robolectric/blob/master/shadowapi/src/main/java/org/robolectric/internal/ShadowProvider.java)
file is generated. For example, to see the ShadowProvider for the framework
shadows, you can run:

```sh
./gradlew :shadows:framework:assemble
cat ./shadows/framework/build/generated/src/apt/main/org/robolectric/Shadows.java
```

In this file you will see the class `public class Shadows implements
ShadowProvider`.

During runtime, Robolectric will use ServiceLoader to detect all shadow packages
that implement ShadowProvider and the shadow classes contained in them.

# Sandbox and ClassLoader

Before a Robolectric test is executed, a
[Sandbox](https://github.com/robolectric/robolectric/blob/master/sandbox/src/main/java/org/robolectric/internal/bytecode/Sandbox.java)
must be initialized. A Sandbox consists of some high-level structures that are
necessary to run a Robolectric test. It primarily contains a
[SandboxClassLoader](https://github.com/robolectric/robolectric/blob/master/sandbox/src/main/java/org/robolectric/internal/bytecode/SandboxClassLoader.java),
which is a custom ClassLoader that is bound to a specific instrumented
Android-all jar. Sandboxes also contain the ExecutorService that serves as the
main thread (UI thread) as well as high-level instrumentation configuration. The
SandboxClassLoader is installed as the default ClassLoader for the test method.
When any Android class is requested, SandboxClassLoader will attempt to load the
Android class from the instrumented Android-all Jar first. The primary goal of
SandboxClassLoader is to ensure that classes from the android.jar stubs jar are
not inadvertently loaded. When classes from the android.jar stubs jar are
loaded, attempting to invoke any method on them will result in a
`RuntimeException(“Stub!”)` error. Typically the Android stubs jar is on the
class path during a Robolectric test, but it is important not to load classes
from the stubs jar.

# Invokedynamic Delegators and ShadowWrangler

This section provides more detail for `invokedynamic delegators` that were
referenced in the instrumentation section. For an overview of the
`invokedynamic` JVM instructions, you can search for articles or watch [YouTube
videos such as this](https://www.youtube.com/watch?v=KhiECfzyVt0).

To reiterate, for any Android method, Robolectric’s instrumentation adds an
`invokedynamic delegator` that is responsible for determining at runtime to
either invoke the real Android framework code or a shadow method. The first time
an Android method is invoked in a Sandbox, it will result in a call to one of
the bootstrap methods in
[InvokeDynamicSupport](https://github.com/robolectric/robolectric/blob/master/sandbox/src/main/java/org/robolectric/internal/bytecode/InvokeDynamicSupport.java).
This will subsequently invoke the
[ShadowWrangler.findShadowMethodHandle](https://github.com/robolectric/robolectric/blob/master/sandbox/src/main/java/org/robolectric/internal/bytecode/ShadowWrangler.java#L197)
to determine if a shadow method exists for the method that is being invoked.  If
a shadow method is available a MethodHandle to it will be returned. Otherwise a
MethodHandle for the original framework code will be returned.

# Test lifecycle

There is a lot of work done by Robolectric before and after a test is run.
Besides the Sandbox and ClassLoader initialization mentioned above, there is
also extensive Android environment initialization that occurs before each test.
The high-level class for this is
[AndroidTestEnvironment](https://github.com/robolectric/robolectric/blob/master/robolectric/src/main/java/org/robolectric/android/internal/AndroidTestEnvironment.java).
This involves:

*   Initializing up the Looper mode (i.e. the scheduler)
*   Initializing system and app resources
*   Initializing the application context and system context
*   Loading the Android manifest for the test
*   Creating the Application object used for the test
*   Initializing the [display configuration](https://robolectric.org/device-configuration/)
*   Setting up the ActivityThread
*   Creating app directories

It is possible for users to extend the test environment setup using
[TestEnvironmentLifecyclePlugin](https://github.com/robolectric/robolectric/blob/master/pluginapi/src/main/java/org/robolectric/pluginapi/TestEnvironmentLifecyclePlugin.java).

Similarly, after each test, many Android classes are reset during
[RobolectricTestRunner.finallyAfterTest](https://github.com/robolectric/robolectric/blob/master/robolectric/src/main/java/org/robolectric/RobolectricTestRunner.java#L301).
This will iterate over all shadows and invoke their static `@Resetter` methods.

# Plugin System

Many parts of Robolectric can be customized using a plugin system based on
Java’s
[ServiceLoader](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html).
This extensibility is useful when running Robolectric in more constrained
environments. For example, by default, most of the Robolectric classes are
designed to work in a Gradle/Android Studio environment. However, there are
companies (such as Google) that use alternate build systems (such as Bazel), and
it can be helpful to have the ability to customize the behavior of some core
modules.

The
[pluginapi](https://github.com/robolectric/robolectric/tree/master/pluginapi/src)
subproject contains many extension points of Robolectric. However, virtually any
class that is loaded by Robolectric’s
[Injector](https://github.com/robolectric/robolectric/blob/master/utils/src/main/java/org/robolectric/util/inject/Injector.java)
has the ability to use
[PluginFinder](https://github.com/robolectric/robolectric/blob/master/utils/src/main/java/org/robolectric/util/inject/PluginFinder.java),
which means it can be extended at runtime.

Typically ServiceLoaders plugins can be easily written using the
[AutoService](https://github.com/google/auto/tree/main/service) project.

