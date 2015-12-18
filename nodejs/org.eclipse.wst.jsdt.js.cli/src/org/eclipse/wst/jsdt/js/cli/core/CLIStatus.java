package org.eclipse.wst.jsdt.js.cli.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Status;

public class CLIStatus extends Status {
	private IProject project;

	public CLIStatus(int severity, String pluginId, int code,
			String message, Throwable exception) {
		super(severity, pluginId, code, message, exception);
	}

	public IProject getProject() {
		return project;
	}
	
	public void setProject(IProject project) {
		this.project = project;
	}
}
