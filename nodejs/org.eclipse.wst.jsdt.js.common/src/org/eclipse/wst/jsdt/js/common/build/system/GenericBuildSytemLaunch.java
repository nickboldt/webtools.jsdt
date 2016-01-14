package org.eclipse.wst.jsdt.js.common.build.system;

import org.eclipse.core.resources.IFile;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.wst.jsdt.js.cli.core.CLICommand;

public abstract class GenericBuildSytemLaunch implements ILaunchShortcut {
	
	protected abstract void launchGrunt(IFile resource, CLICommand command);
	protected abstract CLICommand generateCLICommand(String commandName);

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
	


}
