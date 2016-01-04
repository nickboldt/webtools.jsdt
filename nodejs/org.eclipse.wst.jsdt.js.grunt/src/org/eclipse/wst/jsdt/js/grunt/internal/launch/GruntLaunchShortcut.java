package org.eclipse.wst.jsdt.js.grunt.internal.launch;

import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;

public class GruntLaunchShortcut implements ILaunchShortcut{

	@Override
	public void launch(ISelection selection, String arg1) {
		System.out.println("Running grunt task...");

		if (selection instanceof IStructuredSelection) {
			 Object element = ((IStructuredSelection)selection).getFirstElement();
			 System.out.println("Selection: " + element.toString());
		}
	}

	@Override
	public void launch(IEditorPart arg0, String arg1) {
		
	}

}
