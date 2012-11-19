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
 * ConfigManager
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Fri Oct 11 15:24:14 2002
 * Updated : $Date: 2008-02-24 11:37:48 -0600 (Sun, 24 Feb 2008) $
 *           $Revision: 3766 $
 */
package se.sics.isl.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class ConfigManager {

	private static Logger logCache;

	protected final ConfigManager parent;
	protected final Properties properties = new Properties();

	public ConfigManager() {
		this(null);
	}

	public ConfigManager(ConfigManager parent) {
		this.parent = parent;
	}

	// -------------------------------------------------------------------
	// Config file handling
	// -------------------------------------------------------------------

	public boolean loadConfiguration(String configFile) {
		try {
			InputStream input = new BufferedInputStream(new FileInputStream(
					configFile));
			try {
				loadConfiguration(input);
			} finally {
				input.close();
			}
			return true;
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			throw (IllegalArgumentException) new IllegalArgumentException(
					"could not read config file '" + configFile + '\'')
					.initCause(e);
		}
	}

	public boolean loadConfiguration(URL configURL) {
		try {
			InputStream input = new BufferedInputStream(configURL.openStream());
			try {
				loadConfiguration(input);
			} finally {
				input.close();
			}
			return true;
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			throw (IllegalArgumentException) new IllegalArgumentException(
					"could not read config file '" + configURL + '\'')
					.initCause(e);
		}
	}

	public void loadConfiguration(InputStream input) throws IOException {
		synchronized (properties) {
			properties.clear();
			properties.load(input);
		}
	}

	// -------------------------------------------------------------------
	// Properties handling
	// -------------------------------------------------------------------

	/**
	 * Returns an enumeration of the property names. Does not include inherited
	 * properties.
	 * 
	 * @return an enumeration of the non-inherited property names
	 */
	public Enumeration names() {
		return properties.keys();
	}

	public String getProperty(String name) {
		return getProperty(name, null);
	}

	public String getProperty(String name, String defaultValue) {
		String value = properties.getProperty(name);
		if (value == null || value.length() == 0) {
			value = parent != null ? parent.getProperty(name, defaultValue)
					: defaultValue;
		}
		return value;
	}

	public void setProperty(String name, String value) {
		properties.setProperty(name, value);
		// notifyObservers(name, value);
	}

	public String[] getPropertyAsArray(String name) {
		return getPropertyAsArray(name, null);
	}

	public String[] getPropertyAsArray(String name, String defaultValue) {
		String valueList = getProperty(name, defaultValue);
		if (valueList != null) {
			StringTokenizer tok = new StringTokenizer(valueList, ", \t");
			int len = tok.countTokens();
			if (len > 0) {
				String[] names = new String[len];
				for (int i = 0; i < len; i++) {
					names[i] = tok.nextToken();
				}
				return names;
			}
		}
		return null;
	}

	public int getPropertyAsInt(String name, int defaultValue) {
		String value = getProperty(name, null);
		return value != null ? parseInt(name, value, defaultValue)
				: defaultValue;
	}

	public long getPropertyAsLong(String name, long defaultValue) {
		String value = getProperty(name, null);
		return value != null ? parseLong(name, value, defaultValue)
				: defaultValue;
	}

	public float getPropertyAsFloat(String name, float defaultValue) {
		String value = getProperty(name, null);
		return value != null ? parseFloat(name, value, defaultValue)
				: defaultValue;
	}

	public double getPropertyAsDouble(String name, double defaultValue) {
		String value = getProperty(name, null);
		return value != null ? parseDouble(name, value, defaultValue)
				: defaultValue;
	}

	public boolean getPropertyAsBoolean(String name, boolean defaultValue) {
		String value = getProperty(name, null);
		return value != null ? parseBoolean(name, value, defaultValue)
				: defaultValue;
	}

	// public void setProperty(String name, boolean value) {
	// setProperty(name, value ? "true" : "false");
	// }

	protected int parseInt(String name, String value, int defaultValue) {
		try {
			return Integer.parseInt(value);
		} catch (Exception e) {
			// Do not create the log before it is needed
			if (logCache == null) {
				logCache = Logger.getLogger(ConfigManager.class.getName());
			}
			logCache.warning("config '" + name + "' has a non-integer value '"
					+ value + '\'');
		}
		return defaultValue;
	}

	protected long parseLong(String name, String value, long defaultValue) {
		try {
			return Long.parseLong(value);
		} catch (Exception e) {
			// Do not create the log before it is needed
			if (logCache == null) {
				logCache = Logger.getLogger(ConfigManager.class.getName());
			}
			logCache.warning("config '" + name + "' has a non-long value '"
					+ value + '\'');
		}
		return defaultValue;
	}

	protected float parseFloat(String name, String value, float defaultValue) {
		try {
			return Float.parseFloat(value);
		} catch (Exception e) {
			// Do not create the log before it is needed
			if (logCache == null) {
				logCache = Logger.getLogger(ConfigManager.class.getName());
			}
			logCache.warning("config '" + name + "' has a non-float value '"
					+ value + '\'');
		}
		return defaultValue;
	}

	protected double parseDouble(String name, String value, double defaultValue) {
		try {
			return Double.parseDouble(value);
		} catch (Exception e) {
			// Do not create the log before it is needed
			if (logCache == null) {
				logCache = Logger.getLogger(ConfigManager.class.getName());
			}
			logCache.warning("config '" + name + "' has a non-double value '"
					+ value + '\'');
		}
		return defaultValue;
	}

	protected boolean parseBoolean(String name, String value,
			boolean defaultValue) {
		return "true".equals(value) || "yes".equals(value) || "1".equals(value);
	}

	// -------------------------------------------------------------------
	// Utilities for creating instances of a specified type based on
	// configuration
	// -------------------------------------------------------------------

	/**
	 * Instantiates a number of objects based on names and types found in this
	 * configuration. The property "&lt;configName&gt;.names" is parsed as a
	 * comma separated list of object names. For each found object name,
	 * objectName, an instance is created from the class specified as
	 * "&lt;configName&gt;.&lt;objectName&gt;.class" (or the default class
	 * "&lt;configName&gt;.class". Each instance is checked to be of the
	 * specified type and an array of the specified type is returned with all
	 * the objects.
	 * 
	 * @param configName
	 *            the configuration name
	 * @param type
	 *            the type of the objects
	 * @return the instantiated objects or <CODE>null</CODE> if no objects was
	 *         specified
	 * @exception IllegalConfigurationException
	 *                if an error occurs
	 */
	public Object[] createInstances(String configName, Class type)
			throws IllegalConfigurationException {
		return createInstances(configName, type, getPropertyAsArray(configName
				+ ".names"));
	}

	/**
	 * Instantiates a number of objects based on the specified names and types
	 * found in this configuration. For each specified object name, objectName,
	 * an instance is created from the class specified as
	 * "&lt;configName&gt;.&lt;objectName&gt;.class" (or the default class
	 * "&lt;configName&gt;.class". Each instance is checked to be of the
	 * specified type and an array of the specified type is returned with all
	 * the objects.
	 * 
	 * @param configName
	 *            the configuration name
	 * @param type
	 *            the type of the objects
	 * @param names
	 *            a list of names of object to instantiate
	 * @return the instantiated objects or <CODE>null</CODE> if no objects was
	 *         specified
	 * @exception IllegalConfigurationException
	 *                if an error occurs
	 */
	public Object[] createInstances(String configName, Class type,
			String[] names) throws IllegalConfigurationException {
		if (names == null || names.length == 0) {
			return null;
		}

		String className = null;
		String iName = null;
		String defaultClassName = getProperty(configName + ".class");
		try {
			Object[] vector = (Object[]) java.lang.reflect.Array.newInstance(
					type, names.length);
			for (int i = 0, n = names.length; i < n; i++) {
				iName = names[i];
				className = getProperty(configName + '.' + iName + ".class",
						defaultClassName);
				if (className == null) {
					throw new IllegalConfigurationException(
							"no class definition for " + configName + ' '
									+ iName);
				}
				vector[i] = Class.forName(className).newInstance();
			}
			return vector;

		} catch (IllegalConfigurationException e) {
			throw e;
		} catch (Exception e) {
			throw (IllegalConfigurationException) new IllegalConfigurationException(
					"could not create " + configName + ' ' + iName + " '"
							+ className + '\'').initCause(e);
		}
	}

	// -------------------------------------------------------------------
	// Utilities
	// -------------------------------------------------------------------

	/**
	 * Compares two versions of the format "major.minor.micro". A version with
	 * value <code>null</code> is always considered older than a version not
	 * having the value <code>null</code>.
	 * 
	 * @param version1
	 *            the first version to compare
	 * @param version2
	 *            the second version to compare
	 * @return the value <code>0</code> if the versions are identical; a value
	 *         less than <code>0</code> if the first version is older than the
	 *         second version; and a value larger than <code>0</code> if the
	 *         first version is newer than the second version.
	 */
	public static int compareVersion(String version1, String version2) {
		if (version1 == null) {
			return version2 == null ? 0 : -1;
		}

		if (version2 == null) {
			return 1;
		}

		int s1 = 0;
		int s2 = 0;
		int l1 = version1.length();
		int l2 = version2.length();
		do {
			int i1 = version1.indexOf('.', s1);
			int i2 = version2.indexOf('.', s2);

			if (i1 < 0) {
				i1 = l1;
			}
			if (i2 < 0) {
				i2 = l2;
			}

			int c = compareVersion(version1, s1, i1, version2, s2, i2);
			if (c != 0) {
				// Last comparison or the subversions were not equal. Time to
				// return the result the result.
				return c;
			}

			// Same subversion. Continue with the next subversion.
			s1 = i1 + 1;
			s2 = i2 + 1;

		} while (s1 < l1 || s2 < l2);
		return 0;
	}

	private static int compareVersion(String version1, int s1, int e1,
			String version2, int s2, int e2) {
		int e1len = e1 - s1;
		int e2len = e2 - s2;
		int len = e1len > e2len ? e1len : e2len;
		for (int i = 0, pos1 = e1 - len, pos2 = e2 - len; i < len; i++, pos1++, pos2++) {
			int c1 = (pos1 < s1 || pos1 >= e1) ? '0' : version1.charAt(pos1);
			int c2 = (pos2 < s2 || pos2 >= e2) ? '0' : version2.charAt(pos2);
			if (c1 < c2) {
				return -1;
			} else if (c1 > c2) {
				return 1;
			}
		}
		return 0;
	}

	// public static void main(String[] args) {
	// System.out.println("compareVersion '" + args[0] + "' to '" + args[1]
	// + "' = " + compareVersion(args[0], args[1]));
	// }

} // ConfigManager
