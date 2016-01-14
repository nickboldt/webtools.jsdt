package org.eclipse.wst.jsdt.js.gulp.internal.launch;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;

import org.eclipse.wst.jsdt.js.cli.core.CLI;
import org.eclipse.wst.jsdt.js.cli.core.CLICommand;
import org.eclipse.wst.jsdt.js.gulp.GulpPlugin;
import org.eclipse.wst.jsdt.js.gulp.internal.GulpConstants;
import org.eclipse.wst.jsdt.js.gulp.internal.Messages;
import org.eclipse.wst.jsdt.js.gulp.internal.Task;

public class GenericGruntLaunch implements ILaunchShortcut{

	@Override
	public void launch(ISelection selection, String arg1) {
		if (selection instanceof IStructuredSelection) {
			 Object element = ((IStructuredSelection)selection).getFirstElement();
			 element.toString();
			 if (element != null && element instanceof Task) {
				 Task selectedResource = (Task) element;
				 launchGrunt(selectedResource.getBuildFile(), generateCLICommand(selectedResource.getName()));
			 }
		}
	}

	@Override
	public void launch(IEditorPart arg0, String arg1) {
		
	}
	
	private void launchGrunt(IFile resource, CLICommand command) {
		try {
			 new CLI(resource.getProject(), resource.getParent().getLocation()).execute(command, null);
		} catch (CoreException e) {
			GulpPlugin.logError(e);
			ErrorDialog.openError(Display.getDefault().getActiveShell(), Messages.GruntLaunchError_Title,
					Messages.GruntLaunchError_Message, e.getStatus());
		}
	}
	
	private CLICommand generateCLICommand(String commandName) {
		return new CLICommand(/*GruntConstants.GRUNT*/ "gulp", commandName, null, null);
	}

}
