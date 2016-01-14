package org.eclipse.wst.jsdt.js.grunt.internal.launch;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wst.jsdt.js.cli.core.CLI;
import org.eclipse.wst.jsdt.js.cli.core.CLICommand;
import org.eclipse.wst.jsdt.js.common.build.system.GenericBuildSytemLaunch;
import org.eclipse.wst.jsdt.js.grunt.GruntPlugin;
import org.eclipse.wst.jsdt.js.grunt.internal.GruntConstants;
import org.eclipse.wst.jsdt.js.grunt.internal.Messages;

public class GenericGruntLaunch extends GenericBuildSytemLaunch{

	@Override
	protected void launchGrunt(IFile resource, CLICommand command) {
		try {
			 new CLI(resource.getProject(), resource.getParent().getLocation()).execute(command, null);
		} catch (CoreException e) {
			GruntPlugin.logError(e);
			ErrorDialog.openError(Display.getDefault().getActiveShell(), Messages.GruntLaunchError_Title,
					Messages.GruntLaunchError_Message, e.getStatus());
		}
	}
	
	@Override
	protected CLICommand generateCLICommand(String commandName) {
		return new CLICommand(GruntConstants.GRUNT, commandName, null, null);
	}

}
