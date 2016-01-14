package org.eclipse.wst.jsdt.js.gulp.internal;

import org.eclipse.core.resources.IFile;

public interface Task {
	
	String getName();
	boolean isDefault();
	IFile getBuildFile();
	
}
