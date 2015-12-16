package org.eclipse.wst.jsdt.js.process.launcher.core;

public class CLICommand {
	
	private String toolName;
	private String command;
	private String subCommand;
	private String[] options;
	
	public CLICommand(String toolName, String command, String subCommand, String[] options) {
		this.toolName = toolName;
		this.command = command;
		this.subCommand = subCommand;
		this.options = options;
	}
		
	// TODO: need to improve command generation
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getToolName());
		builder.append(" ");
		builder.append(getCommand());
		if (getSubCommand() != null) {
			builder.append(" ");
			builder.append(getSubCommand());
		}
		if (getOptions() != null) {
			for (String string : getOptions()) {
				if (!string.isEmpty()) {
					builder.append(" ");
					builder.append(string);
				}
			}
		}
		builder.append("\n");
		builder.append("exit\n");
		return builder.toString();
	}
	
//	public String generate() {
//
//	}

	public String getToolName() {
		return toolName;
	}

	public String getCommand() {
		return command;
	}


	public String getSubCommand() {
		return subCommand;
	}

	public String[] getOptions() {
		return options;
	}

}
