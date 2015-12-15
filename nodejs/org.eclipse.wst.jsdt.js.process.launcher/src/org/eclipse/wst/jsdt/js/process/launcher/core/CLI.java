package org.eclipse.wst.jsdt.js.process.launcher.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.wst.jsdt.js.process.launcher.CLIPlugin;
import org.eclipse.wst.jsdt.js.process.launcher.internal.util.ExternalProcessUtility;

/**
 * Wrapper around CLI. Provides low level 
 * access to CLI.
 *
 *@author Gorkem Ercan
 *
 */
public class CLI {
	
	//Store locks for the projects.
	private static Map<String, Lock> projectLock = Collections.synchronizedMap(new HashMap<String,Lock>());
	protected String launchName;
	protected IProject project;
	protected String workingDir;

		
	public CLI(String launchName, IProject project, String workingDir) {
		if (project == null) {
			throw new IllegalArgumentException("No project specified");
		}
		this.launchName = launchName;
		this.project = project;
		this.workingDir = workingDir;
	}
	
//	public CLIResult build (final IProgressMonitor monitor, final String...options )throws CoreException{
//		final CLIStreamListener streamListener = new CLIStreamListener();
//		IProcess process = startShell(streamListener, monitor, getLaunchConfiguration("cordova build"));
//		String cordovaCommand = generateCordovaCommand(null, null, options);
//		sendCordovaCommand(process, cordovaCommand, monitor);
//		CLIResult result = new CLIResult(streamListener.getErrorMessage(), streamListener.getMessage());
//		throwExceptionIfError(result);
//		return result;
//	}
	
	protected void sendCLICommand(final IProcess process, final String cordovaCommand,
			final IProgressMonitor monitor) throws CoreException {
		Lock lock = projectLock();
		lock.lock();
		try {
			DebugPlugin.getDefault().addDebugEventListener(processTerminateListener);
			final IStreamsProxy streamProxy = process.getStreamsProxy();
			streamProxy.write(cordovaCommand.toString());
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, CLIPlugin.PLUGIN_ID, "Fatal error invoking CLI", e));
		} finally {
			lock.unlock();
		}
	}
	
//	private String generateCordovaCommand(final String command, final Command subCommand, final String... options) {
//		StringBuilder builder = new StringBuilder();
//		builder.append("cordova ");
//		builder.append(command);
//		if(subCommand != null){
//			builder.append(" ");
//			builder.append(subCommand.getCliCommand());
//		}
//		for (String string : options) {
//			if(!string.isEmpty()){
//				builder.append(" ");
//				builder.append(string);
//			}
//		}
//		builder.append("\n");
//		return builder.toString();
//	}
	
	//public visibility to support testing
	public IProcess startShell(final IStreamListener listener, final IProgressMonitor monitor, 
			final ILaunchConfiguration launchConfiguration) throws CoreException{
		ArrayList<String> commandList = new ArrayList<String>();
		if(isWindows()){
			commandList.add("cmd");
		}else{
			commandList.add("/bin/bash");
			commandList.add("-l");
		}
		ExternalProcessUtility ep = new ExternalProcessUtility();
		IProcess process = ep.exec(commandList.toArray(new String[commandList.size()]), new File(workingDir), 
				monitor, null, launchConfiguration);
		 if(listener != null){
			 process.getStreamsProxy().getOutputStreamMonitor().addListener(listener);
			 process.getStreamsProxy().getErrorStreamMonitor().addListener(listener);
		 }
		 return process;
	}
	
	private boolean isWindows(){
		String OS = System.getProperty("os.name","unknown");
		return OS.toLowerCase().indexOf("win")>-1;
	}
	

	
//	private ILaunchConfiguration getLaunchConfiguration(String label){
//		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
//		ILaunchConfigurationType type = manager.getLaunchConfigurationType(IExternalToolConstants.ID_PROGRAM_LAUNCH_CONFIGURATION_TYPE);
//		try {
//			ILaunchConfiguration cfg = type.newInstance(null, "cordova");
//			ILaunchConfigurationWorkingCopy wc = cfg.getWorkingCopy();
//			wc.setAttribute(IProcess.ATTR_PROCESS_LABEL, label);
//			cfg = wc.doSave();
//			return cfg;
//		} catch (CoreException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
	
	private Lock projectLock(){
		final String projectName = project.getProject().getName();
		Lock l = projectLock.get(project.getProject().getName());
		if(l == null){
			// Use reentrant locks
			l = new ReentrantLock();
			projectLock.put(projectName, l);
		}
		return l;
	}
	
//	private File getWorkingDirectory(){
//		final IPath wp = project.getProject().getLocation();
//		if(wp == null){
//			return null;
//		}
//		return wp.toFile();
//	}
	
	protected void throwExceptionIfError(CLIResult result) throws CoreException {
		if(result.hasError()){
			throw result.asCoreException();
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
							// TODO: need to write smarter conditions
							if (lc != null /* && launchName.equals(lc.getName()) */ && project != null && project.exists()) {
								try {
									project.refreshLocal(IResource.DEPTH_INFINITE, null);
								} catch (CoreException e) {
									CLIPlugin.logError(e);
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

}
