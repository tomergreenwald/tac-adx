/*
 * DummyAdvertiserTest.java
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
package edu.umich.eecs.tac.agents;


import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import se.sics.tasim.aw.Message;
import se.sics.tasim.props.SimulationStatus;
import edu.umich.eecs.tac.props.*;
import edu.umich.eecs.tac.sim.DummySimulationAgent;

/**
 * @author Ben Cassell
 */
public class DummyAdvertiserTest {

	private DummyAdvertiser dummy;
	private Message message;
	private DummySimulationAgent as;
	private RetailCatalog rc;
	private QueryReport qr;

	@Before
	public void setUp() {
		dummy = new DummyAdvertiser();
		rc = new RetailCatalog();
		rc.addProduct(new Product("man", "com"));
		as = new DummySimulationAgent(dummy, "dummy");
		as.setup();
		dummy.simulationSetup();
		message = new Message("dummy", rc);
		dummy.messageReceived(message);
		dummy.messageReceived(message);
	}

	@Test
	public void testMessageReceived() {
		qr = new QueryReport();
		qr.addQuery(new Query("man", "com"));
		message = new Message("dummy", qr);
		SalesReport sr = new SalesReport();
		sr.addQuery(new Query("man", "com"));
		message = new Message("dummy", sr);
		dummy.messageReceived(message);
		SimulationStatus ss = new SimulationStatus();
		message = new Message("dummy", ss);
		dummy.messageReceived(message);
		AdvertiserInfo ai = new AdvertiserInfo();
		message = new Message("dummy", ai);
		dummy.messageReceived(message);
		BankStatus bs = new BankStatus();
		message = new Message("dummy", bs);
		dummy.messageReceived(message);
		dummy.messageReceived(null);
	}

	@Test
	public void testSimulationSetup() {
		QueryReport qr = new QueryReport();
		qr.addQuery(new Query("man", "com"));
		message = new Message("dummy", qr);
		dummy.messageReceived(message);
		dummy.simulationSetup();
	}

	@Test
	public void testSimulationFinished() {
		dummy.simulationFinished();
	}

	@Test
	public void testSendBidAndAds() {
		dummy.sendBidAndAds();
		qr = new QueryReport();
		qr.addQuery(new Query("man", "com"));
		message = new Message("dummy", qr);
		AdvertiserInfo ai = new AdvertiserInfo();
		ai.setPublisherId("dumbpub");
		message = new Message("dummy", ai);
		dummy.messageReceived(message);
		dummy.sendBidAndAds();
	}

}
