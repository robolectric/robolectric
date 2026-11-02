package org.robolectric.kotest.internal

import java.util.Collections
import java.util.WeakHashMap
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FrameNode
import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.TryCatchBlockNode

/**
 * Changes a class so that a failure of its static initializer is kept, and thrown by its
 * constructors instead. The class can then be initialized outside of Android, which Kotest does
 * with a spec class, and still can't be constructed there, which the extension never does.
 *
 * The class of an object is left to fail: its static initializer is what creates the object.
 */
internal object InitializerGuard {
  private const val INITIALIZER = "<clinit>"
  private const val CONSTRUCTOR = "<init>"
  private const val THROWABLE = "java/lang/Throwable"
  private const val MAJOR_VERSION = 0xFFFF
  private val OWNER = Type.getInternalName(InitializerGuard::class.java)

  // The failures of the static initializers of the guarded classes.
  private val failures: MutableMap<Class<*>, Throwable> = Collections.synchronizedMap(WeakHashMap())

  /** Keeps the failure of the static initializer of a guarded class, which calls this. */
  @JvmStatic
  fun keep(type: Class<*>, failure: Throwable) {
    // There is no object without its static initializer.
    if (ObjectSpecs.isObject(type)) {
      val applies = ObjectSpecs.appliesExtension(type)
      throw if (applies) IllegalStateException(ObjectSpecs.hint(type), failure) else failure
    }
    failures[type] = failure
  }

  /** Throws the failure that was kept for a guarded class, whose constructors call this. */
  @JvmStatic
  fun rethrow(type: Class<*>) {
    val failure = failures[type] ?: return
    // As the JVM does for a class that failed to initialize.
    throw failure as? Error ?: ExceptionInInitializerError(failure)
  }

  /** Returns the guarded class, or null if the class has no static initializer left to guard. */
  fun guard(bytes: ByteArray): ByteArray? {
    val type = ClassNode()
    ClassReader(bytes).accept(type, 0)
    // Not one that is guarded already.
    val initializer =
      type.methods
        .firstOrNull { it.name == INITIALIZER }
        ?.takeUnless { method ->
          method.instructions.any { it is MethodInsnNode && it.owner == OWNER }
        }
    // The handler needs a stack map frame, which older classes don't have.
    if (initializer == null || type.version and MAJOR_VERSION < Opcodes.V1_7) {
      return null
    }
    val self = Type.getObjectType(type.name)
    keepFailure(initializer, self)
    type.methods.filter { it.name == CONSTRUCTOR }.forEach { rethrowFailure(it, self) }
    return ClassWriter(ClassWriter.COMPUTE_MAXS).also { type.accept(it) }.toByteArray()
  }

  /** Makes the static initializer catch what it throws, and keep it. */
  private fun keepFailure(initializer: MethodNode, self: Type) {
    val start = LabelNode()
    val end = LabelNode()
    val handler = LabelNode()
    with(initializer.instructions) {
      insert(start)
      add(end)
      add(handler)
      // Whatever the initializer was doing when it failed, all it has now is what was thrown.
      add(FrameNode(Opcodes.F_FULL, 0, emptyArray(), 1, arrayOf(THROWABLE)))
      add(LdcInsnNode(self))
      add(InsnNode(Opcodes.SWAP))
      add(call("keep", "(Ljava/lang/Class;Ljava/lang/Throwable;)V"))
      add(InsnNode(Opcodes.RETURN))
    }
    // After the handlers of the initializer itself, which come first.
    initializer.tryCatchBlocks.add(TryCatchBlockNode(start, end, handler, THROWABLE))
  }

  /** Makes a constructor throw what the static initializer kept, before anything else. */
  private fun rethrowFailure(constructor: MethodNode, self: Type) {
    val check = InsnList()
    check.add(LdcInsnNode(self))
    check.add(call("rethrow", "(Ljava/lang/Class;)V"))
    constructor.instructions.insert(check)
  }

  private fun call(name: String, descriptor: String) =
    MethodInsnNode(Opcodes.INVOKESTATIC, OWNER, name, descriptor, false)
}
