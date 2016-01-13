package org.eclipse.wst.jsdt.js.grunt.internal.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.wst.jsdt.core.JavaScriptModelException;
import org.eclipse.wst.jsdt.core.dom.JavaScriptUnit;
import org.eclipse.wst.jsdt.js.grunt.internal.GruntTask;
import org.eclipse.wst.jsdt.js.grunt.internal.Task;
import org.eclipse.wst.jsdt.js.grunt.internal.util.ASTUtil;
import org.eclipse.wst.jsdt.js.grunt.internal.util.GruntVisitor;

public class GruntFileContentProvider implements ITreeContentProvider, IResourceChangeListener {
	
	private Viewer viewer;

	private IResource resource;

	protected static final Object[] EMPTY_ARRAY = new Object[0];

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
	    if (resource != null) {
	        resource.getWorkspace().removeResourceChangeListener(this);
	        resource = null;
	    }
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
		   if (resource != null) {
		        resource.getWorkspace().removeResourceChangeListener(this);
		    }

		    resource = (IResource) newInput;

		    if (resource != null) {
		        resource.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
		    }

		    this.viewer =  viewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(Object)
	 */
	@Override
	public Object[] getChildren(Object parentNode) {
		Object[] children = null;
		ArrayList<Task> tasks = new ArrayList<>();
		if (parentNode instanceof IResource) {
			if (parentNode instanceof IFile) {
				try {
					JavaScriptUnit unit = ASTUtil.getJavaScriptUnit((IFile)parentNode);
					GruntVisitor visitor = new GruntVisitor();
					unit.accept(visitor);
					children = visitor.getTasks().toArray();
					for (Object o : children) {
						
						tasks.add(new GruntTask(o.toString(), (IFile) parentNode, false));
					}
				} catch (JavaScriptModelException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return tasks.toArray();
//			return new String[]{"Some Grunt Task"};
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
		if (element instanceof String) {
			return false;
		}
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
	public void resourceChanged(IResourceChangeEvent arg0) {
		viewer.refresh();
	}
}
