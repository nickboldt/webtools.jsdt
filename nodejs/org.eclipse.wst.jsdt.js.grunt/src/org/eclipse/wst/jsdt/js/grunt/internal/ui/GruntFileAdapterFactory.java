package org.eclipse.wst.jsdt.js.grunt.internal.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.wst.jsdt.core.dom.JavaScriptUnit;

public class GruntFileAdapterFactory implements IAdapterFactory {

	private static Class[] PROPERTIES = new Class[] { IResource.class };

	@Override
	public Object getAdapter(Object element, Class key) {
		JavaScriptUnit node = getJSBuildFileNode(element);
		if (node == null) {
			return null;
		}
		if (IResource.class.equals(key)) {
			return getResource(node);
		}
		return null;
	}

	private Object getResource(JavaScriptUnit node) {
		return node.getJavaElement();
	}

	private JavaScriptUnit getJSBuildFileNode(Object element) {
		if (element instanceof JavaScriptUnit) {
			return (JavaScriptUnit) element;
		}
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return PROPERTIES;
	}

}