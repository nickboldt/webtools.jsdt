package org.eclipse.wst.jsdt.js.grunt.internal.ui;

import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.IPipelinedTreeContentProvider;
import org.eclipse.ui.navigator.PipelinedShapeModification;
import org.eclipse.ui.navigator.PipelinedViewerUpdate;
import org.eclipse.wst.jsdt.core.dom.JavaScriptUnit;

public class GruntFileContentProvider
		implements ITreeContentProvider, IPipelinedTreeContentProvider, IResourceChangeListener, IResourceDeltaVisitor {

	protected static final Object[] EMPTY_ARRAY = new Object[0];

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface
	 * .viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// do nothing
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(Object)
	 */
	@Override
	public Object[] getChildren(Object parentNode) {
		if (parentNode instanceof IResource) {
			return new String[]{"Some Grunt Task"};
		}
		
		JavaScriptUnit unit = null; 
/*		if (parentNode instanceof IResource) {
			return new String[]{"Hello"};
		} else */if (parentNode instanceof JavaScriptUnit) {
			unit = (JavaScriptUnit) parentNode;
			return unit.getMessages();
		}
		
		// IJSBuildFileNode buildFileNode = null;
		// if (parentNode instanceof IResource) {
		// buildFileNode = JSBuildFileFactoryManager
		// .tryToCreate((IResource) parentNode);
		// } else if (parentNode instanceof IJSBuildFileNode) {
		// buildFileNode = (IJSBuildFileNode) parentNode;
		// }
		// return JSBuildFileUtil.getTasks(buildFileNode).toArray();
		return new String[]{};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(Object)
	 */
	@Override
	public Object getParent(Object element) {
		// if (element instanceof IJSBuildFileNode) {
		// return ((IJSBuildFileNode) element).getParentNode();
		// }
		// return null;
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(Object)
	 */
	@Override
	public boolean hasChildren(Object element) {
		// if (element instanceof IJSBuildFileNode) {
		// return ((IJSBuildFileNode) element).hasChildren();
		// }
		// return true;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IStructuredContentProvider#getElements(Object)
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof List) {
			return ((List<?>) inputElement).toArray();
		}
		if (inputElement instanceof Object[]) {
			return (Object[]) inputElement;
		}
		return EMPTY_ARRAY;
	}

	@Override
	public boolean visit(IResourceDelta arg0) throws CoreException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void resourceChanged(IResourceChangeEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(ICommonContentExtensionSite arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void restoreState(IMemento arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveState(IMemento arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getPipelinedChildren(Object arg0, Set arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void getPipelinedElements(Object arg0, Set arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getPipelinedParent(Object arg0, Object arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PipelinedShapeModification interceptAdd(PipelinedShapeModification arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean interceptRefresh(PipelinedViewerUpdate arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public PipelinedShapeModification interceptRemove(PipelinedShapeModification arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean interceptUpdate(PipelinedViewerUpdate arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}
