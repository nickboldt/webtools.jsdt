package org.eclipse.wst.jsdt.js.gulp.internal;

import org.eclipse.core.resources.IFile;

public class GruntTask implements Task {
	private String name;
	private boolean isDefault;
	private IFile buildFile;
	
	public GruntTask(String name, IFile buildFile, boolean isDefault) {
		this.name = name;
		this.buildFile = buildFile;
		this.isDefault = isDefault;
	}
	
	@Override
	public String getName() {
		return name;
	}	

	@Override
	public boolean isDefault() {
		return isDefault;
	}

	@Override
	public IFile getBuildFile() {
		return buildFile;
	}

}
