/*
 * Builtin.java
 *
 * COPYRIGHT  2008
 * THE REGENTS OF THE UNIVERSITY OF MICHIGAN
 * ALL RIGHTS RESERVED
 *
 * PERMISSION IS GRANTED TO USE, COPY, CREATE DERIVATIVE WORKS AND REDISTRIBUTE THIS
 * SOFTWARE AND SUCH DERIVATIVE WORKS FOR NONCOMMERCIAL EDUCATION AND RESEARCH
 * PURPOSES, SO LONG AS NO FEE IS CHARGED, AND SO LONG AS THE COPYRIGHT NOTICE
 * ABOVE, THIS GRANT OF PERMISSION, AND THE DISCLAIMER BELOW APPEAR IN ALL COPIES
 * MADE; AND SO LONG AS THE NAME OF THE UNIVERSITY OF MICHIGAN IS NOT USED IN ANY
 * ADVERTISING OR PUBLICITY PERTAINING TO THE USE OR DISTRIBUTION OF THIS SOFTWARE
 * WITHOUT SPECIFIC, WRITTEN PRIOR AUTHORIZATION.
 *
 * THIS SOFTWARE IS PROVIDED AS IS, WITHOUT REPRESENTATION FROM THE UNIVERSITY OF
 * MICHIGAN AS TO ITS FITNESS FOR ANY PURPOSE, AND WITHOUT WARRANTY BY THE
 * UNIVERSITY OF MICHIGAN OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT
 * LIMITATION THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE REGENTS OF THE UNIVERSITY OF MICHIGAN SHALL NOT BE LIABLE FOR ANY
 * DAMAGES, INCLUDING SPECIAL, INDIRECT, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, WITH
 * RESPECT TO ANY CLAIM ARISING OUT OF OR IN CONNECTION WITH THE USE OF THE SOFTWARE,
 * EVEN IF IT HAS BEEN OR IS HEREAFTER ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 */
package tau.tac.adx.sim;

import static tau.tac.adx.sim.TACAdxConstants.TYPE_MESSAGE;
import static tau.tac.adx.sim.TACAdxConstants.TYPE_WARNING;

import java.util.Random;

import se.sics.isl.transport.Transportable;
import se.sics.isl.util.ConfigManager;
import se.sics.tasim.aw.Agent;
import se.sics.tasim.aw.TimeListener;
import se.sics.tasim.is.EventWriter;

/**
 * Based on TAC/SCM version of SICS class.
 * 
 * @author Lee Callender, Patrick Jordan
 */
public abstract class Builtin extends Agent implements TimeListener {

	private final String baseConfigName;
	private ConfigManager config;
	private String configName;

	private TACAdxSimulation simulation;
	private int index;

	protected Builtin(String baseConfigName) {
		this.baseConfigName = baseConfigName;
	}

	protected TACAdxSimulation getSimulation() {
		return simulation;
	}

	// Only for debug and testing purposes.
	protected void setConfig(ConfigManager config, String configName) {
		if (this.config != null) {
			throw new IllegalStateException("config already set");
		}
		this.config = config;
		this.configName = configName;
	}

	protected int getIndex() {
		return index;
	}

	@Override
	protected final void simulationSetup() {
	}

	public final void simulationSetup(TACAdxSimulation simulation, int index) {
		this.index = index;
		this.simulation = simulation;
		this.config = simulation.getConfig();
		this.configName = getName();
		setup();
	}

	@Override
	protected final void simulationStopped() {
		stopped();
	}

	@Override
	protected final void simulationFinished() {
		try {
			shutdown();
		} finally {
			simulation = null;
		}
	}

	// Note: for the properties to be stored in the server configuration
	// that is saved in the simulation log all properties must be
	// accessed during simulation setup!
	protected String getProperty(String name) {
		return getProperty(name, null);
	}

	// Note: for the properties to be stored in the server configuration
	// that is saved in the simulation log all properties must be
	// accessed during simulation setup!
	protected String getProperty(String name, String defaultValue) {
		String value = config.getProperty(baseConfigName + configName + '.'
				+ name);
		if (value == null) {
			value = config.getProperty(baseConfigName + name, defaultValue);
		}
		return value;
	}

	protected String[] getPropertyAsArray(String name) {
		return getPropertyAsArray(name, null);
	}

	protected String[] getPropertyAsArray(String name, String defaultValue) {
		String[] value = config.getPropertyAsArray(baseConfigName + configName
				+ '.' + name);
		if (value == null) {
			value = config.getPropertyAsArray(baseConfigName + name,
					defaultValue);
		}
		return value;
	}

	// Note: for the properties to be stored in the server configuration
	// that is saved in the simulation log all properties must be
	// accessed during simulation setup!
	protected int getPropertyAsInt(String name, int defaultValue) {
		String property = baseConfigName + name;
		int value = config.getPropertyAsInt(property, defaultValue);

		property = baseConfigName + configName + '.' + name;
		value = config.getPropertyAsInt(property, value);

		return value;
	}

	protected int[] getPropertyAsIntArray(String name) {
		return getPropertyAsIntArray(name, null);
	}

	protected int[] getPropertyAsIntArray(String name, String defaultValue) {
		String[] value = getPropertyAsArray(name, defaultValue);
		if (value != null) {
			int[] intValue = new int[value.length];
			for (int i = 0, n = value.length; i < n; i++) {
				intValue[i] = Integer.parseInt(value[i]);
			}
			return intValue;
		}
		return null;
	}

	// Note: for the properties to be stored in the server configuration
	// that is saved in the simulation log all properties must be
	// accessed during simulation setup!
	protected long getPropertyAsLong(String name, long defaultValue) {
		String property = baseConfigName + name;
		long value = config.getPropertyAsLong(property, defaultValue);

		property = baseConfigName + configName + '.' + name;
		value = config.getPropertyAsLong(property, value);

		return value;
	}

	// Note: for the properties to be stored in the server configuration
	// that is saved in the simulation log all properties must be
	// accessed during simulation setup!
	protected float getPropertyAsFloat(String name, float defaultValue) {
		String property = baseConfigName + name;
		float value = config.getPropertyAsFloat(property, defaultValue);

		property = baseConfigName + configName + '.' + name;
		value = config.getPropertyAsFloat(property, value);

		return value;
	}

	// Note: for the properties to be stored in the server configuration
	// that is saved in the simulation log all properties must be
	// accessed during simulation setup!
	protected double getPropertyAsDouble(String name, double defaultValue) {
		String property = baseConfigName + name;
		double value = config.getPropertyAsDouble(property, defaultValue);

		property = baseConfigName + configName + '.' + name;
		value = config.getPropertyAsDouble(property, value);

		return value;
	}

	protected int getNumberOfAdvertisers() {
		return simulation.getNumberOfAdvertisers();
	}

	protected Random getRandom() {
		return simulation.getRandom();
	}

	protected String getAgentName(String agentAddress) {
		return simulation.getAgentName(agentAddress);
	}

	protected EventWriter getEventWriter() {
		return simulation.getEventWriter();
	}

	protected void sendEvent(String message) {
		simulation.getEventWriter().dataUpdated(index, TYPE_MESSAGE, message);
	}

	protected void sendWarningEvent(String message) {
		simulation.getEventWriter().dataUpdated(index, TYPE_WARNING, message);
	}

	protected String[] getAdvertiserAddresses() {
		return simulation.getAdvertiserAddresses();
	}
	
	protected String[] getAdxAdvertiserAddresses() {
		return simulation.getAdxAdvertiserAddresses();
	}

	final void sendMessage(String sender, String receiver, Transportable content) {

	}

	protected abstract void setup();

	protected abstract void stopped();

	protected abstract void shutdown();

} // Builtin
