/*
 * OverviewPanel.java
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

package edu.umich.eecs.tac.viewer.role;

import com.botbox.util.ArrayUtils;
import edu.umich.eecs.tac.viewer.TACAASimulationPanel;
import edu.umich.eecs.tac.viewer.TACAAViewerConstants;
import edu.umich.eecs.tac.viewer.ViewAdaptor;
import static edu.umich.eecs.tac.viewer.ViewerChartFactory.createDaySeriesChartWithColors;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import se.sics.tasim.viewer.TickListener;
import tau.tac.adx.sim.TACAdxConstants;

import javax.swing.*;
import java.awt.*;

/**
 * @author Patrick Jordan
 */
public class OverviewPanel extends SimulationTabPanel {
	private XYSeriesCollection seriescollection;

	private int[] agents;
	private int[] roles;
	private int[] participants;
	private XYSeries[] series;
	private String[] names;
	private int agentCount;
	private int currentDay;

	public OverviewPanel(TACAASimulationPanel simulationPanel) {
		super(simulationPanel);
        setBorder(BorderFactory.createTitledBorder("Advertiser Profits"));
        setBackground(TACAAViewerConstants.CHART_BACKGROUND);
        
		agents = new int[0];
		roles = new int[0];
		participants = new int[0];
		series = new XYSeries[0];
		names = new String[0];
		currentDay = 0;
		initialize();

		getSimulationPanel().addTickListener(new DayListener());
		getSimulationPanel().addViewListener(new BankStatusListener());
	}

	protected void initialize() {
		setLayout(new GridLayout(1, 1));
		seriescollection = new XYSeriesCollection();
		JFreeChart chart = createDaySeriesChartWithColors(null, seriescollection, true);
		ChartPanel chartpanel = new ChartPanel(chart, false);
		chartpanel.setMouseZoomable(true, false);

		add(chartpanel);
	}

	protected void addAgent(int agent) {
		int index = ArrayUtils.indexOf(agents, 0, agentCount, agent);
		if (index < 0) {
			doAddAgent(agent);
		}
	}

	private int doAddAgent(int agent) {
		if (agentCount == participants.length) {
			int newSize = agentCount + 8;
			agents = ArrayUtils.setSize(agents, newSize);
			roles = ArrayUtils.setSize(roles, newSize);
			participants = ArrayUtils.setSize(participants, newSize);
			series = (XYSeries[]) ArrayUtils.setSize(series, newSize);
			names = (String[]) ArrayUtils.setSize(names, newSize);
		}

		agents[agentCount] = agent;

		return agentCount++;
	}

	private void setAgent(int agent, int role, String name, int participantID) {
		addAgent(agent);

		int index = ArrayUtils.indexOf(agents, 0, agentCount, agent);
		roles[index] = role;
		names[index] = name;
		participants[index] = participantID;

		if (series[index] == null && TACAdxConstants.ADVERTISER == roles[index]) {
			series[index] = new XYSeries(name);
			seriescollection.addSeries(series[index]);
		}
	}

	protected void participant(int agent, int role, String name,
			int participantID) {
		setAgent(agent, role, name, participantID);
	}

	protected void dataUpdated(int agent, int type, double value) {
		int index = ArrayUtils.indexOf(agents, 0, agentCount, agent);
		if (index < 0 || series[index] == null
				|| type != TACAdxConstants.DU_BANK_ACCOUNT) {
			return;
		}

		series[index].addOrUpdate(currentDay, value);
	}

	protected void tick(long serverTime) {
	}

	protected void simulationTick(long serverTime, int simulationDate) {
		currentDay = simulationDate;
	}

	protected class DayListener implements TickListener {

		public void tick(long serverTime) {
			OverviewPanel.this.tick(serverTime);
		}

		public void simulationTick(long serverTime, int simulationDate) {
			OverviewPanel.this.simulationTick(serverTime, simulationDate);
		}
	}

	protected class BankStatusListener extends ViewAdaptor {

		public void dataUpdated(int agent, int type, double value) {
			OverviewPanel.this.dataUpdated(agent, type, value);
		}

		public void participant(int agent, int role, String name, int participantID) {
			OverviewPanel.this.participant(agent, role, name, participantID);
		}
	}
}