import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

import java.util.jar.JarEntry
import java.util.jar.JarInputStream

import static java.util.Arrays.asList
import static org.objectweb.asm.Opcodes.ACC_PRIVATE

class CheckApiChangesPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create("checkApiChanges", CheckApiChangesExtension)

        project.configurations {
            checkApiChanges
        }

        project.afterEvaluate {
            project.dependencies.checkApiChanges("${project.checkApiChanges.baseArtifact}@jar") {
                transitive = false
                force = true
            }
        }

        project.task('checkForApiChanges', dependsOn: 'jar') {
            doLast {
                Map<ClassMethod, Change> changedClassMethods = new TreeMap<>()

                def baseUrls = project.configurations.checkApiChanges*.toURI()*.toURL()
                Map<String, ClassMethod> prevClassMethods = findClassMethods(baseUrls)
                Map<String, ClassMethod> curClassMethods = findClassMethods(asList(new URL("file://${project.jar.archivePath}")))

                Set<String> allMethods = new TreeSet<>(prevClassMethods.keySet())
                allMethods.addAll(curClassMethods.keySet())

                for (String classMethodName : allMethods) {
                    ClassMethod prevClassMethod = prevClassMethods.get(classMethodName)
                    ClassMethod curClassMethod = curClassMethods.get(classMethodName)

                    if (prevClassMethod == null) {
                        // added
                        if (curClassMethod.visible) {
                            changedClassMethods.put(curClassMethod, Change.ADDED)
                        }
                    } else if (curClassMethod == null) {
                        // removed
                        if (prevClassMethod.visible && !prevClassMethod.deprecated) {
                            changedClassMethods.put(prevClassMethod, Change.REMOVED)
                        }
                    } else {
//                        println "changed: $classMethodName"
                    }
                }

                String prevClassName = null
                def introClass = { classMethod ->
                    if (classMethod.className != prevClassName) {
                        prevClassName = classMethod.className
                        println "\n$prevClassName:"
                    }
                }

                def entryPoints = project.checkApiChanges.entryPoints
                Closure matchesEntryPoint = { ClassMethod classMethod ->
                    for (String entryPoint : entryPoints) {
                        if (classMethod.className.matches(entryPoint)) {
                            return true
                        }
                    }
                    return false
                }

                for (Map.Entry<ClassMethod, Change> change : changedClassMethods.entrySet()) {
                    def classMethod = change.key
                    def changeType = change.value

                    def showAllChanges = true // todo: only show stuff that's interesting...
                    if (matchesEntryPoint(classMethod) || showAllChanges) {
                        introClass(classMethod)

                        switch (changeType) {
                            case Change.ADDED:
                                println "+ ${classMethod.methodDesc}"
                                break
                            case Change.REMOVED:
                                println "- ${classMethod.methodDesc} (not previously @Deprecated)"
                                break
                        }
                    }


                }
            }
        }
    }

    private Map<String, ClassMethod> findClassMethods(List<URL> baseUrls) {
        Map<String, ClassMethod> classMethods = new HashMap<>()
        for (URL url : baseUrls) {
            if (url.protocol == 'file') {
                def file = new File(url.path)
                def stream = new FileInputStream(file)
                def jarStream = new JarInputStream(stream)
                while (true) {
                    JarEntry entry = jarStream.nextJarEntry
                    if (entry == null) break

                    if (!entry.directory && entry.name.endsWith(".class")) {
                        def reader = new ClassReader(jarStream)
                        def node = new ClassNode()
                        reader.accept(node, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES)
                        for (MethodNode method : node.methods) {
                            def classMethod = new ClassMethod(node, method)
                            classMethods.put(classMethod.desc, classMethod)
                        }
                    }
                }
                stream.close()
            }
        }
        classMethods
    }

    static enum Change {
        ADDED, REMOVED
    }

    static class ClassMethod implements Comparable<ClassMethod> {
        ClassNode classNode
        MethodNode methodNode

        ClassMethod(ClassNode classNode, MethodNode methodNode) {
            this.classNode = classNode
            this.methodNode = methodNode
        }

        boolean equals(o) {
            if (this.is(o)) return true
            if (getClass() != o.class) return false

            ClassMethod that = (ClassMethod) o

            if (classNode.name != that.classNode.name) return false
            if (methodNode.name != that.methodNode.name) return false
            if (methodNode.signature != that.methodNode.signature) return false

            return true
        }

        int hashCode() {
            int result
            result = (classNode.name != null ? classNode.name.hashCode() : 0)
            result = 31 * result + (methodNode.name != null ? methodNode.name.hashCode() : 0)
            result = 31 * result + (methodNode.signature != null ? methodNode.signature.hashCode() : 0)
            return result
        }

        public String getDesc() {
            return "$className#$methodDesc"
        }

        private String getMethodDesc() {
            def args = new StringBuilder()
            def returnType = new StringBuilder()
            def buf = args

            int arrayDepth = 0
            def write = { typeName ->
                if (buf.size() > 0) buf.append(", ")
                buf.append(typeName)
                for (; arrayDepth > 0; arrayDepth--) {
                    buf.append("[]")
                }
            }

            def chars = methodNode.desc.toCharArray()
            def i = 0

            def readObj = {
                if (buf.size() > 0) buf.append(", ")
                for (; i < chars.length; i++) {
                    char c = chars[i]
                    if (c == ';' as char) break
                    buf.append((c == '/' as char) ? '.' : c)
                }
            }

            for (; i < chars.length;) {
                def c = chars[i++]
                switch (c) {
                    case '(': break;
                    case ')': buf = returnType; break;
                    case '[': arrayDepth++; break;
                    case 'Z': write('boolean'); break;
                    case 'B': write('byte'); break;
                    case 'S': write('short'); break;
                    case 'I': write('int'); break;
                    case 'J': write('long'); break;
                    case 'F': write('float'); break;
                    case 'D': write('double'); break;
                    case 'C': write('char'); break;
                    case 'L': readObj(); break;
                    case 'V': write('void'); break;
                }
            }
            "${returnType.toString()} $methodNode.name(${args.toString()})"
        }

        @Override
        public String toString() {
            return internalName();
        }

        private String internalName() {
            classNode.name + "#$methodNode.name$methodNode.desc"
        }

        private String getSignature() {
            methodNode.signature == null ? "()V" : methodNode.signature
        }

        private String getClassName() {
            classNode.name.replace('/', '.')
        }

        boolean isDeprecated() {
            for (AnnotationNode annotationNode : methodNode.visibleAnnotations) {
                if (annotationNode.desc == "Ljava/lang/Deprecated;") {
                    return true
                }
            }
            false
        }

        boolean isVisible() {
            classNode.access != ACC_PRIVATE && !(classNode.name =~ /\$[0-9]/) && !(methodNode.name =~ /^access\$/)
        }

        @Override
        int compareTo(ClassMethod o) {
            internalName() <=> o.internalName()
        }
    }
}

class CheckApiChangesExtension {
    String baseArtifact

    List<String> entryPoints = new ArrayList<>()
}