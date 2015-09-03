/**
 * TAC Supply Chain Management Simulator
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
 * SimulationManager
 *
 * Author  : Joakim Eriksson, Niclas Finne, Sverker Janson
 * Created : Mon Oct 14 14:24:16 2002
 * Updated : $Date: 2008-04-04 12:26:47 -0500 (Fri, 04 Apr 2008) $
 *           $Revision: 3963 $
 */
package se.sics.tasim.sim;

import java.io.File;

import se.sics.isl.util.ConfigManager;
import se.sics.tasim.is.SimulationInfo;

public abstract class SimulationManager {

	private String name;
	private Admin admin;

	protected SimulationManager() {
	}

	final void init(Admin admin, String name) {
		if (name == null || admin == null) {
			throw new NullPointerException();
		}
		this.name = name;
		this.admin = admin;
		init();
	}

	protected String getName() {
		return name;
	}

	protected ConfigManager getConfig() {
		return admin.getConfig();
	}

	protected ConfigManager loadSimulationConfig(String simulationType) {
		checkSimulationType(simulationType);

		ConfigManager config = getConfig();
		String configFile = config.getProperty("manager." + getName() + '.'
				+ simulationType + ".config", simulationType + "_sim.conf");
		config = new ConfigManager(config);
		config.loadConfiguration(new File(admin.getConfigDirectory(),
				configFile).getAbsolutePath());
		return config;
	}

	protected void checkSimulationType(String simulationType) {
		for (int i = 0, n = simulationType.length(); i < n; i++) {
			char c = simulationType.charAt(i);
			if (!Character.isLetterOrDigit(c)) {
				throw new IllegalArgumentException(
						"simulation type may only include "
								+ "letters and digits: " + simulationType);
			}
		}
	}

	// -------------------------------------------------------------------
	// Utilities
	// -------------------------------------------------------------------

	/**
	 * Register the specified simulation type.
	 * 
	 * @param type
	 *            the simulation type to register
	 */
	protected void registerType(String type) {
		checkSimulationType(type);
		admin.addSimulationManager(type, this);
	}

	protected SimulationInfo createSimulationInfo(String type, String params, int length) {
		return new SimulationInfo(admin.getNextUniqueSimulationID(), type,params, length);
	}

	// -------------------------------------------------------------------
	// Methods for SimulationManager sub classes
	// -------------------------------------------------------------------

	/**
	 * Initializes this simulation manager. Recommended actions is to register
	 * all supported simulation types.
	 */
	protected abstract void init();

    public Admin getAdmin() {
        return admin;
    }

    public abstract SimulationInfo createSimulationInfo(String type,String params);

	public abstract boolean join(int agent, int role, SimulationInfo info);

	public abstract String getSimulationRoleName(String type, int simRole);

	public abstract int getSimulationRoleID(String type, String simRole);

	public abstract int getSimulationLength(String type, String params);

	public abstract Simulation createSimulation(SimulationInfo info);

} // SimulationManager
