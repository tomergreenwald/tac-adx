/**
 * SICS ISL Java Utilities
 * http://www.sics.se/tac/    tac-dev@sics.se
 *
 * Copyright (c) 2001-2003 SICS AB. All rights reserved.
 *
 * SICS grants you the right to use, modify, and redistribute this
 * software for noncommercial purposes, on the conditions that you:
 * (1) retain the original headers, including the copyright notice and
 * this text, (2) clearly document the difference between any derived
 * software and the original, and (3) acknowledge your use of this
 * software in pertaining publications and reports.  SICS provides
 * this software "as is", without any warranty of any kind.  IN NO
 * EVENT SHALL SICS BE LIABLE FOR ANY DIRECT, SPECIAL OR INDIRECT,
 * PUNITIVE, INCIDENTAL OR CONSEQUENTIAL LOSSES OR DAMAGES ARISING OUT
 * OF THE USE OF THE SOFTWARE.
 *
 * -----------------------------------------------------------------
 *
 * ArgumentManager
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Tue Apr 08 22:08:32 2003
 * Updated : $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb 2008) $
 *           $Revision: 3766 $
 *
 * TODO:
 * - handling of non-option arguments (ex "javac file.java")
 * - handling of too long argument descriptions
 * - better API
 */
package se.sics.isl.util;

/**
 */
public class ArgumentManager extends ConfigManager {

	private static final int OPTION = 0;
	private static final int ARG_NAME = 1;
	private static final int DESCRIPTION = 2;
	private static final int TYPE = 3;
	private static final int PARTS = 4;

	private String programName;
	private String[] originalArguments;
	private Object[] descriptions;
	private int descriptionCount = 0;

	private int columnWidth = 72;

	private String[] arguments;
	private int argLen;

	public ArgumentManager(String programName, String[] args) {
		this.programName = programName;
		this.originalArguments = args;
	}

	public ArgumentManager(ConfigManager parent, String programName,
			String[] args) {
		super(parent);
		this.programName = programName;
		this.originalArguments = args;
	}

	public void addOption(String option, String argName, String desc) {
		addOption(option, argName, desc, String.class);
	}

	public void addOption(String option, String desc) {
		addOption(option, null, desc, Boolean.class);
	}

	public void addHelp(String option, String desc) {
		addOption(option, null, desc, null);
	}

	public void addHelp(String option) {
		addOption(option, null, null, null);
	}

	private void addOption(String option, String argName, String desc,
			Class type) {
		int index = descriptionCount * PARTS;
		if (descriptions == null) {
			descriptions = new Object[10 * PARTS];
		} else if (index == descriptions.length) {
			Object[] tmp = new Object[index + 10 * PARTS];
			System.arraycopy(descriptions, 0, tmp, 0, index);
			descriptions = tmp;
		}
		descriptions[index + OPTION] = option;
		descriptions[index + ARG_NAME] = argName;
		descriptions[index + DESCRIPTION] = desc;
		descriptions[index + TYPE] = type;
		descriptionCount++;
	}

	public void validateArguments() {
		if (originalArguments == null || descriptionCount == 0) {
			// Nothing to validate
			return;
		}

		String[] arguments = new String[originalArguments.length];
		int argLen = 0;

		for (int i = 0, n = originalArguments.length; i < n; i++) {
			String a = originalArguments[i];
			if (a.length() > 1 && a.charAt(0) == '-') {
				// Argument detected
				a = a.substring(1);
				int index = keyValuesIndexOf(descriptions, PARTS, 0,
						descriptionCount * PARTS, a);
				if (index < 0) {
					System.err.println("illegal argument '" + a + '\'');
					usage(1);
					return;
				}

				if (argLen + 2 >= arguments.length) {
					String[] tmp = new String[argLen + 4];
					System.arraycopy(arguments, 0, tmp, 0, arguments.length);
					arguments = tmp;
				}
				arguments[argLen++] = a;

				Object argumentType = descriptions[index + TYPE];
				if (argumentType == Boolean.class) {
					arguments[argLen++] = "true";

				} else if (argumentType == null) {
					// Usage help request detected
					usage(0);
					return;

				} else if (++i >= n) {
					System.err.println("argument '" + a + "' needs a value");
					usage(1);
					return;

				} else {
					arguments[argLen++] = originalArguments[i];
				}
			} else {
				// Value not permitted here. Only argument, value tuples
				System.err.println("illegal argument '" + a + '\'');
				usage(1);
				return;
			}
		}

		if (argLen > 0) {
			this.arguments = arguments;
			this.argLen = argLen;
		} else {
			this.arguments = null;
			this.argLen = 0;
		}
	}

	public void finishArguments() {
		this.originalArguments = null;
		this.descriptionCount = 0;
		this.descriptions = null;
		this.programName = null;
	}

	public void usage(int error) {
		if (descriptionCount > 0 && programName != null) {
			int len = 0;
			int splitLen = columnWidth / 2;
			for (int i = 0, n = descriptionCount * PARTS; i < n; i += PARTS) {
				String option = (String) descriptions[i + OPTION];
				String argName = (String) descriptions[i + ARG_NAME];
				int w = option.length()
						+ (argName == null ? 0 : (argName.length() + 3));
				if (w > len && w < splitLen) {
					len = w;
				}
			}
			len += 4;

			System.out.println("Usage: " + programName + " [-options]");
			System.out.println("where options include:");
			for (int i = 0, n = descriptionCount * PARTS; i < n; i += PARTS) {
				String desc = (String) descriptions[i + DESCRIPTION];
				if (desc != null && desc.length() > 0) {
					String option = (String) descriptions[i + OPTION];
					String argName = (String) descriptions[i + ARG_NAME];
					int w;
					if (argName == null) {
						System.out.print("  -" + option);
						w = option.length() + 3;
					} else {
						System.out.print("  -" + option + " <" + argName + '>');
						w = option.length() + argName.length() + 3 + 3;
					}
					if (w > splitLen) {
						System.out.println();
						w = 0;
					}
					for (int j = w; j < len; j++) {
						System.out.print(' ');
					}
					// /TODO This should break down long descriptions to several
					// rows
					// based on columnWidth.
					System.out.println(desc);
				}
			}
		}

		System.exit(error);
	}

	// -------------------------------------------------------------------
	// Argument handling
	// -------------------------------------------------------------------

	public boolean hasArgument(String name) {
		return keyValuesIndexOf(arguments, 2, 0, argLen, name) >= 0;
	}

	public String getArgument(String name) {
		return getArgument(name, null);
	}

	public String getArgument(String name, String defaultValue) {
		if (argLen == 0) {
			return defaultValue;
		}

		int index = keyValuesIndexOf(arguments, 2, 0, argLen, name);
		return index < 0 ? defaultValue : arguments[index + 1];
	}

	public int getArgumentAsInt(String name, int defaultValue) {
		String value = getArgument(name, null);
		return value != null ? parseInt(name, value, defaultValue)
				: defaultValue;
	}

	public long getArgumentAsLong(String name, long defaultValue) {
		String value = getArgument(name, null);
		return value != null ? parseLong(name, value, defaultValue)
				: defaultValue;
	}

	public float getArgumentAsFloat(String name, float defaultValue) {
		String value = getArgument(name, null);
		return value != null ? parseFloat(name, value, defaultValue)
				: defaultValue;
	}

	public double getArgumentAsDouble(String name, double defaultValue) {
		String value = getArgument(name, null);
		return value != null ? parseDouble(name, value, defaultValue)
				: defaultValue;
	}

	public boolean getArgumentAsBoolean(String name, boolean defaultValue) {
		String value = getArgument(name, null);
		return value != null ? parseBoolean(name, value, defaultValue)
				: defaultValue;
	}

	public void removeArgument(String name) {
		// removeArgument(name, true);
		// }

		// private void removeArgument(String name, boolean notify) {
		if (argLen > 0) {
			int index = keyValuesIndexOf(arguments, 2, 0, argLen, name);
			if (index >= 0) {
				String oldValue = arguments[index + 1];
				argLen -= 2;
				arguments[index] = arguments[argLen];
				arguments[index + 1] = arguments[argLen + 1];

				// if (notify) {
				// String newValue = getProperty(name);
				// if (oldValue != newValue
				// && (newValue == null || !newValue.equals(oldValue))) {
				// notifyObservers(name, newValue);
				// }
				// }
			}
		}
	}

	// -------------------------------------------------------------------
	// Properties handling
	// -------------------------------------------------------------------

	public String getProperty(String name, String defaultValue) {
		String value = getArgument(name, null);
		return (value == null) ? super.getProperty(name, defaultValue) : value;
	}

	public void setProperty(String name, String value) {
		// Remove any matching argument because the value has changed
		removeArgument(name);
		// removeArgument(name, false);
		super.setProperty(name, value);
	}

	// -------------------------------------------------------------------
	// Utilities
	// -------------------------------------------------------------------

	private int keyValuesIndexOf(Object[] array, int nth, int start, int end,
			Object key) {
		for (int i = start; i < end; i += nth) {
			if (key.equals(array[i])) {
				return i;
			}
		}
		return -1;
	}

	// public static void main(String[] args) {
	// ArgumentManager a = new ArgumentManager("ArgumentManager", args);
	// a.addOption("config", "configfile", "set the config file to use");
	// a.addOption("serverName", "serverName", "set the server name");
	// a.addOption("consoleLogLevel", "level", "set the console log level");
	// a.addOption("fileLogLevel", "level", "set the file log level");
	// a.addOption("useGUI", "show gui");
	// a.addOption("test", "levelsellerenmassaannatvldigtlngt",
	// "test med lng kommentar");
	// a.addHelp("h", "show this help message");
	// a.addHelp("help");

	// a.validateArguments();

	// a.finishArguments();

	// System.out.println("config=" + a.getProperty("config"));
	// System.out.println("serverName=" + a.getProperty("serverName"));
	// System.out.println("gui(arg)=" + a.hasArgument("useGUI"));
	// System.out.println("gui=" + a.getProperty("useGUI"));
	// System.out.println("test=" + a.getPropertyAsInt("test", 0));
	// }

} // ArgumentManager
