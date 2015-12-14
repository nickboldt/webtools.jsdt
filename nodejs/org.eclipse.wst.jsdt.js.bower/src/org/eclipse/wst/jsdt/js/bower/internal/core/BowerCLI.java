package org.eclipse.wst.jsdt.js.bower.internal.core;

import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.ui.internal.ide.actions.OpenWorkspaceAction;
import org.eclipse.wst.jsdt.js.bower.BowerPlugin;
import org.eclipse.wst.jsdt.js.bower.internal.BowerConstants;
import org.eclipse.wst.jsdt.js.process.launcher.core.CLI;
import org.eclipse.wst.jsdt.js.process.launcher.core.CLIResult;
import org.eclipse.wst.jsdt.js.process.launcher.core.CLIStreamListener;

@SuppressWarnings("restriction")
public class BowerCLI extends CLI {

	public BowerCLI(IProject project, String workingDir) {
		super(project, workingDir);
	}
	
	public CLIResult execute() throws CoreException {
		final CLIStreamListener streamListener = new CLIStreamListener();
		IProcess process = startShell(streamListener, null, generateLaunchConfiguration("bower install", workingDir));
		String cordovaCommand = generateBowerCommand("install", null, null);
		sendCordovaCommand(process, cordovaCommand, null);
		CLIResult result = new CLIResult(streamListener.getErrorMessage(), streamListener.getMessage());
		throwExceptionIfError(result);
		return result;
	}
	
	public String generateBowerCommand(final String command, final String subCommand, final String... options) {
		StringBuilder builder = new StringBuilder();
		builder.append("npm");
		builder.append(" ");
		builder.append(command);
		if (subCommand != null) {
			builder.append(" ");
			builder.append(subCommand);
		}
		if (options != null) {
			for (String string : options) {
				if (!string.isEmpty()) {
					builder.append(" ");
					builder.append(string);
				}
			}
		}
		builder.append("\n");
		return builder.toString();
	}
	
	private ILaunchConfiguration generateLaunchConfiguration(String label, String workingDir){
		
//		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
//		DebugPlugin.getDefault().addDebugEventListener(processTerminateListener);
//		ILaunchConfigurationType programType = manager.getLaunchConfigurationType(IExternalToolConstants.ID_PROGRAM_LAUNCH_CONFIGURATION_TYPE);
//		try {
//			ILaunchConfiguration cfg = programType.newInstance(null, getLaunchName());
//			ILaunchConfigurationWorkingCopy wc = cfg.getWorkingCopy();
//			wc.setAttribute(IExternalToolConstants.ATTR_LOCATION, nodeExecutableLocation);
//			wc.setAttribute(IExternalToolConstants.ATTR_WORKING_DIRECTORY, "${workspace_loc:" + workingDirectory + "}"); //$NON-NLS-1$ //$NON-NLS-2$
//			wc.setAttribute(IExternalToolConstants.ATTR_SHOW_CONSOLE, true);
//			
//			// The argument passed to Node are: 1) executable location 2) command name i.e 1) bower 2) update 
//			wc.setAttribute(IExternalToolConstants.ATTR_TOOL_ARGUMENTS, "\"" + toolExecutableLocation +  "\" " + getCommandName()); //$NON-NLS-1$ //$NON-NLS-2$
//			cfg = wc.doSave();
//			cfg.launch(ILaunchManager.RUN_MODE, null, false, true);
//			cfg.delete();
//			WorkbenchResourceUtil.showConsoleView();
//		} catch (CoreException e) {
//			NodePlugin.logError(e);
//			NodeExceptionNotifier.launchError(e);
//		}
		
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(IExternalToolConstants.ID_PROGRAM_LAUNCH_CONFIGURATION_TYPE);
		try {
			ILaunchConfiguration cfg = type.newInstance(null, BowerConstants.BOWER);
			ILaunchConfigurationWorkingCopy wc = cfg.getWorkingCopy();
			wc.setAttribute(IProcess.ATTR_PROCESS_LABEL, label);
			return cfg;
		} catch (CoreException e) {
			BowerPlugin.logError(e);
		}
		return null;
	}
	
}
