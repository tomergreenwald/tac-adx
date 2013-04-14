/*
 * MessageSender.java
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

import java.util.logging.Level;
import java.util.logging.Logger;

import se.sics.tasim.aw.Message;

import com.botbox.util.ArrayQueue;

/**
 * 
 * @author Mariano Schain
 * 
 */
public class MessageSender extends Thread {

	private static final Logger log = Logger.getLogger(MessageSender.class
			.getName());

	private final ServerConnection connection;
	private final ArrayQueue messageQueue = new ArrayQueue();
	private boolean isClosed = false;

	public MessageSender(ServerConnection connection, String name) {
		super(name);
		this.connection = connection;
		start();
	}

	public boolean isClosed() {
		return isClosed;
	}

	public synchronized void close() {
		if (!isClosed) {
			this.isClosed = true;
			messageQueue.clear();
			messageQueue.add(null);
			notify();
		}
	}

	public synchronized boolean addMessage(Message message) {
		if (isClosed) {
			return false;
		}
		messageQueue.add(message);
		notify();
		return true;
	}

	private synchronized Message nextMessage() {
		while (messageQueue.size() == 0) {
			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		return (Message) messageQueue.remove(0);
	}

	// -------------------------------------------------------------------
	// Message sending handling
	// -------------------------------------------------------------------

	@Override
	public void run() {
		do {
			Message msg = null;
			try {
				msg = nextMessage();
				if (msg != null) {
					connection.deliverMessage(msg);
				}

			} catch (ThreadDeath e) {
				log.log(Level.SEVERE, "message thread died", e);
				throw e;

			} catch (Throwable e) {
				log.log(Level.SEVERE, "could not handle message " + msg, e);
			}
		} while (!isClosed);
	}

} // MessageSender

