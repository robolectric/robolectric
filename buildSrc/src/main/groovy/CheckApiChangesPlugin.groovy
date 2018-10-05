import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.MethodNode

import java.util.jar.JarEntry
import java.util.jar.JarInputStream
import java.util.regex.Pattern

import static org.objectweb.asm.Opcodes.ACC_PRIVATE
import static org.objectweb.asm.Opcodes.ACC_PROTECTED
import static org.objectweb.asm.Opcodes.ACC_PUBLIC
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC

class CheckApiChangesPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create("checkApiChanges", CheckApiChangesExtension)

        project.configurations {
            checkApiChangesFrom
            checkApiChangesTo
        }

        project.afterEvaluate {
            project.checkApiChanges.from.each {
                project.dependencies.checkApiChangesFrom(it) {
                    transitive = false
                    force = true
                }
            }

            project.checkApiChanges.to.findAll { it instanceof String }.each {
                project.dependencies.checkApiChangesTo(it) {
                    transitive = false
                    force = true
                }
            }
        }

        project.task('checkForApiChanges', dependsOn: 'jar') {
            doLast {
                Map<ClassMethod, Change> changedClassMethods = new TreeMap<>()

                def fromUrls = project.configurations.checkApiChangesFrom*.toURI()*.toURL()
                println "fromUrls = ${fromUrls*.toString()*.replaceAll("^.*/", "")}"

                def jarUrls = project.checkApiChanges.to
                        .findAll { it instanceof Project }
                        .collect { it.jar.archivePath.toURL() }
                def toUrls = jarUrls + project.configurations.checkApiChangesTo*.toURI()*.toURL()
                println "toUrls = ${toUrls*.toString()*.replaceAll("^.*/", "")}"

                Analysis prev = new Analysis(fromUrls)
                Analysis cur = new Analysis(toUrls)

                Set<String> allMethods = new TreeSet<>(prev.classMethods.keySet())
                allMethods.addAll(cur.classMethods.keySet())

                Set<ClassMethod> deprecatedNotRemoved = new TreeSet<>()
                Set<ClassMethod> newlyDeprecated = new TreeSet<>()

                for (String classMethodName : allMethods) {
                    ClassMethod prevClassMethod = prev.classMethods.get(classMethodName)
                    ClassMethod curClassMethod = cur.classMethods.get(classMethodName)

                    if (prevClassMethod == null) {
                        // added
                        if (curClassMethod.visible) {
                            changedClassMethods.put(curClassMethod, Change.ADDED)
                        }
                    } else if (curClassMethod == null) {
                        def theClass = prevClassMethod.classNode.name.replace('/', '.')
                        def methodDesc = prevClassMethod.methodDesc
                        while (curClassMethod == null && cur.parents[theClass] != null) {
                            theClass = cur.parents[theClass]
                            def parentMethodName = "${theClass}#${methodDesc}"
                            curClassMethod = cur.classMethods[parentMethodName]
                        }

                        // removed
                        if (curClassMethod == null && prevClassMethod.visible && !prevClassMethod.deprecated) {
                            if (classMethodName.contains("getActivityTitle")) {
                                println "hi!"
                            }
                            changedClassMethods.put(prevClassMethod, Change.REMOVED)
                        }
                    } else {
                        if (prevClassMethod.deprecated) {
                            deprecatedNotRemoved << prevClassMethod;
                        } else if (curClassMethod.deprecated) {
                            newlyDeprecated << prevClassMethod;
                        }
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

                def expectedREs = project.checkApiChanges.expectedChanges.collect { Pattern.compile(it) }

                for (Map.Entry<ClassMethod, Change> change : changedClassMethods.entrySet()) {
                    def classMethod = change.key
                    def changeType = change.value

                    def showAllChanges = true // todo: only show stuff that's interesting...
                    if (matchesEntryPoint(classMethod) || showAllChanges) {
                        String classMethodDesc = classMethod.desc
                        def expected = expectedREs.any { it.matcher(classMethodDesc).find() }
                        if (!expected) {
                            introClass(classMethod)

                            switch (changeType) {
                                case Change.ADDED:
                                    println "+ ${classMethod.methodDesc}"
                                    break
                                case Change.REMOVED:
                                    println "- ${classMethod.methodDesc}"
                                    break
                            }
                        }
                    }
                }

                if (!deprecatedNotRemoved.empty) {
                    println "\nDeprecated but not removed:"
                    for (ClassMethod classMethod : deprecatedNotRemoved) {
                        introClass(classMethod)
                        println "* ${classMethod.methodDesc}"
                    }
                }

                if (!newlyDeprecated.empty) {
                    println "\nNewly deprecated:"
                    for (ClassMethod classMethod : newlyDeprecated) {
                        introClass(classMethod)
                        println "* ${classMethod.methodDesc}"
                    }
                }
            }
        }
    }

    static class Analysis {
        final Map<String, String> parents = new HashMap<>()
        final Map<String, ClassMethod> classMethods = new HashMap<>()

        Analysis(List<URL> baseUrls) {
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
                            def classNode = new ClassNode()
                            reader.accept(classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_FRAMES)

                            def superName = classNode.superName.replace('/', '.')
                            if (!"java.lang.Object".equals(superName)) {
                                parents[classNode.name.replace('/', '.')] = superName
                            }

                            if (bitSet(classNode.access, ACC_PUBLIC) || bitSet(classNode.access, ACC_PROTECTED)) {
                                for (MethodNode method : classNode.methods) {
                                    def classMethod = new ClassMethod(classNode, method, url)
                                    if (!bitSet(method.access, ACC_SYNTHETIC)) {
                                        classMethods.put(classMethod.desc, classMethod)
                                    }
                                }
                            }
                        }
                    }
                    stream.close()
                }
            }
            classMethods
        }

    }

    static enum Change {
        REMOVED,
        ADDED,
    }

    static class ClassMethod implements Comparable<ClassMethod> {
        final ClassNode classNode
        final MethodNode methodNode
        final URL originUrl

        ClassMethod(ClassNode classNode, MethodNode methodNode, URL originUrl) {
            this.classNode = classNode
            this.methodNode = methodNode
            this.originUrl = originUrl
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

        boolean hasParent() {
            parentClassName() != "java/lang/Object"
        }

        String parentClassName() {
            classNode.superName
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
                def objNameBuf = new StringBuilder()
                for (; i < chars.length; i++) {
                    char c = chars[i]
                    if (c == ';' as char) break
                    objNameBuf.append((c == '/' as char) ? '.' : c)
                }
                buf.append(objNameBuf.toString().replaceAll(/^java\.lang\./, ''))
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
            "$methodAccessString ${isHiddenApi() ? "@HiddenApi " : ""}${isImplementation() ? "@Implementation " : ""}$methodNode.name(${args.toString()}): ${returnType.toString()}"
        }

        @Override
        public String toString() {
            internalName
        }

        private String getInternalName() {
            classNode.name + "#$methodInternalName"
        }

        private String getMethodInternalName() {
            "$methodNode.name$methodNode.desc"
        }

        private String getSignature() {
            methodNode.signature == null ? "()V" : methodNode.signature
        }

        private String getClassName() {
            classNode.name.replace('/', '.')
        }

        boolean isDeprecated() {
            containsAnnotation(classNode.visibleAnnotations, "Ljava/lang/Deprecated;") ||
                    containsAnnotation(methodNode.visibleAnnotations, "Ljava/lang/Deprecated;")
        }

        boolean isImplementation() {
            containsAnnotation(methodNode.visibleAnnotations, "Lorg/robolectric/annotation/Implementation;")
        }

        boolean isHiddenApi() {
            containsAnnotation(methodNode.visibleAnnotations, "Lorg/robolectric/annotation/HiddenApi;")
        }

        String getMethodAccessString() {
            return getAccessString(methodNode.access)
        }

        private String getClassAccessString() {
            return getAccessString(classNode.access)
        }

        String getAccessString(int access) {
            if (bitSet(access, ACC_PROTECTED)) {
                return "protected"
            } else if (bitSet(access, ACC_PUBLIC)) {
                return "public"
            } else if (bitSet(access, ACC_PRIVATE)) {
                return "private"
            } else {
                return "[package]"
            }
        }

        boolean isVisible() {
            (bitSet(classNode.access, ACC_PUBLIC) || bitSet(classNode.access, ACC_PROTECTED)) &&
                    (bitSet(methodNode.access, ACC_PUBLIC) || bitSet(methodNode.access, ACC_PROTECTED)) &&
                    !bitSet(classNode.access, ACC_SYNTHETIC) &&
                    !(classNode.name =~ /\$[0-9]/) &&
                    !(methodNode.name =~ /^access\$/ || methodNode.name == '<clinit>')
        }

        private static boolean containsAnnotation(List<AnnotationNode> annotations, String annotationInternalName) {
            for (AnnotationNode annotationNode : annotations) {
                if (annotationNode.desc == annotationInternalName) {
                    return true
                }
            }
            return false
        }

        @Override
        int compareTo(ClassMethod o) {
            internalName <=> o.internalName
        }
    }

    private static boolean bitSet(int field, int bit) {
        (field & bit) == bit
    }
}

class CheckApiChangesExtension {
    String[] from
    Object[] to

    String[] entryPoints
    String[] expectedChanges
}