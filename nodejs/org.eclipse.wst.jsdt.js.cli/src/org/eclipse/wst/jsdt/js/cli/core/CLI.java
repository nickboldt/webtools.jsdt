package org.eclipse.wst.jsdt.js.cli.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.externaltools.internal.IExternalToolConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.wst.jsdt.js.cli.CLIPlugin;
import org.eclipse.wst.jsdt.js.cli.internal.util.ExternalProcessUtility;

/**
 * Wrapper around CLI. Provides low level 
 * access to CLI.
 *
 *@author Gorkem Ercan
 *
 */
@SuppressWarnings("restriction")
public class CLI {
	
	//Store locks for the projects.
	private static Map<String, Lock> projectLock = Collections.synchronizedMap(new HashMap<String,Lock>());
	private IProject project;
	private String workingDir;

		
	public CLI( IProject project, String workingDir) {
		if (project == null) {
			throw new IllegalArgumentException("No project specified"); //$NON-NLS-1$
		}
		
		
		if (workingDir == null) {
			// use the project location as the working directory
			this.workingDir = project.getRawLocation().makeAbsolute().toOSString();
		}
		this.project = project;
		this.workingDir = workingDir;
	}
	
//	public CLIResult build (final IProgressMonitor monitor, final String...options )throws CoreException{
//	final CLIStreamListener streamListener = new CLIStreamListener();
//	IProcess process = startShell(streamListener, monitor, getLaunchConfiguration("cordova build"));
//	String cordovaCommand = generateCordovaCommand(null, null, options);
//	sendCordovaCommand(process, cordovaCommand, monitor);
//	CLIResult result = new CLIResult(streamListener.getErrorMessage(), streamListener.getMessage());
//	throwExceptionIfError(result);
//	return result;
//}
	

	public CLIResult execute(CLICommand command, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		final CLIStreamListener streamListener = new CLIStreamListener();
		IProcess process = startShell(streamListener, monitor, generateLaunchConfiguration(command));
		sendCLICommand(process, command, monitor);
		CLIResult result = new CLIResult(streamListener.getErrorMessage(), streamListener.getMessage());
		throwExceptionIfError(result);
		return result;
	}
	
	private ILaunchConfiguration generateLaunchConfiguration(CLICommand command) {	
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(IExternalToolConstants.ID_PROGRAM_LAUNCH_CONFIGURATION_TYPE);
		try {
			ILaunchConfiguration cfg = type.newInstance(null, command.getToolName());
			ILaunchConfigurationWorkingCopy wc = cfg.getWorkingCopy();
			wc.setAttribute(IProcess.ATTR_PROCESS_LABEL, command.getToolName() + command.getCommand());
			return cfg;
		} catch (CoreException e) {
			CLIPlugin.logError(e);
		}
		return null;
	}
	
//	private ILaunchConfiguration getLaunchConfiguration(String label){
//	ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
//	ILaunchConfigurationType type = manager.getLaunchConfigurationType(IExternalToolConstants.ID_PROGRAM_LAUNCH_CONFIGURATION_TYPE);
//	try {
//		ILaunchConfiguration cfg = type.newInstance(null, "cordova");
//		ILaunchConfigurationWorkingCopy wc = cfg.getWorkingCopy();
//		wc.setAttribute(IProcess.ATTR_PROCESS_LABEL, label);
//		cfg = wc.doSave();
//		return cfg;
//	} catch (CoreException e) {
//		e.printStackTrace();
//	}
//	return null;
//}
	
	protected void sendCLICommand(final IProcess process, final CLICommand command,
			final IProgressMonitor monitor) throws CoreException {
		Lock lock = projectLock();
		lock.lock();
		try {
			
			DebugPlugin.getDefault().addDebugEventListener(processTerminateListener);
			final IStreamsProxy streamProxy = process.getStreamsProxy();
			streamProxy.write(command.toString());
			while (!process.isTerminated()) {
				//exit the shell after sending the command
				streamProxy.write("exit\n"); //$NON-NLS-1$
				if (monitor.isCanceled()) {
					process.terminate();
					break;
				}
				Thread.sleep(100);
			}
		} catch (IOException | InterruptedException e) {
			throw new CoreException(new Status(IStatus.ERROR, CLIPlugin.PLUGIN_ID, "Fatal error invoking CLI", e)); //$NON-NLS-1$
		} finally {
			lock.unlock();
		}
	}
	
//	public String generateCommand(final String toolName, final String command, final String subCommand, final String... options) {
//		StringBuilder builder = new StringBuilder();
//		builder.append(toolName);
//		builder.append(" ");
//		builder.append(command);
//		if (subCommand != null) {
//			builder.append(" ");
//			builder.append(subCommand);
//		}
//		if (options != null) {
//			for (String string : options) {
//				if (!string.isEmpty()) {
//					builder.append(" ");
//					builder.append(string);
//				}
//			}
//		}
//		builder.append("\n");
//		builder.append("exit\n");
//		return builder.toString();
//	}
	
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
			commandList.add("cmd"); //$NON-NLS-1$
		}else{
			commandList.add("/bin/bash"); //$NON-NLS-1$
			commandList.add("-l"); //$NON-NLS-1$
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
		String OS = System.getProperty("os.name","unknown");  //$NON-NLS-1$//$NON-NLS-2$
		return OS.toLowerCase().indexOf("win")>-1; //$NON-NLS-1$
	}
	
	
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
