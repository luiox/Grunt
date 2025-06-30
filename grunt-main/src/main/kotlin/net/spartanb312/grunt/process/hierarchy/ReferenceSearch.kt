package net.spartanb312.grunt.process.hierarchy

import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode

object ReferenceSearch {

    fun checkMissing(classNode: ClassNode, hierarchy: Hierarchy): List<Hierarchy.ClassInfo> {
        val missingReference = mutableListOf<Hierarchy.ClassInfo>()
        for (method in classNode.methods) {
            missingReference.addAll(checkMissing(method, hierarchy))
        }
        return missingReference
    }

    fun checkMissing(methodNode: MethodNode, hierarchy: Hierarchy): List<Hierarchy.ClassInfo> {
        val missingReference = mutableListOf<Hierarchy.ClassInfo>()

        fun addMissingIfBroken(info: Hierarchy.ClassInfo) {
            if (info.isBroken) missingReference.add(info)
        }

        methodNode.instructions.forEach { insn ->
            if (insn is FieldInsnNode) {
                if (insn.owner[0] == '[') {
                    val type = Type.getType(insn.owner.substringAfterLast("["))

                    if (type.sort == Type.OBJECT) {
                        addMissingIfBroken(hierarchy.getClassInfo(type.internalName))
                    }
                } else {
                    addMissingIfBroken(hierarchy.getClassInfo(insn.owner))
                }
            }
            if (insn is MethodInsnNode) {
                if (insn.owner[0] == '[') {
                    val type = Type.getType(insn.owner.substringAfterLast("["))

                    if (type.sort == Type.OBJECT) {
                        addMissingIfBroken(hierarchy.getClassInfo(type.internalName))
                    }
                } else {
                    addMissingIfBroken(hierarchy.getClassInfo(insn.owner))
                }
            }
        }
        return missingReference
    }

}