/*******************************************************************************
 * Copyright (c) 2015 Red Hat, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * 	Contributors:
 * 		 Red Hat Inc. - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.wst.jsdt.js.node.launch.shortcut;

import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.wst.jsdt.js.node.NodePlugin;
import org.eclipse.wst.jsdt.js.node.exception.NodeExceptionNotifier;
import org.eclipse.wst.jsdt.js.node.util.WorkbenchResourceUtil;

/**
 * Generic {@link org.eclipse.debug.ui.ILaunchShortcut} which falls back on <strong>native</strong> Node.js implementation
 *     
 * @author "Ilya Buziuk (ibuziuk)"
 */
@SuppressWarnings("restriction")
public abstract class GenericNativeNodeLaunch implements ILaunchShortcut {
	private IProject workingProject;
	
	public IProject getWorkingProject() {
		return workingProject;
	}

	public void setWorkingProject(IProject workingProject) {
		this.workingProject = workingProject;
	}
	
	@Override
	public void launch(ISelection selection, String mode) {
	}

	@Override
	public void launch(IEditorPart editor, String mode) {			
	}
	
	/**
	 * Launches an external Node.js tool (i.e bower, npm, grunt, gulp)
	 *
	 * @param  workingDirectory a directory from which external Node.js process will be launched 
	 * @param  nodeExecutableLocation the location of the Node.js executable
	 * @param  toolExecutableLocation the location of a specific Node.js tool (i.e bower, npm)
	 */
	protected void launchNodeTool(String workingDirectory, String nodeExecutableLocation, String toolExecutableLocation) {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		DebugPlugin.getDefault().addDebugEventListener(processTerminateListener);
		ILaunchConfigurationType programType = manager.getLaunchConfigurationType(IExternalToolConstants.ID_PROGRAM_LAUNCH_CONFIGURATION_TYPE);
		try {
			ILaunchConfiguration cfg = programType.newInstance(null, getLaunchName());
			ILaunchConfigurationWorkingCopy wc = cfg.getWorkingCopy();
			wc.setAttribute(IExternalToolConstants.ATTR_LOCATION, nodeExecutableLocation);
			wc.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, "${workspace_loc:" + workingDirectory + "}"); //$NON-NLS-1$ //$NON-NLS-2$
			wc.setAttribute(IExternalToolConstants.ATTR_SHOW_CONSOLE, true);
			
			// The argument passed to Node are: 1) executable location 2) command name i.e 1) bower 2) update 
			wc.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, "\"" + toolExecutableLocation +  "\" " + getCommandName()); //$NON-NLS-1$ //$NON-NLS-2$
			cfg = wc.doSave();
			cfg.launch(ILaunchManager.RUN_MODE, null, false, true);
			cfg.delete();
			WorkbenchResourceUtil.showConsoleView();
		} catch (CoreException e) {
			NodePlugin.logError(e);
			NodeExceptionNotifier.launchError(e);
		}
	}
	
	IDebugEventSetListener processTerminateListener = new IDebugEventSetListener() {

		@Override
		public void handleDebugEvents(DebugEvent[] events) {
			for (DebugEvent event : events) {
				if (event.getKind() == DebugEvent.TERMINATE) {
					Object source = event.getSource();
					if (source instanceof IProcess) {
						ILaunch launch = ((IProcess) source).getLaunch();
						if (launch != null) {
							ILaunchConfiguration lc = launch.getLaunchConfiguration();
							if (lc != null && getLaunchName().equals(lc.getName()) && getWorkingProject() != null
									&& getWorkingProject().exists()) {
								try {
									getWorkingProject().refreshLocal(IResource.DEPTH_INFINITE, null);
								} catch (CoreException e) {
									NodePlugin.logError(e);
								} finally {
									DebugPlugin.getDefault().removeDebugEventListener(this);
								}
							}
						}
					}

				}
			}
		}
	};

	protected abstract String getWorkingDirectory(IResource resource) throws CoreException;

	protected abstract String getCommandName();

	protected abstract String getLaunchName();

}
