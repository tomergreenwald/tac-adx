/*
 * AgentServiceImpl.java
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
package tau.tac.adx.agentware;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import se.sics.isl.transport.Transportable;
import se.sics.tasim.aw.Agent;
import se.sics.tasim.aw.AgentService;
import se.sics.tasim.aw.Message;
import se.sics.tasim.aw.TimeListener;
import se.sics.tasim.props.SimulationStatus;
import se.sics.tasim.props.StartInfo;

import com.botbox.util.ArrayUtils;

/**
 * 
 * @author Mariano Schain
 * 
 */
public class AgentServiceImpl extends AgentService {

	private static final Logger log = Logger.getLogger(AgentServiceImpl.class
			.getName());

	private final StartInfo startInfo;
	private TimeListener[] timeListeners;
	private final SimClient client;

	private int currentTimeUnit = -1;
	private int maxTimeUnits = Integer.MAX_VALUE;

	private int simulationDay = -1;
	// Report next time unit to time listeners when first message is received
	private boolean isAwaitingNewDay = true;

	private int timerTimeUnit;
	private Timer timer;
	private TimerTask timerTask;

	public AgentServiceImpl(SimClient client, String name, Agent agent,
			Message setupMessage) {
		super(agent, name);
		this.client = client;

		this.startInfo = (StartInfo) setupMessage.getContent();
		initializeAgent();

		// The AgentService is not initialized until a simulation is being
		// started
		simulationSetup(setupMessage.getReceiver());

		int millisPerTimeUnit = this.startInfo.getSecondsPerDay() * 1000;
		if (millisPerTimeUnit > 0) {
			// Set a limit on the simulation length in case the server fails
			// to notify about simulation end but also allow for some delays.
			this.maxTimeUnits = this.startInfo.getNumberOfDays() + 1;
			setupTimer(this.startInfo.getStartTime(), millisPerTimeUnit);
		}
	}

	final void stopAgent() {
		if (timerTask != null) {
			timerTask.cancel();
		}
		if (timer != null) {
			timer.cancel();
		}
		timerTask = null;
		timer = null;
		simulationStopped();
		simulationFinished();
	}

	@Override
	protected void deliverToServer(Message message) {
		client.deliverToServer(message);
	}

	@Override
	protected void deliverToServer(int role, Transportable message) {
		log.severe("Agent can not deliver to role " + role);
	}

	@Override
	protected long getServerTime() {
		return client.getServerTime();
	}

	@Override
	protected void deliverToAgent(Message message) {
		if (isAwaitingNewDay) {
			isAwaitingNewDay = false;
			notifyTimeListeners(++simulationDay);
		}

		try {
			Transportable content = message.getContent();
			if (content instanceof SimulationStatus) {
				// Contains the current day/time unit and indicates that the
				// next message will not arrive until next day/time unit.
				simulationDay = ((SimulationStatus) content).getCurrentDate();
				isAwaitingNewDay = true;
				notifyTimeListeners(simulationDay);
			}

			super.deliverToAgent(message);
		} catch (ThreadDeath e) {
			log.log(Level.SEVERE, "message thread died", e);
			throw e;
		} catch (Throwable e) {
			log.log(Level.SEVERE, "agent could not handle message " + message,
					e);
		}
	}

	// -------------------------------------------------------------------
	// Time Listening
	// -------------------------------------------------------------------

	private void setupTimer(long startServerTime, int millisPerTimeUnit) {
		timer = new Timer();
		timerTask = new TimerTask() {
			@Override
			public void run() {
				tick();
			}
		};
		// Must handle the difference between the server time and the system
		// time
		long startTime = startServerTime + client.getTimeDiff();
		long currentServerTime = client.getServerTime();
		if (currentServerTime > startServerTime) {
			// Since the game already started we need to calculate the
			// current time unit
			currentTimeUnit = (int) ((currentServerTime - startServerTime) / millisPerTimeUnit);
			startTime += currentTimeUnit * millisPerTimeUnit;
		}
		timer.scheduleAtFixedRate(timerTask, new Date(startTime),
				millisPerTimeUnit);
	}

	private void tick() {
		notifyTimeListeners(timerTimeUnit++);
	}

	private void notifyTimeListeners(int unit) {
		boolean notify = false;
		synchronized (this) {
			if (unit > currentTimeUnit) {
				currentTimeUnit = unit;
				notify = true;
			}
		}

		if (notify) {
			log.fine("*** TIME UNIT " + currentTimeUnit);
			if (unit > maxTimeUnits) {
				// It seems like the server has failed to notify this agent
				// about the simulation end
				client.showWarning("Forced Simulation End",
						"forcing simulation to end at time unit " + unit
								+ " (max " + maxTimeUnits + " time units)");
				client.stopSimulation(this);

			} else {
				TimeListener[] listeners = timeListeners;
				if (listeners != null) {
					for (int i = 0, n = listeners.length; i < n; i++) {
						try {
							listeners[i].nextTimeUnit(currentTimeUnit);
						} catch (ThreadDeath e) {
							throw e;
						} catch (Throwable e) {
							log.log(Level.SEVERE,
									"could not deliver time unit "
											+ currentTimeUnit + " to "
											+ listeners[i], e);
						}
					}
				}
			}
		}
	}

	@Override
	protected synchronized void addTimeListener(TimeListener listener) {
		timeListeners = (TimeListener[]) ArrayUtils.add(TimeListener.class,
				timeListeners, listener);
	}

	@Override
	protected synchronized void removeTimeListener(TimeListener listener) {
		timeListeners = (TimeListener[]) ArrayUtils.remove(timeListeners,
				listener);
	}

} // AgentServiceImpl
