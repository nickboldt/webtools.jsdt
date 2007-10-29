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
package org.eclipse.wst.jsdt.internal.compiler.ast;

import org.eclipse.wst.jsdt.internal.compiler.ASTVisitor;
import org.eclipse.wst.jsdt.internal.compiler.codegen.CodeStream;
import org.eclipse.wst.jsdt.internal.compiler.flow.FlowContext;
import org.eclipse.wst.jsdt.internal.compiler.flow.FlowInfo;
import org.eclipse.wst.jsdt.internal.compiler.impl.Constant;
import org.eclipse.wst.jsdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.wst.jsdt.internal.compiler.lookup.BlockScope;
import org.eclipse.wst.jsdt.internal.compiler.lookup.TypeBinding;

public class ArrayReference extends Reference {

	public Expression receiver;
	public Expression position;

	public ArrayReference(Expression rec, Expression pos) {
		this.receiver = rec;
		this.position = pos;
		sourceStart = rec.sourceStart;
	}

public FlowInfo analyseAssignment(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo,
		Assignment assignment,
		boolean compoundAssignment) {
	// TODO (maxime) optimization: unconditionalInits is applied to all existing calls
	if (assignment.expression == null) {
		return analyseCode(currentScope, flowContext, flowInfo);
	}
	return assignment
		.expression
		.analyseCode(
			currentScope,
			flowContext,
			analyseCode(currentScope, flowContext, flowInfo).unconditionalInits());
}

public FlowInfo analyseCode(
		BlockScope currentScope,
		FlowContext flowContext,
		FlowInfo flowInfo) {
	receiver.checkNPE(currentScope, flowContext, flowInfo);
	flowInfo = receiver.analyseCode(currentScope, flowContext, flowInfo);
	return position.analyseCode(currentScope, flowContext, flowInfo);
}

	public void generateAssignment(
		BlockScope currentScope,
		CodeStream codeStream,
		Assignment assignment,
		boolean valueRequired) {

		int pc = codeStream.position;
		receiver.generateCode(currentScope, codeStream, true);
		if (receiver instanceof CastExpression	// ((type[])null)[0]
				&& ((CastExpression)receiver).innermostCastedExpression().resolvedType == TypeBinding.NULL){
			codeStream.checkcast(receiver.resolvedType);
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
		position.generateCode(currentScope, codeStream, true);
		assignment.expression.generateCode(currentScope, codeStream, true);
		codeStream.arrayAtPut(this.resolvedType.id, valueRequired);
		if (valueRequired) {
			codeStream.generateImplicitConversion(assignment.implicitConversion);
		}
	}

	/**
	 * Code generation for a array reference
	 */
	public void generateCode(
		BlockScope currentScope,
		CodeStream codeStream,
		boolean valueRequired) {

		int pc = codeStream.position;
		receiver.generateCode(currentScope, codeStream, true);
		if (receiver instanceof CastExpression	// ((type[])null)[0]
				&& ((CastExpression)receiver).innermostCastedExpression().resolvedType == TypeBinding.NULL){
			codeStream.checkcast(receiver.resolvedType);
		}
		position.generateCode(currentScope, codeStream, true);
		codeStream.arrayAt(this.resolvedType.id);
		// Generating code for the potential runtime type checking
		if (valueRequired) {
			codeStream.generateImplicitConversion(implicitConversion);
		} else {
			if (this.resolvedType == TypeBinding.LONG
				|| this.resolvedType == TypeBinding.DOUBLE) {
				codeStream.pop2();
			} else {
				codeStream.pop();
			}
		}
		codeStream.recordPositionsFrom(pc, this.sourceStart);
	}

	public void generateCompoundAssignment(
		BlockScope currentScope,
		CodeStream codeStream,
		Expression expression,
		int operator,
		int assignmentImplicitConversion,
		boolean valueRequired) {

		receiver.generateCode(currentScope, codeStream, true);
		if (receiver instanceof CastExpression	// ((type[])null)[0]
				&& ((CastExpression)receiver).innermostCastedExpression().resolvedType == TypeBinding.NULL){
			codeStream.checkcast(receiver.resolvedType);
		}
		position.generateCode(currentScope, codeStream, true);
		codeStream.dup2();
		codeStream.arrayAt(this.resolvedType.id);
		int operationTypeID;
		switch(operationTypeID = (implicitConversion & IMPLICIT_CONVERSION_MASK) >> 4) {
			case T_JavaLangString :
			case T_JavaLangObject :
			case T_undefined :
				codeStream.generateStringConcatenationAppend(currentScope, null, expression);
				break;
			default :
				// promote the array reference to the suitable operation type
				codeStream.generateImplicitConversion(implicitConversion);
				// generate the increment value (will by itself  be promoted to the operation value)
				if (expression == IntLiteral.One) { // prefix operation
					codeStream.generateConstant(expression.constant, implicitConversion);
				} else {
					expression.generateCode(currentScope, codeStream, true);
				}
				// perform the operation
				codeStream.sendOperator(operator, operationTypeID);
				// cast the value back to the array reference type
				codeStream.generateImplicitConversion(assignmentImplicitConversion);
		}
		codeStream.arrayAtPut(this.resolvedType.id, valueRequired);
	}

	public void generatePostIncrement(
		BlockScope currentScope,
		CodeStream codeStream,
		CompoundAssignment postIncrement,
		boolean valueRequired) {

		receiver.generateCode(currentScope, codeStream, true);
		if (receiver instanceof CastExpression	// ((type[])null)[0]
				&& ((CastExpression)receiver).innermostCastedExpression().resolvedType == TypeBinding.NULL){
			codeStream.checkcast(receiver.resolvedType);
		}
		position.generateCode(currentScope, codeStream, true);
		codeStream.dup2();
		codeStream.arrayAt(this.resolvedType.id);
		if (valueRequired) {
			if ((this.resolvedType == TypeBinding.LONG)
				|| (this.resolvedType == TypeBinding.DOUBLE)) {
				codeStream.dup2_x2();
			} else {
				codeStream.dup_x2();
			}
		}
		codeStream.generateImplicitConversion(implicitConversion);
		codeStream.generateConstant(
			postIncrement.expression.constant,
			implicitConversion);
		codeStream.sendOperator(postIncrement.operator, this.implicitConversion & COMPILE_TYPE_MASK);
		codeStream.generateImplicitConversion(
			postIncrement.preAssignImplicitConversion);
		codeStream.arrayAtPut(this.resolvedType.id, false);
	}

public int nullStatus(FlowInfo flowInfo) {
	return FlowInfo.UNKNOWN;
}

	public StringBuffer printExpression(int indent, StringBuffer output) {

		receiver.printExpression(0, output).append('[');
		return position.printExpression(0, output).append(']');
	}

	public TypeBinding resolveType(BlockScope scope) {

		constant = Constant.NotAConstant;
//		if (receiver instanceof CastExpression	// no cast check for ((type[])null)[0]
//				&& ((CastExpression)receiver).innermostCastedExpression() instanceof NullLiteral) {
//			this.receiver.bits |= DisableUnnecessaryCastCheck; // will check later on
//		}
		TypeBinding arrayType = receiver.resolveType(scope);
		if (arrayType != null) {
			receiver.computeConversion(scope, arrayType, arrayType);
			if (arrayType.isArrayType()) {
				TypeBinding elementType = ((ArrayBinding) arrayType).elementsType();
				this.resolvedType = ((this.bits & IsStrictlyAssigned) == 0) ? elementType.capture(scope, this.sourceEnd) : elementType;
			} else {
//				scope.problemReporter().referenceMustBeArrayTypeAt(arrayType, this);
				this.resolvedType=TypeBinding.UNKNOWN;
			}
		}
//		TypeBinding positionType = position.resolveTypeExpecting(scope, new TypeBinding[] {scope.getJavaLangNumber(),scope.getJavaLangString()});
//		if (positionType != null) {
//			position.computeConversion(scope, TypeBinding.INT, positionType);
//		}
		return this.resolvedType;
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			receiver.traverse(visitor, scope);
			position.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}
