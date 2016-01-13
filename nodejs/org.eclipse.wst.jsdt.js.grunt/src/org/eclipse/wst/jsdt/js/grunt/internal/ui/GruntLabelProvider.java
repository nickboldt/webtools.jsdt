package org.eclipse.wst.jsdt.js.grunt.internal.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.wst.jsdt.js.grunt.GruntPlugin;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;

public class GruntLabelProvider extends LabelProvider implements IStyledLabelProvider, IColorProvider{
	
//	@Override
//	public StyledString getStyledText(Object element) {
//		if (element instanceof IJSBuildFile) {
//			IJSBuildFile node = (IJSBuildFile) element;
//			StyledString buff = new StyledString(node.getLabel());
//			IFile buildfile = node.getBuildFileResource();
//			if (buildfile != null) {
//				buff.append("  "); //$NON-NLS-1$
//				buff.append('[', StyledString.DECORATIONS_STYLER);
//				buff.append(buildfile.getFullPath().makeRelative().toString(),
//						StyledString.DECORATIONS_STYLER);
//				buff.append(']', StyledString.DECORATIONS_STYLER);
//			}
//			return buff;
//		} else if (element instanceof ITask) {
//			return new StyledString(((ITask) element).getLabel());
//		}
//		return null;
//	}
//
//	@Override
//	public Color getForeground(Object node) {
//		if (node instanceof ITask && ((ITask) node).isDefault()) {
//			return Display.getDefault().getSystemColor(SWT.COLOR_BLUE);
//		}
//		return Display.getDefault().getSystemColor(SWT.COLOR_LIST_FOREGROUND);
//	}
//
//	@Override
//	public Color getBackground(Object element) {
//		return Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
//	}

	@Override
	public Image getImage(Object element) {
		return GruntPlugin.getImageDescriptor("/icons/gruntfile.png").createImage(); //$NON-NLS-1$
	}

	@Override
	public Color getBackground(Object arg0) {
		return Display.getDefault().getSystemColor(SWT.COLOR_LIST_BACKGROUND);
	}

	@Override
	public Color getForeground(Object arg0) {
		return Display.getDefault().getSystemColor(SWT.COLOR_LIST_FOREGROUND);
	}

	@Override
	public StyledString getStyledText(Object object) {
		return new StyledString(object.toString());
	}

}
