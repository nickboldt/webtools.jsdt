package org.eclipse.wst.jsdt.js.common.build.system;

import org.eclipse.core.resources.IFile;

public class BuildTask implements Task {
	private String name;
	private boolean isDefault;
	private IFile buildFile;
	
	public BuildTask(String name, IFile buildFile, boolean isDefault) {
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
