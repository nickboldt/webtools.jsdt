package org.eclipse.wst.jsdt.js.grunt.internal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.wst.jsdt.core.dom.ASTVisitor;
import org.eclipse.wst.jsdt.core.dom.Expression;
import org.eclipse.wst.jsdt.core.dom.FunctionInvocation;
import org.eclipse.wst.jsdt.core.dom.SimpleName;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GruntVisitor extends ASTVisitor {
	private List<String> tasks;
	private static final String GRUNT = "grunt"; //$NON-NLS-1$
	private static final String REGISTER_TASK = "registerTask"; //$NON-NLS-1$
	private static final String INIT_CONFIG = "initConfig"; //$NON-NLS-1$
	
	private static final String GULP = "gulp"; //$NON-NLS-1$
	private static final String TASK= "task"; //$NON-NLS-1$
	
	public GruntVisitor() {
		super();
		this.tasks = new ArrayList<String>();
	}
	
	@SuppressWarnings("unchecked")
	public boolean visit(FunctionInvocation node) {
		SimpleName functionName = node.getName();
		Expression expression = node.getExpression();
		List<Expression> arguments = node.arguments();
		
		if (functionName != null && expression != null && arguments != null) {
		// Test for grunt
		if (REGISTER_TASK.equals(functionName.toString()) && GRUNT.equals(expression.toString())) { 
			if (arguments.size() > 0) {
				tasks.add(arguments.get(0).toString().replaceAll("'", ""));  //$NON-NLS-1$//$NON-NLS-2$
			}
		} else if (INIT_CONFIG.equals(functionName.toString())) {
			for (Expression a : arguments) {
				JsonParser parser = new JsonParser();
				JsonElement element = parser.parse(a.toString());
				System.out.println(a.properties());
				JsonObject asJsonObject = element.getAsJsonObject();
				Set<Entry<String, JsonElement>> entrySet = asJsonObject.entrySet();
				for (Entry<String, JsonElement> entry: entrySet) {
					tasks.add(entry.getKey());
				}
			}
		
		// Test for gulp
		} else if (TASK.equals(functionName.toString()) && GULP.equals(expression.toString())) {
			if (arguments.size() > 0) {
				tasks.add(arguments.get(0).toString().replaceAll("'", ""));  //$NON-NLS-1$//$NON-NLS-2$
			}
		}
		}
		
		return true;
	}
	

	public List<String> getTasks() {
		return this.tasks;
	}
	
//	public boolean visit(ExpressionStatement node) {
//		System.out.println(node.getExpression());
//		return true;
//	}
//	
//	public boolean visit(Block node) {
//		System.out.println(node.getParent());
//		return true;
//	}
//	
//	public boolean visit(BooleanLiteral node) {
//		return true;
//	}
//	
//	public boolean visit(Assignment node) {
//		System.out.println(node.getLeftHandSide());
//		System.out.println(node.getRightHandSide());
//		return true;
//	}
//	
//	public boolean visit(InferredType node) {
//		System.out.println(node);
//		return true;
//	}
//	
//	public boolean visit(SingleVariableDeclaration node) {
//		System.out.println(node.getName());
//		System.out.println(node.getBodyChild());
//		System.out.println(node.properties());
//		return true;
//	}
//	
//	public boolean visit(SimpleName node) {
//		System.out.println(node.getParent());
//		System.out.println(node.getFullyQualifiedName());
//		return true;
//	}
	
//	System.out.println(node.getName());  // registerTask // task 
//	System.out.println(node.getExpression());
//	System.out.println(node.getParent());
//	List<Expression> arguments = node.arguments();
//	for (Expression e : arguments) {
//		if (INIT_CONFIG.equals(node.getName().toString())) {
//		JsonParser parser = new JsonParser();
//		JsonElement element = parser.parse(e.toString());
//		System.out.println(e.properties());
//		JsonObject asJsonObject = element.getAsJsonObject();
//		Set<Entry<String, JsonElement>> entrySet = asJsonObject.entrySet();
//		for (Entry<String, JsonElement> en: entrySet) {
//			System.out.println(en.getKey());
//		}
//		}
//		System.out.println(e.toString());
//	}
//	
//	return true;
	
	
//	@Override
//	public boolean visit(JavaScriptUnit unit) {
//		List statements = unit.statements();
//		System.out.println(statements.size());
//		return true;
//	}
	
//	public boolean visit(FunctionDeclaration node) {
//		System.out.println(node.getName());
//		List<SingleVariableDeclaration> parameters = node.parameters();
//		for (SingleVariableDeclaration p : parameters) {
//			System.out.println(p.getName());
//		}
//		return true;
//	}
	
//	public boolean visit(FunctionRef node) {
//		System.out.println(node.getName());
//		return true;
//	}
}
