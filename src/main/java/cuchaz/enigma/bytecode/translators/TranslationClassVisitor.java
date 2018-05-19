/*******************************************************************************
 * Copyright (c) 2015 Jeff Martin.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public
 * License v3.0 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Contributors:
 * Jeff Martin - initial API and implementation
 ******************************************************************************/

package cuchaz.enigma.bytecode.translators;

import cuchaz.enigma.analysis.JarIndex;
import cuchaz.enigma.bytecode.AccessFlags;
import cuchaz.enigma.mapping.*;
import cuchaz.enigma.mapping.entry.*;
import org.objectweb.asm.*;

import java.util.regex.Pattern;

public class TranslationClassVisitor extends ClassVisitor {
	private static final Pattern OBJECT_PATTERN = Pattern.compile(".*:Ljava/lang/Object;:.*");

	private final Translator translator;
	private final JarIndex jarIndex;
	private final ReferencedEntryPool entryPool;

	private ClassDefEntry obfClassEntry;

	public TranslationClassVisitor(Translator translator, JarIndex jarIndex, ReferencedEntryPool entryPool, int api, ClassVisitor cv) {
		super(api, cv);
		this.translator = translator;
		this.jarIndex = jarIndex;
		this.entryPool = entryPool;
	}

	@Override
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		if (signature != null && OBJECT_PATTERN.matcher(signature).matches()) {
			signature = signature.replaceAll(":Ljava/lang/Object;:", "::");
		}
		obfClassEntry = new ClassDefEntry(name, new AccessFlags(access));
		ClassDefEntry entry = translator.getTranslatedClassDef(obfClassEntry);
		ClassEntry superEntry = translator.getTranslatedClass(entryPool.getClass(superName));
		String[] translatedInterfaces = new String[interfaces.length];
		for (int i = 0; i < interfaces.length; i++) {
			translatedInterfaces[i] = translator.getTranslatedClass(entryPool.getClass(interfaces[i])).getName();
		}
		super.visit(version, entry.getAccess().getFlags(), entry.getName(), signature, superEntry.getName(), translatedInterfaces);
	}

	@Override
	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		FieldDefEntry entry = new FieldDefEntry(obfClassEntry, name, new TypeDescriptor(desc), new AccessFlags(access));
		FieldDefEntry translatedEntry = translator.getTranslatedFieldDef(entry);
		FieldVisitor fv = super.visitField(translatedEntry.getAccess().getFlags(), translatedEntry.getName(), translatedEntry.getDesc().toString(), signature, value);
		return new TranslationFieldVisitor(translator, translatedEntry, api, fv);
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		MethodDefEntry entry = new MethodDefEntry(obfClassEntry, name, new MethodDescriptor(desc), new AccessFlags(access));
		MethodDefEntry translatedEntry = translator.getTranslatedMethodDef(entry);
		if (jarIndex.getBridgedMethod(entry) != null) {
			translatedEntry.getAccess().setBridged();
		}
		String[] translatedExceptions = new String[exceptions.length];
		for (int i = 0; i < exceptions.length; i++) {
			translatedExceptions[i] = translator.getTranslatedClass(entryPool.getClass(exceptions[i])).getName();
		}
		MethodVisitor mv = super.visitMethod(translatedEntry.getAccess().getFlags(), translatedEntry.getName(), translatedEntry.getDesc().toString(), signature, translatedExceptions);
		return new TranslationMethodVisitor(translator, translatedEntry, api, mv);
	}

	@Override
	public void visitOuterClass(String owner, String name, String desc) {
		ClassEntry ownerEntry = translator.getTranslatedClass(entryPool.getClass(owner));
		String translatedDesc = desc != null ? translator.getTranslatedTypeDesc(new TypeDescriptor(desc)).toString() : desc;
		if (name != null) {
			ClassEntry entry = translator.getTranslatedClass(entryPool.getClass(name));
			super.visitOuterClass(ownerEntry.getName(), entry.getName(), translatedDesc);
		} else {
			super.visitOuterClass(ownerEntry.getName(), name, translatedDesc);
		}
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		TypeDescriptor translatedDesc = translator.getTranslatedTypeDesc(new TypeDescriptor(desc));
		AnnotationVisitor av = super.visitAnnotation(translatedDesc.toString(), visible);
		return new TranslationAnnotationVisitor(translator, translatedDesc.getTypeEntry(), api, av);
	}

	@Override
	public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
		TypeDescriptor translatedDesc = translator.getTranslatedTypeDesc(new TypeDescriptor(desc));
		AnnotationVisitor av = super.visitTypeAnnotation(typeRef, typePath, translatedDesc.toString(), visible);
		return new TranslationAnnotationVisitor(translator, translatedDesc.getTypeEntry(), api, av);
	}

	@Override
	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		// If this is not an anonymous class
		if (innerName != null && outerName != null) {
			ClassDefEntry translatedEntry = translator.getTranslatedClassDef(new ClassDefEntry(innerName, new AccessFlags(access)));
			ClassEntry outerEntry = translator.getTranslatedClass(entryPool.getClass(outerName));
			ClassEntry innerEntry = translator.getTranslatedClass(entryPool.getClass(innerName));
			super.visitInnerClass(translatedEntry.getName(), outerEntry.getName(), innerEntry.getName(), translatedEntry.getAccess().getFlags());
		} else {
			int separatorIndex = name.lastIndexOf("$");
			String parentName = name.substring(0, separatorIndex);
			String childName = name.substring(separatorIndex + 1);
			ClassEntry outerEntry = translator.getTranslatedClass(entryPool.getClass(parentName));
			super.visitInnerClass(outerEntry.getName() + "$" + childName, outerName, innerName, access);
		}
	}
}
