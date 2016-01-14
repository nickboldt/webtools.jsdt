package org.eclipse.wst.jsdt.js.gulp.internal.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.wst.jsdt.core.IClassFile;
import org.eclipse.wst.jsdt.core.IJavaScriptUnit;
import org.eclipse.wst.jsdt.core.JavaScriptCore;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;
import org.eclipse.wst.jsdt.core.dom.AST;
import org.eclipse.wst.jsdt.core.dom.ASTParser;
import org.eclipse.wst.jsdt.core.dom.JavaScriptUnit;
import org.eclipse.wst.jsdt.internal.core.JavaProject;

public class ASTUtil {
	
	public static JavaScriptUnit getJavaScriptUnit(IFile file) throws JavaScriptModelException {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(getCompilationUnit(file));
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		return (JavaScriptUnit) parser.createAST(null);
	}
	
	private static IJavaScriptUnit getCompilationUnit(IFile file) {
		return (IJavaScriptUnit) JavaScriptCore.create(file);
	}
	
//	protected IFile getFile(String path) {
//		return getWorkspaceRoot().getFile(new Path(path));
//	}

}
