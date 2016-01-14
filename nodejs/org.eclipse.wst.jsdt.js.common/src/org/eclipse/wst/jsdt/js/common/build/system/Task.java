package org.eclipse.wst.jsdt.js.common.build.system;

import org.eclipse.core.resources.IFile;

public interface Task {
	
	String getName();
	boolean isDefault();
	IFile getBuildFile();
	
}
