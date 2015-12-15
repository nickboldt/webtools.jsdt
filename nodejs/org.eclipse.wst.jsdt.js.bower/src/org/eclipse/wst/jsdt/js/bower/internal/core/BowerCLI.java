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
import org.eclipse.wst.jsdt.js.bower.BowerPlugin;
import org.eclipse.wst.jsdt.js.bower.internal.BowerConstants;
import org.eclipse.wst.jsdt.js.process.launcher.core.CLI;
import org.eclipse.wst.jsdt.js.process.launcher.core.CLIResult;
import org.eclipse.wst.jsdt.js.process.launcher.core.CLIStreamListener;

@SuppressWarnings("restriction")
public class BowerCLI extends CLI {
	private String command;

	public BowerCLI(String command, String launchName, IProject project, String workingDir) {
		super(launchName, project, workingDir);
		this.command = command;
	}
	
	public CLIResult execute() throws CoreException {
		final CLIStreamListener streamListener = new CLIStreamListener();
		IProcess process = startShell(streamListener, null, generateLaunchConfiguration(launchName, workingDir));
		String bowerCommand = generateBowerCommand(command, null, (String[]) null);
		sendCLICommand(process, bowerCommand, null);
		CLIResult result = new CLIResult(streamListener.getErrorMessage(), streamListener.getMessage());
		throwExceptionIfError(result);
		return result;
	}
	
	public String generateBowerCommand(final String command, final String subCommand, final String... options) {
		StringBuilder builder = new StringBuilder();
		builder.append(BowerConstants.BOWER);
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
		builder.append("exit\n");
		return builder.toString();
	}
	
	private ILaunchConfiguration generateLaunchConfiguration(String label, String workingDir) {	
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
