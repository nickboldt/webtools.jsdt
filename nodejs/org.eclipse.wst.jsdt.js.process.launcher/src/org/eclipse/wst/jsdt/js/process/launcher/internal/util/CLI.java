package org.eclipse.wst.jsdt.js.process.launcher.internal.util;

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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.wst.jsdt.js.process.launcher.ProcessLauncher;

/**
 * Wrapper around Cordova CLI. Provides low level 
 * access to Cordova CLI.
 *
 *@author Gorkem Ercan
 *
 */
@SuppressWarnings("restriction")
public class CLI {
//	public static final String OPTION_SAVE = "--save";
//	private static final String P_COMMAND_PLUGIN = "plugin";
//	private static final String P_COMMAND_PLATFORM = "platform";
//	private static final String P_COMMAND_PREPARE = "prepare";
//	private static final String P_COMMAND_BUILD = "build";
	
	public enum Command{
		ADD("add"), 
		REMOVE("remove");
		
		private final String cliCommand;
		Command(String cli){
			this.cliCommand = cli;
		}
		public String getCliCommand() {
			return cliCommand;
		}
	}

	//Store locks for the projects.
	private static Map<String, Lock> projectLock = Collections.synchronizedMap(new HashMap<String,Lock>());
	private IProject project;

	
	/**
	 * Initialize a CLI for a {@link HybridProject}.
	 * 
	 * @param project
	 * @return a cli wrapper
	 */
	public static CLI newCLIforProject(IProject project){
		if(project == null ){
			throw new IllegalArgumentException("No project specified");
		}
		return new CLI(project);
	}
	
	private CLI(IProject project){
		this.project = project;
	}
	
	public CLIResult build (final IProgressMonitor monitor, final String...options )throws CoreException{
		final CLIStreamListener streamListener = new CLIStreamListener();
		IProcess process = startShell(streamListener, monitor, getLaunchConfiguration("cordova build"));
		String cordovaCommand = generateCordovaCommand(null, null, options);
		sendCordovaCommand(process, cordovaCommand, monitor);
		CLIResult result = new CLIResult(streamListener.getErrorMessage(), streamListener.getMessage());
		throwExceptionIfError(result);
		return result;
	}
	
	public CLIResult prepare (final IProgressMonitor monitor, final String...options )throws CoreException{
		final CLIStreamListener streamListener = new CLIStreamListener();
		IProcess process = startShell(streamListener, monitor, getLaunchConfiguration("cordova prepare "));
		String cordovaCommand = generateCordovaCommand(null, null, options);
		sendCordovaCommand(process, cordovaCommand, monitor);
		CLIResult result =  new CLIResult(streamListener.getErrorMessage(), streamListener.getMessage());
		throwExceptionIfError(result);
		return result;
	}
	
	public CLIResult platform (final Command command, final IProgressMonitor monitor, final String... options ) throws CoreException{
		final CLIStreamListener streamListener = new CLIStreamListener();
		IProcess process = startShell(streamListener, monitor, getLaunchConfiguration("cordova platform "+ /*command.getCliCommand()*/ " "));
		String cordovaCommand = generateCordovaCommand(null, command, options);
		sendCordovaCommand(process, cordovaCommand, monitor);
		CLIResult result = new CLIResult(streamListener.getErrorMessage(),streamListener.getMessage());
		throwExceptionIfError(result);
		return result;
	}
	
	public CLIResult plugin(final Command command, final IProgressMonitor monitor, final String... options) throws CoreException{
		final CLIStreamListener streamListener = new CLIStreamListener();
		IProcess process = startShell(streamListener, monitor, getLaunchConfiguration("cordova plugin "+ /*command.getCliCommand()*/ " "));
		String cordovaCommand = generateCordovaCommand(null,command, options);
		sendCordovaCommand(process, cordovaCommand, monitor);
		CLIResult result = new CLIResult(streamListener.getErrorMessage(), streamListener.getMessage());
		throwExceptionIfError(result);
		return result;
	}

	private void sendCordovaCommand(final IProcess process, final String cordovaCommand,
			final IProgressMonitor monitor) throws CoreException {
		Lock lock = projectLock();
		lock.lock();
		try {
			final IStreamsProxy streamProxy = process.getStreamsProxy();
			streamProxy.write(cordovaCommand.toString());
			while (!process.isTerminated()) {
				//exit the shell after sending the command
				streamProxy.write("exit\n");
				if (monitor.isCanceled()) {
					process.terminate();
					break;
				}
				Thread.sleep(50);
			}
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, ProcessLauncher.PLUGIN_ID, "Fatal error invoking cordova CLI", e));
		} catch (InterruptedException e) {
			ProcessLauncher.logError(e, "Exception waiting for process to terminate");
		}
		finally{
			lock.unlock();
		}
	}
	
	private String generateCordovaCommand(final String command, final Command subCommand, final String... options) {
		StringBuilder builder = new StringBuilder();
		builder.append("cordova ");
		builder.append(command);
		if(subCommand != null){
			builder.append(" ");
			builder.append(subCommand.getCliCommand());
		}
		for (String string : options) {
			if(!string.isEmpty()){
				builder.append(" ");
				builder.append(string);
			}
		}
		builder.append("\n");
		return builder.toString();
	}
	
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
		IProcess process = ep.exec(commandList.toArray(new String[commandList.size()]), getWorkingDirectory(), 
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
	
	private ILaunchConfiguration getLaunchConfiguration(String label){
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType(IExternalToolConstants.ID_PROGRAM_LAUNCH_CONFIGURATION_TYPE);
		try {
			ILaunchConfiguration cfg = type.newInstance(null, "cordova");
			ILaunchConfigurationWorkingCopy wc = cfg.getWorkingCopy();
			wc.setAttribute(IProcess.ATTR_PROCESS_LABEL, label);
			cfg = wc.doSave();
			return cfg;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
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
	
	private File getWorkingDirectory(){
		final IPath wp = project.getProject().getLocation();
		if(wp == null){
			return null;
		}
		return wp.toFile();
	}
	
	private void throwExceptionIfError(CLIResult result) throws CoreException {
		if(result.hasError()){
			throw result.asCoreException();
		}
	}
	
}
