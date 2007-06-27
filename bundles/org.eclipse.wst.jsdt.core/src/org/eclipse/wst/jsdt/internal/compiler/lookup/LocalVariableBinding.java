/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.internal.compiler.lookup;

import org.eclipse.wst.jsdt.internal.compiler.ast.ASTNode;
import org.eclipse.wst.jsdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.ast.AbstractVariableDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.ast.Annotation;
import org.eclipse.wst.jsdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.wst.jsdt.internal.compiler.impl.Constant;
import org.eclipse.wst.jsdt.internal.compiler.impl.ReferenceContext;

public class LocalVariableBinding extends VariableBinding {

	public int resolvedPosition; // for code generation (position in method context)
	
	public static final int UNUSED = 0;
	public static final int USED = 1;
	public static final int FAKE_USED = 2;
	public int useFlag; // for flow analysis (default is UNUSED)
	
	public BlockScope declaringScope; // back-pointer to its declaring scope
	public LocalDeclaration declaration; // for source-positions

	public int[] initializationPCs;
	public int initializationCount = 0;

	// for synthetic local variables	
	// if declaration slot is not positionned, the variable will not be listed in attribute
	// note that the name of a variable should be chosen so as not to conflict with user ones (usually starting with a space char is all needed)
	public LocalVariableBinding(char[] name, TypeBinding type, int modifiers, boolean isArgument) {
		super(name, type, modifiers, isArgument ? Constant.NotAConstant : null);
		if (isArgument) this.tagBits |= TagBits.IsArgument;
	}
	
	// regular local variable or argument
	public LocalVariableBinding(LocalDeclaration declaration, TypeBinding type, int modifiers, boolean isArgument) {

		this(declaration.name, type!=null ? type : BaseTypeBinding.UNKNOWN, modifiers, isArgument);
		this.declaration = declaration;
	}

	/* API
	* Answer the receiver's binding type from Binding.BindingID.
	*/
	public final int kind() {

		return LOCAL;
	}
	
	/*
	 * declaringUniqueKey # scopeIndex / varName
	 * p.X { void foo() { int local; } } --> Lp/X;.foo()V#1/local
	 */
	public char[] computeUniqueKey(boolean isLeaf) {
		StringBuffer buffer = new StringBuffer();
		
		// declaring method or type
 		BlockScope scope = this.declaringScope;
		
		if (scope instanceof CompilationUnitScope) {
			CompilationUnitScope compilationUnitScope = (CompilationUnitScope) scope;
			buffer.append(compilationUnitScope.referenceContext.compilationUnitBinding.computeUniqueKey(false));
		}
		else
		{
			ReferenceContext referenceContext = null;
			MethodScope methodScope = scope instanceof MethodScope ? (MethodScope) scope : scope.enclosingMethodScope();
			if (methodScope!=null)
			{
				referenceContext = methodScope.referenceContext;
			}
			else
				referenceContext =	scope.enclosingCompilationUnit().scope.referenceCompilationUnit();
			if (referenceContext instanceof AbstractMethodDeclaration) {
				MethodBinding methodBinding = ((AbstractMethodDeclaration) referenceContext).binding;
				if (methodBinding != null) {
					buffer.append(methodBinding.computeUniqueKey(false/*not a leaf*/));
				}
			} else if (referenceContext instanceof TypeDeclaration) {
				TypeBinding typeBinding = ((TypeDeclaration) referenceContext).binding;
				if (typeBinding != null) {
					buffer.append(typeBinding.computeUniqueKey(false/*not a leaf*/));
				}
			} else if (referenceContext instanceof CompilationUnitDeclaration) {
				 CompilationUnitBinding compilationUnitBinding = ((CompilationUnitDeclaration) referenceContext).compilationUnitBinding;
				if (compilationUnitBinding != null) {
					buffer.append(compilationUnitBinding.computeUniqueKey(false/*not a leaf*/));
				}
			}
		}

		// scope index
		getScopeKey(scope, buffer);

		// variable name
		buffer.append('#');
		buffer.append(this.name);
		
		int length = buffer.length();
		char[] uniqueKey = new char[length];
		buffer.getChars(0, length, uniqueKey, 0);
		return uniqueKey;
	}

	public AnnotationBinding[] getAnnotations() {
		if (this.declaringScope == null)
			return Binding.NO_ANNOTATIONS;
		SourceTypeBinding sourceType = this.declaringScope.enclosingSourceType();
		if (sourceType == null)
			return Binding.NO_ANNOTATIONS;

		AnnotationBinding[] annotations = sourceType.retrieveAnnotations(this);
		if ((this.tagBits & TagBits.AnnotationResolved) == 0) {
			if (((this.tagBits & TagBits.IsArgument) != 0) && this.declaration != null) {
				Annotation[] annotationNodes = declaration.annotations;
				if (annotationNodes != null) {
					int length = annotationNodes.length;
					ASTNode.resolveAnnotations(this.declaringScope, annotationNodes, this);
					annotations = new AnnotationBinding[length];
					for (int i = 0; i < length; i++)
						annotations[i] = new AnnotationBinding(annotationNodes[i]);
					setAnnotations(annotations);
				}
			}
		}
		return annotations;
	}

	private void getScopeKey(BlockScope scope, StringBuffer buffer) {
		int scopeIndex = scope.scopeIndex();
		if (scopeIndex != -1) {
			getScopeKey((BlockScope)scope.parent, buffer);
			buffer.append('#');
			buffer.append(scopeIndex);
		}
	}
	
	// Answer whether the variable binding is a secret variable added for code gen purposes
	public boolean isSecret() {

		return declaration == null && (this.tagBits & TagBits.IsArgument) == 0;
	}

	public void recordInitializationEndPC(int pc) {

		if (initializationPCs[((initializationCount - 1) << 1) + 1] == -1)
			initializationPCs[((initializationCount - 1) << 1) + 1] = pc;
	}

	public void recordInitializationStartPC(int pc) {

		if (initializationPCs == null) 	return;
		if (initializationCount > 0) {
			int previousEndPC = initializationPCs[ ((initializationCount - 1) << 1) + 1];
			 // interval still open, keep using it (108180)
			if (previousEndPC == -1) {
				return;
			}
			// optimize cases where reopening a contiguous interval
			if (previousEndPC == pc) {
				initializationPCs[ ((initializationCount - 1) << 1) + 1] = -1; // reuse previous interval (its range will be augmented)
				return;
			}
		}
		int index = initializationCount << 1;
		if (index == initializationPCs.length) {
			System.arraycopy(initializationPCs, 0, (initializationPCs = new int[initializationCount << 2]), 0, index);
		}
		initializationPCs[index] = pc;
		initializationPCs[index + 1] = -1;
		initializationCount++;
	}

	public void setAnnotations(AnnotationBinding[] annotations) {
		if (this.declaringScope == null) return;

		SourceTypeBinding sourceType = this.declaringScope.enclosingSourceType();
		if (sourceType != null)
			sourceType.storeAnnotations(this, annotations);
	}

	public  boolean isFor(AbstractVariableDeclaration variableDeclaration)
	{
		return variableDeclaration.equals(this.declaration);
	}
	public String toString() {

		String s = super.toString();
		switch (useFlag){
			case USED:
				s += "[pos: " + String.valueOf(resolvedPosition) + "]"; //$NON-NLS-2$ //$NON-NLS-1$
				break;
			case UNUSED:
				s += "[pos: unused]"; //$NON-NLS-1$
				break;
			case FAKE_USED:
				s += "[pos: fake_used]"; //$NON-NLS-1$
				break;
		}
		s += "[id:" + String.valueOf(id) + "]"; //$NON-NLS-2$ //$NON-NLS-1$
		if (initializationCount > 0) {
			s += "[pc: "; //$NON-NLS-1$
			for (int i = 0; i < initializationCount; i++) {
				if (i > 0)
					s += ", "; //$NON-NLS-1$
				s += String.valueOf(initializationPCs[i << 1]) + "-" + ((initializationPCs[(i << 1) + 1] == -1) ? "?" : String.valueOf(initializationPCs[(i<< 1) + 1])); //$NON-NLS-2$ //$NON-NLS-1$
			}
			s += "]"; //$NON-NLS-1$
		}
		return s;
	}
}
