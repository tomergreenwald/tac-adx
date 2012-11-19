/*
 * TACAAAgentView.java
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

package edu.umich.eecs.tac.viewer;

import se.sics.isl.util.ConfigManager;

import javax.swing.*;
import java.util.logging.Logger;

/**
 * @author Patrick Jordan
 */
public abstract class TACAAAgentView extends JPanel implements ViewListener {
	private static final Logger log = Logger.getLogger(TACAAAgentView.class
			.getName());

	private TACAASimulationPanel parent;
	private int index;
	private String name;
	private int role;

	private String roleName;

	private Icon agentIcon;

	public TACAAAgentView() {
	}

	protected final void initialized() {
		initializeView();
	}

	protected abstract void initializeView();

	final void init(TACAASimulationPanel parent, int index, String name,
			int role, String roleName) {
		if (this.name != null) {
			throw new IllegalStateException("already initialized");
		}
		this.parent = parent;
		this.index = index;
		this.name = name;
		this.role = role;
		this.roleName = roleName;

		String iconName = getConfigProperty("image");
		Icon icon;
		if ((iconName != null) && ((icon = getIcon(iconName)) != null)) {
			// Set the background icon to use for this component. Setting
			// this means that no layout manager will be used.
			setIcon(icon);
		}

		initialized();
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public int getRole() {
		return role;
	}

	public String getRoleName() {
		return roleName;
	}

	public Icon getIcon() {
		return agentIcon;
	}

	public void setIcon(Icon agentIcon) {
		this.agentIcon = agentIcon;
	}

	/**
	 * ******************************************************************
	 * Information retrieval and utilities for sub classes
	 * ********************************************************************
	 */

	protected ConfigManager getConfig() {
		return parent.getConfig();
	}

	protected Icon getIcon(String iconName) {
		return parent.getIcon(iconName);
	}

	protected String getConfigProperty(String prop) {
		return getConfigProperty(prop, null);
	}

	protected String getConfigProperty(String prop, String defaultValue) {
		ConfigManager config = parent.getConfig();
		String value = config.getProperty(roleName + '.' + getName() + '.'
				+ prop);
		if (value == null) {
			value = config.getProperty(roleName + '.' + prop);
		}
		return value == null ? defaultValue : value;
	}

	/**
	 * Called when a new simulation day starts (if the simulation supports the
	 * day notion).
	 * 
	 * @param serverTime
	 *            the current server time
	 * @param timeUnit
	 *            the current simulation date
	 */
	protected void nextTimeUnit(long serverTime, int timeUnit) {
	}

}
